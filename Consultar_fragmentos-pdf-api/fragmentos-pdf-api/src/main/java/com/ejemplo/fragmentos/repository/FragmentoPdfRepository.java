package com.ejemplo.fragmentos.repository;

import com.ejemplo.fragmentos.dto.FragmentoResultado;
import com.pgvector.PGvector;

import dev.langchain4j.data.embedding.Embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class FragmentoPdfRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String idiomaPostgres;

    public FragmentoPdfRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${busqueda.idioma-postgres:spanish}") String idiomaPostgres) {
        this.jdbcTemplate = jdbcTemplate;
        this.idiomaPostgres = idiomaPostgres;
    }

    /**
     * Búsqueda EXACTA usando la columna texto_busqueda (tsvector).
     * plainto_tsquery arma la consulta a partir de texto libre del usuario.
     */
    public List<FragmentoResultado> buscarExacto(String consulta, int limite) {
        String sql = """
                SELECT id, ruta, fecha_modif, pdf_nombre, contenido_texto,
                       ts_rank(texto_busqueda, plainto_tsquery(?::regconfig, ?)) AS score
                FROM fragmentos_pdf
                WHERE texto_busqueda @@ plainto_tsquery(?::regconfig, ?)
                ORDER BY score DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, this::mapearFila,
                idiomaPostgres, consulta, idiomaPostgres, consulta, limite);
    }

    /**
     * Búsqueda CONCEPTUAL usando vector_embedding (pgvector).
     * El operador <=> es distancia coseno (requiere el índice/función correspondiente
     * en pgvector); si tu columna fue pensada para distancia euclidiana usá <-> en su lugar.
     * Se ordena ASCENDENTE porque menor distancia = más similar.
     */
    public List<FragmentoResultado> buscarSemantico(float[] embeddingConsulta, int limite) {
        String sql = """
                SELECT id, ruta, fecha_modif, pdf_nombre, contenido_texto,
                       vector_embedding <=> ? AS score
                FROM fragmentos_pdf
                ORDER BY score ASC
                LIMIT ?
                """;
        PGvector vectorParam = new PGvector(embeddingConsulta);
        return jdbcTemplate.query(sql, this::mapearFila, vectorParam, limite);
    }


     public List<FragmentoResultado> buscarSemanticoMLA(Embedding  embeddingConsulta, int limite) {
        String sql = """
                SELECT id, ruta, fecha_modif, pdf_nombre, contenido_texto,
                       vector_embedding <=> ? AS score
                FROM fragmentos_pdf
                ORDER BY score ASC
                LIMIT ?
                """;
                PGvector vectorParam = new PGvector();
                try{
                 vectorParam = new PGvector(embeddingConsulta.toString());
                }
                catch (Exception es)
                {

                }
        
        return jdbcTemplate.query(sql, this::mapearFila, vectorParam, limite);
    }



    /**
     * Búsqueda HÍBRIDA: combina rank textual y similitud semántica con pesos.
     * Normaliza cada score a 0..1 dentro del propio conjunto de resultados
     * antes de combinarlos, para que sean comparables entre sí.
     */
    public List<FragmentoResultado> buscarHibrido(String consulta, float[] embeddingConsulta,
                                                    int limite, double pesoTextual, double pesoSemantico) {
        String sql = """
                WITH textual AS (
                    SELECT id, ts_rank(texto_busqueda, plainto_tsquery(?::regconfig, ?)) AS rank_textual
                    FROM fragmentos_pdf
                    WHERE texto_busqueda @@ plainto_tsquery(?::regconfig, ?)
                ),
                semantico AS (
                    SELECT id, vector_embedding <=> ? AS distancia
                    FROM fragmentos_pdf
                )
                SELECT f.id, f.ruta, f.fecha_modif, f.pdf_nombre, f.contenido_texto,
                       (COALESCE(t.rank_textual, 0) * ?) +
                       ((1 - COALESCE(s.distancia, 1)) * ?) AS score
                FROM fragmentos_pdf f
                LEFT JOIN textual   t ON t.id = f.id
                LEFT JOIN semantico s ON s.id = f.id
                WHERE t.id IS NOT NULL OR s.distancia < 1
                ORDER BY score DESC
                LIMIT ?
                """;
        PGvector vectorParam = new PGvector(embeddingConsulta);
        return jdbcTemplate.query(sql, this::mapearFila,
                idiomaPostgres, consulta, idiomaPostgres, consulta,
                vectorParam, pesoTextual, pesoSemantico, limite);
    }

    private FragmentoResultado mapearFila(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp ts = rs.getTimestamp("fecha_modif");
        return new FragmentoResultado(
                rs.getLong("id"),
                rs.getString("ruta"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("pdf_nombre"),
                rs.getString("contenido_texto"),
                rs.getDouble("score")
        );
    }

    

    public List<FragmentoResultado> buscarHibridoMLA(String consulta, Embedding embeddingConsulta,
                                                    int limite, double pesoTextual, double pesoSemantico) {
        String sql = """
                WITH textual AS (
                    SELECT id, ts_rank(texto_busqueda, plainto_tsquery(?::regconfig, ?)) AS rank_textual
                    FROM fragmentos_pdf
                    WHERE texto_busqueda @@ plainto_tsquery(?::regconfig, ?)
                ),
                semantico AS (
                    SELECT id, vector_embedding <=> ? AS distancia
                    FROM fragmentos_pdf
                )
                SELECT f.id, f.ruta, f.fecha_modif, f.pdf_nombre, f.contenido_texto,
                       (COALESCE(t.rank_textual, 0) * ?) +
                       ((1 - COALESCE(s.distancia, 1)) * ?) AS score
                FROM fragmentos_pdf f
                LEFT JOIN textual   t ON t.id = f.id
                LEFT JOIN semantico s ON s.id = f.id
                WHERE t.id IS NOT NULL OR s.distancia < 1
                ORDER BY score DESC
                LIMIT ?
                """;
        //PGvector vectorParam = new PGvector(embeddingConsulta);
        PGvector vectorParam = new PGvector();
                try{
                 vectorParam = new PGvector(embeddingConsulta.toString());
                }
                catch (Exception es)
                {

                }
        return jdbcTemplate.query(sql, this::mapearFila,
                idiomaPostgres, consulta, idiomaPostgres, consulta,
                vectorParam, pesoTextual, pesoSemantico, limite);
    }



     public List<FragmentoResultado> buscarHibridoMLA2(String consulta, Embedding embeddingConsulta,
                                                    int limite, double pesoTextual, double pesoSemantico) {
        String sql = """
                WITH textual AS (
                    SELECT id, ts_rank(texto_busqueda, plainto_tsquery(?::regconfig, ?)) AS rank_textual
                    FROM fragmentos2_pdf
                    WHERE texto_busqueda @@ plainto_tsquery(?::regconfig, ?)
                ),
                semantico AS (
                    SELECT id, vector_embedding <=> ? AS distancia
                    FROM fragmentos2_pdf
                )
                SELECT f.id, f.ruta, f.fecha_modif, f.pdf_nombre, f.contenido_texto,
                       (COALESCE(t.rank_textual, 0) * ?) +
                       ((1 - COALESCE(s.distancia, 1)) * ?) AS score
                FROM fragmentos2_pdf f
                LEFT JOIN textual   t ON t.id = f.id
                LEFT JOIN semantico s ON s.id = f.id
                WHERE t.id IS NOT NULL OR s.distancia < 1
                ORDER BY score DESC
                LIMIT ?
                """;
        //PGvector vectorParam = new PGvector(embeddingConsulta);
        PGvector vectorParam = new PGvector();
                try{
                 vectorParam = new PGvector(embeddingConsulta.toString());
                }
                catch (Exception es)
                {

                }
        return jdbcTemplate.query(sql, this::mapearFila,
                idiomaPostgres, consulta, idiomaPostgres, consulta,
                vectorParam, pesoTextual, pesoSemantico, limite);
    }

}
