/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
// None

// Local imports
// None

/**
 * Representation of a single light and its paramaters needed for rendering.
 * <p>
 *
 * Lights are described using the 0x4600 series of parameters. Not all of them
 * are mapped here currently. A light is considered to be a directional light
 * at all times, which may be narrowed into a spotlight.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class LightBlock
{
    /** Treat this as a directional light */
    public static final int DIRECTIONAL_LIGHT = 1;

    /** Treat this as a spot light light */
    public static final int SPOT_LIGHT = 2;

    /** Treat this as an ambient light */
    public static final int AMBIENT_LIGHT = 1;


    /** The light type to treat this as. */
    public int type;

    /** Is this light on or off right now? Default to on */
    public boolean enabled;

    /** The 3-component light colour value */
    public float[] color;

    /**
     * The location or direction of the light, depending on whether it is
     * a spotlight or directional light.
     */
    public float[] direction;

    /** If non-null, treat as a spotlight and this is the target location */
    public float[] target;

    /** The angle of effect for the spotlight */
    public float hotspotAngle;

    /** The falloff angle outside the hotspot till it reaches 0 */
    public float falloffAngle;

    /** The roll angle of the light. */
    public float rollAngle;

    /** An aspect ratio setting for the light, allowing for non-circular visuals */
    public float aspectRatio;

    /** The bias of the spotlight ray */
    public float bias;

    /** An inner range for the directional light */
    public float innerRange;

    /** An outer range for the directional light */
    public float outerRange;

    /** A multiplier value for the standard light settings */
    public float multiple;

    /** The (linear?) attentuation parameter of the light */
    public float attenuation;

    /** Flag indicating if a cone indicating visiblity should be shown */
    public boolean seeCone;

    /** Flag indicating that this light will cast shadows */
    public boolean castsShadows;

    /**
     * If shadows are being case, the parameters. [0] is local shadow low
     * bias. [1] is local shadow filter in the range 1 (low) to 10 (high).
     */
    public float[] shadowParams;

    /** If shadowed, the size of the map to create in pixels (square) */
    public int shadowMapSize;

    /**
     * Create a new light block and set it up for a basic directional light.
     */
    public LightBlock()
    {
        type = DIRECTIONAL_LIGHT;
        color = new float[3];
        direction = new float[3];
        aspectRatio = 1;
        enabled = true;
        seeCone = false;
        castsShadows = false;
    }
}
