package com.ejemplo.bridge.controller;

import com.ejemplo.bridge.dto.ChatDtos.*;
import com.ejemplo.bridge.service.OllamaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Expone el contrato de la API de OpenAI bajo /v1, que es lo que
 * Open WebUI espera al configurar una "Connection" de tipo OpenAI API.
 *
 * En Open WebUI: Settings > Connections > agregar
 *   URL:     http://localhost:8080/v1
 *   API Key: cualquier valor no vacío (ej: "test"), no se valida en este ejemplo
 */
@RestController
@RequestMapping("/v1")
public class ChatController {

    private final OllamaService ollamaService;

    public ChatController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @GetMapping("/models")
    public ModelList listarModelos() {
        List<ModelInfo> modelos = ollamaService.listarModelos().stream()
                .map(nombre -> new ModelInfo(nombre, "model", System.currentTimeMillis() / 1000, "ollama-local"))
                .toList();
        return new ModelList("list", modelos);
    }

    @PostMapping("/chat/completions")
    public ChatCompletionResponse completar(@RequestBody ChatCompletionRequest request) {
        String modelo = request.model() != null ? request.model() : "llama3";
        String respuestaTexto = ollamaService.chat(modelo, request.messages());

        Message mensajeRespuesta = new Message("assistant", respuestaTexto);
        Choice choice = new Choice(0, mensajeRespuesta, "stop");

        return new ChatCompletionResponse(
                "chatcmpl-" + UUID.randomUUID(),
                "chat.completion",
                System.currentTimeMillis() / 1000,
                modelo,
                List.of(choice)
        );
    }
}
