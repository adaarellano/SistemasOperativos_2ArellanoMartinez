/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;

import Managers.ManejadorArchivo;

/**
 * Prueba específica para la política SCAN (Elevator Algorithm)
 */
public class TestSCAN {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA ESPECÍFICA - POLÍTICA SCAN ===\n");
        
        ManejadorArchivo manejador = new ManejadorArchivo();
        manejador.cambiarPlanificador("SCAN");
        
        System.out.println("Planificador activo: " + manejador.getPlanificadorActual().getNombrePolitica());
        
        // Configuración del disco
        int cabezalInicial = 25;
        int bloquesDisco = 100;
        boolean direccionInicial = true; // true = derecha, false = izquierda
        
        System.out.println("Configuración SCAN:");
        System.out.println("  - Cabezal inicial: " + cabezalInicial);
        System.out.println("  - Bloques totales: " + bloquesDisco);
        System.out.println("  - Dirección inicial: " + (direccionInicial ? "DERECHA" : "IZQUIERDA"));
        
        // Solicitudes de prueba
        int[] solicitudes = {10, 40, 5, 60, 30, 75, 15};
        
        System.out.println("\nSolicitudes pendientes: ");
        for (int bloque : solicitudes) {
            System.out.println("  - Bloque " + bloque);
        }
        
        // Simular algoritmo SCAN
        System.out.println("\n--- EJECUCIÓN ALGORITMO SCAN ---");
        
        simularSCAN(cabezalInicial, bloquesDisco, direccionInicial, solicitudes);
        
        System.out.println("\n=== PRUEBA SCAN COMPLETADA ===");
    }
    
    private static void simularSCAN(int cabezal, int maxBloques, boolean direccion, int[] solicitudes) {
        System.out.println("Iniciando SCAN desde bloque " + cabezal + " hacia " + 
                          (direccion ? "DERECHA" : "IZQUIERDA"));
        
        boolean cambioDireccion = false;
        
        while (solicitudes.length > 0) {
            if (direccion) {
                // Buscar hacia la derecha
                Integer masCercanoDerecha = null;
                for (int bloque : solicitudes) {
                    if (bloque >= cabezal) {
                        if (masCercanoDerecha == null || bloque < masCercanoDerecha) {
                            masCercanoDerecha = bloque;
                        }
                    }
                }
                
                if (masCercanoDerecha != null) {
                    System.out.println("  → Procesando bloque " + masCercanoDerecha + " (derecha)");
                    cabezal = masCercanoDerecha;
                    solicitudes = removerSolicitud(solicitudes, masCercanoDerecha);
                } else {
                    // Cambiar dirección
                    System.out.println("  → Llegó al final, cambiando dirección a IZQUIERDA");
                    direccion = false;
                    cambioDireccion = true;
                }
            } else {
                // Buscar hacia la izquierda
                Integer masCercanoIzquierda = null;
                for (int bloque : solicitudes) {
                    if (bloque <= cabezal) {
                        if (masCercanoIzquierda == null || bloque > masCercanoIzquierda) {
                            masCercanoIzquierda = bloque;
                        }
                    }
                }
                
                if (masCercanoIzquierda != null) {
                    System.out.println("  ← Procesando bloque " + masCercanoIzquierda + " (izquierda)");
                    cabezal = masCercanoIzquierda;
                    solicitudes = removerSolicitud(solicitudes, masCercanoIzquierda);
                } else {
                    // Cambiar dirección
                    System.out.println("  ← Llegó al inicio, cambiando dirección a DERECHA");
                    direccion = true;
                    cambioDireccion = true;
                }
            }
            
            if (!cambioDireccion) {
                try { Thread.sleep(300); } catch (InterruptedException e) {}
            }
            cambioDireccion = false;
        }
    }
    
    private static int[] removerSolicitud(int[] array, int elemento) {
        // Simular remoción de solicitud procesada
        int count = 0;
        for (int value : array) {
            if (value != elemento) count++;
        }
        
        int[] result = new int[count];
        int index = 0;
        for (int value : array) {
            if (value != elemento) {
                result[index++] = value;
            }
        }
        return result;
    }
}