//package kumaranai.io;
//
//package kumaranai.data;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Component;
//
//import kumaranai.model.DataRecord;
//import kumaranai.model.WriteRecord;
//
///**
// * Bridges DataLoader's DataRecord and DataWriter's WriteRecord.
// * All field mapping logic lives here — DataLoader and DataWriter
// * never need to know about each other.
// */
//@Component
//public class RecordMapper {
//
//    /**
//     * Converts a single DataRecord → WriteRecord.
//     * Add/remove fields here without touching DataRecord or DataWriter.
//     */
//    public WriteRecord toWriteRecord(DataRecord record) {
//        Map<String, String> fields = new LinkedHashMap<>();
//
//        // ✅ Map only the fields you want written to CSV/XLSX
//        fields.put("Name",       safeValue(record.getName()));
//        fields.put("Age",        safeValue(record.getAge()));
//        fields.put("Salary",     safeValue(record.getSalary()));
//        fields.put("Gender",     safeValue(record.getGender()));
//        fields.put("Department", safeValue(record.getDepartment()));
//        fields.put("Score",      safeValue(record.getScore()));
//
//        return new WriteRecord(fields);
//    }
//
//    /**
//     * Converts a full list of DataRecords → WriteRecords.
//     */
//    public List<WriteRecord> toWriteRecords(List<DataRecord> records) {
//        return records.stream()
//                      .map(this::toWriteRecord)
//                      .collect(Collectors.toList());
//    }
//
//    // ── Helper ───────────────────────────────────────────────────
//    private String safeValue(String value) {
//        return value != null ? value.trim() : "";
//    }
//}
