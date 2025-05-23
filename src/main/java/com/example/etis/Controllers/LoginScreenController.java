package com.example.etis.Controllers;

import com.example.etis.Query.QueryTools.QueryHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;


import java.io.IOException;

public class LoginScreenController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    final MainWindowController mContr;

    public LoginScreenController(MainWindowController mainWindowController) throws IOException {
        mContr = mainWindowController;
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

        MainWindowController main = switchView("/com/example/etis/hello-view.fxml");

        main.setqHandler(qh);
        main.statusProperty().set(u);
        main.setCreds(new Pair<>(u, p));
    }
}
