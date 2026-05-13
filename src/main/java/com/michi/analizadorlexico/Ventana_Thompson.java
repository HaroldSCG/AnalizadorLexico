/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.michi.analizadorlexico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

/**
 * Ventana_Thompson — Analizador léxico construido desde cero.
 *
 * <p>El proceso completo está separado en tres capas independientes para
 * mantener cada responsabilidad bien acotada:</p>
 *
 * <ol>
 *   <li><b>Construcción del AFN</b> ({@link ConstructorAFN_Thompson}): a
 *       partir de una o varias expresiones regulares produce un AFN
 *       aplicando el algoritmo de Thompson. Las construcciones son
 *       no-destructivas en los puntos en que reutilizar un mismo fragmento
 *       podría introducir efectos colaterales (operador {@code +}, que se
 *       reduce a {@code A · A*} usando un clon profundo de A).</li>
 *   <li><b>Simulación del AFN</b> ({@link SimuladorAFN}): implementa la
 *       clausura-ε y la operación {@code move}, y entrega el lexema más
 *       largo aceptado a partir de una posición junto con el tipo de
 *       token asociado al estado de aceptación alcanzado.</li>
 *   <li><b>Análisis léxico</b> ({@link AnalizadorLexico}): recorre la
 *       cadena de entrada usando el simulador, agrupa los tokens por
 *       máxima coincidencia, mantiene la posición (línea, columna), y
 *       refina la clasificación cuando corresponde (palabras reservadas
 *       son una refinación de identificadores).</li>
 * </ol>
 *
 * <p>El usuario puede ingresar:</p>
 *
 * <ul>
 *   <li>Una sola expresión regular (modo automático con clasificación
 *       heurística posterior: identificador / número / símbolo /
 *       palabra reservada).</li>
 *   <li>Varios patrones nombrados separados por {@code ;}, con sintaxis
 *       {@code TIPO=regex}. En este caso el tipo del token sale
 *       directamente del estado de aceptación del AFN que dispara la
 *       coincidencia. Ejemplo recomendado:
 *       <pre>
 *  IDENTIFICADOR=[a-zA-Z_][a-zA-Z0-9_]*; NUMERO=[0-9]+; SIMBOLO=[+\-/=&lt;&gt;;,(){}]
 *       </pre>
 *   </li>
 * </ul>
 *
 * <p>No se utilizan librerías externas: sólo {@code java.util} para
 * estructuras y Swing para la GUI.</p>
 *
 * @author harol
 */
public class Ventana_Thompson extends javax.swing.JFrame {

    /** Sentinel para tipos "auto-clasificados" cuando hay un único patrón sin etiqueta. */
    private static final String TIPO_AUTO = "AUTO";

    /** Palabras reservadas reconocidas como refinamiento de los identificadores. */
    private static final Set<String> PALABRAS_RESERVADAS = new HashSet<>();
    static {
        String[] reservadas = {
            // Comunes en pseudocódigo / español
            "si", "sino", "entonces", "mientras", "para", "hacer", "fin",
            "inicio", "imprimir", "leer", "escribir", "retornar", "romper",
            "continuar", "entero", "real", "cadena", "booleano",
            "verdadero", "falso",
            // Comunes en lenguajes tipo C / Java
            "if", "else", "while", "for", "do", "return", "break", "continue",
            "switch", "case", "default", "int", "float", "double", "char",
            "void", "class", "public", "private", "protected", "static",
            "true", "false", "null", "new"
        };
        for (String r : reservadas) {
            PALABRAS_RESERVADAS.add(r);
        }
    }

