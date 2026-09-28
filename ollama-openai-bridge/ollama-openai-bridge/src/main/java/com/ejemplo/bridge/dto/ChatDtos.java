package com.ejemplo.bridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTOs mínimos que replican el contrato de la API de OpenAI
 * (https://platform.openai.com/docs/api-reference/chat)
 * lo suficiente como para que Open WebUI los entienda.
 */
public class ChatDtos {

    public record Message(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletionRequest(
            String model,
            List<Message> messages,
            Boolean stream,
            Double temperature
    ) {}

    public record ChatCompletionResponse(
            String id,
            String object,
            long created,
            String model,
            List<Choice> choices
    ) {}

    public record Choice(
            int index,
            Message message,
            String finish_reason
    ) {}

    public record ModelInfo(String id, String object, long created, String owned_by) {}

    public record ModelList(String object, List<ModelInfo> data) {}
}
