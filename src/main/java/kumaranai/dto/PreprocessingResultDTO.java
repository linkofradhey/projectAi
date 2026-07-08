//package kumaranai.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class PreprocessingResultDTO {//print out output 
//    private int totalRowsLoaded;
//    private int duplicatesRemoved;
//    private int missingValuesFilled;
//    private int totalRowsAfterCleaning;
//    private String outputPath;
//    private String status;
//    private String message;
//}
//	

package kumaranai.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class PreprocessingResultDTO {

    // ── Pipeline Stats ─────────────────────────────────────────────────
    private int totalRowsLoaded;
    private int missingValuesFilled;
    private int duplicatesRemoved;
    private int totalRowsAfterCleaning;

    // ── Output ─────────────────────────────────────────────────────────
    private String outputPath;
    private String status;    // "SUCCESS" or "FAILED"
    private String message;

    // ── Chart (Base64-encoded PNG) ─────────────────────────────────────
    private String pieChartBase64;   // ← UI renders this as <img src="data:image/png;base64,...">
    private String barChartBase64;   // ← optional bar chart

    // ── Getters & Setters ──────────────────────────────────────────────
//    public int getTotalRowsLoaded()                  { return totalRowsLoaded; }
//    public void setTotalRowsLoaded(int v)            { this.totalRowsLoaded = v; }
//
//    public int getMissingValuesFilled()              { return missingValuesFilled; }
//    public void setMissingValuesFilled(int v)        { this.missingValuesFilled = v; }
//
//    public int getDuplicatesRemoved()                { return duplicatesRemoved; }
//    public void setDuplicatesRemoved(int v)          { this.duplicatesRemoved = v; }
//
//    public int getTotalRowsAfterCleaning()           { return totalRowsAfterCleaning; }
//    public void setTotalRowsAfterCleaning(int v)     { this.totalRowsAfterCleaning = v; }
//
//    public String getOutputPath()                    { return outputPath; }
//    public void setOutputPath(String v)              { this.outputPath = v; }
//
//    public String getStatus()                        { return status; }
//    public void setStatus(String v)                  { this.status = v; }
//
//    public String getMessage()                       { return message; }
//    public void setMessage(String v)                 { this.message = v; }
//
//    public String getPieChartBase64()                { return pieChartBase64; }
//    public void setPieChartBase64(String v)          { this.pieChartBase64 = v; }
//
//    public String getBarChartBase64()                { return barChartBase64; }
//    public void setBarChartBase64(String v)          { this.barChartBase64 = v; }
}