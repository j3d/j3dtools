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
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.io.IOException;
import java.util.HashMap;

// Application Specific imports
import org.j3d.texture.TextureCache;
import org.j3d.texture.TextureCacheFactory;

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CacheDemo extends DemoFrame
    implements ItemListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** Map of radio button to file name */
    private HashMap nameMap;

    /** Cache used to store and fetch images from */
    private TextureCache cache;

    /** The appearance we used to change the texture with */
    private Appearance appearance;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public CacheDemo()
    {
        super("Cache test window");

        nameMap = new HashMap();
        cache = TextureCacheFactory.getCache();

        add(canvas, BorderLayout.CENTER);

        buildScene();

        JPanel p1 = new JPanel(new FlowLayout());

        ButtonGroup grp = new ButtonGroup();
        JRadioButton button = new JRadioButton("GIF Image");
        button.addItemListener(this);
        grp.add(button);
        nameMap.put(button, "test_image.gif");
        p1.add(button);

        button = new JRadioButton("JPEG Image");
        button.addItemListener(this);
        grp.add(button);
        nameMap.put(button, "test_image.jpg");
        p1.add(button);

        button = new JRadioButton("PNG Image");
        button.addItemListener(this);
        grp.add(button);
        nameMap.put(button, "test_image.png");
        p1.add(button);

        add(p1, BorderLayout.SOUTH);
    }

    /**
     * Process the change of state request from the colour selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            Object src = evt.getSource();
            String img_name = (String)nameMap.get(src);

            try
            {
                Texture tex = cache.fetchTexture(img_name);
                appearance.setTexture(tex);
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
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

        // Now the geometry. Let's just add a couple of the basic primitives
        // for testing.
        Material material = new Material();
        material.setAmbientColor(ambientBlue);
        material.setDiffuseColor(blue);
        material.setSpecularColor(specular);
        material.setShininess(75.0f);
        material.setLightingEnable(true);

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        int format = QuadArray.COORDINATES |
                     QuadArray.TEXTURE_COORDINATE_2;
        float[] vertices = {
            0, 0, 0,  1, 0, 0,  1, 1, 0,  0, 1, 0
        };

        float[] tex_coords = {
            0, 0,  1, 0,  1, 1,  0, 1
        };

        QuadArray geom = new QuadArray(4, format);
        geom.setCoordinates(0, vertices);
        geom.setTextureCoordinates(0, 0, tex_coords);

        Shape3D shape = new Shape3D(geom, appearance);
        world_object_group.addChild(shape);

        // Add them to the locale

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }

    public static void main(String[] argv)
    {
        CacheDemo demo = new CacheDemo();
        demo.setVisible(true);
    }
}
