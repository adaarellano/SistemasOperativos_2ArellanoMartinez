/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import javax.swing.*;
import java.awt.*;

public class PanelOutput extends JPanel {
    private JTextArea areaTexto;
    private JScrollPane scrollPane;
    
    public PanelOutput() {
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Detalles del Sistema - Bytes y Bloques"));
        
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setBackground(Color.WHITE);
        areaTexto.setForeground(Color.BLACK);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        
        scrollPane = new JScrollPane(areaTexto);
        add(scrollPane, BorderLayout.CENTER);
        
        // Mensaje inicial
        agregarLinea("=== DETALLES DEL SISTEMA ===");
        agregarLinea("Aquí se mostrarán los detalles de bytes, bloques y asignaciones");
        agregarLinea("------------------------------------------------------------");
    }
    
    public void agregarLinea(String texto) {
        SwingUtilities.invokeLater(() -> {
            areaTexto.append(texto + "\n");
            areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
        });
    }
    
    public void limpiar() {
        SwingUtilities.invokeLater(() -> {
            areaTexto.setText("");
            agregarLinea("=== DETALLES DEL SISTEMA ===");
            agregarLinea("------------------------------------------------------------");
        });
    }
    
    public void mostrarDetallesArchivo(String nombre, int bytesReales, int bytesReservados, 
                                     int bloquesUsados, int bloquesReservados, String cadenaBloques) {
        SwingUtilities.invokeLater(() -> {
            agregarLinea("📊 DETALLES DE ARCHIVO: " + nombre);
            agregarLinea("   Bytes reales usados: " + bytesReales + " bytes");
            agregarLinea("   Bytes reservados: " + bytesReservados + " bytes");
            agregarLinea("   Espacio libre: " + (bytesReservados - bytesReales) + " bytes");
            agregarLinea("   Bloques usados: " + bloquesUsados + "/" + bloquesReservados);
            
            // Evitar división por cero si bloquesReservados es 0 (aunque no debería pasar)
            double eficiencia = (bytesReservados > 0) ? (bytesReales * 100.0 / bytesReservados) : 0.0;
            
            agregarLinea("   Eficiencia: " + String.format("%.1f", eficiencia) + "%");
            agregarLinea("   Cadena de bloques: " + cadenaBloques);
            agregarLinea("------------------------------------------------------------");
        });
    }
}