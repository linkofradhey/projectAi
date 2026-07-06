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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import kumaranai.model.DataRecord;

@Component
public class DataLoader {

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
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); 
            Iterator<Row> rowIterator = sheet.iterator();

            // First row = headers
            List<String> headers = new ArrayList<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue().trim());
                }
            }

            // Remaining rows = data
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> fields = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    fields.put(headers.get(i), getCellValueAsString(cell));
                }

                records.add(new DataRecord(fields));
            }

            System.out.println("[DataLoader] Loaded " + records.size() + " records from Excel: " + filePath);

        } catch (Exception e) {
            throw new RuntimeException("[DataLoader] Failed to read Excel file: " + filePath, e);
        }

        return records;
    }

    // ─── Cell Value Extractor ─────────────────────────────────
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: 
                // Avoid scientific notation for whole numbers
                double val = cell.getNumericCellValue();
                return (val == Math.floor(val)) 
                    ? String.valueOf((long) val) 
                    : String.valueOf(val);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case BLANK:   return "";   // treated as missing value
            default:      return "";
        }
    }

    // ─── Existing CSV Reader (unchanged) ──────────────────────
    private List<DataRecord> loadFromCsv(String filePath) {
        // your existing CSV loading logic
        return new ArrayList<>();
    }
}