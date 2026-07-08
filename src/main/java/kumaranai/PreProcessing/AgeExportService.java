//package kumaranai.PreProcessing;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.ClientAnchor;
//import org.apache.poi.ss.usermodel.Drawing;
//import org.apache.poi.ss.usermodel.FillPatternType;
//import org.apache.poi.ss.usermodel.Font;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.poi.ss.usermodel.IndexedColors;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//public class AgeExportService {
//
//    public static byte[] exportToXLSX(
//        List<String> ageColumn,
//        List<Integer> encodedColumn,
//        Map<String, Integer> encodingMap,
//        Map<String, Integer> valueCounts
//    ) throws Exception {
//
//        XSSFWorkbook workbook = new XSSFWorkbook();
//
//        // ── Styles ────────────────────────────────────────────────────
//        CellStyle headerStyle = createHeaderStyle(workbook);
//
//        // ════════════════════════════════════════════════════════════
//        // SHEET 1 — Encoded Age Data
//        // ════════════════════════════════════════════════════════════
//        Sheet sheet1 = workbook.createSheet("Encoded Age Data");
//        int rowIdx = 0;
//
//        Row header1 = sheet1.createRow(rowIdx++);
//        writeHeaderRow(header1, headerStyle,
//            new String[]{"Row Index", "Original Age", "Encoded Label"}, sheet1);
//
//        for (int i = 0; i < ageColumn.size(); i++) {
//            Row row = sheet1.createRow(rowIdx++);
//            row.createCell(0).setCellValue(i + 1);
//            row.createCell(1).setCellValue(ageColumn.get(i));
//            row.createCell(2).setCellValue(encodedColumn.get(i));
//        }
//
//        // ════════════════════════════════════════════════════════════
//        // SHEET 2 — Encoding Map
//        // ════════════════════════════════════════════════════════════
//        Sheet sheet2 = workbook.createSheet("Encoding Map");
//        int rowIdx2 = 0;
//
//        Row header2 = sheet2.createRow(rowIdx2++);
//        writeHeaderRow(header2, headerStyle,
//            new String[]{"Original Age", "Encoded Label"}, sheet2);
//
//        for (Map.Entry<String, Integer> entry : encodingMap.entrySet()) {
//            Row row = sheet2.createRow(rowIdx2++);
//            row.createCell(0).setCellValue(entry.getKey());
//            row.createCell(1).setCellValue(entry.getValue());
//        }
//
//        // ════════════════════════════════════════════════════════════
//        // SHEET 3 — Pie Chart Data + Embedded Pie Chart Image
//        // ════════════════════════════════════════════════════════════
//        Sheet sheet3 = workbook.createSheet("Pie Chart");
//        int rowIdx3 = 0;
//        int total = ageColumn.size();
//
//        Row header3 = sheet3.createRow(rowIdx3++);
//        writeHeaderRow(header3, headerStyle,
//            new String[]{"Age Value", "Encoded Label", "Count", "Percentage"}, sheet3);
//
//        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
//            double pct = (entry.getValue() * 100.0) / total;
//            Row row = sheet3.createRow(rowIdx3++);
//            row.createCell(0).setCellValue(entry.getKey());
//            row.createCell(1).setCellValue(encodingMap.get(entry.getKey()));
//            row.createCell(2).setCellValue(entry.getValue());
//            row.createCell(3).setCellValue(String.format("%.2f%%", pct));
//        }
//
//        // ── Embed Pie Chart Image ─────────────────────────────────────
//        byte[] pieChartBytes = AgeChartGenerator.generatePieChart(valueCounts, encodingMap);
//        embedImageInSheet(workbook, sheet3, pieChartBytes,
//            rowIdx3 + 2,   // start row (2 rows below data)
//            0,             // start col
//            rowIdx3 + 22,  // end row
//            5              // end col
//        );
//
//        // ════════════════════════════════════════════════════════════
//        // SHEET 4 — Bar Chart Data + Embedded Bar Chart Image
//        // ════════════════════════════════════════════════════════════
//        Sheet sheet4 = workbook.createSheet("Bar Chart");
//        int rowIdx4 = 0;
//
//        Row header4 = sheet4.createRow(rowIdx4++);
//        writeHeaderRow(header4, headerStyle,
//            new String[]{"Age Value", "Encoded Label", "Frequency"}, sheet4);
//
//        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
//            Row row = sheet4.createRow(rowIdx4++);
//            row.createCell(0).setCellValue(entry.getKey());
//            row.createCell(1).setCellValue(encodingMap.get(entry.getKey()));
//            row.createCell(2).setCellValue(entry.getValue());
//        }
//
//        // ── Embed Bar Chart Image ─────────────────────────────────────
//        byte[] barChartBytes = AgeChartGenerator.generateBarChart(valueCounts, encodingMap);
//        embedImageInSheet(workbook, sheet4, barChartBytes,
//            rowIdx4 + 2,   // start row
//            0,             // start col
//            rowIdx4 + 22,  // end row
//            6              // end col
//        );
//
//        // ── Write to bytes ────────────────────────────────────────────
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        workbook.write(baos);
//        workbook.close();
//        return baos.toByteArray();
//    }
//
//    // ── Helper: Embed PNG image into a sheet at given cell range ──────
//    private static void embedImageInSheet(
//        XSSFWorkbook workbook, Sheet sheet,
//        byte[] imageBytes,
//        int row1, int col1, int row2, int col2
//    ) {
//        int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
//        Drawing<?> drawing = sheet.createDrawingPatriarch();
//
//        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
//        anchor.setCol1(col1);
//        anchor.setRow1(row1);
//        anchor.setCol2(col2);
//        anchor.setRow2(row2);
//
//        drawing.createPicture(anchor, pictureIdx);
//    }
//
//    // ── Helper: Write styled header row ──────────────────────────────
//    private static void writeHeaderRow(
//        Row row, CellStyle style, String[] headers, Sheet sheet) {
//
//        for (int i = 0; i < headers.length; i++) {
//            Cell cell = row.createCell(i);
//            cell.setCellValue(headers[i]);
//            cell.setCellStyle(style);
//            sheet.setColumnWidth(i, 5000);
//        }
//    }
//
//    // ── Helper: Create header cell style ─────────────────────────────
//    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
//        CellStyle style = workbook.createCellStyle();
//        Font font = workbook.createFont();
//        font.setBold(true);
//        font.setColor(IndexedColors.WHITE.getIndex());
//        style.setFont(font);
//        style.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        style.setAlignment(HorizontalAlignment.CENTER);
//        return style;
//    }
//}