package com.example.etis.Query.Helpers;

import com.example.etis.Query.Helpers.Types.*;

import java.sql.Date;
import java.time.OffsetDateTime;

public record Tables() {
    /*
    *
    *
    *   Subject for change as this may require type creation instead of primitive use
    *
    *
    */

    public record Teismai(int teismasID, String pavadinimas, teismas tipas, String vieta) { }

    public record BylosDetales(int bylosID, String bylosapibendr, Date bylospradziosdata) { }

    public record Byla(int bylosID, String bylosNum, bylosStatusas status, teismas teismoTipas, bylosTipas bylosTipas) { }

    public record Bylos_Posedziai(int posedzioID, int bylosID, bylosTipas tipas, OffsetDateTime data, int teismasID, String vieta, bkStatusas status) { }

    public record Bylos_Dalyviai(int bylosID, kompetencija kompetencija, salis salis, int ak) { }

    public record ProcesoDalyvis(String vardas, String pavarde, int ak, String email, int telnr, boolean neveiksnumas, int pazymejimonr, kontakt_info kontakt_info) { }

    public record Ateinantys_Posedziai(int bylosID, String bylosNum, String pavadinimas, OffsetDateTime data, String vieta) { }

    public record Bylos_Eigoje(String bylosNum, bylosStatusas status, teismas teismoTipas) { }

    public record Bylos_Ilgio_Metrika(int bylosID, String bylosNum, Date pirmas_posedis, Date paskutinis_posedis, int dienu_metrika) { }
}
