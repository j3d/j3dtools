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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

// Application Specific imports
import org.j3d.geom.Box;

import org.j3d.loaders.HeightMapTerrainData;
import org.j3d.loaders.SimpleTiledTerrainData;
import org.j3d.loaders.vterrain.BTHeader;
import org.j3d.loaders.vterrain.BTParser;

import org.j3d.terrain.AbstractStaticTerrainData;
import org.j3d.terrain.AppearanceGenerator;
import org.j3d.terrain.Landscape;
import org.j3d.terrain.TerrainData;
import org.j3d.terrain.ViewFrustum;
import org.j3d.terrain.roam.SplitMergeLandscape;

import org.j3d.texture.TextureCache;
import org.j3d.texture.TextureCacheFactory;

import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationStateManager;
import org.j3d.ui.navigation.NavigationState;

import org.j3d.util.interpolator.ColorInterpolator;

/**
 * Demonstration of the ROAM code.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class CullingDemo extends DemoFrame
    implements ItemListener, AppearanceGenerator
{
    private static final double BACK_CLIP_DISTANCE = 3000.0;
    private static final double FRONT_CLIP_DISTANCE = 1;

    /** The main canvas that we are navigating on */
    private Canvas3D navCanvas;

    /** The canvas that provides a birds-eye view of the scene */
    private Canvas3D topDownCanvas;

    /** Global material instance to use */
    private Material material;

    /** Global polygon attributes to use */
    private PolygonAttributes polyAttr;

    private MouseViewHandler groundNav;
    private MouseViewHandler topDownNav;

    /** The view frustum for the ground canvas */
    private ViewFrustum viewFrustum;

    /** The landscape we are navigating around */
    private Landscape landscape;

    /** The branchgroup to add our terrain to */
    private BranchGroup terrainGroup;

    /** TG that holds the user view position. Used when new terrain set */
    private TransformGroup gndViewTransform;

    /** TG that holds the top-down user view position. Used when new terrain set */
    private TransformGroup topViewTransform;

    private HashMap terrainFilesMap;
    private HashMap textureFilesMap;

    /** Mapping of the button to the polygon mode value */
    private HashMap polyModeMap;

    /** The color interpolator for doing height interpolations with */
    private ColorInterpolator heightRamp;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public CullingDemo()
    {
        super("Terrain Culling Demo");

        topDownCanvas = createCanvas();
        navCanvas = createCanvas();

        Cursor curse = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        navCanvas.setCursor(curse);

        terrainFilesMap = new HashMap();
        textureFilesMap = new HashMap();

        Panel p0 = new Panel(new GridLayout(1, 2));
        p0.add(navCanvas);
        p0.add(topDownCanvas);

        add(p0, BorderLayout.CENTER);

        JPanel p1 = new JPanel(new FlowLayout());

        ButtonGroup grp = new ButtonGroup();
        JRadioButton button = new JRadioButton("Crater Lake");
        button.addItemListener(this);
        grp.add(button);
        p1.add(button);

        terrainFilesMap.put(button, "crater_0513.bt");
        textureFilesMap.put(button, null);

        button = new JRadioButton("Ratcliff Alien");
        button.addItemListener(this);
        grp.add(button);
        p1.add(button);

        terrainFilesMap.put(button, "ratcliff_1k.bt");
        textureFilesMap.put(button, "ratcliff_crater_2048.jpg");
//        textureFilesMap.put(button, null);

        add(p1, BorderLayout.SOUTH);

        // Panel for the polygon mode style
        polyModeMap = new HashMap();

        JPanel p2 = new JPanel(new GridLayout(4, 1));

        p2.add(new JLabel("Render As..."));

        grp = new ButtonGroup();
        button = new JRadioButton("Polygons", true);
        button.addItemListener(this);
        grp.add(button);
        p2.add(button);
        polyModeMap.put(button, new Integer(PolygonAttributes.POLYGON_FILL));


        button = new JRadioButton("Lines");
        button.addItemListener(this);
        grp.add(button);
        p2.add(button);
        polyModeMap.put(button, new Integer(PolygonAttributes.POLYGON_LINE));


        button = new JRadioButton("Points");
        button.addItemListener(this);
        grp.add(button);
        p2.add(button);
        polyModeMap.put(button, new Integer(PolygonAttributes.POLYGON_POINT));


        JPanel p3 = new JPanel(new BorderLayout());
        p3.add(p2, BorderLayout.NORTH);

        add(p3, BorderLayout.EAST);

        groundNav = new MouseViewHandler();
        groundNav.setCanvas(navCanvas);
        groundNav.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                      NavigationState.FLY_STATE);
        groundNav.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                      NavigationState.TILT_STATE);
        groundNav.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                      NavigationState.PAN_STATE);


        NavigationStateManager gnd_nav_mgr =
            new NavigationStateManager(navCanvas);
        gnd_nav_mgr.setMouseHandler(groundNav);

        topDownNav = new MouseViewHandler();
        topDownNav.setCanvas(topDownCanvas);
        topDownNav.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                       NavigationState.PAN_STATE);

        NavigationStateManager top_nav_mgr =
            new NavigationStateManager(topDownCanvas);
        top_nav_mgr.setMouseHandler(topDownNav);

        buildScene();

        viewFrustum = new ViewFrustum(navCanvas);

        // Now set up the material and appearance handling for the generator
        material = new Material();
        material.setLightingEnable(true);

        polyAttr = new PolygonAttributes();
        polyAttr.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        polyAttr.setCullFace(PolygonAttributes.CULL_NONE);
        polyAttr.setBackFaceNormalFlip(true);

        heightRamp = new ColorInterpolator(ColorInterpolator.HSV_SPACE);
        heightRamp.addRGBKeyFrame(-20,  0,    0,    1,     0);
        heightRamp.addRGBKeyFrame(0,    0,    0.7f, 0.95f, 0);
        heightRamp.addRGBKeyFrame(5,    1,    1,    0,     0);
        heightRamp.addRGBKeyFrame(10,   0,    0.6f, 0,     0);
        heightRamp.addRGBKeyFrame(100,  0,    1,    0,     0);
        heightRamp.addRGBKeyFrame(1000, 0.6f, 0.7f, 0,     0);
        heightRamp.addRGBKeyFrame(1500, 0.5f, 0.5f, 0.3f,  0);
        heightRamp.addRGBKeyFrame(2500, 1,    1,    1,     0);
    }

    //----------------------------------------------------------
    // Methods required by ItemListener
    //----------------------------------------------------------

    /**
     * Process the change of state request from the colour selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        if(evt.getStateChange() != ItemEvent.SELECTED)
            return;

        Object src = evt.getSource();

        if(textureFilesMap.containsKey(src))
        {
            // map change request
            String terrain = (String)terrainFilesMap.get(src);
            String texture = (String)textureFilesMap.get(src);

            loadTerrain(terrain, texture);
        }
        else
        {
            Integer mode_int = (Integer)polyModeMap.get(src);
            polyAttr.setPolygonMode(mode_int.intValue());
        }
    }

    //----------------------------------------------------------
    // Methods required by AppearanceGenerator
    //----------------------------------------------------------

    /**
     * Create a new appearance instance. We set them all up with different
     * appearance instances, but share the material information.
     *
     * @return The new appearance instance to use
     */
    public Appearance createAppearance()
    {
        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setPolygonAttributes(polyAttr);

        return app;
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

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

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        Point3d origin = new Point3d(0, 0, 0);
        BoundingSphere light_bounds =
            new BoundingSphere(origin, BACK_CLIP_DISTANCE);
        DirectionalLight headlight = new DirectionalLight();
        headlight.setColor(white);
        headlight.setInfluencingBounds(light_bounds);
        headlight.setEnable(true);

        //
        // View group for the ground navigation system that the
        // roam code will apply to.
        //

        ViewPlatform gnd_camera = new ViewPlatform();

        Transform3D angle = new Transform3D();

        gndViewTransform = new TransformGroup();
        gndViewTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        gndViewTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        gndViewTransform.addChild(gnd_camera);
        gndViewTransform.addChild(headlight);
//        gndViewTransform.addChild(new Box(10, 10, 10));

        View gnd_view = new View();
        gnd_view.setBackClipDistance(BACK_CLIP_DISTANCE);
        gnd_view.setFrontClipDistance(FRONT_CLIP_DISTANCE);
        gnd_view.setPhysicalBody(body);
        gnd_view.setPhysicalEnvironment(env);
        gnd_view.addCanvas3D(navCanvas);
        gnd_view.attachViewPlatform(gnd_camera);

        groundNav.setViewInfo(gnd_view, gndViewTransform);
        groundNav.setNavigationSpeed(50.0f);

        view_group.addChild(gndViewTransform);
        view_group.addChild(groundNav.getTimerBehavior());

        //
        // View transform group for the system that looks in a top-down view
        // of the entire scene graph.
        //

        ViewPlatform god_camera = new ViewPlatform();
        god_camera.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);

        angle = new Transform3D();
        angle.setTranslation(new Vector3d(0, 0, 50));

        topViewTransform = new TransformGroup(angle);
        topViewTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        topViewTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        topViewTransform.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);

        topViewTransform.addChild(god_camera);
