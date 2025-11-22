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
    private PanelDetalles panelDetalles; 
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
    private JComboBox<String> comboPoliticas;
    private PanelProcesos panelProcesos;
    
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
        
        // Panel central dividido en 3 partes
        JSplitPane splitPrincipal = crearPanelCentral();
        
        // Panel inferior - Detalles del sistema
        panelDetalles = new PanelDetalles(manejador);
        manejador.setPanelDetalles(panelDetalles);
        
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
        // === PANEL IZQUIERDO (Archivos + Disco) ===
        JPanel panelIzquierdo = new JPanel(new BorderLayout());

        // 1. Panel de Archivos (JTree)
        panelArchivos = new PanelArchivos(manejador);
        manejador.setPanelArchivos(panelArchivos);

        // 2. Panel de Disco (Cuadrícula)
        panelDisco = new PanelDisco(manejador); 
        manejador.setPanelDisco(panelDisco);

        // Usamos un JSplitPane vertical para el lado izquierdo
        JSplitPane splitIzquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                 panelArchivos, panelDisco);
        splitIzquierdo.setDividerLocation(400); // Ajusta el tamaño del árbol
        panelIzquierdo.add(splitIzquierdo, BorderLayout.CENTER);

        // === PANEL DERECHO (Con Pestañas) ===
        JTabbedPane panelDerecho = new JTabbedPane();

        // Pestaña 1: Consola
        panelConsola = new PanelConsola();
        manejador.setPanelConsola(panelConsola);
        panelDerecho.addTab("Consola", panelConsola);

        // Pestaña 2: Gestor de Procesos (NUEVO)
        panelProcesos = new PanelProcesos(manejador);
        panelDerecho.addTab("Gestor de Procesos", panelProcesos);

        // Pestaña 3: Tabla de Asignación
        panelTablaAsignacion = new PanelTablaAsignacion(manejador);
        manejador.setPanelTablaAsignacion(panelTablaAsignacion);
        panelDerecho.addTab("Tabla de Asignación", panelTablaAsignacion);

        // Pestaña 4: Detalles (Bytes)
        PanelOutput panelOutput = new PanelOutput();
        manejador.setPanelOutput(panelOutput);
        panelDerecho.addTab("Detalles (Bytes)", panelOutput);

        // === PANEL DE CONTROL (Oculto inicialmente) ===
        panelControl = new PanelControl(manejador, panelArchivos, panelConsola, panelOutput, panelDisco, panelTablaAsignacion);
        panelControl.setVisible(false);
        
        // Agregamos el panel de control en la parte superior del panel izquierdo
        panelIzquierdo.add(panelControl, BorderLayout.NORTH);
        
        // === CONEXIÓN DE EVENTOS (¡LA PARTE CLAVE!) ===
        // Esto conecta el clic en el árbol con el panel de detalles automáticamente
        panelArchivos.agregarListenerSeleccion(e -> {
            Object obj = panelArchivos.getObjetoSeleccionado();
            
            if (obj instanceof Archivo) {
                // Si es archivo, mostramos su ficha técnica
                panelOutput.mostrarFichaTecnica((Archivo) obj);
            } else {
                panelOutput.mostrarMensajeVacio();
            }
        });
        
        // === SPLIT PRINCIPAL ===
        // (Ahora divide el panelIzquierdo y el panelDerecho)
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
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
        manejador.setModoAdministrador(esAdmin); // Usar método correcto del manejador
        
        // Mostrar/ocultar panel de control
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
    
    // Clase interna para el estado del sistema (JSON)
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
            // Obtenemos el nombre de la política
            this.planificadorActual = manejador.getPlanificadorActual().getNombrePolitica();
            // Obtenemos el nombre del usuario (String)
            this.usuarioActual = manejador.getUsuarioActual(); 
            this.esModoAdministrador = manejador.esAdministrador();
        }
    }
}