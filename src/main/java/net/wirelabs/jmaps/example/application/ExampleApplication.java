package net.wirelabs.jmaps.example.application;

import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.EventQueue;

public class ExampleApplication extends JFrame {


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
               
                MapPanel map = new MapPanel();

                JPanel config = new ConfigPanel(map.getMapViewer());
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new MigLayout("","[85%, grow][15%,grow]","[grow]"));
                frame.getContentPane().add(map,"cell 0 0, grow");
                frame.getContentPane().add(config, "cell 1 0, grow");


                frame.setBounds(100, 100, 900, 600);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
