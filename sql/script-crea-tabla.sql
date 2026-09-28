-- En el notebook activas la extensión
CREATE EXTENSION IF NOT EXISTS vector;

drop table fragmentos_pdf;

ALTER TABLE public.fragmentos_pdf ALTER COLUMN fecha_modif TYPE timestamp USING fecha_modif::timestamp;

CREATE TABLE fragmentos_pdf (
    id SERIAL PRIMARY KEY,
    ruta text,	
    fecha_modif timestamp,
    pdf_nombre TEXT,    
    contenido_texto TEXT,                    -- <--- Para mostrar al usuario
    texto_busqueda tsvector,                -- <--- Para Búsqueda EXACТА
    vector_embedding vector(384)           -- <--- Para Búsqueda CONCEPTUAL (Ej: OpenAI)
    
);

-- Creamos los índices para que sea rápido en tu laptop
CREATE INDEX idx_texto_exacto ON fragmentos_pdf USING gin(texto_busqueda);
CREATE INDEX idx_vector_conceptual ON fragmentos_pdf USING hnsw (vector_embedding vector_cosine_ops);


create table archivo_huella
( 
	id_ref SERIAL PRIMARY KEY,
	ruta text,
	hash_contenido  varchar(64),
	fecha_indexacion date  
) ;

create index archivo_huella_idx on archivo_huella(hash_contenido);

select * from archivo_huella;

select count(distinct pdf_nombre) as cantidad
	--, ruta 
	from fragmentos_pdf 
--group by ruta;
	;

SELECT pg_size_pretty(pg_table_size('fragmentos_pdf'));


SELECT 
FROM fragmentos_pdf
order by fecha_modif



SELECT id, contenido_texto
FROM fragmentos_pdf
WHERE texto_busqueda @@ to_tsquery('spanish', 'Plan Comunicacional Fase 2A')
LIMIT 5;


SELECT --id , --contenido_texto
--	count(*)
  distinct ruta, pdf_nombre 
FROM fragmentos_pdf w
WHERE to_tsvector('spanish', contenido_texto) @@ plainto_tsquery('spanish', 'EXPROPIA')
or to_tsvector('spanish', contenido_texto) @@ plainto_tsquery('spanish', 'DGAC')
or to_tsvector('spanish', contenido_texto) @@ plainto_tsquery('spanish', 'MEMORA')
order by ruta, PDF_NOMBRE;


-- delete from fragmentos_pdf  where 1=1




CREATE TABLE fragmentos2_pdf (
    id SERIAL PRIMARY KEY,
    ruta text,	
    fecha_modif timestamp,
    pdf_nombre TEXT,    
    contenido_texto TEXT,       
    texto_busqueda tsvector,
    vector_embedding vector(1024)     
);

-- Creamos los índices para que sea rápido en tu laptop
CREATE INDEX fragmentos2_pdf_idx1 ON fragmentos2_pdf USING gin(texto_busqueda);
CREATE INDEX fragmentos2_pdf_idx2 ON fragmentos2_pdf USING hnsw (vector_embedding vector_cosine_ops);

CREATE INDEX fragmentos2_pdf_idx3 ON fragmentos2_pdf (pdf_nombre);


select * from fragmentos2_pdf

