# ollama-openai-bridge

API en Java (Spring Boot) que expone un endpoint compatible con OpenAI
(`/v1/chat/completions`, `/v1/models`) para poder configurarlo como
"Connection" personalizada en Open WebUI, mientras por debajo llama a
tu instancia local de Ollama.

## Cómo correrlo

```bash
mvn spring-boot:run
```

Por defecto levanta en `http://localhost:8080` y asume Ollama en
`http://localhost:11434` (configurable en `application.properties`).

## Probarlo directo con curl

```bash
curl http://localhost:8080/v1/models

curl http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3",
    "messages": [{"role": "user", "content": "Hola, ¿quién sos?"}]
  }'


curl http://localhost:8080/v1/chat/completions  -H "Content-Type: application/json"  -d '{ "model": "llama3",   "messages": [{"role": "user", "content": "Hola, ¿quién sos?"}]}'

```

## Configurarlo en Open WebUI

1. Entrá a **Settings > Connections**.
2. Agregá una nueva conexión tipo **OpenAI API**.
3. URL: `http://localhost:8080/v1`
4. API Key: cualquier texto no vacío (este ejemplo no valida la key).
5. Guardá y elegí el modelo listado (viene de `GET /api/tags` de Ollama).
6. Probá una conversación normal desde la interfaz de Open WebUI: el
   mensaje va a Open WebUI → tu API Java → Ollama → y la respuesta
   vuelve por el mismo camino.

## Dónde enganchar tu RAG con pgvector

Todo el punto de extensión está marcado en
`OllamaService.chat(...)`: ahí es donde tenés que buscar el fragmento
de PDF más relevante en pgvector según el último mensaje del usuario,
armar un mensaje `system` con ese contexto, y agregarlo antes de
mandarle la lista de mensajes a Ollama. Así el "caso de prueba" que
armes en Open WebUI ya estaría ejercitando tu pipeline completo:
Open WebUI → esta API → búsqueda en pgvector → Ollama → respuesta.
