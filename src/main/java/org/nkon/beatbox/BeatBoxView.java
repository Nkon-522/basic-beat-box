package org.nkon.beatbox;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BeatBoxView {
    BeatBoxController beatBoxController = new BeatBoxController();

    private final BorderPane borderPane = new BorderPane();

    // Left
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};

    private final int number_rows = 16;
    private final int number_columns = 16;
    private final CheckBox[][] checkBoxes = new CheckBox[number_rows][number_columns];
    // Right
    private final Button connectButton = new Button("Connect");
    private final Button startButton = new Button("Start");
    private final Button stopButton = new Button("Stop");
    private final Button tempoUpButton = new Button("Tempo Up");
    private final Button tempoDownButton = new Button("Tempo Down");
    private final Button serializeButton = new Button("Serialize");
    private final Button restoreButton = new Button("Restore");
    private final Button sendButton = new Button("Send");
    private final TextArea textArea = new TextArea();
    private final ListView<String> listView = new ListView<>();

    private void setUpButtons() {
        VBox buttonsVBox = new VBox();

        buttonsVBox.getChildren().add(connectButton);
        buttonsVBox.getChildren().add(startButton);
        buttonsVBox.getChildren().add(stopButton);
        buttonsVBox.getChildren().add(tempoUpButton);
        buttonsVBox.getChildren().add(tempoDownButton);
        buttonsVBox.getChildren().add(serializeButton);
        buttonsVBox.getChildren().add(restoreButton);
        buttonsVBox.getChildren().add(sendButton);
        buttonsVBox.getChildren().add(textArea);
        buttonsVBox.getChildren().add(listView);

        connectButton.setOnAction(e -> beatBoxController.setUpConnection());
        startButton.setOnAction(e -> beatBoxController.buildTrackAndStart(checkBoxes));
        stopButton.setOnAction(e -> beatBoxController.stopSequencer());
        tempoUpButton.setOnAction(e -> beatBoxController.changeTempo(1.03f));
        tempoDownButton.setOnAction(e -> beatBoxController.changeTempo(0.97f));
        serializeButton.setOnAction(e -> beatBoxController.writeFile(checkBoxes));
        restoreButton.setOnAction(e -> beatBoxController.readFile(checkBoxes));
        sendButton.setOnAction(e -> beatBoxController.sendMessage(textArea, checkBoxes));

        textArea.setWrapText(true);

        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        beatBoxController.setListView(listView);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> beatBoxController.loadTrack(new_val, checkBoxes)
        );

        borderPane.setRight(buttonsVBox);
    }

    private void setUpCheckBoxes() {
        VBox checkBoxesVBox = new VBox();
        for (int i = 0; i < number_rows; i++) {
            HBox checkBoxRow = new HBox();
            for (int j = 0; j < number_columns; j++) {
                checkBoxes[i][j] = new CheckBox();
                checkBoxRow.getChildren().add(checkBoxes[i][j]);
            }
            checkBoxesVBox.getChildren().add(checkBoxRow);
        }
        borderPane.setCenter(checkBoxesVBox);
    }

    private void setUpInstruments() {
        VBox instrumentsLabelsVBox = new VBox();
        for (String instrumentName : instrumentNames) {
            Label instrumentLabel = new Label(instrumentName);
            instrumentsLabelsVBox.getChildren().add(instrumentLabel);
        }
        borderPane.setLeft(instrumentsLabelsVBox);
    }

    BeatBoxView() {
        beatBoxController.setUpConnection();
        setUpInstruments();
        setUpCheckBoxes();
        setUpButtons();
        beatBoxController.setUpMidi();
    }

    public void close() {
        beatBoxController.closeSequencer();
        beatBoxController.closeConnection();
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }
}
