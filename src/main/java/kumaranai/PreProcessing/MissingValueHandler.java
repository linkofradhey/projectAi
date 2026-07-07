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

    public enum Strategy {
        MEAN,
        MEDIAN,
        MODE,
        CONSTANT,
        REMOVE
    }

    private Strategy strategy;
    private final String constantValue;


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


    private int applyMeanImputation(List<DataRecord> records) {
        int count = 0;
        List<String> columns = getColumns(records);
        if (columns.isEmpty()) return 0;

        for (String col : columns) {
            if (!isNumericColumn(records, col)) continue;

            List<Double> values = getNumericValues(records, col);
            if (values.isEmpty()) continue;

            String fillValue = String.valueOf(computeMean(values));

            for (DataRecord r : records) {
                if (isMissing(r, col)) {
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
        if (columns.isEmpty()) return 0;

        for (String col : columns) {
            if (!isNumericColumn(records, col)) continue;

            List<Double> values = getNumericValues(records, col);
            if (values.isEmpty()) continue;

            String fillValue = String.valueOf(computeMedian(values));

            for (DataRecord r : records) {
                if (isMissing(r, col)) {
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
        if (columns.isEmpty()) return 0;

        for (String col : columns) {
            String fillValue = computeMode(records, col);

            for (DataRecord r : records) {
                if (isMissing(r, col)) {
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
        if (columns.isEmpty()) return 0;

        for (DataRecord r : records) {
            for (String col : columns) {
                if (isMissing(r, col)) {
                    r.setField(col, constantValue);
                    count++;
                }
            }
        }
        return count;
    }

  
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

    
    private List<String> getColumns(List<DataRecord> records) {
        if (records == null || records.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(records.get(0).getFields().keySet());
    }

   
    private boolean isMissing(DataRecord record, String column) {
        if (record == null || column == null) return true;
        String val = record.getField(column);
        return val == null || val.trim().isEmpty();
    }

   
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

    
    private double computeMean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        return Math.round((sum / values.size()) * 100.0) / 100.0;
    }

    
    private double computeMedian(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;

        Collections.sort(values);
        int n = values.size();

        return (n % 2 == 0)
                ? (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0
                : values.get(n / 2);
    }

    
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