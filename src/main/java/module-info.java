module com.example.etis {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens com.example.etis to javafx.fxml;
    exports com.example.etis;
}