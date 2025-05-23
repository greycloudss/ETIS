package com.example.etis.Query.QueryTools;

import javafx.util.Pair;
import org.postgresql.PGConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryHandler {
    private final String dbUrl = "jdbc:postgresql://localhost:5432/TyleGeraByla";
    static Connection conn;

    public QueryHandler(Pair<String, String> creds) throws SQLException {
        this.conn = DriverManager.getConnection(dbUrl, creds.getKey().trim(), creds.getValue().trim());
        PGConnection pg = conn.unwrap(org.postgresql.PGConnection.class);
        pg.addDataType("kontakt_info", com.example.etis.Query.Helpers.Types.kontakt_info.class);
    }

    public Connection getConnection() {
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

    public List<List<?>> executeRawSelect(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<List<?>> rows = new ArrayList<>();
                int cols = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    List<Object> row = new ArrayList<>(cols);
                    for (int c = 1; c <= cols; c++) row.add(rs.getObject(c));
                    rows.add(row);
                }
                return rows;
            }
        } catch (SQLException ex) {
            conn.rollback();
        } finally {
            conn.setAutoCommit(true);
        }
        return null;
    }

    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }
}
