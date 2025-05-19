package com.example.etis.Query.Helpers.Types;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;

public enum bylosTipas implements LabeledEnum {
    Civiline("Civilinė"),
    Baudziamoji("Baudžiamoji"),
    Administracinio_nusizengimo("Administracinė nusižengimo"),
    Administracines_teisenos("Administracinė teisenos");

    private String label;

    bylosTipas(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
