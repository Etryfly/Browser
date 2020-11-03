import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Controller {
    ArrayList<Tab> tabs = new ArrayList<>();
    HashMap<Tab, HistoryModel> historyHashMap = new HashMap<>();
    ArrayList<HistoryModel> historyArray = new ArrayList<>();


    private TableView<HistoryModel> tableView;


    private TableColumn<HistoryModel, String> URLCol;

    private final ObservableList<HistoryModel> historyData =
            FXCollections.observableArrayList();



    private TableColumn<HistoryModel, Long> TimeCol;


    private TableColumn<HistoryModel, Date> DateCol;

    @FXML
    private TabPane tabPane;

    @FXML
    private WebView webView;

    private WebEngine webEngine;

    @FXML
    private Button goButton;

    @FXML
    private Button addNewTabButton;

    @FXML
    private TextField searchRow;

    @FXML
    private void addNewTab() {
        Tab tab = new Tab();
        tab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                TabClose(event);
            }
        });
        AnchorPane pane = new AnchorPane();
        WebView view = new WebView();
        view.setPrefWidth(1000);
        view.setPrefHeight(800);
        view.setId("WebView");
        pane.setId("Pane");
        pane.getChildren().add(view);
        tab.setContent(pane);

        tab.setText("New tab");
        tabPane.getTabs().add(tab);
        tabs.add(tab);
    }


    private void TabClose(Event event) {
        Tab closedTab = (Tab) event.getSource();

        HistoryModel model = historyHashMap.get(closedTab);
        model.setTimeSpend( System.currentTimeMillis() - model.getDate().getTime());
        historyArray.add(model);
        historyData.add(model);

    }

    @FXML
    public void initialize(){

    }

    @FXML
    public void Search() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        String url = searchRow.getText();
        HistoryModel historyModel = new HistoryModel();
        historyModel.setUrl(url);
        Date currDate = new Date(System.currentTimeMillis());

        historyModel.setDate(currDate);
        historyHashMap.put(selectedTab, historyModel);
        AnchorPane pane = (AnchorPane) selectedTab.getContent();
        for (Node paneNode : pane.getChildren()) {
            if (paneNode instanceof WebView) {
                WebView view = (WebView) paneNode;
                view.getEngine().load(url);
            }
        }
    }

    @FXML
    public void HistoryOnAction() {
            tableView = new TableView<HistoryModel>();
            URLCol = new TableColumn<HistoryModel, String>("URL");
            DateCol = new TableColumn<HistoryModel, Date>("Date");
            TimeCol = new TableColumn<HistoryModel, Long>("Time spend");
            URLCol.setCellValueFactory(new PropertyValueFactory<HistoryModel, String>("url"));
            DateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            TimeCol.setCellValueFactory(new PropertyValueFactory<>("timeSpend"));
            tableView.setItems(historyData);
            StackPane pane = new StackPane();
            tableView.getColumns().addAll(URLCol, DateCol, TimeCol);
            pane.getChildren().add(tableView);

            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.showAndWait();
    }
}
