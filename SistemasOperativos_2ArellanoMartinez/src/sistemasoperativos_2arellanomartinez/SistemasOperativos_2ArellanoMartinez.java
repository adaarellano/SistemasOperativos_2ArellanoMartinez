/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
/*
 * SistemasOperativos_2ArellanoMartinez.java
 */
package sistemasoperativos_2arellanomartinez; 

// IMPORTANTE: Importamos tu ventana principal
import MainGUI.Interfaz;
import javax.swing.SwingUtilities;

public class SistemasOperativos_2ArellanoMartinez {

    public static void main(String[] args) {
        System.out.println("Iniciando Simulador de Sistema Operativo...");

        // Iniciar la Interfaz en el hilo seguro de Swing
        SwingUtilities.invokeLater(() -> {
            // Creamos la ventana y la hacemos visible
            Interfaz ventana = new Interfaz();
            ventana.setVisible(true);
        });
    }
}