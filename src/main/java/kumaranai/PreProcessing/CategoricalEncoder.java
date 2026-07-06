package kumaranai.PreProcessing;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;
@Component
public class CategoricalEncoder {

 public enum EncodingType {
     LABEL,    // Assigns integer labels: Male=0, Female=1
     ONE_HOT   // Creates binary columns: Gender_Male=1, Gender_Female=0
 }

 private final EncodingType encodingType;

 private final Map<String, Map<String, Integer>> encodingRegistry = new LinkedHashMap<>();

 //Constructors
 public CategoricalEncoder() {
     this.encodingType = EncodingType.LABEL;
 }

 public CategoricalEncoder(EncodingType encodingType) {
     this.encodingType = encodingType;
 }


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


 private List<DataRecord> applyOneHotEncoding(List<DataRecord> records, List<String> categoricalColumns) {

     for (String column : categoricalColumns) {

         List<String> uniqueValues = records.stream()
                 .map(r -> r.getField(column))
                 .filter(v -> v != null && !v.trim().isEmpty())
                 .map(String::trim)
                 .distinct()
                 .sorted()
                 .collect(Collectors.toList());

         System.out.println("[CategoricalEncoder] ONE_HOT | Column '" + column
                 + "' → Categories: " + uniqueValues);

         for (DataRecord record : records) {
             String value = record.getField(column);

             for (String category : uniqueValues) {
                 String newColumnName = column + "_" + category;
                 String binaryValue   = (value != null && value.trim().equals(category)) ? "1" : "0";
                 record.setField(newColumnName, binaryValue);
             }

             record.removeField(column);
         }

         System.out.println("[CategoricalEncoder] ONE_HOT | Column '" + column
                 + "' replaced with: "
                 + uniqueValues.stream().map(v -> column + "_" + v).collect(Collectors.toList()));
     }

     return records;
 }



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

 
 public Map<String, Map<String, Integer>> getEncodingRegistry() {
     return Collections.unmodifiableMap(encodingRegistry);
 }


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