package net.wirelabs.jmaps.example;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.example.components.ConfigPanel;
import net.wirelabs.jmaps.example.components.MainWindow;
import net.wirelabs.jmaps.example.components.MapPanel;

import javax.swing.*;
import java.awt.*;

@Slf4j
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
                configPanel.setFirstAvailableMap();

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

    }


}
