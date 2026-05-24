package br.com.fiap.fordradar.integrations;

public interface LlmIntegrationService {
    /**
     * Sends a dynamically constructed prompt to the LLM and
     * expects a raw JSON string populated with technical details.
     *
     * @param prompt The orchestrated instructions for the LLM
     * @return The extracted JSON as a string
     */
    String extractTechnicalSpec(String prompt);
}

