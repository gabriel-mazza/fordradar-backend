package br.com.fiap.fordradar.services;

import br.com.fiap.fordradar.dtos.CompetitorVehicleRequestDTO;
import br.com.fiap.fordradar.dtos.CompetitorVehicleResponseDTO;
import br.com.fiap.fordradar.integrations.LlmIntegrationService;
import br.com.fiap.fordradar.models.CompetitorVehicle;
import br.com.fiap.fordradar.repositories.CompetitorVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorVehicleService {

    private final CompetitorVehicleRepository repository;
    private final LlmIntegrationService llmIntegrationService;

    @Transactional
    public CompetitorVehicleResponseDTO lookupVehicle(CompetitorVehicleRequestDTO request) {
        log.info("Looking up competitor vehicle: {} {} {}", request.getBrand(), request.getModel(), request.getVersion());

        Optional<CompetitorVehicle> cached = repository.findByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCase(
                request.getBrand(), request.getModel(), request.getVersion());

        if (cached.isPresent()) {
            log.info("Vehicle found in database (Cache Hit)");
            return mapToDTO(cached.get());
        }

        log.info("Vehicle not found. Orchestrating AI call via Gemini...");

        String prompt = buildPrompt(request);
        String iaResponseJson = llmIntegrationService.extractTechnicalSpec(prompt);

        String cleanJson = iaResponseJson.replace("```json", "").replace("```", "").trim();

        CompetitorVehicle entity = CompetitorVehicle.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .version(request.getVersion())
                .technicalSpec(cleanJson)
                .build();

        CompetitorVehicle saved = repository.save(entity);

        return mapToDTO(saved);
    }

    private String buildPrompt(CompetitorVehicleRequestDTO request) {
        StringBuilder prompt = new StringBuilder("Você é um analista automotivo expert. ");
        prompt.append("Extraia a ficha técnica do veículo: Marca '").append(request.getBrand())
                .append("', Modelo '").append(request.getModel())
                .append("', Versão '").append(request.getVersion()).append("'. ");

        prompt.append("Retorne ESTRITAMENTE em formato JSON válido. Não adicione texto explicativo, markdown ou blocos de código. ");
        prompt.append("IMPORTANTE: Os valores do JSON devem ser SEMPRE strings simples, nunca objetos, arrays ou JSON aninhado. ");
        prompt.append("Se o dado não existir ou não for encontrado, preencha o campo com a string \"empty / not available\". ");
        prompt.append("Use as chaves do JSON EXATAMENTE como listadas abaixo, sem alterar os nomes. ");

        if (request.getTargetAttributes() != null && !request.getTargetAttributes().isEmpty()) {
            prompt.append("As chaves do JSON devem ser obrigatoriamente: ").append(String.join(", ", request.getTargetAttributes()));
            prompt.append(". Não invente outras chaves além dessas.");
        } else {
            prompt.append("As chaves do JSON devem ser obrigatoriamente: motor, potência, torque, transmissão, capacidade de carga.");
        }

        return prompt.toString();
    }

    private CompetitorVehicleResponseDTO mapToDTO(CompetitorVehicle entity) {
        return CompetitorVehicleResponseDTO.builder()
                .id(entity.getId())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .version(entity.getVersion())
                .technicalSpec(entity.getTechnicalSpec())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}