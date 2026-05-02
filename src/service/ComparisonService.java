package service;

import model.Metrics;
import model.Process;
import model.ResultRow;

import java.util.List;

public class ComparisonService {

    public enum ScenarioType {
        A_BASIC_MIXED,
        B_CONFLICT,
        C_FAIRNESS,
        D_VALIDATION,
        CUSTOM
    }

    public String buildPriorityText(List<Process> processes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Priority rule: smaller number means higher priority\n\n");
        sb.append("PID\tPriority\n");
        for (Process p : processes) {
            sb.append(p.getPid()).append("\t").append(p.getPriority()).append("\n");
        }
        return sb.toString();
    }

    public String buildComparisonText(List<Process> processes, Metrics sjf, Metrics pr,
                                      List<ResultRow> sjfRows, List<ResultRow> prRows,
                                      ScenarioType scenario) {
        StringBuilder sb = new StringBuilder();

        if (scenario == ScenarioType.D_VALIDATION) {
            return buildValidationAnalysis(processes);
        }

        int sjfWorst = sjfRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);
        int prWorst = prRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);

        sb.append("Metric Comparison\n");
        sb.append("Selected workload type: ").append(getScenarioLabel(scenario)).append("\n\n");

        sb.append(buildScenarioIntro(scenario));
        sb.append("\n");

        sb.append(buildMetricSummary(sjf, pr, sjfWorst, prWorst));
        sb.append("\n");

        sb.append("Required Analysis Questions\n");

        sb.append("1. Which algorithm gave lower average waiting time?\n");
        sb.append(answerWaitingTime(scenario, sjf, pr));

        sb.append("\n2. Which algorithm gave lower average turnaround time?\n");
        sb.append(answerTurnaroundTime(scenario, sjf, pr));

        sb.append("\n3. Did SJF favor short jobs more strongly?\n");
        sb.append(answerShortJobs(scenario));

        sb.append("\n4. Did Priority Scheduling favor urgent processes more strongly?\n");
        sb.append(answerPriorityBehavior(scenario));

        sb.append("\n5. Was any starvation or unfair delay observed?\n");
        sb.append(answerFairness(scenario, sjfWorst, prWorst));

        sb.append("\n6. Which algorithm would you recommend for the tested workload, and why?\n");
        sb.append(buildRecommendation(scenario, sjf, pr, sjfWorst, prWorst));

        return sb.toString();
    }

    public String buildConclusionText(List<Process> processes, Metrics sjf, Metrics pr,
                                      List<ResultRow> sjfRows, List<ResultRow> prRows,
                                      ScenarioType scenario) {
        StringBuilder sb = new StringBuilder();

        if (scenario == ScenarioType.D_VALIDATION) {
            return buildValidationConclusion(processes);
        }

        int sjfWorst = sjfRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);
        int prWorst = prRows.stream().mapToInt(ResultRow::getWaiting).max().orElse(0);

        sb.append("Conclusion\n");
        sb.append("Selected workload type: ").append(getScenarioLabel(scenario)).append("\n\n");

        sb.append(buildOverallConclusion(scenario, sjf, pr));
        sb.append("\n");

        sb.append("Metric-specific conclusion\n");
        sb.append(buildMetricSpecificConclusion(sjf, pr));

        sb.append("\nTrade-off explanation\n");
        sb.append(buildTradeOffConclusion(scenario));

        sb.append("\nFairness conclusion\n");
        sb.append(buildFairnessConclusion(scenario, sjfWorst, prWorst));

        sb.append("\nFinal recommendation\n");
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
                return "This analysis is based on a basic mixed workload. The main purpose here is to observe the normal behavior of SJF and Priority Scheduling when processes arrive at different times with different burst lengths.";
            case B_CONFLICT:
                return "This analysis is based on a conflict workload. The main purpose here is to reveal the difference between choosing a shorter job and choosing a more urgent job when those two factors disagree.";
            case C_FAIRNESS:
                return "This analysis is based on a fairness-sensitive workload. The main purpose here is to observe whether one algorithm causes one process to wait much longer than the others.";
            default:
                return "This analysis is based on a custom workload entered at runtime. The explanation is produced from the measured results of the given input.";
        }
    }

    private String buildMetricSummary(Metrics sjf, Metrics pr, int sjfWorst, int prWorst) {
        StringBuilder sb = new StringBuilder();

        if (sjf.getAvgWT() < pr.getAvgWT()) {
            sb.append("Lower average waiting time: SJF\n");
        } else if (pr.getAvgWT() < sjf.getAvgWT()) {
            sb.append("Lower average waiting time: Priority Scheduling\n");
        } else {
            sb.append("Average waiting time: equal in both algorithms\n");
        }

        if (sjf.getAvgTAT() < pr.getAvgTAT()) {
            sb.append("Lower average turnaround time: SJF\n");
        } else if (pr.getAvgTAT() < sjf.getAvgTAT()) {
            sb.append("Lower average turnaround time: Priority Scheduling\n");
        } else {
            sb.append("Average turnaround time: equal in both algorithms\n");
        }

        if (sjf.getAvgRT() < pr.getAvgRT()) {
            sb.append("Lower average response time: SJF\n");
        } else if (pr.getAvgRT() < sjf.getAvgRT()) {
            sb.append("Lower average response time: Priority Scheduling\n");
        } else {
            sb.append("Average response time: equal in both algorithms\n");
        }

        if (sjfWorst < prWorst) {
            sb.append("Lower worst waiting time: SJF\n");
        } else if (prWorst < sjfWorst) {
            sb.append("Lower worst waiting time: Priority Scheduling\n");
        } else {
            sb.append("Worst waiting time: equal in both algorithms\n");
        }

        return sb.toString();
    }

    private String answerWaitingTime(ScenarioType scenario, Metrics sjf, Metrics pr) {
        if (sjf.getAvgWT() < pr.getAvgWT()) {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "SJF gave the lower average waiting time in this mixed workload because serving shorter jobs earlier improved the general flow of execution.\n";
                case B_CONFLICT:
                    return "SJF gave the lower average waiting time in this conflict case because job length had a stronger effect on delay reduction than urgency ordering.\n";
                case C_FAIRNESS:
                    return "SJF gave the lower average waiting time, but this fairness-sensitive workload still requires checking whether one individual process suffered a very large delay.\n";
                default:
                    return "SJF gave the lower average waiting time for this custom input.\n";
            }
        } else if (pr.getAvgWT() < sjf.getAvgWT()) {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "Priority Scheduling gave the lower average waiting time in this mixed workload, showing that the selected priority order worked well for these arrivals.\n";
                case B_CONFLICT:
                    return "Priority Scheduling gave the lower average waiting time in this conflict case because urgent processes were favored more strongly than short ones.\n";
                case C_FAIRNESS:
                    return "Priority Scheduling gave the lower average waiting time, although fairness still needs separate inspection in case a low-priority process waited too long.\n";
                default:
                    return "Priority Scheduling gave the lower average waiting time for this custom input.\n";
            }
        } else {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "Both algorithms gave the same average waiting time in this mixed workload.\n";
                case B_CONFLICT:
                    return "Both algorithms gave the same average waiting time even though they followed different decision rules in this conflict case.\n";
                case C_FAIRNESS:
                    return "Both algorithms gave the same average waiting time, but fairness should still be judged using worst-case waiting behavior.\n";
                default:
                    return "Both algorithms gave the same average waiting time for this custom input.\n";
            }
        }
    }

    private String answerTurnaroundTime(ScenarioType scenario, Metrics sjf, Metrics pr) {
        if (sjf.getAvgTAT() < pr.getAvgTAT()) {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "SJF gave the lower average turnaround time in this normal mixed workload, meaning processes completed more efficiently overall.\n";
                case B_CONFLICT:
                    return "SJF gave the lower average turnaround time in this conflict scenario because finishing shorter work earlier reduced total completion delay.\n";
                case C_FAIRNESS:
                    return "SJF gave the lower average turnaround time, but that does not automatically mean it was fairer to every process.\n";
                default:
                    return "SJF gave the lower average turnaround time for this custom input.\n";
            }
        } else if (pr.getAvgTAT() < sjf.getAvgTAT()) {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "Priority Scheduling gave the lower average turnaround time in this normal mixed workload, showing that urgent processes were handled efficiently.\n";
                case B_CONFLICT:
                    return "Priority Scheduling gave the lower average turnaround time in this conflict scenario because the urgency rule shaped completion order more effectively.\n";
                case C_FAIRNESS:
                    return "Priority Scheduling gave the lower average turnaround time, although the fairness result should still be checked separately.\n";
                default:
                    return "Priority Scheduling gave the lower average turnaround time for this custom input.\n";
            }
        } else {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "Both algorithms gave the same average turnaround time in this mixed workload.\n";
                case B_CONFLICT:
                    return "Both algorithms gave the same average turnaround time in this conflict scenario.\n";
                case C_FAIRNESS:
                    return "Both algorithms gave the same average turnaround time, so fairness must be judged through waiting distribution instead.\n";
                default:
                    return "Both algorithms gave the same average turnaround time for this custom input.\n";
            }
        }
    }

    private String answerShortJobs(ScenarioType scenario) {
        switch (scenario) {
            case A_BASIC_MIXED:
                return "Yes. In a normal mixed workload, SJF naturally gives an advantage to shorter jobs whenever they are available for execution.\n";
            case B_CONFLICT:
                return "Yes. This scenario is specifically meant to show that SJF continues to prefer short jobs even when those jobs are assigned lower priority values.\n";
            case C_FAIRNESS:
                return "Yes. SJF still favors short jobs strongly, and that behavior can increase the waiting time of longer jobs in a fairness-sensitive case.\n";
            default:
                return "Yes. By definition, SJF always prefers the shortest available job or shortest remaining job.\n";
        }
    }

    private String answerPriorityBehavior(ScenarioType scenario) {
        switch (scenario) {
            case A_BASIC_MIXED:
                return "Yes. Priority Scheduling gave stronger service to more urgent processes according to the priority rule defined for the system.\n";
            case B_CONFLICT:
                return "Yes. This scenario clearly shows that Priority Scheduling keeps favoring urgent processes even when they are longer than competing jobs.\n";
            case C_FAIRNESS:
                return "Yes. Priority Scheduling still favors urgent processes more strongly, but that can increase delay for lower-priority processes in this kind of workload.\n";
            default:
                return "Yes. By definition, Priority Scheduling selects the most urgent available process first.\n";
        }
    }

    private String answerFairness(ScenarioType scenario, int sjfWorst, int prWorst) {
        switch (scenario) {
            case A_BASIC_MIXED:
                if (sjfWorst < prWorst) {
                    return "Only limited unfair delay was observed in this mixed workload, and SJF appeared slightly fairer because its worst waiting time was lower.\n";
                } else if (prWorst < sjfWorst) {
                    return "Only limited unfair delay was observed in this mixed workload, and Priority Scheduling appeared slightly fairer because its worst waiting time was lower.\n";
                } else {
                    return "No strong fairness difference was observed in this mixed workload because both algorithms had similar worst waiting time.\n";
                }

            case B_CONFLICT:
                if (sjfWorst < prWorst) {
                    return "Yes. In this conflict scenario, Priority Scheduling caused the larger extreme delay, so SJF appeared less harsh on the most delayed process.\n";
                } else if (prWorst < sjfWorst) {
                    return "Yes. In this conflict scenario, SJF caused the larger extreme delay, so Priority Scheduling appeared less harsh on the most delayed process.\n";
                } else {
                    return "The conflict changed execution order, but both algorithms ended with a similar worst waiting time.\n";
                }

            case C_FAIRNESS:
                if (sjfWorst > prWorst) {
                    return "Yes. This fairness-sensitive workload clearly showed stronger unfair delay under SJF because one process waited much longer there.\n";
                } else if (prWorst > sjfWorst) {
                    return "Yes. This fairness-sensitive workload clearly showed stronger unfair delay under Priority Scheduling because one process waited much longer there.\n";
                } else {
                    return "Yes. This fairness-sensitive workload showed noticeable delay concentration, although both algorithms ended with the same worst waiting time.\n";
                }

            default:
                if (sjfWorst < prWorst) {
                    return "A fairness difference was observed, and SJF had the lower worst waiting time.\n";
                } else if (prWorst < sjfWorst) {
                    return "A fairness difference was observed, and Priority Scheduling had the lower worst waiting time.\n";
                } else {
                    return "Both algorithms had the same worst waiting time for this custom input.\n";
                }
        }
    }

    private String buildRecommendation(ScenarioType scenario, Metrics sjf, Metrics pr, int sjfWorst, int prWorst) {
        boolean sjfBetterAvg = sjf.getAvgWT() <= pr.getAvgWT() && sjf.getAvgTAT() <= pr.getAvgTAT();
        boolean prBetterAvg = pr.getAvgWT() <= sjf.getAvgWT() && pr.getAvgTAT() <= sjf.getAvgTAT();

        switch (scenario) {
            case A_BASIC_MIXED:
                if (sjfBetterAvg) {
                    return "For this basic mixed workload, SJF is recommended if the goal is better average efficiency across the process set.\n";
                } else if (prBetterAvg) {
                    return "For this basic mixed workload, Priority Scheduling is recommended if the goal is to combine good average results with urgency-based service.\n";
                } else {
                    return "For this basic mixed workload, the recommendation depends on system policy: SJF for efficiency, Priority Scheduling for urgency.\n";
                }

            case B_CONFLICT:
                if (sjfBetterAvg) {
                    return "For this conflict scenario, SJF is recommended when reducing average delay is more important than serving urgent processes first.\n";
                } else if (prBetterAvg) {
                    return "For this conflict scenario, Priority Scheduling is recommended when urgent tasks must be given stronger service even if they are longer.\n";
                } else {
                    return "For this conflict scenario, the best choice depends directly on whether the system values burst efficiency more or urgency more.\n";
                }

            case C_FAIRNESS:
                if (sjfWorst < prWorst && sjf.getAvgWT() <= pr.getAvgWT()) {
                    return "For this fairness-sensitive workload, SJF is recommended because it controlled worst-case waiting better while still remaining efficient.\n";
                } else if (prWorst < sjfWorst && pr.getAvgWT() <= sjf.getAvgWT()) {
                    return "For this fairness-sensitive workload, Priority Scheduling is recommended because it reduced the larger starvation-like delay more effectively.\n";
                } else {
                    return "For this fairness-sensitive workload, the safer recommendation is the algorithm that better limits extreme waiting, even if the average metrics are close.\n";
                }

            default:
                if (sjfBetterAvg && sjfWorst <= prWorst) {
                    return "For this custom workload, SJF is recommended because it combined stronger average performance with no worse unfair delay.\n";
                } else if (prBetterAvg && prWorst <= sjfWorst) {
                    return "For this custom workload, Priority Scheduling is recommended because it balanced urgency support with good measured performance.\n";
                } else {
                    return "For this custom workload, the final recommendation depends on whether efficiency or urgency is more important in the target system.\n";
                }
        }
    }

    private String buildOverallConclusion(ScenarioType scenario, Metrics sjf, Metrics pr) {
        int sjfWins = 0;
        int prWins = 0;

        if (sjf.getAvgWT() < pr.getAvgWT()) sjfWins++;
        else if (pr.getAvgWT() < sjf.getAvgWT()) prWins++;

        if (sjf.getAvgTAT() < pr.getAvgTAT()) sjfWins++;
        else if (pr.getAvgTAT() < sjf.getAvgTAT()) prWins++;

        if (sjf.getAvgRT() < pr.getAvgRT()) sjfWins++;
        else if (pr.getAvgRT() < sjf.getAvgRT()) prWins++;

        if (sjfWins > prWins) {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "In this basic mixed workload, SJF performed better overall across more of the average performance metrics.";
                case B_CONFLICT:
                    return "In this conflict scenario, SJF performed better overall because burst-time efficiency had the stronger effect on the measured results.";
                case C_FAIRNESS:
                    return "In this fairness-sensitive scenario, SJF performed better in more average metrics, although fairness still remains important in the final judgment.";
                default:
                    return "For this custom workload, SJF performed better overall according to the measured metrics.";
            }
        } else if (prWins > sjfWins) {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "In this basic mixed workload, Priority Scheduling performed better overall across more of the average performance metrics.";
                case B_CONFLICT:
                    return "In this conflict scenario, Priority Scheduling performed better overall because urgency ordering had the stronger effect on the measured results.";
                case C_FAIRNESS:
                    return "In this fairness-sensitive scenario, Priority Scheduling performed better in more average metrics, though fairness remains a separate concern.";
                default:
                    return "For this custom workload, Priority Scheduling performed better overall according to the measured metrics.";
            }
        } else {
            switch (scenario) {
                case A_BASIC_MIXED:
                    return "In this basic mixed workload, neither algorithm dominated all average metrics.";
                case B_CONFLICT:
                    return "In this conflict scenario, the results were mixed, which reflects the trade-off between shorter jobs and more urgent jobs.";
                case C_FAIRNESS:
                    return "In this fairness-sensitive scenario, the averages were mixed, so the final judgment depends heavily on worst-case waiting behavior.";
                default:
                    return "For this custom workload, neither algorithm clearly dominated all measured metrics.";
            }
        }
    }

    private String buildMetricSpecificConclusion(Metrics sjf, Metrics pr) {
        StringBuilder sb = new StringBuilder();

        if (sjf.getAvgWT() < pr.getAvgWT()) sb.append("SJF handled waiting time better.\n");
        else if (pr.getAvgWT() < sjf.getAvgWT()) sb.append("Priority Scheduling handled waiting time better.\n");
        else sb.append("Both handled waiting time equally.\n");

        if (sjf.getAvgTAT() < pr.getAvgTAT()) sb.append("SJF handled turnaround time better.\n");
        else if (pr.getAvgTAT() < sjf.getAvgTAT()) sb.append("Priority Scheduling handled turnaround time better.\n");
        else sb.append("Both handled turnaround time equally.\n");

        if (sjf.getAvgRT() < pr.getAvgRT()) sb.append("SJF handled response time better.\n");
        else if (pr.getAvgRT() < sjf.getAvgRT()) sb.append("Priority Scheduling handled response time better.\n");
        else sb.append("Both handled response time equally.\n");

        return sb.toString();
    }

    private String buildTradeOffConclusion(ScenarioType scenario) {
        switch (scenario) {
            case A_BASIC_MIXED:
                return "This scenario mainly demonstrates the normal trade-off between efficiency and urgency. SJF improves performance by preferring shorter work, while Priority Scheduling improves service to urgent processes.\n";
            case B_CONFLICT:
                return "This scenario directly demonstrates the trade-off between burst efficiency and urgency. SJF follows job size more strongly, while Priority Scheduling follows urgency more strongly.\n";
            case C_FAIRNESS:
                return "This scenario highlights that fairness must also be considered. Even if an algorithm gives good averages, it may still cause one process to wait much longer than others.\n";
            default:
                return "For this custom workload, the main trade-off remains efficiency versus urgency, and the input determines which one mattered more in practice.\n";
        }
    }

    private String buildFairnessConclusion(ScenarioType scenario, int sjfWorst, int prWorst) {
        switch (scenario) {
            case A_BASIC_MIXED:
                if (sjfWorst < prWorst) {
                    return "For this mixed workload, SJF appeared slightly fairer in practice because its worst waiting time was lower.\n";
                } else if (prWorst < sjfWorst) {
                    return "For this mixed workload, Priority Scheduling appeared slightly fairer in practice because its worst waiting time was lower.\n";
                } else {
                    return "For this mixed workload, both algorithms appeared similarly fair in practice.\n";
                }

            case B_CONFLICT:
                if (sjfWorst < prWorst) {
                    return "For this conflict scenario, SJF appeared fairer because it avoided the larger extreme delay.\n";
                } else if (prWorst < sjfWorst) {
                    return "For this conflict scenario, Priority Scheduling appeared fairer because it avoided the larger extreme delay.\n";
                } else {
                    return "For this conflict scenario, both algorithms appeared to have a similar fairness level.\n";
                }

            case C_FAIRNESS:
                if (sjfWorst < prWorst) {
                    return "For this fairness-sensitive scenario, SJF appeared safer against starvation because its worst waiting time was lower.\n";
                } else if (prWorst < sjfWorst) {
                    return "For this fairness-sensitive scenario, Priority Scheduling appeared safer against starvation because its worst waiting time was lower.\n";
                } else {
                    return "For this fairness-sensitive scenario, both algorithms showed the same worst waiting time, so neither had a clear fairness advantage.\n";
                }

            default:
                if (sjfWorst < prWorst) {
                    return "For this custom workload, SJF appeared fairer based on worst waiting time.\n";
                } else if (prWorst < sjfWorst) {
                    return "For this custom workload, Priority Scheduling appeared fairer based on worst waiting time.\n";
                } else {
                    return "For this custom workload, both algorithms appeared equally fair based on worst waiting time.\n";
                }
        }
    }

    private String buildValidationAnalysis(List<Process> processes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Analysis\n");
        sb.append("Selected workload type: Scenario D - Validation case\n\n");

        sb.append("This scenario is intended to demonstrate input validation behavior rather than scheduling performance.\n");
        sb.append("The system should reject invalid rows before any simulation starts.\n\n");

        sb.append("Validation checks that should be enforced:\n");
        sb.append("- PID must not be empty and should be unique.\n");
        sb.append("- Arrival time must be zero or a positive integer.\n");
        sb.append("- Burst time must be greater than zero.\n");
        sb.append("- Priority must be a valid positive integer according to the defined rule.\n\n");

        sb.append("Observed input review:\n");
        for (Process p : processes) {
            sb.append("Process ")
                    .append(p.getPid())
                    .append(": arrival=")
                    .append(p.getArrival())
                    .append(", burst=")
                    .append(p.getBurst())
                    .append(", priority=")
                    .append(p.getPriority())
                    .append("\n");
        }

        sb.append("\nConclusion for validation scenario:\n");
        sb.append("If any invalid value is present, the scheduler should stop execution, display a clear error message, and require the user to correct the input before running the comparison.\n");

        return sb.toString();
    }

    private String buildValidationConclusion(List<Process> processes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Conclusion\n");
        sb.append("Selected workload type: Scenario D - Validation case\n\n");
        sb.append("This scenario is not meant to compare scheduling metrics. Its purpose is to verify that invalid input is detected and blocked correctly.\n\n");
        sb.append("A correct implementation should reject duplicated or empty process IDs, negative arrival times, non-positive burst times, and invalid priority values.\n");
        sb.append("The final recommendation for this scenario is to fix invalid entries first, then rerun the scheduling comparison using valid data only.\n");
        return sb.toString();
    }
}
