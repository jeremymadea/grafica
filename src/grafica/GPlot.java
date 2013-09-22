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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

/**
 * Plot class. It controls the rest of the graphical elements (layers, axes,
 * title, limits).
 * 
 * @author Javier Gracia Carpio
 */
public class GPlot implements PConstants {
    // The parent Processing applet
    protected final PApplet parent;

    // General properties
    protected float[] pos;
    protected float[] outerDim;
    protected float[] mar;
    protected float[] dim;
    protected float[] xLim;
    protected float[] yLim;
    protected boolean fixedXLim;
    protected boolean fixedYLim;
    protected boolean xLog;
    protected boolean yLog;
    protected boolean invertedXScale;
    protected boolean invertedYScale;

    // Format properties
    protected int bgColor;
    protected int boxBgColor;
    protected int boxLineColor;
    protected float boxLineWidth;
    protected int gridLineColor;
    protected float gridLineWidth;

    // Layers
    protected final GLayer mainLayer;
    protected final ArrayList<GLayer> layerList;

    // Axes and title
    protected final GAxis xAxis;
    protected final GAxis topAxis;
    protected final GAxis yAxis;
    protected final GAxis rightAxis;
    protected final GTitle title;

    // Constants
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    public static final int BOTH = 2;
    public static final float LOG10 = (float) Math.log(10);

    /**
     * GPlot constructor
     * 
     * @param parent
     *            the parent Processing applet
     */
    public GPlot(PApplet parent) {
        this.parent = parent;

        pos = new float[] { 0, 0 };
        outerDim = new float[] { 450, 300 };
        mar = new float[] { 60, 70, 40, 30 };
        dim = new float[] { outerDim[0] - mar[1] - mar[3], outerDim[1] - mar[0] - mar[2] };
        xLim = new float[] { 0, 1 };
        yLim = new float[] { 0, 1 };
        fixedXLim = false;
        fixedYLim = false;
        xLog = false;
        yLog = false;
        invertedXScale = false;
        invertedYScale = false;

        bgColor = this.parent.color(255);
        boxBgColor = this.parent.color(245);
        boxLineColor = this.parent.color(210);
        boxLineWidth = 1;
        gridLineColor = this.parent.color(210);
        gridLineWidth = 1;

        mainLayer = new GLayer(this.parent, "main layer", dim, xLim, yLim, xLog, yLog);
        layerList = new ArrayList<GLayer>();

        xAxis = new GAxis(this.parent, X, dim, xLim, xLog);
        topAxis = new GAxis(this.parent, TOP, dim, xLim, xLog);
        yAxis = new GAxis(this.parent, Y, dim, yLim, yLog);
        rightAxis = new GAxis(this.parent, RIGHT, dim, yLim, yLog);
        title = new GTitle(this.parent, dim);
    }

    /**
     * Adds a layer to the plot
     * 
     * @param newLayer
     *            the layer to add
     */
    public void addLayer(GLayer newLayer) {
        if (newLayer != null) {
            // Check that it is the only layer with that id
            String id = newLayer.getId();
            boolean sameId = false;

            for (int i = 0; i < layerList.size(); i++) {
                if (layerList.get(i).isId(id)) {
                    sameId = true;
                    break;
                }
            }

            // Add the layer to the list
            if (!sameId) {
                newLayer.setDim(dim);
                newLayer.setLimAndLog(xLim, yLim, xLog, yLog);
                layerList.add(newLayer);
            } else {
                PApplet.println("A layer with the same id exists. Please change the id and try to add it again.");
            }
        }
    }

    /**
     * Adds a new layer to the plot
     * 
     * @param id
     *            the id to use for the new layer
     * @param points
     *            the points to be included in the layer
     */
    public void addLayer(String id, GPointsArray points) {
        // Check that it is the only layer with that id
        boolean sameId = false;

        for (int i = 0; i < layerList.size(); i++) {
            if (layerList.get(i).isId(id)) {
                sameId = true;
                break;
            }
        }

        // Add the layer to the list
        if (!sameId) {
            GLayer newLayer = new GLayer(parent, id, dim, xLim, yLim, xLog, yLog);
            newLayer.setPoints(points);
            layerList.add(newLayer);
        } else {
            PApplet.println("A layer with the same id exists. Please change the id and try to add it again.");
        }
    }

    /**
     * Removes an exiting layer from the plot
     * 
     * @param id
     *            the id of the layer to remove
     */
    public void removeLayer(String id) {
        int index = -1;

        for (int i = 0; i < layerList.size(); i++) {
            if (layerList.get(i).isId(id)) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            layerList.remove(index);
        } else {
            PApplet.println("Couldn't find a layer in the plot with id = " + id);
        }
    }

    /**
     * Calculates the position of a point in the screen, relative to the plot
     * reference system
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     * 
     * @return the x and y positions in the plot reference system
     */
    public float[] getPlotPosAt(float xScreen, float yScreen) {
        float xPlot = xScreen - (pos[0] + mar[1]);
        float yPlot = yScreen - (pos[1] + mar[2] + dim[1]);

        return new float[] { xPlot, yPlot };
    }

    /**
     * Returns the closest point to a given screen position
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     * 
     * @return the closest point in the plot main layer
     */
    public GPoint getPointAt(float xScreen, float yScreen) {
        float[] plotPos = getPlotPosAt(xScreen, yScreen);

        return mainLayer.getPointAtPlotPos(plotPos[0], plotPos[1]);
    }

    /**
     * Returns the plot value at a given screen position
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     * 
     * @return the plot value
     */
    public float[] getValueAt(float xScreen, float yScreen) {
        float[] plotPos = getPlotPosAt(xScreen, yScreen);

        return mainLayer.plotToValue(plotPos[0], plotPos[1]);
    }

    /**
     * Returns the relative plot position of a given screen position
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     * 
     * @return the relative position in the plot reference system
     */
    public float[] getRelativePlotPosAt(float xScreen, float yScreen) {
        float[] plotPos = getPlotPosAt(xScreen, yScreen);

        return new float[] { plotPos[0] / dim[0], -plotPos[1] / dim[1] };
    }

