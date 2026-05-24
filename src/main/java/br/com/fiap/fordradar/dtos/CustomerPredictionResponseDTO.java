package br.com.fiap.fordradar.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPredictionResponseDTO {
    private Long id;
    private String vin;
    private String customerName;
    private String customerEmail;
    private String phone;
    private BigDecimal retentionScore;
    private LocalDateTime predictionDate;
}

