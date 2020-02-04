package runner;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.Gson;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.vision.VisionRunner;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import pipeline.BallFinderPipeLine;
import target.BallTarget;
import utilities.Color;

public class BallTrackingRunner {
	private VisionRunner <BallFinderPipeLine> runner;
	private CvSource processedVideo;
	private Gson gson = new Gson();


	public BallTrackingRunner(VideoSource videoSource, CvSource processedVideo) {
		this.runner = new VisionRunner<BallFinderPipeLine>(videoSource, new BallFinderPipeLine(), this::process);
		this.processedVideo = processedVideo;
	}

	public void runOnce() {
		this.runner.runOnce();
	}

	private void process (BallFinderPipeLine pipeline) {
		long startTime = System.nanoTime();

        //Convert Blobs detected from the GRIP pipeline to BallTarget objects
        Mat image = pipeline.getImage();
        ArrayList<BallTarget> ballTargets = new ArrayList<BallTarget>();
        MatOfKeyPoint keyPoints = pipeline.findBlobsOutput();
        for (KeyPoint keyPoint : keyPoints.toArray()) {
          ballTargets.add(new BallTarget(keyPoint.pt, keyPoint.size));
        }

        //Sort list of ball targets in order of closest to furthest
        Collections.sort(ballTargets, (left, right) -> (int)(left.distanceToTarget()-right.distanceToTarget()));

        //Annotate the image by outlineing the closest target in green and rest in red
        Scalar targetColor = Color.GREEN;
        for(BallTarget ballTarget: ballTargets){
          Imgproc.circle(image, ballTarget.getCenter(), (int) ballTarget.getDiameterInPixels() / 2, targetColor, 2);
          targetColor = Color.RED;
        }

        //Put the annotated frame out to the processed video stream
        processedVideo.putFrame(image);

        //Convert BallTarget objects to Json
        String[] ballTargetsJson = new String[ballTargets.size()];
        for (int i = 0; i < ballTargets.size(); ++i) {
          ballTargetsJson[i] = gson.toJson(ballTargets.get(i));
        }

        long endTime = System.nanoTime();

        //Send Target data to smartdashboard 
        SmartDashboard.putNumber("Vision/processTime", pipeline.getProcessTime() / 1000000.0);
        SmartDashboard.putNumber("Vision/postProcessTime", (endTime - startTime) / 1000000.0);
        SmartDashboard.putStringArray("Vision/ballTargets", ballTargetsJson);
        // do something with pipeline results
	} 



}