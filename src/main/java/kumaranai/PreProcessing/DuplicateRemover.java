package kumaranai.PreProcessing;

//src/main/java/com/datapreprocessing/handler/DuplicateRemover.java

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

 // ─── Strategy Enum ───────────────────────────────────────────
 public enum KeepStrategy {
     FIRST,  // Keep the first occurrence of a duplicate
     LAST    // Keep the last occurrence of a duplicate
 }

 private final KeepStrategy keepStrategy;
 private final List<String> subsetColumns; // If set, duplicates are checked only on these columns

 // ─── Constructors ────────────────────────────────────────────

 /** Default: keep FIRST, check ALL columns */
 public DuplicateRemover() {
     this.keepStrategy  = KeepStrategy.FIRST;
     this.subsetColumns = null;
 }

 /** Custom strategy, check ALL columns */
 public DuplicateRemover(KeepStrategy keepStrategy) {
     this.keepStrategy  = keepStrategy;
     this.subsetColumns = null;
 }

 /** Custom strategy + subset of columns to check for duplicates */
 public DuplicateRemover(KeepStrategy keepStrategy, List<String> subsetColumns) {
     this.keepStrategy  = keepStrategy;
     this.subsetColumns = subsetColumns;
 }

 // ─── Main Entry Point ────────────────────────────────────────

 /**
  * Removes duplicate DataRecord entries based on the configured strategy.
  *
  * @param records List of DataRecord objects
  * @return Deduplicated list of DataRecord objects
  */
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

     // Reverse back if LAST strategy was used
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

 // ─── Utility Helpers ─────────────────────────────────────────

 /**
  * Builds a unique key for a record.
  * If subsetColumns is defined, only those columns are used for comparison.
  */
 private String buildKey(DataRecord record) {
     if (subsetColumns == null || subsetColumns.isEmpty()) {
         // Use all fields for comparison
         return record.getFields().toString();
     }

     // Use only the specified subset of columns
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

 /**
  * Returns a reversed copy of the list.
  */
 private List<DataRecord> reverseList(List<DataRecord> records) {
     List<DataRecord> reversed = new ArrayList<>(records);
     Collections.reverse(reversed);
     return reversed;
 }

 // ─── Reporting Helper ────────────────────────────────────────

 /**
  * Returns a list of all duplicate records (for logging/reporting purposes).
  *
  * @param records Original list of DataRecord objects
  * @return List of duplicate DataRecord objects
  */
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
