/*****************************************************************************
 *                             (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain;

// External imports
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Local imports
// none

/**
 * Abstract representation of free-form terrain data.
 * <p>
 *
 * This implementation provides the ability to also use an external providers
 * of texture information.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class AbstractFreeFormTerrainData extends AbstractTerrainData
    implements FreeFormTerrainData
{
    /** Reference to the currently set texture tile generator */
    protected TextureTileGenerator tileGenerator;

    /** The number of grid points per tile in width*/
    protected int gridWidth;

    /** The number of grid points per tile in depth*/
    protected int gridDepth;

    /** Number of pixels for a tiled image in a grid step for the width */
    private int widthPixelsPerGridStep;

    /** Number of pixels for a tiled image in a grid step for the depth */
    private int depthPixelsPerGridStep;

    /** Work object to pass the pixel bound information to the tile generator */
    private Rectangle pixelBounds;

    /**
     * Create a new instance.of this data class with all flags set to false.
     */
    protected AbstractFreeFormTerrainData()
    {
        super(FREEFORM_DATA);
    }

    //----------------------------------------------------------
    // Methods required by TerrainData
    //----------------------------------------------------------

    /**
     * Get the total width (number of points on the Y axis) of the grid.
     * Assumes the total max width is known.
     *
     * @return The number of points in the width if the grid
     */
    public int getGridWidth()
    {
        return gridWidth;
    }

    /**
     * Get the total depth (number of points on the X axis) of the grid.
     * Assumes the total max width is known.
     *
     * @return The number of points in the depth of the grid
     */
    public int getGridDepth()
    {
        return gridDepth;
    }

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
    public BufferedImage getTexture(Rectangle bounds)
    {
        pixelBounds.x = bounds.x * widthPixelsPerGridStep;
        pixelBounds.y = bounds.y * depthPixelsPerGridStep;

        pixelBounds.width = bounds.width * widthPixelsPerGridStep;
        pixelBounds.height = bounds.height * depthPixelsPerGridStep;

        BufferedImage ret_val = null;

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

            widthPixelsPerGridStep = tex_size.width / gridWidth;
            depthPixelsPerGridStep = tex_size.height / gridDepth;
        }
    }
}
