package frc.robot.subsystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonUtils;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.common.hardware.VisionLEDMode;
import org.photonvision.targeting.PhotonTrackedTarget;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.Constants.PhotonVisionConstants;
import frc.robot.abstract_classes.CameraInterface;


/**
 * This class is used to interface with the PhotonCamera and PhotonPoseEstimator classes.
 * It is used to get the robot's pose on the field Using only AprilTags.
 */
public class PhotonCameraSystem {
    private PhotonCamera camera;
    public CameraInterface cameraDetails;
    private PhotonPoseEstimator photonPoseEstimator;

    /**
     * This is used to get the robot's pose on the field using only AprilTags.
     * @param cameraDetails The camera details that will be used to get the robot's pose.
     * And to get the camera's PID constants.
     */
    public PhotonCameraSystem(CameraInterface cameraDetails) {
        this.cameraDetails = cameraDetails;
        camera = new PhotonCamera(cameraDetails.getCameraName());
        photonPoseEstimator = getPhotonPoseEstimator();
    }

    /**
     * This is used to get the robot's pose on the field using only AprilTags.
     * Will use the {@link PhotonVisionConstants#New_PiCamera} as default camera details.
     */
    public PhotonCameraSystem() {
        this(new PhotonVisionConstants.New_PiCamera());
    }

    private PhotonPoseEstimator getPhotonPoseEstimator() {
        try {
            // Attempt to load the AprilTagFieldLayout that will tell us where the tags are on the field.
            AprilTagFieldLayout fieldLayout = AprilTagFields.k2023ChargedUp.loadAprilTagLayoutField();
            // Create pose estimator
            photonPoseEstimator =
                    new PhotonPoseEstimator(
                            fieldLayout, PoseStrategy.MULTI_TAG_PNP, camera, cameraDetails.getRobotToCam());
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

    /**
     * This is used for a Target that you know the height of.
     * @param targetHeightMeters The height of the target in meters.
     * @return the distance to the target in meters.
     */
    public Optional<Double> getDistanceToTarget(double targetHeightMeters) {
        var pitch = getPitch();
        if (pitch == 0) return Optional.empty();
        var cameraHeight = cameraDetails.getCameraHeightMeters();
        var cameraPitch = cameraDetails.getCameraPitchRadians();
        return Optional.of(PhotonUtils.calculateDistanceToTargetMeters(cameraHeight, targetHeightMeters, cameraPitch, pitch));
    }

    /**
     * Returns the pitch of the target acording to the camera.
     * @return the diffrence between the middle of the camera and the target In Terms of Pitch.
     * If no target is found, it will return 0 (I have no clue what the units are)
     */
    public double getPitch() {
        var latestResult = camera.getLatestResult();
        if (latestResult.hasTargets()) {
            return latestResult.getBestTarget().getPitch();
        }
        return 0;
    }

    /**
     * Returns the Yaw of the target acording to the camera.
     * @return the diffrence between the middle of the camera and the target In terms of Yaw.
     * If no target is found, it will return 0. (I have no clue what the units are)
    */
    public double getYaw() {
        var latestResult = camera.getLatestResult();
        if (latestResult.hasTargets()) {
            return latestResult.getBestTarget().getYaw();
        }
        return 0;
    }

    /**
     * Returns the Area of the target acording to the camera.
     * @return the area percantage (0 to 100) of the camera fov.
     */
    public double getArea() {
        var latestResult = camera.getLatestResult();
        if (latestResult.hasTargets()) {
            return latestResult.getBestTarget().getArea();
        }
        return 0;
    }

    /**
     * @return The current id of the best april tag being tracked. If no tag is being tracked, it will return -1.
     */
    public int getCurrentAprilTagID() {
        var latestResult = camera.getLatestResult();
        if (latestResult.hasTargets()) {
            return latestResult.getBestTarget().getFiducialId();
        }
        return -1;
    }

    /**
     * @return the current ids of each apriltag being tracked. If there are no aprilTags is being tracked, it will return an empty array.
    */
    public List<Integer> getTrackedTargetsIDs() {
        List<Integer> ids = new ArrayList<>();
        // If there are no targets, return an empty array.
        if (!camera.getLatestResult().hasTargets()) return ids;
        // Get the ids of each target.
        var targets = camera.getLatestResult().getTargets();
        for (var target : targets) {
            ids.add(target.getFiducialId());
        }
        return ids;
    }

    public List<PhotonTrackedTarget> getTrackedTargets() {
        return camera.getLatestResult().getTargets();
    }
    
    /**
     * Lets you Select the LED mode of the camera.
     * @param state The state of the LED that you want.
     */
    public void setLed(VisionLEDMode state) {
        camera.setLED(state);
    }

    /**
     * Returns the robot's pose on the field If found. If not found, it will return empty.
     * @param prevEstimatedRobotPose  .
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
}   
