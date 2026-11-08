package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {
    public static MecanumConfig driveConfig = new MecanumConfig(
            c -> {
                c.frontLeftName.set("front_left_motor");
                c.backLeftName.set("back_left_motor");
                c.frontRightName.set("front_right_motor");
                c.backRightName.set("right_back");
                c.frontLeftDirection.set(DcMotor.Direction.REVERSE);
                c.backLeftDirection.set(DcMotor.Direction.REVERSE);
                c.frontRightDirection.set(DcMotor.Direction.FORWARD);
                c.backRightDirection.set(DcMotor.Direction.FORWARD);
            }
    );

    /*
    ODOMETRY PODS CONFIG GOES HERE!!!
     */

    /*
    FOLLOWER CONFIG GOES HERE!!!
     */

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}