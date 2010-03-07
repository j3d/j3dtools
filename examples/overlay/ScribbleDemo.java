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
import org.j3d.renderer.java3d.geom.Axis;
import org.j3d.renderer.java3d.geom.Box;
import org.j3d.renderer.java3d.overlay.ScribbleOverlay;
import org.j3d.renderer.java3d.overlay.OverlayManager;
import org.j3d.renderer.java3d.overlay.UpdateControlBehavior;

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ScribbleDemo extends DemoFrame
    implements ActionListener, ItemListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The overlay we're scribbling on */
    private ScribbleOverlay scribbler;

    /** Mapping of checkbox to the colour it represents */
    private HashMap colorMap;

    /** The checkbox group used to hold the color selectors */
    private CheckboxGroup cbGroup;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public ScribbleDemo()
    {
        super("Scribble test window");

        colorMap = new HashMap();

        Button button = new Button("Clear");
        button.addActionListener(this);

        add(button, BorderLayout.SOUTH);

        Panel p1 = new Panel(new GridLayout(5, 1));

        p1.add(new Label("Select Color"));

        cbGroup = new CheckboxGroup();

        Checkbox cb = new Checkbox("White", cbGroup, true);
        cb.addItemListener(this);
        colorMap.put(cb, Color.white);
        p1.add(cb);

        cb = new Checkbox("Green", cbGroup, false);
        cb.addItemListener(this);
        colorMap.put(cb, Color.green);
        p1.add(cb);

        cb = new Checkbox("Blue", cbGroup, false);
        cb.addItemListener(this);
        colorMap.put(cb, Color.blue);
        p1.add(cb);

        cb = new Checkbox("Yellow", cbGroup, false);
        cb.addItemListener(this);
        colorMap.put(cb, Color.yellow);
        p1.add(cb);

        Panel p2 = new Panel(new BorderLayout());
        p2.add(p1, BorderLayout.NORTH);

        add(p2, BorderLayout.EAST);

        buildScene();

        scribbler.initialize();
    }

    /**
     * Process the clear request from the button.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        scribbler.clear();
    }

    /**
     * Process the change of state request from the colour selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            Object src = evt.getSource();
            Color col = (Color)colorMap.get(src);
            scribbler.setLineColor(col);
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
        view_tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        view_tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
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
        Material material = new Material();
        material.setAmbientColor(ambientBlue);
        material.setDiffuseColor(blue);
        material.setSpecularColor(specular);
        material.setShininess(75.0f);
        material.setLightingEnable(true);

        Appearance blue_appearance = new Appearance();
        blue_appearance.setMaterial(material);

        Shape3D shape = new Box();
        shape.setAppearance(blue_appearance);
        world_object_group.addChild(shape);

        // And the axis...
        Axis axis = new Axis();
        world_object_group.addChild(axis);
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

        UpdateControlBehavior updater = new UpdateControlBehavior();
        updater.setSchedulingBounds(new BoundingSphere());
        view_tg.addChild(updater);

        scribbler = new ScribbleOverlay(canvas, null);
        scribbler.setVisible(true);

        OverlayManager mgr = new OverlayManager(canvas, updater);
        mgr.addOverlay(scribbler);

        view_tg.addChild(mgr);

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }

    public static void main(String[] argv)
    {
        ScribbleDemo demo = new ScribbleDemo();
        demo.setVisible(true);
    }
}
