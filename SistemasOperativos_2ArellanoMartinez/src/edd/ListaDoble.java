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
public class ListaDoble {
    private Nodo head;
    private Nodo tail;
    private int size;

    // Constructor
    public ListaDoble() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Método para verificar si la lista está vacía
    public boolean isEmpty() {
        return this.head == null;
    }

    // Método para insertar al inicio
    public void insertBegin(Object dato) {
        Nodo nuevoNodo = new Nodo(dato);
        if (isEmpty()) {
            this.head = nuevoNodo;
            this.tail = nuevoNodo;
        } else {
            nuevoNodo.setPnext(this.head);
            this.head.setPprevious(nuevoNodo);
            this.head = nuevoNodo;
        }
        size++;
    }

    // Método para insertar al final
    public void insertEnd(Object dato) {
        Nodo nuevoNodo = new Nodo(dato);
        if (isEmpty()) {
            this.head = nuevoNodo;
            this.tail = nuevoNodo;
        } else {
            nuevoNodo.setPprevious(this.tail);
            this.tail.setPnext(nuevoNodo);
            this.tail = nuevoNodo;
        }
        size++;
    }

    // Método para eliminar del inicio
    public void deleteBegin() {
        if (isEmpty()) {
            System.out.println("La lista está vacía");
        } else {
            this.head = this.head.getPnext();
            if (this.head != null) {
                this.head.setPprevious(null);
            } else {
                this.tail = null; // Si la lista queda vacía
            }
            size--;
        }
    }

    // Método para eliminar del final
    public void deleteEnd() {
        if (isEmpty()) {
            System.out.println("La lista está vacía");
        } else if (this.head.getPnext() == null) { // Si solo hay un nodo
            this.head = null;
            this.tail = null;
        } else {
            this.tail = this.tail.getPprevious();
            this.tail.setPnext(null);
        }
        size--;
    }

    // Método para mostrar la lista
    public String mostrarLista() {
        StringBuilder result = new StringBuilder();
        Nodo temp = this.head;
        while (temp != null) {
            result.append("[").append(temp.getData()).append("]\n");
            temp = temp.getPnext();
        }
        return result.toString();
    }

    // Método para obtener el tamaño de la lista
    public int size() {
        return this.size;
    }

    // Método para buscar un nodo en la lista
    public boolean search(Object dato) {
        Nodo temp = this.head;
        while (temp != null) {
            if (temp.getData().equals(dato)) {
                return true; // Encontrado
            }
            temp = temp.getPnext();
        }
        return false; // No encontrado
    }
    
        public void imprimirLista() {
        Nodo temp = this.head;
        if (isEmpty()) {
            System.out.println("La lista está vacía.");
            return;
        }
        System.out.println("Elementos de la lista:");
        while (temp != null) {
            System.out.print("[" + temp.getData() + "]");
            temp = temp.getPnext();
        }
    }

}

