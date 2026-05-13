/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.michi.analizadorlexico;

import java.awt.BasicStroke;
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

import javax.swing.JPanel;

import com.michi.analizadorlexico.ThompsonGraph.Arista;
import com.michi.analizadorlexico.ThompsonGraph.EstiloArista;
import com.michi.analizadorlexico.ThompsonGraph.Nodo;

/**
 * Panel que pinta un {@link ThompsonGraph}. Cada estado se dibuja como
 * un círculo con su identificador {@code qN} centrado, y cada transición
 * como una flecha (recta o curva) etiquetada.
 *
 * <p>El panel ajusta automáticamente su {@code preferredSize} al tamaño
 * del grafo, de modo que un {@code JScrollPane} contenedor pueda
 * desplazarse si el diagrama excede la ventana.</p>
 *
 * @author harol
 */
public class ThompsonGraphPanel extends JPanel {

    // ----- Estilo gráfico. -----
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

    private static final int FLECHA_LONG = 9;
    private static final int FLECHA_ANCHO = 5;

    // ----- Estado interno. -----
    private ThompsonGraph grafo;
    private String mensaje;
    private boolean mensajeEsError;

    public ThompsonGraphPanel() {
        setBackground(COLOR_FONDO);
        setOpaque(true);
        setPreferredSize(new Dimension(400, 200));
    }

    /** Asigna el grafo que se debe pintar; refresca el tamaño y dispara repintado. */
    public void setGrafo(ThompsonGraph grafo) {
        this.grafo = grafo;
        this.mensaje = null;
        this.mensajeEsError = false;
        if (grafo != null) {
            setPreferredSize(new Dimension(
                    Math.max(grafo.getAncho(), 200),
                    Math.max(grafo.getAlto(), 100)));
        }
        revalidate();
        repaint();
    }

