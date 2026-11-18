# FTC 38503 🤖🐝
**Team Members:** Jiho Lee, Paige Chang, Mikhail Pilipenko, Stella Gwun, Eloy Kim, Vihan Lalit

This is the code repository for Earl of March's FTC 38503 team for the **2026–2027 BIOBUZZ** season. It is based on the official FTC SDK ([FtcRobotController](https://github.com/FIRST-Tech-Challenge/FtcRobotController)).

---

## Architecture 🧱
* **Mechanisms**
    * `MecanumDrive`
    * `Shooter`
    * `IntakeLayers`
    * `FlowerExtraction`
    * `FlowerHood`
* **OpModes**
    * `TeleOpMode`
    * `AutoUpSideRed`
    * `AutoUpSideBlue`
    * `AutoDownSide1Blue`
    * `AutoDownSide1Red`
    * `AutoDownSide2Blue`
    * `AutoDownSide2Red`
---

## Strategy 🧠
As a rookie team, we are focused on building a realistic robot that still performs reliably in competitive settings.

* **Autonomous:** We identified 3 distinct starting and alliance scenarios, resulting in 6 total OpModes (3 per alliance color):
    1. Starting closer to the upper cell.
    2. Starting closer to the lower cell when our alliance partner does **not** shoot.
    3. Starting closer to the lower cell when our alliance partner **does** shoot.
* **TeleOp:** We prioritize shooting in the hives and will not frequently attempt to shoot into the flowers.
* **Endgame:** We continue shooting in the hives to maximize points.

---

## Hardware Breakdown 🚀
*FTC Legal Limit: 8 DC Motors, 8 Servos (using 6 DC motor ports, 7 encoder ports, and 3 servo ports).*

### DC Motors & Encoders

| Subsystem | Hardware / Port Usage | Notes |
| :--- | :--- | :--- |
| **Drivetrain** | 4 DC Motors | Mecanum drive wheels |
| **Shooter** | 1 DC Motor | Flywheel launcher |
| **Outer Intake & X-Odometry** | 1 DC Motor + Encoder Port | Motor runs without an encoder; encoder port reads X-axis (forward) dead wheel |
| **Y-Odometry** | Encoder Port Only | Reads Y-axis (sideways) dead wheel |

### Servos

| Subsystem | Type | Notes |
| :--- | :--- | :--- |
| **Flower Extraction** | 1 Positional Servo | Extracts game elements from flowers |
| **Flower Hood** | 1 Positional Servo | Adjusts hood for flower scoring |
| **Turret Backboard** | 1 Continuous Rotation (CR) Servo | Drives backboard via D-shaft |

---

## Gamepad Controls 🎮
| Input | Action | Notes |
| :--- | :--- | :--- |
| **Left Joystick** | Translational Movement | Strafe and forward/backward drive |
| **Right Joystick** | Rotation | Turn robot left/right |
| **A** | Shoot | Active when in manual shooting mode |
| **B** | Toggle Shooting Mode | Switches between manual and auto shooting |
| **X** | Flower Extraction Mode | Toggles flower extraction mechanism |
| **Y** | Toggle Hood | Moves flower shooting hood up/down |
| **Left Bumper** | Reverse Intake | Changes intake motor direction |
| **Right Bumper** | Stop Intake | Cuts power to intake motor |