    public Ventana_Thompson() {
        initComponents();
        inicializarTabla();

        bAnalizar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ejecutarAnalisis();
            }
        });

        setTitle("Analizador Léxico - Algoritmo de Thompson");
        setLocationRelativeTo(null);
    }

    private void inicializarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Lexema", "Tipo", "Valor", "Línea", "Columna"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaSimbolo.setModel(modelo);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tExpresion = new javax.swing.JTextField();
        tIngreso = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tSalida = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tError = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaSimbolo = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        bAnalizar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(936, 600));

        tIngreso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tIngresoActionPerformed(evt);
            }
        });

        tSalida.setColumns(20);
        tSalida.setRows(5);
        jScrollPane2.setViewportView(tSalida);

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));

        tError.setColumns(20);
        tError.setRows(5);
        jScrollPane3.setViewportView(tError);

        tablaSimbolo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tablaSimbolo);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel1.setText("Expresión Regular:");

        jLabel2.setText("Cadena de Entrada:");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Resultado:");

        bAnalizar.setText("Analizar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addComponent(tExpresion, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tIngreso, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(142, 142, 142)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bAnalizar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(156, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tExpresion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bAnalizar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tIngresoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tIngresoActionPerformed
        ejecutarAnalisis();
    }//GEN-LAST:event_tIngresoActionPerformed

    // =================================================================
    //   ORQUESTACIÓN GUI ↔ MOTOR
    // =================================================================

    private void ejecutarAnalisis() {
        tSalida.setText("");
        tError.setText("");
        DefaultTableModel modelo = (DefaultTableModel) tablaSimbolo.getModel();
        modelo.setRowCount(0);

        String expresion = tExpresion.getText();
        String entrada = tIngreso.getText();

        if (expresion == null || expresion.trim().isEmpty()) {
            tError.append("Error: la expresión regular está vacía.\n");
            return;
        }

        // 1) Parsear la cadena del usuario en una lista de patrones (1..N).
        List<PatronRegex> patrones;
        try {
            patrones = parsearPatrones(expresion);
            if (patrones.isEmpty()) {
                tError.append("Error: la expresión regular está vacía.\n");
                return;
            }
        } catch (RuntimeException ex) {
            tError.append("Error al interpretar los patrones: " + ex.getMessage() + "\n");
            return;
        }

        // 2) Construir el AFN maestro con Thompson.
        AFN afn;
        try {
            ConstructorAFN_Thompson constructor = new ConstructorAFN_Thompson();
            afn = constructor.construirMultiple(patrones);
        } catch (RuntimeException ex) {
            tError.append("Error en la expresión regular: " + ex.getMessage() + "\n");
            return;
        }

        // 3) Analizar la cadena de entrada con el simulador del AFN.
        SimuladorAFN simulador = new SimuladorAFN(afn);
        AnalizadorLexico lexer = new AnalizadorLexico(simulador);
        ResultadoAnalisis resultado = lexer.analizar(entrada == null ? "" : entrada);

        // 4) Volcado de tokens.
        StringBuilder sbTokens = new StringBuilder();
        sbTokens.append("Tokens reconocidos (")
                .append(resultado.tokens.size()).append("):\n");
        sbTokens.append("-----------------------------------------------------------\n");
        sbTokens.append(String.format("%-20s %-22s %-8s %-8s%n",
                "Lexema", "Tipo", "Línea", "Columna"));
        sbTokens.append("-----------------------------------------------------------\n");
        for (Token t : resultado.tokens) {
            sbTokens.append(String.format("%-20s %-22s %-8d %-8d%n",
                    t.lexema, t.tipo, t.linea, t.columna));
        }
        tSalida.setText(sbTokens.toString());

        // 5) Tabla de símbolos (sin duplicados por tipo + lexema; conserva primer hallazgo).
        Map<String, Boolean> agregados = new HashMap<>();
        for (Token t : resultado.tokens) {
            String clave = t.tipo + "::" + t.lexema;
            if (!agregados.containsKey(clave)) {
                agregados.put(clave, Boolean.TRUE);
                modelo.addRow(new Object[]{t.lexema, t.tipo, t.valor, t.linea, t.columna});
            }
        }

        // 6) Errores léxicos.
        if (resultado.errores.isEmpty()) {
            tError.setText("Sin errores léxicos.\n");
        } else {
            StringBuilder sbErr = new StringBuilder();
            sbErr.append("Errores léxicos (").append(resultado.errores.size()).append("):\n");
            sbErr.append("-----------------------------------------------\n");
            for (ErrorLexico e : resultado.errores) {
                sbErr.append("Línea ").append(e.linea)
                     .append(", Columna ").append(e.columna)
                     .append(": secuencia no válida '").append(e.lexema).append("'\n");
            }
            tError.setText(sbErr.toString());
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ventana_Thompson.class.getName())
                .log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ventana_Thompson().setVisible(true);
            }
        });
    }

    // =================================================================
    //   PARSEO DE LOS PATRONES DEL USUARIO
    // =================================================================

    /** Patrón "TIPO=regex" (o regex suelta con tipo AUTO). */
    static class PatronRegex {
        final String tipo;
        final String regex;

        PatronRegex(String tipo, String regex) {
            this.tipo = tipo;
            this.regex = regex;
        }
    }

    /**
     * Divide el texto del campo de expresión regular en patrones.
     *
     * <p>Hay dos modos:</p>
     * <ul>
     *   <li><b>Multi-patrón</b>: si el texto contiene al menos un
     *       {@code =} a nivel superior (interpretado como
     *       {@code TIPO=regex}), se considera entrada multi-patrón y se
     *       divide por {@code ;} a nivel superior. Dentro de cada patrón
     *       el {@code ;} literal debe escaparse como {@code \\;}.</li>
     *   <li><b>Una sola regex</b>: si no hay {@code =} a nivel superior,
     *       se toma todo el texto como una única expresión regular y los
     *       {@code ;} se tratan como caracteres literales (mantiene
     *       compatibilidad hacia atrás con regex como
     *       {@code ...|;|...}).</li>
     * </ul>
     */
    static List<PatronRegex> parsearPatrones(String texto) {
        List<PatronRegex> result = new ArrayList<>();
        if (texto == null) return result;
        String trimmed = texto.trim();
        if (trimmed.isEmpty()) return result;

        // El modo multi-patrón sólo se activa si la entrada comienza con
        // el formato 'NAME=...'. Así un '=' suelto dentro de la regex
        // (por ejemplo `...|=|...` para reconocer el operador igual) no
        // se confunde con la asignación de un tipo.
        if (!comienzaConAsignacionDeTipo(trimmed)) {
            result.add(new PatronRegex(TIPO_AUTO, trimmed));
            return result;
        }

        List<String> partes = dividirTopLevel(trimmed, ';');
        for (String p : partes) {
            String parte = p.trim();
            if (parte.isEmpty()) continue;
            int eq = posicionIgualTopLevel(parte);
            if (eq > 0) {
                String tipo = parte.substring(0, eq).trim();
                String regex = parte.substring(eq + 1).trim();
                if (tipo.isEmpty()) {
                    throw new RuntimeException("Falta el nombre de tipo antes de '=' en: '" + parte + "'");
                }
                if (regex.isEmpty()) {
                    throw new RuntimeException("Falta la expresión regular después de '=' para el tipo '" + tipo + "'");
                }
                result.add(new PatronRegex(tipo.toUpperCase(java.util.Locale.ROOT), regex));
            } else {
                // Fragmento sin etiqueta dentro de un texto multi-patrón.
                result.add(new PatronRegex(TIPO_AUTO, parte));
            }
        }
        return result;
    }

    /**
     * Devuelve {@code true} si {@code texto} comienza con
     * {@code NAME=...}, donde {@code NAME} es un identificador
     * ({@code [a-zA-Z_][a-zA-Z0-9_]*}). Sirve para activar el modo
     * multi-patrón sin falsos positivos cuando hay {@code =} suelto
     * dentro de la regex.
     */
    private static boolean comienzaConAsignacionDeTipo(String texto) {
        int i = 0;
        int n = texto.length();
        if (i >= n) return false;
        char c = texto.charAt(i);
        if (!(Character.isLetter(c) || c == '_')) return false;
        i++;
        while (i < n) {
            c = texto.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') { i++; continue; }
            break;
        }
        while (i < n && (texto.charAt(i) == ' ' || texto.charAt(i) == '\t')) i++;
        return i < n && texto.charAt(i) == '=';
    }

    /** Divide {@code s} por {@code sep} ignorando ocurrencias dentro de [...] o (...) y los escapes \\. */
    private static List<String> dividirTopLevel(String s, char sep) {
        List<String> partes = new ArrayList<>();
        int parens = 0, brackets = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) { i++; continue; }
            if (c == '[') brackets++;
            else if (c == ']' && brackets > 0) brackets--;
            else if (brackets == 0 && c == '(') parens++;
            else if (brackets == 0 && c == ')' && parens > 0) parens--;
            else if (brackets == 0 && parens == 0 && c == sep) {
                partes.add(s.substring(start, i));
                start = i + 1;
            }
        }
        partes.add(s.substring(start));
        return partes;
    }

    /** Devuelve la posición del primer '=' que no esté dentro de [...] o (...). */
    private static int posicionIgualTopLevel(String s) {
        int parens = 0, brackets = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) { i++; continue; }
            if (c == '[') brackets++;
            else if (c == ']' && brackets > 0) brackets--;
            else if (brackets == 0 && c == '(') parens++;
            else if (brackets == 0 && c == ')' && parens > 0) parens--;
            else if (brackets == 0 && parens == 0 && c == '=') return i;
        }
        return -1;
    }

    // =================================================================
    //   CAPA 1 — ESTRUCTURAS DEL AFN
    // =================================================================

    /**
     * Estado del AFN. Cada estado tiene id único, transiciones salientes
     * y, opcionalmente, una etiqueta de tipo con su prioridad si es un
     * estado de aceptación.
     */
    static class Estado {
        int id;
        List<Transicion> transiciones = new ArrayList<>();
        /** Tipo de token al aceptar, o null si el estado no es aceptante. */
        String etiquetaTipo;
        /** Prioridad (menor = mayor preferencia) ante empate de longitud entre patrones. */
        int prioridad;

        Estado(int id) {
            this.id = id;
        }

        boolean esAceptante() {
            return etiquetaTipo != null;
        }
    }

    /**
     * Transición del AFN. Si {@code epsilon} es {@code true} no consume
     * carácter. En caso contrario, {@code simbolos} es el conjunto de
     * caracteres que activa la transición (un solo carácter para
     * literales, varios para clases como {@code [a-z]}).
     */
    static class Transicion {
        boolean epsilon;
        Set<Character> simbolos;
        Estado destino;

        static Transicion eps(Estado destino) {
            Transicion t = new Transicion();
            t.epsilon = true;
            t.destino = destino;
            return t;
        }

        static Transicion simbolo(Set<Character> conjunto, Estado destino) {
            Transicion t = new Transicion();
            t.epsilon = false;
            t.simbolos = conjunto;
            t.destino = destino;
            return t;
        }

        boolean acepta(char c) {
            return !epsilon && simbolos != null && simbolos.contains(c);
        }
    }

    /** Fragmento de AFN: estado inicial y final del fragmento que se está componiendo. */
    static class AFN {
        Estado inicio;
        Estado fin;
    }

    // =================================================================
    //   CAPA 1 — CONSTRUCCIÓN DEL AFN (Algoritmo de Thompson)
    // =================================================================

    /**
     * Constructor de AFN por el algoritmo de Thompson.
     *
     * <p>Fases:</p>
     * <ol>
     *   <li>{@link #tokenizar(String)}: convierte la regex en una lista de tokens.</li>
     *   <li>{@link #validar(List)}: rechaza secuencias mal formadas.</li>
     *   <li>{@link #insertarConcatenacion(List)}: añade el operador de concatenación explícito.</li>
     *   <li>{@link #aPosfijo(List)}: Shunting-Yard para pasar a notación posfija.</li>
     *   <li>{@link #desdePosfijo(List)}: aplica las construcciones de Thompson.</li>
     * </ol>
     *
     * <p>Las construcciones individuales (concat, unión, kleene, opcional)
     * consumen sus operandos del stack y no introducen aliasing
     * problemático. El operador {@code A+} se reduce a {@code A · A*} con
     * un <i>clon</i> profundo de {@code A} para evitar reutilizar la misma
     * subestructura en ambos lados.</p>
     */
    static class ConstructorAFN_Thompson {

        private int contadorEstados = 0;

        private enum TipoTok { SIMBOLO, LPAREN, RPAREN, UNION, CONCAT, ESTRELLA, MAS, PREG }

        private static class Tok {
            final TipoTok tipo;
            final Set<Character> simbolos; // sólo si tipo == SIMBOLO
            Tok(TipoTok tipo)              { this.tipo = tipo; this.simbolos = null; }
            Tok(Set<Character> simbolos)   { this.tipo = TipoTok.SIMBOLO; this.simbolos = simbolos; }
        }

        /** Construye el AFN para una sola expresión regular. */
        public AFN construir(String regex) {
            List<Tok> tokens = tokenizar(regex);
            validar(tokens);
            tokens = insertarConcatenacion(tokens);
            List<Tok> posfijo = aPosfijo(tokens);
            return desdePosfijo(posfijo);
        }

        /**
         * Construye un AFN maestro a partir de una lista de patrones.
         * Cada subAFN conserva su estado de aceptación etiquetado con el
         * tipo del patrón y una prioridad (menor índice = mayor preferencia).
         * El AFN maestro tiene un único estado inicial nuevo con
         * ε-transiciones hacia el inicio de cada subAFN.
         */
        public AFN construirMultiple(List<PatronRegex> patrones) {
            if (patrones == null || patrones.isEmpty()) {
                throw new RuntimeException("No se proporcionaron patrones");
            }
            AFN master = new AFN();
            master.inicio = nuevoEstado();
            master.fin = null; // los aceptantes son múltiples y se reconocen por etiquetaTipo

            for (int i = 0; i < patrones.size(); i++) {
                PatronRegex p = patrones.get(i);
                AFN sub;
                try {
                    sub = construir(p.regex);
                } catch (RuntimeException ex) {
                    throw new RuntimeException("Patrón '" + p.tipo + "': " + ex.getMessage());
                }
                sub.fin.etiquetaTipo = p.tipo;
                sub.fin.prioridad = i;
                master.inicio.transiciones.add(Transicion.eps(sub.inicio));
            }
            return master;
        }

        private Estado nuevoEstado() {
            return new Estado(contadorEstados++);
        }

        // ----------------- 1) Tokenización -----------------

        private List<Tok> tokenizar(String regex) {
            List<Tok> salida = new ArrayList<>();
            int i = 0;
            while (i < regex.length()) {
                char c = regex.charAt(i);
                switch (c) {
                    case '(': salida.add(new Tok(TipoTok.LPAREN));   i++; break;
                    case ')': salida.add(new Tok(TipoTok.RPAREN));   i++; break;
                    case '|': salida.add(new Tok(TipoTok.UNION));    i++; break;
                    case '*': salida.add(new Tok(TipoTok.ESTRELLA)); i++; break;
                    case '+': salida.add(new Tok(TipoTok.MAS));      i++; break;
                    case '?': salida.add(new Tok(TipoTok.PREG));     i++; break;
                    case ']':
                        throw new RuntimeException("']' sin '[' correspondiente en la posición " + i);
                    case '[': {
                        int cierre = encontrarCierreCorchete(regex, i);
                        if (cierre == -1) {
                            throw new RuntimeException("Falta ']' que cierre la clase de caracteres iniciada en la posición " + i);
                        }
                        Set<Character> clase = parsearClase(regex.substring(i + 1, cierre));
                        salida.add(new Tok(clase));
                        i = cierre + 1;
                        break;
                    }
                    case '\\': {
                        if (i + 1 >= regex.length()) {
                            throw new RuntimeException("Escape '\\' al final de la expresión");
                        }
                        Set<Character> uno = new HashSet<>();
                        uno.add(regex.charAt(i + 1));
                        salida.add(new Tok(uno));
                        i += 2;
                        break;
                    }
                    default: {
                        // Los espacios en la regex se ignoran para legibilidad;
                        // usar \\ para incluir un espacio literal.
                        if (c == ' ' || c == '\t') { i++; break; }
                        Set<Character> uno = new HashSet<>();
                        uno.add(c);
                        salida.add(new Tok(uno));
                        i++;
                    }
                }
            }
            return salida;
        }

        /** Devuelve el índice del ']' que cierra la clase iniciada en {@code abre}, respetando escapes. */
        private int encontrarCierreCorchete(String s, int abre) {
            int i = abre + 1;
            // Permite ']' literal sólo si va escapado.
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == '\\' && i + 1 < s.length()) { i += 2; continue; }
                if (c == ']') return i;
                i++;
            }
            return -1;
        }

        /**
         * Convierte el contenido de {@code [...]} en un conjunto de caracteres.
         * Soporta rangos {@code a-z} y la negación con {@code ^} al inicio.
         */
        private Set<Character> parsearClase(String contenido) {
            if (contenido.isEmpty()) {
                throw new RuntimeException("Clase de caracteres vacía '[]'");
            }
            boolean negada = false;
            int start = 0;
            if (contenido.charAt(0) == '^') {
                negada = true;
                start = 1;
                if (contenido.length() == 1) {
                    throw new RuntimeException("Clase negada vacía '[^]'");
                }
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
                    char ini = c;
                    if (ini > fin) {
                        throw new RuntimeException("Rango inválido en clase: '" + ini + "-" + fin + "'");
                    }
                    for (char x = ini; x <= fin; x++) {
                        conjunto.add(x);
                    }
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

        // ----------------- 2) Validación sintáctica -----------------

        private void validar(List<Tok> tokens) {
            if (tokens.isEmpty()) {
                throw new RuntimeException("Expresión regular vacía");
            }
            int parens = 0;
            Tok prev = null;
            for (int idx = 0; idx < tokens.size(); idx++) {
                Tok t = tokens.get(idx);
                boolean unario = t.tipo == TipoTok.ESTRELLA || t.tipo == TipoTok.MAS || t.tipo == TipoTok.PREG;
                if (t.tipo == TipoTok.LPAREN) parens++;
                if (t.tipo == TipoTok.RPAREN) {
                    parens--;
                    if (parens < 0) {
                        throw new RuntimeException("Paréntesis ')' sin su pareja '(' (token #" + (idx + 1) + ")");
                    }
                }

                if (prev == null) {
                    if (unario || t.tipo == TipoTok.UNION) {
                        throw new RuntimeException("La expresión no puede comenzar con '" + nombreOperador(t.tipo) + "'");
                    }
                    if (t.tipo == TipoTok.RPAREN) {
                        throw new RuntimeException("La expresión no puede comenzar con ')'");
                    }
                } else {
                    if (t.tipo == TipoTok.UNION
                            && (prev.tipo == TipoTok.UNION || prev.tipo == TipoTok.LPAREN)) {
                        throw new RuntimeException("Operador '|' sin operando izquierdo válido");
                    }
                    if (unario && (prev.tipo == TipoTok.UNION || prev.tipo == TipoTok.LPAREN)) {
                        throw new RuntimeException("Operador '" + nombreOperador(t.tipo) + "' sin operando previo");
                    }
                    if (t.tipo == TipoTok.RPAREN) {
                        if (prev.tipo == TipoTok.LPAREN) {
                            throw new RuntimeException("Grupo vacío '()' no permitido");
                        }
                        if (prev.tipo == TipoTok.UNION) {
                            throw new RuntimeException("Operador '|' sin operando derecho antes de ')'");
                        }
                    }
                }
                prev = t;
            }
            if (parens != 0) {
                throw new RuntimeException("Paréntesis '(' sin cierre (faltan " + parens + " ')')");
            }
            if (prev != null && prev.tipo == TipoTok.UNION) {
                throw new RuntimeException("Operador '|' sin operando derecho al final");
            }
        }

        private String nombreOperador(TipoTok t) {
            switch (t) {
                case UNION:    return "|";
                case ESTRELLA: return "*";
                case MAS:      return "+";
                case PREG:     return "?";
                case LPAREN:   return "(";
                case RPAREN:   return ")";
                default:       return t.name();
            }
        }

        // ----------------- 3) Concatenación explícita -----------------

        private List<Tok> insertarConcatenacion(List<Tok> entrada) {
            List<Tok> salida = new ArrayList<>();
            for (int i = 0; i < entrada.size(); i++) {
                Tok actual = entrada.get(i);
                salida.add(actual);
                if (i + 1 >= entrada.size()) continue;
                Tok siguiente = entrada.get(i + 1);
                boolean izqOk = actual.tipo == TipoTok.SIMBOLO
                        || actual.tipo == TipoTok.RPAREN
                        || actual.tipo == TipoTok.ESTRELLA
                        || actual.tipo == TipoTok.MAS
                        || actual.tipo == TipoTok.PREG;
                boolean derOk = siguiente.tipo == TipoTok.SIMBOLO
                        || siguiente.tipo == TipoTok.LPAREN;
                if (izqOk && derOk) {
                    salida.add(new Tok(TipoTok.CONCAT));
                }
            }
            return salida;
        }

        // ----------------- 4) Shunting-Yard a posfijo -----------------

        private int precedencia(TipoTok t) {
            switch (t) {
                case UNION:    return 1;
                case CONCAT:   return 2;
                case ESTRELLA:
                case MAS:
                case PREG:     return 3;
                default:       return 0;
            }
        }

        private List<Tok> aPosfijo(List<Tok> tokens) {
            List<Tok> salida = new ArrayList<>();
            List<Tok> pila = new ArrayList<>();
            for (Tok tk : tokens) {
                switch (tk.tipo) {
                    case SIMBOLO:
                        salida.add(tk);
                        break;
                    case LPAREN:
                        pila.add(tk);
                        break;
                    case RPAREN:
                        boolean encontrado = false;
                        while (!pila.isEmpty()) {
                            Tok top = pila.remove(pila.size() - 1);
                            if (top.tipo == TipoTok.LPAREN) {
                                encontrado = true;
                                break;
                            }
                            salida.add(top);
                        }
                        if (!encontrado) {
                            // Ya cubierto por validar(), defensa adicional.
                            throw new RuntimeException("Paréntesis ')' sin su pareja '('");
                        }
                        break;
                    default:
                        while (!pila.isEmpty()) {
                            Tok top = pila.get(pila.size() - 1);
                            if (top.tipo == TipoTok.LPAREN) break;
                            if (precedencia(top.tipo) >= precedencia(tk.tipo)) {
                                salida.add(pila.remove(pila.size() - 1));
                            } else {
                                break;
                            }
                        }
                        pila.add(tk);
                }
            }
            while (!pila.isEmpty()) {
                Tok top = pila.remove(pila.size() - 1);
                if (top.tipo == TipoTok.LPAREN) {
                    throw new RuntimeException("Paréntesis '(' sin cierre");
                }
                salida.add(top);
            }
            return salida;
        }

        // ----------------- 5) Posfijo -> AFN (Thompson) -----------------

        private AFN desdePosfijo(List<Tok> posfijo) {
            List<AFN> pila = new ArrayList<>();
            for (Tok tk : posfijo) {
                switch (tk.tipo) {
                    case SIMBOLO:
                        pila.add(afnSimbolo(tk.simbolos));
                        break;
                    case CONCAT: {
                        if (pila.size() < 2) {
                            throw new RuntimeException("Expresión regular mal formada (concatenación sin operandos)");
                        }
                        AFN b = pila.remove(pila.size() - 1);
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(concatenar(a, b));
                        break;
                    }
                    case UNION: {
                        if (pila.size() < 2) {
                            throw new RuntimeException("Expresión regular mal formada (unión sin operandos)");
                        }
                        AFN b = pila.remove(pila.size() - 1);
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(union(a, b));
                        break;
                    }
                    case ESTRELLA: {
                        if (pila.isEmpty()) {
                            throw new RuntimeException("Expresión regular mal formada (* sin operando)");
                        }
                        pila.add(kleene(pila.remove(pila.size() - 1)));
                        break;
                    }
                    case MAS: {
                        if (pila.isEmpty()) {
                            throw new RuntimeException("Expresión regular mal formada (+ sin operando)");
                        }
                        pila.add(masUno(pila.remove(pila.size() - 1)));
                        break;
                    }
                    case PREG: {
                        if (pila.isEmpty()) {
                            throw new RuntimeException("Expresión regular mal formada (? sin operando)");
                        }
                        pila.add(opcional(pila.remove(pila.size() - 1)));
                        break;
                    }
                    default:
                        throw new RuntimeException("Token inesperado en posfijo: " + tk.tipo);
                }
            }
            if (pila.size() != 1) {
                throw new RuntimeException("Expresión regular mal formada");
            }
            return pila.get(0);
        }

        // ----------------- Construcciones de Thompson -----------------

        private AFN afnSimbolo(Set<Character> simbolos) {
            AFN a = new AFN();
            a.inicio = nuevoEstado();
            a.fin = nuevoEstado();
            a.inicio.transiciones.add(Transicion.simbolo(simbolos, a.fin));
            return a;
        }

        private AFN concatenar(AFN a, AFN b) {
            a.fin.transiciones.add(Transicion.eps(b.inicio));
            AFN res = new AFN();
            res.inicio = a.inicio;
            res.fin = b.fin;
            return res;
        }

        private AFN union(AFN a, AFN b) {
            AFN res = new AFN();
            res.inicio = nuevoEstado();
            res.fin = nuevoEstado();
            res.inicio.transiciones.add(Transicion.eps(a.inicio));
            res.inicio.transiciones.add(Transicion.eps(b.inicio));
            a.fin.transiciones.add(Transicion.eps(res.fin));
            b.fin.transiciones.add(Transicion.eps(res.fin));
            return res;
        }

        private AFN kleene(AFN a) {
            AFN res = new AFN();
            res.inicio = nuevoEstado();
            res.fin = nuevoEstado();
            res.inicio.transiciones.add(Transicion.eps(a.inicio));
            res.inicio.transiciones.add(Transicion.eps(res.fin));
            a.fin.transiciones.add(Transicion.eps(a.inicio));
            a.fin.transiciones.add(Transicion.eps(res.fin));
            return res;
        }

        /**
         * {@code A+} se construye como {@code A · A*} usando un clon
         * profundo de A para no mutar la misma subestructura dos veces
         * con back-edges incompatibles. Así se evita cualquier
         * efecto colateral por aliasing.
         */
        private AFN masUno(AFN a) {
            AFN copia = clonar(a);
            AFN estrellaDeCopia = kleene(copia);
            return concatenar(a, estrellaDeCopia);
        }

        private AFN opcional(AFN a) {
            AFN res = new AFN();
            res.inicio = nuevoEstado();
            res.fin = nuevoEstado();
            res.inicio.transiciones.add(Transicion.eps(a.inicio));
            res.inicio.transiciones.add(Transicion.eps(res.fin));
            a.fin.transiciones.add(Transicion.eps(res.fin));
            return res;
        }

        /**
         * Clon profundo de un fragmento AFN: crea estados nuevos con ids
         * propios y reconstruye todas las transiciones reachable desde
         * {@code original.inicio}. {@code original.fin} se incluye
         * explícitamente aunque, en fragmentos bien formados de Thompson,
         * siempre es alcanzable.
         */
        private AFN clonar(AFN original) {
            Map<Estado, Estado> mapa = new IdentityHashMap<>();
            List<Estado> pila = new ArrayList<>();

            Estado nuevoInicio = nuevoEstado();
            nuevoInicio.etiquetaTipo = original.inicio.etiquetaTipo;
            nuevoInicio.prioridad = original.inicio.prioridad;
            mapa.put(original.inicio, nuevoInicio);
            pila.add(original.inicio);

            // Aseguramos que el fin esté mapeado aunque no sea alcanzable.
            if (!mapa.containsKey(original.fin)) {
                Estado clonFin = nuevoEstado();
                clonFin.etiquetaTipo = original.fin.etiquetaTipo;
                clonFin.prioridad = original.fin.prioridad;
                mapa.put(original.fin, clonFin);
                pila.add(original.fin);
            }

            while (!pila.isEmpty()) {
                Estado actual = pila.remove(pila.size() - 1);
                Estado copia = mapa.get(actual);
                for (Transicion t : actual.transiciones) {
                    Estado destOrig = t.destino;
                    Estado destCopia = mapa.get(destOrig);
                    if (destCopia == null) {
                        destCopia = nuevoEstado();
                        destCopia.etiquetaTipo = destOrig.etiquetaTipo;
                        destCopia.prioridad = destOrig.prioridad;
                        mapa.put(destOrig, destCopia);
                        pila.add(destOrig);
                    }
                    if (t.epsilon) {
                        copia.transiciones.add(Transicion.eps(destCopia));
                    } else {
                        // Compartir la referencia al Set es seguro porque el
                        // conjunto de símbolos es inmutable después de la
                        // tokenización; aun así clonamos para máxima seguridad.
                        copia.transiciones.add(Transicion.simbolo(new HashSet<>(t.simbolos), destCopia));
                    }
                }
            }

            AFN copia = new AFN();
            copia.inicio = mapa.get(original.inicio);
            copia.fin = mapa.get(original.fin);
            return copia;
        }
    }

    // =================================================================
    //   CAPA 2 — SIMULACIÓN DEL AFN
    // =================================================================

    /** Resultado de una operación de máxima coincidencia. */
    static class MatchResult {
        final int longitud;
        final String tipo;

        MatchResult(int longitud, String tipo) {
            this.longitud = longitud;
            this.tipo = tipo;
        }

        static MatchResult sinMatch() {
            return new MatchResult(0, null);
        }
    }

    /**
     * Simulador puro del AFN. Implementa la clausura-ε, el move por
     * símbolo y la búsqueda de máxima coincidencia desde un offset dado.
     * Es completamente independiente del léxico, no conoce nada de
     * tokens ni del flujo de la GUI.
     */
    static class SimuladorAFN {

        private final AFN afn;

        SimuladorAFN(AFN afn) {
            this.afn = afn;
        }

        /**
         * Devuelve la longitud del lexema más largo aceptado por el AFN
         * a partir de {@code desde}, junto con el tipo del estado de
         * aceptación elegido. Si no hay coincidencia, longitud = 0 y
         * tipo = null.
         */
        MatchResult obtenerMatchMasLargo(String entrada, int desde) {
            Set<Estado> actuales = clausuraEpsilon(unicoConjunto(afn.inicio));
            String mejorTipo = tipoAceptante(actuales);
            int mejorLongitud = mejorTipo != null ? 0 : -1;
            int longitud = 0;

            while (desde + longitud < entrada.length() && !actuales.isEmpty()) {
                char c = entrada.charAt(desde + longitud);
                Set<Estado> siguiente = mover(actuales, c);
                if (siguiente.isEmpty()) break;
                actuales = clausuraEpsilon(siguiente);
                longitud++;
                String tipo = tipoAceptante(actuales);
                if (tipo != null) {
                    mejorLongitud = longitud;
                    mejorTipo = tipo;
                }
            }

            if (mejorLongitud <= 0) return MatchResult.sinMatch();
            return new MatchResult(mejorLongitud, mejorTipo);
        }

        private Set<Estado> unicoConjunto(Estado e) {
            Set<Estado> s = new LinkedHashSet<>();
            s.add(e);
            return s;
        }

        private Set<Estado> clausuraEpsilon(Set<Estado> estados) {
            Set<Estado> cierre = new LinkedHashSet<>(estados);
            List<Estado> pila = new ArrayList<>(estados);
            while (!pila.isEmpty()) {
                Estado e = pila.remove(pila.size() - 1);
                for (Transicion t : e.transiciones) {
                    if (t.epsilon && !cierre.contains(t.destino)) {
                        cierre.add(t.destino);
                        pila.add(t.destino);
                    }
                }
            }
            return cierre;
        }

        private Set<Estado> mover(Set<Estado> estados, char c) {
            Set<Estado> destino = new LinkedHashSet<>();
            for (Estado e : estados) {
                for (Transicion t : e.transiciones) {
                    if (t.acepta(c)) destino.add(t.destino);
                }
            }
            return destino;
        }

        /**
         * Si el conjunto contiene algún estado de aceptación, devuelve el
         * tipo asociado al estado con menor {@code prioridad} (mayor
         * preferencia). En caso contrario, devuelve null.
         */
        private String tipoAceptante(Set<Estado> estados) {
            Estado mejor = null;
            for (Estado e : estados) {
                if (!e.esAceptante()) continue;
                if (mejor == null || e.prioridad < mejor.prioridad) {
                    mejor = e;
                }
            }
            return mejor == null ? null : mejor.etiquetaTipo;
        }
    }

    // =================================================================
    //   CAPA 3 — ANÁLISIS LÉXICO
    // =================================================================

    static class Token {
        final String lexema;
        final String tipo;
        final String valor;
        final int linea;
        final int columna;

        Token(String lexema, String tipo, String valor, int linea, int columna) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.valor = valor;
            this.linea = linea;
            this.columna = columna;
        }
    }

    static class ErrorLexico {
        final String lexema;
        final int linea;
        final int columna;

        ErrorLexico(String lexema, int linea, int columna) {
            this.lexema = lexema;
            this.linea = linea;
            this.columna = columna;
        }
    }

    static class ResultadoAnalisis {
        final List<Token> tokens = new ArrayList<>();
        final List<ErrorLexico> errores = new ArrayList<>();
    }

    /**
     * Analizador léxico. Usa el simulador para extraer tokens por máxima
     * coincidencia, gestiona la posición (línea/columna), agrupa
     * caracteres no reconocidos consecutivos en un único error, y aplica
     * el refinamiento de palabras reservadas sobre identificadores.
     */
    static class AnalizadorLexico {

        private final SimuladorAFN simulador;

        AnalizadorLexico(SimuladorAFN simulador) {
            this.simulador = simulador;
        }

        ResultadoAnalisis analizar(String entrada) {
            ResultadoAnalisis resultado = new ResultadoAnalisis();
            int i = 0;
            int linea = 1;
            int columna = 1;
            int n = entrada.length();

            // Buffer para agrupar caracteres erróneos consecutivos.
            StringBuilder bufErr = null;
            int errLinea = 0;
            int errColumna = 0;

            while (i < n) {
                char c = entrada.charAt(i);

                // Saltar espacios y saltos de línea (no son tokens).
                if (c == ' ' || c == '\t') {
                    resultado.errores.addAll(volcarError(bufErr, errLinea, errColumna));
                    bufErr = null;
                    i++; columna++;
                    continue;
                }
                if (c == '\n') {
                    resultado.errores.addAll(volcarError(bufErr, errLinea, errColumna));
                    bufErr = null;
                    i++; linea++; columna = 1;
                    continue;
                }
                if (c == '\r') {
                    i++;
                    continue;
                }

                int inicioLinea = linea;
                int inicioColumna = columna;

                MatchResult m = simulador.obtenerMatchMasLargo(entrada, i);

                if (m.longitud > 0) {
                    resultado.errores.addAll(volcarError(bufErr, errLinea, errColumna));
                    bufErr = null;

                    String lexema = entrada.substring(i, i + m.longitud);
                    String tipo = refinarTipo(m.tipo, lexema);
                    String valor = lexema;
                    resultado.tokens.add(new Token(lexema, tipo, valor, inicioLinea, inicioColumna));

                    for (int k = 0; k < m.longitud; k++) {
                        char cc = entrada.charAt(i + k);
                        if (cc == '\n') { linea++; columna = 1; }
                        else            { columna++; }
                    }
                    i += m.longitud;
                } else {
                    // Carácter no reconocido: agrupar con errores adyacentes
                    // para reportar mejor las "secuencias no reconocidas".
                    if (bufErr == null) {
                        bufErr = new StringBuilder();
                        errLinea = inicioLinea;
                        errColumna = inicioColumna;
                    }
                    bufErr.append(c);
                    i++; columna++;
                }
            }
            resultado.errores.addAll(volcarError(bufErr, errLinea, errColumna));
            return resultado;
        }

        private List<ErrorLexico> volcarError(StringBuilder buf, int linea, int columna) {
            List<ErrorLexico> out = new ArrayList<>();
            if (buf != null && buf.length() > 0) {
                out.add(new ErrorLexico(buf.toString(), linea, columna));
            }
            return out;
        }

        /**
         * Refina el tipo entregado por el AFN según el lexema concreto:
         * <ul>
         *   <li>Si el tipo es {@code AUTO} (modo de regex única sin
         *       etiquetar), se aplica la heurística:
         *       reservada → identificador → número → símbolo.</li>
         *   <li>Si el tipo es {@code IDENTIFICADOR} pero el lexema está
         *       en la tabla de palabras reservadas, se reclasifica como
         *       {@code PALABRA_RESERVADA} (refinamiento estándar
         *       reservada/identificador).</li>
         *   <li>En otro caso, se respeta el tipo del AFN tal cual.</li>
         * </ul>
         */
        private String refinarTipo(String tipoAfn, String lexema) {
            if (tipoAfn == null) return "Símbolo";
            if (TIPO_AUTO.equals(tipoAfn)) {
                if (PALABRAS_RESERVADAS.contains(lexema)) return "Palabra Reservada";
                if (esIdentificador(lexema))             return "Identificador";
                if (esNumero(lexema))                    return "Número";
                return "Símbolo";
            }
            if ("IDENTIFICADOR".equalsIgnoreCase(tipoAfn) && PALABRAS_RESERVADAS.contains(lexema)) {
                return "Palabra Reservada";
            }
            return tipoAfn;
        }

        private boolean esIdentificador(String s) {
            if (s.isEmpty()) return false;
            char primero = s.charAt(0);
            if (!(Character.isLetter(primero) || primero == '_')) return false;
            for (int k = 1; k < s.length(); k++) {
                char c = s.charAt(k);
                if (!(Character.isLetterOrDigit(c) || c == '_')) return false;
            }
            return true;
        }

        private boolean esNumero(String s) {
            if (s.isEmpty()) return false;
            boolean tienePunto = false;
            for (int k = 0; k < s.length(); k++) {
                char c = s.charAt(k);
                if (c == '.') {
                    if (tienePunto) return false;
                    tienePunto = true;
                } else if (!Character.isDigit(c)) {
                    return false;
                }
            }
            return true;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAnalizar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea tError;
    private javax.swing.JTextField tExpresion;
    private javax.swing.JTextField tIngreso;
    private javax.swing.JTextArea tSalida;
    private javax.swing.JTable tablaSimbolo;
    // End of variables declaration//GEN-END:variables
}
