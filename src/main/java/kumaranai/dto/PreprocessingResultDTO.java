package kumaranai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreprocessingResultDTO {//print out output 
    private int totalRowsLoaded;
    private int duplicatesRemoved;
    private int missingValuesFilled;
    private int totalRowsAfterCleaning;
    private String outputPath;
    private String status;
    private String message;
}
	