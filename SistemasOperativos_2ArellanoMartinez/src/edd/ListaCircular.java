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
public class ListaCircular {
    private Nodo head;
    private int size;

    // Constructor
    public ListaCircular() {
        this.head = null;
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
            nuevoNodo.setPnext(this.head); // Apunta a sí mismo, formando el ciclo
        } else {
            Nodo temp = this.head;
            while (temp.getPnext() != this.head) {
                temp = temp.getPnext();
            }
            nuevoNodo.setPnext(this.head);
            temp.setPnext(nuevoNodo);
            this.head = nuevoNodo;
        }
        size++;
    }

    // Método para insertar al final
    public void insertEnd(Object dato) {
        Nodo nuevoNodo = new Nodo(dato);
        if (isEmpty()) {
            this.head = nuevoNodo;
            nuevoNodo.setPnext(this.head); // Apunta a sí mismo, formando el ciclo
        } else {
            Nodo temp = this.head;
            while (temp.getPnext() != this.head) {
                temp = temp.getPnext();
            }
            temp.setPnext(nuevoNodo);
            nuevoNodo.setPnext(this.head);
        }
        size++;
    }

    // Método para eliminar del inicio
    public void deleteBegin() {
        if (isEmpty()) {
            System.out.println("La lista está vacía");
        } else if (this.head.getPnext() == this.head) { // Solo un nodo
            this.head = null;
        } else {
            Nodo temp = this.head;
            while (temp.getPnext() != this.head) {
                temp = temp.getPnext();
            }
            this.head = this.head.getPnext();
            temp.setPnext(this.head);
        }
        size--;
    }

    // Método para eliminar del final
    public void deleteEnd() {
        if (isEmpty()) {
            System.out.println("La lista está vacía");
        } else if (this.head.getPnext() == this.head) { // Solo un nodo
            this.head = null;
        } else {
            Nodo temp = this.head;
            while (temp.getPnext().getPnext() != this.head) {
                temp = temp.getPnext();
            }
            temp.setPnext(this.head);
        }
        size--;
    }

    // Método para mostrar la lista
    public String mostrarLista() {
        StringBuilder result = new StringBuilder();
        Nodo temp = this.head;
        if (temp != null) {
            do {
                result.append("[").append(temp.getData()).append("]\n");
                temp = temp.getPnext();
            } while (temp != this.head);
        }
        return result.toString();
    
    }

    // Método para obtener el tamaño de la lista
    public int size() {
        return this.size;
    }

    // Método para buscar un nodo en la lista
    public boolean search(Object dato) {
        if (isEmpty()) {
            return false;
        }
        Nodo temp = this.head;
        do {
            if (temp.getData().equals(dato)) {
                return true; // Encontrado
            }
            temp = temp.getPnext();
        } while (temp != this.head);
        return false; // No encontrado
    }
    
        public void imprimir() {
        if (isEmpty()) {
            System.out.println("La lista está vacía.");
            return;
        }
        System.out.println("Elementos de la lista circular:");
        Nodo temp = this.head;
        do {
            System.out.println("[" + temp.getData() + "]");
            temp = temp.getPnext();
        } while (temp != this.head); // Continúa hasta que volvemos al head
    }
}