//        topViewTransform.addChild(headlight.cloneNode(false));

        angle = new Transform3D();
        angle.rotX(-Math.PI / 2);

        TransformGroup god_view_tg = new TransformGroup(angle);
        god_view_tg.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        god_view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        god_view_tg.addChild(topViewTransform);

        View god_view = new View();
        god_view.setBackClipDistance(BACK_CLIP_DISTANCE);
        god_view.setFrontClipDistance(FRONT_CLIP_DISTANCE);
        god_view.setPhysicalBody(body);
        god_view.setPhysicalEnvironment(env);
        god_view.addCanvas3D(topDownCanvas);
        god_view.attachViewPlatform(god_camera);

        topDownNav.setViewInfo(god_view, topViewTransform);
        topDownNav.setNavigationSpeed(500);

        view_group.addChild(god_view_tg);
        view_group.addChild(topDownNav.getTimerBehavior());

        // Just an axis for reference
//        world_object_group.addChild(new Axis());

        // Create a new branchgroup that is for the geometry. Initially starts
        // with a null child at position zero so that we only need to write the
        // child and not extend. One less capability to set is good.
        terrainGroup = new BranchGroup();
        terrainGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        terrainGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
//        terrainGroup.addChild(null);

        world_object_group.addChild(terrainGroup);

        Material mat = new Material(ambientBlue, ambientBlue, blue, specular, 0);
        Appearance app = new Appearance();
        app.setMaterial(mat);
        Box box = new Box(50, 50, 1000, app);


        angle.set(new Vector3f(0, 0, -500));
        TransformGroup tg = new TransformGroup(angle);
        tg.addChild(box);

        gndViewTransform.addChild(tg);

        // Add everything to the locale
        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }

    /**
     * Load the terrain and get it read to roll. If the texture file is not
     * specified then no texture will be loaded and colour information is
     * used instead.
     *
     * @param filename The name of the terrain file
     * @param textureName The name of the texture file, or null
     */
    private void loadTerrain(String filename, String textureName)
    {
        BTParser ldr = new BTParser();
        File bt_file = new File(filename);

        View v = navCanvas.getView();
        v.stopView();

        try
        {
            if(landscape != null)
            {
                landscape.setAppearanceGenerator(null);
                landscape.detach();
                landscape = null;
            }

            System.gc();

            System.out.println("Loading terrain file. Please wait");

            ldr.reset(new FileInputStream(bt_file));
            ldr.parse();

            TerrainData terrain = null;

            BTHeader header = ldr.getHeader();

//            if(header.rows > 513)
//            {
                SimpleTiledTerrainData t = new SimpleTiledTerrainData(ldr);
                t.setColorInterpolator(heightRamp);
                terrain = t;
//            }
//           else
//            {
//                HeightMapTerrainData t = new HeightMapTerrainData(ldr);
//                t.setColorInterpolator(heightRamp);
//                terrain = t;
//            }

            System.out.println("Terrain loading complete");

            if(textureName != null)
            {
                System.out.println("Loading texture file");

                TextureCache t_cache = TextureCacheFactory.getCache();
                Texture texture = t_cache.fetchTexture(textureName);

                if((texture != null) &&
                   (terrain instanceof AbstractStaticTerrainData))
                {
                    ((AbstractStaticTerrainData)terrain).setTexture(texture);
                }

                System.out.println("Finished texture");
            }

            System.out.println("Building landscape");

            landscape = new SplitMergeLandscape(viewFrustum, terrain);
            landscape.setCapability(BranchGroup.ALLOW_DETACH);
            landscape.setAppearanceGenerator(this);

            float[] origin = new float[3];
            terrain.getCoordinate(origin, 1, 1);

            Transform3D angle = new Transform3D();

            // setup the top view by just raising it some amount and we want
            Vector3f pos = new Vector3f();
            pos.z += 5000;
            pos.x = origin[0];
            pos.y = origin[2];
            angle.setTranslation(pos);

            topViewTransform.setTransform(angle);

            // the initial view to be some way off the ground too and rotate at
            // 45 deg to look into the "middle" of the terrain.
            terrain.getCoordinate(origin, 0, 0);
            pos.set(origin);
            pos.y += 100;
            pos.x -= 100;
            pos.z -= 100;
            angle.rotY(Math.PI * -0.25); // 45 deg looking into the terrain
            angle.setTranslation(pos);

            gndViewTransform.setTransform(angle);


            // Force a single render so that the view transform is updated
            // and the projection matrix is correct for the view frustum.
            v.renderOnce();

            viewFrustum.viewingPlatformMoved();

            Matrix3f mtx = new Matrix3f();
            Vector3f orient = new Vector3f(0, 0, -1);

            angle.get(mtx, pos);
            mtx.transform(orient);

            landscape.initialize(pos, orient);

            groundNav.setFrameUpdateListener(landscape);

            terrainGroup.removeAllChildren();
            terrainGroup.addChild(landscape);

            // Set the nav speed to be one grid square per second
            groundNav.setNavigationSpeed((float)terrain.getGridXStep());


            v.startView();

            System.out.println("Ready for rendering");
        }
        catch(IOException ioe)
        {
            System.out.println("I/O Error " + ioe);
            ioe.printStackTrace();
        }
    }

    public static void main(String[] argv)
    {
        CullingDemo demo = new CullingDemo();
        demo.setSize(600, 400);
        demo.setVisible(true);
    }
}
