-- En el notebook activas la extensión
CREATE EXTENSION IF NOT EXISTS vector;

drop table fragmentos_pdf;

CREATE TABLE fragmentos_pdf (
    id SERIAL PRIMARY KEY,
    ruta text,
    pdf_nombre TEXT,    
    contenido_texto TEXT,                    -- <--- Para mostrar al usuario
    texto_busqueda tsvector,                -- <--- Para Búsqueda EXACТА
    vector_embedding vector(384)           -- <--- Para Búsqueda CONCEPTUAL (Ej: OpenAI)
);

-- Creamos los índices para que sea rápido en tu laptop
CREATE INDEX idx_texto_exacto ON fragmentos_pdf USING gin(texto_busqueda);
CREATE INDEX idx_vector_conceptual ON fragmentos_pdf USING hnsw (vector_embedding vector_cosine_ops);


create table archivo_huella
( id_ref SERIAL PRIMARY KEY,
  ruta text,
  hash_contenido  varchar(64),
  fecha_indexacion date 
  
) ;

create index archivo_huella_idx on archivo_huella(hash_contenido);
