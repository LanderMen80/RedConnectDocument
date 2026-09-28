package cl.redconnect.pdf;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import com.pgvector.PGvector;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;


public class IndexacionModelobgem3 {
    
    public static void main(String[] args) {
        
        // 1. Obtener el embedding desde Ollama (bge-m3)
        OllamaEmbeddingModel model = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("bge-m3")
                .build();

        String textoOriginal = "El texto que quiero guardar en mi base de datos";
        Response<Embedding> response = model.embed(textoOriginal);
        float[] vectorAsFloats = response.content().vector(); // Arreglo de 1024 floats

        // 2. Conectar a PostgreSQL e insertar el registro
        String url = "jdbc:postgresql://localhost:5432/tu_base_de_datos";
        String usuario = "postgres";
        String contrasena = "tu_password";

        String sql = "INSERT INTO documentos (contenido, embedding) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, usuario, contrasena);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Insertar el texto plano
            pstmt.setString(1, textoOriginal);
            
            // Convertir el float[] de Java al objeto PGvector compatible con la BD
            PGvector pgVector = new PGvector(vectorAsFloats);
            pstmt.setObject(2, pgVector);

            // Ejecutar la consulta
            pstmt.executeUpdate();
            System.out.println("¡Vector guardado exitosamente en PostgreSQL!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
}
