/*****************************************************************************
 *                        Copyright (c) 2002 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This source is based on the SUN HelloUniverse example.
 ****************************************************************************/

import java.util.Hashtable;
import java.util.Map;

import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;

// imports for the ParticleSystem
import org.j3d.geom.particle.*;

import org.j3d.geom.Box;

import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.MouseViewHandler;


/**
 * ParticleTest shows how you can easily add complex particle-
 * based animation to your Java 3D applications. It adds a
 * smoke particle system, where each particle is acted on
 * by wind, gravity and is constrained within a BoundingBox.
 * <p>
 * Particles are texture mapped and represented using a single
 * TriangleArray in BY_REFERENCE mode.
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public class ParticleTest extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    public ParticleTest()
    {
        super("Particle Test");
        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);

        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.WALK_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);
    }

    public void init()
    {
        Color3f white = new Color3f(1, 1, 1);

        VirtualUniverse universe = new VirtualUniverse();
        Locale locale = new Locale(universe);

        BranchGroup view_group = new BranchGroup();
        BranchGroup world_object_group = new BranchGroup();

        ViewPlatform camera = new ViewPlatform();

        Transform3D camera_location = new Transform3D();
        camera_location.setTranslation(new Vector3d(0, 0, 6));

        TransformGroup view_tg = new TransformGroup(camera_location);
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        view_tg.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        view_tg.addChild(camera);
        view_group.addChild(view_tg);

/*
        Point3d origin = new Point3d(0, 0, 0);
        BoundingSphere light_bounds =
            new BoundingSphere(origin, BACK_CLIP_DISTANCE);
        DirectionalLight headlight = new DirectionalLight();
        headlight.setColor(white);
        headlight.setInfluencingBounds(light_bounds);
        view_tg.addChild(headlight);
*/

        Group room = createRoom();
        BranchGroup main_scene = createSceneGraph();

//        world_object_group.addChild(room);
        world_object_group.addChild(main_scene);
        world_object_group.compile();

        // Add them to the locale

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);
        view.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);

        viewHandler.setViewInfo(view, view_tg);
//        viewHandler.setCenterOfRotation();

        viewHandler.setNavigationSpeed(1);
        view_group.addChild(viewHandler.getTimerBehavior());

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }

    public BranchGroup createSceneGraph()
    {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create the TransformGroup node and initialize it to the
        // identity. Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at run time. Add it to
        // the root of the subgraph.
        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
        objRoot.addChild( objTrans );

        // *********************************************************************
        //     begin particle code
        // *********************************************************************

        // this will do basic color interpolations over time
        float[] time = { 0, 1.5f, 2.5f, 5 };
        float[] colors = {
           1, 1, 1, 0,
           1, 0, 0, 0,
           1, 1, 0, 0,
           1, 1, 0, 1
        };

        int particleCount = 10000;
        float[] position = {0, -0.5f, -20};
        float[] direction = {0, 0, 0};

        // create the ParticleInitializer for the ParticleSystem
        // the initializer is used to control how long particles within
        // the system live and how they are reinitialized when they die.
        // this simple initializer lets particles live for 200 iterations
        // and moves them to point 0,0,0 when they die
        float[] line = { -1, 0, 0,
                         -0.5f, -0.5f, 0,
                         0, 0, 0,
                         0.5f, -0.5f, 0,
                         1, 0, 0  };

        float[] velocity = { 0, -0.1f, 0 };

        ParticleInitializer emitter =
/**
           new MaxTimePointEmitter(5000,
                                   particleCount,
                                   position,
                                   colors,
                                   direction,
                                   0,
                                   0.25f);
*/
/*
           new ExplosionPointEmitter(10000,
                                     particleCount,
                                     position,
                                     colors,
                                     1,
                                     0.25f);

*/
            new PolylineEmitter(10000,
                                particleCount,
                                line,
                                line.length / 3,
                                colors,
                                velocity,
                                0.25f);



        // create a "smoke" particle system with 1000 particles
        // the properties for each particle are loaded from a
        // property file ("smoke" is used as a key)
        ParticleSystem smoke_system =
           new PointArrayByRefParticleSystem("smoke", particleCount);
//           new LineArrayByRefParticleSystem("smoke", particleCount, false);
//           new TriangleArrayByRefParticleSystem("smoke", particleCount, false);
//           new TriangleFanByRefParticleSystem("smoke", particleCount, false);
//           new QuadArrayByRefParticleSystem("smoke", particleCount, false);
        smoke_system.setParticleInitializer(emitter);

        // create the ParticleSystemManager for the ParticleSystem
        // this instance uses a behavior and will trigger the Particle System
        // every second frame
        ParticleSystemManager particleSystemManager  =
           new ParticleSystemManager(new WakeupOnElapsedFrames(1));

        // set the bounds on the ParticleSystemManager so it will be scheduled
        BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 500);
        particleSystemManager.setSchedulingBounds(bounds);

        // add the smoke_system to its manager
        particleSystemManager.addParticleSystem(smoke_system);

        // Remove the ones that are too old now, as the first step.
        smoke_system.addParticleFunction(new MaxTimeParticleFunction());

        // this will apply an upward wind force to the particles
