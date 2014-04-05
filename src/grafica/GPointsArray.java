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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Array of points class.
 * 
 * @author Javier Gracia Carpio
 */
public class GPointsArray {
    protected ArrayList<GPoint> points;

    /**
     * Constructor
     */
    public GPointsArray() {
        points = new ArrayList<GPoint>();
    }

    /**
     * Constructor
     * 
     * @param initialSize
     *            the initial estimate for the size of the array
     */
    public GPointsArray(int initialSize) {
        points = new ArrayList<GPoint>(initialSize);
    }

    /**
     * Constructor
     * 
     * @param points
     *            an array of points
     */
    public GPointsArray(GPoint[] points) {
        this.points = new ArrayList<GPoint>(points.length);

        for (int i = 0; i < points.length; i++) {
            if (points[i] != null) {
                this.points.add(new GPoint(points[i]));
            }
        }
    }

    /**
     * Constructor
     * 
     * @param points
     *            an array of points
     */
    public GPointsArray(GPointsArray points) {
        this.points = new ArrayList<GPoint>(points.getNPoints());

        for (int i = 0; i < points.getNPoints(); i++) {
            this.points.add(new GPoint(points.get(i)));
        }
    }

    /**
     * Constructor
     * 
     * @param x
     *            the points x coordinates
     * @param y
     *            the points y coordinates
     * @param labels
     *            the points text labels
     */
    public GPointsArray(float[] x, float[] y, String[] labels) {
        points = new ArrayList<GPoint>(x.length);

        for (int i = 0; i < x.length; i++) {
            points.add(new GPoint(x[i], y[i], labels[i]));
        }
    }

    /**
     * Adds a new point to the array
     * 
     * @param x
     *            the point x coordinate
     * @param y
     *            the point y coordinate
     * @param label
     *            the point text label
     */
    public void add(float x, float y, String label) {
        points.add(new GPoint(x, y, label));
    }

    /**
     * Adds a new point to the array
     * 
     * @param x
     *            the point x coordinate
     * @param y
     *            the point y coordinate
     */
    public void add(float x, float y) {
        points.add(new GPoint(x, y, ""));
    }

    /**
     * Adds a new point to the array
     * 
     * @param point
     *            the point
     */
    public void add(GPoint point) {
        points.add(new GPoint(point));
    }

    /**
     * Adds a new set of points to the array
     * 
     * @param x
     *            the points x coordinates
     * @param y
     *            the points y coordinates
     * @param labels
     *            the points text labels
     */
    public void add(float[] x, float[] y, String[] labels) {
        for (int i = 0; i < x.length; i++) {
            points.add(new GPoint(x[i], y[i], labels[i]));
        }
    }

    /**
     * Adds a new set of points to the array
     * 
     * @param x
     *            the points x coordinates
     * @param y
     *            the points y coordinates
     */
    public void add(float[] x, float[] y) {
        for (int i = 0; i < x.length; i++) {
            points.add(new GPoint(x[i], y[i]));
        }
    }

    /**
     * Adds a new set of points to the array
     * 
     * @param pts
     *            the new set of points
     */
    public void add(GPoint[] pts) {
        for (int i = 0; i < pts.length; i++) {
            points.add(new GPoint(pts[i]));
        }
    }

    /**
     * Adds a new set of points to the array
     * 
     * @param pts
     *            the new set of points
     */
    public void add(GPointsArray pts) {
        for (int i = 0; i < pts.getNPoints(); i++) {
            points.add(new GPoint(pts.get(i)));
        }
    }

    /**
     * Removes invalid points from the array
     */
    public void removeInvalidPoints() {
        for (Iterator<GPoint> it = points.iterator(); it.hasNext();) {
            if (!it.next().isValid()) {
                it.remove();
            }
        }
    }

    /**
     * Sets all the points in the array
     * 
     * @param pts
     *            the new points. The number of points could differ from the
     *            original.
     */
    public void set(GPointsArray pts) {
        if (pts.getNPoints() == points.size()) {
            for (int i = 0; i < points.size(); i++) {
                points.get(i).set(pts.get(i));
            }
        } else if (pts.getNPoints() > points.size()) {
            for (int i = 0; i < points.size(); i++) {
                points.get(i).set(pts.get(i));
            }

            for (int i = points.size(); i < pts.getNPoints(); i++) {
                points.add(new GPoint(pts.get(i)));
            }
        } else {
            for (int i = 0; i < pts.getNPoints(); i++) {
                points.get(i).set(pts.get(i));
            }

            points.subList(pts.getNPoints(), points.size()).clear();
        }
    }

