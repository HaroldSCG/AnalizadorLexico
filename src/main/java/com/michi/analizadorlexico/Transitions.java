
package com.michi.analizadorlexico;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.michi.analizadorlexico.Ventana_Thompson.AFN;
import com.michi.analizadorlexico.Ventana_Thompson.ConstructorAFN_Thompson;
import com.michi.analizadorlexico.Ventana_Thompson.Estado;
import com.michi.analizadorlexico.Ventana_Thompson.PatronRegex;
import com.michi.analizadorlexico.Ventana_Thompson.Transicion;
/**
 * Genera una representación textual de las transiciones del AFN
 * construido con el algoritmo de Thompson para una expresión regular.
 *
 * <p>Formato de salida (una transición por línea):</p>
 * <pre>
 *   q&lt;origen&gt; --&lt;símbolo&gt;--&gt; q&lt;destino&gt;
 * </pre>
 *
 * <p>Donde {@code <símbolo>} puede ser:</p>
 * <ul>
 *   <li>{@code ε} para las transiciones vacías (epsilon),</li>
 *   <li>un carácter literal (ej. {@code a}),</li>
 *   <li>una clase de caracteres compactada (ej. {@code [a-z]} o {@code [0-9A-F]}).</li>
 * </ul>
 *
 * <p>Ejemplo para la expresión regular {@code a|b}:</p>
 * <pre>
 *   q4 --ε--&gt; q0
 *   q4 --ε--&gt; q2
 *   q0 --a--&gt; q1
 *   q2 --b--&gt; q3
 *   q1 --ε--&gt; q5
 *   q3 --ε--&gt; q5
 * </pre>
 *
 * @author michi
 */
public final class Transitions {

    /** Símbolo épsilon usado en las transiciones vacías. */
    private static final String EPSILON = "ε";

    private Transitions() {
        // Utilidad estática: no se debe instanciar.
    }

    // =================================================================
    //   API PÚBLICA
    // =================================================================

    /**
     * Construye el AFN de Thompson para la expresión regular indicada y
     * devuelve la lista de sus transiciones como texto plano, una por
     * línea. Si la expresión está vacía o es inválida se devuelve un
     * mensaje explicativo en lugar de lanzar una excepción, de manera
     * que el resultado siempre sea apto para mostrarse en un
     * {@code JTextArea}.
     *
     * <p>Cuando la expresión contiene varios patrones de la forma
     * {@code TIPO=regex;TIPO=regex} se listan las transiciones de cada
     * sub-AFN precedidas por un encabezado con el nombre del tipo y se
     * reinicia la numeración de estados ({@code q0, q1, ...}) en cada
     * patrón para que cada bloque sea independiente y fácil de leer.</p>
     *
     * @param expresion expresión regular escrita por el usuario, tal
     *                  como aparece en el campo {@code tExpresion}.
     * @return texto con las transiciones del AFN, o un mensaje
     *         informativo si la expresión está vacía o es inválida.
     */
    public static String generar(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            return "";
        }

