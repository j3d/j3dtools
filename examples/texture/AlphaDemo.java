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

import java.awt.image.BufferedImage;

// Application Specific imports
// None

/**
 * Demonstration of the scribble overlay. Presents a box on screen and allows
 * the user to draw over it with a mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class AlphaDemo extends DemoFrame
    implements ActionListener
{
    private static final double BACK_CLIP_DISTANCE = 100.0;

    /** Full transparent colour for the background of the texture. */
//    private static final Color CLEAR_COLOR = new Color(0, 0, 0, 1f);
//    private static final Color CLEAR_COLOR = new Color(0, 0, 0, 1f);
    private static final Color CLEAR_COLOR = Color.black;

    /** Colour of the text. */
//    private static final Color TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
//    private static final Color TEXT_COLOR = new Color(0, 0, 0, 0);
    private static final Color TEXT_COLOR = Color.white;

    /** Button to demo RGB textures */
    private JButton rgbButton;

    /** Button to demo Alpha textures */
    private JButton alphaButton;

    /** Button to return object to no texture */
    private JButton noButton;

    /** The appearance we used to change the texture with */
    private Appearance appearance;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public AlphaDemo()
    {
        super("Alpha texture test window");

        add(canvas, BorderLayout.CENTER);

        buildScene();

        rgbButton = new JButton("RGB");
        rgbButton.addActionListener(this);

        alphaButton = new JButton("Alpha");
        alphaButton.addActionListener(this);

        noButton = new JButton("None");
        noButton.addActionListener(this);

        JPanel p1 = new JPanel(new FlowLayout());
        p1.add(rgbButton);
        p1.add(alphaButton);
        p1.add(noButton);

        add(p1, BorderLayout.SOUTH);
    }

    /**
     * Process the change of state request from the colour selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        Object src = evt.getSource();

        if(src == noButton) {
            appearance.setTexture(null);
        } else if(src == rgbButton) {
            buildRGBTexture();
        } else {
            buildAlphaTexture();
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

        TextureAttributes texAttr = new TextureAttributes();
//        texAttr.setTextureMode(TextureAttributes.REPLACE);

/**/
        texAttr.setTextureMode(TextureAttributes.COMBINE);
        texAttr.setCombineRgbMode(TextureAttributes.COMBINE_REPLACE);
        texAttr.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);

        texAttr.setCombineRgbSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);
        texAttr.setCombineAlphaSource(0, TextureAttributes.COMBINE_TEXTURE_COLOR);

        texAttr.setCombineRgbFunction(0, TextureAttributes.COMBINE_SRC_COLOR);
        texAttr.setCombineAlphaFunction(0, TextureAttributes.COMBINE_SRC_ALPHA);
/**/

        TransparencyAttributes transp = new TransparencyAttributes();
        transp.setTransparencyMode(TransparencyAttributes.BLENDED);

        appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        appearance.setTextureAttributes(texAttr);
        appearance.setTransparencyAttributes(transp);

        int format = TriangleStripArray.COORDINATES |
                     TriangleStripArray.TEXTURE_COORDINATE_2 |
                     TriangleStripArray.NORMALS ;

        float[] vertices = {
            -0.5f, -0.5f, 0,
             0.5f, -0.5f, 0,
            -0.5f,  0.5f, 0,
             0.5f,  0.5f, 0,
        };

        float[] tex_coords = {
            0, 0, 1, 0, 0, 1, 1, 1,
        };

        float[] normals = {
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
        };

        int[] strips =  { 4 };

        TriangleStripArray geom = new TriangleStripArray(4, format, strips);
        geom.setCoordinates(0, vertices);
        geom.setTextureCoordinates(0, 0, tex_coords);
        geom.setNormals(0, normals);

        Shape3D shape = new Shape3D(geom, appearance);

        world_object_group.addChild(shape);

        // Add them to the locale

        PhysicalBody body = new PhysicalBody();
        PhysicalEnvironment env = new PhysicalEnvironment();

        View view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.addCanvas3D(canvas);
        view.attachViewPlatform(camera);

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }

    /**
     * Build up the texture with pure alpha.
     */
    private void buildAlphaTexture() {

        BufferedImage bi = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY);

        drawOnImage(bi);

        ImageComponent2D img_comp =
            new ImageComponent2D(ImageComponent2D.FORMAT_CHANNEL8,
                                 bi,
                                 true,
                                 false);

        Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
                                          Texture.ALPHA,
                                          128,
                                          128);
        texture.setImage(0, img_comp);
        appearance.setTexture(texture);
    }

    private void buildRGBTexture() {
        BufferedImage bi = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY);

        drawOnImage(bi);

            ImageComponent2D img_comp =
                new ImageComponent2D(ImageComponent2D.FORMAT_CHANNEL8,
                                     bi,
                                     true,
                                     false);

        Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
                                          Texture.RGBA,
                                          128,
                                          128);
        texture.setImage(0, img_comp);
        appearance.setTexture(texture);
    }

    private void drawOnImage(BufferedImage img) {
        Graphics2D g = img.createGraphics();

        g.setColor(CLEAR_COLOR);
        g.fillRect(0, 0, 128, 128);
        g.setColor(TEXT_COLOR);

        g.fillRect(32, 32, 64, 64);

        g.setColor(CLEAR_COLOR);
        g.fillRect(48, 48, 32, 32);

        g.dispose();
    }

    public static void main(String[] argv)
    {
        AlphaDemo demo = new AlphaDemo();
        demo.setVisible(true);
    }
}
