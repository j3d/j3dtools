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

import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;

// Application Specific imports
// none

/**
 * Demonstration of a mouse navigation in a world.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DemoFrame extends Frame implements WindowListener
{
    public DemoFrame(String title)
    {
        super(title);

        setLocation(30, 10);
        addWindowListener(this);
    }

    /**
     * Creates a new canvas each time this is called.
     *
     * @return A new canvas instance
     */
    protected Canvas3D createCanvas()
    {
        Canvas3D canvas;

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        canvas = new Canvas3D(dev.getBestConfiguration(template));
        canvas.setStereoEnable(false);
        canvas.setDoubleBufferEnable(true);

        return canvas;
    }

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowOpened(WindowEvent evt)
    {
    }
}
