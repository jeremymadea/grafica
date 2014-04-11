
import grafica.*;

GPlot plot;

int step = 0;
int totalStepNumber = 100;
int lastStepTime = 0;
boolean clockwise = true;

float scale = 5;

void setup() {
  size(450, 450);

  // Create the plot
  plot = new GPlot(this);
  plot.setPos(25, 25);
  plot.setDim(300, 300);
  plot.setXLim(-1.2*scale, 1.2*scale);
  plot.setYLim(-1.2*scale, 1.2*scale);
  plot.getXAxis().setAxisLabelText("x axis");
  plot.getYAxis().setAxisLabelText("y axis");
  plot.setTitleText("Clockwise movement");

  // Prepare the first set of points for the plot
  int nPoints1 = totalStepNumber/10;
  GPointsArray points1 = new GPointsArray(nPoints1);

  for (int i = 0; i < nPoints1; i++) {
    points1.add(calculatePoint(step, totalStepNumber, scale));

    if (clockwise) {
      step++;
    }
    else {
      step--;
    }
  }

  lastStepTime = millis();

  // Prepare the second set of points for the plot
  int nPoints2 = totalStepNumber + 1;
  GPointsArray points2 = new GPointsArray(nPoints2);

  for (int i = 0; i < nPoints2; i++) {
    points2.add(calculatePoint(i, totalStepNumber, 0.9*scale));
  }

  // Add the two set of points to the plot
  plot.setPoints(points1);
  plot.addLayer("surface", points2);
}

void draw() {
  background(150);

  plot.beginDraw();
  plot.drawBackground();
  plot.drawBox();
  plot.drawXAxis();
  plot.drawYAxis();
  plot.drawTopAxis();
  plot.drawRightAxis();
  plot.drawTitle();
  plot.getMainLayer().drawPoints();
  plot.getLayer("surface").drawFilledContour(GPlot.HORIZONTAL, 0);
  plot.endDraw();
  
  // Add and remove new points
  if (millis() - lastStepTime > 100) {
    if (clockwise) {
      plot.addPoint(calculatePoint(step, totalStepNumber, scale));
      plot.removePoint(0);
      step++;
    }
    else {
      plot.addPoint(0, calculatePoint(step, totalStepNumber, scale));
      plot.removePoint(plot.getPointsRef().getNPoints() - 1);
      step--;
    }

    lastStepTime = millis();
  }
}

public void mouseClicked() {
  clockwise = !clockwise;

  if (clockwise) {
    step += plot.getPointsRef().getNPoints() + 1;
    plot.setTitleText("Clockwise movement");
  } 
  else {
    step -= plot.getPointsRef().getNPoints() + 1;
    plot.setTitleText("Anti-clockwise movement");
  }
}

public GPoint calculatePoint(float i, float n, float rad) {
  float delta = 0.1*cos(TWO_PI*10*i/n);
  float ang = TWO_PI*i/n;
  return new GPoint(rad*(1 + delta)*sin(ang), rad*(1 + delta)*cos(ang));
}
