package com.example.etis;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    Pair<String, String> creds;

    MainWindowController mContr;

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



    @FXML
    public void onLogin(ActionEvent actionEvent) {

        username.getText().trim();
        password.getText().trim();

        System.out.println(username.getText() + " " + password.getText());

        //
        //
        //  execute log in block
        //
        //

    }
}
