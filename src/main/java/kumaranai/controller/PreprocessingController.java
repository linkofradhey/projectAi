package kumaranai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kumaranai.dto.PreprocessingConfigDTO;
import kumaranai.dto.PreprocessingResultDTO;
import kumaranai.service.PreprocessingService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // allow frontend calls
public class PreprocessingController {

    private final PreprocessingService preprocessingService;

    public PreprocessingController(PreprocessingService preprocessingService) {
        this.preprocessingService = preprocessingService;
    }

    @PostMapping("/preprocess")
    public ResponseEntity<PreprocessingResultDTO> preprocess(
            @RequestBody PreprocessingConfigDTO config) {
        try {
            PreprocessingResultDTO result = preprocessingService.process(config);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            PreprocessingResultDTO error = new PreprocessingResultDTO();
            error.setStatus("FAILED");
            error.setMessage("Unexpected error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}