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

import java.util.ArrayList;

import com.google.gson.Gson;

public class LoadingStationRunner extends TargetTrackingRunner<LoadingStationPipeline> {

    public LoadingStationRunner(VideoSource videoSource, CvSource processedVideo) {
        super(videoSource, new LoadingStationPipeline(), processedVideo);
    }

    @Override
    protected void process(LoadingStationPipeline pipeline, Mat image) {
        LoadingStationTarget data = null;
        double areaMax = 0;
        MatOfPoint biggestMat = null;
        for (MatOfPoint mat : pipeline.findContoursOutput()) {
            Rect boundingRect = Imgproc.boundingRect(mat);
            if (boundingRect.area() > areaMax) {
                biggestMat = mat;
            }
        }

        if (biggestMat != null) {
            data = new LoadingStationTarget(biggestMat);

            Point centerBottom = new Point((data.bottomRight.x + data.bottomLeft.x) / 2.0,
                    (data.bottomRight.y + data.bottomLeft.y) / 2.0);
            Point centerTop = new Point((data.upperRight.x + data.upperLeft.x) / 2.0,
                    (data.upperRight.y + data.upperLeft.y) / 2.0);

            Imgproc.line(image, data.upperLeft, data.upperRight, Color.GREEN, 2);
            Imgproc.line(image, data.upperRight, data.bottomRight, Color.GREEN, 2);
            Imgproc.line(image, data.bottomRight, data.bottomLeft, Color.GREEN, 2);
            Imgproc.line(image, data.bottomLeft, data.upperLeft, Color.GREEN, 2);
            Imgproc.line(image, centerBottom, centerTop, Color.RED, 1);
        }

        if (data != null) {
            SmartDashboard.putBoolean("Vision/LoadingStation/HasTarget", true);
            SmartDashboard.putNumber("Vision/LoadingStation/Height", data.height);
            SmartDashboard.putNumber("Vision/LoadingStation/DistanceInches", data.distance);
            SmartDashboard.putNumber("Vision/LoadingStation/CenterX", data.centerX);
            SmartDashboard.putNumber("Vision/LoadingStation/Skew", data.skew);
            SmartDashboard.putNumber("Vision/LoadingStation/SkewDegrees", data.skewDegrees);
        } else {
            SmartDashboard.putBoolean("Vision/LoadingStation/HasTarget", false);
        }
    }

}