package frc.robot.subsystems;

import java.io.IOException;
import java.util.Optional;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonUtils;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.common.hardware.VisionLEDMode;

import frc.robot.Constants.PhotonVisionConstants;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class PhotonVisionSubsystem extends SubsystemBase{
    private PhotonCamera camera;
    private PhotonPoseEstimator photonPoseEstimator;
    private boolean temp_target_detected;
    private Field2d field;

    public PhotonVisionSubsystem(Field2d field2d) {
        this.field = field2d;
        SmartDashboard.putData("Field", field);
        if (RobotBase.isSimulation()) {
            return;
        }
        camera = new PhotonCamera(PhotonVisionConstants.kCameraName);
        SmartDashboard.putBoolean("Target Detected", camera.getLatestResult().hasTargets());
        temp_target_detected = camera.getLatestResult().hasTargets();
        System.out.println("Loaded PhotonCamera, Added Field to SmartDashboard");
        photonPoseEstimator = getPhotonPoseEstimator();
    }

    
    @Override
    public void simulationPeriodic() {
        
    }

    @Override
    public void periodic() {
        if (temp_target_detected != hasTargets()) {
            System.out.println("New Target Change");
            System.out.print(hasTargets());
            SmartDashboard.putBoolean("Target Detected", hasTargets());
            temp_target_detected = hasTargets();
        }
        if (hasTargets()) {
            // SmartDashboard.putNumber("Target X", camera.getLatestResult().getBestTarget().getYaw());
            // SmartDashboard.putNumber("Target Y", camera.getLatestResult().getBestTarget().getPitch());
            // SmartDashboard.putNumber("Target Area", camera.getLatestResult().getBestTarget().getArea());
            // SmartDashboard.putNumber("Target Skew", camera.getLatestResult().getBestTarget().getSkew());
            try {
                field.setRobotPose(getEstimatedGlobalPose(field.getRobotPose()).get().estimatedPose.toPose2d());
            }
            catch (Exception e) {
                DriverStation.reportError(e.toString(), e.getStackTrace());
            }
        }
    }
    
    /**
     * Sets the LED mode of the PhotonVision camera.
     * @param state The LED mode to set.
     */
    public void setLED(VisionLEDMode state) {
        camera.setLED(state);
    }

    /**
     * Sets the pipeline of the PhotonVision camera.
     * @param pipeline The pipeline to set.
     */
    public void setPipeline(int pipeline) {
        camera.setPipelineIndex(pipeline);
    }

    /**
     * Gets the X position of the target.
     * @return The X position of the target.
     */
    public double getYaw() {
        if (!hasTargets()) {
            DriverStation.reportError("No Target Found", false);
            return 0;
        }
        return camera.getLatestResult().getBestTarget().getYaw();
    }

    /**
     * Gets the Area of the target, acording to the area of the camera's FOV.
     * @return The Area of the target.
     */
    public double getArea() {
        if (!hasTargets()) {
            DriverStation.reportError("No Target Found", false);
            return 0;
        }
        return camera.getLatestResult().getBestTarget().getArea();
    }
    
    public double getPitch() {
        if (!hasTargets()) {
            DriverStation.reportError("No Target Found", false);
            return 0;
        }
        return camera.getLatestResult().getBestTarget().getPitch();
    }
    
    public double getSkew() {
        if (!hasTargets()) {
            DriverStation.reportError("No Target Found", false);
            return 0;
        }
        return camera.getLatestResult().getBestTarget().getSkew();
    }
    
    public PhotonCamera getCamera() {
        return camera;
    }
    
    public Boolean hasTargets() {
        if (RobotBase.isSimulation()) {
            return false;
        }
        return camera.getLatestResult().hasTargets();
    }

    /**
     * Finds the {@link Pose2d} using aprilTags.
     * @param prevEstimatedRobotPose The previous estimated global pose of the robot in {@link Pose2d}.
     * @return The new {@link EstimatedRobotPose} To get {@link Pose2d} use {@code EstimatedRobotPose.get().estimatedPose.toPose2d()}.
     */
    public Optional<EstimatedRobotPose> getEstimatedGlobalPose(Pose2d prevEstimatedRobotPose) {
        if (photonPoseEstimator == null) {
            // The field layout failed to load, so we cannot estimate poses.
            return Optional.empty();
        }
        photonPoseEstimator.setReferencePose(prevEstimatedRobotPose);
        return photonPoseEstimator.update();
    }
    
    public double getDistance() {
        return PhotonUtils.calculateDistanceToTargetMeters(PhotonVisionConstants.CAMERA_HEIGHT_METERS, PhotonVisionConstants.TARGET_HEIGHT_METERS, 0, getPitch());
    }


    private PhotonPoseEstimator getPhotonPoseEstimator() {
        try {
            // Attempt to load the AprilTagFieldLayout that will tell us where the tags are on the field.
            AprilTagFieldLayout fieldLayout = AprilTagFields.k2023ChargedUp.loadAprilTagLayoutField();
            // Create pose estimator
            photonPoseEstimator =
                    new PhotonPoseEstimator(
                            fieldLayout, PoseStrategy.MULTI_TAG_PNP, camera, PhotonVisionConstants.robotToCam);
            photonPoseEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
            System.out.println("Loaded PhotonPoseEstimator");
        } catch (IOException e) {
            // The AprilTagFieldLayout failed to load. We won't be able to estimate poses if we don't know
            // where the tags are.
            DriverStation.reportError("Failed to load AprilTagFieldLayout", e.getStackTrace());
            photonPoseEstimator = null;
        }
        return photonPoseEstimator;
    }
        
}
