/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package grafica;

import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PShape;

public class GLayer implements PConstants {
    // The parent Processing applet
    private final PApplet parent;

    // Id properties
    private final String id;

    // General properties
    private float[] dim;
    private float[] xLim;
    private float[] yLim;
    private boolean xLog;
    private boolean yLog;

    // Points properties
    private GPointsArray points;
    private GPointsArray screenPoints;
    private boolean[] inside;
    private int[] pointColors;
    private float[] pointSizes;

    // Lines properties
    private int lineColor;
    private float lineWidth;

    // Histogram properties
    private GHistogram hist;
    private GPoint histZeroPoint;

    // Labels properties
    private int labelBgColor;
    private float[] labelSeparation;
    private String fontName;
    private int fontColor;
    private int fontSize;
    private PFont font;

    //
    // Constructor
    // /////////////

    public GLayer(PApplet parent, String id, float[] dim, float[] xLim, float[] yLim, boolean xLog, boolean yLog) {
        this.parent = parent;

        this.id = id;

        this.dim = dim.clone();
        this.xLim = xLim.clone();
        this.yLim = yLim.clone();
        this.xLog = xLog;
        this.yLog = yLog;

        // Do some sanity checks
        if (xLog && (xLim[0] <= 0 || xLim[1] <= 0)) {
            PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            PApplet.println("Will set horizontal limits to (0.1, 10)");
            this.xLim = new float[] { 0.1f, 10 };
        }

        if (yLog && (yLim[0] <= 0 || yLim[1] <= 0)) {
            PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            PApplet.println("Will set vertical limits to (0.1, 10)");
            this.yLim = new float[] { 0.1f, 10 };
        }

        // Continue with the rest
        points = new GPointsArray();
        screenPoints = new GPointsArray();
        inside = new boolean[0];
        pointColors = new int[] { parent.color(255, 0, 0, 150) };
        pointSizes = new float[] { 7 };

        lineColor = parent.color(0, 150);
        lineWidth = 1;

        hist = null;
        histZeroPoint = new GPoint(0, 0);

        labelBgColor = parent.color(255, 200);
        labelSeparation = new float[] { 7, 7 };
        fontName = "SansSerif.plain";
        fontColor = parent.color(0);
        fontSize = 11;
        font = parent.createFont(fontName, fontSize);
    }

    //
    // Methods
    // //////////

    private boolean isValidNumber(float number) {
        return !Float.isNaN(number) && !Float.isInfinite(number);
    }

    public boolean isId(String name) {
        return id.equals(name);
    }

    public float[] valueToPlot(float x, float y) {
        float xScreen, yScreen;

        if (xLog) {
            xScreen = PApplet.log(x / xLim[0]) * dim[0] / PApplet.log(xLim[1] / xLim[0]);
        } else {
            xScreen = (x - xLim[0]) * dim[0] / (xLim[1] - xLim[0]);
        }

        if (yLog) {
            yScreen = -PApplet.log(y / yLim[0]) * dim[1] / PApplet.log(yLim[1] / yLim[0]);
        } else {
            yScreen = -(y - yLim[0]) * dim[1] / (yLim[1] - yLim[0]);
        }

        return new float[] { xScreen, yScreen };
    }

    private GPoint valueToScreen(GPoint p) {
        GPoint result = null;

        if (p != null) {
            float xScreen, yScreen;

            if (xLog) {
                xScreen = PApplet.log(p.getX() / xLim[0]) * dim[0] / PApplet.log(xLim[1] / xLim[0]);
            } else {
                xScreen = (p.getX() - xLim[0]) * dim[0] / (xLim[1] - xLim[0]);
            }

            if (yLog) {
                yScreen = -PApplet.log(p.getY() / yLim[0]) * dim[1] / PApplet.log(yLim[1] / yLim[0]);
            } else {
                yScreen = -(p.getY() - yLim[0]) * dim[1] / (yLim[1] - yLim[0]);
            }

            result = new GPoint(xScreen, yScreen, p.getLabel());
        }

        return result;
    }

    private GPointsArray valueToScreen(GPointsArray pts) {
        GPointsArray result = null;

        if (pts != null) {
            int nPoints = pts.getNPoints();
            result = new GPointsArray(nPoints);

            // Go case by case. More code, but it should be faster
            if (xLog && yLog) {
                float xScalingFactor = dim[0] / PApplet.log(xLim[1] / xLim[0]);
                float yScalingFactor = -dim[1] / PApplet.log(yLim[1] / yLim[0]);
                float xScreen, yScreen;

                for (int i = 0; i < nPoints; i++) {
                    xScreen = PApplet.log(pts.getX(i) / xLim[0]) * xScalingFactor;
                    yScreen = PApplet.log(pts.getY(i) / yLim[0]) * yScalingFactor;
                    result.add(xScreen, yScreen, pts.getLabel(i));
                }
            } else if (xLog) {
                float xScalingFactor = dim[0] / PApplet.log(xLim[1] / xLim[0]);
                float yScalingFactor = -dim[1] / (yLim[1] - yLim[0]);
                float xScreen, yScreen;

                for (int i = 0; i < nPoints; i++) {
                    xScreen = PApplet.log(pts.getX(i) / xLim[0]) * xScalingFactor;
                    yScreen = (pts.getY(i) - yLim[0]) * yScalingFactor;
                    result.add(xScreen, yScreen, pts.getLabel(i));
                }
            } else if (yLog) {
                float xScalingFactor = dim[0] / (xLim[1] - xLim[0]);
                float yScalingFactor = -dim[1] / PApplet.log(yLim[1] / yLim[0]);
                float xScreen, yScreen;

                for (int i = 0; i < nPoints; i++) {
                    xScreen = (pts.getX(i) - xLim[0]) * xScalingFactor;
                    yScreen = PApplet.log(pts.getY(i) / yLim[0]) * yScalingFactor;
                    result.add(xScreen, yScreen, pts.getLabel(i));
                }
            } else {
                float xScalingFactor = dim[0] / (xLim[1] - xLim[0]);
                float yScalingFactor = -dim[1] / (yLim[1] - yLim[0]);
                float xScreen, yScreen;

                for (int i = 0; i < nPoints; i++) {
                    xScreen = (pts.getX(i) - xLim[0]) * xScalingFactor;
                    yScreen = (pts.getY(i) - yLim[0]) * yScalingFactor;
                    result.add(xScreen, yScreen, pts.getLabel(i));
                }
            }
        }

        return result;
    }

