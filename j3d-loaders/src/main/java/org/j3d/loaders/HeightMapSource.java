/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders;

// External imports

// Local imports
// none

/**
 * A marker interface that describs a parser implementation that produces
 * heightfield information.
 * <p>
 *
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public interface HeightMapSource
{
    /**
     * Return the height map created for the last stream parsed. If no stream
     * has been parsed yet, this will return null. Height is relative to
     * sea-level which has a value of zero.
     *
     * @return The array of heights in [row][column] order or null
     */
    public abstract float[][] getHeights();

    /**
     * Fetch information about the real-world stepping sizes that this
     * grid uses. This array
     *
     * @return The stepping information for width[0] and depth [1]
     */
    public abstract float[] getGridStep();
}
