/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

public class SolicitudDisco {
    private static int nextId = 1;
    
    private int id;
    private Proceso procesoSolicitante;
    private String tipoOperacion; // "READ", "WRITE", "CREATE", "DELETE", "CREATE_DIR", "RENAME", "UPDATE"
    private int bloqueSolicitado;
    private int prioridad;
    
    // --- NUEVOS CAMPOS PARA MODO BATCH ---
    // Guardamos aquí los datos para ejecutarlos después
    private String rutaObjetivo;
    private String datosAdicionales; // Para contenido nuevo o nuevo nombre
    private int tamañoBloques;       // Para creación
    private boolean logicaEjecutada; // Saber si ya se hizo o falta hacer
    // -------------------------------------
    
    public SolicitudDisco(Proceso proceso, String tipoOperacion, int bloqueSolicitado) {
        this.id = nextId++;
        this.procesoSolicitante = proceso;
        this.tipoOperacion = tipoOperacion;
        this.bloqueSolicitado = bloqueSolicitado;
        this.prioridad = 1;
        this.logicaEjecutada = true; // Por defecto (Modo Normal) asumimos que ya se hizo la lógica
    }
    
    // Constructor completo para operaciones diferidas
    public SolicitudDisco(String tipo, String ruta, String datos, int tamaño, String usuario) {
        this.id = nextId++;
        this.tipoOperacion = tipo;
        this.rutaObjetivo = ruta;
        this.datosAdicionales = datos;
        this.tamañoBloques = tamaño;
        this.procesoSolicitante = new Proceso(tipo, ruta, usuario); // Proceso dummy
        this.bloqueSolicitado = 0; // Se calculará después
        this.logicaEjecutada = false; // IMPORTANTE: Falta ejecutar la lógica
    }
    
    public int calcularDistancia(int otroBloque) {
        return Math.abs(this.bloqueSolicitado - otroBloque);
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public Proceso getProcesoSolicitante() { return procesoSolicitante; }
    public String getTipoOperacion() { return tipoOperacion; }
    public int getBloqueSolicitado() { return bloqueSolicitado; }
    public void setBloqueSolicitado(int b) { this.bloqueSolicitado = b; }
    
    // Getters nuevos
    public String getRutaObjetivo() { return rutaObjetivo; }
    public String getDatosAdicionales() { return datosAdicionales; }
    public int getTamañoBloques() { return tamañoBloques; }
    public boolean isLogicaEjecutada() { return logicaEjecutada; }
    public void setLogicaEjecutada(boolean ejecutada) { this.logicaEjecutada = ejecutada; }
    
    @Override
    public String toString() {
        return String.format("Solicitud%d [%s] Bloque:%d", id, tipoOperacion, bloqueSolicitado);
    }
}