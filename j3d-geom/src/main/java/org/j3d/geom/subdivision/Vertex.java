/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// Standard imports
// None

// Application specific imports
// None

/**
 * Internal representation of a vertex data structure.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class Vertex
{
    /** Flag to say this vertex is part of a special edge type */
    boolean isSpecial;

    /** The depth into the recursion this vertex was created at */
    int creationDepth;

    /** The current depth that it has been subdivided to */
    int currentDepth;

    /** Coordinates of the vertex on the limit surface */
    float[] limitPosition;

    /** List of positions of this vertex at the various depths */
    float[][] position;

    /** Primary normal of the vertex. Null if not set */
    float[] normal1;

    /** Secondary normal when used as a crease. Null if not set */
    float[] normal2;

    /**
     * Create a new instance of this vertex. It is not special.
     *
     * @param td The total depth of the system (subdivision levels)
     * @param cd The creation depth of this vertex
     */
    Vertex(int td, int cd)
    {
        isSpecial = false;

        creationDepth = cd;
        position = new float[td][3];
    }

    /**
     * Convenience method to the get the position value at subdivision
     * depth d.
     */
    float[] getPos(int d)
    {
        return position[d - creationDepth];
    }

    /**
     * Set the position at the given depth to the new value.
     */
    void setPosition(int d, float[] val)
    {
        position[d][0] = val[0];
        position[d][1] = val[1];
        position[d][2] = val[2];
    }

    /**
     * Set the position at the given depth to the new value.
     */
    void setPosition(int d, float x, float y, float z)
    {
        position[d][0] = x;
        position[d][1] = y;
        position[d][2] = z;
    }

    /**
     * Set the current depth to the new value.
     *
     * @param d The new depth value
     */
    void setDepth(int d)
    {
        currentDepth = d;
    }

    /**
     * Convenience method to allow setting either of the normal values. A null
     * value is used to clear the normal.
     *
     * @param n The normal value to set or null
     * @param pri true if this the primary normal or the secondary
     */
    void setNormal(float[] n, boolean pri)
    {
        if(pri)
        {
            if(n == null)
                normal1 = null;
            else
            {
                if(normal1 == null)
                    normal1 = new float[3];

                normal1[0] = n[0];
                normal1[1] = n[1];
                normal1[2] = n[2];
            }
        }
        else
        {
            if(n == null)
                normal2 = null;
            else
            {
                if(normal2 == null)
                    normal2 = new float[3];

                normal2[0] = n[0];
                normal2[1] = n[1];
                normal2[2] = n[2];
            }
        }
    }
}
