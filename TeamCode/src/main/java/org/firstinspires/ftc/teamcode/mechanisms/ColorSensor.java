package org.firstinspires.ftc.teamcode.mechanisms;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class ColorSensor {
    private NormalizedColorSensor colorSensor;

    private final float[] hsv = new float[3];

    public void init(HardwareMap hwMap) {
        colorSensor = hwMap.get(NormalizedColorSensor.class, "shooter_color_sensor");
        colorSensor.setGain(8);
    }

    public IntakeLayers.DetectedColor getDetectedColor(Telemetry telemetry) {
        if (colorSensor == null) {
            return IntakeLayers.DetectedColor.UNKNOWN;
        }

        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        int r, g, b;
        r = (int) (colors.red * 255.0f);
        g = (int) (colors.green * 255.0f);
        b = (int) (colors.blue * 255.0f);

        Color.RGBToHSV(r, g, b, hsv);

        float hue = hsv[0];
        float saturation = hsv[1];
        float value = hsv[2];

        // telemetry part only for calibration; comment-out in real game
        telemetry.addData("hue (inner)", hue);
        telemetry.addData("saturation (inner)", saturation);
        telemetry.addData("value (inner)", value);


        return IntakeLayers.DetectedColor.UNKNOWN;
    }
}
