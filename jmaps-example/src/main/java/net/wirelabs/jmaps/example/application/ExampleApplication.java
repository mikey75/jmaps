package net.wirelabs.jmaps.example.application;

import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.viewer.geo.Coordinate;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.EventQueue;
import java.io.File;
import java.io.InputStream;



public class ExampleApplication extends JFrame {


    private static MapPanel map;
    private static File mapfile = new File("jmaps-example/src/main/resources/map.xml");

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl","true");

        EventQueue.invokeLater(() -> {
            try {

                map = new MapPanel();

                JPanel config = new ConfigPanel(map.getMapViewer());
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new MigLayout("","[85%, grow][15%,grow]","[grow]"));
                frame.getContentPane().add(map,"cell 0 0, grow");
                frame.getContentPane().add(config, "cell 1 0, grow");


                frame.setBounds(100, 100, 900, 600);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);

                Coordinate home = new Coordinate( 22.49004,51.23264);
                map.getMapViewer().setHome(home);
                map.getMapViewer().setMap(mapfile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


}
