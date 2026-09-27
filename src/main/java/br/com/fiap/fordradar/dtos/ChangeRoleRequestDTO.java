package br.com.fiap.fordradar.dtos;

import br.com.fiap.fordradar.models.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequestDTO {

    @NotNull
    private Role role;
}
