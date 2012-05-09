package org.OpenNI.Samples.UserTracker;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TrackingGUI extends JFrame {


    public TrackingGUI( String title ) throws HeadlessException {
        super(title);
    }

    TrackPanel panel;

    public void initialize( TrackPanel panel ) {
        this.panel = panel;

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });

        if ( panel != null ) {
            this.add("Center", panel);
        } else {
            this.add("Center", new Label( "Cam rendering disabled") );
        }

        this.pack();
        this.setVisible(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        if ( panel != null ) {
            panel.setVisible( false );
        }
    }
}
