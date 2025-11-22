/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PanelUsuario extends JPanel {
    private ManejadorArchivo manejador;
    private PanelConsola panelConsola;
    private Random random;
    
    public PanelUsuario(ManejadorArchivo manejador, PanelConsola panelConsola) {
        this.manejador = manejador;
        this.panelConsola = panelConsola;
        this.random = new Random();
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        // Aumentamos las filas para que quepan los nuevos botones
        setLayout(new GridLayout(6, 1, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Aplicaciones y Pruebas (Modo Usuario)"));
        
        // --- APPS NORMALES ---
        JButton btnWord = crearBotonApp("📝 Microsoft Word", "Guardar documento", new Color(230, 240, 255));
        JButton btnMusic = crearBotonApp("🎵 Spotify", "Guardar música", new Color(230, 255, 230));
        
        // --- BOTONES DE EVALUACIÓN DE POLÍTICAS ---
        // Estos son los importantes para tu defensa
        JButton btnCrearMasivo = crearBotonApp("🔥 Descarga Masiva (Crear 10)", 
                "Genera 10 archivos seguidos para llenar la cola", new Color(255, 230, 230));
        
        JButton btnLeerCaos = crearBotonApp("⚡ Lectura Caótica (Evaluar Políticas)", 
                "Pide leer archivos al inicio, final y medio del disco para probar SSTF/SCAN", new Color(255, 255, 200));
        
        add(btnWord);
        add(btnMusic);
        add(new JSeparator()); // Separador visual
        add(new JLabel("--- ZONA DE EVALUACIÓN DE POLÍTICAS ---", SwingConstants.CENTER));
        add(btnCrearMasivo);
        add(btnLeerCaos);
        
        // === EVENTOS ===
        
        btnWord.addActionListener(e -> {
            String nombre = "doc_" + System.currentTimeMillis() % 1000 + ".docx";
            // Usamos "admin" para evitar bloqueo de permisos en la demo
            manejador.solicitarOperacion("CREAR", "/" + nombre, "admin", 2);
        });
        
        btnMusic.addActionListener(e -> {
            String nombre = "song_" + System.currentTimeMillis() % 1000 + ".mp3";
            manejador.solicitarOperacion("CREAR", "/" + nombre, "admin", 4);
        });
        
        // --- 1. PRUEBA DE CARGA (CREACIÓN) ---
        btnCrearMasivo.addActionListener(e -> {
            panelConsola.agregarLinea("=== 🚀 INICIANDO DESCARGA MASIVA (10 Archivos) ===");
            for (int i = 1; i <= 10; i++) {
                String nombre = "archivo_auto_" + i + ".dat";
                // Solicitamos crear 10 archivos de 3 bloques cada uno
                // Usamos "admin" para asegurar que se creen sin errores de permiso
                manejador.solicitarOperacion("CREAR", "/" + nombre, "admin", 3);
            }
        });
        
        // --- 2. PRUEBA DE POLÍTICAS (LECTURA CAÓTICA) ---
        // Esta es la mejor prueba para ver la diferencia entre FIFO y SSTF
        btnLeerCaos.addActionListener(e -> {
            panelConsola.agregarLinea("=== ⚡ INICIANDO LECTURA CAÓTICA ===");
            panelConsola.agregarLinea("Objetivo: Ver cómo el planificador reordena estas solicitudes.");
            
            // Vamos a pedir leer archivos que sabemos que existen (o intentarlo)
            // en un orden desordenado para obligar al disco a saltar.
            
            // 1. Pedir algo que probablemente esté al FINAL (si creaste muchos archivos)
            manejador.solicitarOperacion("LEER", "/archivo_auto_10.dat", "admin", 0);
            
            // 2. Pedir algo que está al PRINCIPIO
            manejador.solicitarOperacion("LEER", "/ada", "admin", 0); // Asumiendo que 'ada' existe
            
            // 3. Pedir algo del MEDIO
            manejador.solicitarOperacion("LEER", "/archivo_auto_5.dat", "admin", 0);
            
            // 4. Pedir otro del PRINCIPIO
            manejador.solicitarOperacion("LEER", "/tesis", "admin", 0);
            
            panelConsola.agregarLinea(">> 4 Solicitudes enviadas: Final -> Inicio -> Medio -> Inicio");
        });
    }
    
    private JButton crearBotonApp(String texto, String tooltip, Color color) {
        JButton btn = new JButton(texto);
        btn.setToolTipText(tooltip);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        return btn;
    }
}