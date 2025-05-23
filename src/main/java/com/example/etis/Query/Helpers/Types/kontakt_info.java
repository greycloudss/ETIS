package com.example.etis.Query.Helpers.Types;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public final class kontakt_info extends PGobject {
    public String tel;
    public String email;
    public kontakt_info() { setType("kontakt_info"); }
    public kontakt_info(String tel, String email) { this(); this.tel = tel; this.email = email; }

    @Override public void setValue(String v) throws SQLException {
        String s = v.substring(1, v.length() - 1);
        String[] parts = s.split(",", 2);
        tel   = parts[0].replace("\"", "");
        email = parts[1].replace("\"", "");
    }
    @Override public String getValue() {
        return "(" + tel + "," + email + ")";
    }
}
