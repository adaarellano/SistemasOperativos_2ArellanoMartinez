/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import java.awt.Color; // <-- IMPORTANTE: Necesario para los colores

/**
 * Clase que representa un bloque de almacenamiento en el disco simulado
 */
public class Bloque {
    private int idBloque;
    private boolean estaOcupado;
    private String ocupadoPor; 
    private Bloque siguienteBloque;   
    private int idProcesoDueño;  
    private String datos;       
    
    // Variable para guardar el color visual del archivo
    private Color color; 

    // Constructor principal
    public Bloque(int idBloque, boolean estaOcupado, String ocupadoPor, Bloque siguienteBloque) {
        this.idBloque = idBloque;
        this.estaOcupado = estaOcupado;
        this.ocupadoPor = ocupadoPor;
        this.siguienteBloque = siguienteBloque;
        this.idProcesoDueño = -1; 
        this.datos = ""; 
        this.color = Color.GREEN.darker(); // Por defecto verde (libre)
    }

    // Constructor simplificado
    public Bloque(int idBloque) {
        this(idBloque, false, null, null);
    }

    // Getters y Setters
    public int getIdBloque() {
        return idBloque;
    }

    public void setIdBloque(int idBloque) {
        this.idBloque = idBloque;
    }

    public boolean estaOcupado() {
        return estaOcupado;
    }

    public void setEstaOcupado(boolean estaOcupado) {
        this.estaOcupado = estaOcupado;
    }

    public String getOcupadoPor() {
        return ocupadoPor;
    }

    public void setOcupadoPor(String ocupadoPor) {
        this.ocupadoPor = ocupadoPor;
    }

    public Bloque getSiguienteBloque() {
        return siguienteBloque;
    }

    public void setSiguienteBloque(Bloque siguienteBloque) {
        this.siguienteBloque = siguienteBloque;
    }

    public int getIdProcesoDueño() {
        return idProcesoDueño;
    }

    public void setIdProcesoDueño(int idProcesoDueño) {
        this.idProcesoDueño = idProcesoDueño;
    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }

    /**
     * Ocupa el bloque con un archivo, proceso y COLOR específico
     */
    public void ocuparBloque(String nombreArchivo, int idProceso, Color colorArchivo) {
        this.estaOcupado = true;
        this.ocupadoPor = nombreArchivo;
        this.idProcesoDueño = idProceso;
        this.color = colorArchivo; // <-- Guardamos el color
        this.datos = "Datos del archivo: " + nombreArchivo;
    }
    
    // Sobrecarga para compatibilidad (por si acaso)
    public void ocuparBloque(String nombreArchivo, int idProceso) {
        ocuparBloque(nombreArchivo, idProceso, Color.RED.darker()); // Color por defecto rojo
    }

    /**
     * Libera el bloque, dejándolo disponible y VERDE
     */
    public void liberarBloque() {
        this.estaOcupado = false;
        this.ocupadoPor = null;
        this.idProcesoDueño = -1;
        this.siguienteBloque = null;
        this.datos = "";
        this.color = Color.GREEN.darker(); // <-- Resetear a verde
    }

    /**
     * Verifica si el bloque está libre
     */
    public boolean estaLibre() {
        return !estaOcupado;
    }

    /**
     * Obtiene el ID del siguiente bloque en la cadena
     */
    public int getIdSiguienteBloque() {
        return (siguienteBloque != null) ? siguienteBloque.getIdBloque() : -1;
    }
    
    // Getter del color (¡ESTO ES LO QUE FALTABA!)
    public Color getColor() {
        // Si por alguna razón es null, devolver verde
        return (color == null) ? Color.GREEN.darker() : color;
    }

    @Override
    public String toString() {
        String estado = estaOcupado ? "OCUPADO" : "LIBRE";
        String infoDueño = estaOcupado ? " por '" + ocupadoPor + "'" : "";
        String infoSiguiente = (siguienteBloque != null) ? " -> Bloque " + siguienteBloque.getIdBloque() : " -> NULL";
        
        return String.format("Bloque %d [%s%s]%s", idBloque, estado, infoDueño, infoSiguiente);
    }

    public String aStringCorto() {
        if (!estaOcupado) {
            return "L"; 
        }
        return ocupadoPor != null && !ocupadoPor.isEmpty() ? 
               String.valueOf(ocupadoPor.charAt(0)).toUpperCase() : "O";
    }

    public Bloque copiaProfunda() {
        Bloque copia = new Bloque(this.idBloque, this.estaOcupado, this.ocupadoPor, null);
        copia.setIdProcesoDueño(this.idProcesoDueño);
        copia.setDatos(this.datos);
        copia.color = this.color; // Copiar color también
        return copia;
    }
}