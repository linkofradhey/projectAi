package kumaranai.service;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import kumaranai.PreProcessing.AgeBarChartGenerator;
import kumaranai.PreProcessing.AgeChartGenerator;
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

	public PreprocessingService(DataLoader dataLoader, DataWriter dataWriter, MissingValueHandler missingValueHandler,
			DuplicateRemover duplicateRemover, CategoricalEncoder categoricalEncoder,
			NumericalNormalizer numericalNormalizer) {
		this.dataLoader = dataLoader;
		this.dataWriter = dataWriter;
		this.missingValueHandler = missingValueHandler;
		this.duplicateRemover = duplicateRemover;
		this.categoricalEncoder = categoricalEncoder;
		this.numericalNormalizer = numericalNormalizer;
	}

	public PreprocessingResultDTO process(PreprocessingConfigDTO config) throws Exception {
		try {
			// STEP 1: Load raw data (separate copy for chart — before any mutation)
			List<DataRecord> records = dataLoader.load(config.getInputFilePath());
			List<DataRecord> pieRecords = dataLoader.load(config.getInputFilePath()); // ← fresh copy
			List<DataRecord> pieRecords1 = dataLoader.load(config.getInputFilePath()); // ← fresh copy

			int totalLoaded = records.size();

			// STEP 2: Handle missing values
			int missingFilled = missingValueHandler.handle(records, config.getMissingValueStrategy());

			// STEP 3: Remove duplicates
			int beforeDedup = records.size();
			if (config.isRemoveDuplicates()) {
				records = duplicateRemover.remove(records);
			}
			int duplicatesRemovedCount = beforeDedup - records.size();
			pieRecords1=records;
			pieRecords= records;//mapping to get missing values and duplicate in piechart
			// STEP 4: Encode categorical columns
			records = categoricalEncoder.encode(records, config.getCategoricalColumns(), config.getEncodingType());
//till here correct
			// ── STEP 6: Build chart data from the ORIGINAL raw copy ──────────
			List<String> ageColumnList = List.of("age");

			// 6a. Count raw age values — nulls and blanks excluded ✅
			Map<String, Integer> valueCounts = new LinkedHashMap<>();
			for (DataRecord r : pieRecords) {
				String val = r.getField("age");
				if (val != null && !val.isBlank()) {
					valueCounts.put(val, valueCounts.getOrDefault(val, 0) + 1);//we get valuecount
				}
			}

			// 6b. Encode the raw copy to get label → integer mapping
			List<DataRecord> encodedPieRecords = categoricalEncoder.encode(pieRecords1, ageColumnList, "label");

			// 6c. Build encodingMap — strictly exclude missing/blank age values
			Map<String, Integer> encodingMap = new LinkedHashMap<>();
			for (int i = 0; i < pieRecords1.size(); i++) {
				String originalAge = pieRecords1.get(i).getField("age");
				String encodedVal = encodedPieRecords.get(i).getField("age");

				if (originalAge == null || originalAge.isBlank())
					continue; // ✅ exclude missing
				if (encodedVal == null || encodedVal.isBlank())
					continue; // ✅ exclude bad encode
				if (!valueCounts.containsKey(originalAge))
					continue; // ✅ keep maps in sync

				try {
					encodingMap.putIfAbsent(originalAge, Integer.parseInt(encodedVal));
				} catch (NumberFormatException e) {
					System.err.println(
							"⚠️ Skipping malformed encoded value for age='" + originalAge + "': " + encodedVal);
				}
			}

			// 6d. Generate chart only if valid data exists
			String pieChartBase64 = null;
			if (!valueCounts.isEmpty() && !encodingMap.isEmpty()) {
				AgeChartGenerator chartGen = new AgeChartGenerator();
				byte[] pieChartBytes = chartGen.generatePieChart(valueCounts, encodingMap);
				pieChartBase64 = Base64.getEncoder().encodeToString(pieChartBytes);
			} else {
				System.out.println("ℹ️ Skipping pie chart — no valid age data found.");
			}
			


			// ── NEW: Bar chart generation (add right below) ──────────────
			String barChartBase64 = null;
			if (!valueCounts.isEmpty() && !encodingMap.isEmpty()) {
			    AgeBarChartGenerator barChartGen = new AgeBarChartGenerator();
			    byte[] barChartBytes = barChartGen.generateBarChart(valueCounts, encodingMap);
			    barChartBase64 = Base64.getEncoder().encodeToString(barChartBytes);
			} else {
			    System.out.println("ℹ️ Skipping bar chart — no valid age data found.");
			}

			// STEP 5: Normalize numeric columns
			records = numericalNormalizer.normalize(records, config.getNumericalColumns(),config.getNormalizationType());
//we have added in the last because it normalize the value and that value is less than 1 so we are going to add in last
			// STEP 7: Write cleaned output
			dataWriter.write(records, config.getOutputFilePath());
			// STEP 8: Build and return result
			PreprocessingResultDTO result = new PreprocessingResultDTO();
			result.setTotalRowsLoaded(totalLoaded);
			result.setMissingValuesFilled(missingFilled);
			result.setDuplicatesRemoved(duplicatesRemovedCount);
			result.setTotalRowsAfterCleaning(records.size());
			result.setOutputPath(config.getOutputFilePath());
			result.setPieChartBase64(pieChartBase64); 
			result.setBarChartBase64(barChartBase64);     

			result.setStatus("SUCCESS");
			result.setMessage("Preprocessing completed successfully.");

			return result;

		} catch (IOException e) {
			e.printStackTrace();
			PreprocessingResultDTO errorResult = new PreprocessingResultDTO();
			errorResult.setStatus("FAILED");
			errorResult.setMessage("Preprocessing failed: " + e.getMessage());
			return errorResult;
		}
	}
}