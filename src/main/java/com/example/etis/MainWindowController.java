package com.example.etis;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.util.function.Function;

/*

        ----- LOGIN -----

    switch fxml on login btn

    after login set mainContrl privs as something

    will check the cached value

    will update perms -> update allowed tables.


        ----- QUERY -----

    Query Handler will most likely handle the queries and for users above "user" perms will allow to insert things

    for users above "priv" will let add more things such as new judges or...


        -----  -----

*/



public class MainWindowController {

    private LoginScreenController logContr;


    public Button closeButton;

    @FXML
    public Text curUser;

    @FXML
    public Button login;

    @FXML
    public Button find;

    @FXML
    public TableView<Object> userTable;

    @FXML
    public ComboBox<String> tableSelection;

    private Privilege privs;

    ObjectProperty<Privilege> status = new SimpleObjectProperty<Privilege>(null);

    private Pair<String, String> creds;

    @FXML
    public void initialize() {
        tableSelection.getItems().addAll("Teismai", "Posedziai", "Bylu Ilgiai");

        status.addListener((obs, oldVal, newVal) -> {
            curUser.setText("Vartotojo lygmuo: " + newVal);
        });

        userTextUpdate(Privilege.root);

    }

    void userTextUpdate(Privilege priv) {
        this.privs = priv;
    }

    @FXML
    void onFind() {


        //
        //
        //  execute query table block after which expandTableSelection();
        //
        //


    }

    private void expandTableSelection() {

    }


    @FXML
    public AnchorPane contentPane;

    void loginHideInvert() {
        login.setDisable(!login.isDisabled());
        login.setVisible(!login.isVisible());
    }

    void loginHide() {
        login.setDisable(true);
        login.setVisible(false);
    }

    public void onClose() {
        System.gc();
        System.exit(0);
    }

    @FXML
    void onLogin() throws IOException {

        logContr = switchView("LoginScreen.fxml", mainWindowController -> {
            try {
                return new LoginScreenController(mainWindowController);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        if (privs == Privilege.user) return;
        loginHide();

    }


    public <C> C switchView(String fxmlPath, Function<MainWindowController, C> controllerFactory) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(type -> controllerFactory.apply(this));
        Parent view = loader.load();
        contentPane.getChildren().setAll(view);
        return loader.getController();
    }


    public void setPrivs(Privilege privs) {
        this.privs = privs;
    }

    public LoginScreenController getLogContr() {
        return logContr;
    }

    public void setLogContr(LoginScreenController logContr) {
        this.logContr = logContr;
    }
}