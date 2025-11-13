/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PanelControl extends JPanel {
    private ManejadorArchivo manejador;
    private PanelArchivos panelArchivos;
    private PanelConsola panelConsola;
    private PanelOutput panelOutput;
    
    // Botones simples
    private JButton btnCrear, btnLeer, btnEditar, btnEliminar, btnListar, btnRefrescar;
    
    public PanelControl(ManejadorArchivo manejador, PanelArchivos panelArchivos, 
                       PanelConsola panelConsola, PanelOutput panelOutput) {
        this.manejador = manejador;
        this.panelArchivos = panelArchivos;
        this.panelConsola = panelConsola;
        this.panelOutput = panelOutput;
        
        inicializarPanel();
        configurarEventos();
    }
    
    private void inicializarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Operaciones CRUD"));
        
        // Botones básicos
        btnCrear = new JButton("Crear Archivo");
        btnLeer = new JButton("Leer Archivo");
        btnEditar = new JButton("Editar Archivo");
        btnEliminar = new JButton("Eliminar Archivo");
        btnListar = new JButton("Listar Todo");
        btnRefrescar = new JButton("Refrescar");
        
        // Agregar botones
        add(btnCrear);
        add(btnLeer);
        add(btnEditar);
        add(btnEliminar);
        add(btnListar);
        add(btnRefrescar);
    }
    
    private void configurarEventos() {
        btnCrear.addActionListener(e -> crearArchivo());
        btnLeer.addActionListener(e -> leerArchivo());
        btnEditar.addActionListener(e -> editarArchivo());
        btnEliminar.addActionListener(e -> eliminarArchivo());
        btnListar.addActionListener(e -> listarArchivos());
        btnRefrescar.addActionListener(e -> refrescarSistema());
    }
    
    private void crearArchivo() {
        try {
            // Pedir ruta completa
            String rutaCompleta = JOptionPane.showInputDialog(this,
                "Ingrese la ruta completa del archivo (ej: /documentos/miarchivo.txt):",
                "Crear Archivo",
                JOptionPane.QUESTION_MESSAGE);
            
            if (rutaCompleta == null || rutaCompleta.trim().isEmpty()) return;
            
            // Pedir tamaño en bloques
            String tamanoStr = JOptionPane.showInputDialog(this,
                "Tamaño en bloques (cada bloque = 1024 bytes):",
                "3");
            
            if (tamanoStr == null) return;
            
            int tamanoBloques = Integer.parseInt(tamanoStr);
            
            // Crear archivo en el sistema simulado
            boolean exito = manejador.crearArchivo(rutaCompleta, tamanoBloques, "admin");
            
            if (exito) {
                panelConsola.agregarLinea("✅ Archivo creado: " + rutaCompleta);
                refrescarSistema();
            } else {
                panelConsola.agregarLinea("❌ Error al crear archivo");
            }
            
        } catch (Exception ex) {
            panelConsola.agregarLinea("❌ Error: " + ex.getMessage());
        }
    }
    
    private void leerArchivo() {
        try {
            String rutaCompleta = JOptionPane.showInputDialog(this,
                "Ruta del archivo a leer:",
                "Leer Archivo",
                JOptionPane.QUESTION_MESSAGE);
            
            if (rutaCompleta == null || rutaCompleta.trim().isEmpty()) return;
            
            String contenido = manejador.leerArchivo(rutaCompleta, "admin");
            
            // Mostrar en output
            panelOutput.agregarLinea("=== CONTENIDO DE: " + rutaCompleta + " ===");
            panelOutput.agregarLinea(contenido);
            panelOutput.agregarLinea("=== FIN DEL CONTENIDO ===");
            
            panelConsola.agregarLinea("✅ Archivo leído: " + rutaCompleta);
            
        } catch (Exception ex) {
            panelConsola.agregarLinea("❌ Error al leer: " + ex.getMessage());
        }
    }
    
    private void editarArchivo() {
        try {
            String rutaCompleta = JOptionPane.showInputDialog(this,
                "Ruta del archivo a editar:",
                "Editar Archivo",
                JOptionPane.QUESTION_MESSAGE);
            
            if (rutaCompleta == null || rutaCompleta.trim().isEmpty()) return;
            
            // Primero leer el contenido actual
            String contenidoActual = manejador.leerArchivo(rutaCompleta, "admin");
            
            // Pedir nuevo contenido
            JTextArea textArea = new JTextArea(10, 40);
            textArea.setText(contenidoActual);
            JScrollPane scrollPane = new JScrollPane(textArea);
            
            int result = JOptionPane.showConfirmDialog(this, scrollPane,
                "Editar contenido de: " + rutaCompleta,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String nuevoContenido = textArea.getText();
                boolean exito = manejador.actualizarArchivo(rutaCompleta, nuevoContenido, "admin");
                
                if (exito) {
                    panelConsola.agregarLinea("✅ Archivo editado: " + rutaCompleta);
                    panelOutput.agregarLinea("=== ARCHIVO ACTUALIZADO: " + rutaCompleta + " ===");
                    panelOutput.agregarLinea("Nuevo contenido: " + nuevoContenido);
                    refrescarSistema();
                } else {
                    panelConsola.agregarLinea("❌ Error al editar archivo");
                }
            }
            
        } catch (Exception ex) {
            panelConsola.agregarLinea("❌ Error al editar: " + ex.getMessage());
        }
    }
    
    private void eliminarArchivo() {
        try {
            String rutaCompleta = JOptionPane.showInputDialog(this,
                "Ruta del archivo a eliminar:",
                "Eliminar Archivo",
                JOptionPane.QUESTION_MESSAGE);
            
            if (rutaCompleta == null || rutaCompleta.trim().isEmpty()) return;
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar: " + rutaCompleta + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean exito = manejador.eliminarArchivo(rutaCompleta, "admin");
                
                if (exito) {
                    panelConsola.agregarLinea("✅ Archivo eliminado: " + rutaCompleta);
                    refrescarSistema();
                } else {
                    panelConsola.agregarLinea("❌ Error al eliminar archivo");
                }
            }
            
        } catch (Exception ex) {
            panelConsola.agregarLinea("❌ Error al eliminar: " + ex.getMessage());
        }
    }
    
    private void listarArchivos() {
        try {
            panelOutput.agregarLinea("=== LISTADO COMPLETO DEL SISTEMA ===");
            
            // Obtener información del directorio raíz
            String estado = manejador.getEstadoSistema();
            panelOutput.agregarLinea(estado);
            
            // Mostrar árbol completo
            panelOutput.agregarLinea("=== ESTRUCTURA DE ARCHIVOS ===");
            mostrarEstructuraArchivos(manejador.getRaiz(), 0);
            
            panelConsola.agregarLinea("✅ Listado completado");
            
        } catch (Exception ex) {
            panelConsola.agregarLinea("❌ Error al listar: " + ex.getMessage());
        }
    }
    
    private void mostrarEstructuraArchivos(Models.Directorio directorio, int nivel) {
        String indent = "  ".repeat(nivel);
        
        // Mostrar directorio actual
        panelOutput.agregarLinea(indent + "📁 " + directorio.getNombre() + 
            " (Archivos: " + directorio.getArchivos().getSize() + 
            ", Subdirectorios: " + directorio.getSubdirectorios().getSize() + ")");
        
        // Mostrar archivos
        for (int i = 0; i < directorio.getArchivos().getSize(); i++) {
            Models.Archivo archivo = (Models.Archivo) directorio.getArchivos().get(i);
            panelOutput.agregarLinea(indent + "  📄 " + archivo.getNombre() + 
                " [" + archivo.getTamañoBloques() + " bloques, " + 
                archivo.getTamañoBytes() + " bytes]");
        }
        
        // Mostrar subdirectorios recursivamente
        for (int i = 0; i < directorio.getSubdirectorios().getSize(); i++) {
            Models.Directorio subdir = (Models.Directorio) directorio.getSubdirectorios().get(i);
            mostrarEstructuraArchivos(subdir, nivel + 1);
        }
    }
    
    private void refrescarSistema() {
        panelArchivos.actualizarArbol();
        panelConsola.agregarLinea("🔄 Sistema refrescado");
    }
    
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            panelConsola.agregarLinea("=== MODO ADMINISTRADOR ACTIVADO ===");
            panelConsola.agregarLinea("Operaciones CRUD disponibles");
        }
    }
}