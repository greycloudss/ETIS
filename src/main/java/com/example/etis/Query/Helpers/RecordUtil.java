package com.example.etis.Query.Helpers;

import com.example.etis.Query.Helpers.EnumHelper.EnumUtil;
import com.example.etis.Query.Helpers.EnumHelper.LabeledEnum;
import javafx.util.Pair;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;

public final class RecordUtil {
    public static <R> Pair<String, String> iterateRecord(R row) {
        Class<?> rc = row.getClass();
        if (!rc.isRecord()) {
            throw new IllegalArgumentException("Not a record");
        }

        RecordComponent[] comps = rc.getRecordComponents();

        String fieldString = Arrays.stream(comps)
                .map(RecordComponent::getName)
                .collect(Collectors.joining(", ", "(", ")"));

        String varString = Arrays.stream(comps)
                .map(c -> {
                    try {
                        Object v = c.getAccessor().invoke(row);
                        if (v instanceof String) {
                            return "'" + v.toString().replace("'", "''") + "'";
                        } else if (v instanceof Character) {
                            return "'" + v + "'";
                        } else {
                            return String.valueOf(v);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining(", ", "(", ")"));

        return new Pair<>(fieldString, varString);
    }

    public static List<Class<?>> findTableJoin(Class<?> row) {

        if (!row.isRecord()) return null;

        Set<String> rowCols = Arrays.stream(row.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());

        Class<?>[] candidates = {
                Tables.Ateinantys_Posedziai.class,
                Tables.Bylos_Eigoje.class,
                Tables.Bylos_Ilgio_Metrika.class,
                Tables.Teismai.class,
                Tables.BylosDetales.class,
                Tables.Byla.class,
                Tables.Bylos_Posedziai.class,
                Tables.Bylos_Dalyviai.class,
                Tables.ProcesoDalyvis.class
        };

        List<Class<?>> joinables = new ArrayList<>();

        for (Class<?> c : candidates) {
            if (c == row || !c.isRecord()) continue;
            for (RecordComponent rc : c.getRecordComponents())
                if (rowCols.contains(rc.getName())) {
                    joinables.add(c);
                    break;
                }
        }
        return joinables;
    }

    public static Object convert(String raw, Class<?> t) {
        if (raw == null || raw.isEmpty()) return null;
        raw = raw.trim().replaceAll("_", " ");
        if (t == String.class) return raw;
        if (t == int.class || t == Integer.class)    return Integer.valueOf(raw);
        if (t == long.class|| t == Long.class)       return Long.valueOf(raw);
        if (t == double.class|| t == Double.class)   return Double.valueOf(raw);
        if (t == boolean.class|| t == Boolean.class) return Boolean.valueOf(raw);
        if (LabeledEnum.class.isAssignableFrom(t)) return EnumUtil.fromLabel((Class) t, raw);
        if (t.isEnum()) return Enum.valueOf((Class<Enum>)t, raw);

        throw new IllegalArgumentException("Unsupported type " + t);
    }
}
