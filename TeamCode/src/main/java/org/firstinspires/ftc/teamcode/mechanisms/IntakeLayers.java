package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeLayers {
    private DcMotor outer, inner;

    public void init(HardwareMap hardwareMap) {
        outer = hardwareMap.get(DcMotor.class, "outer_intake_motor");
        outer.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        outer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // outer.setDirection(DcMotor.Direction.REVERSE);

        inner = hardwareMap.get(DcMotor.class, "inner_intake_motor");
        inner.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        inner.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // inner.setDirection(DcMotor.Direction.REVERSE);
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
}
