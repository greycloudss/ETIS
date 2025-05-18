package com.example.etis.Query.QueryTools;

import javafx.util.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueryHandler {
    private final String dbUrl = "jdbc:postgresql://localhost:5432/TyleGeraByla";
    Connection conn;

    public QueryHandler(Pair<String, String> creds) throws SQLException {
        this.conn = DriverManager.getConnection(dbUrl, creds.getKey().trim(), creds.getValue().trim());
    }

    public Connection getConnection() throws SQLException {
        return conn;
    }

    public int executeQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        int rowsAffected = stmt.executeUpdate(query);
        stmt.close();
        return rowsAffected;
    }

    public <Row> List<Row> executeSelect(String query,  Function<ResultSet, Row> rowMapper) throws SQLException {
        List<Row> results = new ArrayList<>();

        try (Statement stmt = conn.createStatement(); ResultSet rs   = stmt.executeQuery(query)) {

            while (rs.next())
                results.add(rowMapper.apply(rs));

        }
        return results;
    }
}
