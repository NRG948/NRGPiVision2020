package runner;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import pipeline.LoadingStationPipeline;
import target.LoadingStationTarget;
import utilities.Color;

public class LoadingStationRunner extends TargetTrackingRunner<LoadingStationPipeline> {

    private static final String VISION_LOADING_STATION_HAS_TARGET_KEY = "Vision/LoadingStation/HasTarget";

    public LoadingStationRunner(VideoSource videoSource, CvSource processedVideo) {
        super(videoSource, new LoadingStationPipeline(), processedVideo);
    }

    @Override
    protected void process(LoadingStationPipeline pipeline, Mat image) {
        LoadingStationTarget target = null;
        double areaMax = 0;
        MatOfPoint biggestMat = null;
        for (MatOfPoint mat : pipeline.findContoursOutput()) {
            Rect boundingRect = Imgproc.boundingRect(mat);
            //double ratio = boundingRect.height / boundingRect.width;
            //if (ratio < 11.0/3.5 && ratio > 11.0/8.0) {
                if (boundingRect.area() > areaMax) {
                    biggestMat = mat;
                }
            //}
        }

        if (biggestMat != null) {
            target = new LoadingStationTarget(biggestMat);

            Point centerBottom = new Point((target.bottomRight.x + target.bottomLeft.x) / 2.0,
                    (target.bottomRight.y + target.bottomLeft.y) / 2.0);
            Point centerTop = new Point((target.upperRight.x + target.upperLeft.x) / 2.0,
                    (target.upperRight.y + target.upperLeft.y) / 2.0);

            // Outline the loading station target in blue
            Imgproc.line(image, target.upperLeft, target.upperRight, Color.BLUE, 2);
            Imgproc.line(image, target.upperRight, target.bottomRight, Color.BLUE, 2);
            Imgproc.line(image, target.bottomRight, target.bottomLeft, Color.BLUE, 2);
            Imgproc.line(image, target.bottomLeft, target.upperLeft, Color.BLUE, 2);

            // Draw the center line in red
            Imgproc.line(image, centerBottom, centerTop, Color.RED, 1);
        }

        if (target != null) {
            SmartDashboard.putBoolean(VISION_LOADING_STATION_HAS_TARGET_KEY, true);
            SmartDashboard.putNumber("Vision/LoadingStation/Height", target.height);
            SmartDashboard.putNumber("Vision/LoadingStation/DistanceInches", target.distance);
            SmartDashboard.putNumber("Vision/LoadingStation/Angle", target.angleX);
            SmartDashboard.putNumber("Vision/LoadingStation/Skew", target.skew);
            SmartDashboard.putNumber("Vision/LoadingStation/SkewDegrees", target.skewDegrees);
        } else {
            SmartDashboard.putBoolean(VISION_LOADING_STATION_HAS_TARGET_KEY, false);
        }
    }

    @Override
    public void start() {
        loadCameraConfig("LoadingStationRunner.json");
    }

    @Override
    public void stop() {
        SmartDashboard.putBoolean(VISION_LOADING_STATION_HAS_TARGET_KEY, false);
    }

}