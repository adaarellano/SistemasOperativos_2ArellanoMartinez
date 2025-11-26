/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;

public class PanelControl extends JPanel {
    private ManejadorArchivo manejador;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
    
    // Botones CRUD
    private JButton btnCrear, btnCrearDir, btnEditar, btnEliminar, btnActualizar;
    
    // Botón Especial para Modo Proceso
    private JButton btnProcesarCola;
    
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, PanelConsola panelConsola, PanelOutput panelOutput, PanelDisco panelDisco, PanelTablaAsignacion panelTablaAsignacion) {
        this.manejador = manejador;
        this.panelArchivos = panelArchivos;
        this.panelConsola = panelConsola;
        this.panelDisco = panelDisco;
        this.panelTablaAsignacion = panelTablaAsignacion;
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        // Diseño: 3 filas, 2 columnas
        setLayout(new GridLayout(3, 2, 5, 5)); 
        setBorder(BorderFactory.createTitledBorder("Gestión de Sistema (Admin)"));
        
        // --- CRUD ---
        btnCrear = new JButton("Crear Archivo");
        btnCrearDir = new JButton("Crear Directorio");
        btnEditar = new JButton("Editar / Renombrar");
        btnEliminar = new JButton("Eliminar");
        
        // --- PROCESAR COLA (Inicialmente oculto) ---
        btnProcesarCola = new JButton("▶ PROCESAR COLA");
        btnProcesarCola.setBackground(new Color(144, 238, 144)); // Verde claro
        btnProcesarCola.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnProcesarCola.setVisible(false);
        
        // Botón de actualizar vista (manual)
        btnActualizar = new JButton("Actualizar Vista");
        
        // Añadir en orden
        add(btnCrear);
        add(btnCrearDir);
        add(btnEditar);
        add(btnEliminar);
        add(btnActualizar);
        add(btnProcesarCola); // Ocupa el último espacio
        
        configurarEventos();
        
        // Timer para actualizar el contador del botón cada 500ms
        new Timer(500, e -> {
            if (btnProcesarCola.isVisible() && manejador.getPlanificadorActual() != null) {
                int pendientes = manejador.getPlanificadorActual().getSolicitudesPendientes().getSize();
                btnProcesarCola.setText("▶ PROCESAR COLA (" + pendientes + ")");
                
                if (pendientes > 0) btnProcesarCola.setBackground(new Color(255, 100, 100)); // Rojo claro
                else btnProcesarCola.setBackground(new Color(144, 238, 144)); // Verde claro
            }
        }).start();
    }
    
    private void configurarEventos() {
        btnCrear.addActionListener(e -> crearArchivo());
        btnCrearDir.addActionListener(e -> crearDirectorio());
        btnEditar.addActionListener(e -> editarArchivo());
        btnEliminar.addActionListener(e -> eliminarArchivo());
        
        btnActualizar.addActionListener(e -> {
            panelArchivos.actualizarArbol();
            if (panelDisco != null) panelDisco.actualizarDisco();
            if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
        });
        
        // Evento para liberar la cola
        btnProcesarCola.addActionListener(e -> {
            manejador.procesarColaPendiente();
        });
    }
    
    public void setModoBatch(boolean activo) {
        btnProcesarCola.setVisible(activo);
        if (activo) {
            setBorder(BorderFactory.createTitledBorder("Modo ADMIN PROCESO (Cola en Espera)"));
        } else {
            setBorder(BorderFactory.createTitledBorder("Gestión de Sistema (Admin)"));
        }
    }
    
    // --- MÉTODOS CRUD ---
    
    private void crearArchivo() {
        String rutaBase = panelArchivos.obtenerRutaSeleccionada();
        String nombre = JOptionPane.showInputDialog(this, "Crear archivo en '" + rutaBase + "':\nNombre:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques:");
            try {
                int tamano = Integer.parseInt(tamanoStr);
                String rutaCompleta = rutaBase.equals("/") ? "/" + nombre : rutaBase + "/" + nombre;
                manejador.solicitarOperacion("CREAR", rutaCompleta, "admin", tamano);
                panelConsola.agregarLinea("Solicitud 'CREAR' enviada para: " + rutaCompleta);
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Tamaño inválido."); }
        }
    }
    
    private void crearDirectorio() {
        String rutaBase = panelArchivos.obtenerRutaSeleccionada();
        String nombre = JOptionPane.showInputDialog(this, "Crear directorio en '" + rutaBase + "':\nNombre:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String rutaCompleta = rutaBase.equals("/") ? "/" + nombre : rutaBase + "/" + nombre;
            manejador.solicitarOperacion("CREAR_DIR", rutaCompleta, "admin", 0);
            
            panelConsola.agregarLinea("Solicitud 'CREAR_DIR' enviada para: " + rutaCompleta);
        }
    }
    
    private void editarArchivo() {
        String nombreSeleccionado = panelArchivos.getArchivoSeleccionado();
        if (nombreSeleccionado == null) { JOptionPane.showMessageDialog(this, "Seleccione algo primero."); return; }
        String rutaCompleta = panelArchivos.obtenerRutaSeleccionada();
        
        // CASO A: Es un DIRECTORIO -> Renombrar
        if (panelArchivos.esDirectorioSeleccionado()) {
            String nuevoNombre = JOptionPane.showInputDialog(this, "Renombrar '" + nombreSeleccionado + "':", nombreSeleccionado);
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                manejador.renombrarDirectorio(rutaCompleta, nuevoNombre, "admin");
                panelConsola.agregarLinea("Solicitud 'RENAME' enviada para: " + nombreSeleccionado);
            }
        } 
        // CASO B: Es un ARCHIVO -> Editar Contenido
        else {
            String rutaArchivo = rutaCompleta.equals("/") ? "/" + nombreSeleccionado : rutaCompleta + "/" + nombreSeleccionado;
            String contenidoActual = manejador.leerArchivo(rutaArchivo, "admin");
            if (contenidoActual == null) contenidoActual = "";
            
            JTextArea textArea = new JTextArea(15, 50); 
            textArea.setText(contenidoActual);
            int result = JOptionPane.showConfirmDialog(this, new JScrollPane(textArea), "Editando: " + nombreSeleccionado, JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String nuevoContenido = textArea.getText();
                if (!nuevoContenido.equals(contenidoActual)) {
                    manejador.actualizarArchivo(rutaArchivo, nuevoContenido, "admin");
                    panelConsola.agregarLinea("Solicitud 'UPDATE' enviada para: " + nombreSeleccionado);
                }
            }
        }
    }
    
    private void eliminarArchivo() {
        String nombreSeleccionado = panelArchivos.getArchivoSeleccionado();
        if (nombreSeleccionado == null) { JOptionPane.showMessageDialog(this, "Seleccione algo primero."); return; }
        
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar '" + nombreSeleccionado + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
             String rutaBase = panelArchivos.obtenerRutaSeleccionada();
             String rutaCompleta;
             if (panelArchivos.esDirectorioSeleccionado()) {
                 rutaCompleta = rutaBase;
             } else {
                 rutaCompleta = rutaBase.equals("/") ? "/" + nombreSeleccionado : rutaBase + "/" + nombreSeleccionado;
             }
             
             manejador.solicitarOperacion("ELIMINAR", rutaCompleta, "admin", 0);
             panelConsola.agregarLinea("Solicitud 'ELIMINAR' enviada para: " + nombreSeleccionado);
        }
    }
}