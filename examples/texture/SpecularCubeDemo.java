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
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

// Application Specific imports
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.renderer.java3d.navigation.MouseViewHandler;
import org.j3d.renderer.java3d.navigation.FrameUpdateListener;
import org.j3d.ui.navigation.NavigationState;
import org.j3d.texture.TextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;

/**
 * Demonstration of a cubic environment map combined with multitexturing to
 * produce specular highlights.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SpecularCubeDemo extends DemoFrame
    implements FrameUpdateListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** Number of verticies in a triangle strip for one side */
    private static final int BOX_SIDE_VERTEX_COUNT = 4;

    /** The list of strip counts for the background box */
    private static final int[] BOX_STRIP_COUNTS = { BOX_SIDE_VERTEX_COUNT };

    /** Format flags used for the geometry array */
    private static final int BOX_FORMAT =
        GeometryArray.COORDINATES |
        GeometryArray.TEXTURE_COORDINATE_2 |
        GeometryArray.NORMALS;

    /** The 2D texture coordinates for the box side */
    private static final float[] BOX_TEX_COORDS = {
        0, 1,  0, 0,  1, 0, 1, 1
    };

    /** Texture for the left side */
    private Texture leftTex;
    private ImageComponent leftComp;

    /** Texture for the right side */
    private Texture blackTex;
    private ImageComponent blackComp;


    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** Attributes of the cube map so we can update the texture */
    private TextureAttributes cubeTexAttr;

    /** Transformation used to rotate the cube map */
    private Transform3D cubeTransform;

    /** Quaternion for transfering rotations */
    private Quat4d orientation;

    /** Appearance used by all */
    private Appearance appearance;

    /** Texture unit corresponding to the Cube Map */
    private TextureUnitState cubeTexUnit;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public SpecularCubeDemo()
    {
        super("SpecularCube window");

        orientation = new Quat4d();

        viewHandler = new MouseViewHandler();
        viewHandler.setNavigationSpeed(0.5f);
        viewHandler.setOrbitTime(10);
        viewHandler.setCanvas(canvas);
        viewHandler.setFrameUpdateListener(this);

        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.EXAMINE_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

        int width = loadTextures();
        View view = buildScene();

        buildTexturePanel(width);
        view.addCanvas3D(canvas);

        add(canvas, BorderLayout.CENTER);

        Map props = canvas.queryProperties();
        System.out.println("Cube map available? " + props.get("textureCubeMapAvailable"));
        System.out.println("Num texture units? " + props.get("textureUnitStateMax"));
    }

    /**
     * Called when a transition from one position to another has ended.
     */
    public void transitionEnded(Transform3D t3d)
    {
    }

    /**
     * Called after each phase of transition or mouse navigation.
     */
    public void viewerPositionUpdated(Transform3D t3d)
    {
        // read the rotational component, because that's all we need.
        t3d.get(orientation);
        orientation.negate();
        cubeTransform.set(orientation);
        cubeTexAttr.setTextureTransform(cubeTransform);
    }

    /**
     * Build the texture panel and also place the textures into the multitexture
     * setup.
     *
     * @return The panel that contains the texture management UI
     */
    private void buildTexturePanel(int imgWidth)
    {
        J3DTextureCache tex_ldr = J3DTextureCacheFactory.getCache();

        ImageComponent bump_comp = null;
        ImageComponent color_comp = null;

        try
        {
            bump_comp = tex_ldr.fetchImageComponent("bump_map.jpg");
            color_comp = tex_ldr.fetchImageComponent("colour_map.jpg");
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading in texture " + ioe);
        }

        int w = bump_comp.getWidth();
        int h = bump_comp.getHeight();
        Texture bump_tex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, w, h);
        bump_tex.setImage(0, bump_comp);

        w = color_comp.getWidth();
        h = color_comp.getHeight();
        Texture color_tex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, w, h);
        color_tex.setImage(0, color_comp);

        bump_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        color_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);

        bump_tex.setBoundaryModeS(Texture.CLAMP);
        bump_tex.setBoundaryModeT(Texture.CLAMP);
        bump_tex.setMinFilter(Texture.NICEST);
        bump_tex.setMagFilter(Texture.NICEST);

        color_tex.setBoundaryModeS(Texture.CLAMP);
        color_tex.setBoundaryModeT(Texture.CLAMP);
        color_tex.setMinFilter(Texture.NICEST);
        color_tex.setMagFilter(Texture.NICEST);

        TextureAttributes bump_attr = new TextureAttributes();
        TextureAttributes color_attr = new TextureAttributes();
        TextureAttributes specular_attr = new TextureAttributes();

        bump_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
        color_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
        specular_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);

        // Now fill them all in....
        bump_attr.setTextureMode(TextureAttributes.COMBINE);
        bump_attr.setCombineRgbMode(TextureAttributes.COMBINE_DOT3);
        bump_attr.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);
        bump_attr.setCombineRgbSource(0, TextureAttributes.COMBINE_TEXTURE_COLOR);
        bump_attr.setCombineAlphaSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);
        bump_attr.setTextureBlendColor(0.5f, 0, 0, 0);

        color_attr.setTextureMode(TextureAttributes.MODULATE);

        // Rotate the specular cube map a bit so you can see the visual
        // effects
        Transform3D spec_tx = new Transform3D();
        spec_tx.rotY(-Math.PI / 4);
        specular_attr.setTextureTransform(spec_tx);

        specular_attr.setTextureMode(TextureAttributes.COMBINE);
        specular_attr.setCombineRgbMode(TextureAttributes.COMBINE_ADD);
        specular_attr.setCombineRgbSource(0, TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
        specular_attr.setCombineRgbSource(1, TextureAttributes.COMBINE_TEXTURE_COLOR);

        TextureCubeMap tex_cube =
            new TextureCubeMap(Texture.BASE_LEVEL, Texture.RGB, imgWidth);
        tex_cube.setCapability(Texture.ALLOW_ENABLE_WRITE);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_X,
                          (ImageComponent2D)blackComp);

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_X,
                          (ImageComponent2D)leftComp);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_Y,
                          (ImageComponent2D)blackComp);

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_Y,
                          (ImageComponent2D)blackComp);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_Z,
                          (ImageComponent2D)blackComp);

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_Z,
                          (ImageComponent2D)blackComp);

        TexCoordGeneration cube_coords =
            new TexCoordGeneration(TexCoordGeneration.NORMAL_MAP,
                                   TexCoordGeneration.TEXTURE_COORDINATE_3);

        TextureUnitState[] state_list =
        {
            new TextureUnitState(bump_tex, bump_attr, null),
            new TextureUnitState(color_tex, color_attr, null),
            new TextureUnitState(tex_cube, specular_attr, cube_coords),
        };

        appearance.setTextureUnitState(state_list);
    }

    /**
     * Build the texture panel and also place the textures into the multitexture
     * setup.
     *
     * @return The size of the texture in width pixels
     */
    private int loadTextures()
    {
        J3DTextureCache tex_ldr = J3DTextureCacheFactory.getCache();

        try
        {
            leftComp = tex_ldr.fetchImageComponent("left_specular_map.gif");
            blackComp = tex_ldr.fetchImageComponent("other_specular_map.gif");
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading in texture " + ioe);
        }


        int img_width = leftComp.getWidth();
        int img_height = leftComp.getHeight();

        if(img_width != img_height)
            throw new IllegalStateException("Non-square env maps");

        leftTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);
        blackTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);

        leftTex.setImage(0, leftComp);
        blackTex.setImage(0, blackComp);

        leftTex.setBoundaryModeS(Texture.CLAMP);
        leftTex.setBoundaryModeT(Texture.CLAMP);
        leftTex.setMinFilter(Texture.NICEST);
        leftTex.setMagFilter(Texture.NICEST);

        blackTex.setBoundaryModeS(Texture.CLAMP);
        blackTex.setBoundaryModeT(Texture.CLAMP);
        blackTex.setMinFilter(Texture.NICEST);
        blackTex.setMagFilter(Texture.NICEST);

        return img_width;
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
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        view_tg.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        view_tg.addChild(camera);

        view_group.addChild(view_tg);

        Point3d origin = new Point3d(0, 0, 0);
        BoundingSphere back_bounds =
            new BoundingSphere(origin, BACK_CLIP_DISTANCE);
        DirectionalLight headback = new DirectionalLight();
        headback.setColor(white);
        headback.setInfluencingBounds(back_bounds);
        view_tg.addChild(headback);

        // Now the geometry. Let's just add a couple of the basic primitives
        // for testing.
        Material material = new Material();
        material.setAmbientColor(ambientBlue);
        material.setDiffuseColor(blue);
        material.setSpecularColor(specular);
        material.setShininess(75.0f);
        material.setLightingEnable(true);

        TransparencyAttributes trans_attr =
            new TransparencyAttributes(TransparencyAttributes.NICEST, 0);

        cubeTexAttr = new TextureAttributes();
        cubeTexAttr.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

        cubeTransform = new Transform3D();

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setTextureAttributes(cubeTexAttr);
        appearance.setTransparencyAttributes(trans_attr);

        appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);
        appearance.clearCapabilityIsFrequent(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);

        SphereGenerator generator = new SphereGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;
        generator.generate(data);

        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS |
                     GeometryArray.TEXTURE_COORDINATE_2;

        int[] tex_set = { 0, 0, -1  };

        TriangleArray geom = new TriangleArray(data.vertexCount, format, 3, tex_set);
        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);
        geom.setTextureCoordinates(0, 0, data.textureCoordinates);

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

        viewHandler.setViewInfo(view, view_tg);
        viewHandler.setNavigationSpeed(1);

        view_group.addChild(viewHandler.getTimerBehavior());

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        return view;
    }

    public static void main(String[] argv)
    {
        SpecularCubeDemo demo = new SpecularCubeDemo();
        demo.setVisible(true);
    }
}
