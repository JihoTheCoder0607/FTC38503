package org.firstinspires.ftc.teamcode.mechanisms;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class IntakeLayers {
    private DcMotor intake;
    private DcMotor conveyor;
    private NormalizedColorSensor leftOuterColorSensor;
    private NormalizedColorSensor rightOuterColorSensor;

    private final float[] hsv = new float[3];

    public enum DetectedColor {
        RED,
        BLUE,
        YELLOW,
        UNKNOWN
    }

    public void init(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotor.class, "intake_motor");
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        intake.setDirection(DcMotor.Direction.REVERSE);

        conveyor = hardwareMap.get(DcMotor.class, "conveyor_motor");
        conveyor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        conveyor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        conveyor.setDirection(DcMotor.Direction.REVERSE);


        leftOuterColorSensor = hardwareMap.get(NormalizedColorSensor.class, "left_color_sensor");
        leftOuterColorSensor.setGain(8);

        rightOuterColorSensor = hardwareMap.get(NormalizedColorSensor.class, "right_color_sensor");
        rightOuterColorSensor.setGain(8);
    }

    public void intake() {
        // these values can be changed
        intake.setPower(1.0);
        conveyor.setPower(1.0);
    }

    public void stop() {
        intake.setPower(0.0);
        conveyor.setPower(0.0);
    }

    public void garden() {
        // these values can be changed
        intake.setPower(-1.0);
        conveyor.setPower(-1.0);
    }

    public DetectedColor getDetectedColor(Telemetry telemetry, char side) {
        NormalizedColorSensor sensor;
        if (side == 'L') {
            sensor = leftOuterColorSensor;
        }
        else if (side == 'R') {
            sensor = rightOuterColorSensor;
        }
        else {
            sensor = null;
        }

        if (sensor == null) {
            return DetectedColor.UNKNOWN;
        }

        NormalizedRGBA colors = sensor.getNormalizedColors();

        int r, g, b;
        r = (int) (colors.red * 255.0f);
        g = (int) (colors.green * 255.0f);
        b = (int) (colors.blue * 255.0f);

        Color.RGBToHSV(r, g, b, hsv);

        float hue = hsv[0];
        float saturation = hsv[1];
        float value = hsv[2];

        // telemetry part only for calibration; comment-out in real game
        telemetry.addData(side+" hue (outer)", hue);
        telemetry.addData(side+" saturation (outer)", saturation);
        telemetry.addData(side+" value (outer)", value);

        // need to adjust these values
        double dy = Math.abs(90-hue);
        double dr = Math.abs(20-hue);
        double db = Math.abs(200-hue);

        if (dy == Math.min(dy, Math.min(dr, db))) {
            return IntakeLayers.DetectedColor.YELLOW;
        }
        else if (dr == Math.min(dy, Math.min(dr, db))) {
            return IntakeLayers.DetectedColor.RED;
        }
        else if (db == Math.min(dy, Math.min(dr, db))) {
            return IntakeLayers.DetectedColor.BLUE;
        }
        return IntakeLayers.DetectedColor.UNKNOWN;
    }
}
