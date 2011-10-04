/*****************************************************************************
 *                            (c) j3d.org 2002-2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
// None

// Local parser
// None

/**
 * Representation of a triangle part.
 * <p>
 * Triangles are assumed to be in CCW order.
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawTriangle extends LDrawRenderable
{
    /** First point in the triangle */
    private double[] p1;

    /**
     * Construct the line information from the two points and colour.
     *
     * @param col The colour to render in. Most not be null
     */
    protected LDrawTriangle(LDrawColor col, double[] start, double[] middle, double[] end)
    {
        super(col, start, end);

        p1 = middle;
    }

    /**
     * Get the coordinates of the second point (X, Y, Z)
     *
     * @return a reference to the internal position value
     */
    public double[] getMiddlePoint()
    {
        return p1;
    }
}
