package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.MecanumDrive;

@TeleOp
public class TeleOpMode extends OpMode {
    MecanumDrive drive = new MecanumDrive();
    double forward, strafe, rotate;

    IntakeLayers intakeLayers = new IntakeLayers();
    boolean garden = false;
    boolean lastYState = false;

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
    }

    @Override
    public void start() {
        intakeLayers.intake();
    }

    @Override
    public void loop() {
        forward = gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.drive(forward, strafe, rotate);

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
    }
}
