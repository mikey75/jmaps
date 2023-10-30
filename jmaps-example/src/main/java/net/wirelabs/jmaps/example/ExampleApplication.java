package net.wirelabs.jmaps.example;

import net.wirelabs.jmaps.example.components.ConfigPanel;
import net.wirelabs.jmaps.example.components.MainWindow;
import net.wirelabs.jmaps.example.components.MapPanel;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.JFrame;
import java.awt.EventQueue;
import java.io.File;


public class ExampleApplication {

    private static final Coordinate lublinPL = new Coordinate(22.49004,51.23264);
    private static final File mapfile = new File("jmaps-example/src/main/resources/map.xml");

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        System.setProperty("sun.java2d.opengl", "true");

        EventQueue.invokeLater(() -> {
            try {

                MapPanel mapPanel = new MapPanel();
                ConfigPanel configPanel = new ConfigPanel(mapPanel);
                JFrame mainWindow = new MainWindow(mapPanel, configPanel);
                mainWindow.setVisible(true);

                mapPanel.setHome(lublinPL);
                mapPanel.setMap(mapfile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


}
