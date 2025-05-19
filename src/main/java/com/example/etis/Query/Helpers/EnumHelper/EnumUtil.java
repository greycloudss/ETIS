package com.example.etis.Query.Helpers.EnumHelper;

public final class EnumUtil {
    public static <E extends Enum<E> & LabeledEnum> E fromLabel(Class<E> type, String label) {
        for (E e : type.getEnumConstants()) {
            if (e.getLabel().equals(label)) return e;
        }
        throw new IllegalArgumentException("No enum constant with label " + label);
    }
}
