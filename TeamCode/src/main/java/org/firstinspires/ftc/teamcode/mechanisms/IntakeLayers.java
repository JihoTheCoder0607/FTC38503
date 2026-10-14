package org.firstinspires.ftc.teamcode.mechanisms;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class IntakeLayers {
    private CRServo outer, inner;
    private NormalizedColorSensor leftOuterColorSensor;
    private NormalizedColorSensor rightOuterColorSensor;

    public enum DetectedColor {
        RED,
        BLUE,
        YELLOW,
        UNKNOWN
    }

    public void init(HardwareMap hardwareMap) {
        outer = hardwareMap.get(CRServo.class, "outer_intake_motor");
//        outer.setDirection(CRServo.Direction.REVERSE);

        inner = hardwareMap.get(CRServo.class, "inner_intake_motor");
//        inner.setDirection(CRServo.Direction.REVERSE);

        leftOuterColorSensor = hardwareMap.get(NormalizedColorSensor.class, "left_outer_color_sensor");
        leftOuterColorSensor.setGain(8);

        rightOuterColorSensor = hardwareMap.get(NormalizedColorSensor.class, "right_outer_color_sensor");
        rightOuterColorSensor.setGain(8);
    }

    public void intake() {
        // these values can be changed
        outer.setPower(1.0);
        inner.setPower(1.0);
    }

    public void stop() {
        outer.setPower(0.0);
        inner.setPower(0.0);
    }

    public void garden() {
        // these values can be changed
        outer.setPower(-1.0);
        inner.setPower(-1.0);
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

        float alpha = (colors.alpha < 1e-9f) ? 1.0f : colors.alpha;

        int r, g, b;
        r = (int) (colors.red * 255.0f);
        g = (int) (colors.green * 255.0f);
        b = (int) (colors.blue * 255.0f);

        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);

        float hue = hsv[0];
        float saturation = hsv[1];
        float value = hsv[2];

        // telemetry part only for calibration; comment-out in real game
        telemetry.addData(side+" hue", hue);
        telemetry.addData(side+" saturation", saturation);
        telemetry.addData(side+" value", value);

        // need to adjust these values
        if (colors.alpha < 0.05 || value < 0.1) {
            return DetectedColor.UNKNOWN;
        }

        if (hue >= 40 && hue <= 85) {
            return DetectedColor.YELLOW;
        }
        else if (hue >= 190 && hue <= 250) {
            return DetectedColor.BLUE;
        }
        else if (hue <= 25 || hue >= 330) {
            return DetectedColor.RED;
        }

        return DetectedColor.UNKNOWN;
    }
}
