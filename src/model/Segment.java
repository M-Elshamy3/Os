package model;

public class Segment {
    private final String pid;
    private final int start;
    private int end;

    public Segment(String pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }

    public String getPid() {
        return pid;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
