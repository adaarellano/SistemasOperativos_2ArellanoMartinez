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
        
        // Configurar el árbol
        arbolArchivos.setRootVisible(true);
        arbolArchivos.setShowsRootHandles(true);
        
        add(new JScrollPane(arbolArchivos), BorderLayout.CENTER);
    }
    
    public void actualizarVista(boolean esAdmin) {
        actualizarArbol();
        // Aquí podrías cambiar colores o estilos según el modo
    }
    
    public void actualizarArbol() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Sistema de Archivos");
        modeloArbol.setRoot(raiz);
        
        // Agregar directorio raíz
        agregarDirectorioAlArbol(manejador.getRaiz(), raiz);
        
        // Expandir todo
        for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
            arbolArchivos.expandRow(i);
        }
    }
    
    private void agregarDirectorioAlArbol(Directorio directorio, DefaultMutableTreeNode nodoPadre) {
        String textoNodo = directorio.getNombre() + " (" + 
                          directorio.getArchivos().getSize() + " archivos, " +
                          directorio.getSubdirectorios().getSize() + " directorios)";
        
        DefaultMutableTreeNode nodoDirectorio = new DefaultMutableTreeNode(textoNodo);
        nodoPadre.add(nodoDirectorio);
        
        // Agregar archivos
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            String textoArchivo = archivo.getNombre() + " (" + 
                                archivo.getTamañoBloques() + "/" + 
                                archivo.getBloquesReservados() + " bloques)";
            nodoDirectorio.add(new DefaultMutableTreeNode(textoArchivo));
        }
        
        // Agregar subdirectorios recursivamente
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            agregarDirectorioAlArbol(subdir, nodoDirectorio);
        }
    }
    
    public String getArchivoSeleccionado() {
        TreePath seleccion = arbolArchivos.getSelectionPath();
        if (seleccion != null) {
            Object ultimoComponente = seleccion.getLastPathComponent();
            if (ultimoComponente instanceof DefaultMutableTreeNode) {
                return ultimoComponente.toString();
            }
        }
        return null;
    }
}