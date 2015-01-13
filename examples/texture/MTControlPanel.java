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
import javax.swing.*;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;

import javax.vecmath.Vector3d;

// Local imports
// None

/**
 * Panel used to control the visual features of the multitexturing.
 * <p>
 *
 * Note that because spinners are added, this requires JDK 1.4.
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MTControlPanel extends JPanel
    implements ItemListener, ChangeListener
{
    /** Selector for the bump map */
    private JCheckBox bumpSelector;

    /** Selector for the base texture color map */
    private JCheckBox colourSelector;

    /** Selector for the stencil map */
    private JCheckBox stencilSelector;

    /** Selector for the light map */
    private JCheckBox lightSelector;

    /** Texture corresponding to the bump map */
    private Texture bumpTexture;

    /** Texture corresponding to the base image */
    private Texture colourTexture;

    /** Texture corresponding to the stencil map */
    private Texture stencilTexture;

    /** Texture corresponding to the light map */
    private Texture lightTexture;

    /** Spinner for the ambient light angle */
    private SpinnerNumberModel ambientSpinner;

    /** Spinner for the light map rotation angle */
    private SpinnerNumberModel lightMapSpinner;

    /** Attributes of the ambient texture */
    private TextureAttributes ambientTexAttr;

    /** Attributes of the light texture */
    private TextureAttributes lightTexAttr;

    /** Attributes of the bump texture */
    private TextureAttributes bumpTexAttr;

    private Vector3d bumpLightDirection;
    private Vector3d tempVec;

    /** Transform for modifying the ambient transformation attributes */
    private Transform3D bumpTransform;

    /** Transform for modifying the light transformation attributes */
    private Transform3D lightTransform;

    /**
     * Construct a new panel that works with the 4 textures required. The
     * textures passed
     */
    public MTControlPanel(Texture bump,
                          Texture colour,
                          Texture stencil,
                          Texture light,
                          TextureAttributes bumpAttr,
                          TextureAttributes lightAttr)
    {
        super(new GridLayout(6, 1));

        bumpTexture = bump;
        colourTexture = colour;
        stencilTexture = stencil;
        lightTexture = light;
        bumpTexAttr = bumpAttr;
        lightTexAttr = lightAttr;

        tempVec = new Vector3d();
        bumpLightDirection = new Vector3d(0, -1, -1);
        bumpTransform = new Transform3D();

        lightTransform = new Transform3D();
        lightTexAttr.getTextureTransform(lightTransform);

        bumpSelector = new JCheckBox("Bump map", true);
        bumpSelector.addItemListener(this);
        add(bumpSelector);

        colourSelector = new JCheckBox("Colour texture", true);
        colourSelector.addItemListener(this);
        add(colourSelector);

        stencilSelector = new JCheckBox("Stencil map", true);
        stencilSelector.addItemListener(this);
        add(stencilSelector);

        lightSelector = new JCheckBox("Light map", true);
        lightSelector.addItemListener(this);
        add(lightSelector);

        ambientSpinner = new SpinnerNumberModel(0, 0, 360, 1);
        ambientSpinner.addChangeListener(this);

        JPanel p1 = new JPanel();
        p1.add(new JSpinner(ambientSpinner));
        p1.add(new JLabel("Ambient light angle"));
        add(p1);

        lightMapSpinner = new SpinnerNumberModel(0, 0, 360, 1);
        lightMapSpinner.addChangeListener(this);

        JPanel p2 = new JPanel();
        p2.add(new JSpinner(lightMapSpinner));
        p2.add(new JLabel("Light map angle"));
        add(p2);
    }

    /**
     * Process the change of state request from the colour selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        Object src = evt.getSource();
        int state = evt.getStateChange();

        if(src == bumpSelector)
        {
            bumpTexture.setEnable(state == ItemEvent.SELECTED);
        }
        else if(src == colourSelector)
        {
            colourTexture.setEnable(state == ItemEvent.SELECTED);
        }
        else if(src == stencilSelector)
        {
            stencilTexture.setEnable(state == ItemEvent.SELECTED);
        }
        else
        {
            // light selector.
            lightTexture.setEnable(state == ItemEvent.SELECTED);
        }
    }

    /**
     * Process the change of state of the spinner buttons.
     *
     * @param evt The event that caused this method to be called
     */
    public void stateChanged(ChangeEvent evt)
    {
        Object src = evt.getSource();

        if(src == ambientSpinner)
        {
            Number val = ambientSpinner.getNumber();
            double angle = val.doubleValue() * Math.PI / 180;

            bumpTransform.rotZ(angle);
            bumpTransform.transform(bumpLightDirection, tempVec);

            // Assign the blend color as the light vector. Remember that
            // texture colors are based on [0, 1], but vectors require a
            // [-1, 1].
            bumpTexAttr.setTextureBlendColor((float)(tempVec.x / 2 + 0.5),
                                             (float)(tempVec.y / 2 + 0.5),
                                             (float)(tempVec.z / 2 + 0.5),
                                             0);
        }
        else
        {
            // light map spinner.
            Number val = lightMapSpinner.getNumber();
            double angle = val.doubleValue() * Math.PI / 180;
            lightTransform.rotZ(angle);
            lightTexAttr.setTextureTransform(lightTransform);
        }
    }
}
