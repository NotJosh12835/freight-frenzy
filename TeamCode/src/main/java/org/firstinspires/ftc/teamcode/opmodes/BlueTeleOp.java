package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.CarouselManipulator;
import org.firstinspires.ftc.teamcode.subsystems.Depositor;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Lift;

@TeleOp(name = "Blue TeleOp")
public class BlueTeleOp extends RobotOpMode {


    @Override
    public void init() {
        super.init();
        intake.setIntakeState(Intake.IntakeState.OFF);
        drive.brakeMode(true);
        depositor.setDepositorState(Depositor.depositorState.RESTING);
        duckScorer.setManipulatorState(CarouselManipulator.CarouselManipulatorState.STOWED);
        duckScorer.setAllianceSide(CarouselManipulator.Alliance.BLUE);
    }

    public enum AngleModeBlue{
        MANUAL,
        AUTO
    }

    private BlueTeleOp.AngleModeBlue anglerSwitch = BlueTeleOp.AngleModeBlue.AUTO;
    private int CARGO_THRESHOLD = 160;
    //private int BOX_THRESHOLD = 10;

    @Override
    public void loop() {
        super.loop();

        drive.cartesianDrive(gamepad1.left_stick_x, -gamepad1.left_stick_y, -gamepad1.right_stick_x *.75);
        //drive.cartesianDrive(gamepad1.left_stick_x, gamepad1.left_stick_y , gamepad1.right_stick_x);


        if (lift.getExtensionState() == Lift.ExtensionState.IDLE && intake.getIntakeState() == Intake.IntakeState.IN){
            depositor.freightCheck();
        }

        if (depositor.getStorageState() == Depositor.storageState.IN){
            intake.setIntakeState(Intake.IntakeState.OUT);
            gamepad1.rumble(500);
            //gamepad1.rumble(1.0, 1.0, 1000);
            gamepad2.rumble(500);
            depositor.setStorageState(Depositor.storageState.STORED);
        }

        if (epicGamer1.RIGHT_BUMPER.pressed()){
            if (intake.getIntakeState() != Intake.IntakeState.IN){
                intake.setIntakeState(Intake.IntakeState.IN);
                depositor.setLockState(Depositor.lockState.UNLOCK);
            }
            else {
                intake.setIntakeState(Intake.IntakeState.OFF);
            }
        }

        if (epicGamer1.LEFT_BUMPER.pressed()){
            if (intake.getIntakeState() != Intake.IntakeState.OUT){
                intake.setIntakeState(Intake.IntakeState.OUT);
            }
            else {
                intake.setIntakeState(Intake.IntakeState.OFF);
            }
        }

        if (epicGamer1.X.pressed()){
            depositor.setDepositorState(Depositor.depositorState.SCORING);
        }

        if(epicGamer2.X.pressed()) {
            if (depositor.getDepositorState() == Depositor.depositorState.SCORING) {
                depositor.setDepositorState(Depositor.depositorState.RESTING);
            }
            else {
                depositor.setDepositorState(Depositor.depositorState.SCORING);
            }

        }

        if (epicGamer1.DPAD_UP.pressed()){
            intake.setIntakeState(Intake.IntakeState.UP);
        }

        if (gamepad2.right_trigger != 0){
            lift.setExtensionState(Lift.ExtensionState.OUT);
        }
        else if (gamepad2.left_trigger != 0 && lift.getRetractionLimitValue()){
            lift.setExtensionState(Lift.ExtensionState.IN);
            depositor.setDepositorState(Depositor.depositorState.RESTING);
        }
        else if (lift.getExtensionState() != Lift.ExtensionState.EXTEND_TO_ANGLE && lift.getExtensionState() != Lift.ExtensionState.HOMING) {
            lift.setExtensionState(Lift.ExtensionState.IDLE);
        }


        if (epicGamer1.DPAD_LEFT.state){
            intake.setIntakeState(Intake.IntakeState.UP);
            duckScorer.setManipulatorState(CarouselManipulator.CarouselManipulatorState.REST);
            //intake.setIntakeState(Intake.IntakeState.IN);
        }
        if (epicGamer1.DPAD_RIGHT.state){
            intake.setIntakeState(Intake.IntakeState.UP);
            duckScorer.setManipulatorState(CarouselManipulator.CarouselManipulatorState.SCORING);
            //intake.setIntakeState(Intake.IntakeState.UP);
        }
        if (epicGamer1.DPAD_DOWN.state){
            duckScorer.setManipulatorState(CarouselManipulator.CarouselManipulatorState.STOWED);
        }

        if (epicGamer2.DPAD_RIGHT.pressed()){
            if (anglerSwitch != BlueTeleOp.AngleModeBlue.AUTO) {
                anglerSwitch = BlueTeleOp.AngleModeBlue.AUTO;
            }
            else {
                anglerSwitch = BlueTeleOp.AngleModeBlue.MANUAL;
            }
        }

        if (epicGamer2.LEFT_BUMPER.pressed()){
            depositor.setDepositorState(Depositor.depositorState.RESTING);
            lift.setExtensionState(Lift.ExtensionState.HOMING);
            depositor.setStorageState(Depositor.storageState.NONE);
            depositor.setLockPosition(depositor.UNLOCK);
        }

        if (epicGamer2.RIGHT_BUMPER.pressed()){
            lift.setAnglerState(Lift.AngleState.ADJUST_DOWN);
        }

        if(epicGamer2.LEFT_JOYSTICK_PUSH.pressed()){
            lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
            //lift.setAnglerState(Lift.AngleState.CAP_COLLECT);
        }

        if(epicGamer2.RIGHT_JOYSTICK_PUSH.pressed()){
            lift.setAnglerState(Lift.AngleState.CAP_SCORE);
        }

        if(epicGamer1.RIGHT_JOYSTICK_PUSH.pressed()){
            depositor.setLockPosition(depositor.UNLOCK);
        }

        if(epicGamer1.RIGHT_JOYSTICK_PUSH.pressed()){
            if (depositor.getLockState() == Depositor.lockState.LOCK){
                depositor.setStorageState(Depositor.storageState.NONE);
                depositor.setLockState(Depositor.lockState.UNLOCK);
            }
            else {
                depositor.setLockState(Depositor.lockState.LOCK);
            }
        }

        switch (anglerSwitch){
            case AUTO:
                if (epicGamer2.A.pressed()){
                    lift.setAnglerState(Lift.AngleState.BOTTOM);
                    depositor.setDepositorState(Depositor.depositorState.RESTING);
                }
                if (epicGamer2.B.pressed()){
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    lift.setAnglerState(Lift.AngleState.MID);
                    depositor.setDepositorState(Depositor.depositorState.MID_ANGLE);
                }
                if (epicGamer2.Y.pressed()) {

                    if (depositor.getFreightBlue() > CARGO_THRESHOLD){
                        depositor.setLockState(Depositor.lockState.LOCK);
                        lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                        lift.setAnglerState(Lift.AngleState.TOP);
                        depositor.setDepositorState(Depositor.depositorState.CARGO_CRADLE);
                    }
                    /*else if (CARGO_THRESHOLD < depositor.getFreightRed() && depositor.getFreightRed() < BOX_THRESHOLD){
                        lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                        lift.setAnglerState(Lift.AngleState.TOP);
                        depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);
                    }
                     */
                    else {
                        lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                        lift.setAnglerState(Lift.AngleState.TOP);
                        depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);
                    }

                }
                if (epicGamer2.DPAD_LEFT.pressed()){
                    depositor.setLockState(Depositor.lockState.LOCK);
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    lift.setAnglerState(Lift.AngleState.DUCK);
                    depositor.setDepositorState(Depositor.depositorState.DUCK_ANGLE);
                }

                if(epicGamer2.RIGHT_BUMPER.pressed()){
                    depositor.setLockState(Depositor.lockState.DUCK);
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    lift.setAnglerState(Lift.AngleState.DUCK);
                    depositor.setDepositorState(Depositor.depositorState.DUCK_ANGLE);
                }
                break;
            case MANUAL:
                if (epicGamer2.DPAD_UP.state){
                    lift.setAnglerState(Lift.AngleState.MANUAL_UP);
                }
                else if (epicGamer2.DPAD_DOWN.state){
                    lift.setAnglerState(Lift.AngleState.MANUAL_DOWN);
                }
                else {
                    lift.setAnglerState(Lift.AngleState.IDLE);
                }
                break;
        }

