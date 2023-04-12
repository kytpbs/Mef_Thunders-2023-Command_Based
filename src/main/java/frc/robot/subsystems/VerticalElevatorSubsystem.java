package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.HorizontalElevatorConstants;

public class VerticalElevatorSubsystem extends SubsystemBase{

    public static final Spark Elevator = new Spark(HorizontalElevatorConstants.kHorizontalElevatorPort);
    private final DigitalInput toplimitSwitch = new DigitalInput(0);
    private final DigitalInput bottomlimitSwitch = new DigitalInput(1);


    public VerticalElevatorSubsystem() {
        
    }
    public void setMotor(double speed) {
        Elevator.set(speed);
    }
    public boolean getTopLimitSwitch() {
        return !toplimitSwitch.get();
    }
    public boolean getBottomLimitSwitch() {
        return !bottomlimitSwitch.get();
    }
    public void MoveUp() {
        if (!toplimitSwitch.get()) {
            Elevator.set(0);
        } else {
            Elevator.set(0.8);
        }
    }
    public void MoveDown() {
        if (!bottomlimitSwitch.get()) {
            Elevator.set(0);
        } else {
            Elevator.set(-0.8);
        }
    }
}
