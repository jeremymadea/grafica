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

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

/**
 * Histogram class.
 * 
 * @author Javier Gracia Carpio
 */
public class GHistogram implements PConstants {
    // The parent Processing applet
    protected final PApplet parent;

    // General properties
    protected int type;
    protected float[] dim;
    protected GPointsArray plotPoints;
    protected float[] separations;
    protected int[] bgColors;
    protected int[] lineColors;
    protected float[] lineWidths;
    protected boolean visible;

    // Arrays
    protected int nHistElements;
    protected float[] histSeparations;
    protected int[] histBgColors;
    protected int[] histLineColors;
    protected float[] histLineWidths;
    protected float[] histLeftSides;
    protected float[] histRightSides;

    // Labels properties
    protected float labelsOffset;
    protected boolean drawLabels;
    protected boolean rotateLabels;
    protected String fontName;
    protected int fontColor;
    protected int fontSize;
    protected PFont font;

    /**
     * Constructor
     * 
     * @param parent
     *            the parent Processing applet
     * @param type
     *            the histogram type. It can be GPlot.VERTICAL or
     *            GPlot.HORIZONTAL
     * @param dim
     *            the plot box dimensions in pixels
     * @param plotPoints
     *            the points positions in the plot reference system
     */
    public GHistogram(PApplet parent, int type, float[] dim, GPointsArray plotPoints) {
        this.parent = parent;

        this.type = (type == GPlot.VERTICAL || type == GPlot.HORIZONTAL) ? type : GPlot.VERTICAL;
        this.dim = dim.clone();
        this.plotPoints = new GPointsArray(plotPoints);
        separations = new float[] { 2 };
        bgColors = new int[] { this.parent.color(150, 150, 255) };
        lineColors = new int[] { this.parent.color(100, 100, 255) };
        lineWidths = new float[] { 1 };
        visible = true;

        nHistElements = this.plotPoints.getNPoints();
        histSeparations = new float[nHistElements];
        histBgColors = new int[nHistElements];
        histLineColors = new int[nHistElements];
        histLineWidths = new float[nHistElements];
        histLeftSides = new float[nHistElements];
        histRightSides = new float[nHistElements];

        // Fill the arrays
        for (int i = 0; i < nHistElements; i++) {
            histSeparations[i] = separations[i % separations.length];
            histBgColors[i] = bgColors[i % bgColors.length];
            histLineColors[i] = lineColors[i % lineColors.length];
            histLineWidths[i] = lineWidths[i % lineWidths.length];
        }

        updateSides();

        // Continue with the rest
        labelsOffset = 8;
        drawLabels = false;
        rotateLabels = false;
        fontName = "SansSerif.plain";
        fontColor = this.parent.color(0);
        fontSize = 11;
        font = this.parent.createFont(fontName, fontSize);
    }

    /**
     * Updates the left and right sides arrays
     */
    protected void updateSides() {
        if (nHistElements == 1) {
            histLeftSides[0] = (type == GPlot.VERTICAL) ? 0.2f * dim[0] : 0.2f * dim[1];
            histRightSides[0] = histLeftSides[0];
        } else if (nHistElements > 1) {
            // Calculate the differences between consecutive points
            float[] differences = new float[nHistElements - 1];

            if (type == GPlot.VERTICAL) {
                for (int i = 0; i < nHistElements - 1; i++) {
                    if (plotPoints.isValid(i) && plotPoints.isValid(i + 1)) {
                        if (plotPoints.getX(i + 1) > plotPoints.getX(i)) {
                            differences[i] = (plotPoints.getX(i + 1) - plotPoints.getX(i) - histSeparations[i]) / 2;
                        } else {
                            differences[i] = (plotPoints.getX(i + 1) - plotPoints.getX(i) + histSeparations[i]) / 2;
                        }
                    }
                }
            } else {
                for (int i = 0; i < nHistElements - 1; i++) {
                    if (plotPoints.isValid(i) && plotPoints.isValid(i + 1)) {
                        if (plotPoints.getY(i + 1) > plotPoints.getY(i)) {
                            differences[i] = (plotPoints.getY(i + 1) - plotPoints.getY(i) - histSeparations[i]) / 2;
                        } else {
                            differences[i] = (plotPoints.getY(i + 1) - plotPoints.getY(i) + histSeparations[i]) / 2;
                        }
                    }
                }
            }

            // Fill the arrays
            histLeftSides[0] = differences[0];
            histRightSides[0] = differences[0];

            for (int i = 1; i < nHistElements - 1; i++) {
                histLeftSides[i] = differences[i - 1];
                histRightSides[i] = differences[i];
            }

            histLeftSides[nHistElements - 1] = differences[nHistElements - 2];
            histRightSides[nHistElements - 1] = differences[nHistElements - 2];
        }
    }

