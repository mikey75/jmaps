package net.wirelabs.jmaps.example.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.LayoutManager;

/**
 * Created 10/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MainWindow extends JFrame {

    public MainWindow(JPanel mapPanel, JPanel configPanel) {

        LayoutManager mainWindowLayout = new MigLayout(
                "",
                "[85%, grow][15%,grow]",
                "[grow]"
        );

        setLayout(mainWindowLayout);
        add(mapPanel, "cell 0 0, grow");
        add(configPanel, "cell 1 0, grow");

        setBounds(100, 100, 900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
