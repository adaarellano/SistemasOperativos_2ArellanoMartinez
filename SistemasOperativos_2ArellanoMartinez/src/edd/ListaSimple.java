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
public class ListaSimple {
    
    private Nodo head;
    private int size;
    
    public ListaSimple(){}
    
    public boolean isEmpty(){
        return this.head == null;
    }
    
    public void mostrarLista(ListaSimple lista){
        Nodo tmp = lista.head;
        while(tmp != null){
            System.out.print("["+tmp.getData()+"]");
            tmp = tmp.getPnext();
        }
    }
    
    public void insertBegin(Object nodo){
         Nodo temp = new Nodo(nodo);
        if (isEmpty()) {
            this.head = temp;
        } else {
            temp.setPnext(this.head);
            this.head = temp;
        }
        this.size++;  // Siempre al final
    }
    
    public void insertFinal(Object nodo){
        Nodo temp = new Nodo(nodo);
        if (isEmpty()) {
            this.head = temp;  // Maneja vacío directamente, sin llamar insertBegin
        } else {
            Nodo aux = this.head;
            while (aux.getPnext() != null) {
                aux = aux.getPnext();
            }
            aux.setPnext(temp);
        }
        this.size++;  // Solo UNA vez, al final        
    }
    
    public void deleteBegin(){
        Nodo aux = this.head;
        if(isEmpty()){
            System.out.println("La lista esta vacia");
        }else{
            this.head = aux.getPnext();
            aux.setPnext(null);
            size --;
        }
    }
    
    public void deleteFinal(){
        if (isEmpty()) {
            System.out.println("La lista está vacía");
            return;
        }
        if (this.size == 1) {  // Caso especial: solo head
            this.head = null;
            this.size--;
            return;
        }
        Nodo aux = this.head;
        while (aux.getPnext().getPnext() != null) {  // Ahora seguro (size > 1)
            aux = aux.getPnext();
        }
        aux.setPnext(null);
        this.size--;
    }
      
    public int sizeLista(){
        return this.getSize();
    }
    
    public boolean search(Object dato) {
        Nodo temp = this.head;
        while (temp != null) {
            if (temp.getData().equals(dato)) {
                return true; 
            }
            temp = temp.getPnext();
        }
        return false; 
}

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }
    
    /**
 * Obtiene el dato (Proceso) en el índice especificado sin removerlo.
 * NECESARIO para iterar y encontrar el proceso más corto.
 * @param index La posición del elemento (0-based).
 * @return El objeto (Proceso) en esa posición.
 */
    public Object get(int index) {
      if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        Nodo tmp = this.head;
        if (tmp == null) {  // Check inicial si head null pero size >0 (inconsistencia)
            return null;
        }
        
        for (int i = 0; i < index; i++) {
            if (tmp == null) {  // ← NUEVO: Check defensivo para evitar NPE si size inflado
                throw new IndexOutOfBoundsException("Lista inconsistente: size=" + size + " pero solo " + i + " nodos");
            }
            tmp = tmp.getPnext();
        }
        
        if (tmp == null) {  // Check final
            throw new IndexOutOfBoundsException("Nodo null al final del bucle (lista inconsistente)");
        }
        
        return tmp.getData();
    }

    /**
 * Elimina la primera ocurrencia del objeto especificado de la lista.
 * NECESARIO para sacar un Proceso de la cola cuando es seleccionado.
 * @param elemento El objeto (Proceso) a remover.
 * @return true si el elemento fue encontrado y eliminado, false en caso contrario.
 */
    public boolean remove(Object elemento) {
        if (isEmpty()) {
            return false;
        }

        if (head.getData().equals(elemento)) {
            deleteBegin(); 
            return true;
        }
        
        Nodo actual = head;
        Nodo anterior = null;

        while (actual != null && !actual.getData().equals(elemento)) {
            anterior = actual;
            actual = actual.getPnext();
        }

        if (actual == null) {
            return false;
        }

        anterior.setPnext(actual.getPnext());
        
        size--; 
        return true;
    }
     
    public void clear() {
    this.head = null;
    this.size = 0;
}
     
    
}
