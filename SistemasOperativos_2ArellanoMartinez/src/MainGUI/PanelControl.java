/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import Models.Archivo;
import javax.swing.*;
import java.awt.*;

public class PanelControl extends JPanel {
    private ManejadorArchivo manejador;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
    
    // Botones CRUD Estándar
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
        btnEditar = new JButton("Editar Archivo");
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
        
        // --- TIMER PARA ACTUALIZAR EL BOTÓN ---
        new Timer(500, e -> {
            if (btnProcesarCola.isVisible() && manejador.getPlanificadorActual() != null) {
                int pendientes = manejador.getPlanificadorActual().getSolicitudesPendientes().getSize();
                btnProcesarCola.setText("▶ PROCESAR COLA (" + pendientes + ")");
                
                if (pendientes > 0) {
                    btnProcesarCola.setBackground(new Color(255, 150, 150)); // Rojo si hay carga
                } else {
                    btnProcesarCola.setBackground(new Color(144, 238, 144)); // Verde si está libre
                }
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
            panelConsola.agregarLinea("▶ LIBERANDO EL DISCO... Procesando cola acumulada.");
            manejador.setDiscoPausado(false); // Despausar el disco momentáneamente
            
            // Opcional: Si quieres que se vuelva a pausar después, necesitarías lógica extra.
            // Por ahora, asumimos que al darle Play, procesa todo lo pendiente.
        });
    }
    
    /**
     * Configura el panel según si estamos en Modo Admin Normal o Modo Proceso (Cola)
     */
    public void setModoBatch(boolean activo) {
        btnProcesarCola.setVisible(activo);
        
        if (activo) {
            setBorder(BorderFactory.createTitledBorder("Modo ADMIN PROCESO (Cola en Espera)"));
            btnProcesarCola.setText("▶ PROCESAR COLA (" + manejador.getPlanificadorActual().getSolicitudesPendientes().getSize() + ")");
        } else {
            setBorder(BorderFactory.createTitledBorder("Gestión de Sistema (Admin)"));
        }
    }
    
    // --- MÉTODOS CRUD (Sin cambios) ---
    
    private void crearArchivo() {
        String rutaBase = panelArchivos.obtenerRutaSeleccionada();
        String nombre = JOptionPane.showInputDialog(this, "Crear archivo en '" + rutaBase + "':\nNombre:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques:");
            try {
                int tamano = Integer.parseInt(tamanoStr);
                String rutaCompleta = rutaBase.equals("/") ? "/" + nombre : rutaBase + "/" + nombre;
                manejador.solicitarOperacion("CREAR", rutaCompleta, "admin", tamano);
                panelConsola.agregarLinea("Solicitud 'CREAR' para: " + rutaCompleta);
                if (btnProcesarCola.isVisible()) actualizarTextoBotonCola();
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Tamaño inválido."); }
        }
    }
    
    private void crearDirectorio() {
        String rutaBase = panelArchivos.obtenerRutaSeleccionada();
        String nombre = JOptionPane.showInputDialog(this, "Crear directorio en '" + rutaBase + "':\nNombre:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String rutaCompleta = rutaBase.equals("/") ? "/" + nombre : rutaBase + "/" + nombre;
            manejador.solicitarOperacion("CREAR_DIR", rutaCompleta, "admin", 0);
            panelConsola.agregarLinea("Solicitud 'CREAR_DIR' para: " + rutaCompleta);
            if (btnProcesarCola.isVisible()) actualizarTextoBotonCola();
        }
    }
    
    private void editarArchivo() {
        // 1. Obtener el OBJETO real seleccionado (no solo el texto)
        Object objSeleccionado = panelArchivos.getObjetoSeleccionado();
        
        if (objSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o directorio del árbol primero.");
            return;
        }
        
        // CASO A: Es un DIRECTORIO -> Renombrar
        if (objSeleccionado instanceof Models.Directorio) {
            Models.Directorio dir = (Models.Directorio) objSeleccionado;
            String nombreActual = dir.getNombre();
            String rutaCompleta = dir.getRutaCompleta();
            
            String nuevoNombre = JOptionPane.showInputDialog(this, 
                "Renombrar directorio '" + nombreActual + "':", 
                nombreActual);
                
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                manejador.renombrarDirectorio(rutaCompleta, nuevoNombre, "admin");
            }
        } 
        // CASO B: Es un ARCHIVO -> Editar Contenido
        else if (objSeleccionado instanceof Models.Archivo) {
            Models.Archivo archivo = (Models.Archivo) objSeleccionado;
            
            // --- CORRECCIÓN CLAVE ---
            // Usamos la ruta exacta que el archivo ya tiene guardada.
            // No la reconstruimos manualmente para evitar errores de barras "/".
            String rutaArchivo = archivo.getRutaCompleta(); 
            String nombreArchivo = archivo.getNombre();
            
            // 1. Leer contenido
            String contenidoActual = manejador.leerArchivo(rutaArchivo, "admin");
            
            // Si devuelve null, es que hubo un error de lectura real, no de ruta
            if (contenidoActual == null) {
                panelConsola.agregarLinea("❌ Error leyendo archivo: " + rutaArchivo);
                return;
            }
            
            // 2. Mostrar Editor
            JTextArea textArea = new JTextArea(15, 50); 
            textArea.setText(contenidoActual);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            
            int result = JOptionPane.showConfirmDialog(this, new JScrollPane(textArea), 
                    "Editando: " + nombreArchivo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            // 3. Guardar Cambios
            if (result == JOptionPane.OK_OPTION) {
                String nuevoContenido = textArea.getText();
                if (!nuevoContenido.equals(contenidoActual)) {
                    boolean exito = manejador.actualizarArchivo(rutaArchivo, nuevoContenido, "admin");
                    if (exito) {
                        panelConsola.agregarLinea("Archivo editado: " + nombreArchivo);
                        
                        // Refrescar ficha técnica si está visible
                        panelArchivos.actualizarArbol(); // Para actualizar tamaño en el árbol
                        Object nuevoObj = panelArchivos.getObjetoSeleccionado();
                        // (Opcional: forzar actualización del panel de detalles)
                    } else {
                        panelConsola.agregarLinea("ERROR: No se pudo guardar cambios en " + rutaArchivo);
                    }
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
             panelConsola.agregarLinea("Solicitud 'ELIMINAR' para: " + nombreSeleccionado);
             if (btnProcesarCola.isVisible()) actualizarTextoBotonCola();
        }
    }
    
    private void actualizarTextoBotonCola() {
        // Pequeño delay para dar tiempo a que la solicitud entre en la cola
        SwingUtilities.invokeLater(() -> {
            try { Thread.sleep(100); } catch (Exception e) {}
            int pendientes = manejador.getPlanificadorActual().getSolicitudesPendientes().getSize();
            btnProcesarCola.setText("▶ PROCESAR COLA (" + pendientes + ")");
        });
    }
    
    /**
     * Método público llamado por el Manejador para actualizar el contador del botón
     */
    public void actualizarContadorCola() {
        SwingUtilities.invokeLater(() -> {
            if (btnProcesarCola.isVisible() && manejador.getPlanificadorActual() != null) {
                int pendientes = manejador.getPlanificadorActual().getSolicitudesPendientes().getSize();
                btnProcesarCola.setText("▶ PROCESAR COLA (" + pendientes + ")");
            }
        });
    }
}