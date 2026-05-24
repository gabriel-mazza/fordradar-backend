package br.com.fiap.fordradar.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}