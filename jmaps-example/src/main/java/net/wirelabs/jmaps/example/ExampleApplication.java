package net.wirelabs.jmaps.example;

import net.wirelabs.jmaps.example.components.ConfigPanel;
import net.wirelabs.jmaps.example.components.MainWindow;
import net.wirelabs.jmaps.example.components.MapPanel;

import javax.swing.JFrame;
import java.awt.EventQueue;


public class ExampleApplication {

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

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


}
