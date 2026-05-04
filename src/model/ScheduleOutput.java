package model;

import java.util.List;

public class ScheduleOutput {
    private final List<ResultRow> rows;
    private final List<Segment> gantt;

    public ScheduleOutput(List<ResultRow> rows, List<Segment> gantt) {
        this.rows = rows;
        this.gantt = gantt;
    }

    public List<ResultRow> getRows() {
        return rows;
    }

    public List<Segment> getGantt() {
        return gantt;
    }
}
