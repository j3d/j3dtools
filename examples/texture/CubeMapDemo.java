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

import com.sun.j3d.utils.image.TextureLoader;

// Application Specific imports
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.FrameUpdateListener;

/**
 * Demonstration of a cubic environment map.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CubeMapDemo extends DemoFrame
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

    /** Texture for the right side */
    private Texture rightTex;

    /** Texture for the front side */
    private Texture frontTex;

    /** Texture for the back side */
    private Texture backTex;

    /** Texture for the top side */
    private Texture topTex;

    /** Texture for the bottom side */
    private Texture bottomTex;

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

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public CubeMapDemo()
    {
        super("CubeMap window");

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
        View view = buildScene(width);

        JPanel p1 = new MapControlPanel(bgSwitch, appearance);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(p1, BorderLayout.NORTH);

        canvas.setBackground(Color.blue);
        view.addCanvas3D(canvas);

        add(p2, BorderLayout.EAST);
        add(canvas, BorderLayout.CENTER);

        Map props = canvas.queryProperties();
        System.out.println("Cube map available? " + props.get("textureCubeMapAvailable"));
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
     * @return The size of the texture in width pixels
     */
    private int loadTextures()
    {
        int color_format = TextureLoader.BY_REFERENCE | Texture.RGB;

        TextureLoader left_tl =
            new TextureLoader("left_cube_map.gif", this);
        TextureLoader right_tl =
            new TextureLoader("right_cube_map.gif", this);
        TextureLoader front_tl =
            new TextureLoader("front_cube_map.gif", this);
        TextureLoader back_tl =
            new TextureLoader("back_cube_map.gif", this);
        TextureLoader top_tl =
            new TextureLoader("top_cube_map.gif", this);
        TextureLoader bottom_tl =
            new TextureLoader("bottom_cube_map.gif", this);

        leftTex = left_tl.getTexture();

        int img_width = leftTex.getWidth();
        int img_height = leftTex.getHeight();

        if(img_width != img_height)
            throw new IllegalStateException("Non-square env maps");

        rightTex = right_tl.getTexture();
        frontTex = front_tl.getTexture();
        backTex = back_tl.getTexture();
        topTex = top_tl.getTexture();
        bottomTex = bottom_tl.getTexture();

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
    private View buildScene(int imgWidth)
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

        TextureCubeMap tex_cube =
            new TextureCubeMap(Texture.BASE_LEVEL, Texture.RGB, imgWidth);

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_X,
                          (ImageComponent2D)rightTex.getImage(0));

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_X,
                          (ImageComponent2D)leftTex.getImage(0));

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_Y,
                          (ImageComponent2D)topTex.getImage(0));

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_Y,
                          (ImageComponent2D)bottomTex.getImage(0));

        tex_cube.setImage(0,
                          TextureCubeMap.POSITIVE_Z,
                          (ImageComponent2D)backTex.getImage(0));

        tex_cube.setImage(0,
                          TextureCubeMap.NEGATIVE_Z,
                          (ImageComponent2D)frontTex.getImage(0));

        TexCoordGeneration coord_gen =
            new TexCoordGeneration(TexCoordGeneration.NORMAL_MAP,
                                   TexCoordGeneration.TEXTURE_COORDINATE_3);

        cubeTexAttr = new TextureAttributes();
        cubeTexAttr.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

        cubeTransform = new Transform3D();

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setTexture(tex_cube);
        appearance.setTexCoordGeneration(coord_gen);
        appearance.setTextureAttributes(cubeTexAttr);

        appearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
        appearance.clearCapabilityIsFrequent(Appearance.ALLOW_TEXGEN_WRITE);

        SphereGenerator generator = new SphereGenerator(0.5f, 40);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;
        generator.generate(data);

        int format = QuadArray.COORDINATES | QuadArray.NORMALS;

        QuadArray geom = new QuadArray(data.vertexCount, format);
        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);

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
        CubeMapDemo demo = new CubeMapDemo();
        demo.setVisible(true);
    }
}
