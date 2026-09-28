package com.ejemplo.bridge.service;

import com.ejemplo.bridge.dto.ChatDtos.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final WebClient webClient;

    public OllamaService(@Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
    }

    /**
     * Llama al endpoint /api/chat de Ollama (no streaming, para simplificar el ejemplo).
     * Docs: https://github.com/ollama/ollama/blob/main/docs/api.md#generate-a-chat-completion
     */
    public String chat(String model, List<Message> mensajesEntrada) {

        // --- PUNTO DE ENGANCHE PARA TU RAG ---
        // Acá es donde vos:
        //   1) Tomás el último mensaje del usuario (mensajesEntrada.get(size-1))
        //   2) Generás su embedding (podés usar /api/embeddings de Ollama)
        //   3) Hacés la búsqueda por similitud en pgvector (tu tabla de fragmentos de PDF)
        //   4) Armás un mensaje "system" con el contexto recuperado y lo agregás
        //      ANTES de los mensajesEntrada.
        // Para este ejemplo de prueba, se omite y se manda tal cual.
        List<Message> mensajesFinales = new ArrayList<>(mensajesEntrada);
        // mensajesFinales.add(0, new Message("system", contextoRecuperadoDePgvector));

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", mensajesFinales,
                "stream", false
        );

        Map<?, ?> respuesta = webClient.post()
                .uri("/api/chat")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (respuesta == null || !(respuesta.get("message") instanceof Map<?, ?> mensaje)) {
            return "(sin respuesta de Ollama)";
        }
        Object contenido = mensaje.get("content");
        return contenido != null ? contenido.toString() : "";
    }

    /** Lista los modelos instalados en Ollama, para reflejarlos en /v1/models */
    public List<String> listarModelos() {
        Map<?, ?> respuesta = webClient.get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<String> nombres = new ArrayList<>();
        if (respuesta != null && respuesta.get("models") instanceof List<?> modelos) {
            for (Object m : modelos) {
                if (m instanceof Map<?, ?> mm && mm.get("name") != null) {
                    nombres.add(mm.get("name").toString());
                }
            }
        }
        return nombres;
    }
}
