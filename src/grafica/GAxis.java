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

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class GAxis implements PConstants {
    // The parent Processing applet
    private final PApplet parent;

    // General properties
    private final int type;
    private float[] dim;
    private float[] lim;
    private boolean log;

    // Format properties
    private float offset;
    private int lineColor;
    private float lineWidth;

    // Ticks properties
    private int nTicks;
    private float[] ticks;
    private float[] screenTicks;
    private boolean[] ticksInside;
    private String[] tickLabels;
    private boolean fixedTicks;
    private float tickLength;
    private float smallTickLength;
    private boolean expTickLabels;
    private boolean rotateTickLabels;
    private boolean drawTickLabels;
    private float tickLabelOffset;

    // Label properties
    private final GAxisLabel lab;
    private boolean drawAxisLabel;

    // Text properties
    private String fontName;
    private int fontColor;
    private int fontSize;
    private PFont font;

    /**
     * Constructor
     **/
    public GAxis(PApplet parent, int type, float[] dim, float[] lim, boolean log) {
        this.parent = parent;
        this.type = (type == X || type == Y || type == TOP || type == RIGHT) ? type : X;
        this.dim = dim.clone();
        this.lim = lim.clone();
        this.log = log;

        // Do some sanity checks
        if (log && (lim[0] <= 0 || lim[1] <= 0)) {
            PApplet.println("The limits are negative. This is not allowed in logarithmic scale.");
            PApplet.println("Will set them to (0.1, 10)");
            this.lim = new float[] { 0.1f, 10 };
        }

        offset = 5;
        lineColor = parent.color(0);
        lineWidth = 1;

        nTicks = 5;
        ticks = obtainTicks();
        screenTicks = valueToScreen(ticks);
        ticksInside = isInside(screenTicks);
        tickLabels = obtainTickLabels(ticks);
        fixedTicks = false;
        tickLength = 3;
        smallTickLength = 2;
        expTickLabels = false;
        rotateTickLabels = (this.type == X || this.type == TOP) ? false : true;
        drawTickLabels = (this.type == X || this.type == Y) ? true : false;
        tickLabelOffset = 7;

        lab = new GAxisLabel(parent, this.type, this.dim);
        drawAxisLabel = true;

        fontName = "SansSerif.plain";
        fontColor = parent.color(0);
        fontSize = 11;
        font = parent.createFont(fontName, fontSize);
    }

    //
    // Methods
    // //////////

    private int obtainSigDigits(float number) {
        return PApplet.round(-PApplet.log(0.5f * PApplet.abs(number)) / GPlot.LOG10);
    }

    private float roundPlus(float number, int sigDigits) {
        return BigDecimal.valueOf(number).setScale(sigDigits, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private float[] obtainTicks() {
        if (log) {
            return obtainLogarithmicTicks();
        } else {
            return obtainLinearTicks();
        }
    }

    private float[] obtainLogarithmicTicks() {
        // Get the exponents of the first and last ticks in increasing order
        int firstExp = (lim[1] > lim[0]) ? PApplet.floor(PApplet.log(lim[0]) / GPlot.LOG10) : PApplet.floor(PApplet.log(lim[1])
                / GPlot.LOG10);
        int lastExp = (lim[1] > lim[0]) ? PApplet.ceil(PApplet.log(lim[1]) / GPlot.LOG10) : PApplet.ceil(PApplet.log(lim[0]) / GPlot.LOG10);

        // Calculate the ticks
        float[] result = new float[(lastExp - firstExp) * 9 + 1];

        for (int exp = firstExp; exp < lastExp; exp++) {
            float base = roundPlus(PApplet.exp(exp * GPlot.LOG10), -exp);

            for (int i = 0; i < 9; i++) {
                result[(exp - firstExp) * 9 + i] = (i + 1) * base;
            }
        }

        result[result.length - 1] = roundPlus(PApplet.exp(lastExp * GPlot.LOG10), -lastExp);

        return result;
    }

    private float[] obtainLinearTicks() {
        float[] result = new float[0];

        if (nTicks > 0) {
            // Obtain the required precission for the ticks
            float step = (lim[1] - lim[0]) / nTicks;
            int sigDigits = obtainSigDigits(step);
            step = roundPlus(step, sigDigits);

            if (step == 0 || PApplet.abs(step) > PApplet.abs(lim[1] - lim[0])) {
                sigDigits++;
                step = roundPlus((lim[1] - lim[0]) / nTicks, sigDigits);
            }

            // Obtain the first tick
            float firstTick = roundPlus(lim[0], sigDigits);

            if ((lim[1] - firstTick) * (lim[0] - firstTick) > 0) {
                firstTick = roundPlus(lim[0] + step, sigDigits);
            }

            // Calculate the rest of the ticks
            result = new float[PApplet.floor(PApplet.abs((lim[1] - firstTick) / step)) + 1];
            result[0] = firstTick;

            for (int i = 1; i < result.length; i++) {
                result[i] = roundPlus(result[i - 1] + step, sigDigits);
            }
        }

        return result;
    }

    private float[] valueToScreen(float[] tks) {
        float[] result = new float[tks.length];
        float scaleFactor;

        if (log) {
            if (type == X || type == TOP) {
                scaleFactor = dim[0] / PApplet.log(lim[1] / lim[0]);
            } else {
                scaleFactor = -dim[1] / PApplet.log(lim[1] / lim[0]);
            }

            for (int i = 0; i < result.length; i++) {
                if (tks[i] > 0) {
                    result[i] = PApplet.log(tks[i] / lim[0]) * scaleFactor;
                } else {
                    // Put the tick outside the plot
                    result[i] = (type == X || type == TOP) ? -1 : 1;
                }
            }
        } else {
            if (type == X || type == TOP) {
                scaleFactor = dim[0] / (lim[1] - lim[0]);
            } else {
                scaleFactor = -dim[1] / (lim[1] - lim[0]);
            }

            for (int i = 0; i < result.length; i++) {
                result[i] = (tks[i] - lim[0]) * scaleFactor;
            }
        }

        return result;
    }

    private boolean[] isInside(float[] screenTks) {
        boolean[] result = new boolean[screenTks.length];

        if (type == X || type == TOP) {
            for (int i = 0; i < result.length; i++) {
                result[i] = (screenTks[i] >= 0) && (screenTks[i] <= dim[0]);
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = (-screenTks[i] >= 0) && (-screenTks[i] <= dim[1]);
            }
        }

        return result;
    }

    private float[] removeOutsideTicks() {
        // Count the number of ticks inside
        int counter = 0;

        for (int i = 0; i < ticksInside.length; i++) {
            if (ticksInside[i])
                counter++;
        }

        // Create a new array with the valid ticks
        float[] result = new float[counter];
        counter = 0;

        for (int i = 0; i < ticksInside.length; i++) {
            if (ticksInside[i]) {
                result[counter] = ticks[i];
                counter++;
            }
        }

        return result;
    }

    private float[] removeOutsideScreenTicks() {
        // Count the number of ticks inside
        int counter = 0;

        for (int i = 0; i < ticksInside.length; i++) {
            if (ticksInside[i])
                counter++;
        }

        // Create a new array with the valid ticks
        float[] result = new float[counter];
        counter = 0;

        for (int i = 0; i < ticksInside.length; i++) {
            if (ticksInside[i]) {
                result[counter] = screenTicks[i];
                counter++;
            }
        }

        return result;
    }

    private String[] obtainTickLabels(float[] tks) {
        String[] result = new String[tks.length];

        if (log) {
            for (int i = 0; i < result.length; i++) {
                if (tks[i] > 0) {
                    float logValue = PApplet.log(tks[i]) / GPlot.LOG10;
                    boolean isExactLogValue = PApplet.abs(logValue - PApplet.round(logValue)) < 0.0001;

                    if (isExactLogValue) {
                        logValue = PApplet.round(logValue);

                        if (expTickLabels) {
                            result[i] = "1e" + (int) logValue;
                        } else {
                            if (logValue > -3.1 && logValue < 3.1) {
                                result[i] = (logValue >= 0) ? PApplet.str((int) tks[i]) : PApplet.str(tks[i]);
                            } else {
                                result[i] = "1e" + (int) logValue;
                            }
                        }
                    } else {
                        result[i] = "";
                    }
                } else {
                    result[i] = "";
                }
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = (tks[i] % 1 == 0 && PApplet.abs(tks[i]) < 1e9) ? PApplet.str((int) tks[i]) : PApplet.str(tks[i]);
            }
        }

        return result;
    }

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
                        // Obtain the ticks precission and the tick separation
                        float step = (ticks.length == 1) ? lim[1] - lim[0] : ticks[1] - ticks[0];
                        int sigDigits = obtainSigDigits(step);
                        step = roundPlus(step, sigDigits);

                        if (step == 0 || PApplet.abs(step) > PApplet.abs(lim[1] - lim[0])) {
                            sigDigits++;
                            step = (ticks.length == 1) ? lim[1] - lim[0] : ticks[1] - ticks[0];
                            step = roundPlus(step, sigDigits);
                        }

                        step = ((lim[1] - lim[0]) > 0) ? PApplet.abs(step) : -PApplet.abs(step);

                        // Obtain the first tick
                        float firstTick = ticks[0] + step * PApplet.ceil((lim[0] - ticks[0]) / step);
                        firstTick = roundPlus(firstTick, sigDigits);

                        if ((lim[1] - firstTick) * (lim[0] - firstTick) > 0) {
                            firstTick = ticks[0] + step * PApplet.floor((lim[0] - ticks[0]) / step);
                            firstTick = roundPlus(firstTick, sigDigits);
                        }

                        // Calculate the rest of the ticks
                        ticks = new float[PApplet.floor(PApplet.abs((lim[1] - firstTick) / step)) + 1];
                        ticks[0] = firstTick;

                        for (int i = 1; i < ticks.length; i++) {
                            ticks[i] = roundPlus(ticks[i - 1] + step, sigDigits);
                        }
                    }
                    // Obtain the new tick labels
                    tickLabels = obtainTickLabels(ticks);
                }

                // Update the rest of the arrays
                screenTicks = valueToScreen(ticks);
                ticksInside = isInside(screenTicks);
            }
        }
    }

    public void draw() {
        if (type == X)
            drawAsXAxis();
        else if (type == Y)
            drawAsYAxis();
        else if (type == TOP)
            drawAsTopAxis();
        else if (type == RIGHT)
            drawAsRightAxis();

        if (drawAxisLabel)
            lab.draw();
    }

    private void drawAsXAxis() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        // Draw the ticks
        parent.line(0, offset, dim[0], offset);

        for (int i = 0; i < screenTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(screenTicks[i], offset, screenTicks[i], offset + smallTickLength);
                } else {
                    parent.line(screenTicks[i], offset, screenTicks[i], offset + tickLength);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(RIGHT, CENTER);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(screenTicks[i], offset + tickLabelOffset);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(CENTER, TOP);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], screenTicks[i], offset + tickLabelOffset);
                    }
                }
            }
        }
        parent.popStyle();
    }

    private void drawAsYAxis() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.stroke(lineColor);
        parent.strokeWeight(lineWidth);
        parent.strokeCap(SQUARE);

        // Draw the ticks
        parent.line(-offset, 0, -offset, -dim[1]);

        for (int i = 0; i < screenTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(-offset, screenTicks[i], -offset - smallTickLength, screenTicks[i]);
                } else {
                    parent.line(-offset, screenTicks[i], -offset - tickLength, screenTicks[i]);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(CENTER, BOTTOM);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(-offset - tickLabelOffset, screenTicks[i]);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(RIGHT, CENTER);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], -offset - tickLabelOffset, screenTicks[i]);
                    }
                }
            }
        }
        parent.popStyle();
    }

    private void drawAsTopAxis() {
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

        for (int i = 0; i < screenTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(screenTicks[i], -offset, screenTicks[i], -offset - smallTickLength);
                } else {
                    parent.line(screenTicks[i], -offset, screenTicks[i], -offset - tickLength);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(LEFT, CENTER);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(screenTicks[i], -offset - tickLabelOffset);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(CENTER, BOTTOM);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], screenTicks[i], -offset - tickLabelOffset);
                    }
                }
            }
        }
        parent.popMatrix();
        parent.popStyle();
    }

    private void drawAsRightAxis() {
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

        for (int i = 0; i < screenTicks.length; i++) {
            if (ticksInside[i]) {
                if (log && tickLabels[i].equals("")) {
                    parent.line(offset, screenTicks[i], offset + smallTickLength, screenTicks[i]);
                } else {
                    parent.line(offset, screenTicks[i], offset + tickLength, screenTicks[i]);
                }
            }
        }

        // Draw the tick labels
        if (drawTickLabels) {
            if (rotateTickLabels) {
                parent.textAlign(CENTER, TOP);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.pushMatrix();
                        parent.translate(offset + tickLabelOffset, screenTicks[i]);
                        parent.rotate(-HALF_PI);
                        parent.text(tickLabels[i], 0, 0);
                        parent.popMatrix();
                    }
                }
            } else {
                parent.textAlign(LEFT, CENTER);

                for (int i = 0; i < screenTicks.length; i++) {
                    if (ticksInside[i] && !tickLabels[i].equals("")) {
                        parent.text(tickLabels[i], offset + tickLabelOffset, screenTicks[i]);
                    }
                }
            }
        }
        parent.popMatrix();
        parent.popStyle();
    }

    //
    // Setters
    // //////////

    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            dim = newDim.clone();
            screenTicks = valueToScreen(ticks);
            lab.setDim(dim);
        }
    }

    public void setLim(float[] newLim) {
        if (newLim != null && newLim.length == 2 && newLim[1] != newLim[0]) {
            // Make sure the new limit makes sense
            if (log && (newLim[0] <= 0 || newLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                lim = newLim.clone();

                if (!fixedTicks) {
                    ticks = obtainTicks();
                    tickLabels = obtainTickLabels(ticks);
                }

                screenTicks = valueToScreen(ticks);
                ticksInside = isInside(screenTicks);
            }
        }
    }

    public void setLimAndLog(float[] newLim, boolean newLog) {
        if (newLim != null && newLim.length == 2 && newLim[1] != newLim[0]) {
            // Make sure the new limit makes sense
            if (newLog && (newLim[0] <= 0 || newLim[1] <= 0)) {
                PApplet.println("One of the limits is negative. This is not allowed in logarithmic scale.");
            } else {
                lim = newLim.clone();
                log = newLog;

                if (!fixedTicks) {
                    ticks = obtainTicks();
                    tickLabels = obtainTickLabels(ticks);
                }

                screenTicks = valueToScreen(ticks);
                ticksInside = isInside(screenTicks);
            }
        }
    }

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

            screenTicks = valueToScreen(ticks);
            ticksInside = isInside(screenTicks);
        }
    }

    public void setOffset(float newOffset) {
        offset = newOffset;
    }

    public void setLineColor(int newLineColor) {
        lineColor = newLineColor;
    }

    public void setLineWidth(float newLineWidth) {
        if (newLineWidth > 0) {
            lineWidth = newLineWidth;
        }
    }

    public void setNTicks(int newNTicks) {
        if (newNTicks >= 0) {
            nTicks = newNTicks;

            if (!log) {
                fixedTicks = false;
                ticks = obtainTicks();
                screenTicks = valueToScreen(ticks);
                ticksInside = isInside(screenTicks);
                tickLabels = obtainTickLabels(ticks);
            }
        }
    }

    public void setTicks(float[] newTicks) {
        if (newTicks != null) {
            fixedTicks = true;
            ticks = newTicks.clone();
            screenTicks = valueToScreen(ticks);
            ticksInside = isInside(screenTicks);
            tickLabels = obtainTickLabels(ticks);
        }
    }

    public void setTickLabels(String[] newTickLabels) {
        if (newTickLabels != null && newTickLabels.length == tickLabels.length) {
            fixedTicks = true;
            tickLabels = newTickLabels.clone();
        }
    }

    public void setFixedTicks(boolean newFixedTicks) {
        if (newFixedTicks != fixedTicks) {
            fixedTicks = newFixedTicks;

            if (!fixedTicks) {
                ticks = obtainTicks();
                screenTicks = valueToScreen(ticks);
                ticksInside = isInside(screenTicks);
                tickLabels = obtainTickLabels(ticks);
            }
        }
    }

    public void setTickLength(float newTickLength) {
        tickLength = newTickLength;
    }

    public void setSmallTickLength(float newSmallTickLength) {
        smallTickLength = newSmallTickLength;
    }

    public void setExpTickLabels(boolean newExpTickLabels) {
        if (newExpTickLabels != expTickLabels) {
            expTickLabels = newExpTickLabels;

            if (!fixedTicks) {
                tickLabels = obtainTickLabels(ticks);
            }
        }
    }

    public void setRotateTickLabels(boolean newRotateTickLabels) {
        rotateTickLabels = newRotateTickLabels;
    }

    public void setDrawTickLabels(boolean newDrawTicksLabels) {
        drawTickLabels = newDrawTicksLabels;
    }

    public void setTickLabelOffset(float newTickLabelOffset) {
        tickLabelOffset = newTickLabelOffset;
    }

    public void setDrawAxisLabel(boolean newDrawAxisLabel) {
        drawAxisLabel = newDrawAxisLabel;
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
        lab.setFontProperties(newFontName, newFontColor, newFontSize);
    }

    //
    // Getters
    // //////////

    public float[] getTicks() {
        if (fixedTicks) {
            return ticks.clone();
        } else {
            return removeOutsideTicks();
        }
    }

    public float[] getTicksRef() {
        return ticks;
    }

    public float[] getScreenTicks() {
        if (fixedTicks) {
            return screenTicks.clone();
        } else {
            return removeOutsideScreenTicks();
        }
    }

    public float[] getPlotTicksRef() {
        return screenTicks;
    }

    public GAxisLabel getAxisLabel() {
        return lab;
    }

}
