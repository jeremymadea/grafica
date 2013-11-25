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
 * Array of points class.
 * 
 * @author Javier Gracia Carpio
 */
public class GPointsArray {
    protected static final int DEFAULT_SIZE = 50;
    protected int n;
    protected GPoint[] points;

    /**
     * Constructor
     */
    public GPointsArray() {
        n = 0;
        points = new GPoint[DEFAULT_SIZE];
    }

    /**
     * Constructor
     * 
     * @param initialSize
     *            the initial size for the array
     */
    public GPointsArray(int initialSize) {
        n = 0;

        if (initialSize >= 0) {
            points = new GPoint[initialSize];
        } else {
            points = new GPoint[DEFAULT_SIZE];
        }
    }

    /**
     * Constructor
     * 
     * @param points
     *            an array of points
     */
    public GPointsArray(GPoint[] points) {
        if (points != null) {
            n = 0;
            this.points = new GPoint[points.length];

            for (int i = 0; i < points.length; i++) {
                if (points[i] != null) {
                    this.points[n] = new GPoint(points[i]);
                    n++;
                }
            }
        } else {
            n = 0;
            this.points = new GPoint[DEFAULT_SIZE];
        }
    }

    /**
     * Constructor
     * 
     * @param points
     *            an array of points
     */
    public GPointsArray(GPointsArray points) {
        if (points != null) {
            n = points.getNPoints();
            this.points = new GPoint[n];

            for (int i = 0; i < n; i++) {
                this.points[i] = new GPoint(points.get(i));
            }
        } else {
            n = 0;
            this.points = new GPoint[DEFAULT_SIZE];
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
        if (x != null && y != null && labels != null && x.length == y.length && x.length == labels.length) {
            n = x.length;
            points = new GPoint[n];

            for (int i = 0; i < n; i++) {
                points[i] = new GPoint(x[i], y[i], labels[i]);
            }
        } else {
            n = 0;
            this.points = new GPoint[DEFAULT_SIZE];
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
        if (n + 1 > points.length) {
            points = extendArray(points, n, 50);
        }

        points[n] = new GPoint(x, y, label);
        n++;
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
        add(x, y, "");
    }

    /**
     * Adds a new point to the array
     * 
     * @param point
     *            the point
     */
    public void add(GPoint point) {
        if (point != null) {
            if (n + 1 > points.length) {
                points = extendArray(points, n, 50);
            }

            points[n] = point;
            n++;
        }
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
        if (x != null && y != null && labels != null && x.length == y.length && x.length == labels.length) {
            if (n + x.length > points.length) {
                points = extendArray(points, n, x.length);
            }

            for (int i = 0; i < x.length; i++) {
                points[n] = new GPoint(x[i], y[i], labels[i]);
                n++;
            }
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
        if (x != null && y != null && x.length == y.length) {
            if (n + x.length > points.length) {
                points = extendArray(points, n, x.length);
            }

            for (int i = 0; i < x.length; i++) {
                points[n] = new GPoint(x[i], y[i]);
                n++;
            }
        }
    }

    /**
     * Adds a new set of points to the array
     * 
     * @param pts
     *            the new set of points
     */
    public void add(GPoint[] pts) {
        if (pts != null) {
            if (n + pts.length > points.length) {
                points = extendArray(points, n, pts.length);
            }

            for (int i = 0; i < pts.length; i++) {
                if (pts[i] != null) {
                    points[n] = pts[i];
                    n++;
                }
            }
        }
    }

    /**
     * Adds a new set of points to the array
     * 
     * @param pts
     *            the new set of points
     */
    public void add(GPointsArray pts) {
        if (pts != null) {
            if (n + pts.getNPoints() > points.length) {
                points = extendArray(points, n, pts.getNPoints());
            }

            for (int i = 0; i < pts.getNPoints(); i++) {
                points[n] = pts.get(i);
                n++;
            }
        }
    }

    /**
     * Removes invalid points from the array
     */
    public void removeInvalidPoints() {
        int counter = 0;

        for (int i = 0; i < n; i++) {
            if (points[i].isValid()) {
                points[counter] = points[i];
                counter++;
            }
        }

        for (int i = counter; i < n; i++) {
            points[i] = null;
        }

        n = counter;
    }

    /**
     * Extends an array of points by a given amount
     * 
     * @param pts
     *            the original array
     * @param nPoints
     *            the number of points in the original array
     * @param nIncrease
     *            the number of indices to extend
     * @return the extended array of points
     */
    protected GPoint[] extendArray(GPoint[] pts, int nPoints, int nIncrease) {
        GPoint[] newPts = new GPoint[nPoints + nIncrease];

        for (int i = 0; i < nPoints; i++) {
            newPts[i] = pts[i];
        }

        return newPts;
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
        if (i >= 0 && i < n) {
            points[i].setX(x);
        }
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
        if (i >= 0 && i < n) {
            points[i].setY(y);
        }
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
        if (i >= 0 && i < n) {
            points[i].setXY(x, y);
        }
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
        if (i >= 0 && i < n) {
            points[i].setLabel(label);
        }
    }

    /**
     * Replaces a specific point in the array by another point
     * 
     * @param i
     *            the index of the point to replace
     * @param point
     *            the new point
     */
    public void set(int i, GPoint point) {
        if (i >= 0 && i < n && point != null) {
            points[i] = point;
        }
    }

    /**
     * Sets the total number of points in the array
     * 
     * @param nPoints
     *            the new total number of points in the array
     */
    public void setNPoints(int nPoints) {
        if (nPoints >= 0 && nPoints < n) {
            for (int i = nPoints; i < n; i++) {
                points[i] = null;
            }

            n = nPoints;
        }
    }

    /**
     * Returns the total number of points in the array
     * 
     * @return the total number of points in the array
     */
    public int getNPoints() {
        return n;
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
        return (i >= 0 && i < n) ? points[i] : null;
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
        return (i >= 0 && i < n) ? points[i].getX() : 0;
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
        return (i >= 0 && i < n) ? points[i].getY() : 0;
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
        return (i >= 0 && i < n) ? points[i].getLabel() : "";
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
        return (i >= 0 && i < n) ? points[i].getValid() : false;
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
        return (i >= 0 && i < n) ? points[i].isValid() : false;
    }

    /**
     * Returns the latest point added to the array
     * 
     * @return the latest point added to the array
     */
    public GPoint getLastPoint() {
        return (n > 0) ? points[n - 1] : null;
    }

}
