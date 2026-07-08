package kumaranai.io;

import kumaranai.model.DataRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class DataWriter {

    public void write(List<DataRecord> records, String outputPath) throws IOException {

        if (records == null || records.isEmpty()) {
            System.out.println("[DataWriter] No records to write.");
            return;
        }

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
                        "[DataWriter] Unsupported format: '." + extension + "'. Use .csv or .xlsx");
        }
    }

        private String[] collectHeaders(List<DataRecord> records) {
        LinkedHashSet<String> allHeaders = new LinkedHashSet<>();
        for (DataRecord record : records) {
            allHeaders.addAll(record.getFields().keySet());
        }
        return allHeaders.toArray(new String[0]);
    }

    private void writeXlsx(List<DataRecord> records, File outputFile) throws IOException {

        String[] headers = collectHeaders(records); 

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            Sheet sheet = workbook.createSheet("Cleaned Data");

            // Write header row with styling
            CellStyle headerStyle = buildHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Write data rows
            int rowIdx = 1;
            for (DataRecord record : records) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                for (String header : headers) {
                    String value = record.getField(header);
                    row.createCell(colIdx++).setCellValue(safeValue(value));
                }
            }

            // Auto-size all columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);

            System.out.println("[DataWriter]  XLSX written → "
                    + outputFile.getAbsolutePath() + " | Rows: " + records.size());

        } catch (IOException e) {
            System.err.println("[DataWriter]  XLSX write failed: " + e.getMessage());
            throw e;
        }
    }

    private void writeCsv(List<DataRecord> records, File outputFile) throws IOException {

        String[] headers = collectHeaders(records); 

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // Write header line
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write data rows
            for (DataRecord record : records) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < headers.length; i++) {
                    if (i > 0) line.append(",");
                    line.append(escapeCsv(record.getField(headers[i])));
                }
                writer.write(line.toString());
                writer.newLine();
            }

            writer.flush();

            System.out.println("[DataWriter]  CSV written → "
                    + outputFile.getAbsolutePath() + " | Rows: " + records.size());

        } catch (IOException e) {
            System.err.println("[DataWriter]  CSV write failed: " + e.getMessage());
            throw e;
        }
    }

    private CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot == -1 || dot == path.length() - 1) {
            throw new IllegalArgumentException("[DataWriter] No file extension found in: " + path);
        }
        return path.substring(dot + 1);
    }

    private String safeValue(String value) {
        return value != null ? value : "";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}