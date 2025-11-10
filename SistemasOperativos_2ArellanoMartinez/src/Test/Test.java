/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;

/**
 *
 * @author raiza
 */

import Managers.ManejadorArchivo;
import Models.Archivo;
import Models.Directorio;
import Models.Bloque;
import Models.Proceso;
import edd.ListaSimple;

/**
 * Clase de prueba para demostrar el funcionamiento del sistema de archivos
 * por consola
 */
public class Test {
    
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DEL SISTEMA DE ARCHIVOS ===\n");
        
        try {
            // 1. Crear el manejador de archivos
            ManejadorArchivo manejador = new ManejadorArchivo();
            
            // 2. Mostrar estado inicial del sistema
            mostrarEstadoSistema(manejador, "Estado inicial del sistema");
            
            // 3. Probar creación de archivos
            probarCreacionArchivos(manejador);
            
            // 4. Probar diferentes políticas de planificación
            probarPlanificadores(manejador);
            
            // 5. Probar operaciones CRUD
            probarOperacionesCRUD(manejador);
            
            // 6. Estado final del sistema
            mostrarEstadoSistema(manejador, "Estado final del sistema");
            
        } catch (Exception e) {
            System.err.println("Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== PRUEBA COMPLETADA ===");
    }
    
    private static void mostrarEstadoSistema(ManejadorArchivo manejador, String titulo) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(titulo);
        System.out.println("=".repeat(50));
        System.out.println(manejador.getEstadoSistema());
        System.out.println("=".repeat(50));
    }
    
    private static void probarCreacionArchivos(ManejadorArchivo manejador) {
        System.out.println("\n--- PROBANDO CREACIÓN DE ARCHIVOS ---");
        
        // Crear algunos archivos
        System.out.println("1. Creando archivo documento1.txt...");
        boolean exito1 = manejador.crearArchivo("/documento1.txt", 5, "admin");
        System.out.println("   Resultado: " + (exito1 ? "ÉXITO" : "FALLÓ"));
        
        System.out.println("2. Creando archivo imagen.jpg...");
        boolean exito2 = manejador.crearArchivo("/imagen.jpg", 10, "admin");
        System.out.println("   Resultado: " + (exito2 ? "ÉXITO" : "FALLÓ"));
        
        System.out.println("3. Creando archivo en subdirectorio...");
        boolean exito3 = manejador.crearArchivo("/docs/reporte.pdf", 8, "admin");
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
        
        // Mostrar archivos
        ListaSimple archivos = directorio.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            System.out.println(indent + "  📄 " + archivo.getNombre() + 
                             " (" + archivo.getTamañoBloques() + " bloques)");
        }
        
        // Mostrar subdirectorios recursivamente
        ListaSimple subdirs = directorio.getSubdirectorios();
        for (int i = 0; i < subdirs.getSize(); i++) {
            Directorio subdir = (Directorio) subdirs.get(i);
            mostrarEstructuraDirectorio(subdir, nivel + 1);
        }
    }
    
    private static void probarPlanificadores(ManejadorArchivo manejador) {
        System.out.println("\n--- PROBANDO PLANIFICADORES DE DISCO ---");
        
        String[] politicas = {"FIFO", "SSTF", "SCAN", "C-SCAN"};
        
        for (String politica : politicas) {
            System.out.println("\nCambiando a política: " + politica);
            manejador.cambiarPlanificador(politica);
            
            // Simular algunas operaciones con esta política
            System.out.println("  Planificador actual: " + manejador.getPlanificadorActual().getNombrePolitica());
        }
    }
    
    private static void probarOperacionesCRUD(ManejadorArchivo manejador) {
        System.out.println("\n--- PROBANDO OPERACIONES CRUD ---");
        
        // Leer archivo
        System.out.println("1. Leyendo archivo documento1.txt...");
        String contenido = manejador.leerArchivo("/documento1.txt", "admin");
        System.out.println("   Contenido: " + contenido);
        
        // Actualizar archivo
        System.out.println("2. Actualizando archivo documento1.txt...");
        boolean exito = manejador.actualizarArchivo("/documento1.txt", "Este es el nuevo contenido del archivo", "admin");
        System.out.println("   Resultado: " + (exito ? "ÉXITO" : "FALLÓ"));
        
        // Leer nuevamente para ver el cambio
        System.out.println("3. Leyendo archivo actualizado...");
        contenido = manejador.leerArchivo("/documento1.txt", "admin");
        System.out.println("   Nuevo contenido: " + contenido);
    }
}