    private void updateScreenPoints() {
        int nPoints = points.getNPoints();

        // Go case by case. More code, but it should be faster
        if (xLog && yLog) {
            float xScalingFactor = dim[0] / PApplet.log(xLim[1] / xLim[0]);
            float yScalingFactor = -dim[1] / PApplet.log(yLim[1] / yLim[0]);
            float xScreen, yScreen;

            for (int i = 0; i < nPoints; i++) {
                xScreen = PApplet.log(points.getX(i) / xLim[0]) * xScalingFactor;
                yScreen = PApplet.log(points.getY(i) / yLim[0]) * yScalingFactor;
                screenPoints.setXY(i, xScreen, yScreen);
            }
        } else if (xLog) {
            float xScalingFactor = dim[0] / PApplet.log(xLim[1] / xLim[0]);
            float yScalingFactor = -dim[1] / (yLim[1] - yLim[0]);
            float xScreen, yScreen;

            for (int i = 0; i < nPoints; i++) {
                xScreen = PApplet.log(points.getX(i) / xLim[0]) * xScalingFactor;
                yScreen = (points.getY(i) - yLim[0]) * yScalingFactor;
                screenPoints.setXY(i, xScreen, yScreen);
            }
        } else if (yLog) {
            float xScalingFactor = dim[0] / (xLim[1] - xLim[0]);
            float yScalingFactor = -dim[1] / PApplet.log(yLim[1] / yLim[0]);
            float xScreen, yScreen;

            for (int i = 0; i < nPoints; i++) {
                xScreen = (points.getX(i) - xLim[0]) * xScalingFactor;
                yScreen = PApplet.log(points.getY(i) / yLim[0]) * yScalingFactor;
                screenPoints.setXY(i, xScreen, yScreen);
            }
        } else {
            float xScalingFactor = dim[0] / (xLim[1] - xLim[0]);
            float yScalingFactor = -dim[1] / (yLim[1] - yLim[0]);
            float xScreen, yScreen;

            for (int i = 0; i < nPoints; i++) {
                xScreen = (points.getX(i) - xLim[0]) * xScalingFactor;
                yScreen = (points.getY(i) - yLim[0]) * yScalingFactor;
                screenPoints.setXY(i, xScreen, yScreen);
            }
        }
    }

    public float[] plotToValue(float xScreen, float yScreen) {
        float x, y;

        if (xLog) {
            x = PApplet.exp(PApplet.log(xLim[0]) + xScreen * PApplet.log(xLim[1] / xLim[0]) / dim[0]);
        } else {
            x = xLim[0] + xScreen * (xLim[1] - xLim[0]) / dim[0];
        }

        if (yLog) {
            y = PApplet.exp(PApplet.log(yLim[0]) - yScreen * PApplet.log(yLim[1] / yLim[0]) / dim[1]);
        } else {
            y = yLim[0] - yScreen * (yLim[1] - yLim[0]) / dim[1];
        }

        return new float[] { x, y };
    }

    private boolean isInside(float xScreen, float yScreen) {
        return (xScreen >= 0) && (xScreen <= dim[0]) && (-yScreen >= 0) && (-yScreen <= dim[1]);
    }

    private boolean isInside(GPoint screenPoint) {
        if (screenPoint != null && screenPoint.isValid()) {
            return screenPoint.getX() >= 0 && screenPoint.getX() <= dim[0] && -screenPoint.getY() >= 0 && -screenPoint.getY() <= dim[1];
        } else {
            return false;
        }
    }

    private boolean[] isInside(GPointsArray screenPts) {
        boolean[] result = null;

        if (screenPts != null) {
            int nPoints = screenPts.getNPoints();
            result = new boolean[nPoints];

            for (int i = 0; i < nPoints; i++) {
                result[i] = isInside(screenPts.get(i));
            }
        }

        return result;
    }

    private void updateInsideArray() {
        int nPoints = screenPoints.getNPoints();

        for (int i = 0; i < nPoints; i++) {
            inside[i] = isInside(screenPoints.get(i));
        }
    }

    public GPoint getPointAtPlotPos(float xScreen, float yScreen) {
        int pointIndex = -1;

        if (isInside(xScreen, yScreen)) {
            int nPoints = screenPoints.getNPoints();
            float minDistSq = 25;

            for (int i = 0; i < nPoints; i++) {
                if (inside[i]) {
                    float distSq = PApplet.sq(screenPoints.getX(i) - xScreen) + PApplet.sq(screenPoints.getY(i) - yScreen);

                    if (distSq < minDistSq) {
                        minDistSq = distSq;
                        pointIndex = i;
                    }
                }
            }
        }

        return points.get(pointIndex);
    }

