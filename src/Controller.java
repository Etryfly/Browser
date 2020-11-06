import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Controller {
    ArrayList<Tab> tabs = new ArrayList<>();
    HashMap<Tab, HistoryModel> historyHashMap = new HashMap<>();
    Gson gson = new Gson();


    private TableView<HistoryModel> tableView;


    private TableColumn<HistoryModel, String> URLCol;

    private final ObservableList<HistoryModel> historyData =
            FXCollections.observableArrayList();

    private boolean isHistoryHiddenForAll;
    private List<String> hiddenURLs = new ArrayList<>();

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
    private void backButtonOnClick() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) return;
        WebView view = getWebViewInTab(tab);
        Platform.runLater(() -> view.getEngine().executeScript("history.back()"));
    }

    @FXML
    public void forwardButtonOnClick(MouseEvent mouseEvent) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) return;
        WebView view = getWebViewInTab(tab);
        Platform.runLater(() -> view.getEngine().executeScript("history.forward()"));
    }

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
        if (!isHistoryHiddenForAll && !hiddenURLs.contains(url)) {
            HistoryModel historyModel = new HistoryModel();
            historyModel.setUrl(url);
            Date currDate = new Date(System.currentTimeMillis());

            historyModel.setDate(currDate);
            historyHashMap.put(tab, historyModel);
        }
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
        if (model != null) {
            model.setTimeSpend(System.currentTimeMillis() - model.getDate().getTime());

            historyData.add(model);
        }

    }

    @FXML
    public void initialize(){

    }

    public void onClose() {
        if (historyData.size() > 0) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("history.json"))) {
                for (var entry : historyHashMap.entrySet()) {
                    closeHistory(entry.getKey());
                }
                bufferedWriter.write("[");
                for (int i = 0; i < historyData.size() - 1; i++) {
                    HistoryModel model = historyData.get(i);
                    bufferedWriter.write(gson.toJson(model));
                    bufferedWriter.write(",\n");
                }
                bufferedWriter.write(gson.toJson(historyData.get(historyData.size() - 1)) + "]");
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void Search() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        String url = searchRow.getText();
        closeHistory(selectedTab);
        openHistory(selectedTab, url);

        WebView view = getWebViewInTab(selectedTab);
        view.getEngine().load(url);
    }

    private WebView getWebViewInTab(Tab tab) {

        AnchorPane pane = (AnchorPane) tab.getContent();
        for (Node paneNode : pane.getChildren()) {
            if (paneNode instanceof WebView) {
                return (WebView) paneNode;
            }
        }

        return null;
    }

    private boolean isHistoryLoaded = false;

    @FXML
    public void HistoryOnAction() {

            if (!isHistoryLoaded) {
                try (JsonReader reader = new JsonReader(new FileReader("history.json"))) {
                    Type REVIEW_TYPE = new TypeToken<List<HistoryModel>>() {
                    }.getType();
                    Gson gson = new Gson();
                    List<HistoryModel> modelsList = gson.fromJson(reader, REVIEW_TYPE);
                    if (modelsList != null) {
                        historyData.addAll(modelsList);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isHistoryLoaded = true;
            }

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

    public void HideSettingsOnClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HistoryHide.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            HideHistory controller = loader.getController();
            isHistoryHiddenForAll = controller.isAllSitesHidden();
            hiddenURLs = controller.getHiddenUrls();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void refreshOnClick(ActionEvent actionEvent) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab != null) {
            getWebViewInTab(tab).getEngine().reload();
        }
    }

    @FXML
    public void FavoritesOnClick(ActionEvent actionEvent) {
        TableView<String> view = new TableView<>();
        TableColumn<String, String> col = new TableColumn<>("URL");
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        ObservableList<String> data =
                FXCollections.observableArrayList(favoriteUrls);

        view.setItems(data);
        AnchorPane pane = new AnchorPane();
        view.getColumns().add(col);
        pane.getChildren().add(view);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        AnchorPane.setBottomAnchor(okButton, 30.);
        AnchorPane.setBottomAnchor(cancelButton, 0.);
        pane.getChildren().add(okButton);
        pane.getChildren().add(cancelButton);



        Stage stage = new Stage();
        stage.setScene(new Scene(pane));
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        okButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getWebViewInTab(tab).getEngine().load(view.getSelectionModel().getSelectedItem());
                stage.close();
            }
        });

        cancelButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                stage.close();
            }
        });
        stage.showAndWait();





    }

    ArrayList<String> favoriteUrls = new ArrayList<>();



    @FXML
    public void AddToFavoriteOnClick(ActionEvent actionEvent) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab != null) {
            favoriteUrls.add(getWebViewInTab(tab).getEngine().getLocation());
        }
    }


    @FXML
    public void htmlEditorOnClick(ActionEvent actionEvent) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) return;
        Stage stage = new Stage();
        VBox pane = new VBox();
        TextArea text = new TextArea();
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Save");
        MenuItem item = new MenuItem("Save");
        item.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
            fileChooser.getExtensionFilters().add(extFilter);
            Stage saveDialogStage = new Stage();
            //Show save file dialog
            File file = fileChooser.showSaveDialog(saveDialogStage);

            if (file != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))){

                    writer.write(text.getText());

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            saveDialogStage.close();
        });
        menu.getItems().add(item);
        menuBar.getMenus().add(menu);

        pane.getChildren().addAll(menuBar, text);

        stage.setScene(new Scene(pane));


        WebView view = getWebViewInTab(tab);
        text.setText( (String) view.getEngine().executeScript("document.documentElement.outerHTML"));


        stage.showAndWait();
    }
}
