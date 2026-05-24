package br.com.fiap.fordradar.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorVehicleResponseDTO {
    private Long id;
    private String brand;
    private String model;
    private String version;
    private String technicalSpec;
    private LocalDateTime createdAt;
}

