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
import javax.swing.*;
import javax.media.j3d.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

// Application Specific imports
// none

/**
 * Demonstration of a mouse navigation in a world.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DemoJFrame extends JFrame
{
    /** The canvas supplied by this frame */
    protected Canvas3D canvas;

    public DemoJFrame(String title)
    {
        super(title);

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        canvas = new Canvas3D(dev.getBestConfiguration(template));
        canvas.setStereoEnable(false);
        canvas.setDoubleBufferEnable(true);

        Container content = getContentPane();
        content.add(canvas, BorderLayout.CENTER);

        setSize(400, 400);
        setLocation(40, 40);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Cursor curse = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        canvas.setCursor(curse);
    }
}
