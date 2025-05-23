package com.example.etis.Query;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;
import com.example.etis.Query.QueryTools.QueryBuilder;
import com.example.etis.Query.QueryTools.QueryHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class SQLTable<Row> extends QueryBuilder<Row> {
    private final QueryHandler handler;
    private final Class<Row> RowClass;

    public SQLTable(QueryHandler handler, Class<Row> RowClass) throws SQLException {
        super(RowClass.getSimpleName().toLowerCase(), handler);
        this.RowClass = RowClass;
        this.handler = handler;
    }

    public List<Row> selectQuery() throws SQLException {
        System.gc();
        List<Row> result = new ArrayList<>();
        handler.getConnection().setAutoCommit(false);
        String sql = "SELECT * FROM " + getTableName();

        try (PreparedStatement ps = handler.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

             Function<ResultSet, Row> mapper = recordMapper(RowClass);

             while (rs.next()) result.add(mapper.apply(rs));

        } catch (SQLException e) {
            handler.getConnection().rollback();
        } finally {
            handler.getConnection().commit();
            handler.getConnection().setAutoCommit(true);
        }

        return result;
    }

    public boolean insert(Row row) throws SQLException {
        String cols = String.join(", ",
                Arrays.stream(RowClass.getRecordComponents()).map(RecordComponent::getName).toList());
        String vals = String.join(", ", Arrays.stream(RowClass.getRecordComponents()).map(c -> "?").toList());

        String sql = "INSERT INTO " + getTableName() + " (" + cols + ") VALUES (" + vals + ")";
        try (PreparedStatement ps = handler.getConnection().prepareStatement(sql)) {
            handler.getConnection().setAutoCommit(false);
            for (int i = 0; i < RowClass.getRecordComponents().length; i++) {
                Object v = RowClass.getRecordComponents()[i].getAccessor().invoke(row);
                if (v != null && v.getClass().isEnum()) {
                    if (v instanceof LabeledEnum) {
                        ps.setObject(i + 1, ((LabeledEnum) v).getLabel(), Types.OTHER);
                    } else {
                        ps.setObject(i + 1, ((Enum<?>) v).name(), Types.OTHER);
                    }
                } else {
                    ps.setObject(i + 1, v);
                }
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            handler.getConnection().rollback();
            System.out.println(e.getMessage());
            if (e.getCause() != null) e.getCause().printStackTrace();
        } finally {
            handler.getConnection().commit();
            handler.getConnection().setAutoCommit(true);
        }

        return false;
    }


    public boolean updateColumnById(String column, int idValue, Object newValue) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " + column + " = ? WHERE " + idValue + " = ?";
        try (PreparedStatement ps = handler.getConnection().prepareStatement(sql)) {
            handler.getConnection().setAutoCommit(false);
            ps.setObject(1, newValue);
            ps.setObject(2, idValue);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            handler.getConnection().rollback();
        } finally {
            handler.getConnection().commit();
            handler.getConnection().setAutoCommit(true);
        }

        return false;
    }

    public boolean deleteById(int idValue) throws SQLException {
        String idCol = RowClass.getRecordComponents()[0].getName();
        String sql = "DELETE FROM " + getTableName() + " WHERE " + idCol + " = ?";
        try (PreparedStatement ps = handler.getConnection().prepareStatement(sql)) {
            handler.getConnection().setAutoCommit(false);
            ps.setObject(1, idValue);
            boolean deleted = ps.executeUpdate() > 0;
            handler.getConnection().commit();
            return deleted;
        } catch (SQLException e) {
            handler.getConnection().rollback();
            throw e;
        } finally {
            handler.getConnection().setAutoCommit(true);
        }
    }

    public Class<Row> getRowClass() {
        return RowClass;
    }
}
