package cl.axioma.r7868.leepdf;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LeePDF_escaneado {

    public static void main(String[] args) {
        
        String NombreArchivo = "D:\\compartido\\Recepcion-desde-Central-Estacion-Total.pdf";
        //File imagen = new File("D:\\compartido\\Recepcion-desde-Central-Estacion-Total.pdf");
        
File archivoPdf = new File(NombreArchivo); //"documento_escaneado.pdf");
        
        try (PDDocument documento = Loader.loadPDF(archivoPdf)) {
            PDFRenderer renderizador = new PDFRenderer(documento);
            Tesseract tesseract = new Tesseract();
            
            // Configura la ruta de los datos de idioma de Tesseract (tessdata)
            tesseract.setDatapath("D:\\FuentesDev\\DataTeseract\\"); 
            tesseract.setLanguage("spa"); // Español
            
            // Recorre cada página del PDF
            for (int i = 0; i < documento.getNumberOfPages(); i++) {
                BufferedImage imagenPagina = renderizador.renderImageWithDPI(i, 300); // 300 DPI para mejor precisión
                String texto = tesseract.doOCR(imagenPagina);
                System.out.println("--- Página " + (i + 1) + " ---");
                System.out.println(texto);
            }
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }




    }



}
