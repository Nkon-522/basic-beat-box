package org.nkon.beatbox;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BeatBoxView {
    private final double verticalSpacing = 30;
    @SuppressWarnings("FieldCanBeLocal")
    private final double horizontalSpacing = 15;

    private final double padding = 10;
    BeatBoxController beatBoxController = new BeatBoxController();

    private final BorderPane borderPane = new BorderPane();

    // Left
    boolean isConnected;
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

    private void handleConnection() {
        connectButton.setDisable(isConnected);
        sendButton.setDisable(!isConnected);
    }

    private void setUpButtons() {
        VBox buttonsVBox = new VBox();
        buttonsVBox.setSpacing(verticalSpacing-10);
        buttonsVBox.setPadding(new Insets(padding));

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

        connectButton.setOnAction(e -> {isConnected = beatBoxController.setUpConnection(); handleConnection();});
        startButton.setOnAction(e -> beatBoxController.buildTrackAndStart(checkBoxes));
        stopButton.setOnAction(e -> beatBoxController.stopSequencer());
        tempoUpButton.setOnAction(e -> beatBoxController.changeTempo(1.03f));
        tempoDownButton.setOnAction(e -> beatBoxController.changeTempo(0.97f));
        serializeButton.setOnAction(e -> beatBoxController.writeFile(checkBoxes));
        restoreButton.setOnAction(e -> beatBoxController.readFile(checkBoxes));
        sendButton.setOnAction(e -> {isConnected = beatBoxController.sendMessage(textArea, checkBoxes); handleConnection();});

        handleConnection();

        textArea.setWrapText(true);
        textArea.setPrefHeight(80);

        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setPrefHeight(150);

        beatBoxController.setListView(listView);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> beatBoxController.loadTrack(new_val, checkBoxes)
        );

        borderPane.setRight(buttonsVBox);
    }

    private void setUpCheckBoxes() {
        VBox checkBoxesVBox = new VBox();
        checkBoxesVBox.setSpacing(verticalSpacing);
        checkBoxesVBox.setPadding(new Insets(padding));

        for (int i = 0; i < number_rows; i++) {
            HBox checkBoxRow = new HBox();
            checkBoxRow.setSpacing(horizontalSpacing);
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
        instrumentsLabelsVBox.setSpacing(verticalSpacing);
        instrumentsLabelsVBox.setPadding(new Insets(padding));

        for (String instrumentName : instrumentNames) {
            Label instrumentLabel = new Label(instrumentName);
            instrumentsLabelsVBox.getChildren().add(instrumentLabel);
        }
        borderPane.setLeft(instrumentsLabelsVBox);
    }

    BeatBoxView() {
        isConnected = beatBoxController.setUpConnection();
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
