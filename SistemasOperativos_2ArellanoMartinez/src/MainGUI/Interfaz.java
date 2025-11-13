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
    private PanelOutput panelOutput;
    private JComboBox<String> comboPoliticas;
    
    public Interfaz() {
        this.manejador = new ManejadorArchivo();
        inicializarGUI();
    }
    
    private void inicializarGUI() {
        setTitle("Sistema de Archivos - Simulador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior - Modos
        JPanel panelSuperior = crearPanelSuperior();
        
        // Panel central
        JSplitPane splitPrincipal = crearPanelCentral();
        
        add(panelSuperior, BorderLayout.NORTH);
        add(splitPrincipal, BorderLayout.CENTER);
        
        configurarEventos();
        
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new FlowLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Control del Sistema"));
        
        btnModoAdmin = new JButton("Modo Administrador");
        btnModoUsuario = new JButton("Modo Usuario");
        
        comboPoliticas = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        comboPoliticas.setEnabled(false);
        
        panelSuperior.add(btnModoAdmin);
        panelSuperior.add(btnModoUsuario);
        panelSuperior.add(new JLabel("Política:"));
        panelSuperior.add(comboPoliticas);
        
        return panelSuperior;
    }
    
    private JSplitPane crearPanelCentral() {
        // Panel izquierdo - Archivos
        panelArchivos = new PanelArchivos(manejador);
        
        // Panel derecho dividido verticalmente
        JSplitPane splitDerecho = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Panel superior derecho - Consola
        panelConsola = new PanelConsola();
        manejador.setPanelConsola(panelConsola);
        
        // Panel inferior derecho - Output
        panelOutput = new PanelOutput();
        manejador.setPanelOutput(panelOutput);
        
        splitDerecho.setTopComponent(panelConsola);
        splitDerecho.setBottomComponent(panelOutput);
        splitDerecho.setDividerLocation(350);
        
        // Panel de control (oculto inicialmente)
        panelControl = new PanelControl(manejador, panelArchivos, panelConsola, panelOutput);
        panelControl.setVisible(false);
        
        // Panel principal izquierdo con archivos y controles
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.add(panelArchivos, BorderLayout.CENTER);
        panelIzquierdo.add(panelControl, BorderLayout.SOUTH);
        
        // Split principal
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                                panelIzquierdo, splitDerecho);
        splitPrincipal.setDividerLocation(400);
        
        return splitPrincipal;
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
        
        String modo = esAdmin ? "ADMINISTRADOR" : "USUARIO";
        panelConsola.agregarLinea("=== MODO " + modo + " ACTIVADO ===");
        
        if (esAdmin) {
            panelConsola.agregarLinea("Operaciones CRUD disponibles");
        } else {
            panelConsola.agregarLinea("Solo lectura - Ver archivos");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
    }
}