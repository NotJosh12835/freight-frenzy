package org.firstinspires.ftc.teamcode.opmodes.autos;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.motion.TrajectorySequence;
import org.firstinspires.ftc.teamcode.subsystems.CarouselManipulator;
import org.firstinspires.ftc.teamcode.subsystems.Depositor;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Lift;
import org.firstinspires.ftc.teamcode.subsystems.RRMecanumDrive;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.vision.BlueWarehouseTeamElementPipeline;

@Autonomous
public class BlueWarehouseAuto extends RobotAuto {

    BlueWarehouseTeamElementPipeline.Location elementLocation = BlueWarehouseTeamElementPipeline.Location.RIGHT;

    private static int DIST_FROM_WALL = 15;
    private static int COLLECT_DEPTH = 22; //Formally: 22
    private static int SCORE_ANGLE = 317;
    private static int Y_OFFSET = 7;
    private static int X_OFFSET = 6;
    public double autoStartTime;
    public int cycleCount = 0;

    public static final Pose2d startPose = new Pose2d(6, 63);
    public static final Pose2d FIRST_SCORE_POSE = new Pose2d(3, 56, Math.toRadians(327));
    public static final Pose2d SCORE_POSE = new Pose2d(9, 56, Math.toRadians(SCORE_ANGLE));
    public static final Pose2d GAP_OUTER_POSE = new Pose2d(DIST_FROM_WALL, 56, Math.toRadians(270));
    public static final Pose2d GAP_INNER_POSE = new Pose2d(DIST_FROM_WALL, 31, Math.toRadians(270));
    public static final Pose2d COLLECT_POSE = new Pose2d(DIST_FROM_WALL, COLLECT_DEPTH, Math.toRadians(270));


    public static final Pose2d SECOND_COLLECT_POSE = new Pose2d(DIST_FROM_WALL + X_OFFSET, COLLECT_DEPTH - Y_OFFSET + 1, Math.toRadians(255));
    public static final Pose2d SECOND_GAP_OUTER_POSE = new Pose2d(DIST_FROM_WALL + X_OFFSET, 56 - Y_OFFSET, Math.toRadians(270));
    public static final Pose2d SECOND_SCORE_POSE = new Pose2d(6 + X_OFFSET, 56 - Y_OFFSET, Math.toRadians(SCORE_ANGLE));

    public static final Pose2d THIRD_COLLECT_POSE = new Pose2d(26, 12, Math.toRadians(250));
    public static final Pose2d THIRD_GAP_OUTER_POSE = new Pose2d(26, 47, Math.toRadians(270));
    public static final Pose2d THIRD_SCORE_POSE = new Pose2d(18, 48, Math.toRadians(317));

    public static final Pose2d GAP_POSE = new Pose2d(21, 40, Math.toRadians(270));
    public static final Pose2d FAST_PARK = new Pose2d(33, 14, Math.toRadians(270));
    public static final Pose2d PARK_POSE = new Pose2d(-12, 28, Math.toRadians(270));

    public Pose2d lastPose = startPose;
    public double lastHeading = 0;

    private static int TURN_TO_WALL = -59;

    private static int EXTEND_TO_ANGLE = 350;
    private static int EXTEND_TO_TOP = 1175; //2530
    private static int EXTEND_TO_MID = 1275; //2400
    private static int EXTEND_TO_BOTTOM = 1250; //2325
    private static int TIME_TO_DEPOSIT = 600;

    Trajectory toFirstScore;
    Trajectory toWarehouse;
    Trajectory toCollect;
    Trajectory toScore;
    Trajectory toSafePark;
    Trajectory toFirstWarehouse;
    Trajectory toGapExit;
    Trajectory toGapEntrance;
    Trajectory toOuterField;

    Trajectory toSecondOuterField;
    Trajectory toSecondGapEntrance;
    Trajectory toSecondScore;
    Trajectory toSecondCollect;

    Trajectory toThirdOuterField;
    Trajectory toThirdGapEntrance;
    Trajectory toThirdScore;
    Trajectory toThirdCollect;

