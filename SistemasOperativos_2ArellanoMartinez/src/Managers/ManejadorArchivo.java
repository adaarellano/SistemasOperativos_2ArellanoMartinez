/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import Models.*;
import Planificador.FIFO;
import Planificador.SSTF;
import Planificador.SCAN;
import Planificador.CSCAN;
import Planificador.PlanificadorDisco;
import edd.ListaSimple;
import java.util.*;

/**
 * Manejador principal del sistema de archivos
 * Controla todo: archivos, directorios, procesos y planificación de disco
 */
public class ManejadorArchivo {
    // === ALMACENAMIENTO ===
    private Bloque[] bloquesDisco;
    private Directorio raiz;
    private int totalBloques = 100;
    
    // === USUARIOS Y PERMISOS ===
    private boolean esModoAdministrador = true;
    private String usuarioActual = "admin";
    private ListaSimple usuarios; // Lista de usuarios del sistema
    
    // === PROCESOS ===
    private ListaSimple colaProcesos;
    private ListaSimple procesosActivos;
    
    // === PLANIFICACIÓN DE DISCO ===
    private PlanificadorDisco planificadorActual;
    private ListaSimple solicitudesDisco;
    
    // === ESTADÍSTICAS ===
    private int archivosCreados = 0;
    private int archivosEliminados = 0;
    private int operacionesRealizadas = 0;
    
    public ManejadorArchivo() {
        inicializarSistema();
    }
    
    private void inicializarSistema() {
        // 1. Inicializar disco
        bloquesDisco = new Bloque[totalBloques];
        for (int i = 0; i < totalBloques; i++) {
            bloquesDisco[i] = new Bloque(i);
        }
        
        // 2. Inicializar directorio raíz
        raiz = new Directorio("/");
        
        // 3. Inicializar colas
        colaProcesos = new ListaSimple();
        procesosActivos = new ListaSimple();
        solicitudesDisco = new ListaSimple();
        usuarios = new ListaSimple();
        
        // 4. Crear usuarios por defecto
        crearUsuariosPorDefecto();
        
        // 5. Inicializar planificador (FIFO por defecto)
        planificadorActual = new FIFO();
        
        System.out.println("Sistema de archivos inicializado correctamente");
    }
    
    /**
     * Crea usuarios por defecto del sistema
     */
    private void crearUsuariosPorDefecto() {
        usuarios.insertFinal(new Usuario("admin", "admin"));
        usuarios.insertFinal(new Usuario("usuario1", "usuario"));
        usuarios.insertFinal(new Usuario("usuario2", "usuario"));
        System.out.println("Usuarios por defecto creados");
    }
    
    // ===== OPERACIONES PRINCIPALES DE ARCHIVOS =====
    
