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
import java.util.stream.Stream;

public class QueryBuilder<Row> {
    private final String   tableName;

    public QueryBuilder(String tableName) {
        this.tableName   = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public static <L, R> String joinQuery(Class<L> leftRecord, String leftTableName,Class<R> rightRecord, String rightTableName) {
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
                .map(k -> "a." + k + " = b." + k)
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


    public String select() {
        return "SELECT * FROM " + tableName;
    }

    public String insert(Row row) {
        Pair<String, String> data = RecordUtil.iterateRecord(row);

        return "INSERT INTO " + tableName + " " + data.getKey() + " VALUES " + data.getValue();
    }

    public String update(Row row) {
        Pair<String, String> data = RecordUtil.iterateRecord(row);
        String[] cols = data.getKey().split(",");
        String[] vals = data.getValue().split(",");

        String idCol = cols[0];
        String idVal = vals[0];

        StringBuilder b = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        for (int i = 1; i < cols.length; i++) {
            b.append(cols[i]).append(" = ").append(vals[i]);
            if (i < cols.length - 1) b.append(", ");
        }
        b.append(" WHERE ").append(idCol).append(" = ").append(idVal);

        return b.toString();
    }


    public String delete() {
        return "DELETE FROM " + tableName;
    }

    public String drop() {
        return "DROP TABLE " + tableName;
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

