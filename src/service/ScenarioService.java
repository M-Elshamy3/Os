package service;

import model.Process;

import java.util.Arrays;
import java.util.List;

public class ScenarioService {
    public List<Process> scenarioA() {
        return Arrays.asList(
                new Process("P1", 0, 7, 3, 0),
                new Process("P2", 2, 4, 1, 1),
                new Process("P3", 4, 1, 4, 2),
                new Process("P4", 5, 4, 2, 3),
                new Process("P5", 6, 2, 5, 4)
        );
    }

    public List<Process> scenarioB() {
        return Arrays.asList(
                new Process("P1", 0, 10, 1, 0),
                new Process("P2", 1, 2, 5, 1),
                new Process("P3", 2, 1, 6, 2),
                new Process("P4", 3, 3, 4, 3)
        );
    }

    public List<Process> scenarioC() {
        return Arrays.asList(
                new Process("P1", 0, 20, 3, 0),
                new Process("P2", 1, 1, 1, 1),
                new Process("P3", 2, 1, 2, 2),
                new Process("P4", 3, 1, 4, 3),
                new Process("P5", 4, 1, 5, 4)
        );
    }

    public List<Process> scenarioD() {
        return Arrays.asList(
                new Process("P1", 0, 5, 2, 0),
                new Process("P1", 1, -3, 1, 1),
                new Process("", 2, 4, 3, 2)
        );
    }
}
