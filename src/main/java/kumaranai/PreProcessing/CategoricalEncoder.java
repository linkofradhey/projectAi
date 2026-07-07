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

// public enum EncodingType {
//     LABEL,    // Assigns integer labels: Male=0, Female=1
//     ONE_HOT   // Creates binary columns: Gender_Male=1, Gender_Female=0
// }
//
// private final EncodingType encodingType;

 private final Map<String, Map<String, Integer>> encodingRegistry = new LinkedHashMap<>();

 //Constructors
// public CategoricalEncoder() {
//     this.encodingType = EncodingType.LABEL;
// }
//
// public CategoricalEncoder(EncodingType encodingType) {
//     this.encodingType = encodingType;
// }


 public List<DataRecord> encode(List<DataRecord> records, List<String> categoricalColumns,String encodingType) {
	  if (records == null || records.isEmpty()) {
	         System.out.println("[CategoricalEncoder] No records to process.");
	         return records;
	     }

	     if (categoricalColumns == null || categoricalColumns.isEmpty()) {
	         System.out.println("[CategoricalEncoder] No categorical columns specified.");
	         return records;
	     }
	     
	     switch (encodingType) {
	         case "label":
	             return applyLabelEncoding(records, categoricalColumns);
	         case "onehot":
	             return applyOneHotEncoding(records, categoricalColumns);
	         default:
	             return records;
	     }
 }

 

 public void encodePerformanceScore(List<DataRecord> records) {
     for (DataRecord record : records) {
         Object raw = record.getField("performance_score");
         if (raw != null) {
             double score = Double.parseDouble(raw.toString());
             boolean encoded = score >= 70.0;        // true if ≥ 70, false if < 70
             record.setField("performance_score", String.valueOf(encoded)); // "true" or "false"       
             }
     }
 }
 public void encodeAgeCategory(List<DataRecord> records) {
     for (DataRecord record : records) {
         Object raw = record.getField("age");
         if (raw != null) {
             int age = (int) Double.parseDouble(raw.toString());
             int label;

             if (age < 25) {
                 label = 0;       // Young
             } else if (age < 35) {
                 label = 1;       // Early Career
             } else if (age < 45) {
                 label = 2;       // Mid Career
             } else {
                 label = 3;       // Senior
             }

             record.setField("age", String.valueOf(label)); // "0", "1", "2", "3"
         }
     }
 }
 private List<DataRecord> applyLabelEncoding(List<DataRecord> records, List<String> categoricalColumns) {

	    for (String column : categoricalColumns) {

	        // ✅ Special case: age → bucket label encoding
	        if (column.equals("age")) {
	            for (DataRecord record : records) {
	                Object raw = record.getField(column);
	                if (raw != null) {
	                    int age = (int) Double.parseDouble(raw.toString());
	                    int label;

	                    if (age < 25) {
	                        label = 0;   // Young
	                    } else if (age < 35) {
	                        label = 1;   // Early Career
	                    } else if (age < 45) {
	                        label = 2;   // Mid Career
	                    } else {
	                        label = 3;   // Senior
	                    }

	                    record.setField(column, String.valueOf(label));
	                }
	            }
	            System.out.println("[CategoricalEncoder] LABEL | Column 'age' → bucket labels {<25=0, 25-34=1, 35-44=2, ≥45=3}");
	            continue; // ← skip generic label logic for this column
	        }

	        // ── Generic label encoding logic (unchanged) ───────────
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

	        // ✅ Special case: performance_score → boolean true/false
	        if (column.equals("performance_score")) {
	            for (DataRecord record : records) {
	                Object raw = record.getField(column);
	                if (raw != null) {
	                    double score = Double.parseDouble(raw.toString());
	                    boolean encoded = score >= 70.0;   // true if ≥ 70.0, false if < 70.0
	                    record.setField(column, String.valueOf(encoded));
	                }
	            }
	            System.out.println("[CategoricalEncoder] ONE_HOT | Column 'performance_score' → threshold 70.0 (true/false)");
	            continue; // ← skip generic one-hot logic for this column
	        }

	        // ── Generic one-hot logic (unchanged) ──────────────────
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