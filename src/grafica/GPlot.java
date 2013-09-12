package grafica;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class GPlot implements PConstants {
	private final PApplet applet;

	// General properties
	private float[] pos;
	private float[] dim;
	private float[] mar;
	private float[] innerDim;
	private float[] xLim;
	private float[] yLim;
	private boolean fixedXLim;
	private boolean fixedYLim;
	private boolean xLog;
	private boolean yLog;

	// Layers
	private final GLayer mainLayer;
	private final ArrayList<GLayer> layerList;

	// Format properties
	private int bgColor;
	private int innerBgColor;
	private int innerLineColor;
	private float innerLineWidth;

	// Grid properties
	private int gridLineColor;
	private float gridLineWidth;

	// Axis properties
	private final GAxis xAxis;
	private final GAxis topAxis;
	private final GAxis yAxis;
	private final GAxis rightAxis;

	// Title properties
	private final GTitle upperTitle;

	//
	// Constructor
	// /////////////

	public GPlot(PApplet applet) {
		this.applet = applet;

		pos = new float[] { 0, 0 };
		dim = new float[] { 450, 300 };
		mar = new float[] { 60, 90, 40, 20 };
		innerDim = new float[] { dim[0] - mar[1] - mar[3],
				dim[1] - mar[0] - mar[2] };
		xLim = new float[] { 0, 1 };
		yLim = new float[] { 0, 1 };
		fixedXLim = false;
		fixedYLim = false;
		xLog = false;
		yLog = false;

		mainLayer = new GLayer(applet, "main layer", innerDim, xLim, yLim,
				xLog, yLog);
		layerList = new ArrayList<GLayer>();

		bgColor = applet.color(255, 0);
		innerBgColor = applet.color(200, 50);
		innerLineColor = applet.color(150, 100);
		innerLineWidth = 1;

		gridLineColor = applet.color(150, 100);
		gridLineWidth = 1;

		xAxis = new GAxis(applet, "x", innerDim, xLim, xLog);
		topAxis = new GAxis(applet, "top", innerDim, xLim, xLog);
		yAxis = new GAxis(applet, "y", innerDim, yLim, yLog);
		rightAxis = new GAxis(applet, "right", innerDim, yLim, yLog);

		upperTitle = new GTitle(applet, innerDim);
	}

	//
	// Methods
	// //////////

	public void addLayer(GLayer l) {
		if (l != null) {
			// Check that it is the only layer with that id
			String id = l.getId();
			boolean sameId = false;

			for (int i = 0; i < layerList.size(); i++) {
				if (((GLayer) layerList.get(i)).isId(id)) {
					sameId = true;
					break;
				}
			}

			// Add the layer to the list
			if (!sameId) {
				l.setDim(innerDim);
				l.setLimitsAndLog(xLim, yLim, xLog, yLog);
				layerList.add(l);
			} else {
				PApplet.println("A layer with the same id exists. Please change the id and try to add it again.");
			}
		}
	}

	public void addLayer(String id, GPointsArray pts) {
		// Check that it is the only layer with that id
		boolean sameId = false;

		for (int i = 0; i < layerList.size(); i++) {
			if (((GLayer) layerList.get(i)).isId(id)) {
				sameId = true;
				break;
			}
		}

		// Add the layer to the list
		if (!sameId) {
			GLayer l = new GLayer(applet, id, innerDim, xLim, yLim, xLog, yLog);
			l.setPoints(pts);
			layerList.add(l);
		} else {
			PApplet.println("A layer with the same id exists. Please change the id and try to add it again.");
		}
	}

	public void removeLayer(String id) {
		int index = -1;

		for (int i = 0; i < layerList.size(); i++) {
			if (((GLayer) layerList.get(i)).isId(id)) {
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

	private float[] getScreenPosAt(float x, float y) {
		float xScreen = x - pos[0] - mar[1];
		float yScreen = y - pos[1] - mar[2] - innerDim[1];

		return new float[] { xScreen, yScreen };
	}

	public GPoint getPointAt(float x, float y) {
		float[] screenPos = getScreenPosAt(x, y);

		return mainLayer.getPointAtScreenPos(screenPos[0], screenPos[1]);
	}

	public float[] getValueAt(float x, float y) {
		float[] screenPos = getScreenPosAt(x, y);

		return mainLayer.screenToValue(screenPos[0], screenPos[1]);
	}

	public float[] getRelativePosAt(float x, float y) {
		float[] screenPos = getScreenPosAt(x, y);

		return new float[] { screenPos[0] / innerDim[0],
				-screenPos[1] / innerDim[1] };
	}

	public boolean overPlot(float x, float y) {
		return (x >= pos[0]) && (x <= pos[0] + dim[0]) && (y >= pos[1])
				&& (y <= pos[1] + dim[1]);
	}

	public boolean overInnerPlot(float x, float y) {
		return (x >= pos[0] + mar[1]) && (x <= pos[0] + dim[0] - mar[3])
				&& (y >= pos[1] + mar[2]) && (y <= pos[1] + dim[1] - mar[0]);
	}

	private float[] obtainXLim(GPointsArray pts) {
		// Find the points limits
		float[] lim = new float[] { Float.MAX_VALUE, -Float.MAX_VALUE };

		for (int i = 0; i < pts.getNPoints(); i++) {
			if (pts.isValid(i)) {
				// Use the point if it's inside, and it's not negative if the
				// scale is logarithmic
				float x = pts.getX(i);
				float y = pts.getY(i);
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

		// Check that the new limits make sense and increase the range a bit
		if (lim[1] > lim[0]) {
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

	private float[] obtainYLim(GPointsArray pts) {
		// Find the points limits
		float[] lim = new float[] { Float.MAX_VALUE, -Float.MAX_VALUE };

		for (int i = 0; i < pts.getNPoints(); i++) {
			if (pts.isValid(i)) {
				// Use the point if it's inside, and it's not negative if the
				// scale is logarithmic
				float x = pts.getX(i);
				float y = pts.getY(i);
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

		// Check that the new limits make sense and increase the range a bit
		if (lim[1] > lim[0]) {
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

	public void moveHorizontalAxes(float deltaScreen) {
		// Obtain the new x limits
		if (xLog) {
			float delta = PApplet.exp(PApplet.log(xLim[1] / xLim[0])
					* deltaScreen / innerDim[0]);
			xLim[0] *= delta;
			xLim[1] *= delta;
		} else {
			float delta = (xLim[1] - xLim[0]) * deltaScreen / innerDim[0];
			xLim[0] += delta;
			xLim[1] += delta;
		}

		// Fix the limits
		fixedXLim = true;

		// Move the horizontal axes
		xAxis.move(xLim);
		topAxis.move(xLim);

		// Update the vertical axes if needed
		if (!fixedYLim) {
			yLim = obtainYLim(mainLayer.getPointsRef());
			yAxis.setLim(yLim);
			rightAxis.setLim(yLim);
		}

		// Update the layers
		mainLayer.setLimits(xLim, yLim);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.setLimits(xLim, yLim);
		}
	}

	public void moveVerticalAxes(float deltaScreen) {
		// Obtain the new y limits
		if (yLog) {
			float delta = PApplet.exp(PApplet.log(yLim[1] / yLim[0])
					* deltaScreen / innerDim[1]);
			yLim[0] *= delta;
			yLim[1] *= delta;
		} else {
			float delta = (yLim[1] - yLim[0]) * deltaScreen / innerDim[1];
			yLim[0] += delta;
			yLim[1] += delta;
		}

		// Fix the limits
		fixedYLim = true;

		// Move the vertical axes
		yAxis.move(yLim);
		rightAxis.move(yLim);

		// Update the horizontal axes if needed
		if (!fixedXLim) {
			xLim = obtainXLim(mainLayer.getPointsRef());
			xAxis.setLim(xLim);
			topAxis.setLim(xLim);
		}

		// Update the layers
		mainLayer.setLimits(xLim, yLim);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.setLimits(xLim, yLim);
		}
	}

	public void zoom(float times, float x, float y) {
		// Calculate the new limits
		float[] newCenter = getValueAt(x, y);

		if (xLog) {
			float delta = PApplet.exp(PApplet.log(xLim[1] / xLim[0])
					/ (2 * times));
			xLim = new float[] { newCenter[0] / delta, newCenter[0] * delta };
		} else {
			float delta = (xLim[1] - xLim[0]) / (2 * times);
			xLim = new float[] { newCenter[0] - delta, newCenter[0] + delta };
		}

		if (yLog) {
			float delta = PApplet.exp(PApplet.log(yLim[1] / yLim[0])
					/ (2 * times));
			yLim = new float[] { newCenter[1] / delta, newCenter[1] * delta };
		} else {
			float delta = (yLim[1] - yLim[0]) / (2 * times);
			yLim = new float[] { newCenter[1] - delta, newCenter[1] + delta };
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
			GLayer l = (GLayer) layerList.get(i);
			l.setLimits(xLim, yLim);
		}
	}

	private void align(float[] valueScreenPos, float[] newScreenPos) {
		// Calculate the new limits
		float deltaXScreen = valueScreenPos[0] - newScreenPos[0];
		float deltaYScreen = valueScreenPos[1] - newScreenPos[1];

		if (xLog) {
			float delta = PApplet.exp(PApplet.log(xLim[1] / xLim[0])
					* deltaXScreen / innerDim[0]);
			xLim = new float[] { xLim[0] * delta, xLim[1] * delta };
		} else {
			float delta = (xLim[1] - xLim[0]) * deltaXScreen / innerDim[0];
			xLim = new float[] { xLim[0] + delta, xLim[1] + delta };
		}

		if (yLog) {
			float delta = PApplet.exp(-PApplet.log(yLim[1] / yLim[0])
					* deltaYScreen / innerDim[1]);
			yLim = new float[] { yLim[0] * delta, yLim[1] * delta };
		} else {
			float delta = -(yLim[1] - yLim[0]) * deltaYScreen / innerDim[1];
			yLim = new float[] { yLim[0] + delta, yLim[1] + delta };
		}

		// Fix the limits
		fixedXLim = true;
		fixedYLim = true;

		// Move the horizontal and vertical axes
		xAxis.move(xLim);
		topAxis.move(xLim);
		yAxis.move(yLim);
		rightAxis.move(yLim);

		// Update the layers
		mainLayer.setLimits(xLim, yLim);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.setLimits(xLim, yLim);
		}
	}

	public void align(float[] value, float x, float y) {
		float[] valueScreenPos = mainLayer.valueToScreen(value[0], value[1]);
		float[] newScreenPos = getScreenPosAt(x, y);

		align(valueScreenPos, newScreenPos);
	}

	public void center(float x, float y) {
		float[] valueScreenPos = getScreenPosAt(x, y);
		float[] newScreenPos = new float[] { innerDim[0] / 2, -innerDim[1] / 2 };

		align(valueScreenPos, newScreenPos);
	}

	public void startHistogram(String histType) {
		mainLayer.startHistogram(histType);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.startHistogram(histType);
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
		applet.pushMatrix();
		applet.translate(pos[0] + mar[1], pos[1] + mar[2] + innerDim[1]);
	}

	public void endDraw() {
		applet.popMatrix();
	}

	public void drawBackground() {
		applet.pushStyle();
		applet.fill(bgColor);
		applet.noStroke();
		applet.rect(-mar[1], -mar[2] - innerDim[1], dim[0], dim[1]);
		applet.popStyle();
	}

	public void drawInnerRegion() {
		applet.pushStyle();
		applet.fill(innerBgColor);
		applet.stroke(innerLineColor);
		applet.strokeWeight(innerLineWidth);
		applet.strokeCap(SQUARE);
		applet.rect(0, -innerDim[1], innerDim[0], innerDim[1]);
		applet.popStyle();
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
		upperTitle.draw();
	}

	public void drawPoints() {
		mainLayer.drawPoints();

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.drawPoints();
		}
	}

	public void drawPoints(PShape s) {
		mainLayer.drawPoints(s);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.drawPoints(s);
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
			GLayer l = (GLayer) layerList.get(i);
			l.drawLines();
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
			GLayer l = (GLayer) layerList.get(i);
			l.drawFilledLines(type, referenceValue);
		}
	}

	public void drawLabel(GPoint p) {
		mainLayer.drawLabel(p);
	}

	public void drawLabelsAt(float x, float y) {
		float[] screenPos = getScreenPosAt(x, y);
		mainLayer.drawLabelAtScreenPos(screenPos[0], screenPos[1]);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.drawLabelAtScreenPos(screenPos[0], screenPos[1]);
		}
	}

	public void drawGridLines(String type) {
		applet.pushStyle();
		applet.noFill();
		applet.stroke(gridLineColor);
		applet.strokeWeight(gridLineWidth);
		applet.strokeCap(SQUARE);

		if (type.equals("both") || type.equals("vertical")) {
			float[] xScreenTicks = xAxis.getScreenTicksRef();

			for (int i = 0; i < xScreenTicks.length; i++) {
				if (xScreenTicks[i] >= 0 && xScreenTicks[i] <= innerDim[0]) {
					applet.line(xScreenTicks[i], 0, xScreenTicks[i],
							-innerDim[1]);
				}
			}
		}

		if (type.equals("both") || type.equals("horizontal")) {
			float[] yScreenTicks = yAxis.getScreenTicksRef();

			for (int i = 0; i < yScreenTicks.length; i++) {
				if (-yScreenTicks[i] >= 0 && -yScreenTicks[i] <= innerDim[1]) {
					applet.line(0, yScreenTicks[i], innerDim[0],
							yScreenTicks[i]);
				}
			}
		}
		applet.popStyle();
	}

	public void drawHistogram() {
		mainLayer.drawHistogram();

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.drawHistogram();
		}
	}

	public void drawPolygon(GPointsArray poly, int col) {
		mainLayer.drawPolygon(poly, col);
	}

	public void drawAnnotation(String text, float x, float y, int horAlign,
			int verAlign) {
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

	public void setDim(float[] newDim) {
		if (newDim != null && newDim.length == 2 && newDim[0] > 0
				&& newDim[1] > 0) {
			// Make sure that the new inner dimensions are positive
			float[] newInnerDim = new float[] { newDim[0] - mar[1] - mar[3],
					newDim[1] - mar[0] - mar[2] };

			if (newInnerDim[0] > 0 && newInnerDim[1] > 0) {
				dim = newDim.clone();
				innerDim = newInnerDim;
				xAxis.setDim(innerDim);
				topAxis.setDim(innerDim);
				yAxis.setDim(innerDim);
				rightAxis.setDim(innerDim);
				upperTitle.setDim(innerDim);

				// Update the layers
				mainLayer.setDim(innerDim);

				for (int i = 0; i < layerList.size(); i++) {
					GLayer l = (GLayer) layerList.get(i);
					l.setDim(innerDim);
				}
			}
		}
	}

	public void setMar(float[] newMar) {
		if (newMar != null && newMar.length == 4) {
			// Make sure that the new inner dimensions are positive
			float[] newInnerDim = new float[] { dim[0] - newMar[1] - newMar[3],
					dim[1] - newMar[0] - newMar[2] };

			if (newInnerDim[0] > 0 && newInnerDim[1] > 0) {
				mar = newMar.clone();
				innerDim = newInnerDim;
				xAxis.setDim(innerDim);
				topAxis.setDim(innerDim);
				yAxis.setDim(innerDim);
				rightAxis.setDim(innerDim);
				upperTitle.setDim(innerDim);

				// Update the layers
				mainLayer.setDim(innerDim);

				for (int i = 0; i < layerList.size(); i++) {
					GLayer l = (GLayer) layerList.get(i);
					l.setDim(innerDim);
				}
			}
		}
	}

	public void setInnerDim(float[] newInnerDim) {
		if (newInnerDim != null && newInnerDim.length == 2
				&& newInnerDim[0] > 0 && newInnerDim[1] > 0) {
			// Make sure that the new dimensions are positive
			float[] newDim = new float[] { newInnerDim[0] + mar[1] + mar[3],
					newInnerDim[1] + mar[0] + mar[2] };

			if (newDim[0] > 0 && newDim[1] > 0) {
				dim = newDim;
				innerDim = newInnerDim.clone();
				xAxis.setDim(innerDim);
				topAxis.setDim(innerDim);
				yAxis.setDim(innerDim);
				rightAxis.setDim(innerDim);
				upperTitle.setDim(innerDim);

				// Update the layers
				mainLayer.setDim(innerDim);

				for (int i = 0; i < layerList.size(); i++) {
					GLayer l = (GLayer) layerList.get(i);
					l.setDim(innerDim);
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
					GLayer l = (GLayer) layerList.get(i);
					l.setLimits(xLim, yLim);
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
					GLayer l = (GLayer) layerList.get(i);
					l.setLimits(xLim, yLim);
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
				GLayer l = (GLayer) layerList.get(i);
				l.setLimitsAndLog(xLim, yLim, xLog, yLog);
			}
		}
	}

	public void setBgColor(int newBgColor) {
		bgColor = newBgColor;
	}

	public void setInnerBgColor(int newInnerBgColor) {
		innerBgColor = newInnerBgColor;
	}

	public void setInnerLineColor(int newInnerLineColor) {
		innerLineColor = newInnerLineColor;
	}

	public void setInnerLineWidth(float newInnerLineWidth) {
		if (newInnerLineWidth > 0) {
			innerLineWidth = newInnerLineWidth;
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
				GLayer l = (GLayer) layerList.get(i);
				l.setLimits(xLim, yLim);
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

	public void setHistType(String histType) {
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

	public void setAllFontProperties(String fontName, int fontColor,
			int fontSize) {
		xAxis.setAllFontProperties(fontName, fontColor, fontSize);
		topAxis.setAllFontProperties(fontName, fontColor, fontSize);
		yAxis.setAllFontProperties(fontName, fontColor, fontSize);
		rightAxis.setAllFontProperties(fontName, fontColor, fontSize);
		upperTitle.setFontProperties(fontName, fontColor, fontSize);

		mainLayer.setAllFontProperties(fontName, fontColor, fontSize);

		for (int i = 0; i < layerList.size(); i++) {
			GLayer l = (GLayer) layerList.get(i);
			l.setAllFontProperties(fontName, fontColor, fontSize);
		}
	}

	//
	// Getters
	// //////////

	public float[] getPos() {
		return pos.clone();
	}

	public float[] getDim() {
		return dim.clone();
	}

	public float[] getMar() {
		return mar.clone();
	}

	public float[] getInnerDim() {
		return innerDim.clone();
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
			if (((GLayer) layerList.get(i)).isId(id)) {
				index = i;
				break;
			}
		}

		if (index >= 0) {
			return (GLayer) layerList.get(index);
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
		return upperTitle;
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
