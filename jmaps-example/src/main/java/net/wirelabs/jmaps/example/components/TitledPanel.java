package net.wirelabs.jmaps.example.components;

import lombok.NoArgsConstructor;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;


@NoArgsConstructor
public abstract class TitledPanel extends JPanel {


    protected TitledPanel(String name) {

        setBorder(new TitledBorder(name));

        if (customLayout() != null) {
            setLayout(customLayout());
        }
    }

    protected LayoutManager customLayout() {
        return null;
    }
}
