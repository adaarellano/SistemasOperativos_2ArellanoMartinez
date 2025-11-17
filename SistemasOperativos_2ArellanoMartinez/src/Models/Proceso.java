/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Managers.ManejadorArchivo;
import java.util.concurrent.Semaphore;

/**
 * Clase que representa un proceso para operaciones del sistema de archivos
 * Adaptado para el simulador de sistema de archivos con gestión de E/S
 * @author TuNombre
 */
public class Proceso {
    private static int nextId = 1;
    
    // Identificación
    private String id;
    private String nombre;
    
    // Estado y control de ejecución
    private Estado estado;
    private Thread hiloEjecucion;
    private final Semaphore semaforoControl;
    private volatile boolean pausado;
    private volatile boolean ejecutando;
    
    // Operación específica del sistema de archivos
    private String tipoOperacion; // "CREAR", "LEER", "ACTUALIZAR", "ELIMINAR"
    private String rutaArchivo;   // Archivo/directorio objetivo
    private String usuario;       // Usuario que solicita la operación
    private int tamañoBloques;    // Solo para operaciones de creación
    private String datos;         // Datos para escritura/actualización
    
    // Gestión de E/S para el sistema de archivos
    private int tiempoESRestante; // Tiempo restante para operación de E/S
    private int duracionES;       // Duración total de la operación E/S
    
    // Métricas de tiempo
    private long tiempoLlegada;
    private long tiempoInicioEjecucion;
    private long tiempoFinalizacion;
    private long tiempoEjecucionTotal;
    
