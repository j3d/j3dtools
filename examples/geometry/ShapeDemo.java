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
import org.j3d.geom.*;

import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;

/**
 * Demonstration of the various pieces of geometry objects based on
 * Shape3D provided by the org.j3d.geom package.
 * <p>
 * The objects are rendered on screen to show that all the values are correctly
 * generated. There is no capability to navigate around them or to change any
 * of the rendering attributes like the face set.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ShapeDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public ShapeDemo()
    {
        super("Shape Demo test window");

        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.FLY_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

        buildScene();
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

        Transform3D angle = new Transform3D();
        angle.setTranslation(new Vector3d(0, 2, 10));

        TransformGroup view_tg = new TransformGroup(angle);
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
        view_tg.addChild(headlight);

        // And the axis...
        Axis axis = new Axis();
        world_object_group.addChild(axis);

        // A couple of labels.
        Shape3D label1 = new RasterTextLabel("Label 1", null);
        Shape3D label2 = new RasterTextLabel("Label 2", Color.red, 0, 0.5f, 0);
        Shape3D label3 = new RasterTextLabel("Label 3",
                                             Color.blue,
                                             0,
                                             1.0f,
                                             0,
                                             Color.green,
                                             null);


        world_object_group.addChild(label1);
        world_object_group.addChild(label2);
        world_object_group.addChild(label3);
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
        ShapeDemo demo = new ShapeDemo();
        demo.setVisible(true);
    }
}
