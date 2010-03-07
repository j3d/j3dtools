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

import java.awt.image.BufferedImage;
import java.util.HashMap;

// Application Specific imports
import org.j3d.util.ImageUtils;

import org.j3d.renderer.java3d.overlay.ImageOverlay;
import org.j3d.renderer.java3d.overlay.UpdateControlBehavior;

import org.j3d.renderer.java3d.geom.Axis;

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class ImageDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public ImageDemo()
    {
        super("Image test window");

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

        // Load the test image, which should be in the same directory as
        // this demo.
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img1 = tk.createImage("test_image.gif");
        Image img2 = tk.createImage("test_image2.png");

        try
        {
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(img1, 0);
            mt.addImage(img2, 0);
            mt.waitForAll();
        }
        catch(InterruptedException ie)
        {
        }

        int w1 = img1.getWidth(null);
        int h1 = img1.getHeight(null);
        int w2 = img2.getWidth(null);
        int h2 = img2.getHeight(null);

        UpdateControlBehavior updater = new UpdateControlBehavior();
        updater.setSchedulingBounds(new BoundingSphere());
        view_tg.addChild(updater);

        ImageOverlay ovl1 =
            new ImageOverlay(canvas,
                             new Dimension(w1, h1),
                             ImageUtils.createBufferedImage(img1));
        ovl1.setLocation(10, 10);
        ovl1.setUpdateManager(updater);
        ovl1.setVisible(true);


        ImageOverlay ovl2 =
            new ImageOverlay(canvas,
                             new Dimension(w2, h2),
                             ImageUtils.createBufferedImage(img2));
        ovl2.setLocation(100, 10);
        ovl2.setUpdateManager(updater);
        ovl2.setVisible(true);

        view_tg.addChild(ovl1.getRoot());
        view_tg.addChild(ovl2.getRoot());
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        ovl1.initialize();
        ovl2.initialize();

        ovl1.repaint();
        ovl2.repaint();
    }

    public static void main(String[] argv)
    {
        ImageDemo demo = new ImageDemo();
        demo.setVisible(true);
    }
}
