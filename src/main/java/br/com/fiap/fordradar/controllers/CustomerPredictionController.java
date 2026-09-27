package br.com.fiap.fordradar.controllers;

import br.com.fiap.fordradar.dtos.CustomerPredictionRequestDTO;
import br.com.fiap.fordradar.exceptions.BusinessRuleException;
import br.com.fiap.fordradar.dtos.CustomerPredictionResponseDTO;
import br.com.fiap.fordradar.services.CustomerPredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class CustomerPredictionController {

    private final CustomerPredictionService service;

    @PostMapping
    public ResponseEntity<CustomerPredictionResponseDTO> savePrediction(@RequestBody @Valid CustomerPredictionRequestDTO request) {
        CustomerPredictionResponseDTO response = service.savePrediction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{vin}")
    public ResponseEntity<CustomerPredictionResponseDTO> getPredictionByVin(@PathVariable String vin) {
        if (!vin.matches("^[A-HJ-NPR-Za-hj-npr-z0-9]{17}$")) {
            throw new BusinessRuleException("Invalid VIN format");
        }
        CustomerPredictionResponseDTO response = service.findByVin(vin);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerPredictionResponseDTO>> listPredictions(
            @PageableDefault(size = 10, sort = "retentionScore") Pageable pageable) {
        Pageable safe = org.springframework.data.domain.PageRequest.of(
                Math.max(pageable.getPageNumber(), 0), Math.min(pageable.getPageSize(), 50), pageable.getSort());
        Page<CustomerPredictionResponseDTO> response = service.findAll(safe);
        return ResponseEntity.ok(response);
    }
}

