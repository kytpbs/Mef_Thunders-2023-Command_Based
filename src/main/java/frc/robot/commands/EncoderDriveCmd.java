package frc.robot.commands;

import frc.robot.Constants.AutonomousConstants.EncoderPIDConstants;
import frc.robot.Constants.AutonomousConstants.headingPIDConstants;
import frc.robot.subsystems.DriveSubsystem;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class EncoderDriveCmd extends CommandBase {
  private final DriveSubsystem driveSubsystem;
	private final PIDController headingPidController;
	private final PIDController encoderPIDController;
  private final double distance;
	private boolean finished;

  /**
   * Creates a new EncoderDriveCmd.
   *
   * @param driveSubsystem The subsystem used by this command.
   */
  public EncoderDriveCmd(DriveSubsystem driveSubsystem, double distance) {
    this.driveSubsystem = driveSubsystem;
    this.distance = distance;
		headingPidController = new PIDController(
            headingPIDConstants.kP,
            headingPIDConstants.kI, 
            headingPIDConstants.kD);
		encoderPIDController = new PIDController(
						EncoderPIDConstants.kP,
						EncoderPIDConstants.kI, 
						EncoderPIDConstants.kD);
    encoderPIDController.setTolerance(EncoderPIDConstants.kToleranceMeters);
    encoderPIDController.setIntegratorRange(0, 0.4);
    addRequirements(this.driveSubsystem);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {    
    System.out.println("Encoder Drive Started!");
    driveSubsystem.resetEncoders();
    double firstHeading = driveSubsystem.getAngle();
    headingPidController.setSetpoint(firstHeading);
		encoderPIDController.setSetpoint(distance);
		finished = false;
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
		if (finished) {
			return;
		}
		double fixheadingspeed = headingPidController.calculate(driveSubsystem.getAngle());
		double fixdistancespeed = encoderPIDController.calculate(driveSubsystem.getAvarageEncoderDistance());

		driveSubsystem.drive(-fixdistancespeed, fixheadingspeed, false);
    finished = encoderPIDController.atSetpoint();
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    driveSubsystem.stopMotors();
    System.out.println("Encoder Command Finished!");
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return finished;
  }
}

