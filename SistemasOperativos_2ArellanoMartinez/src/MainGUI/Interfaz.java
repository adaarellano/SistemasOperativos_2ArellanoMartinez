/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import Models.Archivo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class Interfaz extends JFrame {
    private ManejadorArchivo manejador;
    private boolean esModoAdministrador = false;
    
    // Componentes
    private JButton btnModoAdmin, btnModoUsuario, btnModoProceso;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    
    // CardLayout para los paneles de control
    private JPanel panelBotonesSuperiores; 
    private CardLayout cardLayout;         
    private PanelControl panelControl;     // Admin y Admin Proceso
    private PanelUsuario panelUsuario;     // Usuario
    
    private PanelDetalles panelDetalles; 
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
    private JComboBox<String> comboPoliticas;
    private PanelProcesos panelProcesos;
    
    private Gson gson;
    private final String ARCHIVO_CONFIG = "sistema_archivos.json";
    
    public Interfaz() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.manejador = new ManejadorArchivo();
        inicializarGUI();
        cargarEstado(); 
    }
    
    private void inicializarGUI() {
        setTitle("Sistema de Archivos - Simulador Avanzado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel panelSuperior = crearPanelSuperior();
        JSplitPane splitPrincipal = crearPanelCentral();
        
        panelDetalles = new PanelDetalles(manejador);
        manejador.setPanelDetalles(panelDetalles);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(splitPrincipal, BorderLayout.CENTER);
        add(panelDetalles, BorderLayout.SOUTH);
        
        configurarEventos();
        
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Arrancar en MODO USUARIO (Seguro)
        cambiarModo("USER");
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new FlowLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Control del Sistema"));
        
        btnModoAdmin = new JButton("Modo Administrador");
        btnModoUsuario = new JButton("Modo Usuario");
        btnModoProceso = new JButton("Modo Admin Proceso"); 
        btnModoProceso.setBackground(new Color(255, 230, 150)); // Amarillo
        
        comboPoliticas = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        comboPoliticas.setEnabled(false);
        
        JButton btnGuardar = new JButton("Guardar Estado");
        JButton btnCargar = new JButton("Cargar Estado");
        
        panelSuperior.add(btnModoAdmin);
        panelSuperior.add(btnModoUsuario);
        panelSuperior.add(btnModoProceso);
        panelSuperior.add(new JLabel("Política:"));
        panelSuperior.add(comboPoliticas);
        panelSuperior.add(btnGuardar);
        panelSuperior.add(btnCargar);
        
        btnGuardar.addActionListener(e -> guardarEstado());
        btnCargar.addActionListener(e -> cargarEstado());
        
        return panelSuperior;
    }
    
    private JSplitPane crearPanelCentral() {
        JPanel panelIzquierdo = new JPanel(new BorderLayout());

        panelArchivos = new PanelArchivos(manejador);
        manejador.setPanelArchivos(panelArchivos);

        panelDisco = new PanelDisco(manejador); 
        manejador.setPanelDisco(panelDisco);

        JSplitPane splitIzquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelArchivos, panelDisco);
        splitIzquierdo.setDividerLocation(400);
        panelIzquierdo.add(splitIzquierdo, BorderLayout.CENTER);

        JTabbedPane panelDerecho = new JTabbedPane();

        panelConsola = new PanelConsola();
        manejador.setPanelConsola(panelConsola);
        panelDerecho.addTab("Consola", panelConsola);

        panelProcesos = new PanelProcesos(manejador);
        panelDerecho.addTab("Gestor de Procesos", panelProcesos);

        panelTablaAsignacion = new PanelTablaAsignacion(manejador);
        manejador.setPanelTablaAsignacion(panelTablaAsignacion);
        panelDerecho.addTab("Tabla de Asignación", panelTablaAsignacion);

        PanelOutput panelOutput = new PanelOutput();
        manejador.setPanelOutput(panelOutput);
        panelDerecho.addTab("Detalles (Bytes)", panelOutput);
        
        PanelEstadisticas panelEstadisticas = new PanelEstadisticas(manejador);
        manejador.setPanelEstadisticas(panelEstadisticas);
        panelDerecho.addTab("Estadísticas", panelEstadisticas); 

        // === PANELES CAMBIANTES ===
        panelControl = new PanelControl(manejador, panelArchivos, panelConsola, panelOutput, panelDisco, panelTablaAsignacion);
        panelUsuario = new PanelUsuario(manejador, panelConsola);
        
        cardLayout = new CardLayout();
        panelBotonesSuperiores = new JPanel(cardLayout);
        
        panelBotonesSuperiores.add(panelUsuario, "USER");   
        panelBotonesSuperiores.add(panelControl, "ADMIN");  
        
        panelIzquierdo.add(panelBotonesSuperiores, BorderLayout.NORTH);
        
        panelArchivos.agregarListenerSeleccion(e -> {
            Object obj = panelArchivos.getObjetoSeleccionado();
            if (obj instanceof Archivo) {
                panelOutput.mostrarFichaTecnica((Archivo) obj);
            } else {
                panelOutput.mostrarMensajeVacio();
            }
        });
        
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPrincipal.setDividerLocation(500);
        
        return splitPrincipal;
    }
    
    private void configurarEventos() {
        btnModoAdmin.addActionListener(e -> cambiarModo("ADMIN"));
        btnModoUsuario.addActionListener(e -> cambiarModo("USER"));
        btnModoProceso.addActionListener(e -> cambiarModo("PROCESO"));
        
        comboPoliticas.addActionListener(e -> {
            if (esModoAdministrador) {
                String politica = (String) comboPoliticas.getSelectedItem();
                manejador.cambiarPlanificador(politica);
                panelDetalles.actualizarDetalles();
            }
        });
    }
    
    private void cambiarModo(String modo) {
        boolean esAdmin = !modo.equals("USER");
        this.esModoAdministrador = esAdmin;
        manejador.setModoAdministrador(esAdmin);
        
        if (modo.equals("USER")) {
            cardLayout.show(panelBotonesSuperiores, "USER");
            manejador.setDiscoPausado(false); // Usuario siempre en vivo
        } else {
            cardLayout.show(panelBotonesSuperiores, "ADMIN"); // Tanto Admin como Proceso usan este panel
            
            if (modo.equals("PROCESO")) {
                manejador.setDiscoPausado(true); // PAUSA
                panelControl.setModoBatch(true); // Muestra botón PLAY
            } else {
                manejador.setDiscoPausado(false); // VIVO
                panelControl.setModoBatch(false); // Oculta botón PLAY
            }
        }
        
        comboPoliticas.setEnabled(esAdmin);
        panelArchivos.actualizarVista(esAdmin);
        panelConsola.agregarLinea("=== CAMBIO DE MODO: " + modo + " ===");
        panelDetalles.actualizarDetalles();
    }
    
    // ... (Métodos de guardar/cargar y estado sistema igual) ...
    // (Copia el resto de tu Interfaz.java original aquí)
    
    private void guardarEstado() {
        try {
            EstadoSistema estado = new EstadoSistema(manejador);
            String json = gson.toJson(estado);
            try (FileWriter writer = new FileWriter(ARCHIVO_CONFIG)) {
                writer.write(json);
            }
            panelConsola.agregarLinea("✅ Estado guardado en: " + ARCHIVO_CONFIG);
        } catch (IOException e) {
            panelConsola.agregarLinea("❌ Error al guardar: " + e.getMessage());
        }
    }
    
    private void cargarEstado() {
        panelConsola.agregarLinea("🔄 Carga de estado en desarrollo...");
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
        private String usuarioActual; // Aquí esperamos un String
        private boolean esModoAdministrador;
        
        public EstadoSistema(ManejadorArchivo manejador) {
            this.archivosCreados = manejador.getArchivosCreados();
            this.archivosEliminados = manejador.getArchivosEliminados();
            this.operacionesRealizadas = manejador.getOperacionesRealizadas();
            this.bloquesOcupados = manejador.getBloquesOcupados();
            this.planificadorActual = manejador.getPlanificadorActual().getNombrePolitica();
            
            // --- CORRECCIÓN AQUÍ ---
            // Obtenemos el objeto Usuario y le pedimos su nombre (String)
            this.usuarioActual = manejador.getUsuarioActual().getUsername(); 
            // -----------------------
            
            this.esModoAdministrador = manejador.esAdministrador();
        }
    }}