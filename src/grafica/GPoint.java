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
