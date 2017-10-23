package sample;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.Socket;
import java.io.*;
import java.util.LinkedList;

public class ChatClient{
    final Socket socket;
    final BufferedReader socketReader;
    final BufferedWriter socketWriter;
    final BufferedReader userInput;
    final String login;
    private Receiver receiver;


    public ChatClient(String host, int port, String login) throws IOException {
        socket = new Socket(host, port);
        socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        this.login = login;

        userInput = new BufferedReader(new InputStreamReader(System.in));
        receiver = new Receiver();
        new Thread(receiver).start();
    }

    public Receiver getReceiver(){
        return receiver;
    }

    public void sendMessage(String message){
        try {
                socketWriter.write(login + ": " + message);
                socketWriter.write("\n");
                socketWriter.flush();
            //messages.add(login + ": " + message);
        } catch (IOException e) {
            close();
        }
    }


    public synchronized void close() {
        if (!socket.isClosed()) {
            try {
                socket.close();
                System.exit(0);
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }


    //public static void main(String[] args) {

    //}


    public class Receiver implements Runnable {
        String line;
        private ObservableList<String> messages = FXCollections.observableArrayList();

        public ObservableList<String> getMessages(){
            return messages;
        }

        private ObservableList<String> participants = FXCollections.observableArrayList();

        public ObservableList<String> getParticipants(){
            return participants;
        }

        private synchronized void AdderMessages(String line){
            Platform.runLater(() -> {
                messages.add(line);
            });
        }

        private synchronized void AdderParticipants(String participantName){
            Platform.runLater(() -> {
                participants.add(participantName);
            });
        }

        private synchronized void RemoveParticipants(String participantName){
            Platform.runLater(() -> {
                participants.remove(participantName);
            });
        }

        public synchronized void MuteParticipant(String nameOfParticipant){
            Platform.runLater(() -> {
                if(!nameOfParticipant.contains("Muted") && !nameOfParticipant.equals(login)) {
                    participants.remove(nameOfParticipant);
                    participants.add(nameOfParticipant + " (Muted)");
                }
                //System.out.println(nameOfParticipant + " (Muted)");
            });
        }

        public synchronized void UnMuteParticipant(String mutedParticipant){
            Platform.runLater(() -> {
                participants.remove(mutedParticipant);
                participants.add(mutedParticipant.split(" ")[0]);
            });
        }

        public void run() {
            while (!socket.isClosed()) {
                line = null;
                try {
                    line = socketReader.readLine();
                } catch (IOException e) {
                    if ("Socket closed".equals(e.getMessage())) {
                        break;
                    }
                    System.out.println("Connection lost");
                    close();
                }
                if(line == null){
                    System.out.println("Server has closed connection");
                    close();
                } else{
                    if(line.split(":")[1].equals(" ~newBody")){
                        AdderParticipants(line.split(":")[0]);
                    }
                    else if(line.split(":")[1].equals(" ~killBody")){
                        RemoveParticipants(line.split(":")[0]);
                    }
                    else{
                        if(!participants.contains(line.split(":")[0] + " (Muted)")){
                            AdderMessages(line);
                        }

                    }

                }

            }
        }

    }
}