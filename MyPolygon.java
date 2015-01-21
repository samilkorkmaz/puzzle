package puzzle;

import java.awt.Polygon;
import java.util.Random;

/**
 * Custom polygon class.
 *
 * @author skorkmaz
 */
public class MyPolygon extends Polygon {

    private boolean isSnapped;

    public MyPolygon(int width, int height) {
        this(getXCoords(width), getYCoords(height), getXCoords(width).length);
    }

    public MyPolygon(int[] xCoords, int[] yCoords, int nCoords) {
        super(xCoords, yCoords, nCoords);
        isSnapped = false;
        //make sure that the smallest coordinate is zero:
        setSmallestCoordinateToZero(super.xpoints);
        setSmallestCoordinateToZero(super.ypoints);
    }

    private static int[] getXCoords(final int width) {
        Random random = new Random();
        int x0 = random.nextInt(200);
        int[] xCoords = {x0, x0 + width, x0 + width, x0};
        return xCoords;
    }

    private static int[] getYCoords(final int height) {
        int[] yCoords = {0, 0, height, height};
        return yCoords;
    }

    private void setSmallestCoordinateToZero(int[] array) {
        int minValue = minOfArray(array);
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] - minValue;
        }
    }

    private int minOfArray(final int[] array) {
        int minValue = Integer.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    public boolean isSnapped() {
        return isSnapped;
    }

    public void setIsSnapped(boolean isSnapped) {
        this.isSnapped = isSnapped;
    }

}
