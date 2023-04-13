// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static class OperatorConstants {
    public static final int kJoystickPort = 0;
  }
  public static class DriveConstants {
    public static final int kLeftMotor1Port = 1;
    public static final int kLeftMotor2Port = 2;
    public static final int kRightMotor1Port = 3;
    public static final int kRightMotor2Port = 4;
  }
  public static class VerticalElevatorConstants {
    public static final int kVerticalElevatorPort = 2;
    public static final double kSpeed = 0.6;
  }
  public static class HorizontalElevatorConstants {
    public static final int kHorizontalElevatorPort = 0;
    public static final double kSpeed = 0.8;
  }
  public static class IntakeConstants {
    public static final int kRedlinePort = 1;
    public static final int kCanNeoPort = 5;
    public static final double kUpSpeed = 0.6;
    public static final double kDownSpeed = -0.4;
    public static final double kRedlineSpeed = 1;
    public static class IntakePIDConstants {
      public static final double kP = 0.4;
      public static final double kI = 0.0;
      public static final double kD = 0.0;
    }
  }
  public static class IoConstants {
    public static final int kJoystickPort = 0;
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;
  }
  public static class AutonomousConstants {
    public static final double kDriveSpeed = 0;
    public static final double kDriveTime = 0;
    public static final double kRedlineSpeed = 0;
    public static final double kRedlineTime = 0;

    public static final String kTimedAuto = "Timer Auto";
    public static final String kGyroAuto = "Gyro Auto";
    public static final String kCameraAuto = "Camera Auto";
    public static final String kStabilize = "Stabilize Auto";
  }

  public static class PhotonVisionConstants {
    public static final String kCameraName = "IMX219";
    public static final Transform3d robotToCam =
                new Transform3d(
                        new Translation3d(0.5, 0.0, 0),
                        new Rotation3d(
                                0, 0,
                                0));
  }
}
