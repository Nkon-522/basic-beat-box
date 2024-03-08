module org.example.beatbox {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens org.nkon.beatbox to javafx.fxml;
    exports org.nkon.beatbox;
}