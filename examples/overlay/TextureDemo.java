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
import java.io.IOException;
import java.util.HashMap;

// Application Specific imports
import org.j3d.renderer.java3d.geom.Axis;
import org.j3d.renderer.java3d.overlay.TextureOverlay;
import org.j3d.renderer.java3d.overlay.ImageOverlay;
import org.j3d.renderer.java3d.overlay.UpdateControlBehavior;
import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TextureDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public TextureDemo()
    {
        super("TextureOverlay test window");

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
        // this demo. Texture 1 has transparency, texture 2 does not.
        J3DTextureCache cache = J3DTextureCacheFactory.getCache();
        Texture2D texture1 = null;
        Texture2D texture2 = null;

        try
        {
            texture1 = (Texture2D)cache.fetchTexture("test_image.gif");
            texture1.setMagFilter(Texture.NICEST);
            texture1.setMinFilter(Texture.FASTEST);

            texture2 = (Texture2D)cache.fetchTexture("test_image2.gif");
            texture2.setMagFilter(Texture.NICEST);
            texture2.setMinFilter(Texture.FASTEST);
        }
        catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
            ioe.printStackTrace();
            return;
        }

        int w = texture1.getWidth();
        int h = texture1.getHeight();

        Dimension o_bounds = new Dimension(w, h);

        UpdateControlBehavior updater = new UpdateControlBehavior();
        updater.setSchedulingBounds(new BoundingSphere());
        view_tg.addChild(updater);

        TextureOverlay ovl1 = new TextureOverlay(canvas, o_bounds, texture1);
        ovl1.setLocation(10, 10);
        ovl1.setUpdateManager(updater);
        ovl1.setVisible(true);

        TextureOverlay ovl2 = new TextureOverlay(canvas, o_bounds, texture2);
        ovl2.setLocation(100, 10);
        ovl2.setUpdateManager(updater);
        ovl2.setVisible(true);

        view_tg.addChild(ovl1.getRoot());
        view_tg.addChild(ovl2.getRoot());
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        ovl1.initialize();
        ovl2.initialize();
    }

    public static void main(String[] argv)
    {
        TextureDemo demo = new TextureDemo();
        demo.setVisible(true);
    }
}
