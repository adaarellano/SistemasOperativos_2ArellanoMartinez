package MainGUI;
import Managers.ManejadorArchivo;
import Models.Bloque;
import javax.swing.*;
import java.awt.*;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Day
 */
/**
 * Panel que dibuja la Simulación de Disco (SD) visualmente.
 * Muestra una cuadrícula de bloques y su estado (libre/ocupado).
 */
public class PanelDisco extends JPanel {
    private ManejadorArchivo manejador;
    private final int TAMANO_BLOQUE_PX = 15; // Tamaño en píxeles de cada bloque
    private final int FILAS = 10;
    private final int COLUMNAS = 10;

    public PanelDisco(ManejadorArchivo manejador) {
        this.manejador = manejador;
        inicializarPanel();
    }
    
    private void inicializarPanel() {
        setBorder(BorderFactory.createTitledBorder("Simulación de Disco (SD)"));
        // Calculamos el tamaño preferido basado en los bloques
        int ancho = (COLUMNAS * TAMANO_BLOQUE_PX) + 40; // 40px para padding
        int alto = (FILAS * TAMANO_BLOQUE_PX) + 40;
        setPreferredSize(new Dimension(ancho, alto));
    }
    
    /**
     * Este método especial de Swing se llama automáticamente para dibujar.
     * Aquí es donde ocurre la magia visual.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia el panel
        
        Bloque[] bloques = manejador.getBloquesDisco();
        if (bloques == null) return;
        
        // Posición inicial para dibujar (con un pequeño margen)
        int x = 20;
        int y = 30;
        
        for (int i = 0; i < bloques.length; i++) {
            Bloque bloque = bloques[i];
            
            // Elegir color
            if (bloque.estaLibre()) {
                g.setColor(Color.GREEN.darker()); // Libre
            } else {
                g.setColor(Color.RED.darker()); // Ocupado
            }
            
            // Dibujar el bloque (un rectángulo relleno)
            g.fillRect(x, y, TAMANO_BLOQUE_PX, TAMANO_BLOQUE_PX);
            
            // Dibujar borde negro
            g.setColor(Color.BLACK);
            g.drawRect(x, y, TAMANO_BLOQUE_PX, TAMANO_BLOQUE_PX);
            
            // Mover a la siguiente posición
            x += TAMANO_BLOQUE_PX;
            
            // Si llegamos al final de una fila, saltar a la siguiente
            if ((i + 1) % COLUMNAS == 0) {
                x = 20;
                y += TAMANO_BLOQUE_PX;
            }
        }
    }
    
    /**
     * Llama a este método para forzar al panel a redibujarse
     * (por ejemplo, después de crear un archivo).
     */
    public void actualizarDisco() {
        // repaint() le dice a Swing: "necesito redibujarme"
        repaint();
    }
}
