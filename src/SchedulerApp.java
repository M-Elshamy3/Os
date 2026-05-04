import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Metrics;
import model.Process;
import model.ResultRow;
import model.ScheduleOutput;
import service.ComparisonService;
import service.ScenarioService;
import service.SchedulerService;
import service.ValidationService;
import ui.GanttFormatter;
import ui.TableFactory;

import java.util.ArrayList;
import java.util.List;

public class SchedulerApp extends Application {

    private final List<TextField[]> inputFields = new ArrayList<>();
    private final ValidationService validationService = new ValidationService();
    private final SchedulerService schedulerService = new SchedulerService();
    private final ScenarioService scenarioService = new ScenarioService();
    private final ComparisonService comparisonService = new ComparisonService();
    private Spinner<Integer> countSpinner;
    private GridPane inputGrid;
    private TextArea validationArea;
    private TableView<Process> processTable;
    private TextArea priorityArea;
    private TextArea sjfGanttArea;
    private TextArea priorityGanttArea;
    private TableView<ResultRow> sjfResultTable;
    private TableView<ResultRow> priorityResultTable;
    private Label sjfAvgLabel;
    private Label priorityAvgLabel;
    private TextArea comparisonArea;
    private TextArea conclusionArea;
    private ComparisonService.ScenarioType selectedScenario = ComparisonService.ScenarioType.CUSTOM;

    @Override
    public void start(Stage stage) {
        Label title = new Label("SJF vs Priority Comparison Project");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        root.setFillWidth(true);
        root.setPrefWidth(1200);

        VBox inputPanel = buildInputPanel();
        HBox processSection = buildProcessSection();
        HBox ganttSection = buildGanttSection();
        HBox resultsSection = buildResultsSection();
        VBox comparisonSection = buildComparisonSection();
        VBox conclusionSection = buildConclusionSection();

        root.getChildren().addAll(
                title,
                inputPanel,
                processSection,
                ganttSection,
                resultsSection,
                comparisonSection,
                conclusionSection
        );

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Scene scene = new Scene(scrollPane, 1180, 760);

        stage.setTitle("Preemptive SJF vs Preemptive Priority Scheduling");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.centerOnScreen();
        stage.show();

        generateInputRows(5);
    }

    private VBox buildInputPanel() {
        Label sectionTitle = new Label("Input Panel");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        countSpinner = new Spinner<>(1, 100, 5);
        countSpinner.setEditable(true);
        countSpinner.setPrefWidth(90);

        Button createRowsButton = new Button("Create Rows");
        Button runButton = new Button("Run Comparison");
        Button clearButton = new Button("Clear");

        Button scenarioAButton = new Button("Scenario A");
        Button scenarioBButton = new Button("Scenario B");
        Button scenarioCButton = new Button("Scenario C");
        Button scenarioDButton = new Button("Scenario D");

        HBox controls = new HBox(10,
                new Label("Number of Processes:"), countSpinner,
                createRowsButton, runButton, clearButton,
                scenarioAButton, scenarioBButton, scenarioCButton, scenarioDButton
        );

        controls.setAlignment(Pos.CENTER_LEFT);

        inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(8);

        validationArea = new TextArea();
        validationArea.setEditable(false);
        validationArea.setPrefRowCount(4);
        validationArea.setPrefHeight(90);

        VBox box = new VBox(
                10,
                sectionTitle,
                controls,
                inputGrid,
                new Label("Validation Behavior"),
                validationArea
        );

        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: black; -fx-border-width: 1;");

        createRowsButton.setOnAction(e -> {
            selectedScenario = ComparisonService.ScenarioType.CUSTOM;
            generateInputRows(countSpinner.getValue());
        });

        runButton.setOnAction(e -> runSimulation());

        clearButton.setOnAction(e -> clearAll());

        scenarioAButton.setOnAction(e -> {
            selectedScenario = ComparisonService.ScenarioType.A_BASIC_MIXED;
            loadScenario(scenarioService.scenarioA());
        });

        scenarioBButton.setOnAction(e -> {
            selectedScenario = ComparisonService.ScenarioType.B_CONFLICT;
            loadScenario(scenarioService.scenarioB());
        });

        scenarioCButton.setOnAction(e -> {
            selectedScenario = ComparisonService.ScenarioType.C_FAIRNESS;
            loadScenario(scenarioService.scenarioC());
        });

        scenarioDButton.setOnAction(e -> {
            selectedScenario = ComparisonService.ScenarioType.D_VALIDATION;
            loadScenario(scenarioService.scenarioD());
        });

        return box;
    }

