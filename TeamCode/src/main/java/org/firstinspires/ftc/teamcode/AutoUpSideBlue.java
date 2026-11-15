package org.firstinspires.ftc.teamcode;

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

    // Poses
    private final Pose startPose = poseFactory.of(11.7917, 58.958, 0); // need to update every match
    private final Pose pose1 = poseFactory.of(23.5833, 23.5833, 45);
    private final Pose pose2 = poseFactory.of(47.167, 129.708, 90);
    private final Pose pose3 = poseFactory.of(58.9583, 129.708, 270);
    private final Pose parkPose = poseFactory.of(11.792, 106.125, 0);

    FlowerExtraction flowerExtraction = new FlowerExtraction();


    // ---------- Shooter ----------
    Shooter shooter = new Shooter();

    // Path methods
    private Path phase1() {
        return line(startPose, pose1).linear(startPose, pose1);
    }
    private Path phase2() {
        return line(pose1, pose2).linear(pose1, pose2);
    }
    private Path phase3() {
        return line(pose2, pose3).linear(pose2, pose3);
    }

    private Path park(){
        return line(pose3, parkPose).linear(pose3, parkPose);
    }

    private Command autoRoutine() {
        return sequential(
                follow(follower, phase1()),
                // Add mechanism commands here.
                race(
                        infinite(() -> shooter.shoot(shooter.getDetectedColor(telemetry))),
                        waitMs(1500)
                ),
                instant(() -> shooter.stop()),
                follow(follower, phase2()),
                instant(() -> flowerExtraction.down()),
                waitMs(1500),
                instant(() -> flowerExtraction.up()),
                follow(follower, phase3()),
                race(
                        infinite(() -> shooter.shoot(shooter.getDetectedColor(telemetry))),
                        waitMs(1500)
                ),
                instant(() -> shooter.stop()),
                follow(follower, park())
        );
    }

    @Override
    public void init() {
        Scheduler.reset();

        follower = Constants.create(hardwareMap);
        follower.setPose(startPose);
        follower.update();
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