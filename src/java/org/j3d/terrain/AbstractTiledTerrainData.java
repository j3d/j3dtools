/*****************************************************************************
 *                             (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain;

// Standard imports
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.media.j3d.Texture;

// Application specific imports
// none

/**
 * An abstract representation of the base functionality for a TerrainData
 * implementation.
 * <p>
 *
 * This class provides a lot of the common functionality that all
 * implementations will require. Good to save you typing.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class AbstractTiledTerrainData extends AbstractTerrainData
    implements TiledTerrainData
{
    /** Reference to the currently set texture tile generator */
    protected TextureTileGenerator tileGenerator;

    /** The number of grid points per tile. */
    protected int gridPointsPerTile;

    /** Number of pixels for a tiled image in a grid step for the width */
    private int widthPixelsPerGridStep;

    /** Number of pixels for a tiled image in a grid step for the depth */
    private int depthPixelsPerGridStep;

    /** Work object to pass the pixel bound information to the tile generator */
    private Rectangle pixelBounds;

    /**
     * Create a new instance.of this data class with all flags set to false.
     */
    protected AbstractTiledTerrainData()
    {
        super(TILED_DATA);

        colorAvailable = false;
        textureAvailable = false;
    }

    //----------------------------------------------------------
    // Methods required by TerrainData
    //----------------------------------------------------------

    /**
     * Get the number of grid points along one side of a single tile.
     *
     * @return The number of points in the size if the grid
     */
    public int getTileSize()
    {
        return gridPointsPerTile;
    }

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
    public Texture getTexture(int tileX, int tileY)
    {
        // first convert tile to grid coords
        int x_grid = convertTileToGridCoord(tileX);
        int y_grid = convertTileToGridCoord(tileY);

        pixelBounds.x = x_grid * widthPixelsPerGridStep;
        pixelBounds.y = y_grid * depthPixelsPerGridStep;

        // width & height already set and don't change

        Texture ret_val = null;

        if(tileGenerator != null)
            ret_val = tileGenerator.getTextureTile(pixelBounds);

        return ret_val;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the texture tile geneattor instance to the new value. Setting a
     * value of null will clear the current instance. Calling this method
     * with a non-null generator instance does not automatically set the
     * has texture flag.
     *
     * @param gen The new generator instance to use
     */
    public void setTextureTileGenerator(TextureTileGenerator gen)
    {
        tileGenerator = gen;

        if(gen != null)
        {
            if(pixelBounds != null)
                pixelBounds = new Rectangle();

            // recalculate the conversion information.
            Dimension tex_size = gen.getTextureSize();

            widthPixelsPerGridStep = tex_size.width / gridPointsPerTile;
            depthPixelsPerGridStep = tex_size.height / gridPointsPerTile;

            pixelBounds.width = tex_size.width;
            pixelBounds.height = tex_size.height;
        }
    }

    /**
     * Convenience method to convert the tile coordinate to a grid coordinate
     *
     * @param tile The tile coordinate
     * @return The equivalent grid coordinate
     */
    protected int convertTileToGridCoord(int tile)
    {
        return tile * gridPointsPerTile;
    }

    /**
     * Convenience method to convert the grid coordinate to the tile that it
     * belongs in. If the point is exactly on the edge of a grid, the value
     * returned is for the larger tile coordinate.
     *
     * @param grid The grid coordinate
     * @return The equivalent grid coordinate
     */
    protected int convertGridToTileCoord(int grid)
    {
        return grid / gridPointsPerTile;
    }
}
