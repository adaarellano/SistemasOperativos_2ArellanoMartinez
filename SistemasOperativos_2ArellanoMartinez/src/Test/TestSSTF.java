/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;


import Managers.ManejadorArchivo;
import Models.SolicitudDisco;
import Models.Proceso;
import Planificador.SSTF;
import edd.ListaSimple;


/**
 * Prueba específica para la política SSTF con salida detallada
 */
public class TestSSTF {
    
   
    public static void main(String[] args) {
        System.out.println("=== PRUEBA REAL SSTF CON SOLICITUDES VERDADERAS ===\n");
        
        // Crear planificador SSTF directamente
        SSTF planificador = new SSTF();
        
        System.out.println("Inicializando SSTF con cabezal en 30");
        planificador.setCabezalActual(30);
        
        // CREAR SOLICITUDES REALES
        System.out.println("\n--- CREANDO SOLICITUDES REALES DE DISCO ---");
        
        // Necesitamos procesos para crear solicitudes (pueden ser null para prueba)
        Proceso proceso1 = new Proceso("READ", "/archivo1.txt", "admin");
        Proceso proceso2 = new Proceso("WRITE", "/archivo2.txt", "admin");
        Proceso proceso3 = new Proceso("READ", "/archivo3.txt", "admin");
        Proceso proceso4 = new Proceso("WRITE", "/archivo4.txt", "admin");
        Proceso proceso5 = new Proceso("READ", "/archivo5.txt", "admin");
        
        // Crear solicitudes de disco REALES
        SolicitudDisco solicitud1 = new SolicitudDisco(proceso1, "READ", 25);
        SolicitudDisco solicitud2 = new SolicitudDisco(proceso2, "WRITE", 10);
        SolicitudDisco solicitud3 = new SolicitudDisco(proceso3, "READ", 50);
        SolicitudDisco solicitud4 = new SolicitudDisco(proceso4, "WRITE", 5);
        SolicitudDisco solicitud5 = new SolicitudDisco(proceso5, "READ", 35);
        
        // Agregar solicitudes al planificador
        System.out.println("Agregando solicitudes al planificador SSTF:");
        System.out.println("1. " + solicitud1);
        planificador.agregarSolicitud(solicitud1);
        
        System.out.println("2. " + solicitud2);
        planificador.agregarSolicitud(solicitud2);
        
        System.out.println("3. " + solicitud3);
        planificador.agregarSolicitud(solicitud3);
        
        System.out.println("4. " + solicitud4);
        planificador.agregarSolicitud(solicitud4);
        
        System.out.println("5. " + solicitud5);
        planificador.agregarSolicitud(solicitud5);
        
        System.out.println("\nTotal solicitudes en cola: " + planificador.getSolicitudesPendientes().getSize());
        
        // PROCESAR SOLICITUDES REALES
        System.out.println("\n--- PROCESANDO SOLICITUDES (SSTF REAL) ---");
        
        int solicitudCount = 1;
        while (planificador.getSolicitudesPendientes().getSize() > 0) {
            System.out.println("\n>>> SOLICITUD " + solicitudCount + ":");
            
            SolicitudDisco siguiente = planificador.obtenerSiguiente();
            
            if (siguiente != null) {
                System.out.println("PROCESADA: " + siguiente.getTipoOperacion() + 
                                 " bloque " + siguiente.getBloqueSolicitado());
                System.out.println("Cabezal ahora en: " + planificador.getCabezalActual());
                System.out.println("Solicitudes restantes: " + planificador.getSolicitudesPendientes().getSize());
            }
            
            solicitudCount++;
            
            // Pequeña pausa para ver el proceso
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        
        System.out.println("\n--- RESUMEN FINAL ---");
        System.out.println("Todas las solicitudes procesadas");
        System.out.println("Posicion final del cabezal: " + planificador.getCabezalActual());
        System.out.println("Total solicitudes procesadas: " + (solicitudCount - 1));
        
        System.out.println("\n=== PRUEBA REAL SSTF COMPLETADA ===");
    }
}