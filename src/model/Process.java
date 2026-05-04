package model;

public class Process {
    private final String pid;
    private final int arrival;
    private final int burst;
    private final int priority;
    private final int inputOrder;

    public Process(String pid, int arrival, int burst, int priority, int inputOrder) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.inputOrder = inputOrder;
    }

    public String getPid() {
        return pid;
    }

    public int getArrival() {
        return arrival;
    }

    public int getBurst() {
        return burst;
    }

    public int getPriority() {
        return priority;
    }

    public int getInputOrder() {
        return inputOrder;
    }
}
