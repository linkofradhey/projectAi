package kumaranai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kumaranai.dto.PreprocessingConfigDTO;
import kumaranai.dto.PreprocessingResultDTO;
import kumaranai.service.PreprocessingService;

//controller/PreprocessingController.java
@RestController
@RequestMapping("/api")
public class PreprocessingController {

 private final PreprocessingService service;

 public PreprocessingController(PreprocessingService service) {
     this.service = service;
 }

 @PostMapping("/preprocess")
 public ResponseEntity<PreprocessingResultDTO> preprocess(
         @RequestBody PreprocessingConfigDTO config) {

     PreprocessingResultDTO result = service.process(config);
		return ResponseEntity.ok(result);
	}
}