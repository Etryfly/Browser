import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.ArrayList;

public class Controller {
    ArrayList<Tab> tabs = new ArrayList<>();

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
        AnchorPane pane = new AnchorPane();
        WebView view = new WebView();
        pane.getChildren().add(view);
        tab.setContent(pane);
        tab.setText("New tab");
        tabPane.getTabs().add(tab);
        tabs.add(tab);
    }

    @FXML
    public void initialize(){
        webEngine = webView.getEngine();
        tabs.add(tabPane.getTabs().get(0));
    }

    @FXML
    public void Search() {
//        String url = searchRow.getText();
//        webEngine.load(url);
//        System.out.println(webEngine.getLoadWorker().stateProperty());
    }

}
