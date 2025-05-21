package com.example.etis.Controllers;


import com.example.etis.Query.Helpers.Privilege;
import com.example.etis.Query.Helpers.Tables;
import com.example.etis.Query.QueryTools.QueryHandler;
import com.example.etis.Query.SQLTable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private QueryHandler qHandler;

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

    @FXML
    private TableView<ObservableList<?>> dataView;

    private Privilege privs;

    ObjectProperty<String> status = new SimpleObjectProperty<>(null);

    private final Map<String, SQLTable<?>> map = new HashMap<>();

    private boolean loggedIn = false;

    private SQLTable<Tables.Teismai> teismaiSQLTable;
    private SQLTable<Tables.BylosDetales> bylosDetalesSQLTable;
    private SQLTable<Tables.Byla> bylaSQLTable;
    private SQLTable<Tables.Bylos_Posedziai> bylosPosedziaiSQLTable;
    private SQLTable<Tables.Bylos_Dalyviai> bylosDalyviaiSQLTable;
    private SQLTable<Tables.ProcesoDalyvis> procesodalyvisSQLTable;
    private SQLTable<Tables.Bylos_Ilgio_Metrika> Bylu_Ilgio_MetrikaSQLTable;
    private SQLTable<Tables.Bylos_Eigoje> BylosEigojeSQLTable;
    private SQLTable<Tables.Ateinantys_Posedziai> ateinantysPosedziaiSQLTable;

    public <T> void refreshTable(SQLTable<T> sqlTable, Class<T> clazz) throws SQLException {
        userTable.getColumns().clear();
        for (RecordComponent rc : clazz.getRecordComponents()) {
            TableColumn<Object, Object> col = new TableColumn<>(rc.getName());
            col.setCellValueFactory(cd -> {
                try {
                    Object v = rc.getAccessor().invoke(cd.getValue());
                    return new SimpleObjectProperty<>(v);
                } catch (Exception e) {
                    return new SimpleObjectProperty<>(null);
                }
            });
            userTable.getColumns().add(col);
        }

        userTable.setItems(FXCollections.observableArrayList(sqlTable.selectQuery()));
    }



    @FXML
    public void initialize() throws SQLException {

        tableSelection.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            try {
                switch (n) {
                    case "Teismai"                      -> refreshTable(teismaiSQLTable                    ,     Tables.Teismai.class);
                    case "Bylu ilgio metrika"           -> refreshTable(Bylu_Ilgio_MetrikaSQLTable         ,     Tables.Bylos_Ilgio_Metrika.class);
                    case "Bylos Eigoje"                 -> refreshTable(BylosEigojeSQLTable                ,     Tables.Bylos_Eigoje.class);
                    case "Byla"                         -> refreshTable(bylaSQLTable                       ,     Tables.Byla.class);
                    case "Posedziai"                    -> refreshTable(bylosPosedziaiSQLTable             ,     Tables.Bylos_Posedziai.class);
                    case "Bylos Dalyviai"               -> refreshTable(bylosDalyviaiSQLTable              ,     Tables.Bylos_Dalyviai.class);
                    case "Proceso Dalyviai"             -> refreshTable(procesodalyvisSQLTable             ,     Tables.ProcesoDalyvis.class);
                    case "Bylos Detales"                -> refreshTable(bylosDetalesSQLTable               ,     Tables.BylosDetales.class);
                    case "Ateinantys Posedziai"         -> refreshTable(ateinantysPosedziaiSQLTable        ,     Tables.Ateinantys_Posedziai.class);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        tableSelection.valueProperty().addListener((o, ov, nv) -> {
            try {
                refresh();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        status.addListener((o, ov, nv) -> curUser.setText(nv));

        userTextUpdate(Privilege.root);

    }

    void userTextUpdate(Privilege priv) {
        this.privs = priv;
    }

    @FXML
    void onFind() {

    }

    @FXML
    public AnchorPane contentPane;

    public void onClose() {
        System.gc();
        System.exit(0);
    }

    @FXML
    void onLogin() throws IOException, SQLException {

        logContr = switchView("/com/example/etis/LoginScreen.fxml", mainWindowController -> {
            try {
                return new LoginScreenController(mainWindowController);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <C> C switchView(String fxmlPath, Function<MainWindowController, C> controllerFactory) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(type -> controllerFactory.apply(this));
        Parent view = loader.load();
        contentPane.getChildren().setAll(view);
        return loader.getController();
    }

    public ObjectProperty<String> statusProperty() {
        return status;
    }

    private void refresh() throws SQLException {
        String key = tableSelection.getValue();
        if (key == null || !map.containsKey(key)) return;

        ObservableList<List<?>> raw = (ObservableList<List<?>>) map.get(key).selectQuery();
        ObservableList<ObservableList<?>> rows = FXCollections.observableArrayList();
        for (List<?> r : raw) rows.add(FXCollections.observableArrayList(r));
        dataView.setItems(rows);
    }

    public void setqHandler(QueryHandler qh) throws SQLException {
        qHandler = qh;

        teismaiSQLTable             = new SQLTable<>(qHandler, Tables.Teismai.class);
        bylosDetalesSQLTable        = new SQLTable<>(qHandler, Tables.BylosDetales.class);
        bylaSQLTable                = new SQLTable<>(qHandler, Tables.Byla.class);
        bylosPosedziaiSQLTable      = new SQLTable<>(qHandler, Tables.Bylos_Posedziai.class);
        bylosDalyviaiSQLTable       = new SQLTable<>(qHandler, Tables.Bylos_Dalyviai.class);
        procesodalyvisSQLTable      = new SQLTable<>(qHandler, Tables.ProcesoDalyvis.class);
        BylosEigojeSQLTable         = new SQLTable<>(qHandler, Tables.Bylos_Eigoje.class);
        Bylu_Ilgio_MetrikaSQLTable  = new SQLTable<>(qHandler, Tables.Bylos_Ilgio_Metrika.class);
        ateinantysPosedziaiSQLTable = new SQLTable<>(qHandler, Tables.Ateinantys_Posedziai.class);

        tableSelection.getItems().setAll(
                "Teismai", "Posedziai", "Bylu ilgio metrika",
                "Byla", "Bylos Dalyviai", "Proceso Dalyviai",
                "Bylos Eigoje",  "Ateinantys Posedziai"
        );
        tableSelection.getSelectionModel().selectFirst();
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

    public QueryHandler getqHandler() {
        return qHandler;
    }


    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}