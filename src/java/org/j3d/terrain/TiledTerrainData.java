/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain;

// Standard imports
import java.awt.Rectangle;
import java.awt.image.BufferedImage;


// Application specific imports
// none

/**
 * Representatoin of data source that holds its data in tiles.
 * <p>
 *
 * A tiled source is used to represent large amounts of data, that is not
 * all held in memory at once. It may be held by the terrain data
 * representation but the terrain renderer only access pieces by tile.
 * <p>
 *
 * Because the terrain height is tiled, the assumption is that that the
 * textures are tiled too. A single image is too large to handle the entire
 * data set. The texture object will be queried for each patch that we are working
 * with.
 * <p>
 *
 * The implementation of this data set works on a modal-style basis. You can
 * ask the data for tile information so that the rendering will optimise for
 * that data access size. As part of this, it also works in an additional
 * coordinate system - tile coordinates. Tile have their own coordinate system
 * that describes tiles in their own X, Y system that overlays a point. Grid X
 * and Y are still relative to the overall terrain patch, we just use tile
 * coords to make it easier to make sure references to data areas use whole
 * tile information and not parts of them.
 *
 * {@link TerrainData#getSourceDataType()} always returns
 * {@link TerrainData#TILED_DATA}.
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public interface TiledTerrainData extends TerrainData
{
    /**
     * Fetch the texture or part of a texture that can be applied to the
     * sub-region of the overall object. This is to allow for texture tiling
     * of very large texture images or terrain items. If there is no texture
     * or no texture for that region, then this should return null.
     * <p>
     *
     * @param tileX the x coordinate of the tile number for the texture
     * @param tileY the y coordinate of the tile number for the texture
     * @return The texture object suitable for that bounds or null
     */
    public BufferedImage getTexture(int tileX, int tileY);

    /**
     * Get the number of grid points along one side of a single tile. Assumes
     * a tile is square. The value should be a power of two for efficiency of
     * the rendering algorithms.
     *
     * @return The number of points in the size if the grid
     */
    public int getTileSize();

    /**
     * Fetch the area, in tile coordinates of the area that is currently
     * available in memory. This is a hint to the caller that it would be
     * a good strategy to start with these tiles for fastest load time.
     * <p>
     *
     * The implementation should fill in the passed rectangle with the bounds
     * information. The bounds are inclusive, so if the implementation of
     * this interface provides the value of {0, 0, 2, 2} this will indicate
     * a 3x3 area is available.
     *
     * @param bounds The bounds of the available data, to be filled in by the
     *    implementation of this class
     */
    public void getTilesAvailableBounds(Rectangle bounds);

    /**
     * Set the bounding information, in number of tiles, of the area that the
     * terrain rendering code will be limiting its access to. This is a
     * directive by the terrain rendering code that it will be using all the
     * tiles contained within this boarder area within the next frame so the
     * implementation class should start to load that data now. The
     * implementation is free to load more than this aree into memory if it
     * feels like it.
     * <p>
     *
     * The values passes are inclusive tile values. A bounds of {0, 0, 1, 1}
     * would be a 2x2 set of tiles.
     *
     * @param bounds The bounds of the area, in tile coordinates, that will be
     *    accessed
     */
    public void setActiveBounds(Rectangle bounds);
}
