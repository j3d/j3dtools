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
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

import com.sun.j3d.utils.image.TextureLoader;

// Local imports
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.GeometryData;
import org.j3d.renderer.java3d.navigation.MouseViewHandler;
import org.j3d.ui.navigation.NavigationState;

/**
 * Demonstration of a cubic environment map.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SphereMapDemo extends DemoFrame
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** The navigation information handler */
    private MouseViewHandler viewHandler;

    /** Attributes of the cube map so we can update the texture */
    private TextureAttributes cubeTexAttr;

    /** Transformation used to rotate the cube map */
    private Transform3D cubeTransform;

    /** Quaternion for transfering rotations */
    private Quat4d orientation;

    /** Appearance used by all */
    private Appearance appearance;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public SphereMapDemo()
    {
        super("SphereMap window");

        orientation = new Quat4d();

        viewHandler = new MouseViewHandler();
        viewHandler.setCanvas(canvas);

        viewHandler.setButtonNavigation(MouseEvent.BUTTON1_MASK,
                                        NavigationState.EXAMINE_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON2_MASK,
                                        NavigationState.TILT_STATE);
        viewHandler.setButtonNavigation(MouseEvent.BUTTON3_MASK,
                                        NavigationState.PAN_STATE);

        View view = buildScene();

        canvas.setBackground(Color.blue);
        view.addCanvas3D(canvas);

        add(canvas, BorderLayout.CENTER);
    }

    /**
     * Build the scenegraph for the canvas
     */
    private View buildScene()
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
        angle.setTranslation(new Vector3d(0, 0, 2));

        TransformGroup view_tg = new TransformGroup(angle);
        view_tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        view_tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        view_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        view_tg.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        view_tg.addChild(camera);

        view_group.addChild(view_tg);

        Point3d origin = new Point3d(0, 0, 0);
        BoundingSphere back_bounds =
            new BoundingSphere(origin, BACK_CLIP_DISTANCE);
        DirectionalLight headback = new DirectionalLight();
        headback.setColor(white);
        headback.setInfluencingBounds(back_bounds);
        view_tg.addChild(headback);

        // Now the geometry. Let's just add a couple of the basic primitives
        // for testing.
        Material material = new Material();
        material.setAmbientColor(ambientBlue);
        material.setDiffuseColor(blue);
        material.setSpecularColor(specular);
        material.setShininess(75.0f);
        material.setLightingEnable(true);


        TextureLoader sphere_tl =
            new TextureLoader("sphere_env_map.jpg", this);

        Texture sphere_tex = sphere_tl.getTexture();

        sphere_tex.setBoundaryModeS(Texture.CLAMP);
        sphere_tex.setBoundaryModeT(Texture.CLAMP);
        sphere_tex.setMinFilter(Texture.NICEST);
        sphere_tex.setMagFilter(Texture.NICEST);

        TexCoordGeneration coord_gen =
            new TexCoordGeneration(TexCoordGeneration.SPHERE_MAP,
                                   TexCoordGeneration.TEXTURE_COORDINATE_2);

        cubeTexAttr = new TextureAttributes();
        cubeTexAttr.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

        cubeTransform = new Transform3D();

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setTexture(sphere_tex);
        appearance.setTexCoordGeneration(coord_gen);
        appearance.setTextureAttributes(cubeTexAttr);

        appearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
        appearance.clearCapabilityIsFrequent(Appearance.ALLOW_TEXGEN_WRITE);

        SphereGenerator generator = new SphereGenerator(0.5f, 40);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;
        generator.generate(data);

        int format = QuadArray.COORDINATES | QuadArray.NORMALS;

        QuadArray geom = new QuadArray(data.vertexCount, format);
        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);

        Shape3D shape = new Shape3D(geom, appearance);
        world_object_group.addChild(shape);

        // Add them to the locale
        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.attachViewPlatform(camera);

        viewHandler.setViewInfo(view, view_tg);
        viewHandler.setNavigationSpeed(1);

        view_group.addChild(viewHandler.getTimerBehavior());

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);

        return view;
    }

    public static void main(String[] argv)
    {
        SphereMapDemo demo = new SphereMapDemo();
        demo.setVisible(true);
    }
}
