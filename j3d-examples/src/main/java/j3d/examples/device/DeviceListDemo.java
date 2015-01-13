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

package j3d.examples.device;

// External imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

// Local imports
import org.j3d.device.input.DeviceManager;
import org.j3d.device.input.InputDevice;
import org.j3d.device.input.jinput.USBManager;

/**
 * Demonstration of the device manager and the list of items it contains
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DeviceListDemo extends JFrame
{
    /**
     * Create a basic mouse demo that uses fly, tilt and pan states.
     */
    public DeviceListDemo()
    {
        super("DeviceListDemo test window");

        setLocation(40, 40);
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        DeviceManager mgr = new USBManager();
        InputDevice[] inputs = mgr.getDevices();

        int max_size = 	mgr.getNumDevices();

        JPanel p1 = new JPanel(new GridLayout(1, 3));

        p1.add(createDevicePanel("InputDevices", inputs, max_size));

        Container cont = getContentPane();
        cont.add(p1, BorderLayout.CENTER);
    }

    /**
     * Construct one panel from the device list
     *
     * @param title The title for the border
     * @param l List to create the panel from
     * @param size The max number of items to use
     * @return The panel they belong to
     */
    private JPanel createDevicePanel(String title, InputDevice[] l, int size)
    {
        JPanel p = new JPanel(new GridLayout(size, 1));

        EtchedBorder eb = new EtchedBorder();
        TitledBorder tb = new TitledBorder(eb, title);
        p.setBorder(tb);

        for(int i = 0; i < size; i++)
        {
            p.add(new JLabel(l[i].getName()));
        }

        return p;
    }

    public static void main(String[] argv)
    {
        DeviceListDemo demo = new DeviceListDemo();
        demo.setVisible(true);
    }
}
