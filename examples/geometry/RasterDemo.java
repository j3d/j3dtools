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

import java.util.HashMap;

// Application Specific imports
import org.j3d.geom.*;

import org.j3d.ui.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;

/**
 * Demonstration of the RasterTextLabel class in the org.j3d.geom package.
 * <p>
 *
 * There are 4 rasters placed in the scene - one at the origin of the axis,
 * then one each at the ends of the axis (for X, Y, Z).
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class RasterDemo extends DemoFrame
    implements ActionListener, ItemListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The main raster that we can change the text one */
    private RasterTextLabel mainLabel;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** Textfield holding the text to update */
    private TextField labelTf;

    /** Button for the update action */
    private Button update;

    /** Button for the crop action */
    private Button crop;

    /** Flag speaking of the current resizable state */
    private boolean resizable;


    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public RasterDemo()
    {
        super("Raster demo window");

        Panel p1 = new Panel(new BorderLayout());

        resizable = false;

        Checkbox cb = new Checkbox("resizable");
        cb.addItemListener(this);

        p1.add(cb, BorderLayout.WEST);

        labelTf = new TextField(30);
        p1.add(labelTf, BorderLayout.CENTER);

        Panel p2 = new Panel(new GridLayout(1, 2));

        update = new Button("Update");
        update.addActionListener(this);
        p2.add(update);

        crop = new Button("Crop");
        crop.addActionListener(this);
        p2.add(crop);

        p1.add(p2, BorderLayout.EAST);

        add(p1, BorderLayout.SOUTH);

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
     * Callback for the update button being pressed.
     *
     * @param evt The event that caused this method to be caused
     */
    public void actionPerformed(ActionEvent evt)
    {
        if(evt.getSource() == update)
        {
            String text = labelTf.getText();

            mainLabel.setText(text);
        }
        else
        {
            mainLabel.crop();
        }
    }

    /**
     * Item event means we've changed the status of the resizable flag
     *
     * @param evt The event that caused this method to be caused
     */
    public void itemStateChanged(ItemEvent evt)
    {
        resizable = !resizable;
        mainLabel.fixSize(resizable);
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

        // And the axis...
        Axis axis = new Axis();
        mainLabel = new RasterTextLabel("Raster Demo", Color.white, true, true);

        RasterTextLabel x_marker =
            new RasterTextLabel("X", Color.red, false, 2, 0, 0);

        RasterTextLabel y_marker =
            new RasterTextLabel("Y", Color.green, false, 0, 2, 0);

        RasterTextLabel z_marker =
            new RasterTextLabel("Z", Color.blue, false, 0, 0, 2);

        world_object_group.addChild(axis);
        world_object_group.addChild(mainLabel);
        world_object_group.addChild(x_marker);
        world_object_group.addChild(y_marker);
        world_object_group.addChild(z_marker);
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
        RasterDemo demo = new RasterDemo();
        demo.setVisible(true);
    }
}
