module com.example.etis {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires java.sql;

    opens com.example.etis to javafx.fxml;
    exports com.example.etis;
    exports com.example.etis.Helpers;
    opens com.example.etis.Helpers to javafx.fxml;
    exports com.example.etis.Controllers;
    opens com.example.etis.Controllers to javafx.fxml;
}