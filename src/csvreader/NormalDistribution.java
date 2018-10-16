package csvreader;

public class NormalDistribution {
	private final double mean;
    private final double variance;

    public NormalDistribution(double mean, double variance) {
        if (variance < 0.0) {
            throw new IllegalArgumentException("Variance must be non-negative. Given variance: " + variance);
        }
        this.mean = mean;
        this.variance = variance;
    }

    public double frequencyOf(double value) {
        if (this.variance == 0.0) {
            return this.mean == value ? 1.0 : 0.0;
        }
        return Math.exp(-0.5 * Math.pow(value - this.mean, 2.0) / this.variance)
                / Math.sqrt(2.0 * Math.PI * this.variance);
    }
}
