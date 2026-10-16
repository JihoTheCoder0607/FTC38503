package org.firstinspires.ftc.teamcode.mechanisms;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Size;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.ArrayList;
import java.util.List;

public class Turret {
    private Telemetry telemetry;

    // ---------- AprilTag ----------
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private List<AprilTagDetection> detectedTags = new ArrayList<>();

    // ---------- Alignment ----------
    private Servo turnServo;
    private double kP = 0.001;
    private double kD = 0.0000;
    private double goalX = 0;
    private double lastError = 0;
    private double angleTolerance = 0.5;

    // The servo position baseline variable
    private double currentServoPosition = 0.5;
    private final double MAX_SERVO_STEP = 0.05; // Limits maximum adjustment speed per frame
    private final ElapsedTime timer = new ElapsedTime();

    // ---------- Intake & Shooting Mechanism
    private NormalizedColorSensor leftInnerColorSensor;
    private NormalizedColorSensor rightInnerColorSensor;
    private DcMotor leftShooter;
    private DcMotor rightShooter;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // ---------- AprilTag ----------
        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(true)
                .setDrawTagOutline(true)
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hwMap.get(WebcamName.class, "webcam1"));
        builder.setCameraResolution(new Size(640, 480));
        builder.addProcessor(aprilTagProcessor);
        visionPortal = builder.build();

        // ---------- Alignment ----------
        turnServo = hwMap.get(Servo.class, "turret_servo");
        turnServo.setPosition(currentServoPosition);
        timer.reset();

        // ---------- Intake & Shooting ----------
        leftInnerColorSensor = hwMap.get(NormalizedColorSensor.class, "left_inner_color_sensor");
        rightInnerColorSensor = hwMap.get(NormalizedColorSensor.class, "right_inner_color_sensor");

        leftShooter = hwMap.get(DcMotor.class, "left_shooter");
        leftShooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftShooter.setDirection(DcMotor.Direction.REVERSE);

        rightShooter = hwMap.get(DcMotor.class, "right_shooter");
        rightShooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void updateTagProcessor() {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        detectedTags = (detections != null) ? detections : new ArrayList<>();
    }

    @SuppressLint("DefaultLocale")
    public void displayDetectionTelemetry(AprilTagDetection detectedId) {
        if (detectedId == null) return;

        if (detectedId instanceof AprilTagClusterDetection) {
            AprilTagClusterDetection clusterDet = (AprilTagClusterDetection) detectedId;
            if (clusterDet.metadata != null) {
                telemetry.addLine(String.format("\n==== %s", clusterDet.metadata.name));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f (cm)", detectedId.ftcPose.x, detectedId.ftcPose.y, detectedId.ftcPose.z));
                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f (deg)", detectedId.ftcPose.pitch, detectedId.ftcPose.roll, detectedId.ftcPose.yaw));
                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f (cm, deg, deg)", detectedId.ftcPose.range, detectedId.ftcPose.bearing, detectedId.ftcPose.elevation));
            } else {
                telemetry.addLine("\n==== Unknown Cluster");
            }
        }
    }

    public List<AprilTagDetection> getTagByColor(IntakeLayers.DetectedColor enemyColor) {
        String requiredPrefix = (enemyColor == IntakeLayers.DetectedColor.RED) ? "B" : "R";
        List<AprilTagDetection> allianceColorTags = new ArrayList<>();
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

    public void stop() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }

    // ---------- Alignment ----------
    public void setkP(double newKP) {
        kP = newKP;
    }
    public double getkP() {
        return kP;
    }
    public void setkD(double newKD) {
        kD = newKD;
    }
    public double getkD() {
        return kD;
    }
    public void resetTimer() {
        timer.reset();
    }

    public void updateTurret(List<AprilTagDetection> detections) {
        double deltaTime = timer.seconds();
        timer.reset();

        if (detections == null || detections.isEmpty()) {
            lastError = 0;
            return;
        }

        AprilTagDetection primaryTag = detections.get(0);
        if (primaryTag.ftcPose == null) return;

        double error = goalX - primaryTag.ftcPose.bearing;
        double pTerm = error * kP;
        double dTerm = 0;

        if (deltaTime > 0) {
            dTerm = ((error - lastError) / deltaTime) * kD;
        }

        if (Math.abs(error) > angleTolerance) {
            double servoDelta = pTerm + dTerm;

            servoDelta = Range.clip(servoDelta, -MAX_SERVO_STEP, MAX_SERVO_STEP);

            currentServoPosition = Range.clip(currentServoPosition + servoDelta, 0.0, 1.0);
            turnServo.setPosition(currentServoPosition);
        }

        lastError = error;
    }

    // ---------- Intake & Shooting ----------
    public IntakeLayers.DetectedColor getDetectedColor(Telemetry telemetry, char side) {
        NormalizedColorSensor sensor;
        if (side == 'L') {
            sensor = leftInnerColorSensor;
        }
        else if (side == 'R') {
            sensor = rightInnerColorSensor;
        }
        else {
            sensor = null;
        }

        if (sensor == null) {
            return IntakeLayers.DetectedColor.UNKNOWN;
        }

        NormalizedRGBA colors = sensor.getNormalizedColors();

        int r, g, b;
        r = (int) (colors.red * 255.0f);
        g = (int) (colors.green * 255.0f);
        b = (int) (colors.blue * 255.0f);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);

        float hue = hsv[0];
        float saturation = hsv[1];
        float value = hsv[2];

        // telemetry part only for calibration; comment-out in real game
        telemetry.addData(side+" hue (inner)", hue);
        telemetry.addData(side+" saturation (inner)", saturation);
        telemetry.addData(side+" value (inner)", value);

        // need to adjust these values
        if (colors.alpha < 0.05 || value < 0.1) {
            return IntakeLayers.DetectedColor.UNKNOWN;
        }

        if (hue >= 40 && hue <= 85) {
            return IntakeLayers.DetectedColor.YELLOW;
        }
        else if (hue >= 190 && hue <= 250) {
            return IntakeLayers.DetectedColor.BLUE;
        }
        else if (hue <= 25 || hue >= 330) {
            return IntakeLayers.DetectedColor.RED;
        }

        return IntakeLayers.DetectedColor.UNKNOWN;
    }

    public void shoot(IntakeLayers.DetectedColor color, char side) {
        DcMotor shooter;
        if (side == 'L') {
            shooter = leftShooter;
        }
        else if (side == 'R') {
            shooter = rightShooter;
        }
        else {
            shooter = null;
        }

        if (shooter == null) {
            return;
        }

        if (color == IntakeLayers.DetectedColor.UNKNOWN) {
            shooter.setPower(0.8); // values need testing
        }
        else if (color == IntakeLayers.DetectedColor.YELLOW) {
            shooter.setPower(0.7); // values need testing
        }
        else {
            shooter.setPower(1.0); // values need testing
        }
    }
}
