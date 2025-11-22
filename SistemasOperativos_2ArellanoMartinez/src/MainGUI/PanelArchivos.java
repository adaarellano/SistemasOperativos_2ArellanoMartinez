/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import Models.Archivo;
import Models.Directorio;
import edd.ListaSimple;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

public class PanelArchivos extends JPanel {
    private ManejadorArchivo manejador;
    private JTree arbolArchivos;
    private DefaultTreeModel modeloArbol;
    
    public PanelArchivos(ManejadorArchivo manejador) {
        this.manejador = manejador;
        inicializarPanel();
        actualizarArbol();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        
        // Crear modelo de árbol
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Sistema de Archivos");
        modeloArbol = new DefaultTreeModel(raiz);
        arbolArchivos = new JTree(modeloArbol);
        
        // Configurar el árbol para permitir selección simple
        arbolArchivos.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbolArchivos.setShowsRootHandles(true);
        
        add(new JScrollPane(arbolArchivos), BorderLayout.CENTER);
    }
    
    public void actualizarVista(boolean esAdmin) {
        actualizarArbol();
    }
    
    public void actualizarArbol() {
        // Envolver en invokeLater asegura que la actualización visual
        // ocurra en el hilo correcto
        SwingUtilities.invokeLater(() -> {
            DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Sistema de Archivos");
            
            // Agregar directorio raíz
            if (manejador != null && manejador.getRaiz() != null) {
                agregarDirectorioAlArbol(manejador.getRaiz(), raiz);
            }
            
            modeloArbol.setRoot(raiz);
            modeloArbol.reload();
            
            // Expandir todo para ver los archivos
            for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
                arbolArchivos.expandRow(i);
            }
        });
    }
    
    private void agregarDirectorioAlArbol(Directorio directorio, DefaultMutableTreeNode nodoPadre) {
        // Formato: Nombre (X archivos, Y directorios)
        String textoNodo = directorio.getNombre() + " (" + 
                          directorio.getArchivos().getSize() + " archivos, " +
                          directorio.getSubdirectorios().getSize() + " directorios)";
        
        DefaultMutableTreeNode nodoDirectorio = new DefaultMutableTreeNode(textoNodo);
        nodoPadre.add(nodoDirectorio);
        
        // Agregar subdirectorios recursivamente
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            agregarDirectorioAlArbol(subdir, nodoDirectorio);
        }
        
        // Agregar archivos
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            // Formato: Nombre (X/Y bloques)
            String textoArchivo = archivo.getNombre() + " (" + 
                                archivo.getTamañoBloques() + "/" + 
                                archivo.getBloquesReservados() + " bloques)";
            nodoDirectorio.add(new DefaultMutableTreeNode(textoArchivo));
        }
    }
    
    /**
     * Método robusto para obtener el texto del nodo seleccionado.
     */
    public String getArchivoSeleccionado() {
        // Forma segura de obtener la selección
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
        
        if (nodo == null) {
            return null; // Nada seleccionado
        }
        
        if (nodo.isRoot()) {
            return null; // No dejar seleccionar la raíz
        }
        
        // Retorna el texto (UserObject)
        return nodo.toString();
    }
}