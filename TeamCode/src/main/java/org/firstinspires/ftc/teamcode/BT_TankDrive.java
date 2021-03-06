/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * This is NOT an opmode.
 *
 * This class can be used to define all the specific hardware for a single robot.
 * In this case that robot is a Pushbot.
 * See PushbotTeleopTank_Iterative and others classes starting with "Pushbot" for usage examples.
 *
 * This hardware class assumes the following device names have been configured on the robot:
 * Note:  All names are lower case and some have single spaces between words.
 *
 */
public class BT_TankDrive {
    /* Public OpMode members. */
    public DcMotor  frontLeftDrive   = null;
    public DcMotor  frontRightDrive  = null;
    public DcMotor  rearLeftDrive   = null;
    public DcMotor  rearRightDrive  = null;
    public BT_Gyro  gyro = new BT_Gyro();
    
    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 2.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_CM       = 10.16 ;     // For figuring circumference
    static final double     COUNTS_PER_CM           = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_CM * 3.1415);
    static final double     AUTO_DRIVE_SPEED             = 0.6;
    static final double      AUTO_TURN_SPEED              = 0.3;

    static final double     THRESHOLD = 0.1;
    static final double     P_TURN_COEFF            = 0.1;

    ElapsedTime runtime = new ElapsedTime();
    private OpMode callerOpmode;

    /* local OpMode members. */
    HardwareMap hwMap           =  null;
    
    /* Constructor */
    public BT_TankDrive(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap, OpMode callerOpmode) {
        // Save reference to Hardware map
        hwMap = ahwMap;
        this.callerOpmode = callerOpmode;

        // Define and Initialize Motors
        frontLeftDrive  = hwMap.get(DcMotor.class, "frontLeftDrive");
        frontRightDrive = hwMap.get(DcMotor.class, "frontRightDrive");
        rearLeftDrive = hwMap.get(DcMotor.class, "rearLeftDrive");
        rearRightDrive = hwMap.get(DcMotor.class, "rearRightDrive");

        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD); // Set to REVERSE if using AndyMark motors
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);// Set to FORWARD if using AndyMark motors
        rearLeftDrive.setDirection(DcMotor.Direction.FORWARD); // Set to REVERSE if using AndyMark motors
        rearRightDrive.setDirection(DcMotor.Direction.REVERSE);// Set to FORWARD if using AndyMark motors

        // Set all motors to zero power
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        rearLeftDrive.setPower(0);
        rearRightDrive.setPower(0);

        // Set all motors to run without encoders.
        // May want to use RUN_USING_ENCODERS if encoders are installed.
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //Initiate the gyro
        gyro.init(hwMap);
    }
    
    public void move (double distCm , double timeoutS ){
        encoderDrive( AUTO_DRIVE_SPEED, distCm, distCm, timeoutS);
    }
    public void moveByPower (double power ){
        frontLeftDrive.setPower(power);
        frontRightDrive.setPower(power);
    }

    public void turn (double degrees, double timeoutMs, Telemetry telemetry) {

        double rightSpeed, leftSpeed;
        double steer;
        double error = getError(degrees);
        double t;
        runtime.reset();
        // keep looping while we are still active, and not on heading.
        while((Math.abs(error) > THRESHOLD) && (runtime.time() < timeoutMs)) {
            while (Math.abs(error) > THRESHOLD ) {
                // Update telemetry & Allow time for other processes to run.
                steer = getSteer(error, P_TURN_COEFF);
                rightSpeed = AUTO_TURN_SPEED * steer;
                if(rightSpeed>0&& rightSpeed<0.1)
                    rightSpeed=0.1;
                else if(rightSpeed<0&&rightSpeed>-0.1)
                    rightSpeed=-0.1;
                leftSpeed = -rightSpeed;

                frontLeftDrive.setPower(leftSpeed);
                frontRightDrive.setPower(rightSpeed);
                error = getError(degrees);
                telemetry.addData("Error", error);
                telemetry.update();
            }
            frontLeftDrive.setPower(0);
            frontRightDrive.setPower(0);
            t= runtime.time();
            while (runtime.time() < t + 300){
                error = getError(degrees);
                telemetry.addData("Error", error);
                telemetry.update();
            }
        }
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
    }
    public double getError(double targetAngle) {

        double robotError;

        // calculate error in -179 to +180 range  (
        robotError = targetAngle - gyro.getAngle();
        while (robotError > 180)  robotError -= 360;
        while (robotError <= -180) robotError += 360;
        return robotError;
    }
    public double getSteer(double error, double PCoeff) {
        return Range.clip(error * PCoeff, -1, 1);
    }

    public void tankTeleopDrive (Gamepad gamepad) {
        double left;
        double right;
        // Run wheels in tank mode (note: The joystick goes negative when pushed forwards, so negate it)
        left = -gamepad.left_stick_y;
        right = -gamepad.right_stick_y;

        frontLeftDrive.setPower(left);
        frontRightDrive.setPower(right);
        rearLeftDrive.setPower(left);
        rearRightDrive.setPower(right);
    }

    public void encoderDrive(double speed,
                             double leftCm, double rightCm,
                             double timeoutMs) {
        int newLeftTarget;
        int newRightTarget;
        ElapsedTime runtime =new ElapsedTime();
            // Determine new target position, and pass to motor controller
            newLeftTarget = frontLeftDrive.getCurrentPosition() + (int)(leftCm * COUNTS_PER_CM);
            newRightTarget = frontRightDrive.getCurrentPosition() + (int)(rightCm * COUNTS_PER_CM);
            frontLeftDrive.setTargetPosition(newLeftTarget);
            frontRightDrive.setTargetPosition(newRightTarget);

            // Turn On RUN_TO_POSITION
            frontLeftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            frontRightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            frontLeftDrive.setPower(Math.abs(speed));
            frontRightDrive.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while ((runtime.time() < timeoutMs) &&
                    (frontLeftDrive.isBusy() && frontRightDrive.isBusy())) {

            }

            // Stop all motion;
            frontLeftDrive.setPower(0);
            frontRightDrive.setPower(0);

            // Turn off RUN_TO_POSITION
            frontLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            frontRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        }
    }
