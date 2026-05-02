package ui;

import model.Segment;

import java.util.List;

public class GanttFormatter {
    public static String buildGanttText(List<Segment> gantt) {
        StringBuilder top = new StringBuilder();
        StringBuilder times = new StringBuilder();

        for (int i = 0; i < gantt.size(); i++) {
            String name = gantt.get(i).getPid();
            int width = Math.max(8, name.length() + 2);
            int leftPad = (width - name.length()) / 2;
            int rightPad = width - name.length() - leftPad;

            top.append("|").append(" ".repeat(leftPad)).append(name).append(" ".repeat(rightPad));

            String start = String.valueOf(gantt.get(i).getStart());
            times.append(start);
            int remain = width + 1 - start.length();
            times.append(" ".repeat(Math.max(1, remain)));

            if (i == gantt.size() - 1) {
                times.append(gantt.get(i).getEnd());
            }
        }

        top.append("|");
        return top + "\n" + times;
    }
}