    private HBox buildProcessSection() {
        Label leftTitle = new Label("Process Table");
        leftTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        processTable = TableFactory.createProcessTable();
        processTable.setPrefHeight(220);
        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox left = new VBox(8, leftTitle, processTable);
        left.setPrefWidth(680);
        left.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        Label rightTitle = new Label("Priority Input Area");
        rightTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        priorityArea = new TextArea();
        priorityArea.setEditable(false);
        priorityArea.setPrefHeight(220);

        VBox right = new VBox(8, rightTitle, priorityArea);
        right.setPrefWidth(460);
        right.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        return new HBox(12, left, right);
    }

    private HBox buildGanttSection() {
        Label sjfTitle = new Label("Gantt Chart for SJF");
        sjfTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        sjfGanttArea = new TextArea();
        sjfGanttArea.setEditable(false);
        sjfGanttArea.setPrefHeight(150);

        VBox left = new VBox(8, sjfTitle, sjfGanttArea);
        left.setPrefWidth(570);
        left.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        Label prTitle = new Label("Gantt Chart for Priority");
        prTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        priorityGanttArea = new TextArea();
        priorityGanttArea.setEditable(false);
        priorityGanttArea.setPrefHeight(150);

        VBox right = new VBox(8, prTitle, priorityGanttArea);
        right.setPrefWidth(570);
        right.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        return new HBox(12, left, right);
    }

    private HBox buildResultsSection() {
        Label sjfTitle = new Label("Results Table for SJF");
        sjfTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        sjfResultTable = TableFactory.createResultTable();
        sjfResultTable.setPrefHeight(260);
        sjfResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        sjfAvgLabel = new Label();

        VBox left = new VBox(8, sjfTitle, sjfResultTable, sjfAvgLabel);
        left.setPrefWidth(570);
        left.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        Label prTitle = new Label("Results Table for Priority");
        prTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        priorityResultTable = TableFactory.createResultTable();
        priorityResultTable.setPrefHeight(260);
        priorityResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        priorityAvgLabel = new Label();

        VBox right = new VBox(8, prTitle, priorityResultTable, priorityAvgLabel);
        right.setPrefWidth(570);
        right.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        return new HBox(12, left, right);
    }

    private VBox buildComparisonSection() {
        Label title = new Label("Comparison Summary Section");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        comparisonArea = new TextArea();
        comparisonArea.setEditable(false);
        comparisonArea.setPrefHeight(220);

        VBox box = new VBox(8, title, comparisonArea);
        box.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        return box;
    }

    private VBox buildConclusionSection() {
        Label title = new Label("Final Conclusion Area");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        conclusionArea = new TextArea();
        conclusionArea.setEditable(false);
        conclusionArea.setPrefHeight(150);

        VBox box = new VBox(8, title, conclusionArea);
        box.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");

        return box;
    }

    private void generateInputRows(int n) {
        inputGrid.getChildren().clear();
        inputFields.clear();

        inputGrid.add(new Label("PID"), 0, 0);
        inputGrid.add(new Label("Arrival Time"), 1, 0);
        inputGrid.add(new Label("Burst Time"), 2, 0);
        inputGrid.add(new Label("Priority"), 3, 0);

        for (int i = 0; i < n; i++) {
            TextField pidField = new TextField();
            TextField arrivalField = new TextField();
            TextField burstField = new TextField();
            TextField priorityField = new TextField();

            pidField.setPrefWidth(160);
            arrivalField.setPrefWidth(120);
            burstField.setPrefWidth(120);
            priorityField.setPrefWidth(120);

            inputFields.add(new TextField[]{
                    pidField,
                    arrivalField,
                    burstField,
                    priorityField
            });

            inputGrid.add(pidField, 0, i + 1);
            inputGrid.add(arrivalField, 1, i + 1);
            inputGrid.add(burstField, 2, i + 1);
            inputGrid.add(priorityField, 3, i + 1);
        }
    }

    private List<Process> readProcesses(List<String> errors) {
        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < inputFields.size(); i++) {
            String pid = inputFields.get(i)[0].getText().trim();
            String atText = inputFields.get(i)[1].getText().trim();
            String btText = inputFields.get(i)[2].getText().trim();
            String prText = inputFields.get(i)[3].getText().trim();

            Integer at = validationService.parseInteger(atText);
            Integer bt = validationService.parseInteger(btText);
            Integer pr = validationService.parseInteger(prText);

            if (pid.isEmpty()) {
                errors.add("Row " + (i + 1) + ": PID cannot be empty.");
            }

            if (at == null) {
                errors.add("Row " + (i + 1) + ": Arrival Time must be an integer.");
            }

            if (bt == null) {
                errors.add("Row " + (i + 1) + ": Burst Time must be an integer.");
            }

            if (pr == null) {
                errors.add("Row " + (i + 1) + ": Priority must be an integer.");
            }

            processes.add(new Process(
                    pid,
                    at == null ? 0 : at,
                    bt == null ? 0 : bt,
                    pr == null ? 0 : pr,
                    i
            ));
        }

