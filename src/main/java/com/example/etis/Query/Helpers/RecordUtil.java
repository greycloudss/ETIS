package com.example.etis.Query.Helpers;

import javafx.util.Pair;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

        Set<String> rowCols = Arrays.stream(row.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());

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
}
