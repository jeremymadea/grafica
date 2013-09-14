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
 * Main class that controls the rest of the graphical elements (layers, axes,
 * title, limits).
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
        mar = new float[] { 60, 90, 40, 20 };
        dim = new float[] { outerDim[0] - mar[1] - mar[3], outerDim[1] - mar[0] - mar[2] };
        xLim = new float[] { 0, 1 };
        yLim = new float[] { 0, 1 };
        fixedXLim = false;
        fixedYLim = false;
        xLog = false;
        yLog = false;

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
                newLayer.setLimitsAndLog(xLim, yLim, xLog, yLog);
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
     * Shifts the plot coordinates in a way that after that the given plot
     * physical value will be at the specified screen position
     * 
     * @param value
     *            the x and y plot physical value
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
     * Centers the plot coordinates at the plot value at the specified screen
     * position
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
     *            the type of histogram to use. It can be GHistogram.VERTICAL or GHistogram.HORIZONTAL
     */
    public void startHistograms(int histType) {
        mainLayer.startHistogram(histType);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).startHistogram(histType);
        }
    }

    public void defaultDraw() {
        beginDraw();
        drawBackground();
        drawInnerRegion();
        drawXAxis();
        drawYAxis();
        drawTitle();
        drawLines();
        drawPoints();
        endDraw();
    }

    public void beginDraw() {
        parent.pushMatrix();
        parent.translate(pos[0] + mar[1], pos[1] + mar[2] + dim[1]);
    }

    public void endDraw() {
        parent.popMatrix();
    }

    public void drawBackground() {
        parent.pushStyle();
        parent.fill(bgColor);
        parent.noStroke();
        parent.rect(-mar[1], -mar[2] - dim[1], outerDim[0], outerDim[1]);
        parent.popStyle();
    }

    public void drawInnerRegion() {
        parent.pushStyle();
        parent.fill(boxBgColor);
        parent.stroke(boxLineColor);
        parent.strokeWeight(boxLineWidth);
        parent.strokeCap(SQUARE);
        parent.rect(0, -dim[1], dim[0], dim[1]);
        parent.popStyle();
    }

    public void drawXAxis() {
        xAxis.draw();
    }

    public void drawYAxis() {
        yAxis.draw();
    }

    public void drawTopAxis() {
        topAxis.draw();
    }

    public void drawRightAxis() {
        rightAxis.draw();
    }

    public void drawTitle() {
        title.draw();
    }

    public void drawPoints() {
        mainLayer.drawPoints();

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawPoints();
        }
    }

    public void drawPoints(PShape s) {
        mainLayer.drawPoints(s);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawPoints(s);
        }
    }

    public void drawPoint(GPoint p, int col, float size) {
        mainLayer.drawPoint(p, col, size);
    }

    public void drawPoint(GPoint p) {
        mainLayer.drawPoint(p);
    }

    public void drawPoint(GPoint p, PShape s) {
        mainLayer.drawPoint(p, s);
    }

    public void drawPoint(GPoint p, PShape s, int col) {
        mainLayer.drawPoint(p, s, col);
    }

    public void drawLines() {
        mainLayer.drawLines();

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawLines();
        }
    }

    public void drawLine(GPoint p1, GPoint p2, int col, float lw) {
        mainLayer.drawLine(p1, p2, col, lw);
    }

    public void drawLine(GPoint p1, GPoint p2) {
        mainLayer.drawLine(p1, p2);
    }

    public void drawHorizontalLine(float val, int col, float lw) {
        mainLayer.drawHorizontalLine(val, col, lw);
    }

    public void drawHorizontalLine(float val) {
        mainLayer.drawHorizontalLine(val);
    }

    public void drawVerticalLine(float val, int col, float lw) {
        mainLayer.drawVerticalLine(val, col, lw);
    }

    public void drawVerticalLine(float val) {
        mainLayer.drawVerticalLine(val);
    }

    public void drawInclinedLine(float slope, float xCut, int col, float lw) {
        mainLayer.drawInclinedLine(slope, xCut, col, lw);
    }

    public void drawInclinedLine(float slope, float xCut) {
        mainLayer.drawInclinedLine(slope, xCut);
    }

    public void drawFilledLines(String type, float referenceValue) {
        mainLayer.drawFilledLines(type, referenceValue);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawFilledLines(type, referenceValue);
        }
    }

    public void drawLabel(GPoint p) {
        mainLayer.drawLabel(p);
    }

    public void drawLabelsAt(float x, float y) {
        float[] screenPos = getPlotPosAt(x, y);
        mainLayer.drawLabelAtScreenPos(screenPos[0], screenPos[1]);

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawLabelAtScreenPos(screenPos[0], screenPos[1]);
        }
    }

    public void drawGridLines(String type) {
        parent.pushStyle();
        parent.noFill();
        parent.stroke(gridLineColor);
        parent.strokeWeight(gridLineWidth);
        parent.strokeCap(SQUARE);

        if (type.equals("both") || type.equals("vertical")) {
            float[] xScreenTicks = xAxis.getScreenTicksRef();

            for (int i = 0; i < xScreenTicks.length; i++) {
                if (xScreenTicks[i] >= 0 && xScreenTicks[i] <= dim[0]) {
                    parent.line(xScreenTicks[i], 0, xScreenTicks[i], -dim[1]);
                }
            }
        }

        if (type.equals("both") || type.equals("horizontal")) {
            float[] yScreenTicks = yAxis.getScreenTicksRef();

            for (int i = 0; i < yScreenTicks.length; i++) {
                if (-yScreenTicks[i] >= 0 && -yScreenTicks[i] <= dim[1]) {
                    parent.line(0, yScreenTicks[i], dim[0], yScreenTicks[i]);
                }
            }
        }
        parent.popStyle();
    }

    public void drawHistogram() {
        mainLayer.drawHistogram();

        for (int i = 0; i < layerList.size(); i++) {
            layerList.get(i).drawHistogram();
        }
    }

    public void drawPolygon(GPointsArray poly, int col) {
        mainLayer.drawPolygon(poly, col);
    }

    public void drawAnnotation(String text, float x, float y, int horAlign, int verAlign) {
        mainLayer.drawAnnotation(text, x, y, horAlign, verAlign);
    }

    //
    // Setters
    // //////////

    public void setPos(float[] newPos) {
        if (newPos != null && newPos.length == 2) {
            pos = newPos.clone();
        }
    }

    public void setOuterDim(float[] newOuterDim) {
        if (newOuterDim != null && newOuterDim.length == 2 && newOuterDim[0] > 0 && newOuterDim[1] > 0) {
            // Make sure that the new inner dimensions are positive
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

    public void setMar(float[] newMar) {
        if (newMar != null && newMar.length == 4) {
            // Make sure that the new inner dimensions are positive
            float[] newDim = new float[] { outerDim[0] - newMar[1] - newMar[3], outerDim[1] - newMar[0] - newMar[2] };

            if (newDim[0] > 0 && newDim[1] > 0) {
                mar = newMar.clone();
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

    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            // Make sure that the new dimensions are positive
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

    public void setXlim(float[] newXLim) {
        if (newXLim != null && newXLim.length == 2 && newXLim[1] != newXLim[0]) {
            // Make sure the new limit makes sense
            if (xLog && (newXLim[0] <= 0 || newXLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                xLim = newXLim.clone();

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

    public void setYlim(float[] newYLim) {
        if (newYLim != null && newYLim.length == 2 && newYLim[1] != newYLim[0]) {
            // Make sure the new limit makes sense
            if (yLog && (newYLim[0] <= 0 || newYLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                yLim = newYLim.clone();

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

    public void setFixedXLim(boolean newFixedXLim) {
        fixedXLim = newFixedXLim;
    }

    public void setFixedYLim(boolean newFixedYLim) {
        fixedYLim = newFixedYLim;
    }

    public void setLog(String log) {
        boolean newXLog = xLog;
        boolean newYLog = yLog;

        if (log.equals("xy") || log.equals("yx")) {
            newXLog = true;
            newYLog = true;
        } else if (log.equals("x")) {
            newXLog = true;
            newYLog = false;
        } else if (log.equals("y")) {
            newXLog = false;
            newYLog = true;
        } else if (log.equals("")) {
            newXLog = false;
            newYLog = false;
        }

        // Do something only if the scale changed
        if (newXLog != xLog || newYLog != yLog) {
            // Set the new log scales
            xLog = newXLog;
            yLog = newYLog;

            // Unfix the limits if the old ones don't make sense
            if (xLog && fixedXLim && (xLim[0] <= 0 || xLim[1] <= 0))
                fixedXLim = false;
            if (yLog && fixedYLim && (yLim[0] <= 0 || yLim[1] <= 0))
                fixedYLim = false;

            // Calculate the new limmits if needed
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
            mainLayer.setLimitsAndLog(xLim, yLim, xLog, yLog);

            for (int i = 0; i < layerList.size(); i++) {
                layerList.get(i).setLimitsAndLog(xLim, yLim, xLog, yLog);
            }
        }
    }

    public void setBgColor(int newBgColor) {
        bgColor = newBgColor;
    }

    public void setInnerBgColor(int newInnerBgColor) {
        boxBgColor = newInnerBgColor;
    }

    public void setInnerLineColor(int newInnerLineColor) {
        boxLineColor = newInnerLineColor;
    }

    public void setInnerLineWidth(float newInnerLineWidth) {
        if (newInnerLineWidth > 0) {
            boxLineWidth = newInnerLineWidth;
        }
    }

    public void setGridLineColor(int newGridLineColor) {
        gridLineColor = newGridLineColor;
    }

    public void setGridLineWidth(float newGridLineWidth) {
        if (newGridLineWidth > 0) {
            gridLineWidth = newGridLineWidth;
        }
    }

    public void setPoints(GPointsArray points) {
        if (points != null) {
            // Calculate the new limmits and update the axes if needed
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

    public void setPointColors(int[] pointColors) {
        mainLayer.setPointColors(pointColors);
    }

    public void setPointSizes(float[] pointSizes) {
        mainLayer.setPointSizes(pointSizes);
    }

    public void setLineColor(int lineColor) {
        mainLayer.setLineColor(lineColor);
    }

    public void setLineWidth(float lineWidth) {
        mainLayer.setLineWidth(lineWidth);
    }

    public void setHistZeroPoint(GPoint zeroPoint) {
        mainLayer.setHistZeroPoint(zeroPoint);
    }

    public void setHistType(int histType) {
        mainLayer.setHistType(histType);
    }

    public void setHistVisible(boolean visible) {
        mainLayer.setHistVisible(visible);
    }

    public void setDrawHistLabels(boolean drawHistLabels) {
        mainLayer.setDrawHistLabels(drawHistLabels);
    }

    public void setLabelBgColor(int labelBgColor) {
        mainLayer.setLabelBgColor(labelBgColor);
    }

    public void setLabelSeparation(float[] labelSeparation) {
        mainLayer.setLabelSeparation(labelSeparation);
    }

    public void setFontName(String fontName) {
        mainLayer.setFontName(fontName);
    }

    public void setFontColor(int fontColor) {
        mainLayer.setFontColor(fontColor);
    }

    public void setFontSize(int fontSize) {
        mainLayer.setFontSize(fontSize);
    }

    public void setFontProperties(String fontName, int fontColor, int fontSize) {
        mainLayer.setFontProperties(fontName, fontColor, fontSize);
    }

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

    //
    // Getters
    // //////////

    public float[] getPos() {
        return pos.clone();
    }

    public float[] getDim() {
        return outerDim.clone();
    }

    public float[] getMar() {
        return mar.clone();
    }

    public float[] getInnerDim() {
        return dim.clone();
    }

    public float[] getXLim() {
        return xLim.clone();
    }

    public float[] getYLim() {
        return yLim.clone();
    }

    public boolean getFixedXLim() {
        return fixedXLim;
    }

    public boolean getFixedYLim() {
        return fixedYLim;
    }

    public boolean getXLog() {
        return xLog;
    }

    public boolean getYLog() {
        return yLog;
    }

    public GLayer getMainLayer() {
        return mainLayer;
    }

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

    public GAxis getXAxis() {
        return xAxis;
    }

    public GAxis getTopAxis() {
        return topAxis;
    }

    public GAxis getYAxis() {
        return yAxis;
    }

    public GAxis getRightAxis() {
        return rightAxis;
    }

    public GTitle getTitle() {
        return title;
    }

    public GPointsArray getPoints() {
        return mainLayer.getPoints();
    }

    public GPointsArray getPointsRef() {
        return mainLayer.getPointsRef();
    }

    public GPointsArray getScreenPoints() {
        return mainLayer.getScreenPoints();
    }

    public GPointsArray getScreenPointsRef() {
        return mainLayer.getScreenPointsRef();
    }

    public GHistogram getHistogram() {
        return mainLayer.getHistogram();
    }
}
