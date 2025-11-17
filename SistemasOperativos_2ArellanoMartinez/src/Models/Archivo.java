/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

public class Archivo {
    private String nombre;
    private String contenido;
    private int tamañoBloques;
    private int bloquesReservados;
    private String propietario;
    private String fechaCreacion;
    private String rutaReal;
    private String permisos;
    private boolean esPublico; // **NUEVO: Para control de permisos**
    
    public Archivo(String nombre, int tamañoBloques, String propietario) {
        this.nombre = nombre;
        this.tamañoBloques = tamañoBloques;
        this.bloquesReservados = tamañoBloques;
        this.propietario = propietario;
        this.contenido = "";
        this.fechaCreacion = java.time.LocalDateTime.now().toString();
        this.rutaReal = null;
        this.permisos = "rw-r--r--";
        this.esPublico = false; // Por defecto no es público
    }
    
    // **NUEVO: Método para verificar si es público**
    public boolean esPublico() {
        return esPublico;
    }
    
    
    public void setEsPublico(boolean esPublico) {
        this.esPublico = esPublico;
    }
    
    // **NUEVO: Método para obtener usuario propietario (compatibilidad con Usuario)**
    public String getUsuarioPropietario() {
        return propietario;
    }
    
    // Getters y Setters existentes
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public int getTamañoBloques() { return tamañoBloques; }
    public void setTamañoBloques(int tamañoBloques) { this.tamañoBloques = tamañoBloques; }
    public int getBloquesReservados() { return bloquesReservados; }
    public void setBloquesReservados(int bloquesReservados) { this.bloquesReservados = bloquesReservados; }
    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getRutaReal() { return rutaReal; }
    public void setRutaReal(String rutaReal) { this.rutaReal = rutaReal; }
    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }
    
    @Override
    public String toString() {
        return nombre + " (" + tamañoBloques + " bloques)";
    }
}