//package kumaranai.dto;
//
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Value;
//
//import kumaranai.PreProcessing.MissingValueHandler;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class PreprocessingConfigDTO {
//
//	@Value("${preprocessing.input.file}")
//	private String inputFilePath="src/main/resources/input.xlsx";
//	private String outputFilePath;
//
//	private List<String> numericColumns;
//	private List<String> categoricalColumns;
//	private boolean removeDuplicates;
//	private MissingValueHandler.Strategy missingValueStrategy; 
//	private String encodingType;
//
//	public PreprocessingConfigDTO() {
//	}
//
//	public PreprocessingConfigDTO(String inputFilePath, String outputFilePath, List<String> numericColumns,
//			List<String> categoricalColumns, boolean removeDuplicates,
//			MissingValueHandler.Strategy missingValueStrategy,String encodingType) {// to get the input from frontend
//
//		this.inputFilePath = inputFilePath;
//		this.outputFilePath = outputFilePath;
//		this.numericColumns = numericColumns;
//		this.categoricalColumns = categoricalColumns;
//		this.removeDuplicates = removeDuplicates;
//		this.missingValueStrategy = missingValueStrategy;
//		this.encodingType = encodingType;
//		
//	}
//
//	@Override
//	public String toString() {
//		return "PreprocessingConfigDTO{" + "inputFilePath='" + inputFilePath + '\'' + ", outputFilePath='"
//				+ outputFilePath + '\'' + ", numericColumns=" + numericColumns + ", categoricalColumns="
//				+ categoricalColumns + ", removeDuplicates=" + removeDuplicates + ", missingValueStrategy='"
//				+ missingValueStrategy + '\'' + "encodingType='"+encodingType + '\'' + '}';
//	}
//}
package kumaranai.dto;

import java.util.List;

import kumaranai.PreProcessing.MissingValueHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreprocessingConfigDTO {

    private String inputFilePath;
    private String outputFilePath;

    private MissingValueHandler.Strategy missingValueStrategy;   // e.g. "mean", "mode", "drop"
    private boolean removeDuplicates;

    private List<String> categoricalColumns;
    private String encodingType;           // "label" or "onehot"

    private List<String> numericColumns;   // columns to normalize

    // ── Getters & Setters ─────────────────────────────────────────────
//    public String getInputFilePath()               { return inputFilePath; }
//    public void setInputFilePath(String v)         { this.inputFilePath = v; }
//
//    public String getOutputFilePath()              { return outputFilePath; }
//    public void setOutputFilePath(String v)        { this.outputFilePath = v; }
//
//    public String getMissingValueStrategy()        { return missingValueStrategy; }
//    public void setMissingValueStrategy(String v)  { this.missingValueStrategy = v; }
//
//    public boolean isRemoveDuplicates()            { return removeDuplicates; }
//    public void setRemoveDuplicates(boolean v)     { this.removeDuplicates = v; }
//
//    public List<String> getCategoricalColumns()    { return categoricalColumns; }
//    public void setCategoricalColumns(List<String> v) { this.categoricalColumns = v; }
//
//    public String getEncodingType()                { return encodingType; }
//    public void setEncodingType(String v)          { this.encodingType = v; }
//
//    public List<String> getNumericColumns()        { return numericColumns; }
//    public void setNumericColumns(List<String> v)  { this.numericColumns = v; }
}