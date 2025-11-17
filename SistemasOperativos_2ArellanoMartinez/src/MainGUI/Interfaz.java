/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import Models.Archivo;
import Models.Directorio;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edd.ListaSimple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Interfaz extends JFrame {
    private ManejadorArchivo manejador;
    private boolean esModoAdministrador = false;
    
    // Componentes principales
    private JButton btnModoAdmin, btnModoUsuario;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelControl panelControl;
    private PanelDetalles panelDetalles; // NUEVO: Panel para detalles
    private JComboBox<String> comboPoliticas;
    private JSplitPane splitPrincipal;

    
    // Para persistencia JSON
    private Gson gson;
    private final String ARCHIVO_CONFIG = "sistema_archivos.json";
    
    public Interfaz() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.manejador = new ManejadorArchivo();
        inicializarGUI();
        cargarEstado(); // Cargar estado anterior si existe
    }
    
    private void inicializarGUI() {
        setTitle("Sistema de Archivos - Simulador Avanzado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior - Modos y políticas
        JPanel panelSuperior = crearPanelSuperior();
        splitPrincipal = crearPanelCentral();
        // Panel central dividido en 3 partes
        JSplitPane splitPrincipal = crearPanelCentral();
        
        // Panel inferior - Detalles del sistema
        panelDetalles = new PanelDetalles(manejador);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(splitPrincipal, BorderLayout.CENTER);
        add(panelDetalles, BorderLayout.SOUTH);
        
        configurarEventos();
        
        setSize(1200, 800); // Más grande para los 3 paneles
        setLocationRelativeTo(null);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new FlowLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Control del Sistema"));
        
        btnModoAdmin = new JButton("Modo Administrador");
        btnModoUsuario = new JButton("Modo Usuario");
        
        comboPoliticas = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        comboPoliticas.setEnabled(false);
        
        JButton btnGuardar = new JButton("Guardar Estado");
        JButton btnCargar = new JButton("Cargar Estado");
        
        panelSuperior.add(btnModoAdmin);
        panelSuperior.add(btnModoUsuario);
        panelSuperior.add(new JLabel("Política:"));
        panelSuperior.add(comboPoliticas);
        panelSuperior.add(btnGuardar);
        panelSuperior.add(btnCargar);
        
        // Configurar eventos de guardar/cargar
        btnGuardar.addActionListener(e -> guardarEstado());
        btnCargar.addActionListener(e -> cargarEstado());
        
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
        PanelOutput panelOutput = new PanelOutput();
        manejador.setPanelOutput(panelOutput);
        
        splitDerecho.setTopComponent(panelConsola);
        splitDerecho.setBottomComponent(panelOutput);
        splitDerecho.setDividerLocation(300);

        // **CORRECCIÓN: PanelControl debe estar en el layout principal, no en el split**
        panelControl = new PanelControl(manejador, panelArchivos, panelConsola, panelOutput);
        
        // Split principal (izquierda: archivos, derecha: consola+output)
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                                panelArchivos, splitDerecho);
        splitPrincipal.setDividerLocation(500);
        
        return splitPrincipal;
    }
    
    private void configurarEventos() {
        btnModoAdmin.addActionListener(e -> cambiarModo(true));
        btnModoUsuario.addActionListener(e -> cambiarModo(false));
        
        comboPoliticas.addActionListener(e -> {
            if (esModoAdministrador) {
                String politica = (String) comboPoliticas.getSelectedItem();
                manejador.cambiarPlanificador(politica);
                panelDetalles.actualizarDetalles();
            }
        });
    }
    
     private void cambiarModo(boolean esAdmin) {
        this.esModoAdministrador = esAdmin;
        manejador.setEsAdministrador(esAdmin); // **IMPORTANTE: Actualizar el manejador**
        
        // **CORRECCIÓN: Mostrar/ocultar el panel de control en el layout principal**
        if (esAdmin) {
            // Agregar panelControl al sur (inferior) del frame
            add(panelControl, BorderLayout.SOUTH);
        } else {
            // Remover panelControl
            remove(panelControl);
        }
        
        comboPoliticas.setEnabled(esAdmin);
        panelArchivos.actualizarVista(esAdmin);
        
        String modo = esAdmin ? "ADMINISTRADOR" : "USUARIO";
        panelConsola.agregarLinea("=== MODO " + modo + " ACTIVADO ===");
        
        // **IMPORTANTE: Revalidar y repintar para que los cambios se reflejen**
        revalidate();
        repaint();
        
        panelDetalles.actualizarDetalles();
    }

    // ===== PERSISTENCIA CON JSON =====
    
    private void guardarEstado() {
        try {
            // Crear objeto de estado para guardar
            EstadoSistema estado = new EstadoSistema(manejador);
            String json = gson.toJson(estado);
            
            try (FileWriter writer = new FileWriter(ARCHIVO_CONFIG)) {
                writer.write(json);
            }
            
            panelConsola.agregarLinea("✅ Estado del sistema guardado en: " + ARCHIVO_CONFIG);
            panelConsola.agregarLinea("Ubicación: " + System.getProperty("user.dir"));
            
        } catch (IOException e) {
            panelConsola.agregarLinea("❌ Error al guardar estado: " + e.getMessage());
        }
    }
    
    private void cargarEstado() {
        try {
            // Por simplicidad, en esta versión solo mostramos un mensaje
            // En una versión completa aquí cargarías el estado desde JSON
            panelConsola.agregarLinea("🔄 Función de carga en desarrollo...");
            panelConsola.agregarLinea("Los archivos se guardan en la memoria del sistema");
            
        } catch (Exception e) {
            panelConsola.agregarLinea("❌ Error al cargar estado: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
    }
    
    private static class EstadoSistema {
    private int archivosCreados;
    private int archivosEliminados;
    private int operacionesRealizadas;
    private int bloquesOcupados;
    private String planificadorActual;
    private String usuarioActual;
    private boolean esModoAdministrador;
    
    public EstadoSistema(ManejadorArchivo manejador) {
        this.archivosCreados = manejador.getArchivosCreados();
        this.archivosEliminados = manejador.getArchivosEliminados();
        this.operacionesRealizadas = manejador.getOperacionesRealizadas();
        this.bloquesOcupados = manejador.getBloquesOcupados();
        this.planificadorActual = manejador.getPlanificadorActual(); // Ahora funciona
        this.usuarioActual = manejador.getUsuarioActual(); // Ahora funciona
        this.esModoAdministrador = manejador.esAdministrador();
    }
}
        }