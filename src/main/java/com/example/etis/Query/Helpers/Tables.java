package com.example.etis.Query.Helpers;

import com.example.etis.Query.Helpers.Types.kompetencija;

import java.sql.Date;

public record Tables() {
    /*
    *
    *
    *   Subject for change as this may require type creation instead of primitive use
    *
    *
    */

    public record Teismai(int teismasID, String pavadinimas, String tipas, String vieta) { }

    public record BylosDetales(int bylosID, String bylosapibendr, Date bylospradziosdata) { }

    public record Byla(int bylosID, int bylosNum, String status, String teismoTipas, String bylosTipas) { }

    public record BylosPosedis(int posedzioID, int bylosID, int bylosNum, Date sDate, int courtID, String address, String bkStatus) { }

    public record BylosDalyvis(int bylosID, kompetencija kompetencija, boolean salis, int ak) { }

    public record ProcesoDalyvis(String vardas, String pavarde, int ak, String email, int telnr, boolean neveiksnumas, int pazymejimonr) { }

}
