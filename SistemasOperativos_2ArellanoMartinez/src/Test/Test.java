/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;

import Managers.ManejadorArchivo;
import Models.Archivo;
import Models.Directorio;
import Models.Bloque;
import Models.Proceso;
import edd.ListaSimple;

/**
 * Clase de prueba para demostrar el funcionamiento del sistema de archivos
 * por consola - VERSIÓN ACTUALIZADA
 */
public class Test {
    
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DEL SISTEMA DE ARCHIVOS (VERSIÓN MEJORADA) ===\n");
        
        try {
            // 1. Crear el manejador de archivos
            ManejadorArchivo manejador = new ManejadorArchivo();
            
            // 2. Mostrar estado inicial del sistema
            mostrarEstadoSistema(manejador, "Estado inicial del sistema");
            
            // 3. Probar creación de archivos con nueva gestión de tamaño
            probarCreacionArchivos(manejador);
            
            // 4. Probar operaciones CRUD con nueva información de tamaño
            probarOperacionesCRUD(manejador);
            
            // 5. Mostrar información detallada de archivos
            mostrarInfoDetalladaArchivos(manejador);
            
            // 6. Estado final del sistema
            mostrarEstadoSistema(manejador, "Estado final del sistema");
            
        } catch (Exception e) {
            System.err.println("Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== PRUEBA COMPLETADA ===");
    }
    
