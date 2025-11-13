package MainGUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class PanelOutput extends JPanel {
    private JTextArea textArea;
    private JScrollPane scrollPane;
    
    public PanelOutput() {
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Output del Sistema"));
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Agregar texto inicial
        agregarLinea("=== SISTEMA DE ARCHIVOS INICIADO ===");
        agregarLinea("Esperando operaciones...");
    }
    
    public void agregarLinea(String texto) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(texto + "\n");
            
            // Auto-scroll al final
            int length = textArea.getDocument().getLength();
            textArea.setCaretPosition(length);
        });
    }
    
    public void limpiarOutput() {
        SwingUtilities.invokeLater(() -> {
            textArea.setText("");
            agregarLinea("=== OUTPUT LIMPIADO ===");
            agregarLinea("Sistema listo...");
        });
    }
}