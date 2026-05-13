/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.michi.analizadorlexico;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.michi.analizadorlexico.Ventana_Thompson.AFN;
import com.michi.analizadorlexico.Ventana_Thompson.ConstructorAFN_Thompson;
import com.michi.analizadorlexico.Ventana_Thompson.Estado;
import com.michi.analizadorlexico.Ventana_Thompson.PatronRegex;
import com.michi.analizadorlexico.Ventana_Thompson.Transicion;

/**
 * Toda la lógica auxiliar de Thompson que vive fuera de
 * {@link Ventana_Thompson} está concentrada en este único archivo:
 *
 * <ol>
 *   <li><b>Listado textual de transiciones</b> (lo que se muestra en
 *       el {@code JTextArea tTrans}) — {@link #generar(String)}.</li>
 *   <li><b>Ventana con el grafo del AFN</b> — {@link #mostrarDiagrama(String)}.
 *       Construye y abre, de forma programática (sin archivos
 *       {@code .form} ni clases auxiliares), un {@code JFrame} con un
 *       único {@code JPanel} llamado {@code jGraph} donde se pinta el
 *       grafo del AFN de Thompson para la expresión.</li>
 * </ol>
 *
 * <p>La construcción del grafo usa patrones de layout estructurados
 * (con entrada y salida explícitas) para cada operación de Thompson —
 * símbolo, concatenación, unión, Kleene, una-o-más y opcional — de
 * modo que la composición resulta visualmente coherente. Cada estado
 * se dibuja como un círculo con su identificador {@code qN} dentro,
 * y las transiciones se dibujan como flechas etiquetadas (con
 * {@code ε} para las epsilon y la representación compacta del símbolo
 * para las demás).</p>
 *
 * @author harol
 */
public final class Transitions {

    private Transitions() {
        // Utilidad estática: no se debe instanciar.
    }

    // =================================================================
    //   API PÚBLICA
    // =================================================================

    /** Símbolo épsilon usado en las transiciones vacías. */
    private static final String EPSILON = "ε";

    /**
     * Construye el AFN de Thompson para la expresión y devuelve la lista
     * de sus transiciones como texto plano, una por línea, con el
     * formato {@code qN --símbolo--> qM}. Si la expresión está vacía o
     * es inválida se devuelve un mensaje informativo (no se lanza una
     * excepción) para que el resultado siempre pueda mostrarse en un
     * {@code JTextArea}.
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
        if (patrones.isEmpty()) return "";

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
     * Abre (o reutiliza) la ventana {@link Diagrama} y dibuja en su
     * {@code JPanel jGraph} el AFN correspondiente a {@code expresion}.
     * Si la expresión contiene varios patrones ({@code TIPO=regex;...})
     * se muestra únicamente el primero. Si la expresión es vacía o
     * inválida, en lugar del grafo se muestra un mensaje informativo
     * dentro del propio {@code jGraph}.
     *
     * <p>El {@code JPanel jGraph} de {@link Diagrama} está declarado
     * {@code private} por NetBeans en {@code initComponents()} y su
     * declaración no debe modificarse manualmente, por lo que aquí lo
     * obtenemos vía reflexión. De este modo, toda la integración con
     * {@link Diagrama} vive exclusivamente en este archivo y la clase
     * {@link Diagrama} permanece intacta (justo como la genera
     * NetBeans).</p>
     */
    public static void mostrarDiagrama(String expresion) {
        crearVentanaSiHaceFalta();
        actualizarVentana(expresion);
        ventanaDiagrama.setVisible(true);
        ventanaDiagrama.toFront();
        ventanaDiagrama.requestFocus();
    }

    // =================================================================
    //   LISTADO TEXTUAL DE TRANSICIONES (generar)
    // =================================================================

    /** Construye el AFN para una sola regex y devuelve sus transiciones. */
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
     * Recorre el AFN en BFS desde su estado inicial y emite las
     * transiciones en el orden en el que se descubren los estados.
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

