/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificador;

import Models.SolicitudDisco;
import edd.ListaSimple;

/**
 * Interfaz que todos los planificadores de disco deben implementar
 */
public interface PlanificadorDisco {
    
    /**
     * Agrega una solicitud a la cola del planificador
     */
    void agregarSolicitud(SolicitudDisco solicitud);
    
    /**
     * Obtiene la siguiente solicitud a procesar según la política
     */
    SolicitudDisco obtenerSiguiente();
    
    /**
     * Obtiene todas las solicitudes pendientes (para la GUI)
     */
    ListaSimple getSolicitudesPendientes();
    
    /**
     * Obtiene el nombre de la política
     */
    String getNombrePolitica();
    
    /**
     * Obtiene la posición actual del cabezal (para políticas que lo necesitan)
     */
    int getCabezalActual();
    
    /**
     * Establece la posición del cabezal
     */
    void setCabezalActual(int cabezal);
}