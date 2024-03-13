package org.nkon.beatbox;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BeatBoxApplication extends Application {
    BeatBoxView beatBoxView = new BeatBoxView();

    @Override
    public void stop() {
        beatBoxView.close();
    }

    @Override
    public void start(Stage stage) {

        Group parent = new Group();
        parent.getChildren().add(beatBoxView.getBorderPane());
        Scene scene = new Scene(parent);

        stage.setTitle("BeatBox!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}