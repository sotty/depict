package it.unibo.deis.lia.depict;


import javax.swing.*;
import java.awt.*;

public class TrackingGUI extends JFrame {


    TrackPanel panel;
    JRECPanel eventPanel;


    public TrackingGUI(String title, GraphicsConfiguration configuration ) throws HeadlessException {
        super( title, configuration );
    }


    public void initialize( TrackPanel panel, JRECPanel eventPanel ) {


        this.panel = panel;
        this.eventPanel = eventPanel;

        if ( panel != null ) {
            this.getContentPane().add( panel, BorderLayout.CENTER );
        } else {
            this.getContentPane().add( new Label( "Cam rendering disabled" ), BorderLayout.CENTER );
        }

        if ( eventPanel != null ) {
            this.getContentPane().add( eventPanel, BorderLayout.SOUTH );
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

    public TrackPanel getPanel() {
        return panel;
    }

    public JRECPanel getEventPanel() {
        return eventPanel;
    }
}
