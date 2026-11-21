package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.mechanisms.FlowerExtraction;
import org.firstinspires.ftc.teamcode.mechanisms.FlowerHood;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers.DetectedColor;
import org.firstinspires.ftc.teamcode.mechanisms.MecanumDrive;
import org.firstinspires.ftc.teamcode.mechanisms.Shooter;

@TeleOp
public class TeleOpMode extends OpMode {
    // ---------- Drivetrain ----------
    MecanumDrive drive = new MecanumDrive();
    double forward, strafe, rotate;

    // ---------- Intake ----------
    IntakeLayers intakeLayers = new IntakeLayers();
    DetectedColor enemyColor = DetectedColor.RED;
    boolean manualGarden = false;
    boolean lastLBState = false;

    boolean lastRightBumperState = false;
    boolean forceStop = false;

    // ---------- Flower Extraction ----------
    FlowerExtraction flowerExtraction = new FlowerExtraction();
    boolean extractorDown = false;
    boolean lastXState = false;

    // ---------- Flower Hood ----------
    FlowerHood flowerHood = new FlowerHood();
    boolean hoodDown = true;
    boolean lastYState = false;

    // ---------- shooter ----------
    Shooter shooter = new Shooter();
    boolean shooting = false;
    boolean lastAState = false;

    boolean manualMode = false;
    boolean lastBState = false;

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
        flowerExtraction.init(hardwareMap);
        flowerHood.init(hardwareMap);
        shooter.init(hardwareMap, telemetry);
        telemetry.addLine("Initialized all mechanisms");
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
        DetectedColor outerColorL = intakeLayers.getDetectedColor(telemetry, 'L');
        DetectedColor outerColorR = intakeLayers.getDetectedColor(telemetry, 'R');
        boolean enemyDetected = outerColorL == enemyColor || outerColorR == enemyColor;

        if (gamepad1.left_bumper && !lastLBState) {
            manualGarden = !manualGarden;
        }
        lastLBState = gamepad1.left_bumper;
        if (gamepad1.right_bumper && !lastRightBumperState) {
            forceStop = !forceStop;
        }
        lastRightBumperState = gamepad1.right_bumper;

        if (forceStop) {
            intakeLayers.stop();
        } else if (enemyDetected || manualGarden) {
            intakeLayers.garden();
        } else {
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
        if (gamepad1.y && !lastYState) {
            if (hoodDown) {
                flowerHood.up();
            } else {
                flowerHood.down();
            }
            hoodDown = !hoodDown;
        }
        lastYState = gamepad1.y;

        // ---------- Shooter ----------
        if (gamepad1.a && !lastAState) {
            shooting = !shooting;
        }
        lastAState = gamepad1.a;
        if (shooting) {
            shooter.shoot(enemyColor, shooter.getDetectedColor(telemetry));
        }
    }
}
