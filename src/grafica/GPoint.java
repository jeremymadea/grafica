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

public class GPoint {
    private float x;
    private float y;
    private String label;
    private boolean valid;

    //
    // Constructor
    // /////////////

    public GPoint(float x, float y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    public GPoint(float x, float y) {
        this(x, y, "");
    }

    public GPoint(GPoint p) {
        this(p.getX(), p.getY(), p.getLabel());
    }

    //
    // Methods
    // ///////////

    private boolean isValidNumber(float number) {
        return !Float.isNaN(number) && !Float.isInfinite(number);
    }

    //
    // Setters
    // //////////

    public void setX(float newX) {
        x = newX;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    public void setY(float newY) {
        y = newY;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    public void setXY(float newX, float newY) {
        x = newX;
        y = newY;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }

    //
    // Getters
    // //////////

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getLabel() {
        return label;
    }

    public boolean getValid() {
        return valid;
    }

    public boolean isValid() {
        return valid;
    }

}
