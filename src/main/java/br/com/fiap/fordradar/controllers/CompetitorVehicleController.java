package br.com.fiap.fordradar.controllers;

import br.com.fiap.fordradar.dtos.CompetitorVehicleRequestDTO;
import br.com.fiap.fordradar.dtos.CompetitorVehicleResponseDTO;
import br.com.fiap.fordradar.services.CompetitorVehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class CompetitorVehicleController {

    private final CompetitorVehicleService service;

    @PostMapping("/compare")
    public ResponseEntity<CompetitorVehicleResponseDTO> compareVehicle(@RequestBody @Valid CompetitorVehicleRequestDTO request) {
        CompetitorVehicleResponseDTO response = service.lookupVehicle(request);
        return ResponseEntity.ok(response);
    }
}

