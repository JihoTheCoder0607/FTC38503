package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.FlowerExtraction;
import org.firstinspires.ftc.teamcode.mechanisms.FlowerHood;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers.DetectedColor;
import org.firstinspires.ftc.teamcode.mechanisms.MecanumDrive;
import org.firstinspires.ftc.teamcode.mechanisms.Turret;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.util.List;

@TeleOp
public class TeleOpMode extends OpMode {
    // ---------- Drivetrain ----------
    MecanumDrive drive = new MecanumDrive();
    double forward, strafe, rotate;

    // ---------- Intake ----------
    IntakeLayers intakeLayers = new IntakeLayers();
    DetectedColor enemyColor = DetectedColor.RED;
//    DetectedColor enemyColor = DetectedColor.BLUE;

    boolean manualGarden = false;
    boolean lastYState = false;

    // ---------- Flower Extraction ----------
    FlowerExtraction flowerExtraction = new FlowerExtraction();

    boolean extractorDown = false;
    boolean lastXState = false;

    // ---------- Flower Hood ----------
    FlowerHood flowerHood = new FlowerHood();
    boolean hoodDown = true;
    boolean lastLBState = false;

    // ---------- Turret ----------
    Turret turret = new Turret();

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
        flowerExtraction.init(hardwareMap);
        flowerHood.init(hardwareMap);
        turret.init(hardwareMap, telemetry);
    }

    @Override
    public void start() {
        intakeLayers.intake();
        flowerExtraction.up();
        flowerHood.down();
    }

    @Override
    public void loop() {
        // ---------- Drivetrain ----------
        forward = -gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.drive(forward, strafe, rotate);

        // ---------- Intake ----------
        DetectedColor colorL = intakeLayers.getDetectedColor(telemetry, 'L');
        DetectedColor colorR = intakeLayers.getDetectedColor(telemetry, 'R');

        boolean enemyDetected =
                colorL == enemyColor || colorR == enemyColor;

        if (gamepad1.y && !lastYState) {
            manualGarden = !manualGarden;
        }
        lastYState = gamepad1.y;

        if (gamepad1.right_bumper) {
            intakeLayers.stop();
        }

        else if (enemyDetected) {
            intakeLayers.garden();
        }

        else if (manualGarden) {
            intakeLayers.garden();
        }

        else {
            intakeLayers.intake();
        }


        // ---------- Flower extraction ----------
        if (gamepad1.x && !lastXState) {
            if (extractorDown) {
                flowerExtraction.up();
            } else {
                flowerExtraction.down();
            }
            extractorDown = !extractorDown;
        }
        lastXState = gamepad1.x;

        // ---------- Flower hood ----------
        if (gamepad1.left_bumper && !lastLBState) {
            if (hoodDown) {
                flowerHood.up();
            } else {
                flowerHood.down();
            }
            hoodDown = !hoodDown;
        }
        lastLBState = gamepad1.left_bumper;

        // ---------- Turret ----------
        // update vision portal
        turret.update();
        List<AprilTagDetection> detections = turret.getTagByColor(enemyColor);
    }
}