        validationService.validateProcesses(processes, errors);

        return processes;
    }

    private void runSimulation() {
        validationArea.clear();

        List<String> errors = new ArrayList<>();
        List<Process> processes = readProcesses(errors);

        selectedScenario = scenarioService.detectScenario(processes);

        if (!errors.isEmpty()) {
            validationArea.setText(String.join("\n", errors));

            clearOutputOnly();

            if (selectedScenario == ComparisonService.ScenarioType.D_VALIDATION) {
                Metrics emptyMetrics = new Metrics(0, 0, 0);

                comparisonArea.setText(comparisonService.buildComparisonText(
                        processes,
                        emptyMetrics,
                        emptyMetrics,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        selectedScenario
                ));

                conclusionArea.setText(comparisonService.buildConclusionText(
                        processes,
                        emptyMetrics,
                        emptyMetrics,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        selectedScenario
                ));
            }

            return;
        }

        validationArea.setText("Input is valid. Detected workload type: " + getScenarioDisplayName(selectedScenario));

        processTable.setItems(FXCollections.observableArrayList(processes));

        priorityArea.setText(comparisonService.buildPriorityText(processes));

        ScheduleOutput sjfOut = schedulerService.runPreemptiveSJF(processes);
        ScheduleOutput prOut = schedulerService.runPreemptivePriority(processes);

        Metrics sjfMetrics = schedulerService.calculateMetrics(sjfOut.getRows());
        Metrics prMetrics = schedulerService.calculateMetrics(prOut.getRows());

        sjfGanttArea.setText(GanttFormatter.buildGanttText(sjfOut.getGantt()));
        priorityGanttArea.setText(GanttFormatter.buildGanttText(prOut.getGantt()));

        sjfResultTable.setItems(FXCollections.observableArrayList(sjfOut.getRows()));
        priorityResultTable.setItems(FXCollections.observableArrayList(prOut.getRows()));

        sjfAvgLabel.setText(String.format(
                "Average WT = %.2f    Average TAT = %.2f    Average RT = %.2f",
                sjfMetrics.getAvgWT(),
                sjfMetrics.getAvgTAT(),
                sjfMetrics.getAvgRT()
        ));

        priorityAvgLabel.setText(String.format(
                "Average WT = %.2f    Average TAT = %.2f    Average RT = %.2f",
                prMetrics.getAvgWT(),
                prMetrics.getAvgTAT(),
                prMetrics.getAvgRT()
        ));

        comparisonArea.setText(comparisonService.buildComparisonText(
                processes,
                sjfMetrics,
                prMetrics,
                sjfOut.getRows(),
                prOut.getRows(),
                selectedScenario
        ));

        conclusionArea.setText(comparisonService.buildConclusionText(
                processes,
                sjfMetrics,
                prMetrics,
                sjfOut.getRows(),
                prOut.getRows(),
                selectedScenario
        ));
    }

    private String getScenarioDisplayName(ComparisonService.ScenarioType scenario) {
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

    private void clearOutputOnly() {
        processTable.setItems(FXCollections.observableArrayList());
        priorityArea.clear();

        sjfGanttArea.clear();
        priorityGanttArea.clear();

        sjfResultTable.setItems(FXCollections.observableArrayList());
        priorityResultTable.setItems(FXCollections.observableArrayList());

        comparisonArea.clear();
        conclusionArea.clear();

        sjfAvgLabel.setText("");
        priorityAvgLabel.setText("");
    }

    private void clearAll() {
        selectedScenario = ComparisonService.ScenarioType.CUSTOM;

        countSpinner.getValueFactory().setValue(5);
        generateInputRows(5);

        validationArea.clear();

        clearOutputOnly();
    }

    private void loadScenario(List<Process> processes) {
        countSpinner.getValueFactory().setValue(processes.size());

        generateInputRows(processes.size());

        for (int i = 0; i < processes.size(); i++) {
            inputFields.get(i)[0].setText(processes.get(i).getPid());
            inputFields.get(i)[1].setText(String.valueOf(processes.get(i).getArrival()));
            inputFields.get(i)[2].setText(String.valueOf(processes.get(i).getBurst()));
            inputFields.get(i)[3].setText(String.valueOf(processes.get(i).getPriority()));
        }
    }
}
