package grafica;

public class GPointsArray {
	private int n;
	private GPoint[] points;

	//
	// Constructor
	// /////////////

	public GPointsArray() {
		n = 0;
		points = new GPoint[50];
	}

	public GPointsArray(int initialSize) {
		n = 0;
		points = new GPoint[initialSize];
	}

	public GPointsArray(GPoint[] pts) {
		if (pts != null) {
			points = new GPoint[pts.length];
			n = 0;

			for (int i = 0; i < pts.length; i++) {
				if (pts[i] != null) {
					points[n] = new GPoint(pts[i]);
					n++;
				}
			}
		} else {
			n = 0;
			points = new GPoint[50];
		}
	}

	public GPointsArray(GPointsArray pts) {
		if (pts != null) {
			n = pts.getNPoints();
			points = new GPoint[n];

			for (int i = 0; i < n; i++) {
				points[i] = new GPoint(pts.get(i));
			}
		} else {
			n = 0;
			points = new GPoint[50];
		}
	}

	//
	// Methods
	// //////////

	public void add(float x, float y, String label) {
		if (n + 1 > points.length) {
			points = extendArray(points, n, 50);
		}

		points[n] = new GPoint(x, y, label);
		n++;
	}

	public void add(float x, float y) {
		add(x, y, "");
	}

	public void add(GPoint p) {
		if (p != null) {
			add(p.getX(), p.getY(), p.getLabel());
		}
	}

	public void add(float[] x, float[] y, String[] labels) {
		if (x != null && y != null && labels != null && x.length == y.length
				&& x.length == labels.length) {
			if (n + x.length > points.length) {
				points = extendArray(points, n, x.length);
			}

			for (int i = 0; i < x.length; i++) {
				points[n] = new GPoint(x[i], y[i], labels[i]);
				n++;
			}
		}
	}

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

	public void add(GPoint[] pts) {
		if (pts != null) {
			if (n + pts.length > points.length) {
				points = extendArray(points, n, pts.length);
			}

			for (int i = 0; i < pts.length; i++) {
				if (pts[i] != null) {
					points[n] = new GPoint(pts[i].getX(), pts[i].getY(),
							pts[i].getLabel());
					n++;
				}
			}
		}
	}

	public void add(GPointsArray pts) {
		if (pts != null) {
			if (n + pts.getNPoints() > points.length) {
				points = extendArray(points, n, pts.getNPoints());
			}

			for (int i = 0; i < pts.getNPoints(); i++) {
				points[n] = new GPoint(pts.get(i));
				n++;
			}
		}
	}

	public void removeInvalidPoints() {
		int counter = 0;

		for (int i = 0; i < n; i++) {
			if (points[i].isValid()) {
				points[counter] = points[i];
				counter++;
			}
		}

		n = counter;
	}

	public void removeInvalidPointsAtExtremes() {
		boolean started = false;
		int lastValidIndexAdded = -1;
		int counter = 0;

		for (int i = 0; i < n; i++) {
			if (points[i].isValid()) {
				points[counter] = points[i];
				lastValidIndexAdded = counter;
				started = true;
				counter++;
			} else if (started) {
				points[counter] = points[i];
				counter++;
			}
		}

		n = lastValidIndexAdded + 1;
	}

	private GPoint[] extendArray(GPoint[] pts, int nPoints, int step) {
		GPoint[] result = new GPoint[nPoints + step];

		for (int i = 0; i < nPoints; i++) {
			result[i] = pts[i];
		}

		return result;
	}

	//
	// Setters
	// //////////

	public void setX(int i, float x) {
		points[i].setX(x);
	}

	public void setY(int i, float y) {
		points[i].setY(y);
	}

	public void setXY(int i, float x, float y) {
		points[i].setXY(x, y);
	}

	public void setLabel(int i, String label) {
		points[i].setLabel(label);
	}

	public void set(int i, GPoint p) {
		if (p != null) {
			points[i] = new GPoint(p);
		}
	}

	public void setNPoints(int nPoints) {
		if (nPoints < n) {
			for (int i = nPoints; i < n; i++) {
				points[i] = null;
			}

			n = nPoints;
		}
	}

	//
	// Getters
	// //////////

	public int getNPoints() {
		return n;
	}

	public GPoint get(int i) {
		return (i >= 0 && i < n) ? points[i] : null;
	}

	public float getX(int i) {
		return (i >= 0 && i < n) ? points[i].getX() : 0;
	}

	public float getY(int i) {
		return (i >= 0 && i < n) ? points[i].getY() : 0;
	}

	public String getLabel(int i) {
		return (i >= 0 && i < n) ? points[i].getLabel() : "";
	}

	public boolean getValid(int i) {
		return (i >= 0 && i < n) ? points[i].getValid() : false;
	}

	public boolean isValid(int i) {
		return (i >= 0 && i < n) ? points[i].isValid() : false;
	}

	public GPoint getLastPoint() {
		return (n > 0) ? points[n - 1] : null;
	}

}
