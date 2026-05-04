package model;

public record ResultRow(String pid, int arrival, int burst, int priority, int start, int completion, int waiting,
                        int turnaround, int response) {
}
