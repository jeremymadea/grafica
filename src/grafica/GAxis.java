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

import java.math.BigDecimal;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

/**
 * A GAxis contains the information of one of the GPlot axes
 * 
 * @author Javier Gracia Carpio
 */
public class GAxis implements PConstants {
    // The parent Processing applet
    protected final PApplet parent;

    // General properties
    protected final int type;
    protected float[] dim;
    protected float[] lim;
    protected boolean log;

    // Format properties
    protected float offset;
    protected int lineColor;
    protected float lineWidth;

    // Ticks properties
    protected int nTicks;
    protected float[] ticks;
    protected float[] plotTicks;
    protected boolean[] ticksInside;
    protected String[] tickLabels;
    protected boolean fixedTicks;
    protected float tickLength;
    protected float smallTickLength;
    protected boolean expTickLabels;
    protected boolean rotateTickLabels;
    protected boolean drawTickLabels;
    protected float tickLabelOffset;

    // Label properties
    protected final GAxisLabel lab;
    protected boolean drawAxisLabel;

    // Text properties
    protected String fontName;
    protected int fontColor;
    protected int fontSize;
    protected PFont font;

    /**
     * GAxis constructor
     * 
     * @param parent
     *            the parent Processing applet
     * @param type
     *            the axis type. It can be X, Y, TOP or RIGHT
     * @param dim
     *            the dimensions in pixels
     * @param lim
     *            the limits
     * @param log
     *            the axis scale. True if it's logarithmic
     */
    public GAxis(PApplet parent, int type, float[] dim, float[] lim, boolean log) {
        this.parent = parent;

        this.type = (type == X || type == Y || type == TOP || type == RIGHT) ? type : X;
        this.dim = dim.clone();
        this.lim = lim.clone();
        this.log = log;

        // Do some sanity checks
        if (this.log && (this.lim[0] <= 0 || this.lim[1] <= 0)) {
            PApplet.println("The limits are negative. This is not allowed in logarithmic scale.");
            PApplet.println("Will set them to (0.1, 10)");
            this.lim = new float[] { 0.1f, 10 };
        }

        offset = 5;
        lineColor = this.parent.color(0);
        lineWidth = 1;

        nTicks = 5;
        ticks = obtainTicks();
        plotTicks = valueToPlot(ticks);
        ticksInside = isInside(plotTicks);
        tickLabels = obtainTickLabels(ticks);
        fixedTicks = false;
        tickLength = 3;
        smallTickLength = 2;
        expTickLabels = false;
        rotateTickLabels = (this.type == X || this.type == TOP) ? false : true;
        drawTickLabels = (this.type == X || this.type == Y) ? true : false;
        tickLabelOffset = 7;

        lab = new GAxisLabel(this.parent, this.type, this.dim);
        drawAxisLabel = true;

        fontName = "SansSerif.plain";
        fontColor = this.parent.color(0);
        fontSize = 11;
        font = this.parent.createFont(fontName, fontSize);
    }

    /**
     * Calculates the optimum number of significant digits to use for a given
     * number
     * 
     * @param number
     *            the number
     * 
     * @return the number of significant digits
     */
    protected int obtainSigDigits(float number) {
        return Math.round(-PApplet.log(0.5f * Math.abs(number)) / GPlot.LOG10);
    }

