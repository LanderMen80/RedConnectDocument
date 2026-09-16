package cl.redconnect.pdf;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class IndexadorPDFCarpeta {

    public static void main(String[] args) {
         
            // 1. Configurar la ruta del PDF de prueba en tu notebook
            String RutaCarpeta = "D:\\FuentesDev\\PDF_EJEMPLOS\\ORD_ENVIADOS";
        //     String strArchivo = "";
             File fldCarpetaInicial = new File(RutaCarpeta);
        //     File[] listOfFiles = folder.listFiles();
        //     for (File file : listOfFiles) {
        //         if (file.isFile()) {
        //             System.out.println(file.getName());
        //         }
        //     }
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

            listarArchivosRecursivo(fldCarpetaInicial);

        System.out.println("¡Indexación completada.!");
    }

    public static void listarArchivosRecursivo(File carpeta) {
        if (carpeta.isDirectory()) {
            File[] elementos = carpeta.listFiles();

            if (elementos != null) {
                for (File elemento : elementos) {
                    if (elemento.isDirectory()) {
                        System.out.println("Carpeta: " + elemento.getName());
                        listarArchivosRecursivo(elemento);
                    } else {
                        // Si es archivo, imprime su ruta
                        System.out.println("Archivo encontrado: " + elemento.getAbsolutePath());
                        indexarArchivo(elemento);
                    }
                }
            }
        }
    }

    public static void indexarArchivo(File archivo) {
        String strArchivo = "";

        strArchivo = archivo.getPath();
        if (Files.exists(Paths.get(strArchivo)) == false) {
            System.err.println("❌ ERROR: El archivo NO existe en esa ubicación. Revisa el nombre y la extensión.");
            return;
        }

        try {
            // 2. Leer el PDF completo
            DocumentParser parser = new ApacheTikaDocumentParser();
            Document documento = parser.parse(Paths.get(strArchivo).toUri().toURL().openStream());
            String RutaFull = Paths.get(strArchivo).getParent().toString();

            // 3. Hacer CHUNKING: Dividimos el texto en fragmentos de 300 caracteres con 30
            // de superposición
            // Nota: Para producción usarás tokens, pero para probar en tu laptop con
            // caracteres es ultra rápido.
            // DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);

            // Configuración óptima para all-MiniLM-L6-v2
            DocumentSplitter splitter = DocumentSplitters.recursive(800, 100);

            List<TextSegment> fragmentos = splitter.split(documento);

            // 4. Inicializar el Modelo de Embedding Local (Se ejecuta 100% en la CPU de tu
            // notebook)
            EmbeddingModel modeloEmbedding = new AllMiniLmL6V2EmbeddingModel();

            // 5. Conectar a PostgreSQL
            String url = "jdbc:postgresql://localhost:5432/poc"; // cambia 'postgres' por tu base de datos si aplica
            String usuario = "postgres";
            String clave = "marco";

            try (Connection conn = DriverManager.getConnection(url, usuario, clave)) {

                String strExiste = "Select count(*) from "










                System.out.println("Conectado a Postgres. Indexando " + fragmentos.size() + " fragmentos...");

                // Preparamos la consulta SQL Híbrida
                // Usamos to_tsvector para la búsqueda exacta y el marcador de posición para el
                // vector semántico
                String sql = "INSERT INTO fragmentos_pdf (pdf_nombre, ruta, contenido_texto, texto_busqueda, vector_embedding) "
                        +
                        "VALUES (?, ?,   ?,    to_tsvector('spanish', ?)  ,    ?::vector)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (TextSegment fragmento : fragmentos) {
                        String texto = fragmento.text();

                        // Generamos el embedding conceptual del fragmento
                        Embedding embedding = modeloEmbedding.embed(texto).content();
                        String vectorString = embedding.toString(); // Convierte el vector a formato [0.1, 0.2, ...]

                        vectorString = vectorString.replace("Embedding { vector = ", "");
                        vectorString = vectorString.replace("}", "");

                        pstmt.setString(1, Paths.get(archivo.getName()).getFileName().toString());
                        pstmt.setString(2, RutaFull);
                        pstmt.setString(3, texto);
                        pstmt.setString(4, texto); // Alimenta el motor de búsqueda clásica (léxica)
                        pstmt.setString(5, vectorString); // Alimenta pgvector

                        pstmt.addBatch(); // Indexación por lotes para no saturar

                        // System.out.println(pstmt.toString());

                    }
                    pstmt.executeBatch();
                }
                // System.out.println("¡Indexación completada con éxito en tu laptop!");
            }
        } catch (Exception e) {
            System.out.println("Error al indexar archivo: " + strArchivo + " Err: " + e.toString());
            // e.printStackTrace();
        }
    }




    // Función auxiliar para generar la "huella digital" (MD5) de un archivo
    private static String generarHash(Path archivo) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(archivo)) {
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = is.read(buffer)) != -1) {
                digest.update(buffer, 0, leidos);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    
}
