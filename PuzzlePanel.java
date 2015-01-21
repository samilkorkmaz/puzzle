package puzzle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Simple puzzle that consists of moving pieces to their correct positions by dragging them with the mouse.
 *
 * NOTES:<br>
 * - You cannot change polygon location with polygon.getBounds().x = 10. The only way is to use translate and for that, you have to calculate dx, dy.
 *
 * Reference (for detecting image under mouse click coordinates and dragging images): http://stackoverflow.com/a/8899569/51358
 *
 * @author Samil Korkmaz
 * @date January 2015
 * @copyright Public Domain
 */
public class PuzzlePanel extends JPanel {

    private static final String IMAGE_DIRECTORY = "./src/puzzle/images/";
    private static final int SNAP_DISTANCE_PIXELS = 10;
    private static final Color POLYGON_COLOR = Color.LIGHT_GRAY;
    private static final Color STRING_COLOR = Color.BLUE;
    private static final int PREF_WIDTH = 400;
    private static final int PREF_HEIGHT = 300;
    private static final Stroke POLYGON_STROKE = new BasicStroke(1f);
    private int prevMouseX;
    private int prevMouseY;
    private final List<MyPolygon> boundingPolygonList;
    private final List<Polygon> snapPolygonList = new ArrayList<>();
    private Image imageFullEmpty;
    private final List<Image> imageList = new ArrayList<>();
    private final List<MyPoint> imageCenterPointList = new ArrayList<>();

    public static class MyPoint extends Point {

        public MyPoint(int x, int y) {
            super(x, y);
        }

