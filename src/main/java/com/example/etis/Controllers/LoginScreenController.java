package com.example.etis.Controllers;

import com.example.etis.Query.Helpers.Privilege;
import com.example.etis.Query.QueryTools.QueryHandler;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class LoginScreenController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    Pair<String, String> creds;

    final MainWindowController mContr;

    public LoginScreenController(MainWindowController mainWindowController) throws IOException {
        mContr = mainWindowController;
    }

    Pair<String, String> getCreds() {
        return creds;
    }

    public void onClose() {
        System.exit(0);
    }

    @FXML
    public AnchorPane contentPaneL;

    private <C> C switchView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();
        contentPaneL.getChildren().setAll(view);
        return loader.getController();
    }

    @FXML
    private void onLogin() throws Exception {
        String u = username.getText().trim();
        String p = password.getText().trim();

        QueryHandler qh = new QueryHandler(new Pair<>(u, p));

        MainWindowController main =
                switchView("/com/example/etis/hello-view.fxml");

        main.setqHandler(qh);
        main.statusProperty().set(u);
    }
}
