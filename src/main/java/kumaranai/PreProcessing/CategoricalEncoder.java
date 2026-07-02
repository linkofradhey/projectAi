package kumaranai.PreProcessing;

//src/main/java/com/datapreprocessing/encoder/CategoricalEncoder.java

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;
@Component
public class CategoricalEncoder {

 // ─── Encoding Strategy Enum ──────────────────────────────────
 public enum EncodingType {
     LABEL,    // Assigns integer labels: Male=0, Female=1
     ONE_HOT   // Creates binary columns: Gender_Male=1, Gender_Female=0
 }

 private final EncodingType encodingType;

 // Stores the label maps for each column (useful for inverse transform / reporting)
 private final Map<String, Map<String, Integer>> encodingRegistry = new LinkedHashMap<>();

 // ─── Constructors ────────────────────────────────────────────

 /** Default: Label Encoding */
 public CategoricalEncoder() {
     this.encodingType = EncodingType.LABEL;
 }

 /** Custom encoding type */
 public CategoricalEncoder(EncodingType encodingType) {
     this.encodingType = encodingType;
 }

 // ─── Main Entry Point ────────────────────────────────────────

 /**
  * Encodes categorical columns based on the configured encoding type.
  *
  * @param records            List of DataRecord objects
  * @param categoricalColumns List of column names to encode
  * @return Encoded list of DataRecord objects
  */
 public List<DataRecord> encode(List<DataRecord> records, List<String> categoricalColumns) {

     if (records == null || records.isEmpty()) {
         System.out.println("[CategoricalEncoder] No records to process.");
         return records;
     }

     if (categoricalColumns == null || categoricalColumns.isEmpty()) {
         System.out.println("[CategoricalEncoder] No categorical columns specified.");
         return records;
     }

     switch (encodingType) {
         case LABEL:
             return applyLabelEncoding(records, categoricalColumns);
         case ONE_HOT:
             return applyOneHotEncoding(records, categoricalColumns);
         default:
             return records;
     }
 }

 // ─── Label Encoding ──────────────────────────────────────────

 /**
  * Label Encoding: assigns a unique integer to each category value.
  * Example: ["Male", "Female", "Male"] → [0, 1, 0]
  */
 private List<DataRecord> applyLabelEncoding(List<DataRecord> records, List<String> categoricalColumns) {

     for (String column : categoricalColumns) {
         Map<String, Integer> labelMap = buildLabelMap(records, column);
         encodingRegistry.put(column, labelMap);

         for (DataRecord record : records) {
             String value = record.getField(column);
             if (value != null && labelMap.containsKey(value.trim())) {
                 record.setField(column, String.valueOf(labelMap.get(value.trim())));
             }
         }

         System.out.println("[CategoricalEncoder] LABEL | Column '" + column + "' → " + labelMap);
     }

     return records;
 }

 // ─── One-Hot Encoding ────────────────────────────────────────

 /**
  * One-Hot Encoding: creates a new binary column for each unique category value.
  * Example: Gender column with [Male, Female] →
  *          Gender_Male=1, Gender_Female=0  (for a Male record)
  *          Gender_Male=0, Gender_Female=1  (for a Female record)
  *
  * The original column is removed after encoding.
  */
 private List<DataRecord> applyOneHotEncoding(List<DataRecord> records, List<String> categoricalColumns) {

     for (String column : categoricalColumns) {

         // Collect all unique values for this column
         List<String> uniqueValues = records.stream()
                 .map(r -> r.getField(column))
                 .filter(v -> v != null && !v.trim().isEmpty())
                 .map(String::trim)
                 .distinct()
                 .sorted()
                 .collect(Collectors.toList());

         System.out.println("[CategoricalEncoder] ONE_HOT | Column '" + column
                 + "' → Categories: " + uniqueValues);

         // For each record, add binary columns and remove the original
         for (DataRecord record : records) {
             String value = record.getField(column);

             for (String category : uniqueValues) {
                 String newColumnName = column + "_" + category;
                 String binaryValue   = (value != null && value.trim().equals(category)) ? "1" : "0";
                 record.setField(newColumnName, binaryValue);
             }

             // Remove the original categorical column
             record.removeField(column);
         }

         System.out.println("[CategoricalEncoder] ONE_HOT | Column '" + column
                 + "' replaced with: "
                 + uniqueValues.stream().map(v -> column + "_" + v).collect(Collectors.toList()));
     }

     return records;
 }

 // ─── Utility Helpers ─────────────────────────────────────────

 /**
  * Builds a label-to-integer mapping for a given column.
  * Unique values are sorted alphabetically for consistency.
  */
 private Map<String, Integer> buildLabelMap(List<DataRecord> records, String column) {
     Map<String, Integer> labelMap = new LinkedHashMap<>();

     List<String> uniqueValues = records.stream()
             .map(r -> r.getField(column))
             .filter(v -> v != null && !v.trim().isEmpty())
             .map(String::trim)
             .distinct()
             .sorted()
             .collect(Collectors.toList());

     for (int i = 0; i < uniqueValues.size(); i++) {
         labelMap.put(uniqueValues.get(i), i);
     }

     return labelMap;
 }

 // ─── Reporting ───────────────────────────────────────────────

 /**
  * Returns the encoding registry (label maps for all encoded columns).
  * Useful for inverse transformation or audit logging.
  */
 public Map<String, Map<String, Integer>> getEncodingRegistry() {
     return Collections.unmodifiableMap(encodingRegistry);
 }

 /**
  * Decodes a label-encoded value back to its original category.
  *
  * @param column       Column name
  * @param encodedValue Encoded integer value as String
  * @return Original category string, or "unknown" if not found
  */
 public String decode(String column, String encodedValue) {
     Map<String, Integer> labelMap = encodingRegistry.get(column);
     if (labelMap == null) return "unknown";

     return labelMap.entrySet().stream()
             .filter(e -> String.valueOf(e.getValue()).equals(encodedValue))
             .map(Map.Entry::getKey)
             .findFirst()
             .orElse("unknown");
 }
}