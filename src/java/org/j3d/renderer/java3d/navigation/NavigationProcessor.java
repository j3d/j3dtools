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

package org.j3d.renderer.java3d.navigation;

// Standard imports
import javax.media.j3d.*;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.vecmath.*;

// Application specific imports
import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.HeightDataSource;

import org.j3d.geom.GeometryData;
import org.j3d.renderer.java3d.util.J3DIntersectionUtils;
import org.j3d.util.UserSupplementData;
import org.j3d.util.MatrixUtils;

/**
 * A listener and handler responsible for executing all navigation commands
 * from mice and keyboards to move a viewpoint around a scene.
 * <p>
 *
 * This class does not contain any direct event handling. Instead it assumes
 * that another class with either derive from it or delegate to it to do the
 * actual movement processing. This allows it to be used as a standard AWT
 * event listener or a Java3D behaviour as required by the software.
 * <p>
 *
 * The class works like a standard VRML browser type navigation system. Press
 * the mouse button and drag to get the viewpoint moving. The more you drag
 * the button away from the start point, the faster the movement. The handler
 * does not recognize the use of the Shift key as a modifier to produce an
 * accelerated movement.
 * <p>
 *
 * This class will not change the cursor on the canvas in response to the
 * current mouse and navigation state. It will only notify the state change
 * listener. It is the responsibility of the listener to do this work.
 * <p>
 *
 * Separate states are allowed to be set for each button. Once one button is
 * pressed, all the other button presses are ignored. By default, all the
 * buttons start with no state set. The user will have to explicitly set
 * the state for each button to get them to work.
 * <p>
 *
 * The handler does not currently implement the Examine mode.
 * <p>
 *
 * <b>Terrain Following</b>
 * <p>
 *
 * When doing terrain following, the handler will project a ray from the
 * current viewpoint position to the ground position. It will then offset the
 * current position by the new position that we should be going to. If the
 * distance in the overall Y axis is less than the step height, the translation
 * will be allowed to proceed and the height adjusted to the new value. If it
 * is greater, then it will set the Z component to zero, allowing no forward
 * movement. Thus, if the next translation also has a sideways component, it
 * will simply shift sideways and not move forward.
 * <p>
 *
 * If there is no terrain under the current eye position, or the next eye
 * position, it will not change the Y axis value at all.
 * <P>
 *
 * If you do not wish to have terrain following for all modes, then pass a
 * <code>null</code> value for the terrain parameter to setWorldInfo().
 * <p>
 *
 *
 * <b>Collision Detection</b>
 * <p>
 *
 * Collision detection is based on using a fixed point representation of the
 * avatar - we do not have a volumetric body for it. A ray is cast in the
 * direction that we are looking that is the length of the avatarSize plus the
 * amount that we are due to move this next frame.
 * <p>
 *
 * If you do not wish to have collision detection for all modes, then pass a
 * <code>null</code> value for the collidables parameter to setWorldInfo().
 * <p>
 *
 * <b>Navigation Modes</b>
 * <p>
 *
 * <i>NONE<i>
 * <p>
 *
 * All navigation is disabled. We ignore any requests from mouse or
 * keyboard to move the viewpoint.
 * <p>
 *
 *
 * <i>EXAMINE</i>
 * <p>
 *
 * The viewpoint is moved around the center of rotation provided by
 * the user. There is no collision detection or terrain following in this mode.
 * <p>
 *
 * For movement input, direction is treated as the direction in the local
 * coordinates on the surface of a sphere formed around the rotation origin.
 * The scale represents the movement along the vector and then each component
 * defines the proportion used to move in that direction.
 * [0] is left/right, [1] is up/down, [2] is zoom in out where +Z is zoom out.
 * <p>
 *
 * <i>FLY</i>
 * <p>
 * The user moves through the scene that moves the eyepoint in forward,
 * reverse and side to side movements. There is collision detection, but no
 * terrain following.
 * <p>
 *
 * For movement input the direction controls the scale of the input to
 * translate and rotate about. [0] controls left/right rotation, [1]
 * controls pitch and [2] controls the amount of forward movement.
 * <p>
 *
 * <i>WALK</i>
 * <p>
 *
 * The user moves through the scene with left/right options and forward
 * reverse, but they are bound to the terrain and have collision detection.
 * <p>
 *
 * For movement input, the direction controls only two of the axes as gravity
 * is used to constrain in the local Y axis. [0] is the amount of rotation
 * left/right to perform each frame and [2] controls the forward/reverse
 * movement.
 * <p>
 *
 * <i>PAN</i>
 * <p>
 *
 * The camera moves in a sliding fashion along the given axis - the local
 * X or Z axis. There is not collision detection or terrain following.
 * <p>
 *
 * Move the viewpoint left/right/up/down while maintaining the current
 * viewing direction. [0] is used for left/right and [1] is used for
 * up/down. [2] is not used.
 * <p>
 *
 * <i>TILT</i>
 * <p>
 *
 * The camera rotates around the local axis in an up/down, left/right
 * fashion. It stays anchored in the one place and does not have terrain
 * following or collision detection.
 * <p>
 *
 * Movement input controls how the tilt is performed and is an absolute
 * value that controls total tilt. [0] controls rotation of the camera
 * in the X-Z plane, [1] controls rotation in the Y-Z plane.
 *
 * <b>TODO</b>
 * <p>
 * The collision vector does not move according to the direction that we are
 * travelling rather than the direction we are facing. Allows us to walk
 * backwards through objects when we shouldn't.
 * <p>
 * Implement Examine mode handling
 *
 * @author per-frame movement algorithms by
 *   <a href="http://www.ife.no/vr/">Halden VR Centre, Institute for Energy Technology</a><br>
 *   Terrain/Collision implementation by Justin Couch
 *   Replaced the Swing timer system with J3D behavior system: Morten Gustavsen.
 *   Modified the tilt navigation mode : Svein Tore Edvardsen.
 * @version $Revision: 1.6 $
 */
public class NavigationProcessor
{
    /** The avatar representation is a floating eyeball */
    public static final int AVATAR_POINT = 1;

    /** The avatar representation is a cylinder */
    public static final int AVATAR_CYLINDER = 2;

    /** The avatar representation is two shoulder rays */
    public static final int AVATAR_SHOULDERS = 3;

     /** The default height of the avatar */
    private static final float DEFAULT_AVATAR_HEIGHT = 1.8f;

    /*** The default size of the avatar for collisions */
    private static final float DEFAULT_AVATAR_SIZE = 0.25f;

    /** The default step height of the avatar to climb */
    private static final float DEFAULT_STEP_HEIGHT = 0.4f;

    /** Default time to orbit an object in examine mode */
    private static final float DEFAULT_ORBIT_TIME = 5;

    /** High-Side epsilon float = 0 */
    private static final double ZEROEPS = 0.000001;

