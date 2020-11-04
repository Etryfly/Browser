import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Controller {
    ArrayList<Tab> tabs = new ArrayList<>();
    HashMap<Tab, HistoryModel> historyHashMap = new HashMap<>();
    Gson gson = new Gson();
    BufferedWriter bufferedWriter;
    {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter("history.json", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        tab.setOnClosed(event -> closeHistory((Tab) event.getSource()));
        AnchorPane pane = new AnchorPane();
        WebView view = new WebView();
        String startUrl = "http://google.com";
        view.setPrefWidth(1000);
        view.setPrefHeight(800);
        view.setId("WebView");
        view.getEngine().load(startUrl);
        pane.setId("Pane");
        pane.getChildren().add(view);
        tab.setContent(pane);
        openHistory(tab, startUrl);
        tab.setText("New tab");
        tabPane.getTabs().add(tab);
        tabs.add(tab);

    }


    private void openHistory(Tab tab, String url) {

        HistoryModel historyModel = new HistoryModel();
        historyModel.setUrl(url);
        Date currDate = new Date(System.currentTimeMillis());

        historyModel.setDate(currDate);
        historyHashMap.put(tab, historyModel);
    }

    @FXML
    private void SavePage() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage saveDialogStage = new Stage();
        //Show save file dialog
        File file = fileChooser.showSaveDialog(saveDialogStage);
//        saveDialogStage.showAndWait();
        if (file != null) {
            AnchorPane pane = (AnchorPane) selectedTab.getContent();
            for (Node paneNode : pane.getChildren()) {
                if (paneNode instanceof WebView) {
                    WebView view = (WebView) paneNode;
                    Document document = view.getEngine().getDocument();


                    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file))) {
                        ZipEntry zip = new ZipEntry(view.getEngine().getTitle());
                        zipOutputStream.putNextEntry(zip);
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                        transformer.transform(new DOMSource(document),
                                new StreamResult(new OutputStreamWriter(zipOutputStream, "UTF-8")));
////                        transformer.transform(new DOMSource(document),
//                                new StreamResult(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
                        zipOutputStream.finish();
                        zipOutputStream.flush();
                        zipOutputStream.closeEntry();


                    } catch (TransformerConfigurationException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        saveDialogStage.close();
    }

    private void closeHistory(Tab tab) {

        HistoryModel model = historyHashMap.get(tab);
        model.setTimeSpend( System.currentTimeMillis() - model.getDate().getTime());

        historyData.add(model);
        try {
            bufferedWriter.write(gson.toJson(model));
            bufferedWriter.write(",\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void initialize(){

    }

    public void onClose() {
        //call history close
        try {
            for (var entry : historyHashMap.entrySet()) {
                closeHistory(entry.getKey());
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void Search() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        String url = searchRow.getText();
        closeHistory(selectedTab);
        openHistory(selectedTab, url);

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
