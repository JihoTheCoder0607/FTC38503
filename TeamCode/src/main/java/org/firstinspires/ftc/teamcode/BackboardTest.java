package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
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
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Shooter {
    private Telemetry telemetry;

    // ---------- Intake & Shooting Mechanism + Backboard

    private final float[] hsv = new float[3];

    private Servo backboard;
    private final double NECTAR_BB_POSITION = 0.5;
    private final double POLLEN_BB_POSITION = 0.3;

    private final double STOP_POSITION = 0.2;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // ---------- Intake & Shooting + Backboard ----------

        backboard = hwMap.get(Servo.class, "backboard");
        backboard.setDirection(Servo.Direction.REVERSE);
    }

    // ---------- Intake & Shooting ----------
    public void move(IntakeLayers.DetectedColor color) {
        if (color == IntakeLayers.DetectedColor.UNKNOWN) {
            backboard.setPosition(NECTAR_BB_POSITION);
        }
        else if (color == IntakeLayers.DetectedColor.YELLOW) {
            backboard.setPosition(POLLEN_BB_POSITION);
        }
        else {
            backboard.setPosition(NECTAR_BB_POSITION);
        }
    }


    public void stop() {
        backboard.setPosition(STOP_POSITION);
    }
}

@TeleOp
public class BackboardTest extends OpMode {
    private Shooter shooter = new Shooter();
    public void init() {
        shooter.init(hardwareMap, telemetry);
    }

    public void start() {
        shooter.move(IntakeLayers.DetectedColor.YELLOW);
    }

    public void loop() {

    }
}