    // Referencia al manejador de archivos para ejecutar operaciones reales
    private transient ManejadorArchivo manejadorArchivo;
    
    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, TERMINADO
    }
    
    /**
     * Constructor para operaciones de creación
     */
    public Proceso(String tipoOperacion, String rutaArchivo, String usuario, int tamañoBloques) {
        this.id = "P" + nextId++;
        this.nombre = tipoOperacion + "_" + obtenerNombreArchivo(rutaArchivo);
        this.estado = Estado.NUEVO;
        this.tipoOperacion = tipoOperacion;
        this.rutaArchivo = rutaArchivo;
        this.usuario = usuario;
        this.tamañoBloques = tamañoBloques;
        this.datos = "";
        this.semaforoControl = new Semaphore(0);
        this.pausado = true;
        this.ejecutando = false;
        this.duracionES = calcularDuracionES(tipoOperacion, tamañoBloques);
        this.tiempoESRestante = 0;
        
        crearHiloEjecucion();
    }
    
    /**
     * Constructor para operaciones de lectura/actualización/eliminación
     */
    public Proceso(String tipoOperacion, String rutaArchivo, String usuario) {
        this(tipoOperacion, rutaArchivo, usuario, 0);
    }
    
    /**
     * Constructor para operaciones de actualización con datos
     */
    public Proceso(String tipoOperacion, String rutaArchivo, String usuario, String datos) {
        this(tipoOperacion, rutaArchivo, usuario, 0);
        this.datos = datos;
        this.duracionES = calcularDuracionES(tipoOperacion, datos.length() / 1024); // Estimación basada en tamaño de datos
    }
    
    private void crearHiloEjecucion() {
        hiloEjecucion = new Thread(() -> {
            System.out.println("Hilo de proceso iniciado: " + nombre);
            
            while (!Thread.currentThread().isInterrupted() && estado != Estado.TERMINADO) {
                try {
                    // Esperar permiso del ManejadorArchivo
                    semaforoControl.acquire();
                    
                    if (estado == Estado.EJECUTANDO && !pausado && !estaEnES()) {
                        ejecutarOperacionArchivo();
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("Hilo de proceso interrumpido: " + nombre);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en proceso " + nombre + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Hilo de proceso finalizado: " + nombre);
        });
        
        hiloEjecucion.setName("Proceso-" + id);
    }
    
    /**
     * Ejecuta la operación real sobre el sistema de archivos
     */
    private void ejecutarOperacionArchivo() {
        if (manejadorArchivo == null) {
            System.err.println("Error: ManejadorArchivo no configurado para " + nombre);
            estado = Estado.TERMINADO;
            return;
        }
        
        System.out.println("Proceso " + id + " ejecutando: " + tipoOperacion + " " + rutaArchivo);
        
        // Iniciar operación de E/S
        iniciarOperacionES();
        
        // Simular tiempo de procesamiento de E/S
        try {
            // Simular operación de E/S en chunks
            while (tiempoESRestante > 0 && !pausado && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(100); // Simular trabajo de E/S
                tiempoESRestante--;
                
                // Actualizar métricas
                tiempoEjecucionTotal++;
                
                // Notificar progreso (podría usarse para actualizar GUI)
                if (tiempoESRestante % 5 == 0) {
                    System.out.println(nombre + " - E/S en progreso: " + 
                                     (duracionES - tiempoESRestante) + "/" + duracionES);
                }
            }
            
            // Si no fue interrumpido, ejecutar la operación real
            if (!pausado && !Thread.currentThread().isInterrupted()) {
                ejecutarOperacionReal();
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }
    
    /**
     * Ejecuta la operación real en el ManejadorArchivo
     */
    private void ejecutarOperacionReal() {
        boolean exito = false;
        
        try {
            switch(tipoOperacion.toUpperCase()) {
                case "CREAR":
                    exito = manejadorArchivo.crearArchivo(rutaArchivo, tamañoBloques, usuario);
                    break;
                case "LEER":
                    String contenido = manejadorArchivo.leerArchivo(rutaArchivo, usuario);
                    exito = (contenido != null);
                    break;
                case "ACTUALIZAR":
                    exito = manejadorArchivo.actualizarArchivo(rutaArchivo, datos, usuario);
                    break;
                case "ELIMINAR":
                    exito = manejadorArchivo.eliminarArchivo(rutaArchivo, usuario);
                    break;
                default:
                    System.err.println("Operación no soportada: " + tipoOperacion);
            }
            
            if (exito) {
                System.out.println("Proceso " + id + " COMPLETADO con éxito: " + tipoOperacion);
            } else {
                System.err.println("Proceso " + id + " FALLÓ: " + tipoOperacion);
            }
            
        } catch (Exception e) {
            System.err.println("Error en operación " + tipoOperacion + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Finalizar proceso
            estado = Estado.TERMINADO;
            tiempoFinalizacion = System.currentTimeMillis();
            ejecutando = false;
        }
    }
    
    /**
     * Inicia una operación de E/S para este proceso
     */
    private void iniciarOperacionES() {
        this.tiempoESRestante = duracionES;
        this.estado = Estado.BLOQUEADO;
        System.out.println(nombre + " INICIA E/S - Bloqueado por " + duracionES + " unidades de tiempo");
    }
    
    /**
     * Calcula la duración de E/S basada en el tipo de operación y tamaño
     */
    private int calcularDuracionES(String tipoOperacion, int tamaño) {
        switch(tipoOperacion.toUpperCase()) {
            case "CREAR":
                return Math.max(3, tamaño / 2); // Más tiempo para crear archivos grandes
            case "LEER":
                return Math.max(2, tamaño / 4);
            case "ACTUALIZAR":
                return Math.max(4, tamaño / 2);
            case "ELIMINAR":
                return Math.max(1, tamaño / 8);
            default:
                return 5;
        }
    }
    
    /**
     * Extrae el nombre del archivo de la ruta completa
     */
    private String obtenerNombreArchivo(String ruta) {
        if (ruta == null || ruta.isEmpty()) return "unknown";
        String[] partes = ruta.split("/");
        return partes[partes.length - 1];
    }
    
    // ===== MÉTODOS DE CONTROL =====
    
    /**
     * El ManejadorArchivo da permiso para ejecutar
     */
    public void permitirEjecucion() {
        if (!pausado && estado == Estado.EJECUTANDO && !estaEnES()) {
            semaforoControl.release();
        }
    }
    
    /**
     * Inicia el proceso (lo pone en estado LISTO)
     */
    public void iniciar() {
        if (hiloEjecucion.getState() == Thread.State.NEW) {
            hiloEjecucion.start();
        }
        
        this.estado = Estado.LISTO;
        this.tiempoLlegada = System.currentTimeMillis();
        this.pausado = false; // <-- CORREGIDO
        this.ejecutando = false;
        
        System.out.println("Proceso INICIADO: " + nombre + " [LISTO]");
    }
    
    /**
     * Pausa la ejecución del proceso
     */
    public void pausar() {
        pausado = true;
        if (estado == Estado.EJECUTANDO) {
            estado = Estado.LISTO;
        }
        ejecutando = false;
        System.out.println("Proceso PAUSADO: " + nombre);
    }
    
    /**
     * Reanuda la ejecución del proceso
     */
    public void reanudar() {
        pausado = false;
        if (estado != Estado.TERMINADO && estado != Estado.BLOQUEADO) {
            estado = Estado.EJECUTANDO;
        }
        ejecutando = true;
        System.out.println("Proceso REANUDADO: " + nombre);
    }
    
    /**
     * Detiene completamente el proceso
     */
    public void detener() {
        ejecutando = false;
        pausado = true;
        estado = Estado.TERMINADO;
        
        if (hiloEjecucion != null && hiloEjecucion.isAlive()) {
            hiloEjecucion.interrupt();
        }
        
        tiempoFinalizacion = System.currentTimeMillis();
        System.out.println("Proceso DETENIDO: " + nombre);
    }
    
    /**
     * Configura el ManejadorArchivo para operaciones reales
     */
    public void setManejadorArchivo(ManejadorArchivo manejador) {
        this.manejadorArchivo = manejador;
    }
    
    // ===== MÉTODOS DE VERIFICACIÓN =====
    
    public boolean estaEnES() {
        return tiempoESRestante > 0;
    }
    
    public boolean estaTerminado() {
        return estado == Estado.TERMINADO;
    }
    
    public boolean estaEjecutando() {
        return ejecutando && !pausado && !estaEnES();
    }
    
    public boolean estaListo() {
        return estado == Estado.LISTO && !pausado && !estaEnES();
    }
    
    // ===== GETTERS Y SETTERS =====
    
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { 
        this.estado = estado; 
        if (estado == Estado.EJECUTANDO) {
            this.pausado = false;
            this.ejecutando = true;
            if (tiempoInicioEjecucion == 0) {
                this.tiempoInicioEjecucion = System.currentTimeMillis();
            }
        }
    }
    
    public String getTipoOperacion() { return tipoOperacion; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getUsuario() { return usuario; }
    public int getTamañoBloques() { return tamañoBloques; }
    public String getDatos() { return datos; }
    public int getTiempoESRestante() { return tiempoESRestante; }
    public int getDuracionES() { return duracionES; }
    public boolean isPausado() { return pausado; }
    
    // ===== MÉTRICAS =====
    
    public long getTiempoLlegada() { return tiempoLlegada; }
    public long getTiempoInicioEjecucion() { return tiempoInicioEjecucion; }
    public long getTiempoFinalizacion() { return tiempoFinalizacion; }
    public long getTiempoEjecucionTotal() { return tiempoEjecucionTotal; }
    
    public long getTiempoEspera() {
        if (tiempoInicioEjecucion == 0) return 0;
        return tiempoInicioEjecucion - tiempoLlegada;
    }
    
    public long getTiempoRetorno() {
        if (tiempoFinalizacion == 0) return 0;
        return tiempoFinalizacion - tiempoLlegada;
    }
    
    public double getPorcentajeCompletado() {
        if (duracionES == 0) return 0.0;
        return ((duracionES - tiempoESRestante) / (double) duracionES) * 100.0;
    }
    
    // ===== REPRESENTACIÓN =====
    
    @Override
    public String toString() {
        String estadoDesc = estado.toString();
        if (pausado) estadoDesc += " [PAUSADO]";
        if (estaEnES()) estadoDesc += " [E/S: " + tiempoESRestante + "]";
        
        return String.format("%s - %s (%s) [%s] [Usuario: %s]", 
            id, nombre, tipoOperacion, estadoDesc, usuario);
    }
    
    /**
     * Representación corta para tablas
     */
    public String toShortString() {
        return String.format("%s - %s", id, tipoOperacion);
    }
    
    /**
     * Información detallada para debugging
     */
    public String toDebugString() {
        return String.format(
            "Proceso{id=%s, nombre=%s, estado=%s, operacion=%s, archivo=%s, " +
            "usuario=%s, E/S=%d/%d, pausado=%s, ejecutando=%s}",
            id, nombre, estado, tipoOperacion, rutaArchivo, usuario,
            (duracionES - tiempoESRestante), duracionES, pausado, ejecutando
        );
    }
}