    /** Fixed vector always pointing down -Y */
    private static final Vector3d Y_DOWN = new Vector3d(0, -1, 0);

    /** Fixed vector always pointing Y up for the examine mode */
    private static final Vector3d Y_UP = new Vector3d(0, 1, 0);

    /** Fixed vector always pointing along -Z */
    private static final Vector3d COLLISION_DIRECTION = new Vector3d(0, 0, -1);


    /** Intersection utilities used for terrain following */
    private J3DIntersectionUtils terrainIntersect;

    /** Intersection utilities used for terrain following */
    private J3DIntersectionUtils collideIntersect;

    /** The view that we are moving about. */
    private View view;

    /** The transform group above the view that is being moved each frame */
    private TransformGroup viewTg;

    /** The transform that belongs to the view transform group */
    private Transform3D viewTx;

    /** Path from the world root to the viewTg when in a shared graph */
    private SceneGraphPath viewPath;

    /** An observer for collision information */
    private CollisionListener collisionListener;

    /**
     * The current navigation state either set from us or externally as
     * the mouse if being dragged around. This is different to the state
     * that a given mouse button will generate
     */
    private int navigationState;

    /**
     * Flag indicating that we are currently doing something and should
     * ignore any current mouse presses.
     */
    private boolean movementInProgress;

    /** The current movement speed in m/s in the local coordinate system */
    private float speed;

    /** Scaled version of the speed based on the last movement instruction */
    private float scaledSpeed;

    // Java3D stuff for terrain following and collision detection

    /** The branchgroup to do the terrain picking on */
    private BranchGroup terrain;

    /** The branchgroup to do collision picking on */
    private BranchGroup collidables;

    /** Pick shape for terrain following */
    private PickRay terrainPicker;

    /** Pick shape for collision detection */
    private PickShape collisionPicker;

    /** The local down direction for the terrain picking */
    private Vector3d downVector;

    /** The vector along which we do collision detection */
    private Vector3d collisionVector;

    /** Movement direction vector */
    private Vector3d movementDirection;

    /** Orientation of the user's gaze */
    private Vector3d lookDirection;

    /** Last frames look Direction */
    private Vector3d lastLookDirection;

    /** An observer for information about updates for this transition */
    private FrameUpdateListener updateListener;

    /** The height of the avatar above the terrain */
    private float avatarHeight;

    /** The size of the avatar for collision detection */
    private float avatarSize;

    /** The step height of the avatar to allow stair climbing */
    private float avatarStep;

    /** Difference between the avatar height and the step height */
    private float lastTerrainHeight;

    /** Vector used to read the location value from a Transform3D */
    private Vector3d locationVector;

    /** Point3D used to represent the location for the picker setup */
    private Point3d locationPoint;

    /** Point 3D use to calculate the end point for collisions per frame */
    private Point3d locationEndPoint;

    /** Point 3D use to calculate the knee points for collisions per frame */
    private Point3d kneePoint;

    /** A point that we use for working calculations (coord transforms) */
    private Point3d wkPoint;

    /**
     * Vector for doing difference calculations on the point we have and the next
     * while doing terrian following.
     */
    private Vector3d diffVec;

    /** The intersection point that we really collided with */
    private Point3d intersectionPoint;

    // Vars for doing examine mode

    /** Center of rotation for examine and look at modes */
    private Point3d centerOfRotation;

    /** Time it takes to do a single orbit around the object */
    private float orbitTime;

    /** Current angle in the rotation. Always relative to the +X axis. */
    private double lastAngle;

    /** current radius of the user from the center of rotation */
    private double rotationRadius;

    // The variables from here down are working variables during the drag
    // process. We declare them as class scope so that we don't generate
    // garbage for every mouse movement. The idea is we just re-use these
    // rather than create and destroy each time.

    /** The translation amount set by the last change in movement value */
    private Vector3d dragTranslationAmt;

    /** A working value for the current frame's translation of the eye */
    private Vector3d oneFrameTranslation;

    /** A working value for the current frame's rotation of the eye */
    private Transform3D oneFrameRotation;

    /** The current translation total from the start of the movement */
    private Vector3d viewTranslation;

    /** The current viewpoint location in world coordinates */
    private Transform3D worldEyeTransform;

    /** The amount to move the view in mouse coords up/down */
    private double inputRotationY;

    /** The amount to move the view in mouse coords left/right */
    private double inputRotationX;

    /** Flag to indicate that we should be doing collisions this time */
    private boolean allowCollisions;

    /** Flag to indicate that we should do terrain following this time */
    private boolean allowTerrainFollowing;

    /** Used to correct the rotations */
    private double angle;

    /** behavior that drives our updates to the screen */
    private FrameTimerBehavior frameTimer;

    /** Calculations for frame duration timing */
    private long startFrameDurationCalc;

    /**
     * Working variable that is used to track the duration it took to render
     * the last frame so we can compensate for this frame.
     */
    private long frameDuration;

    /** Temp placeholder of the object that has just been collided with */
    private SceneGraphPath collidedObject;

    /** The avatar representation to use */
    private int avatarRep;

    /** Scratch var for lookat calcs */
    private Matrix4d lookatTmp;

    /** Matrix utilities */
    private MatrixUtils matUtils;

    /**
     * Inner class that provides an internally driven frame timer loop if there
     * is no external driver for the navigation system.
     * This behavior drives the updates of the screen and assures that no
     * new frame is rendered before the previous one is finished.
     */
    private class FrameTimerBehavior extends Behavior
    {
        /** The criteria used to wake up the behaviour */
        private WakeupCondition fpsCriterion;

        /**
         * Construct a new default instance of the behaviour.
         */
        public FrameTimerBehavior()
        {
            fpsCriterion = new WakeupOnElapsedFrames(0, false);
        }

        /**
         * Initialise this behavior to wake up on each frame that has elapsed.
         */
        public void initialize()
        {
            wakeupOn(fpsCriterion);
        }

        public void processStimulus( Enumeration criteria )
        {
            frameDuration = System.currentTimeMillis() - startFrameDurationCalc;

            // Hack to cover up lost mouseRel events
            if(getEnable() == false)
            {
                startFrameDurationCalc = System.currentTimeMillis();
                wakeupOn(fpsCriterion);

                return;
            }

            processClockTick();

            wakeupOn(fpsCriterion);
        }
    }

