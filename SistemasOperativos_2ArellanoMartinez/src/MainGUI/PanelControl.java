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

public class PanelControl extends JPanel {
    private ManejadorArchivo manejador;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelDisco panelDisco;
    private PanelTablaAsignacion panelTablaAsignacion;
    
    private JButton btnCrear, btnEditar, btnEliminar, btnActualizar;
    
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, PanelConsola panelConsola, PanelOutput panelOutput, PanelDisco panelDisco, PanelTablaAsignacion panelTablaAsignacion) {
        this.manejador = manejador;
        this.panelArchivos = panelArchivos;
        this.panelConsola = panelConsola;
        this.panelDisco = panelDisco;
        this.panelTablaAsignacion = panelTablaAsignacion;
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createTitledBorder("Operaciones CRUD (Solo Administrador)"));
        
        btnCrear = new JButton("Crear Archivo");
        btnEditar = new JButton("Editar Archivo");
        btnEliminar = new JButton("Eliminar Archivo");
        btnActualizar = new JButton("Actualizar Vista");
        
        add(btnCrear);
        add(btnEditar);
        add(btnEliminar);
        add(btnActualizar);
        
        configurarEventos();
    }
    
    private void configurarEventos() {
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
                boolean exito = manejador.crearArchivo(ruta, tamano, "admin");
                
                if (exito) {
                    panelConsola.agregarLinea("Archivo creado: " + nombre + " (" + tamano + " bloques)");
                    panelArchivos.actualizarArbol();
                    panelDisco.actualizarDisco();
                    panelTablaAsignacion.actualizarTabla();
                } else {
                    panelConsola.agregarLinea("ERROR: No se pudo crear el archivo");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tamaño inválido");
            }
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
                boolean exito = manejador.actualizarArchivo(ruta, nuevoContenido, "admin");
                
                if (exito) {
                    panelConsola.agregarLinea("Archivo editado: " + nombreArchivo);
                    panelConsola.agregarLinea("Nuevo contenido: " + nuevoContenido);
                } else {
                    panelConsola.agregarLinea("ERROR: No se pudo editar el archivo");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo primero");
        }
    }
    
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
                boolean exito = manejador.eliminarArchivo(ruta, "admin");
                
                if (exito) {
                    panelConsola.agregarLinea("Archivo eliminado: " + nombreArchivo);
                    panelArchivos.actualizarArbol();
                    panelDisco.actualizarDisco();
                    panelTablaAsignacion.actualizarTabla();
                } else {
                    panelConsola.agregarLinea("ERROR: No se pudo eliminar el archivo");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo primero");
        }
    }
}