    Trajectory goForward;
    Trajectory toFastPark;

    private enum CyclingState{
        COLLECTING,
        TRAVELLING,
        DELIVERING,
        NULL,
        PARKING
    }

    CyclingState cycleState = CyclingState.NULL;

    @Override
    public void runOpMode() throws InterruptedException {
        subsystemConfigType = subsystemConfig.WAREHOUSE_AUTO;

        initialize();
        vision.setRobotLocation(Vision.robotLocation.BLUE_WAREHOUSE);
        vision.enable();


        RRMecanumDrive RRdrive = new RRMecanumDrive(hardwareMap);

        while (!opModeIsActive() && !isStopRequested()){
            telemetry.addData("Element Location: ", vision.getElementPipelineBlueWarehouse().getLocation());
            telemetry.update();
        }

        RRdrive.setPoseEstimate(startPose);

        duckScorer.setManipulatorState(CarouselManipulator.CarouselManipulatorState.STOWED);

        waitForStart();

        autoStartTime = getRuntime();

        lift.setExtensionState(Lift.ExtensionState.IDLE);

        updateThread.start();

        elementLocation = vision.getElementPipelineBlueWarehouse().getLocation();

        toFirstScore = RRdrive.trajectoryBuilder(startPose)
                .lineToLinearHeading(FIRST_SCORE_POSE)
                .addTemporalMarker(0.1, () -> {
                    depositor.setLockState(Depositor.lockState.LOCK);
                    //lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    switch (elementLocation){

                        case RIGHT:
                            lift.setExtensionState(Lift.ExtensionState.FIRST_TOP_EXTEND);
                            lift.setAnglerState(Lift.AngleState.AUTO_TOP);
                            break;
                        case MID:
                            lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_MID);
                            lift.setAnglerState(Lift.AngleState.AUTO_MID);
                            break;
                        case LEFT:
                            lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_BOTTOM);
                            lift.setAnglerState(Lift.AngleState.AUTO_BOTTOM);
                            break;
                    }
                })
                .build();

        toGapEntrance = RRdrive.trajectoryBuilder(lastPose) //toScore.end()
                .lineToLinearHeading(GAP_OUTER_POSE)
                .build();

        toSecondGapEntrance = RRdrive.trajectoryBuilder(lastPose) //toScore.end()
                .lineToLinearHeading(SECOND_GAP_OUTER_POSE)
                .build();

        toThirdGapEntrance = RRdrive.trajectoryBuilder(lastPose)
                .lineToLinearHeading(THIRD_GAP_OUTER_POSE)
                .build();

        toCollect = RRdrive.trajectoryBuilder(toGapEntrance.end())
                .lineToLinearHeading(COLLECT_POSE)
                .build();

        toSecondCollect = RRdrive.trajectoryBuilder(toSecondGapEntrance.end())
                .lineToLinearHeading(SECOND_COLLECT_POSE)
                .build();

        toThirdCollect = RRdrive.trajectoryBuilder(toThirdGapEntrance.end())
                .lineToLinearHeading(THIRD_COLLECT_POSE)
                .build();

        toOuterField = RRdrive.trajectoryBuilder(toCollect.end())
                .lineToLinearHeading(GAP_OUTER_POSE)
                .addTemporalMarker(0.3, () -> {
                    intake.setIntakeState(Intake.IntakeState.OUT);
                })
                .addSpatialMarker(new Vector2d(12, 31), () -> {
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    lift.setAnglerState(Lift.AngleState.AUTO_TOP);
                    //depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);
                })
                .addSpatialMarker(new Vector2d(12, 40), () -> {
                    intake.setIntakeState(Intake.IntakeState.OFF);
                })
                .build();

        toSecondOuterField = RRdrive.trajectoryBuilder(toSecondCollect.end())
                .lineToLinearHeading(SECOND_GAP_OUTER_POSE)
                .addTemporalMarker(0.3, () -> {
                    intake.setIntakeState(Intake.IntakeState.OUT);
                })
                .addSpatialMarker(new Vector2d(12, 31), () -> {
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    lift.setAnglerState(Lift.AngleState.AUTO_TOP);
                    //depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);
                })
                .addSpatialMarker(new Vector2d(12, 40), () -> {
                    intake.setIntakeState(Intake.IntakeState.OFF);
                })
                .build();

        toThirdOuterField = RRdrive.trajectoryBuilder(toThirdCollect.end())
                .lineToLinearHeading(THIRD_GAP_OUTER_POSE)
                .addTemporalMarker(0.3, () -> {
                    intake.setIntakeState(Intake.IntakeState.OUT);
                })
                .addSpatialMarker(new Vector2d(12, 31), () -> {
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);
                    lift.setAnglerState(Lift.AngleState.AUTO_TOP);
                    //depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);
                })
                .addSpatialMarker(new Vector2d(12, 40), () -> {
                    intake.setIntakeState(Intake.IntakeState.OFF);
                })
                .build();


        toScore = RRdrive.trajectoryBuilder(toOuterField.end())
                .lineToLinearHeading(SCORE_POSE)
                .addTemporalMarker(0.1, () -> {
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_TOP);
                })
                .build();

        toSecondScore = RRdrive.trajectoryBuilder(toSecondOuterField.end())
                .lineToLinearHeading(SECOND_SCORE_POSE)
                .addTemporalMarker(0.1, () -> {
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_TOP);
                })
                .build();

        toThirdScore = RRdrive.trajectoryBuilder(toThirdOuterField.end())
                .lineToLinearHeading(THIRD_SCORE_POSE)
                .addTemporalMarker(0.1, () -> {
                    lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_TOP);
                })
                .build();

        toFastPark = RRdrive.trajectoryBuilder(new Pose2d(33, 56 - Y_OFFSET, Math.toRadians(270)))
                .lineToLinearHeading(FAST_PARK)
                .build();


        //Drive backwards slightly and turn towards shipping hub

        RRdrive.followTrajectory(toFirstScore);
        sleep(250);


        depositor.setDepositorState(Depositor.depositorState.SCORING);
        sleep(TIME_TO_DEPOSIT);

        homeLift();

        lastPose = RRdrive.getPoseEstimate();

        RRdrive.followTrajectory(toGapEntrance);

        cycleState = CyclingState.COLLECTING;


        while(!isStopRequested() && opModeIsActive() && cycleState != CyclingState.NULL) {

            switch (cycleState) {
                case COLLECTING:

                    intake.setIntakeState(Intake.IntakeState.IN);

                    /*
                    if (cycleCount >= 1){
                        RRdrive.followTrajectoryAsync(toCorrectedCollect);
                    }
                    else {
                        RRdrive.followTrajectoryAsync(toCollect);
                    }
                    while (depositor.getDetectionDistanceInches() > 1.5 && !isStopRequested() && opModeIsActive() && RRdrive.isBusy()) {
                        RRdrive.update();
                    }
                    */

                    if (cycleCount < 1){
                        RRdrive.followTrajectory(toCollect);
                    }
                    else if (cycleCount == 1){
                        RRdrive.followTrajectory(toSecondCollect);
                    }
                    else{
                        RRdrive.followTrajectory(toThirdCollect);
                    }

                    depositor.setLockState(Depositor.lockState.LOCK);

                    cycleState = CyclingState.DELIVERING;
                    break;

                case DELIVERING:
                    if (cycleCount < 1){
                        intake.setIntakeState(Intake.IntakeState.OUT);
                        RRdrive.followTrajectory(toOuterField);
                        RRdrive.followTrajectory(toScore);
                    }
                    else if (cycleCount == 1) {
                        intake.setIntakeState(Intake.IntakeState.OUT);
                        RRdrive.followTrajectory(toSecondOuterField);
                        RRdrive.followTrajectory(toSecondScore);
                    }
                    else {
                        intake.setIntakeState(Intake.IntakeState.OUT);
                        RRdrive.followTrajectory(toThirdOuterField);
                        RRdrive.followTrajectory(toThirdScore);
                        //sleep(350);
                    }

                    depositor.setDepositorState(Depositor.depositorState.SCORING);
                    sleep(TIME_TO_DEPOSIT);
                    //extendToScore(Lift.AngleState.TOP);
                    homeLift();

                    if ((getRuntime() - autoStartTime) >= 23.0){
                        cycleState = CyclingState.PARKING;
                    }
                    else {

                        cycleCount++;

                        if (cycleCount == 1){
                            RRdrive.followTrajectory(toGapEntrance);
                        }
                        else if (cycleCount == 2){
                            RRdrive.followTrajectory(toSecondGapEntrance);
                        }
                        else {
                            RRdrive.followTrajectory(toThirdGapEntrance);
                        }

                        cycleState = CyclingState.COLLECTING;

                    }

                    /*
                    if (cycleCount >= 1){
                        RRdrive.followTrajectory(toCorrectedGapEntrance);
                    }
                    else {
                        RRdrive.followTrajectory(toGapEntrance);
                    }

                    //double time = getRuntime();
                    //RRdrive.followTrajectory(toWarehouse);

                    if ((getRuntime() - autoStartTime) >= 23.0) {
                        cycleState = CyclingState.PARKING;
                    }
                    else {
                        cycleCount++;
                        cycleState = CyclingState.COLLECTING;
                    }

                     */
                    break;

                case PARKING:
                    //RRdrive.followTrajectory(toWarehouse);
                    //RRdrive.followTrajectory(toSafePark);
                    RRdrive.followTrajectory(toFastPark);
                    cycleState = CyclingState.NULL;
                    break;

                case NULL:
                    break;
            }
        }





        /*
        while(!isStopRequested() && opModeIsActive()){
            if(depositor.getDetectionDistanceInches() > 1.5){
                intake.setIntakeState(Intake.IntakeState.IN);
                RRdrive.followTrajectory(toCollect);
            }
            else {
                RRdrive.breakFollowing();
                lastPose = RRdrive.getPoseEstimate();

            }
        }


        while(cycleState != CyclingState.NULL && !isStopRequested() && opModeIsActive()){
            depositor.freightCheck();

            switch (cycleState){
                case TRAVELLING:
                    RRdrive.followTrajectory(toGapExit);
                    RRdrive.followTrajectory(toOuterField);
                    RRdrive.followTrajectory(toScore);
                    cycleState = CyclingState.DELIVERING;
                    break;

                case DELIVERING:
                    extendToScore(Lift.AngleState.TOP);
                    homeLift();
                    RRdrive.followTrajectory(toGapEntrance);
                    RRdrive.followTrajectory(toWarehouse);
                    cycleState = CyclingState.COLLECTING;
                    break;

                case COLLECTING:
                    if(getRuntime() > 24){
                        RRdrive.breakFollowing();
                        sleep(500);
                        lastPose = RRdrive.getPoseEstimate();
                        cycleState = CyclingState.NULL;
                    }

                    else if (depositor.getStorageState() != Depositor.storageState.IN){
                        intake.setIntakeState(Intake.IntakeState.IN);
                        RRdrive.followTrajectory(toCollect);
                    }

                    else {
                        intake.setIntakeState(Intake.IntakeState.OUT);
                        RRdrive.breakFollowing();
                        sleep(500);
                        lastPose = RRdrive.getPoseEstimate();
                        if(getRuntime() > 24) {
                            cycleState = CyclingState.NULL;
                        }
                        else{
                            cycleState = CyclingState.TRAVELLING;
                        }
                    }
                    break;

            }
        }

        RRdrive.followTrajectory(toSafePark);


         */





        //Drive straight into the warehouse


        //Turn the intake on and continue to drive slowly into the warehouse until a freight is detected



        //Set the intake to the out state


        //Drive backwards to exit the warehouse


        //Turn towards the shipping hub



        //Extend the lift to score in the top level of the hub




        //Repeat above commands until a time check indicates that the robot needs to park.



    }
