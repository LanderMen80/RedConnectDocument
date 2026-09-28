# fragmentos-pdf-api

API en Java (Spring Boot) para consultar la tabla `fragmentos_pdf`
(PostgreSQL + pgvector), con tres modos de búsqueda:

- **Exacta**: usa la columna `texto_busqueda` (tsvector) con `plainto_tsquery`.
- **Semántica**: genera el embedding de la consulta vía Ollama y lo compara
  contra `vector_embedding` con distancia coseno (`<=>`).
- **Híbrida**: combina las dos anteriores con pesos configurables.

## Antes de correr

1. Editá `src/main/resources/application.properties`:
   - `spring.datasource.url/username/password` → tu conexión real a PostgreSQL.
   - `ollama.embedding-model` → **debe ser el mismo modelo** que usaste para
     generar `vector_embedding` al indexar los PDFs. Si usaste otro modelo,
     los vectores no van a ser comparables y los resultados de `/semantico`
     y `/hibrido` van a ser basura.
   - `busqueda.idioma-postgres` → el idioma con el que se generó `texto_busqueda`
     (normalmente `spanish`).

2. Asegurate de tener la extensión pgvector habilitada en la base:
   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

3. (Opcional pero recomendado) Índices para que las búsquedas escalen:
   ```sql
   CREATE INDEX IF NOT EXISTS idx_fragmentos_tsvector
       ON fragmentos_pdf USING GIN (texto_busqueda);

   CREATE INDEX IF NOT EXISTS idx_fragmentos_embedding
       ON fragmentos_pdf USING ivfflat (vector_embedding vector_cosine_ops)
       WITH (lists = 100);
   ```

## Correr

```bash
mvn spring-boot:run
```

Levanta en `http://localhost:8081`.

## Swagger UI

Con la app corriendo, entrá a:

```
http://localhost:8081/swagger-ui.html
```

Ahí ves los tres endpoints documentados, con ejemplos, y podés probarlos
directo desde el navegador (botón "Try it out") sin usar curl. El JSON
crudo de OpenAPI queda disponible en `http://localhost:8081/v3/api-docs`.

## Probar

```bash
# Exacta
curl "http://localhost:8081/api/fragmentos/exacto?q=contrato%20arriendo&limite=5"

# Semántica (usa Ollama para el embedding de la consulta)
curl "http://localhost:8081/api/fragmentos/semantico?q=problemas%20de%20pago&limite=5"

# Híbrida
curl "http://localhost:8081/api/fragmentos/hibrido?q=problemas%20de%20pago&limite=5&pesoTextual=0.4&pesoSemantico=0.6"
```

## Notas

- El repositorio usa `JdbcTemplate` en vez de JPA porque mapear el tipo
  `vector` de pgvector con JPA/Hibernate agrega complejidad innecesaria
  para este caso; con SQL directo + la librería `com.pgvector:pgvector`
  alcanza y es más transparente.
- Si tu columna fue pensada con distancia euclidiana en vez de coseno,
  cambiá el operador `<=>` por `<->` en `FragmentoPdfRepository`.
- Este proyecto solo hace **consultas** (no reindexación ni inserciones);
  se asume que tu proceso existente sigue siendo el que carga los PDFs
  y llena `fragmentos_pdf`.
