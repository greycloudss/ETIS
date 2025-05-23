package com.example.etis.Controllers;


import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;
import com.example.etis.Query.Helpers.Privilege;
import com.example.etis.Query.Helpers.RecordUtil;
import com.example.etis.Query.Helpers.Tables;
import com.example.etis.Query.QueryTools.QueryBuilder;
import com.example.etis.Query.QueryTools.QueryHandler;
import com.example.etis.Query.SQLTable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;


public class MainWindowController {
    private LoginScreenController logContr;

    private QueryHandler qHandler;

    @FXML
    public Button closeButton, droppy;

    @FXML
    public Text curUser;

    @FXML
    public Button login;

    @FXML
    public TableView<Object> userTable;

    @FXML
    public ComboBox<String> tableSelection, tableActions;

    private Privilege privs;

    ObjectProperty<String> status = new SimpleObjectProperty<>(null);

    private final Map<String, SQLTable<?>> map = new HashMap<>();

    private boolean loggedIn = false;

    private Pair<String, String> creds = new Pair<>("", "");

    private Class<?> currentRowType;

    @FXML
    public AnchorPane contentPane;


    private SQLTable<Tables.Teismai> teismaiSQLTable;
    private SQLTable<Tables.Byla> bylaSQLTable;
    private SQLTable<Tables.Bylos_Posedziai> bylosPosedziaiSQLTable;
    private SQLTable<Tables.Bylos_Dalyviai> bylosDalyviaiSQLTable;
    private SQLTable<Tables.ProcesoDalyvis> procesodalyvisSQLTable;
    private SQLTable<Tables.Bylos_Ilgio_Metrika> Bylu_Ilgio_MetrikaSQLTable;
    private SQLTable<Tables.Bylos_Eigoje> BylosEigojeSQLTable;
    private SQLTable<Tables.Ateinantys_Posedziai> ateinantysPosedziaiSQLTable;

    @FXML
    private Pane queryPane;

    String regexKeyword;