        /**
         * The equals() implementation in Point class checks x and y values. We need object reference check so that ArrayList.remove() will really remove the object, not
         * the first object with the same x, y values. Note that this is not a problem with Polygon class because Polygon inherits equals() from Object which does
         * reference comparison.
         */
        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }
    }

    private void loadImage(String fileName, final int xTranslation, final int yTranslation) throws IOException {
        BufferedImage image = ImageIO.read(new File(fileName));
        imageList.add(image);
        int w1 = image.getWidth(null);
        int h1 = image.getHeight(null);
        MyPolygon polygon = new MyPolygon(w1, h1);
        boundingPolygonList.add(polygon);
        MyPolygon snapPolygon = new MyPolygon(w1, h1);
        snapPolygon.translate(xTranslation, yTranslation);
        snapPolygonList.add(snapPolygon);
        imageCenterPointList.add(new MyPoint(polygon.getBounds().x, polygon.getBounds().y));
    }

    public PuzzlePanel() {
        this.boundingPolygonList = new ArrayList<>();
        MyMouseAdapter myMouseAdapter = new MyMouseAdapter();
        addMouseListener(myMouseAdapter);
        addMouseMotionListener(myMouseAdapter);
        //createPolygonsFromDataInFiles();
        try {
            imageFullEmpty = ImageIO.read(new File(IMAGE_DIRECTORY + "full_empty.png"));
            loadImage(IMAGE_DIRECTORY + "center.png", 229, 157);
            loadImage(IMAGE_DIRECTORY + "petal1.png", 203, 100);
            loadImage(IMAGE_DIRECTORY + "petal2.png", 269, 102);
            loadImage(IMAGE_DIRECTORY + "petal3.png", 293, 169);
            loadImage(IMAGE_DIRECTORY + "petal4.png", 245, 196);
            loadImage(IMAGE_DIRECTORY + "petal5.png", 201, 172);
        } catch (IOException ex) {
            Logger.getLogger(PuzzlePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int calcAvg(final int[] valueArray) {
        int sum = 0;
        for (int i = 0; i < valueArray.length; i++) {
            sum = sum + valueArray[i];
        }
        int avg = sum / valueArray.length;
        return avg;
    }

    private Point calcCenterOfPolygon(Polygon polygon) {
        int xAvg = calcAvg(polygon.xpoints);
        int yAvg = calcAvg(polygon.ypoints);
        return new Point(xAvg, yAvg);
    }

    private void drawSnapPolygon(Graphics2D g2, final int iPolygon) {
        Polygon sp = snapPolygonList.get(iPolygon);
        g2.setColor(POLYGON_COLOR);
        g2.draw(sp);
    }

    private int calcDistToSnapPolygon(final Polygon polygon, final int iPolygon) {
        Point centerPolygon = calcCenterOfPolygon(polygon);
        Point centerStationaryPolygon = calcCenterOfPolygon(snapPolygonList.get(iPolygon));
        return (int) Math.round(Math.hypot(centerStationaryPolygon.x - centerPolygon.x, centerStationaryPolygon.y - centerPolygon.y));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imageFullEmpty, 200, 100, null);
        Graphics2D g2 = (Graphics2D) g;
        //draw smooth (antialiased) edges:
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(POLYGON_STROKE);
        for (int iPolygon = 0; iPolygon < boundingPolygonList.size(); iPolygon++) {
            MyPolygon myPolygon = boundingPolygonList.get(iPolygon);
            int distToStationaryPolygon = calcDistToSnapPolygon(myPolygon, iPolygon);
            if (!myPolygon.isSnapped() && distToStationaryPolygon > SNAP_DISTANCE_PIXELS) {
                g2.setColor(POLYGON_COLOR);
            } else { //snapped
                int deltaX = snapPolygonList.get(iPolygon).getBounds().x - myPolygon.getBounds().x;
                int deltaY = snapPolygonList.get(iPolygon).getBounds().y - myPolygon.getBounds().y;
                myPolygon.translate(deltaX, deltaY);
                imageCenterPointList.get(iPolygon).x = myPolygon.getBounds().x;
                imageCenterPointList.get(iPolygon).y = myPolygon.getBounds().y;
                myPolygon.setIsSnapped(true);
            }
            for (int i = 0; i < imageList.size(); i++) {
                g.drawImage(imageList.get(i), imageCenterPointList.get(i).x, imageCenterPointList.get(i).y, null);
            }
            drawSnapPolygon(g2, iPolygon);
            g2.draw(myPolygon);
        }
        boolean allPolygonsSnapped = true;
        for (MyPolygon myPolygon : boundingPolygonList) {
            allPolygonsSnapped = allPolygonsSnapped && myPolygon.isSnapped();
        }
        if (allPolygonsSnapped) {
            g2.setColor(STRING_COLOR);
            g2.setFont(new Font("Tahoma", Font.BOLD, 24));
            g2.drawString("Complete!", 100, 50);
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREF_WIDTH, PREF_HEIGHT);
    }

    private class MyMouseAdapter extends MouseAdapter {

        private MyPolygon selectedPolygon = null;
        private MyPoint selectedImageCenter;

        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1) {
                if (boundingPolygonList.size() > 0) {
                    for (int i = boundingPolygonList.size() - 1; i >= 0; i--) {
                        if (boundingPolygonList.get(i).contains(evt.getPoint())) { //if there is a polygon at clicked point
                            selectedPolygon = boundingPolygonList.get(i);
                            Image selectedImage = imageList.get(i);
                            selectedImageCenter = imageCenterPointList.get(i);

                            //move the selected polygon to the end of list so that it will be drawn last (i.e. on top) in paintComponent and checked first for mouse click:
                            boundingPolygonList.remove(selectedPolygon);
                            boundingPolygonList.add(boundingPolygonList.size(), selectedPolygon);

                            Polygon snapPolygon = snapPolygonList.get(i);
                            snapPolygonList.remove(snapPolygon);
                            snapPolygonList.add(snapPolygonList.size(), snapPolygon);

                            imageList.remove(selectedImage);
                            imageList.add(imageList.size(), selectedImage);

                            imageCenterPointList.remove(selectedImageCenter);
                            imageCenterPointList.add(imageCenterPointList.size(), selectedImageCenter);

                            prevMouseX = evt.getX();
                            prevMouseY = evt.getY();
                            repaint();
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (selectedPolygon != null) {
                if (!selectedPolygon.isSnapped()) {
                    selectedPolygon.translate(evt.getX() - prevMouseX, evt.getY() - prevMouseY);
                    selectedImageCenter.setLocation(selectedPolygon.getBounds().x, selectedPolygon.getBounds().y);
                    prevMouseX = evt.getX();
                    prevMouseY = evt.getY();
                }
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectedPolygon != null) {
                repaint();
                selectedPolygon = null;
                selectedImageCenter = null;
            }
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Simple Puzzle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new PuzzlePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

}
