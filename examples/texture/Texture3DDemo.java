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
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

// Application Specific imports
import org.j3d.geom.*;

import org.j3d.renderer.java3d.navigation.MouseViewHandler;
import org.j3d.renderer.java3d.navigation.FrameUpdateListener;
import org.j3d.ui.navigation.NavigationState;
import org.j3d.util.ImageUtils;

/**
 * Demonstration of the various pieces of standard geometry provided by
 * the org.j3d.geom package.
 * <p>
 * The objects are rendered on screen to show that all the values are correctly
 * generated. There is no capability to navigate around them or to change any
 * of the rendering attributes like the face set.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class Texture3DDemo extends DemoFrame
    implements ItemListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** Default size of texture to generate */
    private static final int DEFAULT_TEXTURE_SIZE = 64;

    // Constants indicating which geometry is currently showing
    private static final int BOX = 0;
    private static final int CONE = 1;
    private static final int SPHERE = 2;
    private static final int TORUS = 3;
    private static final int OFFSET_3D = 4;

    /** The polygon attributes that we're going to be toggling wireframe */
    private PolygonAttributes targetPolyAttr;

    /** Map for the rendering mode handling */
    private HashMap renderingMap;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** Radio button for the 2D texture option */
    private Checkbox texture2dButton;

    /** Radio button for the 3D texture option */
    private Checkbox texture3dButton;

    /** Radio button for the box geometry option */
    private Checkbox boxButton;

    /** Radio button for the cone geometry option */
    private Checkbox coneButton;

    /** Radio button for the sphere geometry option */
    private Checkbox sphereButton;

    /** Radio button for the torus geometry option */
    private Checkbox torusButton;

    /** Flag indicating if we are  */
    private boolean use2dTexture;

    /** The id of the current geometry type */
    private int currentGeometry;

    /** BitSet used to control the switch that shows the right geometry */
    private BitSet switchSelector;

    /** Switch showing the current geometry */
    private Switch geomSwitch;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public Texture3DDemo()
    {
        super("3D Texturing Demo");

        // Check to see that 3D textures are supported
        Map properties = canvas.queryProperties();
        Boolean has_3d_texture = (Boolean)properties.get("texture3DAvailable");
        if(has_3d_texture.booleanValue())
            System.out.println("3D texture mapping is available");
        else
        {
            System.out.println("No 3D texturing available");
            System.exit(0);
        }

        switchSelector = new BitSet();

        // Buttons to select rendering type
        CheckboxGroup chk_grp = new CheckboxGroup();
        Checkbox solid = new Checkbox("Faces", chk_grp, true);
        Checkbox wireframe = new Checkbox("Wireframe", chk_grp, false);
        Checkbox point = new Checkbox("Points", chk_grp, false);

        solid.addItemListener(this);
        wireframe.addItemListener(this);
        point.addItemListener(this);

        renderingMap = new HashMap();
        renderingMap.put(solid, new Integer(PolygonAttributes.POLYGON_FILL));
        renderingMap.put(wireframe, new Integer(PolygonAttributes.POLYGON_LINE));
        renderingMap.put(point, new Integer(PolygonAttributes.POLYGON_POINT));

        Panel p1 = new Panel(new GridLayout(1, 4));
        p1.setBackground(SystemColor.menu);
        p1.add(new Label("Render as: "));
        p1.add(solid);
        p1.add(wireframe);
        p1.add(point);

        add(p1, BorderLayout.SOUTH);

        // Buttons for 2D/3D texture
        chk_grp = new CheckboxGroup();
        texture2dButton = new Checkbox("2D", chk_grp, true);
        texture3dButton = new Checkbox("3D", chk_grp, false);

        texture2dButton.addItemListener(this);
        texture3dButton.addItemListener(this);

        Panel p2 = new Panel(new GridLayout(1, 2));
        p2.setBackground(SystemColor.menu);
        p2.add(texture2dButton);
        p2.add(texture3dButton);

        chk_grp = new CheckboxGroup();
        boxButton = new Checkbox("Box", chk_grp, true);
        coneButton = new Checkbox("Cone", chk_grp, false);
        sphereButton = new Checkbox("Sphere", chk_grp, false);
        torusButton = new Checkbox("Torus", chk_grp, false);

        boxButton.addItemListener(this);
        coneButton.addItemListener(this);
        sphereButton.addItemListener(this);
        torusButton.addItemListener(this);

        Panel p3 = new Panel(new GridLayout(6, 1));
        p3.setBackground(SystemColor.menu);
        p3.add(new Label());
        p3.add(new Label("Geometry type"));
        p3.add(boxButton);
        p3.add(coneButton);
        p3.add(sphereButton);
        p3.add(torusButton);

        Panel p4 = new Panel(new BorderLayout());
        p4.setBackground(SystemColor.menu);
        p4.add(new Label("Texture type"), BorderLayout.NORTH);
        p4.add(p2, BorderLayout.CENTER);
        p4.add(p3, BorderLayout.SOUTH);

        Panel p5 = new Panel(new BorderLayout());
        p5.setBackground(SystemColor.menu);
        p5.add(p4, BorderLayout.NORTH);

        add(p5, BorderLayout.EAST);

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
     * Process the item menus on the rendering menu
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        Object src = evt.getSource();
        if(evt.getStateChange() != ItemEvent.SELECTED)
            return;

        if(src == texture2dButton)
        {
            switchSelector.clear(currentGeometry + OFFSET_3D);
            switchSelector.set(currentGeometry);
            use2dTexture = true;
            geomSwitch.setChildMask(switchSelector);
        }
        else if(src == texture3dButton)
        {
            switchSelector.set(currentGeometry + OFFSET_3D);
            switchSelector.clear(currentGeometry);
            use2dTexture = false;
            geomSwitch.setChildMask(switchSelector);
        }
        else if(src == boxButton)
        {
            changeGeometry(BOX);
        }
        else if(src == coneButton)
        {
            changeGeometry(CONE);
        }
        else if(src == sphereButton)
        {
            changeGeometry(SPHERE);
        }
        else if(src == torusButton)
        {
            changeGeometry(TORUS);
        }
        else
        {
            Integer value = (Integer)renderingMap.get(src);
            targetPolyAttr.setPolygonMode(value.intValue());
        }
    }

    /**
     * Convenience function to change the selected geometry to the new
     * type. Automatically manages 2D v 3D issues.
     *
     * @param type The ID of the new type of geometry to use
     */
    private void changeGeometry(int type)
    {
        int old_id = use2dTexture ?
                     currentGeometry :
                     currentGeometry + OFFSET_3D;

        currentGeometry = type;

        int new_id = use2dTexture ?
                     currentGeometry :
                     currentGeometry + OFFSET_3D;

        switchSelector.clear(old_id);
        switchSelector.set(new_id);
        geomSwitch.setChildMask(switchSelector);
    }

    /**
     * Build the scenegraph for the canvas
     */
    private void buildScene()
    {
        Color3f ambientGrey = new Color3f(0.2f, 0.02f, 0.2f);
        Color3f white = new Color3f(1, 1, 1);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f grey = new Color3f(0.80f, 0.80f, 0.80f);
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

        // Now the geometry. Let's just add a couple of the basic primitives
        // for testing.
        Material mat = new Material();
        mat.setAmbientColor(ambientGrey);
        mat.setDiffuseColor(grey);
        mat.setSpecularColor(specular);
        mat.setShininess(75.0f);
        mat.setLightingEnable(true);

        targetPolyAttr = new PolygonAttributes();
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
        targetPolyAttr.setPolygonMode(targetPolyAttr.POLYGON_FILL);

        world_object_group.addChild(createObjects(mat));
        world_object_group.compile();

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
        view_group.addChild(viewHandler.getTimerBehavior());

        // Add them to the locale
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }

    /**
     * Create the basic objects. There are 4 objects - Box, Cone, Sphere,
     * Torus. Then two sets. The first 4 are generated using 2D coordinates.
     * The second are created using 3D coordinates.
     */
    private Switch createObjects(Material mat)
    {
        GeometryGenerator generator;

        GeometryData data_2d = new GeometryData();
        data_2d.geometryType = GeometryData.TRIANGLES;
        data_2d.geometryComponents = GeometryData.TEXTURE_2D_DATA |
                                     GeometryData.NORMAL_DATA;

        GeometryData data_3d = new GeometryData();
        data_3d.geometryType = GeometryData.TRIANGLES;
//        data_3d.geometryType = GeometryData.QUADS;
        data_3d.geometryComponents = GeometryData.TEXTURE_3D_DATA |
                                     GeometryData.NORMAL_DATA;

        int format_2d = GeometryArray.COORDINATES |
                        GeometryArray.NORMALS |
                        GeometryArray.TEXTURE_COORDINATE_2;

        int format_3d = GeometryArray.COORDINATES |
                        GeometryArray.NORMALS |
                        GeometryArray.TEXTURE_COORDINATE_3;

        // Create the texture objects needed
        Appearance app_2d = new Appearance();
        app_2d.setMaterial(mat);
        app_2d.setTexture(create2DTexture());
        app_2d.setPolygonAttributes(targetPolyAttr);

        Appearance app_3d = new Appearance();
        app_3d.setMaterial(mat);
        app_3d.setTexture(create3DTexture());
        app_3d.setPolygonAttributes(targetPolyAttr);

/*
        TexCoordGeneration tex_gen =
            new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
                                   TexCoordGeneration.TEXTURE_COORDINATE_3);
        tex_gen.setPlaneR(new Vector4f(0, 0, 1, 0));
        app_3d.setTexCoordGeneration(tex_gen);
*/

        generator = new BoxGenerator(1, 1, 1);
        generator.generate(data_2d);
        generator.generate(data_3d);

        data_3d.geometryType = GeometryData.TRIANGLES;

        GeometryArray geom_2d = new TriangleArray(data_2d.vertexCount, format_2d);
        geom_2d.setCoordinates(0, data_2d.coordinates);
        geom_2d.setNormals(0, data_2d.normals);
        geom_2d.setTextureCoordinates(0, 0, data_2d.textureCoordinates);

        GeometryArray geom_3d = new TriangleArray(data_3d.vertexCount, format_3d);
//        GeometryArray geom_3d = new QuadArray(data_3d.vertexCount, format_3d);

        geom_3d.setCoordinates(0, data_3d.coordinates);
        geom_3d.setNormals(0, data_3d.normals);
        geom_3d.setTextureCoordinates(0, 0, data_3d.textureCoordinates);

        Shape3D box_2d = new Shape3D(geom_2d, app_2d);
        Shape3D box_3d = new Shape3D(geom_3d, app_3d);

        data_2d.coordinates = null;
        data_3d.coordinates = null;
        data_2d.normals = null;
        data_3d.normals = null;
        data_2d.textureCoordinates = null;
        data_3d.textureCoordinates = null;

        generator = new ConeGenerator(1, 0.5f);
        generator.generate(data_2d);
        generator.generate(data_3d);

        geom_2d = new TriangleArray(data_2d.vertexCount, format_2d);
        geom_2d.setCoordinates(0, data_2d.coordinates);
        geom_2d.setNormals(0, data_2d.normals);
        geom_2d.setTextureCoordinates(0, 0, data_2d.textureCoordinates);

        geom_3d = new TriangleArray(data_3d.vertexCount, format_3d);
        geom_3d.setCoordinates(0, data_3d.coordinates);
        geom_3d.setNormals(0, data_3d.normals);
        geom_3d.setTextureCoordinates(0, 0, data_3d.textureCoordinates);

        Shape3D cone_2d = new Shape3D(geom_2d, app_2d);
        Shape3D cone_3d = new Shape3D(geom_3d, app_3d);

        data_2d.coordinates = null;
        data_3d.coordinates = null;
        data_2d.normals = null;
        data_3d.normals = null;
        data_2d.textureCoordinates = null;
        data_3d.textureCoordinates = null;

        generator = new SphereGenerator(0.5f);
        generator.generate(data_2d);
        generator.generate(data_3d);

        geom_2d = new TriangleArray(data_2d.vertexCount, format_2d);
        geom_2d.setCoordinates(0, data_2d.coordinates);
        geom_2d.setNormals(0, data_2d.normals);
        geom_2d.setTextureCoordinates(0, 0, data_2d.textureCoordinates);

        geom_3d = new TriangleArray(data_3d.vertexCount, format_3d);
        geom_3d.setCoordinates(0, data_3d.coordinates);
        geom_3d.setNormals(0, data_3d.normals);
        geom_3d.setTextureCoordinates(0, 0, data_3d.textureCoordinates);

        Shape3D sphere_2d = new Shape3D(geom_2d, app_2d);
        Shape3D sphere_3d = new Shape3D(geom_3d, app_3d);

        data_2d.coordinates = null;
        data_3d.coordinates = null;
        data_2d.normals = null;
        data_3d.normals = null;
        data_2d.textureCoordinates = null;
        data_3d.textureCoordinates = null;

        generator = new TorusGenerator(0.1f, 0.5f);
        generator.generate(data_2d);
        generator.generate(data_3d);

        geom_2d = new TriangleArray(data_2d.vertexCount, format_2d);
        geom_2d.setCoordinates(0, data_2d.coordinates);
        geom_2d.setNormals(0, data_2d.normals);
        geom_2d.setTextureCoordinates(0, 0, data_2d.textureCoordinates);

        geom_3d = new TriangleArray(data_3d.vertexCount, format_3d);
        geom_3d.setCoordinates(0, data_3d.coordinates);
        geom_3d.setNormals(0, data_3d.normals);
        geom_3d.setTextureCoordinates(0, 0, data_3d.textureCoordinates);

        Shape3D torus_2d = new Shape3D(geom_2d, app_2d);
        Shape3D torus_3d = new Shape3D(geom_3d, app_3d);

        currentGeometry = BOX;
        use2dTexture = true;
        switchSelector.set(BOX);

        geomSwitch = new Switch(Switch.CHILD_MASK, switchSelector);
        geomSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        geomSwitch.addChild(box_2d);
        geomSwitch.addChild(cone_2d);
        geomSwitch.addChild(sphere_2d);
        geomSwitch.addChild(torus_2d);
        geomSwitch.addChild(box_3d);
        geomSwitch.addChild(cone_3d);
        geomSwitch.addChild(sphere_3d);
        geomSwitch.addChild(torus_3d);

        return geomSwitch;
    }

    /**
     * Convenience method to create the 2D texture object.
     *
     * @return The object
     */
    private Texture2D create2DTexture()
    {
        Toolkit tk = getToolkit();
        Image img = tk.createImage("3d_texture_1.gif");

        try
        {
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(img, 0);
            mt.waitForAll();
        }
        catch(InterruptedException ie)
        {
        }

        BufferedImage buf_img = ImageUtils.createBufferedImage(img);

        ImageComponent2D comp =
            new ImageComponent2D(ImageComponent.FORMAT_RGB, buf_img);

        int img_width = buf_img.getWidth(null);
        int img_height = buf_img.getHeight(null);

        // Setup the texture.

        Texture2D ret_val = new Texture2D(Texture.BASE_LEVEL,
                                          Texture.RGB,
                                          img_width,
                                          img_height);
        ret_val.setImage(0, comp);
        return ret_val;
    }

    /**
     * Convenience method to create the 2D texture object.
     *
     * @return The object
     */
    private Texture3D create3DTexture()
    {
        Toolkit tk = getToolkit();
        Image img1 = tk.createImage("3d_texture_1.gif");
        Image img2 = tk.createImage("3d_texture_2.gif");

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

        BufferedImage[] buf_img =
        {
            ImageUtils.createBufferedImage(img1),
            ImageUtils.createBufferedImage(img2)
        };

        int img_width = buf_img[0].getWidth(null);
        int img_height = buf_img[0].getHeight(null);


        ImageComponent3D comp =
            new ImageComponent3D(ImageComponent.FORMAT_RGB, img_width, img_height, 2);
        comp.set(0, buf_img[0]);
        comp.set(1, buf_img[1]);

        // Setup the texture. Cheat - depth is always 2.
        Texture3D ret_val = new Texture3D(Texture.BASE_LEVEL,
                                          Texture.RGB,
                                          img_width,
                                          img_height,
                                          2);
        ret_val.setImage(0, comp);
        ret_val.setBoundaryModeT(Texture.CLAMP);
        ret_val.setBoundaryModeS(Texture.CLAMP);
        ret_val.setBoundaryModeR(Texture.CLAMP);

        return ret_val;
    }

    /**
     * Functional entry point to run this demo.
     */
    public static void main(String[] argv)
    {
        Texture3DDemo demo = new Texture3DDemo();
        demo.setVisible(true);
    }
}
