package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class FlowerExtraction {
    private Servo extractor;

    public void init(HardwareMap hwMap) {
        extractor = hwMap.get(Servo.class, "extractor_servo");
        extractor.scaleRange(0.0, 0.5); // for 180 degrees servo
//        extractor.scaleRange(0.0, 0.67); // for 270 degrees servo
    }

    public void down() {
        extractor.setPosition(0.0);
    }

    public void up() {
        extractor.setPosition(1.0);
    }
}
