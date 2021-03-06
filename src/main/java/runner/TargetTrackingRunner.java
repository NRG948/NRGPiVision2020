package runner;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoMode;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionRunner;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import utilities.Convert;
import utilities.ResourceUtils;

/**
 * Genaric class to manage running a GRIP pipeline
 * 
 * @param <Pipeline>
 */
public abstract class TargetTrackingRunner<Pipeline extends VisionPipeline> {
    private Pipeline pipeline;
    private VideoSource videoSource;
    private CvSource processedVideo;
    private Mat image;
    private Mat processedImage;
    private VisionRunner<Wrapper> runner;
    private long pipelineStartTime;
    private long postProcessStartTime;
    private int genCount = 0;

    /**
     * A wrapper that allows us to capture the current image to be processed by the
     * GRIP pipeline.
     */
    private class Wrapper implements VisionPipeline {
        @Override
        public void process(Mat arg0) {
            pipelineStartTime = System.nanoTime();
            image = arg0;
            pipeline.process(arg0);
        }
    }

    /**
     * Contructs an instance of this class.
     * 
     * @param videoSource    The input video source.
     * @param pipeline       The GRIP pipeline.
     * @param processedVideo The annotated output video.
     */
    public TargetTrackingRunner(VideoSource videoSource, Pipeline pipeline, CvSource processedVideo) {
        this.videoSource = videoSource;
        this.pipeline = pipeline;
        this.processedVideo = processedVideo;
        this.runner = new VisionRunner<Wrapper>(videoSource, new Wrapper(), this::unwrap);

        VideoMode videoMode = processedVideo.getVideoMode();
        this.processedImage = new Mat(videoMode.height, videoMode.width, videoMode.pixelFormat.getValue());
    }

    /**
     * Returns the input video source.
     * 
     * @return The input video source.
     */
    protected VideoSource getVideoSource() {
        return this.videoSource;
    }

    /***
     * returns the annotated output video.
     * 
     * @return The annotated output video.
     */
    protected CvSource getProcessedVideo() {
        return this.processedVideo;
    }

    /**
     * Runs one iteration of the GRIP pipeline.
     */
    public void runOnce() {
        // One iteration of the GRIP pipeline
        this.runner.runOnce();
        // Put the processed image to the output video stream
        if (image != null) {
            Imgproc.resize(this.image, this.processedImage, this.processedImage.size());
            this.processedVideo.putFrame(this.processedImage);
        }
        // Report latency statistics to smart dashboard
        long pipelineEndTime = System.nanoTime();
        SmartDashboard.putNumber("Vision/Latency/totalTime",
                Convert.nanosToMillis(pipelineEndTime - this.pipelineStartTime));
        SmartDashboard.putNumber("Vision/Latency/postProcessTime",
                Convert.nanosToMillis(pipelineEndTime - this.postProcessStartTime));
        SmartDashboard.putNumber("Vision/genCount", genCount++);
    }

    /**
     * This method is called by the VisionRunner<Wrapper> object when the GRIP
     * pipeline has produced outputs.
     * 
     * @param wrapper Unused.
     */
    private void unwrap(Wrapper wrapper) {
        postProcessStartTime = System.nanoTime();
        this.process(this.pipeline, this.image);
    }

    /**
     * Loads and sets the camera config for the specified file.
     */
    protected void loadCameraConfig(String jsonFile) {
        try {
            String configuration = ResourceUtils.loadJsonResource(this.getClass(), jsonFile);
            getVideoSource().setConfigJson(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is implemented by a subclass to process the GRIP pipeline outputs
     * for the current image.
     * 
     * @param pipeline The GRIP pipeline.
     * @param image    The processed image.
     */
    protected abstract void process(Pipeline pipeline, Mat image);

    /**
     * Starts the GRIP pipeline.
     */
    public abstract void start();

    /**
     * Stops the GRIP pipeline.
     */
    public abstract void stop();

}
