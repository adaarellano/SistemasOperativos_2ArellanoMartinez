/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import Models.*;
import Planificador.*;
import edd.ListaSimple;
import MainGUI.*;
import java.awt.Color;
import javax.swing.SwingUtilities;

public class ManejadorArchivo {
    // === ALMACENAMIENTO ===
    private Bloque[] bloquesDisco;
    private int totalBloques = 100;
    private Directorio raiz;
    
    // === SISTEMA ===
    private boolean esModoAdministrador = false;
    private Usuario usuarioActual;
    private ListaSimple usuarios; 
    private volatile boolean sistemaActivo = true; 
    private volatile boolean discoPausado = false; // MODO BATCH
    
    // === COMPONENTES ===
    private ListaSimple colaProcesos;    
    private ListaSimple procesosActivos; 
    private PlanificadorDisco planificadorActual;
    private int cabezalActual = 0; 
    private BufferManager bufferManager;
    
    // === ESTADÍSTICAS ===
    private int archivosCreados = 0;
    private int archivosEliminados = 0;
    private int operacionesRealizadas = 0;
    
    // === GUI ===
    private PanelConsola panelConsola;
    private PanelArchivos panelArchivos; 
    private PanelDisco panelDisco; 
    private PanelTablaAsignacion panelTablaAsignacion; 
    private PanelDetalles panelDetalles; 
    private PanelEstadisticas panelEstadisticas;

