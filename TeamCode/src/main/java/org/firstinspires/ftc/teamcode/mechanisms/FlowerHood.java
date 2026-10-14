package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class FlowerHood {
    private Servo hood;

    public void init(HardwareMap hwMap) {
        hood = hwMap.get(Servo.class, "hood_servo");
        hood.scaleRange(0.0, 0.5); // for 180 degrees servo
//        hood.scaleRange(0.0, 0.67); // for 270 degrees servo
    }

    public void down() {
        hood.setPosition(0.0);
    }

    public void up() {
        hood.setPosition(1.0);
    }
}
