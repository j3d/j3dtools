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
 * A single color data from the keyframe tracking.
 * <p>
 *
 * Colours are all transformed to [0-1] floating point values.
 * <p>
 *
 * The data represented is
 * <pre>
 *     struct {
 *        short framenum
 *        int unknown
 *        float red
 *        float green
 *        float blue
 *     }
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class ColorData extends TrackData
{
    /** The red component value */
    public float red;

    /** The green component value */
    public float green;

    /** The blue component value */
    public float blue;
}