/*
    void buildTrajectories(){
        toFirstScore = newDrive.trajectoryBuilder(startPose)
                .lineToLinearHeading(SCORE_POSE)
                .build();



        toGapEntrance = RRdrive.trajectoryBuilder(toFirstScore.end().plus(new Pose2d(0, 0, Math.toRadians(TURN_TO_WALL)))) //toScore.end()
                .lineToConstantHeading(GAP_OUTER_POSE)
                .build();




        toGapEntrance = RRdrive.trajectoryBuilder(lastPose) //toScore.end()
                .lineToLinearHeading(GAP_OUTER_POSE)
                .build();


        toWarehouse = RRdrive.trajectoryBuilder(toGapEntrance.end())
                .lineToLinearHeading(GAP_INNER_POSE)
                .build();

        goForward = RRdrive.trajectoryBuilder(lastPose)
                .forward(40)
                .build();



        /*
        toCollect = RRdrive.trajectoryBuilder(toWarehouse.end())
                .forward(12)
                .build();

        toGapExit = RRdrive.trajectoryBuilder(lastPose)
                .splineToLinearHeading(GAP_INNER_POSE, lastHeading)
                .build();

        toOuterField = RRdrive.trajectoryBuilder(toGapExit.end())
                .lineToLinearHeading(GAP_OUTER_POSE)
                .addSpatialMarker(new Vector2d(20, 60), () -> {
                    intake.setIntakeState(Intake.IntakeState.OFF);
                })
                .build();

        toScore = RRdrive.trajectoryBuilder(toOuterField.end())
                .lineToLinearHeading(SCORE_POSE)
                .build();

        toSafePark = RRdrive.trajectoryBuilder(lastPose)
                .splineToLinearHeading(PARK_POSE, lastHeading)
                .build();



    }
    */



    public void homeLift(){
        depositor.setDepositorState(Depositor.depositorState.RESTING);
        depositor.setLockState(Depositor.lockState.UNLOCK);
        lift.setExtensionState(Lift.ExtensionState.HOMING);
        lift.setAnglerState(Lift.AngleState.BOTTOM);
    }

    public void extendToReady(){
        depositor.setLockState(Depositor.lockState.LOCK);
        lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);

        //Angle slides towards the top level of the shipping hub
        lift.setAnglerState(Lift.AngleState.TOP);
        depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);
    }

    public void extendToPosition(int counts) {
        double startTime = getRuntime();
        //lift.setLiftPower(-1.0); //Formally 0.3
        //lift.setExtensionState(Lift.ExtensionState.OUT_AUTO);
        lift.setExtensionState(Lift.ExtensionState.OUT);


        while (opModeIsActive()) {
            if (Math.abs(lift.getLiftPosition()) > counts) {
                break;
            }
            else if (getRuntime() - startTime > 4.0){
                break;
            }
        }

        //lift.setLiftPower(0.0);
        lift.setExtensionState(Lift.ExtensionState.IDLE);
    }

    public void extendToScore (Lift.AngleState angleState){
        //Extend to "ready to angle position"
        //extendToPosition(EXTEND_TO_ANGLE);
        depositor.setLockState(Depositor.lockState.LOCK);
        lift.setExtensionState(Lift.ExtensionState.EXTEND_TO_ANGLE);

        //sleep(1000);

        //Angle slides towards the top level of the shipping hub
        lift.setAnglerState(angleState);
        depositor.setDepositorState(Depositor.depositorState.TOP_ANGLE);

        //sleep(250); //Previously 1000

        //Extend slides to the correct level of the shipping hub
        switch (angleState){
            case TOP:
                extendToPosition(EXTEND_TO_TOP);
                break;
            case MID:
                extendToPosition(EXTEND_TO_MID);
                break;
            case BOTTOM:
                extendToPosition(EXTEND_TO_BOTTOM);
                break;
        }

        //sleep(1000);

        //Set the depositor to a scoring position
        depositor.setDepositorState(Depositor.depositorState.SCORING);

        //Wait x seconds
        sleep(TIME_TO_DEPOSIT);

    }

    public void setCyclingState (CyclingState newState) {
        cycleState = newState;
    }


}
