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
import java.awt.Color;
import javax.swing.SwingUtilities;

public class ManejadorArchivo {
    // === ALMACENAMIENTO FÍSICO ===
    private Bloque[] bloquesDisco;
    private int totalBloques = 100;
    
    // === ESTRUCTURA LÓGICA ===
    private Directorio raiz;
    
    // === USUARIOS Y PERMISOS ===
    private boolean esModoAdministrador = false;
    private Usuario usuarioActual;
    private ListaSimple usuarios; 
    
    // === PROCESOS (CPU) ===
    private ListaSimple colaProcesos;    
    private ListaSimple procesosActivos; 
    private Thread hiloPlanificador;     
    
    // === PLANIFICACIÓN DE DISCO (I/O) ===
    private PlanificadorDisco planificadorActual;
    private int cabezalActual = 0; 
    private Thread hiloDisco;            
    private volatile boolean sistemaActivo = true; 
    private volatile boolean discoPausado = false; // TRUE = Modo Proceso, FALSE = Modo Admin Normal
    
    // === MEMORIA CACHÉ (BUFFER) ===
    private BufferManager bufferManager;
    
    // === ESTADÍSTICAS ===
    private int archivosCreados = 0;
    private int archivosEliminados = 0;
    private int operacionesRealizadas = 0;
    
    // === REFERENCIAS A LA GUI ===
    private PanelConsola panelConsola;
    private PanelArchivos panelArchivos; 
    private PanelDisco panelDisco; 
    private PanelTablaAsignacion panelTablaAsignacion; 
    private PanelDetalles panelDetalles; 
    private PanelEstadisticas panelEstadisticas;

    public ManejadorArchivo() {
        inicializarSistema();
    }
    
    private void inicializarSistema() {
        bloquesDisco = new Bloque[totalBloques];
        for (int i = 0; i < totalBloques; i++) {
            bloquesDisco[i] = new Bloque(i);
        }
        
        raiz = new Directorio("/");
        
        colaProcesos = new ListaSimple();
        procesosActivos = new ListaSimple();
        usuarios = new ListaSimple();
        
        crearUsuariosPorDefecto();
        this.usuarioActual = (Usuario) usuarios.get(1);
        
        planificadorActual = new FIFO(); 
        
        iniciarHiloDisco(); 
        iniciarHiloPlanificador();
        
        logConsola("=== SISTEMA DE ARCHIVOS INICIALIZADO ===");
        logConsola("Total bloques: " + totalBloques);
    }
    
    private void crearUsuariosPorDefecto() {
        usuarios.insertFinal(new Usuario("admin", "admin"));
        usuarios.insertFinal(new Usuario("usuario1", "usuario"));
        usuarios.insertFinal(new Usuario("usuario2", "usuario"));
    }
    
    // =========================================================
    //              OPERACIONES DE DIRECTORIOS
    // =========================================================
    
    public boolean crearDirectorio(String ruta, String usuario) {
        logConsola("=== SOLICITANDO CREACIÓN DE DIRECTORIO ===");
        
        if (!verificarPermisosEscritura(usuario)) {
            logConsola("❌ ERROR: Sin permisos.");
            return false;
        }
        
        if (discoPausado) {
            encolarSolicitudPendiente("CREATE_DIR", ruta, null, 0, usuario);
            return true;
        }
        
        return ejecutarLogicaCrearDirectorio(ruta);
    }
    
    public boolean renombrarDirectorio(String rutaCompleta, String nuevoNombre, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return false;
        
        if (discoPausado) {
            encolarSolicitudPendiente("RENAME", rutaCompleta, nuevoNombre, 0, usuario);
            return true;
        }
        
        return ejecutarLogicaRenombrar(rutaCompleta, nuevoNombre);
    }

    // =========================================================
    //              OPERACIONES DE ARCHIVOS (CRUD)
    // =========================================================

