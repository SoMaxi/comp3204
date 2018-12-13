package comp3204.Comp3204_cw3.run1;

//a custom class to store how far away is each training image from the test image. also saves the name of the training image (i.e. class/category)
public class Result{
	
	public String getBestGuess() {
		return bestGuess;
	}

	public void setBestGuess(String bestGuess) {
		this.bestGuess = bestGuess;
	}

	public int getIntName() {
		return intName;
	}

	public void setIntName(int intName) {
		this.intName = intName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	String label;
	double distance;
	String bestGuess;
	int intName;
	public Result(String label, double d) {

		this.label = label;
		this.distance = d;

	}
	
	
	public Result(String label, String bestGuess) {

		
		//saving the integer part of the image name
		this.intName = Integer.parseInt(label.substring(0, label.length() - 4));
		this.label = label;
		this.bestGuess = bestGuess;

	}
}
