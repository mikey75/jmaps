package net.wirelabs.jmaps.example.components;

import lombok.NoArgsConstructor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;


@NoArgsConstructor
public class TitledPanel extends JPanel {


    public TitledPanel(String name) {

        setBorder(new TitledBorder(name));

        if (customLayout() != null) {
            setLayout(customLayout());
        }
    }

    protected LayoutManager customLayout() {
        return null;
    }
}
