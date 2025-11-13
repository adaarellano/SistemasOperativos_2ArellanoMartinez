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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PanelArchivos extends JPanel {
    private ManejadorArchivo manejador;
    private JTree arbolArchivos;
    private DefaultTreeModel modeloArbol;
    private JLabel lblEstadisticas;
    private boolean esModoAdministrador;
    
    // Iconos para diferentes tipos de elementos
    private Icon iconoArchivo;
    private Icon iconoDirectorio;
    private Icon iconoRaiz;
    
    public PanelArchivos(ManejadorArchivo manejador) {
        this.manejador = manejador;
        this.esModoAdministrador = false;
        inicializarIconos();
        inicializarPanel();
        actualizarArbol();
    }
    
    private void inicializarIconos() {
        // Iconos básicos (puedes reemplazar con imágenes reales)
        iconoArchivo = UIManager.getIcon("FileView.fileIcon");
        iconoDirectorio = UIManager.getIcon("FileView.directoryIcon");
        iconoRaiz = UIManager.getIcon("FileView.hardDriveIcon");
        
        // Si los iconos del sistema no están disponibles, crear unos básicos
        if (iconoArchivo == null) {
            iconoArchivo = new DefaultTreeCellRenderer().getLeafIcon();
        }
        if (iconoDirectorio == null) {
            iconoDirectorio = new DefaultTreeCellRenderer().getClosedIcon();
        }
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        
        // Crear modelo de árbol con renderizador personalizado
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(
            new NodoSistema("Sistema de Archivos", TipoNodo.RAIZ));
        modeloArbol = new DefaultTreeModel(raiz);
        arbolArchivos = new JTree(modeloArbol);
        
        // Configurar renderizador personalizado
        arbolArchivos.setCellRenderer(new RenderizadorArbol());
        
        // Configurar el árbol
        arbolArchivos.setRootVisible(true);
        arbolArchivos.setShowsRootHandles(true);
        arbolArchivos.setExpandsSelectedPaths(true);
        
        // Agregar listener para clics
        agregarListeners();
        
        // Panel de estadísticas
        lblEstadisticas = new JLabel("Cargando...");
        lblEstadisticas.setBorder(BorderFactory.createLoweredBevelBorder());
        
        add(new JScrollPane(arbolArchivos), BorderLayout.CENTER);
        add(lblEstadisticas, BorderLayout.SOUTH);
    }
    
    private void agregarListeners() {
        arbolArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Doble clic
                    manejarDobleClic();
                }
            }
        });
        
        arbolArchivos.addTreeSelectionListener(e -> {
            actualizarEstadisticas();
            resaltarElementoSeleccionado();
        });
    }
    
    private void manejarDobleClic() {
        TreePath seleccion = arbolArchivos.getSelectionPath();
        if (seleccion != null) {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) seleccion.getLastPathComponent();
            if (nodo.getUserObject() instanceof NodoSistema) {
                NodoSistema nodoSistema = (NodoSistema) nodo.getUserObject();
                // Aquí podrías abrir el archivo o expandir/contraer directorio
                if (nodoSistema.getTipo() == TipoNodo.ARCHIVO) {
                    abrirArchivo(nodoSistema.getNombre());
                }
            }
        }
    }
    
    private void abrirArchivo(String nombreArchivo) {
        // Lógica para abrir/visualizar archivo
        JOptionPane.showMessageDialog(this, 
            "Abriendo archivo: " + nombreArchivo, 
            "Abrir Archivo", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void actualizarVista(boolean esAdmin) {
        this.esModoAdministrador = esAdmin;
        actualizarArbol();
        actualizarEstadisticas();
        
        // Cambiar apariencia según el modo
        if (esAdmin) {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 2), 
                "Sistema de Archivos - MODO ADMINISTRADOR"));
        } else {
            setBorder(BorderFactory.createTitledBorder(
                "Sistema de Archivos - MODO USUARIO"));
        }
    }
    
    public void actualizarArbol() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(
            new NodoSistema("Sistema de Archivos", TipoNodo.RAIZ));
        modeloArbol.setRoot(raiz);
        
        // Agregar directorio raíz
        agregarDirectorioAlArbol(manejador.getRaiz(), raiz);
        
        // Expandir todo
        expandirTodo();
        
        actualizarEstadisticas();
    }
    
    private void expandirTodo() {
        for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
            arbolArchivos.expandRow(i);
        }
    }
    
    private void agregarDirectorioAlArbol(Directorio directorio, DefaultMutableTreeNode nodoPadre) {
        NodoSistema nodoDir = new NodoSistema(
            directorio.getNombre(), 
            TipoNodo.DIRECTORIO,
            directorio
        );
        
        DefaultMutableTreeNode nodoDirectorio = new DefaultMutableTreeNode(nodoDir);
        nodoPadre.add(nodoDirectorio);
        
        // Agregar archivos
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            NodoSistema nodoArchivo = new NodoSistema(
                archivo.getNombre(),
                TipoNodo.ARCHIVO,
                archivo
            );
            nodoDirectorio.add(new DefaultMutableTreeNode(nodoArchivo));
        }
        
        // Agregar subdirectorios recursivamente
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            agregarDirectorioAlArbol(subdir, nodoDirectorio);
        }
    }
    
    private void actualizarEstadisticas() {
        int totalArchivos = contarArchivos(manejador.getRaiz());
        int totalDirectorios = contarDirectorios(manejador.getRaiz());
        int bloquesOcupados = manejador.getBloquesOcupados();
        
        String texto = String.format(
            "Archivos: %d | Directorios: %d | Bloques ocupados: %d | Modo: %s",
            totalArchivos, totalDirectorios, bloquesOcupados,
            esModoAdministrador ? "Administrador" : "Usuario"
        );
        
        lblEstadisticas.setText(texto);
    }
    
    private int contarArchivos(Directorio directorio) {
        int count = directorio.getArchivos().getSize();
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            count += contarArchivos(subdir);
        }
        return count;
    }
    
    private int contarDirectorios(Directorio directorio) {
        int count = directorio.getSubdirectorios().getSize();
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            count += contarDirectorios(subdir);
        }
        return count;
    }
    
    private void resaltarElementoSeleccionado() {
        // Puedes implementar resaltado especial aquí
    }
    
    public String getArchivoSeleccionado() {
        TreePath seleccion = arbolArchivos.getSelectionPath();
        if (seleccion != null) {
            Object ultimoComponente = seleccion.getLastPathComponent();
            if (ultimoComponente instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) ultimoComponente;
                if (nodo.getUserObject() instanceof NodoSistema) {
                    return ((NodoSistema) nodo.getUserObject()).getNombre();
                }
            }
        }
        return null;
    }
    
    public Object getElementoSeleccionado() {
        TreePath seleccion = arbolArchivos.getSelectionPath();
        if (seleccion != null) {
            Object ultimoComponente = seleccion.getLastPathComponent();
            if (ultimoComponente instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) ultimoComponente;
                return nodo.getUserObject();
            }
        }
        return null;
    }
    
    // Clases internas para mejor organización de datos
    private enum TipoNodo {
        RAIZ, DIRECTORIO, ARCHIVO
    }
    
    private class NodoSistema {
        private String nombre;
        private TipoNodo tipo;
        private Object datos;
        
        public NodoSistema(String nombre, TipoNodo tipo) {
            this(nombre, tipo, null);
        }
        
        public NodoSistema(String nombre, TipoNodo tipo, Object datos) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.datos = datos;
        }
        
        // Getters
        public String getNombre() { return nombre; }
        public TipoNodo getTipo() { return tipo; }
        public Object getDatos() { return datos; }
        
        @Override
        public String toString() {
            switch (tipo) {
                case RAIZ:
                    return nombre;
                case DIRECTORIO:
                    Directorio dir = (Directorio) datos;
                    return nombre + " (" + dir.getArchivos().getSize() + " archivos, " +
                           dir.getSubdirectorios().getSize() + " directorios)";
                case ARCHIVO:
                    Archivo archivo = (Archivo) datos;
                    return nombre + " [" + archivo.getTamañoBloques() + 
                           "/" + archivo.getBloquesReservados() + " bloques]";
                default:
                    return nombre;
            }
        }
    }
    
    private class RenderizadorArbol extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                if (nodo.getUserObject() instanceof NodoSistema) {
                    NodoSistema nodoSistema = (NodoSistema) nodo.getUserObject();
                    
                    // Configurar icono según el tipo
                    switch (nodoSistema.getTipo()) {
                        case RAIZ:
                            setIcon(iconoRaiz != null ? iconoRaiz : iconoDirectorio);
                            break;
                        case DIRECTORIO:
                            setIcon(iconoDirectorio);
                            break;
                        case ARCHIVO:
                            setIcon(iconoArchivo);
                            break;
                    }
                    
                    // Resaltar según modo
                    if (!esModoAdministrador && nodoSistema.getTipo() == TipoNodo.ARCHIVO) {
                        setForeground(Color.GRAY);
                    }
                }
            }
            
            return this;
        }
    }
}