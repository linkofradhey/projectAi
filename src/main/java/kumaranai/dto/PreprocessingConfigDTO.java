package kumaranai.dto;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import kumaranai.PreProcessing.MissingValueHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreprocessingConfigDTO {

	@Value("${preprocessing.input.file}")
	private String inputFilePath="src/main/resources/input.xlsx";
	private String outputFilePath;

	private List<String> numericColumns;
	private List<String> categoricalColumns;
	private boolean removeDuplicates;
	private MissingValueHandler.Strategy missingValueStrategy; 
	private String encodingType;

	public PreprocessingConfigDTO() {
	}

	public PreprocessingConfigDTO(String inputFilePath, String outputFilePath, List<String> numericColumns,
			List<String> categoricalColumns, boolean removeDuplicates,
			MissingValueHandler.Strategy missingValueStrategy,String encodingType) {// to get the input from frontend

		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
		this.numericColumns = numericColumns;
		this.categoricalColumns = categoricalColumns;
		this.removeDuplicates = removeDuplicates;
		this.missingValueStrategy = missingValueStrategy;
		this.encodingType = encodingType;
		
	}

	@Override
	public String toString() {
		return "PreprocessingConfigDTO{" + "inputFilePath='" + inputFilePath + '\'' + ", outputFilePath='"
				+ outputFilePath + '\'' + ", numericColumns=" + numericColumns + ", categoricalColumns="
				+ categoricalColumns + ", removeDuplicates=" + removeDuplicates + ", missingValueStrategy='"
				+ missingValueStrategy + '\'' + "encodingType='"+encodingType + '\'' + '}';
	}
}
