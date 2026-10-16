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

    // auto-updating P and D
    double[] stepSizes = {0.1, 0.01, 0.001, 0.0001, 0.00001};
    int stepIndex = 2;

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
        flowerExtraction.init(hardwareMap);
        flowerHood.init(hardwareMap);
        turret.init(hardwareMap, telemetry);

        telemetry.addLine("Initialized all mechanisms");
    }

    @Override
    public void start() {
        intakeLayers.intake();
        flowerExtraction.up();
        flowerHood.down();
        turret.resetTimer();
    }

    @Override
    public void loop() {
        // ---------- Drivetrain ----------
        forward = -gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.drive(forward, strafe, rotate);

        // ---------- Intake ----------
        DetectedColor outerColorL = intakeLayers.getDetectedColor(telemetry, 'L');
        DetectedColor outerColorR = intakeLayers.getDetectedColor(telemetry, 'R');

        boolean enemyDetected =
                outerColorL == enemyColor || outerColorR == enemyColor;

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
        turret.updateTagProcessor();
        List<AprilTagDetection> detections = turret.getTagByColor(enemyColor);

        turret.updateTurret(detections);

        // update P and D on the fly
        // left trigger cycles through different step sizes for tuning precision
        if (gamepad1.leftTriggerWasPressed()) {
            stepIndex = (stepIndex + 1) % stepSizes.length;
        }

        // d-pad left / right adjusts the P gain
        if (gamepad1.dpadLeftWasPressed()) {
            turret.setkP(turret.getkP() - stepSizes[stepIndex]);
        }
        if (gamepad1.dpadRightWasPressed()) {
            turret.setkP(turret.getkP() + stepSizes[stepIndex]);
        }

        // d-pad up / down adjusts the D gain
        if (gamepad1.dpadUpWasPressed()) {
            turret.setkD(turret.getkD() + stepSizes[stepIndex]);
        }
        if (gamepad1.dpadUpWasPressed()) {
            turret.setkD(turret.getkD() - stepSizes[stepIndex]);
        }

        telemetry.addData("Tuning P (dpad LR)", turret.getkP());
        telemetry.addData("Tuning D (dpad UD)", turret.getkD());
        telemetry.addData("Step Size (left trigger)", stepSizes[stepIndex]);


        if (detections != null && !detections.isEmpty()) {
            telemetry.addLine("AprilTag Detected");
        }
        else {
            telemetry.addLine("No Tag Detected. Stopping Turret Motor");
        }

        DetectedColor innerColorL = turret.getDetectedColor(telemetry, 'L');
        DetectedColor innerColorR = turret.getDetectedColor(telemetry, 'R');

        turret.shoot(innerColorL, 'L');
        turret.shoot(innerColorR, 'R');
    }
}
