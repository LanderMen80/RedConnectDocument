package com.ejemplo.fragmentos.controller;

import com.ejemplo.fragmentos.dto.FragmentoResultado;
import com.ejemplo.fragmentos.repository.FragmentoPdfRepository;
import com.ejemplo.fragmentos.service.EmbeddingService;

import dev.langchain4j.data.embedding.Embedding;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import dev.langchain4j.data.embedding.Embedding;

import java.util.List;

@RestController
@RequestMapping("/api/fragmentos")
@Tag(name = "Fragmentos PDF", description = "Búsqueda sobre la tabla fragmentos_pdf (exacta, semántica e híbrida)")
public class FragmentoPdfController {

    private final FragmentoPdfRepository repository;
    private final EmbeddingService embeddingService;

    public FragmentoPdfController(FragmentoPdfRepository repository, EmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    @GetMapping("/exacto")
    @Operation(summary = "Búsqueda exacta", description = "Busca por palabras clave usando la columna texto_busqueda (tsvector).")
    public List<FragmentoResultado> buscarExacto(
            @Parameter(description = "Texto de búsqueda", example = "contrato arriendo") @RequestParam String q,
            @Parameter(description = "Cantidad máxima de resultados") @RequestParam(defaultValue = "10") int limite) {
        return repository.buscarExacto(q, limite);
    }

    @GetMapping("/semantico")
    @Operation(summary = "Búsqueda semántica", description = "Genera el embedding de la consulta con Ollama y busca por similitud conceptual en vector_embedding (pgvector).")
    public List<FragmentoResultado> buscarSemantico(
            @Parameter(description = "Texto de búsqueda", example = "problemas de pago") @RequestParam String q,
            @Parameter(description = "Cantidad máxima de resultados") @RequestParam(defaultValue = "10") int limite) {
        float[] embedding = embeddingService.generarEmbedding(q);
        return repository.buscarSemantico(embedding, limite);
    }

    ////// 
    /// ////// 
    /// ////// 
    /// 
    @GetMapping("/semanticoMLA")
    @Operation(summary = "Búsqueda semántica", description = "Genera el embedding de la consulta con Ollama y busca por similitud conceptual en vector_embedding (pgvector).")
    public List<FragmentoResultado> buscarSemanticoMLA(
            @Parameter(description = "Texto de búsqueda", example = "problemas de pago") @RequestParam String q,
            @Parameter(description = "Cantidad máxima de resultados") @RequestParam(defaultValue = "10") int limite) {
        Embedding embedding = embeddingService.generarMiEmbeding(q);
        return repository.buscarSemanticoMLA(embedding, limite);
    }
    ////// 
    /// 
    /// 
    /// 
    /// 

    @GetMapping("/hibrido")
    @Operation(summary = "Búsqueda híbrida", description = "Combina el rank textual (tsvector) y la similitud semántica (pgvector) con pesos configurables.")
    public List<FragmentoResultado> buscarHibrido(
            @Parameter(description = "Texto de búsqueda", example = "problemas de pago") @RequestParam String q,
            @Parameter(description = "Cantidad máxima de resultados") @RequestParam(defaultValue = "10") int limite,
            @Parameter(description = "Peso relativo del componente textual") @RequestParam(defaultValue = "0.5") double pesoTextual,
            @Parameter(description = "Peso relativo del componente semántico") @RequestParam(defaultValue = "0.5") double pesoSemantico) {
        float[] embedding = embeddingService.generarEmbedding(q);
        return repository.buscarHibrido(q, embedding, limite, pesoTextual, pesoSemantico);
    }


    @GetMapping("/hibridoMLA")
    @Operation(summary = "Búsqueda híbrida", description = "Combina el rank textual (tsvector) y la similitud semántica (pgvector) con pesos configurables.")
    public List<FragmentoResultado> buscarHibridoMLA(
            @Parameter(description = "Texto de búsqueda", example = "problemas de pago") @RequestParam String q,
            @Parameter(description = "Cantidad máxima de resultados") @RequestParam(defaultValue = "10") int limite,
            @Parameter(description = "Peso relativo del componente textual") @RequestParam(defaultValue = "0.5") double pesoTextual,
            @Parameter(description = "Peso relativo del componente semántico") @RequestParam(defaultValue = "0.5") double pesoSemantico) {
        Embedding embedding =  embeddingService.generarMiEmbeding(q);
        return repository.buscarHibridoMLA2(q, embedding, limite, pesoTextual, pesoSemantico);
    }
}
