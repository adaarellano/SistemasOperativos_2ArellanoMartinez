/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import javax.swing.*;
import java.awt.*;

public class PanelConsola extends JPanel {
    private JTextArea areaTexto;
    private JScrollPane scrollPane;
    
    public PanelConsola() {
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Consola del Sistema"));
        
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setBackground(Color.BLACK);
        areaTexto.setForeground(Color.GREEN);
        
        scrollPane = new JScrollPane(areaTexto);
        add(scrollPane, BorderLayout.CENTER);
        
        // Mensaje inicial
        agregarLinea("=== SISTEMA DE ARCHIVOS INICIADO ===");
        agregarLinea("Seleccione un modo para comenzar...");
    }
    
    public void agregarLinea(String texto) {
        SwingUtilities.invokeLater(() -> {
            areaTexto.append(texto + "\n");
            // Auto-scroll al final
            areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
        });
        
        // Simular delay para ver línea por línea
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void limpiarConsola() {
        SwingUtilities.invokeLater(() -> {
            areaTexto.setText("");
        });
    }
}