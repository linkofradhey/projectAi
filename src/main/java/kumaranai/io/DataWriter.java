package kumaranai.io;

import kumaranai.model.DataRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Writes DataRecord objects (HashMap-based) to CSV or XLSX.
 * Fully dynamic — no hardcoded column names.
 * DataLoader and DataRecord are completely untouched.
 */
@Component
public class DataWriter {

    // ────────────────────────────────────────────────────────────
    //  PUBLIC ENTRY POINT
    // ────────────────────────────────────────────────────────────

    /**
     * Auto-detects output format from file extension (.csv or .xlsx).
     *
     * @param records    List of DataRecord objects from preprocessing
     * @param outputPath Full output file path
     */
    public void write(List<DataRecord> records, String outputPath) throws IOException {

        if (records == null || records.isEmpty()) {
            System.out.println("[DataWriter] No records to write.");
            return;
        }

        // ✅ Ensure output directory exists
        File outputFile = new File(outputPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        String extension = getExtension(outputPath).toLowerCase();

        switch (extension) {
            case "xlsx":
                writeXlsx(records, outputFile);
                break;
            case "csv":
                writeCsv(records, outputFile);
                break;
            default:
                throw new IllegalArgumentException(
                    "[DataWriter] Unsupported format: '." + extension +
                    "'. Use .csv or .xlsx"
                );
        }
    }

    // ────────────────────────────────────────────────────────────
    //  XLSX WRITER
    // ────────────────────────────────────────────────────────────

    private void writeXlsx(List<DataRecord> records, File outputFile) throws IOException {

        // ✅ Extract headers from the first record's field keys
        // LinkedHashMap in DataRecord preserves insertion order
        Map<String, String> firstFields = records.get(0).getFields();
        String[] headers = firstFields.keySet().toArray(new String[0]);

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            Sheet sheet = workbook.createSheet("Cleaned Data");

            // ── Header row ───────────────────────────────────────
            CellStyle headerStyle = buildHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows ────────────────────────────────────────
            int rowIdx = 1;
            for (DataRecord record : records) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                for (String header : headers) {
                    // ✅ Uses DataRecord's own getField() helper
                    String value = record.getField(header);
                    row.createCell(colIdx++).setCellValue(safeValue(value));
                }
            }

            // ✅ Auto-size all columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ✅ Flush and write — prevents corrupt file
            workbook.write(fos);
            fos.flush();

            System.out.println("[DataWriter] ✅ XLSX written → "
                + outputFile.getAbsolutePath()
                + " | Rows: " + records.size());

        } catch (IOException e) {
            System.err.println("[DataWriter] ❌ XLSX write failed: " + e.getMessage());
            throw e;
        }
    }

    // ────────────────────────────────────────────────────────────
    //  CSV WRITER
    // ────────────────────────────────────────────────────────────

    private void writeCsv(List<DataRecord> records, File outputFile) throws IOException {

        // ✅ Extract headers from the first record's field keys
        Map<String, String> firstFields = records.get(0).getFields();
        String[] headers = firstFields.keySet().toArray(new String[0]);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // ── Header line ──────────────────────────────────────
            writer.write(String.join(",", headers));
            writer.newLine();

            // ── Data lines ───────────────────────────────────────
            for (DataRecord record : records) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < headers.length; i++) {
                    if (i > 0) line.append(",");
                    // ✅ Uses DataRecord's own getField() helper
                    line.append(escapeCsv(record.getField(headers[i])));
                }
                writer.write(line.toString());
                writer.newLine();
            }

            writer.flush();

            System.out.println("[DataWriter] ✅ CSV written → "
                + outputFile.getAbsolutePath()
                + " | Rows: " + records.size());

        } catch (IOException e) {
            System.err.println("[DataWriter] ❌ CSV write failed: " + e.getMessage());
            throw e;
        }
    }

    // ────────────────────────────────────────────────────────────
    //  HELPERS
    // ────────────────────────────────────────────────────────────

    /** Bold + blue header style for XLSX */
    private CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /** Extracts file extension from path */
    private String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot == -1 || dot == path.length() - 1) {
            throw new IllegalArgumentException(
                "[DataWriter] No file extension found in: " + path
            );
        }
        return path.substring(dot + 1);
    }

    /** Prevents NullPointerException for missing/null values */
    private String safeValue(String value) {
        return value != null ? value : "";
    }

    /** Wraps CSV values in quotes if they contain commas or newlines */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}