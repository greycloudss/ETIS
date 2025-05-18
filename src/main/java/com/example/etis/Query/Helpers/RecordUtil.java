package com.example.etis.Query.Helpers;

import javafx.util.Pair;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
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
}
