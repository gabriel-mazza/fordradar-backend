package br.com.fiap.fordradar.services;

import br.com.fiap.fordradar.dtos.CustomerPredictionRequestDTO;
import br.com.fiap.fordradar.dtos.CustomerPredictionResponseDTO;
import br.com.fiap.fordradar.exceptions.ResourceNotFoundException;
import br.com.fiap.fordradar.models.CustomerPrediction;
import br.com.fiap.fordradar.repositories.CustomerPredictionRepository;
import br.com.fiap.fordradar.security.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerPredictionService {

    private final CustomerPredictionRepository repository;

    @Transactional
    public CustomerPredictionResponseDTO savePrediction(CustomerPredictionRequestDTO request) {
        AuditLog.event("PREDICTION_UPSERT", "SUCCESS", "actor", AuditLog.currentActor(), "vin", AuditLog.maskVin(request.getVin()));

        CustomerPrediction entity = repository.findByVin(request.getVin())
                .orElse(new CustomerPrediction());

        entity.setVin(request.getVin());
        entity.setCustomerName(request.getCustomerName());
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setPhone(request.getPhone());
        entity.setRetentionScore(request.getRetentionScore());

        CustomerPrediction saved = repository.save(entity);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public CustomerPredictionResponseDTO findByVin(String vin) {
        AuditLog.event("PII_READ", "SUCCESS", "actor", AuditLog.currentActor(), "vin", AuditLog.maskVin(vin));
        CustomerPrediction entity = repository.findByVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException("No prediction found for VIN: " + vin));
        return mapToDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<CustomerPredictionResponseDTO> findAll(Pageable pageable) {
        AuditLog.event("PII_LIST", "SUCCESS", "actor", AuditLog.currentActor(), "page", pageable.getPageNumber(), "size", pageable.getPageSize());
        return repository.findAll(pageable).map(this::mapToDTO);
    }

    private CustomerPredictionResponseDTO mapToDTO(CustomerPrediction entity) {
        return CustomerPredictionResponseDTO.builder()
                .id(entity.getId())
                .vin(entity.getVin())
                .customerName(entity.getCustomerName())
                .customerEmail(entity.getCustomerEmail())
                .phone(entity.getPhone())
                .retentionScore(entity.getRetentionScore())
                .predictionDate(entity.getPredictionDate())
                .build();
    }
}