    public <T> void refreshTable(SQLTable<T> sqlTable, Class<T> clazz) throws SQLException {
        userTable.getColumns().clear();
        currentRowType = clazz;
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
        curUser.setText("Vartotojas: viešas");

        tableActions.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            switch (n) {
                case "Pateikti naujus duomenis" -> displayInsert();
                case "Sujungti" -> displayJoins();
                case "Pakeisti duomenis" -> displaySet();
                case "Peržiūtėti duomenis" -> queryPane.getChildren().clear();
            }
        });

        tableSelection.valueProperty().addListener((o, ov, nv) -> {
            try {
                refresh();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        enableRowDeletion();

        userTextUpdate(Privilege.root);
    }

    private void displaySet() {
        queryPane.getChildren().clear();
        String key = tableSelection.getValue();
        if (key == null) return;
        @SuppressWarnings("unchecked")
        SQLTable<Object> tbl = (SQLTable<Object>) map.get(key);
        if (tbl == null) return;
        Class<?> rc = tbl.getRowClass();
        RecordComponent[] comps = rc.getRecordComponents();
        ComboBox<String> cols = new ComboBox<>(
                FXCollections.observableArrayList(
                        Arrays.stream(comps).map(RecordComponent::getName).toList()
                )
        );

        cols.getSelectionModel().selectFirst();
        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField newVal = new TextField();
        newVal.setPromptText("Nauja reikšmė");
        Button apply = new Button("Pakeisti");
        apply.getStyleClass().setAll(login.getStyleClass());
        apply.setOnAction(e -> {
            try {
                tbl.updateColumnById(cols.getValue(), Integer.parseInt(idField.getText()), newVal.getText());
                refreshTable(tbl, (Class<Object>)rc);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        Label stul = new Label("Stulpelis:");
        Label idd= new Label("ID:");
        Label val = new Label("Reikšmė:");

        stul.setFont(new Font("Eras Demi ITC", 10));
        stul.setTextFill(Color.WHITE);

        idd.setFont(new Font("Eras Demi ITC", 10));
        idd.setTextFill(Color.WHITE);

        val.setFont(new Font("Eras Demi ITC", 10));
        val.setTextFill(Color.WHITE);


        HBox form = new HBox(5, stul, cols);
        HBox form1 = new HBox(5, idd, idField);
        HBox form2 = new HBox(5, val, newVal);
        HBox form3 = new HBox(5, apply);

        VBox form4 = new VBox(5, form, form1, form2, form3);

        form.getChildren().stream()
                .filter(n -> n instanceof Label)
                .map(n -> (Label)n)
                .forEach(lbl -> {
                    lbl.setFont(new Font("Eras Demi ITC", 10));
                    lbl.setTextFill(Color.WHITE);
                });
        queryPane.getChildren().add(form4);
    }

    private void displayInsert() {
        queryPane.getChildren().clear();
        String key = tableSelection.getValue();
        if (key == null) return;
        @SuppressWarnings("unchecked")
        SQLTable<Object> table = (SQLTable<Object>) map.get(key);
        if (table == null) return;
        Class<?> rcType = table.getRowClass();
        RecordComponent[] comps = rcType.getRecordComponents();
        VBox form = new VBox(6);
        List<Control> inputs = new ArrayList<>();
        for (var comp : comps) {
            HBox row = new HBox(4);
            Label lbl = new Label(comp.getName() + ":");
            lbl.setFont(new Font("Eras Demi ITC", 10));
            lbl.setTextFill(Color.WHITE);
            Control input = comp.getType().isEnum()
                    ? new ComboBox<>(FXCollections.observableArrayList(Arrays.stream(comp.getType()
                            .getEnumConstants()).map(e -> ((LabeledEnum)e).getLabel()).toList()))
                    : new TextField();
            if (input instanceof ComboBox<?> cb) cb.getSelectionModel().selectFirst();
            inputs.add(input);
            row.getChildren().addAll(lbl, input);
            form.getChildren().add(row);
        }
        Button submit = new Button("Įtraukti");
        submit.getStyleClass().setAll(login.getStyleClass());
        submit.setOnAction(e -> {
            try {
                Object[] args = new Object[comps.length];
                for (int i = 0; i < comps.length; i++) {
                    Control c = inputs.get(i);
                    String raw = (c instanceof ComboBox<?> cb)
                            ? cb.getValue().toString()
                            : ((TextField)c).getText();
                    args[i] = RecordUtil.convert(raw, comps[i].getType());
                }

                Constructor<?> ctor = rcType.getDeclaredConstructor(
                        Arrays.stream(comps)
                                .map(RecordComponent::getType)
                                .toArray(Class[]::new)
                );
                Object row = ctor.newInstance(args);

                table.insert(row);

                refreshTable(table, (Class<Object>)rcType);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        form.getChildren().add(submit);
        queryPane.getChildren().add(form);
    }

    private void displayJoins() {
        queryPane.getChildren().clear();
        String leftKey = tableSelection.getValue();
        if (leftKey == null) return;
        SQLTable<?> leftTbl = map.get(leftKey);
        if (leftTbl == null) return;
        List<Class<?>> joinClasses = RecordUtil.findTableJoin(leftTbl.getRowClass());
        List<String> rightKeys = new ArrayList<>();
        for (var entry : map.entrySet()) {
            if (!entry.getKey().equals(leftKey)
                    && joinClasses.contains(entry.getValue().getRowClass())) {
                rightKeys.add(entry.getKey());
            }
        }
        if (rightKeys.isEmpty()) {
            queryPane.getChildren().add(new Label("Nėra lentelių su bendrais laukais"));
            return;
        }
        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(rightKeys));
        cb.getSelectionModel().selectFirst();
        Button b = new Button("Sujungti");
        b.getStyleClass().setAll(login.getStyleClass());
        b.setOnAction(e -> {
            userTable.getColumns().clear();
            String rightKey = cb.getValue();
            SQLTable<?> rightTbl = map.get(rightKey);
            String sql = QueryBuilder.joinQuery(
                    leftTbl.getRowClass(), leftTbl.getTableName(),
                    rightTbl.getRowClass(), rightTbl.getTableName()
            );
            List<List<?>> raw;
            try {
                raw = qHandler.executeRawSelect(sql);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            showRawInUserTable(raw);
        });
        queryPane.getChildren().add(new HBox(5, cb, b));
    }

    private void showRawInUserTable(List<List<?>> raw){
        userTable.getColumns().clear();
        if(raw.isEmpty())return;
        int c=raw.get(0).size();
        for(int i=0;i<c;i++){
            int idx=i;
            TableColumn<Object,Object> col=new TableColumn<>("C"+(i+1));
            col.setCellValueFactory(cd->new SimpleObjectProperty<>(((List<?>)cd.getValue()).get(idx)));
            userTable.getColumns().add(col);
        }
        userTable.setItems(FXCollections.observableArrayList(raw));
    }

    private void enableRowDeletion() {
        droppy.setDisable(true);

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) ->
                droppy.setDisable(sel == null));

        droppy.setOnAction(_evt -> {
            Object selected = userTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try {
                RecordComponent idComp = selected.getClass().getRecordComponents()[0];
                Object rawId    = idComp.getAccessor().invoke(selected);
                int    id       = ( (Number) rawId ).intValue();

                @SuppressWarnings("unchecked")
                SQLTable<Object> tbl = (SQLTable<Object>) map.get(tableSelection.getValue());
                tbl.deleteById(id);
                refreshTable(tbl, (Class<Object>) selected.getClass());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
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
        SQLTable<?> tbl = map.get(key);
        if (tbl == null) return;

        List<?> rawRows = tbl.selectQuery();
        ObservableList<ObservableList<?>> items = FXCollections.observableArrayList();

        for (Object row : rawRows) {
            if (row instanceof List<?> rowList) {
                items.add(FXCollections.observableArrayList(rowList));
            } else {
                RecordComponent[] comps = row.getClass().getRecordComponents();
                ObservableList<Object> vals = FXCollections.observableArrayList();
                for (var rc : comps) {
                    try { vals.add(rc.getAccessor().invoke(row)); }
                    catch (Exception e) { vals.add(null); }
                }
                items.add(vals);
            }
        }

        userTable.getItems().clear();
        userTable.getItems().addAll(items);
    }

    public void setqHandler(QueryHandler qh) throws SQLException {
        qHandler = qh;

        teismaiSQLTable             = new SQLTable<>(qHandler, Tables.Teismai.class);
        bylaSQLTable                = new SQLTable<>(qHandler, Tables.Byla.class);
        bylosPosedziaiSQLTable      = new SQLTable<>(qHandler, Tables.Bylos_Posedziai.class);
        bylosDalyviaiSQLTable       = new SQLTable<>(qHandler, Tables.Bylos_Dalyviai.class);
        procesodalyvisSQLTable      = new SQLTable<>(qHandler, Tables.ProcesoDalyvis.class);
        BylosEigojeSQLTable         = new SQLTable<>(qHandler, Tables.Bylos_Eigoje.class);
        Bylu_Ilgio_MetrikaSQLTable  = new SQLTable<>(qHandler, Tables.Bylos_Ilgio_Metrika.class);

        map.put("Teismai", teismaiSQLTable);
        map.put("Posedziai", bylosPosedziaiSQLTable);
        map.put("Bylu ilgio metrika", Bylu_Ilgio_MetrikaSQLTable);
        map.put("Byla", bylaSQLTable);
        map.put("Bylos Dalyviai", bylosDalyviaiSQLTable);
        map.put("Proceso Dalyviai", procesodalyvisSQLTable);
        map.put("Bylos Eigoje", BylosEigojeSQLTable);

        tableSelection.getItems().setAll(
                "Teismai", "Posedziai", "Bylu ilgio metrika",
                "Byla", "Bylos Dalyviai", "Proceso Dalyviai",
                "Bylos Eigoje"
        );

        tableActions.getItems().setAll("Peržiūtėti duomenis",
                "Pateikti naujus duomenis", "Sujungti", "Pakeisti duomenis");

        tableSelection.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            if (tableActions.getSelectionModel().getSelectedItem() == null) return;
            try {
                switch (n) {
                    case "Teismai"                      -> refreshTable(teismaiSQLTable                    ,     Tables.Teismai.class);
                    case "Bylu ilgio metrika"           -> refreshTable(Bylu_Ilgio_MetrikaSQLTable         ,     Tables.Bylos_Ilgio_Metrika.class);
                    case "Bylos Eigoje"                 -> refreshTable(BylosEigojeSQLTable                ,     Tables.Bylos_Eigoje.class);
                    case "Byla"                         -> refreshTable(bylaSQLTable                       ,     Tables.Byla.class);
                    case "Posedziai"                    -> refreshTable(bylosPosedziaiSQLTable             ,     Tables.Bylos_Posedziai.class);
                    case "Bylos Dalyviai"               -> refreshTable(bylosDalyviaiSQLTable              ,     Tables.Bylos_Dalyviai.class);
                    case "Proceso Dalyviai"             -> refreshTable(procesodalyvisSQLTable             ,     Tables.ProcesoDalyvis.class);
                }
                switch (tableActions.getValue()) {
                    case "Pateikti naujus duomenis" -> displayInsert();
                    case "Sujungti" -> displayJoins();
                    case "Pakeisti duomenis" -> displaySet();
                    case "Peržiūtėti duomenis" -> queryPane.getChildren().clear();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        tableActions.getSelectionModel().selectFirst();
        tableSelection.getSelectionModel().selectFirst();


        regexKeyword = qHandler.buildPostgresKeywordsRegex();
    }

    public LoginScreenController getLogContr() {
        return logContr;
    }

    public void setCreds(Pair<String, String> creds) {
        this.creds = creds;
        curUser.setText(curUser.getText().replace("viešas", creds.getKey().trim()));
        login.setVisible(false);
    }

    void userTextUpdate(Privilege priv) {
        this.privs = priv;
    }

    public void onClose() {
        System.gc();
        System.exit(0);
    }
}