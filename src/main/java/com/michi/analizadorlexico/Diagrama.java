/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.michi.analizadorlexico;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JScrollPane;

/**
 * Ventana que muestra el diagrama (grafo) del AFN construido con el
 * algoritmo de Thompson para una expresión regular.
 *
 * <p>El formulario asociado ({@code Diagrama.form}) contiene un único
 * {@link javax.swing.JPanel} llamado {@code jGraph} que ocupa toda la
 * ventana. El motor de pintura ({@link ThompsonGraphPanel}) se inserta
 * dentro de {@code jGraph} de forma programática (envuelto en un
 * {@code JScrollPane} para soportar grafos grandes), de modo que el
 * archivo {@code .form} permanece sencillo y editable visualmente en
 * NetBeans sin requerir clases personalizadas.</p>
 *
 * <p>La API pública {@link #mostrar(String)} es la entrada principal
 * desde otros formularios (en particular desde el botón
 * {@code bDiagrama} de {@link Ventana_Thompson}).</p>
 *
 * @author harol
 */
public class Diagrama extends javax.swing.JFrame {

    private static Diagrama instancia;

    /** Lienzo donde se dibuja realmente el grafo (hijo de {@code jGraph}). */
    private final ThompsonGraphPanel lienzo = new ThompsonGraphPanel();

    public Diagrama() {
        initComponents();
        // jGraph es un JPanel "limpio" generado por el .form. Aquí lo
        // configuramos para que albergue el lienzo de pintura dentro de
        // un JScrollPane, ocupando todo su área.
        jGraph.setLayout(new BorderLayout());
        jGraph.add(new JScrollPane(lienzo), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        lienzo.setMensaje("Escriba una expresión regular y pulse 'Diagrama' "
                + "en la ventana principal.", false);
    }

    /**
     * Abre (o reutiliza) la ventana de diagrama y dibuja el AFN
     * correspondiente a {@code expresion}. Si la expresión contiene
     * varios patrones ({@code TIPO=regex;...}), se dibuja únicamente
     * el primero — la GUI sólo dispone de un lienzo. El usuario puede
     * cambiar la expresión en la ventana principal y volver a pulsar
     * el botón "Diagrama" para refrescarlo.
     */
    public static void mostrar(String expresion) {
        // Si la ventana se cerró previamente con DISPOSE_ON_CLOSE, el frame
        // ya no es "displayable" y debe recrearse antes de volver a usarse.
        if (instancia == null || !instancia.isDisplayable()) {
            instancia = new Diagrama();
        }
        instancia.actualizarDesdeExpresion(expresion);
        instancia.setVisible(true);
        instancia.toFront();
        instancia.requestFocus();
    }

    /** Actualiza el diagrama a partir de la expresión recibida. */
    public void actualizarDesdeExpresion(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            setTitle("Diagrama de Thompson");
            lienzo.setMensaje("La expresión regular está vacía.", true);
            return;
        }
        // Aceptar el modo multi-patrón "TIPO=regex;..." reutilizando el
        // mismo parser que Ventana_Thompson para mantener consistencia.
        List<Ventana_Thompson.PatronRegex> patrones;
        try {
            patrones = Ventana_Thompson.parsearPatrones(expresion);
        } catch (RuntimeException ex) {
            setTitle("Diagrama de Thompson");
            lienzo.setMensaje("No fue posible interpretar la expresión: "
                    + ex.getMessage(), true);
            return;
        }
        if (patrones.isEmpty()) {
            setTitle("Diagrama de Thompson");
            lienzo.setMensaje("La expresión regular está vacía.", true);
            return;
        }

        Ventana_Thompson.PatronRegex primero = patrones.get(0);
        String regex = primero.regex;
        try {
            ThompsonGraph grafo = ThompsonGraph.fromRegex(regex);
            lienzo.setGrafo(grafo);
            if (patrones.size() > 1) {
                setTitle("Diagrama de Thompson  —  patrón '" + primero.tipo
                        + "'  (mostrando el primero de " + patrones.size() + ")");
            } else {
                setTitle("Diagrama de Thompson  —  " + regex);
            }
        } catch (RuntimeException ex) {
            setTitle("Diagrama de Thompson");
            lienzo.setMensaje("No fue posible construir el diagrama: "
                    + ex.getMessage(), true);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jGraph = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Diagrama de Thompson");
        setPreferredSize(new java.awt.Dimension(900, 500));

        jGraph.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jGraphLayout = new javax.swing.GroupLayout(jGraph);
        jGraph.setLayout(jGraphLayout);
        jGraphLayout.setHorizontalGroup(
            jGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 900, Short.MAX_VALUE)
        );
        jGraphLayout.setVerticalGroup(
            jGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(Diagrama.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Diagrama d = new Diagrama();
                d.actualizarDesdeExpresion("(a|b)*abb");
                d.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jGraph;
    // End of variables declaration//GEN-END:variables
}
