package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class FlowerExtraction {
    private Servo extractorL, extractorR;

    public void init(HardwareMap hwMap) {
        extractorL = hwMap.get(Servo.class, "left_extractor_servo");
        extractorL.scaleRange(0.0, 0.5); // for 180 degrees servo
//        extractorL.scaleRange(0.0, 0.67); // for 270 degrees servo

        extractorR = hwMap.get(Servo.class, "right_extractor_servo");
        extractorR.scaleRange(0.0, 0.5); // for 180 degrees servo
//        extractorR.scaleRange(0.0, 0.67); // for 270 degrees servo
    }

    public void down() {
        extractorL.setPosition(0.0);
        extractorR.setPosition(0.0);
    }

    public void up() {
        extractorL.setPosition(1.0);
        extractorR.setPosition(1.0);
    }
}
