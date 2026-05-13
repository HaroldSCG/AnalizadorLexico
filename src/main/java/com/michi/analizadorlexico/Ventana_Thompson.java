/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.michi.analizadorlexico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

/**
 * Ventana_Thompson
 *
 * Analizador léxico construido desde cero utilizando el algoritmo de
 * Thompson para convertir expresiones regulares en un AFN (Autómata Finito
 * No Determinista) y simularlo sobre la cadena de entrada para reconocer
 * tokens. NO se utilizan librerías externas (solo Swing para la interfaz y
 * estructuras estándar de Java).
 *
 * Operaciones soportadas en la expresión regular:
 *   concatenación (implícita)   ej:  ab
 *   unión               |       ej:  a|b
 *   cerradura de Kleene *       ej:  a*
 *   una o más           +       ej:  a+
 *   opcional            ?       ej:  a?
 *   agrupación        ( )       ej:  (ab)+
 *   clase de caracteres [ ]     ej:  [a-zA-Z_]   [^0-9]
 *   escape              \       ej:  \+   \(   \\
 *
 * Si se desea reconocer varios tipos de tokens, pueden encadenarse con la
 * unión, por ejemplo:
 *     [a-zA-Z_][a-zA-Z0-9_]*|[0-9]+|[+\-/=&lt;&gt;;,(){}]
 *
 * @author harol
 */
public class Ventana_Thompson extends javax.swing.JFrame {

    /** Palabras reservadas reconocidas por el analizador. */
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

