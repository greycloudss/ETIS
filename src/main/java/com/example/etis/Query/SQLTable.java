package com.example.etis.Query;

import com.example.etis.Query.QueryTools.QueryBuilder;
import com.example.etis.Query.QueryTools.QueryHandler;

import java.lang.reflect.RecordComponent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public boolean deleteById(String idColumn, Object idValue) throws SQLException {
        String sql = String.format(
                "DELETE FROM %s WHERE %s = ?",
                getTableName(), idColumn
        );
        try (var ps = handler.getConnection().prepareStatement(sql)) {
            ps.setObject(1, idValue);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Row> selectQuery() throws SQLException {
        rows.clear();

        rows = handler.executeSelect(super.select(), QueryBuilder.recordMapper(RowClass));

        return rows;
    }

    public boolean insertQuery(Row row) throws SQLException {
        var comps = row.getClass().getRecordComponents();
        String cols = Arrays.stream(comps)
                .map(RecordComponent::getName)
                .collect(Collectors.joining(", "));
        String vals = Arrays.stream(comps).map(c -> "?")
                .collect(Collectors.joining(", "));
        String sql = "INSERT INTO "+getTableName()+" ("+cols+") VALUES ("+vals+")";
        try (var ps = handler.getConnection().prepareStatement(sql)) {
            for (int i=0;i<comps.length;i++) {
                Object v = comps[i].getAccessor().invoke(row);
                if (v instanceof com.example.etis.Query.Helpers.EnumHelper.LabeledEnum le)
                    ps.setObject(i+1, le.getLabel(), Types.OTHER);
                else
                    ps.setObject(i+1, v);
            }
            return ps.executeUpdate()>0;
        } catch (ReflectiveOperationException e) {
            throw new SQLException(e);
        }
    }

    public boolean setQuery(Row row) throws SQLException {
        boolean success = handler.executeQuery(super.update(row)) > 0;

        selectQuery();

        return success;
    }

    public void updateColumnById(String column, Object idValue, Object newValue) throws SQLException {
        String idCol = RowClass.getRecordComponents()[0].getName();
        String idLiteral = formatLiteral(idValue);
        String valLiteral  = formatLiteral(newValue);
        String sql = "UPDATE " + getTableName() +
                " SET " + column + " = " + valLiteral +
                " WHERE " + idCol + " = " + idLiteral;
        handler.executeQuery(sql);
    }

    private String formatLiteral(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        return "'" + v.toString().replace("'", "''") + "'";
    }


    public boolean dropQuery() throws SQLException {
        return handler.executeQuery(super.drop()) > 0;
    }

    public boolean deleteQuery() throws SQLException {

        return false;
    }

    public int getRowCount() {
        return rowCount;
    }

    public Class<Row> getRowClass() {
        return RowClass;
    }
}
