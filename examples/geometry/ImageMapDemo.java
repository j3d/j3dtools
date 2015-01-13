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

import java.awt.image.BufferedImage;

// Local imports
import org.j3d.geom.terrain.*;

import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.UnsupportedTypeException;
import org.j3d.ui.navigation.NavigationState;

import org.j3d.renderer.java3d.geom.Axis;
import org.j3d.renderer.java3d.navigation.MouseViewHandler;

/**
 * Demonstration of the height image map converter that generates
 * random terrrains, into a texture and then shows the output in 3D.
 * <p>
 *
 * This shows (and tests!) how you can generate a 3D terrain model,
 * then create an image from that, then turn the image into a terrain
 * model again. The screen presents these three steps from left to right.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ImageMapDemo extends DemoFrame
    implements ActionListener
{
    private static final float MAX_HEIGHT = 10f;
    private static final double BACK_CLIP_DISTANCE = 100.0;

    private static final byte[] BLACK_BYTES =
        { (byte)0, (byte)0, (byte)0, (byte)255 };
    private static final byte[] WHITE_BYTES =
        { (byte)255, (byte)255, (byte)255, (byte)255 };

    /** Color ramp for the terrain colours */
    private ColorRampGenerator colorGenerator;

    /** Generator for the terrain */
    private FractalTerrainGenerator terrainGenerator;

    /** Builder of images */
    private HeightImageCreator imageConverter;

    /** Elevation grid creator based on the image */
    private ElevationGridGenerator gridGenerator;

    /** Builder of heights from images */
    private HeightDataCreator heightConverter;

    /** Shape3D to put the before view in */
    private Shape3D beforeShape;

    /** Shape3D to put the after view in */
    private Shape3D afterShape;

    // bunch of text fields for the parameters
    private TextField widthTf;
    private TextField depthTf;
    private TextField heightTf;
    private TextField roughnessTf;
    private TextField iterationsTf;
    private TextField seaTf;

    /** Checkbox for Use sea level */
    private Checkbox seaCheck;
    private Checkbox colorCheck;
    private ScrollPane scroller;

    private ColorPanel minColor;
    private ColorPanel maxColor;

    /** The image we are playing with */
    private ImageCanvas imageCanvas;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public ImageMapDemo()
    {
        super("Height Map Converter");

        setBackground(SystemColor.menu);

        terrainGenerator =
            new FractalTerrainGenerator(30, 30,
                                        MAX_HEIGHT,
                                        true,
                                        0, 4, 2, 0,
                                        null);

        imageConverter = new HeightImageCreator();
        heightConverter = new HeightDataCreator(-MAX_HEIGHT, MAX_HEIGHT);

        gridGenerator = new ElevationGridGenerator();

        createParamsPanel();

        imageCanvas = new ImageCanvas();
        scroller = new ScrollPane();
        scroller.add(imageCanvas);

        Panel view_panel = new Panel(new GridLayout(1, 3));

        view_panel.add(buildScene(true));
        view_panel.add(scroller);
        view_panel.add(buildScene(false));

        add(view_panel, BorderLayout.CENTER);

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

        // Make it bloody big to fit everything on!
        setSize(1024, 600);
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

            Color4b min_col = minColor.getColor();
            Color4b max_col = maxColor.getColor();

            imageConverter.setColorRange(min_col, max_col);

            terrainGenerator.setDimensions(width, depth);
            terrainGenerator.setGenerationFactors(height,
                                                  iterations,
                                                  roughness,
                                                  0);
            terrainGenerator.setSeaData(use_sea, sea_level);

            rebuildImage();
        }
        catch(NumberFormatException nfe)
        {
            System.out.println("Number formatting problem");
        }
    }

    /**
     * Create a new image based on the latest generation
     */
    private void rebuildImage()
    {
        float[][] terrain = terrainGenerator.generate();

        // firstly, create the geometry for it to go in the before view.
        gridGenerator.setTerrainDetail(terrain, 0);
        gridGenerator.setDimensions(30, 30, terrain.length, terrain[0].length);

        createGrid(true);

        BufferedImage image;

        if(colorCheck.getState())
            image = imageConverter.createColorImage(terrain);
        else
            image = imageConverter.createGreyScaleImage(terrain);

        imageCanvas.setImage(image);
        scroller.doLayout();
        imageCanvas.repaint();

        terrain = heightConverter.createHeightField(image);

        gridGenerator.setTerrainDetail(terrain, 0);
        gridGenerator.setDimensions(30, 30, terrain.length, terrain[0].length);

        createGrid(false);
    }

    /**
     * Create a grid from the grid generator and place it in the nominated
     * target shape.
     *
     * @param before true to use the before shape, false for after
     */
    private void createGrid(boolean before)
    {
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
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
        geom = new QuadArray(data.vertexCount, format);
        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);
        geom.setColors(0, data.colors);

        if(before)
            beforeShape.setGeometry(geom);
        else
            afterShape.setGeometry(geom);
    }

    /**
     * Convenience method to create the parameter panel.
     */
    private void createParamsPanel()
    {
        Panel main_panel = new Panel(new GridLayout(14, 1));

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
        p7.add(seaCheck, BorderLayout.WEST);

        Panel p8 = new Panel(new BorderLayout());
        seaTf = new TextField("0", 5);
        p8.add(new Label("Sea Height:"), BorderLayout.WEST);
        p8.add(seaTf, BorderLayout.EAST);

        Panel p9 = new Panel(new BorderLayout());
        Button go = new Button("Regenerate");
        go.addActionListener(this);
        p9.add(go);

        Panel p10 = new Panel(new BorderLayout());
        colorCheck = new Checkbox("Use Colors", true);
        p10.add(colorCheck, BorderLayout.WEST);

        Label l1 = new Label("Min (0-255)   [a,r,g,b]");
        Label l2 = new Label("Max (0-255)   [a,r,g,b]");
        minColor = new ColorPanel(new Color4b(BLACK_BYTES));
        maxColor = new ColorPanel(new Color4b(WHITE_BYTES));

        main_panel.add(p1);
        main_panel.add(p2);
        main_panel.add(p3);
        main_panel.add(p4);
        main_panel.add(p5);
        main_panel.add(p6);
        main_panel.add(p7);
        main_panel.add(p8);
        main_panel.add(p10);
        main_panel.add(l1);
        main_panel.add(minColor);
        main_panel.add(l2);
        main_panel.add(maxColor);
        main_panel.add(p9);

        Panel spacer = new Panel(new BorderLayout());

        spacer.add(main_panel, BorderLayout.NORTH);
        add(spacer, BorderLayout.EAST);
    }

    /**
     * Build a scenegraph for a canvas.
     *
     * @param before true if this is the before view, false for after.
     * @return The universe that has been built
     */
    private Canvas3D buildScene(boolean before)
    {
        Canvas3D ret_val = createCanvas();

        MouseViewHandler viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(ret_val);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.FLY_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

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
        Material targetMaterial = new Material();
        targetMaterial.setAmbientColor(ambientBlue);
        targetMaterial.setDiffuseColor(blue);
        targetMaterial.setSpecularColor(specular);
        targetMaterial.setShininess(75.0f);
        targetMaterial.setLightingEnable(true);
        targetMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);

        Appearance blue_appearance = new Appearance();
        blue_appearance.setMaterial(targetMaterial);

        PolygonAttributes targetPolyAttr = new PolygonAttributes();
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
        targetPolyAttr.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
        targetPolyAttr.setPolygonMode(targetPolyAttr.POLYGON_FILL);
        blue_appearance.setPolygonAttributes(targetPolyAttr);


        Shape3D targetShape = new Shape3D();
        targetShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        targetShape.setAppearance(blue_appearance);
        world_object_group.addChild(targetShape);

        // And the axis...
        Axis axis = new Axis();
        world_object_group.addChild(axis);
        world_object_group.compile();

        // Add them to the locale
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(ret_val);
        view.attachViewPlatform(camera);

        viewHandler.setViewInfo(view, view_tg);
        viewHandler.setNavigationSpeed(3.0f);

        if(before)
            beforeShape = targetShape;
        else
            afterShape = targetShape;

        return ret_val;
    }

    public static void main(String[] argv)
    {
        ImageMapDemo demo = new ImageMapDemo();
        demo.setVisible(true);
    }
}
