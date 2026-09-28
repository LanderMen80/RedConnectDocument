package cl.axioma.r7868.leepdf;

import java.io.File;
import java.io.FileWriter; 
import java.io.IOException; 

public class App {

    public static void main(String[] args) {

        String strRutaCarpeta = "D:\\FuentesDev\\PDF_EJEMPLOS\\PROVIDENCIAS_RECIBIDAS";
        String strRutaSalida = "D:\\FuentesDev\\PDF_EJEMPLOS";
        String strSepara = ";";
        
        File carpeta = new File(strRutaCarpeta); // Cambia por tu ruta

        try{
            FileWriter fwSalidaArchivo = new FileWriter(strRutaSalida + "\\PROVIDENCIAS_LEIDAS.txt");
            
            fwSalidaArchivo.write("Nro" + strSepara +
                                    "Fecha" + strSepara + 
                                    "Fecha" + strSepara +
                                    "Remitente" + strSepara + 
                                    "Destinatario" + strSepara +
                                    "Materias" + strSepara + 
                                    "Antecedentes" + strSepara +
                                    "Incluye" + strSepara + 
                                    "NombreArchivo"  +  System.lineSeparator()
                                );
            if (carpeta.exists() && carpeta.isDirectory()) {
                            //  Lista de archivos PDF
                            String[] archivos =   carpeta.list((dir, name) -> name.toLowerCase().endsWith(".pdf")) ;

                                // Por cada PDF
                                for (String strItemArchivo : archivos) {

                                   // if (strItemArchivo.compareTo("Prov. N° 02 IF COVI 78-68 CONST 26 0397 Entrega Tomo 11 (AFP) Volumen 5 para firma digital IF.pdf")==0){

                                        AppProcesaPDF.CargarArchivo(strRutaCarpeta + "\\" + strItemArchivo);
                                        
                                        //System.out.println(strRutaCarpeta + "\\" + strItemArchivo);

                                        fwSalidaArchivo.write(
                                                            AppProcesaPDF.NroDocumento + strSepara +
                                                            AppProcesaPDF.Fecha + strSepara + 
                                                            AppProcesaPDF.Fecha + strSepara +
                                                            AppProcesaPDF.Remitente + strSepara + 
                                                            AppProcesaPDF.Destinatario + strSepara +
                                                            AppProcesaPDF.Materias + strSepara + 
                                                            AppProcesaPDF.Antecedentes + strSepara +
                                                            AppProcesaPDF.Incluye + strSepara +
                                                            strItemArchivo  +  System.lineSeparator()
                                            );
                                    // }
                                
                            }
                
                            fwSalidaArchivo.close(); // must close manually
                            
                        // try {
                        //     FileWriter myWriter = new FileWriter("fomato_salida.txt");
                        //     myWriter.write("Files in Java might be tricky, but it is fun enough!");
                        //     myWriter.close(); // must close manually
                        // } catch (IOException e) {
                        //     System.out.println("An error occurred.");
                        //     e.printStackTrace();
                }

         } catch (IOException e) {
        //     System.out.println("An error occurred.");
        //     e.printStackTrace();
         }
        } 
}