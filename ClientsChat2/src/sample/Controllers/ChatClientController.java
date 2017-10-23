package sample.Controllers;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import sample.ChatClient;
import sun.rmi.runtime.Log;

import java.util.Observable;

public class ChatClientController {

        @FXML
        private TextField txtFieldForMessage;

        @FXML
        private ListView<String> fldMainMessages;// = new ListView<String>();

        @FXML
        private ListView<String> fldListOfParticipants;


    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            fldMainMessages.setItems(LogInController.CurrentClient.getReceiver().getMessages());
            fldListOfParticipants.setItems(LogInController.CurrentClient.getReceiver().getParticipants());
        });
    }

    public void OnSendMessage(ActionEvent actionEvent){
        if(txtFieldForMessage.getText().length() > 0) {
            LogInController.CurrentClient.sendMessage(txtFieldForMessage.getText());
            txtFieldForMessage.setText("");
        }
    }

    public void OnDisconnect(ActionEvent actionEvent){
        LogInController.CurrentClient.sendMessage("~killBody");
    }

    public void OnUnMute(ActionEvent actionEvent){
        String selectedNameOfParticipant = fldListOfParticipants.getFocusModel().getFocusedItem();
        LogInController.CurrentClient.getReceiver().UnMuteParticipant(selectedNameOfParticipant);
    }


    public void OnMuteParticipant(ActionEvent actionEvent){

        String selectedNameOfParticipant = fldListOfParticipants.getFocusModel().getFocusedItem();
        //String.valueOf(fldListOfParticipants.getItems()).replaceAll("[^A-Za-zА-Яа-я0-9]", "");

        Platform.runLater(() -> {
            LogInController.CurrentClient.getReceiver().MuteParticipant(selectedNameOfParticipant);
        });
    }


}