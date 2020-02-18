package target;

import static utilities.Constants.HALF_IMAGE_FOV_X;
import static utilities.Constants.IMAGE_CENTER_X;

import org.opencv.core.Point;

/**
 * A class representing a fuel cell target.
 */
public class FuelCellTarget {
    private static final double TARGET_WIDTH_INCHES = 7.1;

    private Point center;
    private double diameterInPixels;

    /**
     * Constructs an instance of this class.
     * 
     * @param center           The center point of the target in the image.
     * @param diameterInPixels The diameter of the target, in pixels.
     */
    public FuelCellTarget(Point center, double diameterInPixels) {
        this.center = center;
        this.diameterInPixels = diameterInPixels;
    }

    /**
     * Returns the center of the target.
     * 
     * @return The center of the target in the image.
     */
    public Point getCenter() {
        return center;
    }

    /**
     * Returns the diameter of the target.
     * 
     * @return The diameter of the target, in pixels.
     */
    public double getDiameterInPixels() {
        return diameterInPixels;
    }

    /**
     * Returns the distance to the target.
     * 
     * @return The distance to the target, in inches.
     */
    public double getDistanceToTarget() {
        double distance = (TARGET_WIDTH_INCHES * IMAGE_CENTER_X / (diameterInPixels * Math.tan(HALF_IMAGE_FOV_X)));
        return distance;
    }

    /**
     * Returns the angle to the target.
     * 
     * @return The angle to the target, in degrees.
     */
    public double getAngleToTarget() {
        double deltaX = center.x - IMAGE_CENTER_X;
        return -Math.toDegrees(Math.atan2(deltaX, IMAGE_CENTER_X / Math.tan(HALF_IMAGE_FOV_X)));
    }
}