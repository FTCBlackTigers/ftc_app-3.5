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

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

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
public class BT_Intake
{
    /* Public OpMode members. */
    public DcMotor leftIntake = null;
    public DcMotor rightIntake = null;
    public DcMotor intakeMotor = null;
    public Servo intakeServo = null;

    public static final double MID_INTAKE_POWER  = 0.9;
    public static final double INTAKE_POWER  = 1;
    public static final double EJECT_POWER  = 0.8;
    public static final double SERVO_OUT  = 1 ;
    public static final double SERVO_IN = 0 ;
    public static final double STOP_SERVO  = 0.393 ;
    public static boolean isPressed = false;
    /* local OpMode members. */
    HardwareMap hwMap = null;

    /* Constructor */
    public BT_Intake(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;
        // Define and Initialize Motors

        leftIntake = hwMap.get(DcMotor.class, "leftIntake");
        rightIntake = hwMap.get(DcMotor.class, "rightIntake");
        intakeMotor =  hwMap.get(DcMotor.class, "intakeMotor");
        intakeServo = hwMap.get(Servo.class, "intakeServo");

        leftIntake.setDirection(DcMotor.Direction.REVERSE); // Set to REVERSE if using AndyMark motors
        rightIntake.setDirection(DcMotor.Direction.FORWARD);// Set to FORWARD if using AndyMark motors
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);// Set to FORWARD if using AndyMark motors
        intakeServo.setDirection(Servo.Direction.FORWARD);
        // Set all motors to zero power
        stop();

        leftIntake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightIntake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeServo.setPosition(STOP_SERVO);
    }
    public void glyphsIn() {
        leftIntake.setPower(INTAKE_POWER);
        rightIntake.setPower(INTAKE_POWER);
        intakeMotor.setPower(MID_INTAKE_POWER);
        intakeServo.setPosition(SERVO_IN);
    }

    public void glyphsOut() {
        leftIntake.setPower(-INTAKE_POWER);
        rightIntake.setPower(-INTAKE_POWER);
        intakeMotor.setPower(-MID_INTAKE_POWER);
        intakeServo.setPosition(SERVO_OUT);
    }

    public void ejectGlyphs() {
        leftIntake.setPower(EJECT_POWER);
        rightIntake.setPower(EJECT_POWER);
    }
    public void stop() {
        // Set all motors to zero power
        leftIntake.setPower(0);
        rightIntake.setPower(0);
        intakeMotor.setPower(0);
        intakeServo.setPosition(STOP_SERVO);
    }
    public void teleopMotion(Gamepad gamepad, Telemetry telemetry){
        boolean glyphOut = gamepad.left_trigger > 0.5;
        boolean glyphIn = gamepad.right_trigger > 0.5;
        if (glyphOut) {
            isPressed = true ;
            glyphsOut();
            telemetry.addData("dr: ","glyphs out");
            telemetry.addLine("power: "+ intakeMotor.getPower());
        }
        else if (glyphIn) {
            isPressed = true;
            glyphsIn();
            telemetry.addData("dr: ","glyphs in");
        }
        else {
           stop();
            isPressed = false;
        }
    }
 }
