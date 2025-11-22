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
 * Implementación de política SCAN (Elevator Algorithm)
 * El cabezal se mueve en una dirección atendiendo solicitudes en el camino
 */
public class SCAN implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    private boolean direccionDerecha; // true = derecha, false = izquierda
    private final int BLOQUES_MAXIMOS = 100; // Tamaño máximo del disco
    
    public SCAN() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
        this.direccionDerecha = true; // Por defecto va hacia la derecha
    }
    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        System.out.println("SCAN - Solicitud agregada: " + solicitud);
    }
    
    @Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            return null;
        }
        
        SolicitudDisco mejorSolicitud = null;
        int mejorDistancia = Integer.MAX_VALUE;
        int indiceMejor = -1;
        
        // Buscar solicitud en la dirección actual
        for (int i = 0; i < colaSolicitudes.getSize(); i++) {
            SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
            int bloque = solicitud.getBloqueSolicitado();
            int distancia = bloque - cabezalActual;
            
            if (direccionDerecha && distancia >= 0) {
                // En dirección derecha y adelante del cabezal
                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    mejorSolicitud = solicitud;
                    indiceMejor = i;
                }
            } else if (!direccionDerecha && distancia <= 0) {
                // En dirección izquierda y antes del cabezal
                int distanciaAbs = Math.abs(distancia);
                if (distanciaAbs < mejorDistancia) {
                    mejorDistancia = distanciaAbs;
                    mejorSolicitud = solicitud;
                    indiceMejor = i;
                }
            }
        }
        
        // Si no hay solicitudes en la dirección actual, cambiar dirección
        if (mejorSolicitud == null) {
            direccionDerecha = !direccionDerecha;
            System.out.println("SCAN - Cambiando dirección a: " + (direccionDerecha ? "derecha" : "izquierda"));
            return obtenerSiguiente(); // Intentar nuevamente
        }
        
        // Eliminar y procesar la solicitud encontrada
        Object elemento = colaSolicitudes.get(indiceMejor);
        colaSolicitudes.remove(elemento);
        
        // Actualizar cabezal
        cabezalActual = mejorSolicitud.getBloqueSolicitado();
        
        System.out.println("SCAN - Procesando: " + mejorSolicitud + " (dirección: " + 
                          (direccionDerecha ? "derecha" : "izquierda") + ")");
        
        return mejorSolicitud;
    }
    
    @Override
    public ListaSimple getSolicitudesPendientes() {
        return colaSolicitudes;
    }
    
    @Override
    public String getNombrePolitica() {
        return "SCAN";
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