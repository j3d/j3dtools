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
 * Common information that all tracks keep with them.
 * <p>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class TrackData
{
    /** The frame number of this item of data */
    public int frameNumber;

    /** keys for working with the spline information. See spec for more info. */
    public int splineFlags;

    /** Data that is set if splineFlags is non-zero. Array is length 5. */
    public float[] splineData;
}
