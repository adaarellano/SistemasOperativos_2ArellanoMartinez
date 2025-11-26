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
        
        // Nodo raíz temporal
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Cargando...");
        modeloArbol = new DefaultTreeModel(raiz);
        arbolArchivos = new JTree(modeloArbol);
        
        // Configuración básica
        arbolArchivos.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbolArchivos.setShowsRootHandles(true);
        
        // === RENDERIZADOR ===
        arbolArchivos.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, 
                    boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                Object userObject = nodo.getUserObject();
                
                if (userObject instanceof Directorio) {
                    Directorio dir = (Directorio) userObject;
                    // Si es la raíz, mostramos un nombre especial
                    if (dir.esRaiz()) {
                        setText("Sistema de Archivos (Raíz)");
                        setIcon(UIManager.getIcon("FileView.computerIcon"));
                    } else {
                        String textoVisual = dir.getNombre() + " (" + 
                                           dir.getArchivos().getSize() + " archivos, " + 
                                           dir.getSubdirectorios().getSize() + " dirs)";
                        setText(textoVisual);
                        setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    }
                } else if (userObject instanceof Archivo) {
                    Archivo arch = (Archivo) userObject;
                    String textoVisual = arch.getNombre() + " (" + 
                                       arch.getTamañoBloques() + "/" + 
                                       arch.getBloquesReservados() + " blqs)";
                    setText(textoVisual);
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
                return this;
            }
        });
        
        add(new JScrollPane(arbolArchivos), BorderLayout.CENTER);
    }
    
    public void actualizarVista(boolean esAdmin) {
        actualizarArbol();
    }
    
    public void actualizarArbol() {
        SwingUtilities.invokeLater(() -> {
            if (manejador != null && manejador.getRaiz() != null) {
                // 1. La raíz del árbol AHORA contiene el objeto Directorio Raíz real
                DefaultMutableTreeNode nodoRaiz = new DefaultMutableTreeNode(manejador.getRaiz());
                
                // 2. Llenamos el árbol (Aquí se llama al método privado)
                agregarDirectorioAlArbol(manejador.getRaiz(), nodoRaiz);
                
                modeloArbol.setRoot(nodoRaiz);
                modeloArbol.reload();
                
                // 3. Expandir todo
                for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
                    arbolArchivos.expandRow(i);
                }
            }
        });
    }
    
    // MÉTODO RECURSIVO UNIFICADO
    private void agregarDirectorioAlArbol(Directorio directorio, DefaultMutableTreeNode nodoPadre) {
        // 1. Agregar subdirectorios
        ListaSimple subdirs = directorio.getSubdirectorios();
        for (int i = 0; i < subdirs.getSize(); i++) {
            Directorio subdir = (Directorio) subdirs.get(i);
            // Guardamos el OBJETO Directorio
            DefaultMutableTreeNode nodoSubdir = new DefaultMutableTreeNode(subdir);
            nodoPadre.add(nodoSubdir);
            
            // Recursividad: Llenar este subdirectorio
            agregarDirectorioAlArbol(subdir, nodoSubdir);
        }
        
        // 2. Agregar archivos (CON FILTRO)
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            
            // --- FILTRO: SOLO MOSTRAR SI YA FUE PROCESADO POR EL DISCO ---
            if (archivo.isConfirmadoEnDisco()) { 
                // Guardamos el OBJETO Archivo
                DefaultMutableTreeNode nodoArchivo = new DefaultMutableTreeNode(archivo);
                nodoPadre.add(nodoArchivo);
            }
            // ---------------------
        }
    }
    
    /**
     * Obtiene la ruta exacta preguntándole directamente al objeto seleccionado.
     */
    public String obtenerRutaSeleccionada() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
        
        // Si no hay nada seleccionado, devolvemos la raíz "/"
        if (nodo == null) return "/";
        
        Object obj = nodo.getUserObject();
        
        if (obj instanceof Directorio) {
            // Si seleccionaste una carpeta, devolvemos su ruta exacta
            return ((Directorio) obj).getRutaCompleta();
        } else if (obj instanceof Archivo) {
            // Si seleccionaste un archivo, devolvemos la ruta de SU CARPETA PADRE
            String rutaArchivo = ((Archivo) obj).getRutaCompleta();
            int ultimoSlash = rutaArchivo.lastIndexOf('/');
            if (ultimoSlash == 0) return "/"; 
            return rutaArchivo.substring(0, ultimoSlash);
        }
        
        return "/";
    }
    
    /**
     * Devuelve true si lo seleccionado es un Directorio, false si es Archivo o nada.
     */
    public boolean esDirectorioSeleccionado() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
        if (nodo == null) return false;
        
        Object obj = nodo.getUserObject();
        return (obj instanceof Directorio);
    }
    
    // Método para permitir que la Interfaz escuche clics en el árbol
    public void agregarListenerSeleccion(javax.swing.event.TreeSelectionListener listener) {
        arbolArchivos.addTreeSelectionListener(listener);
    }

    // Método para obtener el OBJETO real (Archivo o Directorio) seleccionado
    public Object getObjetoSeleccionado() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
        if (nodo == null) return null;
        return nodo.getUserObject();
    }
    
    public String getArchivoSeleccionado() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
        if (nodo == null) return null;
        
        Object obj = nodo.getUserObject();
        if (obj instanceof Archivo) {
            return ((Archivo) obj).getNombre();
        } else if (obj instanceof Directorio) {
            return ((Directorio) obj).getNombre();
        }
        return null;
    }
}