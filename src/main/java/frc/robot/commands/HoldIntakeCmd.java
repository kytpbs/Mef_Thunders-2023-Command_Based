package frc.robot.commands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.Constants.IntakeConstants;


public class HoldIntakeCmd extends CommandBase{
    private final IntakeSubsystem Intakesubsystem;
    private final PIDController pos_pid = new PIDController(IntakeConstants.IntakePIDConstants.kP, IntakeConstants.IntakePIDConstants.kI, IntakeConstants.IntakePIDConstants.kD);

    public HoldIntakeCmd(IntakeSubsystem Intakesubsystem) {
        this.Intakesubsystem = Intakesubsystem;
        addRequirements(Intakesubsystem);
    }
    @Override
    public void initialize() {
        pos_pid.reset();
        pos_pid.setSetpoint(Intakesubsystem.getNeoPos());
    }
    @Override
    public void execute() {
        double speed = pos_pid.calculate(Intakesubsystem.getNeoPos());
        Intakesubsystem.setNeo(speed);
    }
    @Override
    public void end(boolean interrupted) {
        Intakesubsystem.setNeo(0);
    }
    @Override
    public boolean isFinished() {
        return false;
    }    
}