        telemetry.addData("Angler Position: ", lift.getAnglerPosition());
        telemetry.addData("liftLeft Position: ", lift.getLiftLeftPosition());
        telemetry.addData("liftRight Position: ", lift.getLiftRightPosition());
        telemetry.addData("Limit pressed = ", lift.getAngleLimitValue());
        telemetry.addData("Angle State: ", lift.getAnglerState());
        telemetry.addData("Angler Power: ", lift.getAnglerPower());
        telemetry.addData("Depositor Pivot Position: ", depositor.getDepositorPivotPosition());
        telemetry.addData("Retraction Limit: ", !lift.getRetractionLimitValue());
        telemetry.addData("Detection Distance Inches: ", depositor.getDetectionDistanceInches());
        //telemetry.addData("Detection Distance Centimeters: ", depositor.getDetectionDistanceCentimeter());
        telemetry.addData("Extension Through Bore Encoder:", lift.getLiftRightPosition());
        telemetry.addData("Angle Adjuster Through Bore Encoder:", intake.getIntakePosition());
        telemetry.addData("Extension State: ", lift.getExtensionState());
        telemetry.addData("Color Reading: ", depositor.getFreightBlue());
        telemetry.addData("Storage State: ", depositor.getStorageState());
        telemetry.addData("Angle Adjust Through-Bore: ", intake.getIntakePosition());
        //telemetry.addData("Runtime: ", getRuntime());


    }
}