    /**
     * Create a new mouse handler with no view information set. This
     * handler will not do anything until the view transform
     * references have been set and the navigation modes for at least one
     * mouse button.
     */
    public NavigationProcessor()
    {
        navigationState = NavigationState.NO_STATE;
        movementInProgress = false;

        terrainIntersect = new J3DIntersectionUtils();
        collideIntersect = new J3DIntersectionUtils();

        viewTg = new TransformGroup();
        viewTx = new Transform3D();

        worldEyeTransform = new Transform3D();
        downVector = new Vector3d();
        terrainPicker = new PickRay();

        movementDirection = new Vector3d();
        lookDirection = new Vector3d();
        lastLookDirection = new Vector3d();
        lastLookDirection.x = 0;
        lastLookDirection.y = 0;
        lastLookDirection.z = 0;

        centerOfRotation = new Point3d();
        collisionVector = new Vector3d();
        intersectionPoint = new Point3d();
        wkPoint = new Point3d();
        diffVec = new Vector3d();

//        avatarRep = AVATAR_SHOULDERS;
        avatarRep = AVATAR_POINT;

        if (avatarRep == AVATAR_POINT) {
            collisionPicker = new PickSegment();
        } else if (avatarRep == AVATAR_CYLINDER) {
            collisionPicker = new PickCylinderSegment();
        } else if (avatarRep == AVATAR_SHOULDERS) {
            collisionPicker = new PickCylinderSegment();
        }


        locationVector = new Vector3d();
        locationPoint = new Point3d();
        locationEndPoint = new Point3d();
        kneePoint = new Point3d();

        dragTranslationAmt = new Vector3d();
        oneFrameTranslation = new Vector3d();
        oneFrameRotation = new Transform3D();
        viewTranslation = new Vector3d();
        inputRotationY = 0;
        inputRotationX = 0;

        allowCollisions = false;
        allowTerrainFollowing = false;

        orbitTime = DEFAULT_ORBIT_TIME;
        avatarHeight = DEFAULT_AVATAR_HEIGHT;
        avatarSize = DEFAULT_AVATAR_SIZE;
        avatarStep = DEFAULT_STEP_HEIGHT;
        lastTerrainHeight = 0;
        speed = 0;

        centerOfRotation = new Point3d(0, 0, 0);
        lookatTmp = new Matrix4d();
        matUtils = new MatrixUtils();
    }

    /**
     * Fetch the behaviour used to do the timer management for scenegraph
     * updates during navigation. This behaviour is needed if you want to have
     * automatic navigation from the mouse input. If you wish to do your own
     * management of when to update the navigation, then you should not call
     * this method, and instead call the processNewFrame() method.
     * <p>
     *
     * If this behavior is not fetched and you do want automatic handling of
     * navigation, navigation will not happen! A single instance
     * of the behaviour is used during the entire lifetime of this instance.
     *
     * @return The behavior used to control updates
     */
    public Behavior getTimerBehavior()
    {
        if(frameTimer == null)
        {
            BoundingSphere sched = new BoundingSphere();
            sched.setRadius(Double.POSITIVE_INFINITY);

            frameTimer = new FrameTimerBehavior();
            frameTimer.setSchedulingBounds(sched);
            frameTimer.setEnable(false);
        }

        return frameTimer;
    }

    /**
     * Set the center of rotation explicitly to this place. Coordinates must
     * be in the coordinate space of the current view transform group. The
     * provided array must be of least length 3. Center of rotation is used
     * in examine mode.
     *
     * @param center The new center to use
     * @param lookPos The position to look from.  NULL is the current user position.
     */
    public void setCenterOfRotation(float[] center, float[] lookPos)
    {
        centerOfRotation.x = center[0];
        centerOfRotation.y = center[1];
        centerOfRotation.z = center[2];

        if(navigationState == NavigationState.EXAMINE_STATE ||
            navigationState == NavigationState.LOOKAT_STATE)
        {
            if (lookPos == null)
            {
                viewTg.getTransform(viewTx);
                viewTx.get(viewTranslation);
                locationPoint.set(viewTranslation);
            } else
            {
                locationPoint.x = lookPos[0];
                locationPoint.y = lookPos[1];
                locationPoint.z = lookPos[2];
            }

            double x = locationPoint.x - centerOfRotation.x;
            double z = locationPoint.z - centerOfRotation.z;

            rotationRadius = Math.sqrt(x * x + z * z);
            lastAngle = Math.atan2(z, x);

            matUtils.lookAt(locationPoint, centerOfRotation, Y_UP, lookatTmp);
            viewTx.set(lookatTmp);
            viewTx.invert();
            viewTg.setTransform(viewTx);
        }
    }

    /**
     * Set the view and it's related transform group to use. The transform
     * group must allow for reading the local to Vworld coordinates so that
     * we can accurately implement terrain following.
     *
     * @param view is the View object that we're modifying.
     * @param tg The transform group above the view object that should be used
     * @throws IllegalArgumentException One of the values is null and the
     *   other is not
     * @throws IllegalStateException The transform group does not allow
     *   reading of the vworld transforms or does not allow it to be set
     */
    public void setViewInfo(View view, TransformGroup tg)
    {
        setViewInfo(view, tg, null);
    }

    /**
     * Set the view and it's related transform group to use and the path to
     * get there from the root of the scene. The transform group must allow
     * for reading the local to Vworld coordinates so that we can accurately
     * implement terrain following. A null value for the path is permitted.
     *
     * @param view is the View object that we're modifying.
     * @param tg The transform group above the view object that should be used
     * @param path The path from the root to the transform to use or null
     * @throws IllegalArgumentException One of the values is null and the
     *   other is not
     * @throws IllegalStateException The transform group does not allow
     *   reading of the vworld transforms or does not allow it to be set
     */
    public void setViewInfo(View view, TransformGroup tg, SceneGraphPath path)
    {
        if(((view != null) && (tg == null)) ||
           ((view == null) && (tg != null)))
            throw new IllegalArgumentException("View or TG is null when " +
                                               "the other isn't");

        this.view = view;
        this.viewTg = tg;
        viewPath = path;

        if(tg == null)
            return;


        viewTg.getTransform(viewTx);

        if(tg.isLive())
        {
           if(!viewTg.getCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ))
                throw new IllegalStateException("Live scenegraph and cannot " +
                                                "read the VWorld transform");
        }
        else
        {
            tg.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
        }

