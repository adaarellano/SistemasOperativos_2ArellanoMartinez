/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;
import Managers.*;
import Models.*;

public class PanelControl extends JPanel {
    private ManejadorArchivo manejador;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
    
    // Declaramos todos los botones, incluyendo el nuevo para Directorios
    private JButton btnCrear, btnCrearDir, btnEditar, btnEliminar, btnActualizar, btnProcesar;
    
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, PanelConsola panelConsola, PanelOutput panelOutput, PanelDisco panelDisco, PanelTablaAsignacion panelTablaAsignacion) {
        this.manejador = manejador;
        this.panelArchivos = panelArchivos;
        this.panelConsola = panelConsola;
        this.panelDisco = panelDisco;
        this.panelTablaAsignacion = panelTablaAsignacion;
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        // CAMBIO: Usamos GridLayout(3, 2) para que quepan los 5 botones
        // 3 filas, 2 columnas, con espacio de 5px entre ellos.
        setLayout(new GridLayout(4, 2, 5, 5));        
        setBorder(BorderFactory.createTitledBorder("Operaciones CRUD (Solo Administrador)"));
        
        btnCrear = new JButton("Crear Archivo");
        btnCrearDir = new JButton("Crear Directorio"); // <-- NUEVO BOTÓN
        btnEditar = new JButton("Editar Archivo");
        btnEliminar = new JButton("Eliminar");
        btnActualizar = new JButton("Actualizar Vista");
        
        //modo 2 admi
        btnProcesar = new JButton("▶ PROCESAR COLA");
        btnProcesar.setBackground(new Color(200, 255, 200)); // Color verde suave
        btnProcesar.setEnabled(false); // Desactivado por defecto
        
        // Agregar tooltips para ayuda visual
        btnCrear.setToolTipText("Crear un nuevo archivo y asignar bloques");
        btnCrearDir.setToolTipText("Crear una nueva carpeta (directorio)");
        btnEditar.setToolTipText("Modificar contenido de un archivo existente");
        btnEliminar.setToolTipText("Eliminar archivo o directorio seleccionado");
        btnActualizar.setToolTipText("Refrescar visualmente el árbol");
        
        // Añadir botones al panel (El orden importa en GridLayout)
        add(btnCrear);
        add(btnCrearDir); // <-- AÑADIR AL PANEL
        add(btnEditar);
        add(btnEliminar);
        add(btnActualizar);
        add(btnActualizar);
        add(btnProcesar);
        configurarEventos();
    }
    
    private void configurarEventos() {
        btnCrear.addActionListener(e -> crearArchivo());
        btnCrearDir.addActionListener(e -> crearDirectorio()); // <-- NUEVO EVENTO
        btnEditar.addActionListener(e -> editarArchivo());
        btnEliminar.addActionListener(e -> eliminarArchivo());
        
        // El botón actualizar es manual, aunque el sistema ya se actualiza solo
        btnActualizar.addActionListener(e -> {
            panelArchivos.actualizarArbol();
            if (panelDisco != null) panelDisco.actualizarDisco();
            if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
        });
        /// Modo 2 admi 
        btnProcesar.addActionListener(e -> {
        manejador.procesarLotePendiente();
        });
    }
    
private void crearArchivo() {
        // 1. Obtener la ruta seleccionada del árbol
        String rutaBase = panelArchivos.obtenerRutaSeleccionada();
        
        // 2. Mostrar la ruta en el mensaje para que sepas dónde estás creando
        String nombre = JOptionPane.showInputDialog(this, 
            "Crear archivo en '" + rutaBase + "':\nNombre del archivo:");
            
        if (nombre != null && !nombre.trim().isEmpty()) {
            String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques:");
            try {
                int tamano = Integer.parseInt(tamanoStr);
                
                // 3. Construir la ruta correcta
                // Si la base es "/", la ruta es "/nombre"
                // Si la base es "/arellano", la ruta es "/arellano/nombre"
                String rutaCompleta = rutaBase.equals("/") ? "/" + nombre : rutaBase + "/" + nombre;
                
                // 4. Enviar la solicitud con la ruta completa
                manejador.solicitarOperacion("CREAR", rutaCompleta, "admin", tamano);

                panelConsola.agregarLinea("Solicitud de PROCESO 'CREAR' enviada para: " + nombre);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño inválido. Ingrese un número entero.");
            }
        }
    }
    
