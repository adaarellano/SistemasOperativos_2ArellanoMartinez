/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificador;

import Models.SolicitudDisco;
import edd.ListaSimple;

/**
 * Implementación de política SSTF (Shortest Seek Time First)
 * Atiende la solicitud más cercana al cabezal actual
 * CON SALIDA DETALLADA EN CONSOLA
 */
public class SSTF implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    private boolean debugDetallado = true; // Activar salida detallada
    
    public SSTF() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
    }
    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        if (debugDetallado) {
            System.out.println("SSTF - Solicitud agregada: " + solicitud.getTipoOperacion() + 
                             " bloque " + solicitud.getBloqueSolicitado());
        }
    }
    
    @Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            if (debugDetallado) {
                System.out.println("SSTF - No hay solicitudes pendientes");
            }
            return null;
        }
        
        if (debugDetallado) {
            System.out.println("\n=== SSTF - BUSCANDO SIGUIENTE SOLICITUD ===");
            System.out.println("Cabezal actual: " + cabezalActual);
            System.out.println("Solicitudes pendientes: " + colaSolicitudes.getSize());
        }
        
        SolicitudDisco masCercana = null;
        int distanciaMinima = Integer.MAX_VALUE;
        int indiceMasCercano = -1;
        
        // Mostrar todas las distancias calculadas
        if (debugDetallado) {
            System.out.println("--- CALCULANDO DISTANCIAS ---");
        }
        
        // Buscar la solicitud más cercana al cabezal actual
        for (int i = 0; i < colaSolicitudes.getSize(); i++) {
            SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
            int bloque = solicitud.getBloqueSolicitado();
            int distancia = Math.abs(bloque - cabezalActual);
            
            if (debugDetallado) {
                System.out.println("  Bloque " + bloque + " - Distancia: " + distancia + 
                                 " (|" + cabezalActual + " - " + bloque + "|)");
            }
            
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercana = solicitud;
                indiceMasCercano = i;
                
                if (debugDetallado) {
                    System.out.println("    -> NUEVA MAS CERCANA: Bloque " + bloque + 
                                     " (distancia: " + distancia + ")");
                }
            }
        }
        
        if (masCercana != null) {
            // Eliminar la solicitud encontrada
            Object elemento = colaSolicitudes.get(indiceMasCercano);
            colaSolicitudes.remove(elemento);
            
            // Actualizar cabezal
            int nuevoCabezal = masCercana.getBloqueSolicitado();
            int distanciaRecorrida = Math.abs(nuevoCabezal - cabezalActual);
            
            if (debugDetallado) {
                System.out.println("--- RESULTADO SSTF ---");
                System.out.println("Solicitud seleccionada: " + masCercana.getTipoOperacion() + 
                                 " bloque " + nuevoCabezal);
                System.out.println("Distancia recorrida: " + distanciaRecorrida);
                System.out.println("Cabezal: " + cabezalActual + " -> " + nuevoCabezal);
                System.out.println("Solicitudes restantes: " + colaSolicitudes.getSize());
                System.out.println("=== FIN DECISION SSTF ===\n");
            }
            
            cabezalActual = nuevoCabezal;
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
        if (debugDetallado) {
            System.out.println("SSTF - Cabezal movido a: " + cabezal);
        }
        this.cabezalActual = cabezal;
    }
    
    /**
     * Método adicional para mostrar estado completo
     */
    public String getEstadoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("SSTF - Estado Actual:\n");
        sb.append("  Cabezal: ").append(cabezalActual).append("\n");
        sb.append("  Solicitudes pendientes: ").append(colaSolicitudes.getSize()).append("\n");
        
        if (!colaSolicitudes.isEmpty()) {
            sb.append("  Solicitudes en cola:\n");
            for (int i = 0; i < colaSolicitudes.getSize(); i++) {
                SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
                int distancia = Math.abs(solicitud.getBloqueSolicitado() - cabezalActual);
                sb.append("    - ").append(solicitud.getTipoOperacion())
                  .append(" bloque ").append(solicitud.getBloqueSolicitado())
                  .append(" (distancia: ").append(distancia).append(")\n");
            }
        }
        
        return sb.toString();
    }
}