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
        
        // Agregar listener para doble clic
        arbolArchivos.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
            if (nodo != null && nodo.getUserObject() instanceof String) {
                // Puedes agregar funcionalidad de selección aquí
            }
        });
        
        add(new JScrollPane(arbolArchivos), BorderLayout.CENTER);
    }
    
    public void actualizarVista(boolean esAdmin) {
        actualizarArbol();
    }
    
    public void actualizarArbol() {
        DefaultMutableTreeNode raizArbol = new DefaultMutableTreeNode("Sistema de Archivos");
        modeloArbol.setRoot(raizArbol);
        
        // Agregar directorio raíz del sistema simulado
        agregarDirectorioAlArbol(manejador.getRaiz(), raizArbol);
        
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
                                archivo.getTamañoBloques() + " bloques)";
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
                String texto = ultimoComponente.toString();
                // Extraer solo el nombre del archivo (antes del paréntesis)
                if (texto.contains("(")) {
                    return texto.split("\\(")[0].trim();
                }
                return texto;
            }
        }
        return null;
    }
    
    public JTree getArbolArchivos() {
        return arbolArchivos;
    }
}