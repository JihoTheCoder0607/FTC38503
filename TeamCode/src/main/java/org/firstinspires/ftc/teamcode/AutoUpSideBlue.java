package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import static com.pedropathing.ivy.commands.Commands.*;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.race;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import static com.pedropathing.api.Paths.*;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.mechanisms.FlowerExtraction;
import org.firstinspires.ftc.teamcode.mechanisms.Shooter;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous
public class AutoUpSideBlue extends OpMode {
    private Follower follower;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(132.5385, 81.0431, 180);
    private final Pose point1 = poseFactory.of(117.9167, 117.9167, 225);
    private final Pose point1Control1 = poseFactory.of(117.2264, 86.465, 180);
    private final Pose point2Start = poseFactory.of(117.9167, 117.9167, 225);
    private final Pose point2 = poseFactory.of(94.9588, 11.8856, 270);
    private final Pose point2Control1 = poseFactory.of(121.7308, 52.1527, 180);
    private final Pose point3Start = poseFactory.of(94.9588, 11.8856, 270);
    private final Pose point3 = poseFactory.of(84.3091, 22.3046, 90);
    private final Pose point4Start = poseFactory.of(84.3091, 22.3046, 90);
    private final Pose point4 = poseFactory.of(129.708, 35.375, 180);


    public Path path1() {
        return Paths.curve(start, point1Control1, point1).linear(start, point1);
    }

    public Path path2() {
        return Paths.curve(point2Start, point2Control1, point2).linear(point2Start, point2);
    }

    public Path path3() {
        return Paths.line(point3Start, point3).linear(point3Start, point3);
    }

    public Path path4() {
        return Paths.line(point4Start, point4).linear(point4Start, point4);
    }

    FlowerExtraction flowerExtraction = new FlowerExtraction();


    // ---------- Shooter ----------
    Shooter shooter = new Shooter();

    private Command autoRoutine() {
        return sequential(
                follow(follower, path1()),
                // Add mechanism commands here.
                race(
                        infinite(() -> shooter.shoot(shooter.getDetectedColor(telemetry))),
                        waitMs(3000)
                ),
                instant(() -> shooter.stop()),
                follow(follower, path2()),
                instant(() -> flowerExtraction.down()),
                waitMs(3000),
                instant(() -> flowerExtraction.up()),
                follow(follower, path3()),
                race(
                        infinite(() -> shooter.shoot(shooter.getDetectedColor(telemetry))),
                        waitMs(3000)
                ),
                instant(() -> shooter.stop()),
                follow(follower, path4())
        );
    }

    @Override
    public void init() {
        Scheduler.reset();

        follower = Constants.create(hardwareMap);
        follower.setPose(start);
        follower.update();

        flowerExtraction.init(hardwareMap);

        shooter.init(hardwareMap, telemetry);
    }

    @Override
    public void start() {
        schedule(autoRoutine());
    }

    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();
        // add your other methods needed in the loop here

        telemetry.addData("X", follower.pose().x());
        telemetry.addData("Y", follower.pose().y());
        telemetry.addData("Heading", Math.toDegrees(follower.pose().heading()));
        telemetry.addData("Follower Mode", follower.mode());
        telemetry.update();
    }
}