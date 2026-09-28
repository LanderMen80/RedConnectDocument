package com.ejemplo.fragmentos.dto;

import java.time.LocalDateTime;

/**
 * Representa una fila de fragmentos_pdf devuelta al cliente.
 * "score" tiene distinto significado según el tipo de búsqueda:
 *   - exacta:     rank de ts_rank (más alto = más relevante)
 *   - semántica:  distancia coseno/L2 de pgvector (más BAJO = más parecido)
 *   - híbrida:    score combinado normalizado (más alto = mejor)
 */
public record FragmentoResultado(
        long id,
        String ruta,
        LocalDateTime fechaModif,
        String pdfNombre,
        String contenidoTexto,
        double score
) {}
