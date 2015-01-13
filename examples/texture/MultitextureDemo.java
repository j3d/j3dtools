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

// External imports
import java.awt.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.io.IOException;
import java.util.HashMap;

import com.sun.j3d.utils.image.TextureLoader;

// Local imports
// None

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MultitextureDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** Map of radio button to file name */
    private HashMap nameMap;

    /** The appearance we used to change the texture with */
    private Appearance appearance;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public MultitextureDemo()
    {
        super("Multitexture window");

        nameMap = new HashMap();

        View view = buildScene();
        JPanel p1 = buildTexturePanel();

        canvas.setBackground(Color.blue);
        view.addCanvas3D(canvas);

        add(p1, BorderLayout.EAST);
        add(canvas, BorderLayout.CENTER);
    }

    /**
     * Build the texture panel and also place the textures into the multitexture
     * setup.
     *
     * @return The panel that contains the texture management UI
     */
    private JPanel buildTexturePanel()
    {
        int color_format = TextureLoader.BY_REFERENCE | TextureLoader.Y_UP | Texture.RGB;
        int alpha_format = TextureLoader.BY_REFERENCE | TextureLoader.Y_UP | Texture.ALPHA;
        int light_format = TextureLoader.BY_REFERENCE | TextureLoader.Y_UP | Texture.LUMINANCE;

        TextureLoader bump_tl =
            new TextureLoader("bump_map.jpg", color_format, this);
        TextureLoader colour_tl =
            new TextureLoader("colour_map.jpg", color_format, this);
        TextureLoader stencil_tl =
            new TextureLoader("stencil_map.png", color_format, this);
        TextureLoader light_tl =
            new TextureLoader("light_map.jpg", color_format, this);

        Texture bump_tex = bump_tl.getTexture();
        Texture colour_tex = colour_tl.getTexture();
        Texture stencil_tex = stencil_tl.getTexture();
        Texture light_tex = light_tl.getTexture();

        bump_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        colour_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        stencil_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        light_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);

        bump_tex.setBoundaryModeS(Texture.CLAMP);
        bump_tex.setBoundaryModeT(Texture.CLAMP);
        bump_tex.setMinFilter(Texture.NICEST);
        bump_tex.setMagFilter(Texture.NICEST);

        colour_tex.setBoundaryModeS(Texture.CLAMP);
        colour_tex.setBoundaryModeT(Texture.CLAMP);
        colour_tex.setMinFilter(Texture.NICEST);
        colour_tex.setMagFilter(Texture.NICEST);

        stencil_tex.setBoundaryModeS(Texture.CLAMP);
        stencil_tex.setBoundaryModeT(Texture.CLAMP);
        stencil_tex.setMinFilter(Texture.NICEST);
        stencil_tex.setMagFilter(Texture.NICEST);

        light_tex.setBoundaryModeS(Texture.CLAMP);
        light_tex.setBoundaryModeT(Texture.CLAMP);
        light_tex.setMinFilter(Texture.NICEST);
        light_tex.setMagFilter(Texture.NICEST);

        TextureAttributes bump_attr = new TextureAttributes();
        TextureAttributes colour_attr = new TextureAttributes();
        TextureAttributes stencil_attr = new TextureAttributes();
        TextureAttributes light_attr = new TextureAttributes();

        // Now fill them all in....
        bump_attr.setTextureMode(TextureAttributes.COMBINE);
        bump_attr.setCombineRgbMode(TextureAttributes.COMBINE_DOT3);
        bump_attr.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);
        bump_attr.setCombineRgbSource(0, TextureAttributes.COMBINE_TEXTURE_COLOR);
        bump_attr.setCombineAlphaSource(0, TextureAttributes.COMBINE_CONSTANT_COLOR);

        bump_attr.setTextureBlendColor(0.5f, 0, 0, 0);
        bump_attr.setCapability(TextureAttributes.ALLOW_BLEND_COLOR_WRITE);

        colour_attr.setTextureMode(TextureAttributes.MODULATE);
        stencil_attr.setTextureMode(TextureAttributes.MODULATE);
        light_attr.setTextureMode(TextureAttributes.MODULATE);
        light_attr.setCapability(TextureAttributes.ALLOW_TRANSFORM_READ);
        light_attr.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

        TextureUnitState[] state_list =
        {
            new TextureUnitState(bump_tex, bump_attr, null),
            new TextureUnitState(colour_tex, colour_attr, null),
            new TextureUnitState(stencil_tex, stencil_attr, null),
            new TextureUnitState(light_tex, light_attr, null)
        };

        appearance.setTextureUnitState(state_list);

        return new MTControlPanel(bump_tex,
                                  colour_tex,
                                  stencil_tex,
                                  light_tex,
                                  bump_attr,
                                  light_attr);
    }

    /**
     * Build the scenegraph for the canvas
     */
    private View buildScene()
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
        angle.setTranslation(new Vector3d(0, 0, 2));

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
        appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);
        appearance.clearCapabilityIsFrequent(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);

        int format = QuadArray.COORDINATES |
                     QuadArray.TEXTURE_COORDINATE_2;
        float[] vertices = {
            -0.5f, -1, 0,  0.5f, -1, 0,  0.5f, 1, 0,  -0.5f, 1, 0
        };

        float[] tex_coords = {
            0, 0,  1, 0,  1, 1,  0, 1
        };

        float[] light_tex_coords = {
            0.25f, 0.25f,  0.75f, 0.25f,  0.75f, 0.75f,  0.25f, 0.75f
        };

        int[] tex_set = { 0, 0, 0, 1 };

        QuadArray geom = new QuadArray(4, format, 4, tex_set);
        geom.setCoordinates(0, vertices);
        geom.setTextureCoordinates(0, 0, tex_coords);
        geom.setTextureCoordinates(1, 0, light_tex_coords);

        Shape3D shape = new Shape3D(geom, appearance);
        world_object_group.addChild(shape);

        // Add them to the locale
        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.attachViewPlatform(camera);

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        return view;
    }

    public static void main(String[] argv)
    {
        MultitextureDemo demo = new MultitextureDemo();
        demo.setVisible(true);
    }
}
