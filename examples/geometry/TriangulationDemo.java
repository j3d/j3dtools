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

import java.io.IOException;
import java.util.HashMap;

// Application Specific imports
import org.j3d.geom.*;

import org.j3d.ui.navigation.NavigationState;

import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;
import org.j3d.renderer.java3d.navigation.MouseViewHandler;

/**
 * Demonstration of the various pieces of standard geometry provided by
 * the org.j3d.geom package.
 * <p>
 * The objects are rendered on screen to show that all the values are correctly
 * generated. There is no capability to navigate around them or to change any
 * of the rendering attributes like the face set.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TriangulationDemo extends DemoFrame
    implements ItemListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The geometry that we're going to be setting the values for */
    private Shape3D targetShape;

    /** The material that we're going to be toggling lighting */
    private Material targetMaterial;

    /** The polygon attributes that we're going to be toggling wireframe */
    private PolygonAttributes targetPolyAttr;

    /** Map of menu item to the generator it uses */
    private HashMap generatorMap;

    /** Map of the polygon type menu item to the data type (integer) */
    private HashMap renderingMap;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** Menu item to select lighting or not rendering */
    private CheckboxMenuItem lightingMenuItem;

    /** Menu item to select front or back face culling */
    private CheckboxMenuItem cullingMenuItem;

    /** Menu item to select textured rendering or no */
    private CheckboxMenuItem textureMenuItem;

    /** The menu holding polygon items */
    private Menu renderingMenu;

    /** Flag indicating if we want real wireframe or just no faces */
    private boolean useTexture;

    /** Texture object for the current appearance */
    private Texture2D texture;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public TriangulationDemo()
    {
        super("Geometry Demo test window");

        renderingMap = new HashMap();

        MenuBar menu_bar = new MenuBar();
        renderingMenu = new Menu("Rendering Options");

        menu_bar.add(renderingMenu);

        textureMenuItem = new CheckboxMenuItem("Use texture");
        lightingMenuItem = new CheckboxMenuItem("Use Lighting");

        textureMenuItem.setState(false);
        lightingMenuItem.setState(true);

        textureMenuItem.addItemListener(this);
        lightingMenuItem.addItemListener(this);

        renderingMenu.add(textureMenuItem);
        renderingMenu.add(lightingMenuItem);

        setMenuBar(menu_bar);

        CheckboxGroup chk_grp = new CheckboxGroup();
        Checkbox solid = new Checkbox("Faces", chk_grp, true);
        Checkbox wireframe = new Checkbox("Wireframe", chk_grp, false);
        Checkbox point = new Checkbox("Points", chk_grp, false);

        solid.addItemListener(this);
        wireframe.addItemListener(this);
        point.addItemListener(this);

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

        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.FLY_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

        // The rendering menu
        useTexture = false;

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
        boolean on = (evt.getStateChange() == ItemEvent.SELECTED);


        Menu parent = null;

        if(src instanceof CheckboxMenuItem)
            parent = (Menu)((CheckboxMenuItem)src).getParent();
        Integer value;

        if(parent == renderingMenu)
        {
            if(src == lightingMenuItem)
            {
                targetMaterial.setLightingEnable(on);
            }
            else if(src == textureMenuItem)
            {
                useTexture = on;
            }
        }
        else
        {
            if(!on)
                return;

            value = (Integer)renderingMap.get(src);
            targetPolyAttr.setPolygonMode(value.intValue());
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
        targetMaterial = new Material();
        targetMaterial.setAmbientColor(ambientBlue);
        targetMaterial.setDiffuseColor(blue);
        targetMaterial.setSpecularColor(specular);
        targetMaterial.setShininess(75.0f);
        targetMaterial.setLightingEnable(true);
        targetMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);

        Appearance blue_appearance = new Appearance();
        blue_appearance.setMaterial(targetMaterial);

        targetPolyAttr = new PolygonAttributes();
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
        targetPolyAttr.setPolygonMode(targetPolyAttr.POLYGON_FILL);
        blue_appearance.setPolygonAttributes(targetPolyAttr);

        try {
            J3DTextureCache t_cache = J3DTextureCacheFactory.getCache();
            texture = (Texture2D)t_cache.fetchTexture("globe_map_2.jpg");
            texture.setCapability(Texture.ALLOW_ENABLE_WRITE);
            blue_appearance.setTexture(texture);
        } catch(IOException ioe) {
            System.out.println("error loading texture " + ioe);

        }

        targetShape = new Shape3D();
        targetShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        targetShape.setAppearance(blue_appearance);
        world_object_group.addChild(targetShape);

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

        TriangulationUtils triangulator = new TriangulationUtils();

        float[] coords =
        {
           -1.5f,    -1, 0,
           -1.5f,  0.5f, 0,
           -0.5f,  0.5f, 0,
           -0.5f,    -1, 0,
               2,    -1, 0,
               2,     1, 0,
            1.5f,     1, 0,
            1.5f, -0.5f, 0,
            0.5f, -0.5f, 0,
            0.5f,     1, 0,
              -2,     1, 0,
              -2,    -1, 0
        };

        float[] normals =
        {
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1
        };

        float[] tex_coords =
        {
            0.125f, 0,
            0.125f, 0.75f,
            0.375f, 0.75f,
            0.375f, 0,
            1, 0,
            1, 1,
            0.875f, 1,
            0.875f, 0.25f,
            0.675f, 0.25f,
            0.675f, 1,
            0, 1,
            0, 0
        };

        float[] normal = { 0, 0, 1 };
        int[] output = new int[30];

        int num = triangulator.triangulateConcavePolygon(coords,
                                                         0,
                                                         12,
                                                         output,
                                                         normal);

        // Adjust the output index values for the coordinates.
        for(int i = 0; i < output.length; i++)
            output[i] /= 3;

        // build up the arrays of triangles
        int format = IndexedTriangleArray.COORDINATES |
                     IndexedTriangleArray.NORMALS |
                     IndexedTriangleArray.TEXTURE_COORDINATE_2 |
                     IndexedTriangleArray.USE_COORD_INDEX_ONLY;

        IndexedTriangleArray ita =
            new IndexedTriangleArray(coords.length / 3, format, num * 3);

        ita.setCoordinates(0, coords);
        ita.setCoordinateIndices(0, output);
        ita.setNormals(0, normals);
        ita.setTextureCoordinates(0, 0, tex_coords);

        targetShape.addGeometry(ita);
    }

    public static void main(String[] argv)
    {
        TriangulationDemo demo = new TriangulationDemo();
        demo.setVisible(true);
    }
}
