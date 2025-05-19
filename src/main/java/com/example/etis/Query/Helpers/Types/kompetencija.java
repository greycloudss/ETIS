package com.example.etis.Query.Helpers.Types;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;

public enum kompetencija implements LabeledEnum {
    ADVOKATAS("Advokatas"),
    TEISEJAS("Teisejas"),
    PROKURORAS("Prokuroras"),
    TEISEJO_SEKRETORĖ("Teisejo Sekretore");

    private final String label;

    kompetencija(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
