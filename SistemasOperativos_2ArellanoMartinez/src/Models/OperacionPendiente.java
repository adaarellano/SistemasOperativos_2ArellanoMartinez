/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 *
 * @author ile2
 */

public class OperacionPendiente {
    private String tipo;      // CREAR, LEER, ELIMINAR...
    private String ruta;
    private String usuario;
    private int tamano;       // Para crear
    private String datos;     // Para editar
    
    // Constructor completo
    public OperacionPendiente(String tipo, String ruta, String usuario, int tamano, String datos) {
        this.tipo = tipo;
        this.ruta = ruta;
        this.usuario = usuario;
        this.tamano = tamano;
        this.datos = datos;
    }
    
    // Getters
    public String getTipo() { return tipo; }
    public String getRuta() { return ruta; }
    public String getUsuario() { return usuario; }
    public int getTamano() { return tamano; }
    public String getDatos() { return datos; }
    
    @Override
    public String toString() {
        return tipo + ": " + ruta;
    }
}