    /**
     * Crea un nuevo archivo en el sistema
     */
    public boolean crearArchivo(String ruta, int tamañoBloques, String usuario) {
        if (!verificarPermisosEscritura(usuario)) {
            System.err.println("Sin permisos para crear archivo: " + usuario);
            return false;
        }
        
        if (!hayEspacioSuficiente(tamañoBloques)) {
            System.err.println("No hay espacio suficiente para " + tamañoBloques + " bloques");
            return false;
        }
        
        try {
            // Extraer directorio y nombre de archivo de la ruta
            String[] partes = ruta.split("/");
            String nombreArchivo = partes[partes.length - 1];
            String rutaDirectorio = obtenerRutaDirectorio(ruta);
            
            // Buscar o crear directorio
            Directorio directorioDestino = buscarOCrearDirectorio(rutaDirectorio);
            if (directorioDestino == null) {
                System.err.println("No se pudo encontrar/crear el directorio: " + rutaDirectorio);
                return false;
            }
            
            // Crear archivo
            Archivo nuevoArchivo = new Archivo(nombreArchivo, tamañoBloques, usuario, ruta);
            
            // Asignar bloques al archivo
            if (!asignarBloquesArchivo(nuevoArchivo, tamañoBloques)) {
                System.err.println("No se pudieron asignar bloques al archivo");
                return false;
            }
            
            // Agregar archivo al directorio
            boolean exito = directorioDestino.agregarArchivo(nuevoArchivo);
            if (exito) {
                archivosCreados++;
                operacionesRealizadas++;
                System.out.println("Archivo creado exitosamente: " + ruta + " (" + tamañoBloques + " bloques)");
                
                // Crear solicitud de disco para la operación
                crearSolicitudDisco("CREATE", nuevoArchivo.getPrimerBloque().getIdBloque(), usuario);
                
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error al crear archivo " + ruta + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Lee el contenido de un archivo
     */
    public String leerArchivo(String ruta, String usuario) {
        if (!verificarPermisosLectura(usuario)) {
            return "Acceso denegado";
        }
        
        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) {
            return "Archivo no encontrado: " + ruta;
        }
        
        if (!archivo.getUsuarioPropietario().equals(usuario) && !esModoAdministrador) {
            return "No tiene permisos para leer este archivo";
        }
        
        // Crear solicitud de disco para lectura
        if (archivo.getPrimerBloque() != null) {
            crearSolicitudDisco("READ", archivo.getPrimerBloque().getIdBloque(), usuario);
        }
        
        operacionesRealizadas++;
        return archivo.leerContenido();
    }
    
    /**
     * Actualiza el contenido de un archivo
     */
    public boolean actualizarArchivo(String ruta, String datos, String usuario) {
        if (!verificarPermisosEscritura(usuario)) {
            return false;
        }
        
        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) {
            System.err.println("Archivo no encontrado: " + ruta);
            return false;
        }
        
        if (!archivo.getUsuarioPropietario().equals(usuario) && !esModoAdministrador) {
            System.err.println("No tiene permisos para modificar este archivo");
            return false;
        }
        
        boolean exito = archivo.escribirContenido(datos);
        if (exito) {
            // Crear solicitud de disco para escritura
            if (archivo.getPrimerBloque() != null) {
                crearSolicitudDisco("WRITE", archivo.getPrimerBloque().getIdBloque(), usuario);
            }
            
            operacionesRealizadas++;
            System.out.println("Archivo actualizado: " + ruta);
        }
        
        return exito;
    }
    
    /**
     * Elimina un archivo del sistema
     */
    public boolean eliminarArchivo(String ruta, String usuario) {
        if (!verificarPermisosEscritura(usuario)) {
            return false;
        }
        
        try {
            String[] partes = ruta.split("/");
            String nombreArchivo = partes[partes.length - 1];
            String rutaDirectorio = obtenerRutaDirectorio(ruta);
            
            Directorio directorio = raiz.buscarDirectorioRecursivo(rutaDirectorio);
            if (directorio == null) {
                System.err.println("Directorio no encontrado: " + rutaDirectorio);
                return false;
            }
            
            Archivo archivo = directorio.buscarArchivo(nombreArchivo);
            if (archivo == null) {
                System.err.println("Archivo no encontrado: " + nombreArchivo);
                return false;
            }
            
            if (!archivo.getUsuarioPropietario().equals(usuario) && !esModoAdministrador) {
                System.err.println("No tiene permisos para eliminar este archivo");
                return false;
            }
            
            // Liberar bloques del archivo
            liberarBloquesArchivo(archivo);
            
            // Eliminar archivo del directorio
            boolean exito = directorio.eliminarArchivo(nombreArchivo);
            if (exito) {
                archivosEliminados++;
                operacionesRealizadas++;
                
                // Crear solicitud de disco para eliminación
                crearSolicitudDisco("DELETE", 0, usuario); // Bloque 0 para eliminación
                
                System.out.println("Archivo eliminado: " + ruta);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo " + ruta + ": " + e.getMessage());
        }
        
        return false;
    }
    
    // ===== GESTIÓN DE BLOQUES =====
    
    /**
     * Asigna bloques a un archivo usando asignación encadenada
     */
    private boolean asignarBloquesArchivo(Archivo archivo, int cantidadBloques) {
        if (cantidadBloques <= 0) {
            return false;
        }
        
        ListaSimple bloquesAsignados = new ListaSimple();
        
        // Buscar bloques libres
        for (int i = 0; i < bloquesDisco.length && bloquesAsignados.getSize() < cantidadBloques; i++) {
            if (bloquesDisco[i].estaLibre()) {
                bloquesAsignados.insertFinal(bloquesDisco[i]);
            }
        }
        
        // Verificar si encontramos suficientes bloques
        if (bloquesAsignados.getSize() < cantidadBloques) {
            System.err.println("Solo se encontraron " + bloquesAsignados.getSize() + " bloques libres de " + cantidadBloques + " requeridos");
            return false;
        }
        
        // Configurar la cadena de bloques
        Bloque primerBloque = (Bloque) bloquesAsignados.get(0);
        archivo.setPrimerBloque(primerBloque);
        
        Bloque actual = primerBloque;
        for (int i = 1; i < bloquesAsignados.getSize(); i++) {
            Bloque siguiente = (Bloque) bloquesAsignados.get(i);
            actual.setSiguienteBloque(siguiente);
            actual = siguiente;
        }
        
        // Marcar bloques como ocupados
        for (int i = 0; i < bloquesAsignados.getSize(); i++) {
            Bloque bloque = (Bloque) bloquesAsignados.get(i);
            bloque.ocuparBloque(archivo.getNombre(), -1); // -1 para proceso del sistema
        }
        
        System.out.println("Asignados " + cantidadBloques + " bloques al archivo: " + archivo.getNombre());
        return true;
    }
    
    /**
     * Libera los bloques asignados a un archivo
     */
    private void liberarBloquesArchivo(Archivo archivo) {
        Bloque actual = archivo.getPrimerBloque();
        int bloquesLiberados = 0;
        
        while (actual != null) {
            actual.liberarBloque();
            bloquesLiberados++;
            actual = actual.getSiguienteBloque();
        }
        
        System.out.println("Liberados " + bloquesLiberados + " bloques del archivo: " + archivo.getNombre());
    }
    
    /**
     * Verifica si hay espacio suficiente en el disco
     */
    private boolean hayEspacioSuficiente(int tamañoNecesario) {
        int bloquesLibres = 0;
        for (Bloque bloque : bloquesDisco) {
            if (bloque.estaLibre()) {
                bloquesLibres++;
            }
        }
        return bloquesLibres >= tamañoNecesario;
    }
    
    // ===== GESTIÓN DE SOLICITUDES DE DISCO =====
    
    /**
     * Crea una nueva solicitud de disco
     */
    private void crearSolicitudDisco(String tipoOperacion, int bloque, String usuario) {
        // En una implementación real, aquí crearíamos una SolicitudDisco
        // y la agregaríamos al planificador
        System.out.println("Solicitud de disco creada: " + tipoOperacion + " bloque " + bloque + " por " + usuario);
        
        // Para simulación, procesamos inmediatamente
        // En la versión final, esto iría a la cola del planificador
        procesarSolicitudDisco(tipoOperacion, bloque);
    }
    
    /**
     * Procesa una solicitud de disco (simulación)
     */
    private void procesarSolicitudDisco(String tipoOperacion, int bloque) {
        System.out.println("Procesando solicitud de disco: " + tipoOperacion + " en bloque " + bloque);
        // Simular tiempo de E/S
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // ===== GESTIÓN DE PROCESOS =====
    
    public void solicitarOperacion(String tipoOperacion, String ruta, String usuario, int tamaño) {
        Proceso proceso = new Proceso(tipoOperacion, ruta, usuario, tamaño);
        proceso.setManejadorArchivo(this);
        colaProcesos.insertFinal(proceso);
        proceso.iniciar();
        
        planificarProcesos();
    }
    
    private void planificarProcesos() {
        // Lógica simple de planificación: tomar el primer proceso listo
        for (int i = 0; i < colaProcesos.getSize(); i++) {
            Proceso proceso = (Proceso) colaProcesos.get(i);
            if (proceso.estaListo()) {
                proceso.setEstado(Proceso.Estado.EJECUTANDO);
                proceso.permitirEjecucion();
                
                // Mover a procesos activos
                procesosActivos.insertFinal(proceso);
                colaProcesos.remove(proceso);
                break;
            }
        }
    }
    
    // ===== BÚSQUEDAS Y NAVEGACIÓN =====
    
    /**
     * Busca un archivo por ruta completa
     */
    public Archivo buscarArchivo(String ruta) {
        String[] partes = ruta.split("/");
        String nombreArchivo = partes[partes.length - 1];
        String rutaDirectorio = obtenerRutaDirectorio(ruta);
        
        Directorio directorio = raiz.buscarDirectorioRecursivo(rutaDirectorio);
        if (directorio != null) {
            return directorio.buscarArchivo(nombreArchivo);
        }
        
        return null;
    }
    
    /**
     * Busca o crea un directorio en la ruta especificada
     */
    private Directorio buscarOCrearDirectorio(String ruta) {
        if (ruta == null || ruta.isEmpty() || ruta.equals("/")) {
            return raiz;
        }
        
        Directorio directorio = raiz.buscarDirectorioRecursivo(ruta);
        if (directorio != null) {
            return directorio;
        }
        
        // Crear directorios recursivamente
        String[] partes = ruta.split("/");
        Directorio actual = raiz;
        
        for (String nombreDir : partes) {
            if (nombreDir.isEmpty()) continue; // Saltar elemento vacío
            
            Directorio subdir = actual.buscarSubdirectorio(nombreDir);
            if (subdir == null) {
                subdir = actual.crearSubdirectorio(nombreDir);
                if (subdir == null) {
                    return null; // Error al crear directorio
                }
            }
            actual = subdir;
        }
        
        return actual;
    }
    
    /**
     * Extrae la ruta del directorio de una ruta completa de archivo
     */
    private String obtenerRutaDirectorio(String rutaArchivo) {
        int lastSlash = rutaArchivo.lastIndexOf('/');
        if (lastSlash <= 0) {
            return "/";
        }
        return rutaArchivo.substring(0, lastSlash);
    }
    
    // ===== VERIFICACIÓN DE PERMISOS =====
    
    private boolean verificarPermisosLectura(String usuario) {
        // Simplificado: en modo administrador o usuario válido
        return esModoAdministrador || buscarUsuario(usuario) != null;
    }
    
    private boolean verificarPermisosEscritura(String usuario) {
        // Solo administradores pueden escribir (por ahora)
        return esModoAdministrador;
    }
    
    private Usuario buscarUsuario(String username) {
        for (int i = 0; i < usuarios.getSize(); i++) {
            Usuario usuario = (Usuario) usuarios.get(i);
            if (usuario.getUsername().equals(username)) {
                return usuario;
            }
        }
        return null;
    }
    
    // ===== CONFIGURACIÓN DEL SISTEMA =====
    
    public void setModoAdministrador(boolean esAdmin) {
        this.esModoAdministrador = esAdmin;
        this.usuarioActual = esAdmin ? "admin" : "usuario1";
        System.out.println("Modo cambiado a: " + (esAdmin ? "Administrador" : "Usuario"));
    }
    
    public void cambiarPlanificador(String tipoPlanificador) {
        switch(tipoPlanificador.toUpperCase()) {
            case "FIFO":
                planificadorActual = new FIFO();
                break;
            case "SSTF":
                planificadorActual = new SSTF();
                break;
            case "SCAN":
                planificadorActual = new SCAN();
                break;
            case "C-SCAN":
                planificadorActual = new CSCAN();
                break;
            default:
                System.err.println("Planificador no válido: " + tipoPlanificador);
                return;
        }
        System.out.println("Planificador cambiado a: " + planificadorActual.getNombrePolitica());
    }
    
    // ===== GETTERS PARA LA INTERFAZ GRÁFICA =====
    
    public Bloque[] getBloquesDisco() {
        return bloquesDisco;
    }
    
    public Directorio getRaiz() {
        return raiz;
    }
    
    public ListaSimple getProcesosActivos() {
        return procesosActivos;
    }
    
    public ListaSimple getColaProcesos() {
        return colaProcesos;
    }
    
    public ListaSimple getSolicitudesDisco() {
        return solicitudesDisco;
    }
    
    public PlanificadorDisco getPlanificadorActual() {
        return planificadorActual;
    }
    
    public boolean esAdministrador() {
        return esModoAdministrador;
    }
    
    public String getUsuarioActual() {
        return usuarioActual;
    }
    
    public int getTotalBloques() {
        return totalBloques;
    }
    
    public int getBloquesOcupados() {
        int ocupados = 0;
        for (Bloque bloque : bloquesDisco) {
            if (bloque.estaOcupado()) {
                ocupados++;
            }
        }
        return ocupados;
    }
    
    public int getArchivosCreados() {
        return archivosCreados;
    }
    
    public int getOperacionesRealizadas() {
        return operacionesRealizadas;
    }
    
    // ===== INFORMACIÓN DEL SISTEMA =====
    
    public String getEstadoSistema() {
        return String.format(
            "=== ESTADO DEL SISTEMA ===\n" +
            "Usuario: %s (%s)\n" +
            "Archivos creados: %d\n" +
            "Archivos eliminados: %d\n" +
            "Operaciones realizadas: %d\n" +
            "Bloques ocupados: %d/%d\n" +
            "Planificador: %s\n" +
            "Procesos activos: %d\n" +
            "Procesos en cola: %d",
            usuarioActual, esModoAdministrador ? "Admin" : "Usuario",
            archivosCreados, archivosEliminados, operacionesRealizadas,
            getBloquesOcupados(), totalBloques,
            planificadorActual.getNombrePolitica(),
            procesosActivos.getSize(), colaProcesos.getSize()
        );
    }
}