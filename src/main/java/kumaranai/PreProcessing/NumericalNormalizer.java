package kumaranai.PreProcessing;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;
@Component
public class NumericalNormalizer {
// public enum NormalizationType {
//     MIN_MAX,    // Scales values to [0, 1]
//     Z_SCORE,    // Standardizes to mean=0, std=1
//     ROBUST      // Uses median and IQR — resistant to outliers
// }

// private final NormalizationType normalizationType;

 private final Map<String, double[]> statsRegistry = new LinkedHashMap<>();


// public NumericalNormalizer() {
//     this.normalizationType = NormalizationType.MIN_MAX;
// }

// public NumericalNormalizer(NormalizationType normalizationType) {
//     this.normalizationType = normalizationType;
// }


 
 public List<DataRecord> normalize(List<DataRecord> records, List<String> numericalColumns,String normalizationType) {

     if (records == null || records.isEmpty()) {
         System.out.println("[NumericalNormalizer] No records to process.");
         return records;
     }

     if (numericalColumns == null || numericalColumns.isEmpty()) {
         System.out.println("[NumericalNormalizer] No numerical columns specified.");
         return records;
     }

     switch (normalizationType) {
         case "minmax": return applyMinMax(records, numericalColumns);
         case "zscore": return applyZScore(records, numericalColumns);
         case "robust":  return applyRobust(records, numericalColumns);
         default:      return records;
     }
 }


 
 private List<DataRecord> applyMinMax(List<DataRecord> records, List<String> columns) {

     for (String column : columns) {
         List<Double> values = extractDoubles(records, column);
         if (values.isEmpty()) continue;

         double min   = Collections.min(values);
         double max   = Collections.max(values);
         double range = max - min;

         if (range == 0) {
             System.out.println("[NumericalNormalizer] MIN_MAX | Column '" + column
                     + "' has zero range. Skipping.");
             continue;
         }

         statsRegistry.put(column, new double[]{min, max});

         for (DataRecord record : records) {
             applyTransform(record, column, v -> (v - min) / range);//
         }

         System.out.println("[NumericalNormalizer] MIN_MAX | Column '" + column
                 + "' | Min=" + min + ", Max=" + max);
     }

     return records;
 }

 
 private List<DataRecord> applyZScore(List<DataRecord> records, List<String> columns) {

     for (String column : columns) {
         List<Double> values = extractDoubles(records, column);
         if (values.isEmpty()) continue;

         double mean = computeMean(values);
         double std  = computeStdDev(values, mean);

         if (std == 0) {
             System.out.println("[NumericalNormalizer] Z_SCORE | Column '" + column
                     + "' has zero std deviation. Skipping.");
             continue;
         }

         statsRegistry.put(column, new double[]{mean, std});

         for (DataRecord record : records) {
             applyTransform(record, column, v -> (v - mean) / std);
         }

         System.out.println("[NumericalNormalizer] Z_SCORE | Column '" + column
                 + "' | Mean=" + String.format("%.4f", mean)
                 + ", StdDev=" + String.format("%.4f", std));
     }

     return records;
 }


 private List<DataRecord> applyRobust(List<DataRecord> records, List<String> columns) {

     for (String column : columns) {
         List<Double> values = extractDoubles(records, column);
         if (values.isEmpty()) continue;

         Collections.sort(values);
         double median = computeMedian(values);
         double q1     = computePercentile(values, 25);
         double q3     = computePercentile(values, 75);
         double iqr    = q3 - q1;

         if (iqr == 0) {
             System.out.println("[NumericalNormalizer] ROBUST | Column '" + column
                     + "' has zero IQR. Skipping.");
             continue;
         }

         statsRegistry.put(column, new double[]{median, iqr});

         for (DataRecord record : records) {
             applyTransform(record, column, v -> (v - median) / iqr);
         }

         System.out.println("[NumericalNormalizer] ROBUST | Column '" + column
                 + "' | Median=" + String.format("%.4f", median)
                 + ", IQR=" + String.format("%.4f", iqr));
     }

     return records;
 }


 private void applyTransform(DataRecord record, String column,
                             java.util.function.DoubleUnaryOperator transform) {
     String raw = record.getField(column);
     if (raw != null && !raw.trim().isEmpty()) {
         try {
             double value       = Double.parseDouble(raw.trim());
             double transformed = transform.applyAsDouble(value);
             record.setField(column, String.format("%.6f", transformed));
         } catch (NumberFormatException e) {
             System.out.println("[NumericalNormalizer] Skipping non-numeric value '"
                     + raw + "' in column '" + column + "'");
         }
     }
 }

 private List<Double> extractDoubles(List<DataRecord> records, String column) {
     return records.stream()
             .map(r -> r.getField(column))
             .filter(v -> v != null && !v.trim().isEmpty())
             .flatMap(v -> {
                 try {
                     return java.util.stream.Stream.of(Double.parseDouble(v.trim()));
                 } catch (NumberFormatException e) {
                     return java.util.stream.Stream.empty();
                 }
             })
             .collect(Collectors.toList());
 }

 private double computeMean(List<Double> values) {
     return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
 }

 private double computeStdDev(List<Double> values, double mean) {
     double variance = values.stream()
             .mapToDouble(v -> Math.pow(v - mean, 2))
             .average().orElse(0);
     return Math.sqrt(variance);
 }

 private double computeMedian(List<Double> sorted) {
     int size = sorted.size();
     return (size % 2 == 0)
             ? (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0
             : sorted.get(size / 2);
 }

 private double computePercentile(List<Double> sorted, double percentile) {
     int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
     return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
 }

 public Map<String, double[]> getStatsRegistry() {
     return Collections.unmodifiableMap(statsRegistry);
 }

 public double inverseTransform(String column, double scaledValue,String normalizationType) {
     double[] stats = statsRegistry.get(column);
     if (stats == null) throw new IllegalArgumentException("No stats found for column: " + column);

     switch (normalizationType) {
         case "minmax": return scaledValue * (stats[1] - stats[0]) + stats[0]; // v * range + min
         case "zscore": return scaledValue * stats[1] + stats[0];              // v * std + mean
         default:      throw new UnsupportedOperationException(
                 "Inverse transform not supported for: " + normalizationType);
     }
 }
}