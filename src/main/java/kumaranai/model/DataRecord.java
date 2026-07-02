package kumaranai.model;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataRecord {
	
	private Map<String , String> fields  = new HashMap<>();
	

    public String getField(String columnName) {
        return fields.get(columnName);
    }

    public void setField(String columnName, String value) {
        fields.put(columnName, value);
    }

    public boolean isMissing(String columnName) {
        String value = fields.get(columnName);
        return value == null || value.trim().isEmpty();
    }

    public boolean hasField(String columnName) {
        return fields.containsKey(columnName);
    }

    public void removeField(String columnName) {
        fields.remove(columnName);
    }
}
