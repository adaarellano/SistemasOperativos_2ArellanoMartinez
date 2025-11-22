/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificador;

/**
 *
 * @author raiza
 */
import Models.SolicitudDisco;
import edd.ListaSimple;

/**
 * Implementación de política C-SCAN (Circular SCAN)
 * Como SCAN pero solo va en una dirección (circular)
 */
public class CSCAN implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    private final int BLOQUES_MAXIMOS = 100;
    
    public CSCAN() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
    }
    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        System.out.println("C-SCAN - Solicitud agregada: " + solicitud);
    }
    
    @Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            return null;
        }
        
        SolicitudDisco mejorSolicitud = null;
        int mejorDistancia = Integer.MAX_VALUE;
        int indiceMejor = -1;
        
        // Buscar la solicitud más cercana en dirección ascendente
        for (int i = 0; i < colaSolicitudes.getSize(); i++) {
            SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
            int bloque = solicitud.getBloqueSolicitado();
            
            if (bloque >= cabezalActual) {
                int distancia = bloque - cabezalActual;
                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    mejorSolicitud = solicitud;
                    indiceMejor = i;
                }
            }
        }
        
        // Si no hay solicitudes adelante, ir al inicio (comportamiento circular)
        if (mejorSolicitud == null) {
            // Buscar la solicitud con el bloque más pequeño
            int bloqueMinimo = Integer.MAX_VALUE;
            for (int i = 0; i < colaSolicitudes.getSize(); i++) {
                SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
                if (solicitud.getBloqueSolicitado() < bloqueMinimo) {
                    bloqueMinimo = solicitud.getBloqueSolicitado();
                    mejorSolicitud = solicitud;
                    indiceMejor = i;
                }
            }
            
            if (mejorSolicitud != null) {
                System.out.println("C-SCAN - Volviendo al inicio, procesando: " + mejorSolicitud);
            }
        } else {
            System.out.println("C-SCAN - Procesando: " + mejorSolicitud);
        }
        
        if (mejorSolicitud != null) {
            Object elemento = colaSolicitudes.get(indiceMejor);
            colaSolicitudes.remove(elemento);
            cabezalActual = mejorSolicitud.getBloqueSolicitado();
        }
        
        return mejorSolicitud;
    }
    
    @Override
    public ListaSimple getSolicitudesPendientes() {
        return colaSolicitudes;
    }
    
    @Override
    public String getNombrePolitica() {
        return "C-SCAN";
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