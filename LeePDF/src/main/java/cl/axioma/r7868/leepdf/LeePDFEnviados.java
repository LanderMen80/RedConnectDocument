package cl.axioma.r7868.leepdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LeePDFEnviados {

    public static String Remitente;
    public static String Materias;
    public static String Referencias;
    public static String Destinatario;
    public static String Antecedentes;
    public static String Fecha;
    public static String NroDocumento;
    public static String Incluye;

    public static void main(String[] args) throws Exception {
        // if (args.length < 1) {
        // System.out.println("Uso: java ExtractorSecciones <ruta_al_pdf>");
        // return;
        // }
        File file = new File("D:\\FuentesDev\\PDF_EJEMPLOS\\A-2.pdf");
        // String rutaPdf = "D:\\FuentesDev\\PDF_EJEMPLOS\\A-5.pdf"; //args[0];
        String textoCompleto;

        try (PDDocument documento = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // IMPORTANTE: algunos PDFs (ej. con membrete/logo) guardan el texto del
            // encabezado en una posición del "content stream" distinta al orden visual
            // (a veces el bloque REF/ANT/MAT queda al final del stream, después del
            // cuerpo de la carta). setSortByPosition ordena el texto por su posición
            // real en la página (arriba-abajo, izquierda-derecha) en vez del orden
            // en que fue escrito en el PDF, evitando que el MAT. quede "colgado" sin
            // nada después para servir de límite de fin de sección.
            stripper.setSortByPosition(true);
            // Solo necesitamos la primera página, donde está el encabezado
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            textoCompleto = stripper.getText(documento);
        }

        // Normalizamos saltos de línea para facilitar el regex
        String texto = textoCompleto.replace("\r\n", "\n");

        Referencias = extraerSeccion(texto, "REF\\.:", "ANT\\.:");
        Antecedentes = extraerSeccion(texto, "ANT\\.:", "MAT\\.:");
        // MAT. termina donde empieza el cuerpo de la carta (ej: "Señor" o doble salto
        // de línea)
        Materias = extraerSeccion(texto, "MAT\\.:", "Se[ñn]or|De nuestra consideraci[oó]n");

        // Destinatario: bloque que empieza en "Señor" y termina en "PRESENTE"
        Destinatario = extraerSeccion(texto, "Se[ñn]or\\n", "PRESENTE");

        // Remitente: bloque que empieza después de "atentamente," y termina en las
        // iniciales de quien redactó (ej: JK/DR/CRC/FRC/prb), en "c.c.:" o en el pie de
        // página
        Remitente = extraerSeccion(texto, "[Aa]tentamente,?\\s*\\n",
                "[A-Z]{2,4}/[A-Z]{2,4}|c\\.c\\.:|Rosita Renard");

        // System.out.println("===== REF. =====");
        // System.out.println(ref);
        // System.out.println("\n===== ANT. =====");
        // System.out.println(ant);
        // System.out.println("\n===== MAT. =====");
        // System.out.println(mat);
        // System.out.println("\n===== DESTINATARIO =====");
        // System.out.println("Señor\n" + destinatario + "\nPRESENTE");
        // System.out.println("\n===== REMITENTE =====");
        // System.out.println(Remitente);

    }

    public static void CargarArchivo(String RutaCompletaArchivo) throws IOException {
        File file = new File(RutaCompletaArchivo);
        String textoCompleto;

        try (PDDocument documento = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // IMPORTANTE: algunos PDFs (ej. con membrete/logo) guardan el texto del
            // encabezado en una posición del "content stream" distinta al orden visual
            // (a veces el bloque REF/ANT/MAT queda al final del stream, después del
            // cuerpo de la carta). setSortByPosition ordena el texto por su posición
            // real en la página (arriba-abajo, izquierda-derecha) en vez del orden
            // en que fue escrito en el PDF, evitando que el MAT. quede "colgado" sin
            // nada después para servir de límite de fin de sección.
            stripper.setSortByPosition(true);
            // Solo necesitamos la primera página, donde está el encabezado
            // stripper.setStartPage(1);
            // stripper.setEndPage(1);
            textoCompleto = stripper.getText(documento);
        }

        // Normalizamos saltos de línea para facilitar el regex
        String texto = textoCompleto.replace("\r\n", "\n").trim();

        /// (?<=REF\.:[\s\S]*?(?=
        ///
        Referencias = extraerSeccion(texto, "REF\\.:", "ANT\\.:");
        Antecedentes = extraerSeccion(texto, "ANT\\.:", "MAT\\.:");
        // MAT. termina donde empieza el cuerpo de la carta (ej: "Señor" o doble salto
        // de línea)
        Materias = extraerSeccion(texto, "MAT\\.:", "Se[ñn]or|De nuestra consideraci[oó]n");

        // Destinatario: bloque que empieza en "Señor" y termina en "PRESENTE"
        // Destinatario = extraerSeccion(texto, "Se[ñn]or\\n", "PRESENTE");
        Destinatario = extraeDestinatario(texto, "(?<=Señor)[\\s\\S]*?(?=PRESENTE)");

        // Remitente: bloque que empieza después de "atentamente," y termina en las
        // iniciales de quien redactó (ej: JK/DR/CRC/FRC/prb), en "c.c.:" o en el pie de
        // página
        // Remitente = extraerSeccion(texto, "[Aa]tentamente,?\\s*\\n",
        // "[A-Z]{2,4}/[A-Z]{2,4}|c\\.c\\.:|Rosita Renard");

        Remitente = extraeDestinatario(texto, "(?<=Le saluda atentamente,)[\\s\\S]*?Gerente General");

        Path path = Paths.get(RutaCompletaArchivo);

        String strNombreArchivo = path.getFileName().toString();
        NroDocumento = extraeDestinatario(strNombreArchivo, "(?<=Prov\\. N°\\s)\\d+(?=\\s)");

        Incluye = extraeInluyeEnlaces(texto, "https?://[^\\r\\n]+(?:\\r?\\n[^\\r\\n]+)?");

        Fecha = extraeFecha(texto);
    }

    /**
     * Extrae el texto entre dos etiquetas usando regex.
     * 
     * @param texto  texto completo donde buscar
     * @param inicio patrón regex que marca el inicio (ej: "REF\\.:")
     * @param fin    patrón regex que marca el fin (ej: "ANT\\.:")
     * @return el contenido entre ambas etiquetas, limpio de espacios sobrantes
     */
    private static String extraerSeccion(String texto, String inicio, String fin) {
        String patronStr = inicio + "(.*?)(?=" + fin + ")";
        Pattern patron = Pattern.compile(patronStr, Pattern.DOTALL);
        Matcher m = patron.matcher(texto);

        if (m.find()) {
            return limpiar(m.group(1));
        }

        // // Intento 2 (fallback): si no aparece la etiqueta de fin (ej. la sección
        // // es lo último en la página), se toma todo lo que sigue hasta el final del
        // texto.
        // Pattern patronSinFin = Pattern.compile("(?<=" + inicio + "[\\s\\S]*?(?=",
        // Pattern.DOTALL);
        // Matcher m2 = patronSinFin.matcher(texto);
        // if (m2.find()) {
        // return limpiar(m2.group(1));
        // }

        return "(no encontrado)";
    }

    public static String extraeDestinatario(String texto, String ExpReg) {
        Pattern patron = Pattern.compile(ExpReg, Pattern.DOTALL);
        Matcher m = patron.matcher(texto);

        if (m.find()) {
            return limpiar(m.group());
        }

        return "(no encontrado)";

    }

    public static String extraeInluyeEnlaces(String texto, String ExpReg) {
        Pattern patron = Pattern.compile(ExpReg, Pattern.DOTALL);
        Matcher m = patron.matcher(texto);
        String strEnlaces = "";

        int intContador = 0;
        while (m.find()) {
            if (intContador > 0) {
                strEnlaces = strEnlaces + "  [" + intContador + "] : " + m.group()
                        .replaceAll("\\s*\\r?\\n\\s*", "");

            } else {
                strEnlaces = "[" + intContador + "] : " + m.group()
                        .replaceAll("\\s*\\r?\\n\\s*", "");

            }

            intContador++;
        }

        return strEnlaces;
        // return "(no encontrado)";

    }

    private static String limpiar(String contenido) {
        // Limpieza: quitamos espacios/tabs repetidos y espacios al inicio/fin
        return contenido.trim().replaceAll("[ \\t]+", " ").replaceAll("\r", "").replaceAll("\n", "");
    }

    public static String extraeFecha(String contenido) {
        Pattern pattern = Pattern.compile(
                "(\\d{1,2})\\s+de\\s+([a-záéíóúñ]+)\\s+de\\s+(\\d{4})",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(contenido);

        if (matcher.find()) {
            String fechaTexto = matcher.group(1) + " de "
                    + matcher.group(2) + " de "
                    + matcher.group(3);

            DateTimeFormatter entrada = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

            DateTimeFormatter salida = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            LocalDate fecha = LocalDate.parse(fechaTexto, entrada);

            return (fecha.format(salida));
        } else {
            return "";
        }

    }

}