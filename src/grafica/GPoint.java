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

/**
 * Point class. A GPoint is composed of two coordinates (x, y) and a text label
 * 
 * @author Javier Gracia Carpio
 */
public class GPoint {
    protected float x;
    protected float y;
    protected String label;
    protected boolean valid;

    /**
     * Constructor
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param label
     *            the text label
     */
    public GPoint(float x, float y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    /**
     * Constructor
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public GPoint(float x, float y) {
        this(x, y, "");
    }

    /**
     * Constructor
     * 
     * @param point
     *            a GPoint
     */
    public GPoint(GPoint point) {
        this(point.getX(), point.getY(), point.getLabel());
    }

    /**
     * Checks if the provided number is valid (i.e., is not NaN or Infinite)
     * 
     * @param number
     *            the number to check
     * 
     * @return true if its valid
     */
    protected boolean isValidNumber(float number) {
        return !Float.isNaN(number) && !Float.isInfinite(number);
    }

    /**
     * Sets the point x coordinate
     * 
     * @param newX
     *            the new x coordinate
     */
    public void setX(float newX) {
        x = newX;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    /**
     * Sets the point y coordinate
     * 
     * @param newY
     *            the new y coordinate
     */
    public void setY(float newY) {
        y = newY;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    /**
     * Sets the point x and y coordinates
     * 
     * @param newX
     *            the new x coordinate
     * @param newY
     *            the new y coordinate
     */
    public void setXY(float newX, float newY) {
        x = newX;
        y = newY;
        valid = isValidNumber(x) && isValidNumber(y);
    }

    /**
     * Sets the point text label
     * 
     * @param newLabel
     *            the new point text label
     */
    public void setLabel(String newLabel) {
        label = newLabel;
    }

    /**
     * Returns the point x coordinate
     * 
     * @return the point x coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the point y coordinate
     * 
     * @return the point y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the point text label
     * 
     * @return the point text label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns if the point coordinates are valid or not
     * 
     * @return true if the point coordinates are valid
     */
    public boolean getValid() {
        return valid;
    }

    /**
     * Returns if the point coordinates are valid or not
     * 
     * @return true if the point coordinates are valid
     */
    public boolean isValid() {
        return valid;
    }

}