        // TODO:
        // Adjust the step height to the values from the scaled down vector
        // component so that it relates to the world coordinate system.
    }

    /**
     * Change the currently set scene graph path for the world root to this new
     * path without changing the rest of the view setup. Null will clear the
     * current path set.
     *
     * @param path The new path to use for the viewpoint
     */
    public void setViewPath(SceneGraphPath path)
    {
        viewPath = path;
    }

    /**
     * Set the branchgroups to use for terrain and collision information. The
     * two are treated separately for the different processes. The caller may
     * choose to make them the same reference, but the code internally treats
     * them separately.
     * <p>
     *
     * <b>Note</b> For picking purposes, the code currently assumes that both
     * groups do not have any parent transforms. That is, their world origin
     * is the same as the transform group presented in the view information.
     *
     * @param terrainGroup  The geometry to use as terrain for following
     * @param worldGroup The geometry to use for collisions
     */
    public void setWorldInfo(BranchGroup terrainGroup, BranchGroup worldGroup)
    {
        terrain = terrainGroup;
        collidables = worldGroup;
    }

    /**
     * Set the information about the avatar that is used for collisions and
     * navigation information.
     *
     * @param height The heigth of the avatar above the terrain
     * @param size The distance between the avatar and collidable objects
     * @param stepHeight The height that an avatar can step over
     */
    public void setAvatarInfo(float height, float size, float stepHeight)
    {
        avatarHeight = height;
        avatarSize = size;
        avatarStep = stepHeight;

        // TODO:
        // Adjust the step height to the values from the scaled down vector
        // component so that it relates to the world coordinate system.
    }

    /**
     * Set the navigation speed to the new value. The speed must be a
     * non-negative number.
     *
     * @param newSpeed The new speed value to use
     * @throws IllegalArgumentException The value was negative
     */
    public void setNavigationSpeed(float newSpeed) {
        if(newSpeed < 0)
            throw new IllegalArgumentException("Negative speed value");
        speed = newSpeed;
    }

    /**
     * Set the time it takes in seconds to make one 360 degree rotation of the
     * center position when in examine mode. The time must be a non-negative
     * number.
     *
     * @param time The time value to use
     * @throws IllegalArgumentException The value was <= 0
     */
    public void setOrbitTime(float time) {
        if(time <= 0)
            throw new IllegalArgumentException("Orbit time <= 0");

        orbitTime = time;
    }


    /**
     * Set the ability to use a given state within the handler for a
     * specific mouse button (up to 3). This allows the caller to control
     * exactly what states are allowed to be used and with which buttons.
     * Note that it is quite legal to set all three buttons to the same
     * navigation state
     *
     * @param state The navigation state to use for that button
     */
    public void setNavigationState(int state)
    {
        navigationState = state;
    }

    /**
     * Set the listener for collision notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for updates
     */
    public void setCollisionListener(CollisionListener l)
    {
        collisionListener = l;
    }

    /**
     * Callback to ask the listener what navigation state it thinks it is
     * in.
     *
     * @return The state that the listener thinks it is in
     */
    public int getNavigationState()
    {
        return navigationState;
    }

    /**
     * Call to update the user position now based on the difference in time
     * between the last call and this call. This is to be called when the user
     * wishes to manually control the navigation process rather than relying on
     * the inbuilt timer. The user should not be using both mechanisms at the
     * same time although the code takes no steps to enforce this. If you do
     * try to do manual updates while also having the automated system, the
     * results are undefined.
     */
    public void processNextFrame()
    {
        frameDuration = System.currentTimeMillis() - startFrameDurationCalc;
        if(frameDuration == 0)
            frameDuration = 1;

        processClockTick();
    }

    /**
     * Start the user moving in the current direction. Initialises all the
     * internal timers but does not actually start the movement.
     */
    public void startMove()
    {
        if(movementInProgress || (viewTg == null) ||
           (navigationState == NavigationState.NO_STATE) ||
            navigationState == NavigationState.LOOKAT_STATE)
            return;

        viewTg.getTransform(viewTx);
        viewTx.get(viewTranslation);

        inputRotationY = 0;
        inputRotationX = 0;

        oneFrameRotation.setIdentity();
        dragTranslationAmt.scale(0);

        //start the frame duration calculation
        startFrameDurationCalc = System.currentTimeMillis();

        //enable the behavior that controls navigation
        if(frameTimer != null)
            frameTimer.setEnable(true);

        switch(navigationState)
        {
            case NavigationState.FLY_STATE:
                allowCollisions = (collidables != null);
                allowTerrainFollowing = false;
                break;

            case NavigationState.PAN_STATE:
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NavigationState.TILT_STATE:
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NavigationState.WALK_STATE:
                allowCollisions = (collidables != null);
                allowTerrainFollowing = (terrain != null);
                break;

            case NavigationState.EXAMINE_STATE:
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NavigationState.NO_STATE:
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;
        }

        if((navigationState == NavigationState.WALK_STATE) &&
           allowTerrainFollowing)
        {
            setInitialTerrainHeight();
        }

        if(navigationState == NavigationState.EXAMINE_STATE)
        {
            // If in navigation state, need to determine the original angle of the
            // user. Always relative to the +X axis.
            double x = viewTranslation.x - centerOfRotation.x;
            double z = viewTranslation.z - centerOfRotation.z;

            rotationRadius = Math.sqrt(x * x + z * z);
            lastAngle = Math.atan2(z, x);

            locationPoint.set(viewTranslation);

            matUtils.lookAt(locationPoint, centerOfRotation, Y_UP, lookatTmp);
            viewTx.set(lookatTmp);
            viewTx.invert();
            viewTg.setTransform(viewTx);
        }
    }

    /**
     * Halt the current movement being processed. Any move input after this
     * will be ignored and the user will stop moving.
     */
    public void stopMove()
    {
        movementInProgress = false;
        allowCollisions = false;
        allowTerrainFollowing = false;

        //disable behavior that controls navigation
        if(frameTimer != null)
            frameTimer.setEnable(false);

        viewTx.normalize();

        inputRotationY = 0;
        inputRotationX = 0;

        oneFrameRotation.setIdentity();
        dragTranslationAmt.scale(0);

        viewTg.getTransform(viewTx);
    }

    /**
     * Update the user movement to be going in this absolute direction. Scale
     * gives a proportion of the set speed value to move in that direction.
     * The interpretation of the 3 components of the vector are described in
     * the class header documentation.
     *
     * @param direction The new direction to move the user
     * @param scale fractional value of the set speed to use [0, inf)
     */
    public void move(float[] direction, float scale)
    {
        if(viewTg == null)
            return;

        // Don't move when speed = 0
        if (speed == 0) {
            scale = 0;
        }

        scaledSpeed = speed * scale;

        switch(navigationState)
        {
            case NavigationState.FLY_STATE:
                //  Translate on Z:
                dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);

                //  Rotate around Y:
                inputRotationY = direction[0] * scale;
                inputRotationX = direction[1] * scaledSpeed;

                allowCollisions = (collidables != null);
                allowTerrainFollowing = false;
                break;

            case NavigationState.PAN_STATE:
                //  Translate on X,Y:
                dragTranslationAmt.set(direction[0] * scaledSpeed,
                                       direction[1] * scaledSpeed,
                                       direction[2] * scaledSpeed);

                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NavigationState.TILT_STATE:
                //  Rotate arround X,Y:
                inputRotationX = direction[1] * scale;
                inputRotationY = direction[0] * scale;
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NavigationState.WALK_STATE:
                //  Translate on Z only
                dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);

                //  Rotate around Y:
                inputRotationY = direction[0] * scale;

                allowCollisions = (collidables != null);
                allowTerrainFollowing = (terrain != null);
                break;

            case NavigationState.EXAMINE_STATE:
                //  Translate on Z only
                dragTranslationAmt.set(0, 0, direction[2] * scaledSpeed);
                inputRotationY = direction[0] * scale;
                inputRotationX = direction[1] * scale;

                // do nothing
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NavigationState.NO_STATE:
                // do nothing
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;
        }
    }

    /**
     * Orient the viewer direction to this position, but do not change their
     * movement direction. The direction is a normalised 3D vector relative to
     * the current movement. 0,0,0 means always look where the movement is
     * taking you.
     *
     * @param direction 3D vector where the user should be looking
     */
    public void orient(float[] direction)
    {
        lookDirection.x = direction[0];
        lookDirection.y = direction[1];
        lookDirection.z = direction[2];

        viewTg.getTransform(viewTx);
        viewTx.get(viewTranslation);

        inputRotationY = 0;
        inputRotationX = 0;

        oneFrameRotation.setIdentity();
        dragTranslationAmt.scale(0);

        frameDuration = 0;
        processClockTick();
    }

    /**
     * Get the current user position in world coordinates.
     *
     * @param pos The position vector to fill in
     */
    public void getPosition(Vector3d pos) {

        if(viewPath != null)
            viewTg.getLocalToVworld(viewPath, worldEyeTransform);
        else
            viewTg.getLocalToVworld(worldEyeTransform);

        worldEyeTransform.mul(viewTx);

        worldEyeTransform.get(locationVector);

        pos.x = locationVector.x;
        pos.y = locationVector.y;
        pos.z = locationVector.z;
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Set the listener for frame update notifications. By setting a value of
     * null it will clear the currently set instance
     *
     * @param l The listener to use for this transition
     */
    public void setFrameUpdateListener(FrameUpdateListener l)
    {
        updateListener = l;
    }

    /**
     * Internal processing method for the per-frame update behaviour. Assumes
     * that the frameDuration has been calculated before calling this method.
     */
    private void processClockTick()
    {
        if(navigationState == NavigationState.EXAMINE_STATE)
            processExamineMotion();
        else if (navigationState == NavigationState.LOOKAT_STATE)
            return;
        else
            processDefaultMotion();
    }

    /**
     * Motion behaviour processing for the examine mode.
     */
    private void processExamineMotion()
    {
        boolean matrix_changed = false;
        double total_theta = lastAngle;
        double x, y, z;

        // Check to see if the radius is changing at all. If so, recalculate
        // for this frame
        viewTg.getTransform(viewTx);
        viewTx.get(locationVector);

        if(dragTranslationAmt.z != 0)
        {
            double motionDelay = 0.0005 * frameDuration;

            x = locationVector.x - centerOfRotation.x;
            z = locationVector.z - centerOfRotation.z;

//            double speed = dragTranslationAmt.z * 2 / screenHeight;
            double local_speed = dragTranslationAmt.z * scaledSpeed;

            rotationRadius = Math.sqrt(x * x + z * z) + local_speed;

            matrix_changed = true;
        }

        if(inputRotationY != 0)
        {
            // how much of a circle did we take this time? Frame duration in
            // ms but orbit time in seconds. Speed is proportional to the amount of
            // width draging
            double local_speed = inputRotationY * scaledSpeed;
            double theta_inc = local_speed * Math.PI * 2 * frameDuration / (orbitTime * 1000);

            total_theta += theta_inc;

            if(total_theta > Math.PI * 2)
                total_theta -= Math.PI * 2;

            lastAngle = total_theta;

            matrix_changed = true;
        }

        if(matrix_changed)
        {
            x = rotationRadius * Math.cos(total_theta);
            z = rotationRadius * Math.sin(total_theta);

            // just set the x and z position based on the angle. The Y position
            // remains unchanged. This is so you can elevate and orbit looking down.
            // Also means we don't have to worry about divide by zero as the user
            // goes over the poles.

            locationPoint.x = centerOfRotation.x + x;
            locationPoint.y = locationVector.y;
            locationPoint.z = centerOfRotation.z + z;

            matUtils.lookAt(locationPoint, centerOfRotation, Y_UP, lookatTmp);
            viewTx.set(lookatTmp);
            viewTx.invert();
            viewTg.setTransform(viewTx);

        }

        startFrameDurationCalc = System.currentTimeMillis();

        if(updateListener != null)
        {
            try
            {
                updateListener.viewerPositionUpdated(viewTx);
            }
            catch(Exception e)
            {
                System.out.println("Error sending frame update message");
                e.printStackTrace();
            }
        }
    }

    /**
     * Normal motion behaviour processing for anything that is not special
     * cased.
     */
    private void processDefaultMotion()
    {
        double motionDelay = 0.005 * frameDuration;

        viewTg.getTransform(viewTx);

        // Remove last lookDirection
        if (!(lastLookDirection.x == 0 && lastLookDirection.y == 0
            && lastLookDirection.z == 0)) {
            oneFrameRotation.setEuler(lastLookDirection);
            viewTx.mul(oneFrameRotation);
        }

        oneFrameRotation.rotX(inputRotationX * motionDelay);
        viewTx.mul(oneFrameRotation);

        //  RotateY:
        oneFrameRotation.rotY(inputRotationY * motionDelay);
        viewTx.mul(oneFrameRotation);

        //  Translation:
        oneFrameTranslation.set(dragTranslationAmt);
        oneFrameTranslation.scale(motionDelay);

        viewTx.transform(oneFrameTranslation);

        boolean collision = false;

        // If we allow collisions, adjust it for the collision amount
        if(allowCollisions)
            collision = !checkCollisions();

        if(allowTerrainFollowing && !collision)
            collision = !checkTerrainFollowing();

        if(collision)
        {
            if(collisionListener != null)
                collisionListener.avatarCollision(collidedObject);

            collidedObject = null;

            // Z doesn't always stop the user, must clear all
            oneFrameTranslation.z = 0;

            oneFrameTranslation.x = 0;
            oneFrameTranslation.y = 0;

        }

        // Now set the translation amounts that have been adjusted by any
        // collisions.
        viewTranslation.add(oneFrameTranslation);
        viewTx.setTranslation(viewTranslation);

        if (!(lookDirection.x == 0 && lookDirection.y == 0 &&
            lookDirection.z == 0)) {
            oneFrameRotation.setEuler(lookDirection);
            lastLookDirection.x = -lookDirection.x;
            lastLookDirection.y = -lookDirection.y;
            lastLookDirection.z = -lookDirection.z;
            viewTx.mul(oneFrameRotation);
        }

        try
        {
            viewTg.setTransform(viewTx);
        }
        catch(Exception e)
        {
            //check for bad transform:
            viewTx.rotX(0);
            viewTg.setTransform(viewTx);
            // e.printStackTrace();
        }

        startFrameDurationCalc = System.currentTimeMillis();

        if(updateListener != null)
        {
            try
            {
                updateListener.viewerPositionUpdated(viewTx);
            }
            catch(Exception e)
            {
                System.out.println("Error sending frame update message");
                e.printStackTrace();
            }
        }
    }

    /**
     * Check the terrain following component of the translation for the next
     * frame. Adjusts the oneFrameTranslation amount depending on the terrain
     * and step height we encounter at this next location.
     *
     * @return true if the terrain following has successfully been applied
     *   false means a collision.
     */
    private boolean checkTerrainFollowing()
    {
        boolean ret_val = true;

        if(viewPath != null)
            viewTg.getLocalToVworld(viewPath, worldEyeTransform);
        else
            viewTg.getLocalToVworld(worldEyeTransform);

        worldEyeTransform.mul(viewTx);

        worldEyeTransform.get(locationVector);
        worldEyeTransform.transform(Y_DOWN, downVector);

        locationPoint.add(locationVector, oneFrameTranslation);
        terrainPicker.set(locationPoint, downVector);

        SceneGraphPath[] ground = terrain.pickAllSorted(terrainPicker);

        // if there is no ground below us, do nothing.
        if((ground == null) || (ground.length == 0))
            return ret_val;

        double shortest_length = -1;

        for(int i = 0; i < ground.length; i++)
        {
            // Firstly, check the path to see if this is eligible for picking
            // Look at the picked item first, then do a depth traversal of the
            // path from the root down to the the node.
            Node end = ground[i].getObject();
            Object user_data = end.getUserData();

            if(user_data instanceof UserSupplementData &&
               !((UserSupplementData)user_data).isTerrain)
                    continue;

            int num_path_items = ground[i].nodeCount();
            boolean not_eligible = false;

            for(int j = 0; j < num_path_items && !not_eligible; j++)
            {
                Node group = ground[i].getNode(j);
                user_data = group.getUserData();

                if(user_data instanceof UserSupplementData)
                    not_eligible = !((UserSupplementData)user_data).isTerrain;
            }

            if(not_eligible)
                continue;

            // So this is ok, let's look at the object to see whether we
            // intersect with the actual geometry.
            Transform3D local_tx = ground[i].getTransform();
            local_tx.get(locationVector);

            Shape3D i_shape = (Shape3D)ground[i].getObject();

            // Get the user data, if the user data contains a height data
            // source use that to determine the terrain, otherwise pass it
            // through to the geometry intersection handling. Inside that
            // Also check to see what geometry is being used
            user_data = i_shape.getUserData();
            HeightDataSource hds = null;
            GeometryData geom_data = null;

            if(user_data instanceof UserSupplementData)
            {
                UserSupplementData usd = (UserSupplementData)user_data;

                if(usd.geometryData instanceof HeightDataSource)
                    hds = (HeightDataSource)usd.geometryData;
                else if(usd.geometryData instanceof GeometryData)
                    geom_data = (GeometryData)usd.geometryData;
            }
            else if(user_data instanceof HeightDataSource)
            {
                hds = (HeightDataSource)user_data;
            }
            else if(user_data instanceof GeometryData)
                geom_data = (GeometryData)user_data;

            if(hds != null)
            {
                intersectionPoint.x = locationVector.x;
                intersectionPoint.y = locationVector.y;
                intersectionPoint.z = hds.getHeight((float)locationVector.x,
                                                    (float)locationVector.y);
            }
            else
            {
                // Do we have geometry data to play with at the shape level?
                // If so, use that in preference to going down to the
                // individual geometry arrays of the shape.
                if(geom_data != null)
                {
                    if(terrainIntersect.rayUnknownGeometry(locationPoint,
                                                           downVector,
                                                           0,
                                                           geom_data,
                                                           local_tx,
                                                           wkPoint,
                                                           false))
                    {
                        diffVec.sub(locationPoint, wkPoint);

                        if((shortest_length == -1) ||
                           (diffVec.lengthSquared() < shortest_length))
                        {
                            shortest_length = diffVec.lengthSquared();
                            intersectionPoint.set(wkPoint);
                            collidedObject = ground[i];
                        }
                    }
                }
                else
                {
                    Enumeration geom_list = i_shape.getAllGeometries();

                    while(geom_list.hasMoreElements())
                    {
                        GeometryArray geom = (GeometryArray)geom_list.nextElement();

                        if(geom == null)
                            continue;

                        user_data = geom.getUserData();
                        geom_data = null;

                        if(user_data instanceof UserSupplementData)
                        {
                            UserSupplementData usd = (UserSupplementData)user_data;

                            if(usd.geometryData instanceof HeightDataSource)
                                hds = (HeightDataSource)usd.geometryData;
                            else if(usd.geometryData instanceof GeometryData)
                                geom_data = (GeometryData)usd.geometryData;
                        }
                        else if(user_data instanceof HeightDataSource)
                        {
                            hds = (HeightDataSource)user_data;
                        }
                        else if(user_data instanceof GeometryData)
                            geom_data = (GeometryData)user_data;


                        boolean intersect = false;

                        // Ah, finally. This is where we do the intersection
                        // testing against either the raw geometry or the object.
                        if(geom_data != null)
                        {
                            intersect =
                                terrainIntersect.rayUnknownGeometry(locationPoint,
                                                                  downVector,
                                                                  0,
                                                                  geom_data,
                                                                  local_tx,
                                                                  wkPoint,
                                                                  false);
                        }
                        else
                        {
                            intersect =
                                terrainIntersect.rayUnknownGeometry(locationPoint,
                                                                  downVector,
                                                                  0,
                                                                  geom,
                                                                  local_tx,
                                                                  wkPoint,
                                                                  false);
                        }

                        if(intersect)
                        {
                            diffVec.sub(locationPoint, wkPoint);
                            if((shortest_length == -1) ||
                               (diffVec.lengthSquared() < shortest_length))
                            {
                                shortest_length = diffVec.lengthSquared();
                                intersectionPoint.set(wkPoint);
                                collidedObject = ground[i];
                            }
                        }
                    }
                }
            }
        }
        // No intersection!!!! How did that happen? Well, just exit and
        // pretend there was nothing below us
        if(shortest_length == -1)
            return true;

        // Is the difference in world Y values greater than the step height?
        // If so, then jump the viewpoint to the new terrain height plus the
        // avatar height above ground. Handles both rising and descending
        // terrain. If the difference is greater than the step height, we set
        // the translation to nothing in the Z direction.
        double terrain_step = intersectionPoint.y - lastTerrainHeight;
        double height_above_terrain = locationPoint.y - intersectionPoint.y;


        // Do we need to adjust the height? If so the check if the height is a
        // step that is too high or not
        if(!floatEq(height_above_terrain - avatarHeight, 0))
        {
            if(floatEq(terrain_step, 0))
            {
                // Flat surface. Check to see the avatar height is correct
                oneFrameTranslation.y = avatarHeight - height_above_terrain;
                ret_val = true;
            }
            else if(terrain_step < avatarStep)
            {
                oneFrameTranslation.y = terrain_step;
                ret_val = true;
            }
            else
            {
                // prevent it. Set the transform to 0.
                ret_val = false;

                // Don't let lastTerrainHeight get set
                return ret_val;
            }
        }

        lastTerrainHeight = (float)intersectionPoint.y;

        return ret_val;
    }

    /**
     * Check the collision detection component of the translation for the next
     * frame. Basically test for a collision within the given distance that
     * would be travelled next frame. If nothing is picked then no collision
     * will occur. If it does find something then obviously a collision will
     * occurr so you do return a flag to say so.
     *
     * @param prefetch True if viewpoint info has already been fetched for
     *    this frame
     * @return true if the no collisions detected, false means a collision.
     */
    private boolean checkCollisions()
    {
        boolean ret_val = true;

        if(viewPath != null)
            viewTg.getLocalToVworld(viewPath, worldEyeTransform);
        else
            viewTg.getLocalToVworld(worldEyeTransform);

        worldEyeTransform.mul(viewTx);

        // Where are we now?
        worldEyeTransform.get(locationVector);
        locationPoint.set(locationVector);

/*
        // Where are we going to be soon?
        worldEyeTransform.transform(COLLISION_DIRECTION, collisionVector);

        collisionVector.scale(avatarSize);

        locationEndPoint.add(locationVector, collisionVector);
        locationEndPoint.add(oneFrameTranslation);
*/

        // TODO: Look in the direction of movement.  Still not sure this is right

        collisionVector.x = oneFrameTranslation.x;
        collisionVector.y = oneFrameTranslation.y;
        collisionVector.z = oneFrameTranslation.z;

        if(collisionVector.length() > 0)
            collisionVector.normalize();

        collisionVector.scale(avatarSize);

        locationEndPoint.add(locationVector, collisionVector);
        locationEndPoint.add(oneFrameTranslation);

        // We need to transform the end point to the direction that we are
        // currently travelling. At the moment, this always points forward
        // in the same direction as the viewpoint.
        switch(avatarRep) {
            case AVATAR_POINT:
                ((PickSegment)collisionPicker).set(locationPoint, locationEndPoint);
                break;

            case AVATAR_CYLINDER:
                kneePoint.x = locationEndPoint.x;

                // Not good for stepHeights > avatarSize
                kneePoint.y = locationEndPoint.y - avatarHeight + avatarStep;
                kneePoint.z = locationEndPoint.z;

                ((PickCylinderSegment)collisionPicker).set(locationEndPoint,kneePoint, avatarSize);
                break;

            case AVATAR_SHOULDERS:
                double center;

                center = locationPoint.x;

                locationPoint.x -= avatarSize;
                // A small shoulder cone
                kneePoint.x = center + avatarSize;

                kneePoint.y = locationEndPoint.y - avatarSize / 2;
                kneePoint.z = locationEndPoint.z;

                ((PickCylinderSegment)collisionPicker).set(locationEndPoint,kneePoint, avatarSize);
                break;
        }

        SceneGraphPath[] closest = collidables.pickAllSorted(collisionPicker);

        if((closest == null) || (closest.length == 0))
            return true;

        boolean real_collision = false;
        float length = (float)collisionVector.length();

        for(int i = 0; (i < closest.length) && !real_collision; i++)
        {
            // Firstly, check the path to see if this is eligible for picking
            // Look at the picked item first, then do a depth traversal of the
            // path from the root down to the the node.
            Node end = closest[i].getObject();
            Object user_data = end.getUserData();

            if(user_data instanceof UserSupplementData &&
               !((UserSupplementData)user_data).collidable)
                    continue;

            int num_path_items = closest[i].nodeCount();
            boolean not_eligible = false;

            for(int j = 0; j < num_path_items && !not_eligible; j++)
            {
                Node group = closest[i].getNode(j);
                user_data = group.getUserData();

                if(user_data instanceof UserSupplementData)
                    not_eligible = !((UserSupplementData)user_data).collidable;
            }

            if(not_eligible)
                continue;

            // OK, so we collided on the bounds, lets check on the geometry
            // directly to see if we had a real collision. Java3D just gives
            // us the collision based on the bounding box intersection. We
            // might actually have just walked through something like an
            // archway.
            Transform3D local_tx = closest[i].getTransform();
            Shape3D i_shape = (Shape3D)closest[i].getObject();
            Enumeration geom_list = i_shape.getAllGeometries();
            GeometryData geom_data = null;

            while(geom_list.hasMoreElements() && !real_collision)
            {
                GeometryArray geom = (GeometryArray)geom_list.nextElement();

                if(geom == null)
                    continue;

                user_data = geom.getUserData();
                geom_data = null;

                if(user_data instanceof UserSupplementData)
                {
                    UserSupplementData usd = (UserSupplementData)user_data;

                    if(usd.geometryData instanceof GeometryData)
                        geom_data = (GeometryData)usd.geometryData;
                }
                else if(user_data instanceof GeometryData)
                    geom_data = (GeometryData)user_data;

                // Ah, finally. This is where we do the intersection
                // testing against either the raw geometry or the object.
                if(geom_data != null)
                {
                    real_collision =
                        terrainIntersect.rayUnknownGeometry(locationPoint,
                                                           collisionVector,
                                                           length,
                                                           geom_data,
                                                           local_tx,
                                                           wkPoint,
                                                           true);

                        if (real_collision)
                            System.out.println("head collided:  dir: " + collisionVector + " cpnt: " + wkPoint);

                    // Fake CylinderSegment test with a second ray at kneePnt
                    if (!real_collision && avatarRep == AVATAR_CYLINDER) {
                        real_collision =
                            terrainIntersect.rayUnknownGeometry(kneePoint,
                                                               collisionVector,
                                                               length,
                                                               geom_data,
                                                               local_tx,
                                                               wkPoint,
                                                               true);
                        if (real_collision)
                            System.out.println("knee collided");
                    }

                    if (!real_collision && avatarRep == AVATAR_SHOULDERS) {
                        // Test second shoulder
                        real_collision =
                            terrainIntersect.rayUnknownGeometry(kneePoint,
                                                               collisionVector,
                                                               length,
                                                               geom_data,
                                                               local_tx,
                                                               wkPoint,
                                                               true);
                        if (real_collision)
                            System.out.println("right shoulder collided");
                    }

                }
                else
                {
                    real_collision =
                        terrainIntersect.rayUnknownGeometry(locationPoint,
                                                           collisionVector,
                                                           length,
                                                           geom,
                                                           local_tx,
                                                           wkPoint,
                                                           true);

                        if (real_collision)
                            System.out.println("head collided");

                    // Fake CylinderSegment test with a second ray at kneePnt
                    if (!real_collision && avatarRep == AVATAR_CYLINDER) {
                        real_collision =
                            terrainIntersect.rayUnknownGeometry(kneePoint,
                                                               collisionVector,
                                                               length,
                                                               geom,
                                                               local_tx,
                                                               wkPoint,
                                                               true);
                        if (real_collision)
                            System.out.println("knee collided");
                    }

                    if (!real_collision && avatarRep == AVATAR_SHOULDERS) {
                        // Test second shoulder
                        real_collision =
                            terrainIntersect.rayUnknownGeometry(kneePoint,
                                                               collisionVector,
                                                               length,
                                                               geom,
                                                               local_tx,
                                                               wkPoint,
                                                               true);
                        if (real_collision)
                            System.out.println("right shoulder collided");
                    }

                }
            }


            ret_val = !real_collision;

            if(real_collision)
                collidedObject = closest[i];
        }

        return ret_val;
    }

    /**
     * Check the terrain height at the current position. This is done when
     * we first start moving a viewpoint with a mouse press.
     *
     * @return true if the terrain following has successfully been applied
     *   false means a collision.
     */
    private void setInitialTerrainHeight()
    {
        if(terrain == null)
            return;

        if(viewPath != null)
            viewTg.getLocalToVworld(viewPath, worldEyeTransform);
        else
            viewTg.getLocalToVworld(worldEyeTransform);

        worldEyeTransform.mul(viewTx);

        worldEyeTransform.get(locationVector);
        worldEyeTransform.transform(Y_DOWN, downVector);

        locationPoint.set(locationVector);
        terrainPicker.set(locationPoint, downVector);

        SceneGraphPath[] ground = terrain.pickAllSorted(terrainPicker);

        // if there is no ground below us, do nothing.
        if(ground == null)
            return;

        double shortest_length = -1;

        for(int i = 0; i < ground.length; i++)
        {
            // Firstly, check the path to see if this is eligible for picking.
            Node end = ground[i].getObject();
            Object user_data = end.getUserData();

            if(user_data instanceof UserSupplementData &&
               !((UserSupplementData)user_data).isTerrain)
                    continue;

            int num_path_items = ground[i].nodeCount();
            boolean not_eligible = false;

            for(int j = 0; j < num_path_items && !not_eligible; j++)
            {
                Node group = ground[i].getNode(j);
                user_data = group.getUserData();

                if(user_data instanceof UserSupplementData)
                    not_eligible = !((UserSupplementData)user_data).isTerrain;
            }

            if(not_eligible)
                continue;

            Transform3D local_tx = ground[i].getTransform();
            local_tx.get(locationVector);

            Shape3D i_shape = (Shape3D)ground[i].getObject();

            // Get the user data, if the user data contains a height data
            // source use that to determine the terrain, otherwise pass it
            // through to the geometry intersection handling. Inside that
            // Also check to see what geometry is being used
            user_data = i_shape.getUserData();
            HeightDataSource hds = null;
            GeometryData geom_data = null;

            if(user_data instanceof UserSupplementData)
            {
                UserSupplementData usd = (UserSupplementData)user_data;

                if(usd.geometryData instanceof HeightDataSource)
                    hds = (HeightDataSource)usd.geometryData;
                else if(usd.geometryData instanceof GeometryData)
                    geom_data = (GeometryData)usd.geometryData;
            }
            else if(user_data instanceof HeightDataSource)
            {
                hds = (HeightDataSource)user_data;
            }
            else if(user_data instanceof GeometryData)
                geom_data = (GeometryData)user_data;

            if(hds != null)
            {
                intersectionPoint.x = locationVector.x;
                intersectionPoint.y = locationVector.y;
                intersectionPoint.z = hds.getHeight((float)locationVector.x,
                                                    (float)locationVector.y);
            }
            else
            {
                // Do we have geometry data to play with at the shape level?
                // If so, use that in preference to going down to the
                // individual geometry arrays of the shape.
                if(geom_data != null)
                {
                    if(terrainIntersect.rayUnknownGeometry(locationPoint,
                                                           downVector,
                                                           0,
                                                           geom_data,
                                                           local_tx,
                                                           wkPoint,
                                                           false))
                    {
                        diffVec.sub(locationPoint, wkPoint);

                        if((shortest_length == -1) ||
                           (diffVec.lengthSquared() < shortest_length))
                        {
                            shortest_length = diffVec.lengthSquared();
                            intersectionPoint.set(wkPoint);
                        }
                    }
                }
                else
                {
                    Enumeration geom_list = i_shape.getAllGeometries();

                    while(geom_list.hasMoreElements())
                    {
                        GeometryArray geom = (GeometryArray)geom_list.nextElement();

                        if(geom == null)
                            continue;

                        user_data = geom.getUserData();
                        geom_data = null;

                        if(user_data instanceof UserSupplementData)
                        {
                            UserSupplementData usd = (UserSupplementData)user_data;

                            if(usd.geometryData instanceof HeightDataSource)
                                hds = (HeightDataSource)usd.geometryData;
                            else if(usd.geometryData instanceof GeometryData)
                                geom_data = (GeometryData)usd.geometryData;
                        }
                        else if(user_data instanceof HeightDataSource)
                        {
                            hds = (HeightDataSource)user_data;
                        }
                        else if(user_data instanceof GeometryData)
                            geom_data = (GeometryData)user_data;


                        boolean intersect = false;

                        // Ah, finally. This is where we do the intersection
                        // testing against either the raw geometry or the object.
                        if(geom_data != null)
                        {
                            intersect =
                                terrainIntersect.rayUnknownGeometry(locationPoint,
                                                                  downVector,
                                                                  0,
                                                                  geom_data,
                                                                  local_tx,
                                                                  wkPoint,
                                                                  false);
                        }
                        else
                        {
                            intersect =
                                terrainIntersect.rayUnknownGeometry(locationPoint,
                                                                  downVector,
                                                                  0,
                                                                  geom,
                                                                  local_tx,
                                                                  wkPoint,
                                                                  false);
                        }

                        if(intersect)
                        {
                            diffVec.sub(locationPoint, wkPoint);

                            if((shortest_length == -1) ||
                               (diffVec.lengthSquared() < shortest_length))
                            {
                                shortest_length = diffVec.lengthSquared();
                                intersectionPoint.set(wkPoint);
                            }
                        }
                    }
                }
            }
        }

        // No intersection!!!! How did that happen? Well, just exit and
        // pretend there was nothing below us
        if(shortest_length != -1)
            lastTerrainHeight = (float)intersectionPoint.y;
    }

    /**
     * Compares to floats to determine if they are equal or very close
     *
     * @param val1 The first value to compare
     * @param val2 The second value to compare
     * @return True if they are equal within the given epsilon
     */
    private boolean floatEq(double val1, double val2)
    {
        double diff = val1 - val2;

        if (diff < 0)
            diff *= -1;

        return (diff < ZEROEPS);
    }
}