    /**
     * Rounds a number to a given number of significant digits
     * 
     * @param number
     *            the number to round
     * @param sigDigits
     *            the number of significant digits
     * 
     * @return the rounded number
     */
    protected float roundPlus(float number, int sigDigits) {
        return BigDecimal.valueOf(number).setScale(sigDigits, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * Calculates the axis ticks
     * 
     * @return the axis ticks
     */
    protected float[] obtainTicks() {
        if (log) {
            return obtainLogarithmicTicks();
        } else {
            return obtainLinearTicks();
        }
    }

    /**
     * Calculates the axis ticks for the logarithmic scale
     * 
     * @return the axis ticks
     */
    protected float[] obtainLogarithmicTicks() {
        // Get the exponents of the first and last ticks in increasing order
        int firstExp = (lim[1] > lim[0]) ? PApplet.floor(PApplet.log(lim[0]) / GPlot.LOG10) : PApplet.floor(PApplet.log(lim[1])
                / GPlot.LOG10);
        int lastExp = (lim[1] > lim[0]) ? PApplet.ceil(PApplet.log(lim[1]) / GPlot.LOG10) : PApplet.ceil(PApplet.log(lim[0]) / GPlot.LOG10);

        // Calculate the ticks
        float[] logarithmicTicks = new float[(lastExp - firstExp) * 9 + 1];

        for (int exp = firstExp; exp < lastExp; exp++) {
            float base = roundPlus(PApplet.exp(exp * GPlot.LOG10), -exp);

            for (int i = 0; i < 9; i++) {
                logarithmicTicks[(exp - firstExp) * 9 + i] = (i + 1) * base;
            }
        }

        logarithmicTicks[logarithmicTicks.length - 1] = roundPlus(PApplet.exp(lastExp * GPlot.LOG10), -lastExp);

        return logarithmicTicks;
    }

    /**
     * Calculates the axis ticks for the linear scale
     * 
     * @return the axis ticks
     */
    protected float[] obtainLinearTicks() {
        float[] linearTicks = new float[0];

        if (nTicks > 0) {
            // Obtain the required precision for the ticks
            float step = (lim[1] - lim[0]) / nTicks;
            int sigDigits = obtainSigDigits(step);
            step = roundPlus(step, sigDigits);

            if (step == 0 || Math.abs(step) > Math.abs(lim[1] - lim[0])) {
                sigDigits++;
                step = roundPlus((lim[1] - lim[0]) / nTicks, sigDigits);
            }

            // Obtain the first tick
            float firstTick = roundPlus(lim[0], sigDigits);

            if ((lim[1] - firstTick) * (lim[0] - firstTick) > 0) {
                firstTick = roundPlus(lim[0] + step, sigDigits);
            }

            // Calculate the rest of the ticks
            linearTicks = new float[PApplet.floor(Math.abs((lim[1] - firstTick) / step)) + 1];
            linearTicks[0] = firstTick;

            for (int i = 1; i < linearTicks.length; i++) {
                linearTicks[i] = roundPlus(linearTicks[i - 1] + step, sigDigits);
            }
        }

        return linearTicks;
    }

    /**
     * Calculates the positions of the axis ticks in the plot reference system
     * 
     * @param tks
     *            the axis ticks
     * 
     * @return the positions of the axis ticks in the plot reference system
     */
    protected float[] valueToPlot(float[] tks) {
        float[] plotPos = new float[tks.length];
        float scaleFactor;

        if (log) {
            if (type == X || type == TOP) {
                scaleFactor = dim[0] / PApplet.log(lim[1] / lim[0]);
            } else {
                scaleFactor = -dim[1] / PApplet.log(lim[1] / lim[0]);
            }

            for (int i = 0; i < plotPos.length; i++) {
                plotPos[i] = PApplet.log(tks[i] / lim[0]) * scaleFactor;
            }
        } else {
            if (type == X || type == TOP) {
                scaleFactor = dim[0] / (lim[1] - lim[0]);
            } else {
                scaleFactor = -dim[1] / (lim[1] - lim[0]);
            }

            for (int i = 0; i < plotPos.length; i++) {
                plotPos[i] = (tks[i] - lim[0]) * scaleFactor;
            }
        }

        return plotPos;
    }

    /**
     * Checks which ticks are inside the axis limits
     * 
     * @param plotTks
     *            the axis ticks positions in the plot reference system
     * 
     * @return a boolean array with the elements set to true if the tick is
     *         inside the axis limits
     */
    protected boolean[] isInside(float[] plotTks) {
        boolean[] insideCond = new boolean[plotTks.length];

        if (type == X || type == TOP) {
            for (int i = 0; i < insideCond.length; i++) {
                insideCond[i] = (plotTks[i] >= 0) && (plotTks[i] <= dim[0]);
            }
        } else {
            for (int i = 0; i < insideCond.length; i++) {
                insideCond[i] = (-plotTks[i] >= 0) && (-plotTks[i] <= dim[1]);
            }
        }

        return insideCond;
    }

    /**
     * Removes those axis ticks that are outside the axis limits
     * 
     * @return the ticks that are inside the axis limits
     */
    protected float[] removeOutsideTicks() {
        float[] validTicks = new float[ticksInside.length];
        int counter = 0;

        for (int i = 0; i < ticksInside.length; i++) {
            if (ticksInside[i]) {
                validTicks[counter] = ticks[i];
                counter++;
            }
        }

        return Arrays.copyOf(validTicks, counter);
    }

    /**
     * Removes those axis ticks in the plot reference system that are outside
     * the axis limits
     * 
     * @return the ticks in the plot reference system that are inside the axis
     *         limits
     */
    protected float[] removeOutsidePlotTicks() {
        float[] validPlotTicks = new float[ticksInside.length];
        int counter = 0;

        for (int i = 0; i < ticksInside.length; i++) {
            if (ticksInside[i]) {
                validPlotTicks[counter] = plotTicks[i];
                counter++;
            }
        }

        return Arrays.copyOf(validPlotTicks, counter);
    }

    /**
     * Obtains the axis tick labels
     * 
     * @param tks
     *            the axis ticks
     * 
     * @return the axis tick labels
     */
    protected String[] obtainTickLabels(float[] tks) {
        String[] labels = new String[tks.length];

        if (log) {
            for (int i = 0; i < labels.length; i++) {
                if (tks[i] > 0) {
                    float logValue = PApplet.log(tks[i]) / GPlot.LOG10;
                    boolean isExactLogValue = Math.abs(logValue - Math.round(logValue)) < 0.0001;

                    if (isExactLogValue) {
                        logValue = Math.round(logValue);

                        if (expTickLabels) {
                            labels[i] = "1e" + (int) logValue;
                        } else {
                            if (logValue > -3.1 && logValue < 3.1) {
                                labels[i] = (logValue >= 0) ? PApplet.str((int) tks[i]) : PApplet.str(tks[i]);
                            } else {
                                labels[i] = "1e" + (int) logValue;
                            }
                        }
                    } else {
                        labels[i] = "";
                    }
                } else {
                    labels[i] = "";
                }
            }
        } else {
            for (int i = 0; i < labels.length; i++) {
                labels[i] = (tks[i] % 1 == 0 && Math.abs(tks[i]) < 1e9) ? PApplet.str((int) tks[i]) : PApplet.str(tks[i]);
            }
        }

        return labels;
    }

    /**
     * Moves the axis limits
     * 
     * @param newLim
     *            the new axis limits
     */
    public void moveLim(float[] newLim) {
        if (newLim != null && newLim.length == 2 && newLim[1] != newLim[0]) {
            // Check that the new limit makes sense
            if (log && (newLim[0] <= 0 || newLim[1] <= 0)) {
                PApplet.println("The limits are negative. This is not allowed in logarithmic scale.");
            } else {
                lim = newLim.clone();

                // Calculate the new ticks if they are not fixed
                if (!fixedTicks) {
                    if (log) {
                        ticks = obtainLogarithmicTicks();
                    } else if (ticks.length > 0) {
                        // Obtain the ticks precision and the tick separation
                        float step = (ticks.length == 1) ? lim[1] - lim[0] : ticks[1] - ticks[0];
                        int sigDigits = obtainSigDigits(step);
                        step = roundPlus(step, sigDigits);

                        if (step == 0 || Math.abs(step) > Math.abs(lim[1] - lim[0])) {
                            sigDigits++;
                            step = (ticks.length == 1) ? lim[1] - lim[0] : ticks[1] - ticks[0];
                            step = roundPlus(step, sigDigits);
                        }

                        step = (lim[1] > lim[0]) ? Math.abs(step) : -Math.abs(step);

                        // Obtain the first tick
                        float firstTick = ticks[0] + step * PApplet.ceil((lim[0] - ticks[0]) / step);
                        firstTick = roundPlus(firstTick, sigDigits);

                        if ((lim[1] - firstTick) * (lim[0] - firstTick) > 0) {
                            firstTick = ticks[0] + step * PApplet.floor((lim[0] - ticks[0]) / step);
                            firstTick = roundPlus(firstTick, sigDigits);
                        }

                        // Calculate the rest of the ticks
                        ticks = new float[PApplet.floor(Math.abs((lim[1] - firstTick) / step)) + 1];
                        ticks[0] = firstTick;

                        for (int i = 1; i < ticks.length; i++) {
                            ticks[i] = roundPlus(ticks[i - 1] + step, sigDigits);
                        }
                    }

                    // Obtain the new tick labels
                    tickLabels = obtainTickLabels(ticks);
                }

                // Update the rest of the arrays
                plotTicks = valueToPlot(ticks);
                ticksInside = isInside(plotTicks);
            }
        }
    }

    /**
     * Draws the axis
     */
    public void draw() {
        switch (type) {
        case X:
            drawAsXAxis();
            break;
        case Y:
            drawAsYAxis();
            break;
        case TOP:
            drawAsTopAxis();
            break;
        case RIGHT:
            drawAsRightAxis();
            break;
        }

        if (drawAxisLabel)
            lab.draw();
    }

    /**
     * Draws the axis as an X axis
     */
    protected void drawAsXAxis() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        // Draw the ticks
        parent.line(0, offset, dim[0], offset);

        for (int i = 0; i < plotTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(plotTicks[i], offset, plotTicks[i], offset + smallTickLength);
                } else {
                    parent.line(plotTicks[i], offset, plotTicks[i], offset + tickLength);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(RIGHT, CENTER);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(plotTicks[i], offset + tickLabelOffset);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(CENTER, TOP);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], plotTicks[i], offset + tickLabelOffset);
                    }
                }
            }
        }
        parent.popStyle();
    }

    /**
     * Draws the axis as a Y axis
     */
    protected void drawAsYAxis() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        // Draw the ticks
        parent.line(-offset, 0, -offset, -dim[1]);

        for (int i = 0; i < plotTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(-offset, plotTicks[i], -offset - smallTickLength, plotTicks[i]);
                } else {
                    parent.line(-offset, plotTicks[i], -offset - tickLength, plotTicks[i]);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(CENTER, BOTTOM);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(-offset - tickLabelOffset, plotTicks[i]);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(RIGHT, CENTER);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], -offset - tickLabelOffset, plotTicks[i]);
                    }
                }
            }
        }
        parent.popStyle();
    }

    /**
     * Draws the axis as a TOP axis
     */
    protected void drawAsTopAxis() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        parent.pushMatrix();
        parent.translate(0, -dim[1]);

        // Draw the ticks
        parent.line(0, -offset, dim[0], -offset);

        for (int i = 0; i < plotTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(plotTicks[i], -offset, plotTicks[i], -offset - smallTickLength);
                } else {
                    parent.line(plotTicks[i], -offset, plotTicks[i], -offset - tickLength);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(LEFT, CENTER);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(plotTicks[i], -offset - tickLabelOffset);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(CENTER, BOTTOM);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], plotTicks[i], -offset - tickLabelOffset);
                    }
                }
            }
        }
        parent.popMatrix();
        parent.popStyle();
    }

    /**
     * Draws the axis as a RIGHT axis
     */
    protected void drawAsRightAxis() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        parent.pushMatrix();
        parent.translate(dim[0], 0);

        // Draw the ticks
        parent.line(offset, 0, offset, -dim[1]);

        for (int i = 0; i < plotTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(offset, plotTicks[i], offset + smallTickLength, plotTicks[i]);
                } else {
                    parent.line(offset, plotTicks[i], offset + tickLength, plotTicks[i]);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(CENTER, TOP);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(offset + tickLabelOffset, plotTicks[i]);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(LEFT, CENTER);

                for (int i = 0; i < plotTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], offset + tickLabelOffset, plotTicks[i]);
                    }
                }
            }
        }
        parent.popMatrix();
        parent.popStyle();
    }

    /**
     * Sets the axis dimensions
     * 
     * @param newDim
     *            the new axis dimensions
     */
    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            dim = newDim.clone();
            plotTicks = valueToPlot(ticks);
            lab.setDim(dim);
        }
    }

    /**
     * Sets the axis limits
     * 
     * @param newLim
     *            the new axis limits
     */
    public void setLim(float[] newLim) {
        if (newLim != null && newLim.length == 2 && newLim[1] != newLim[0]) {
            // Make sure the new limits makes sense
            if (log && (newLim[0] <= 0 || newLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                lim = newLim.clone();

                if (!fixedTicks) {
                    ticks = obtainTicks();
                    tickLabels = obtainTickLabels(ticks);
                }

                plotTicks = valueToPlot(ticks);
                ticksInside = isInside(plotTicks);
            }
        }
    }

    /**
     * Sets the axis limits and the axis scale
     * 
     * @param newLim
     *            the new axis limits
     * @param newLog
     *            the new axis scale
     */
    public void setLimAndLog(float[] newLim, boolean newLog) {
        if (newLim != null && newLim.length == 2 && newLim[1] != newLim[0]) {
            // Make sure the new limits makes sense
            if (newLog && (newLim[0] <= 0 || newLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                lim = newLim.clone();
                log = newLog;

                if (!fixedTicks) {
                    ticks = obtainTicks();
                    tickLabels = obtainTickLabels(ticks);
                }

                plotTicks = valueToPlot(ticks);
                ticksInside = isInside(plotTicks);
            }
        }
    }

    /**
     * Sets the axis scale
     * 
     * @param newLog
     *            the new axis scale
     */
    public void setLog(boolean newLog) {
        if (newLog != log) {
            log = newLog;

            // Check if the old limits still make sense
            if (log && (lim[0] <= 0 || lim[1] <= 0)) {
                PApplet.println("The limits are negative. This is not allowed in logarithmic scale.");
                PApplet.println("Will set them to (0.1, 10)");
                lim = new float[] { 0.1f, 10 };
            }

            if (!fixedTicks) {
                ticks = obtainTicks();
                tickLabels = obtainTickLabels(ticks);
            }

            plotTicks = valueToPlot(ticks);
            ticksInside = isInside(plotTicks);
        }
    }

    /**
     * Sets the axis offset with respect to the plot box
     * 
     * @param newOffset
     *            the new axis offset
     */
    public void setOffset(float newOffset) {
        offset = newOffset;
    }

    /**
     * Sets the line color
     * 
     * @param newLineColor
     *            the new line color
     */
    public void setLineColor(int newLineColor) {
        lineColor = newLineColor;
    }

    /**
     * Sets the line width
     * 
     * @param newLineWidth
     *            the new line width
     */
    public void setLineWidth(float newLineWidth) {
        if (newLineWidth > 0) {
            lineWidth = newLineWidth;
        }
    }

    /**
     * Sets the approximate number of ticks in the axis. The actual number of
     * ticks depends on the axis limits and the axis scale
     * 
     * @param newNTicks
     *            the new approximate number of ticks in the axis
     */
    public void setNTicks(int newNTicks) {
        if (newNTicks >= 0) {
            nTicks = newNTicks;

            if (!log) {
                fixedTicks = false;
                ticks = obtainTicks();
                plotTicks = valueToPlot(ticks);
                ticksInside = isInside(plotTicks);
                tickLabels = obtainTickLabels(ticks);
            }
        }
    }

    /**
     * Sets the axis ticks
     * 
     * @param newTicks
     *            the new axis ticks
     */
    public void setTicks(float[] newTicks) {
        if (newTicks != null) {
            fixedTicks = true;
            ticks = newTicks.clone();
            plotTicks = valueToPlot(ticks);
            ticksInside = isInside(plotTicks);
            tickLabels = obtainTickLabels(ticks);
        }
    }

    /**
     * Sets the axis ticks labels
     * 
     * @param newTickLabels
     *            the new axis ticks labels
     */
    public void setTickLabels(String[] newTickLabels) {
        if (newTickLabels != null && newTickLabels.length == tickLabels.length) {
            fixedTicks = true;
            tickLabels = newTickLabels.clone();
        }
    }

    /**
     * Sets if the axis ticks are fixed or not
     * 
     * @param newFixedTicks
     *            true if the axis ticks should be fixed
     */
    public void setFixedTicks(boolean newFixedTicks) {
        if (newFixedTicks != fixedTicks) {
            fixedTicks = newFixedTicks;

            if (!fixedTicks) {
                ticks = obtainTicks();
                plotTicks = valueToPlot(ticks);
                ticksInside = isInside(plotTicks);
                tickLabels = obtainTickLabels(ticks);
            }
        }
    }

    /**
     * Sets the tick length
     * 
     * @param newTickLength
     *            the new tick length
     */
    public void setTickLength(float newTickLength) {
        tickLength = newTickLength;
    }

    /**
     * Sets the small tick length
     * 
     * @param newSmallTickLength
     *            the new small tick length
     */
    public void setSmallTickLength(float newSmallTickLength) {
        smallTickLength = newSmallTickLength;
    }

    /**
     * Sets if the ticks labels should be displayed in exponential form or not
     * 
     * @param newExpTickLabels
     *            true if the ticks labels should be in exponential form
     */
    public void setExpTickLabels(boolean newExpTickLabels) {
        if (newExpTickLabels != expTickLabels) {
            expTickLabels = newExpTickLabels;

            if (!fixedTicks) {
                tickLabels = obtainTickLabels(ticks);
            }
        }
    }

    /**
     * Sets if the ticks labels should be displayed rotated or not
     * 
     * @param newRotateTickLabels
     *            true is the ticks labels should be rotated
     */
    public void setRotateTickLabels(boolean newRotateTickLabels) {
        rotateTickLabels = newRotateTickLabels;
    }

    /**
     * Sets if the ticks labels should be drawn or not
     * 
     * @param newDrawTicksLabels
     *            true it the ticks labels should be drawn
     */
    public void setDrawTickLabels(boolean newDrawTicksLabels) {
        drawTickLabels = newDrawTicksLabels;
    }

    /**
     * Sets the tick label offset
     * 
     * @param newTickLabelOffset
     *            the new tick label offset
     */
    public void setTickLabelOffset(float newTickLabelOffset) {
        tickLabelOffset = newTickLabelOffset;
    }

    /**
     * Sets if the axis label should be drawn or not
     * 
     * @param newDrawAxisLabel
     *            true if the axis label should be drawn
     */
    public void setDrawAxisLabel(boolean newDrawAxisLabel) {
        drawAxisLabel = newDrawAxisLabel;
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
     * Sets the font properties
     * 
     * @param newFontName
     *            the new of the new font
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

    /**
     * Sets the font properties in the axis and the axis label
     * 
     * @param newFontName
     *            the new font name
     * @param newFontColor
     *            the new font color
     * @param newFontSize
     *            the new font size
     */
    public void setAllFontProperties(String newFontName, int newFontColor, int newFontSize) {
        setFontProperties(newFontName, newFontColor, newFontSize);
        lab.setFontProperties(newFontName, newFontColor, newFontSize);
    }

    /**
     * Returns a copy of the axis ticks
     * 
     * @return a copy of the axis ticks
     */
    public float[] getTicks() {
        if (fixedTicks) {
            return ticks.clone();
        } else {
            return removeOutsideTicks();
        }
    }

    /**
     * Returns the axis ticks
     * 
     * @return the axis ticks
     */
    public float[] getTicksRef() {
        return ticks;
    }

    /**
     * Returns a copy of the axis ticks positions in the plot reference system
     * 
     * @return a copy of the axis ticks positions in the plot reference system
     */
    public float[] getPlotTicks() {
        if (fixedTicks) {
            return plotTicks.clone();
        } else {
            return removeOutsidePlotTicks();
        }
    }

    /**
     * Returns the axis ticks positions in the plot reference system
     * 
     * @return the axis ticks positions in the plot reference system
     */
    public float[] getPlotTicksRef() {
        return plotTicks;
    }

    /**
     * Returns the axis label
     * 
     * @return the axis label
     */
    public GAxisLabel getAxisLabel() {
        return lab;
    }

}
