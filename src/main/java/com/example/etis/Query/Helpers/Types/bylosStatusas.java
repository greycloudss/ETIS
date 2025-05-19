package com.example.etis.Query.Helpers.Types;

import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;

public enum bylosStatusas implements LabeledEnum {
    Eigoje("Eigoje"),
    Baigta("Baigta"),
    Sustabdyta("Sustabdyta");

    private String label;

    bylosStatusas(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
