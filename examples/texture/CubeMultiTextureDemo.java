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
import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.FrameUpdateListener;
import org.j3d.texture.TextureCache;
import org.j3d.texture.TextureCacheFactory;

/**
 * Demonstration of a cubic environment map combined with multitexturing.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CubeMultiTextureDemo extends DemoFrame
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
    private Texture rightTex;
    private ImageComponent rightComp;

    /** Texture for the front side */
    private Texture frontTex;
    private ImageComponent frontComp;

    /** Texture for the back side */
    private Texture backTex;
    private ImageComponent backComp;

    /** Texture for the top side */
    private Texture topTex;
    private ImageComponent topComp;

    /** Texture for the bottom side */
    private Texture bottomTex;
    private ImageComponent bottomComp;

    /** Switch used to control appearance of background image */
    private Switch bgSwitch;

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
    public CubeMultiTextureDemo()
    {
        super("CubeMultiTexture window");

        orientation = new Quat4d();

        viewHandler = new MouseViewHandler();
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

        JPanel p2 = buildTexturePanel(width);
        JPanel p1 = new MapControlPanel(bgSwitch, cubeTexUnit);

        JPanel p3 = new JPanel(new BorderLayout());
        p3.add(p1, BorderLayout.NORTH);
        p3.add(p2, BorderLayout.SOUTH);

        canvas.setBackground(Color.blue);
        view.addCanvas3D(canvas);

        add(p3, BorderLayout.EAST);
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
    private JPanel buildTexturePanel(int imgWidth)
    {
        TextureCache tex_ldr = TextureCacheFactory.getCache();

        ImageComponent bump_comp = null;
        ImageComponent stencil_comp = null;
        ImageComponent light_comp = null;

        try
        {
            bump_comp = tex_ldr.fetchImageComponent("bump_map.jpg");
            stencil_comp = tex_ldr.fetchImageComponent("stencil_map.png");
            light_comp = tex_ldr.fetchImageComponent("light_map.jpg");
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading in texture " + ioe);
        }

        int w = bump_comp.getWidth();
        int h = bump_comp.getHeight();
        Texture bump_tex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, w, h);
        bump_tex.setImage(0, bump_comp);

        w = stencil_comp.getWidth();
        h = stencil_comp.getHeight();
        Texture stencil_tex = new Texture2D(Texture.BASE_LEVEL, Texture.LUMINANCE_ALPHA, w, h);
        stencil_tex.setImage(0, stencil_comp);

        w = light_comp.getWidth();
        h = light_comp.getHeight();
        Texture light_tex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, w, h);
        light_tex.setImage(0, light_comp);

        bump_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        stencil_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        light_tex.setCapability(Texture.ALLOW_ENABLE_WRITE);

        bump_tex.setBoundaryModeS(Texture.CLAMP);
        bump_tex.setBoundaryModeT(Texture.CLAMP);
        bump_tex.setMinFilter(Texture.NICEST);
        bump_tex.setMagFilter(Texture.NICEST);

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

        bump_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
        colour_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
        stencil_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
        light_attr.setPerspectiveCorrectionMode(TextureAttributes.NICEST);

        // Now fill them all in....
        bump_attr.setTextureMode(TextureAttributes.COMBINE);
        bump_attr.setCombineRgbMode(TextureAttributes.COMBINE_DOT3);
        bump_attr.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);
        bump_attr.setCombineRgbSource(0, TextureAttributes.COMBINE_TEXTURE_COLOR);
        bump_attr.setCombineAlphaSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);

        bump_attr.setTextureBlendColor(0.5f, 0, 0, 0);
        bump_attr.setCapability(TextureAttributes.ALLOW_BLEND_COLOR_WRITE);

        colour_attr.setTextureMode(TextureAttributes.MODULATE);

        stencil_attr.setTextureMode(TextureAttributes.COMBINE);
        stencil_attr.setCombineRgbMode(TextureAttributes.COMBINE_REPLACE);
        stencil_attr.setCombineRgbFunction(0, TextureAttributes.COMBINE_SRC_COLOR);
        stencil_attr.setCombineRgbSource(0, TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
        stencil_attr.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);
        stencil_attr.setCombineAlphaFunction(0, TextureAttributes.COMBINE_SRC_ALPHA);
        stencil_attr.setCombineAlphaSource(0, TextureAttributes.COMBINE_TEXTURE_COLOR);

        light_attr.setTextureMode(TextureAttributes.MODULATE);
        light_attr.setCapability(TextureAttributes.ALLOW_TRANSFORM_READ);
        light_attr.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

        TextureCubeMap tex_cube =
            new TextureCubeMap(Texture.BASE_LEVEL, Texture.RGB, imgWidth);
        tex_cube.setCapability(Texture.ALLOW_ENABLE_WRITE);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_X,
                          (ImageComponent2D)rightComp);

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_X,
                          (ImageComponent2D)leftComp);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_Y,
                          (ImageComponent2D)topComp);

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_Y,
                          (ImageComponent2D)bottomComp);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_Z,
                          (ImageComponent2D)backComp);

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_Z,
                          (ImageComponent2D)frontComp);

        TexCoordGeneration coord_gen =
            new TexCoordGeneration(TexCoordGeneration.NORMAL_MAP,
                                   TexCoordGeneration.TEXTURE_COORDINATE_3);

        TextureUnitState[] state_list =
        {
            new TextureUnitState(bump_tex, bump_attr, null),
            new TextureUnitState(tex_cube, colour_attr, coord_gen),
            new TextureUnitState(stencil_tex, stencil_attr, null),
            new TextureUnitState(light_tex, light_attr, null)
        };

        cubeTexUnit = state_list[1];
        cubeTexUnit.setCapability(TextureUnitState.ALLOW_STATE_WRITE);

        appearance.setTextureUnitState(state_list);

        return new MTControlPanel(bump_tex,
                                  tex_cube,
                                  stencil_tex,
                                  light_tex,
                                  bump_attr,
                                  light_attr);
    }

    /**
     * Build the texture panel and also place the textures into the multitexture
     * setup.
     *
     * @return The size of the texture in width pixels
     */
    private int loadTextures()
    {
        TextureCache tex_ldr = TextureCacheFactory.getCache();

        try
        {
            leftComp = tex_ldr.fetchImageComponent("left_cube_map.gif");
            rightComp = tex_ldr.fetchImageComponent("right_cube_map.gif");
            frontComp = tex_ldr.fetchImageComponent("front_cube_map.gif");
            backComp = tex_ldr.fetchImageComponent("back_cube_map.gif");
            topComp = tex_ldr.fetchImageComponent("top_cube_map.gif");
            bottomComp = tex_ldr.fetchImageComponent("bottom_cube_map.gif");
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
        rightTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);
        frontTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);
        backTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);
        topTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);
        bottomTex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, img_width, img_height);

        leftTex.setImage(0, leftComp);
        rightTex.setImage(0, rightComp);
        frontTex.setImage(0, frontComp);
        backTex.setImage(0, backComp);
        topTex.setImage(0, topComp);
        bottomTex.setImage(0, bottomComp);

        leftTex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        rightTex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        frontTex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        backTex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        topTex.setCapability(Texture.ALLOW_ENABLE_WRITE);
        bottomTex.setCapability(Texture.ALLOW_ENABLE_WRITE);

        leftTex.setBoundaryModeS(Texture.CLAMP);
        leftTex.setBoundaryModeT(Texture.CLAMP);
        leftTex.setMinFilter(Texture.NICEST);
        leftTex.setMagFilter(Texture.NICEST);

        rightTex.setBoundaryModeS(Texture.CLAMP);
        rightTex.setBoundaryModeT(Texture.CLAMP);
        rightTex.setMinFilter(Texture.NICEST);
        rightTex.setMagFilter(Texture.NICEST);

        frontTex.setBoundaryModeS(Texture.CLAMP);
        frontTex.setBoundaryModeT(Texture.CLAMP);
        frontTex.setMinFilter(Texture.NICEST);
        frontTex.setMagFilter(Texture.NICEST);

        backTex.setBoundaryModeS(Texture.CLAMP);
        backTex.setBoundaryModeT(Texture.CLAMP);
        backTex.setMinFilter(Texture.NICEST);
        backTex.setMagFilter(Texture.NICEST);

        topTex.setBoundaryModeS(Texture.CLAMP);
        topTex.setBoundaryModeT(Texture.CLAMP);
        topTex.setMinFilter(Texture.NICEST);
        topTex.setMagFilter(Texture.NICEST);

        bottomTex.setBoundaryModeS(Texture.CLAMP);
        bottomTex.setBoundaryModeT(Texture.CLAMP);
        bottomTex.setMinFilter(Texture.NICEST);
        bottomTex.setMagFilter(Texture.NICEST);

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

        // Build the background box. It is a Background object that uses
        // geometry to build an inverted box to put the textures on. Above it
        // is a switch node to allow turning it on and off through the UI.
        Group bg_group = new Group();
        constructBackgroundBox(bg_group);

        bgSwitch = new Switch(Switch.CHILD_ALL);
        bgSwitch.addChild(bg_group);
        bgSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

        Bounds infinite_bounds =
            new BoundingSphere(new Point3d(), Double.MAX_VALUE);

        BranchGroup bg_geom = new BranchGroup();
        bg_geom.addChild(bgSwitch);

        Background background = new Background(bg_geom);
        background.setApplicationBounds(infinite_bounds);

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

