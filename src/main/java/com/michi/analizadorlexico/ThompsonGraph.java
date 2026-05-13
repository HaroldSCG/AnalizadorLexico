/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.michi.analizadorlexico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modelo del grafo del AFN construido con el algoritmo de Thompson, junto
 * con el layout en coordenadas absolutas listo para dibujar en un
 * {@link ThompsonGraphPanel}.
 *
 * <p>El grafo se construye a partir de la expresión regular original
 * usando patrones de layout estructurados por cada operación de
 * Thompson, en lugar de un layout genérico. Para cada construcción
 * (símbolo, concatenación, unión, Kleene, una-o-más y opcional) se
 * coloca un sub-grafo con <i>una entrada y una salida explícitas</i>,
 * de modo que la composición resulta visualmente coherente: la salida
 * de un fragmento conecta siempre con la entrada del siguiente.</p>
 *
 * <p>Cada estado se numera ({@code q0, q1, ...}) en el mismo orden en
 * que lo numera {@link Ventana_Thompson.ConstructorAFN_Thompson},
 * de manera que los identificadores del diagrama coinciden con los
 * que aparecen en {@code tTrans}.</p>
 *
 * @author harol
 */
public final class ThompsonGraph {

    // ----- Constantes geométricas (en píxeles "lógicos"). -----

    /** Radio del círculo que representa a un estado. */
    public static final int RADIO_NODO = 18;

    /** Separación horizontal entre la entrada y la salida de un símbolo. */
    static final int LONG_ARCO = 46;

    /** Separación horizontal entre sub-cajas consecutivas (concatenación). */
    static final int HUECO_H = 30;

    /** Separación vertical entre ramas (unión). */
    static final int HUECO_V = 36;

    /** Espacio extra arriba/abajo para arcos de salto / vuelta en Kleene/?. */
    static final int ESPACIO_ARCO = 36;

    /** Margen del lienzo alrededor del grafo. */
    static final int MARGEN = 28;

    // ----- Datos del grafo expuestos al panel. -----

    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();
    private int ancho;
    private int alto;
    private Nodo inicial;
    private Nodo aceptante;

    private ThompsonGraph() { /* uso interno: construir vía fromRegex(). */ }

    /** Construye el grafo a partir de una expresión regular. */
    public static ThompsonGraph fromRegex(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            throw new IllegalArgumentException("Expresión regular vacía");
        }
        Parser parser = new Parser(regex);
        Ast ast = parser.parseTodo();
        Constructor c = new Constructor();
        Caja caja = c.construir(ast);

