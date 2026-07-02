//package kumaranai.model;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * Output-specific model used exclusively by DataWriter.
// * Decoupled from DataRecord — safe to modify for CSV/XLSX needs
// * without affecting DataLoader or any other pipeline component.
// */
//public class WriteRecord {
//
//    // ✅ Stores column name → value in insertion order (LinkedHashMap)
//    private final Map<String, String> fields;
//
//    // ── Constructor ──────────────────────────────────────────────
//    public WriteRecord(Map<String, String> fields) {
//        this.fields = new LinkedHashMap<>(fields); // preserve column order
//    }
//
//    // ── Getters ──────────────────────────────────────────────────
//
//    /** Returns all fields as an ordered map (column → value) */
//    public Map<String, String> getFields() {
//        return fields;
//    }
//
//    /** Returns column headers in order */
//    public String[] getHeaders() {
//        return fields.keySet().toArray(new String[0]);
//    }
//
//    /** Returns values in column order */
//    public String[] getValues() {
//        return fields.values().toArray(new String[0]);
//    }
//
//    /** Safe getter — returns "" if column not found */
//    public String get(String columnName) {
//        return fields.getOrDefault(columnName, "");
//    }
//
//    @Override
//    public String toString() {
//        return "WriteRecord" + fields;
//    }
//}