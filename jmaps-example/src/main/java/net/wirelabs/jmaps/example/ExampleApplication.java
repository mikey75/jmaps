package net.wirelabs.jmaps.example;

import net.wirelabs.jmaps.example.components.ConfigPanel;
import net.wirelabs.jmaps.example.components.MainWindow;
import net.wirelabs.jmaps.example.components.MapPanel;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.EventQueue;
import java.io.File;


public class ExampleApplication  {

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        Coordinate lublinPL = new Coordinate(22.49004,51.23264);
        File mapfile = new File("jmaps-example/src/main/resources/map.xml");

        System.setProperty("sun.java2d.opengl","true");

        EventQueue.invokeLater(() -> {
            try {

                MapPanel map = new MapPanel();
                map.setHome(lublinPL);
                map.setMap(mapfile);

                JPanel config = new ConfigPanel(map);
                JFrame mainWindow = new MainWindow(map, config);
                mainWindow.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


}
