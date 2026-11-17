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
public class AutoDownSide1Red extends OpMode {
    private Follower follower;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(8.7958, 83.0314, 0);
    private final Pose path1 = poseFactory.of(58.9583, 129.7083, 270);
    private final Pose path1Control1 = poseFactory.of(39.2738, 99.4293, 0);
    private final Pose point2Start = poseFactory.of(58.9583, 129.7083, 270);
    private final Pose point2 = poseFactory.of(47.166, 117.9166, 0);
    private final Pose point3Start = poseFactory.of(47.166, 117.9166, 0);
    private final Pose point3 = poseFactory.of(47.166, 129.7083, 90);
    private final Pose point4Start = poseFactory.of(47.166, 129.7083, 90);
    private final Pose point4 = poseFactory.of(11.7917, 106.125, 0);

    public Path path1() {
        return Paths.curve(start, path1Control1, path1).linear(start, path1);
    }

    public Path path2() {
        return Paths.line(point2Start, point2).linear(point2Start, point2);
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
                waitMs(5000),
                race(
                        infinite(() -> shooter.shoot(shooter.getDetectedColor(telemetry))),
                        waitMs(3000)
                ),
                instant(() -> shooter.stop()),
                follow(follower, path2()),
                follow(follower, path3()),
                instant(() -> flowerExtraction.down()),
                waitMs(3000),
                instant(() -> flowerExtraction.up()),
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