    /**
     * Draws the histogram
     * 
     * @param plotBasePoint
     *            the histogram base point in the plot reference system
     */
    public void draw(GPoint plotBasePoint) {
        if (visible && plotBasePoint != null) {
            // Calculate the baseline for the histogram
            float baseline = 0;

            if (plotBasePoint.isValid()) {
                baseline = (type == GPlot.VERTICAL) ? plotBasePoint.getY() : plotBasePoint.getX();
            }

            // Draw the rectangles
            parent.pushStyle();
            parent.strokeCap(SQUARE);
            parent.rectMode(CORNERS);

            for (int i = 0; i < nHistElements; i++) {
                if (plotPoints.isValid(i)) {
                    // Obtain the corners
                    float x1, x2, y1, y2;

                    if (type == GPlot.VERTICAL) {
                        x1 = plotPoints.getX(i) - histLeftSides[i];
                        x2 = plotPoints.getX(i) + histRightSides[i];
                        y1 = plotPoints.getY(i);
                        y2 = baseline;
                    } else {
                        x1 = baseline;
                        x2 = plotPoints.getX(i);
                        y1 = plotPoints.getY(i) - histLeftSides[i];
                        y2 = plotPoints.getY(i) + histRightSides[i];
                    }

                    if (x1 < 0) {
                        x1 = 0;
                    } else if (x1 > dim[0]) {
                        x1 = dim[0];
                    }

                    if (-y1 < 0) {
                        y1 = 0;
                    } else if (-y1 > dim[1]) {
                        y1 = -dim[1];
                    }

                    if (x2 < 0) {
                        x2 = 0;
                    } else if (x2 > dim[0]) {
                        x2 = dim[0];
                    }

                    if (-y2 < 0) {
                        y2 = 0;
                    } else if (-y2 > dim[1]) {
                        y2 = -dim[1];
                    }

                    // Draw the rectangle
                    parent.fill(histBgColors[i]);
                    parent.stroke(histLineColors[i]);
                    parent.strokeWeight(histLineWidths[i]);

                    if (Math.abs(x2 - x1) > 2 * histLineWidths[i] && Math.abs(y2 - y1) > 2 * histLineWidths[i]) {
                        parent.rect(x1, y1, x2, y2);
                    } else if ((type == GPlot.VERTICAL && x2 != x1 && !(y1 == y2 && (y1 == 0 || y1 == -dim[1])))
                            || (type == GPlot.HORIZONTAL && y2 != y1 && !(x1 == x2 && (x1 == 0 || x1 == dim[0])))) {
                        parent.rect(x1, y1, x2, y2);
                        parent.line(x1, y1, x1, y2);
                        parent.line(x2, y1, x2, y2);
                        parent.line(x1, y1, x2, y1);
                        parent.line(x1, y2, x2, y2);
                    }
                }
            }

            parent.popStyle();

            // Draw the labels
            if (drawLabels) {
                drawHistLabels();
            }
        }
    }

    /**
     * Draws the histogram labels
     */
    protected void drawHistLabels() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.noStroke();

