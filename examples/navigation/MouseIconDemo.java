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
import org.j3d.ui.navigation.NavigationStateListener;
import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationStateManager;

/**
 * Demonstration of a mouse navigation in a world.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MouseIconDemo extends DemoFrame
    implements NavigationStateListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** The current navigation state we are in */
    private int navigationState;

    /** A label telling us what state we are in */
    private Label navTypeLabel;

    public MouseIconDemo()
    {
        super("MouseIconDemo test window");

        navTypeLabel = new Label("Navigation state: <none>");
        add(navTypeLabel, BorderLayout.SOUTH);

        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);

        NavigationStateManager nav_mgr = new NavigationStateManager(canvas);
        nav_mgr.setNavigationStateListener(this);
        nav_mgr.setMouseHandler(viewHandler);
        nav_mgr.setNavigationState(WALK_STATE);

        buildScene();
    }

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

        Shape3D torus = new Torus(0.125f, 0.5f, blueAppearance).getChild() ;
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
    }

    public static void main(String[] argv)
    {
        MouseIconDemo demo = new MouseIconDemo();
        demo.setVisible(true);
    }
}
