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
import javax.media.j3d.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.Scene;

// Application Specific imports
import org.j3d.ui.LoaderFileFilter;
import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.NavigationStateManager;
import org.j3d.util.device.FileLoaderDescriptor;
import org.j3d.util.device.DeviceManager;

/**
 * Demonstration of the using the device manager to list file loaders, place a
 * file open dialog and put the contents into the scene.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class LoaderUsageDemo extends DemoJFrame
    implements ActionListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The file dialog to choose files */
    private JFileChooser fileDialog;

    /** The locale that all the J3d content goes into */
    private Locale locale;

    /** Device manager so we can find the loader instance */
    private DeviceManager manager;

    /** The current contents, if anything has already been loaded */
    private BranchGroup currentContent;

    /**
     * Create a basic mouse demo that uses fly, tilt and pan states.
     */
    public LoaderUsageDemo()
    {
        super("LoaderUsageDemo test window");

        manager = DeviceManager.getDeviceManager();
        List loaders = manager.getAllFileLoaders();

        Iterator itr = loaders.iterator();
        fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Open a 3D File");

        while(itr.hasNext())
        {
            FileLoaderDescriptor ldd = (FileLoaderDescriptor)itr.next();
            FileFilter ff = new LoaderFileFilter(ldd);
            fileDialog.addChoosableFileFilter(ff);
        }

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        JMenuItem open_item = new JMenuItem("Open");
        open_item.addActionListener(this);

        JMenu file_menu = new JMenu("File");
        file_menu.add(open_item);

        JMenuBar menubar = new JMenuBar();
        menubar.add(file_menu);

        setJMenuBar(menubar);

        createScene();
    }

    // Action Listener for the menuitems

    public void actionPerformed(ActionEvent evt)
    {
        int button = fileDialog.showOpenDialog(this);
        if(button != JFileChooser.APPROVE_OPTION)
            return;

        LoaderFileFilter ff = (LoaderFileFilter)fileDialog.getFileFilter();

        try
        {
            File selected_file = fileDialog.getSelectedFile();
            URL selected_url = selected_file.toURL();

            FileLoaderDescriptor fld = ff.getDescriptor();
            Loader ldr = manager.getFileLoader(fld);

            if(ldr != null)
            {
                ldr.setFlags(Loader.LOAD_ALL);

                Scene scene = ldr.load(selected_url);
                BranchGroup bg = scene.getSceneGroup();
                bg.setCapability(BranchGroup.ALLOW_DETACH);

                if(currentContent != null)
                    currentContent.detach();

                locale.addBranchGraph(bg);
                currentContent = bg;
            }
        }
        catch(IOException ioe)
        {
            System.out.println("Error loading file " + ioe.getMessage());
        }
    }

    // Setup the scene to add content to
    private void createScene()
    {
        Color3f white = new Color3f(1, 1, 1);

        VirtualUniverse universe = new VirtualUniverse();
        locale = new Locale(universe);

        BranchGroup view_group = new BranchGroup();

        ViewPlatform camera = new ViewPlatform();

        TransformGroup view_tg = new TransformGroup();
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        view_tg.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        view_tg.addChild(camera);
        view_group.addChild(view_tg);

        Point3d origin = new Point3d(0, 0, 0);
        BoundingSphere light_bounds =
            new BoundingSphere(origin, BACK_CLIP_DISTANCE);
        DirectionalLight headlight = new DirectionalLight();
        headlight.setColor(white);
        headlight.setInfluencingBounds(light_bounds);
        view_group.addChild(headlight);
        locale.addBranchGraph(view_group);

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);

        MouseViewHandler viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);

        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.FLY_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

        viewHandler.setViewInfo(view, view_tg);
        viewHandler.setNavigationSpeed(1);

        NavigationStateManager nav_mgr = new NavigationStateManager(canvas);
        nav_mgr.setMouseHandler(viewHandler);
    }

    public static void main(String[] argv)
    {
        LoaderUsageDemo demo = new LoaderUsageDemo();
        demo.setVisible(true);
    }
}
