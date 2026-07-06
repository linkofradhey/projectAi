package kumaranai.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import kumaranai.PreProcessing.CategoricalEncoder;
import kumaranai.PreProcessing.DuplicateRemover;
import kumaranai.PreProcessing.MissingValueHandler;
import kumaranai.PreProcessing.MissingValueHandler.Strategy;
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

	public PreprocessingService(DataLoader dataLoader, DataWriter dataWriter, MissingValueHandler missingValueHandler,
			DuplicateRemover duplicateRemover, CategoricalEncoder categoricalEncoder,
			NumericalNormalizer numericalNormalizer) {//constructor to intialize the value of the tools 

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

			// STEP 2: Handle missing values ✅ int type + Enum arg
			int missingFilled = missingValueHandler.handle(records, config.getMissingValueStrategy());

			// STEP 3: Remove duplicates
			List<DataRecord> duplicatesRemoved = null;
			if (config.isRemoveDuplicates()) {
				duplicatesRemoved = duplicateRemover.remove(records);
			}

			// STEP 4: Encode categorical columns
			categoricalEncoder.encode(records, config.getCategoricalColumns());

			// STEP 5: Normalize numeric columns
			numericalNormalizer.normalize(records, config.getNumericColumns());

			// STEP 6: Write output

			dataWriter.write(records, config.getOutputFilePath());

			// STEP 7: Build result
			PreprocessingResultDTO result = new PreprocessingResultDTO();
			result.setTotalRowsLoaded(totalLoaded);
			result.setDuplicatesRemoved(duplicatesRemoved);
			result.setMissingValuesFilled(missingFilled);
			result.setTotalRowsAfterCleaning(records.size());
			result.setOutputPath(config.getOutputFilePath());
			result.setStatus("SUCCESS");
			result.setMessage("Preprocessing completed successfully.");

			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			PreprocessingResultDTO result = new PreprocessingResultDTO();

			e.printStackTrace();
			return result;
		}
		
	}
}