    // Hilos
    private Thread hiloDisco;
    private Thread hiloPlanificador;

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
        logConsola("=== SISTEMA INICIALIZADO ===");
    }
    
    private void crearUsuariosPorDefecto() {
        usuarios.insertFinal(new Usuario("admin", "admin"));
        usuarios.insertFinal(new Usuario("usuario1", "usuario"));
    }

    // =========================================================
    //              MÉTODOS CRUD (PÚBLICOS)
    // =========================================================
    // Estos métodos ahora deciden si EJECUTAR YA o ENCOLAR
    
    public boolean crearArchivo(String ruta, int tamañoBloques, String usuario) {
        if (!verificarPermisosEscritura(usuario)) return error("Sin permisos");
        
        if (discoPausado) {
            // MODO PROCESO: Solo encolamos la intención
            encolarSolicitudPendiente("CREATE", ruta, null, tamañoBloques, usuario);
            return true;
        }
        // MODO NORMAL: Ejecutamos lógica inmediata
        return ejecutarLogicaCrearArchivo(ruta, tamañoBloques, usuario);
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
        if (!verificarPermisosEscritura(usuario)) return false;
        
        if (discoPausado) {
            // Determinamos si es DIR o FILE para saber qué tipo poner, 
            // aunque ELIMINAR genérico funcionaría si la lógica lo maneja.
            // Por simplicidad, usaremos DELETE para ambos y la lógica resolverá.
            encolarSolicitudPendiente("DELETE", ruta, null, 0, usuario);
            return true;
        }
        return ejecutarLogicaEliminar(ruta, usuario);
    }
    
    public String leerArchivo(String ruta, String usuario) {
        // La lectura siempre intenta ser inmediata para devolver datos,
        // pero si hay cola, se simula el retraso.
        if (!verificarPermisosLectura(usuario)) return null;
        
        Archivo archivo = buscarArchivo(ruta);
        if (archivo == null) return null;
        
        // Buffer check
        Bloque primer = archivo.getPrimerBloque();
        if (primer != null && bufferManager != null && bufferManager.leerBloque(primer.getIdBloque()) != null) {
            logConsola("✅ [Buffer] CACHE HIT!");
            return archivo.leerContenido();
        }
        
        // Si no está en buffer, creamos solicitud de lectura
        crearSolicitudDisco("READ", (primer!=null?primer.getIdBloque():0), usuario);
        return archivo.leerContenido();
    }

    // =========================================================
    //              LÓGICA INTERNA DE EJECUCIÓN
    // =========================================================
    // Estos métodos hacen el trabajo real sobre el árbol y el disco
    
    private boolean ejecutarLogicaCrearArchivo(String ruta, int tamaño, String usuario) {
        if (!hayEspacioSuficiente(tamaño)) return error("Disco lleno");
        
        // Buscar padre
        String nombre = getNombreDesdeRuta(ruta);
        String rutaPadre = getPadreDesdeRuta(ruta);
        Directorio destino = (rutaPadre.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(rutaPadre);
        
        if (destino == null) return error("Directorio destino no existe");
        
        Archivo nuevo = new Archivo(nombre, tamaño, usuario, ruta);
        if (!asignarBloquesArchivo(nuevo, tamaño)) return error("Fallo asignación bloques");
        
        // En modo normal, se confirma de una. En batch, se confirmará al procesar.
        nuevo.setConfirmadoEnDisco(true); 
        
        if (destino.agregarArchivo(nuevo)) {
            archivosCreados++;
            logConsola("✅ ARCHIVO CREADO: " + nombre);
            // Solicitud VISUAL (Ya la lógica se hizo)
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
                // Los directorios no ocupan bloques, no mandamos solicitud de disco visual
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
        
        // Intentar borrar archivo
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
        
        // Intentar borrar directorio
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
    //              GESTOR DE COLAS Y DISCO
    // =========================================================
    
    // Método especial para encolar SIN ejecutar lógica (Modo Batch)
    private void encolarSolicitudPendiente(String tipo, String ruta, String datos, int tam, String usu) {
        SolicitudDisco sol = new SolicitudDisco(tipo, ruta, datos, tam, usu);
        planificadorActual.agregarSolicitud(sol);
        int pendientes = planificadorActual.getSolicitudesPendientes().getSize();
        logConsola("⏸️ PENDIENTE (" + tipo + "): " + ruta + ". Total en cola: " + pendientes);
    }
    
    // Método normal para efectos visuales (Modo Normal)
    private void crearSolicitudDisco(String tipo, int bloque, String usuario) {
        Proceso p = new Proceso(tipo, "sys", usuario);
        SolicitudDisco s = new SolicitudDisco(p, tipo, bloque);
        // En modo normal, la lógica ya se hizo
        s.setLogicaEjecutada(true); 
        planificadorActual.agregarSolicitud(s);
    }

    private void iniciarHiloDisco() {
        hiloDisco = new Thread(() -> {
            while (sistemaActivo) {
                try {
                    // SI ESTÁ PAUSADO, DUERME
                    if (discoPausado) { Thread.sleep(200); continue; }
                    
                    SolicitudDisco solicitud = planificadorActual.obtenerSiguiente();
                    
                    if (solicitud != null) {
                        procesarSolicitudDisco(solicitud);
                    } else {
                        Thread.sleep(200);
                    }
                } catch (Exception e) { }
            }
        });
        hiloDisco.start();
    }
    
    private void procesarSolicitudDisco(SolicitudDisco solicitud) {
        logConsola("⚙️ PROCESANDO: " + solicitud.getTipoOperacion() + " -> " + 
                   (solicitud.getRutaObjetivo() != null ? solicitud.getRutaObjetivo() : "Bloque " + solicitud.getBloqueSolicitado()));
        
        try {
            Thread.sleep(2000); // Simular tiempo
            
            // --- EJECUCIÓN DIFERIDA (MODO BATCH) ---
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
                // Marcar como hecha para no repetir
                solicitud.setLogicaEjecutada(true);
                
                // Forzar actualización visual final
                actualizarGUICompleta();
            }
            // ---------------------------------------
            
            logConsola("✅ COMPLETADO.");
            
        } catch (InterruptedException e) { }
    }

    // =========================================================
    //              UTILIDADES Y HELPERS
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
    
    private boolean verificarPermisosEscritura(String usuario) {
        return esModoAdministrador || "admin".equals(usuario);
    }
    
    private boolean verificarPermisosLectura(String usuario) { return true; }
    
    private boolean hayEspacioSuficiente(int n) { return (totalBloques - getBloquesOcupados()) >= n; }
    
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
    
    private boolean asignacionSimple(Archivo archivo, int cantidad) { /* Usar asignarBloquesArchivo */ return false; }
    
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
    
    private Archivo buscarArchivo(String ruta) {
        String nombre = getNombreDesdeRuta(ruta);
        String padre = getPadreDesdeRuta(ruta);
        Directorio d = (padre.equals("/")) ? raiz : raiz.buscarDirectorioRecursivo(padre);
        return (d != null) ? d.buscarArchivo(nombre) : null;
    }

    // --- PROCESOS CPU ---
    public void solicitarOperacion(String tipo, String ruta, String usuario, int tam) {
        // Este método es el punto de entrada de la GUI
        // Dependiendo de la operación, llamamos a los métodos específicos
        switch(tipo) {
            case "CREAR": crearArchivo(ruta, tam, usuario); break;
            case "CREAR_DIR": crearDirectorio(ruta, usuario); break;
            case "ELIMINAR": eliminarArchivo(ruta, usuario); break;
            // LEER y ACTUALIZAR se llaman directo desde PanelControl, pero si quisieras unificarlos:
            case "LEER": leerArchivo(ruta, usuario); break;
        }
        // Crear proceso visual para la tabla
        Proceso p = new Proceso(tipo, ruta, usuario, tam);
        p.setManejadorArchivo(this);
        colaProcesos.insertFinal(p);
        p.iniciar();
    }
    
    private void iniciarHiloPlanificador() {
        hiloPlanificador = new Thread(() -> {
            while(sistemaActivo) {
                try {
                    planificarProcesos();
                    Thread.sleep(250);
                } catch(Exception e) {}
            }
        });
        hiloPlanificador.start();
    }
    
    private void planificarProcesos() {
        for(int i=0; i<procesosActivos.getSize(); i++) {
            Proceso p = (Proceso)procesosActivos.get(i);
            if(p.estaTerminado()) {
                procesosActivos.remove(p);
                i--;
                if(!discoPausado) actualizarGUICompleta();
            }
        }
        if(!colaProcesos.isEmpty()) {
            Proceso p = (Proceso)colaProcesos.get(0);
            if(p.estaListo()) {
                p.setEstado(Proceso.Estado.EJECUTANDO);
                colaProcesos.remove(p);
                procesosActivos.insertFinal(p);
            }
        }
    }
    
    // =========================================================
    //              MÉTODOS DE AYUDA Y LOGGING
    // =========================================================

    /**
     * Envía un mensaje a la consola de la interfaz (o System.out si no hay GUI).
     */
    public void logConsola(String msg) { // <--- Cámbialo a PUBLIC por si acaso
        if (panelConsola != null) {
            panelConsola.agregarLinea(msg);
        } else {
            System.out.println(msg);
        }
    }

    // --- SETTERS Y GETTERS ---
    private void actualizarGUICompleta() {
        SwingUtilities.invokeLater(() -> {
            if (panelArchivos != null) panelArchivos.actualizarArbol();
            if (panelDisco != null) panelDisco.actualizarDisco();
            if (panelTablaAsignacion != null) panelTablaAsignacion.actualizarTabla();
            if (panelDetalles != null) panelDetalles.actualizarDetalles();
            if (panelEstadisticas != null) panelEstadisticas.actualizarGrafica();
        });
    }
    
    public void setPanelConsola(PanelConsola p) { this.panelConsola = p; if(bufferManager==null) bufferManager=new BufferManager(p); }
    public void setPanelArchivos(PanelArchivos p) { this.panelArchivos = p; }
    public void setPanelDisco(PanelDisco p) { this.panelDisco = p; }
    public void setPanelTablaAsignacion(PanelTablaAsignacion p) { this.panelTablaAsignacion = p; }
    public void setPanelDetalles(PanelDetalles p) { this.panelDetalles = p; }
    public void setPanelEstadisticas(PanelEstadisticas p) { this.panelEstadisticas = p; }
    public void setPanelOutput(PanelOutput p) {}
    
    public void setModoAdministrador(boolean admin) { 
        this.esModoAdministrador = admin; 
        this.usuarioActual = (Usuario) usuarios.get(admin?0:1);
    }
    public void setDiscoPausado(boolean pausado) { 
        this.discoPausado = pausado; 
        if(pausado) logConsola("⏸️ MODO PROCESO: Cola pausada.");
        else logConsola("▶️ MODO PROCESO: Procesando...");
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