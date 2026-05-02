package service;

import model.Process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidationService {
    public Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s);
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
            if (!p.getPid().isEmpty()) {
                if (used.contains(p.getPid())) {
                    errors.add("Duplicate PID found: " + p.getPid());
                }
                used.add(p.getPid());
            }

            if (p.getArrival() < 0) {
                errors.add((p.getPid().isEmpty() ? "Blank PID" : p.getPid()) + ": Arrival Time cannot be negative.");
            }

            if (p.getBurst() <= 0) {
                errors.add((p.getPid().isEmpty() ? "Blank PID" : p.getPid()) + ": Burst Time must be greater than zero.");
            }
        }
    }
}
