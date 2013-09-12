package grafica;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class GTitle implements PConstants {
	private final PApplet applet;

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

	public GTitle(PApplet applet, float[] dim) {
		this.applet = applet;
		this.dim = dim.clone();
		relativePos = 0.5f;
		screenPos = relativePos * dim[0];
		offset = 10;

		text = "";
		textAlignment = CENTER;
		fontName = "SansSerif.bold";
		fontColor = applet.color(100);
		fontSize = 13;
		font = applet.createFont(fontName, fontSize);
	}

	//
	// Methods
	// //////////

	public void draw() {
		applet.pushStyle();
		applet.textFont(font);
		applet.textSize(fontSize);
		applet.fill(fontColor);
		applet.noStroke();
		applet.textAlign(textAlignment, BOTTOM);
		applet.text(text, screenPos, -offset - dim[1]);
		applet.popStyle();
	}

	//
	// Setters
	// //////////

	public void setDim(float[] newDim) {
		if (newDim != null && newDim.length == 2 && newDim[0] > 0
				&& newDim[1] > 0) {
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
