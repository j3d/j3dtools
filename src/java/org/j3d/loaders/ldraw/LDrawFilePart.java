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
 * Base representation of a part from the file that is not the header.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class LDrawFilePart
{
    /** The colour that this part is to be rendered in */
    private LDrawColor partColor;

    /**
     * Construct the base part that is rendered in the specific colour.
     *
     * @param col The colour to render in. Most not be null
     */
    protected LDrawFilePart(LDrawColor col)
    {
        assert col != null : "Null part colour not allowed";

        partColor = col;
    }

    /**
     * Get the colour that the part is rendered in.
     */
    public LDrawColor getColor()
    {
        return partColor;
    }
}
