/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;

public class PanelUsuario extends JPanel {
    private ManejadorArchivo manejador;
    private PanelConsola panelConsola;
    
    public PanelUsuario(ManejadorArchivo manejador, PanelConsola panelConsola) {
        this.manejador = manejador;
        this.panelConsola = panelConsola;
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Modo Usuario (Solo Lectura)"));
        
        // Mensaje informativo
        JTextArea info = new JTextArea();
        info.setText("\n\n   Usted está en Modo Usuario.\n\n" +
                     "   - Puede navegar por el Árbol de Archivos.\n" +
                     "   - Puede ver detalles de archivos públicos.\n" +
                     "   - NO puede crear ni eliminar archivos del sistema.\n\n" +
                     "   Para realizar cambios, contacte al Administrador.");
        
        info.setEditable(false);
        info.setOpaque(false);
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Centrar el texto
        JPanel centro = new JPanel(new GridBagLayout());
        centro.add(info);
        
        add(centro, BorderLayout.CENTER);
    }
}