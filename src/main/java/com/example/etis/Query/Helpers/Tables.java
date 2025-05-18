package com.example.etis.Query.Helpers;

import com.example.etis.Query.SQLTable;

import java.sql.Date;

public record Tables() {
    /*
    *
    *
    *   Subject for change as this may require type creation instead of primitive use
    *
    *
    */

    public record Teismas(int courtID, String name, String courtType, String Address) { }

    public record BylosDetales(int caseID, String tldr, Date sDate) { }

    public record Byla(int caseID, int caseNum, String status, String courtType, String caseType) { }

    public record BylosPosedis(int hearingID, int caseID, int caseNum, Date sDate, int courtID, String address, String bkStatus) { }

    public record BylosDalyvis(int caseID,  Competency comp,  boolean defendant, int ssn) { }

    public record ProcesoDalyvis(String firstname, String lastname, int ssn, String email, int phoneNumber, boolean ableness, int compNumber) { }

}
