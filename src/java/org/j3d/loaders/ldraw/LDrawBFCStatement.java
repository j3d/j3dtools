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
import javax.vecmath.Matrix4d;

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
     * @param col The colour to render in. Most not be null
     * @param ref The file reference to load
     * @param matrix THe transformation matrix to use.
     */
    public LDrawBFCStatement(boolean ccw, boolean cull)
    {
        super(null);

        this.ccw = ccw;
        this.cull = cull;
    }

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
