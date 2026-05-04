package model;

public record Process(String pid, int arrival, int burst, int priority, int inputOrder) {
}
