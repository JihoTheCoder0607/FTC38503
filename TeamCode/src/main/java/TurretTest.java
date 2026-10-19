import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.Turret;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.util.List;

@TeleOp
public class TurretTest extends OpMode {
    // ---------- Turret ----------
    Turret turret = new Turret();
    IntakeLayers.DetectedColor enemyColor = IntakeLayers.DetectedColor.RED;

    // Tuning P and D
    double[] stepSizes = {0.1, 0.01, 0.001, 0.0001, 0.00001};
    int stepIndex = 2;
    boolean manualShoot = false;
    boolean lastBState = false;

    // State tracking variables for edge-detection tuning controls
    boolean lastLeftTriggerState = false;
    boolean lastDpadLeftState = false;
    boolean lastDpadRightState = false;
    boolean lastDpadUpState = false;
    boolean lastDpadDownState = false;

    @Override
    public void init() {
        turret.init(hardwareMap, telemetry, 'L');
        telemetry.addLine("Initialized all mechanisms");
    }

    @Override
    public void start() {
        turret.resetTimer();
    }

    public void autoShoot(Turret turret, char side) {
        turret.updateTagProcessor();
        List<AprilTagDetection> detections = turret.getTagByColor(enemyColor);
        turret.updateTurret(detections);
        telemetry.addData(side + " Tuning P", turret.getkP());
        telemetry.addData(side + " Tuning D", turret.getkD());
        telemetry.addData(side + " Step Size", stepSizes[stepIndex]);

        if (detections != null && !detections.isEmpty()) {
            telemetry.addLine(side + " Turret: AprilTag Detected");
        } else {
            telemetry.addLine(side + " Turret: No Tag Detected. Stopping Motor");
        }
    }

    @Override
    public void loop() {
        // ---------- Turret Tuning Controls (Handled once per loop frame) ----------
        // Left trigger threshold logic acts like a button press
        boolean currentLeftTriggerState = gamepad1.left_trigger > 0.0;
        if (currentLeftTriggerState && !lastLeftTriggerState) {
            stepIndex = (stepIndex + 1) % stepSizes.length;
        }
        lastLeftTriggerState = currentLeftTriggerState;

        if (gamepad1.dpad_left && !lastDpadLeftState) {
            turret.setkP(turret.getkP() - stepSizes[stepIndex]);
        }
        lastDpadLeftState = gamepad1.dpad_left;

        if (gamepad1.dpad_right && !lastDpadRightState) {
            turret.setkP(turret.getkP() + stepSizes[stepIndex]);
        }
        lastDpadRightState = gamepad1.dpad_right;

        if (gamepad1.dpad_up && !lastDpadUpState) {
            turret.setkD(turret.getkD() + stepSizes[stepIndex]);
        }
        lastDpadUpState = gamepad1.dpad_up;

        if (gamepad1.dpad_down && !lastDpadDownState) {
            turret.setkD(turret.getkD() - stepSizes[stepIndex]);
        }
        lastDpadDownState = gamepad1.dpad_down;

        // ---------- Turret Execution ----------
        if (gamepad1.b && !lastBState) {
            manualShoot = !manualShoot;
        }
        lastBState = gamepad1.b;

        if (!manualShoot) {
            autoShoot(turret, 'L');
            turret.shoot(turret.getDetectedColor(telemetry, 'L'));
        } else {
            turret.manualMode();
            turret.shoot(turret.getDetectedColor(telemetry, 'L'));
        }
    }
}
