/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 * Clase que representa un bloque de almacenamiento en el disco simulado
 * Cada bloque puede estar ocupado por un archivo o libre
 * Implementa la asignación encadenada mediante referencia al siguiente bloque
 */

public class Bloque {
    private int idBloque;
    private boolean estaOcupado;
    private String ocupadoPor; // Nombre del archivo que ocupa este bloque
    private Bloque siguienteBloque;   // Referencia al siguiente bloque en la cadena
    private int idProcesoDueño;  // ID del proceso que creó el archivo
    private String datos;       // Datos simulados almacenados en el bloque

    // Constructor principal
    public Bloque(int idBloque, boolean estaOcupado, String ocupadoPor, Bloque siguienteBloque) {
        this.idBloque = idBloque;
        this.estaOcupado = estaOcupado;
        this.ocupadoPor = ocupadoPor;
        this.siguienteBloque = siguienteBloque;
        this.idProcesoDueño = -1; // -1 indica que no tiene dueño
        this.datos = ""; // Datos vacíos por defecto
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
     * Ocupa el bloque con un archivo y proceso específico
     */
    public void ocuparBloque(String nombreArchivo, int idProceso) {
        this.estaOcupado = true;
        this.ocupadoPor = nombreArchivo;
        this.idProcesoDueño = idProceso;
        this.datos = "Datos del archivo: " + nombreArchivo;
    }

    /**
     * Libera el bloque, dejándolo disponible
     */
    public void liberarBloque() {
        this.estaOcupado = false;
        this.ocupadoPor = null;
        this.idProcesoDueño = -1;
        this.siguienteBloque = null;
        this.datos = "";
    }

    /**
     * Verifica si el bloque está libre
     */
    public boolean estaLibre() {
        return !estaOcupado;
    }

    /**
     * Obtiene el ID del siguiente bloque en la cadena
     * Retorna -1 si no hay siguiente bloque
     */
    public int getIdSiguienteBloque() {
        return (siguienteBloque != null) ? siguienteBloque.getIdBloque() : -1;
    }

    /**
     * Representación en String del bloque para debugging
     */
    @Override
    public String toString() {
        String estado = estaOcupado ? "OCUPADO" : "LIBRE";
        String infoDueño = estaOcupado ? " por '" + ocupadoPor + "'" : "";
        String infoSiguiente = (siguienteBloque != null) ? " -> Bloque " + siguienteBloque.getIdBloque() : " -> NULL";
        
        return String.format("Bloque %d [%s%s]%s", idBloque, estado, infoDueño, infoSiguiente);
    }

    /**
     * Representación corta para la interfaz gráfica
     */
    public String aStringCorto() {
        if (!estaOcupado) {
            return "L"; // Libre
        }
        // Ocupado: mostrar inicial del archivo o ID
        return ocupadoPor != null && !ocupadoPor.isEmpty() ? 
               String.valueOf(ocupadoPor.charAt(0)).toUpperCase() : "O";
    }

    /**
     * Crea una copia profunda del bloque (útil para operaciones de buffer)
     */
    public Bloque copiaProfunda() {
        Bloque copia = new Bloque(this.idBloque, this.estaOcupado, this.ocupadoPor, null);
        copia.setIdProcesoDueño(this.idProcesoDueño);
        copia.setDatos(this.datos);
        // Nota: siguienteBloque no se copia para evitar ciclos infinitos
        return copia;
    }
}