    public boolean crearArchivo(String ruta, int tamañoBloques, String usuario) {
        logConsola("=== SOLICITANDO CREACIÓN DE ARCHIVO ===");
        
        if (!verificarPermisosEscritura(usuario)) {
            logConsola("❌ ERROR: Sin permisos.");
            return false;
        }
        
        if (discoPausado) {
            encolarSolicitudPendiente("CREATE", ruta, null, tamañoBloques, usuario);
            return true;
        }
        
        return ejecutarLogicaCrearArchivo(ruta, tamañoBloques, usuario);
    }
    
    public String leerArchivo(String ruta, String usuario) {
        if (!verificarPermisosLectura(usuario)) return null;

        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) {
            logConsola("❌ ERROR: Archivo no encontrado");
            return null;
        }

        Bloque primerBloque = archivo.getPrimerBloque();
        if (primerBloque != null && bufferManager != null) {
            Bloque enCache = bufferManager.leerBloque(primerBloque.getIdBloque());
            if (enCache != null) {
                logConsola("✅ [Buffer] CACHE HIT!");
                operacionesRealizadas++;
                return archivo.leerContenido();
            }
        }
        
        logConsola("❌ [Buffer] CACHE MISS! Solicitando al disco...");
        if (archivo.getPrimerBloque() != null) {
            crearSolicitudDisco("READ", archivo.getPrimerBloque().getIdBloque(), usuario);
        }

