package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.FlowerExtraction;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.MecanumDrive;

@TeleOp
public class TeleOpMode extends OpMode {
    // Drivetrain
    MecanumDrive drive = new MecanumDrive();
    double forward, strafe, rotate;

    // Intake
    IntakeLayers intakeLayers = new IntakeLayers();
    boolean garden = false;
    boolean lastYState = false;

    // Flower Extraction
    FlowerExtraction flowerExtraction = new FlowerExtraction();

    boolean down = false;
    boolean lastXState = false;

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
        flowerExtraction.init(hardwareMap);
    }

    @Override
    public void start() {
        intakeLayers.intake();
        flowerExtraction.up();
    }

    @Override
    public void loop() {
        // Drivetrain
        forward = gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.drive(forward, strafe, rotate);

        // Intake
        if (gamepad1.y && !lastYState) {
            if (garden) {
                intakeLayers.intake();
            }
            else {
                intakeLayers.garden();
            }
            garden = !garden;
        }

        else if (gamepad1.left_bumper) {
            intakeLayers.stop();
        }

        lastYState = gamepad1.y;

        // Flower extraction
        if (gamepad1.x && !lastXState) {
            if (down) {
                flowerExtraction.up();
            }
            else {
                flowerExtraction.down();
            }
            down = !down;
        }

        lastXState = gamepad1.x;
    }
}
