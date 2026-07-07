package kumaranai.PreProcessing;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;
@Component
public class DuplicateRemover {

 public enum KeepStrategy {
     FIRST,  
     LAST    
 }

 private final KeepStrategy keepStrategy;
 private final List<String> subsetColumns; 


 public DuplicateRemover() {
     this.keepStrategy  = KeepStrategy.FIRST;
     this.subsetColumns = null;
 }

 public DuplicateRemover(KeepStrategy keepStrategy) {
     this.keepStrategy  = keepStrategy;
     this.subsetColumns = null;
 }

 public DuplicateRemover(KeepStrategy keepStrategy, List<String> subsetColumns) {
     this.keepStrategy  = keepStrategy;
     this.subsetColumns = subsetColumns;
 }


 public List<DataRecord> remove(List<DataRecord> records) {

     if (records == null || records.isEmpty()) {
         System.out.println("[DuplicateRemover] No records to process.");
         return records;
     }

     // If LAST strategy → reverse, deduplicate, reverse back
     List<DataRecord> workingList = (keepStrategy == KeepStrategy.LAST)
             ? reverseList(records)
             : new ArrayList<>(records);

     Set<String>       seen   = new LinkedHashSet<>();
     List<DataRecord>  unique = new ArrayList<>();

     for (DataRecord record : workingList) {
         String key = buildKey(record);
         if (seen.add(key)) {
             unique.add(record);
         }
     }

     if (keepStrategy == KeepStrategy.LAST) {
         Collections.reverse(unique);
     }

     int removed = records.size() - unique.size();
     System.out.println("[DuplicateRemover] Strategy   : " + keepStrategy);
     System.out.println("[DuplicateRemover] Subset cols : " + (subsetColumns != null ? subsetColumns : "ALL"));
     System.out.println("[DuplicateRemover] Removed     : " + removed + " duplicate(s)");
     System.out.println("[DuplicateRemover] Remaining   : " + unique.size() + " record(s)");

     return unique;
 }


 
 private String buildKey(DataRecord record) {
     if (subsetColumns == null || subsetColumns.isEmpty()) {
         return record.getFields().toString();
     }

     StringBuilder keyBuilder = new StringBuilder();
     for (String column : subsetColumns) {
         String value = record.getField(column);
         keyBuilder.append(column)
                   .append("=")
                   .append(value != null ? value.trim() : "")
                   .append(";");
     }
     return keyBuilder.toString();
 }

 private List<DataRecord> reverseList(List<DataRecord> records) {
     List<DataRecord> reversed = new ArrayList<>(records);
     Collections.reverse(reversed);
     return reversed;
 }


 public List<DataRecord> getDuplicates(List<DataRecord> records) {

     if (records == null || records.isEmpty()) return Collections.emptyList();

     Set<String>      seen       = new HashSet<>();
     List<DataRecord> duplicates = new ArrayList<>();

     for (DataRecord record : records) {
         String key = buildKey(record);
         if (!seen.add(key)) {
             duplicates.add(record);
         }
     }

     System.out.println("[DuplicateRemover] Found " + duplicates.size() + " duplicate record(s).");
     return duplicates;
 }
}
