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
import org.j3d.geom.Axis;
import org.j3d.geom.overlay.ImageButtonOverlay;
import org.j3d.geom.overlay.OverlayManager;
import org.j3d.geom.overlay.UpdateControlBehavior;
import org.j3d.util.ImageUtils;

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ImageButtonDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public ImageButtonDemo()
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
        Image active_img = tk.createImage("active.gif");
        Image inactive_img = tk.createImage("inactive.gif");
        Image click_img = tk.createImage("clicked.gif");
        Image mouse_img = tk.createImage("mouseover.gif");

        try
        {
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(active_img, 0);
            mt.addImage(inactive_img, 1);
            mt.addImage(click_img, 2);
            mt.addImage(mouse_img, 3);
            mt.waitForAll();
        }
        catch(InterruptedException ie)
        {
        }

        int w = active_img.getWidth(null);
        int h = active_img.getHeight(null);

        BufferedImage[] images = new BufferedImage[4];
        images[ImageButtonOverlay.INACTIVE_IMAGE] =
            ImageUtils.createBufferedImage(inactive_img);
        images[ImageButtonOverlay.ACTIVE_IMAGE] =
            ImageUtils.createBufferedImage(active_img);
        images[ImageButtonOverlay.CLICKED_IMAGE] =
            ImageUtils.createBufferedImage(click_img);
        images[ImageButtonOverlay.MOUSEOVER_IMAGE] =
            ImageUtils.createBufferedImage(mouse_img);

        UpdateControlBehavior updater = new UpdateControlBehavior();
        updater.setSchedulingBounds(new BoundingSphere());
        view_tg.addChild(updater);

        ImageButtonOverlay overlay =
            new ImageButtonOverlay(canvas,
                                   new Rectangle(10, 10, w, h),
                                   images);
        overlay.setVisible(true);

        OverlayManager mgr = new OverlayManager(canvas, updater);
        mgr.addOverlay(overlay);

        view_tg.addChild(mgr);
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        overlay.initialize();
    }

    public static void main(String[] argv)
    {
        ImageButtonDemo demo = new ImageButtonDemo();
        demo.setVisible(true);
    }
}
