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
import MainGUI.PanelConsola;
import MainGUI.PanelOutput;
import java.util.*;

/**
 * Manejador principal del sistema de archivos
 * Controla todo: archivos, directorios, procesos y planificación de disco
 * VERSIÓN MODIFICADA PARA INTERFAZ GRÁFICA
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
    
    private PanelOutput panelOutput;
    
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
    
    // === REFERENCIA A INTERFAZ ===
    private PanelConsola panelConsola;
    private boolean esAdministrador;
    
    public ManejadorArchivo() {
        inicializarSistema();
    }
    
    public ManejadorArchivo(PanelConsola panelConsola) {
        this.panelConsola = panelConsola;
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
        
        logConsola("=== SISTEMA DE ARCHIVOS INICIALIZADO ===");
        logConsola("Total bloques: " + totalBloques);
        logConsola("Planificador por defecto: FIFO");
        logConsola("Modo inicial: Administrador");
    }
    
    /**
     * Crea usuarios por defecto del sistema
     */
    private void crearUsuariosPorDefecto() {
        usuarios.insertFinal(new Usuario("admin", "admin"));
        usuarios.insertFinal(new Usuario("usuario1", "usuario"));
        usuarios.insertFinal(new Usuario("usuario2", "usuario"));
        logConsola("Usuarios creados: admin, usuario1, usuario2");
    }
    
    // ===== OPERACIONES PRINCIPALES DE ARCHIVOS =====
    
    /**
     * Crea un nuevo archivo en el sistema - CON LOGS MEJORADOS
     */
    public boolean crearArchivo(String ruta, int tamañoBloques, String usuario) {
        logConsola("=== SOLICITANDO CREACIÓN DE ARCHIVO ===");
        logConsola("Ruta: " + ruta);
        logConsola("Tamaño solicitado: " + tamañoBloques + " bloques");
        logConsola("Usuario: " + usuario);
        
        if (!verificarPermisosEscritura(usuario)) {
            logConsola("ERROR: Sin permisos para crear archivo");
            return false;
        }
        
        if (!hayEspacioSuficiente(tamañoBloques)) {
            logConsola("ERROR: No hay espacio suficiente");
            return false;
        }
        
        try {
            // Extraer directorio y nombre de archivo de la ruta
            String[] partes = ruta.split("/");
            String nombreArchivo = partes[partes.length - 1];
            String rutaDirectorio = obtenerRutaDirectorio(ruta);
            
            logConsola("Buscando directorio: " + rutaDirectorio);
            
            // Buscar o crear directorio
            Directorio directorioDestino = buscarOCrearDirectorio(rutaDirectorio);
            if (directorioDestino == null) {
                logConsola("ERROR: No se pudo encontrar/crear el directorio");
                return false;
            }
            
            logConsola("Creando archivo: " + nombreArchivo);
            // Crear archivo
            Archivo nuevoArchivo = new Archivo(nombreArchivo, tamañoBloques, usuario, ruta);
            
            logConsola("Asignando " + tamañoBloques + " bloques...");
            // Asignar bloques al archivo
            if (!asignarBloquesArchivo(nuevoArchivo, tamañoBloques)) {
                logConsola("ERROR: No se pudieron asignar bloques");
                return false;
            }
            
            logConsola("Agregando archivo al directorio...");
            // Agregar archivo al directorio
            boolean exito = directorioDestino.agregarArchivo(nuevoArchivo);
            if (exito) {
                archivosCreados++;
                operacionesRealizadas++;
                
                logConsola("✅ ARCHIVO CREADO EXITOSAMENTE");
                logConsola("   Nombre: " + nombreArchivo);
                logConsola("   Bloques reservados: " + tamañoBloques);
                logConsola("   Cadena de bloques: " + nuevoArchivo.getInfoBloques());
                
                // Crear solicitud de disco para la operación
                crearSolicitudDisco("CREATE", nuevoArchivo.getPrimerBloque().getIdBloque(), usuario);
                
                return true;
            } else {
                logConsola("❌ ERROR: No se pudo agregar el archivo al directorio");
            }
            
        } catch (Exception e) {
            logConsola("❌ ERROR EXCEPCIÓN: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Lee el contenido de un archivo - CON LOGS MEJORADOS
     */
    public String leerArchivo(String ruta, String usuario) {
        logConsola("=== SOLICITANDO LECTURA DE ARCHIVO ===");
        logConsola("Ruta: " + ruta);
        logConsola("Usuario: " + usuario);
        
        if (!verificarPermisosLectura(usuario)) {
            logConsola("❌ ERROR: Acceso denegado");
            return "Acceso denegado";
        }
        
        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) {
            logConsola("❌ ERROR: Archivo no encontrado");
            return "Archivo no encontrado: " + ruta;
        }
        
        if (!archivo.getUsuarioPropietario().equals(usuario) && !esModoAdministrador) {
            logConsola("❌ ERROR: No tiene permisos para leer este archivo");
            return "No tiene permisos para leer este archivo";
        }
        
        logConsola("Archivo encontrado: " + archivo.getNombre());
        logConsola("Tamaño: " + archivo.getTamañoBytes() + " bytes");
        
        // Crear solicitud de disco para lectura
        if (archivo.getPrimerBloque() != null) {
            crearSolicitudDisco("READ", archivo.getPrimerBloque().getIdBloque(), usuario);
        }
        
        operacionesRealizadas++;
        
        String contenido = archivo.leerContenido();
        logConsola("✅ CONTENIDO LEÍDO: '" + contenido + "'");
        
        return contenido;
    }
    
    /**
     * Actualiza el contenido de un archivo - CON LOGS MEJORADOS
     */
    public boolean actualizarArchivo(String ruta, String datos, String usuario) {
        logConsola("=== SOLICITANDO ACTUALIZACIÓN DE ARCHIVO ===");
        logConsola("Ruta: " + ruta);
        logConsola("Usuario: " + usuario);
        logConsola("Nuevos datos (" + datos.length() + " caracteres)");
        
        if (!verificarPermisosEscritura(usuario)) {
            logConsola("❌ ERROR: Sin permisos de escritura");
            return false;
        }
        
        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) {
            logConsola("❌ ERROR: Archivo no encontrado");
            return false;
        }
        
        if (!archivo.getUsuarioPropietario().equals(usuario) && !esModoAdministrador) {
            logConsola("❌ ERROR: No tiene permisos para modificar este archivo");
            return false;
        }
        
        logConsola("Archivo encontrado: " + archivo.getNombre());
        
        boolean exito = archivo.escribirContenido(datos);
        if (exito) {
            logConsola("✅ ARCHIVO ACTUALIZADO EXITOSAMENTE");
            logConsola("   Nuevo tamaño: " + archivo.getTamañoBytes() + " bytes");
            logConsola("   Bloques usados: " + archivo.getTamañoBloques() + "/" + archivo.getBloquesReservados());
            
            // Crear solicitud de disco para escritura
            if (archivo.getPrimerBloque() != null) {
                crearSolicitudDisco("WRITE", archivo.getPrimerBloque().getIdBloque(), usuario);
            }
            
            operacionesRealizadas++;
        } else {
            logConsola("❌ ERROR: No se pudo actualizar el archivo");
        }
        
        return exito;
    }
    
    /**
     * Elimina un archivo del sistema - CON LOGS MEJORADOS
     */
    public boolean eliminarArchivo(String ruta, String usuario) {
        logConsola("=== SOLICITANDO ELIMINACIÓN DE ARCHIVO ===");
        logConsola("Ruta: " + ruta);
        logConsola("Usuario: " + usuario);
        
        if (!verificarPermisosEscritura(usuario)) {
            logConsola("❌ ERROR: Sin permisos de escritura");
            return false;
        }
        
        try {
            String[] partes = ruta.split("/");
            String nombreArchivo = partes[partes.length - 1];
            String rutaDirectorio = obtenerRutaDirectorio(ruta);
            
            logConsola("Buscando directorio: " + rutaDirectorio);
            Directorio directorio = raiz.buscarDirectorioRecursivo(rutaDirectorio);
            if (directorio == null) {
                logConsola("❌ ERROR: Directorio no encontrado");
                return false;
            }
            
            logConsola("Buscando archivo: " + nombreArchivo);
            Archivo archivo = directorio.buscarArchivo(nombreArchivo);
            if (archivo == null) {
                logConsola("❌ ERROR: Archivo no encontrado");
                return false;
            }
            
            if (!archivo.getUsuarioPropietario().equals(usuario) && !esModoAdministrador) {
                logConsola("❌ ERROR: No tiene permisos para eliminar este archivo");
                return false;
            }
            
            logConsola("Liberando bloques del archivo...");
            // Liberar bloques del archivo
            liberarBloquesArchivo(archivo);
            
            logConsola("Eliminando archivo del directorio...");
            // Eliminar archivo del directorio
            boolean exito = directorio.eliminarArchivo(nombreArchivo);
            if (exito) {
                archivosEliminados++;
                operacionesRealizadas++;
                
                logConsola("✅ ARCHIVO ELIMINADO EXITOSAMENTE");
                logConsola("   Nombre: " + nombreArchivo);
                logConsola("   Bloques liberados: " + archivo.getBloquesReservados());
                
                // Crear solicitud de disco para eliminación
                crearSolicitudDisco("DELETE", 0, usuario);
                
                return true;
            } else {
                logConsola("❌ ERROR: No se pudo eliminar el archivo del directorio");
            }
            
        } catch (Exception e) {
            logConsola("❌ ERROR EXCEPCIÓN: " + e.getMessage());
        }
        
        return false;
    }
    
    // ===== GESTIÓN DE BLOQUES =====
    
    /**
     * Asigna bloques a un archivo usando asignación encadenada - CON LOGS
     */
    private boolean asignarBloquesArchivo(Archivo archivo, int cantidadBloques) {
        logConsola("Asignando " + cantidadBloques + " bloques...");
        
        if (cantidadBloques <= 0) {
            logConsola("❌ ERROR: Cantidad de bloques inválida");
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
            logConsola("❌ ERROR: Solo " + bloquesAsignados.getSize() + " bloques libres de " + cantidadBloques + " requeridos");
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
            bloque.ocuparBloque(archivo.getNombre(), -1);
        }
        
        logConsola("✅ " + cantidadBloques + " bloques asignados exitosamente");
        return true;
    }
    
    public void setEsAdministrador(boolean esAdmin) {
        this.esAdministrador = esAdmin;
    }
    
    
    /**
     * Libera los bloques asignados a un archivo - CON LOGS
     */
    private void liberarBloquesArchivo(Archivo archivo) {
        Bloque actual = archivo.getPrimerBloque();
        int bloquesLiberados = 0;
        
        while (actual != null) {
            actual.liberarBloque();
            bloquesLiberados++;
            actual = actual.getSiguienteBloque();
        }
        
        logConsola("✅ " + bloquesLiberados + " bloques liberados");
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
        
        logConsola("Bloques libres: " + bloquesLibres + "/" + totalBloques);
        logConsola("Bloques necesarios: " + tamañoNecesario);
        
        return bloquesLibres >= tamañoNecesario;
    }
    
    // ===== GESTIÓN DE SOLICITUDES DE DISCO =====
    
    /**
     * Crea una nueva solicitud de disco - CON LOGS MEJORADOS
     */
    private void crearSolicitudDisco(String tipoOperacion, int bloque, String usuario) {
        logConsola("📋 CREANDO SOLICITUD DE DISCO");
        logConsola("   Tipo: " + tipoOperacion);
        logConsola("   Bloque: " + bloque);
        logConsola("   Usuario: " + usuario);
        logConsola("   Planificador: " + planificadorActual.getNombrePolitica());
        
        // Simular procesamiento
        procesarSolicitudDisco(tipoOperacion, bloque);
    }
    
    /**
     * Procesa una solicitud de disco (simulación) - CON LOGS MEJORADOS
     */
    private void procesarSolicitudDisco(String tipoOperacion, int bloque) {
        logConsola("⚡ PROCESANDO SOLICITUD DE DISCO");
        logConsola("   Operación: " + tipoOperacion);
        logConsola("   Bloque objetivo: " + bloque);
        
        // Simular tiempo de E/S
        try {
            logConsola("   Simulando tiempo de E/S...");
            Thread.sleep(800); // Más tiempo para ver en la interfaz
            logConsola("   ✅ Operación completada");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logConsola("   ❌ Operación interrumpida");
        }
    }
    
    // ===== MÉTODOS DE LOGGING MEJORADOS =====
    
    /**
     * Método para enviar mensajes a la consola
     */
    private void logConsola(String mensaje) {
        if (panelConsola != null) {
            panelConsola.agregarLinea(mensaje);
        } else {
            System.out.println(mensaje); // Fallback a consola normal
        }
        
        // Pequeña pausa para ver línea por línea en la interfaz
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // ===== EL RESTO DE LOS MÉTODOS SE MANTIENEN IGUAL =====
    
    // ===== GESTIÓN DE PROCESOS =====
    
    public void solicitarOperacion(String tipoOperacion, String ruta, String usuario, int tamaño) {
        logConsola("🔄 SOLICITANDO OPERACIÓN: " + tipoOperacion + " " + ruta);
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
                logConsola("Proceso en ejecución: " + proceso.getNombre());
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
            if (nombreDir.isEmpty()) continue;
            
            Directorio subdir = actual.buscarSubdirectorio(nombreDir);
            if (subdir == null) {
                subdir = actual.crearSubdirectorio(nombreDir);
                if (subdir == null) {
                    return null;
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
        return esModoAdministrador || buscarUsuario(usuario) != null;
    }
    
    private boolean verificarPermisosEscritura(String usuario) {
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
        logConsola("=== CAMBIO DE MODO ===");
        logConsola("Nuevo modo: " + (esAdmin ? "ADMINISTRADOR" : "USUARIO"));
        logConsola("Usuario actual: " + usuarioActual);
    }
    
    public void cambiarPlanificador(String tipoPlanificador) {
        logConsola("=== CAMBIANDO PLANIFICADOR ===");
        logConsola("Planificador anterior: " + planificadorActual.getNombrePolitica());
        
        switch(tipoPlanificador.toUpperCase()) {
            case "FIFO":
                planificadorActual = new FIFO();
                logConsola("Nuevo planificador: FIFO (First-In-First-Out)");
                break;
            case "SSTF":
                planificadorActual = new SSTF();
                logConsola("Nuevo planificador: SSTF (Shortest Seek Time First)");
                logConsola("  - Elige siempre la solicitud más cercana al cabezal");
                break;
            case "SCAN":
                planificadorActual = new SCAN();
                logConsola("Nuevo planificador: SCAN (Elevator Algorithm)");
                logConsola("  - Funciona como un ascensor, va y viene");
                break;
            case "C-SCAN":
                planificadorActual = new CSCAN();
                logConsola("Nuevo planificador: C-SCAN (Circular SCAN)");
                logConsola("  - Siempre en una dirección, vuelve al inicio");
                break;
            default:
                logConsola("❌ ERROR: Planificador no válido: " + tipoPlanificador);
                return;
        }
        
        logConsola("✅ Planificador cambiado exitosamente");
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
    
     public void setPanelOutput(PanelOutput panelOutput) {
        this.panelOutput = panelOutput;
    }
    
    public int getArchivosCreados() {
        return archivosCreados;
    }
    
    public int getOperacionesRealizadas() {
        return operacionesRealizadas;
    }
    
    // ===== SETTER PARA PANEL CONSOLA =====
    
    public void setPanelConsola(PanelConsola panelConsola) {
        this.panelConsola = panelConsola;
    }
    
    // ===== INFORMACIÓN DEL SISTEMA =====
    
    public String getEstadoSistema() {
        return String.format("=== ESTADO DEL SISTEMA ===\n" +
            "Usuario: %s (%s)\n" +
            "Archivos creados: %d\n" +
            "Archivos eliminados: %d\n" +
            "Operaciones realizadas: %d\n" +
            "Bloques ocupados: %d/%d\n" +
            "Planificador: %s\n" +
            "Procesos activos: %d\n" +
            "Procesos en cola: %d",
            usuarioActual, esModoAdministrador ? "Admin" : "Usuario",
            archivosCreados, getArchivosEliminados(), operacionesRealizadas,
            getBloquesOcupados(), totalBloques,
            planificadorActual.getNombrePolitica(),
            procesosActivos.getSize(), colaProcesos.getSize()
        );
    }
    
    /**
     * Método para mostrar detalles en el panel output
     */
    private void mostrarDetallesArchivo(Archivo archivo) {
        if (panelOutput != null) {
            panelOutput.mostrarDetallesArchivo(
                archivo.getNombre(),
                archivo.getTamañoBytes(),
                archivo.getTamañoReservadoBytes(),
                archivo.getTamañoBloques(),
                archivo.getBloquesReservados(),
                archivo.getInfoBloques()
            );
        }
    }

    /**
     * @return the archivosEliminados
     */
    public int getArchivosEliminados() {
        return archivosEliminados;
    }
}