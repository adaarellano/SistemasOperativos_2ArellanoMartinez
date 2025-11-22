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
    
    /**
     * Muestra la "Ficha Técnica" de un archivo seleccionado.
     * Calcula fragmentación y muestra la ruta física.
     */
    public void mostrarFichaTecnica(Models.Archivo archivo) {
        SwingUtilities.invokeLater(() -> {
            areaTexto.setText(""); // Limpiar pantalla anterior
            
            StringBuilder sb = new StringBuilder();
            sb.append("============================================\n");
            sb.append("       FICHA TÉCNICA DEL ARCHIVO\n");
            sb.append("============================================\n\n");
            
            sb.append("📄 NOMBRE:      ").append(archivo.getNombre()).append("\n");
            sb.append("📂 RUTA:        ").append(archivo.getRutaCompleta()).append("\n");
            sb.append("👤 PROPIETARIO: ").append(archivo.getUsuarioPropietario()).append("\n");
            sb.append("📅 CREADO:      ").append(archivo.getFechaCreacion()).append("\n\n");
            
            sb.append("--- ALMACENAMIENTO (LÓGICO) ---\n");
            sb.append("Tamaño de datos:   ").append(archivo.getTamañoBytes()).append(" bytes\n");
            sb.append("Contenido:         \"").append(archivo.leerContenido()).append("\"\n\n");
            
            sb.append("--- ALMACENAMIENTO (FÍSICO) ---\n");
            sb.append("Bloques reservados: ").append(archivo.getBloquesReservados()).append("\n");
            sb.append("Espacio total:      ").append(archivo.getTamañoReservadoBytes()).append(" bytes\n");
            
            // CÁLCULO DE FRAGMENTACIÓN (Desperdicio)
            int desperdicio = archivo.getTamañoReservadoBytes() - archivo.getTamañoBytes();
            double porcentajeDesperdicio = 0;
            if (archivo.getTamañoReservadoBytes() > 0) {
                porcentajeDesperdicio = (desperdicio * 100.0) / archivo.getTamañoReservadoBytes();
            }
            
            sb.append("Espacio desperdiciado: ").append(desperdicio).append(" bytes (")
              .append(String.format("%.2f", porcentajeDesperdicio)).append("%)\n");
            sb.append("Estado: ").append(desperdicio == 0 ? "OPTIMIZADO" : "FRAGMENTACIÓN INTERNA").append("\n\n");
            
            sb.append("--- MAPA DE BLOQUES ---\n");
            sb.append(archivo.getInfoBloques()).append("\n");
            
            areaTexto.setText(sb.toString());
            areaTexto.setCaretPosition(0); // Ir al inicio
        });
    }
    
    public void mostrarMensajeVacio() {
        SwingUtilities.invokeLater(() -> {
            areaTexto.setText("\n\n   Seleccione un archivo en el árbol\n   para ver sus detalles técnicos.");
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