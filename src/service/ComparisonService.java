package service;

import model.Metrics;
import model.Process;
import model.ResultRow;

import java.util.List;

public class ComparisonService {

    public String buildPriorityText(List<Process> processes) {
        StringBuilder sb = new StringBuilder();

        sb.append("SJF mode: preemptive SJF, also called Shortest Remaining Time First.\n");
        sb.append("SJF tie handling: shortest remaining time, then arrival time, then input order.\n\n");

        sb.append("Priority rule: smaller number means higher priority.\n");
        sb.append("Priority mode: preemptive priority scheduling.\n");
        sb.append("Priority tie handling: priority, then arrival time, then remaining burst time, then input order.\n\n");

        sb.append("PID\tPriority\n");

        for (Process p : processes) {
            sb.append(p.getPid()).append("\t").append(p.getPriority()).append("\n");
        }

        return sb.toString();
    }

    public String buildComparisonText(List<Process> processes, Metrics sjf, Metrics pr,
                                      List<ResultRow> sjfRows, List<ResultRow> prRows,
                                      ScenarioType scenario) {
        if (scenario == ScenarioType.D_VALIDATION) {
            return buildValidationAnalysis(processes);
        }

        int sjfWorst = sjfRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);
        int prWorst = prRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);

        StringBuilder sb = new StringBuilder();

        sb.append("Metric Comparison\n");
        sb.append("Selected workload type: ").append(getScenarioLabel(scenario)).append("\n\n");

        sb.append(buildScenarioIntro(scenario)).append("\n\n");

        sb.append(buildMetricSummary(sjf, pr, sjfWorst, prWorst)).append("\n");

        sb.append("Required Analysis Questions\n\n");

        sb.append("1. Which algorithm gave lower average waiting time?\n");
        sb.append(answerWaitingTime(scenario, sjf, pr)).append("\n");

        sb.append("2. Which algorithm gave lower average turnaround time?\n");
        sb.append(answerTurnaroundTime(scenario, sjf, pr)).append("\n");

        sb.append("3. Did SJF favor short jobs more strongly?\n");
        sb.append(answerShortJobs(scenario)).append("\n");

        sb.append("4. Did Priority Scheduling favor urgent processes more strongly?\n");
        sb.append(answerPriorityBehavior(scenario)).append("\n");

        sb.append("5. Was any starvation or unfair delay observed?\n");
        sb.append(answerFairness(scenario, sjfWorst, prWorst)).append("\n");

        sb.append("6. Which algorithm would you recommend for the tested workload, and why?\n");
        sb.append(buildRecommendation(scenario, sjf, pr, sjfWorst, prWorst));

        return sb.toString();
    }

    public String buildConclusionText(List<Process> processes, Metrics sjf, Metrics pr,
                                      List<ResultRow> sjfRows, List<ResultRow> prRows,
                                      ScenarioType scenario) {
        if (scenario == ScenarioType.D_VALIDATION) {
            return buildValidationConclusion(processes);
        }

        int sjfWorst = sjfRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);
        int prWorst = prRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);

        StringBuilder sb = new StringBuilder();

        sb.append("Conclusion\n");
        sb.append("Selected workload type: ").append(getScenarioLabel(scenario)).append("\n\n");

        sb.append(buildOverallConclusion(scenario, sjf, pr)).append("\n\n");

        sb.append("Metric-specific conclusion\n");
        sb.append(buildMetricSpecificConclusion(sjf, pr)).append("\n");

        sb.append("Trade-off explanation\n");
        sb.append(buildTradeOffConclusion(scenario)).append("\n");

        sb.append("Fairness conclusion\n");
        sb.append(buildFairnessConclusion(scenario, sjfWorst, prWorst)).append("\n");

        sb.append("Final recommendation\n");
        sb.append(buildRecommendation(scenario, sjf, pr, sjfWorst, prWorst));

        return sb.toString();
    }

    private String getScenarioLabel(ScenarioType scenario) {
        switch (scenario) {
            case A_BASIC_MIXED:
                return "Scenario A - Basic mixed workload";
            case B_CONFLICT:
                return "Scenario B - Conflict between burst time and priority";
            case C_FAIRNESS:
                return "Scenario C - Fairness / starvation-sensitive case";
            case D_VALIDATION:
                return "Scenario D - Validation case";
            default:
                return "Custom workload";
        }
    }

    private String buildScenarioIntro(ScenarioType scenario) {
        switch (scenario) {
            case A_BASIC_MIXED:
                return "This workload has different arrival times, burst times, and priorities. It shows normal behavior for both algorithms.";
            case B_CONFLICT:
                return "This workload creates a conflict between short jobs and urgent jobs. It helps show the trade-off between SJF efficiency and priority urgency.";
            case C_FAIRNESS:
                return "This workload is fairness-sensitive. It checks whether one process waits much longer than the others.";
            default:
                return "This is a custom workload entered at runtime. The explanation is based on the measured results.";
        }
    }

    private String buildMetricSummary(Metrics sjf, Metrics pr, int sjfWorst, int prWorst) {
        StringBuilder sb = new StringBuilder();

        sb.append(compareMetric("Lower average waiting time", "SJF", "Priority Scheduling",
                sjf.getAvgWT(), pr.getAvgWT()));
        sb.append(compareMetric("Lower average turnaround time", "SJF", "Priority Scheduling",
                sjf.getAvgTAT(), pr.getAvgTAT()));
        sb.append(compareMetric("Lower average response time", "SJF", "Priority Scheduling",
                sjf.getAvgRT(), pr.getAvgRT()));

        if (sjfWorst < prWorst) {
            sb.append("Lower worst waiting time: SJF\n");
        } else if (prWorst < sjfWorst) {
            sb.append("Lower worst waiting time: Priority Scheduling\n");
        } else {
            sb.append("Worst waiting time: equal in both algorithms\n");
        }

        return sb.toString();
    }

    private String compareMetric(String label, String leftName, String rightName, double left, double right) {
        if (left < right) {
            return label + ": " + leftName + "\n";
        } else if (right < left) {
            return label + ": " + rightName + "\n";
        } else {
            return label + ": equal in both algorithms\n";
        }
    }

    private String answerWaitingTime(ScenarioType scenario, Metrics sjf, Metrics pr) {
        if (sjf.getAvgWT() < pr.getAvgWT()) {
            return "SJF gave the lower average waiting time because it favored processes with shorter remaining burst time.\n";
        } else if (pr.getAvgWT() < sjf.getAvgWT()) {
            return "Priority Scheduling gave the lower average waiting time because the priority order worked better for this workload.\n";
        } else {
            return "Both algorithms gave the same average waiting time.\n";
        }
    }

    private String answerTurnaroundTime(ScenarioType scenario, Metrics sjf, Metrics pr) {
        if (sjf.getAvgTAT() < pr.getAvgTAT()) {
            return "SJF gave the lower average turnaround time because shorter remaining jobs completed sooner overall.\n";
        } else if (pr.getAvgTAT() < sjf.getAvgTAT()) {
            return "Priority Scheduling gave the lower average turnaround time because urgent processes were completed more effectively.\n";
        } else {
            return "Both algorithms gave the same average turnaround time.\n";
        }
    }

    private String answerShortJobs(ScenarioType scenario) {
        switch (scenario) {
            case B_CONFLICT:
                return "Yes. This conflict scenario shows that preemptive SJF continues to favor short jobs even when those jobs have lower priority.\n";
            case C_FAIRNESS:
                return "Yes. Preemptive SJF favors short jobs strongly, which can delay longer jobs and create fairness concerns.\n";
            default:
                return "Yes. Preemptive SJF selects the process with the shortest remaining burst time whenever a scheduling decision is made.\n";
        }
    }

    private String answerPriorityBehavior(ScenarioType scenario) {
        switch (scenario) {
            case B_CONFLICT:
                return "Yes. This conflict scenario shows that Priority Scheduling favors urgent high-priority processes even if they are longer.\n";
            case C_FAIRNESS:
                return "Yes. Priority Scheduling favors urgent processes, but low-priority processes may experience longer delay.\n";
            default:
                return "Yes. Priority Scheduling selects the most urgent available process according to the priority rule.\n";
        }
    }

    private String answerFairness(ScenarioType scenario, int sjfWorst, int prWorst) {
        if (sjfWorst < prWorst) {
            return "A fairness difference was observed. SJF had the lower worst waiting time, so it appeared fairer for this workload.\n";
        } else if (prWorst < sjfWorst) {
            return "A fairness difference was observed. Priority Scheduling had the lower worst waiting time, so it appeared fairer for this workload.\n";
        } else {
            return "Both algorithms had the same worst waiting time, so neither had a clear fairness advantage by this measure.\n";
        }
    }

    private String buildRecommendation(ScenarioType scenario, Metrics sjf, Metrics pr, int sjfWorst, int prWorst) {
        int sjfScore = 0;
        int prScore = 0;

        if (sjf.getAvgWT() < pr.getAvgWT()) {
            sjfScore++;
        } else if (pr.getAvgWT() < sjf.getAvgWT()) {
            prScore++;
        }

        if (sjf.getAvgTAT() < pr.getAvgTAT()) {
            sjfScore++;
        } else if (pr.getAvgTAT() < sjf.getAvgTAT()) {
            prScore++;
        }

        if (sjf.getAvgRT() < pr.getAvgRT()) {
            sjfScore++;
        } else if (pr.getAvgRT() < sjf.getAvgRT()) {
            prScore++;
        }

        if (sjfWorst < prWorst) {
            sjfScore++;
        } else if (prWorst < sjfWorst) {
            prScore++;
        }

        if (sjfScore > prScore) {
            return "SJF is recommended for this workload because it performed better across more measured metrics and focused on reducing delay through shorter remaining time.\n";
        } else if (prScore > sjfScore) {
            return "Priority Scheduling is recommended for this workload because it performed better across more measured metrics and gave stronger service to urgent processes.\n";
        } else {
            return "Neither algorithm clearly dominates. SJF is better when efficiency is more important, while Priority Scheduling is better when urgency is more important.\n";
        }
    }

    private String buildOverallConclusion(ScenarioType scenario, Metrics sjf, Metrics pr) {
        int sjfWins = 0;
        int prWins = 0;

        if (sjf.getAvgWT() < pr.getAvgWT()) {
            sjfWins++;
        } else if (pr.getAvgWT() < sjf.getAvgWT()) {
            prWins++;
        }

        if (sjf.getAvgTAT() < pr.getAvgTAT()) {
            sjfWins++;
        } else if (pr.getAvgTAT() < sjf.getAvgTAT()) {
            prWins++;
        }

        if (sjf.getAvgRT() < pr.getAvgRT()) {
            sjfWins++;
        } else if (pr.getAvgRT() < sjf.getAvgRT()) {
            prWins++;
        }

        if (sjfWins > prWins) {
            return "SJF performed better overall across more average performance metrics.";
        } else if (prWins > sjfWins) {
            return "Priority Scheduling performed better overall across more average performance metrics.";
        } else {
            return "Neither algorithm dominated all metrics. The result depends on whether the system values efficiency or urgency more.";
        }
    }

    private String buildMetricSpecificConclusion(Metrics sjf, Metrics pr) {
        StringBuilder sb = new StringBuilder();

        if (sjf.getAvgWT() < pr.getAvgWT()) {
            sb.append("SJF handled waiting time better.\n");
        } else if (pr.getAvgWT() < sjf.getAvgWT()) {
            sb.append("Priority Scheduling handled waiting time better.\n");
        } else {
            sb.append("Both handled waiting time equally.\n");
        }

        if (sjf.getAvgTAT() < pr.getAvgTAT()) {
            sb.append("SJF handled turnaround time better.\n");
        } else if (pr.getAvgTAT() < sjf.getAvgTAT()) {
            sb.append("Priority Scheduling handled turnaround time better.\n");
        } else {
            sb.append("Both handled turnaround time equally.\n");
        }

        if (sjf.getAvgRT() < pr.getAvgRT()) {
            sb.append("SJF handled response time better.\n");
        } else if (pr.getAvgRT() < sjf.getAvgRT()) {
            sb.append("Priority Scheduling handled response time better.\n");
        } else {
            sb.append("Both handled response time equally.\n");
        }

        return sb.toString();
    }

    private String buildTradeOffConclusion(ScenarioType scenario) {
        switch (scenario) {
            case B_CONFLICT:
                return "This scenario clearly demonstrates the trade-off between efficiency and urgency. SJF follows shortest remaining burst time, while Priority Scheduling follows process importance.";
            case C_FAIRNESS:
                return "This scenario shows that good average metrics do not always guarantee fairness. One process may still wait much longer than others.";
            default:
                return "SJF focuses on efficiency by favoring short remaining jobs, while Priority Scheduling focuses on urgency by favoring high-priority processes.";
        }
    }

    private String buildFairnessConclusion(ScenarioType scenario, int sjfWorst, int prWorst) {
        if (sjfWorst < prWorst) {
            return "SJF appeared fairer based on worst waiting time.";
        } else if (prWorst < sjfWorst) {
            return "Priority Scheduling appeared fairer based on worst waiting time.";
        } else {
            return "Both algorithms appeared equally fair based on worst waiting time.";
        }
    }

    private String buildValidationAnalysis(List<Process> processes) {
        StringBuilder sb = new StringBuilder();

        sb.append("Validation Analysis\n");
        sb.append("Selected workload type: Scenario D - Validation case\n\n");

        sb.append("This scenario is intended to demonstrate input validation behavior rather than scheduling performance.\n");
        sb.append("The system should reject invalid rows before any simulation starts.\n\n");

        sb.append("Validation checks enforced:\n");
        sb.append("- PID must not be empty.\n");
        sb.append("- PID must be unique.\n");
        sb.append("- Arrival time must be zero or positive.\n");
        sb.append("- Burst time must be greater than zero.\n");
        sb.append("- Priority must be greater than zero.\n\n");

        sb.append("Observed input review:\n");

        for (Process p : processes) {
            sb.append("Process ")
                    .append(p.getPid().isEmpty() ? "Blank PID" : p.getPid())
                    .append(": arrival=")
                    .append(p.getArrival())
                    .append(", burst=")
                    .append(p.getBurst())
                    .append(", priority=")
                    .append(p.getPriority())
                    .append("\n");
        }

        sb.append("\nConclusion for validation scenario:\n");
        sb.append("If invalid input is present, the scheduler stops execution and displays clear validation errors.\n");

        return sb.toString();
    }

    private String buildValidationConclusion(List<Process> processes) {
        StringBuilder sb = new StringBuilder();

        sb.append("Conclusion\n");
        sb.append("Selected workload type: Scenario D - Validation case\n\n");
        sb.append("This scenario is not meant to compare scheduling metrics. Its purpose is to verify that invalid input is detected and blocked correctly.\n");
        sb.append("The program should reject duplicated IDs, empty IDs, negative arrival times, non-positive burst times, and invalid priority values.\n");
        sb.append("The final recommendation is to correct invalid entries first, then rerun the scheduling comparison.\n");

        return sb.toString();
    }

    public enum ScenarioType {
        A_BASIC_MIXED,
        B_CONFLICT,
        C_FAIRNESS,
        D_VALIDATION,
        CUSTOM
    }
}
