/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

import org.j3d.device.input.vfx.*;

/**
 * Demo for The VFX 3D HMD setup.
 */
public class VFXTrackerDemo extends JFrame
{
    /**
     * Construct a new instance of the demo.
     */
    public VFXTrackerDemo()
    {
        super("VFX HMD demo");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(GraphicsConfigTemplate3D.REQUIRED);
//        template.setStereo(GraphicsConfigTemplate3D.REQUIRED);

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        GraphicsConfiguration gfx_config =
            dev.getBestConfiguration(template);

        Container content_pane = getContentPane();

        Canvas3D canvas = new Canvas3D(gfx_config);
        content_pane.add(canvas);
    }

    public static void main(String[] args)
    {
        //change this to the appropriate serial port identifier
        final VFXDriver inputDevice = VFXDriver.getVFXDriver();
        inputDevice.resetZeroPosition();

        if(inputDevice != null)
        {
            // Stick a small screen up so that there is something to
            // kill.
            JFrame window = new VFXTrackerDemo();
            window.setSize(100, 100);
            window.setVisible(true);
        }

        Thread th = new Thread() {
            float[] val = new float[3];
            public void run() {
                while(true) {
                    inputDevice.getTrackerPosition(val);
                    System.out.print("read ");
                    System.out.print(val[0]);
                    System.out.print(' ');
                    System.out.print(val[1]);
                    System.out.print(' ');
                    System.out.print(val[2]);
                    System.out.println();

                    try {
                      Thread.sleep(100);
                    } catch(InterruptedException ie) {
                    }
                }
            }
        };

        th.run();
    }
}