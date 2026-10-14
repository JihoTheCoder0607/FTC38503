package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.FlowerExtraction;
import org.firstinspires.ftc.teamcode.mechanisms.FlowerHood;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers.DetectedColor;
import org.firstinspires.ftc.teamcode.mechanisms.MecanumDrive;

@TeleOp
public class TeleOpMode extends OpMode {
    // Drivetrain
    MecanumDrive drive = new MecanumDrive();
    double forward, strafe, rotate;

    // Intake
    IntakeLayers intakeLayers = new IntakeLayers();
    DetectedColor enemyColor = DetectedColor.RED;
//    DetectedColor enemyColor = DetectedColor.BLUE;

    boolean garden = false;
    boolean lastYState = false;

    // Flower Extraction
    FlowerExtraction flowerExtraction = new FlowerExtraction();

    boolean extractorDown = false;
    boolean lastXState = false;

    // Flower Hood
    FlowerHood flowerHood = new FlowerHood();
    boolean hoodDown = true;
    boolean lastLBState = false;

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
        flowerExtraction.init(hardwareMap);
        flowerHood.init(hardwareMap);
    }

    @Override
    public void start() {
        intakeLayers.intake();
        flowerExtraction.up();
        flowerHood.down();
    }

    @Override
    public void loop() {
        // Drivetrain
        forward = -gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.drive(forward, strafe, rotate);

        // Intake
        DetectedColor colorL = intakeLayers.getDetectedColor(telemetry, 'L');
        DetectedColor colorR = intakeLayers.getDetectedColor(telemetry, 'R');

        if (gamepad1.right_bumper) {
            intakeLayers.stop();
            garden = false;
        } else if (gamepad1.y && !lastYState) {
            if (garden) {
                intakeLayers.intake();
            } else {
                intakeLayers.garden();
            }
            garden = !garden;
        } else if (colorL == enemyColor || colorR == enemyColor) {
            intakeLayers.garden();
            garden = true;
        }

        lastYState = gamepad1.y;

        // Flower extraction
        if (gamepad1.x && !lastXState) {
            if (extractorDown) {
                flowerExtraction.up();
            } else {
                flowerExtraction.down();
            }
            extractorDown = !extractorDown;
        }
        lastXState = gamepad1.x;

        // Flower hood
        if (gamepad1.left_bumper && !lastLBState) {
            if (hoodDown) {
                flowerHood.up();
            } else {
                flowerHood.down();
            }
            hoodDown = !hoodDown;
        }
        lastLBState = gamepad1.left_bumper;
    }
}
