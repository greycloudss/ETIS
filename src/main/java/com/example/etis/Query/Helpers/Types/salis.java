package com.example.etis.Query.Helpers.Types;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;

public enum salis implements LabeledEnum {
    Ieskovas_Pareiskejas("Ieškovas/Pareiškėjas"),
    Atsakovas("Atsakovas");

    String label;

    salis(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
