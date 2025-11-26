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
    private Directorio raiz;
    
    // === SISTEMA ===
    private boolean esModoAdministrador = false;
    private Usuario usuarioActual;
    private ListaSimple usuarios; 
    private volatile boolean sistemaActivo = true; 
    private volatile boolean discoPausado = false; 
    private volatile boolean procesandoLote = false;
    
    // === COMPONENTES ===
    private ListaSimple colaProcesos;    
    private ListaSimple procesosActivos; 
    private PlanificadorDisco planificadorActual;
    private int cabezalActual = 0; 
    private Thread hiloDisco;
    private Thread hiloPlanificador;
    private BufferManager bufferManager;
    
    // === ESTADÍSTICAS Y GUI ===
    private int archivosCreados = 0;
    private int archivosEliminados = 0;
    private int operacionesRealizadas = 0;
    
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
        for (int i = 0; i < totalBloques; i++) bloquesDisco[i] = new Bloque(i);
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
    }
    
    private void crearUsuariosPorDefecto() {
        usuarios.insertFinal(new Usuario("admin", "admin"));
        usuarios.insertFinal(new Usuario("usuario1", "usuario"));
        usuarios.insertFinal(new Usuario("usuario2", "usuario"));
    }

    // =========================================================
    //              PUNTO DE ENTRADA UNIVERSAL (SOLICITUDES)
    // =========================================================
    
    /**
     * Método maestro para solicitar operaciones desde la GUI.
     * Soporta todos los tipos de CRUD y maneja la cola automáticamente.
     */
    public void solicitarOperacion(String tipo, String ruta, String usuario, int tam, String datosAdicionales) {
        switch(tipo) {
            case "CREAR": 
                crearArchivo(ruta, tam, usuario); 
                break;
            case "CREAR_DIR": 
                crearDirectorio(ruta, usuario); 
                break;
            case "ELIMINAR": 
                eliminarArchivo(ruta, usuario); 
                break;
            case "LEER": 
                leerArchivo(ruta, usuario); 
                break;
            case "UPDATE": 
                actualizarArchivo(ruta, datosAdicionales, usuario); 
                break;
            case "RENAME": 
                renombrarDirectorio(ruta, datosAdicionales, usuario); 
                break;
            default:
                logConsola("⚠️ Operación desconocida: " + tipo);
        }
    }
    
    // Sobrecarga para operaciones simples (sin datos adicionales)
    public void solicitarOperacion(String tipo, String ruta, String usuario, int tam) {
        solicitarOperacion(tipo, ruta, usuario, tam, null);
    }

    // =========================================================
    //              MÉTODOS CRUD PÚBLICOS
    // =========================================================
    
    public boolean crearArchivo(String ruta, int tamaño, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return error("Sin permisos");
        if (discoPausado) {
            encolarSolicitudPendiente("CREATE", ruta, null, tamaño, usuario);
            return true;
        }
        return ejecutarLogicaCrearArchivo(ruta, tamaño, usuario);
    }
    
    public boolean crearDirectorio(String ruta, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return error("Sin permisos");
        if (discoPausado) {
            encolarSolicitudPendiente("CREATE_DIR", ruta, null, 0, usuario);
            return true;
        }
        return ejecutarLogicaCrearDirectorio(ruta);
    }
    
    public boolean actualizarArchivo(String ruta, String datos, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return false;
        if (discoPausado) {
            encolarSolicitudPendiente("UPDATE", ruta, datos, 0, usuario);
            return true;
        }
        return ejecutarLogicaActualizarArchivo(ruta, datos, usuario);
    }
    
    public boolean renombrarDirectorio(String ruta, String nuevoNombre, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return false;
        if (discoPausado) {
            encolarSolicitudPendiente("RENAME", ruta, nuevoNombre, 0, usuario);
            return true;
        }
        return ejecutarLogicaRenombrar(ruta, nuevoNombre);
    }
    
    public boolean eliminarArchivo(String ruta, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return error("Sin permisos");
        if (discoPausado) {
            encolarSolicitudPendiente("DELETE", ruta, null, 0, usuario);
            return true;
        }
        return ejecutarLogicaEliminar(ruta, usuario);
    }
    
    public String leerArchivo(String ruta, String usuario) {
        if (!verificarPermisosLectura(usuario)) return null;
        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) {
            logConsola("❌ ERROR: Archivo no encontrado: " + ruta);
            return null;
        }
        
        // Lectura inmediata para obtener datos (simulación visual aparte)
        Bloque primer = archivo.getPrimerBloque();
        if (primer != null && bufferManager != null && bufferManager.leerBloque(primer.getIdBloque()) != null) {
            logConsola("✅ [Buffer] CACHE HIT!");
        } else {
            // Si no está en buffer, simulamos la petición de lectura
            crearSolicitudDisco("READ", (primer!=null?primer.getIdBloque():0), usuario);
        }
        operacionesRealizadas++;
        return archivo.leerContenido();
    }

    // =========================================================
    //              LÓGICA INTERNA (REAL)
    // =========================================================
    
    private boolean ejecutarLogicaCrearArchivo(String ruta, int tamaño, String usuario) {
        if (!hayEspacioSuficiente(tamaño)) return error("Disco lleno");
        String nombre = getNombreDesdeRuta(ruta);
        String rutaPadre = getPadreDesdeRuta(ruta);
        Directorio destino = (rutaPadre.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(rutaPadre);
        
        if (destino == null) return error("Directorio destino no existe: " + rutaPadre);
        
        Archivo nuevo = new Archivo(nombre, tamaño, usuario, ruta);
        if (!asignarBloquesArchivo(nuevo, tamaño)) return error("Fallo asignación bloques");
        
        // Si no estamos pausados, confirmar visualización inmediatamente
        if (!discoPausado) nuevo.setConfirmadoEnDisco(true);
        
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
            String viejo = dir.getNombre();
            dir.setNombre(nuevoNombre);
            logConsola("✅ RENOMBRADO: " + viejo + " -> " + nuevoNombre);
            if (!discoPausado) actualizarGUICompleta();
            return true;
        }
        return false;
    }
    
    private boolean ejecutarLogicaEliminar(String ruta, String usuario) {
        String nombre = getNombreDesdeRuta(ruta);
        String padrePath = getPadreDesdeRuta(ruta);
        Directorio padre = (padrePath.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(padrePath);
        
        if (padre == null) return error("No se encontró padre: " + padrePath);
        
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
        return error("No se encontró: " + nombre);
    }

    // =========================================================
    //              PROCESAMIENTO DE COLA (BATCH)
    // =========================================================

    private void encolarSolicitudPendiente(String tipo, String ruta, String datos, int tam, String usu) {
        // Crear proceso visual
        Proceso p = new Proceso(tipo, ruta, usu, tam);
        p.setManejadorArchivo(this);
        colaProcesos.insertFinal(p);
        p.iniciar();
        
        // Crear solicitud física con datos diferidos
        SolicitudDisco sol = new SolicitudDisco(tipo, ruta, datos, tam, usu);
        sol.setLogicaEjecutada(false);
        planificadorActual.agregarSolicitud(sol);
        
        int pendientes = planificadorActual.getSolicitudesPendientes().getSize();
        logConsola("⏸️ PENDIENTE (" + tipo + "): " + ruta + ". Total en cola: " + pendientes);
    }
    
    public void procesarColaPendiente() {
        if (discoPausado) {
            this.procesandoLote = true;
            logConsola("▶️ PROCESANDO LOTE DE SOLICITUDES...");
        }
    }
    
    private void procesarSolicitudDisco(SolicitudDisco solicitud) {
        if (solicitud == null) return;
        
        logConsola("⚙️ DISCO: Procesando " + solicitud.getTipoOperacion() + " -> " + solicitud.getBloqueSolicitado());
        this.cabezalActual = solicitud.getBloqueSolicitado();
        
        try {
            Thread.sleep(2000); 
            
            if (solicitud.getTipoOperacion().equals("READ") && bufferManager != null) {
                Bloque b = bloquesDisco[solicitud.getBloqueSolicitado()];
                bufferManager.agregarBloque(b);
            }
            
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
                
                // Confirmación visual retardada para creaciones
                if (tipo.equals("CREATE")) {
                     Archivo a = buscarArchivo(ruta);
                     if (a != null) a.setConfirmadoEnDisco(true);
                }
                
                solicitud.setLogicaEjecutada(true);
                actualizarGUICompleta();
            }
            
            logConsola("✅ COMPLETADO.");
            
        } catch (InterruptedException e) { }
    }

    // =========================================================
    //              HILOS
    // =========================================================

    private void iniciarHiloDisco() {
        hiloDisco = new Thread(() -> {
            while (sistemaActivo) {
                try {
                    boolean debeTrabajar = !discoPausado || procesandoLote;
                    if (!debeTrabajar) { Thread.sleep(200); continue; }
                    
                    if (discoPausado && procesandoLote && planificadorActual.getSolicitudesPendientes().isEmpty()) {
                        procesandoLote = false;
                        logConsola("⏸️ Lote terminado. Disco pausado.");
                        continue;
                    }
                    
                    SolicitudDisco solicitud = planificadorActual.obtenerSiguiente();
                    if (solicitud != null) procesarSolicitudDisco(solicitud);
                    else Thread.sleep(200);
                } catch (Exception e) { }
            }
        });
        hiloDisco.setDaemon(true);
        hiloDisco.start();
    }
    
    private void iniciarHiloPlanificador() {
        hiloPlanificador = new Thread(() -> {
            while (sistemaActivo) {
                try { planificarProcesos(); Thread.sleep(250); } catch (Exception e) {}
            }
        });
        hiloPlanificador.setDaemon(true);
        hiloPlanificador.start();
    }
    
    private synchronized void planificarProcesos() {
        for (int i = 0; i < procesosActivos.getSize(); i++) {
            Proceso p = (Proceso) procesosActivos.get(i);
            if (p.estaTerminado()) {
                logConsola("♻️ Proceso finalizado: " + p.getNombre());
                procesosActivos.remove(p);
                i--;
                if(!discoPausado) actualizarGUICompleta();
            }
        }
        if (!colaProcesos.isEmpty()) {
            Proceso p = (Proceso) colaProcesos.get(0);
            if (p.estaListo()) {
                p.setEstado(Proceso.Estado.EJECUTANDO);
                colaProcesos.remove(p);
                procesosActivos.insertFinal(p);
            }
        }
    }

    // =========================================================
    //              HELPERS
    // =========================================================
    
    private void crearSolicitudDisco(String tipo, int bloque, String usuario) {
        Proceso p = new Proceso(tipo, "sys", usuario);
        SolicitudDisco s = new SolicitudDisco(p, tipo, bloque);
        s.setLogicaEjecutada(true); 
        planificadorActual.agregarSolicitud(s);
    }
    
    private Archivo buscarArchivo(String ruta) {
        String nombre = getNombreDesdeRuta(ruta);
        String padre = getPadreDesdeRuta(ruta);
        Directorio d = (padre.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(padre);
        return (d != null) ? d.buscarArchivo(nombre) : null;
    }
    
    private String getNombreDesdeRuta(String ruta) {
        int i = ruta.lastIndexOf('/');
        return (i == 0) ? ruta.substring(1) : ruta.substring(i + 1);
    }
    
    private String getPadreDesdeRuta(String ruta) {
        int i = ruta.lastIndexOf('/');
        return (i == 0) ? "/" : ruta.substring(0, i);
    }
    
    private void liberarRecursosRecursivo(Directorio dir) {
        ListaSimple archs = dir.getArchivos();
        for(int i=0; i<archs.getSize(); i++) liberarBloquesArchivo((Archivo)archs.get(i));
        ListaSimple subs = dir.getSubdirectorios();
        for(int i=0; i<subs.getSize(); i++) liberarRecursosRecursivo((Directorio)subs.get(i));
    }
    
    private void liberarBloquesArchivo(Archivo archivo) {
        Bloque actual = archivo.getPrimerBloque();
        while (actual != null) {
            Bloque sig = actual.getSiguienteBloque();
            actual.liberarBloque();
            actual = sig;
        }
    }
    
    private boolean asignarBloquesArchivo(Archivo archivo, int cantidad) {
        ListaSimple asignados = new ListaSimple();
        for(int i=0; i<totalBloques && asignados.getSize()<cantidad; i++) {
            if(bloquesDisco[i].estaLibre()) asignados.insertFinal(bloquesDisco[i]);
        }
        if(asignados.getSize() < cantidad) return false;
        Bloque primero = (Bloque)asignados.get(0);
        archivo.setPrimerBloque(primero);
        Bloque actual = primero;
        for(int i=1; i<asignados.getSize(); i++) {
            Bloque sig = (Bloque)asignados.get(i);
            actual.setSiguienteBloque(sig);
            actual = sig;
        }
        Color c = archivo.getColor();
        for(int i=0; i<asignados.getSize(); i++) ((Bloque)asignados.get(i)).ocuparBloque(archivo.getNombre(), -1, c);
        return true;
    }

    private boolean error(String msg) { logConsola("❌ " + msg); return false; }
    public void logConsola(String msg) { if (panelConsola != null) panelConsola.agregarLinea(msg); else System.out.println(msg); }
    
    private void actualizarGUICompleta() {
        SwingUtilities.invokeLater(() -> {
            if (panelArchivos != null) panelArchivos.actualizarArbol();
            if (panelDisco != null) panelDisco.actualizarDisco();
            if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
            if (panelDetalles != null) panelDetalles.actualizarDetalles();
            if (panelEstadisticas != null) panelEstadisticas.actualizarGrafica();
        });
    }
    
    private boolean hayEspacioSuficiente(int n) { return (totalBloques - getBloquesOcupados()) >= n; }
    private boolean verificarPermisosEscritura(String u) { return esModoAdministrador || "admin".equals(u); }
    private boolean verificarPermisosLectura(String u) { return true; }

    // Setters
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
        this.procesandoLote = false;
        if (pausado) logConsola("⏸️ MODO PROCESO: Cola pausada.");
        else logConsola("▶️ MODO NORMAL: Disco en vivo.");
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

    // Getters
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