module com.example.etis {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jdk.javadoc;
    requires jdk.internal.le;

    opens com.example.etis to javafx.fxml;
    exports com.example.etis;
    exports com.example.etis.Query.Helpers;
    opens com.example.etis.Query.Helpers to javafx.fxml;
    exports com.example.etis.Controllers;
    opens com.example.etis.Controllers to javafx.fxml;
    exports com.example.etis.Query;
    opens com.example.etis.Query to javafx.fxml;
    exports com.example.etis.Query.QueryTools;
    opens com.example.etis.Query.QueryTools to javafx.fxml;
    exports com.example.etis.Query.Helpers.Types;
    opens com.example.etis.Query.Helpers.Types to javafx.fxml;
    exports com.example.etis.Query.Helpers.EnumHelper;
    opens com.example.etis.Query.Helpers.EnumHelper to javafx.fxml;
}