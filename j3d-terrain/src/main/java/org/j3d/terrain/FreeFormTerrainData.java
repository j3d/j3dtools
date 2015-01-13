/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain;

// External imports
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Local imports
// none

/**
 * A source of terrain data that has no fixed requirements for its access.
 * <p>
 *
 * The source data provided by this dataset is assumed to contain a large
 * amount of some form of memory-managed data. The nanagement routines do
 * not really care about how the terrain is accessed. However, the
 * implementation can expect some form of locality of reference access to
 * the data. Terrain rendering algorithms don't randomly access the data,
 * so it is safe to assume that at least some form of caching can be done
 * and expect near neighbouts to be probably accessed very shortly.
 * <p>
 *
 * Textures are assumed to be tiled, but that is really up to the
 * implementation. If the implementation does not tile the textures then
 * for each call to <code>getTexture()</code> just return the same
 * instance each time. Also, make sure that the texture coordinates returned
 * from the various <code>getCoordinateX()</code> methods are all coorect
 * for the type of texture you are using.
 * <p>
 *
 * {@link TerrainData#getSourceDataType()} always returns
 * {@link TerrainData#FREEFORM_DATA}.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public interface FreeFormTerrainData extends TerrainData
{
    /**
     * Fetch the texture or part of a texture that can be applied to the
     * sub-region of the overall object. This is to allow for texture tiling
     * of very large texture images or terrain items. If there is no texture
     * or no texture for that region, then this should return null.
     * <p>
     *
     * If the texture object only covers parts of the bounds, that is fine.
     * Just return the full object and make sure the texture coordinates and
     * texture attributes are correctly set up.
     *
     * @param bounds The bounds of the region based on the grid positions
     * @return The texture object suitable for that bounds or null
     */
    public BufferedImage getTexture(Rectangle bounds);

    /**
     * Get the total width (number of points on the Y axis) of the grid.
     * Assumes the total max width is known.
     *
     * @return The number of points in the width if the grid
     */
    public int getGridWidth();

    /**
     * Get the total depth (number of points on the X axis) of the grid.
     * Assumes the total max width is known.
     *
     * @return The number of points in the depth of the grid
     */
    public int getGridDepth();
}