//        smoke_system.addParticleFunction(
//           new WindParticleFunction(new Vector3d(0, 0.003, 0)));

        // this will apply a gravity force to the particles
//        smoke_system.addParticleFunction(new GravityParticleFunction());

        // this will do basic F=m.a position updates on the particles
        smoke_system.addParticleFunction(new PhysicsFunction());

        ColorRampFunction colorRamp = new ColorRampFunction(time, colors, true);
        smoke_system.addParticleFunction(colorRamp);

        // this will clamp the position of the particles to within a box
        double size = 1.8;
        Point3d b_min = new Point3d(-size,-size,-size);
        Point3d b_max = new Point3d(size, size, size);
        BoundingBox particle_bounds = new BoundingBox(b_min, b_max);

        smoke_system.addParticleFunction(
            new BoundingBoxParticleFunction(particle_bounds));

        smoke_system.initialize();

        // add the particleSystemManager (Behavior) to the scenegraph
        objTrans.addChild( particleSystemManager );

        // add the geometry for the ParticleSystem to the scenegraph
        objTrans.addChild( smoke_system.getNode() );

        // *********************************************************************
        //     end particle code
        // *********************************************************************

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile( );

        return objRoot;
    }

    /**
     * Convenience method to create a room made from boxes
     */
    private Group createRoom()
    {
        Color3f specular = new Color3f(0.7f, 0.7f, 0.7f);
        Color3f blue = new Color3f(0, 0.2f, 0.8f);
        Color3f red = new Color3f(0.8f, 0, 0.2f);
        Color3f cyan = new Color3f(0, 0.8f, 0.8f);
        Color3f yellow = new Color3f(0.8f, 0.8f, 0);
        Color3f green = new Color3f(0, 0.8f, 0.2f);

        // Build a "room" from 5 boxes to put the particles in
        Material blue_mat = new Material();
        blue_mat.setEmissiveColor(blue);
        blue_mat.setDiffuseColor(blue);
        blue_mat.setSpecularColor(specular);
        blue_mat.setLightingEnable(true);
        Appearance blue_app = new Appearance();
        blue_app.setMaterial(blue_mat);

        Shape3D left_wall = new Box(0.1f, 4, 4, blue_app);

        Transform3D tg = new Transform3D();
        tg.setTranslation(new Vector3d(-2, 0, 0));

        TransformGroup left_tx = new TransformGroup(tg);
        left_tx.addChild(left_wall);

        Material cyan_mat = new Material();
        cyan_mat.setEmissiveColor(cyan);
        cyan_mat.setDiffuseColor(cyan);
        cyan_mat.setSpecularColor(specular);
        cyan_mat.setLightingEnable(true);
        Appearance cyan_app = new Appearance();
        cyan_app.setMaterial(cyan_mat);

        Shape3D right_wall = new Box(0.1f, 4, 4, cyan_app);

        tg.setTranslation(new Vector3d(2, 0, 0));

        TransformGroup right_tx = new TransformGroup(tg);
        right_tx.addChild(right_wall);

        Material green_mat = new Material();
        green_mat.setEmissiveColor(green);
        green_mat.setDiffuseColor(green);
        green_mat.setSpecularColor(specular);
        green_mat.setLightingEnable(true);
        Appearance green_app = new Appearance();
        green_app.setMaterial(green_mat);

        Shape3D rear_wall = new Box(4, 4, 0.1f, green_app);

        tg.setTranslation(new Vector3d(0, 0, -2));

        TransformGroup rear_tx = new TransformGroup(tg);
        rear_tx.addChild(rear_wall);

        Material red_mat = new Material();
        red_mat.setEmissiveColor(red);
        red_mat.setDiffuseColor(red);
        red_mat.setSpecularColor(specular);
        red_mat.setLightingEnable(true);
        Appearance red_app = new Appearance();
        red_app.setMaterial(red_mat);

        Shape3D floor = new Box(4, 0.1f, 4, red_app);

        tg.setTranslation(new Vector3d(0, -2, 0));

        TransformGroup floor_tx = new TransformGroup(tg);
        floor_tx.addChild(floor);

        Material yellow_mat = new Material();
        yellow_mat.setEmissiveColor(yellow);
        yellow_mat.setDiffuseColor(yellow);
        yellow_mat.setSpecularColor(specular);
        yellow_mat.setLightingEnable(true);
        Appearance yellow_app = new Appearance();
        yellow_app.setMaterial(yellow_mat);

        Shape3D roof = new Box(4, 0.1f, 4, yellow_app);

        tg.setTranslation(new Vector3d(0, 2, 0));

        TransformGroup roof_tx = new TransformGroup(tg);
        roof_tx.addChild(roof);

        Group room = new Group();
        room.addChild(left_tx);
        room.addChild(right_tx);
        room.addChild(rear_tx);
        room.addChild(floor_tx);
        room.addChild(roof_tx);

        return room;
    }

    //
    // The following allows ParticleTest to be run as an application
    // as well as an applet
    //
    public static void main(String[] args)
    {
        ParticleTest demo = new ParticleTest();
        demo.init();
        demo.setVisible(true);
    }
}
