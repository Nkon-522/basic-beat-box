package org.nkon.beatbox;

import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;

import javax.sound.midi.*;
import java.io.*;

import static javax.sound.midi.ShortMessage.*;

public class BeatBoxController {

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    FileChooser fileChooser = new FileChooser();

    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public void closeSequencer() {
        sequencer.close();
    }

    public void stopSequencer() {
        sequencer.stop();
    }

    private void makeTracks(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];
            if (key != 0) {
                track.add(makeEvent(NOTE_ON, 9, key, 100, i));
                track.add(makeEvent(NOTE_OFF, 9, key, 100, i + 1));
            }
        }
    }

    private MidiEvent makeEvent(int command, int channel, int one, int two, int tick) {
        MidiEvent midiEvent = null;
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(command, channel, one, two);
            midiEvent = new MidiEvent(msg, tick);
        } catch (InvalidMidiDataException e) {
            e.getCause();
        }
        return midiEvent;
    }

    public void changeTempo(float tempoMultiplier) {
        float tempoFactor = sequencer.getTempoFactor();
        sequencer.setTempoFactor(tempoFactor * tempoMultiplier);
    }

    public void writeFile(CheckBox[][] checkBoxes) {
        File file = fileChooser.showSaveDialog(null);
        if (file == null) { return; }

        boolean [][] checkBoxState = new boolean[checkBoxes.length][checkBoxes[0].length];
        for (int i = 0; i < checkBoxes.length; i++) {
            for (int j = 0; j < checkBoxes[0].length; j++) {
                if (checkBoxes[i][j].isSelected()) {
                    checkBoxState[i][j] = true;
                }
            }
        }

        try (ObjectOutputStream os =
                     new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()))
        ) {
            os.writeObject(checkBoxState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile(CheckBox[][] checkBoxes) {
        File file = fileChooser.showOpenDialog(null);
        if (file == null) { return; }

        boolean[][] checkboxState = null;
        try (ObjectInputStream is =
                     new ObjectInputStream(new FileInputStream(file.getAbsolutePath()))
        ) {
            checkboxState = (boolean[][]) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (checkboxState == null) {return;}
        for (int i = 0; i < checkBoxes.length; i++) {
            for (int j = 0; j < checkBoxes[0].length; j++) {
                checkBoxes[i][j].setSelected(checkboxState[i][j]);
            }
        }
        sequencer.stop();
        buildTrackAndStart(checkBoxes);
    }

    public void buildTrackAndStart(CheckBox[][] checkBoxes) {
        int[] trackList;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < checkBoxes.length; i++) {
            trackList = new int[checkBoxes.length];
            int key = instruments[i];

            for (int j = 0; j < checkBoxes[0].length; j++) {
                if (checkBoxes[i][j].isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }

            makeTracks(trackList);
            track.add(makeEvent(CONTROL_CHANGE, 1, 127, 0, 16));
        }

        track.add(makeEvent(PROGRAM_CHANGE, 9, 1, 0, 15));

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.setTempoInBPM(120);
            sequencer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (MidiUnavailableException | InvalidMidiDataException e) {
            System.out.println(e.getCause().toString());
        }
    }

    public BeatBoxController() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a file (*.ser)", "*.ser");
        fileChooser.getExtensionFilters().add(filter);
    }
}
