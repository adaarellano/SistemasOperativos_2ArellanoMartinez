/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificador;


import Models.SolicitudDisco;
import edd.ListaSimple;
import MainGUI.PanelConsola;
/**
 * Implementación de política FIFO (First-In, First-Out)
 * La solicitud más antigua se atiende primero
 */


public class FIFO implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    private PanelConsola consola;
    
    public FIFO() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
    }
    
    public FIFO(PanelConsola consola) {
        this();
        this.consola = consola;
    }

    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        if (consola != null) {
            consola.agregarLinea("FIFO - Solicitud agregada: " + solicitud.getTipoOperacion() + 
                               " bloque " + solicitud.getBloqueSolicitado());
        }
    }
    
    @Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            // ELIMINADO: if (consola != null) consola.agregarLinea("FIFO - No hay solicitudes pendientes");
            return null;
        }
        
        // FIFO: siempre toma la primera solicitud (más antigua)
        SolicitudDisco siguiente = (SolicitudDisco) colaSolicitudes.get(0);
        colaSolicitudes.remove(siguiente);
        
        // Actualizar cabezal
        int nuevoCabezal = siguiente.getBloqueSolicitado();
        int distancia = Math.abs(nuevoCabezal - cabezalActual);
        
        if (consola != null) {
            consola.agregarLinea("FIFO - Procesando: " + siguiente.getTipoOperacion() + 
                               " bloque " + nuevoCabezal);
        }
        
        cabezalActual = nuevoCabezal;
        return siguiente;
    }
    
    @Override
    public ListaSimple getSolicitudesPendientes() {
        return colaSolicitudes;
    }
    
    @Override
    public String getNombrePolitica() {
        return "FIFO";
    }
    
    @Override
    public int getCabezalActual() {
        return cabezalActual;
    }
    
    @Override
    public void setCabezalActual(int cabezal) {
        this.cabezalActual = cabezal;
    }
}