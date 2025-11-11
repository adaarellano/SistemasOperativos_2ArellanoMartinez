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
    private Bloque primerBloque;
    private String usuarioPropietario;
    private String permisos;
    private long fechaCreacion;
    private long fechaModificacion;
    private String contenido;
    private String rutaCompleta;
    
    // NUEVOS CAMPOS PARA GESTIÓN CORRECTA
    private int tamañoReservadoBytes;  // Bytes reservados al crear (bloques × 1024)
    private int bloquesReservados;     // Bloques reservados al crear
    
    // Estados del archivo
    private boolean estaAbierto;
    private boolean esPublico;
    
    /**
     * Constructor principal para crear archivos
     */
    public Archivo(String nombre, int tamañoBloques, String usuarioPropietario, String rutaCompleta) {
        this.nombre = nombre;
        this.bloquesReservados = tamañoBloques;
        this.tamañoReservadoBytes = tamañoBloques * 1024; // Reserva inicial
        
        // Inicialmente el archivo está vacío
        this.tamañoBytes = 0;
        this.tamañoBloques = 0;
        
        this.usuarioPropietario = usuarioPropietario;
        this.rutaCompleta = rutaCompleta;
        this.permisos = "rw-r--r--";
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaModificacion = this.fechaCreacion;
        this.contenido = "";
        this.estaAbierto = false;
        this.esPublico = false;
        this.primerBloque = null;
        
        // Extraer extensión si existe
        int puntoIndex = nombre.lastIndexOf('.');
        if (puntoIndex > 0 && puntoIndex < nombre.length() - 1) {
            this.extension = nombre.substring(puntoIndex + 1);
        } else {
            this.extension = "txt"; // Extensión por defecto
        }
        
        System.out.println("Archivo creado: " + nombre + " - Reservados: " + 
                          bloquesReservados + " bloques (" + tamañoReservadoBytes + " bytes)");
    }
    
    /**
     * Constructor simplificado para archivos temporales
     */
    public Archivo(String nombre, int tamañoBloques, String usuarioPropietario) {
        this(nombre, tamañoBloques, usuarioPropietario, "/" + nombre);
    }
    
    // ===== OPERACIONES DE CONTENIDO =====
    
    /**
     * Escribe contenido en el archivo
     */
    public boolean escribirContenido(String nuevoContenido) {
        if (!tienePermisoEscritura()) {
            System.err.println("Sin permisos de escritura para: " + nombre);
            return false;
        }
        
        this.contenido = nuevoContenido;
        this.tamañoBytes = nuevoContenido.length();
        this.tamañoBloques = calcularBloquesNecesarios(tamañoBytes);
        this.fechaModificacion = System.currentTimeMillis();
        
        System.out.println("Contenido escrito en: " + nombre + 
                          " - Real: " + tamañoBytes + " bytes (" + tamañoBloques + " bloques usados)" +
                          " - Reservado: " + tamañoReservadoBytes + " bytes (" + bloquesReservados + " bloques)");
        return true;
    }
    
    /**
     * Añade contenido al final del archivo
     */
    public boolean añadirContenido(String contenidoAdicional) {
        if (!tienePermisoEscritura()) {
            System.err.println("Sin permisos de escritura para: " + nombre);
            return false;
        }
        
        this.contenido += contenidoAdicional;
        this.tamañoBytes = this.contenido.length();
        this.tamañoBloques = calcularBloquesNecesarios(tamañoBytes);
        this.fechaModificacion = System.currentTimeMillis();
        
        System.out.println("Contenido añadido a: " + nombre + 
                          " - Real: " + tamañoBytes + " bytes (" + tamañoBloques + " bloques usados)");
        return true;
    }
    
    /**
     * Lee el contenido del archivo
     */
    public String leerContenido() {
        if (!tienePermisoLectura()) {
            System.err.println("Sin permisos de lectura para: " + nombre);
            return "Acceso denegado";
        }
        
        System.out.println("Leyendo archivo: " + nombre + 
                          " - Real: " + tamañoBytes + " bytes (" + tamañoBloques + " bloques usados)" +
                          " - Reservado: " + tamañoReservadoBytes + " bytes (" + bloquesReservados + " bloques)");
        return contenido;
    }
    
    /**
     * Limpia el contenido del archivo
     */
    public boolean limpiarContenido() {
        if (!tienePermisoEscritura()) {
            return false;
        }
        
        this.contenido = "";
        this.tamañoBytes = 0;
        this.tamañoBloques = 0;
        this.fechaModificacion = System.currentTimeMillis();
        
        System.out.println("Contenido limpiado: " + nombre + " (archivo vacío)");
        return true;
    }
    
    /**
     * Calcula cuántos bloques se necesitan para X bytes
     */
    private int calcularBloquesNecesarios(int bytes) {
        if (bytes == 0) return 0;
        return (int) Math.ceil((double) bytes / 1024);
    }
    
    // ===== GETTERS Y SETTERS =====
    
    public String getNombre() { 
        return nombre; 
    }
    
    public String getExtension() { 
        return extension; 
    }
    
    public int getTamañoBytes() { 
        return tamañoBytes; // Devuelve tamaño REAL
    }
    
    public int getTamañoBloques() { 
        return tamañoBloques; // Devuelve bloques USADOS
    }
    
    public Bloque getPrimerBloque() { 
        return primerBloque; 
    }
    
    public void setPrimerBloque(Bloque primerBloque) {
        this.primerBloque = primerBloque;
    }
    
    public String getUsuarioPropietario() { 
        return usuarioPropietario; 
    }
    
    public void setUsuarioPropietario(String usuarioPropietario) { 
        this.usuarioPropietario = usuarioPropietario; 
        this.fechaModificacion = System.currentTimeMillis();
    }
    
    public String getPermisos() { 
        return permisos; 
    }
    
    public long getFechaCreacion() { 
        return fechaCreacion; 
    }
    
    public long getFechaModificacion() { 
        return fechaModificacion; 
    }
    
    public String getContenido() { 
        return contenido; 
    }
    
    public String getRutaCompleta() { 
        return rutaCompleta; 
    }
    
    public void setRutaCompleta(String rutaCompleta) { 
        this.rutaCompleta = rutaCompleta; 
    }
    
    public boolean estaAbierto() { 
        return estaAbierto; 
    }
    
    public boolean esPublico() { 
        return esPublico; 
    }
    
    // NUEVOS GETTERS PARA INFORMACIÓN DE RESERVA
    public int getTamañoReservadoBytes() {
        return tamañoReservadoBytes;
    }
    
    public int getBloquesReservados() {
        return bloquesReservados;
    }
    
    public int getEspacioLibreBytes() {
        return tamañoReservadoBytes - tamañoBytes;
    }
    
    public int getBloquesLibres() {
        return bloquesReservados - tamañoBloques;
    }
    
    // ===== MÉTODOS DE INFORMACIÓN CORREGIDOS =====
    
    /**
     * Obtiene información detallada del archivo
     */
    public String getInfoCompleta() {
        return String.format(
            "Archivo: %s\n" +
            "Ruta: %s\n" +
            "Tamaño REAL: %d bytes (%d bloques usados)\n" +
            "Espacio RESERVADO: %d bytes (%d bloques reservados)\n" +
            "Espacio LIBRE: %d bytes (%d bloques libres)\n" +
            "Propietario: %s\n" +
            "Permisos: %s\n" +
            "Creado: %s\n" +
            "Modificado: %s\n" +
            "Estado: %s\n" +
            "Bloques: %s",
            nombre, rutaCompleta, 
            tamañoBytes, tamañoBloques,
            tamañoReservadoBytes, bloquesReservados,
            getEspacioLibreBytes(), getBloquesLibres(),
            usuarioPropietario, permisos,
            new java.util.Date(fechaCreacion),
            new java.util.Date(fechaModificacion),
            estaAbierto ? "Abierto" : "Cerrado",
            getInfoBloques()
        );
    }
    
    /**
     * Obtiene información resumida para tablas
     */
    public String getInfoResumida() {
        return String.format("%s | %d/%d bloques | %d/%d bytes | %s", 
            nombre, tamañoBloques, bloquesReservados,
            tamañoBytes, tamañoReservadoBytes,
            usuarioPropietario);
    }
    
    // ===== GESTIÓN DE BLOQUES =====
    
    /**
     * Obtiene información de la cadena de bloques
     */
    public String getInfoBloques() {
        if (primerBloque == null) {
            return "Sin bloques asignados";
        }
        
        StringBuilder sb = new StringBuilder();
        Bloque actual = primerBloque;
        int contador = 0;
        
        sb.append("Cadena de bloques: ");
        while (actual != null && contador < 20) {
            sb.append(actual.getIdBloque());
            if (actual.getSiguienteBloque() != null) {
                sb.append(" -> ");
            }
            actual = actual.getSiguienteBloque();
            contador++;
        }
        
        if (contador >= 20) {
            sb.append("... (cadena muy larga)");
        }
        
        return sb.toString();
    }
    
    /**
     * Calcula cuántos bloques están realmente asignados
     */
    public int getBloquesAsignados() {
        if (primerBloque == null) {
            return 0;
        }
        
        int contador = 0;
        Bloque actual = primerBloque;
        
        while (actual != null && contador < 1000) {
            contador++;
            actual = actual.getSiguienteBloque();
        }
        
        return contador;
    }
    
    // ===== GESTIÓN DE PERMISOS =====
    
    /**
     * Verifica si el usuario actual tiene permisos de lectura
     */
    public boolean tienePermisoLectura() {
        return true; // Simplificado por ahora
    }
    
    /**
     * Verifica si el usuario actual tiene permisos de escritura
     */
    public boolean tienePermisoEscritura() {
        return true; // Simplificado por ahora
    }
    
    // ... (otros métodos como abrir, cerrar, renombrar, etc.)
    
    @Override
    public String toString() {
        return String.format("Archivo{nombre='%s', real=%d/%d bytes, bloques=%d/%d}", 
                nombre, tamañoBytes, tamañoReservadoBytes, tamañoBloques, bloquesReservados);
    }
    
    /**
     * Representación para el JTree
     */
    public String paraArbol() {
        return nombre + " (" + tamañoBloques + "/" + bloquesReservados + " bloques)";
    }
}