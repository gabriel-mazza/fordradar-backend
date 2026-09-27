package br.com.fiap.fordradar.controllers;

import br.com.fiap.fordradar.dtos.ChangeRoleRequestDTO;
import br.com.fiap.fordradar.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestBody @Valid ChangeRoleRequestDTO request) {
        adminService.changeRole(id, request.getRole());
        return ResponseEntity.noContent().build();
    }
}
