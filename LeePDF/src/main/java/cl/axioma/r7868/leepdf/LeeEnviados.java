package cl.axioma.r7868.leepdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class LeeEnviados {

    public static void main(String[] args) throws Exception {

        String strRutaCarpeta = "D:\\FuentesDev\\PDF_EJEMPLOS\\ORD_ENVIADOS_SEPTIEMBRE";
        String strRutaSalida = "D:\\FuentesDev\\PDF_EJEMPLOS\\";
        String strSepara = ";";

        File carpeta = new File(strRutaCarpeta); // Cambia por tu ruta

        try {
            FileWriter fwSalidaArchivo = new FileWriter(strRutaSalida + "\\ORDINARIOS_LEIDOS_septiembre_v2.txt");

            // fwSalidaArchivo.write("Nro" + strSepara +
            //         "Fecha" + strSepara +
            //         "Fecha" + strSepara +
            //         "Remitente" + strSepara +
            //         "Destinatario" + strSepara +
            //         "Materias" + strSepara +
            //         "Antecedentes" + strSepara +
            //         "Incluye" + strSepara +
            //         "NombreArchivo" + System.lineSeparator());
            int intCuenta = 0 ;

            if (carpeta.exists() && carpeta.isDirectory()) {
                // Lista de archivos PDF
                String[] archivos = carpeta.list((dir, name) -> name.toLowerCase().endsWith(".pdf"));

                // Por cada PDF
                for (String strItemArchivo : archivos) {

                    // if (strItemArchivo.compareTo("Prov. N° 02 IF COVI 78-68 CONST 26 0397 Entrega
                    // Tomo 11 (AFP) Volumen 5 para firma digital IF.pdf")==0){

                     System.out.println( "Procesando: " + strItemArchivo);

                     
                    String texto = LeeInfoEnviados.extraerTexto(strRutaCarpeta + "\\" + strItemArchivo);
                    Map<String, String> campos = LeeInfoEnviados.extraerCampos(texto);

                    //campos.forEach((clave, valor) -> System.out.println(clave + " -> " + valor));

                    //Nro;Fecha;Fecha;Remitente;Destinatario;Materias;Antecedentes;Incluye;NombreArchivo

                    String strLineaObtenida = "";
                    
                    
                    //campos.forEach((clave, valor) -> System.out.println(clave + " -> " + valor));

                    if (intCuenta == 0)
                    { 
                            for (Map.Entry<String, String> entry : campos.entrySet()) {
                                strLineaObtenida = strLineaObtenida +  entry.getKey()  + strSepara ;
                            }

                            strLineaObtenida = strLineaObtenida + "Nombre_Archivo;"  ;
                            fwSalidaArchivo.write(
                                                        strLineaObtenida +  System.lineSeparator()
                                                  );
                            strLineaObtenida = "" ;
                            intCuenta =  1; 
                    }
                    for (Map.Entry<String, String> entry : campos.entrySet()) {
                            // System.out.println(entry.getKey() + " : " + entry.getValue());
                            // if (intCuenta == 0 )
                            // {
                                
                            //     strLineaObtenida =   strLineaObtenida + entry.getValue() + strSepara;
                            //     intCuenta= 1;
                            // }
                            // else
                            // {
                                strLineaObtenida = strLineaObtenida + entry.getValue() + strSepara;
                            //}
                        }

                            strLineaObtenida =   strLineaObtenida + LeeInfoEnviados.NombreArchivo + strSepara;
                    
                            fwSalidaArchivo.write(
                                                strLineaObtenida +  System.lineSeparator()
                                                );

                     

                }

                fwSalidaArchivo.close(); // must close manually

                // try {
                // FileWriter myWriter = new FileWriter("fomato_salida.txt");
                // myWriter.write("Files in Java might be tricky, but it is fun enough!");
                // myWriter.close(); // must close manually
                // } catch (IOException e) {
                // System.out.println("An error occurred.");
                // e.printStackTrace();
            }

        } catch (IOException e) {
            // System.out.println("An error occurred.");
            // e.printStackTrace();
        }
    }
}