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

package org.j3d.ui.navigation;

// Standard imports
import javax.media.j3d.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.Timer;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// Application specific imports
import org.j3d.geom.IntersectionUtils;

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
 * does not recognize the use of the Shift key as a modifier to produce an * accelarated movement.
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
 * The handler does not currently implement the Walk mode as it requires
 * picking handling for gravity and collision detection.
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
 *
 * <b>Collision Detection</b>
 * <p>
 *
 *
 * <b>TODO</b>
 * <p>
 * The collision vector does not move according to the direction that we are
 * travelling rather than the direction we are facing. Allows us to walk
 * backwards through objects when we shouldn't.
 *
 * @author per-frame movement algorithms by
 *   <a href="http://www.ife.org/vr/">Halden VR Centre, Institute for Energy Technology</a><br>
 *   Terrain/Collision implementation by Justin Couch
 * @version $Revision $
 */
public class NavigationHandler
    implements ActionListener,
               NavigationStateListener
{
    /** The default height of the avatar */
    private static final float DEFAULT_AVATAR_HEIGHT = 1.8f;

    /*** The default size of the avatar for collisions */
    private static final float DEFAULT_AVATAR_SIZE = 0.25f;

    /** The default step height of the avatar to climb */
    private static final float DEFAULT_STEP_HEIGHT = 0.4f;

    /** Fixed vector always pointing down -Y */
    private static final Vector3d Y_DOWN = new Vector3d(0, -1, 0);

    /** Fixed vector always pointing along -Z */
    private static final Vector3d COLLISION_DIRECTION = new Vector3d(0, 0, -1);


    /** Intersection utilities used for terrain following */
    private IntersectionUtils terrainIntersect;

    /** Intersection utilities used for terrain following */
    private IntersectionUtils collideIntersect;


    /** Timer used to control smooth motion of the mouse */
    private Timer timer;

    /** The view that we are moving about. */
    private View view;

    /** The transform group above the view that is being moved each frame */
    private TransformGroup viewTg = new TransformGroup();

    /** The transform that belongs to the view transform group */
    private Transform3D viewTx = new Transform3D();

    /** An observer for information about updates for this transition */
    private FrameUpdateListener updateListener;

    /** An observer for navigation state change information */
    private NavigationStateListener navigationListener;

    /** An observer for collision information */
    private CollisionListener collisionListener;

    /**
     * The current navigation state either set from us or externally as
     * the mouse if being dragged around. This is different to the state
     * that a given mouse button will generate
     */
    private int navigationState;

    /** The previous state so we can set it back to normal */
    private int previousState;

    /** The navigation state for use with button 1 */
    private int buttonOneState;

    /** The navigation state for use with button 2 */
    private int buttonTwoState;

    /** The navigation state for use with button 3 */
    private int buttonThreeState;

    /**
     * Flag indicating that we are currently doing something and should
     * ignore any current mouse presses.
     */
    private boolean movementInProgress;

    /** The mouse button that is currently being pressed */
    private int activeButton;

    /** The current movement speed in m/s in the local coordinate system */
    private float speed;

    // Java3D stuff for terrain following and collision detection

    /** The branchgroup to do the terrain picking on */
    private BranchGroup terrain;

    /** The branchgroup to do collision picking on */
    private BranchGroup collidables;

    /** Pick shape for terrain following */
    private PickRay terrainPicker;

    /** Pick shape for collision detection */
    private PickSegment collisionPicker;

    /** The local down direction for the terrain picking */
    private Vector3d downVector;

    /** The vector along which we do collision detection */
    private Vector3d collisionVector;

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

    /** A point that we use for working calculations (coord transforms) */
    private Point3d wkPoint;

    /**
     * Vector for doing difference calculations on the point we have and the next
     * while doing terrian following.
     */
    private Vector3d diffVec;

    /** The intersection point that we really collided with */
    private Point3d intersectionPoint;

    // The variables from here down are working variables during the drag
    // process. We declare them as class scope so that we don't generate
    // garbage for every mouse movement. The idea is we just re-use these
    // rather than create and destroy each time.

    /** The translation amount set by the last change in drag value */
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
    private double mouseRotationY;

    /** The amount to move the view in mouse coords left/right */
    private double mouseRotationX;

    /** The position where the mouse started it's last press */
    private Point2d startMousePos;

    /** The latest position of the mouse from the last event */
    private Point2d latestMousePos;

    /**
     * The difference between the last mouse point from the last event and
     * where it started.
     */
    private Point2d mouseDifference;

    /** Flag to indicate that we should be doing collisions this time */
    private boolean allowCollisions;

    /** Flag to indicate that we should do terrain following this time */
    private boolean allowTerrainFollowing;

    /**
     * Create a new mouse handler with no view information set. This
     * handler will not do anything until the view transform
     * references have been set and the navigation modes for at least one
     * mouse button.
     */
    public NavigationHandler()
    {
        navigationState = NO_STATE;
        previousState = NO_STATE;
        buttonOneState = NO_STATE;
        buttonTwoState = NO_STATE;
        buttonThreeState = NO_STATE;
        movementInProgress = false;

        //  Timer:
        timer = new Timer(1000, this);
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        timer.stop();
        timer.setLogTimers(false);
        timer.setCoalesce(false);

        terrainIntersect = new IntersectionUtils();
        collideIntersect = new IntersectionUtils();

        worldEyeTransform = new Transform3D();
        downVector = new Vector3d();
        terrainPicker = new PickRay();

        collisionVector = new Vector3d();
        collisionPicker = new PickSegment();
        intersectionPoint = new Point3d();
        wkPoint = new Point3d();
        diffVec = new Vector3d();


        locationVector = new Vector3d();
        locationPoint = new Point3d();
        locationEndPoint = new Point3d();

        dragTranslationAmt = new Vector3d();
        oneFrameTranslation = new Vector3d();
        oneFrameRotation = new Transform3D();
        viewTranslation = new Vector3d();
        mouseRotationY = 0;
        mouseRotationX = 0;
        startMousePos = new Point2d();
        latestMousePos = new Point2d();
        mouseDifference = new Point2d();

        allowCollisions = false;
        allowTerrainFollowing = false;


        avatarHeight = DEFAULT_AVATAR_HEIGHT;
        avatarSize = DEFAULT_AVATAR_SIZE;
        avatarStep = DEFAULT_STEP_HEIGHT;
        lastTerrainHeight = 0;
        speed = 0;
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
        if(((view != null) && (tg == null)) ||
           ((view == null) && (tg != null)))
            throw new IllegalArgumentException("View or TG is null when " +
                                               "the other isn't");

        this.view = view;
        this.viewTg = tg;

        if(tg == null)
            return;

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
     * Set the ability to use a given state within the handler for a
     * specific mouse button (up to 3). This allows the caller to control
     * exactly what states are allowed to be used and with which buttons.
     * Note that it is quite legal to set all three buttons to the same
     * navigation state
     *
     * @param button The mouse button value from
     *    {@link java.awt.event.MouseEvent}
     * @param state The navigation state to use for that button
     */
    public void setButtonNavigation(int button, int state)
    {
        switch(button)
        {
            case MouseEvent.BUTTON1_MASK:
                buttonOneState = state;
                break;

            case MouseEvent.BUTTON2_MASK:
                buttonTwoState = state;
                break;

            case MouseEvent.BUTTON3_MASK:
                buttonThreeState = state;
                break;
        }
    }

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
     * Set the listener for navigation state change notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setNavigationStateListener(NavigationStateListener l)
    {
        navigationListener = l;
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

    //----------------------------------------------------------
    // Methods required by the NavigationStateListener
    //----------------------------------------------------------

    /**
     * Notification that the panning state has changed to the new state.
     *
     * @param state One of the state values declared here
     */
    public void setNavigationState(int state)
    {
        navigationState = state;
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

    //----------------------------------------------------------
    // Methods required by the MouseMotionListener
    //----------------------------------------------------------

    /**
     * Process a mouse drag event to change the current movement value from
     * the previously set value to the new value
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt)
    {
        if(viewTg == null)
            return;

        latestMousePos.set(evt.getX(), evt.getY());
        mouseDifference.sub(startMousePos, latestMousePos);

        switch(navigationState)
        {
            case FLY_STATE:
                //  Translate on Z:
                dragTranslationAmt.set(0,0,-mouseDifference.y * speed);

                //  Rotate around Y:
                mouseRotationY = mouseDifference.x;

                allowCollisions = (collidables != null);
                allowTerrainFollowing = false;
                break;

            case PAN_STATE:
                //  Translate on X,Y:
                dragTranslationAmt.set(-mouseDifference.x * 2,
                                       mouseDifference.y * 2,
                                       0);

                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case TILT_STATE:
                //  Rotate arround X,Y:
                mouseRotationX = mouseDifference.y;
                mouseRotationY = mouseDifference.x;
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case WALK_STATE:
                //  Translate on Z only
                dragTranslationAmt.set(0,0,-mouseDifference.y * speed);

                //  Rotate around Y:
                mouseRotationY = mouseDifference.x;

                // do nothing
                allowCollisions = (collidables != null);
                allowTerrainFollowing = (terrain != null);
                break;

            case EXAMINE_STATE:
                // do nothing
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;

            case NO_STATE:
                // do nothing
                allowCollisions = false;
                allowTerrainFollowing = false;
                break;
        }
    }

    /**
     * Process a mouse press and set the timer going. This will cause the
     * navigation state to be set depending on the mouse button pressed.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        if(movementInProgress || (viewTg == null))
            return;

        int button = evt.getModifiers();

        previousState = navigationState;
        activeButton = (button & (int)MouseEvent.MOUSE_EVENT_MASK);

        // Set the cursor:
        if((button & MouseEvent.BUTTON1_MASK) != 0)
            navigationState = buttonOneState;
        else if((button & MouseEvent.BUTTON2_MASK) != 0)
            navigationState = buttonTwoState;
        else if((button & MouseEvent.BUTTON3_MASK) != 0)
            navigationState = buttonThreeState;

        if(navigationListener != null)
            navigationListener.setNavigationState(navigationState);

        if(navigationState == NO_STATE)
            return;

        viewTg.getTransform(viewTx);
        viewTx.get(viewTranslation);
        startMousePos.set(evt.getX(), evt.getY());
        timer.start();

        if(navigationState == WALK_STATE)
            setInitialTerrainHeight();
    }

    /**
     * Process a mouse release to return all the values back to normal. This
     * places all of the transforms back to identity and sets it as though the
     * nothing had happened.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
        int button = evt.getModifiers();

        // Ignore this if the released button is not the one doing the
        // work.
        if((viewTg == null) ||
           (movementInProgress &&
           ((button & MouseEvent.MOUSE_EVENT_MASK) != activeButton)))
            return;

        movementInProgress = false;
        allowCollisions = false;
        allowTerrainFollowing = false;

        // There's a potential problem here of dealing with the user
        // clicking button a, then also clicking button b, then releasing
        // a followed by b. Both of the release events will trigger this a
        // set the view transform, which we don't really want to do.

        timer.stop();
        viewTx.normalize();

        mouseRotationY = 0;
        mouseRotationX = 0;
        oneFrameRotation.setIdentity();
        oneFrameRotation.setIdentity();
        dragTranslationAmt.scale(0);

        viewTg.getTransform(viewTx);

        navigationState = previousState;

        if(navigationListener != null)
            navigationListener.setNavigationState(previousState);
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener
    //----------------------------------------------------------

    /**
     * Process an action event from the timer. This event is only for the time
     * and should not be associated with any other sort of action event like
     * menu callbacks.
     *
     * @param evt The event that caused this action to be called
     */
    public void actionPerformed(ActionEvent actionEvent)
    {
        // Some magic numbers here that I don't know where they came from.
        int frameDelay = 10 + (int)(view.getLastFrameDuration() / 2.0);
        double motionDelay = 0.000005 * frameDelay;

        timer.setDelay(frameDelay);

        // Firstly calculate where they should be in the next frame. Rotations
        // are fine to pass through as they never cause a collision. The
        // transforms we have to worry about so don't apply those until we've
        // checked for a collision.

        //  RotateX:
        oneFrameRotation.rotX(mouseRotationX * motionDelay);
        viewTx.mul(oneFrameRotation);

        //  RotateY:
        oneFrameRotation.rotY(mouseRotationY * motionDelay);
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
                collisionListener.avatarCollision();

            oneFrameTranslation.z = 0;
        }

        // Now set the translation amounts that have been adjusted by any
        // collisions.
        viewTranslation.add(oneFrameTranslation);
        viewTx.setTranslation(viewTranslation);

        try
        {
            viewTg.setTransform(viewTx);
        }
        catch(Exception e)
        {
            //check for bad transform:
            System.out.println("Transformgroup set invalid");
            viewTx.rotX(0);
            viewTg.setTransform(viewTx);
            // e.printStackTrace();
        }

        if(updateListener != null)
            updateListener.viewerPositionUpdated(viewTx);
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

        viewTg.getLocalToVworld(worldEyeTransform);
        worldEyeTransform.mul(viewTx);

        worldEyeTransform.get(locationVector);
        worldEyeTransform.transform(Y_DOWN, downVector);

        locationPoint.add(locationVector, oneFrameTranslation);
        terrainPicker.set(locationPoint, downVector);

        SceneGraphPath[] ground = terrain.pickAllSorted(terrainPicker);

        // if there is no ground below us, do nothing.
        if((ground == null) || (ground.length == 0))
        {
            return ret_val;
        }

        double shortest_length = -1;

        for(int i = 0; i < ground.length; i++)
        {
            Transform3D local_tx = ground[i].getTransform();
            local_tx.get(locationVector);

            Shape3D i_shape = (Shape3D)ground[i].getObject();

            Enumeration geom_list = i_shape.getAllGeometries();

            while(geom_list.hasMoreElements())
            {
                GeometryArray geom = (GeometryArray)geom_list.nextElement();

                if(geom == null)
                    continue;

                if(terrainIntersect.rayUnknownGeometry(locationPoint,
                                                  downVector,
                                                  0,
                                                  geom,
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
        if(height_above_terrain != avatarHeight)
        {
            if(terrain_step == 0)
            {
                // Flat surface. Check to see the avatar height is correct
                oneFrameTranslation.y = avatarHeight - height_above_terrain;
            }
            else if(terrain_step < avatarStep)
            {
                oneFrameTranslation.y += terrain_step;
                ret_val = true;
            }
            else
            {
                // prevent it. Set the transform to 0.
                ret_val = false;
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

        viewTg.getLocalToVworld(worldEyeTransform);
        worldEyeTransform.mul(viewTx);

        // Where are we now?
        worldEyeTransform.get(locationVector);
        locationPoint.set(locationVector);

        // Where are we going to be soon?
        worldEyeTransform.transform(COLLISION_DIRECTION, collisionVector);

        collisionVector.scale(avatarSize);

        locationEndPoint.add(locationVector, collisionVector);
        locationEndPoint.add(oneFrameTranslation);

        // We need to transform the end point to the direction that we are
        // currently travelling. At the moment, this always points forward
        // in the same direction as the viewpoint.
        collisionPicker.set(locationPoint, locationEndPoint);

        SceneGraphPath[] closest = collidables.pickAllSorted(collisionPicker);

        if((closest == null) || (closest.length == 0))
            return true;

        boolean real_collision = false;
        float length = (float)collisionVector.length();


        for(int i = 0; (i < closest.length) && !real_collision; i++)
        {
            // OK, so we collided on the bounds, lets check on the geometry
            // directly to see if we had a real collision. Java3D just gives
            // us the collision based on the bounding box intersection. We
            // might actually have just walked through something like an
            // archway.
            Transform3D local_tx = closest[i].getTransform();

            Shape3D i_shape = (Shape3D)closest[i].getObject();

            Enumeration geom_list = i_shape.getAllGeometries();

            while(geom_list.hasMoreElements() && !real_collision)
            {
                GeometryArray geom = (GeometryArray)geom_list.nextElement();

                if(geom == null)
                    continue;

                real_collision =
                    collideIntersect.rayUnknownGeometry(locationPoint,
                                                   collisionVector,
                                                   length,
                                                   geom,
                                                   local_tx,
                                                   wkPoint,
                                                   true);
            }

            ret_val = !real_collision;
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
            Transform3D local_tx = ground[i].getTransform();
            local_tx.get(locationVector);

            Shape3D i_shape = (Shape3D)ground[i].getObject();

            Enumeration geom_list = i_shape.getAllGeometries();

            while(geom_list.hasMoreElements())
            {
                GeometryArray geom = (GeometryArray)geom_list.nextElement();

                if(geom == null)
                    continue;

                if(terrainIntersect.rayUnknownGeometry(locationPoint,
                                                  downVector,
                                                  0,
                                                  geom,
                                                  local_tx,
                                                  wkPoint,
                                                  false))
                {
                    diffVec.sub(locationPoint, wkPoint);

                    if((shortest_length == -1) ||
                       (diffVec.length() < shortest_length))
                    {
                        shortest_length = diffVec.length();
                        intersectionPoint.set(wkPoint);
                    }
                }
            }
        }

        // No intersection!!!! How did that happen? Well, just exit and
        // pretend there was nothing below us
        if(shortest_length != -1)
            lastTerrainHeight = (float)intersectionPoint.y;
    }
}
