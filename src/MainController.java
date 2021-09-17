import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private TextField hostTextField;
    @FXML
    private TextField userTextField;
    @FXML
    private Button submitButton;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void submit(ActionEvent e) throws Exception {
        String host = hostTextField.getText();
        String user = userTextField.getText();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Chat.fxml"));
        root = loader.load();
        ChatController controller = loader.getController();
        controller.initChat(user, host);
        this.switchToChat(e, root);
    }

    public void switchToChat(ActionEvent e, Parent root) throws IOException {
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
