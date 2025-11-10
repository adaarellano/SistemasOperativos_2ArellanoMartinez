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
    private int tamañoBytes;
    private int tamañoBloques;
    private Bloque primerBloque;
    private String usuarioPropietario;
    private String permisos;
    private long fechaCreacion;
    private long fechaModificacion;
    private String contenido;
    private String rutaCompleta;
    
    // Estados del archivo
    private boolean estaAbierto;
    private boolean esPublico;
    
    /**
     * Constructor principal para crear archivos
     */
    public Archivo(String nombre, int tamañoBloques, String usuarioPropietario, String rutaCompleta) {
        this.nombre = nombre;
        this.tamañoBloques = tamañoBloques;
        this.tamañoBytes = tamañoBloques * 1024; // Asumimos 1KB por bloque
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
        this.fechaModificacion = System.currentTimeMillis();
        this.tamañoBytes = nuevoContenido.length();
        this.tamañoBloques = (int) Math.ceil((double) tamañoBytes / 1024);
        
        System.out.println("Contenido escrito en: " + nombre + " (" + tamañoBytes + " bytes)");
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
        this.fechaModificacion = System.currentTimeMillis();
        this.tamañoBytes = this.contenido.length();
        this.tamañoBloques = (int) Math.ceil((double) tamañoBytes / 1024);
        
        System.out.println("Contenido añadido a: " + nombre + " (" + tamañoBytes + " bytes)");
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
        
        System.out.println("Leyendo archivo: " + nombre + " (" + tamañoBytes + " bytes)");
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
        this.tamañoBloques = 1; // Mínimo 1 bloque
        this.fechaModificacion = System.currentTimeMillis();
        
        System.out.println("Contenido limpiado: " + nombre);
        return true;
    }
    
    // ===== GESTIÓN DE BLOQUES =====
    
    /**
     * Asigna el primer bloque de la cadena de bloques
     */
    public void setPrimerBloque(Bloque bloque) {
        this.primerBloque = bloque;
        if (bloque != null) {
            bloque.setOcupadoPor(this.nombre);
        }
    }
    
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
        while (actual != null && contador < 20) { // Límite para evitar ciclos infinitos
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
        
        while (actual != null && contador < 1000) { // Límite de seguridad
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
        // En un sistema real, aquí verificaríamos los permisos del archivo
        // vs el usuario actual del sistema
        return true; // Simplificado por ahora
    }
    
    /**
     * Verifica si el usuario actual tiene permisos de escritura
     */
    public boolean tienePermisoEscritura() {
        // En un sistema real, verificar permisos reales
        return true; // Simplificado por ahora
    }
    
    /**
     * Cambia los permisos del archivo (solo admin)
     */
    public boolean cambiarPermisos(String nuevosPermisos, String usuarioSolicitante) {
        if (!usuarioSolicitante.equals("admin") && !usuarioSolicitante.equals(usuarioPropietario)) {
            System.err.println("Solo admin o propietario puede cambiar permisos");
            return false;
        }
        
        this.permisos = nuevosPermisos;
        this.fechaModificacion = System.currentTimeMillis();
        System.out.println("Permisos cambiados a '" + nuevosPermisos + "' para: " + nombre);
        return true;
    }
    
    /**
     * Hace el archivo público o privado
     */
    public void setPublico(boolean esPublico) {
        this.esPublico = esPublico;
        if (esPublico) {
            this.permisos = "rw-r--r--";
        } else {
            this.permisos = "rw-------";
        }
    }
    
    // ===== OPERACIONES DE ESTADO =====
    
    /**
     * Abre el archivo para operaciones
     */
    public boolean abrir() {
        if (estaAbierto) {
            System.out.println("El archivo ya está abierto: " + nombre);
            return false;
        }
        
        estaAbierto = true;
        System.out.println("Archivo abierto: " + nombre);
        return true;
    }
    
    /**
     * Cierra el archivo
     */
    public boolean cerrar() {
        if (!estaAbierto) {
            System.out.println("El archivo ya está cerrado: " + nombre);
            return false;
        }
        
        estaAbierto = false;
        System.out.println("Archivo cerrado: " + nombre);
        return true;
    }
    
    /**
     * Renombra el archivo
     */
    public boolean renombrar(String nuevoNombre, String usuarioSolicitante) {
        if (!usuarioSolicitante.equals("admin") && !usuarioSolicitante.equals(usuarioPropietario)) {
            System.err.println("Sin permisos para renombrar: " + nombre);
            return false;
        }
        
        String nombreAnterior = this.nombre;
        this.nombre = nuevoNombre;
        this.fechaModificacion = System.currentTimeMillis();
        
        // Actualizar extensión
        int puntoIndex = nuevoNombre.lastIndexOf('.');
        if (puntoIndex > 0 && puntoIndex < nuevoNombre.length() - 1) {
            this.extension = nuevoNombre.substring(puntoIndex + 1);
        }
        
        System.out.println("Archivo renombrado: '" + nombreAnterior + "' -> '" + nuevoNombre + "'");
        return true;
    }
    
    // ===== GETTERS Y SETTERS =====
    
    public String getNombre() { 
        return nombre; 
    }
    
    public String getExtension() { 
        return extension; 
    }
    
    public int getTamañoBytes() { 
        return tamañoBytes; 
    }
    
    public int getTamañoBloques() { 
        return tamañoBloques; 
    }
    
    public void setTamañoBloques(int tamañoBloques) { 
        this.tamañoBloques = tamañoBloques; 
        this.tamañoBytes = tamañoBloques * 1024;
        this.fechaModificacion = System.currentTimeMillis();
    }
    
    public Bloque getPrimerBloque() { 
        return primerBloque; 
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
    
    // ===== MÉTODOS DE INFORMACIÓN =====
    
    /**
     * Obtiene información detallada del archivo
     */
    public String getInfoCompleta() {
        return String.format(
            "Archivo: %s\n" +
            "Ruta: %s\n" +
            "Tamaño: %d bytes (%d bloques)\n" +
            "Propietario: %s\n" +
            "Permisos: %s\n" +
            "Creado: %s\n" +
            "Modificado: %s\n" +
            "Estado: %s\n" +
            "Bloques: %s",
            nombre, rutaCompleta, tamañoBytes, tamañoBloques,
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
        return String.format("%s | %d bloques | %s | %s", 
            nombre, tamañoBloques, usuarioPropietario, 
            new java.util.Date(fechaModificacion));
    }
    
    @Override
    public String toString() {
        return String.format("Archivo{nombre='%s', tamaño=%d bloques, propietario='%s', ruta='%s'}",
                nombre, tamañoBloques, usuarioPropietario, rutaCompleta);
    }
    
    /**
     * Representación para el JTree
     */
    public String paraArbol() {
        return nombre + " (" + tamañoBloques + " bloques)";
    }
    
    /**
     * Verifica si el archivo está vacío
     */
    public boolean estaVacio() {
        return contenido == null || contenido.isEmpty();
    }
    
    /**
     * Obtiene las primeras líneas del contenido (para preview)
     */
    public String getPreview(int maxLineas) {
        if (contenido == null || contenido.isEmpty()) {
            return "[Archivo vacío]";
        }
        
        String[] lineas = contenido.split("\n");
        StringBuilder preview = new StringBuilder();
        
        for (int i = 0; i < Math.min(lineas.length, maxLineas); i++) {
            preview.append(lineas[i]);
            if (i < Math.min(lineas.length, maxLineas) - 1) {
                preview.append("\n");
            }
        }
        
        if (lineas.length > maxLineas) {
            preview.append("\n... (").append(lineas.length - maxLineas).append(" más líneas)");
        }
        
        return preview.toString();
    }
}