    private float[][] obtainAxisIntersections(GPoint screenPoint1, GPoint screenPoint2) {
        float[][] cuts = new float[0][0];

        if (screenPoint1 != null && screenPoint2 != null && screenPoint1.isValid() && screenPoint2.isValid()) {
            float x1 = screenPoint1.getX();
            float y1 = screenPoint1.getY();
            float x2 = screenPoint2.getX();
            float y2 = screenPoint2.getY();
            boolean inside1 = isInside(x1, y1);
            boolean inside2 = isInside(x2, y2);

            // Check if the line between the two points could cut the borders of
            // the inner plotting area
            boolean dontCut = (inside1 && inside2) || (x1 < 0 && x2 < 0) || (x1 > dim[0] && x2 > dim[0]) || (-y1 < 0 && -y2 < 0)
                    || (-y1 > dim[1] && -y2 > dim[1]);

            if (!dontCut) {
                // Obtain the axis cuts of the line that cross the two points
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;

                if (deltaX == 0) {
                    cuts = new float[2][2];
                    cuts[0] = new float[] { x1, 0 };
                    cuts[1] = new float[] { x1, -dim[1] };
                } else if (deltaY == 0) {
                    cuts = new float[2][2];
                    cuts[0] = new float[] { 0, y1 };
                    cuts[1] = new float[] { dim[0], y1 };
                } else {
                    // Obtain the straight line (y = A + slope*x) that crosses
                    // the two points
                    float slope = deltaY / deltaX;
                    float A = y1 - slope * x1;

                    // Calculate the axis cuts of that line
                    cuts = new float[4][2];
                    cuts[0] = new float[] { -A / slope, 0 };
                    cuts[1] = new float[] { (-dim[1] - A) / slope, -dim[1] };
                    cuts[2] = new float[] { 0, A };
                    cuts[3] = new float[] { dim[0], A + slope * dim[0] };
                }

                // Select only the cuts that fall inside the inner region and
                // are located between the two points
                cuts = getValidCuts(cuts, screenPoint1, screenPoint2);

                // Make sure we have the correct number of cuts
                if (inside1 || inside2) {
                    // One of the points is inside. We should have one cut only
                    if (cuts.length != 1) {
                        GPoint pointInside = (inside1) ? screenPoint1 : screenPoint2;

                        // If too many cuts
                        if (cuts.length > 1) {
                            cuts = removeDuplicatedCuts(cuts, 0);

                            if (cuts.length > 1) {
                                cuts = removePointFromCuts(cuts, pointInside, 0);

                                // In case of rounding number errors
                                if (cuts.length > 1) {
                                    cuts = removeDuplicatedCuts(cuts, 0.001f);

                                    if (cuts.length > 1) {
                                        cuts = removePointFromCuts(cuts, pointInside, 0.001f);
                                    }
                                }
                            }
                        }

                        // If the cut is missing, then it must be equal to the
                        // point inside
                        if (cuts.length == 0) {
                            cuts = new float[1][2];
                            cuts[0] = new float[] { pointInside.getX(), pointInside.getY() };
                        }
                    }
                } else {
                    // Both points are outside. We should have either two cuts
                    // or none
                    if (cuts.length > 2) {
                        cuts = removeDuplicatedCuts(cuts, 0);

                        // In case of rounding number errors
                        if (cuts.length > 2) {
                            cuts = removeDuplicatedCuts(cuts, 0.001f);
                        }
                    }

                    // If we have two cuts, order them (the closest to the first
                    // point goes first)
                    if (cuts.length == 2) {
                        if ((PApplet.abs(cuts[0][0] - x1) + PApplet.abs(cuts[0][1] - y1)) < (PApplet.abs(cuts[1][0] - x1) + PApplet
                                .abs(cuts[1][1] - y1))) {
                            cuts = new float[][] { cuts[0], cuts[1] };
                        } else {
                            cuts = new float[][] { cuts[1], cuts[0] };
                        }
                    }

                    // If one cut is missing, add the same one twice
                    if (cuts.length == 1) {
                        cuts = new float[][] { cuts[0], cuts[0] };
                    }
                }

                // Some sanity checks
                if ((inside1 || inside2) && cuts.length != 1) {
                    PApplet.println("There should be one cut!!!");
                } else if (!inside1 && !inside2 && cuts.length != 0 && cuts.length != 2) {
                    PApplet.println("There should be either 0 or 2 cuts!!! We found " + cuts.length);
                }
            }
        }

        return cuts;
    }

    private float[][] getValidCuts(float[][] cuts, GPoint screenPoint1, GPoint screenPoint2) {
        float x1 = screenPoint1.getX();
        float y1 = screenPoint1.getY();
        float x2 = screenPoint2.getX();
        float y2 = screenPoint2.getY();
        float deltaX = PApplet.abs(x2 - x1);
        float deltaY = PApplet.abs(y2 - y1);
        int counter = 0;

        for (int i = 0; i < cuts.length; i++) {
            // Check that the cut is inside the inner plotting area
            if (isInside(cuts[i][0], cuts[i][1])) {
                // Check that the cut falls between the two points
                if (PApplet.abs(cuts[i][0] - x1) <= deltaX && PApplet.abs(cuts[i][1] - y1) <= deltaY
                        && PApplet.abs(cuts[i][0] - x2) <= deltaX && PApplet.abs(cuts[i][1] - y2) <= deltaY) {
                    cuts[counter] = cuts[i];
                    counter++;
                }
            }
        }

        return Arrays.copyOf(cuts, counter);
    }

    private float[][] removeDuplicatedCuts(float[][] cuts, float tolerance) {
        int counter = 0;

        for (int i = 0; i < cuts.length; i++) {
            boolean repeated = false;

            for (int j = 0; j < counter; j++) {
                if (PApplet.abs(cuts[j][0] - cuts[i][0]) <= tolerance && PApplet.abs(cuts[j][1] - cuts[i][1]) <= tolerance) {
                    repeated = true;
                }
            }

            if (!repeated) {
                cuts[counter] = cuts[i];
                counter++;
            }
        }

        return Arrays.copyOf(cuts, counter);
    }

    private float[][] removePointFromCuts(float[][] cuts, GPoint screenPoint, float tolerance) {
        float x = screenPoint.getX();
        float y = screenPoint.getY();
        int counter = 0;

        for (int i = 0; i < cuts.length; i++) {
            if (PApplet.abs(cuts[i][0] - x) > tolerance || PApplet.abs(cuts[i][1] - y) > tolerance) {
                cuts[counter] = cuts[i];
                counter++;
            }
        }

        return Arrays.copyOf(cuts, counter);
    }

    public void startHistogram(int histType) {
        hist = new GHistogram(parent, dim, screenPoints);
        hist.setType(histType);
    }

    public void drawPoints() {
        int nPoints = screenPoints.getNPoints();
        int nColors = pointColors.length;
        int nSizes = pointSizes.length;

        parent.pushStyle();
        parent.ellipseMode(CENTER);
        parent.noStroke();

        if (nColors == 1 && nSizes == 1) {
            parent.fill(pointColors[0]);

            for (int i = 0; i < nPoints; i++) {
                if (inside[i]) {
                    parent.ellipse(screenPoints.getX(i), screenPoints.getY(i), pointSizes[0], pointSizes[0]);
                }
            }
        } else if (nColors == 1) {
            parent.fill(pointColors[0]);

            for (int i = 0; i < nPoints; i++) {
                if (inside[i]) {
                    parent.ellipse(screenPoints.getX(i), screenPoints.getY(i), pointSizes[i % nSizes], pointSizes[i % nSizes]);
                }
            }
        } else if (nSizes == 1) {
            for (int i = 0; i < nPoints; i++) {
                if (inside[i]) {
                    parent.fill(pointColors[i % nColors]);
                    parent.ellipse(screenPoints.getX(i), screenPoints.getY(i), pointSizes[0], pointSizes[0]);
                }
            }
        } else {
            for (int i = 0; i < nPoints; i++) {
                if (inside[i]) {
                    parent.fill(pointColors[i % nColors]);
                    parent.ellipse(screenPoints.getX(i), screenPoints.getY(i), pointSizes[i % nSizes], pointSizes[i % nSizes]);
                }
            }
        }
        parent.popStyle();
    }

