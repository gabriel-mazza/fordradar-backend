package br.com.fiap.fordradar.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPredictionRequestDTO {

    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    @Size(max = 150, message = "Customer name must not exceed 150 characters")
    private String customerName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotNull(message = "Retention score cannot be null")
    @DecimalMin(value = "0.0", message = "Minimum score is 0.0")
    @DecimalMax(value = "100.0", message = "Maximum score is 100.0")
    private BigDecimal retentionScore;
}

