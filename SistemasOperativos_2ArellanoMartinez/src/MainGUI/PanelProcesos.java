/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import Models.Proceso;
import edd.ListaSimple;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel que muestra la cola de procesos (Listos, Bloqueados, etc.)
 * en un JTable, como lo requiere el proyecto.
 */
public class PanelProcesos extends JPanel {
    private ManejadorArchivo manejador;
    private JTable tablaProcesos;
    private DefaultTableModel modeloTabla;
    
    public PanelProcesos(ManejadorArchivo manejador) {
        this.manejador = manejador;
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Gestor de Procesos y E/S"));
        
        // Definir las columnas
        String[] columnas = {"PID", "Nombre", "Estado", "Operación", "Usuario", "% E/S"};
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProcesos = new JTable(modeloTabla);
        add(new JScrollPane(tablaProcesos), BorderLayout.CENTER);
        
        // Iniciar un hilo que actualice esta tabla automáticamente
        iniciarHiloActualizador();
    }
    
    /**
     * Hilo que refresca la tabla de procesos cada segundo.
     */
    private void iniciarHiloActualizador() {
        Thread hilo = new Thread(() -> {
            while (true) {
                try {
                    // Actualizar la tabla en el hilo de Swing
                    SwingUtilities.invokeLater(this::actualizarTabla);
                    Thread.sleep(250); // Refrescar cada segundo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }
    
    /**
     * Limpia y vuelve a llenar la tabla con el estado actual de los procesos.
     */
    public void actualizarTabla() {
        if (manejador == null) return;
        
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        // Obtener listas del manejador
        ListaSimple colaListos = manejador.getColaProcesos();
        ListaSimple enEjecucion = manejador.getProcesosActivos();
        
        // 1. Añadir procesos en cola (Listos)
        for (int i = 0; i < colaListos.getSize(); i++) {
            Proceso p = (Proceso) colaListos.get(i);
            agregarProcesoATabla(p);
        }
        
        // 2. Añadir procesos activos (Ejecutando/Bloqueado)
        for (int i = 0; i < enEjecucion.getSize(); i++) {
            Proceso p = (Proceso) enEjecucion.get(i);
            agregarProcesoATabla(p);
        }
    }
    
    private void agregarProcesoATabla(Proceso p) {
        if (p == null) return;
        
        String pid = p.getId();
        String nombre = p.getNombre();
        String estado = p.getEstado().toString();
        String operacion = p.getTipoOperacion();
        String usuario = p.getUsuario();
        String progreso = "N/A";
        
        if (p.getEstado() == Proceso.Estado.BLOQUEADO && p.estaEnES()) {
            progreso = String.format("%.0f%%", p.getPorcentajeCompletado());
        }
        
        modeloTabla.addRow(new Object[]{pid, nombre, estado, operacion, usuario, progreso});
    }
}