    /**
     * Indicates if a given screen position is inside the main plot area
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     * 
     * @return true if the position is inside the main plot area
     */
    public boolean isOverPlot(float xScreen, float yScreen) {
        return (xScreen >= pos[0]) && (xScreen <= pos[0] + outerDim[0]) && (yScreen >= pos[1]) && (yScreen <= pos[1] + outerDim[1]);
    }

    /**
     * Indicates if a given screen position is inside the plot box area
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     * 
     * @return true if the position is inside the plot box area
     */
    public boolean isOverBox(float xScreen, float yScreen) {
        return (xScreen >= pos[0] + mar[1]) && (xScreen <= pos[0] + outerDim[0] - mar[3]) && (yScreen >= pos[1] + mar[2])
                && (yScreen <= pos[1] + outerDim[1] - mar[0]);
    }

    /**
     * Calculates the x limits of a given set of points
     * 
     * @param points
     *            the points for which we want to calculate the x limits
     * 
     * @return the x limits
     */
    protected float[] obtainXLim(GPointsArray points) {
        // Find the points limits
        float[] lim = new float[] { Float.MAX_VALUE, -Float.MAX_VALUE };

        for (int i = 0; i < points.getNPoints(); i++) {
            if (points.isValid(i)) {
                // Use the point if it's inside, and it's not negative if the
                // scale is logarithmic
                float x = points.getX(i);
                float y = points.getY(i);
                boolean isInside = true;

                if (fixedYLim) {
                    isInside = ((yLim[1] >= yLim[0]) && (y >= yLim[0]) && (y <= yLim[1]))
                            || ((yLim[1] < yLim[0]) && (y <= yLim[0]) && (y >= yLim[1]));
                }

                if (isInside && !(xLog && x <= 0)) {
                    if (x < lim[0])
                        lim[0] = x;
                    if (x > lim[1])
                        lim[1] = x;
                }
            }
        }

        // Check that the new limits make sense
        if (lim[1] > lim[0]) {
            // Increase the range a bit
            if (xLog) {
                float delta = PApplet.exp(0.1f * PApplet.log(lim[1] / lim[0]));
                lim[0] /= delta;
                lim[1] *= delta;
            } else {
                float delta = 0.1f * (lim[1] - lim[0]);
                lim[0] -= delta;
                lim[1] += delta;
            }
        } else if (xLog && (xLim[0] <= 0 || xLim[1] <= 0)) {
            lim = new float[] { 0.1f, 10 };
        } else {
            lim = xLim;
        }

        // Invert the limits if necessary
        if (invertedXScale) {
            lim = new float[] { lim[1], lim[0] };
        }

        return lim;
    }

    /**
     * Calculates the y limits of a given set of points
     * 
     * @param points
     *            the points for which we want to calculate the y limits
     * 
     * @return the y limits
     */
    protected float[] obtainYLim(GPointsArray points) {
        // Find the points limits
        float[] lim = new float[] { Float.MAX_VALUE, -Float.MAX_VALUE };

        for (int i = 0; i < points.getNPoints(); i++) {
            if (points.isValid(i)) {
                // Use the point if it's inside, and it's not negative if the
                // scale is logarithmic
                float x = points.getX(i);
                float y = points.getY(i);
                boolean isInside = true;

                if (fixedXLim) {
                    isInside = ((xLim[1] >= xLim[0]) && (x >= xLim[0]) && (x <= xLim[1]))
                            || ((xLim[1] < xLim[0]) && (x <= xLim[0]) && (x >= xLim[1]));
                }

                if (isInside && !(yLog && y <= 0)) {
                    if (y < lim[0])
                        lim[0] = y;
                    if (y > lim[1])
                        lim[1] = y;
                }
            }
        }

        // Check that the new limits make sense
        if (lim[1] > lim[0]) {
            // Increase the range a bit
            if (yLog) {
                float delta = PApplet.exp(0.1f * PApplet.log(lim[1] / lim[0]));
                lim[0] /= delta;
                lim[1] *= delta;
            } else {
                float delta = 0.1f * (lim[1] - lim[0]);
                lim[0] -= delta;
                lim[1] += delta;
            }
        } else if (yLog && (yLim[0] <= 0 || yLim[1] <= 0)) {
            lim = new float[] { 0.1f, 10 };
        } else {
            lim = yLim;
        }

        // Invert the limits if necessary
        if (invertedYScale) {
            lim = new float[] { lim[1], lim[0] };
        }

        return lim;
    }

