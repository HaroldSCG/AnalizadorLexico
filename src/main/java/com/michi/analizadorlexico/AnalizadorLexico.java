/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.michi.analizadorlexico;
import javax.swing.JFrame;

/**
 *
 * @author michi
 */
public class AnalizadorLexico {

    public static void main(String[] args) {
        JFrame ventana= new JFrame("NewJPanel");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(900, 500);

        Ventana panel = new Ventana();  // Instancias el JPanel

        ventana.add(panel);             // Lo agregas al JFrame
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
