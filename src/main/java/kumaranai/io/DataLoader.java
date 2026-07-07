package kumaranai.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory; // FIX #2: replaces XSSFWorkbook import
import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;

@Component
public class DataLoader {

    /**
     * Entry point — detects file type and routes to correct loader.
     */
    public List<DataRecord> load(String filePath) {
        if (filePath.endsWith(".xlsx") || filePath.endsWith(".xls")) {
            return loadFromExcel(filePath);
        } else {
            return loadFromCsv(filePath);
        }
    }

    private List<DataRecord> loadFromExcel(String filePath) {
        List<DataRecord> records = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             // FIX #2: WorkbookFactory handles both .xls and .xlsx
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Read header row
            List<String> headers = new ArrayList<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    // FIX #3: Use getCellValueAsString() — safe for any cell type
                    headers.add(getCellValueAsString(cell));
                }
            }

            // Read data rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> fields = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    fields.put(headers.get(i), getCellValueAsString(cell));
                }

                // FIX #1: DataRecord has no Map constructor — use setField() instead
                DataRecord record = new DataRecord();
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    record.setField(entry.getKey(), entry.getValue());
                }
                records.add(record);
            }

            System.out.println("[DataLoader] Loaded " + records.size() + " records from Excel: " + filePath);

        } catch (Exception e) {
            throw new RuntimeException("[DataLoader] Failed to read Excel file: " + filePath, e);
        }

        return records;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                return (val == Math.floor(val))
                    ? String.valueOf((long) val)
                    : String.valueOf(val);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case BLANK:   return "";  // treated as missing value
            default:      return "";
        }
    }

    // FIX #4: Throw exception instead of silently returning empty list
    private List<DataRecord> loadFromCsv(String filePath) {
        throw new UnsupportedOperationException(
            "[DataLoader] CSV loading is not yet implemented for: " + filePath
        );
    }
}