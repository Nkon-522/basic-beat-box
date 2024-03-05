module org.example.beatbox {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.example.beatbox to javafx.fxml;
    exports org.example.beatbox;
}