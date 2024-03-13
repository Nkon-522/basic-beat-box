package org.nkon.beatbox;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import javax.sound.midi.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.sound.midi.ShortMessage.*;

public class BeatBoxController {

    Socket socket;
    ExecutorService executorService;

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;


    FileChooser fileChooser = new FileChooser();

    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    private String userName;
    private int nextNum;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private final HashMap<String, boolean[][]> otherSeqsMap = new HashMap<>();
    private ListView<String> listView;

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

    private boolean [][] setCheckBoxState(CheckBox[][] checkBoxes) {
        boolean [][] checkBoxState = new boolean[checkBoxes.length][checkBoxes[0].length];
        for (int i = 0; i < checkBoxes.length; i++) {
            for (int j = 0; j < checkBoxes[0].length; j++) {
                if (checkBoxes[i][j].isSelected()) {
                    checkBoxState[i][j] = true;
                }
            }
        }
        return checkBoxState;
    }

    public void writeFile(CheckBox[][] checkBoxes) {
        File file = fileChooser.showSaveDialog(null);
        if (file == null) { return; }

        boolean [][] checkBoxState = setCheckBoxState(checkBoxes);

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

    public void sendMessage(TextArea userMessage, CheckBox[][] checkBoxes) {
        boolean[][] checkBoxState = setCheckBoxState(checkBoxes);
        try {
            objectOutputStream.writeObject(userName + nextNum++ + ": " + userMessage.getText() );
            objectOutputStream.writeObject(checkBoxState);
        } catch (IOException e) {
            System.out.println("Terribly sorry. Could not send it to the server.");
            e.printStackTrace();
        }
        userMessage.setText("");
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

    private void changeSequence(boolean[][] checkboxState, CheckBox[][] checkBoxes) {
        for (int i = 0; i < checkBoxes.length; i++) {
            for (int j = 0; j < checkBoxes[0].length; j++) {
                checkBoxes[i][j].setSelected(checkboxState[i][j]);
            }
        }
    }

    public void loadTrack(String selectedValue, CheckBox[][] checkBoxes) {
        boolean[][] selectedState = otherSeqsMap.get(selectedValue);
        changeSequence(selectedState, checkBoxes);
        sequencer.stop();
        buildTrackAndStart(checkBoxes);
    }

    public void setListView(ListView<String> listView) {
        this.listView = listView;
    }

    private class RemoteReader implements Runnable {
        @Override
        public void run() {
            try {
                Object object;
                while ( (object = objectInputStream.readObject()) != null ) {
                    System.out.println("got an object from server!");
                    System.out.println(object.getClass());

                    String nameToShow = (String) object;
                    boolean[][] checkBoxState = (boolean[][]) objectInputStream.readObject();
                    otherSeqsMap.put(nameToShow, checkBoxState);

                    Platform.runLater(()-> listView.getItems().add(nameToShow));
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void setUpConnection() {
        try {
            socket = new Socket("127.0.0.1", 4242);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            userName = "User#"+(int) objectInputStream.readObject()+"|";

            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new RemoteReader());
        } catch (IOException e) {
            System.out.println("Couldn't connect to the server!");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't receive id number!");
        }
    }

    public void closeConnection() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                executorService.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
