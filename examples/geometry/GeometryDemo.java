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
import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.io.IOException;
import java.util.HashMap;

// Local imports
import org.j3d.geom.*;

import org.j3d.renderer.java3d.geom.Axis;
import org.j3d.renderer.java3d.navigation.MouseViewHandler;
import org.j3d.texture.TextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;
import org.j3d.ui.navigation.NavigationState;

/**
 * Demonstration of the various pieces of standard geometry provided by
 * the org.j3d.geom package.
 * <p>
 * The objects are rendered on screen to show that all the values are correctly
 * generated. There is no capability to navigate around them or to change any
 * of the rendering attributes like the face set.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class GeometryDemo extends DemoFrame
    implements ActionListener, ItemListener
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

    /** Map of the geometry type (quad, tris etc) to the data type */
    private HashMap polygonMap;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** Menu item to select lighting or not rendering */
    private CheckboxMenuItem lightingMenuItem;

    /** Menu item to select front or back face culling */
    private CheckboxMenuItem cullingMenuItem;

    /** Menu item to select textured rendering or no */
    private CheckboxMenuItem textureMenuItem;

    /** Menu item that is the current geometry generation type */
    private CheckboxMenuItem currentPolygonItem;

    /** The menu holding polygon items */
    private Menu renderingMenu;

    /** The menu holding geometry polygon types items */
    private Menu polygonMenu;

    /** Flag indicating if we want real wireframe or just no faces */
    private boolean useTexture;

    /** The last generator selected */
    private GeometryGenerator currentGenerator;

    /** The current type of geometry to generate for the primitive */
    private int geometryType;

    /** Texture object for the current appearance */
    private Texture2D texture;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public GeometryDemo()
    {
        super("Geometry Demo test window");

        polygonMap = new HashMap();
        renderingMap = new HashMap();

        MenuBar menu_bar = new MenuBar();
        Menu geom_menu = new Menu("Geometry");
        renderingMenu = new Menu("Rendering Options");
        polygonMenu = new Menu("Polygon types");

        menu_bar.add(geom_menu);
        menu_bar.add(renderingMenu);
        menu_bar.add(polygonMenu);

        MenuItem box_menu = new MenuItem("Box");
        MenuItem cone_menu = new MenuItem("Cone");
        MenuItem sphere_menu = new MenuItem("Sphere");
        MenuItem cylinder_menu = new MenuItem("Cylinder");
        MenuItem torus_menu = new MenuItem("Torus");
        MenuItem spring_menu = new MenuItem("Spring");
        MenuItem mobius_menu = new MenuItem("Mobius");
        mobius_menu.setEnabled(false);

        box_menu.addActionListener(this);
        cone_menu.addActionListener(this);
        sphere_menu.addActionListener(this);
        cylinder_menu.addActionListener(this);
        torus_menu.addActionListener(this);
        spring_menu.addActionListener(this);
        mobius_menu.addActionListener(this);

        geom_menu.add(box_menu);
        geom_menu.add(cone_menu);
        geom_menu.add(sphere_menu);
        geom_menu.add(cylinder_menu);
        geom_menu.add(torus_menu);
        geom_menu.add(spring_menu);
        geom_menu.add(mobius_menu);

        textureMenuItem = new CheckboxMenuItem("Use texture");
        lightingMenuItem = new CheckboxMenuItem("Use Lighting");

        textureMenuItem.setState(false);
        lightingMenuItem.setState(true);

        textureMenuItem.addItemListener(this);
        lightingMenuItem.addItemListener(this);

        renderingMenu.add(textureMenuItem);
        renderingMenu.add(lightingMenuItem);

        CheckboxMenuItem triangles = new CheckboxMenuItem("Triangles");
        CheckboxMenuItem quads = new CheckboxMenuItem("Quads");
        CheckboxMenuItem i_quads = new CheckboxMenuItem("Indexed Quads");
        CheckboxMenuItem i_tris = new CheckboxMenuItem("Indexed Triangles");
        CheckboxMenuItem is_tris = new CheckboxMenuItem("Indexed Triangle Strips");
        CheckboxMenuItem if_tris = new CheckboxMenuItem("Indexed Fan Triangles");
        CheckboxMenuItem f_tris = new CheckboxMenuItem("Triangle Fans");
        CheckboxMenuItem s_tris = new CheckboxMenuItem("Triangle Strips");

        triangles.setState(false);
        quads.setState(true);
        i_quads.setState(false);
        i_tris.setState(false);
        is_tris.setState(false);
        if_tris.setState(false);
        f_tris.setState(false);
        s_tris.setState(false);

        triangles.addItemListener(this);
        quads.addItemListener(this);
        i_quads.addItemListener(this);
        i_tris.addItemListener(this);
        is_tris.addItemListener(this);
        if_tris.addItemListener(this);
        f_tris.addItemListener(this);
        s_tris.addItemListener(this);

        polygonMenu.add(triangles);
        polygonMenu.add(quads);
        polygonMenu.add(i_quads);
        polygonMenu.add(i_tris);
        polygonMenu.add(is_tris);
        polygonMenu.add(if_tris);
        polygonMenu.add(f_tris);
        polygonMenu.add(s_tris);

        polygonMap.put(triangles, new Integer(GeometryData.TRIANGLES));
        polygonMap.put(quads, new Integer(GeometryData.QUADS));
        polygonMap.put(i_quads, new Integer(GeometryData.INDEXED_QUADS));
        polygonMap.put(i_tris, new Integer(GeometryData.INDEXED_TRIANGLES));
        polygonMap.put(is_tris,
                       new Integer(GeometryData.INDEXED_TRIANGLE_STRIPS));
        polygonMap.put(if_tris,
                       new Integer(GeometryData.INDEXED_TRIANGLE_FANS));
        polygonMap.put(f_tris, new Integer(GeometryData.TRIANGLE_FANS));
        polygonMap.put(s_tris, new Integer(GeometryData.TRIANGLE_STRIPS));

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

        generatorMap = new HashMap();
        generatorMap.put(box_menu, new BoxGenerator());
        generatorMap.put(cone_menu, new ConeGenerator());
        generatorMap.put(sphere_menu, new SphereGenerator());
        generatorMap.put(cylinder_menu, new CylinderGenerator());
        generatorMap.put(torus_menu, new TorusGenerator());
        generatorMap.put(spring_menu, new SpringGenerator());
//        generatorMap.put(mobius_menu, new MobiusGenerator());

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

        geometryType = GeometryData.QUADS;
        currentPolygonItem = quads;

        buildScene();
    }

    /**
     * Process the geometry request command.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        Object src = evt.getSource();

        currentGenerator = (GeometryGenerator)generatorMap.get(src);
/*
        if(currentGenerator instanceof MobiusGenerator)
        {
            targetPolyAttr.setCullFace(PolygonAttributes.CULL_NONE);
            targetPolyAttr.setBackFaceNormalFlip(true);
        }
        else
        {
            targetPolyAttr.setCullFace(PolygonAttributes.CULL_BACK);
            targetPolyAttr.setBackFaceNormalFlip(false);
        }
*/
        rebuildGeometry();
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
                rebuildGeometry();
            }
        }
        else if(parent == polygonMenu)
        {
            if(!on)
                return;

            value = (Integer)polygonMap.get(src);
            geometryType = value.intValue();
            currentPolygonItem.setState(false);
            currentPolygonItem = ((CheckboxMenuItem)src);
            rebuildGeometry();
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
     * Rebuild the geometry based on the internal flags
     */
    private void rebuildGeometry()
    {
        if(currentGenerator == null)
            return;

        GeometryData data = new GeometryData();
        data.geometryType = geometryType;
        data.geometryComponents = GeometryData.TEXTURE_2D_DATA |
                                  GeometryData.NORMAL_DATA;

        try
        {
            currentGenerator.generate(data);
        }
        catch(UnsupportedTypeException ute)
        {
            System.out.println("Geometry type is not supported");
            return;
        }

        int format = GeometryArray.COORDINATES | GeometryArray.NORMALS;

        if(useTexture)
            format |= GeometryArray.TEXTURE_COORDINATE_2;

        GeometryArray geom = null;
        IndexedGeometryArray i_geom;

        switch(geometryType)
        {
            case GeometryData.TRIANGLES:
                geom = new TriangleArray(data.vertexCount, format);
                break;

            case GeometryData.QUADS:
                geom = new QuadArray(data.vertexCount, format);
                break;

            case GeometryData.INDEXED_QUADS:

                i_geom = new IndexedQuadArray(data.vertexCount,
                                              format,
                                              data.indexesCount);
                i_geom.setCoordinateIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
                if(useTexture)
                    i_geom.setTextureCoordinateIndices(0, 0, data.indexes);

                geom = i_geom;
                break;

            case GeometryData.INDEXED_TRIANGLES:

                i_geom = new IndexedTriangleArray(data.vertexCount,
                                                  format,
                                                  data.indexesCount);
                i_geom.setCoordinateIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
                if(useTexture)
                    i_geom.setTextureCoordinateIndices(0, 0, data.indexes);

                geom = i_geom;
                break;

            case GeometryData.TRIANGLE_STRIPS:
                geom = new TriangleStripArray(data.vertexCount,
                                              format,
                                              data.stripCounts);
                break;

            case GeometryData.TRIANGLE_FANS:
                geom = new TriangleFanArray(data.vertexCount,
                                            format,
                                            data.stripCounts);
                break;

            case GeometryData.INDEXED_TRIANGLE_STRIPS:
                i_geom = new IndexedTriangleStripArray(data.vertexCount,
                                                       format,
                                                       data.indexesCount,
                                                       data.stripCounts);
                i_geom.setCoordinateIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
                if(useTexture)
                    i_geom.setTextureCoordinateIndices(0, 0, data.indexes);

                geom = i_geom;
                break;

            case GeometryData.INDEXED_TRIANGLE_FANS:
                i_geom = new IndexedTriangleFanArray(data.vertexCount,
                                                       format,
                                                       data.indexesCount,
                                                       data.stripCounts);
                i_geom.setCoordinateIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);

                if(useTexture)
                    i_geom.setTextureCoordinateIndices(0, 0, data.indexes);

                geom = i_geom;
                break;
        }

        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);

        if(useTexture)
            geom.setTextureCoordinates(0, 0, data.textureCoordinates);

        texture.setEnable(useTexture);

        targetShape.setGeometry(geom);
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

        // And the axis...
        Axis axis = new Axis();
        world_object_group.addChild(axis);
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

    public static void main(String[] argv)
    {
        GeometryDemo demo = new GeometryDemo();
        demo.setVisible(true);
    }
}