private void crearDirectorio() {
        String rutaBase = panelArchivos.obtenerRutaSeleccionada();
        
        String nombre = JOptionPane.showInputDialog(this, 
            "Crear directorio en '" + rutaBase + "':\nNombre:");
            
        if (nombre != null && !nombre.trim().isEmpty()) {
            String rutaCompleta = rutaBase.equals("/") ? "/" + nombre : rutaBase + "/" + nombre;
            
            manejador.solicitarOperacion("CREAR_DIR", rutaCompleta, "admin", 0);
            
            panelConsola.agregarLinea("Solicitud 'CREAR_DIR' enviada para: " + nombre);
        }
    }
    
    private void editarArchivo() {
        // 1. Obtener nombre limpio
        String nombreSeleccionado = panelArchivos.getArchivoSeleccionado();
        
        if (nombreSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o directorio del árbol primero.");
            return;
        }
        
        // 2. Obtener la ruta completa
        String rutaCompleta = panelArchivos.obtenerRutaSeleccionada();
        
        // CASO A: Es un DIRECTORIO -> Renombrar
        if (panelArchivos.esDirectorioSeleccionado()) {
            String nuevoNombre = JOptionPane.showInputDialog(this, 
                "Renombrar directorio '" + nombreSeleccionado + "':", 
                nombreSeleccionado);
                
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                manejador.renombrarDirectorio(rutaCompleta, nuevoNombre, "admin");
            }
        } 
        // CASO B: Es un ARCHIVO -> Editar Contenido (ESTILO NOTEPAD)
        else {
            // Reconstruimos la ruta del archivo
            String rutaArchivo;
            if (rutaCompleta.equals("/")) {
                rutaArchivo = "/" + nombreSeleccionado;
            } else {
                rutaArchivo = rutaCompleta + "/" + nombreSeleccionado;
            }
            
            // 1. Leer contenido actual
            String contenidoActual = manejador.leerArchivo(rutaArchivo, "admin");
            if (contenidoActual == null) contenidoActual = ""; 
            
            // Creamos un Área de Texto de 15 filas x 50 columnas
            JTextArea textArea = new JTextArea(15, 50); 
            textArea.setText(contenidoActual);
            textArea.setLineWrap(true);       // Que el texto baje automáticamente
            textArea.setWrapStyleWord(true);  // Que no corte palabras a la mitad
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            
            int result = JOptionPane.showConfirmDialog(this, 
                scrollPane, 
                "Editando: " + nombreSeleccionado, 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
            
            // 2. Si le dio OK, enviamos la solicitud
            if (result == JOptionPane.OK_OPTION) {
                String nuevoContenido = textArea.getText();
                
                if (!nuevoContenido.equals(contenidoActual)) {
                    // --- CORRECCIÓN AQUÍ ---
                    // Ya no esperamos un boolean, solo enviamos la orden.
                    manejador.solicitarOperacionEditar(rutaArchivo, nuevoContenido, "admin");

                    // Mensaje genérico de confirmación de envío
                    panelConsola.agregarLinea("Solicitud de edición enviada para: " + nombreSeleccionado);
                }
            }
        }
    }
    
    private void eliminarArchivo() {
        String archivoSeleccionado = panelArchivos.getArchivoSeleccionado();
        
        if (archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o directorio primero.");
            return;
        }

        // Limpiar el nombre (quitar la info de tamaño que pone el JTree)
        String nombreLimpio;
        if (archivoSeleccionado.contains("(")) {
            nombreLimpio = archivoSeleccionado.split("\\(")[0].trim();
        } else {
            nombreLimpio = archivoSeleccionado.trim();
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar '" + nombreLimpio + "'?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
             String ruta = "/" + nombreLimpio;

             // Solicitamos el PROCESO de eliminación
             manejador.solicitarOperacion("ELIMINAR", ruta, "admin", 0);

             panelConsola.agregarLinea("Solicitud de PROCESO 'ELIMINAR' enviada para: " + nombreLimpio);
        }
    }
    
     //Modo admi 2
    public void setModoProceso(boolean activar) {
    btnProcesar.setEnabled(activar);
    if (activar) {
        setBorder(BorderFactory.createTitledBorder("Operaciones Batch (Modo Proceso)"));
        btnProcesar.setBackground(Color.GREEN);
    } else {
        setBorder(BorderFactory.createTitledBorder("Operaciones CRUD (Admin Directo)"));
        btnProcesar.setBackground(new Color(200, 255, 200));
    }
}
}