    public void drawPoints(PShape s) {
        if (s != null) {
            int nPoints = screenPoints.getNPoints();
            int nColors = pointColors.length;

            parent.pushStyle();
            if (nColors == 1) {
                parent.fill(pointColors[0]);
                parent.stroke(pointColors[0]);

                for (int i = 0; i < nPoints; i++) {
                    if (inside[i]) {
                        parent.shape(s, screenPoints.getX(i), screenPoints.getY(i));
                    }
                }
            } else {
                for (int i = 0; i < nPoints; i++) {
                    if (inside[i]) {
                        parent.fill(pointColors[i % nColors]);
                        parent.stroke(pointColors[i % nColors]);
                        parent.shape(s, screenPoints.getX(i), screenPoints.getY(i));
                    }
                }
            }
            parent.popStyle();
        }
    }

    public void drawPoint(GPoint p, int col, float size) {
        if (p != null) {
            GPoint screenPoint = valueToScreen(p);

            if (isInside(screenPoint)) {
                parent.pushStyle();
                parent.ellipseMode(CENTER);
                parent.fill(col);
                parent.noStroke();
                parent.ellipse(screenPoint.getX(), screenPoint.getY(), size, size);
                parent.popStyle();
            }
        }
    }

    public void drawPoint(GPoint p) {
        drawPoint(p, pointColors[0], pointSizes[0]);
    }

    public void drawPoint(GPoint p, PShape s) {
        if (p != null && s != null) {
            GPoint screenPoint = valueToScreen(p);

            if (isInside(screenPoint)) {
                parent.shape(s, screenPoint.getX(), screenPoint.getY());
            }
        }
    }

    public void drawPoint(GPoint p, PShape s, int col) {
        if (p != null && s != null) {
            GPoint screenPoint = valueToScreen(p);

            if (isInside(screenPoint)) {
                parent.pushStyle();
                parent.fill(col);
                parent.stroke(col);
                parent.strokeCap(SQUARE);
                parent.shape(s, screenPoint.getX(), screenPoint.getY());
                parent.popStyle();
            }
        }
    }

    public void drawLines() {
        parent.pushStyle();
        parent.noFill();
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        for (int i = 0; i < screenPoints.getNPoints() - 1; i++) {
            if (inside[i] && inside[i + 1]) {
                parent.line(screenPoints.getX(i), screenPoints.getY(i), screenPoints.getX(i + 1), screenPoints.getY(i + 1));
            } else if (screenPoints.isValid(i) && screenPoints.isValid(i + 1)) {
                // At least one of the points is outside the inner region.
                // Obtain the valid line axis intersections
                float[][] cuts = obtainAxisIntersections(screenPoints.get(i), screenPoints.get(i + 1));

                if (inside[i]) {
                    parent.line(screenPoints.getX(i), screenPoints.getY(i), cuts[0][0], cuts[0][1]);
                } else if (inside[i + 1]) {
                    parent.line(cuts[0][0], cuts[0][1], screenPoints.getX(i + 1), screenPoints.getY(i + 1));
                } else if (cuts.length > 1) {
                    parent.line(cuts[0][0], cuts[0][1], cuts[1][0], cuts[1][1]);
                }
            }
        }
        parent.popStyle();
    }

    public void drawLine(GPoint p1, GPoint p2, int col, float lw) {
        if (p1 != null && p2 != null) {
            GPoint screenPoint1 = valueToScreen(p1);
            GPoint screenPoint2 = valueToScreen(p2);

            if (screenPoint1.isValid() && screenPoint2.isValid()) {
                boolean inside1 = isInside(screenPoint1);
                boolean inside2 = isInside(screenPoint2);

                parent.pushStyle();
                parent.noFill();
                parent.stroke(col);
                parent.strokeWeight(lw);
                parent.strokeCap(SQUARE);

                if (inside1 && inside2) {
                    parent.line(screenPoint1.getX(), screenPoint1.getY(), screenPoint2.getX(), screenPoint2.getY());
                } else {
                    // At least one of the points is outside the inner region.
                    // Obtain the valid line axis intersections
                    float[][] cuts = obtainAxisIntersections(screenPoint1, screenPoint2);

                    if (inside1) {
                        parent.line(screenPoint1.getX(), screenPoint1.getY(), cuts[0][0], cuts[0][1]);
                    } else if (inside2) {
                        parent.line(cuts[0][0], cuts[0][1], screenPoint2.getX(), screenPoint2.getY());
                    } else if (cuts.length > 1) {
                        parent.line(cuts[0][0], cuts[0][1], cuts[1][0], cuts[1][1]);
                    }
                }
                parent.popStyle();
            }
        }
    }

    public void drawLine(GPoint p1, GPoint p2) {
        drawLine(p1, p2, lineColor, lineWidth);
    }

    public void drawHorizontalLine(float val, int col, float lw) {
        GPoint screenPoint = valueToScreen(new GPoint(1, val));

        if (screenPoint.isValid() && -screenPoint.getY() >= 0 && -screenPoint.getY() <= dim[1]) {
            parent.pushStyle();
            parent.noFill();
            parent.stroke(col);
            parent.strokeWeight(lw);
            parent.strokeCap(SQUARE);
            parent.line(0, screenPoint.getY(), dim[0], screenPoint.getY());
            parent.popStyle();
        }
    }

    public void drawHorizontalLine(float val) {
        drawHorizontalLine(val, lineColor, lineWidth);
    }

