/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;

import Managers.ManejadorArchivo;

/**
 * Prueba específica para la política C-SCAN (Circular SCAN)
 */
public class TestCSCAN {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA ESPECÍFICA - POLÍTICA C-SCAN ===\n");
        
        ManejadorArchivo manejador = new ManejadorArchivo();
        manejador.cambiarPlanificador("C-SCAN");
        
        System.out.println("Planificador activo: " + manejador.getPlanificadorActual().getNombrePolitica());
        
        // Configuración del disco
        int cabezalInicial = 25;
        int bloquesDisco = 100;
        
        System.out.println("Configuración C-SCAN:");
        System.out.println("  - Cabezal inicial: " + cabezalInicial);
        System.out.println("  - Bloques totales: 0-" + (bloquesDisco - 1));
        System.out.println("  - Dirección: SIEMPRE ASCENDENTE (circular)");
        
        // Solicitudes de prueba
        int[] solicitudes = {10, 40, 5, 60, 30, 95, 15, 80};
        
        System.out.println("\nSolicitudes pendientes: ");
        for (int bloque : solicitudes) {
            System.out.println("  - Bloque " + bloque);
        }
        
        // Simular algoritmo C-SCAN
        System.out.println("\n--- EJECUCIÓN ALGORITMO C-SCAN ---");
        
        simularCSCAN(cabezalInicial, bloquesDisco, solicitudes);
        
        System.out.println("\n=== PRUEBA C-SCAN COMPLETADA ===");
    }
    
    private static void simularCSCAN(int cabezal, int maxBloques, int[] solicitudes) {
        System.out.println("Iniciando C-SCAN desde bloque " + cabezal);
        System.out.println("Comportamiento circular: al llegar al final, vuelve al inicio");
        
        while (solicitudes.length > 0) {
            // Buscar la solicitud más cercana en dirección ascendente
            Integer masCercanoAdelante = null;
            for (int bloque : solicitudes) {
                if (bloque >= cabezal) {
                    if (masCercanoAdelante == null || bloque < masCercanoAdelante) {
                        masCercanoAdelante = bloque;
                    }
                }
            }
            
            if (masCercanoAdelante != null) {
                System.out.println("  ↑ Procesando bloque " + masCercanoAdelante + " (ascendente)");
                cabezal = masCercanoAdelante;
                solicitudes = removerSolicitud(solicitudes, masCercanoAdelante);
            } else {
                // Volver al inicio (comportamiento circular)
                System.out.println("  ↻ Llegó al final, volviendo al bloque 0");
                cabezal = 0;
                
                // Procesar la solicitud más pequeña
                Integer masPequeno = null;
                for (int bloque : solicitudes) {
                    if (masPequeno == null || bloque < masPequeno) {
                        masPequeno = bloque;
                    }
                }
                
                if (masPequeno != null) {
                    System.out.println("  ↑ Procesando bloque " + masPequeno + " (desde inicio)");
                    cabezal = masPequeno;
                    solicitudes = removerSolicitud(solicitudes, masPequeno);
                }
            }
            
            try { Thread.sleep(300); } catch (InterruptedException e) {}
        }
    }
    
    private static int[] removerSolicitud(int[] array, int elemento) {
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