    /**
     * Creates new form Ventana_Thompson
     */
    public Ventana_Thompson() {
        initComponents();
        inicializarTabla();

        // Botón Analizar: ejecuta el análisis léxico con Thompson.
        bAnalizar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ejecutarAnalisis();
            }
        });

        setTitle("Analizador Léxico - Algoritmo de Thompson");
        setLocationRelativeTo(null);
    }

    /** Configura el modelo de la tabla de símbolos con las columnas adecuadas. */
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
    //   EJECUCIÓN DEL ANÁLISIS (orquesta Thompson + simulación AFN)
    // =================================================================

    /**
     * Punto de entrada disparado por el botón "Analizar".
     * 1. Toma la expresión regular ingresada y construye el AFN con Thompson.
     * 2. Toma la cadena de entrada y la recorre simulando el AFN para
     *    obtener tokens por máxima coincidencia.
     * 3. Clasifica los tokens, alimenta la tabla de símbolos y reporta
     *    errores léxicos con su posición (línea, columna).
     */
    private void ejecutarAnalisis() {
        tSalida.setText("");
        tError.setText("");
        DefaultTableModel modelo = (DefaultTableModel) tablaSimbolo.getModel();
        modelo.setRowCount(0);

        String expresion = tExpresion.getText();
        String entrada = tIngreso.getText();

        if (expresion == null || expresion.isEmpty()) {
            tError.append("Error: la expresión regular está vacía.\n");
            return;
        }

        // Construcción del AFN con Thompson.
        AFN afn;
        try {
            ThompsonAFN constructor = new ThompsonAFN();
            afn = constructor.construir(expresion);
        } catch (RuntimeException ex) {
            tError.append("Error en la expresión regular: " + ex.getMessage() + "\n");
            return;
        }

        // Análisis léxico de la cadena.
        AnalizadorLexicoThompson lexer = new AnalizadorLexicoThompson(afn);
        ResultadoAnalisis resultado = lexer.analizar(entrada == null ? "" : entrada);

        // Volcar tokens.
        StringBuilder sbTokens = new StringBuilder();
        sbTokens.append("Tokens reconocidos (")
                .append(resultado.tokens.size()).append("):\n");
        sbTokens.append("-----------------------------------------------------------\n");
        sbTokens.append(String.format("%-20s %-20s %-8s %-8s%n",
                "Lexema", "Tipo", "Línea", "Columna"));
        sbTokens.append("-----------------------------------------------------------\n");
        for (Token t : resultado.tokens) {
            sbTokens.append(String.format("%-20s %-20s %-8d %-8d%n",
                    t.lexema, t.tipo, t.linea, t.columna));
        }
        tSalida.setText(sbTokens.toString());

        // Volcar tabla de símbolos (sin duplicados, conserva orden de aparición).
        Map<String, Boolean> agregados = new HashMap<>();
        for (Token t : resultado.tokens) {
            String clave = t.tipo + "::" + t.lexema;
            if (!agregados.containsKey(clave)) {
                agregados.put(clave, Boolean.TRUE);
                modelo.addRow(new Object[]{t.lexema, t.tipo, t.valor, t.linea, t.columna});
            }
        }

        // Volcar errores léxicos.
        if (resultado.errores.isEmpty()) {
            tError.setText("Sin errores léxicos.\n");
        } else {
            StringBuilder sbErr = new StringBuilder();
            sbErr.append("Errores léxicos (").append(resultado.errores.size()).append("):\n");
            sbErr.append("-----------------------------------------------\n");
            for (ErrorLexico e : resultado.errores) {
                sbErr.append("Línea ").append(e.linea)
                     .append(", Columna ").append(e.columna)
                     .append(": carácter no válido '").append(e.lexema).append("'\n");
            }
            tError.setText(sbErr.toString());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Ventana_Thompson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ventana_Thompson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ventana_Thompson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ventana_Thompson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ventana_Thompson().setVisible(true);
            }
        });
    }

    // =================================================================
    //   ESTRUCTURAS DEL AFN (Estados, Transiciones, Autómata)
    // =================================================================

    /** Estado del AFN. Cada estado tiene un id único y una lista de transiciones salientes. */
    static class Estado {
        int id;
        List<Transicion> transiciones = new ArrayList<>();

        Estado(int id) {
            this.id = id;
        }
    }

    /**
     * Transición del AFN.
     *   - Si {@code epsilon == true}, es una transición vacía (ε).
     *   - En caso contrario, {@code simbolos} contiene el conjunto de
     *     caracteres aceptados por la transición (un solo carácter para
     *     literales, varios para clases como [a-z]).
     */
    static class Transicion {
        boolean epsilon;
        Set<Character> simbolos; // null si epsilon
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

    /** Fragmento de AFN con un estado inicial y uno de aceptación. */
    static class AFN {
        Estado inicio;
        Estado fin;
    }

    // =================================================================
    //   CONSTRUCTOR DE AFN POR EL ALGORITMO DE THOMPSON
    // =================================================================

    /**
     * Construye un AFN a partir de una expresión regular utilizando el
     * algoritmo de Thompson.
     *
     * Fases:
     *  1. Tokenizar la expresión (literales, clases [..], operadores).
     *  2. Insertar el operador de concatenación explícito '·'.
     *  3. Convertir a notación posfija usando el algoritmo Shunting-Yard.
     *  4. Recorrer el posfijo y armar el AFN aplicando las construcciones
     *     de Thompson (concatenación, unión, cerradura).
     */
    static class ThompsonAFN {

        private int contadorEstados = 0;

        /** Tipos de tokens del lexer de la expresión regular. */
        private enum TipoTok { SIMBOLO, LPAREN, RPAREN, UNION, CONCAT, ESTRELLA, MAS, PREG }

        /** Token interno usado para construir el AFN. */
        private static class Tok {
            TipoTok tipo;
            Set<Character> simbolos; // solo si tipo == SIMBOLO
            Tok(TipoTok tipo) { this.tipo = tipo; }
            Tok(Set<Character> s) { this.tipo = TipoTok.SIMBOLO; this.simbolos = s; }
        }

        public AFN construir(String regex) {
            List<Tok> tokens = tokenizar(regex);
            tokens = insertarConcatenacion(tokens);
            List<Tok> posfijo = aPosfijo(tokens);
            return desdePosfijo(posfijo);
        }

        private Estado nuevoEstado() {
            return new Estado(contadorEstados++);
        }

        /** Convierte la expresión regular en una lista de tokens. */
        private List<Tok> tokenizar(String regex) {
            List<Tok> salida = new ArrayList<>();
            int i = 0;
            while (i < regex.length()) {
                char c = regex.charAt(i);
                switch (c) {
                    case '(':
                        salida.add(new Tok(TipoTok.LPAREN));
                        i++;
                        break;
                    case ')':
                        salida.add(new Tok(TipoTok.RPAREN));
                        i++;
                        break;
                    case '|':
                        salida.add(new Tok(TipoTok.UNION));
                        i++;
                        break;
                    case '*':
                        salida.add(new Tok(TipoTok.ESTRELLA));
                        i++;
                        break;
                    case '+':
                        salida.add(new Tok(TipoTok.MAS));
                        i++;
                        break;
                    case '?':
                        salida.add(new Tok(TipoTok.PREG));
                        i++;
                        break;
                    case '[': {
                        int cierre = regex.indexOf(']', i + 1);
                        if (cierre == -1) {
                            throw new RuntimeException("Falta ']' que cierre la clase de caracteres en la posición " + i);
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
                        char esc = regex.charAt(i + 1);
                        Set<Character> uno = new HashSet<>();
                        uno.add(esc);
                        salida.add(new Tok(uno));
                        i += 2;
                        break;
                    }
                    default: {
                        // Carácter literal (se ignoran espacios para que la
                        // expresión sea más legible). Para incluir un espacio
                        // explícitamente, escapar con \.
                        if (c == ' ' || c == '\t') {
                            i++;
                            break;
                        }
                        Set<Character> uno = new HashSet<>();
                        uno.add(c);
                        salida.add(new Tok(uno));
                        i++;
                    }
                }
            }
            return salida;
        }

        /**
         * Convierte el contenido de [...] en un conjunto de caracteres.
         * Soporta rangos a-z y la negación con ^ al inicio.
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
                if (i + 2 < contenido.length() && contenido.charAt(i + 1) == '-') {
                    char fin = contenido.charAt(i + 2);
                    char ini = c;
                    if (ini > fin) {
                        throw new RuntimeException("Rango inválido en clase: " + ini + "-" + fin);
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
                for (int ch = 32; ch < 127; ch++) { // ASCII imprimible
                    char cc = (char) ch;
                    if (!conjunto.contains(cc)) {
                        neg.add(cc);
                    }
                }
                return neg;
            }
            return conjunto;
        }

        /** Inserta el operador explícito de concatenación entre tokens consecutivos. */
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

        /** Devuelve la precedencia de un operador (Shunting-Yard). */
        private int precedencia(TipoTok t) {
            switch (t) {
                case UNION: return 1;
                case CONCAT: return 2;
                case ESTRELLA:
                case MAS:
                case PREG: return 3;
                default: return 0;
            }
        }

        /** Convierte la lista de tokens infija a posfija (RPN). */
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
                            throw new RuntimeException("Paréntesis ')' sin su pareja '('");
                        }
                        break;
                    default: // operador
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

        /** Construye el AFN siguiendo el algoritmo de Thompson sobre el posfijo. */
        private AFN desdePosfijo(List<Tok> posfijo) {
            List<AFN> pila = new ArrayList<>();
            for (Tok tk : posfijo) {
                switch (tk.tipo) {
                    case SIMBOLO:
                        pila.add(afnSimbolo(tk.simbolos));
                        break;
                    case CONCAT: {
                        if (pila.size() < 2) throw new RuntimeException("Expresión regular mal formada (concatenación)");
                        AFN b = pila.remove(pila.size() - 1);
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(concatenar(a, b));
                        break;
                    }
                    case UNION: {
                        if (pila.size() < 2) throw new RuntimeException("Expresión regular mal formada (unión)");
                        AFN b = pila.remove(pila.size() - 1);
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(union(a, b));
                        break;
                    }
                    case ESTRELLA: {
                        if (pila.isEmpty()) throw new RuntimeException("Expresión regular mal formada (*)");
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(kleene(a));
                        break;
                    }
                    case MAS: {
                        if (pila.isEmpty()) throw new RuntimeException("Expresión regular mal formada (+)");
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(masUno(a));
                        break;
                    }
                    case PREG: {
                        if (pila.isEmpty()) throw new RuntimeException("Expresión regular mal formada (?)");
                        AFN a = pila.remove(pila.size() - 1);
                        pila.add(opcional(a));
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

        // ----------- Construcciones de Thompson -----------

        /** AFN para un símbolo o clase de caracteres: i --c--> f */
        private AFN afnSimbolo(Set<Character> simbolos) {
            AFN a = new AFN();
            a.inicio = nuevoEstado();
            a.fin = nuevoEstado();
            a.inicio.transiciones.add(Transicion.simbolo(simbolos, a.fin));
            return a;
        }

        /** Concatenación A·B : conecta fin(A) ---ε---> inicio(B). */
        private AFN concatenar(AFN a, AFN b) {
            a.fin.transiciones.add(Transicion.eps(b.inicio));
            AFN res = new AFN();
            res.inicio = a.inicio;
            res.fin = b.fin;
            return res;
        }

        /** Unión A|B : nuevo inicio con ε hacia inicio(A) e inicio(B); fin(A) y fin(B) con ε al nuevo fin. */
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

        /** Cerradura de Kleene A* : nuevo inicio→inicio(A), nuevo inicio→nuevo fin, fin(A)→inicio(A), fin(A)→nuevo fin. */
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

        /** A+ = A · A* (equivalente). */
        private AFN masUno(AFN a) {
            // Reusamos el patrón A · A* pero clonando estructura para no afectar a.
            // Forma equivalente compacta:
            AFN res = new AFN();
            res.inicio = nuevoEstado();
            res.fin = nuevoEstado();
            res.inicio.transiciones.add(Transicion.eps(a.inicio));
            a.fin.transiciones.add(Transicion.eps(a.inicio));
            a.fin.transiciones.add(Transicion.eps(res.fin));
            return res;
        }

        /** A? : nuevo inicio→inicio(A), nuevo inicio→nuevo fin, fin(A)→nuevo fin. */
        private AFN opcional(AFN a) {
            AFN res = new AFN();
            res.inicio = nuevoEstado();
            res.fin = nuevoEstado();
            res.inicio.transiciones.add(Transicion.eps(a.inicio));
            res.inicio.transiciones.add(Transicion.eps(res.fin));
            a.fin.transiciones.add(Transicion.eps(res.fin));
            return res;
        }
    }

    // =================================================================
    //   SIMULACIÓN DEL AFN Y ANÁLISIS LÉXICO
    // =================================================================

    /** Token reconocido por el analizador. */
    static class Token {
        String lexema;
        String tipo;
        String valor;
        int linea;
        int columna;

        Token(String lexema, String tipo, String valor, int linea, int columna) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.valor = valor;
            this.linea = linea;
            this.columna = columna;
        }
    }

    /** Error léxico detectado. */
    static class ErrorLexico {
        String lexema;
        int linea;
        int columna;

        ErrorLexico(String lexema, int linea, int columna) {
            this.lexema = lexema;
            this.linea = linea;
            this.columna = columna;
        }
    }

    /** Resultado del análisis: tokens y errores. */
    static class ResultadoAnalisis {
        List<Token> tokens = new ArrayList<>();
        List<ErrorLexico> errores = new ArrayList<>();
    }

    /**
     * Recorre la cadena de entrada y, en cada posición, intenta consumir
     * el lexema más largo aceptado por el AFN (máxima coincidencia).
     */
    static class AnalizadorLexicoThompson {

        private final AFN afn;

        AnalizadorLexicoThompson(AFN afn) {
            this.afn = afn;
        }

        ResultadoAnalisis analizar(String entrada) {
            ResultadoAnalisis resultado = new ResultadoAnalisis();
            int i = 0;
            int linea = 1;
            int columna = 1;
            int n = entrada.length();

            while (i < n) {
                char c = entrada.charAt(i);

                // Saltar espacios en blanco / saltos de línea (no son tokens).
                if (c == ' ' || c == '\t') {
                    i++;
                    columna++;
                    continue;
                }
                if (c == '\n') {
                    i++;
                    linea++;
                    columna = 1;
                    continue;
                }
                if (c == '\r') {
                    i++;
                    continue;
                }

                int inicioLinea = linea;
                int inicioColumna = columna;

                int longitudMatch = obtenerLongitudMatchMasLargo(entrada, i);

                if (longitudMatch > 0) {
                    String lexema = entrada.substring(i, i + longitudMatch);
                    String tipo = clasificar(lexema);
                    String valor = lexema;
                    resultado.tokens.add(new Token(lexema, tipo, valor, inicioLinea, inicioColumna));
                    // Avanzar respetando posibles saltos de línea dentro del lexema.
                    for (int k = 0; k < longitudMatch; k++) {
                        char cc = entrada.charAt(i + k);
                        if (cc == '\n') {
                            linea++;
                            columna = 1;
                        } else {
                            columna++;
                        }
                    }
                    i += longitudMatch;
                } else {
                    // No hay coincidencia: error léxico de un carácter.
                    resultado.errores.add(new ErrorLexico(String.valueOf(c), inicioLinea, inicioColumna));
                    i++;
                    columna++;
                }
            }
            return resultado;
        }

        /**
         * Simula el AFN sobre {@code entrada} a partir de {@code desde} y
         * devuelve la longitud del lexema más largo aceptado (0 si no hay).
         */
        private int obtenerLongitudMatchMasLargo(String entrada, int desde) {
            Set<Estado> actuales = clausuraEpsilon(unicoConjunto(afn.inicio));
            int mejorLongitud = contieneEstadoFinal(actuales) ? 0 : -1;
            int longitud = 0;

            while (desde + longitud < entrada.length() && !actuales.isEmpty()) {
                char c = entrada.charAt(desde + longitud);
                Set<Estado> siguiente = mover(actuales, c);
                if (siguiente.isEmpty()) break;
                actuales = clausuraEpsilon(siguiente);
                longitud++;
                if (contieneEstadoFinal(actuales)) {
                    mejorLongitud = longitud;
                }
            }
            return Math.max(mejorLongitud, 0);
        }

        private Set<Estado> unicoConjunto(Estado e) {
            Set<Estado> s = new LinkedHashSet<>();
            s.add(e);
            return s;
        }

        /** Calcula la clausura-ε de un conjunto de estados. */
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

        /** Devuelve el conjunto de estados alcanzados al consumir 'c'. */
        private Set<Estado> mover(Set<Estado> estados, char c) {
            Set<Estado> destino = new LinkedHashSet<>();
            for (Estado e : estados) {
                for (Transicion t : e.transiciones) {
                    if (t.acepta(c)) {
                        destino.add(t.destino);
                    }
                }
            }
            return destino;
        }

        private boolean contieneEstadoFinal(Set<Estado> estados) {
            for (Estado e : estados) {
                if (e == afn.fin) return true;
            }
            return false;
        }

        /**
         * Clasifica un lexema en una de las categorías léxicas pedidas:
         *   - Palabra Reservada
         *   - Identificador
         *   - Número
         *   - Símbolo
         */
        private String clasificar(String lexema) {
            if (PALABRAS_RESERVADAS.contains(lexema)) {
                return "Palabra Reservada";
            }
            if (esIdentificador(lexema)) {
                return "Identificador";
            }
            if (esNumero(lexema)) {
                return "Número";
            }
            return "Símbolo";
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
