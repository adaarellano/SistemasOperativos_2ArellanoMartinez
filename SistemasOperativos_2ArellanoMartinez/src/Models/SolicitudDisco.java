/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 * Clase que representa una solicitud de E/S al disco
 * Usada por el planificador de disco
 */
public class SolicitudDisco {
    private static int nextId = 1;
    
    private int id;
    private Proceso procesoSolicitante;
    private String tipoOperacion; // "READ", "WRITE", "CREATE", "DELETE"
    private int bloqueSolicitado; // Número de bloque en el disco
    private int prioridad;
    private long tiempoLlegada;
    private Estado estado;
    
    public enum Estado {
        PENDIENTE, EN_EJECUCION, COMPLETADA, CANCELADA
    }
    
    public SolicitudDisco(Proceso proceso, String tipoOperacion, int bloqueSolicitado) {
        this.id = nextId++;
        this.procesoSolicitante = proceso;
        this.tipoOperacion = tipoOperacion;
        this.bloqueSolicitado = bloqueSolicitado;
        this.prioridad = 1; // Prioridad por defecto
        this.tiempoLlegada = System.currentTimeMillis();
        this.estado = Estado.PENDIENTE;
    }
    
    // Calcula la distancia a otro bloque (para SSTF)
    public int calcularDistancia(int otroBloque) {
        return Math.abs(this.bloqueSolicitado - otroBloque);
    }
    
    // Getters
    public int getId() { return id; }
    public Proceso getProcesoSolicitante() { return procesoSolicitante; }
    public String getTipoOperacion() { return tipoOperacion; }
    public int getBloqueSolicitado() { return bloqueSolicitado; }
    public int getPrioridad() { return prioridad; }
    public long getTiempoLlegada() { return tiempoLlegada; }
    public Estado getEstado() { return estado; }
    
    public void setEstado(Estado estado) { this.estado = estado; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }
    
    @Override
    public String toString() {
        return String.format("Solicitud%d [%s] Bloque:%d Proceso:%s", 
            id, tipoOperacion, bloqueSolicitado, procesoSolicitante.getNombre());
    }
}