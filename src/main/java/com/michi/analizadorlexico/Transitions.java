/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.michi.analizadorlexico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
 * @author harol
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
}
