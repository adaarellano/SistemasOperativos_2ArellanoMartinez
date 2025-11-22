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
import MainGUI.*;
import MainGUI.PanelOutput;
import java.util.*;
import javax.swing.SwingUtilities;

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
    private int cabezalActual = 0; 
    private Thread hiloDisco; 
    private volatile boolean sistemaActivo = true; 
    
    // === ESTADÍSTICAS ===
    private int archivosCreados = 0;
    private int archivosEliminados = 0;
    private int operacionesRealizadas = 0;
    
    // === REFERENCIA A INTERFAZ ===
    private PanelConsola panelConsola;
    private PanelArchivos panelArchivos; 
    private PanelDisco panelDisco; 
    private PanelTablaAsignacion panelTablaAsignacion; 
    private PanelDetalles panelDetalles; 
    private BufferManager bufferManager;
    
    public ManejadorArchivo() {
        inicializarSistema();
    }
    
    public ManejadorArchivo(PanelConsola panelConsola, BufferManager bufferManager) {
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
        solicitudesDisco = new ListaSimple(); // 'solicitudesDisco' aquí es un 'ListaSimple'
        usuarios = new ListaSimple();
        
        // 4. Crear usuarios por defecto
        crearUsuariosPorDefecto();
        
        // 5. Inicializar planificador (FIFO por defecto)
        // El 'setPanelConsola' lo inicializará correctamente
        planificadorActual = new FIFO(); 
        
        // 6. Iniciar el hilo del disco
        iniciarHiloDisco(); 
        
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
            logConsola("❌ ERROR: Sin permisos para crear archivo");
            return false;
        }
        
        if (!hayEspacioSuficiente(tamañoBloques)) {
            logConsola("❌ ERROR: No hay espacio suficiente");
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
                logConsola("❌ ERROR: No se pudo encontrar/crear el directorio");
                return false;
            }
            
            logConsola("Creando archivo: " + nombreArchivo);
            // Crear archivo
            Archivo nuevoArchivo = new Archivo(nombreArchivo, tamañoBloques, usuario, ruta);
            
            logConsola("Asignando " + tamañoBloques + " bloques...");
            // Asignar bloques al archivo
            if (!asignarBloquesArchivo(nuevoArchivo, tamañoBloques)) {
                logConsola("❌ ERROR: No se pudieron asignar bloques");
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

        // ... (resto de verificaciones de permisos si las tienes) ...

        logConsola("Archivo encontrado: " + archivo.getNombre());
        logConsola("Tamaño: " + archivo.getTamañoBytes() + " bytes");

        // --- ¡LÓGICA DEL BUFFER! ---
        // Por simplicidad, solo comprobamos el primer bloque del archivo.
        
        Bloque primerBloque = archivo.getPrimerBloque();
        
        if (primerBloque != null) {
            // 1. Preguntar al buffer primero
            Bloque bloqueEnCache = bufferManager.leerBloque(primerBloque.getIdBloque());
            
            if (bloqueEnCache != null) {
                // ¡CACHE HIT! No necesitamos ir al disco.
                logConsola("✅ CACHE HIT: Datos leídos desde RAM (Buffer). Rápido.");
                operacionesRealizadas++;
                return archivo.leerContenido(); // Devolver contenido al instante
            }
        }
        
        // ¡CACHE MISS! El bloque no está en el buffer.
        // Debemos solicitarlo al disco (lento).
        logConsola("❌ CACHE MISS: Datos no encontrados en RAM. Solicitando E/S al disco...");
        
        // Crear solicitud de disco para lectura (Este es el flujo lento)
        if (archivo.getPrimerBloque() != null) {
            crearSolicitudDisco("READ", archivo.getPrimerBloque().getIdBloque(), usuario);
        }

        operacionesRealizadas++;
        return archivo.leerContenido();
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

    /**
     * Inicia el hilo del "Demonio de Disco".
     * Este hilo se ejecuta en segundo plano, revisa la cola de solicitudes
     * y le pide al planificador actual cuál procesar a continuación.
     */
    private void iniciarHiloDisco() {
        hiloDisco = new Thread(() -> {
            logConsola("💿 Hilo del disco iniciado. Esperando solicitudes...");
            
            while (sistemaActivo) {
                try {
                    // 1. Pedir la siguiente solicitud al planificador
                    SolicitudDisco solicitud = planificadorActual.obtenerSiguiente();
                    
                    if (solicitud != null) {
                        // 2. Si hay una, procesarla
                        procesarSolicitudDisco(solicitud); // Llama a la NUEVA versión
                    } else {
                        // 3. Si no hay, esperar un momento
                        Thread.sleep(500); // Espera si la cola está vacía
                    }
                    
                } catch (InterruptedException e) {
                    if (sistemaActivo) logConsola("Hilo del disco interrumpido.");
                    sistemaActivo = false;
                } catch (Exception e) {
                    logConsola("❌ Error en el hilo del disco: " + e.getMessage());
                }
            }
            logConsola("💿 Hilo del disco detenido.");
        });
        
        hiloDisco.setName("Disco-Scheduler-Thread");
        hiloDisco.setDaemon(true); 
        hiloDisco.start();
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
     * Crea una nueva solicitud de disco y la AÑADE A LA COLA del planificador.
     * Ya NO la procesa directamente.
     */
    private void crearSolicitudDisco(String tipoOperacion, int bloque, String usuario) {
        logConsola("📋 ENCOLANDO SOLICLITUD DE DISCO");
        logConsola("   Tipo: " + tipoOperacion);
        logConsola("   Bloque: " + bloque);
        logConsola("   Planificador: " + planificadorActual.getNombrePolitica());

        // El constructor de SolicitudDisco pide un Proceso.
        Proceso dummyProceso = new Proceso(tipoOperacion, "ruta_desconocida", usuario);
        SolicitudDisco nuevaSolicitud = new SolicitudDisco(dummyProceso, tipoOperacion, bloque);
        
        // Añadir la solicitud al planificador activo
        planificadorActual.agregarSolicitud(nuevaSolicitud);
        
        logConsola("   ✅ Solicitud ENCOLADA. (El planificador decidirá...)");
        logConsola("   Solicitudes pendientes: " + planificadorActual.getSolicitudesPendientes().getSize());
    }
    
    /**
     * Procesa UNA solicitud de disco específica. (NUEVA VERSIÓN)
     * Este método es llamado por el hiloDisco, NO directamente.
     */
    private void procesarSolicitudDisco(SolicitudDisco solicitud) {
        if (solicitud == null) return;
        
        logConsola("⚡ PROCESANDO SOLICITUD (Decidido por " + planificadorActual.getNombrePolitica() + ")");
        logConsola("   Operación: " + solicitud.getTipoOperacion());
        logConsola("   Bloque objetivo: " + solicitud.getBloqueSolicitado());
        
        // Calcular distancia y mover cabezal
        int distancia = Math.abs(solicitud.getBloqueSolicitado() - this.cabezalActual);
        this.cabezalActual = solicitud.getBloqueSolicitado();
        
        logConsola("   Cabezal se mueve a: " + this.cabezalActual + " (Distancia: " + distancia + ")");
        
        // Simular tiempo de E/S
        try {
            logConsola("   Simulando tiempo de E/S...");
            Thread.sleep(800); // 800ms de tiempo de acceso
            
            // --- ¡AQUÍ ESTÁ TU LÓGICA DE BUFFER! ---
            // Si fue una lectura (un CACHE MISS), ahora llenamos el caché.
            if (solicitud.getTipoOperacion().equals("READ")) {
                Bloque bloqueLeido = bloquesDisco[solicitud.getBloqueSolicitado()];
                if (bufferManager != null) {
                    bufferManager.agregarBloque(bloqueLeido);
                }
            }
            
            logConsola("   ✅ Operación completada en bloque " + this.cabezalActual);
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
        // --- NUEVO: Parte 1 - Limpiar procesos terminados ---
        // Recorremos la lista de activos para "limpiar" los que ya terminaron
        for (int i = 0; i < procesosActivos.getSize(); i++) {
            Proceso p = (Proceso) procesosActivos.get(i);
            if (p.estaTerminado()) {
                logConsola("♻️ Limpiando proceso terminado: " + p.getNombre());
                procesosActivos.remove(p);
                i--; // Ajustar el índice después de eliminar
                actualizarGUICompleta();
            }
        }

        // --- Parte 2 - Planificar nuevos procesos (Tu código original) ---
        // Lógica simple de planificación: tomar el primer proceso listo
        for (int i = 0; i < colaProcesos.getSize(); i++) {
            Proceso proceso = (Proceso) colaProcesos.get(i);
            
            // Con el Arreglo 1, esto ahora devolverá true
            if (proceso.estaListo()) { 
                proceso.setEstado(Proceso.Estado.EJECUTANDO);
                proceso.permitirEjecucion();
                
                // Mover a procesos activos
                procesosActivos.insertFinal(proceso);
                colaProcesos.remove(proceso);
                logConsola("📊 Proceso en ejecución: " + proceso.getNombre());
                break; // Solo planifica uno a la vez (simple)
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
        logConsola("Cabezal actual en: " + this.cabezalActual);

        // Guardar la cola de solicitudes pendientes
        ListaSimple pendientes = planificadorActual.getSolicitudesPendientes();
        
        switch(tipoPlanificador.toUpperCase()) {
            case "FIFO":
                planificadorActual = new FIFO(panelConsola);
                logConsola("Nuevo planificador: FIFO (First-In-First-Out)");
                break;
            case "SSTF":
                planificadorActual = new SSTF(panelConsola);
                logConsola("Nuevo planificador: SSTF (Shortest Seek Time First)");
                break;
            case "SCAN":
                planificadorActual = new SCAN();
                logConsola("Nuevo planificador: SCAN (Elevator Algorithm)");
                break;
            case "C-SCAN":
                planificadorActual = new CSCAN();
                logConsola("Nuevo planificador: C-SCAN (Circular SCAN)");
                break;
            default:
                logConsola("❌ ERROR: Planificador no válido: " + tipoPlanificador);
                return;
        }
        
        // Restaurar el estado al nuevo planificador
        planificadorActual.setCabezalActual(this.cabezalActual);
        
        // Volver a encolar las solicitudes pendientes
        if (pendientes != null && !pendientes.isEmpty()) {
            logConsola("...Moviendo " + pendientes.getSize() + " solicitudes pendientes al nuevo planificador...");
            for (int i = 0; i < pendientes.getSize(); i++) {
                planificadorActual.agregarSolicitud((SolicitudDisco) pendientes.get(i));
            }
        }
        
        logConsola("✅ Planificador cambiado exitosamente a " + planificadorActual.getNombrePolitica());
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
        // Ahora 'panelConsola' no es null, por lo que esto es seguro.
        if (this.bufferManager == null) {
            this.bufferManager = new BufferManager(this.panelConsola);
        }
    }
    
        /**
     * Fuerza una actualización de todos los paneles de la GUI.
     */
    private void actualizarGUICompleta() {
        if (panelArchivos != null) panelArchivos.actualizarArbol();
        if (panelDisco != null) panelDisco.actualizarDisco();
        if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
        if (panelDetalles != null) panelDetalles.actualizarDetalles();
    }
    
    public void setPanelArchivos(PanelArchivos panel) {
    this.panelArchivos = panel;
    }

    public void setPanelDisco(PanelDisco panel) {
        this.panelDisco = panel;
    }

    public void setPanelTablaAsignacion(PanelTablaAsignacion panel) {
        this.panelTablaAsignacion = panel;
    }

    public void setPanelDetalles(PanelDetalles panel) {
        this.panelDetalles = panel;
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
}    // ... otros métodos existentes
