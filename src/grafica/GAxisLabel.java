package grafica;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class GAxisLabel implements PConstants {
	private final PApplet applet;

	// General properties
	private final String type;
	private float[] dim;
	private float relativePos;
	private float screenPos;
	private float offset;
	private boolean rotate;

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

	public GAxisLabel(PApplet applet, String type, float[] dim) {
		this.applet = applet;
		this.type = (type.equals("x") || type.equals("y") || type.equals("top") || type
				.equals("right")) ? type : "x";
		this.dim = dim.clone();
		relativePos = 0.5f;
		screenPos = (type.equals("x") || type.equals("top")) ? relativePos
				* dim[0] : -relativePos * dim[1];
		offset = 35;
		rotate = (type.equals("x") || type.equals("top")) ? false : true;

		text = "";
		textAlignment = CENTER;
		fontName = "SansSerif.plain";
		fontColor = applet.color(0);
		fontSize = 13;
		font = applet.createFont(fontName, fontSize);
	}

	//
	// Methods
	// //////////

	public void draw() {
		if (type.equals("x"))
			drawAsXLabel();
		else if (type.equals("y"))
			drawAsYLabel();
		else if (type.equals("top"))
			drawAsTopLabel();
		else if (type.equals("right"))
			drawAsRightLabel();
	}

	private void drawAsXLabel() {
		applet.pushStyle();
		applet.textFont(font);
		applet.textSize(fontSize);
		applet.fill(fontColor);
		applet.noStroke();

		if (rotate) {
			applet.textAlign(RIGHT, CENTER);

			applet.pushMatrix();
			applet.translate(screenPos, offset);
			applet.rotate(-HALF_PI);
			applet.text(text, 0, 0);
			applet.popMatrix();
		} else {
			applet.textAlign(textAlignment, TOP);
			applet.text(text, screenPos, offset);
		}
		applet.popStyle();
	}

	private void drawAsYLabel() {
		applet.pushStyle();
		applet.textFont(font);
		applet.textSize(fontSize);
		applet.fill(fontColor);
		applet.noStroke();

		if (rotate) {
			applet.textAlign(textAlignment, BOTTOM);

			applet.pushMatrix();
			applet.translate(-offset, screenPos);
			applet.rotate(-HALF_PI);
			applet.text(text, 0, 0);
			applet.popMatrix();
		} else {
			applet.textAlign(RIGHT, CENTER);
			applet.text(text, -offset, screenPos);
		}
		applet.popStyle();
	}

	private void drawAsTopLabel() {
		applet.pushStyle();
		applet.textFont(font);
		applet.textSize(fontSize);
		applet.fill(fontColor);
		applet.noStroke();

		if (rotate) {
			applet.textAlign(LEFT, CENTER);

			applet.pushMatrix();
			applet.translate(screenPos, -offset - dim[1]);
			applet.rotate(-HALF_PI);
			applet.text(text, 0, 0);
			applet.popMatrix();
		} else {
			applet.textAlign(textAlignment, BOTTOM);
			applet.text(text, screenPos, -offset - dim[1]);
		}
		applet.popStyle();
	}

	private void drawAsRightLabel() {
		applet.pushStyle();
		applet.textFont(font);
		applet.textSize(fontSize);
		applet.fill(fontColor);
		applet.noStroke();

		if (rotate) {
			applet.textAlign(textAlignment, TOP);

			applet.pushMatrix();
			applet.translate(offset + dim[0], screenPos);
			applet.rotate(-HALF_PI);
			applet.text(text, 0, 0);
			applet.popMatrix();
		} else {
			applet.textAlign(LEFT, CENTER);
			applet.text(text, offset + dim[0], screenPos);
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
			screenPos = (type.equals("x") || type.equals("top")) ? relativePos
					* dim[0] : -relativePos * dim[1];
		}
	}

	public void setRelativePos(float newRelativePos) {
		relativePos = newRelativePos;
		screenPos = (type.equals("x") || type.equals("top")) ? relativePos
				* dim[0] : -relativePos * dim[1];
	}

	public void setOffset(float newOffset) {
		offset = newOffset;
	}

	public void setRotate(boolean newRotate) {
		rotate = newRotate;
	}

	public void setText(String newText) {
		text = newText;
	}

	public void setTextAlignment(int newTextAlignment) {
		if (newTextAlignment == CENTER || newTextAlignment == LEFT
				|| newTextAlignment == RIGHT) {
			textAlignment = newTextAlignment;
		}
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
