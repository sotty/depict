/****************************************************************************
*                                                                           *
*  OpenNI 1.x Alpha                                                         *
*  Copyright (C) 2011 PrimeSense Ltd.                                       *
*                                                                           *
*  This file is part of OpenNI.                                             *
*                                                                           *
*  OpenNI is free software: you can redistribute it and/or modify           *
*  it under the terms of the GNU Lesser General Public License as published *
*  by the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                      *
*                                                                           *
*  OpenNI is distributed in the hope that it will be useful,                *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
*  GNU Lesser General Public License for more details.                      *
*                                                                           *
*  You should have received a copy of the GNU Lesser General Public License *
*  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
*                                                                           *
****************************************************************************/
package org.OpenNI.Samples.UserTracker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.ActionListener;

public class CopyOfUserTrackerApplication {

    /**
	 * 
	 */
	//public UserTracker viewer;

	
    public CopyOfUserTracker viewer;
   	private boolean shouldRun = true;
	private JFrame frame;

    public CopyOfUserTrackerApplication (JFrame frame)
    {
    	this.frame = frame;
    	frame.addKeyListener(new KeyListener()
		{
			public void keyTyped(KeyEvent arg0) {}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					shouldRun = false;
				}
			}
		});
    }

    public static void main(String s[])
    {
        JFrame f = new JFrame("OpenNI User Tracker");
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        CopyOfUserTrackerApplication app = new CopyOfUserTrackerApplication(f);
        
        app.viewer = new CopyOfUserTracker();
        
        f.add("Center", app.viewer);
        f.pack();
        f.setVisible(true);
        app.run();
    }

    void run() {
        while(shouldRun) {

            viewer.updateDepth();
            viewer.repaint();
        }
        frame.dispose();
        viewer.writeFlush();
    }
    
}