    public void drawVerticalLine(float val, int col, float lw) {
        GPoint screenPoint = valueToScreen(new GPoint(val, 1));

        if (screenPoint.isValid() && screenPoint.getX() >= 0 && screenPoint.getX() <= dim[0]) {
            parent.pushStyle();
            parent.noFill();
            parent.stroke(col);
            parent.strokeWeight(lw);
            parent.strokeCap(SQUARE);
            parent.line(screenPoint.getX(), 0, screenPoint.getX(), -dim[1]);
            parent.popStyle();
        }
    }

    public void drawVerticalLine(float val) {
        drawVerticalLine(val, lineColor, lineWidth);
    }

    public void drawInclinedLine(float slope, float xCut, int col, float lw) {
        GPoint p1, p2;

        if (xLog && yLog) {
            p1 = new GPoint(xLim[0], PApplet.pow(10, slope * PApplet.log(xLim[0]) / PApplet.log(10.0f) + xCut));
            p2 = new GPoint(xLim[1], PApplet.pow(10, slope * PApplet.log(xLim[1]) / PApplet.log(10.0f) + xCut));
        } else if (xLog) {
            p1 = new GPoint(xLim[0], slope * PApplet.log(xLim[0]) / PApplet.log(10.0f) + xCut);
            p2 = new GPoint(xLim[1], slope * PApplet.log(xLim[1]) / PApplet.log(10.0f) + xCut);
        } else if (yLog) {
            p1 = new GPoint(xLim[0], PApplet.pow(10, slope * xLim[0] + xCut));
            p2 = new GPoint(xLim[1], PApplet.pow(10, slope * xLim[1] + xCut));
        } else {
            p1 = new GPoint(xLim[0], slope * xLim[0] + xCut);
            p2 = new GPoint(xLim[1], slope * xLim[1] + xCut);
        }

        drawLine(p1, p2, col, lw);
    }

    public void drawInclinedLine(float slope, float xCut) {
        drawInclinedLine(slope, xCut, lineColor, lineWidth);
    }

    public void drawFilledLines(String type, float referenceValue) {
        // Get the points that compose the shape
        GPointsArray shapePoints = null;

        if (type.equals("horizontal")) {
            shapePoints = getHorizontalShape(referenceValue);
        } else {
            shapePoints = getVerticalShape(referenceValue);
        }

        // Draw the shape
        if (shapePoints != null && shapePoints.getNPoints() > 0) {
            parent.pushStyle();
            parent.fill(lineColor);
            parent.noStroke();

            parent.beginShape();
            for (int i = 0; i < shapePoints.getNPoints(); i++) {
                if (shapePoints.isValid(i)) {
                    parent.vertex(shapePoints.getX(i), shapePoints.getY(i));
                }
            }
            parent.endShape(CLOSE);
            parent.popStyle();
        }
    }

    private GPointsArray getHorizontalShape(float referenceValue) {
        // Collect the points and cuts inside the inner region
        int nPoints = screenPoints.getNPoints();
        GPointsArray shapePoints = new GPointsArray(2 * nPoints);
        int indexFirstPoint = -1;
        int indexLastPoint = -1;

        for (int i = 0; i < nPoints; i++) {
            if (screenPoints.isValid(i)) {
                boolean addedPoints = false;

                // Add the point if it's inside the inner region
                if (inside[i]) {
                    shapePoints.add(screenPoints.getX(i), screenPoints.getY(i), "normal point");
                    addedPoints = true;
                }

                // If it's outside, add the projection of the point on the
                // horizontal axes
                if (!inside[i] && screenPoints.getX(i) >= 0 && screenPoints.getX(i) <= dim[0]) {
                    if (-screenPoints.getY(i) < 0) {
                        shapePoints.add(screenPoints.getX(i), 0, "projection");
                        addedPoints = true;
                    } else {
                        shapePoints.add(screenPoints.getX(i), -dim[1], "projection");
                        addedPoints = true;
                    }
                }

                // Add the axis cuts if there is any
                int nextIndex = i + 1;

                while (nextIndex < nPoints - 1 && !screenPoints.isValid(nextIndex)) {
                    nextIndex++;
                }

                if (nextIndex < nPoints && screenPoints.isValid(nextIndex)) {
                    float[][] cuts = obtainAxisIntersections(screenPoints.get(i), screenPoints.get(nextIndex));

                    for (int j = 0; j < cuts.length; j++) {
                        shapePoints.add(cuts[j][0], cuts[j][1], "cut");
                        addedPoints = true;
                    }
                }

                if (addedPoints) {
                    if (indexFirstPoint < 0) {
                        indexFirstPoint = i;
                    }

                    indexLastPoint = i;
                }
            }
        }

        // Continue if there are points in the shape
        if (shapePoints.getNPoints() > 0) {
            // Calculate the starting point
            GPoint startPoint = new GPoint(shapePoints.get(0));

            if (startPoint.getX() != 0 && startPoint.getX() != dim[0]) {
                if (startPoint.getLabel().equals("cut")) {
                    if (screenPoints.getX(indexFirstPoint) < 0) {
                        startPoint.setX(0);
                        startPoint.setLabel("extreme");
                    } else {
                        startPoint.setX(dim[0]);
                        startPoint.setLabel("extreme");
                    }
                } else if (indexFirstPoint != 0) {
                    // Get the previous valid point
                    int prevIndex = indexFirstPoint - 1;

                    while (prevIndex > 0 && !screenPoints.isValid(prevIndex)) {
                        prevIndex--;
                    }

                    if (screenPoints.isValid(prevIndex)) {
                        if (screenPoints.getX(prevIndex) < 0) {
                            startPoint.setX(0);
                            startPoint.setLabel("extreme");
                        } else {
                            startPoint.setX(dim[0]);
                            startPoint.setLabel("extreme");
                        }
                    }
                }
            }

            // Calculate the end point
            GPoint endPoint = new GPoint(shapePoints.getLastPoint());

            if (endPoint.getX() != 0 && endPoint.getX() != dim[0] && indexLastPoint != nPoints - 1) {
                int nextIndex = indexLastPoint + 1;

                while (nextIndex < nPoints - 1 && !screenPoints.isValid(nextIndex)) {
                    nextIndex++;
                }

                if (screenPoints.isValid(nextIndex)) {
                    if (screenPoints.getX(nextIndex) < 0) {
                        endPoint.setX(0);
                        endPoint.setLabel("extreme");
                    } else {
                        endPoint.setX(dim[0]);
                        endPoint.setLabel("extreme");
                    }
                }
            }

            // Add the end point if it's a new extreme
            if (endPoint.getLabel().equals("extreme")) {
                shapePoints.add(endPoint);
            }

            // Add the reference connections
            if (yLog && referenceValue <= 0)
                referenceValue = PApplet.min(yLim[0], yLim[1]);
            GPoint screenReferencePoint = valueToScreen(new GPoint(1, referenceValue));

            if (-screenReferencePoint.getY() < 0) {
                shapePoints.add(endPoint.getX(), 0);
                shapePoints.add(startPoint.getX(), 0);
            } else if (-screenReferencePoint.getY() > dim[1]) {
                shapePoints.add(endPoint.getX(), -dim[1]);
                shapePoints.add(startPoint.getX(), -dim[1]);
            } else {
                shapePoints.add(endPoint.getX(), screenReferencePoint.getY());
                shapePoints.add(startPoint.getX(), screenReferencePoint.getY());
            }

            // Add the starting point if it's a new extreme
            if (startPoint.getLabel().equals("extreme")) {
                shapePoints.add(startPoint);
            }
        }

        return shapePoints;
    }

