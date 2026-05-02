package ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Process;
import model.ResultRow;

public class TableFactory {

    public static TableView<Process> createProcessTable() {
        TableView<Process> table = new TableView<>();

        TableColumn<Process, String> pid = new TableColumn<>("PID");
        pid.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPid()));

        TableColumn<Process, Number> arrival = new TableColumn<>("Arrival");
        arrival.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getArrival()));

        TableColumn<Process, Number> burst = new TableColumn<>("Burst");
        burst.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getBurst()));

        TableColumn<Process, Number> priority = new TableColumn<>("Priority");
        priority.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPriority()));

        table.getColumns().addAll(pid, arrival, burst, priority);
        return table;
    }

    public static TableView<ResultRow> createResultTable() {
        TableView<ResultRow> table = new TableView<>();

        TableColumn<ResultRow, String> pid = new TableColumn<>("PID");
        pid.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPid()));

        TableColumn<ResultRow, Number> at = new TableColumn<>("AT");
        at.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getArrival()));

        TableColumn<ResultRow, Number> bt = new TableColumn<>("BT");
        bt.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getBurst()));

        TableColumn<ResultRow, Number> pr = new TableColumn<>("PR");
        pr.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPriority()));

        TableColumn<ResultRow, Number> st = new TableColumn<>("ST");
        st.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStart()));

        TableColumn<ResultRow, Number> ct = new TableColumn<>("CT");
        ct.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCompletion()));

        TableColumn<ResultRow, Number> wt = new TableColumn<>("WT");
        wt.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getWaiting()));

        TableColumn<ResultRow, Number> tat = new TableColumn<>("TAT");
        tat.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTurnaround()));

        TableColumn<ResultRow, Number> rt = new TableColumn<>("RT");
        rt.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getResponse()));

        table.getColumns().addAll(pid, at, bt, pr, st, ct, wt, tat, rt);
        return table;
    }
}
