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
import javax.swing.*;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.media.j3d.Appearance;
import javax.media.j3d.Switch;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.TextureUnitState;

// Application Specific imports
// None

/**
 * Panel used to control the visual features of the multitexturing.
 * <p>
 *
 * Note that because spinners are added, this requires JDK 1.4.
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MapControlPanel extends JPanel
    implements ItemListener
{
    /** Selector for the Background visibility */
    private JCheckBox backgroundSelector;

    /** Selector for the front map */
    private JRadioButton normalSelector;

    /** Selector for the back map */
    private JRadioButton reflSelector;

    /** Switch for controlling the background */
    private Switch backgroundSwitch;

    /** Appearance for putting the texture coordinate generation */
    private Appearance appearance;

    /** TextureUnitState for putting the texture coordinate generation */
    private TextureUnitState textureUnit;

    /** Coord generation using normals */
    private TexCoordGeneration normalMapGen;

    /** Coord generation using reflections */
    private TexCoordGeneration reflMapGen;

    /**
     * Construct a new panel that works with the 6 textures and background
     * switch.
     */
    public MapControlPanel(Switch bgSwitch, Appearance app)
    {
        this(bgSwitch);

        appearance = app;
    }

    /**
     * Construct a new panel that works with the background switch and a
     * TextureUnitState to control the texcoord generation.
     */
    public MapControlPanel(Switch bgSwitch, TextureUnitState texUnit)
    {
        this(bgSwitch);

        textureUnit = texUnit;
    }

    /**
     * Common internal constructor for simplified setup.
     */
    private MapControlPanel(Switch bgSwitch)
    {
        super(new GridLayout(3, 1));

        backgroundSwitch = bgSwitch;

        ButtonGroup grouper = new ButtonGroup();

        backgroundSelector = new JCheckBox("Show background", true);
        backgroundSelector.addItemListener(this);
        add(backgroundSelector);

        normalSelector = new JRadioButton("Use Normal mapping", true);
        normalSelector.addItemListener(this);
        grouper.add(normalSelector);
        add(normalSelector);

        reflSelector = new JRadioButton("Use Reflection mapping", false);
        reflSelector.addItemListener(this);
        grouper.add(reflSelector);
        add(reflSelector);

        normalMapGen =
            new TexCoordGeneration(TexCoordGeneration.NORMAL_MAP,
                                   TexCoordGeneration.TEXTURE_COORDINATE_3);

        reflMapGen =
            new TexCoordGeneration(TexCoordGeneration.REFLECTION_MAP,
                                   TexCoordGeneration.TEXTURE_COORDINATE_3);
    }

    /**
     * Process the change of state request from the back selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        Object src = evt.getSource();
        boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);

        if(src == normalSelector)
        {
            if(selected)
            {
                if(appearance != null)
                    appearance.setTexCoordGeneration(normalMapGen);
                else
                    textureUnit.setTexCoordGeneration(normalMapGen);
            }
        }
        else if(src == reflSelector)
        {
            if(selected)
            {
                if(appearance != null)
                    appearance.setTexCoordGeneration(reflMapGen);
                else
                    textureUnit.setTexCoordGeneration(reflMapGen);
            }
        }
        else
        {
            int child = selected ? Switch.CHILD_ALL : Switch.CHILD_NONE;
            backgroundSwitch.setWhichChild(child);
        }
    }
}
