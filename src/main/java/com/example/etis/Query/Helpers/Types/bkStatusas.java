package com.example.etis.Query.Helpers.Types;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;

public enum bkStatusas implements LabeledEnum  {
    Atiduotas("Atiduotas teismui"),
    TEISEJAS("Nutrauktas");

    private final String label;

    bkStatusas(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
