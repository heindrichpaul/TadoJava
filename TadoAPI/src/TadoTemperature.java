public class TadoTemperature {
	private double celsius;
	private double fahrenheit;

	public double getCelsius() {
		return celsius;
	}

	public double getFahrenheit() {
		return fahrenheit;
	}

	public TadoTemperature(double celsius, double fahrenheit) {
		super();
		this.celsius = celsius;
		this.fahrenheit = fahrenheit;
	}

	@Override
	public String toString() {
		return "TadoTemperature [celsius=" + celsius + ", fahrenheit=" + fahrenheit + "]";
	}
}