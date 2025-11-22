/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 * Clase que representa un archivo en el sistema de archivos
 * Con gestión de bloques, permisos y metadatos
 */
public class Archivo {
    private String nombre;
    private String extension;
    private int tamañoBytes;           // Tamaño REAL del contenido
    private int tamañoBloques;         // Bloques USADOS realmente
    
    // *** CAMPO CLAVE FALTANTE: El inicio de la lista enlazada ***
    private Bloque primerBloque;       
    
    private String usuarioPropietario; // Compatibilidad con nombre de variable en Manejador
    private String permisos;
    public String fechaCreacion;
    private long fechaModificacionLong; // Para ordenamientos
    private String contenido;
    private String rutaCompleta;
    private String rutaReal; // Para gestión de archivos reales
    
    // NUEVOS CAMPOS PARA GESTIÓN CORRECTA
    private int tamañoReservadoBytes;  // Bytes reservados al crear (bloques × 1024)
    private int bloquesReservados;     // Bloques reservados al crear
    
    // Estados del archivo
    private boolean estaAbierto;
    private boolean esPublico;
    
    /**
     * Constructor principal usado por ManejadorArchivo
     */
    public Archivo(String nombre, int bloquesReservados, String usuarioPropietario, String rutaCompleta) {
        this.nombre = nombre;
        this.bloquesReservados = bloquesReservados;
        this.tamañoReservadoBytes = bloquesReservados * 1024; // Asumimos 1024 bytes por bloque
        
        // Inicialmente el archivo está vacío de contenido
        this.tamañoBytes = 0;
        this.tamañoBloques = 0; 
        
        this.usuarioPropietario = usuarioPropietario;
        this.rutaCompleta = rutaCompleta;
        this.permisos = "rw-r--r--";
        this.fechaCreacion = java.time.LocalDateTime.now().toString();
        this.fechaModificacionLong = System.currentTimeMillis();
        this.contenido = "";
        this.estaAbierto = false;
        this.esPublico = false;
        this.primerBloque = null;
        this.rutaReal = null;
        
        // Extraer extensión
        int puntoIndex = nombre.lastIndexOf('.');
        if (puntoIndex > 0 && puntoIndex < nombre.length() - 1) {
            this.extension = nombre.substring(puntoIndex + 1);
        } else {
            this.extension = "txt";
        }
    }
    
    /**
     * Constructor simplificado (compatibilidad)
     */
    public Archivo(String nombre, int bloquesReservados, String usuarioPropietario) {
        this(nombre, bloquesReservados, usuarioPropietario, "/" + nombre);
    }
    
    // ===== OPERACIONES DE CONTENIDO =====
    
    /**
     * Escribe contenido en el archivo y recalcula su tamaño real
     */
    public boolean escribirContenido(String nuevoContenido) {
        this.contenido = nuevoContenido;
        this.tamañoBytes = nuevoContenido.length(); // 1 char = 1 byte aprox para simulación
        
        // Calcular cuántos bloques ocupa realmente este contenido
        // Math.ceil(bytes / 1024)
        if (this.tamañoBytes == 0) {
            this.tamañoBloques = 0;
        } else {
            this.tamañoBloques = (int) Math.ceil((double) this.tamañoBytes / 1024);
        }
        
        this.fechaModificacionLong = System.currentTimeMillis();
        return true;
    }
    
    public String leerContenido() {
        return contenido;
    }
    
    // ===== GESTIÓN DE BLOQUES (LO QUE FALTABA) =====
    
    public Bloque getPrimerBloque() {
        return primerBloque;
    }

    public void setPrimerBloque(Bloque primerBloque) {
        this.primerBloque = primerBloque;
    }
    
    /**
     * Recorre la lista enlazada para generar un String (ej: "0 -> 5 -> 2")
     * Usado por el PanelOutput y ManejadorArchivo
     */
    public String getInfoBloques() {
        if (primerBloque == null) {
            return "Sin bloques asignados";
        }
        
        StringBuilder sb = new StringBuilder();
        Bloque actual = primerBloque;
        int contador = 0;
        
        while (actual != null && contador < 50) { // Límite por seguridad
            sb.append(actual.getIdBloque());
            if (actual.getSiguienteBloque() != null) {
                sb.append(" -> ");
            }
            actual = actual.getSiguienteBloque();
            contador++;
        }
        
        if (contador >= 50) {
            sb.append("...");
        }
        
        return sb.toString();
    }

    // ===== GETTERS Y SETTERS =====

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getTamañoBytes() { return tamañoBytes; } // Tamaño REAL
    public int getTamañoBloques() { return tamañoBloques; } // Bloques USADOS

    public int getBloquesReservados() { return bloquesReservados; } // Bloques TOTALES ASIGNADOS
    public int getTamañoReservadoBytes() { return tamañoReservadoBytes; }

    public String getUsuarioPropietario() { return usuarioPropietario; }
    // Alias para compatibilidad con código viejo que use getPropietario
    public String getPropietario() { return usuarioPropietario; }
    
    public String getRutaCompleta() { return rutaCompleta; }
    public void setRutaCompleta(String ruta) { this.rutaCompleta = ruta; }

    public String getRutaReal() { return rutaReal; }
    public void setRutaReal(String rutaReal) { this.rutaReal = rutaReal; }

    public boolean esPublico() { return esPublico; }
    public void setEsPublico(boolean esPublico) { this.esPublico = esPublico; }
    
    public String getFechaCreacion() { return fechaCreacion; }

    /**
     * Obtiene información detallada del archivo (Requerido por TestFIFO)
     */
    public String getInfoCompleta() {
        return String.format(
            "Archivo: %s\n" +
            "Ruta: %s\n" +
            "Tamaño REAL: %d bytes (%d bloques usados)\n" +
            "Espacio RESERVADO: %d bytes (%d bloques reservados)\n" +
            "Propietario: %s\n" +
            "Permisos: %s\n" +
            "Creado: %s\n" +
            "Bloques: %s",
            nombre, rutaCompleta, 
            tamañoBytes, tamañoBloques,
            tamañoReservadoBytes, bloquesReservados,
            usuarioPropietario, permisos,
            fechaCreacion,
            getInfoBloques()
        );
    }
    @Override
    public String toString() {
        return nombre + " (" + tamañoBloques + "/" + bloquesReservados + " blqs)";
    }
}