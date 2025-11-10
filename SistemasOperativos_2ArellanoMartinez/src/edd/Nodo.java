/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

/**
 *
 * @author Ada y Day
 */
public class Nodo {private Object data;      // El objeto a guardar
    private Nodo pnext;      // La dirección del siguiente nodo
    private Nodo pprevious;  // La dirección del nodo anterior (para listas dobles)

    // Constructor principal
    public Nodo(Object data) {
        this.data = data;
        this.pnext = null;
        this.pprevious = null;
    }
    
    // Constructor vacío (para flexibilidad)
    public Nodo() {
        this.data = null;
        this.pnext = null;
        this.pprevious = null;
    }

    // ===== GETTERS Y SETTERS =====
    
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Nodo getPnext() {
        return pnext;
    }

    public void setPnext(Nodo pnext) {
        this.pnext = pnext;
    }

    public Nodo getPprevious() {
        return pprevious;
    }

    public void setPprevious(Nodo pprevious) {
        this.pprevious = pprevious;
    }
    
    // Método toString para debugging
    @Override
    public String toString() {
        return "Nodo{" + "data=" + data + '}';
    }
}
