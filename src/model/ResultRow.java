package model;

public class ResultRow {
    private final String pid;
    private final int arrival;
    private final int burst;
    private final int priority;
    private final int start;
    private final int completion;
    private final int waiting;
    private final int turnaround;
    private final int response;

    public ResultRow(String pid, int arrival, int burst, int priority, int start,
                     int completion, int waiting, int turnaround, int response) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.start = start;
        this.completion = completion;
        this.waiting = waiting;
        this.turnaround = turnaround;
        this.response = response;
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

    public int getStart() {
        return start;
    }

    public int getCompletion() {
        return completion;
    }

    public int getWaiting() {
        return waiting;
    }

    public int getTurnaround() {
        return turnaround;
    }

    public int getResponse() {
        return response;
    }
}
