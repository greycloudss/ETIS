package com.example.etis.Query.Helpers.Types;

public class kontakt_info {
    int telNr;
    String email;

    kontakt_info(int telNr, String email) {
        this.telNr = telNr;
        this.email = email;
    }

    public int getTelNr() {
        return telNr;
    }

    public String getEmail() {
        return email;
    }

    public String returnString() {
        return "("+ telNr + ",'" + email + "')";
    }
}