/*
        SphereGenerator generator = new SphereGenerator(0.5f, 40);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;
        generator.generate(data);

        int format = QuadArray.COORDINATES | QuadArray.NORMALS;

        QuadArray geom = new QuadArray(data.vertexCount, format);
        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);
*/

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

        int[] tex_set = { 0, -1, 0, 1 };

        QuadArray geom = new QuadArray(4, format, 4, tex_set);
        geom.setCoordinates(0, vertices);
        geom.setTextureCoordinates(0, 0, tex_coords);
        geom.setTextureCoordinates(1, 0, light_tex_coords);


        Shape3D shape = new Shape3D(geom, appearance);
        world_object_group.addChild(shape);
        world_object_group.addChild(background);

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

    /**
     * Build the background box structure. The box is composed of 6 separate
     * sides so that they can individually have a texture assigned to them.
     *
     * @param bgGroup The group to add the geometry to
     */
    private void constructBackgroundBox(Group bgGroup)
    {
        // Create all the geometry at once....

        // Setup the texture attributes. These nominate the defaults, so its
        // probably a waste currently, but we may want to change these at a
        // later date depending on the requirements/performance.
        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.REPLACE);
        tex_attr.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);

        // Create the shape geometrys for each side. There is no need for appearance as
        // we are going to create this as an unlit, white box.
        Shape3D shape;
        Appearance app;

        // unit box back coordinates
        float[] back_coords = {
            1, 1, 1, 1, -1, 1, -1, -1, 1,  -1, 1, 1,
        };

        float[] back_normals = {
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1
        };

        app = new Appearance();
        app.setTexture(backTex);
        app.setTextureAttributes(tex_attr);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(back_coords, back_normals));
        bgGroup.addChild(shape);

        // unit box front coordinates
        float[] front_coords = {
            -1, 1, -1,  -1, -1, -1,  1, -1, -1, 1, 1, -1
        };

        float[] front_normals = {
             0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
        };

        app = new Appearance();
        app.setTexture(frontTex);
        app.setTextureAttributes(tex_attr);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(front_coords, front_normals));
        bgGroup.addChild(shape);

        // unit box left coordinates
        float[] left_coords = {
            -1, 1, 1,  -1, -1, 1,  -1, -1, -1,  -1, 1, -1
        };

        float[] left_normals = {
             1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0
        };

        app = new Appearance();
        app.setTexture(leftTex);
        app.setTextureAttributes(tex_attr);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(left_coords, left_normals));
        bgGroup.addChild(shape);

        // unit box right coordinates
        float[] right_coords = {
            1, 1, -1, 1, -1, -1, 1, -1, 1,  1, 1, 1
        };

        float[] right_normals = {
             -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
        };

        app = new Appearance();
        app.setTexture(rightTex);
        app.setTextureAttributes(tex_attr);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(right_coords, right_normals));
        bgGroup.addChild(shape);

        // unit box top coordinates
        float[] top_coords = {
            -1, 1, 1,  -1, 1, -1,  1, 1, -1,  1, 1, 1
        };

        float[] top_normals = {
             0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0
        };

        app = new Appearance();
        app.setTexture(topTex);
        app.setTextureAttributes(tex_attr);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(top_coords, top_normals));
        bgGroup.addChild(shape);

        // unit box bottom coordinates
        float[] bottom_coords = {
            -1, -1, -1,  -1, -1, 1,  1, -1, 1,  1, -1, -1
        };


        float[] bottom_normals = {
             0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0
        };

        app = new Appearance();
        app.setTexture(bottomTex);
        app.setTextureAttributes(tex_attr);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(createSideGeom(bottom_coords, bottom_normals));
        bgGroup.addChild(shape);
    }

    /**
     * Convenience method to create the side geometry
     *
     * @param coords THe coordinates to use
     * @param normals The normals to use
     * @return The geometry representing this side
     */
    private GeometryArray createSideGeom(float[] coords, float[] normals)
    {
        QuadArray array =
            new QuadArray(BOX_SIDE_VERTEX_COUNT, BOX_FORMAT);

        array.setCoordinates(0, coords);
        array.setNormals(0, normals);
        array.setTextureCoordinates(0, 0, BOX_TEX_COORDS);

        return array;
    }

    public static void main(String[] argv)
    {
        CubeMultiTextureDemo demo = new CubeMultiTextureDemo();
        demo.setVisible(true);
    }
}
