package com.example.etis.Query;

import com.example.etis.Query.QueryTools.QueryBuilder;
import com.example.etis.Query.QueryTools.QueryHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLTable<Row> extends QueryBuilder<Row> {
    private List<Row> rows = new ArrayList<>();
    private final QueryHandler handler;
    private final Class<Row> RowClass;
    private final int rowCount;

    public SQLTable(QueryHandler handler, Class<Row> RowClass) throws SQLException {
        super(RowClass.getSimpleName().toLowerCase());
        this.RowClass = RowClass;
        this.handler = handler;
        rowCount = RowClass.getDeclaredFields().length;
    }

    public List<Row> getRows() {
        return rows;
    }

    public List<Row> selectQuery() throws SQLException {
        rows.clear();

        rows = handler.executeSelect(super.select(), QueryBuilder.recordMapper(RowClass));

        return rows;
    }

    public boolean insertQuery(Row row) throws SQLException {
        boolean success = handler.executeQuery(super.select()) > 0;
        selectQuery();
        return success;
    }

    public boolean setQuery(Row row) throws SQLException {
        boolean success = handler.executeQuery(super.update(row)) > 0;

        selectQuery();

        return success;
    }

    public boolean deleteQuery() {

        return false;
    }

    public int getRowCount() {
        return rowCount;
    }
}
