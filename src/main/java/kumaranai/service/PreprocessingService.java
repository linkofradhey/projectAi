package kumaranai.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import kumaranai.PreProcessing.CategoricalEncoder;
import kumaranai.PreProcessing.DuplicateRemover;
import kumaranai.PreProcessing.MissingValueHandler;
import kumaranai.PreProcessing.NumericalNormalizer;
import kumaranai.dto.PreprocessingConfigDTO;
import kumaranai.dto.PreprocessingResultDTO;
import kumaranai.io.DataLoader;
import kumaranai.io.DataWriter;
import kumaranai.model.DataRecord;

@Service
public class PreprocessingService {

    private final DataLoader dataLoader;
    private final DataWriter dataWriter;
    private final MissingValueHandler missingValueHandler;
    private final DuplicateRemover duplicateRemover;
    private final CategoricalEncoder categoricalEncoder;
    private final NumericalNormalizer numericalNormalizer;

    public PreprocessingService(DataLoader dataLoader, DataWriter dataWriter,
            MissingValueHandler missingValueHandler, DuplicateRemover duplicateRemover,
            CategoricalEncoder categoricalEncoder, NumericalNormalizer numericalNormalizer) {

        this.dataLoader = dataLoader;
        this.dataWriter = dataWriter;
        this.missingValueHandler = missingValueHandler;
        this.duplicateRemover = duplicateRemover;
        this.categoricalEncoder = categoricalEncoder;
        this.numericalNormalizer = numericalNormalizer;
    }

    public PreprocessingResultDTO process(PreprocessingConfigDTO config) {
        try {
            // STEP 1: Load raw data
            List<DataRecord> records = dataLoader.load(config.getInputFilePath());
            int totalLoaded = records.size();

            // STEP 2: Handle missing values
            int missingFilled = missingValueHandler.handle(records, config.getMissingValueStrategy());

            // STEP 3: reassign records after duplicate removal
            int beforeDedup = records.size();
            if (config.isRemoveDuplicates()) {
                records = duplicateRemover.remove(records); 
            }
            int duplicatesRemovedCount = beforeDedup - records.size();

            // STEP 4: Encode categorical columns (mutates records in-place)
            records =categoricalEncoder.encode(records, config.getCategoricalColumns(),config.getEncodingType());

            // STEP 5: Normalize numeric columns (mutates records in-place)
            records =numericalNormalizer.normalize(records, config.getNumericColumns());

            // STEP 6: Write fully cleaned output ✅
            dataWriter.write(records, config.getOutputFilePath());

            // STEP 7: Build result
            PreprocessingResultDTO result = new PreprocessingResultDTO();
            result.setTotalRowsLoaded(totalLoaded);
            result.setMissingValuesFilled(missingFilled);
            result.setDuplicatesRemoved(duplicatesRemovedCount); // ✅ now an int count
            result.setTotalRowsAfterCleaning(records.size());
            result.setOutputPath(config.getOutputFilePath());
            result.setStatus("SUCCESS");
            result.setMessage("Preprocessing completed successfully.");

            return result;

        } catch (IOException e) {
            e.printStackTrace();

            // ✅ Return a meaningful error result instead of empty DTO
            PreprocessingResultDTO errorResult = new PreprocessingResultDTO();
            errorResult.setStatus("FAILED");
            errorResult.setMessage("Preprocessing failed: " + e.getMessage());
            return errorResult;
        }
    }
}