    /**
     * Moves the horizontal axes limits by a given amount specified in pixel
     * units
     * 
     * @param delta
     *            pixels to move
     */
    public void moveHorizontalAxesLim(float delta) {
        // Obtain the new x limits
        if (xLog) {
            float deltaLim = PApplet.exp(PApplet.log(xLim[1] / xLim[0]) * delta / dim[0]);
            xLim[0] *= deltaLim;
            xLim[1] *= deltaLim;
        } else {
            float deltaLim = (xLim[1] - xLim[0]) * delta / dim[0];
            xLim[0] += deltaLim;
            xLim[1] += deltaLim;
        }

        // Fix the limits
        fixedXLim = true;

        // Move the horizontal axes
        xAxis.moveLim(xLim);
        topAxis.moveLim(xLim);

        // Update the vertical axes if needed
        if (!fixedYLim) {
            yLim = obtainYLim(mainLayer.getPointsRef());
            yAxis.setLim(yLim);
            rightAxis.setLim(yLim);
        }

        // Update the layers
        mainLayer.setLimits(xLim, yLim);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).setLimits(xLim, yLim);
        }
    }

    /**
     * Moves the vertical axes limits by a given amount specified in pixel units
     * 
     * @param delta
     *            pixels to move
     */
    public void moveVerticalAxesLim(float delta) {
        // Obtain the new y limits
        if (yLog) {
            float deltaLim = PApplet.exp(PApplet.log(yLim[1] / yLim[0]) * delta / dim[1]);
            yLim[0] *= deltaLim;
            yLim[1] *= deltaLim;
        } else {
            float deltaLim = (yLim[1] - yLim[0]) * delta / dim[1];
            yLim[0] += deltaLim;
            yLim[1] += deltaLim;
        }

        // Fix the limits
        fixedYLim = true;

        // Move the vertical axes
        yAxis.moveLim(yLim);
        rightAxis.moveLim(yLim);

        // Update the horizontal axes if needed
        if (!fixedXLim) {
            xLim = obtainXLim(mainLayer.getPointsRef());
            xAxis.setLim(xLim);
            topAxis.setLim(xLim);
        }

        // Update the layers
        mainLayer.setLimits(xLim, yLim);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).setLimits(xLim, yLim);
        }
    }

    /**
     * Centers the plot coordinates on the specified screen position and zooms
     * the limits range by a given factor
     * 
     * @param factor
     *            the plot limits will be zoomed by this factor
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     */
    public void zoom(float factor, float xScreen, float yScreen) {
        // Calculate the new limits
        float[] newCenter = getValueAt(xScreen, yScreen);

        if (xLog) {
            float deltaLim = PApplet.exp(PApplet.log(xLim[1] / xLim[0]) / (2 * factor));
            xLim = new float[] { newCenter[0] / deltaLim, newCenter[0] * deltaLim };
        } else {
            float deltaLim = (xLim[1] - xLim[0]) / (2 * factor);
            xLim = new float[] { newCenter[0] - deltaLim, newCenter[0] + deltaLim };
        }

        if (yLog) {
            float deltaLim = PApplet.exp(PApplet.log(yLim[1] / yLim[0]) / (2 * factor));
            yLim = new float[] { newCenter[1] / deltaLim, newCenter[1] * deltaLim };
        } else {
            float deltaLim = (yLim[1] - yLim[0]) / (2 * factor);
            yLim = new float[] { newCenter[1] - deltaLim, newCenter[1] + deltaLim };
        }

        // Fix the limits
        fixedXLim = true;
        fixedYLim = true;

        // Update the horizontal and vertical axes
        xAxis.setLim(xLim);
        topAxis.setLim(xLim);
        yAxis.setLim(yLim);
        rightAxis.setLim(yLim);

        // Update the layers
        mainLayer.setLimits(xLim, yLim);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).setLimits(xLim, yLim);
        }
    }

    /**
     * Shifts the plot coordinates in a way that the value at a given plot
     * position will have after that the specified new plot position
     * 
     * @param valuePlotPos
     *            current plot position of the value
     * @param newPlotPos
     *            new plot position of the value
     */
    protected void shiftPlotPos(float[] valuePlotPos, float[] newPlotPos) {
        // Calculate the new limits
        float deltaXPlot = valuePlotPos[0] - newPlotPos[0];
        float deltaYPlot = valuePlotPos[1] - newPlotPos[1];

        if (xLog) {
            float deltaLim = PApplet.exp(PApplet.log(xLim[1] / xLim[0]) * deltaXPlot / dim[0]);
            xLim = new float[] { xLim[0] * deltaLim, xLim[1] * deltaLim };
        } else {
            float deltaLim = (xLim[1] - xLim[0]) * deltaXPlot / dim[0];
            xLim = new float[] { xLim[0] + deltaLim, xLim[1] + deltaLim };
        }

        if (yLog) {
            float deltaLim = PApplet.exp(-PApplet.log(yLim[1] / yLim[0]) * deltaYPlot / dim[1]);
            yLim = new float[] { yLim[0] * deltaLim, yLim[1] * deltaLim };
        } else {
            float deltaLim = -(yLim[1] - yLim[0]) * deltaYPlot / dim[1];
            yLim = new float[] { yLim[0] + deltaLim, yLim[1] + deltaLim };
        }

        // Fix the limits
        fixedXLim = true;
        fixedYLim = true;

        // Move the horizontal and vertical axes
        xAxis.moveLim(xLim);
        topAxis.moveLim(xLim);
        yAxis.moveLim(yLim);
        rightAxis.moveLim(yLim);

        // Update the layers
        mainLayer.setLimits(xLim, yLim);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).setLimits(xLim, yLim);
        }
    }

    /**
     * Shifts the plot coordinates in a way that after that the given plot value
     * will be at the specified screen position
     * 
     * @param value
     *            the x and y plot value
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     */
    public void align(float[] value, float xScreen, float yScreen) {
        float[] valuePlotPos = mainLayer.valueToPlot(value[0], value[1]);
        float[] newPlotPos = getPlotPosAt(xScreen, yScreen);

        shiftPlotPos(valuePlotPos, newPlotPos);
    }

    /**
     * Centers the plot coordinates at the plot value that is at the specified
     * screen position
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     */
    public void center(float xScreen, float yScreen) {
        float[] valuePlotPos = getPlotPosAt(xScreen, yScreen);
        float[] newPlotPos = new float[] { dim[0] / 2, -dim[1] / 2 };

        shiftPlotPos(valuePlotPos, newPlotPos);
    }

    /**
     * Initializes the histograms in all the plot layers
     * 
     * @param histType
     *            the type of histogram to use. It can be GPlot.VERTICAL or
     *            GPlot.HORIZONTAL
     */
    public void startHistograms(int histType) {
        mainLayer.startHistogram(histType);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).startHistogram(histType);
        }
    }

    /**
     * Draws the plot on the screen with default parameters
     */
    public void defaultDraw() {
        beginDraw();
        drawBackground();
        drawBox();
        drawXAxis();
        drawYAxis();
        drawTitle();
        drawLines();
        drawPoints();
        endDraw();
    }

    /**
     * Prepares the environment to start drawing the different plot components
     * (points, axes, title, etc). Use endDraw() to return the sketch to its
     * original state
     */
    public void beginDraw() {
        parent.pushStyle();
        parent.pushMatrix();
        parent.translate(pos[0] + mar[1], pos[1] + mar[2] + dim[1]);
    }

    /**
     * Returns the sketch to the state that it had before calling beginDraw()
     */
    public void endDraw() {
        parent.popMatrix();
        parent.popStyle();
    }

    /**
     * Draws the plot background. This includes the box area and the margins
     */
    public void drawBackground() {
        parent.pushStyle();
        parent.fill(bgColor);
        parent.noStroke();
        parent.rect(-mar[1], -mar[2] - dim[1], outerDim[0], outerDim[1]);
        parent.popStyle();
    }

    /**
     * Draws the box area. This doesn't include the plot margins
     */
    public void drawBox() {
        parent.pushStyle();
        parent.fill(boxBgColor);
        parent.stroke(boxLineColor);
        parent.strokeWeight(boxLineWidth);
        parent.strokeCap(SQUARE);
        parent.rect(0, -dim[1], dim[0], dim[1]);
        parent.popStyle();
    }

    /**
     * Draws the x axis
     */
    public void drawXAxis() {
        xAxis.draw();
    }

    /**
     * Draws the top axis
     */
    public void drawTopAxis() {
        topAxis.draw();
    }

    /**
     * Draws the y axis
     */
    public void drawYAxis() {
        yAxis.draw();
    }

    /**
     * Draws the right axis
     */
    public void drawRightAxis() {
        rightAxis.draw();
    }

    /**
     * Draws the title
     */
    public void drawTitle() {
        title.draw();
    }

    /**
     * Draws the points from all layers in the plot
     */
    public void drawPoints() {
        mainLayer.drawPoints();

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawPoints();
        }
    }

    /**
     * Draws the points from all layers in the plot
     * 
     * @param pointShape
     *            the shape that should be used to represent the points
     */
    public void drawPoints(PShape pointShape) {
        mainLayer.drawPoints(pointShape);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawPoints(pointShape);
        }
    }

    /**
     * Draws a point in the plot
     * 
     * @param point
     *            the point to draw
     * @param pointColor
     *            color to use
     * @param pointSize
     *            point size in pixels
     */
    public void drawPoint(GPoint point, int pointColor, float pointSize) {
        mainLayer.drawPoint(point, pointColor, pointSize);
    }

    /**
     * Draws a point in the plot
     * 
     * @param point
     *            the point to draw
     */
    public void drawPoint(GPoint point) {
        mainLayer.drawPoint(point);
    }

    /**
     * Draws a point in the plot
     * 
     * @param point
     *            the point to draw
     * @param pointShape
     *            the shape that should be used to represent the point
     */
    public void drawPoint(GPoint point, PShape pointShape) {
        mainLayer.drawPoint(point, pointShape);
    }

    /**
     * Draws a point in the plot
     * 
     * @param point
     *            the point to draw
     * @param pointShape
     *            the shape that should be used to represent the points
     * @param pointColor
     *            color to use
     */
    public void drawPoint(GPoint point, PShape pointShape, int pointColor) {
        mainLayer.drawPoint(point, pointShape, pointColor);
    }

    /**
     * Draws lines connecting the points from all layers in the plot
     */
    public void drawLines() {
        mainLayer.drawLines();

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawLines();
        }
    }

    /**
     * Draws a line in the plot, defined by two extreme points
     * 
     * @param point1
     *            first point
     * @param point2
     *            second point
     * @param lineColor
     *            line color
     * @param lineWidth
     *            line width
     */
    public void drawLine(GPoint point1, GPoint point2, int lineColor, float lineWidth) {
        mainLayer.drawLine(point1, point2, lineColor, lineWidth);
    }

    /**
     * Draws a line in the plot, defined by two extreme points
     * 
     * @param point1
     *            first point
     * @param point2
     *            second point
     */
    public void drawLine(GPoint point1, GPoint point2) {
        mainLayer.drawLine(point1, point2);
    }

    /**
     * Draws a line in the plot, defined by the slope and the cut in the y axis
     * 
     * @param slope
     *            the line slope
     * @param yCut
     *            the line y axis cut
     * @param lineColor
     *            line color
     * @param lineWidth
     *            line width
     */
    public void drawLine(float slope, float yCut, int lineColor, float lineWidth) {
        mainLayer.drawLine(slope, yCut, lineColor, lineWidth);
    }

    /**
     * Draws a line in the plot, defined by the slope and the cut in the y axis
     * 
     * @param slope
     *            the line slope
     * @param yCut
     *            the line y axis cut
     */
    public void drawLine(float slope, float yCut) {
        mainLayer.drawLine(slope, yCut);
    }

    /**
     * Draws an horizontal line in the plot
     * 
     * @param value
     *            line horizontal value
     * @param lineColor
     *            line color
     * @param lineWidth
     *            line width
     */
    public void drawHorizontalLine(float value, int lineColor, float lineWidth) {
        mainLayer.drawHorizontalLine(value, lineColor, lineWidth);
    }

    /**
     * Draws an horizontal line in the plot
     * 
     * @param value
     *            line horizontal value
     */
    public void drawHorizontalLine(float value) {
        mainLayer.drawHorizontalLine(value);
    }

    /**
     * Draws a vertical line in the plot
     * 
     * @param value
     *            line vertical value
     * @param lineColor
     *            line color
     * @param lineWidth
     *            line width
     */
    public void drawVerticalLine(float value, int lineColor, float lineWidth) {
        mainLayer.drawVerticalLine(value, lineColor, lineWidth);
    }

    /**
     * Draws a vertical line in the plot
     * 
     * @param value
     *            line vertical value
     */
    public void drawVerticalLine(float value) {
        mainLayer.drawVerticalLine(value);
    }

    /**
     * Draws filled contours connecting the points from all layers in the plot
     * and a reference value
     * 
     * @param contourType
     *            the type of contours to use. It can be GPlot.VERTICAL or
     *            GPlot.HORIZONTAL
     * @param referenceValue
     *            the reference value to use to close the contour
     */
    public void drawFilledContours(int contourType, float referenceValue) {
        mainLayer.drawFilledContour(contourType, referenceValue);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawFilledContour(contourType, referenceValue);
        }
    }

    /**
     * Draws the label of a given point
     * 
     * @param point
     *            the point
     */
    public void drawLabel(GPoint point) {
        mainLayer.drawLabel(point);
    }

    /**
     * Draws the labels of the points in the layers that are close to a given
     * screen position
     * 
     * @param xScreen
     *            x screen position in the parent Processing applet
     * @param yScreen
     *            y screen position in the parent Processing applet
     */
    public void drawLabelsAt(float xScreen, float yScreen) {
        float[] plotPos = getPlotPosAt(xScreen, yScreen);
        mainLayer.drawLabelAtPlotPos(plotPos[0], plotPos[1]);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawLabelAtPlotPos(plotPos[0], plotPos[1]);
        }
    }

    /**
     * Draws lines connecting the horizontal and vertical axis ticks
     * 
     * @param gridType
     *            the type of grid to use. It could be GPlot.HORIZONTAL,
     *            GPlot.VERTICAL or GPlot.BOTH
     */
    public void drawGridLines(int gridType) {
        parent.pushStyle();
        parent.noFill();
        parent.stroke(gridLineColor);
        parent.strokeWeight(gridLineWidth);
        parent.strokeCap(SQUARE);

        if (gridType == BOTH || gridType == VERTICAL) {
            float[] xPlotTicks = xAxis.getPlotTicksRef();

            for (int i = 0; i < xPlotTicks.length; i++) {
                if (xPlotTicks[i] >= 0 && xPlotTicks[i] <= dim[0]) {
                    parent.line(xPlotTicks[i], 0, xPlotTicks[i], -dim[1]);
                }
            }
        }

        if (gridType == BOTH || gridType == HORIZONTAL) {
            float[] yPlotTicks = yAxis.getPlotTicksRef();

            for (int i = 0; i < yPlotTicks.length; i++) {
                if (-yPlotTicks[i] >= 0 && -yPlotTicks[i] <= dim[1]) {
                    parent.line(0, yPlotTicks[i], dim[0], yPlotTicks[i]);
                }
            }
        }

        parent.popStyle();
    }

    /**
     * Draws the histograms of all layers
     */
    public void drawHistograms() {
        mainLayer.drawHistogram();

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawHistogram();
        }
    }

    /**
     * Draws a polygon defined by a set of points
     * 
     * @param polygonPoints
     *            the points that define the polygon
     * @param polygonColor
     *            the color to use to draw the polygon (contour and background)
     */
    public void drawPolygon(GPointsArray polygonPoints, int polygonColor) {
        mainLayer.drawPolygon(polygonPoints, polygonColor);
    }

    /**
     * Draws an annotation at a given plot value
     * 
     * @param text
     *            the annotation text
     * @param x
     *            x plot value
     * @param y
     *            y plot value
     * @param horAlign
     *            text horizontal alignment. It can be RIGHT, LEFT or CENTER
     * @param verAlign
     *            text vertical alignment. It can be TOP, BOTTOM or CENTER
     */
    public void drawAnnotation(String text, float x, float y, int horAlign, int verAlign) {
        mainLayer.drawAnnotation(text, x, y, horAlign, verAlign);
    }

    /**
     * Draws a legend at the specified relative position
     * 
     * @param text
     *            the text to use for each layer in the plot
     * @param xRelativePos
     *            the plot x relative position for each layer in the plot
     * @param yRelativePos
     *            the plot y relative position for each layer in the plot
     */
    public void drawLegend(String[] text, float[] xRelativePos, float[] yRelativePos) {
        if (text != null && xRelativePos != null && yRelativePos != null && text.length == xRelativePos.length
                && xRelativePos.length == yRelativePos.length) {
            parent.pushStyle();
            parent.rectMode(CENTER);
            parent.noStroke();

            for (int i = 0; i < text.length; i++) {
                float[] plotPosition = new float[] { xRelativePos[i] * dim[0], -yRelativePos[i] * dim[1] };
                float[] position = mainLayer.plotToValue(plotPosition[0], plotPosition[1]);

                if (i == 0) {
                    parent.fill(mainLayer.getLineColor());
                    parent.rect(plotPosition[0] - 15, plotPosition[1], 14, 14);
                    mainLayer.drawAnnotation(text[i], position[0], position[1], LEFT, CENTER);
                } else {
                    parent.fill(layerList.get(i - 1).getLineColor());
                    parent.rect(plotPosition[0] - 15, plotPosition[1], 14, 14);
                    layerList.get(i - i).drawAnnotation(text[i], position[0], position[1], LEFT, CENTER);
                }
            }

            parent.popStyle();
        }

    }

    /**
     * Sets the plot position
     * 
     * @param newPos
     *            the new plot position
     */
    public void setPos(float[] newPos) {
        if (newPos != null && newPos.length == 2) {
            pos = newPos.clone();
        }
    }

    /**
     * Sets the plot position
     * 
     * @param x
     *            the new plot x position on the screen
     * @param y
     *            the new plot y position on the screen
     */
    public void setPos(float x, float y) {
        pos = new float[] { x, y };
    }

    /**
     * Sets the plot outer dimensions
     * 
     * @param newOuterDim
     *            the new plot outer dimensions
     */
    public void setOuterDim(float[] newOuterDim) {
        if (newOuterDim != null && newOuterDim.length == 2 && newOuterDim[0] > 0 && newOuterDim[1] > 0) {
            // Make sure that the new plot dimensions are positive
            float[] newDim = new float[] { newOuterDim[0] - mar[1] - mar[3], newOuterDim[1] - mar[0] - mar[2] };

            if (newDim[0] > 0 && newDim[1] > 0) {
                outerDim = newOuterDim.clone();
                dim = newDim;
                xAxis.setDim(dim);
                topAxis.setDim(dim);
                yAxis.setDim(dim);
                rightAxis.setDim(dim);
                title.setDim(dim);

                // Update the layers
                mainLayer.setDim(dim);

                for (int i = 0; i < layerList.size(); i++) {
                    layerList.get(i).setDim(dim);
                }
            }
        }
    }

    /**
     * Sets the plot outer dimensions
     * 
     * @param xOuterDim
     *            the new plot x outer dimension
     * @param yOuterDim
     *            the new plot y outer dimension
     */
    public void setOuterDim(float xOuterDim, float yOuterDim) {
        setOuterDim(new float[] { xOuterDim, yOuterDim });
    }

    /**
     * Sets the plot margins
     * 
     * @param newMar
     *            the new plot margins
     */
    public void setMar(float[] newMar) {
        if (newMar != null && newMar.length == 4) {
            // Make sure that the new outer dimensions are positive
            float[] newOuterDim = new float[] { dim[0] + newMar[1] + newMar[3], dim[1] + newMar[0] + newMar[2] };

            if (newOuterDim[0] > 0 && newOuterDim[1] > 0) {
                mar = newMar.clone();
                outerDim = newOuterDim;
            }
        }
    }

    /**
     * Sets the plot box dimensions
     * 
     * @param newDim
     *            the new plot box dimensions
     */
    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            // Make sure that the new outer dimensions are positive
            float[] newOuterDim = new float[] { newDim[0] + mar[1] + mar[3], newDim[1] + mar[0] + mar[2] };

            if (newOuterDim[0] > 0 && newOuterDim[1] > 0) {
                outerDim = newOuterDim;
                dim = newDim.clone();
                xAxis.setDim(dim);
                topAxis.setDim(dim);
                yAxis.setDim(dim);
                rightAxis.setDim(dim);
                title.setDim(dim);

                // Update the layers
                mainLayer.setDim(dim);

                for (int i = 0; i < layerList.size(); i++) {
                    layerList.get(i).setDim(dim);
                }
            }
        }
    }

    /**
     * Sets the plot box dimensions
     * 
     * @param xDim
     *            the new plot box x dimension
     * @param yDim
     *            the new plot box y dimension
     */
    public void setDim(float xDim, float yDim) {
        setDim(new float[] { xDim, yDim });
    }

    /**
     * Sets the horizontal axes limits
     * 
     * @param newXLim
     *            the new horizontal axes limits
     */
    public void setXLim(float[] newXLim) {
        if (newXLim != null && newXLim.length == 2 && newXLim[1] != newXLim[0]) {
            // Make sure the new limits makes sense
            if (xLog && (newXLim[0] <= 0 || newXLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                xLim = newXLim.clone();
                invertedXScale = xLim[0] > xLim[1];

                // Fix the limits
                fixedXLim = true;

                // Update the axes
                xAxis.setLim(xLim);
                topAxis.setLim(xLim);

                if (!fixedYLim) {
                    yLim = obtainYLim(mainLayer.getPointsRef());
                    yAxis.setLim(yLim);
                    rightAxis.setLim(yLim);
                }

                // Update the layers
                mainLayer.setLimits(xLim, yLim);

                for (int i = 0; i < layerList.size(); i++) {
                    layerList.get(i).setLimits(xLim, yLim);
                }
            }
        }
    }

    /**
     * Sets the vertical axes limits
     * 
     * @param newYLim
     *            the new vertical axes limits
     */
    public void setYLim(float[] newYLim) {
        if (newYLim != null && newYLim.length == 2 && newYLim[1] != newYLim[0]) {
            // Make sure the new limits makes sense
            if (yLog && (newYLim[0] <= 0 || newYLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                yLim = newYLim.clone();
                invertedYScale = yLim[0] > yLim[1];

                // Fix the limits
                fixedYLim = true;

                // Update the axes
                yAxis.setLim(yLim);
                rightAxis.setLim(yLim);

                if (!fixedXLim) {
                    xLim = obtainXLim(mainLayer.getPointsRef());
                    xAxis.setLim(xLim);
                    topAxis.setLim(xLim);
                }

                // Update the layers
                mainLayer.setLimits(xLim, yLim);

                for (int i = 0; i < layerList.size(); i++) {
                    layerList.get(i).setLimits(xLim, yLim);
                }
            }
        }
    }

    /**
     * Sets if the horizontal axes limits are fixed or not
     * 
     * @param newFixedXLim
     *            the fixed condition for the horizontal axes
     */
    public void setFixedXLim(boolean newFixedXLim) {
        fixedXLim = newFixedXLim;
    }

    /**
     * Sets if the vertical axes limits are fixed or not
     * 
     * @param newFixedYLim
     *            the fixed condition for the vertical axes
     */
    public void setFixedYLim(boolean newFixedYLim) {
        fixedYLim = newFixedYLim;
    }

    /**
     * Sets if the scale for the horizontal and vertical axes is logarithmic or
     * not
     * 
     * @param logType
     *            the type of scale for the horizontal and vertical axes
     */
    public void setLogScale(String logType) {
        boolean newXLog = xLog;
        boolean newYLog = yLog;

        if (logType.equals("xy") || logType.equals("yx")) {
            newXLog = true;
            newYLog = true;
        } else if (logType.equals("x")) {
            newXLog = true;
            newYLog = false;
        } else if (logType.equals("y")) {
            newXLog = false;
            newYLog = true;
        } else if (logType.equals("")) {
            newXLog = false;
            newYLog = false;
        }

        // Do something only if the scale changed
        if (newXLog != xLog || newYLog != yLog) {
            // Set the new log scales
            xLog = newXLog;
            yLog = newYLog;

            // Unfix the limits if the old ones don't make sense
            if (xLog && fixedXLim && (xLim[0] <= 0 || xLim[1] <= 0)) {
                fixedXLim = false;
            }

            if (yLog && fixedYLim && (yLim[0] <= 0 || yLim[1] <= 0)) {
                fixedYLim = false;
            }

            // Calculate the new limits if needed
            if (!fixedXLim) {
                xLim = obtainXLim(mainLayer.getPointsRef());
            }

            if (!fixedYLim) {
                yLim = obtainYLim(mainLayer.getPointsRef());
            }

            // Update the axes
            xAxis.setLimAndLog(xLim, xLog);
            topAxis.setLimAndLog(xLim, xLog);
            yAxis.setLimAndLog(yLim, yLog);
            rightAxis.setLimAndLog(yLim, yLog);

            // Update the layers
            mainLayer.setLimAndLog(xLim, yLim, xLog, yLog);

            for (int i = 0; i < layerList.size(); i++) {
                layerList.get(i).setLimAndLog(xLim, yLim, xLog, yLog);
            }
        }
    }

    /**
     * Sets if the scale of the horizontal axes should be inverted or not
     * 
     * @param newInvertedXScale
     *            true if the horizontal scale should be inverted
     */
    public void setInvertedXScale(boolean newInvertedXScale) {
        if (newInvertedXScale != invertedXScale) {
            invertedXScale = newInvertedXScale;
            xLim = new float[] { xLim[1], xLim[0] };

            // Update the axes
            xAxis.setLim(xLim);
            topAxis.setLim(xLim);

            // Update the layers
            mainLayer.setXLim(xLim);

            for (int i = 0; i < layerList.size(); i++) {
                layerList.get(i).setXLim(xLim);
            }
        }
    }

    /**
     * Sets if the scale of the vertical axes should be inverted or not
     * 
     * @param newInvertedYScale
     *            true if the vertical scale should be inverted
     */
    public void setInvertedYScale(boolean newInvertedYScale) {
        if (newInvertedYScale != invertedYScale) {
            invertedYScale = newInvertedYScale;
            yLim = new float[] { yLim[1], yLim[0] };

            // Update the axes
            yAxis.setLim(yLim);
            rightAxis.setLim(yLim);

            // Update the layers
            mainLayer.setYLim(yLim);

            for (int i = 0; i < layerList.size(); i++) {
                layerList.get(i).setYLim(yLim);
            }
        }
    }

    /**
     * Sets the plot background color
     * 
     * @param newBgColor
     *            the new plot background color
     */
    public void setBgColor(int newBgColor) {
        bgColor = newBgColor;
    }

    /**
     * Sets the box background color
     * 
     * @param newBoxBgColor
     *            the new box background color
     */
    public void setBoxBgColor(int newBoxBgColor) {
        boxBgColor = newBoxBgColor;
    }

    /**
     * Sets the box line color
     * 
     * @param newBoxLineColor
     *            the new box background color
     */
    public void setBoxLineColor(int newBoxLineColor) {
        boxLineColor = newBoxLineColor;
    }

    /**
     * Sets the box line width
     * 
     * @param newBoxLineWidth
     *            the new box line width
     */
    public void setBoxLineWidth(float newBoxLineWidth) {
        if (newBoxLineWidth > 0) {
            boxLineWidth = newBoxLineWidth;
        }
    }

    /**
     * Sets the grid line color
     * 
     * @param newGridLineColor
     *            the new grid line color
     */
    public void setGridLineColor(int newGridLineColor) {
        gridLineColor = newGridLineColor;
    }

    /**
     * Sets the grid line width
     * 
     * @param newGridLineWidth
     *            the new grid line width
     */
    public void setGridLineWidth(float newGridLineWidth) {
        if (newGridLineWidth > 0) {
            gridLineWidth = newGridLineWidth;
        }
    }

    /**
     * Sets the points for the main layer
     * 
     * @param points
     *            the new points for the main layer
     */
    public void setPoints(GPointsArray points) {
        if (points != null) {
            // Calculate the new limits and update the axes if needed
            if (!fixedXLim) {
                xLim = obtainXLim(points);
                xAxis.setLim(xLim);
                topAxis.setLim(xLim);
            }

            if (!fixedYLim) {
                yLim = obtainYLim(points);
                yAxis.setLim(yLim);
                rightAxis.setLim(yLim);
            }

            // Update the layers
            mainLayer.setLimits(xLim, yLim);

            for (int i = 0; i < layerList.size(); i++) {
                layerList.get(i).setLimits(xLim, yLim);
            }

            // Add the points to the main layer
            mainLayer.setPoints(points);
        }
    }

    /**
     * Sets the point colors for the main layer
     * 
     * @param pointColors
     *            the point colors for the main layer
     */
    public void setPointColors(int[] pointColors) {
        mainLayer.setPointColors(pointColors);
    }

    /**
     * Sets the point sizes for the main layer
     * 
     * @param pointSizes
     *            the point sizes for the main layer
     */
    public void setPointSizes(float[] pointSizes) {
        mainLayer.setPointSizes(pointSizes);
    }

    /**
     * Sets the line color for the main layer
     * 
     * @param lineColor
     *            the line color for the main layer
     */
    public void setLineColor(int lineColor) {
        mainLayer.setLineColor(lineColor);
    }

    /**
     * Sets the line width for the main layer
     * 
     * @param lineWidth
     *            the line with for the main layer
     */
    public void setLineWidth(float lineWidth) {
        mainLayer.setLineWidth(lineWidth);
    }

    /**
     * Sets the base point for the histogram in the main layer
     * 
     * @param basePoint
     *            the base point for the histogram in the main layer
     */
    public void setHistBasePoint(GPoint basePoint) {
        mainLayer.setHistBasePoint(basePoint);
    }

    /**
     * Sets the histogram type for the histogram in the main layer
     * 
     * @param histType
     *            the histogram type for the histogram in the main layer. It can
     *            be GPlot.HORIZONTAL or GPlot.VERTICAL
     */
    public void setHistType(int histType) {
        mainLayer.setHistType(histType);
    }

    /**
     * Sets if the histogram in the main layer is visible or not
     * 
     * @param visible
     *            if true, the histogram is visible
     */
    public void setHistVisible(boolean visible) {
        mainLayer.setHistVisible(visible);
    }

    /**
     * Sets if the labels of the histogram in the main layer will be drawn or
     * not
     * 
     * @param drawHistLabels
     *            if true, the histogram labels will be drawn
     */
    public void setDrawHistLabels(boolean drawHistLabels) {
        mainLayer.setDrawHistLabels(drawHistLabels);
    }

    /**
     * Sets the label background color of the points in the main layer
     * 
     * @param labelBgColor
     *            the label background color of the points in the main layer
     */
    public void setLabelBgColor(int labelBgColor) {
        mainLayer.setLabelBgColor(labelBgColor);
    }

    /**
     * Sets the label separation of the points in the main layer
     * 
     * @param labelSeparation
     *            the label separation of the points in the main layer
     */
    public void setLabelSeparation(float[] labelSeparation) {
        mainLayer.setLabelSeparation(labelSeparation);
    }

    /**
     * Set the plot title text
     * 
     * @param text
     *            the plot title text
     */
    public void setTitleText(String text) {
        title.setText(text);
    }

    /**
     * Sets the axis offset for all the axes in the plot
     * 
     * @param offset
     *            the new axis offset
     */
    public void setAxesOffset(float offset) {
        xAxis.setOffset(offset);
        topAxis.setOffset(offset);
        yAxis.setOffset(offset);
        rightAxis.setOffset(offset);
    }

    /**
     * Sets the tick length for all the axes in the plot
     * 
     * @param tickLength
     *            the new tick length
     */
    public void setTicksLength(float tickLength) {
        xAxis.setTickLength(tickLength);
        topAxis.setTickLength(tickLength);
        yAxis.setTickLength(tickLength);
        rightAxis.setTickLength(tickLength);
    }

    /**
     * Sets the name of the font that is used in the main layer
     * 
     * @param fontName
     *            the name of the font that will be used in the main layer
     */
    public void setFontName(String fontName) {
        mainLayer.setFontName(fontName);
    }

    /**
     * Sets the color of the font that is used in the main layer
     * 
     * @param fontColor
     *            the color of the font that will be used in the main layer
     */
    public void setFontColor(int fontColor) {
        mainLayer.setFontColor(fontColor);
    }

    /**
     * Sets the size of the font that is used in the main layer
     * 
     * @param fontSize
     *            the size of the font that will be used in the main layer
     */
    public void setFontSize(int fontSize) {
        mainLayer.setFontSize(fontSize);
    }

    /**
     * Sets the properties of the font that is used in the main layer
     * 
     * @param fontName
     *            the name of the font that will be used in the main layer
     * @param fontColor
     *            the color of the font that will be used in the main layer
     * @param fontSize
     *            the size of the font that will be used in the main layer
     */
    public void setFontProperties(String fontName, int fontColor, int fontSize) {
        mainLayer.setFontProperties(fontName, fontColor, fontSize);
    }

    /**
     * Sets the properties of the font that will be used in all plot elements
     * (layer, axes, title, histogram)
     * 
     * @param fontName
     *            the name of the font that will be used in all plot elements
     * @param fontColor
     *            the color of the font that will be used in all plot elements
     * @param fontSize
     *            the size of the font that will be used in all plot elements
     */
    public void setAllFontProperties(String fontName, int fontColor, int fontSize) {
        xAxis.setAllFontProperties(fontName, fontColor, fontSize);
        topAxis.setAllFontProperties(fontName, fontColor, fontSize);
        yAxis.setAllFontProperties(fontName, fontColor, fontSize);
        rightAxis.setAllFontProperties(fontName, fontColor, fontSize);
        title.setFontProperties(fontName, fontColor, fontSize);

        mainLayer.setAllFontProperties(fontName, fontColor, fontSize);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).setAllFontProperties(fontName, fontColor, fontSize);
        }
    }

    /**
     * Returns the plot position
     * 
     * @return the plot position
     */
    public float[] getPos() {
        return pos.clone();
    }

    /**
     * Returns the plot outer dimensions
     * 
     * @return the plot outer dimensions
     */
    public float[] getOuterDim() {
        return outerDim.clone();
    }

    /**
     * Returns the plot margins
     * 
     * @return the plot margins
     */
    public float[] getMar() {
        return mar.clone();
    }

    /**
     * Returns the box dimensions
     * 
     * @return the box dimensions
     */
    public float[] getDim() {
        return dim.clone();
    }

    /**
     * Returns the limits of the horizontal axes
     * 
     * @return the limits of the horizontal axes
     */
    public float[] getXLim() {
        return xLim.clone();
    }

    /**
     * Returns the limits of the vertical axes
     * 
     * @return the limits of the vertical axes
     */
    public float[] getYLim() {
        return yLim.clone();
    }

    /**
     * Returns true if the horizontal axes limits are fixed
     * 
     * @return true, if the horizontal axes limits are fixed
     */
    public boolean getFixedXLim() {
        return fixedXLim;
    }

    /**
     * Returns true if the vertical axes limits are fixed
     * 
     * @return true, if the vertical axes limits are fixed
     */
    public boolean getFixedYLim() {
        return fixedYLim;
    }

    /**
     * Returns true if the horizontal axes scale is logarithmic
     * 
     * @return true, if the horizontal axes scale is logarithmic
     */
    public boolean getXLog() {
        return xLog;
    }

    /**
     * Returns true if the vertical axes scale is logarithmic
     * 
     * @return true, if the vertical axes scale is logarithmic
     */
    public boolean getYLog() {
        return yLog;
    }

    /**
     * Returns the plot main layer
     * 
     * @return the plot main layer
     */
    public GLayer getMainLayer() {
        return mainLayer;
    }

    /**
     * Returns a layer with an specific id
     * 
     * @param id
     *            the id of the layer to return
     * 
     * @return the layer with the specified id
     */
    public GLayer getLayer(String id) {
        int index = -1;

        for (int i = 0; i < layerList.size(); i++) {
            if (layerList.get(i).isId(id)) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            return layerList.get(index);
        } else {
            PApplet.println("Couldn't find a layer in the plot with id = " + id);
            return null;
        }
    }

    /**
     * Returns the plot x axis
     * 
     * @return the plot x axis
     */
    public GAxis getXAxis() {
        return xAxis;
    }

    /**
     * Returns the plot top axis
     * 
     * @return the plot top axis
     */
    public GAxis getTopAxis() {
        return topAxis;
    }

    /**
     * Returns the plot y axis
     * 
     * @return the plot y axis
     */
    public GAxis getYAxis() {
        return yAxis;
    }

    /**
     * Returns the plot right axis
     * 
     * @return the plot right axis
     */
    public GAxis getRightAxis() {
        return rightAxis;
    }

    /**
     * Returns the plot title
     * 
     * @return the plot title
     */
    public GTitle getTitle() {
        return title;
    }

    /**
     * Returns a copy of the points of the main layer
     * 
     * @return a copy of the points of the main layer
     */
    public GPointsArray getPoints() {
        return mainLayer.getPoints();
    }

    /**
     * Returns the points of the main layer
     * 
     * @return the points of the main layer
     */
    public GPointsArray getPointsRef() {
        return mainLayer.getPointsRef();
    }

    /**
     * Returns the histogram of the main layer
     * 
     * @return the histogram of the main layer
     */
    public GHistogram getHistogram() {
        return mainLayer.getHistogram();
    }
}
