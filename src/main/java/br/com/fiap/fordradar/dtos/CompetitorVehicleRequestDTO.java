package br.com.fiap.fordradar.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorVehicleRequestDTO {

    @NotBlank(message = "Brand cannot be blank")
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 \\-]+$", message = "Brand contains invalid characters")
    private String brand;

    @NotBlank(message = "Model cannot be blank")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 \\-]+$", message = "Model contains invalid characters")
    private String model;

    @NotBlank(message = "Version cannot be blank")
    @Size(max = 100, message = "Version must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 \\-]+$", message = "Version contains invalid characters")
    private String version;

    @Size(max = 20, message = "Too many attributes requested")
    private List<String> targetAttributes;

    public List<String> getSanitizedAttributes() {
        if (targetAttributes == null) return null;
        return targetAttributes.stream()
                .filter(attr -> attr != null && !attr.isBlank())
                .filter(attr -> attr.length() <= 50)
                .filter(attr -> attr.matches("^[a-zA-ZÀ-ÿ0-9 \\-_]+$"))
                .toList();
    }
}