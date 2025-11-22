/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import edd.ListaSimple; // Importamos tu ListaSimple

/**
 * Clase que representa un directorio en el sistema de archivos
 * Usa ListaSimple para archivos y subdirectorios
 */
public class Directorio {
    private String nombre;
    private String rutaCompleta;
    private Directorio directorioPadre;
    
    // Usamos TU ListaSimple
    private ListaSimple archivos;
    private ListaSimple subdirectorios;
    
    // Metadatos
    private String usuarioPropietario;
    private String permisos;
    private long fechaCreacion;
    private long fechaModificacion;
    
    /**
     * Constructor para directorio raíz
     */
    public Directorio(String nombre) {
        this(nombre, null);
    }
    
    /**
     * Constructor para subdirectorios
     */
    public Directorio(String nombre, Directorio directorioPadre) {
        this.nombre = nombre;
        this.directorioPadre = directorioPadre;
        this.archivos = new ListaSimple();
        this.subdirectorios = new ListaSimple();
        this.usuarioPropietario = "admin";
        this.permisos = "rwxr-xr-x";
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaModificacion = this.fechaCreacion;
        
        // Calcular ruta completa
        if (directorioPadre == null) {
            this.rutaCompleta = nombre.equals("/") ? "/" : "/" + nombre;
        } else {
            this.rutaCompleta = directorioPadre.getRutaCompleta() + 
                               (directorioPadre.getRutaCompleta().equals("/") ? "" : "/") + nombre;
        }
    }
    
    // ===== OPERACIONES CON ARCHIVOS =====
    
    /**
     * Agrega un archivo al directorio
     */
    public boolean agregarArchivo(Archivo archivo) {
        // Verificar si ya existe un archivo con ese nombre
        if (buscarArchivo(archivo.getNombre()) != null) {
            return false; // Ya existe
        }
        
        archivos.insertFinal(archivo);
        actualizarFechaModificacion();
        return true;
    }
    
