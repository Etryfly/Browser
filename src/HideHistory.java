import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HideHistory {

    @FXML
    public CheckBox HideForAllCheckBox;

    @FXML
    public TextArea TextBox;

    @FXML
    public Button okButton;


    @FXML
    public void OkButtonOnClick(MouseEvent mouseEvent) {
        isAllSitesHidden = HideForAllCheckBox.isSelected();
        String urls = TextBox.getText();
        hiddenUrls = Arrays.asList(urls.split("\n"));
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();

    }

    private boolean isAllSitesHidden;

    private List<String> hiddenUrls = new ArrayList<>();

    public boolean isAllSitesHidden() {
        return isAllSitesHidden;
    }

    public List<String> getHiddenUrls() {
        return hiddenUrls;
    }
}
