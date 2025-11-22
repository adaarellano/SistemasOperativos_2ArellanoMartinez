/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificador;

import Models.SolicitudDisco;
import edd.ListaSimple;
import MainGUI.PanelConsola;

public class SSTF implements PlanificadorDisco {
    private ListaSimple colaSolicitudes;
    private int cabezalActual;
    private PanelConsola consola;  // ← Usamos ESTA referencia para logs
    
    public SSTF() {
        this.colaSolicitudes = new ListaSimple();
        this.cabezalActual = 0;
    }
    
    public SSTF(PanelConsola consola) {
        this();
        this.consola = consola;  // ← Recibimos la consola real de la interfaz
    }
    
    @Override
    public void agregarSolicitud(SolicitudDisco solicitud) {
        colaSolicitudes.insertFinal(solicitud);
        if (consola != null) {  // ← Verificamos si tenemos consola
            consola.agregarLinea("SSTF - Solicitud agregada: " + solicitud.getTipoOperacion() + 
                               " bloque " + solicitud.getBloqueSolicitado());
        }
    }
    
@Override
    public SolicitudDisco obtenerSiguiente() {
        if (colaSolicitudes.isEmpty()) {
            // ELIMINADO: El log de "No hay solicitudes" para evitar spam
            return null;
        }
        
        // SI TENEMOS CONSOLA, MOSTRAMOS DETALLES DE LA DECISIÓN
        if (consola != null) {
            consola.agregarLinea("\n=== SSTF - BUSCANDO SIGUIENTE SOLICITUD ===");
            consola.agregarLinea("Cabezal actual: " + cabezalActual);
        }
        
        SolicitudDisco masCercana = null;
        int distanciaMinima = Integer.MAX_VALUE;
        int indiceMasCercano = -1;
        
        // Buscar la solicitud más cercana
        for (int i = 0; i < colaSolicitudes.getSize(); i++) {
            SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
            int bloque = solicitud.getBloqueSolicitado();
            int distancia = Math.abs(bloque - cabezalActual);
            
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercana = solicitud;
                indiceMasCercano = i;
            }
        }
        
        if (masCercana != null) {
            // Eliminar y procesar
            Object elemento = colaSolicitudes.get(indiceMasCercano);
            colaSolicitudes.remove(elemento);
            
            int nuevoCabezal = masCercana.getBloqueSolicitado();
            
            if (consola != null) {
                consola.agregarLinea("SSTF eligió: Bloque " + nuevoCabezal + 
                                   " (Distancia: " + distanciaMinima + ")");
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
        if (consola != null) {
            consola.agregarLinea("SSTF - Cabezal movido a: " + cabezal);
        }
        this.cabezalActual = cabezal; 
    }
}