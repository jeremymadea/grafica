package grafica;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class GHistogram implements PConstants {
	private final PApplet applet;

	// General properties
	private float[] dim;
	private GPointsArray screenPoints;
	private float[] separations;
	private int[] bgColors;
	private int[] lineColors;
	private float[] lineWidths;
	private boolean vertical;
	private boolean visible;

	// Arrays
	private int nHistElements;
	private float[] histSeparations;
	private int[] histBgColors;
	private int[] histLineColors;
	private float[] histLineWidths;
	private float[] histLeftSides;
	private float[] histRightSides;

	// Labels properties
	private boolean drawLabels;
	private float labelsOffset;
	private boolean rotateLabels;
	private String fontName;
	private int fontColor;
	private int fontSize;
	private PFont font;

	//
	// Constructor
	// /////////////

	public GHistogram(PApplet applet, float[] dim, GPointsArray screenPoints) {
		this.applet = applet;

		this.dim = dim.clone();
		this.screenPoints = new GPointsArray(screenPoints);
		separations = new float[] { 2 };
		bgColors = new int[] { applet.color(150, 150, 255) };
		lineColors = new int[] { applet.color(100, 100, 255) };
		lineWidths = new float[] { 1 };
		vertical = true;
		visible = true;

		nHistElements = screenPoints.getNPoints();
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
		drawLabels = false;
		labelsOffset = 8;
		rotateLabels = false;
		fontName = "SansSerif.plain";
		fontColor = applet.color(0);
		fontSize = 11;
		font = applet.createFont(fontName, fontSize);
	}

	//
	// Methods
	// //////////

	private void updateSides() {
		if (nHistElements == 1) {
			histLeftSides[0] = (vertical) ? 0.2f * dim[0] : 0.2f * dim[1];
			histRightSides[0] = histLeftSides[0];
		} else if (nHistElements > 1) {
			// Calculate the differences between consecutive points
			float[] differences = new float[nHistElements - 1];

			if (vertical) {
				for (int i = 0; i < nHistElements - 1; i++) {
					if (screenPoints.isValid(i) && screenPoints.isValid(i + 1)) {
						differences[i] = (screenPoints.getX(i + 1)
								- screenPoints.getX(i) - histSeparations[i]) / 2;
					}
				}
			} else {
				for (int i = 0; i < nHistElements - 1; i++) {
					if (screenPoints.isValid(i) && screenPoints.isValid(i + 1)) {
						differences[i] = (screenPoints.getY(i + 1)
								- screenPoints.getY(i) - histSeparations[i]) / 2;
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

	public void draw(GPoint screenZeroPoint) {
		if (visible && screenZeroPoint != null) {
			// Calculate the baseline for the histogram
			float baseline = 0;

			if (screenZeroPoint.isValid()) {
				baseline = (vertical) ? screenZeroPoint.getY()
						: screenZeroPoint.getX();
			}

			// Draw the rectangles
			applet.pushStyle();
			applet.strokeCap(SQUARE);
			applet.rectMode(CORNERS);

			for (int i = 0; i < nHistElements; i++) {
				if (screenPoints.isValid(i)) {
					// Obtain the corners
					float x1, x2, y1, y2;

					if (vertical) {
						x1 = screenPoints.getX(i) - histLeftSides[i];
						x2 = screenPoints.getX(i) + histRightSides[i];
						y1 = screenPoints.getY(i);
						y2 = baseline;
					} else {
						x1 = baseline;
						x2 = screenPoints.getX(i);
						y1 = screenPoints.getY(i) - histLeftSides[i];
						y2 = screenPoints.getY(i) + histRightSides[i];
					}

					if (x1 < 0)
						x1 = 0;
					else if (x1 > dim[0])
						x1 = dim[0];

					if (-y1 < 0)
						y1 = 0;
					else if (-y1 > dim[1])
						y1 = -dim[1];

					if (x2 < 0)
						x2 = 0;
					else if (x2 > dim[0])
						x2 = dim[0];

					if (-y2 < 0)
						y2 = 0;
					else if (-y2 > dim[1])
						y2 = -dim[1];

					// Draw the rectangle
					applet.fill(histBgColors[i]);
					applet.stroke(histLineColors[i]);
					applet.strokeWeight(histLineWidths[i]);

					if (PApplet.abs(x2 - x1) > 2 * histLineWidths[i]
							&& PApplet.abs(y2 - y1) > 2 * histLineWidths[i]) {
						applet.rect(x1, y1, x2, y2);
					} else if ((vertical && x2 != x1 && !(y1 == y2 && (-y1 == 0 || -y1 == dim[1])))
							|| (!vertical && y2 != y1 && !(x1 == x2 && (x1 == 0 || x1 == dim[0])))) {
						applet.rect(x1, y1, x2, y2);
						applet.line(x1, y1, x1, y2);
						applet.line(x2, y1, x2, y2);
						applet.line(x1, y1, x2, y1);
						applet.line(x1, y2, x2, y2);
					}
				}
			}
			applet.popStyle();

			// Draw the labels
			if (drawLabels) {
				drawHistLabels();
			}
		}
	}

	private void drawHistLabels() {
		applet.pushStyle();
		applet.textFont(font);
		applet.textSize(fontSize);
		applet.fill(fontColor);
		applet.noStroke();

		if (vertical) {
			if (rotateLabels) {
				applet.textAlign(RIGHT, CENTER);

				for (int i = 0; i < nHistElements; i++) {
					if (screenPoints.isValid(i) && screenPoints.getX(i) >= 0
							&& screenPoints.getX(i) <= dim[0]) {
						applet.pushMatrix();
						applet.translate(screenPoints.getX(i), labelsOffset);
						applet.rotate(-HALF_PI);
						applet.text(screenPoints.getLabel(i), 0, 0);
						applet.popMatrix();
					}
				}
			} else {
				applet.textAlign(CENTER, TOP);

				for (int i = 0; i < nHistElements; i++) {
					if (screenPoints.isValid(i) && screenPoints.getX(i) >= 0
							&& screenPoints.getX(i) <= dim[0]) {
						applet.text(screenPoints.getLabel(i),
								screenPoints.getX(i), labelsOffset);
					}
				}
			}
		} else {
			if (rotateLabels) {
				applet.textAlign(CENTER, BOTTOM);

				for (int i = 0; i < nHistElements; i++) {
					if (screenPoints.isValid(i) && -screenPoints.getY(i) >= 0
							&& -screenPoints.getY(i) <= dim[1]) {
						applet.pushMatrix();
						applet.translate(-labelsOffset, screenPoints.getY(i));
						applet.rotate(-HALF_PI);
						applet.text(screenPoints.getLabel(i), 0, 0);
						applet.popMatrix();
					}
				}
			} else {
				applet.textAlign(RIGHT, CENTER);

				for (int i = 0; i < nHistElements; i++) {
					if (screenPoints.isValid(i) && -screenPoints.getY(i) >= 0
							&& -screenPoints.getY(i) <= dim[1]) {
						applet.text(screenPoints.getLabel(i), -labelsOffset,
								screenPoints.getY(i));
					}
				}
			}
		}
		applet.popStyle();
	}

	//
	// Setters
	// //////////

	public void setDim(float[] newDim) {
		if (newDim != null && newDim.length == 2 && newDim[0] > 0
				&& newDim[1] > 0) {
			dim = newDim.clone();
			updateSides();
		}
	}

	public void setScreenPoints(GPointsArray newScreenPoints) {
		if (newScreenPoints != null) {
			screenPoints = new GPointsArray(newScreenPoints);

			// Update the arrays
			nHistElements = screenPoints.getNPoints();

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

	public void setSeparations(float[] newSeparations) {
		if (newSeparations != null && newSeparations.length > 0) {
			separations = newSeparations.clone();

			for (int i = 0; i < histSeparations.length; i++) {
				histSeparations[i] = separations[i % separations.length];
			}

			updateSides();
		}
	}

	public void setBgColors(int[] newBgColors) {
		if (newBgColors != null && newBgColors.length > 0) {
			bgColors = newBgColors.clone();

			for (int i = 0; i < histBgColors.length; i++) {
				histBgColors[i] = bgColors[i % bgColors.length];
			}
		}
	}

	public void setLineColors(int[] newLineColors) {
		if (newLineColors != null && newLineColors.length > 0) {
			lineColors = newLineColors.clone();

			for (int i = 0; i < histLineColors.length; i++) {
				histLineColors[i] = lineColors[i % lineColors.length];
			}
		}
	}

	public void setLineWidths(float[] newLineWidths) {
		if (newLineWidths != null && newLineWidths.length > 0) {
			lineWidths = newLineWidths.clone();

			for (int i = 0; i < histLineWidths.length; i++) {
				histLineWidths[i] = lineWidths[i % lineWidths.length];
				if (histLineWidths[i] < 0)
					histLineWidths[i] = 0;
			}
		}
	}

	public void setType(String type) {
		if (type.equals("vertical") && !vertical) {
			vertical = true;
			updateSides();
		} else if (type.equals("horizontal") && vertical) {
			vertical = false;
			updateSides();
		}
	}

	public void setVisible(boolean newVisible) {
		visible = newVisible;
	}

	public void setDrawLabels(boolean newDrawLabels) {
		drawLabels = newDrawLabels;
	}

	public void setLabelsOffset(float newLabelsOffset) {
		labelsOffset = newLabelsOffset;
	}

	public void setRotateLabels(boolean newRotateLabels) {
		rotateLabels = newRotateLabels;
	}

	public void setFontName(String newFontName) {
		fontName = newFontName;
		font = applet.createFont(fontName, fontSize);
	}

	public void setFontColor(int newFontColor) {
		fontColor = newFontColor;
	}

	public void setFontSize(int newFontSize) {
		if (newFontSize > 0) {
			fontSize = newFontSize;
			font = applet.createFont(fontName, fontSize);
		}
	}

	public void setFontProperties(String newFontName, int newFontColor,
			int newFontSize) {
		if (newFontSize > 0) {
			fontName = newFontName;
			fontColor = newFontColor;
			fontSize = newFontSize;
			font = applet.createFont(fontName, fontSize);
		}
	}

}
