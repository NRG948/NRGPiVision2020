package runner;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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
    private Gson gson = new Gson();

    public LoadingStationRunner(VideoSource videoSource, CvSource processedVideo) {
        super(videoSource, new LoadingStationPipeline(), processedVideo);
    }

    @Override
    protected void process(LoadingStationPipeline pipeline, Mat image) {
        // TODO Auto-generated method stub
        ArrayList<LoadingStationTarget> targets = new ArrayList<LoadingStationTarget>();
        for (MatOfPoint mat : pipeline.findContoursOutput()) {
            LoadingStationTarget target = new LoadingStationTarget(mat);
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
    }

}