    private GPointsArray getVerticalShape(float referenceValue) {
        // Collect the points and cuts inside the inner region
        int nPoints = screenPoints.getNPoints();
        GPointsArray shapePoints = new GPointsArray(2 * nPoints);
        int indexFirstPoint = -1;
        int indexLastPoint = -1;

        for (int i = 0; i < nPoints; i++) {
            if (screenPoints.isValid(i)) {
                boolean addedPoints = false;

                // Add the point if it's inside the inner region
                if (inside[i]) {
                    shapePoints.add(screenPoints.getX(i), screenPoints.getY(i), "normal point");
                    addedPoints = true;
                }

                // If it's outside, add the projection of the point on the
                // vertical axes
                if (!inside[i] && -screenPoints.getY(i) >= 0 && -screenPoints.getY(i) <= dim[1]) {
                    if (screenPoints.getX(i) < 0) {
                        shapePoints.add(0, screenPoints.getY(i), "projection");
                        addedPoints = true;
                    } else {
                        shapePoints.add(dim[0], screenPoints.getY(i), "projection");
                        addedPoints = true;
                    }
                }

                // Add the axis cuts if there is any
                int nextIndex = i + 1;

                while (nextIndex < nPoints - 1 && !screenPoints.isValid(nextIndex)) {
                    nextIndex++;
                }

                if (nextIndex < nPoints && screenPoints.isValid(nextIndex)) {
                    float[][] cuts = obtainAxisIntersections(screenPoints.get(i), screenPoints.get(nextIndex));

                    for (int j = 0; j < cuts.length; j++) {
                        shapePoints.add(cuts[j][0], cuts[j][1], "cut");
                        addedPoints = true;
                    }
                }

                if (addedPoints) {
                    if (indexFirstPoint < 0) {
                        indexFirstPoint = i;
                    }

                    indexLastPoint = i;
                }
            }
        }

        // Continue if there are points in the shape
        if (shapePoints.getNPoints() > 0) {
            // Calculate the starting point
            GPoint startPoint = new GPoint(shapePoints.get(0));

            if (startPoint.getY() != 0 && startPoint.getY() != -dim[1]) {
                if (startPoint.getLabel().equals("cut")) {
                    if (-screenPoints.getY(indexFirstPoint) < 0) {
                        startPoint.setY(0);
                        startPoint.setLabel("extreme");
                    } else {
                        startPoint.setY(-dim[1]);
                        startPoint.setLabel("extreme");
                    }
                } else if (indexFirstPoint != 0) {
                    // Get the previous valid point
                    int prevIndex = indexFirstPoint - 1;

                    while (prevIndex > 0 && !screenPoints.isValid(prevIndex)) {
                        prevIndex--;
                    }

                    if (screenPoints.isValid(prevIndex)) {
                        if (-screenPoints.getY(prevIndex) < 0) {
                            startPoint.setY(0);
                            startPoint.setLabel("extreme");
                        } else {
                            startPoint.setY(-dim[1]);
                            startPoint.setLabel("extreme");
                        }
                    }
                }
            }

            // Calculate the end point
            GPoint endPoint = new GPoint(shapePoints.getLastPoint());

            if (endPoint.getY() != 0 && endPoint.getY() != -dim[1] && indexLastPoint != nPoints - 1) {
                int nextIndex = indexLastPoint + 1;

                while (nextIndex < nPoints - 1 && !screenPoints.isValid(nextIndex)) {
                    nextIndex++;
                }

                if (screenPoints.isValid(nextIndex)) {
                    if (-screenPoints.getY(nextIndex) < 0) {
                        endPoint.setY(0);
                        endPoint.setLabel("extreme");
                    } else {
                        endPoint.setY(-dim[1]);
                        endPoint.setLabel("extreme");
                    }
                }
            }

            // Add the end point if it's a new extreme
            if (endPoint.getLabel().equals("extreme")) {
                shapePoints.add(endPoint);
            }

            // Add the reference connections
            if (xLog && referenceValue <= 0)
                referenceValue = PApplet.min(xLim[0], xLim[1]);
            GPoint screenReferencePoint = valueToScreen(new GPoint(referenceValue, 1));

            if (screenReferencePoint.getX() < 0) {
                shapePoints.add(0, endPoint.getY());
                shapePoints.add(0, startPoint.getY());
            } else if (screenReferencePoint.getX() > dim[0]) {
                shapePoints.add(dim[0], endPoint.getY());
                shapePoints.add(dim[0], startPoint.getY());
            } else {
                shapePoints.add(screenReferencePoint.getX(), endPoint.getY());
                shapePoints.add(screenReferencePoint.getX(), startPoint.getY());
            }

            // Add the starting point if it's a new extreme
            if (startPoint.getLabel().equals("extreme")) {
                shapePoints.add(startPoint);
            }
        }

        return shapePoints;
    }

