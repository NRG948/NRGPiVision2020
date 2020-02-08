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
    public class TargetData {
        public double height;
        public double centerX;
        public Point upperLeft;
        public Point upperRight;
        public Point bottomLeft;
        public Point bottomRight;
        public double distance;
        public double skew;
        public double skewDegrees;
        public TargetData(double height, double centerX, Point upperLeft, Point upperRight, Point bottomLeft, Point bottomRight, 
                double distance, double skew, double skewDegrees) {
            this.height = height;
            this.centerX = centerX;
            this.upperLeft = upperLeft;
            this.upperRight = upperRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
            this.distance = distance;
            this.skew = skew;
            this.skewDegrees= skewDegrees;
        }
        public TargetData() {}
    }

    private final double TARGET_HEIGHT_INCHES = 11.0;
    private final double IMAGE_CENTER_Y = 480.0 / 2;
    private final double HALF_IMAGE_FOV_Y = Math.atan(17.4375 / 37);
    
    private Gson gson = new Gson();

    public LoadingStationRunner(VideoSource videoSource, CvSource processedVideo) {
        super(videoSource, new LoadingStationPipeline(), processedVideo);
    }

    @Override
    protected void process(LoadingStationPipeline pipeline, Mat image) {
        // TODO Auto-generated method stub
        ArrayList<LoadingStationTarget> targets = new ArrayList<LoadingStationTarget>();
        TargetData data = new TargetData();
        double areaMax = 0;
        MatOfPoint biggestMat = null;
        for (MatOfPoint mat : pipeline.findContoursOutput()) {
            Rect boundingRect = Imgproc.boundingRect(mat);
            if (boundingRect.area() > areaMax) {
                biggestMat = mat;
            }
        }

        if (biggestMat != null) {
            data = calculateTargetData(biggestMat);

            Point centerBottom = new Point((data.bottomRight.x + data.bottomLeft.x) / 2.0, (data.bottomRight.y + data.bottomLeft.y) / 2.0);
            Point centerTop = new Point((data.upperRight.x + data.upperLeft.x) / 2.0, (data.upperRight.y + data.upperLeft.y) / 2.0);

            Imgproc.line(image, data.upperLeft, data.upperRight, Color.GREEN, 2);
            Imgproc.line(image, data.upperRight, data.bottomRight, Color.GREEN, 2);
            Imgproc.line(image, data.bottomRight, data.bottomLeft, Color.GREEN, 2);
            Imgproc.line(image, data.bottomLeft, data.upperLeft, Color.GREEN, 2);
            Imgproc.line(image, centerBottom, centerTop, Color.RED, 1);
        }
        String[] targetsJson = new String[targets.size()];
        for (int i = 0; i < targets.size(); ++i) {
            targetsJson[i] = gson.toJson(targets.get(i));
        }
        SmartDashboard.putStringArray("Vision/LoadingStationTargets", targetsJson);
        SmartDashboard.putNumber("Vision/LoadingStationCount", targets.size());
        SmartDashboard.putNumber("Vision/LoadingStation/Height", data.height);
        SmartDashboard.putNumber("Vision/LoadingStation/DistanceInches", data.distance);
        SmartDashboard.putNumber("Vision/LoadingStation/CenterX", data.centerX);
        SmartDashboard.putNumber("Vision/LoadingStation/Skew", data.skew);
        SmartDashboard.putNumber("Vision/LoadingStation/SkewDegrees", data.skewDegrees);
    }

    public TargetData calculateTargetData(MatOfPoint mat) {
        Point upperLeft = new Point(0, 0);
        Point upperRight = new Point(0, 0);
        Point bottomLeft = new Point(0, 0);
        Point bottomRight = new Point(0, 0);
        double upperLeftMax = -Integer.MAX_VALUE;
        double upperRightMax = -Integer.MAX_VALUE;
        double bottomLeftMax = -Integer.MAX_VALUE;
        double bottomRightMax = -Integer.MAX_VALUE;

        for (Point point : mat.toList()) {
            if (-point.x - point.y > upperLeftMax) {
                upperLeftMax = -point.x - point.y;
                upperLeft.x = point.x;
                upperLeft.y = point.y;
            }
            
            if (point.x - point.y > upperRightMax) {
                upperRightMax = point.x - point.y;
                upperRight.x = point.x;
                upperRight.y = point.y;
            }

            if (-point.x + point.y > bottomLeftMax) {
                bottomLeftMax = -point.x + point.y;
                bottomLeft.x = point.x;
                bottomLeft.y = point.y;
            }

            if (point.x + point.y > bottomRightMax) {
                bottomRightMax = point.x + point.y;
                bottomRight.x = point.x;
                bottomRight.y = point.y;
            }
        }
        double heightLeft = Math.sqrt(Math.pow(bottomLeft.y - upperLeft.y, 2.0) + Math.pow(bottomLeft.x - upperLeft.x, 2.0));
        double heightRight = Math.sqrt(Math.pow(bottomRight.y - upperRight.y, 2.0) + Math.pow(bottomRight.x - upperRight.x, 2.0));
        double height = (heightLeft + heightRight) / 2.0;
        double widthBottom = Math.sqrt(Math.pow(bottomRight.x - bottomLeft.x, 2.0) + Math.pow(bottomRight.y - bottomLeft.y, 2.0));
        double widthTop = Math.sqrt(Math.pow(upperRight.x - upperLeft.x, 2.0) + Math.pow(upperRight.y - upperLeft.y, 2.0));
        double width = (widthBottom + widthTop) / 2.0;
        double centerX = (bottomRight.x - bottomLeft.x + upperRight.x - upperRight.y) / 2.0;
        int leftOrRight = heightRight > heightLeft ? 1 : -1;
        // Skew is from -1.0 to 1.0, with negative values representing the robot being to the left of the target, and positive to the right
        double skew = Math.abs(1 - (11.0 / 7.0) * (width / height)) * leftOrRight; 
        double skewDegrees = Math.acos(Math.min(1.0, (11.0 / 7.0) * (width / height))) * (180 / Math.PI) * leftOrRight;
        double distance = (TARGET_HEIGHT_INCHES * IMAGE_CENTER_Y / (height * Math.tan(HALF_IMAGE_FOV_Y)));
        return new TargetData(height, centerX, upperLeft, upperRight, bottomLeft, bottomRight, distance, skew, skewDegrees);
    }
}