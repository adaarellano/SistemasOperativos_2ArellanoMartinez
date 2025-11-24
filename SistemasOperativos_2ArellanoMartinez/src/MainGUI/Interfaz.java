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
import Managers.*;
import Models.*;

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
    private JButton btnModoAdmin, btnModoUsuario, btnModoProceso;
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
        
        btnModoAdmin = new JButton("Admin Directo");
        btnModoProceso = new JButton("Admin Proceso"); // NUEVO
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
        panelSuperior.add(btnModoProceso); // Añadir
        
        // Configurar eventos de guardar/cargar
        btnGuardar.addActionListener(e -> guardarEstado());
        btnCargar.addActionListener(e -> cargarEstado());
        
        return panelSuperior;
    }
    
private JSplitPane crearPanelCentral() {
        // === PANEL IZQUIERDO (Archivos + Disco + Botones) ===
        JPanel panelIzquierdo = new JPanel(new BorderLayout());

        // 1. Panel de Archivos (JTree)
        panelArchivos = new PanelArchivos(manejador);
        manejador.setPanelArchivos(panelArchivos);

        // 2. Panel de Disco (Cuadrícula)
        panelDisco = new PanelDisco(manejador); 
        manejador.setPanelDisco(panelDisco);

        // Usamos un JSplitPane vertical para dividir Árbol (arriba) y Disco (abajo)
        JSplitPane splitIzquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                 panelArchivos, panelDisco);
        splitIzquierdo.setDividerLocation(400); // Altura del árbol
        panelIzquierdo.add(splitIzquierdo, BorderLayout.CENTER);

        // === PANEL DERECHO (Con Pestañas) ===
        JTabbedPane panelDerecho = new JTabbedPane();

        // Pestaña 1: Consola
        panelConsola = new PanelConsola();
        manejador.setPanelConsola(panelConsola);
        panelDerecho.addTab("Consola", panelConsola);

        // Pestaña 2: Gestor de Procesos
        panelProcesos = new PanelProcesos(manejador);
        panelDerecho.addTab("Gestor de Procesos", panelProcesos);

        // Pestaña 3: Tabla de Asignación (FAT)
        panelTablaAsignacion = new PanelTablaAsignacion(manejador);
        manejador.setPanelTablaAsignacion(panelTablaAsignacion);
        panelDerecho.addTab("Tabla de Asignación", panelTablaAsignacion);

        // Pestaña 4: Detalles Técnicos (Bytes)
        PanelOutput panelOutput = new PanelOutput();
        manejador.setPanelOutput(panelOutput);
        panelDerecho.addTab("Detalles (Bytes)", panelOutput);
        
        // Pestaña 5: Estadísticas (Gráfica JFreeChart)
        PanelEstadisticas panelEstadisticas = new PanelEstadisticas(manejador);
        manejador.setPanelEstadisticas(panelEstadisticas);
        panelDerecho.addTab("Estadísticas", panelEstadisticas);

        // === PANEL DE CONTROL (Botones) ===
        // Se instancia con todas las dependencias para poder actualizar todo
        panelControl = new PanelControl(manejador, panelArchivos, panelConsola, panelOutput, panelDisco, panelTablaAsignacion);
        panelControl.setVisible(false); // Inicia oculto (Modo Usuario)
        
        // Agregamos el panel de control en la parte SUPERIOR del lado izquierdo
        panelIzquierdo.add(panelControl, BorderLayout.NORTH);
        
        // === CONEXIÓN DE EVENTOS (IMPORTANTE) ===
        // Esto conecta el clic en el árbol con el panel de detalles automáticamente
        panelArchivos.agregarListenerSeleccion(e -> {
            Object obj = panelArchivos.getObjetoSeleccionado();
            
            if (obj instanceof Models.Archivo) {
                // Si es archivo, mostramos su ficha técnica en la pestaña de detalles
                panelOutput.mostrarFichaTecnica((Models.Archivo) obj);
            } else {
                panelOutput.mostrarMensajeVacio();
            }
        });
        
        // === SPLIT PRINCIPAL ===
        // Divide la pantalla en Izquierda (Gestión) y Derecha (Info)
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPrincipal.setDividerLocation(500); // Ancho del panel izquierdo
        
        return splitPrincipal;
    }


    private void configurarEventos() {
        btnModoAdmin.addActionListener(e -> cambiarModo(1));   // 1 = Admin Directo
        btnModoProceso.addActionListener(e -> cambiarModo(2)); // 2 = Admin Proceso (Batch)
        btnModoUsuario.addActionListener(e -> cambiarModo(0)); // 0 = Usuario
        comboPoliticas.addActionListener(e -> {
            if (esModoAdministrador) {
                String politica = (String) comboPoliticas.getSelectedItem();
                manejador.cambiarPlanificador(politica);
                panelDetalles.actualizarDetalles();
            }
        });
    }
    
   
    private void cambiarModo(int tipoModo) {
    boolean esAdmin = (tipoModo == 1 || tipoModo == 2);
    boolean esBatch = (tipoModo == 2);
    
    this.esModoAdministrador = esAdmin;
    
    // 1. Configurar Manejador
    manejador.setModoAdministrador(esAdmin);
    manejador.setModoBatch(esBatch); // Activar/Desactivar cola de espera
    
    // 2. Configurar GUI
    panelControl.setVisible(esAdmin);
    panelControl.setModoProceso(esBatch); // Activar botón "Procesar"
    
    comboPoliticas.setEnabled(esAdmin);
    panelArchivos.actualizarVista(esAdmin);
    
    // 3. Mensajes
    String titulo = "";
    switch(tipoModo) {
        case 0: titulo = "USUARIO (Solo Lectura)"; break;
        case 1: titulo = "ADMINISTRADOR (Ejecución Inmediata)"; break;
        case 2: titulo = "ADMINISTRADOR PROCESO (Cola de Espera)"; break;
    }
    
    panelConsola.agregarLinea("=== MODO " + titulo + " ACTIVADO ===");
    if (esBatch) {
        panelConsola.agregarLinea("ℹ Las operaciones se guardarán en cola.");
        panelConsola.agregarLinea("ℹ Presione 'PROCESAR COLA' para ejecutar algoritmos.");
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