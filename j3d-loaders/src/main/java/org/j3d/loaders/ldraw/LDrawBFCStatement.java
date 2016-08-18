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

// Local parser
// None

/**
 * Statement that changes the BFC configuration
 * <p>
 *
 * The definition of the BFC statements can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawBFCStatement extends LDrawFilePart
{
    /** Flag indicating the winding direction. True for counter clockwise */
    private boolean ccw;

    /** Flag indicating if back face culling is on */
    private boolean cull;

    /**
     * Construct the base part that is rendered in the specific colour. The
     * class keeps a reference to the given matrix rather than a copy.
     *
     * @param ccw True if the triangles are declared in counter clockwise order
     * @param cull true to have backface culling on
     */
    public LDrawBFCStatement(boolean ccw, boolean cull)
    {
        this.ccw = ccw;
        this.cull = cull;
    }

    //------------------------------------------------------------------------
    // Methods defined by Object
    //------------------------------------------------------------------------

    @Override
    public String toString()
    {
        StringBuilder bldr = new StringBuilder("LDraw BFC Statement ");
        bldr.append(" Culling? ");
        bldr.append(cull ? 'Y' : 'N');
        bldr.append(" CCW ");
        bldr.append(ccw ? 'Y' : 'N');

        return bldr.toString();
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Get the winding direction flag.
     *
     * @return true if to use counter-clockwise triangles
     */
    public boolean isCCW()
    {
        return ccw;
    }

    /**
     * Get whether back faces are to be culled.
     *
     * @return true if to remove back faces of polygons
     */
    public boolean isCulled()
    {
        return cull;
    }
}
