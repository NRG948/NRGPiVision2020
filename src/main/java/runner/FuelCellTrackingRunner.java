package runner;

import java.util.ArrayList;
import java.util.Collections;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import pipeline.FuelCellTrackingPipeLine;
import target.FuelCellTarget;
import utilities.Color;

/**
 * Runs the GRIP pipeline for tracking fuel cells.
 */
public class FuelCellTrackingRunner extends TargetTrackingRunner<FuelCellTrackingPipeLine> {

  private static final String VISION_FUEL_CELL_HAS_TARGET_KEY = "Vision/fuelCell/hasTarget";

  /**
   * Contructs an instance of this class.
   * 
   * @param videoSource    The input video source.
   * @param pipeline       The GRIP pipeline.
   * @param processedVideo The annotated output video.
   */
  public FuelCellTrackingRunner(VideoSource videoSource, CvSource processedVideo) {
    super(videoSource, new FuelCellTrackingPipeLine(), processedVideo);
  }

  /**
   * Processes the outputs of the GRIP pipeline for tracking fuel cells.
   */
  protected void process(FuelCellTrackingPipeLine pipeline, Mat image) {

    // Convert Blobs detected from the GRIP pipeline to BallTarget objects
    ArrayList<FuelCellTarget> ballTargets = new ArrayList<FuelCellTarget>();
    MatOfKeyPoint keyPoints = pipeline.findBlobsOutput();
    for (KeyPoint keyPoint : keyPoints.toArray()) {
      ballTargets.add(new FuelCellTarget(keyPoint.pt, keyPoint.size));
    }

    // Sort list of ball targets in order of closest to furthest
    Collections.sort(ballTargets, (left, right) -> (int) (left.getDistanceToTarget() - right.getDistanceToTarget()));

    // Annotate the image by outlining the closest target in one color and rest in another color
    Scalar targetColor = Color.BLUE;
    for (FuelCellTarget ballTarget : ballTargets) {
      Imgproc.circle(image, ballTarget.getCenter(), (int) ballTarget.getDiameterInPixels() / 2, targetColor, 2);
      targetColor = Color.RED;
    }

    // Send Target data to smartdashboard
    if (!ballTargets.isEmpty()) {
      SmartDashboard.putBoolean(VISION_FUEL_CELL_HAS_TARGET_KEY, true);
      FuelCellTarget fuelCellTarget = ballTargets.get(0);
      SmartDashboard.putNumber("Vision/fuelCell/distance", fuelCellTarget.getDistanceToTarget());
      SmartDashboard.putNumber("Vision/fuelCell/angle", fuelCellTarget.getAngleToTarget());
    } else {
      SmartDashboard.putBoolean(VISION_FUEL_CELL_HAS_TARGET_KEY, false);
    }
  }

  @Override
  public void start() {
    loadCameraConfig("FuelCellTrackingRunner.json");
  }

  @Override
  public void stop() {
    SmartDashboard.putBoolean(VISION_FUEL_CELL_HAS_TARGET_KEY, false);
  }
}