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

import javax.vecmath.Color4b;
import javax.vecmath.Color3f;

import java.util.HashMap;

// Local imports
import org.j3d.geom.terrain.*;


/**
 * Demonstration of the height map converter that is purely 2D.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class HeightMapDemo extends DemoFrame
    implements ActionListener
{
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
    private ImageCanvas canvas;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public HeightMapDemo()
    {
        super("Height Map Converter");

        setBackground(SystemColor.menu);

        terrainGenerator =
            new FractalTerrainGenerator(30, 30, 10, true, 0, 4, 2, 0, null);

        imageConverter = new HeightImageCreator();

        createParamsPanel();

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

        canvas = new ImageCanvas();
        scroller = new ScrollPane();
        scroller.add(canvas);

        add(scroller, BorderLayout.CENTER);
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

        if(colorCheck.getState())
            canvas.setImage(imageConverter.createColorImage(terrain));
        else
            canvas.setImage(imageConverter.createGreyScaleImage(terrain));

        scroller.doLayout();
        canvas.repaint();
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

    public static void main(String[] argv)
    {
        HeightMapDemo demo = new HeightMapDemo();
        demo.setVisible(true);
    }
}
