package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.mechanisms.FlowerExtraction;
import org.firstinspires.ftc.teamcode.mechanisms.FlowerHood;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers;
import org.firstinspires.ftc.teamcode.mechanisms.IntakeLayers.DetectedColor;
import org.firstinspires.ftc.teamcode.mechanisms.MecanumDrive;
import org.firstinspires.ftc.teamcode.mechanisms.Shooter;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@TeleOp(name = "ShooterLogger", group = "Testing")
public class ShooterLogger extends OpMode {

    // ---------- Tuning Constants ----------
    // Time to keep flywheel and intake running after sensor detection
    // so the scoring element exits before pausing for result logging.
    private static final double LAUNCH_DELAY_SEC = 0.40;
    private static final double FINE_POWER_STEP = 0.02;
    private static final double COARSE_POWER_STEP = 0.05;

    // ---------- Drivetrain ----------
    private final MecanumDrive drive = new MecanumDrive();
    private double forward, strafe, rotate;

    // ---------- Intake ----------
    private final IntakeLayers intakeLayers = new IntakeLayers();
    private DetectedColor enemyColor = DetectedColor.RED;

    private boolean manualGarden = false;
    private boolean lastLBState = false;
    private boolean lastRightBumperState = false;
    private boolean forceStop = false;

    // ---------- Flower Extraction ----------
    private final FlowerExtraction flowerExtraction = new FlowerExtraction();
    private boolean extractorDown = false;
    private boolean lastXState = false;

    // ---------- Flower Hood ----------
    private final FlowerHood flowerHood = new FlowerHood();
    private boolean hoodDown = true;
    private boolean lastYState = false;

    // ---------- Shooter ----------
    private final Shooter shooter = new Shooter();
    private boolean shooting = false;
    private boolean lastAState = false;

    private double targetPower = 0.65;
    private boolean lastDpadUp = false;
    private boolean lastDpadDown = false;
    private boolean lastDpadLeft = false;
    private boolean lastDpadRight = false;

    // ---------- Data Logging ----------
    private FileWriter pollenWriter;
    private FileWriter nectarWriter;

    private int pollenCount = 0;
    private int nectarCount = 0;

    private double currentTagDistance = Double.NaN;
    private String lastLogStatus = "None yet";

    // ---------- Latched Shot Data & Launch State ----------
    private DetectedColor latchedColor = null;
    private double latchedPower = 0.0;
    private double latchedDistance = Double.NaN;

    private boolean launchInProgress = false;
    private final ElapsedTime launchTimer = new ElapsedTime();

    private boolean awaitingResult = false;
    private boolean pieceNeedsClear = false;

    private boolean gamepad2XLast = false;
    private boolean gamepad2BLast = false;
    private boolean gamepad2YLast = false;

    @Override
    public void init() {
        drive.init(hardwareMap);
        intakeLayers.init(hardwareMap);
        flowerExtraction.init(hardwareMap);
        flowerHood.init(hardwareMap);
        shooter.init(hardwareMap, telemetry);

        // ---------- Create Logging Directory in FIRST folder ----------
        File dir = new File(AppUtil.FIRST_FOLDER, "ShooterLogger");

        if (!dir.exists() && !dir.mkdirs()) {
            lastLogStatus = "ERROR: Cannot create logging directory";
        }

        String sessionStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss_SSS",
                Locale.US
        ).format(new Date());

        File pollenFile = new File(dir, "pollen_shots_" + sessionStamp + ".csv");
        File nectarFile = new File(dir, "nectar_shots_" + sessionStamp + ".csv");

        // ---------- Initialize CSV Files ----------
        try {
            pollenWriter = new FileWriter(pollenFile, false);
            writeLine(
                    pollenWriter,
                    "iteration,timestamp,type,tag_distance_cm,motor_power,result"
            );

            nectarWriter = new FileWriter(nectarFile, false);
            writeLine(
                    nectarWriter,
                    "iteration,timestamp,type,tag_distance_cm,motor_power,result"
            );

            lastLogStatus = "Files created (" + sessionStamp + ")";
        } catch (IOException e) {
            lastLogStatus = "FILE ERROR: " + e.getMessage();
            closeWriters();
        }

