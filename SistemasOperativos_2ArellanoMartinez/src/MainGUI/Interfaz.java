/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interfaz extends JFrame {
    private ManejadorArchivo manejador;
    private boolean esModoAdministrador = false;
    
    // Componentes principales
    private JButton btnModoAdmin, btnModoUsuario;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelControl panelControl;
    private JComboBox<String> comboPoliticas;
    
    public Interfaz() {
        this.manejador = new ManejadorArchivo();
        inicializarGUI();
    }
    
    private void inicializarGUI() {
        setTitle("Sistema de Archivos - Simulador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior - Modos y políticas
        JPanel panelSuperior = new JPanel(new FlowLayout());
        btnModoAdmin = new JButton("Modo Administrador");
        btnModoUsuario = new JButton("Modo Usuario");
        
        comboPoliticas = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        comboPoliticas.setEnabled(false); // Solo admin puede cambiar
        
        panelSuperior.add(btnModoAdmin);
        panelSuperior.add(btnModoUsuario);
        panelSuperior.add(new JLabel("Política:"));
        panelSuperior.add(comboPoliticas);
        
        // Paneles principales
        panelArchivos = new PanelArchivos(manejador);
        panelConsola = new PanelConsola();
        panelControl = new PanelControl(manejador, panelArchivos, panelConsola);
        panelControl.setVisible(false); // Inicialmente oculto
        
        // Configurar división
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                            panelArchivos, panelConsola);
        splitPane.setDividerLocation(400);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
        
        configurarEventos();
        
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private void configurarEventos() {
        btnModoAdmin.addActionListener(e -> cambiarModo(true));
        btnModoUsuario.addActionListener(e -> cambiarModo(false));
        
        comboPoliticas.addActionListener(e -> {
            if (esModoAdministrador) {
                String politica = (String) comboPoliticas.getSelectedItem();
                manejador.cambiarPlanificador(politica);
                panelConsola.agregarLinea("Política cambiada a: " + politica);
            }
        });
    }
    
    private void cambiarModo(boolean esAdmin) {
        this.esModoAdministrador = esAdmin;
        panelControl.setVisible(esAdmin);
        comboPoliticas.setEnabled(esAdmin);
        panelArchivos.actualizarVista(esAdmin);
        
        String modo = esAdmin ? "ADMINISTRADOR" : "USUARIO";
        panelConsola.agregarLinea("=== MODO " + modo + " ACTIVADO ===");
        
        if (esAdmin) {
            panelConsola.agregarLinea("Acceso completo al sistema");
            panelConsola.agregarLinea("Puede crear, editar y eliminar archivos");
        } else {
            panelConsola.agregarLinea("Acceso limitado - Solo lectura");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
    }
}