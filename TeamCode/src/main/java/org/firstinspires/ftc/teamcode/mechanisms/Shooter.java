package org.firstinspires.ftc.teamcode.mechanisms;

import android.graphics.Color;
import android.util.Size;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shooter {
    private Telemetry telemetry;

    // ---------- AprilTag ----------
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private List<AprilTagDetection> detectedTags = new ArrayList<>();

    private final List<AprilTagDetection> allianceColorTags = new ArrayList<>();

    // ---------- Intake & Shooting Mechanism + Backboard
    private NormalizedColorSensor colorSensor;

    private final float[] hsv = new float[3];
    private DcMotor shooter;

    private Servo backboard;
    private final double NECTAR_BB_POSITION = 0.5;
    private final double POLLEN_BB_POSITION = 0.3;

    private final double STOP_POSITION = 0.2;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // ---------- AprilTag ----------
        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(false)
                .setDrawTagOutline(false)
                .setDrawAxes(false)
                .setDrawCubeProjection(false)
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)
                .build();

        aprilTagProcessor.setDecimation(3);

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hwMap.get(WebcamName.class, "webcam1"));
        builder.setCameraResolution(new Size(640, 480));
        builder.enableLiveView(false);
        builder.addProcessor(aprilTagProcessor);
        visionPortal = builder.build();

        // ---------- Intake & Shooting + Backboard ----------
        shooter = hwMap.get(DcMotor.class, "shooter");
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setDirection(DcMotor.Direction.REVERSE);

        colorSensor = hwMap.get(NormalizedColorSensor.class, "shooter_color_sensor");
        colorSensor.setGain(8);

        backboard = hwMap.get(Servo.class, "backboard");
        backboard.setDirection(Servo.Direction.REVERSE);
    }

    public void updateTagProcessor() {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        detectedTags = (detections != null) ? detections : Collections.emptyList();
    }

    public List<AprilTagDetection> getTagByColor(IntakeLayers.DetectedColor enemyColor) {
        String requiredPrefix = (enemyColor == IntakeLayers.DetectedColor.RED) ? "B" : "R";

        allianceColorTags.clear();
        for (AprilTagDetection detection : detectedTags) {
            if (detection instanceof AprilTagClusterDetection) {
                AprilTagClusterDetection clusterDet = (AprilTagClusterDetection) detection;
                if (clusterDet.metadata != null && clusterDet.metadata.name != null) {
                    if (clusterDet.metadata.name.startsWith(requiredPrefix)) {
                        allianceColorTags.add(clusterDet);
                    }
                }
            }
        }
        return allianceColorTags;
    }

    // ---------- Intake & Shooting ----------
    public IntakeLayers.DetectedColor getDetectedColor(Telemetry telemetry) {
        if (colorSensor == null) {
            return IntakeLayers.DetectedColor.UNKNOWN;
        }

        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        int r, g, b;
        r = (int) (colors.red * 255.0f);
        g = (int) (colors.green * 255.0f);
        b = (int) (colors.blue * 255.0f);

        Color.RGBToHSV(r, g, b, hsv);

        float hue = hsv[0];
        float saturation = hsv[1];
        float value = hsv[2];

        // telemetry part only for calibration; comment-out in real game
        telemetry.addData("hue (inner)", hue);
        telemetry.addData("saturation (inner)", saturation);
        telemetry.addData("value (inner)", value);

        if (colorSensor instanceof DistanceSensor) {
            double distanceCm = ((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM);

            telemetry.addData("Distance (cm)", distanceCm);

            if (distanceCm > 3.0) {
                return IntakeLayers.DetectedColor.UNKNOWN;
            }
            // need to adjust these values
            if (colors.alpha < 0.05 || value < 0.1) {
                return IntakeLayers.DetectedColor.UNKNOWN;
            }
            if (hue >= 40 && hue <= 85) {
                return IntakeLayers.DetectedColor.YELLOW;
            }
            else if (hue <= 25 || hue >= 330) {
                return IntakeLayers.DetectedColor.RED;
            }
            else if (hue >= 190 && hue <= 250) {
                return IntakeLayers.DetectedColor.BLUE;
            }
        }
        return IntakeLayers.DetectedColor.UNKNOWN;
    }

    public double polyPollen(List<AprilTagDetection> tags) {
        if (tags == null || tags.isEmpty()) {
            return 0;
        }

        // These values can be calculated by collecting data & polynomial regression
        double a = 0;
        double b = 0;
        double c = 0;

        double avgDistance = 0;
        for (AprilTagDetection tag : tags) {
            avgDistance += tag.ftcPose.range;
        }
        avgDistance /= tags.size();

        double power = a * Math.pow(avgDistance, 2) + b * avgDistance + c;

        return Math.min(1.0, Math.max(0, power));
    }
    public double polyNectar(List<AprilTagDetection> tags) {
        if (tags == null || tags.isEmpty()) {
            return 0;
        }

        // These values can be calculated by collecting data & polynomial regression
        double a = 0;
        double b = 0;
        double c = 0;

        double avgDistance = 0;
        for (AprilTagDetection tag : tags) {
            avgDistance += tag.ftcPose.range;
        }
        avgDistance /= tags.size();

        double power = a * Math.pow(avgDistance, 2) + b * avgDistance + c;

        return Math.min(1.0, Math.max(0, power));
    }
    public void shoot(IntakeLayers.DetectedColor enemyColor, IntakeLayers.DetectedColor color) {
        List<AprilTagDetection> tags = this.getTagByColor(enemyColor);
        if (color == IntakeLayers.DetectedColor.UNKNOWN) {
            backboard.setPosition(NECTAR_BB_POSITION);
            shooter.setPower(polyNectar(tags)); // values need testing
        }
        else if (color == IntakeLayers.DetectedColor.YELLOW) {
            backboard.setPosition(POLLEN_BB_POSITION);
            shooter.setPower(polyPollen(tags)); // values need testing
        }
        else {
            backboard.setPosition(NECTAR_BB_POSITION);
            shooter.setPower(polyNectar(tags)); // values need testing
        }
    }

    public void stop() {
        backboard.setPosition(STOP_POSITION);
        shooter.setPower(0.0);
    }
}