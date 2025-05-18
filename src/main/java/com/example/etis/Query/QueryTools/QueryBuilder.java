package com.example.etis.Query.QueryTools;

import com.example.etis.Query.Helpers.RecordUtil;
import javafx.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.function.Function;

public class QueryBuilder<Row> {
    private final String   tableName;

    public QueryBuilder(String tableName) {
        this.tableName   = tableName;
    }

    public String getTableName() {
        return tableName;
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
        String[] keyArr =  data.getKey().split(",");
        String[] valArr =  data.getValue().split(",");
        StringBuilder builder = new StringBuilder("UPDATE " + tableName + " SET ");

        for (int i = 0; i < keyArr.length; ++i)
            builder.append(keyArr[i]).append(" = ").append(valArr[i]).append(", ");

        return builder.toString();
    }

    public String delete() {
        return "DELETE FROM " + tableName;
    }

    public static <R> Function<ResultSet, R> recordMapper(Class<R> recordClass) {
        RecordComponent[] comps = recordClass.getRecordComponents();
        Class<?>[] types     = Arrays.stream(comps)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        Constructor<R> ctor;
        try {
            @SuppressWarnings("unchecked")
            Constructor<R> c = (Constructor<R>) recordClass.getDeclaredConstructor(types);
            ctor = c;
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
                        case "int", "java.lang.Integer" -> rs.getInt(name);
                        case "long", "java.lang.Long" -> rs.getLong(name);
                        case "double", "java.lang.Double" -> rs.getDouble(name);
                        case "boolean", "java.lang.Boolean" -> rs.getBoolean(name);
                        case "java.lang.String" -> rs.getString(name);
                        default -> rs.getObject(name, t);
                    };

                }
                return ctor.newInstance(args);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}

