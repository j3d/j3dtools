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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

// Application Specific imports
import org.j3d.util.device.DeviceDescriptor;
import org.j3d.util.device.DeviceManager;
import org.j3d.ui.DeviceDescriptorJLabel;

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
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        DeviceManager mgr = DeviceManager.getDeviceManager();
        List loaders = mgr.getAllFileLoaders();
        List audio = mgr.getAllAudioDevices();
        List inputs = mgr.getAllInputDevices();

        int max_size = loaders. size();
        max_size = (max_size > audio.size()) ? max_size : audio.size();
        max_size = (max_size > inputs.size()) ? max_size : inputs.size();

        JPanel p1 = new JPanel(new GridLayout(1, 3));

        p1.add(createDevicePanel("File Loaders", loaders, max_size));
        p1.add(createDevicePanel("InputDevices", inputs, max_size));
        p1.add(createDevicePanel("AudioDevices", audio, max_size));

        Container cont = getContentPane();
        cont.add(p1, BorderLayout.NORTH);

        pack();
    }

    /**
     * Construct one panel from the device list
     *
     * @param title The title for the border
     * @param l List to create the panel from
     * @param size The max number of items to use
     * @return The panel they belong to
     */
    private JPanel createDevicePanel(String title, List l, int size)
    {
        JPanel p = new JPanel(new GridLayout(size, 1));

        EtchedBorder eb = new EtchedBorder();
        TitledBorder tb = new TitledBorder(eb, title);
        p.setBorder(tb);

        Iterator itr = l.iterator();

        while(itr.hasNext())
        {
            DeviceDescriptor dd = (DeviceDescriptor)itr.next();
            p.add(new DeviceDescriptorJLabel(dd));
        }

        return p;
    }

    public static void main(String[] argv)
    {
        DeviceListDemo demo = new DeviceListDemo();
        demo.setVisible(true);
    }
}