    /**
     * Elimina un archivo del directorio
     */
    public boolean eliminarArchivo(String nombreArchivo) {
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            if (archivo.getNombre().equals(nombreArchivo)) {
                // Eliminar por índice
                Object elemento = archivos.get(i);
                archivos.remove(elemento);
                actualizarFechaModificacion();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Busca un archivo por nombre
     */
    public Archivo buscarArchivo(String nombreArchivo) {
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            if (archivo.getNombre().equals(nombreArchivo)) {
                return archivo;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los archivos del directorio
     */
    public ListaSimple getArchivos() {
        return archivos;
    }
    
    // ===== OPERACIONES CON SUBDIRECTORIOS =====
    
    /**
     * Crea un nuevo subdirectorio
     */
    public Directorio crearSubdirectorio(String nombreSubdirectorio) {
        // Verificar si ya existe
        if (buscarSubdirectorio(nombreSubdirectorio) != null) {
            return null;
        }
        
        Directorio subdir = new Directorio(nombreSubdirectorio, this);
        subdirectorios.insertFinal(subdir);
        actualizarFechaModificacion();
        return subdir;
    }
    
    /**
     * Elimina un subdirectorio y todo su contenido por nombre
     */
    public boolean eliminarSubdirectorio(String nombreSubdirectorio) {
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            if (subdir.getNombre().equals(nombreSubdirectorio)) {
                Object elemento = subdirectorios.get(i);
                subdirectorios.remove(elemento);
                actualizarFechaModificacion();
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina un subdirectorio específico por referencia de objeto
     */
    public boolean eliminarSubdirectorio(Directorio subdirectorio) {
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            if (subdir == subdirectorio) {
                Object elemento = subdirectorios.get(i);
                subdirectorios.remove(elemento);
                actualizarFechaModificacion();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Busca un subdirectorio por nombre
     */
    public Directorio buscarSubdirectorio(String nombreSubdirectorio) {
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            if (subdir.getNombre().equals(nombreSubdirectorio)) {
                return subdir;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los subdirectorios
     */
    public ListaSimple getSubdirectorios() {
        return subdirectorios;
    }
    
    // ===== OPERACIONES DE BÚSQUEDA EN PROFUNDIDAD =====
    
    /**
     * Busca un archivo en este directorio y todos los subdirectorios
     */
    public Archivo buscarArchivoRecursivo(String nombreArchivo) {
        // Buscar en archivos locales
        Archivo archivo = buscarArchivo(nombreArchivo);
        if (archivo != null) {
            return archivo;
        }
        
        // Buscar en subdirectorios
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            archivo = subdir.buscarArchivoRecursivo(nombreArchivo);
            if (archivo != null) {
                return archivo;
            }
        }
        
        return null;
    }
    
    /**
     * Busca un directorio por ruta completa
     */
    public Directorio buscarDirectorioRecursivo(String ruta) {
        String[] partes = ruta.split("/");
        return buscarDirectorioRecursivo(partes, 0);
    }
    
    private Directorio buscarDirectorioRecursivo(String[] partes, int indice) {
        // Caso base: Si nos pasamos del arreglo, devolvemos este directorio
        if (indice >= partes.length) {
            return this;
        }
        
        // CORRECCIÓN: Si la parte actual está vacía (por ejemplo, por un "//" o al inicio),
        // simplemente la saltamos y seguimos buscando en el siguiente índice.
        if (partes[indice].isEmpty()) {
            return buscarDirectorioRecursivo(partes, indice + 1);
        }
        
        String nombreBuscado = partes[indice];
        
        // Buscamos en los subdirectorios
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            if (subdir.getNombre().equals(nombreBuscado)) {
                // Si es el último elemento de la ruta, hemos llegado
                if (indice == partes.length - 1) {
                    return subdir;
                }
                // Si faltan más partes, seguimos bajando
                return subdir.buscarDirectorioRecursivo(partes, indice + 1);
            }
        }
        
        return null; // No encontrado
    }
    
    // ===== OPERACIONES DE NAVEGACIÓN =====
    
    /**
     * Obtiene la ruta completa del directorio
     */
    public String getRutaCompleta() {
        return rutaCompleta;
    }
    
    /**
     * Navega a un subdirectorio por nombre
     */
    public Directorio navegar(String nombreSubdirectorio) {
        return buscarSubdirectorio(nombreSubdirectorio);
    }
    
    /**
     * Navega al directorio padre
     */
    public Directorio getDirectorioPadre() {
        return directorioPadre;
    }
    
    /**
     * Verifica si es el directorio raíz
     */
    public boolean esRaiz() {
        return directorioPadre == null;
    }
    
    // ===== INFORMACIÓN DEL DIRECTORIO =====
    
    /**
     * Obtiene el tamaño total del directorio (incluyendo subdirectorios)
     */
    public int getTamañoTotal() {
        int tamaño = 0;
        
        // Sumar tamaño de archivos
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            tamaño += archivo.getTamañoBloques();
        }
        
        // Sumar tamaño de subdirectorios
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            tamaño += subdir.getTamañoTotal();
        }
        
        return tamaño;
    }
    
    /**
     * Obtiene el número total de archivos (incluyendo subdirectorios)
     */
    public int getNumeroTotalArchivos() {
        int count = archivos.getSize();
        
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            count += subdir.getNumeroTotalArchivos();
        }
        
        return count;
    }
    
    /**
     * Obtiene el número total de subdirectorios (incluyendo subdirectorios anidados)
     */
    public int getNumeroTotalSubdirectorios() {
        int count = subdirectorios.getSize();
        
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            count += subdir.getNumeroTotalSubdirectorios();
        }
        
        return count;
    }
    
    // ===== MÉTODOS DE ACTUALIZACIÓN =====
    
    private void actualizarFechaModificacion() {
        this.fechaModificacion = System.currentTimeMillis();
    }
    
    // ===== GETTERS Y SETTERS =====
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
        actualizarFechaModificacion();
    }
    
    public String getUsuarioPropietario() {
        return usuarioPropietario;
    }
    
    public void setUsuarioPropietario(String usuarioPropietario) {
        this.usuarioPropietario = usuarioPropietario;
    }
    
    public String getPermisos() {
        return permisos;
    }
    
    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }
    
    public long getFechaCreacion() {
        return fechaCreacion;
    }
    
    public long getFechaModificacion() {
        return fechaModificacion;
    }
    
    // ===== REPRESENTACIÓN EN STRING =====
    
    @Override
    public String toString() {
        return String.format("Directorio{nombre='%s', ruta='%s', archivos=%d, subdirectorios=%d}",
                nombre, rutaCompleta, archivos.getSize(), subdirectorios.getSize());
    }
    
    /**
     * Representación para el JTree
     */
    public String paraArbol() {
        return nombre + " (" + archivos.getSize() + " archivos, " + subdirectorios.getSize() + " dirs)";
    }
    
    /**
     * Lista el contenido del directorio
     */
    public String listarContenido() {
        StringBuilder sb = new StringBuilder();
        sb.append("Directorio: ").append(rutaCompleta).append("\n");
        sb.append("Archivos: ").append(archivos.getSize()).append("\n");
        
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = (Archivo) archivos.get(i);
            sb.append("  - ").append(archivo.getNombre())
              .append(" (").append(archivo.getTamañoBloques()).append(" bloques)\n");
        }
        
        sb.append("Subdirectorios: ").append(subdirectorios.getSize()).append("\n");
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio subdir = (Directorio) subdirectorios.get(i);
            sb.append("  - ").append(subdir.getNombre()).append("/\n");
        }
        
        return sb.toString();
    }
}