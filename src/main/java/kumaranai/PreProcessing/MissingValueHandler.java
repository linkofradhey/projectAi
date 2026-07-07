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

    private Strategy strategy;
    private final String constantValue;

    // ─── Constructors ─────────────────────────────────────────────

    // FIX #6: Default constructor now initializes strategy to avoid null
    public MissingValueHandler() {
        this.strategy = Strategy.CONSTANT;
        this.constantValue = "";
    }

    public MissingValueHandler(Strategy strategy) {
        this(strategy, "0");
    }

    public MissingValueHandler(Strategy strategy, String constantValue) {
        this.strategy = strategy;
        this.constantValue = constantValue;
    }

    // ─── Public Entry Point ───────────────────────────────────────

    /**
     * Apply the chosen missing-value strategy to the list of records.
     * @param records  list of DataRecord objects
     * @param strategy chosen imputation/removal strategy
     * @return number of cells filled (or rows removed for REMOVE strategy)
     */
    public int handle(List<DataRecord> records, Strategy strategy) {
        this.strategy = strategy;

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
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    // ─── Strategy Implementations ─────────────────────────────────

    /**
     * MEAN: Fill missing numeric cells with the column mean.
     * Non-numeric columns are skipped.
     */
    private int applyMeanImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);
        if (columns.isEmpty()) return 0;

        for (String col : columns) {
            if (!isNumericColumn(records, col)) continue;

            // FIX #3: Compute values once, pass list directly
            List<Double> values = getNumericValues(records, col);
            if (values.isEmpty()) continue;

            String fillValue = String.valueOf(computeMean(values));

            for (DataRecord r : records) {
                // FIX #1: Use local isMissing() instead of r.isMissing()
                if (isMissing(r, col)) {
                    r.setField(col, fillValue);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * MEDIAN: Fill missing numeric cells with the column median.
     * Non-numeric columns are skipped.
     */
    private int applyMedianImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);
        if (columns.isEmpty()) return 0;

        for (String col : columns) {
            if (!isNumericColumn(records, col)) continue;

            // FIX #3: Compute values once, pass list directly
            List<Double> values = getNumericValues(records, col);
            if (values.isEmpty()) continue;

            String fillValue = String.valueOf(computeMedian(values));

            for (DataRecord r : records) {
                // FIX #1: Use local isMissing() instead of r.isMissing()
                if (isMissing(r, col)) {
                    r.setField(col, fillValue);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * MODE: Fill missing cells (any column) with the most frequent value.
     */
    private int applyModeImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);
        if (columns.isEmpty()) return 0;

        for (String col : columns) {
            String fillValue = computeMode(records, col);

            for (DataRecord r : records) {
                // FIX #1: Use local isMissing() instead of r.isMissing()
                if (isMissing(r, col)) {
                    r.setField(col, fillValue);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * CONSTANT: Fill all missing cells with the configured constant value.
     */
    private int applyConstantFill(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);
        if (columns.isEmpty()) return 0;

        for (DataRecord r : records) {
            for (String col : columns) {
                // FIX #1: Use local isMissing() instead of r.isMissing()
                if (isMissing(r, col)) {
                    r.setField(col, constantValue);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * REMOVE: Drop any row that has at least one missing value.
     * FIX #2: Replaced wrong toString()=="0" with proper per-field missing check.
     */
    private int dropRowsWithMissing(List<DataRecord> records) {
        int before = records.size();
        if (before == 0) return 0;

        List<String> columns = getColumns(records);
        if (columns.isEmpty()) return 0;

        Iterator<DataRecord> it = records.iterator();
        while (it.hasNext()) {
            DataRecord r = it.next();
            boolean hasMissing = columns.stream().anyMatch(col -> isMissing(r, col));
            if (hasMissing) {
                it.remove();
            }
        }

        return before - records.size();
    }

    // ─── Helper Methods ───────────────────────────────────────────

    /**
     * FIX #5: Get column names safely — returns empty list if records is null/empty.
     */
    private List<String> getColumns(List<DataRecord> records) {
        if (records == null || records.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(records.get(0).getFields().keySet());
    }

    /**
     * FIX #1: Local missing check — null, empty, or whitespace-only = missing.
     */
    private boolean isMissing(DataRecord record, String column) {
        if (record == null || column == null) return true;
        String val = record.getField(column);
        return val == null || val.trim().isEmpty();
    }

    /**
     * Strict numeric column check:
     * All non-missing values in the column must parse as Double.
     */
    private boolean isNumericColumn(List<DataRecord> records, String column) {
        int numericCount = 0;
        int totalNonMissing = 0;

        for (DataRecord r : records) {
            String val = r.getField(column);
            if (val != null && !val.trim().isEmpty()) {
                totalNonMissing++;
                try {
                    Double.parseDouble(val.trim());
                    numericCount++;
                } catch (NumberFormatException ignored) {
                    // non-numeric value found
                }
            }
        }

        return totalNonMissing > 0 && numericCount == totalNonMissing;
    }

    /**
     * Collect all non-missing numeric values from a column.
     */
    private List<Double> getNumericValues(List<DataRecord> records, String column) {
        List<Double> list = new ArrayList<>();

        for (DataRecord r : records) {
            String val = r.getField(column);
            if (val != null && !val.trim().isEmpty()) {
                try {
                    list.add(Double.parseDouble(val.trim()));
                } catch (NumberFormatException ignored) {
                    // skip non-parseable values
                }
            }
        }
        return list;
    }

    /**
     * FIX #3: Compute mean from a pre-collected list of doubles.
     * Rounded to 2 decimal places.
     */
    private double computeMean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        return Math.round((sum / values.size()) * 100.0) / 100.0;
    }

    /**
     * FIX #3: Compute median from a pre-collected list of doubles.
     */
    private double computeMedian(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;

        Collections.sort(values);
        int n = values.size();

        return (n % 2 == 0)
                ? (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0
                : values.get(n / 2);
    }

    /**
     * FIX #4: Compute mode by streaming field values directly (not DataRecord objects).
     */
    private String computeMode(List<DataRecord> records, String column) {
        Map<String, Long> freq = records.stream()
                .map(r -> r.getField(column))
                .filter(v -> v != null && !v.trim().isEmpty())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        return freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(constantValue);
    }
}