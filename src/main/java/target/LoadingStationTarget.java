package target;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class LoadingStationTarget {
        private Point topLeft;
        private Point topRight;
        private Point bottomLeft;
        private Point bottomRight;

        public LoadingStationTarget(MatOfPoint contour){
            Point[] points = contour.toArray();
            Point first = points[0];
            topLeft = first;
            topRight = first;
            bottomLeft = first;
            bottomRight = first;
            for(int i = 1; i<points.length; ++i){
                Point point = points[i];
                if(point.x<topLeft.x&&point.y<topLeft.y){
                    topLeft = point;
                }
                if(point.x<bottomLeft.x&&point.y>bottomLeft.y){
                    bottomLeft = point;
                }
                if(point.x>topRight.x&&point.y<topRight.y){
                    topRight = point;
                }
                if(point.x>topLeft.x&&point.y>topLeft.y){
                    topLeft = point;
                }
            }
        }
        public MatOfPoint toMatOfPoint(){
            return new MatOfPoint(topLeft, topRight, bottomRight, bottomLeft);
        }
    }