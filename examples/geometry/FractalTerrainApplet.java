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

import java.applet.Applet;
import java.util.HashMap;

// Application Specific imports
import org.j3d.geom.terrain.*;

import org.j3d.geom.Axis;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.UnsupportedTypeException;
import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;

/**
 * Demonstration of the Fractal terrain generator
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class FractalTerrainApplet extends DemoApplet
    implements ActionListener, ItemListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The geometry that we're going to be setting the values for */
    private Shape3D targetShape;

    /** The material that we're going to be toggling lighting */
    private Material targetMaterial;

    /** The polygon attributes that we're going to be toggling wireframe */
    private PolygonAttributes targetPolyAttr;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** The current type of geometry to generate for the primitive */
    private int geometryType;

    /** Color ramp for the terrain colours */
    private ColorRampGenerator colorGenerator;

    /** Generator for the terrain */
    private FractalTerrainGenerator terrainGenerator;

    /** Map holding polygon rendering info */
    private HashMap renderingMap;

    // bunch of text fields for the parameters
    private TextField widthTf;
    private TextField depthTf;
    private TextField heightTf;
    private TextField roughnessTf;
    private TextField iterationsTf;
    private TextField seaTf;

    /** Checkbox for Use sea level */
    private Checkbox seaCheck;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public FractalTerrainApplet()
    {
    }

    public void init()
    {
        super.init();

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

        setBackground(SystemColor.menu);

        Panel p1 = new Panel(new GridLayout(1, 4));
        p1.setBackground(SystemColor.menu);
        p1.add(new Label("Render as: "));
        p1.add(solid);
        p1.add(wireframe);
        p1.add(point);

        add(p1, BorderLayout.SOUTH);

        terrainGenerator =
            new FractalTerrainGenerator(30, 30, 10, true, 0, 4, 2, 0, null);

        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.FLY_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

        // The rendering menu
        geometryType = GeometryData.QUADS;

        createParamsPanel();
        buildScene();

        // Setup the colour generator
        float[] heights = { 0, 0.2f, 1, 3, 8 };
        Color3f[] colors = {
            new Color3f(0, 0, 1),
            new Color3f(1, 1, 0),
            new Color3f(0, 0.6f, 0),
            new Color3f(0, 1, 0),
            new Color3f(1, 1, 1),
        };

        colorGenerator = new ColorRampGenerator(heights, colors);
    }

    /**
     * Process a button request - for regeneration of the terrain
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        // Strip the various fields
        try
        {
            float width = Float.parseFloat(widthTf.getText());
            float depth = Float.parseFloat(depthTf.getText());
            float height = Float.parseFloat(heightTf.getText());
            int iterations = Integer.parseInt(iterationsTf.getText());
            float roughness = Float.parseFloat(roughnessTf.getText());
            boolean use_sea = seaCheck.getState();
            float sea_level = Float.parseFloat(seaTf.getText());

            terrainGenerator.setDimensions(width, depth);
            terrainGenerator.setGenerationFactors(height,
                                                  iterations,
                                                  roughness,
                                                  0);
            terrainGenerator.setSeaData(use_sea, sea_level);

            rebuildGeometry();
        }
        catch(NumberFormatException nfe)
        {
            System.out.println("Number formatting problem");
        }
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

        if(!on)
            return;

        Integer value = (Integer)renderingMap.get(src);
        targetPolyAttr.setPolygonMode(value.intValue());
    }

    /**
     * Rebuild the geometry based on the internal flags
     */
    private void rebuildGeometry()
    {
        GeometryData data = new GeometryData();
        data.geometryType = geometryType;
        data.geometryComponents = GeometryData.TEXTURE_2D_DATA |
                                  GeometryData.NORMAL_DATA;

        try
        {
            terrainGenerator.generate(data);
        }
        catch(UnsupportedTypeException ute)
        {
            System.out.println("Geometry type is not supported");
            return;
        }

        colorGenerator.generate(data);

        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS |
                     GeometryArray.COLOR_3;
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
                i_geom.setColorIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
                geom = i_geom;
                break;
            case GeometryData.INDEXED_TRIANGLES:

                i_geom = new IndexedTriangleArray(data.vertexCount,
                                                  format,
                                                  data.indexesCount);
                i_geom.setCoordinateIndices(0, data.indexes);
                i_geom.setColorIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
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
                i_geom.setColorIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
                geom = i_geom;
                break;

            case GeometryData.INDEXED_TRIANGLE_FANS:
                i_geom = new IndexedTriangleFanArray(data.vertexCount,
                                                     format,
                                                     data.indexesCount,
                                                     data.stripCounts);
                i_geom.setCoordinateIndices(0, data.indexes);
                i_geom.setColorIndices(0, data.indexes);
                i_geom.setNormalIndices(0, data.indexes);
                geom = i_geom;
                break;
        }

        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);
        geom.setColors(0, data.colors);

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

        targetShape = new Shape3D();
        targetShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        targetShape.setAppearance(blue_appearance);
        world_object_group.addChild(targetShape);

        // And the axis...
        Axis axis = new Axis();
        world_object_group.addChild(axis);

        // another light
        DirectionalLight sunlight = new DirectionalLight();
        sunlight.setColor(white);
        sunlight.setInfluencingBounds(light_bounds);
        sunlight.setDirection(-1, -1, 0);

        world_object_group.addChild(sunlight);

        // Add them to the locale
        world_object_group.compile();
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);

        viewHandler.setViewInfo(view, view_tg);
        viewHandler.setNavigationSpeed(3.0f);
    }

    /**
     * Convenience method to create the parameter panel.
     */
    private void createParamsPanel()
    {
        Panel main_panel = new Panel(new GridLayout(9, 1));

        Panel p1 = new Panel(new BorderLayout());
        p1.add(new Label("Parameters..."), BorderLayout.WEST);

        Panel p2 = new Panel(new BorderLayout());
        widthTf = new TextField("30", 5);
        p2.add(new Label("Width:"), BorderLayout.WEST);
        p2.add(widthTf, BorderLayout.EAST);

        Panel p3 = new Panel(new BorderLayout());
        depthTf = new TextField("30", 5);
        p3.add(new Label("Depth:"), BorderLayout.WEST);
        p3.add(depthTf, BorderLayout.EAST);

        Panel p4 = new Panel(new BorderLayout());
        heightTf = new TextField("10", 5);
        p4.add(new Label("Max Height:"), BorderLayout.WEST);
        p4.add(heightTf, BorderLayout.EAST);

        Panel p5 = new Panel(new BorderLayout());
        iterationsTf = new TextField("4", 5);
        p5.add(new Label("Iterations:"), BorderLayout.WEST);
        p5.add(iterationsTf, BorderLayout.EAST);

        Panel p6 = new Panel(new BorderLayout());
        roughnessTf = new TextField("2", 5);
        p6.add(new Label("Roughness:"), BorderLayout.WEST);
        p6.add(roughnessTf, BorderLayout.EAST);

        Panel p7 = new Panel(new BorderLayout());
        seaCheck = new Checkbox("Show sea level", true);
        p7.add(seaCheck, BorderLayout.EAST);

        Panel p8 = new Panel(new BorderLayout());
        seaTf = new TextField("0", 5);
        p8.add(new Label("Sea Height:"), BorderLayout.WEST);
        p8.add(seaTf, BorderLayout.EAST);

        Panel p9 = new Panel(new BorderLayout());
        Button go = new Button("Regenerate");
        go.addActionListener(this);
        p9.add(go);

        main_panel.add(p1);
        main_panel.add(p2);
        main_panel.add(p3);
        main_panel.add(p4);
        main_panel.add(p5);
        main_panel.add(p6);
        main_panel.add(p7);
        main_panel.add(p8);
        main_panel.add(p9);

        Panel spacer = new Panel(new BorderLayout());

        spacer.add(main_panel, BorderLayout.NORTH);
        add(spacer, BorderLayout.EAST);
    }
}
