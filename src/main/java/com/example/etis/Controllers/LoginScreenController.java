package com.example.etis.Controllers;

import com.example.etis.Query.QueryTools.QueryHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
    public void onLogin(ActionEvent actionEvent) throws SQLException {

        username.getText().trim();
        password.getText().trim();
        creds = new Pair<>(username.getText(), password.getText());

        System.out.println(username.getText() + " " + password.getText());

        mContr.setqHandler(new QueryHandler(creds));

        //
        //
        //  execute log in block
        //
        //

        try (Connection c = mContr.getqHandler().getConnection()) {
            System.out.println("Connected successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
