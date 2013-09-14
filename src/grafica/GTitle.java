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

public class GTitle implements PConstants {
    // The parent Processing applet
    private final PApplet parent;

    // General properties
    private float[] dim;
    private float relativePos;
    private float screenPos;
    private float offset;

    // Text properties
    private String text;
    private int textAlignment;
    private String fontName;
    private int fontColor;
    private int fontSize;
    private PFont font;

    //
    // Constructor
    // /////////////

    public GTitle(PApplet parent, float[] dim) {
        this.parent = parent;
        this.dim = dim.clone();
        relativePos = 0.5f;
        screenPos = relativePos * dim[0];
        offset = 10;

        text = "";
        textAlignment = CENTER;
        fontName = "SansSerif.bold";
        fontColor = parent.color(100);
        fontSize = 13;
        font = parent.createFont(fontName, fontSize);
    }

    //
    // Methods
    // //////////

    public void draw() {
        parent.pushStyle();
        parent.textFont(font);
        parent.textSize(fontSize);
        parent.fill(fontColor);
        parent.noStroke();
        parent.textAlign(textAlignment, BOTTOM);
        parent.text(text, screenPos, -offset - dim[1]);
        parent.popStyle();
    }

    //
    // Setters
    // //////////

    public void setDim(float[] newDim) {
        if (newDim != null && newDim.length == 2 && newDim[0] > 0 && newDim[1] > 0) {
            dim = newDim.clone();
            screenPos = relativePos * dim[0];
        }
    }

    public void setRelativePos(float newRelativePos) {
        relativePos = newRelativePos;
        screenPos = relativePos * dim[0];
    }

    public void setOffset(float newOffset) {
        offset = newOffset;
    }

    public void setText(String newText) {
        text = newText;
    }

    public void setTextAlignment(int newTextAlignment) {
        if (newTextAlignment == CENTER || newTextAlignment == LEFT || newTextAlignment == RIGHT) {
            textAlignment = newTextAlignment;
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

}