    /**
     * Convierte el conjunto de caracteres aceptados por una transición
     * en una representación legible. Las clases con varios caracteres
     * se compactan en rangos ({@code [a-z]}, {@code [0-9A-F]}, ...).
     */
    private static String formatearSimbolos(Set<Character> simbolos) {
        if (simbolos == null || simbolos.isEmpty()) return "∅";
        if (simbolos.size() == 1) return escaparMostrable(simbolos.iterator().next());

        List<Character> ordenados = new ArrayList<>(simbolos);
        Collections.sort(ordenados);
        StringBuilder sb = new StringBuilder("[");
        int i = 0, n = ordenados.size();
        while (i < n) {
            char ini = ordenados.get(i);
            int j = i;
            while (j + 1 < n && ordenados.get(j + 1) == (char) (ordenados.get(j) + 1)) j++;
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

    private static String escaparMostrable(char c) {
        switch (c) {
            case '\n': return "\\n";
            case '\t': return "\\t";
            case '\r': return "\\r";
            case '\f': return "\\f";
            case '\b': return "\\b";
            case ' ':  return "\\s";
            default:
                if (c < 32 || c == 127) return String.format("\\x%02X", (int) c);
                return String.valueOf(c);
        }
    }

    // =================================================================
    //   VENTANA DEL DIAGRAMA (creación programática, sin .form)
    // =================================================================

    private static Diagrama ventanaDiagrama;
    private static LienzoGrafo lienzo;     // hijo de Diagrama.jGraph que pinta.

    /**
     * Crea la ventana {@link Diagrama} la primera vez (o si fue
     * cerrada y ya no es {@code isDisplayable}). Después de
     * {@code initComponents}, sustituye el contenido de {@code jGraph}
     * por un {@code JScrollPane} que envuelve al {@link LienzoGrafo}.
     */
    private static void crearVentanaSiHaceFalta() {
        if (ventanaDiagrama != null && ventanaDiagrama.isDisplayable()) return;

        ventanaDiagrama = new Diagrama();
        ventanaDiagrama.setTitle("Diagrama de Thompson");

        JPanel jGraph = obtenerJGraph(ventanaDiagrama);

        // El GroupLayout que pone NetBeans en jGraph define su tamaño
        // preferido (p.ej. 884×388). Lo capturamos antes de sustituir
        // el layout para que el JFrame no encoja al instalar el
        // BorderLayout interno, que reportaría el tamaño preferido
        // del JScrollPane (mucho menor).
        Dimension prefOriginal = jGraph.getPreferredSize();

        jGraph.removeAll();
        jGraph.setLayout(new BorderLayout());
        lienzo = new LienzoGrafo();
        jGraph.add(new JScrollPane(lienzo), BorderLayout.CENTER);
        if (prefOriginal != null && prefOriginal.width > 50 && prefOriginal.height > 50) {
            jGraph.setPreferredSize(prefOriginal);
            jGraph.setMinimumSize(prefOriginal);
        }
        jGraph.revalidate();

        ventanaDiagrama.setLocationRelativeTo(null);
        lienzo.setMensaje("Escriba una expresión regular y pulse 'Diagrama' "
                + "en la ventana principal.", false);
    }

    /**
     * Devuelve el campo {@code jGraph} de la instancia de
     * {@link Diagrama} indicada. Se usa reflexión porque NetBeans
     * declara el campo como {@code private} dentro del bloque
     * "Generated Code" que no debe modificarse a mano. Como ambas
     * clases están en el mismo paquete y módulo, la reflexión es la
     * forma más limpia de obtener el panel sin tocar
     * {@link Diagrama}.
     */
    private static JPanel obtenerJGraph(Diagrama d) {
        try {
            java.lang.reflect.Field f = Diagrama.class.getDeclaredField("jGraph");
            f.setAccessible(true);
            Object v = f.get(d);
            if (!(v instanceof JPanel)) {
                throw new RuntimeException("Diagrama.jGraph no es un JPanel");
            }
            return (JPanel) v;
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("No se pudo acceder a Diagrama.jGraph: "
                    + ex.getMessage(), ex);
        }
    }

    /** Refresca el contenido del lienzo a partir de {@code expresion}. */
    private static void actualizarVentana(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            ventanaDiagrama.setTitle("Diagrama de Thompson");
            lienzo.setMensaje("La expresión regular está vacía.", true);
            return;
        }
        List<PatronRegex> patrones;
        try {
            patrones = Ventana_Thompson.parsearPatrones(expresion);
        } catch (RuntimeException ex) {
            ventanaDiagrama.setTitle("Diagrama de Thompson");
            lienzo.setMensaje("No fue posible interpretar la expresión: "
                    + ex.getMessage(), true);
            return;
        }
        if (patrones.isEmpty()) {
            ventanaDiagrama.setTitle("Diagrama de Thompson");
            lienzo.setMensaje("La expresión regular está vacía.", true);
            return;
        }
        PatronRegex primero = patrones.get(0);
        try {
            Grafo g = construirGrafo(primero.regex);
            lienzo.setGrafo(g);
            if (patrones.size() > 1) {
                ventanaDiagrama.setTitle("Diagrama de Thompson  —  patrón '"
                        + primero.tipo + "'  (mostrando el primero de "
                        + patrones.size() + ")");
            } else {
                ventanaDiagrama.setTitle("Diagrama de Thompson  —  " + primero.regex);
            }
        } catch (RuntimeException ex) {
            ventanaDiagrama.setTitle("Diagrama de Thompson");
            lienzo.setMensaje("No fue posible construir el diagrama: "
                    + ex.getMessage(), true);
        }
    }

    // =================================================================
    //   MODELO DEL GRAFO (estructuras públicas mínimas).
    // =================================================================

    /** Estado del AFN representado como nodo del grafo. */
    static final class Nodo {
        final int id;
        double x, y;
        boolean inicial;
        boolean aceptante;
        Nodo(int id) { this.id = id; }
        String getLabel() { return "q" + id; }
    }

    /** Estilo geométrico de una arista. */
    enum EstiloArista { RECTA, CURVA_ARRIBA, CURVA_ABAJO }

    /** Transición del AFN representada como arista del grafo. */
    static final class Arista {
        final Nodo desde, hasta;
        final String etiqueta;
        final EstiloArista estilo;
        final double curvatura;
        Arista(Nodo desde, Nodo hasta, String etiqueta, EstiloArista estilo, double curvatura) {
            this.desde = desde;
            this.hasta = hasta;
            this.etiqueta = etiqueta;
            this.estilo = estilo;
            this.curvatura = curvatura;
        }
    }

    /** Grafo completo listo para pintar (coordenadas absolutas). */
    static final class Grafo {
        final List<Nodo> nodos = new ArrayList<>();
        final List<Arista> aristas = new ArrayList<>();
        int ancho;
        int alto;
        Nodo inicial;
        Nodo aceptante;
    }

    // =================================================================
    //   AST DE LA REGEX.
    // =================================================================

    private enum Op { SIMBOLO, CONCAT, UNION, ESTRELLA, MAS, OPCIONAL }

    private static final class Ast {
        final Op op;
        final Ast izq;
        final Ast der;
        final Set<Character> simbolos;
        final String etiquetaSimbolo;
        private Ast(Op op, Ast izq, Ast der, Set<Character> simbolos, String etiquetaSimbolo) {
            this.op = op; this.izq = izq; this.der = der;
            this.simbolos = simbolos; this.etiquetaSimbolo = etiquetaSimbolo;
        }
        static Ast simbolo(Set<Character> s, String etiqueta) {
            return new Ast(Op.SIMBOLO, null, null, s, etiqueta);
        }
        static Ast bin(Op op, Ast a, Ast b) { return new Ast(op, a, b, null, null); }
        static Ast un(Op op, Ast a)         { return new Ast(op, a, null, null, null); }
    }

    // =================================================================
    //   PARSER recursivo descendente (regex -> AST).
    // =================================================================

    private static final class Parser {
        private final String src;
        private int i;
        Parser(String src) { this.src = src; }

        Ast parseTodo() {
            saltarBlancos();
            Ast a = parseUnion();
            saltarBlancos();
            if (i < src.length()) {
                throw new RuntimeException("Carácter inesperado '"
                        + src.charAt(i) + "' en la posición " + i);
            }
            return a;
        }

        private Ast parseUnion() {
            Ast izq = parseConcat();
            while (acepta('|')) {
                izq = Ast.bin(Op.UNION, izq, parseConcat());
            }
            return izq;
        }

        private Ast parseConcat() {
            Ast izq = parsePostfijo();
            while (true) {
                saltarBlancos();
                if (i >= src.length()) break;
                char c = src.charAt(i);
                if (c == '|' || c == ')') break;
                izq = Ast.bin(Op.CONCAT, izq, parsePostfijo());
            }
            return izq;
        }

        private Ast parsePostfijo() {
            Ast a = parseAtomo();
            while (true) {
                saltarBlancos();
                if (i >= src.length()) break;
                char c = src.charAt(i);
                if      (c == '*') { i++; a = Ast.un(Op.ESTRELLA, a); }
                else if (c == '+') { i++; a = Ast.un(Op.MAS, a); }
                else if (c == '?') { i++; a = Ast.un(Op.OPCIONAL, a); }
                else break;
            }
            return a;
        }

        private Ast parseAtomo() {
            saltarBlancos();
            if (i >= src.length()) {
                throw new RuntimeException("Se esperaba un átomo en la posición " + i);
            }
            char c = src.charAt(i);
            if (c == '(') {
                i++;
                Ast a = parseUnion();
                saltarBlancos();
                if (i >= src.length() || src.charAt(i) != ')') {
                    throw new RuntimeException("Falta ')' que cierre el grupo");
                }
                i++;
                return a;
            }
            if (c == '[') {
                int cierre = encontrarCierreCorchete(src, i);
                if (cierre < 0) {
                    throw new RuntimeException("Falta ']' que cierre la clase en posición " + i);
                }
                Set<Character> clase = parsearClase(src.substring(i + 1, cierre));
                String etiqueta = src.substring(i, cierre + 1);
                i = cierre + 1;
                return Ast.simbolo(clase, etiqueta);
            }
            if (c == '\\') {
                if (i + 1 >= src.length()) {
                    throw new RuntimeException("Escape '\\' al final de la expresión");
                }
                char e = src.charAt(i + 1);
                Set<Character> s = new HashSet<>();
                s.add(e);
                i += 2;
                return Ast.simbolo(s, String.valueOf(e));
            }
            if (c == '|' || c == '*' || c == '+' || c == '?' || c == ')') {
                throw new RuntimeException("Operador inesperado '" + c + "' en la posición " + i);
            }
            Set<Character> s = new HashSet<>();
            s.add(c);
            i++;
            return Ast.simbolo(s, String.valueOf(c));
        }

        private boolean acepta(char esperado) {
            saltarBlancos();
            if (i < src.length() && src.charAt(i) == esperado) { i++; return true; }
            return false;
        }

        private void saltarBlancos() {
            while (i < src.length() && (src.charAt(i) == ' ' || src.charAt(i) == '\t')) i++;
        }
    }

    private static int encontrarCierreCorchete(String s, int abre) {
        int j = abre + 1;
        while (j < s.length()) {
            char c = s.charAt(j);
            if (c == '\\' && j + 1 < s.length()) { j += 2; continue; }
            if (c == ']') return j;
            j++;
        }
        return -1;
    }

    private static Set<Character> parsearClase(String contenido) {
        if (contenido.isEmpty()) throw new RuntimeException("Clase de caracteres vacía '[]'");
        boolean negada = false;
        int start = 0;
        if (contenido.charAt(0) == '^') {
            negada = true; start = 1;
            if (contenido.length() == 1) throw new RuntimeException("Clase negada vacía '[^]'");
        }
        Set<Character> conjunto = new HashSet<>();
        int i = start;
        while (i < contenido.length()) {
            char c = contenido.charAt(i);
            if (c == '\\' && i + 1 < contenido.length()) {
                conjunto.add(contenido.charAt(i + 1));
                i += 2;
                continue;
            }
            if (i + 2 < contenido.length() && contenido.charAt(i + 1) == '-'
                    && contenido.charAt(i + 2) != ']') {
                char fin = contenido.charAt(i + 2);
                if (c > fin) throw new RuntimeException("Rango inválido en clase: '" + c + "-" + fin + "'");
                for (char x = c; x <= fin; x++) conjunto.add(x);
                i += 3;
            } else {
                conjunto.add(c);
                i++;
            }
        }
        if (negada) {
            Set<Character> neg = new HashSet<>();
            for (int ch = 32; ch < 127; ch++) {
                char cc = (char) ch;
                if (!conjunto.contains(cc)) neg.add(cc);
            }
            return neg;
        }
        return conjunto;
    }

    // =================================================================
    //   CONSTRUCCIÓN DEL GRAFO — patrones estructurados por operación.
    //
    //   Cada operación produce una "Caja" con coordenadas relativas y
    //   dos puntos de conexión bien definidos: ENTRADA y SALIDA. La
    //   composición se hace siempre conectando salida -> entrada, lo
    //   que mantiene el grafo visualmente coherente.
    // =================================================================

    /** Radio del círculo que representa a un estado (pequeño pero legible). */
    static final int RADIO_NODO = 18;

    private static final int LONG_ARCO    = 46;  // distancia entrada-salida en un símbolo.
    private static final int HUECO_H      = 30;  // separación entre cajas en concat.
    private static final int HUECO_V      = 36;  // separación vertical entre ramas en unión.
    private static final int ESPACIO_ARCO = 36;  // espacio arriba/abajo para arcos curvos.
    private static final int MARGEN       = 28;  // margen alrededor del grafo.

    /** Construye el grafo completo a partir de la regex. */
    static Grafo construirGrafo(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            throw new IllegalArgumentException("Expresión regular vacía");
        }
        Parser parser = new Parser(regex);
        Ast ast = parser.parseTodo();

        Constructor c = new Constructor();
        Caja caja = c.construir(ast);

        Grafo g = new Grafo();
        for (Nodo n : caja.nodos) {
            n.x += MARGEN;
            n.y += MARGEN;
            g.nodos.add(n);
        }
        g.aristas.addAll(caja.aristas);
        g.ancho = (int) Math.ceil(caja.ancho + 2 * MARGEN);
        g.alto = (int) Math.ceil(caja.alto + 2 * MARGEN);
        g.inicial = caja.entrada;
        g.aceptante = caja.salida;
        g.inicial.inicial = true;
        g.aceptante.aceptante = true;
        return g;
    }

