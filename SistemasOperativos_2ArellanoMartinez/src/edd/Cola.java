/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

import edd.Nodo;

/**
 *
 * @author Day Y Ada
 */
public class Cola {
    private Nodo frente;      // Puntero al primer elemento de la cola (head)
    private Nodo finalCola;   // Puntero al último elemento de la cola (tail)
    private int tamano;       // Almacena el número actual de elementos en la cola

    // Constructor de la Cola
    public Cola() {
        this.frente = null;      // La cola está vacía, no hay frente
        this.finalCola = null;   // La cola está vacía, no hay final
        this.tamano = 0;         // El tamaño inicial es 0
    }

    // 1. `estaVacia()`: Verifica si la cola no contiene elementos.
    // Retorna `true` si la cola está vacía, `false` en caso contrario.
    public boolean estaVacia() {
        return frente == null; // Si el frente es null, la cola está vacía. También podrías usar `return tamano == 0;`
    }

    // 2. `encolar(Object elemento)`: Agrega un elemento al final de la cola.
    // `elemento` es el dato que se va a añadir a la cola.
    public void encolar(Object elemento) {
        Nodo nuevoNodo = new Nodo(elemento); // Crea un nuevo nodo con el elemento dado

        if (estaVacia()) {
            // Si la cola está vacía, el nuevo nodo es tanto el frente como el final
            frente = nuevoNodo;
        } else {
            // Si la cola no está vacía, el nodo que actualmente es el final
            // debe apuntar al nuevo nodo.
            finalCola.setPnext(nuevoNodo);
        }
        finalCola = nuevoNodo; // El nuevo nodo es ahora el final de la cola
        tamano++; // Incrementa el tamaño de la cola
        System.out.println("Encolado: " + elemento);
    }

    // 3. `desencolar()`: Elimina y retorna el elemento del frente de la cola.
    // Retorna el dato del elemento eliminado. Si la cola está vacía, retorna `null` y un mensaje de error.
    public Object desencolar() {
        if (estaVacia()) {
            System.out.println("Error: La cola está vacía, no se puede desencolar.");
            return null; // O podrías lanzar una excepción como `throw new IllegalStateException("La cola está vacía.");`
        }

        // Guarda el dato del nodo en el frente antes de eliminarlo
        Object datoDesencolado = frente.getData();
        // El nuevo frente de la cola es el siguiente nodo
        frente = frente.getPnext();
        
        // Si después de desencolar, el frente se vuelve null,
        // significa que la cola ahora está completamente vacía,
        // por lo que el final también debe ser null.
        if (frente == null) {
            finalCola = null;
        }
        
        tamano--; // Decrementa el tamaño de la cola
        System.out.println("Desencolado: " + datoDesencolado);
        return datoDesencolado;
    }

    // 4. `verFrente()` (o `peek` / `head`): Retorna el elemento del frente sin eliminarlo.
    // Retorna el dato del elemento en el frente. Si la cola está vacía, retorna `null` y un mensaje de error.
    public Object verFrente() {
        if (estaVacia()) {
            System.out.println("Error: La cola está vacía, no hay elemento en el frente.");
            return null; // O lanzar una excepción
        }
        return frente.getData(); // Retorna el dato del nodo en el frente
    }

    // 5. `verFinal()` (o `tail`): Retorna el elemento del final sin eliminarlo.
    // Retorna el dato del elemento en el final. Si la cola está vacía, retorna `null` y un mensaje de error.
    public Object verFinal() {
        if (estaVacia()) {
            System.out.println("Error: La cola está vacía, no hay elemento al final.");
            return null; // O lanzar una excepción
        }
        return finalCola.getData(); // Retorna el dato del nodo en el final
    }

    // 6. `getTamano()`: Retorna el número actual de elementos en la cola.
    public int getTamano() {
        return tamano;
    }

    // Método para imprimir la cola (útil para depuración)
    public void imprimirCola() {
        if (estaVacia()) {
            System.out.println("Cola: [Vacía]");
            return;
        }
        System.out.print("Cola: [Frente -> ");
        Nodo actual = frente; // Empezamos desde el frente
        while (actual != null) { // Recorremos mientras el nodo actual no sea nulo
            System.out.print(actual.getData()); // Imprimimos el dato
            if (actual.getPnext() != null) { // Si hay un siguiente, ponemos la flecha
                System.out.print(" -> ");
            }
            actual = actual.getPnext(); // AVANZAMOS al siguiente nodo (¡crucial para no bucle infinito!)
        }
        System.out.println(" <- Final]");
}

}
