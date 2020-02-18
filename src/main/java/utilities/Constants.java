package utilities;

public class Constants {
    /** The y-coordinate of the center of the source image. */
    public static final double IMAGE_CENTER_Y = 480.0 / 2;

    /** The x-coordinate of the center of the source image. */
    public static final double IMAGE_CENTER_X = 640.0 / 2;

    /** Half of the field of view along the y-axis of the source image. */
    public static final double HALF_IMAGE_FOV_Y = Math.atan(17.4375 / 37);

    /** Half of the field of view along the x-axis of the source image. */
    public static final double HALF_IMAGE_FOV_X = Math.atan(36.0 / 57.125);
}
