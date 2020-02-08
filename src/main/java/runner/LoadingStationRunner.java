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
        public TargetData(double height, double centerX) {
            this.height = height;
            this.centerX = centerX;
        }
    }
    
    private Gson gson = new Gson();

    public LoadingStationRunner(VideoSource videoSource, CvSource processedVideo) {
        super(videoSource, new LoadingStationPipeline(), processedVideo);
    }

    @Override
    protected void process(LoadingStationPipeline pipeline, Mat image) {
        // TODO Auto-generated method stub
        ArrayList<LoadingStationTarget> targets = new ArrayList<LoadingStationTarget>();
        TargetData data = new TargetData(0, 0);
        double areaMax = 0;
        MatOfPoint biggestMat = null;
        for (MatOfPoint mat : pipeline.findContoursOutput()) {
            Rect boundingRect = Imgproc.boundingRect(mat);
            if (boundingRect.area() > areaMax) {
                biggestMat = mat;
            }
        }

        if (biggestMat != null) {
            //double height = calculateHeight(mat);
            data = calculateTargetData(biggestMat);
            // Bad logical
            LoadingStationTarget target = new LoadingStationTarget(biggestMat);
            targets.add(target);
            ArrayList<MatOfPoint> polyList = new ArrayList<MatOfPoint>();
            polyList.add(target.toMatOfPoint());
            Imgproc.polylines(image, polyList, true, Color.RED, 2);
        }
        String[] targetsJson = new String[targets.size()];
        for (int i = 0; i < targets.size(); ++i) {
            targetsJson[i] = gson.toJson(targets.get(i));
        }
        SmartDashboard.putStringArray("Vision/LoadingStationTargets", targetsJson);
        SmartDashboard.putNumber("Vision/LoadingStationCount", targets.size());
        SmartDashboard.putNumber("Vision/LoadingStation/Height", data.height);
        SmartDashboard.putNumber("Vision/LoadingStation/CenterX", data.centerX);
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
        double height = (bottomLeft.y - upperLeft.y + bottomRight.y - upperRight.y) / 2.0;
        double centerX = (bottomRight.x - bottomLeft.x + upperRight.x - upperRight.y) / 2.0;
        return new TargetData(height, centerX);
    }

}