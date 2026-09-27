package br.com.fiap.fordradar.integrations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class GeminiIntegrationService implements LlmIntegrationService {

    private final WebClient webClient;

    @Value("${llm.api-key}")
    private String apiKey;

    public GeminiIntegrationService(WebClient.Builder webClientBuilder, @Value("${llm.url}") String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    @Override
    public String extractTechnicalSpec(String prompt) {
        try {
            GeminiRequest requestBody = buildRequestBody(prompt);

            GeminiResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Failed to extract data from Gemini LLM: {}", e.getMessage());
            return """
            {
               "engine": "empty / not available",
               "power": "empty / not available",
               "torque": "empty / not available",
               "transmission": "empty / not available",
               "payloadCapacity": "empty / not available"
            }
            """;
        }
    }

    private GeminiRequest buildRequestBody(String prompt) {
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
        return new GeminiRequest(List.of(content));
    }

    private String extractTextFromResponse(GeminiResponse response) {
        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
            return response.candidates().get(0).content().parts().get(0).text();
        }
        throw new IllegalStateException("Empty or invalid response from Gemini API");
    }
}

