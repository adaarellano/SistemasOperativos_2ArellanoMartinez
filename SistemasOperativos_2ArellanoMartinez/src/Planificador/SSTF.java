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
 * Implementación de política SSTF (Shortest Seek Time First)
 * Atiende la solicitud más cercana al cabezal actual
 */

public class SSTF implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    
    public SSTF() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
    }
    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        System.out.println("SSTF - Solicitud agregada: " + solicitud);
    }
    
    @Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            return null;
        }
        
        SolicitudDisco masCercana = null;
        int distanciaMinima = Integer.MAX_VALUE;
        int indiceMasCercano = -1;
        
        // Buscar la solicitud más cercana al cabezal actual
        for (int i = 0; i < colaSolicitudes.getSize(); i++) {
            SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
            int distancia = Math.abs(solicitud.getBloqueSolicitado() - cabezalActual);
            
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercana = solicitud;
                indiceMasCercano = i;
            }
        }
        
        if (masCercana != null) {
            // Eliminar la solicitud encontrada
            Object elemento = colaSolicitudes.get(indiceMasCercano);
            colaSolicitudes.remove(elemento);
            
            // Actualizar cabezal
            cabezalActual = masCercana.getBloqueSolicitado();
            
            System.out.println("SSTF - Procesando: " + masCercana + " (distancia: " + distanciaMinima + ")");
        }
        
        return masCercana;
    }
    
    @Override
    public ListaSimple getSolicitudesPendientes() {
        return colaSolicitudes;
    }
    
    @Override
    public String getNombrePolitica() {
        return "SSTF";
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