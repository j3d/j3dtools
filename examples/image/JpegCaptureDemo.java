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
import javax.vecmath.*;

// Application Specific imports
import org.j3d.geom.Torus;
import org.j3d.ui.image.JPEGImageObserver;
import org.j3d.ui.navigation.NavigationStateListener;
import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationStateManager;

/**
 * Demonstration of a mouse navigation in a world that presents a GUI
 * representation.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class JpegCaptureDemo extends DemoFrame
    implements ActionListener, NavigationStateListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** The current navigation state we are in */
    private int navigationState;

    /** A label telling us what state we are in */
    private Label navTypeLabel;

    /** The observer used to do the image capture */
    private JPEGImageObserver imageHandler;

    /** Text field with the file name in it */
    private TextField filename;

    /** The button to say "capture now" */
    private Button capture;

    /** The button to say "reset the observer" now */
    private Button reset;

    public JpegCaptureDemo()
    {
        super("JpegCaptureDemo test window");

        navTypeLabel = new Label("Navigation state: <none>");
        add(navTypeLabel, BorderLayout.SOUTH);

        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);

        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK, FLY_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK, TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK, PAN_STATE);

        NavigationStateManager nav_mgr = new NavigationStateManager(canvas);
        nav_mgr.setNavigationStateListener(this);
        nav_mgr.setMouseHandler(viewHandler);

        Panel p1 = new Panel(new GridLayout(2, 1));

        Panel p2 = new Panel(new BorderLayout());

        Label l1 = new Label("Output file: ");
        filename = new TextField(System.getProperty("user.dir"));

        p2.add(l1, BorderLayout.EAST);
        p2.add(filename, BorderLayout.CENTER);

        Panel p3 = new Panel(new GridLayout(1, 2));

        capture = new Button("Capture Now");
        capture.addActionListener(this);

        reset = new Button("Reset");
        reset.addActionListener(this);

        p3.add(capture);
        p3.add(reset);


        p1.add(p2);
        p1.add(p3);

        add(p1, BorderLayout.NORTH);

        buildScene();

        imageHandler = new JPEGImageObserver();
        canvas.addCaptureObserver(imageHandler);
    }

    // Methods for ActionListener

    /**
     * Process an action event (probably from the capture button)
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        Object src = evt.getSource();

        if(src == capture)
        {
            // Get the filename and then
            String name = filename.getText();

            if(name == null)
                navTypeLabel.setText("No filename set");

            name = name.trim();

            if(name.length() == 0)
                navTypeLabel.setText("No filename set");

            try
            {
                imageHandler.setFilename(name);
                imageHandler.setCaptureNextFrame();

                // force the re-render as we know that the content does not
                // render usually because it is static
                canvas.getView().repaint();
            }
            catch(IllegalStateException ise)
            {
                navTypeLabel.setText("Oops, it seems like you tried to " +
                                     "capture again without a reset");
            }
        }
        else
        {
            // This is the reset
            canvas.removeCaptureObserver(imageHandler);
            imageHandler = new JPEGImageObserver();
            canvas.addCaptureObserver(imageHandler);
        }
    }

    // Methods for NavigationStateListener

    /**
     * Callback to ask the listener what navigation state it thinks it is in.
     *
     * @return the current navigation state
     */
    public int getNavigationState()
    {
       return navigationState;
    }

    /**
     * Set the navigation state to the new state for display
     *
     * @param state The new state to be
     */
    public void setNavigationState(int state)
    {
        String label = null;

        switch(state)
        {
            case WALK_STATE:
                label = "Navigation state: Walk";
                break;

            case PAN_STATE:
                label = "Navigation state: Pan";
                break;

            case TILT_STATE:
                label = "Navigation state: Tilt";
                break;

            case FLY_STATE:
                label = "Navigation state: Fly";
                break;

            case EXAMINE_STATE:
                label = "Navigation state: Examine";
                break;

            case NO_STATE:
                label = "Navigation state: <none>";
                break;

        }

        if(label != null)
            navTypeLabel.setText(label);
    }

    /**
     * Build the scenegraph for the canvas
     */
    private void buildScene()
    {
        Color3f ambientBlue = new Color3f(0.0f, 0.02f, 0.5f);
        Color3f white = new Color3f(1, 1, 1);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f blue = new Color3f(0.00f, 0.20f, 0.80f);
        Color3f specular = new Color3f(0.7f, 0.7f, 0.7f);

        VirtualUniverse universe = new VirtualUniverse();
        Locale locale = new Locale(universe);

        BranchGroup view_group = new BranchGroup();
        BranchGroup world_object_group = new BranchGroup();

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

        // Now the geometry. Let's just add a couple of the basic primitives
        // for testing.
        Material blueMaterial =
           new Material(ambientBlue, black, blue, specular, 75.0f);
        blueMaterial.setLightingEnable(true);

        Appearance blueAppearance = new Appearance();
        blueAppearance.setMaterial(blueMaterial);

        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(pa.POLYGON_LINE);
        pa.setCullFace(pa.CULL_NONE);
        blueAppearance.setPolygonAttributes(pa);

        Transform3D torus_angle = new Transform3D();
        torus_angle.setRotation(new AxisAngle4d(1, 0, 0, 0.78));
        torus_angle.setTranslation(new Vector3d(0, 0, -4));

        TransformGroup torus_tg = new TransformGroup(torus_angle);

        Shape3D torus = new Torus(0.125f, 0.5f, blueAppearance);
        torus_tg.addChild(torus);

        world_object_group.addChild(torus_tg);
        world_object_group.compile();

        // Add them to the locale
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);

        viewHandler.setViewInfo(view, view_tg);
        viewHandler.setNavigationSpeed(1.0f);
    }

    public static void main(String[] argv)
    {
        JpegCaptureDemo demo = new JpegCaptureDemo();
        demo.setVisible(true);
    }
}