    /** Sub-grafo en coordenadas relativas, con entrada y salida explícitas. */
    private static final class Caja {
        double ancho;
        double alto;
        Nodo entrada;
        Nodo salida;
        final List<Nodo> nodos = new ArrayList<>();
        final List<Arista> aristas = new ArrayList<>();

        void trasladar(double dx, double dy) {
            for (Nodo n : nodos) { n.x += dx; n.y += dy; }
        }
        double maxX() { double m = 0; for (Nodo n : nodos) m = Math.max(m, n.x); return m; }
        double maxY() { double m = 0; for (Nodo n : nodos) m = Math.max(m, n.y); return m; }
    }

    /** Constructor recursivo del grafo (postorder sobre el AST). */
    private static final class Constructor {
        private int contadorEstados = 0;

        Caja construir(Ast ast) {
            switch (ast.op) {
                case SIMBOLO:  return cajaSimbolo(ast.simbolos, ast.etiquetaSimbolo);
                case CONCAT:   return cajaConcat(construir(ast.izq), construir(ast.der));
                case UNION:    return cajaUnion(construir(ast.izq), construir(ast.der));
                case ESTRELLA: return cajaCerradura(construir(ast.izq), true);
                case OPCIONAL: return cajaCerradura(construir(ast.izq), false);
                case MAS: {
                    // a+ = a · a*  (sub-AFN independiente para cada ocurrencia,
                    // equivalente al clon que hace ConstructorAFN_Thompson).
                    Caja primero = construir(ast.izq);
                    Caja segundo = cajaCerradura(construir(ast.izq), true);
                    return cajaConcat(primero, segundo);
                }
                default:
                    throw new RuntimeException("Operación no soportada: " + ast.op);
            }
        }

