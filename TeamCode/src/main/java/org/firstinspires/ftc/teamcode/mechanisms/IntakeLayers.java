package org.firstinspires.ftc.teamcode.mechanisms;

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

        float normRed, normGreen, normBlue;
        normRed = colors.red / alpha;
        normGreen = colors.green / alpha;
        normBlue = colors.blue / alpha;

        // telemetry part only for calibration; comment-out in real game
        telemetry.addData(side+" red", normRed);
        telemetry.addData(side+" green", normGreen);
        telemetry.addData(side+" blue", normBlue);

        // need to adjust these values
        if (normRed > 0.35 && normGreen < 0.3 && normBlue < 0.3) {
            return DetectedColor.RED;
        }
        else if (normRed > 0.5 && normGreen > 0.9 && normBlue < 0.6) {
            return DetectedColor.YELLOW;
        }
        else if (normRed < 0.2 && normGreen < 0.5 && normBlue > 0.5) {
            return DetectedColor.BLUE;
        }

        return DetectedColor.UNKNOWN;
    }
}
