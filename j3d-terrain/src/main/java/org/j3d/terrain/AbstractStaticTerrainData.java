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
import java.awt.image.BufferedImage;

// Local imports
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
 * @version $Revision: 1.2 $
 */
public abstract class AbstractStaticTerrainData extends AbstractTerrainData
    implements StaticTerrainData
{
    /** The texture object that is loaded for this instance. */
    protected BufferedImage texture;

    /** The width of the terrain in grid points. (Y coordinate) */
    protected int gridWidth;

    /** The depth of the terrain in grid points. (X coordinate) */
    protected int gridDepth;

    /**
     * Create a new instance.of this data class with all flags set to false.
     */
    protected AbstractStaticTerrainData()
    {
        super(STATIC_DATA);
    }

    //----------------------------------------------------------
    // Methods required by TerrainData
    //----------------------------------------------------------

    /**
     * Fetch the BufferedImage that is used to cover the entire terrain. If no
     * texture is used, then return null. Assumes a single large texture for
     * the entire terrain.
     *
     * @return The texture instance to use or null
     */
    @Override
    public BufferedImage getTexture()
    {
        return texture;
    }

    /**
     * Get the width (number of points on the Y axis) of the grid.
     *
     * @return The number of points in the width if the grid
     */
    @Override
    public int getGridWidth()
    {
        return gridWidth;
    }

    /**
     * Get the depth (number of points on the X axis) of the grid.
     *
     * @return The number of points in the depth of the grid
     */
    @Override
    public int getGridDepth()
    {
        return gridWidth;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the texture object that can be used with this terrain instance.
     * Passing a value of null will remove the currently set texture.
     *
     * @param tex The texture instance to use or null to clear
     */
    public void setTexture(BufferedImage tex)
    {
        texture = tex;
    }

}
