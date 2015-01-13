/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// External imports
// None

// Local imports
// None

/**
 * Internal representation of a sector data structure for piecewise smooth
 * subdivision using the Beirmann et al algorithm.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class Sector
{
    /** The sector type is untagged */
    public static final int UNTAGGED_SECTOR = 0;

    /** The sector type is convex */
    public static final int CONVEX_SECTOR = 1;

    /** The sector type is concave */
    public static final int CONCAVE_SECTOR = 2;

    /** The index of the vertex in the above face */
    int vertexIndex;

    /** Type of sector describing flat, concave or convex */
    int tag;

    /**
     * Value controlling the shape of the surface [0, 1]. Reasonable values
     * are 0.5 at concave corners, 0 elsewhere.
     */
    float flatness;

    /**
     * Angle used to describe corner vertices. (0, pi) for convex corners,
     * (pi, 2pi) for concave corners. Reasonable choices are pi/2 and 3pi/2
     * respectively.
     */
    float theta;

    /** The limit normal direction required. null if no normal */
    float[] normal;

    /**
     * How face the faces converge to the proscribed normal. [0, 1].
     * -1 if not set
     */
    float normalT;

    /**
     * Convenience method to set the normal. A value of null will remove the
     * currently set value.
     *
     */
    void setNormal(float[] n)
    {
        if(n == null)
            normal = null;
        else
        {
            if(normal == null)
                normal = new float[3];

            normal[0] = n[0];
            normal[1] = n[1];
            normal[2] = n[2];
        }
    }
}
