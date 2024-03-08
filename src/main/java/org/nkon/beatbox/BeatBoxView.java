package org.nkon.beatbox;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BeatBoxView {
    BeatBoxController beatBoxController = new BeatBoxController();

    private final BorderPane borderPane = new BorderPane();
    ;
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
    Button startButton = new Button("Start");
    Button stopButton = new Button("Stop");
    Button tempoUpButton = new Button("Tempo Up");
    Button tempoDownButton = new Button("Tempo Down");

    private void setUpButtons() {
        VBox buttonsVBox = new VBox();
        buttonsVBox.getChildren().add(startButton);
        buttonsVBox.getChildren().add(stopButton);
        buttonsVBox.getChildren().add(tempoUpButton);
        buttonsVBox.getChildren().add(tempoDownButton);

        startButton.setOnAction(e -> beatBoxController.buildTrackAndStart(checkBoxes));
        stopButton.setOnAction(e -> beatBoxController.stopSequencer());
        tempoUpButton.setOnAction(e -> beatBoxController.changeTempo(1.03f));
        tempoDownButton.setOnAction(e -> beatBoxController.changeTempo(0.97f));

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
        setUpInstruments();
        setUpCheckBoxes();
        setUpButtons();
        beatBoxController.setUpMidi();
    }

    public void close() {
        beatBoxController.closeSequencer();
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }
}
