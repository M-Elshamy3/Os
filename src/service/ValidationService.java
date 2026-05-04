package service;

import model.Process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidationService {

    public Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public void validateProcesses(List<Process> processes, List<String> errors) {
        Set<String> used = new HashSet<>();

        if (processes.isEmpty()) {
            errors.add("No processes provided.");
        }

        for (Process p : processes) {
            String name = p.pid().isEmpty() ? "Blank PID" : p.pid();

            if (!p.pid().isEmpty()) {
                if (used.contains(p.pid())) {
                    errors.add("Duplicate PID found: " + p.pid());
                }

                used.add(p.pid());
            }

            if (p.arrival() < 0) {
                errors.add(name + ": Arrival Time cannot be negative.");
            }

            if (p.burst() <= 0) {
                errors.add(name + ": Burst Time must be greater than zero.");
            }

            if (p.priority() <= 0) {
                errors.add(name + ": Priority must be greater than zero.");
            }
        }
    }
}
