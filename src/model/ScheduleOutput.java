package model;

import java.util.List;

public record ScheduleOutput(List<ResultRow> rows, List<Segment> gantt) {
}
