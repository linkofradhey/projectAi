package kumaranai.dto;

import java.util.List;

import kumaranai.model.DataRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreprocessingResultDTO {
    private int totalRowsLoaded;
    private List<DataRecord> duplicatesRemoved;
    private int missingValuesFilled;
    private int totalRowsAfterCleaning;
    private String outputPath;
    private String status;
    private String message;
}
	