    /** Muestra un mensaje en lugar del diagrama (regex vacía, error, ayuda, etc.). */
    public void setMensaje(String mensaje, boolean esError) {
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

            if (grafo == null) {
                pintarMensaje(g2);
                return;
            }
            pintarGrafo(g2, grafo);
        } finally {
            g2.dispose();
        }
    }

    // =================================================================
    //   PINTURA DEL MENSAJE (estado inicial / error).
    // =================================================================

    private void pintarMensaje(Graphics2D g2) {
        String texto = mensaje != null
                ? mensaje
                : "Escriba una expresión regular y abra el diagrama.";
        g2.setColor(mensajeEsError ? COLOR_ERROR : COLOR_INFO);
        g2.setFont(FUENTE_INFO);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(texto)) / 2;
        int y = getHeight() / 2;
        g2.drawString(texto, Math.max(10, x), y);
    }

    // =================================================================
    //   PINTURA DEL GRAFO.
    // =================================================================

    private void pintarGrafo(Graphics2D g2, ThompsonGraph grafo) {
        // 1) Aristas primero, para que los círculos queden encima.
        g2.setStroke(TRAZO_ARISTA);
        g2.setFont(FUENTE_ETIQUETA);
        for (Arista a : grafo.getAristas()) {
            pintarArista(g2, a);
        }

        // 2) Marcador "→" hacia el estado inicial para hacerlo evidente.
        Nodo ini = grafo.getInicial();
        if (ini != null) {
            pintarMarcadorInicial(g2, ini);
        }

        // 3) Nodos.
        g2.setStroke(TRAZO_NODO);
        g2.setFont(FUENTE_NODO);
        for (Nodo n : grafo.getNodos()) {
            pintarNodo(g2, n);
        }
    }

    private void pintarNodo(Graphics2D g2, Nodo n) {
        int r = ThompsonGraph.RADIO_NODO;
        double cx = n.getX();
        double cy = n.getY();
        Ellipse2D e = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);

        Color relleno;
        Color borde;
        if (n.isAceptante()) {
            relleno = COLOR_NODO_ACEPT;
            borde = COLOR_BORDE_ACEPT;
        } else if (n.isInicial()) {
            relleno = COLOR_NODO_INI;
            borde = COLOR_BORDE_INI;
        } else {
            relleno = COLOR_NODO;
            borde = COLOR_BORDE;
        }
        g2.setColor(relleno);
        g2.fill(e);
        g2.setColor(borde);
        g2.draw(e);

        // Doble círculo para el estado aceptante (notación clásica).
        if (n.isAceptante()) {
            int rr = r - 3;
            g2.draw(new Ellipse2D.Double(cx - rr, cy - rr, 2 * rr, 2 * rr));
        }

        // Etiqueta centrada.
        g2.setColor(COLOR_ETIQUETA);
        FontMetrics fm = g2.getFontMetrics();
        String label = n.getLabel();
        int tx = (int) Math.round(cx - fm.stringWidth(label) / 2.0);
        int ty = (int) Math.round(cy + (fm.getAscent() - fm.getDescent()) / 2.0);
        g2.drawString(label, tx, ty);
    }

    private void pintarMarcadorInicial(Graphics2D g2, Nodo ini) {
        int r = ThompsonGraph.RADIO_NODO;
        double endX = ini.getX() - r;
        double endY = ini.getY();
        double startX = endX - 16;
        double startY = endY;
        g2.setColor(COLOR_BORDE_INI);
        g2.setStroke(TRAZO_ARISTA);
        g2.draw(new Line2D.Double(startX, startY, endX, endY));
        pintarCabezaFlecha(g2, endX, endY, 1.0, 0.0, COLOR_BORDE_INI);
    }

    // =================================================================
    //   PINTURA DE UNA ARISTA: recta, curva arriba, curva abajo o bucle.
    // =================================================================

    private void pintarArista(Graphics2D g2, Arista a) {
        double ax = a.getDesde().getX();
        double ay = a.getDesde().getY();
        double bx = a.getHasta().getX();
        double by = a.getHasta().getY();

        switch (a.getEstilo()) {
            case CURVA_ARRIBA:
                pintarCurva(g2, ax, ay, bx, by, -1, a.getCurvatura(), a.getEtiqueta());
                break;
            case CURVA_ABAJO:
                pintarCurva(g2, ax, ay, bx, by, +1, a.getCurvatura(), a.getEtiqueta());
                break;
            case BUCLE:
                pintarBucle(g2, ax, ay, a.getEtiqueta());
                break;
            case RECTA:
            default:
                pintarRecta(g2, ax, ay, bx, by, a.getEtiqueta());
        }
    }

    private void pintarRecta(Graphics2D g2, double ax, double ay, double bx, double by, String etiqueta) {
        int r = ThompsonGraph.RADIO_NODO;
        double dx = bx - ax;
        double dy = by - ay;
        double dist = Math.hypot(dx, dy);
        if (dist < 1e-6) return;
        double ux = dx / dist;
        double uy = dy / dist;
        double sx = ax + ux * r;
        double sy = ay + uy * r;
        double ex = bx - ux * r;
        double ey = by - uy * r;

        g2.setColor(COLOR_ARISTA);
        g2.draw(new Line2D.Double(sx, sy, ex, ey));
        pintarCabezaFlecha(g2, ex, ey, ux, uy, COLOR_ARISTA);

        // Etiqueta a 1/2 de la línea, desplazada perpendicularmente
        // hacia "arriba" (perpendicular izquierdo).
        if (etiqueta != null && !etiqueta.isEmpty()) {
            double mx = (sx + ex) / 2;
            double my = (sy + ey) / 2;
            double px = -uy;
            double py = ux;
            // En coords de pantalla, "arriba" tiene y menor; perp con py<0 es arriba si ux>0.
            // Elegimos el lado que minimice ocultar la línea: el lado opuesto al sentido de avance.
            double offset = 10;
            // Asegurar la etiqueta arriba de la línea cuando es horizontal-ish.
            if (py > 0) { px = -px; py = -py; }
            pintarEtiqueta(g2, etiqueta, mx + px * offset, my + py * offset);
        }
    }

    private void pintarCurva(Graphics2D g2, double ax, double ay, double bx, double by,
                              int signoVertical, double curvatura, String etiqueta) {
        int r = ThompsonGraph.RADIO_NODO;
        // Por defecto, asumimos arcos entre nodos a la misma altura (skip / back-edge);
        // el "arriba" o "abajo" se calcula puramente como un desvío vertical del punto medio.
        double mx = (ax + bx) / 2;
        double my = (ay + by) / 2;
        double offset = Math.max(curvatura, 18);
        double cx = mx;
        double cy = my + signoVertical * offset;

        // Calcular puntos de inicio y fin en el borde de los círculos,
        // en dirección a la primera tangente de la curva.
        double t0x = cx - ax;
        double t0y = cy - ay;
        double l0 = Math.hypot(t0x, t0y);
        double sx = ax + (t0x / l0) * r;
        double sy = ay + (t0y / l0) * r;

        double t1x = cx - bx;
        double t1y = cy - by;
        double l1 = Math.hypot(t1x, t1y);
        double ex = bx + (t1x / l1) * r;
        double ey = by + (t1y / l1) * r;

        QuadCurve2D q = new QuadCurve2D.Double(sx, sy, cx, cy, ex, ey);
        g2.setColor(COLOR_ARISTA);
        g2.draw(q);

        // Dirección tangente en el extremo final de la curva: B'(1) = 2*(P2 - P1).
        double tex = 2 * (ex - cx);
        double tey = 2 * (ey - cy);
        double tl = Math.hypot(tex, tey);
        if (tl > 1e-6) {
            pintarCabezaFlecha(g2, ex, ey, tex / tl, tey / tl, COLOR_ARISTA);
        }

        // Etiqueta cerca de la cúspide del arco.
        if (etiqueta != null && !etiqueta.isEmpty()) {
            // Punto en t=0.5: (P0 + 2*P1 + P2)/4
            double labelX = (sx + 2 * cx + ex) / 4;
            double labelY = (sy + 2 * cy + ey) / 4;
            // Empujar la etiqueta levemente más en la dirección de la curva.
            labelY += signoVertical * 8;
            pintarEtiqueta(g2, etiqueta, labelX, labelY);
        }
    }

    private void pintarBucle(Graphics2D g2, double ax, double ay, String etiqueta) {
        // Reservado para futuras extensiones; en las construcciones de Thompson
        // nunca se generan aristas de autobucle directas.
        int r = ThompsonGraph.RADIO_NODO;
        double cx = ax;
        double cy = ay - r - 8;
        Ellipse2D loop = new Ellipse2D.Double(cx - 10, cy - 10, 20, 20);
        g2.setColor(COLOR_ARISTA);
        g2.draw(loop);
        if (etiqueta != null && !etiqueta.isEmpty()) {
            pintarEtiqueta(g2, etiqueta, cx, cy - 12);
        }
    }

    private void pintarCabezaFlecha(Graphics2D g2, double x, double y,
                                     double ux, double uy, Color color) {
        // Triángulo con base en (x - ux*L, y - uy*L), abierto ±ANCHO en perpendicular.
        double bx = x - ux * FLECHA_LONG;
        double by = y - uy * FLECHA_LONG;
        double px = -uy;
        double py = ux;
        double x1 = bx + px * FLECHA_ANCHO;
        double y1 = by + py * FLECHA_ANCHO;
        double x2 = bx - px * FLECHA_ANCHO;
        double y2 = by - py * FLECHA_ANCHO;
        Path2D.Double cab = new Path2D.Double();
        cab.moveTo(x, y);
        cab.lineTo(x1, y1);
        cab.lineTo(x2, y2);
        cab.closePath();
        Color anterior = g2.getColor();
        g2.setColor(color);
        g2.fill(cab);
        g2.setColor(anterior);
    }

    private void pintarEtiqueta(Graphics2D g2, String texto, double x, double y) {
        Font anterior = g2.getFont();
        g2.setFont(FUENTE_ETIQUETA);
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(texto);
        int h = fm.getAscent();
        // Fondo blanco semitransparente para que la etiqueta sea legible aunque
        // se superponga a una línea.
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
