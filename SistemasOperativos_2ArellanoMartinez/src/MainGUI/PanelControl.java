/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import Models.Archivo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PanelControl extends JPanel {
    private ManejadorArchivo manejador;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
<<<<<<< HEAD
    private PanelOutput panelOutput;
=======
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
>>>>>>> day
    
    private JButton btnCrear, btnEditar, btnEliminar, btnActualizar;
    private JFileChooser fileChooser;
    private File directorioBase;
    
<<<<<<< HEAD
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, 
                       PanelConsola panelConsola, PanelOutput panelOutput) {
        this.manejador = manejador;
        this.panelArchivos = panelArchivos;
        this.panelConsola = panelConsola;
        this.panelOutput = panelOutput;
        
        // Establecer directorio base para el sistema de archivos simulado
        establecerDirectorioBase();
=======
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, PanelConsola panelConsola, PanelOutput panelOutput, PanelDisco panelDisco, PanelTablaAsignacion panelTablaAsignacion) {
        this.manejador = manejador;
        this.panelArchivos = panelArchivos;
        this.panelConsola = panelConsola;
        this.panelDisco = panelDisco;
        this.panelTablaAsignacion = panelTablaAsignacion;
>>>>>>> day
        inicializarPanel();
    }
    
    private void establecerDirectorioBase() {
        // Crear un directorio en el escritorio o en la carpeta del proyecto
        String userHome = System.getProperty("user.home");
        directorioBase = new File(userHome + "/FileSystemSimulator");
        
        if (!directorioBase.exists()) {
            if (directorioBase.mkdirs()) {
                panelConsola.agregarLinea("✅ Directorio base creado: " + directorioBase.getAbsolutePath());
            } else {
                panelConsola.agregarLinea("❌ Error al crear directorio base");
            }
        }
        
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(directorioBase);
        fileChooser.setDialogTitle("Sistema de Archivos Simulado");
    }
    
    private void inicializarPanel() {
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createTitledBorder("Operaciones CRUD (Solo Administrador)"));
        
        btnCrear = new JButton("Crear Archivo Real");
        btnEditar = new JButton("Editar Archivo Real");
        btnEliminar = new JButton("Eliminar Archivo Real");
        btnActualizar = new JButton("Actualizar Vista");
        
        // Agregar tooltips para mejor usabilidad
        btnCrear.setToolTipText("Crear un archivo real en el sistema");
        btnEditar.setToolTipText("Editar el contenido de un archivo real");
        btnEliminar.setToolTipText("Eliminar un archivo real del sistema");
        btnActualizar.setToolTipText("Actualizar la vista del árbol de archivos");
        
        add(btnCrear);
        add(btnEditar);
        add(btnEliminar);
        add(btnActualizar);
        
        configurarEventos();
    }
    
    private void configurarEventos() {
<<<<<<< HEAD
        btnCrear.addActionListener(e -> crearArchivoReal());
        btnEditar.addActionListener(e -> editarArchivoReal());
        btnEliminar.addActionListener(e -> eliminarArchivoReal());
        btnActualizar.addActionListener(e -> {
            panelArchivos.actualizarArbol();
            panelConsola.agregarLinea("✅ Vista actualizada");
        });
    }
    
    private void crearArchivoReal() {
        // Opción 1: Usar JFileChooser para seleccionar ubicación
        fileChooser.setDialogTitle("Crear Nuevo Archivo");
        fileChooser.setApproveButtonText("Crear");
        fileChooser.setSelectedFile(new File(directorioBase, "nuevo_archivo.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            crearArchivoEnSistemaReal(archivo);
=======
        btnCrear.addActionListener(e -> crearArchivo());
        btnEditar.addActionListener(e -> editarArchivo());
        btnEliminar.addActionListener(e -> eliminarArchivo());
        btnActualizar.addActionListener(e -> panelArchivos.actualizarArbol());
        
    }
    
    private void crearArchivo() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques:");
            try {
                int tamano = Integer.parseInt(tamanoStr);
                
                // Crear archivo en la raíz por simplicidad
                String ruta = "/" + nombre;
               // CAMBIO: Ya no llamamos a crearArchivo directamente.
                // Ahora solicitamos un PROCESO para que haga el trabajo.
                manejador.solicitarOperacion("CREAR", ruta, "admin", tamano);

                panelConsola.agregarLinea("Solicitud de PROCESO 'CREAR' enviada para: " + nombre);

                panelConsola.agregarLinea("Solicitud de PROCESO 'CREAR' enviada para: " + nombre);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño inválido");
            }
>>>>>>> day
        }
    }
    
    private void crearArchivoEnSistemaReal(File archivo) {
        try {
            String nombreArchivo = archivo.getName();
            
            // Pedir contenido del archivo
            String contenido = JOptionPane.showInputDialog(this, 
                "Contenido del archivo " + nombreArchivo + ":", 
                "Crear Archivo", 
                JOptionPane.PLAIN_MESSAGE);
            
<<<<<<< HEAD
            if (contenido == null) return; // Usuario canceló
            
            // Crear el archivo real
            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(contenido);
=======
            if (nuevoContenido != null) {
                String ruta = "/" + nombreArchivo;
                // CAMBIO: Solicitamos un PROCESO para actualizar.
                // Pasamos el contenido nuevo en el 'Proceso.java' (aunque tu constructor actual no lo pide,
                // el 'ManejadorArchivo.solicitarOperacion' lo ignora. Lo ideal sería mejorarlo,
                // pero por ahora usamos el 'manejador.actualizarArchivo' para simular).

                // Solución temporal (ya que 'solicitarOperacion' no tiene para 'datos'):
                boolean exito = manejador.actualizarArchivo(ruta, nuevoContenido, "admin");

                if (exito) {
                    panelConsola.agregarLinea("Archivo editado (modo directo): " + nombreArchivo);
                } else {
                    panelConsola.agregarLinea("ERROR: No se pudo editar el archivo");
                }
>>>>>>> day
            }
            
            // Calcular tamaño en bloques (simulado)
            int tamanoBytes = contenido.getBytes().length;
            int bloques = (int) Math.ceil(tamanoBytes / 1024.0); // 1 bloque = 1KB
            
            // Registrar en el sistema simulado
            boolean exito = manejador.crearArchivo(nombreArchivo, bloques, "admin");
            
            if (exito) {
                panelConsola.agregarLinea("✅ Archivo REAL creado: " + archivo.getAbsolutePath());
                panelConsola.agregarLinea("   Tamaño: " + tamanoBytes + " bytes (" + bloques + " bloques)");
                panelArchivos.actualizarArbol();
                
                if (panelOutput != null) {
                    panelOutput.mostrarDetallesArchivo(nombreArchivo, tamanoBytes, 
                        bloques * 1024, bloques, bloques, "Bloques asignados: " + bloques);
                }
            } else {
                panelConsola.agregarLinea("❌ Error en sistema simulado, pero archivo real fue creado");
            }
            
        } catch (IOException ex) {
            panelConsola.agregarLinea("❌ Error al crear archivo real: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al crear archivo: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
<<<<<<< HEAD
    private void editarArchivoReal() {
        fileChooser.setDialogTitle("Seleccionar Archivo para Editar");
        fileChooser.setApproveButtonText("Editar");
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            editarArchivoEnSistemaReal(archivo);
=======
    private void eliminarArchivo() {
        String archivoSeleccionado = panelArchivos.getArchivoSeleccionado();
        if (archivoSeleccionado != null && archivoSeleccionado.contains("(")) {
            String nombreArchivo = archivoSeleccionado.split("\\(")[0].trim();
            
            int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar " + nombreArchivo + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
             String ruta = "/" + nombreArchivo;

             // CAMBIO: Solicitamos un PROCESO para eliminar.
             manejador.solicitarOperacion("ELIMINAR", ruta, "admin", 0);

             panelConsola.agregarLinea("Solicitud de PROCESO 'ELIMINAR' enviada para: " + nombreArchivo);

             panelConsola.agregarLinea("Solicitud de PROCESO 'ELIMINAR' enviada para: " + nombreArchivo);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo primero");
>>>>>>> day
        }
    }
    
    private void editarArchivoEnSistemaReal(File archivo) {
        try {
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "El archivo no existe: " + archivo.getName(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Leer contenido actual
            String contenidoActual = new String(Files.readAllBytes(archivo.toPath()));
            
            // Pedir nuevo contenido
            String nuevoContenido = (String) JOptionPane.showInputDialog(this, 
                "Editar contenido de " + archivo.getName() + ":", 
                "Editar Archivo", 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                null, 
                contenidoActual);
            
            if (nuevoContenido != null && !nuevoContenido.equals(contenidoActual)) {
                // Escribir nuevo contenido
                try (FileWriter writer = new FileWriter(archivo)) {
                    writer.write(nuevoContenido);
                }
                
                // Actualizar en sistema simulado
                int tamanoBytes = nuevoContenido.getBytes().length;
                int bloques = (int) Math.ceil(tamanoBytes / 1024.0);
                
                panelConsola.agregarLinea("✅ Archivo REAL editado: " + archivo.getName());
                panelConsola.agregarLinea("   Nuevo tamaño: " + tamanoBytes + " bytes (" + bloques + " bloques)");
                panelArchivos.actualizarArbol();
            }
            
        } catch (IOException ex) {
            panelConsola.agregarLinea("❌ Error al editar archivo real: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al editar archivo: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarArchivoReal() {
        fileChooser.setDialogTitle("Seleccionar Archivo para Eliminar");
        fileChooser.setApproveButtonText("Eliminar");
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            eliminarArchivoEnSistemaReal(archivo);
        }
    }
    
    private void eliminarArchivoEnSistemaReal(File archivo) {
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(this, 
                "El archivo no existe: " + archivo.getName(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar PERMANENTEMENTE el archivo?\n" + 
            archivo.getName() + "\n\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar Eliminación Permanente",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            if (archivo.delete()) {
                // Eliminar del sistema simulado
                boolean exito = manejador.eliminarArchivo(archivo.getName(), "admin");
                
                panelConsola.agregarLinea("✅ Archivo REAL eliminado: " + archivo.getName());
                if (!exito) {
                    panelConsola.agregarLinea("   ⚠️  Archivo eliminado del sistema real pero no del simulado");
                }
                panelArchivos.actualizarArbol();
            } else {
                panelConsola.agregarLinea("❌ Error al eliminar archivo real");
                JOptionPane.showMessageDialog(this, 
                    "No se pudo eliminar el archivo. Puede estar en uso.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public File getDirectorioBase() {
        return directorioBase;
    }
}