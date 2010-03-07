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

import java.util.HashMap;

// Application Specific imports
import org.j3d.renderer.java3d.geom.Axis;
import org.j3d.renderer.java3d.overlay.LabelOverlay;
import org.j3d.renderer.java3d.overlay.Overlay;
import org.j3d.renderer.java3d.overlay.UpdateControlBehavior;

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class LabelDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public LabelDemo()
    {
        super("Label test window");

        buildScene();
    }

    /**
     * Build the scenegraph for the canvas
     */
    private void buildScene()
    {
        Color3f white = new Color3f(1, 1, 1);

        VirtualUniverse universe = new VirtualUniverse();
        Locale locale = new Locale(universe);

        BranchGroup view_group = new BranchGroup();
        BranchGroup world_object_group = new BranchGroup();

        ViewPlatform camera = new ViewPlatform();

        Transform3D angle = new Transform3D();
        angle.setTranslation(new Vector3d(0, 2, 10));

        TransformGroup view_tg = new TransformGroup(angle);
        view_tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        view_tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
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
        world_object_group.compile();

        // Add them to the locale
        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);

        UpdateControlBehavior updater = new UpdateControlBehavior();
        updater.setSchedulingBounds(new BoundingSphere());
        view_tg.addChild(updater);

        Color bg_color = new Color(0.1f, 0.1f, 0.1f, 1f);
        Dimension ovl_size = new Dimension(64, 32);

        LabelOverlay o1 = new LabelOverlay(canvas, ovl_size, "Default");
        o1.setLocation(0, 0);
        o1.setBackgroundColor(bg_color);
        o1.setUpdateManager(updater);
        o1.setVisible(true);

        LabelOverlay o2 = new LabelOverlay(canvas, ovl_size, "Left");
        o2.setLocation(0, 32);
        o2.setBackgroundColor(bg_color);
        o2.setHorizontalAlignment(LabelOverlay.LEFT_ALIGN);
        o2.setUpdateManager(updater);
        o2.setVisible(true);

        LabelOverlay o3 = new LabelOverlay(canvas, ovl_size, "Center");
        o3.setLocation(0, 64);
        o3.setBackgroundColor(bg_color);
        o3.setHorizontalAlignment(LabelOverlay.CENTER_ALIGN);
        o3.setUpdateManager(updater);
        o3.setVisible(true);

        LabelOverlay o4 = new LabelOverlay(canvas, ovl_size, "Right");
        o4.setLocation(0, 96);
        o4.setBackgroundColor(bg_color);
        o4.setHorizontalAlignment(LabelOverlay.RIGHT_ALIGN);
        o4.setUpdateManager(updater);
        o4.setVisible(true);

        LabelOverlay o5 = new LabelOverlay(canvas, ovl_size, "Default");
        o5.setLocation(0, 128);
        o5.setBackgroundColor(bg_color);
        o5.setUpdateManager(updater);
        o5.setVisible(true);

        LabelOverlay o6 = new LabelOverlay(canvas, ovl_size, "Top");
        o6.setLocation(64, 128);
        o6.setBackgroundColor(bg_color);
        o6.setVerticalAlignment(LabelOverlay.TOP_ALIGN);
        o6.setUpdateManager(updater);
        o6.setVisible(true);

        LabelOverlay o7 = new LabelOverlay(canvas, ovl_size, "Center");
        o7.setLocation(128, 128);
        o7.setBackgroundColor(bg_color);
        o7.setVerticalAlignment(LabelOverlay.CENTER_ALIGN);
        o7.setUpdateManager(updater);
        o7.setVisible(true);

        LabelOverlay o8 = new LabelOverlay(canvas, ovl_size, "Bottom");
        o8.setLocation(192, 128);
        o8.setBackgroundColor(bg_color);
        o8.setVerticalAlignment(LabelOverlay.BOTTOM_ALIGN);
        o8.setUpdateManager(updater);
        o8.setVisible(true);

        view_tg.addChild(o1.getRoot());
        view_tg.addChild(o2.getRoot());
        view_tg.addChild(o3.getRoot());
        view_tg.addChild(o4.getRoot());
        view_tg.addChild(o5.getRoot());
        view_tg.addChild(o6.getRoot());
        view_tg.addChild(o7.getRoot());
        view_tg.addChild(o8.getRoot());
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        o1.setAntialiased(false);
        o2.setAntialiased(false);
        o3.setAntialiased(false);
        o4.setAntialiased(false);
        o5.setAntialiased(false);
        o6.setAntialiased(false);
        o7.setAntialiased(false);
        o8.setAntialiased(false);

        o1.initialize();
        o2.initialize();
        o3.initialize();
        o4.initialize();
        o5.initialize();
        o6.initialize();
        o7.initialize();
        o8.initialize();
    }

    public static void main(String[] argv)
    {
        LabelDemo demo = new LabelDemo();
        demo.setVisible(true);
    }
}