        telemetry.addLine("=== ShooterLogger Ready ===");
        telemetry.addLine("GP1 A: Toggle Shooter | D-Pad: Adjust Power");
        telemetry.addLine("GP2 X: HIT | B: MISS | Y: DISCARD");
        telemetry.addData("Status", lastLogStatus);
        telemetry.update();
    }

    @Override
    public void start() {
        intakeLayers.intake();
        flowerExtraction.up();
        flowerHood.down();
    }

    @Override
    public void loop() {
        // Always update AprilTag distance so the driver can line up shots before firing
        updateTagDistance();

        // ==================================================
        // 1. WAITING FOR SHOT RESULT (PAUSED STATE)
        // ==================================================
        if (awaitingResult) {
            drive.drive(0, 0, 0);
            intakeLayers.stop();
            shooter.stop();

            // Gamepad 2 X = HIT
            if (gamepad2.x && !gamepad2XLast) {
                if (logResult("HIT")) {
                    resetShotState();
                }
            }
            // Gamepad 2 B = MISS
            else if (gamepad2.b && !gamepad2BLast) {
                if (logResult("MISS")) {
                    resetShotState();
                }
            }
            // Gamepad 2 Y = DISCARD (false sensor trigger)
            else if (gamepad2.y && !gamepad2YLast) {
                lastLogStatus = "DISCARDED false trigger (" +
                        (latchedColor != null ? latchedColor.name() : "NONE") + ")";
                resetShotState();
            }

            gamepad2XLast = gamepad2.x;
            gamepad2BLast = gamepad2.b;
            gamepad2YLast = gamepad2.y;

            // Keep Gamepad 1 edge states synced to avoid accidental presses on resume
            syncGamepad1States();

            telemetry.addLine("=== WAITING FOR SHOT RESULT ===");
            telemetry.addLine("GP2: [X] = HIT  |  [B] = MISS  |  [Y] = DISCARD");
            telemetry.addData("Latched Piece", latchedColor != null ? latchedColor.name() : "NONE");
            telemetry.addData("Latched Distance (cm)", String.format(Locale.US, "%.1f", latchedDistance));
            telemetry.addData("Latched Motor Power", String.format(Locale.US, "%.4f", latchedPower));
            telemetry.addData("Logged (Pollen / Nectar)", pollenCount + " / " + nectarCount);
            telemetry.addData("Last Log", lastLogStatus);
            telemetry.update();
            return;
        }

        // ==================================================
        // 2. DRIVETRAIN
        // ==================================================
        forward = -gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;
        drive.drive(forward, strafe, rotate);

        // ==================================================
        // 3. INTAKE
        // ==================================================
        DetectedColor outerColorL = intakeLayers.getDetectedColor(telemetry, 'L');
        DetectedColor outerColorR = intakeLayers.getDetectedColor(telemetry, 'R');
        boolean enemyDetected = (outerColorL == enemyColor || outerColorR == enemyColor);

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

        // ==================================================
        // 4. FLOWER EXTRACTION & HOOD
        // ==================================================
        if (gamepad1.x && !lastXState) {
            if (extractorDown) {
                flowerExtraction.up();
            } else {
                flowerExtraction.down();
            }
            extractorDown = !extractorDown;
        }
        lastXState = gamepad1.x;

        if (gamepad1.y && !lastYState) {
            if (hoodDown) {
                flowerHood.up();
            } else {
                flowerHood.down();
            }
            hoodDown = !hoodDown;
        }
        lastYState = gamepad1.y;

        // ==================================================
        // 5. TARGET POWER ADJUSTMENT (GAMEPAD 1 D-PAD)
        // ==================================================
        if (gamepad1.dpad_up && !lastDpadUp) {
            targetPower = clampPower(targetPower + FINE_POWER_STEP);
        }
        lastDpadUp = gamepad1.dpad_up;

        if (gamepad1.dpad_down && !lastDpadDown) {
            targetPower = clampPower(targetPower - FINE_POWER_STEP);
        }
        lastDpadDown = gamepad1.dpad_down;

        if (gamepad1.dpad_right && !lastDpadRight) {
            targetPower = clampPower(targetPower + COARSE_POWER_STEP);
        }
        lastDpadRight = gamepad1.dpad_right;

        if (gamepad1.dpad_left && !lastDpadLeft) {
            targetPower = clampPower(targetPower - COARSE_POWER_STEP);
        }
        lastDpadLeft = gamepad1.dpad_left;

        // ==================================================
        // 6. SHOOTER & LAUNCH WINDOW
        // ==================================================
        if (gamepad1.a && !lastAState) {
            shooting = !shooting;
            if (!shooting) {
                shooter.stop();
                launchInProgress = false;
                pieceNeedsClear = false;
            }
        }
        lastAState = gamepad1.a;

        if (shooting) {
            DetectedColor color = shooter.getDetectedColor(telemetry);
            boolean isPollen = (color == DetectedColor.YELLOW);
            boolean isNectar = (color == DetectedColor.BLUE || color == DetectedColor.RED);

            // Keep flywheel spun up at targetPower; set backboard angle for detected piece
            // (or maintain latched piece's backboard angle while launch is in progress)
            DetectedColor activeColor = launchInProgress ? latchedColor : color;
            shooter.shootWithManualPower(activeColor, targetPower);

            if (isPollen || isNectar) {
                if (!pieceNeedsClear && !launchInProgress && !awaitingResult) {
                    latchedColor = color;
                    latchedDistance = currentTagDistance;
                    latchedPower = targetPower;

                    launchInProgress = true;
                    pieceNeedsClear = true;
                    launchTimer.reset();
                }
            } else {
                pieceNeedsClear = false;
            }

            // Wait for LAUNCH_DELAY_SEC so the scoring element actually exits before pausing
            if (launchInProgress && launchTimer.seconds() >= LAUNCH_DELAY_SEC) {
                launchInProgress = false;
                awaitingResult = true;
            }
        }

        // ---------- Gamepad 2 edge state ----------
        gamepad2XLast = gamepad2.x;
        gamepad2BLast = gamepad2.b;
        gamepad2YLast = gamepad2.y;

        // ---------- Telemetry ----------
        telemetry.addData("Shooting", shooting ? (launchInProgress ? "FIRING..." : "READY / SPINNING") : "OFF");
        telemetry.addData("Target Power", String.format(Locale.US, "%.2f", targetPower));
        telemetry.addData("Tag Distance (cm)", String.format(Locale.US, "%.1f", currentTagDistance));
        telemetry.addData("Logged (Pollen / Nectar)", pollenCount + " / " + nectarCount);
        telemetry.addData("Last Log", lastLogStatus);
        telemetry.update();
    }

    // ---------- Helpers ----------

    private void updateTagDistance() {
        shooter.updateTagProcessor();
        List<AprilTagDetection> matchingTags = shooter.getTagByColor(enemyColor);

        double tagRangeSum = 0;
        int tagCount = 0;

        for (AprilTagDetection tag : matchingTags) {
            if (tag.ftcPose != null &&
                    !Double.isNaN(tag.ftcPose.range) &&
                    Double.isFinite(tag.ftcPose.range)) {
                tagRangeSum += tag.ftcPose.range;
                tagCount++;
            }
        }

        currentTagDistance = (tagCount > 0) ? (tagRangeSum / tagCount) : Double.NaN;
    }

    private void resetShotState() {
        awaitingResult = false;
        launchInProgress = false;
        pieceNeedsClear = false;
        latchedColor = null;
    }

    private void syncGamepad1States() {
        lastAState = gamepad1.a;
        lastLBState = gamepad1.left_bumper;
        lastRightBumperState = gamepad1.right_bumper;
        lastXState = gamepad1.x;
        lastYState = gamepad1.y;
        lastDpadUp = gamepad1.dpad_up;
        lastDpadDown = gamepad1.dpad_down;
        lastDpadLeft = gamepad1.dpad_left;
        lastDpadRight = gamepad1.dpad_right;
    }

    private double clampPower(double power) {
        return Math.round(Math.min(1.0, Math.max(0.0, power)) * 100.0) / 100.0;
    }

    // ---------- Data Logging ----------

    private boolean logResult(String result) {
        if (latchedColor == null) {
            lastLogStatus = "SKIPPED: No piece latched";
            return false;
        }

        double dist = Double.isNaN(latchedDistance) ? -1.0 : latchedDistance;
        String type = (latchedColor == DetectedColor.YELLOW) ? "POLLEN" : "NECTAR";
        FileWriter writer = type.equals("POLLEN") ? pollenWriter : nectarWriter;

        if (writer == null) {
            lastLogStatus = "SKIPPED: File writer is null";
            return false;
        }

        try {
            String timestamp = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                    Locale.US
            ).format(new Date());

            int count = type.equals("POLLEN") ? pollenCount + 1 : nectarCount + 1;

            String row = count + "," +
                    timestamp + "," +
                    type + "," +
                    String.format(Locale.US, "%.2f", dist) + "," +
                    String.format(Locale.US, "%.4f", latchedPower) + "," +
                    result;

            writeLine(writer, row);

            if (type.equals("POLLEN")) {
                pollenCount++;
            } else {
                nectarCount++;
            }

            lastLogStatus = "Logged #" + count + ": " + result + " (" + type + ")";
            return true;
        } catch (IOException e) {
            lastLogStatus = "Log ERROR: " + e.getMessage();
            return false;
        }
    }

    private void writeLine(FileWriter writer, String row) throws IOException {
        writer.write(row);
        writer.write('\n');
        writer.flush();
    }

    private void closeWriters() {
        try {
            if (pollenWriter != null) {
                pollenWriter.close();
                pollenWriter = null;
            }
        } catch (IOException ignored) {
        }

        try {
            if (nectarWriter != null) {
                nectarWriter.close();
                nectarWriter = null;
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void stop() {
        shooter.stop();
        shooter.close();
        intakeLayers.stop();
        drive.drive(0, 0, 0);
        closeWriters();
    }
}