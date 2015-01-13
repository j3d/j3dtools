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
 * Data holder class for coefficients used in processing quads.
 * <P>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class QuadCoefficients
{
    /** Center coefficient value */
    float center;

    /** Coefficients in the edge, in order */
    float[] edge;

    /** Coefficients in the faces, in order */
    float[] face;

    /**
     * Construct a new default instance of this class.
     *
     * @param size The number of items to allocate for the arrays
     * @param edgePlus true if edge.size should be size + 1
     */
    QuadCoefficients(int size, boolean edgePlus)
    {
        edge = new float[size + (edgePlus ? 1 : 0)];
        face = new float[size];
    }
}