    /**
     * Sets the x and y coordinates and the label of a point with those from
     * another point
     * 
     * @param i
     *            the point index. If the index equals the array size, it will
     *            add a new point to the array.
     * @param point
     *            the point to use
     */
    public void set(int i, GPoint point) {
        if (i < points.size()) {
            points.get(i).set(point);
        } else if (i == points.size()) {
            points.add(new GPoint(point));
        }
    }

    /**
     * Sets the x and y coordinates of a specific point in the array
     * 
     * @param i
     *            the point index. If the index equals the array size, it will
     *            add a new point to the array.
     * @param x
     *            the point new x coordinate
     * @param y
     *            the point new y coordinate
     * @param label
     *            the point new text label
     */
    public void set(int i, float x, float y, String label) {
        if (i < points.size()) {
            points.get(i).set(x, y, label);
        } else if (i == points.size()) {
            points.add(new GPoint(x, y, label));
        }
    }

    /**
     * Sets the x coordinate of a specific point in the array
     * 
     * @param i
     *            the point index
     * @param x
     *            the point new x coordinate
     */
    public void setX(int i, float x) {
        points.get(i).setX(x);
    }

    /**
     * Sets the y coordinate of a specific point in the array
     * 
     * @param i
     *            the point index
     * @param y
     *            the point new y coordinate
     */
    public void setY(int i, float y) {
        points.get(i).setY(y);
    }

    /**
     * Sets the x and y coordinates of a specific point in the array
     * 
     * @param i
     *            the point index
     * @param x
     *            the point new x coordinate
     * @param y
     *            the point new y coordinate
     */
    public void setXY(int i, float x, float y) {
        points.get(i).setXY(x, y);
    }

    /**
     * Sets the text label of a specific point in the array
     * 
     * @param i
     *            the point index
     * @param label
     *            the point new text label
     */
    public void setLabel(int i, String label) {
        points.get(i).setLabel(label);
    }

    /**
     * Sets the total number of points in the array
     * 
     * @param nPoints
     *            the new total number of points in the array. It should be
     *            smaller than the current number.
     */
    public void setNPoints(int nPoints) {
        points.subList(nPoints, points.size()).clear();
    }

    /**
     * Returns the total number of points in the array
     * 
     * @return the total number of points in the array
     */
    public int getNPoints() {
        return points.size();
    }

    /**
     * Returns a given point in the array
     * 
     * @param i
     *            the point index in the array
     * 
     * @return the point reference
     */
    public GPoint get(int i) {
        return points.get(i);
    }

    /**
     * Returns the x coordinate of a point in the array
     * 
     * @param i
     *            the point index in the array
     * 
     * @return the point x coordinate
     */
    public float getX(int i) {
        return points.get(i).getX();
    }

    /**
     * Returns the y coordinate of a point in the array
     * 
     * @param i
     *            the point index in the array
     * 
     * @return the point y coordinate
     */
    public float getY(int i) {
        return points.get(i).getY();
    }

    /**
     * Returns the text label of a point in the array
     * 
     * @param i
     *            the point index in the array
     * 
     * @return the point text label
     */
    public String getLabel(int i) {
        return points.get(i).getLabel();
    }

    /**
     * Returns if a point in the array is valid or not
     * 
     * @param i
     *            the point index in the array
     * 
     * @return true if the point is valid
     */
    public boolean getValid(int i) {
        return points.get(i).getValid();
    }

    /**
     * Returns if a point in the array is valid or not
     * 
     * @param i
     *            the point index in the array
     * 
     * @return true if the point is valid
     */
    public boolean isValid(int i) {
        return points.get(i).isValid();
    }

    /**
     * Returns the latest point added to the array
     * 
     * @return the latest point added to the array
     */
    public GPoint getLastPoint() {
        return (points.size() > 0) ? points.get(points.size() - 1) : null;
    }
}
