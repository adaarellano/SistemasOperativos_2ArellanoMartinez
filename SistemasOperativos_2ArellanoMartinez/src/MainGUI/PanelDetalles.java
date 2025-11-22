/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;

public class PanelDetalles extends JPanel {
    private ManejadorArchivo manejador;
    private JLabel lblEstado, lblBloques, lblArchivos, lblPlanificador;
    
    public PanelDetalles(ManejadorArchivo manejador) {
        this.manejador = manejador;
        inicializarPanel();
        actualizarDetalles();
    }
    
    private void inicializarPanel() {
        setLayout(new GridLayout(1, 4));
        setBorder(BorderFactory.createTitledBorder("Estado del Sistema en Tiempo Real"));
        
        lblEstado = crearLabelEstilo("Estado: Inicializando...");
        lblBloques = crearLabelEstilo("Bloques: 0/0");
        lblArchivos = crearLabelEstilo("Archivos: 0 creados, 0 eliminados");
        lblPlanificador = crearLabelEstilo("Planificador: FIFO");
        
        add(lblEstado);
        add(lblBloques);
        add(lblArchivos);
        add(lblPlanificador);
    }
    
    private JLabel crearLabelEstilo(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        return label;
    }
    
    public void actualizarDetalles() {
        // 1. Protección contra nulos (Si el manejador no está listo, no hacemos nada)
        if (manejador == null) return;

        String estado = manejador.esAdministrador() ? "ADMIN" : "USUARIO";
        String bloques = "Bloques: " + manejador.getBloquesOcupados() + "/" + manejador.getTotalBloques();
        String archivos = "Archivos: " + manejador.getArchivosCreados() + " creados, " + 
                         manejador.getArchivosEliminados() + " eliminados";
        
        // 2. Protección específica para el Planificador
        String nombrePolitica = "Desconocido";
        if (manejador.getPlanificadorActual() != null) {
            nombrePolitica = manejador.getPlanificadorActual().getNombrePolitica();
        }
        String planificador = "Planificador: " + nombrePolitica;
        
        lblEstado.setText("Modo: " + estado);
        lblBloques.setText(bloques);
        lblArchivos.setText(archivos);
        lblPlanificador.setText(planificador);
    }
}