package runner;

import org.opencv.core.Mat;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionRunner;

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
    private VisionRunner<Wrapper> runner;

    /**
     * A wrapper that allows us to capture the current image to be processed by the
     * GRIP pipeline.
     */
    private class Wrapper implements VisionPipeline {
        @Override
        public void process(Mat arg0) {
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

    }

    /**
     * Returns the input video source.
     * 
     * @return
     */
    protected VideoSource getVideoSource() {
        return this.videoSource;
    }

    /***
     * returns the annotated output video.
     * 
     * @return
     */
    protected CvSource getProcessedVideo() {
        return this.processedVideo;
    }

    /**
     * Runs one iteration of the GRIP pipeline.
     */
    public void runOnce() {
        this.runner.runOnce();

    }

    /**
     * This method is called by the VisionRunner<Wrapper> object when the GRIP
     * pipeline has produced outputs.
     * 
     * @param wrapper Unused.
     */
    private void unwrap(Wrapper wrapper) {
        this.process(this.pipeline, this.image);
    }

    /**
     * This method is implemented by a subclass to process the GRIP pipeline outputs
     * for the current image.
     * 
     * @param pipeline The GRIP pipeline.
     * @param image The processed image.
     */
    protected abstract void process(Pipeline pipeline, Mat image);
}
