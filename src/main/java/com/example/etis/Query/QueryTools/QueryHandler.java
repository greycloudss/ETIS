package com.example.etis.Query.QueryTools;

import javafx.util.Pair;
import org.postgresql.PGConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueryHandler {
    private final String dbUrl = "jdbc:postgresql://localhost:5432/TyleGeraByla";
    static Connection conn;

    public QueryHandler(Pair<String, String> creds) throws SQLException {
        this.conn = DriverManager.getConnection(dbUrl, creds.getKey().trim(), creds.getValue().trim());
        PGConnection pg = conn.unwrap(org.postgresql.PGConnection.class);
        pg.addDataType("kontakt_info", com.example.etis.Query.Helpers.Types.kontakt_info.class);
    }

    public Connection getConnection() throws SQLException {
        return conn;
    }

    public String buildPostgresKeywordsRegex() throws java.sql.SQLException {
        StringBuilder b = new StringBuilder("(?i)\\b(?:");
        try (java.sql.Statement s = conn.createStatement();
             java.sql.ResultSet r = s.executeQuery("SELECT word FROM pg_get_keywords()")) {
            boolean first = true;
            while (r.next()) {
                if (!first) b.append('|');
                b.append(r.getString(1));
                first = false;
            }
        }
        b.append(")\\b");
        return b.toString();
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

    public List<List<?>> executeRawSelect(String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            List<List<?>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.add(rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
}
