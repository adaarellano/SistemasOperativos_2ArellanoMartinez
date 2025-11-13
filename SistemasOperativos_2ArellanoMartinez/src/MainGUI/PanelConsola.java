package MainGUI;

import javax.swing.*;
import java.awt.*;

public class PanelConsola extends JPanel {
    private JTextArea textArea;
    private JScrollPane scrollPane;
    
    public PanelConsola() {
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Consola del Sistema"));
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Mensaje inicial
        agregarLinea("=== CONSOLA DEL SISTEMA DE ARCHIVOS ===");
        agregarLinea("Sistema inicializado correctamente");
        agregarLinea("Modo actual: USUARIO (solo lectura)");
    }
    
    public void agregarLinea(String texto) {
        SwingUtilities.invokeLater(() -> {
            textArea.append("> " + texto + "\n");
            
            // Auto-scroll al final
            int length = textArea.getDocument().getLength();
            textArea.setCaretPosition(length);
        });
    }
    
    public void limpiarConsola() {
        SwingUtilities.invokeLater(() -> {
            textArea.setText("");
            agregarLinea("=== CONSOLA LIMPIADA ===");
            agregarLinea("Sistema listo...");
        });
    }
}