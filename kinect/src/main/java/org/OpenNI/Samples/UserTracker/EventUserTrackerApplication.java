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

import http.it.unibo.deis.lia.depict.KinectModelFactory;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EventUserTrackerApplication {

    /**
     *
     */
    private EventUserTracker tracker;

    private boolean shouldRun = true;
    private boolean render = true;
//    private boolean render = false;

    public EventUserTrackerApplication()
    {

        TrackingGUI frame = new TrackingGUI( "Kinect Detect" );
        this.tracker = new EventUserTracker( KinectModelFactory.MODEL_TYPES.DROOLS, frame, render );

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

    public static void main(String s[]) {
        new EventUserTrackerApplication().run();
    }

    void run() {
        while( shouldRun ) {
            tracker.resample();
        }
        tracker.dispose();
    }

}
