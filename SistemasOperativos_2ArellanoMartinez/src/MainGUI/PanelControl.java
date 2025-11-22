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
    
    // Declaramos todos los botones, incluyendo el nuevo para Directorios
    private JButton btnCrear, btnCrearDir, btnEditar, btnEliminar, btnActualizar;
    
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
        setLayout(new GridLayout(3, 2, 5, 5)); 
        
        setBorder(BorderFactory.createTitledBorder("Operaciones CRUD (Solo Administrador)"));
        
        btnCrear = new JButton("Crear Archivo");
        btnCrearDir = new JButton("Crear Directorio"); // <-- NUEVO BOTÓN
        btnEditar = new JButton("Editar Archivo");
        btnEliminar = new JButton("Eliminar");
        btnActualizar = new JButton("Actualizar Vista");
        
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
    }
    
    private void crearArchivo() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo (ej: notas.txt):");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques:");
            try {
                int tamano = Integer.parseInt(tamanoStr);
                
                // Crear ruta (por ahora en la raíz)
                String ruta = "/" + nombre;
                
                // Solicitamos el PROCESO al manejador
                manejador.solicitarOperacion("CREAR", ruta, "admin", tamano);

                panelConsola.agregarLinea("Solicitud de PROCESO 'CREAR' enviada para: " + nombre);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño inválido. Ingrese un número entero.");
            }
        }
    }
    
    // NUEVO MÉTODO: Crear Directorio
    private void crearDirectorio() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del Directorio:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String ruta = "/" + nombre;
            
            // Solicitamos el proceso. Tamaño 0 porque los directorios 
            // en esta simulación son lógicos (no ocupan bloques físicos del SD).
            manejador.solicitarOperacion("CREAR_DIR", ruta, "admin", 0);
            
            panelConsola.agregarLinea("Solicitud 'CREAR_DIR' enviada para: " + nombre);
        }
    }
    
    private void editarArchivo() {
        String archivoSeleccionado = panelArchivos.getArchivoSeleccionado();
        if (archivoSeleccionado != null && archivoSeleccionado.contains("(")) {
            // Extraer nombre del archivo (parte antes del paréntesis)
            String nombreArchivo = archivoSeleccionado.split("\\(")[0].trim();
            
            String nuevoContenido = JOptionPane.showInputDialog(this, 
                "Nuevo contenido para " + nombreArchivo + ":", 
                "Editar Archivo", 
                JOptionPane.PLAIN_MESSAGE);
            
            if (nuevoContenido != null) {
                String ruta = "/" + nombreArchivo;
                
                // Solución temporal: Llamada directa para editar contenido
                // Idealmente esto también sería un proceso "WRITE"
                boolean exito = manejador.actualizarArchivo(ruta, nuevoContenido, "admin");

                if (exito) {
                    panelConsola.agregarLinea("Archivo editado: " + nombreArchivo);
                } else {
                    panelConsola.agregarLinea("ERROR: No se pudo editar el archivo");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo del árbol primero.");
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
}