    public void drawLabel(GPoint p) {
        if (p != null) {
            GPoint screenPoint = valueToScreen(p);

            if (screenPoint.isValid()) {
                float xLabelPos = screenPoint.getX() + labelSeparation[0];
                float yLabelPos = screenPoint.getY() - labelSeparation[1];
                float delta = 3;

                parent.pushStyle();
                parent.noStroke();
                parent.textFont(font);
                parent.textSize(fontSize);
                parent.textAlign(LEFT, BOTTOM);

                // Draw the background
                parent.fill(labelBgColor);
                parent.rect(xLabelPos - delta, yLabelPos - fontSize - delta, parent.textWidth(p.getLabel()) + 2 * delta, fontSize + 2
                        * delta);

                // Draw the text
                parent.fill(fontColor);
                parent.text(p.getLabel(), xLabelPos, yLabelPos);
                parent.popStyle();
            }
        }
    }

    public void drawLabelAtScreenPos(float xScreen, float yScreen) {
        GPoint p = getPointAtPlotPos(xScreen, yScreen);
        drawLabel(p);
    }

    public void drawHistogram() {
        if (hist != null) {
            hist.draw(valueToScreen(histZeroPoint));
        }
    }

    public void drawPolygon(GPointsArray poly, int col) {
        if (poly != null && poly.getNPoints() > 2) {
            // Remove the polygon invalid points
            GPointsArray screenPoly = valueToScreen(poly);
            screenPoly.removeInvalidPoints();

            // Create an new polygon with the points inside the plotting area
            // and the valid axis cuts
            boolean[] insidePoly = isInside(screenPoly);
            int nPoints = screenPoly.getNPoints();
            GPointsArray newPoly = new GPointsArray(2 * nPoints);

            for (int i = 0; i < nPoints; i++) {
                if (insidePoly[i]) {
                    newPoly.add(screenPoly.getX(i), screenPoly.getY(i), "normal point");
                }

                // Obtain the cuts with the next point
                int nextIndex = (i + 1 < nPoints) ? i + 1 : 0;
                float[][] cuts = obtainAxisIntersections(screenPoly.get(i), screenPoly.get(nextIndex));

                if (cuts.length == 1) {
                    newPoly.add(cuts[0][0], cuts[0][1], "single cut");
                } else if (cuts.length > 1) {
                    newPoly.add(cuts[0][0], cuts[0][1], "double cut");
                    newPoly.add(cuts[1][0], cuts[1][1], "double cut");
                }
            }

            // Final modification of the polygon
            nPoints = newPoly.getNPoints();
            GPointsArray croppedPoly = new GPointsArray(2 * nPoints);

            for (int i = 0; i < nPoints; i++) {
                // Add the point
                croppedPoly.add(newPoly.get(i));

                // Add new points in case we have two consecutive cuts, one of
                // them is single, and they are in consecutive axes
                int next = (i + 1 < nPoints) ? i + 1 : 0;
                String lab = newPoly.getLabel(i);
                String nextLab = newPoly.getLabel(next);

                boolean cond = (lab.equals("single cut") && nextLab.equals("single cut"))
                        || (lab.equals("single cut") && nextLab.equals("double cut"))
                        || (lab.equals("double cut") && nextLab.equals("single cut"));

                if (cond) {
                    float x1 = newPoly.getX(i);
                    float y1 = newPoly.getY(i);
                    float x2 = newPoly.getX(next);
                    float y2 = newPoly.getY(next);
                    float deltaX = PApplet.abs(x2 - x1);
                    float deltaY = PApplet.abs(y2 - y1);

                    // Check that they come from consecutive axes
                    if (deltaX > 0 && deltaY > 0 && deltaX != dim[0] && deltaY != dim[1]) {
                        float x = (x1 == 0 || x1 == dim[0]) ? x1 : x2;
                        float y = (y1 == 0 || y1 == -dim[1]) ? y1 : y2;
                        croppedPoly.add(x, y, "special cut");
                    }
                }
            }

            // Draw the cropped polygon
            if (croppedPoly.getNPoints() > 2) {
                parent.pushStyle();
                parent.fill(col);
                parent.noStroke();

                parent.beginShape();
                for (int i = 0; i < croppedPoly.getNPoints(); i++) {
                    parent.vertex(croppedPoly.getX(i), croppedPoly.getY(i));
                }
                parent.endShape(CLOSE);
                parent.popStyle();
            }
        }
    }

    public void drawAnnotation(String text, float x, float y, int horAlign, int verAlign) {
        float[] screenPos = valueToPlot(x, y);

        if (isValidNumber(screenPos[0]) && isValidNumber(screenPos[1]) && isInside(screenPos[0], screenPos[1])) {
            if (horAlign != CENTER && horAlign != RIGHT && horAlign != LEFT) {
                horAlign = LEFT;
            }

            if (verAlign != CENTER && verAlign != TOP && verAlign != BOTTOM) {
                verAlign = CENTER;
            }

            parent.pushStyle();
            parent.textFont(font);
            parent.textSize(fontSize);
            parent.fill(fontColor);
            parent.textAlign(horAlign, verAlign);
            parent.text(text, screenPos[0], screenPos[1]);
            parent.popStyle();
        }
    }

    //
    // Setters
    // //////////

    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            dim = newDim.clone();
            updateScreenPoints();
            updateInsideArray();

