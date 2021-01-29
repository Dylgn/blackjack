module dylan {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    opens dylan to javafx.fxml;
    exports dylan;
}