package frc.robot.abstract_classes;

import edu.wpi.first.math.geometry.Transform3d;

public interface CameraInterface {
    String getCameraName();
    double getCameraHeightMeters();
    double getCameraPitchRadians();
    Transform3d getRobotToCam();
    PIDConstants getTurnPIDConstants();
    PIDConstants getFowardPIDConstants();
}
