package com.example.etis.Query.Helpers.Types;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;

public enum teismas implements LabeledEnum {
    Bendros_kompetencijos("Bendros kompetencijos"),
    Regioninis_apygardos("Regioninis apygardos"),
    Konstitucinis("Konstitucinis"),
    Vyriausiasis_administracinis("Vyriausiasis administracinis");

    String label;

    teismas(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