        operacionesRealizadas++;
        return archivo.leerContenido();
    }
    
    public boolean actualizarArchivo(String ruta, String datos, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return false;
        
        if (discoPausado) {
            encolarSolicitudPendiente("UPDATE", ruta, datos, 0, usuario);
            return true;
        }
        
        return ejecutarLogicaActualizarArchivo(ruta, datos, usuario);
    }
    
    public boolean eliminarArchivo(String ruta, String usuario) {
        logConsola("=== SOLICITANDO ELIMINACIÓN: " + ruta + " ===");
        
        if (!verificarPermisosEscritura(usuario)) {
            logConsola("❌ ERROR: Sin permisos.");
            return false;
        }
        
        if (discoPausado) {
            encolarSolicitudPendiente("DELETE", ruta, null, 0, usuario);
            return true;
        }
        
        return ejecutarLogicaEliminar(ruta, usuario);
    }

    // =========================================================
    //              LÓGICA INTERNA DE EJECUCIÓN
    // =========================================================
    
    private boolean ejecutarLogicaCrearArchivo(String ruta, int tamaño, String usuario) {
        if (!hayEspacioSuficiente(tamaño)) return error("Disco lleno");
        
        String nombre = getNombreDesdeRuta(ruta);
        String rutaPadre = getPadreDesdeRuta(ruta);
        Directorio destino = (rutaPadre.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(rutaPadre);
        
        if (destino == null) return error("Directorio destino no existe");
        
        Archivo nuevo = new Archivo(nombre, tamaño, usuario, ruta);
        if (!asignarBloquesArchivo(nuevo, tamaño)) return error("Fallo asignación bloques");
        
        nuevo.setConfirmadoEnDisco(true); 
        
        if (destino.agregarArchivo(nuevo)) {
            archivosCreados++;
            logConsola("✅ ARCHIVO CREADO: " + nombre);
            crearSolicitudDisco("CREATE", nuevo.getPrimerBloque().getIdBloque(), usuario);
            if (!discoPausado) actualizarGUICompleta();
            return true;
        }
        return false;
    }
    
    private boolean ejecutarLogicaCrearDirectorio(String ruta) {
        String nombre = getNombreDesdeRuta(ruta);
        String rutaPadre = getPadreDesdeRuta(ruta);
        Directorio padre = (rutaPadre.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(rutaPadre);
        
        if (padre != null) {
            if (padre.crearSubdirectorio(nombre) != null) {
                logConsola("✅ DIRECTORIO CREADO: " + nombre);
                if (!discoPausado) actualizarGUICompleta();
                return true;
            }
        }
        return error("Error al crear directorio");
    }
    
    private boolean ejecutarLogicaActualizarArchivo(String ruta, String datos, String usuario) {
        Archivo archivo = buscarArchivo(ruta);
        if (archivo != null) {
            archivo.escribirContenido(datos);
            logConsola("✅ ARCHIVO ACTUALIZADO: " + archivo.getNombre());
            int bloque = (archivo.getPrimerBloque() != null) ? archivo.getPrimerBloque().getIdBloque() : 0;
            crearSolicitudDisco("UPDATE", bloque, usuario);
            if (!discoPausado) actualizarGUICompleta();
            return true;
        }
        return false;
    }
    
    private boolean ejecutarLogicaRenombrar(String ruta, String nuevoNombre) {
        Directorio dir = (ruta.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(ruta);
        if (dir != null && !dir.esRaiz()) {
            dir.setNombre(nuevoNombre);
            logConsola("✅ RENOMBRADO: " + nuevoNombre);
            if (!discoPausado) actualizarGUICompleta();
            return true;
        }
        return false;
    }
    
    private boolean ejecutarLogicaEliminar(String ruta, String usuario) {
        String nombre = getNombreDesdeRuta(ruta);
        String padrePath = getPadreDesdeRuta(ruta);
        Directorio padre = (padrePath.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(padrePath);
        
        if (padre == null) return false;
        
        Archivo arch = padre.buscarArchivo(nombre);
        if (arch != null) {
            liberarBloquesArchivo(arch);
            padre.eliminarArchivo(nombre);
            archivosEliminados++;
            crearSolicitudDisco("DELETE", 0, usuario);
            logConsola("✅ ELIMINADO: " + nombre);
            if (!discoPausado) actualizarGUICompleta();
            return true;
        }
        
        Directorio sub = padre.buscarSubdirectorio(nombre);
        if (sub != null) {
            liberarRecursosRecursivo(sub);
            padre.eliminarSubdirectorio(sub);
            logConsola("✅ DIRECTORIO ELIMINADO: " + nombre);
            if (!discoPausado) actualizarGUICompleta();
            return true;
        }
        return false;
    }

    // =========================================================
    //              GESTIÓN DE MEMORIA Y DISCO
    // =========================================================

    private boolean asignarBloquesArchivo(Archivo archivo, int cantidadBloques) {
        ListaSimple bloquesAsignados = new ListaSimple();
        
        for (int i = 0; i < totalBloques && bloquesAsignados.getSize() < cantidadBloques; i++) {
            if (bloquesDisco[i].estaLibre()) {
                bloquesAsignados.insertFinal(bloquesDisco[i]);
            }
        }
        
        if (bloquesAsignados.getSize() < cantidadBloques) return false;
        
        Bloque primerBloque = (Bloque) bloquesAsignados.get(0);
        archivo.setPrimerBloque(primerBloque);
        
        Bloque actual = primerBloque;
        for (int i = 1; i < bloquesAsignados.getSize(); i++) {
            Bloque siguiente = (Bloque) bloquesAsignados.get(i);
            actual.setSiguienteBloque(siguiente);
            actual = siguiente;
        }
        
        Color colorArchivo = archivo.getColor(); 
        for (int i = 0; i < bloquesAsignados.getSize(); i++) {
            Bloque bloque = (Bloque) bloquesAsignados.get(i);
            bloque.ocuparBloque(archivo.getNombre(), -1, colorArchivo);
        }
        
        return true;
    }
    
    private void liberarBloquesArchivo(Archivo archivo) {
        Bloque actual = archivo.getPrimerBloque();
        while (actual != null) {
            Bloque sig = actual.getSiguienteBloque();
            actual.liberarBloque(); 
            actual = sig;
        }
    }
    
    private void liberarRecursosRecursivo(Directorio dir) {
        ListaSimple archs = dir.getArchivos();
        for(int i=0; i<archs.getSize(); i++) liberarBloquesArchivo((Archivo)archs.get(i));
        ListaSimple subs = dir.getSubdirectorios();
        for(int i=0; i<subs.getSize(); i++) liberarRecursosRecursivo((Directorio)subs.get(i));
    }
    
    private boolean hayEspacioSuficiente(int necesarios) {
        int libres = totalBloques - getBloquesOcupados();
        return libres >= necesarios;
    }

    // =========================================================
    //              SISTEMA DE PROCESOS
    // =========================================================

    public void solicitarOperacion(String tipo, String ruta, String usuario, int tamaño) {
        // En modo normal, ejecutamos directo. En batch, se encola.
        switch(tipo) {
            case "CREAR": crearArchivo(ruta, tamaño, usuario); break;
            case "CREAR_DIR": crearDirectorio(ruta, usuario); break;
            case "ELIMINAR": eliminarArchivo(ruta, usuario); break;
            case "LEER": leerArchivo(ruta, usuario); break;
        }
    }
    
    private void encolarSolicitudPendiente(String tipo, String ruta, String datos, int tam, String usu) {
        // Crear Proceso visual para la tabla
        Proceso p = new Proceso(tipo, ruta, usu, tam);
        p.setManejadorArchivo(this);
        colaProcesos.insertFinal(p);
        p.iniciar(); // Entra como LISTO
        
        // Crear Solicitud física para el disco
        SolicitudDisco sol = new SolicitudDisco(tipo, ruta, datos, tam, usu);
        sol.setLogicaEjecutada(false); // IMPORTANTE: Pendiente de lógica
        planificadorActual.agregarSolicitud(sol);
        
        int pendientes = planificadorActual.getSolicitudesPendientes().getSize();
        logConsola("⏸️ PENDIENTE (" + tipo + "): " + ruta + ". Total en cola: " + pendientes);
        
        // IMPORTANTE: No llamamos a planificarProcesos() en modo pausa para que se queden en LISTO
    }
    
    private void iniciarHiloPlanificador() {
        hiloPlanificador = new Thread(() -> {
            while (sistemaActivo) {
                try {
                    planificarProcesos();
                    Thread.sleep(250); 
                } catch (InterruptedException e) { }
            }
        });
        hiloPlanificador.setDaemon(true);
        hiloPlanificador.start();
    }
    
    private synchronized void planificarProcesos() {
        // Limpieza de procesos terminados
        for (int i = 0; i < procesosActivos.getSize(); i++) {
            Proceso p = (Proceso) procesosActivos.get(i);
            if (p.estaTerminado()) {
                logConsola("♻️ Proceso finalizado: " + p.getNombre());
                procesosActivos.remove(p);
                i--;
                if(discoPausado) actualizarGUICompleta();
            }
        }
        
        // Si NO está pausado, movemos procesos
        if (!discoPausado && !colaProcesos.isEmpty()) {
            Proceso p = (Proceso) colaProcesos.get(0);
            if (p.estaListo()) {
                p.setEstado(Proceso.Estado.EJECUTANDO);
                p.permitirEjecucion(); 
                colaProcesos.remove(p);
                procesosActivos.insertFinal(p);
            }
        }
    }
    
    private void iniciarHiloDisco() {
        hiloDisco = new Thread(() -> {
            while (sistemaActivo) {
                try {
                    if (discoPausado) {
                        Thread.sleep(100);
                        continue;
                    }
                    
                    SolicitudDisco solicitud = planificadorActual.obtenerSiguiente();
                    
                    if (solicitud != null) {
                        procesarSolicitudDisco(solicitud);
                    } else {
                        Thread.sleep(200); 
                    }
                } catch (InterruptedException e) { }
            }
        });
        hiloDisco.setDaemon(true);
        hiloDisco.start();
    }
    
    private void crearSolicitudDisco(String tipo, int bloque, String usuario) {
        Proceso dummy = new Proceso(tipo, "sys", usuario); 
        SolicitudDisco solicitud = new SolicitudDisco(dummy, tipo, bloque);
        solicitud.setLogicaEjecutada(true); 
        planificadorActual.agregarSolicitud(solicitud);
    }
    
    private void procesarSolicitudDisco(SolicitudDisco solicitud) {
        if (solicitud == null) return;
        
        logConsola("⚙️ DISCO: Procesando " + solicitud.getTipoOperacion());
        this.cabezalActual = solicitud.getBloqueSolicitado();
        
        try {
            Thread.sleep(2000); 
            
            if (solicitud.getTipoOperacion().equals("READ") && bufferManager != null) {
                Bloque b = bloquesDisco[solicitud.getBloqueSolicitado()];
                bufferManager.agregarBloque(b);
            }
            
            // Ejecutar lógica diferida si es necesario
            if (!solicitud.isLogicaEjecutada()) {
                String tipo = solicitud.getTipoOperacion();
                String ruta = solicitud.getRutaObjetivo();
                String datos = solicitud.getDatosAdicionales();
                String usu = solicitud.getProcesoSolicitante().getUsuario();
                int tam = solicitud.getTamañoBloques();
                
                switch(tipo) {
                    case "CREATE": ejecutarLogicaCrearArchivo(ruta, tam, usu); break;
                    case "CREATE_DIR": ejecutarLogicaCrearDirectorio(ruta); break;
                    case "UPDATE": ejecutarLogicaActualizarArchivo(ruta, datos, usu); break;
                    case "RENAME": ejecutarLogicaRenombrar(ruta, datos); break;
                    case "DELETE": ejecutarLogicaEliminar(ruta, usu); break;
                }
                solicitud.setLogicaEjecutada(true);
                actualizarGUICompleta();
            }
            
            logConsola("✅ DISCO: Operación completada.");
            
        } catch (InterruptedException e) { }
    }

    // =========================================================
    //              UTILIDADES Y SETTERS
    // =========================================================

    private String getNombreDesdeRuta(String ruta) {
        int i = ruta.lastIndexOf('/');
        return (i == 0) ? ruta.substring(1) : ruta.substring(i + 1);
    }
    
    private String getPadreDesdeRuta(String ruta) {
        int i = ruta.lastIndexOf('/');
        return (i == 0) ? "/" : ruta.substring(0, i);
    }
    
    private boolean error(String msg) {
        logConsola("❌ " + msg);
        return false;
    }

    public void logConsola(String msg) {
        if (panelConsola != null) panelConsola.agregarLinea(msg);
        else System.out.println(msg);
    }

    private void actualizarGUICompleta() {
        SwingUtilities.invokeLater(() -> {
            if (panelArchivos != null) panelArchivos.actualizarArbol();
            if (panelDisco != null) panelDisco.actualizarDisco();
            if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
            if (panelDetalles != null) panelDetalles.actualizarDetalles();
            if (panelEstadisticas != null) panelEstadisticas.actualizarGrafica();
        });
    }
    
    // Setters y Getters
    public void setPanelConsola(PanelConsola p) { this.panelConsola = p; if(bufferManager==null) bufferManager=new BufferManager(p); }
    public void setPanelArchivos(PanelArchivos p) { this.panelArchivos = p; }
    public void setPanelDisco(PanelDisco p) { this.panelDisco = p; }
    public void setPanelTablaAsignacion(PanelTablaAsignacion p) { this.panelTablaAsignacion = p; }
    public void setPanelDetalles(PanelDetalles p) { this.panelDetalles = p; }
    public void setPanelEstadisticas(PanelEstadisticas p) { this.panelEstadisticas = p; }
    public void setPanelOutput(PanelOutput p) {}
    
    public void setModoAdministrador(boolean admin) { 
        this.esModoAdministrador = admin; 
        this.usuarioActual = (Usuario) usuarios.get(admin ? 0 : 1);
    }
    
    public void setDiscoPausado(boolean pausado) {
        this.discoPausado = pausado;
        if (pausado) logConsola("⏸️ MODO BATCH: Cola pausada.");
        else logConsola("▶️ MODO BATCH: Procesando...");
    }
    
    public void cambiarPlanificador(String tipo) {
        ListaSimple pend = planificadorActual.getSolicitudesPendientes();
        switch(tipo) {
            case "FIFO": planificadorActual = new FIFO(panelConsola); break;
            case "SSTF": planificadorActual = new SSTF(panelConsola); break;
            case "SCAN": planificadorActual = new SCAN(); break;
            case "C-SCAN": planificadorActual = new CSCAN(); break;
        }
        planificadorActual.setCabezalActual(cabezalActual);
        if(pend!=null) for(int i=0; i<pend.getSize(); i++) planificadorActual.agregarSolicitud((SolicitudDisco)pend.get(i));
        logConsola("Planificador: " + tipo);
    }
    
    // =========================================================
    //              UTILIDADES Y HELPERS (FALTANTES)
    // =========================================================

    /**
     * Busca un archivo en el sistema dada su ruta completa.
     * Retorna el objeto Archivo o null si no existe.
     */
    private Archivo buscarArchivo(String ruta) {
        // Validar ruta básica
        if (ruta == null || ruta.isEmpty()) return null;
        
        int lastSlash = ruta.lastIndexOf('/');
        if (lastSlash == -1) return null; // Ruta inválida
        
        String dirPath;
        String fileName;
        
        if (lastSlash == 0) {
            // Caso: /archivo.txt
            dirPath = "/";
            fileName = ruta.substring(1);
        } else {
            // Caso: /carpeta/archivo.txt
            dirPath = ruta.substring(0, lastSlash);
            fileName = ruta.substring(lastSlash + 1);
        }
        
        // Buscar directorio padre
        Directorio dir = (dirPath.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(dirPath);
        
        // Si el directorio existe, buscamos el archivo dentro
        if (dir != null) {
            return dir.buscarArchivo(fileName);
        }
        
        return null;
    }

    /**
     * Verifica si el usuario tiene permiso para escribir/modificar.
     */
    private boolean verificarPermisosEscritura(String usuario) {
        // El admin (o el sistema "admin") siempre tiene permiso.
        return esModoAdministrador || "admin".equals(usuario);
    }
    
    /**
     * Verifica permisos de lectura.
     */
    private boolean verificarPermisosLectura(String usuario) {
        // Por ahora todos leen todo en la simulación visual
        return true; 
    }

    // --- Ayudantes para desglosar rutas ---

    
    private boolean asignacionSimple(Archivo archivo, int cantidad) { 
        // Método legacy, redirigir a la implementación real
        return asignarBloquesArchivo(archivo, cantidad); 
    }

    public Bloque[] getBloquesDisco() { return bloquesDisco; }
    public Directorio getRaiz() { return raiz; }
    public ListaSimple getColaProcesos() { return colaProcesos; }
    public ListaSimple getProcesosActivos() { return procesosActivos; }
    public PlanificadorDisco getPlanificadorActual() { return planificadorActual; }
    public boolean esAdministrador() { return esModoAdministrador; }
    public Usuario getUsuarioActual() { return usuarioActual; }
    public int getTotalBloques() { return totalBloques; }
    public int getBloquesOcupados() { int c=0; for(Bloque b:bloquesDisco) if(b.estaOcupado()) c++; return c; }
    public int getArchivosCreados() { return archivosCreados; }
    public int getArchivosEliminados() { return archivosEliminados; }
    public int getOperacionesRealizadas() { return operacionesRealizadas; }
}