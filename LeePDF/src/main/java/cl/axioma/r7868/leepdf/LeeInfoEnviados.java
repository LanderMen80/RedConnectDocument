package cl.axioma.r7868.leepdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.pdfbox.Loader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeeInfoEnviados {

    // Orden de las etiquetas tal como aparecen en el documento.
    // El orden importa: se usa para saber dónde termina el valor de cada campo.
    private static final String[] ETIQUETAS = {
            "ORD. IF N°", "REF.", "ANT.", "MAT.", "INCL.", "DE", "A", "Prov. N°" 
    };

    // Ej.: "Santiago, 03 de agosto 2026"
    private static final Pattern PATRON_FECHA = Pattern.compile(
            //"Santiago,\\s*(\\d{1,2})\\s+de\\s+([A-Za-zÀ-ÿ]+)\\s+(\\d{4})");
             "Santiago,\\s*(\\d{1,2})\\s+de\\s+([A-Za-zÀ-ÿ]+)\\s+(?:de\\s+)?(\\d{4})"
             , Pattern.CASE_INSENSITIVE);

    private static final Pattern Especial_Responsable = Pattern.compile (
        "(?<=CLR)/[^\\s]+" , Pattern.CASE_INSENSITIVE
    ) ;

    // Campos sin marcador de cierre confiable: se cortan a N líneas no vacías.
    private static final Map<String, Integer> LIMITE_LINEAS = new LinkedHashMap<>();
    static {
        LIMITE_LINEAS.put("DE", 2);
        LIMITE_LINEAS.put("A", 2);
    }

    // public static void main(String[] args) throws Exception {
    // String rutaPdf = args.length > 0
    // ? args[0]
    // :
    // "/mnt/user-data/uploads/ORD__N_1314_SCCOVI_773entrega_lotes_73_y_74_con_TPM_el_31_7_26_3_8_26.pdf";

    // String texto = extraerTexto(rutaPdf);
    // Map<String, String> campos = extraerCampos(texto);

    // campos.forEach((clave, valor) ->
    // System.out.println(clave + " -> " + valor)
    // );
    // }

    /** Extrae todo el texto plano del PDF. */
    public static String extraerTexto(String rutaPdf) throws Exception {

        Path path = Paths.get(rutaPdf);

        NombreArchivo = path.getFileName().toString();

        try (PDDocument doc = Loader.loadPDF(new File(rutaPdf))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    private static final Map<String, Integer> MESES = new LinkedHashMap<>();
    static {
        MESES.put("enero", 1);
        MESES.put("febrero", 2);
        MESES.put("marzo", 3);
        MESES.put("abril", 4);
        MESES.put("mayo", 5);
        MESES.put("junio", 6);
        MESES.put("julio", 7);
        MESES.put("agosto", 8);
        MESES.put("septiembre", 9);
        MESES.put("setiembre", 9);
        MESES.put("octubre", 10);
        MESES.put("noviembre", 11);
        MESES.put("diciembre", 12);
    }

    /** Busca "Santiago, dd de <mes> yyyy" y lo devuelve como "dd-mm-yyyy". */
    private static String extraerFecha(String texto) {
        Matcher m = PATRON_FECHA.matcher(texto);
        if (!m.find()) {
            return null;
        }
        int dia = Integer.parseInt(m.group(1));
        String mesTexto = m.group(2).toLowerCase();
        Integer mes = MESES.get(mesTexto);
        String anio = m.group(3);
        if (mes == null) {
            return null;
        }
        return String.format("%02d-%02d-%s", dia, mes, anio);
    }


    public static String Extraer_Responsables(String texto)
    {
            
        Matcher m = Especial_Responsable.matcher(texto);
        if (!m.find()) {
            return "";
        }
        String strResponsable = m.group();
        
        //      /fe/mgg/ftf     obtener ftf

        if (strResponsable.length() > 1)
        {
            String[] contenido = strResponsable.split("/+");
            strResponsable = contenido[contenido.length-1];
        }


        return strResponsable;
    }
    /**
     * Recorre el texto y, para cada etiqueta, captura todo lo que hay
     * entre ella y la siguiente etiqueta conocida (o el fin del texto),
     * anclando siempre al inicio de línea para no confundir etiquetas
     * cortas ("A", "DE") con palabras que las contengan ("ARCHIVO", "DEL"...).
     */

    public static String NombreArchivo;

    public static Map<String, String> extraerCampos(String texto) {
        Map<String, String> resultado = new LinkedHashMap<>();
        boolean blnEncontre = false;

        // Prefijo: inicio de línea, con posibles espacios/tabs y viñetas ("-", "•").
        String prefijo = "^[ \\t]*[-\\u2022]?[ \\t]*";

        for (int i = 0; i < ETIQUETAS.length; i++) {
            blnEncontre=false;
            String etiquetaActual = ETIQUETAS[i];
            String sufijoActual = sufijoLimite(etiquetaActual);
            String siguientes = construirAlternativaSiguientes(i + 1);

            String regex = "(?m)" + prefijo + Pattern.quote(etiquetaActual) + sufijoActual
                    + "\\s*:?\\s*"
                    + "([\\s\\S]*?)"
                    + "(?=" + prefijo + "(?:" + siguientes + ")|\\z)";

            Matcher m = Pattern.compile(regex).matcher(texto);
            if (!m.find()) {
                blnEncontre = false;
                 resultado.put(nombreLegible(etiquetaActual), "(sin valor)");
                 continue;
            }
            else
                blnEncontre = true;

            String crudo = m.group(1);

            Integer maxLineas = LIMITE_LINEAS.get(etiquetaActual);
            if (maxLineas != null) {
                crudo = limitarLineas(crudo, maxLineas);
            }

            String valor = limpiar(crudo);
            if (!valor.isEmpty()) {
                resultado.put(nombreLegible(etiquetaActual), valor);
            } 
            else
                {
                    resultado.put(nombreLegible(etiquetaActual), "(sin valor)");
                } 
        }

        String fecha = extraerFecha(texto);
        if (fecha != null) {
            resultado.put("FECHA", fecha);
         } //else {
        //     resultado.put("FECHA", "(sin valor)");
        // }

        String strResponsable = Extraer_Responsables(texto);
        if (strResponsable != null)
        {
            resultado.put("RESPONSABLE", strResponsable);
        }

        return resultado;
    }

    /**
     * Límite de palabra condicional: solo se exige cuando la etiqueta termina
     * en letra/número (ej. "A", "DE"), para no matchear "DEL", "ARCHIVO", etc.
     * No se exige cuando termina en puntuación (ej. "REF.:", "Prov.N°"),
     * porque ahí el propio signo ya la distingue de una palabra cualquiera
     * y un \\b en ese punto no encontraría un límite real.
     */
    private static String sufijoLimite(String etiqueta) {
        char ultimo = etiqueta.charAt(etiqueta.length() - 1);
        boolean esLetraONumero = Character.isLetterOrDigit(ultimo);
        return esLetraONumero ? "(?![A-Za-zÀ-ÿ0-9])" : "";
    }

    /**
     * Construye "(ETQ1|ETQ2|...)" con las etiquetas restantes, para usar de límite
     * (lookahead).
     */
    private static String construirAlternativaSiguientes(int desde) {
        StringBuilder sb = new StringBuilder();
        for (int j = desde; j < ETIQUETAS.length; j++) {
            if (sb.length() > 0)
                sb.append('|');
            sb.append(Pattern.quote(ETIQUETAS[j])).append(sufijoLimite(ETIQUETAS[j]));
        }
        // Si no quedan más etiquetas, el límite es "nunca" -> se usa \z (fin de texto)
        return sb.length() == 0 ? "(?!)" : sb.toString();
    }

    /** Se queda solo con las primeras N líneas no vacías del bloque capturado. */
    private static String limitarLineas(String texto, int maxLineas) {
        String[] lineas = texto.split("\n");
        StringBuilder sb = new StringBuilder();
        int contadas = 0;
        for (String linea : lineas) {
            if (linea.trim().isEmpty())
                continue;
            if (contadas >= maxLineas)
                break;
            sb.append(linea).append('\n');
            contadas++;
        }
        return sb.toString();
    }

    /**
     * Colapsa espacios/saltos de línea redundantes que deja la extracción del PDF.
     */
    private static String limpiar(String s) {
        return s.replaceAll("[ \\t]*\\n[ \\t]*", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private static String nombreLegible(String etiqueta) {
        switch (etiqueta) {
            case "ORD. IF N°":
                return "ORD. IF N°";
            case "REF.:":
                return "REF";
            case "ANT.:":
                return "ANT";
            case "MAT.:":
                return "MAT";
            case "INCL.:":
                return "INCL";
            case "DE":
                return "DE";
            case "A":
                return "PARA (A)";
            case "Prov.N°":
                return "Prov.N°";
            default:
                return etiqueta;
        }
    }
}
