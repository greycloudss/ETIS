package com.example.etis.Query.QueryTools;

import com.example.etis.Query.Helpers.RecordUtil;
import javafx.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryBuilder<Row> {
    private final String   tableName;
    private final QueryHandler handler;

    public QueryBuilder(String tableName, QueryHandler handler) {
        this.tableName   = tableName;
        this.handler = handler;
    }

    public String getTableName() {
        return tableName;
    }

    public static <L, R> String joinQuery(
            Class<L> leftRecord, String leftTableName,
            Class<R> rightRecord, String rightTableName
    ) {
        RecordComponent[] lc = leftRecord.getRecordComponents();
        RecordComponent[] rc = rightRecord.getRecordComponents();

        Set<String> leftFields = Arrays.stream(lc)
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
        Set<String> rightFields = Arrays.stream(rc)
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());

        Set<String> joinKeys = new LinkedHashSet<>(leftFields);
        joinKeys.retainAll(rightFields);
        if (joinKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "No common fields to join on between "
                            + leftRecord.getSimpleName() + " and "
                            + rightRecord.getSimpleName()
            );
        }

        String on = joinKeys.stream()
                .map(k -> "a." + k + "::text = b." + k + "::text")
                .collect(Collectors.joining(" AND "));

        String selectA = Arrays.stream(lc)
                .map(c -> "a." + c.getName())
                .collect(Collectors.joining(", "));
        String selectB = Arrays.stream(rc)
                .map(c -> "b." + c.getName() + " AS b_" + c.getName())
                .collect(Collectors.joining(", "));
        String selectList = selectA + ", " + selectB;

        return String.format(
                "SELECT %s FROM %s a JOIN %s b ON %s",
                selectList, leftTableName, rightTableName, on
        );
    }

    public static <R> Function<ResultSet, R> recordMapper(Class<R> recordClass) {
        RecordComponent[] comps = recordClass.getRecordComponents();
        Class<?>[] types = Arrays.stream(comps).map(RecordComponent::getType).toArray(Class<?>[]::new);
        Constructor<R> ctor;
        try {
            ctor = recordClass.getDeclaredConstructor(types);
            ctor.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return rs -> {
            try {
                Object[] args = new Object[comps.length];
                for (int i = 0; i < comps.length; i++) {
                    String name = comps[i].getName();
                    Class<?> t  = comps[i].getType();
                    args[i] = switch (t.getName()) {
                        case "int", "java.lang.Integer"   -> rs.getInt(name);
                        case "long", "java.lang.Long"     -> rs.getLong(name);
                        case "double", "java.lang.Double" -> rs.getDouble(name);
                        case "boolean","java.lang.Boolean"-> rs.getBoolean(name);
                        case "java.lang.String"           -> rs.getString(name);
                        default -> {
                            String raw = rs.getString(name);
                            if (raw == null)               yield null;
                            if (com.example.etis.Query.Helpers.EnumHelper.LabeledEnum.class.isAssignableFrom(t))
                                yield com.example.etis.Query.Helpers.EnumHelper.EnumUtil.fromLabel((Class) t, raw.trim());
                            if (t.isEnum())
                                yield Enum.valueOf((Class<? extends Enum>) t,
                                        raw.trim().toUpperCase().replace(' ', '_').replace('-', '_'));
                            yield rs.getObject(name, t);
                        }
                    };

                }
                return ctor.newInstance(args);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}

