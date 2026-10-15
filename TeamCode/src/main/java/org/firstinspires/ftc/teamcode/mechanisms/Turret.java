package org.firstinspires.ftc.teamcode.mechanisms;

import android.annotation.SuppressLint;
import android.util.Size;
import com.qualcomm.robotcore.hardware.HardwareMap;
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
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private List<AprilTagDetection> detectedTags = new ArrayList<>();
    private Telemetry telemetry;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

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
    }

    public void update() {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        // Defensive check: ensure detectedTags is never assigned null
        detectedTags = (detections != null) ? detections : new ArrayList<>();
    }

    @SuppressLint("DefaultLocale")
    public void displayDetectionTelemetry(AprilTagDetection detectedId) {
        if (detectedId == null) {
            return;
        }

        if (detectedId instanceof AprilTagClusterDetection) {
            AprilTagClusterDetection clusterDet = (AprilTagClusterDetection) detectedId;

            if (clusterDet.metadata != null) {
                telemetry.addLine(String.format("\n==== %s", clusterDet.metadata.name));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (cm)", detectedId.ftcPose.x, detectedId.ftcPose.y, detectedId.ftcPose.z));
                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detectedId.ftcPose.pitch, detectedId.ftcPose.roll, detectedId.ftcPose.yaw));
                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (cm, deg, deg)", detectedId.ftcPose.range, detectedId.ftcPose.bearing, detectedId.ftcPose.elevation));
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
}
