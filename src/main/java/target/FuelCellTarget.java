package target;

import org.opencv.core.Point;

public class FuelCellTarget {
    private static final double TARGET_WIDTH_INCHES = 7.1;
    private static final double HALF_IMAGE_FOV = (Math.atan(36.0 / 57.125));
    private Point center;
    private double diameterInPixels;
    private static final double IMAGE_CENTER_X = 640/2;
    public FuelCellTarget(Point center, double diameterInPixels) {
        this.center = center;
        this.diameterInPixels = diameterInPixels;
    }
    public Point getCenter(){ 
        return center;
    }
    public double getDiameterInPixels(){
        return diameterInPixels;
    }
    public double getDistanceToTarget(){
        double distance = (TARGET_WIDTH_INCHES * IMAGE_CENTER_X / (diameterInPixels * Math.tan(HALF_IMAGE_FOV)));
        return distance;
    }
    public double getAngleToTarget() {
        double deltaX = center.x - IMAGE_CENTER_X;
        return -Math.toDegrees(Math.atan2(deltaX, IMAGE_CENTER_X / Math.tan(HALF_IMAGE_FOV)));
    }
}