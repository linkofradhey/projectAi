//package PreProcessing;
//
////src/main/java/com/datapreprocessing/handler/MissingValueHandler.java
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import dto.MissingValueStrategy;
//import model.DataRecord;
//
//public class MissingValueHandler {
//
// // ─── Strategy Enum ───────────────────────────────────────────
// public enum Strategy {
//     MEAN,       // Fill with column mean    (numeric columns)
//     MEDIAN,     // Fill with column median  (numeric columns)
//     MODE,       // Fill with most frequent value (categorical columns)
//     CONSTANT,   // Fill with a fixed value
//     REMOVE      // Remove rows with missing values
// }
//
//
// private final Strategy strategy;
// private final String   constantValue; // used only when strategy = CONSTANT
//
// // ─── Constructors ────────────────────────────────────────────
//
// public MissingValueHandler() {
//     this.strategy      = Strategy.MEAN;
//     this.constantValue = "0";
// }
//
// public MissingValueHandler(Strategy strategy) {
//     this.strategy      = strategy;
//     this.constantValue = "0";
// }
//
// public MissingValueHandler(Strategy strategy, String constantValue) {
//     this.strategy      = strategy;
//     this.constantValue = constantValue;
// }
//
// // ─── Main Entry Point ────────────────────────────────────────
//
// /**
//  * Handles missing values across all records based on the chosen strategy.
//  *
//  * @param records List of DataRecord objects
//  * @return Cleaned list of DataRecord objects
//  */
// public int handle(List<DataRecord> records, MissingValueStrategy strategy) {
//     int count = 0;
//
//     switch (strategy) {
//         case MEAN:
//             count = applyMeanImputation(records);
//             break;
//         case MEDIAN:
//             count = applyMedianImputation(records);
//             break;
//         case MODE:
//             count = applyModeImputation(records);
//             break;
//         case CONSTANT:
//             count = applyConstantFill(records);
//             break;
//         case DROP:
//             count = dropRowsWithMissing(records);
//             break;
//         default:
//             throw new IllegalArgumentException("Unknown strategy: " + strategy);
//     }
//
//     return count;
// }
//
// private int applyMeanImputation(List<DataRecord> records) {
//     // TODO: implement mean imputation logic
//     return 0;
// }
//
// private int applyMedianImputation(List<DataRecord> records) {
//     // TODO: implement median imputation logic
//     return 0;
// }
//
// private int applyModeImputation(List<DataRecord> records) {
//     // TODO: implement mode imputation logic
//     return 0;
// }
//
// private int applyConstantFill(List<DataRecord> records) {
//     // TODO: implement constant fill logic
//     return 0;
// }
//
// private int dropRowsWithMissing(List<DataRecord> records) {
//     int before = records.size();
//     Iterator<DataRecord> iterator = records.iterator();
//     while (iterator.hasNext()) {
//         DataRecord record = iterator.next();
//         if (record.hasMissingValues()) {
//             iterator.remove();
//         }
//     }
//     return before - records.size();
// }
// // ─── Strategy Implementations ────────────────────────────────
//
// /**
//  * Computes the fill value for a column based on the selected strategy.
//  */
// private String computeFillValue(List<DataRecord> records, String column) {
//     switch (strategy) {
//         case MEAN:
//             return isNumericColumn(records, column)
//                     ? String.valueOf(computeMean(records, column))
//                     : computeMode(records, column);
//
//         case MEDIAN:
//             return isNumericColumn(records, column)
//                     ? String.valueOf(computeMedian(records, column))
//                     : computeMode(records, column);
//
//         case MODE:
//             return computeMode(records, column);
//
//         case CONSTANT:
//             return constantValue;
//
//         default:
//             return "unknown";
//     }
// }
//
// /**
//  * Computes the MEAN of a numeric column (ignores missing values).
//  */
// private double computeMean(List<DataRecord> records, String column) {
//     List<Double> values = getNumericValues(records, column);
//     if (values.isEmpty()) return 0.0;
//
//     double sum = values.stream().mapToDouble(Double::doubleValue).sum();
//     double mean = sum / values.size();
//
//     // Round to 2 decimal places
//     return Math.round(mean * 100.0) / 100.0;
// }
//
// /**
//  * Computes the MEDIAN of a numeric column (ignores missing values).
//  */
// private double computeMedian(List<DataRecord> records, String column) {
//     List<Double> values = getNumericValues(records, column);
//     if (values.isEmpty()) return 0.0;
//
//     Collections.sort(values);
//     int size = values.size();
//
//     if (size % 2 == 0) {
//         return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
//     } else {
//         return values.get(size / 2);
//     }
// }
//
// /**
//  * Computes the MODE (most frequent value) of a column (ignores missing values).
//  */
// private String computeMode(List<DataRecord> records, String column) {
//     Map<String, Long> frequency = records.stream()
//             .filter(r -> !r.isMissing(column))
//             .collect(Collectors.groupingBy(
//                     r -> r.getField(column),
//                     Collectors.counting()
//             ));
//
//     return frequency.entrySet().stream()
//             .max(Map.Entry.comparingByValue())
//             .map(Map.Entry::getKey)
//             .orElse("unknown");
// }
//
// /**
//  * Removes all records that have at least one missing value.
//  */
// private List<DataRecord> removeRowsWithMissingValues(List<DataRecord> records) {
//     List<String> columns = new ArrayList<>(records.get(0).getFields().keySet());
//
//     List<DataRecord> cleaned = records.stream()
//             .filter(record -> columns.stream().noneMatch(record::isMissing))
//             .collect(Collectors.toList());
//
//     int removed = records.size() - cleaned.size();
//     System.out.println("[MissingValueHandler] Removed " + removed + " rows with missing values.");
//
//     return cleaned;
// }
//
// // ─── Utility Helpers ─────────────────────────────────────────
//
// /**
//  * Checks if a column contains numeric values.
//  */
// private boolean isNumericColumn(List<DataRecord> records, String column) {
//     for (DataRecord record : records) {
//         String value = record.getField(column);
//         if (value != null && !value.trim().isEmpty()) {
//             try {
//                 Double.parseDouble(value.trim());
//                 return true;
//             } catch (NumberFormatException e) {
//                 return false;
//             }
//         }
//     }
//     return false;
// }
//
// /**
//  * Extracts all non-missing numeric values from a column.
//  */
// private List<Double> getNumericValues(List<DataRecord> records, String column) {
//     List<Double> values = new ArrayList<>();
//     for (DataRecord record : records) {
//         String value = record.getField(column);
//         if (value != null && !value.trim().isEmpty()) {
//             try {o
//                 values.add(Double.parseDouble(value.trim()));
//             } catch (NumberFormatException ignored) {}
//         }
//     }
//     return values;
// }
//}