            if (hist != null) {
                hist.setDim(dim);
                hist.setScreenPoints(screenPoints);
            }
        }
    }

    public void setXlim(float[] newXLim) {
        if (newXLim != null && newXLim.length == 2 && newXLim[1] != newXLim[0] && isValidNumber(newXLim[0]) && isValidNumber(newXLim[1])) {
            // Make sure the new limit makes sense
            if (xLog && (newXLim[0] <= 0 || newXLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                xLim = newXLim.clone();
                updateScreenPoints();
                updateInsideArray();

                if (hist != null) {
                    hist.setScreenPoints(screenPoints);
                }
            }
        }
    }

    public void setYlim(float[] newYLim) {
        if (newYLim != null && newYLim.length == 2 && newYLim[1] != newYLim[0] && isValidNumber(newYLim[0]) && isValidNumber(newYLim[1])) {
            // Make sure the new limit makes sense
            if (yLog && (newYLim[0] <= 0 || newYLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                yLim = newYLim.clone();
                updateScreenPoints();
                updateInsideArray();

                if (hist != null) {
                    hist.setScreenPoints(screenPoints);
                }
            }
        }
    }

    public void setLimits(float[] newXLim, float[] newYLim) {
        if (newXLim != null && newXLim.length == 2 && newXLim[1] != newXLim[0] && newYLim != null && newYLim.length == 2
                && newYLim[1] != newYLim[0] && isValidNumber(newXLim[0]) && isValidNumber(newXLim[1]) && isValidNumber(newYLim[0])
                && isValidNumber(newYLim[1])) {
            // Make sure the new limits make sense
            if (xLog && (newXLim[0] <= 0 || newXLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                xLim = newXLim.clone();
            }

            if (yLog && (newYLim[0] <= 0 || newYLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                yLim = newYLim.clone();
            }

            updateScreenPoints();
            updateInsideArray();

            if (hist != null) {
                hist.setScreenPoints(screenPoints);
            }
        }
    }

    public void setLimitsAndLog(float[] newXLim, float[] newYLim, boolean newXLog, boolean newYLog) {
        if (newXLim != null && newXLim.length == 2 && newXLim[1] != newXLim[0] && newYLim != null && newYLim.length == 2
                && newYLim[1] != newYLim[0] && isValidNumber(newXLim[0]) && isValidNumber(newXLim[1]) && isValidNumber(newYLim[0])
                && isValidNumber(newYLim[1])) {
            // Make sure the new limits make sense
            if (newXLog && (newXLim[0] <= 0 || newXLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                xLim = newXLim.clone();
                xLog = newXLog;
            }

            if (newYLog && (newYLim[0] <= 0 || newYLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                yLim = newYLim.clone();
                yLog = newYLog;
            }

            updateScreenPoints();
            updateInsideArray();

            if (hist != null) {
                hist.setScreenPoints(screenPoints);
            }
        }
    }

    public void setXLog(boolean newXLog) {
        if (newXLog != xLog) {
            if (newXLog && (xLim[0] <= 0 || xLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
                PApplet.println("Will set horizontal limits to (0.1, 10)");
                xLim = new float[] { 0.1f, 10 };
            }

            xLog = newXLog;
            updateScreenPoints();
            updateInsideArray();

            if (hist != null) {
                hist.setScreenPoints(screenPoints);
            }
        }
    }

    public void setYLog(boolean newYLog) {
        if (newYLog != yLog) {
            if (newYLog && (yLim[0] <= 0 || yLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
                PApplet.println("Will set vertical limits to (0.1, 10)");
                yLim = new float[] { 0.1f, 10 };
            }

            yLog = newYLog;
            updateScreenPoints();
            updateInsideArray();

            if (hist != null) {
                hist.setScreenPoints(screenPoints);
            }
        }
    }

    public void setPoints(GPointsArray newPoints) {
        if (newPoints != null) {
            points = new GPointsArray(newPoints);
            screenPoints = valueToScreen(points);
            inside = isInside(screenPoints);

            if (hist != null) {
                hist.setScreenPoints(screenPoints);
            }
        }
    }

    public void setInside(boolean[] newInside) {
        if (newInside != null && newInside.length == inside.length) {
            inside = newInside.clone();
        }
    }

    public void setPointColors(int[] newPointColors) {
        if (newPointColors != null && newPointColors.length > 0) {
            pointColors = newPointColors.clone();
        }
    }

    public void setPointSizes(float[] newPointSizes) {
        if (newPointSizes != null && newPointSizes.length > 0) {
            pointSizes = newPointSizes.clone();
        }
    }

    public void setLineColor(int newLineColor) {
        lineColor = newLineColor;
    }

    public void setLineWidth(float newLineWidth) {
        if (newLineWidth > 0) {
            lineWidth = newLineWidth;
        }
    }

    public void setHistZeroPoint(GPoint newHistZeroPoint) {
        if (newHistZeroPoint != null) {
            histZeroPoint = new GPoint(newHistZeroPoint);
        }
    }

    public void setHistType(int histType) {
        if (hist != null) {
            hist.setType(histType);
        }
    }

    public void setHistVisible(boolean visible) {
        if (hist != null) {
            hist.setVisible(visible);
        }
    }

    public void setDrawHistLabels(boolean drawHistLabels) {
        if (hist != null) {
            hist.setDrawLabels(drawHistLabels);
        }
    }

    public void setLabelBgColor(int newLabelBgColor) {
        labelBgColor = newLabelBgColor;
    }

    public void setLabelSeparation(float[] newLabelSeparation) {
        if (newLabelSeparation != null && newLabelSeparation.length == 2) {
            labelSeparation = newLabelSeparation.clone();
        }
    }

    public void setFontName(String newFontName) {
        fontName = newFontName;
        font = parent.createFont(fontName, fontSize);
    }

    public void setFontColor(int newFontColor) {
        fontColor = newFontColor;
    }

    public void setFontSize(int newFontSize) {
        if (newFontSize > 0) {
            fontSize = newFontSize;
            font = parent.createFont(fontName, fontSize);
        }
    }

    public void setFontProperties(String newFontName, int newFontColor, int newFontSize) {
        if (newFontSize > 0) {
            fontName = newFontName;
            fontColor = newFontColor;
            fontSize = newFontSize;
            font = parent.createFont(fontName, fontSize);
        }
    }

    public void setAllFontProperties(String newFontName, int newFontColor, int newFontSize) {
        setFontProperties(newFontName, newFontColor, newFontSize);

        if (hist != null) {
            hist.setFontProperties(newFontName, newFontColor, newFontSize);
        }
    }

    //
    // Getters
    // //////////

    public String getId() {
        return id;
    }

    public float[] getDim() {
        return dim.clone();
    }

    public float[] getXLim() {
        return xLim.clone();
    }

    public float[] getYLim() {
        return yLim.clone();
    }

    public boolean getXLog() {
        return xLog;
    }

    public boolean getYLog() {
        return yLog;
    }

    public GPointsArray getPoints() {
        return new GPointsArray(points);
    }

    public GPointsArray getPointsRef() {
        return points;
    }

    public GPointsArray getScreenPoints() {
        return new GPointsArray(screenPoints);
    }

    public GPointsArray getScreenPointsRef() {
        return screenPoints;
    }

    public GHistogram getHistogram() {
        return hist;
    }

}
