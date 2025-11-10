/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import edd.ListaSimple;

/**
 * Clase que representa un usuario del sistema de archivos
 * Gestiona permisos y propiedades de usuario
 */
public class Usuario {
    private String username;
    private String tipo; // "admin" o "usuario"
    private String homeDirectory; // Directorio personal
    private boolean estaActivo;
    private ListaSimple archivosPropios; // Tus archivos
    
    public Usuario(String username, String tipo) {
        this.username = username;
        this.tipo = tipo;
        this.homeDirectory = "/home/" + username;
        this.estaActivo = true;
        this.archivosPropios = new ListaSimple();
    }
    
    // Verifica si es administrador
    public boolean esAdministrador() {
        return "admin".equals(tipo);
    }
    
    // Verifica permisos sobre un archivo
    public boolean puedeLeer(Archivo archivo) {
        if (esAdministrador()) return true;
        if (archivo.esPublico()) return true;
        return archivo.getUsuarioPropietario().equals(username);
    }
    
    public boolean puedeEscribir(Archivo archivo) {
        if (esAdministrador()) return true;
        return archivo.getUsuarioPropietario().equals(username);
    }
    
    // Getters
    public String getUsername() { return username; }
    public String getTipo() { return tipo; }
    public String getHomeDirectory() { return homeDirectory; }
    public boolean estaActivo() { return estaActivo; }
}