        if (type == GPlot.VERTICAL) {
            if (rotateLabels) {
                parent.textAlign(RIGHT, CENTER);

                for (int i = 0; i < nHistElements; i++) {
                    if (plotPoints.isValid(i) && plotPoints.getX(i) >= 0 && plotPoints.getX(i) <= dim[0]) {
                        parent.pushMatrix();
                        parent.translate(plotPoints.getX(i), labelsOffset);
                        parent.rotate(-HALF_PI);
                        parent.text(plotPoints.getLabel(i), 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(CENTER, TOP);

                for (int i = 0; i < nHistElements; i++) {
                    if (plotPoints.isValid(i) && plotPoints.getX(i) >= 0 && plotPoints.getX(i) <= dim[0]) {
                        parent.text(plotPoints.getLabel(i), plotPoints.getX(i), labelsOffset);
                    }
                }
            }
        } else {
            if (rotateLabels) {
                parent.textAlign(CENTER, BOTTOM);

                for (int i = 0; i < nHistElements; i++) {
                    if (plotPoints.isValid(i) && -plotPoints.getY(i) >= 0 && -plotPoints.getY(i) <= dim[1]) {
                        parent.pushMatrix();
                        parent.translate(-labelsOffset, plotPoints.getY(i));
                        parent.rotate(-HALF_PI);
                        parent.text(plotPoints.getLabel(i), 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(RIGHT, CENTER);

                for (int i = 0; i < nHistElements; i++) {
                    if (plotPoints.isValid(i) && -plotPoints.getY(i) >= 0 && -plotPoints.getY(i) <= dim[1]) {
                        parent.text(plotPoints.getLabel(i), -labelsOffset, plotPoints.getY(i));
                    }
                }
            }
        }

        parent.popStyle();
    }

    /**
     * Sets the type of histogram to display
     * 
     * @param newType
     *            the new type of histogram to display
     */
    public void setType(int newType) {
        if (newType != type && (newType == GPlot.VERTICAL || newType == GPlot.HORIZONTAL)) {
            type = newType;
            updateSides();
        }
    }

    /**
     * Sets the plot box dimensions information
     * 
     * @param newDim
     *            the new plot box dimensions information
     */
    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            dim = newDim.clone();
            updateSides();
        }
    }

    /**
     * Sets the plot box dimensions information
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
     * Sets the point positions on the plot reference system
     * 
     * @param newPlotPoints
     *            the new point positions in the plot reference system
     */
    public void setPlotPoints(GPointsArray newPlotPoints) {
        if (newPlotPoints != null) {
            plotPoints = new GPointsArray(newPlotPoints);

            // Update the arrays
            nHistElements = plotPoints.getNPoints();

            if (nHistElements > histSeparations.length) {
                histSeparations = new float[nHistElements];
                histBgColors = new int[nHistElements];
                histLineColors = new int[nHistElements];
                histLineWidths = new float[nHistElements];
                histLeftSides = new float[nHistElements];
                histRightSides = new float[nHistElements];

                for (int i = 0; i < nHistElements; i++) {
                    histSeparations[i] = separations[i % separations.length];
                    histBgColors[i] = bgColors[i % bgColors.length];
                    histLineColors[i] = lineColors[i % lineColors.length];
                    histLineWidths[i] = lineWidths[i % lineWidths.length];
                }
            }

            updateSides();
        }
    }

    /**
     * Sets the separations between the histogram elements
     * 
     * @param newSeparations
     *            the new separations between the histogram elements
     */
    public void setSeparations(float[] newSeparations) {
        if (newSeparations != null && newSeparations.length > 0) {
            separations = newSeparations.clone();

            for (int i = 0; i < histSeparations.length; i++) {
                histSeparations[i] = separations[i % separations.length];
            }

            updateSides();
        }
    }

    /**
     * Sets the background colors of the histogram elements
     * 
     * @param newBgColors
     *            the new background colors of the histogram elements
     */
    public void setBgColors(int[] newBgColors) {
        if (newBgColors != null && newBgColors.length > 0) {
            bgColors = newBgColors.clone();

            for (int i = 0; i < histBgColors.length; i++) {
                histBgColors[i] = bgColors[i % bgColors.length];
            }
        }
    }

    /**
     * Sets the line colors of the histogram elements
     * 
     * @param newLineColors
     *            the new line colors of the histogram elements
     */
    public void setLineColors(int[] newLineColors) {
        if (newLineColors != null && newLineColors.length > 0) {
            lineColors = newLineColors.clone();

            for (int i = 0; i < histLineColors.length; i++) {
                histLineColors[i] = lineColors[i % lineColors.length];
            }
        }
    }

    /**
     * Sets the line widths of the histogram elements
     * 
     * @param newLineWidths
     *            the new line widths of the histogram elements
     */
    public void setLineWidths(float[] newLineWidths) {
        if (newLineWidths != null && newLineWidths.length > 0) {
            lineWidths = newLineWidths.clone();

            for (int i = 0; i < histLineWidths.length; i++) {
                histLineWidths[i] = lineWidths[i % lineWidths.length];

                if (histLineWidths[i] < 0) {
                    histLineWidths[i] = 0;
                }
            }
        }
    }

    /**
     * Sets if the histogram should be visible or not
     * 
     * @param newVisible
     *            true if the histogram should be visible
     */
    public void setVisible(boolean newVisible) {
        visible = newVisible;
    }

    /**
     * Sets the histogram labels offset
     * 
     * @param newLabelsOffset
     *            the new histogram labels offset
     */
    public void setLabelsOffset(float newLabelsOffset) {
        labelsOffset = newLabelsOffset;
    }

    /**
     * Sets if the histogram labels should be drawn or not
     * 
     * @param newDrawLabels
     *            true if the histogram labels should be drawn
     */
    public void setDrawLabels(boolean newDrawLabels) {
        drawLabels = newDrawLabels;
    }

    /**
     * Sets if the histogram labels should be rotated or not
     * 
     * @param newRotateLabels
     *            true if the histogram labels should be rotated
     */
    public void setRotateLabels(boolean newRotateLabels) {
        rotateLabels = newRotateLabels;
    }

    /**
     * Sets the font name
     * 
     * @param newFontName
     *            the name of the new font
     */
    public void setFontName(String newFontName) {
        fontName = newFontName;
        font = parent.createFont(fontName, fontSize);
    }

    /**
     * Sets the font color
     * 
     * @param newFontColor
     *            the new font color
     */
    public void setFontColor(int newFontColor) {
        fontColor = newFontColor;
    }

    /**
     * Sets the font size
     * 
     * @param newFontSize
     *            the new font size
     */
    public void setFontSize(int newFontSize) {
        if (newFontSize > 0) {
            fontSize = newFontSize;
            font = parent.createFont(fontName, fontSize);
        }
    }

    /**
     * Sets all the font properties at once
     * 
     * @param newFontName
     *            the name of the new font
     * @param newFontColor
     *            the new font color
     * @param newFontSize
     *            the new font size
     */
    public void setFontProperties(String newFontName, int newFontColor, int newFontSize) {
        if (newFontSize > 0) {
            fontName = newFontName;
            fontColor = newFontColor;
            fontSize = newFontSize;
            font = parent.createFont(fontName, fontSize);
        }
    }

}
