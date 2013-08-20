/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// Standard imports
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;

import java.applet.Applet;

// Application Specific imports
// none

/**
 * Base class representation of the applet for showing Java3D content.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DemoApplet extends Applet
{
    /** The canvas supplied by this applet */
    protected Canvas3D canvas;

    /**
     * Create a basic demo applet with the canvas created, but stopped.
     */
    public DemoApplet()
    {
    }

    public void init()
    {
        setLayout(new BorderLayout());

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        canvas = new Canvas3D(dev.getBestConfiguration(template));
        canvas.setStereoEnable(false);
        canvas.setDoubleBufferEnable(true);
        //canvas.stopRenderer();

        add(canvas, BorderLayout.CENTER);
    }


    /**
     * Start the rendering process now
     */
    public void start()
    {
        canvas.startRenderer();
    }

    /**
     * Stop the renderer completely as we are no longer visible.
     */
    public void stop()
    {
        canvas.stopRenderer();
    }
}