        private Nodo nuevoNodo() { return new Nodo(contadorEstados++); }

        // ----- símbolo:  (q) --a--> (q) -----
        private Caja cajaSimbolo(Set<Character> simbolos, String etiquetaLegible) {
            Caja b = new Caja();
            Nodo a = nuevoNodo();
            Nodo c = nuevoNodo();
            String etiqueta = (etiquetaLegible != null && !etiquetaLegible.isEmpty())
                    ? etiquetaLegible
                    : formatearSimbolos(simbolos);
            // El arco se alarga si la etiqueta es ancha para que no
            // se solape con los círculos vecinos.
            double anchoArco = Math.max(LONG_ARCO, anchoEstimadoTexto(etiqueta) + 20);
            a.x = RADIO_NODO;             a.y = RADIO_NODO;
            c.x = RADIO_NODO + anchoArco; c.y = RADIO_NODO;
            b.nodos.add(a); b.nodos.add(c);
            b.aristas.add(new Arista(a, c, etiqueta, EstiloArista.RECTA, 0));
            b.entrada = a; b.salida = c;
            b.ancho = c.x + RADIO_NODO;
            b.alto = 2 * RADIO_NODO;
            return b;
        }

        // ----- concatenación:  [A] --ε--> [B] -----
        private Caja cajaConcat(Caja a, Caja b) {
            // Alinear b.entrada.y con a.salida.y.
            double dy = a.salida.y - b.entrada.y;
            if (dy > 0)      b.trasladar(0, dy);
            else if (dy < 0) a.trasladar(0, -dy);
            // Desplazar b a la derecha de a.
            b.trasladar(a.ancho + HUECO_H, 0);

            Caja r = new Caja();
            r.nodos.addAll(a.nodos); r.nodos.addAll(b.nodos);
            r.aristas.addAll(a.aristas); r.aristas.addAll(b.aristas);
            r.aristas.add(new Arista(a.salida, b.entrada, EPSILON, EstiloArista.RECTA, 0));
            r.entrada = a.entrada; r.salida = b.salida;
            r.ancho = b.maxX() + RADIO_NODO;
            r.alto = Math.max(a.maxY(), b.maxY()) + RADIO_NODO;
            return r;
        }

