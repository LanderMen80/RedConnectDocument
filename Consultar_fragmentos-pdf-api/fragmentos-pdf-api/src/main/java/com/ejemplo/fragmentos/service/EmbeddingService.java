package com.ejemplo.fragmentos.service;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final String modeloEmbedding;

    public EmbeddingService(
            @Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${ollama.embedding-model:all-minilm}") String modeloEmbedding) {
        this.webClient = WebClient.builder().baseUrl(ollamaBaseUrl).build();
        this.modeloEmbedding = modeloEmbedding;
    }

    /**
     * Llama a POST /api/embeddings de Ollama y devuelve el vector como float[].
     * Docs: https://github.com/ollama/ollama/blob/main/docs/api.md#generate-embeddings
     */
    @SuppressWarnings("unchecked")
    public float[] generarEmbedding(String texto) {
        Map<String, Object> body = Map.of(
                "model", modeloEmbedding,
                "prompt", texto
        );
        
        

        Map<?, ?> respuesta = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (respuesta == null || !(respuesta.get("embedding") instanceof List<?> lista)) {
            throw new IllegalStateException("Ollama no devolvió un embedding válido para el modelo " + modeloEmbedding);
        }

        float[] vector = new float[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            vector[i] = ((Number) lista.get(i)).floatValue();
        }
        return vector;
    }



    public Embedding generarMiEmbeding(String texto)
    {
        EmbeddingModel modeloEmbedding = new AllMiniLmL6V2EmbeddingModel();
        Embedding embedding = modeloEmbedding.embed(texto).content();

        

        return embedding;

    } 
}
