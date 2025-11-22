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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
/**
 *
 * @author Day
 */
public class PanelTablaAsignacion extends JPanel {
    private ManejadorArchivo manejador;
    private JTable tablaAsignacion;
    private DefaultTableModel modeloTabla;
    
    public PanelTablaAsignacion(ManejadorArchivo manejador) {
        this.manejador = manejador;
        inicializarPanel();
        actualizarTabla();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Tabla de Asignación de Archivos (FAT)"));
        
        // Definir las columnas para la tabla
        String[] columnas = {"Archivo", "Ruta", "Bloques (Usados/Reservados)", "1er Bloque"};
        
        // Crear el modelo de la tabla (no editable)
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Crear el JTable con el modelo
        tablaAsignacion = new JTable(modeloTabla);
        tablaAsignacion.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Añadir el JTable a un JScrollPane
        add(new JScrollPane(tablaAsignacion), BorderLayout.CENTER);
    }
    
    /**
     * Limpia y vuelve a llenar la tabla con datos actualizados
     * del sistema de archivos.
     */
    public void actualizarTabla() {
        // 1. Limpiar datos antiguos
        modeloTabla.setRowCount(0);
        
        // 2. Obtener el directorio raíz
        Directorio raiz = manejador.getRaiz();
        if (raiz == null) return;
        
        // 3. Iniciar el recorrido recursivo para encontrar todos los archivos
        agregarArchivosDeDirectorio(raiz);
    }
    
  /**
     * Método recursivo para recorrer el árbol de directorios
     * y añadir los archivos Y directorios encontrados a la tabla.
     */
    private void agregarArchivosDeDirectorio(Directorio directorio) {
        // 1. Añadir ARCHIVOS del directorio actual
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            
            String nombre = archivo.getNombre();
            String ruta = archivo.getRutaCompleta();
            String bloques = archivo.getTamañoBloques() + " / " + archivo.getBloquesReservados();
            String primerBloque = (archivo.getPrimerBloque() != null) ? 
                                  String.valueOf(archivo.getPrimerBloque().getIdBloque()) : "N/A";
            
            modeloTabla.addRow(new Object[]{nombre, ruta, bloques, primerBloque});
        }
        
        // 2. Añadir SUBDIRECTORIOS y recorrerlos
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            
            // --- NUEVO: Agregar el directorio a la tabla ---
            // Los directorios en esta simulación son lógicos (0 bloques)
            String nombreDir = "[" + subdir.getNombre() + "]"; // Corchetes para distinguir
            String rutaDir = subdir.getRutaCompleta();
            
            modeloTabla.addRow(new Object[]{nombreDir, rutaDir, "0 / 0", "DIR"});
            // -----------------------------------------------
            
            // Llamada recursiva para entrar en el directorio
            agregarArchivosDeDirectorio(subdir); 
        }
    }
}