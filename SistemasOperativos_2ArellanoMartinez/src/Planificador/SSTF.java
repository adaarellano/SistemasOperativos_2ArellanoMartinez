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
            if (consola != null) {
                consola.agregarLinea("SSTF - No hay solicitudes pendientes");
            }
            return null;
        }
        
        // SI TENEMOS CONSOLA, MOSTRAMOS DETALLES
        if (consola != null) {
            consola.agregarLinea("\n=== SSTF - BUSCANDO SIGUIENTE SOLICITUD ===");
            consola.agregarLinea("Cabezal actual: " + cabezalActual);
            consola.agregarLinea("Solicitudes pendientes: " + colaSolicitudes.getSize());
            consola.agregarLinea("--- CALCULANDO DISTANCIAS ---");
        }
        
        SolicitudDisco masCercana = null;
        int distanciaMinima = Integer.MAX_VALUE;
        int indiceMasCercano = -1;
        
        // Buscar la solicitud más cercana
        for (int i = 0; i < colaSolicitudes.getSize(); i++) {
            SolicitudDisco solicitud = (SolicitudDisco) colaSolicitudes.get(i);
            int bloque = solicitud.getBloqueSolicitado();
            int distancia = Math.abs(bloque - cabezalActual);
            
            // MOSTRAR CÁLCULO EN CONSOLA
            if (consola != null) {
                consola.agregarLinea("  Bloque " + bloque + " - Distancia: " + distancia + 
                                   " (|" + cabezalActual + " - " + bloque + "|)");
            }
            
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercana = solicitud;
                indiceMasCercano = i;
                
                if (consola != null) {
                    consola.agregarLinea("    -> NUEVA MAS CERCANA: Bloque " + bloque + 
                                       " (distancia: " + distancia + ")");
                }
            }
        }
        
        if (masCercana != null) {
            // Eliminar y procesar
            Object elemento = colaSolicitudes.get(indiceMasCercano);
            colaSolicitudes.remove(elemento);
            
            int nuevoCabezal = masCercana.getBloqueSolicitado();
            int distanciaRecorrida = Math.abs(nuevoCabezal - cabezalActual);
            
            // MOSTRAR RESULTADO EN CONSOLA
            if (consola != null) {
                consola.agregarLinea("--- RESULTADO SSTF ---");
                consola.agregarLinea("Solicitud seleccionada: " + masCercana.getTipoOperacion() + 
                                   " bloque " + nuevoCabezal);
                consola.agregarLinea("Distancia recorrida: " + distanciaRecorrida);
                consola.agregarLinea("Cabezal: " + cabezalActual + " -> " + nuevoCabezal);
                consola.agregarLinea("Solicitudes restantes: " + colaSolicitudes.getSize());
                consola.agregarLinea("=== FIN DECISION SSTF ===\n");
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