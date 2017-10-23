package sample.Controllers;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import sample.ChatClient;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class LogInController {

    public static ChatClient CurrentClient;

    @FXML
    private Button connectButton;

    @FXML
    private TextField NickName;

    @FXML
    private TextField IPAddress;

    @FXML
    private TextField PortOfServer;

    public void ConnectionButton(ActionEvent actionEvent) {

        //connectButton.setText("1");

        Stage stage = (Stage) connectButton.getScene().getWindow();
        stage.close();

        try {
            Stage stage1 = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("../View/ChatWindow.fxml"));
            stage1.setTitle("ChatWindow");
            stage1.setScene(new Scene(root));
            stage1.initModality(Modality.WINDOW_MODAL);
            stage1.initOwner(((Node)actionEvent.getSource()).getScene().getWindow());
            stage1.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //try {
        //    new ChatClient(IPAddress.getText(), Integer.parseInt(PortOfServer.getText()), NickName.getText()).run();
        //} catch (IOException e) {
        //    System.out.println("Unable to connect. Server not running?");
        //}

        try {
            CurrentClient = new ChatClient(IPAddress.getText(), Integer.parseInt(PortOfServer.getText()), NickName.getText());
            CurrentClient.sendMessage("~newBody");
        } catch (IOException e) {
            System.out.println("Unable to connect. Server not running?");
        }


    }

}
