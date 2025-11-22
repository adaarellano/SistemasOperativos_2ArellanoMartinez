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
    
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, PanelConsola panelConsola, PanelOutput panelOutput, PanelDisco panelDisco1, PanelTablaAsignacion panelTablaAsignacion1) {
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
        btnActualizar.addActionListener(e -> {
            panelArchivos.actualizarArbol();
            if (panelDisco != null) panelDisco.actualizarDisco();
            if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
        });
        
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

             // CAMBIO: Solicitamos un PROCESO para eliminar.
             manejador.solicitarOperacion("ELIMINAR", ruta, "admin", 0);

             panelConsola.agregarLinea("Solicitud de PROCESO 'ELIMINAR' enviada para: " + nombreArchivo);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo primero");
        }
    }
}