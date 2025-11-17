/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import Models.Archivo;
import Models.Directorio;
import Models.Usuario; // Importar la clase Usuario
import edd.ListaSimple;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ManejadorArchivo {
    private Directorio raiz;
    private int archivosCreados;
    private int archivosEliminados;
    private int operacionesRealizadas;
    private int bloquesOcupados;
    private int capacidadTotal;
    private boolean esAdministrador;
    private String planificadorActual;
    private File directorioBase;
    
    // **NUEVO: Gestión de usuarios**
    private Usuario usuarioActual;
    private ListaSimple usuarios;
    
    public ManejadorArchivo() {
        this.raiz = new Directorio("/");
        this.archivosCreados = 0;
        this.archivosEliminados = 0;
        this.operacionesRealizadas = 0;
        this.bloquesOcupados = 0;
        this.capacidadTotal = 1000;
        this.esAdministrador = false;
        this.planificadorActual = "FIFO";
        
        // **NUEVO: Inicializar sistema de usuarios**
        this.usuarios = new ListaSimple();
        this.usuarioActual = new Usuario("usuario", "usuario"); // Usuario por defecto
        
        // Crear usuario administrador por defecto
        Usuario admin = new Usuario("admin", "admin");
        usuarios.insertFinal(admin);
        
        // Establecer directorio base para archivos reales
        String userHome = System.getProperty("user.home");
        this.directorioBase = new File(userHome + "/FileSystemSimulator");
        
        if (!directorioBase.exists()) {
            if (directorioBase.mkdirs()) {
                System.out.println("Directorio base creado: " + directorioBase.getAbsolutePath());
            }
        }
    }
    
    // **NUEVO: Métodos de gestión de usuarios**
    public boolean cambiarUsuario(String username) {
        for (int i = 0; i < usuarios.getSize(); i++) {
            Usuario usuario = (Usuario) usuarios.get(i);
            if (usuario.getUsername().equals(username)) {
                this.usuarioActual = usuario;
                this.esAdministrador = usuario.esAdministrador();
                return true;
            }
        }
        return false;
    }
    
    public boolean agregarUsuario(String username, String tipo) {
        // Solo administradores pueden agregar usuarios
        if (!esAdministrador) return false;
        
        // Verificar si ya existe
        for (int i = 0; i < usuarios.getSize(); i++) {
            Usuario usuario = (Usuario) usuarios.get(i);
            if (usuario.getUsername().equals(username)) {
                return false;
            }
        }
        
        Usuario nuevoUsuario = new Usuario(username, tipo);
        usuarios.insertFinal(nuevoUsuario);
        return true;
    }
    
    // **ACTUALIZADO: getUsuarioActual() ahora retorna el objeto Usuario**
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    // **MÉTODO COMPATIBILIDAD: Para EstadoSistema que espera String**
    public String getUsuarioActualNombre() {
        return usuarioActual.getUsername();
    }
    
    /**
     * Método alternativo con nombre diferente para evitar conflictos
     */
    public String getNombreUsuarioActual() {
        return esAdministrador ? "admin" : "usuario";
    }
    
    // **ACTUALIZADO: Métodos CRUD con verificación de permisos**
    public boolean crearArchivo(String nombre, int tamanoBloques, String usuario) {
        // Verificar permisos usando la clase Usuario
        if (!usuarioActual.puedeEscribir(null)) { // null porque es archivo nuevo
            if (!usuarioActual.esAdministrador()) {
                return false;
            }
        }
        
        if (bloquesOcupados + tamanoBloques > capacidadTotal) {
            return false;
        }
        
        try {
            // Crear archivo real
            File archivoReal = new File(directorioBase, nombre);
            if (archivoReal.exists()) {
                return false;
            }
            
            if (!archivoReal.createNewFile()) {
                return false;
            }
            
            // Crear archivo simulado
            Archivo archivo = new Archivo(nombre, tamanoBloques, usuarioActual.getUsername());
            archivo.setRutaReal(archivoReal.getAbsolutePath());
            
            if (raiz.agregarArchivo(archivo)) {
                archivosCreados++;
                operacionesRealizadas++;
                bloquesOcupados += tamanoBloques;
                return true;
            }
            
            return false;
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo real: " + e.getMessage());
            return false;
        }
    }
    
    public String leerArchivo(String nombre, String usuario) {
        // Buscar archivo primero
        Archivo archivo = raiz.buscarArchivoRecursivo(nombre);
        if (archivo == null) {
            return null;
        }
        
        // **NUEVO: Verificar permisos usando la clase Usuario**
        if (!usuarioActual.puedeLeer(archivo)) {
            System.err.println("Permiso denegado para leer: " + nombre);
            return null;
        }
        
        try {
            // Leer archivo real
            if (archivo.getRutaReal() == null) {
                return null;
            }
            
            File archivoReal = new File(archivo.getRutaReal());
            if (!archivoReal.exists()) {
                return null;
            }
            
            byte[] bytes = Files.readAllBytes(archivoReal.toPath());
            String contenido = new String(bytes, "UTF-8");
            
            operacionesRealizadas++;
            return contenido;
            
        } catch (IOException e) {
            System.err.println("Error al leer archivo: " + e.getMessage());
            return null;
        }
    }
    
    public boolean eliminarArchivo(String nombre, String usuario) {
        // Buscar archivo
        Archivo archivo = raiz.buscarArchivoRecursivo(nombre);
        if (archivo == null) {
            return false;
        }
        
        // **NUEVO: Verificar permisos**
        if (!usuarioActual.puedeEscribir(archivo)) {
            System.err.println("Permiso denegado para eliminar: " + nombre);
            return false;
        }
        
        // Eliminar archivo real
        String rutaReal = archivo.getRutaReal();
        if (rutaReal != null) {
            File archivoReal = new File(rutaReal);
            if (archivoReal.exists() && !archivoReal.delete()) {
                return false;
            }
        }
        
        // Eliminar del sistema simulado
        if (eliminarArchivoRecursivo(raiz, nombre)) {
            archivosEliminados++;
            operacionesRealizadas++;
            bloquesOcupados -= archivo.getTamañoBloques();
            return true;
        }
        
        return false;
    }
    
    // **ACTUALIZADO: cambiarModo ahora usa la clase Usuario**
    public void setEsAdministrador(boolean esAdmin) {
        this.esAdministrador = esAdmin;
        if (esAdmin) {
            this.usuarioActual = new Usuario("admin", "admin");
        } else {
            this.usuarioActual = new Usuario("usuario", "usuario");
        }
    }
    
    // **MÉTODO COMPATIBILIDAD: Para código existente**
    public String getPlanificadorActual() { 
        return planificadorActual; 
    }
    
    // ... (el resto de tus métodos permanecen igual)
    private boolean eliminarArchivoRecursivo(Directorio directorio, String nombreArchivo) {
        if (directorio.eliminarArchivo(nombreArchivo)) {
            return true;
        }
        
        ListaSimple subdirectorios = directorio.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            if (eliminarArchivoRecursivo(subdir, nombreArchivo)) {
                return true;
            }
        }
        return false;
    }
    
    // ... otros métodos existentes
}