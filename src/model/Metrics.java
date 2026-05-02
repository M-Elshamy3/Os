package model;

public class Metrics {
    private final double avgWT;
    private final double avgTAT;
    private final double avgRT;

    public Metrics(double avgWT, double avgTAT, double avgRT) {
        this.avgWT = avgWT;
        this.avgTAT = avgTAT;
        this.avgRT = avgRT;
    }

    public double getAvgWT() {
        return avgWT;
    }

    public double getAvgTAT() {
        return avgTAT;
    }

    public double getAvgRT() {
        return avgRT;
    }
}