package kumaranai.PreProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;
@Component

public class MissingValueHandler {

    // ─── Strategy Enum ───────────────────────────────────────────
    public enum Strategy {
        MEAN,
        MEDIAN,
        MODE,
        CONSTANT,
        REMOVE
    }

    private  Strategy strategy;
    private final String constantValue;

    public MissingValueHandler() {
		this.constantValue = "";
    }
    // ─── Constructors ────────────────────────────────────────────
    public MissingValueHandler(Strategy strategy) {
        this(strategy, "0");
    }

    public MissingValueHandler(Strategy strategy, String constantValue) {
        this.strategy = strategy;
        this.constantValue = constantValue;
    }

    // ─── Main Method ─────────────────────────────────────────────
    public int handle(List<DataRecord> records, Strategy strategy) {
        switch (strategy) {
            case MEAN:
                return applyMeanImputation(records);
            case MEDIAN:
                return applyMedianImputation(records);
            case MODE:
                return applyModeImputation(records);
            case CONSTANT:
                return applyConstantFill(records);
            case REMOVE:
                return dropRowsWithMissing(records);
            default:
                throw new IllegalArgumentException("Unknown strategy");
        }
    }

    // ─── Strategy Implementations ────────────────────────────────

    private int applyMeanImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);

        for (String col : columns) {
            if (!isNumericColumn(records, col)) continue;

            String fillValue = String.valueOf(computeMean(records, col));

            for (DataRecord r : records) {
                if (r.isMissing(col)) {
                    r.setField(col, fillValue);
                    count++;
                }
            }
        }
        return count;
    }

    private int applyMedianImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);

        for (String col : columns) {
            if (!isNumericColumn(records, col)) continue;

            String fillValue = String.valueOf(computeMedian(records, col));

            for (DataRecord r : records) {
                if (r.isMissing(col)) {
                    r.setField(col, fillValue);
                    count++;
                }
            }
        }
        return count;
    }

    private int applyModeImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);

        for (String col : columns) {
            String fillValue = computeMode(records, col);

            for (DataRecord r : records) {
                if (r.isMissing(col)) {
                    r.setField(col, fillValue);
                    count++;
                }
            }
        }
        return count;
    }

    private int applyConstantFill(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);

        for (DataRecord r : records) {
            for (String col : columns) {
                if (r.isMissing(col)) {
                    r.setField(col, constantValue);
                    count++;
                }
            }
        }
        return count;
    }

    private int dropRowsWithMissing(List<DataRecord> records) {
        int before = records.size();

        Iterator<DataRecord> it = records.iterator();
        while (it.hasNext()) {
            if (it.next().toString()=="0" ) {//doubt
                it.remove();
            }
        }

        return before - records.size();
    }

    // ─── Helper Methods ──────────────────────────────────────────

    private List<String> getColumns(List<DataRecord> records) {
        return new ArrayList<>(records.get(0).getFields().keySet());
    }

    private double computeMean(List<DataRecord> records, String column) {
        List<Double> values = getNumericValues(records, column);
        if (values.isEmpty()) return 0.0;

        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        return Math.round((sum / values.size()) * 100.0) / 100.0;
    }

    private double computeMedian(List<DataRecord> records, String column) {
        List<Double> values = getNumericValues(records, column);
        if (values.isEmpty()) return 0.0;

        Collections.sort(values);
        int n = values.size();

        return (n % 2 == 0)
                ? (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0
                : values.get(n / 2);
    }

    private String computeMode(List<DataRecord> records, String column) {
        Map<String, Long> freq = records.stream()
                .filter(r -> !r.isMissing(column))
                .collect(Collectors.groupingBy(
                        r -> r.getField(column),
                        Collectors.counting()
                ));

        return freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(constantValue);
    }

    private boolean isNumericColumn(List<DataRecord> records, String column) {
        int numericCount = 0, total = 0;

        for (DataRecord r : records) {
            String val = r.getField(column);
            if (val != null && !val.trim().isEmpty()) {
                total++;
                try {
                    Double.parseDouble(val);
                    numericCount++;
                } catch (Exception ignored) {}
            }
        }

        return total > 0 && numericCount == total; // strict check
    }

    private List<Double> getNumericValues(List<DataRecord> records, String column) {
        List<Double> list = new ArrayList<>();

        for (DataRecord r : records) {
            String val = r.getField(column);
            if (val != null && !val.trim().isEmpty()) {
                try {
                    list.add(Double.parseDouble(val));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

	
}