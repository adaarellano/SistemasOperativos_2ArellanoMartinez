/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MainGUI;

import Managers.ManejadorArchivo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class PanelEstadisticas extends JPanel {
    private ManejadorArchivo manejador;
    private DefaultPieDataset dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;

    public PanelEstadisticas(ManejadorArchivo manejador) {
        this.manejador = manejador;
        inicializarPanel();
    }

    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Métricas del Disco"));
        
        // 1. Crear el conjunto de datos inicial
        dataset = new DefaultPieDataset();
        dataset.setValue("Espacio Libre", 100);
        dataset.setValue("Espacio Ocupado", 0);

        // 2. Crear la gráfica de Pastel (Pie Chart)
        chart = ChartFactory.createPieChart(
                "Uso del Disco", // Título del gráfico
                dataset,         // Datos
                true,            // Leyenda
                true,            // Tooltips
                false            // URLs
        );

        // 3. Estilizar la gráfica para que se vea profesional
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Espacio Libre", new Color(46, 204, 113)); // Verde Esmeralda
        plot.setSectionPaint("Espacio Ocupado", new Color(231, 76, 60)); // Rojo Alizarin
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        // 4. Meter la gráfica en un Panel de Swing especial
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        
        // Cargar datos iniciales
        actualizarGrafica();
    }

    /**
     * Recalcula los porcentajes y actualiza el gráfico visualmente.
     */
    public void actualizarGrafica() {
        if (manejador == null) return;

        int total = manejador.getTotalBloques();
        int ocupados = manejador.getBloquesOcupados();
        int libres = total - ocupados;

        // Actualizar los valores del dataset (JFreeChart repinta solo)
        dataset.setValue("Espacio Libre", libres);
        dataset.setValue("Espacio Ocupado", ocupados);
        
        // Actualizar el título con el porcentaje exacto
        double porcentaje = (ocupados * 100.0) / total;
        chart.setTitle("Uso del Disco (" + String.format("%.1f", porcentaje) + "%)");
    }
}