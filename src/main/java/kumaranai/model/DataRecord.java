package kumaranai.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataRecord {

    private final Map<String, String> fields = new LinkedHashMap<>();

    public String getField(String columnName) {
        return fields.get(columnName);
    }

    public void setField(String columnName, String value) {
        fields.put(columnName, value);
    }

    public void setField(String columnName, Object value) {
        fields.put(columnName, value == null ? null : String.valueOf(value));
    }

    public boolean hasField(String columnName) {
        return fields.containsKey(columnName);
    }

    public void removeField(String columnName) {
        fields.remove(columnName);
    }

    public Map<String, String> getFields() {
        return new LinkedHashMap<>(fields);
    }

    @Override
    public String toString() {
        return "DataRecord" + fields;
    }
}