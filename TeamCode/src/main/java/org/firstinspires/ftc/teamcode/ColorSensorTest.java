package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.ColorSensor;

@TeleOp
public class ColorSensorTest extends OpMode {
    private ColorSensor cs = new ColorSensor();

    @Override
    public void init() {
        cs.init(hardwareMap);
    }

    @Override
    public void loop() {
        cs.getDetectedColor(telemetry);
    }
}
