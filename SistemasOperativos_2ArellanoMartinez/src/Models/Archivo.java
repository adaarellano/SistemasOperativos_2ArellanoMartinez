/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import java.awt.Color; // Importante para los colores del disco

/**
 * Clase que representa un archivo en el sistema de archivos
 * Con gestión de bloques, permisos, metadatos y colores visuales.
 */
public class Archivo {
    private String nombre;
    private String extension;
    private int tamañoBytes;           // Tamaño REAL del contenido
    private int tamañoBloques;         // Bloques USADOS realmente
    
    // Puntero al inicio de la lista enlazada de bloques
    private Bloque primerBloque;       
    
    private String usuarioPropietario;
    private String permisos;
    private String fechaCreacion;
    private long fechaModificacionLong;
    private String contenido;
    private String rutaCompleta;
    private String rutaReal;
    
    private int tamañoReservadoBytes;  // Bytes reservados (bloques * 1024)
    private int bloquesReservados;     // Bloques reservados al crear
    
    private boolean estaAbierto;
    private boolean esPublico;
    
    // Color único para representar este archivo en el PanelDisco
    private Color color; 
    
    /**
     * Constructor principal
     */
    public Archivo(String nombre, int bloquesReservados, String usuarioPropietario, String rutaCompleta) {
        this.nombre = nombre;
        this.bloquesReservados = bloquesReservados;
        this.tamañoReservadoBytes = bloquesReservados * 1024; // 1024 bytes por bloque
        
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
        
        // Generar un color aleatorio brillante para este archivo
        float r = (float) Math.random();
        float g = (float) Math.random();
        float b = (float) Math.random();
        this.color = new Color(r, g, b).brighter(); 
    }
    
    /**
     * Constructor simplificado
     */
    public Archivo(String nombre, int bloquesReservados, String usuarioPropietario) {
        this(nombre, bloquesReservados, usuarioPropietario, "/" + nombre);
    }
    
    // ===== OPERACIONES DE CONTENIDO =====
    
    public boolean escribirContenido(String nuevoContenido) {
        this.contenido = nuevoContenido;
        this.tamañoBytes = nuevoContenido.length();
        
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
    
    // ===== GESTIÓN DE BLOQUES =====
    
    public Bloque getPrimerBloque() {
        return primerBloque;
    }

    public void setPrimerBloque(Bloque primerBloque) {
        this.primerBloque = primerBloque;
    }
    
    public String getInfoBloques() {
        if (primerBloque == null) {
            return "Sin bloques asignados";
        }
        
        StringBuilder sb = new StringBuilder();
        Bloque actual = primerBloque;
        int contador = 0;
        
        while (actual != null && contador < 50) {
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
    
    /**
     * Obtiene información detallada del archivo (Para PanelOutput y Debug)
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

    // ===== GETTERS Y SETTERS =====

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getTamañoBytes() { return tamañoBytes; }
    public int getTamañoBloques() { return tamañoBloques; }

    public int getBloquesReservados() { return bloquesReservados; }
    public int getTamañoReservadoBytes() { return tamañoReservadoBytes; }

    public String getUsuarioPropietario() { return usuarioPropietario; }
    public String getPropietario() { return usuarioPropietario; }
    
    public String getRutaCompleta() { return rutaCompleta; }
    public void setRutaCompleta(String ruta) { this.rutaCompleta = ruta; }

    public String getRutaReal() { return rutaReal; }
    public void setRutaReal(String rutaReal) { this.rutaReal = rutaReal; }

    public boolean esPublico() { return esPublico; }
    public void setEsPublico(boolean esPublico) { this.esPublico = esPublico; }
    
    public String getFechaCreacion() { return fechaCreacion; }
    
    // Getter para el color visual en el disco
    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return nombre + " (" + tamañoBloques + "/" + bloquesReservados + " blqs)";
    }
}