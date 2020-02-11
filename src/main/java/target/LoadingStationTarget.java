package target;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class LoadingStationTarget {
    private final double TARGET_HEIGHT_INCHES = 11.0;
    private final double IMAGE_CENTER_Y = 480.0 / 2;
    private final double HALF_IMAGE_FOV_Y = Math.atan(17.4375 / 37);
    
    public double height;
    public double centerX;
    public Point upperLeft;
    public Point upperRight;
    public Point bottomLeft;
    public Point bottomRight;
    public double distance;
    public double skew;
    public double skewDegrees;

    public LoadingStationTarget(double height, double centerX, Point upperLeft, Point upperRight, Point bottomLeft,
            Point bottomRight, double distance, double skew, double skewDegrees) {
        this.height = height;
        this.centerX = centerX;
        this.upperLeft = upperLeft;
        this.upperRight = upperRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.distance = distance;
        this.skew = skew;
        this.skewDegrees = skewDegrees;
    }

    public LoadingStationTarget(MatOfPoint mat) {
        upperLeft = new Point(0, 0);
        upperRight = new Point(0, 0);
        bottomLeft = new Point(0, 0);
        bottomRight = new Point(0, 0);
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
        height = (heightLeft + heightRight) / 2.0;

        double widthBottom = Math.sqrt(Math.pow(bottomRight.x - bottomLeft.x, 2.0) + Math.pow(bottomRight.y - bottomLeft.y, 2.0));
        double widthTop = Math.sqrt(Math.pow(upperRight.x - upperLeft.x, 2.0) + Math.pow(upperRight.y - upperLeft.y, 2.0));
        double width = (widthBottom + widthTop) / 2.0;

        centerX = (bottomRight.x - bottomLeft.x + upperRight.x - upperRight.y) / 2.0;
        
        int leftOrRight = heightRight > heightLeft ? 1 : -1;
        // Skew is from -1.0 to 1.0, with negative values representing the robot being to the left of the target, and positive to the right
        skew = Math.abs(1 - (11.0 / 7.0) * (width / height)) * leftOrRight; 
        skewDegrees = Math.acos(Math.min(1.0, (11.0 / 7.0) * (width / height))) * (180 / Math.PI) * leftOrRight;
        distance = (TARGET_HEIGHT_INCHES * IMAGE_CENTER_Y / (height * Math.tan(HALF_IMAGE_FOV_Y)));
    }
}