        // ----- unión:  (S) -ε→ [A] -ε→ (E)  ;  (S) -ε→ [B] -ε→ (E) -----
        private Caja cajaUnion(Caja a, Caja b) {
            double anchoInterno = Math.max(a.ancho, b.ancho);
            if (a.ancho < anchoInterno) a.trasladar((anchoInterno - a.ancho) / 2, 0);
            if (b.ancho < anchoInterno) b.trasladar((anchoInterno - b.ancho) / 2, 0);
            // Apilar A arriba, B abajo.
            b.trasladar(0, a.alto + HUECO_V);
            // Reservar espacio a izquierda y derecha para S y E.
            double offsetX = 2 * RADIO_NODO + LONG_ARCO;
            a.trasladar(offsetX, 0); b.trasladar(offsetX, 0);

            Nodo S = nuevoNodo();
            Nodo E = nuevoNodo();
            double centroY = (a.entrada.y + b.entrada.y) / 2.0;
            S.x = RADIO_NODO;                                       S.y = centroY;
            E.x = offsetX + anchoInterno + LONG_ARCO + RADIO_NODO;  E.y = centroY;

            Caja r = new Caja();
            r.nodos.add(S);
            r.nodos.addAll(a.nodos); r.nodos.addAll(b.nodos);
            r.nodos.add(E);
            r.aristas.addAll(a.aristas); r.aristas.addAll(b.aristas);
            r.aristas.add(new Arista(S, a.entrada, EPSILON, EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(S, b.entrada, EPSILON, EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(a.salida, E, EPSILON, EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(b.salida, E, EPSILON, EstiloArista.RECTA, 0));
            r.entrada = S; r.salida = E;
            r.ancho = E.x + RADIO_NODO;
            r.alto = b.maxY() + RADIO_NODO;
            return r;
        }

        // ----- Kleene / opcional:
        //   ┌──── ε (skip, por arriba) ────┐
        //   │                                ↓
        //  (S) ──ε──→ [A.in ......... A.out] ──ε──→ (E)
        //                ↑                  │
        //                └── ε (back, abajo, sólo Kleene) ──┘
        private Caja cajaCerradura(Caja a, boolean conBackEdge) {
            double espacioArriba = ESPACIO_ARCO;
            double espacioAbajo = conBackEdge ? ESPACIO_ARCO : 0;
            a.trasladar(0, espacioArriba);

            double offsetX = 2 * RADIO_NODO + LONG_ARCO;
            a.trasladar(offsetX, 0);

            Nodo S = nuevoNodo();
            Nodo E = nuevoNodo();
            S.x = RADIO_NODO;                            S.y = a.entrada.y;
            E.x = a.salida.x + LONG_ARCO + RADIO_NODO;   E.y = a.salida.y;

            Caja r = new Caja();
            r.nodos.add(S); r.nodos.addAll(a.nodos); r.nodos.add(E);
            r.aristas.addAll(a.aristas);
            r.aristas.add(new Arista(S, a.entrada, EPSILON, EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(a.salida, E, EPSILON, EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(S, E, EPSILON, EstiloArista.CURVA_ARRIBA, espacioArriba - 6));
            if (conBackEdge) {
                r.aristas.add(new Arista(a.salida, a.entrada, EPSILON, EstiloArista.CURVA_ABAJO, espacioAbajo - 6));
            }
            r.entrada = S; r.salida = E;
            r.ancho = E.x + RADIO_NODO;
            r.alto = espacioArriba + a.alto + espacioAbajo;
            return r;
        }

        /** Estimación heurística del ancho en píxeles que ocupará un texto. */
        private static double anchoEstimadoTexto(String s) {
            if (s == null) return 0;
            return 7.5 * s.length() + 8;
        }
    }

    // =================================================================
    //   LIENZO: JPanel interno que pinta el grafo.
    // =================================================================

    static final class LienzoGrafo extends JPanel {

        // Estilo gráfico.
        private static final Color COLOR_FONDO        = Color.WHITE;
        private static final Color COLOR_NODO         = new Color(0xFF, 0xF9, 0xE6);
        private static final Color COLOR_NODO_INI     = new Color(0xD9, 0xEC, 0xFB);
        private static final Color COLOR_NODO_ACEPT   = new Color(0xD7, 0xF1, 0xD9);
        private static final Color COLOR_BORDE        = new Color(0x33, 0x33, 0x33);
        private static final Color COLOR_BORDE_ACEPT  = new Color(0x1B, 0x6E, 0x2B);
        private static final Color COLOR_BORDE_INI    = new Color(0x1F, 0x57, 0x8A);
        private static final Color COLOR_ARISTA       = new Color(0x33, 0x33, 0x33);
        private static final Color COLOR_ETIQUETA     = new Color(0x10, 0x10, 0x10);
        private static final Color COLOR_INFO         = new Color(0x70, 0x70, 0x70);
        private static final Color COLOR_ERROR        = new Color(0xA8, 0x2A, 0x2A);

        private static final Font FUENTE_NODO     = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        private static final Font FUENTE_ETIQUETA = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
        private static final Font FUENTE_INFO     = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

        private static final Stroke TRAZO_NODO   = new BasicStroke(1.5f);
        private static final Stroke TRAZO_ARISTA = new BasicStroke(1.2f);
        private static final int FLECHA_LONG  = 9;
        private static final int FLECHA_ANCHO = 5;

        private Grafo grafo;
        private String mensaje;
        private boolean mensajeEsError;

        LienzoGrafo() {
            setBackground(COLOR_FONDO);
            setOpaque(true);
            setPreferredSize(new Dimension(400, 200));
        }

        void setGrafo(Grafo grafo) {
            this.grafo = grafo;
            this.mensaje = null;
            this.mensajeEsError = false;
            if (grafo != null) {
                setPreferredSize(new Dimension(
                        Math.max(grafo.ancho, 200),
                        Math.max(grafo.alto, 100)));
            }
            revalidate();
            repaint();
        }

        void setMensaje(String mensaje, boolean esError) {
            this.grafo = null;
            this.mensaje = mensaje;
            this.mensajeEsError = esError;
            setPreferredSize(new Dimension(400, 200));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                if (grafo == null) { pintarMensaje(g2); return; }
                pintarGrafo(g2);
            } finally {
                g2.dispose();
            }
        }

        private void pintarMensaje(Graphics2D g2) {
            String texto = mensaje != null
                    ? mensaje
                    : "Escriba una expresión regular y abra el diagrama.";
            g2.setColor(mensajeEsError ? COLOR_ERROR : COLOR_INFO);
            g2.setFont(FUENTE_INFO);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(texto)) / 2;
            g2.drawString(texto, Math.max(10, x), getHeight() / 2);
        }

        private void pintarGrafo(Graphics2D g2) {
            // 1) Aristas primero (los círculos van encima).
            g2.setStroke(TRAZO_ARISTA);
            g2.setFont(FUENTE_ETIQUETA);
            for (Arista a : grafo.aristas) pintarArista(g2, a);

            // 2) Marcador "→" hacia el estado inicial.
            if (grafo.inicial != null) pintarMarcadorInicial(g2, grafo.inicial);

            // 3) Nodos.
            g2.setStroke(TRAZO_NODO);
            g2.setFont(FUENTE_NODO);
            for (Nodo n : grafo.nodos) pintarNodo(g2, n);
        }

        private void pintarNodo(Graphics2D g2, Nodo n) {
            int r = RADIO_NODO;
            double cx = n.x, cy = n.y;
            Ellipse2D e = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);

            Color relleno, borde;
            if (n.aceptante)    { relleno = COLOR_NODO_ACEPT; borde = COLOR_BORDE_ACEPT; }
            else if (n.inicial) { relleno = COLOR_NODO_INI;   borde = COLOR_BORDE_INI; }
            else                { relleno = COLOR_NODO;       borde = COLOR_BORDE; }
            g2.setColor(relleno); g2.fill(e);
            g2.setColor(borde);   g2.draw(e);
            // Doble círculo en estado aceptante.
            if (n.aceptante) {
                int rr = r - 3;
                g2.draw(new Ellipse2D.Double(cx - rr, cy - rr, 2 * rr, 2 * rr));
            }
            g2.setColor(COLOR_ETIQUETA);
            FontMetrics fm = g2.getFontMetrics();
            String label = n.getLabel();
            int tx = (int) Math.round(cx - fm.stringWidth(label) / 2.0);
            int ty = (int) Math.round(cy + (fm.getAscent() - fm.getDescent()) / 2.0);
            g2.drawString(label, tx, ty);
        }

        private void pintarMarcadorInicial(Graphics2D g2, Nodo ini) {
            int r = RADIO_NODO;
            double endX = ini.x - r, endY = ini.y;
            double startX = endX - 16, startY = endY;
            g2.setColor(COLOR_BORDE_INI);
            g2.setStroke(TRAZO_ARISTA);
            g2.draw(new Line2D.Double(startX, startY, endX, endY));
            pintarCabezaFlecha(g2, endX, endY, 1.0, 0.0, COLOR_BORDE_INI);
        }

        private void pintarArista(Graphics2D g2, Arista a) {
            double ax = a.desde.x, ay = a.desde.y;
            double bx = a.hasta.x, by = a.hasta.y;
            switch (a.estilo) {
                case CURVA_ARRIBA: pintarCurva(g2, ax, ay, bx, by, -1, a.curvatura, a.etiqueta); break;
                case CURVA_ABAJO:  pintarCurva(g2, ax, ay, bx, by, +1, a.curvatura, a.etiqueta); break;
                case RECTA:
                default:           pintarRecta(g2, ax, ay, bx, by, a.etiqueta);
            }
        }

        private void pintarRecta(Graphics2D g2, double ax, double ay, double bx, double by, String etiqueta) {
            int r = RADIO_NODO;
            double dx = bx - ax, dy = by - ay;
            double dist = Math.hypot(dx, dy);
            if (dist < 1e-6) return;
            double ux = dx / dist, uy = dy / dist;
            double sx = ax + ux * r, sy = ay + uy * r;
            double ex = bx - ux * r, ey = by - uy * r;
            g2.setColor(COLOR_ARISTA);
            g2.draw(new Line2D.Double(sx, sy, ex, ey));
            pintarCabezaFlecha(g2, ex, ey, ux, uy, COLOR_ARISTA);

            if (etiqueta != null && !etiqueta.isEmpty()) {
                double mx = (sx + ex) / 2, my = (sy + ey) / 2;
                double px = -uy, py = ux;
                if (py > 0) { px = -px; py = -py; }
                pintarEtiqueta(g2, etiqueta, mx + px * 10, my + py * 10);
            }
        }

        private void pintarCurva(Graphics2D g2, double ax, double ay, double bx, double by,
                                 int signoVertical, double curvatura, String etiqueta) {
            int r = RADIO_NODO;
            double mx = (ax + bx) / 2, my = (ay + by) / 2;
            double offset = Math.max(curvatura, 18);
            double cx = mx, cy = my + signoVertical * offset;

            double t0x = cx - ax, t0y = cy - ay; double l0 = Math.hypot(t0x, t0y);
            double sx = ax + (t0x / l0) * r,     sy = ay + (t0y / l0) * r;
            double t1x = cx - bx, t1y = cy - by; double l1 = Math.hypot(t1x, t1y);
            double ex = bx + (t1x / l1) * r,     ey = by + (t1y / l1) * r;

            QuadCurve2D q = new QuadCurve2D.Double(sx, sy, cx, cy, ex, ey);
            g2.setColor(COLOR_ARISTA);
            g2.draw(q);

            double tex = 2 * (ex - cx), tey = 2 * (ey - cy);
            double tl = Math.hypot(tex, tey);
            if (tl > 1e-6) {
                pintarCabezaFlecha(g2, ex, ey, tex / tl, tey / tl, COLOR_ARISTA);
            }
            if (etiqueta != null && !etiqueta.isEmpty()) {
                double lx = (sx + 2 * cx + ex) / 4;
                double ly = (sy + 2 * cy + ey) / 4 + signoVertical * 8;
                pintarEtiqueta(g2, etiqueta, lx, ly);
            }
        }

        private void pintarCabezaFlecha(Graphics2D g2, double x, double y,
                                        double ux, double uy, Color color) {
            double bx = x - ux * FLECHA_LONG, by = y - uy * FLECHA_LONG;
            double px = -uy, py = ux;
            double x1 = bx + px * FLECHA_ANCHO, y1 = by + py * FLECHA_ANCHO;
            double x2 = bx - px * FLECHA_ANCHO, y2 = by - py * FLECHA_ANCHO;
            Path2D.Double cab = new Path2D.Double();
            cab.moveTo(x, y); cab.lineTo(x1, y1); cab.lineTo(x2, y2); cab.closePath();
            Color anterior = g2.getColor();
            g2.setColor(color); g2.fill(cab);
            g2.setColor(anterior);
        }

        private void pintarEtiqueta(Graphics2D g2, String texto, double x, double y) {
            Font anterior = g2.getFont();
            g2.setFont(FUENTE_ETIQUETA);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(texto);
            int h = fm.getAscent();
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRect((int) Math.round(x - w / 2.0) - 2,
                        (int) Math.round(y - h / 2.0) - 1,
                        w + 4, h + 2);
            g2.setColor(COLOR_ETIQUETA);
            g2.drawString(texto,
                    (int) Math.round(x - w / 2.0),
                    (int) Math.round(y + h / 2.0 - 1));
            g2.setFont(anterior);
        }
    }
}