        ThompsonGraph g = new ThompsonGraph();
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
        g.aceptante.aceptante = true;
        g.inicial.inicial = true;
        return g;
    }

    // ----- API consultada por el panel. -----

    public List<Nodo> getNodos()       { return Collections.unmodifiableList(nodos); }
    public List<Arista> getAristas()   { return Collections.unmodifiableList(aristas); }
    public int getAncho()              { return ancho; }
    public int getAlto()               { return alto; }
    public Nodo getInicial()           { return inicial; }
    public Nodo getAceptante()         { return aceptante; }

    // =================================================================
    //   ESTRUCTURAS PÚBLICAS — un Nodo es un estado; una Arista, una transición.
    // =================================================================

    public static final class Nodo {
        final int id;
        double x, y;
        boolean inicial;
        boolean aceptante;

        Nodo(int id) { this.id = id; }

        public int getId()           { return id; }
        public double getX()         { return x; }
        public double getY()         { return y; }
        public boolean isInicial()   { return inicial; }
        public boolean isAceptante() { return aceptante; }
        public String getLabel()     { return "q" + id; }
    }

    /** Estilo geométrico de una arista (define cómo el panel la dibuja). */
    public enum EstiloArista {
        /** Línea recta. */
        RECTA,
        /** Arco que se curva por arriba de la línea origen→destino. */
        CURVA_ARRIBA,
        /** Arco que se curva por abajo de la línea origen→destino. */
        CURVA_ABAJO,
        /** Auto-bucle (origen == destino). */
        BUCLE
    }

    public static final class Arista {
        final Nodo desde;
        final Nodo hasta;
        final String etiqueta;
        final EstiloArista estilo;
        /** Magnitud (en píxeles) del desvío del punto de control de la curva. */
        final double curvatura;

        Arista(Nodo desde, Nodo hasta, String etiqueta, EstiloArista estilo, double curvatura) {
            this.desde = desde;
            this.hasta = hasta;
            this.etiqueta = etiqueta;
            this.estilo = estilo;
            this.curvatura = curvatura;
        }

        public Nodo getDesde()         { return desde; }
        public Nodo getHasta()         { return hasta; }
        public String getEtiqueta()    { return etiqueta; }
        public EstiloArista getEstilo(){ return estilo; }
        public double getCurvatura()   { return curvatura; }
    }

    // =================================================================
    //   AST de la expresión regular.
    // =================================================================

    private enum Op { SIMBOLO, CONCAT, UNION, ESTRELLA, MAS, OPCIONAL }

    private static final class Ast {
        final Op op;
        final Ast izq;
        final Ast der;
        final Set<Character> simbolos;
        final String etiquetaSimbolo;

        private Ast(Op op, Ast izq, Ast der, Set<Character> simbolos, String etiquetaSimbolo) {
            this.op = op;
            this.izq = izq;
            this.der = der;
            this.simbolos = simbolos;
            this.etiquetaSimbolo = etiquetaSimbolo;
        }

        static Ast simbolo(Set<Character> s, String etiqueta) {
            return new Ast(Op.SIMBOLO, null, null, s, etiqueta);
        }
        static Ast bin(Op op, Ast a, Ast b) { return new Ast(op, a, b, null, null); }
        static Ast un(Op op, Ast a)         { return new Ast(op, a, null, null, null); }
    }

    // =================================================================
    //   PARSER (recursivo descendente) de la regex a AST.
    // =================================================================

    private static final class Parser {
        private final String src;
        private int i;

        Parser(String src) { this.src = src; }

        Ast parseTodo() {
            // Permite espacios en la regex como hace ConstructorAFN_Thompson.
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
                Ast der = parseConcat();
                izq = Ast.bin(Op.UNION, izq, der);
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
                Ast der = parsePostfijo();
                izq = Ast.bin(Op.CONCAT, izq, der);
            }
            return izq;
        }

        private Ast parsePostfijo() {
            Ast a = parseAtomo();
            while (true) {
                saltarBlancos();
                if (i >= src.length()) break;
                char c = src.charAt(i);
                if (c == '*')      { i++; a = Ast.un(Op.ESTRELLA, a); }
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
    //   CAJA — sub-grafo con coordenadas relativas y entrada/salida.
    // =================================================================

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

        double maxX() {
            double m = 0;
            for (Nodo n : nodos) m = Math.max(m, n.x);
            return m;
        }

        double maxY() {
            double m = 0;
            for (Nodo n : nodos) m = Math.max(m, n.y);
            return m;
        }
    }

    // =================================================================
    //   CONSTRUCTOR — recorre el AST y produce la caja final.
    // =================================================================

    private static final class Constructor {

        private int contadorEstados = 0;

        Caja construir(Ast ast) {
            switch (ast.op) {
                case SIMBOLO:  return cajaSimbolo(ast.simbolos, ast.etiquetaSimbolo);
                case CONCAT:   return cajaConcat(construir(ast.izq), construir(ast.der));
                case UNION:    return cajaUnion(construir(ast.izq), construir(ast.der));
                case ESTRELLA: return cajaEstrella(construir(ast.izq));
                case OPCIONAL: return cajaOpcional(construir(ast.izq));
                case MAS: {
                    // a+ = a · a*  con un sub-AFN independiente para cada ocurrencia
                    // (lo equivalente a clonar 'a').
                    Caja primero = construir(ast.izq);
                    Caja segundo = cajaEstrella(construir(ast.izq));
                    return cajaConcat(primero, segundo);
                }
                default:
                    throw new RuntimeException("Operación no soportada: " + ast.op);
            }
        }

        private Nodo nuevoNodo() {
            return new Nodo(contadorEstados++);
        }

        // ----- Patrón: símbolo -----
        //
        //   (qN) --a--> (qM)
        //
        private Caja cajaSimbolo(Set<Character> simbolos, String etiquetaLegible) {
            Caja b = new Caja();
            Nodo a = nuevoNodo();
            Nodo c = nuevoNodo();
            String etiqueta = (etiquetaLegible != null && !etiquetaLegible.isEmpty())
                    ? etiquetaLegible
                    : formatearSimbolos(simbolos);
            // Asegurar que la etiqueta quepa entre los dos círculos: si la
            // etiqueta es ancha (por ejemplo, una clase '[a-zA-Z_]'),
            // alargamos el arco para que no se solape con los nodos.
            double anchoArco = Math.max(LONG_ARCO, anchoEstimadoTexto(etiqueta) + 20);
            a.x = RADIO_NODO;             a.y = RADIO_NODO;
            c.x = RADIO_NODO + anchoArco; c.y = RADIO_NODO;
            b.nodos.add(a);
            b.nodos.add(c);
            b.aristas.add(new Arista(a, c, etiqueta, EstiloArista.RECTA, 0));
            b.entrada = a;
            b.salida = c;
            b.ancho = c.x + RADIO_NODO;
            b.alto = 2 * RADIO_NODO;
            return b;
        }

        /**
         * Estimación heurística del ancho en píxeles que ocupará una
         * cadena renderizada por el panel a 11 pt sans-serif. Se prefiere
         * sobrestimar ligeramente para garantizar que no haya solapamientos
         * entre la etiqueta y los círculos de los nodos vecinos.
         */
        private static double anchoEstimadoTexto(String s) {
            if (s == null) return 0;
            return 7.5 * s.length() + 8;
        }

        // ----- Patrón: concatenación  A · B -----
        //
        //   [A.in ...... A.out] --ε--> [B.in ...... B.out]
        //
        private Caja cajaConcat(Caja a, Caja b) {
            // Alinear b.entrada.y con a.salida.y.
            double dy = a.salida.y - b.entrada.y;
            if (dy > 0)      b.trasladar(0, dy);
            else if (dy < 0) a.trasladar(0, -dy);

            // Desplazar b a la derecha de a con el hueco de separación.
            double dx = a.ancho + HUECO_H;
            b.trasladar(dx, 0);

            Caja r = new Caja();
            r.nodos.addAll(a.nodos);
            r.nodos.addAll(b.nodos);
            r.aristas.addAll(a.aristas);
            r.aristas.addAll(b.aristas);
            r.aristas.add(new Arista(a.salida, b.entrada, "ε", EstiloArista.RECTA, 0));
            r.entrada = a.entrada;
            r.salida = b.salida;
            r.ancho = b.maxX() + RADIO_NODO;
            r.alto = Math.max(a.maxY(), b.maxY()) + RADIO_NODO;
            return r;
        }

        // ----- Patrón: unión  A | B -----
        //
        //                ε              ε
        //            ┌─→ [A] ─┐
        //          (S)         (E)
        //            └─→ [B] ─┘
        //                ε              ε
        //
        private Caja cajaUnion(Caja a, Caja b) {
            double anchoInterno = Math.max(a.ancho, b.ancho);

            // Centrar cada rama horizontalmente en el ancho compartido.
            if (a.ancho < anchoInterno) a.trasladar((anchoInterno - a.ancho) / 2, 0);
            if (b.ancho < anchoInterno) b.trasladar((anchoInterno - b.ancho) / 2, 0);

            // Apilar A arriba, B abajo, con un hueco vertical.
            b.trasladar(0, a.alto + HUECO_V);

            // Reservar espacio a izquierda y derecha para S y E.
            double offsetX = 2 * RADIO_NODO + LONG_ARCO;
            a.trasladar(offsetX, 0);
            b.trasladar(offsetX, 0);

            Nodo S = nuevoNodo();
            Nodo E = nuevoNodo();
            double centroY = (a.entrada.y + b.entrada.y) / 2.0;
            S.x = RADIO_NODO;
            S.y = centroY;
            E.x = offsetX + anchoInterno + LONG_ARCO + RADIO_NODO;
            E.y = centroY;

            Caja r = new Caja();
            r.nodos.add(S);
            r.nodos.addAll(a.nodos);
            r.nodos.addAll(b.nodos);
            r.nodos.add(E);
            r.aristas.addAll(a.aristas);
            r.aristas.addAll(b.aristas);
            // S → entradas (recta — pequeña inclinación, suficiente).
            r.aristas.add(new Arista(S, a.entrada, "ε", EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(S, b.entrada, "ε", EstiloArista.RECTA, 0));
            // Salidas → E.
            r.aristas.add(new Arista(a.salida, E, "ε", EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(b.salida, E, "ε", EstiloArista.RECTA, 0));
            r.entrada = S;
            r.salida = E;
            r.ancho = E.x + RADIO_NODO;
            r.alto = b.maxY() + RADIO_NODO;
            return r;
        }

        // ----- Patrón: cerradura de Kleene  A* -----
        //
        //       ┌─────────────── ε (skip, por arriba) ───────────────┐
        //       │                                                     ↓
        //      (S) ──ε──→ [A.in ......... A.out] ──ε──→ (E)
        //                    ↑                  │
        //                    └──── ε (back, por abajo) ──┘
        //
        private Caja cajaEstrella(Caja a) {
            return cajaCerradura(a, true);
        }

        // ----- Patrón: opcional  A? -----
        //
        //       ┌─── ε (skip, por arriba) ───┐
        //       │                             ↓
        //      (S) ──ε──→ [A.in ... A.out] ──ε──→ (E)
        //
        private Caja cajaOpcional(Caja a) {
            return cajaCerradura(a, false);
        }

        /** Implementación común para Kleene (con back-edge) y opcional (sin él). */
        private Caja cajaCerradura(Caja a, boolean conBackEdge) {
            // Espacio extra arriba y abajo para los arcos curvos.
            double espacioArriba = ESPACIO_ARCO;
            double espacioAbajo = conBackEdge ? ESPACIO_ARCO : 0;
            a.trasladar(0, espacioArriba);

            // Reservar espacio a la izquierda para S y a la derecha para E.
            double offsetX = 2 * RADIO_NODO + LONG_ARCO;
            a.trasladar(offsetX, 0);

            Nodo S = nuevoNodo();
            Nodo E = nuevoNodo();
            S.x = RADIO_NODO;
            S.y = a.entrada.y;
            E.x = a.salida.x + LONG_ARCO + RADIO_NODO;
            E.y = a.salida.y;

            Caja r = new Caja();
            r.nodos.add(S);
            r.nodos.addAll(a.nodos);
            r.nodos.add(E);
            r.aristas.addAll(a.aristas);

            // Conexiones internas (rectas).
            r.aristas.add(new Arista(S, a.entrada, "ε", EstiloArista.RECTA, 0));
            r.aristas.add(new Arista(a.salida, E, "ε", EstiloArista.RECTA, 0));

            // Arco de salto S → E por arriba.
            double curvaSkip = espacioArriba - 6;
            r.aristas.add(new Arista(S, E, "ε", EstiloArista.CURVA_ARRIBA, curvaSkip));

            if (conBackEdge) {
                // Back-edge A.salida → A.entrada por abajo.
                double curvaBack = espacioAbajo - 6;
                r.aristas.add(new Arista(a.salida, a.entrada, "ε", EstiloArista.CURVA_ABAJO, curvaBack));
            }

            r.entrada = S;
            r.salida = E;
            r.ancho = E.x + RADIO_NODO;
            r.alto = espacioArriba + a.alto + espacioAbajo;
            return r;
        }

    }

    // =================================================================
    //   FORMATEO DE SÍMBOLOS PARA LAS ETIQUETAS DE LAS ARISTAS.
    // =================================================================

    /** Versión compacta usada cuando una clase de caracteres no tiene etiqueta textual original. */
    static String formatearSimbolos(Set<Character> simbolos) {
        if (simbolos == null || simbolos.isEmpty()) return "∅";
        if (simbolos.size() == 1) {
            return escaparMostrable(simbolos.iterator().next());
        }
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
            case ' ':  return "␣";
            default:
                if (c < 32 || c == 127) return String.format("\\x%02X", (int) c);
                return String.valueOf(c);
        }
    }
}