    private static void mostrarEstadoSistema(ManejadorArchivo manejador, String titulo) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(titulo);
        System.out.println("=".repeat(60));
        System.out.println(manejador.getEstadoSistema());
        System.out.println("=".repeat(60));
    }
    
    private static void probarCreacionArchivos(ManejadorArchivo manejador) {
        System.out.println("\n--- PROBANDO CREACIÓN DE ARCHIVOS (NUEVA GESTIÓN DE TAMAÑO) ---");
        
        // Crear archivos con diferentes tamaños reservados
        System.out.println("1. Creando archivo pequeño (2 bloques reservados)...");
        boolean exito1 = manejador.crearArchivo("/pequeno.txt", 2, "admin");
        System.out.println("   Resultado: " + (exito1 ? "ÉXITO" : "FALLÓ"));
        
        System.out.println("2. Creando archivo mediano (5 bloques reservados)...");
        boolean exito2 = manejador.crearArchivo("/mediano.txt", 5, "admin");
        System.out.println("   Resultado: " + (exito2 ? "ÉXITO" : "FALLÓ"));
        
        System.out.println("3. Creando archivo grande (10 bloques reservados)...");
        boolean exito3 = manejador.crearArchivo("/grande.dat", 10, "admin");
        System.out.println("   Resultado: " + (exito3 ? "ÉXITO" : "FALLÓ"));
        
        // Mostrar estructura de directorios
        System.out.println("\n--- ESTRUCTURA DE DIRECTORIOS ---");
        mostrarEstructuraDirectorio(manejador.getRaiz(), 0);
    }
    
    private static void mostrarEstructuraDirectorio(Directorio directorio, int nivel) {
        String indent = "  ".repeat(nivel);
        
        System.out.println(indent + "📁 " + directorio.getNombre() + 
                         " (archivos: " + directorio.getArchivos().getSize() + 
                         ", subdirs: " + directorio.getSubdirectorios().getSize() + ")");
        
        // Mostrar archivos con nueva información
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            System.out.println(indent + "  📄 " + archivo.getNombre() + 
                             " (" + archivo.getTamañoBloques() + "/" + archivo.getBloquesReservados() + 
                             " bloques - " + archivo.getTamañoBytes() + "/" + archivo.getTamañoReservadoBytes() + " bytes)");
        }
        
        // Mostrar subdirectorios recursivamente
        ListaSimple subdirs = directorio.getSubdirectorios();
        for (int i = 0; i < subdirs.getSize(); i++) {
            Directorio subdir = (Directorio) subdirs.get(i);
            mostrarEstructuraDirectorio(subdir, nivel + 1);
        }
    }
    
    private static void probarOperacionesCRUD(ManejadorArchivo manejador) {
        System.out.println("\n--- PROBANDO OPERACIONES CRUD (NUEVA INFORMACIÓN DE TAMAÑO) ---");
        
        // Leer archivo (inicialmente vacío)
        System.out.println("1. Leyendo archivo mediano.txt (inicialmente vacío)...");
        String contenido = manejador.leerArchivo("/mediano.txt", "admin");
        System.out.println("   Contenido: '" + contenido + "'");
        
        // Actualizar archivo con contenido pequeño
        System.out.println("2. Escribiendo contenido pequeño en mediano.txt...");
        String contenidoPequeno = "Hola Mundo!";
        boolean exito = manejador.actualizarArchivo("/mediano.txt", contenidoPequeno, "admin");
        System.out.println("   Resultado: " + (exito ? "ÉXITO" : "FALLÓ"));
        
        // Leer nuevamente para ver el cambio
        System.out.println("3. Leyendo archivo actualizado...");
        contenido = manejador.leerArchivo("/mediano.txt", "admin");
        System.out.println("   Nuevo contenido: '" + contenido + "'");
        
        // Actualizar con contenido más grande
        System.out.println("4. Escribiendo contenido grande en mediano.txt...");
        String contenidoGrande = "Este es un contenido mucho más largo que ocupa más espacio en el archivo. " +
                                "Estamos probando la nueva gestión de tamaño que separa espacio reservado vs usado.";
        exito = manejador.actualizarArchivo("/mediano.txt", contenidoGrande, "admin");
        System.out.println("   Resultado: " + (exito ? "ÉXITO" : "FALLÓ"));
        
        // Leer el contenido grande
        System.out.println("5. Leyendo archivo con contenido grande...");
        contenido = manejador.leerArchivo("/mediano.txt", "admin");
        System.out.println("   Contenido largo: '" + contenido.substring(0, 50) + "...'");
    }
    
    private static void mostrarInfoDetalladaArchivos(ManejadorArchivo manejador) {
        System.out.println("\n--- INFORMACIÓN DETALLADA DE ARCHIVOS ---");
        
        // Obtener archivos del directorio raíz
        ListaSimple archivos = manejador.getRaiz().getArchivos();
        
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            System.out.println("\n" + "─".repeat(50));
            System.out.println("INFORMACIÓN COMPLETA DE: " + archivo.getNombre());
            System.out.println("─".repeat(50));
            System.out.println(archivo.getInfoCompleta());
            
            // Mostrar estadísticas adicionales
            System.out.println("\nESTADÍSTICAS:");
            System.out.println("  • Porcentaje de uso: " + 
                String.format("%.1f%%", (archivo.getTamañoBytes() / (double) archivo.getTamañoReservadoBytes()) * 100));
            System.out.println("  • Eficiencia de bloques: " + 
                String.format("%.1f%%", (archivo.getTamañoBloques() / (double) archivo.getBloquesReservados()) * 100));
            System.out.println("  • Bytes por bloque: " + 
                (archivo.getTamañoBloques() > 0 ? 
                 String.format("%.1f", archivo.getTamañoBytes() / (double) archivo.getTamañoBloques()) : "N/A"));
        }
    }
    
    private static void probarPlanificadores(ManejadorArchivo manejador) {
        System.out.println("\n--- PROBANDO PLANIFICADORES DE DISCO ---");
        
        String[] politicas = {"FIFO", "SSTF", "SCAN", "C-SCAN"};
        
        for (String politica : politicas) {
            System.out.println("\nCambiando a política: " + politica);
            manejador.cambiarPlanificador(politica);
            System.out.println("  Planificador actual: " + manejador.getPlanificadorActual().getNombrePolitica());
        }
        
        // Volver a FIFO para continuar
        manejador.cambiarPlanificador("FIFO");
    }
}