        List<PatronRegex> patrones;
        try {
            patrones = Ventana_Thompson.parsearPatrones(expresion);
        } catch (RuntimeException ex) {
            return "No fue posible interpretar la expresión: " + ex.getMessage();
        }
        if (patrones.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < patrones.size(); i++) {
            PatronRegex patron = patrones.get(i);
            if (patrones.size() > 1) {
                if (i > 0) sb.append('\n');
                sb.append("# Transiciones para el patrón '")
                  .append(patron.tipo).append("' (")
                  .append(patron.regex).append(")\n");
            }
            sb.append(generarParaPatron(patron.regex));
        }
        return sb.toString();
    }

    /**
     * Monta dentro del panel indicado una vista gráfica de las transiciones
     * generadas por {@link #generar(String)}. El JFrame solo necesita pasar el
     * texto que ya muestra en {@code tTrans}; el parseo y el dibujo permanecen
     * centralizados en esta clase.
     *
     * @param contenedor panel donde se debe mostrar el grafo.
     * @param transiciones texto con líneas del tipo {@code q0 --a--> q1}.
     */
    public static void mostrarDiagrama(JPanel contenedor, String transiciones) {
        if (contenedor == null) {
            return;
        }

        contenedor.removeAll();
        contenedor.setLayout(new BorderLayout());

        DocumentoDiagrama documento = parsearDocumentoDiagrama(transiciones);
        if (!documento.tieneTransiciones()) {
            JLabel mensaje = new JLabel(
                "<html><div style='text-align:center;'>"
                + "No hay transiciones válidas para dibujar.<br>"
                + "Ejecute el análisis y verifique el contenido de transiciones."
                + "</div></html>",
                SwingConstants.CENTER
            );
            contenedor.add(mensaje, BorderLayout.CENTER);
        } else {
            PanelDiagrama panelDiagrama = new PanelDiagrama(documento);
            JScrollPane scroll = new JScrollPane(panelDiagrama);
            scroll.getViewport().setBackground(Color.WHITE);
            contenedor.add(scroll, BorderLayout.CENTER);
        }

        contenedor.revalidate();
        contenedor.repaint();
    }

    // =================================================================
    //   CONSTRUCCIÓN Y RECORRIDO DEL AFN
    // =================================================================

    /**
     * Construye el AFN para una sola regex y devuelve sus transiciones.
     * Usa un {@link ConstructorAFN_Thompson} recién instanciado para
     * que la numeración de estados arranque siempre en {@code q0}.
     */
    private static String generarParaPatron(String regex) {
        AFN afn;
        try {
            ConstructorAFN_Thompson constructor = new ConstructorAFN_Thompson();
            afn = constructor.construir(regex);
        } catch (RuntimeException ex) {
            return "  (regex inválida: " + ex.getMessage() + ")\n";
        }
        return listarTransiciones(afn);
    }

    /**
     * Recorre el AFN en BFS desde su estado inicial y produce las
     * transiciones en el orden en el que se descubren. Cada estado se
     * visita una sola vez, pero <i>todas</i> sus transiciones salientes
     * se emiten, incluyendo las que apuntan a estados ya visitados.
     */
    private static String listarTransiciones(AFN afn) {
        StringBuilder sb = new StringBuilder();
        Set<Estado> visitados = Collections.newSetFromMap(new IdentityHashMap<Estado, Boolean>());
        Deque<Estado> cola = new ArrayDeque<>();

        cola.add(afn.inicio);
        visitados.add(afn.inicio);

        while (!cola.isEmpty()) {
            Estado actual = cola.pollFirst();
            for (Transicion t : actual.transiciones) {
                String simbolo = t.epsilon ? EPSILON : formatearSimbolos(t.simbolos);
                sb.append('q').append(actual.id)
                  .append(" --").append(simbolo).append("--> ")
                  .append('q').append(t.destino.id)
                  .append('\n');
                if (visitados.add(t.destino)) {
                    cola.addLast(t.destino);
                }
            }
        }
        return sb.toString();
    }

    // =================================================================
    //   FORMATEO DE SÍMBOLOS
    // =================================================================

    /**
     * Convierte el conjunto de caracteres aceptados por una transición
     * no-épsilon en una representación legible. Las clases con varios
     * caracteres se compactan en rangos ({@code [a-z]}, {@code [0-9A-F]}, ...).
     */
    private static String formatearSimbolos(Set<Character> simbolos) {
        if (simbolos == null || simbolos.isEmpty()) {
            return "∅";
        }
        if (simbolos.size() == 1) {
            return escaparMostrable(simbolos.iterator().next());
        }
        List<Character> ordenados = new ArrayList<>(simbolos);
        Collections.sort(ordenados);

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i = 0;
        int n = ordenados.size();
        while (i < n) {
            char ini = ordenados.get(i);
            int j = i;
            while (j + 1 < n && ordenados.get(j + 1) == (char) (ordenados.get(j) + 1)) {
                j++;
            }
            char fin = ordenados.get(j);
            if (ini == fin) {
                sb.append(escaparMostrable(ini));
            } else if (fin == (char) (ini + 1)) {
                sb.append(escaparMostrable(ini)).append(escaparMostrable(fin));
            } else {
                sb.append(escaparMostrable(ini)).append('-').append(escaparMostrable(fin));
            }
            i = j + 1;
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Devuelve una representación imprimible de un carácter, sustituyendo
     * los caracteres de control y el espacio por secuencias visibles
     * (\\n, \\t, \\s, etc.) para que las transiciones siempre se
     * muestren de forma legible en el {@code JTextArea}.
     */
    private static String escaparMostrable(char c) {
        switch (c) {
            case '\n': return "\\n";
            case '\t': return "\\t";
            case '\r': return "\\r";
            case '\f': return "\\f";
            case '\b': return "\\b";
            case ' ':  return "\\s";
            default:
                if (c < 32 || c == 127) {
                    return String.format("\\x%02X", (int) c);
                }
                return String.valueOf(c);
        }
    }

    // =================================================================
    //   PARSEO Y DIBUJO DEL DIAGRAMA
    // =================================================================

    private static DocumentoDiagrama parsearDocumentoDiagrama(String texto) {
        DocumentoDiagrama documento = new DocumentoDiagrama();
        BloqueDiagrama bloqueActual = new BloqueDiagrama(null);

        if (texto == null || texto.trim().isEmpty()) {
            return documento;
        }

        String[] lineas = texto.split("\\R");
        for (String linea : lineas) {
            String limpia = linea.trim();
            if (limpia.isEmpty()) {
                continue;
            }

            if (limpia.startsWith("#")) {
                if (bloqueActual.tieneTransiciones()) {
                    documento.agregar(bloqueActual);
                }
                bloqueActual = new BloqueDiagrama(limpia.substring(1).trim());
                continue;
            }

            AristaDiagrama arista = parsearArista(limpia);
            if (arista != null) {
                bloqueActual.agregar(arista);
            }
        }

        if (bloqueActual.tieneTransiciones()) {
            documento.agregar(bloqueActual);
        }
        return documento;
    }

    private static AristaDiagrama parsearArista(String linea) {
        if (!linea.startsWith("q") || linea.length() < 2) {
            return null;
        }

        int finOrigen = 1;
        while (finOrigen < linea.length() && Character.isDigit(linea.charAt(finOrigen))) {
            finOrigen++;
        }
        if (finOrigen == 1) {
            return null;
        }

        int inicioEtiqueta = linea.indexOf("--", finOrigen);
        int finEtiqueta = linea.lastIndexOf("-->");
        if (inicioEtiqueta < 0 || finEtiqueta < 0 || finEtiqueta < inicioEtiqueta + 2) {
            return null;
        }

        String destinoTexto = linea.substring(finEtiqueta + 3).trim();
        if (!destinoTexto.startsWith("q") || destinoTexto.length() < 2) {
            return null;
        }

        int finDestino = 1;
        while (finDestino < destinoTexto.length() && Character.isDigit(destinoTexto.charAt(finDestino))) {
            finDestino++;
        }
        if (finDestino == 1) {
            return null;
        }

        try {
            int origen = Integer.parseInt(linea.substring(1, finOrigen));
            int destino = Integer.parseInt(destinoTexto.substring(1, finDestino));
            String etiqueta = linea.substring(inicioEtiqueta + 2, finEtiqueta).trim();
            return new AristaDiagrama(origen, destino, etiqueta.isEmpty() ? "?" : etiqueta);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final class DocumentoDiagrama {
        private final List<BloqueDiagrama> bloques = new ArrayList<>();

        private void agregar(BloqueDiagrama bloque) {
            bloques.add(bloque);
        }

        private boolean tieneTransiciones() {
            return bloques.stream().anyMatch(BloqueDiagrama::tieneTransiciones);
        }
    }

    private static final class BloqueDiagrama {
        private final String titulo;
        private final List<AristaDiagrama> aristas = new ArrayList<>();
        private final Set<Integer> estados = new LinkedHashSet<>();
        private final Set<Integer> conSalida = new LinkedHashSet<>();
        private Integer estadoInicial;

        private BloqueDiagrama(String titulo) {
            this.titulo = titulo == null || titulo.isBlank() ? "AFN Thompson" : titulo;
        }

        private void agregar(AristaDiagrama arista) {
            if (estadoInicial == null) {
                estadoInicial = arista.origen;
            }
            aristas.add(arista);
            estados.add(arista.origen);
            estados.add(arista.destino);
            conSalida.add(arista.origen);
        }

        private boolean tieneTransiciones() {
            return !aristas.isEmpty();
        }

        private List<Integer> estadosOrdenados() {
            List<Integer> ordenados = new ArrayList<>(estados);
            ordenados.sort(Comparator.naturalOrder());
            return ordenados;
        }

        private boolean esAceptacion(int estado) {
            return !conSalida.contains(estado);
        }
    }

    private static final class AristaDiagrama {
        private final int origen;
        private final int destino;
        private final String etiqueta;

        private AristaDiagrama(int origen, int destino, String etiqueta) {
            this.origen = origen;
            this.destino = destino;
            this.etiqueta = etiqueta;
        }
    }

    private static final class PanelDiagrama extends JPanel {
        private static final int RADIO_ESTADO = 24;
        private static final int MARGEN_X = 55;
        private static final int MARGEN_Y = 28;
        private static final int ESPACIO_X = 125;
        private static final int ESPACIO_Y = 112;
        private static final int ALTO_TITULO = 34;
        private static final int SEPARACION_BLOQUE = 42;
        private static final Stroke TRAZO_NORMAL = new BasicStroke(1.8f);
        private static final Stroke TRAZO_ACEPTACION = new BasicStroke(1.4f);
        private static final Color COLOR_ESTADO = new Color(245, 248, 255);
        private static final Color COLOR_INICIAL = new Color(220, 245, 224);
        private static final Color COLOR_ACEPTACION = new Color(255, 248, 220);
        private static final Color COLOR_LINEA = new Color(45, 45, 45);

        private final DocumentoDiagrama documento;

        private PanelDiagrama(DocumentoDiagrama documento) {
            this.documento = documento;
            setBackground(Color.WHITE);
            setPreferredSize(calcularTamanoPreferido());
        }

        private Dimension calcularTamanoPreferido() {
            int ancho = 900;
            int alto = MARGEN_Y;
            for (BloqueDiagrama bloque : documento.bloques) {
                int cantidadEstados = Math.max(1, bloque.estados.size());
                int columnas = Math.min(7, cantidadEstados);
                int filas = (int) Math.ceil(cantidadEstados / (double) columnas);
                ancho = Math.max(ancho, MARGEN_X * 2 + columnas * ESPACIO_X);
                alto += ALTO_TITULO + Math.max(1, filas) * ESPACIO_Y + SEPARACION_BLOQUE;
            }
            return new Dimension(ancho, Math.max(430, alto + MARGEN_Y));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int y = MARGEN_Y;
                for (BloqueDiagrama bloque : documento.bloques) {
                    int altoBloque = dibujarBloque(g2, bloque, y);
                    y += altoBloque + SEPARACION_BLOQUE;
                }
            } finally {
                g2.dispose();
            }
        }

        private int dibujarBloque(Graphics2D g2, BloqueDiagrama bloque, int yInicio) {
            g2.setColor(new Color(70, 70, 70));
            g2.drawString(bloque.titulo, MARGEN_X, yInicio + 15);

            List<Integer> estados = bloque.estadosOrdenados();
            int anchoDisponible = Math.max(ESPACIO_X, getWidth() - (MARGEN_X * 2));
            int columnas = Math.max(1, Math.min(estados.size(), anchoDisponible / ESPACIO_X));
            Map<Integer, Point> posiciones = new LinkedHashMap<>();
            int yEstados = yInicio + ALTO_TITULO + RADIO_ESTADO;

            for (int i = 0; i < estados.size(); i++) {
                int fila = i / columnas;
                int columna = i % columnas;
                int estadosEnFila = Math.min(columnas, estados.size() - (fila * columnas));
                int anchoFila = Math.max(0, (estadosEnFila - 1) * ESPACIO_X);
                int xInicial = Math.max(MARGEN_X + RADIO_ESTADO, (getWidth() - anchoFila) / 2);
                int x = xInicial + columna * ESPACIO_X;
                int y = yEstados + fila * ESPACIO_Y;
                posiciones.put(estados.get(i), new Point(x, y));
            }

            for (AristaDiagrama arista : bloque.aristas) {
                dibujarArista(g2, bloque, posiciones, arista);
            }
            for (Integer estado : estados) {
                dibujarEstado(g2, bloque, posiciones.get(estado), estado);
            }

            int filas = (int) Math.ceil(estados.size() / (double) columnas);
            return ALTO_TITULO + Math.max(1, filas) * ESPACIO_Y;
        }

        private void dibujarEstado(Graphics2D g2, BloqueDiagrama bloque, Point punto, int estado) {
            boolean inicial = bloque.estadoInicial != null && bloque.estadoInicial == estado;
            boolean aceptacion = bloque.esAceptacion(estado);

            Color relleno = inicial ? COLOR_INICIAL : (aceptacion ? COLOR_ACEPTACION : COLOR_ESTADO);
            g2.setColor(relleno);
            g2.fillOval(punto.x - RADIO_ESTADO, punto.y - RADIO_ESTADO, RADIO_ESTADO * 2, RADIO_ESTADO * 2);

            g2.setColor(COLOR_LINEA);
            g2.setStroke(TRAZO_NORMAL);
            g2.drawOval(punto.x - RADIO_ESTADO, punto.y - RADIO_ESTADO, RADIO_ESTADO * 2, RADIO_ESTADO * 2);
            if (aceptacion) {
                g2.setStroke(TRAZO_ACEPTACION);
                g2.drawOval(punto.x - RADIO_ESTADO + 5, punto.y - RADIO_ESTADO + 5,
                            (RADIO_ESTADO - 5) * 2, (RADIO_ESTADO - 5) * 2);
            }
            if (inicial) {
                dibujarFlechaInicial(g2, punto);
            }

            String texto = "q" + estado;
            FontMetrics fm = g2.getFontMetrics();
            int tx = punto.x - fm.stringWidth(texto) / 2;
            int ty = punto.y + (fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(texto, tx, ty);
        }

        private void dibujarFlechaInicial(Graphics2D g2, Point punto) {
            int x1 = punto.x - RADIO_ESTADO - 34;
            int y = punto.y;
            int x2 = punto.x - RADIO_ESTADO - 4;
            g2.setStroke(TRAZO_NORMAL);
            g2.draw(new Line2D.Double(x1, y, x2, y));
            dibujarPuntaFlecha(g2, x2, y, 0);
        }

        private void dibujarArista(Graphics2D g2, BloqueDiagrama bloque,
                                   Map<Integer, Point> posiciones, AristaDiagrama arista) {
            Point origen = posiciones.get(arista.origen);
            Point destino = posiciones.get(arista.destino);
            if (origen == null || destino == null) {
                return;
            }

            g2.setColor(COLOR_LINEA);
            g2.setStroke(TRAZO_NORMAL);

            if (arista.origen == arista.destino) {
                dibujarBucle(g2, origen, arista.etiqueta);
                return;
            }

            double dx = destino.x - origen.x;
            double dy = destino.y - origen.y;
            double distancia = Math.max(1.0, Math.hypot(dx, dy));
            double ux = dx / distancia;
            double uy = dy / distancia;

            double x1 = origen.x + ux * RADIO_ESTADO;
            double y1 = origen.y + uy * RADIO_ESTADO;
            double x2 = destino.x - ux * RADIO_ESTADO;
            double y2 = destino.y - uy * RADIO_ESTADO;

            double offset = calcularOffsetArista(bloque, arista);
            if (Math.abs(offset) < 0.1) {
                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                dibujarPuntaFlecha(g2, x2, y2, Math.atan2(y2 - y1, x2 - x1));
                dibujarEtiqueta(g2, arista.etiqueta, (x1 + x2) / 2, (y1 + y2) / 2 - 7);
                return;
            }

            double mx = (x1 + x2) / 2;
            double my = (y1 + y2) / 2;
            double cx = mx + (-uy * offset);
            double cy = my + (ux * offset);
            QuadCurve2D curva = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
            g2.draw(curva);
            dibujarPuntaFlecha(g2, x2, y2, Math.atan2(y2 - cy, x2 - cx));
            dibujarEtiqueta(g2, arista.etiqueta, cx, cy - 7);
        }

        private double calcularOffsetArista(BloqueDiagrama bloque, AristaDiagrama arista) {
            int totalMismaDireccion = 0;
            int indiceMismaDireccion = 0;
            boolean tieneRetorno = false;

            for (AristaDiagrama actual : bloque.aristas) {
                if (actual.origen == arista.origen && actual.destino == arista.destino) {
                    if (actual == arista) {
                        indiceMismaDireccion = totalMismaDireccion;
                    }
                    totalMismaDireccion++;
                }
                if (actual.origen == arista.destino && actual.destino == arista.origen) {
                    tieneRetorno = true;
                }
            }

            double offset = 0;
            if (tieneRetorno) {
                offset = arista.origen < arista.destino ? -34 : 34;
            }
            if (totalMismaDireccion > 1) {
                offset += (indiceMismaDireccion - ((totalMismaDireccion - 1) / 2.0)) * 24;
            }
            return offset;
        }

        private void dibujarBucle(Graphics2D g2, Point punto, String etiqueta) {
            int x = punto.x - RADIO_ESTADO;
            int y = punto.y - RADIO_ESTADO - 35;
            Arc2D arco = new Arc2D.Double(x, y, RADIO_ESTADO * 2, RADIO_ESTADO * 2, 35, 290, Arc2D.OPEN);
            g2.draw(arco);
            dibujarPuntaFlecha(g2, punto.x + RADIO_ESTADO - 5, punto.y - RADIO_ESTADO - 2,
                               Math.toRadians(70));
            dibujarEtiqueta(g2, etiqueta, punto.x, y - 4);
        }

        private void dibujarEtiqueta(Graphics2D g2, String etiqueta, double x, double y) {
            FontMetrics fm = g2.getFontMetrics();
            int ancho = fm.stringWidth(etiqueta) + 8;
            int alto = fm.getHeight();
            int bx = (int) Math.round(x - ancho / 2.0);
            int by = (int) Math.round(y - alto + fm.getDescent());

            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(bx, by, ancho, alto, 8, 8);
            g2.setColor(COLOR_LINEA);
            g2.drawString(etiqueta, bx + 4, by + fm.getAscent());
        }

        private void dibujarPuntaFlecha(Graphics2D g2, double x, double y, double angulo) {
            double largo = 10;
            double apertura = Math.toRadians(26);

            Path2D punta = new Path2D.Double();
            punta.moveTo(x, y);
            punta.lineTo(x - largo * Math.cos(angulo - apertura), y - largo * Math.sin(angulo - apertura));
            punta.lineTo(x - largo * Math.cos(angulo + apertura), y - largo * Math.sin(angulo + apertura));
            punta.closePath();
            g2.fill(punta);
        }
    }
}
