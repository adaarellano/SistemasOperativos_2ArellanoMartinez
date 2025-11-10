/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificador;


import Models.SolicitudDisco;
import edd.ListaSimple;

/**
 * Implementación de política FIFO (First-In, First-Out)
 * La solicitud más antigua se atiende primero
 */
public class FIFO implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    
    public FIFO() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
    }
    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        System.out.println("FIFO - Solicitud agregada: " + solicitud);
    }
    
    @Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            return null;
        }
        
        // FIFO: siempre toma la primera solicitud (más antigua)
        SolicitudDisco siguiente = (SolicitudDisco) colaSolicitudes.get(0);
        colaSolicitudes.remove(siguiente);
        
        // Actualizar cabezal
        cabezalActual = siguiente.getBloqueSolicitado();
        
        System.out.println("FIFO - Procesando: " + siguiente);
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