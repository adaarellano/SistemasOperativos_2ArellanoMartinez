/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

import edd.Nodo;

/**
 *
 * @author Ada y Day
 */
public class Pila {
    
    public Nodo peak;
    public int tamano;

    public Pila(){
        this.peak = null;
        this.tamano =0;
        
    
    }
    public boolean isEmpty(){
        return peak == null; 
    }
    
     // Apilar (push): Ahora recibe un Object para el dato
    public void apilar(Object nuevoDato) {
        Nodo nuevoNodo = new Nodo(nuevoDato); // Crea un Nodo con el dato
        nuevoNodo.setPnext(peak);             // El nuevo nodo apunta a la antigua cima
        peak = nuevoNodo;                     // El nuevo nodo es la nueva cima
        tamano++;
        System.out.println("Apilado: " + nuevoDato);
    }

    // Desapilar (pop): Retorna un Object
    public Object desapilar() {
        if (isEmpty()) {
            System.out.println("Error: La pila está vacía, no se puede desapilar.");
            return null; // O lanzar una excepción como IllegalStateException
        }
        Object datoDesapilado = peak.getData(); // Obtiene el dato de la cima
        peak = peak.getPnext();                 // La nueva cima es el siguiente nodo
        tamano--;
        System.out.println("Desapilado: " + datoDesapilado);
        return datoDesapilado;
    }

    // Ver Cima (peek): Retorna un Object
    public Object verCima() {
        if (isEmpty()) {
            System.out.println("Error: La pila está vacía, no hay elemento en la cima.");
            return null; // O lanzar una excepción
        }
        return peak.getData(); // Obtiene el dato de la cima sin eliminarlo
    }

    public int getTamano() { 
        return tamano;
    }

    public void imprimirPila() {
        if (isEmpty()) {
            System.out.println("Pila: [Vacía]");
            return;
        }
        System.out.print("Pila: [");
        Nodo actual = peak;
        while (actual != null) { // Ciclo while recorriendo los nodos
            System.out.print(actual.getData()); // Usa getData()
            if (actual.getPnext() != null) { // Usa getPnext()
                System.out.print(" -> ");
            }
            actual = actual.getPnext(); // Avanza al siguiente nodo usando getPnext()
        }
        System.out.println("] (Cima)");
    }
}
