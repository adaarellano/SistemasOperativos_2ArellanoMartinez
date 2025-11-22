/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;
import Models.Bloque;
import edd.Cola;
import edd.ListaSimple;
import MainGUI.*;

/**
 * Gestiona el buffer de disco (caché) usando una política FIFO.
 * Almacena copias de bloques en memoria para reducir accesos al disco.
 */
public class BufferManager {
    // Límite de bloques en el caché (RAM)
    private static final int MAX_CACHE_SIZE = 10;
    
    // Almacén de bloques (para búsqueda rápida)
    private ListaSimple cacheStorage;
    // Cola para política FIFO (para saber cuál sacar)
    private Cola fifoQueue;
    
    private PanelConsola consola; // Para logs

    public BufferManager(PanelConsola consola) {
        this.cacheStorage = new ListaSimple();
        this.fifoQueue = new Cola();
        this.consola = consola;
        log("Buffer (Caché de Disco) inicializado. Política: FIFO, Tamaño: " + MAX_CACHE_SIZE);
    }
    
    private void log(String mensaje) {
        if (consola != null) {
            consola.agregarLinea("📀 [Buffer] " + mensaje);
        }
    }

    /**
     * Intenta leer un bloque desde el caché (RAM).
     * @param idBloque El ID del bloque a buscar.
     * @return El Bloque si está en caché (HIT), o null si no está (MISS).
     */
    public Bloque leerBloque(int idBloque) {
        for (int i = 0; i < cacheStorage.getSize(); i++) {
            Bloque bloque = (Bloque) cacheStorage.get(i);
            if (bloque.getIdBloque() == idBloque) {
                log("CACHE HIT! Bloque " + idBloque + " encontrado en RAM.");
                return bloque; // ¡Acierto! Lo encontramos
            }
        }
        
        log("CACHE MISS! Bloque " + idBloque + " no está en RAM.");
        return null; // ¡Fallo! No está en el caché
    }

    /**
     * Agrega un bloque al caché (generalmente después de un CACHE MISS).
     * @param bloque El bloque leído desde el disco.
     */
    public void agregarBloque(Bloque bloque) {
        if (bloque == null) return;
        
        // Si el bloque ya está en caché, no hacemos nada
        if (leerBloque(bloque.getIdBloque()) != null) {
            return;
        }

        log("Agregando bloque " + bloque.getIdBloque() + " al caché.");
        
        if (cacheStorage.getSize() >= MAX_CACHE_SIZE) {
            // El caché está lleno. Debemos sacar el más viejo (FIFO).
            Integer idMasViejo = (Integer) fifoQueue.desencolar();
            if (idMasViejo != null) {
                // Removerlo del almacenamiento
                removerBloquePorId(idMasViejo);
                log("Caché lleno. Eliminado bloque FIFO: " + idMasViejo);
            }
        }
        
        // Añadir el bloque nuevo (copia) al caché
        // Guardamos una copia para simular que está en RAM, 
        // separado del disco.
        cacheStorage.insertFinal(bloque.copiaProfunda());
        fifoQueue.encolar(bloque.getIdBloque());
    }
    
    /**
     * Método de ayuda para buscar y eliminar un bloque del ListaSimple
     */
    private void removerBloquePorId(int id) {
        for (int i = 0; i < cacheStorage.getSize(); i++) {
            Bloque bloque = (Bloque) cacheStorage.get(i);
            if (bloque.getIdBloque() == id) {
                cacheStorage.remove(bloque);
                return;
            }
        }
    }
}