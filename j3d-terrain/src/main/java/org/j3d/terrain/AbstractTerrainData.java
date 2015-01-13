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

// Local imports
// none

/**
 * An abstract representation of the base functionality for a TerrainData
 * implementation.
 * <p>
 *
 * This class provides a lot of the common functionality that all
 * implementations will require. Good to save you typing. Does not allow
 * you to directly extend this class. It is provided for basic common
 * functionality that is extended by the more derived versions.
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class AbstractTerrainData implements TerrainData
{
    /** Flag indicating if colour data is available from this dataset. */
    protected boolean colorAvailable;

    /** Flag indicating if texture data is available from this dataset. */
    protected boolean textureAvailable;

    /** The real world distance between each X (width) grid position */
    protected double gridStepX;

    /** The real world distance between each Y (depth) grid position */
    protected double gridStepY;

    /** The terrain data type constant */
    private final int terrainType;

    /**
     * Create a new instance.of this data class with all flags set to false.
     *
     * @param dataType The type of data that this instance represents
     */
    protected AbstractTerrainData(int dataType)
    {
        terrainType = dataType;

        colorAvailable = false;
        textureAvailable = false;
    }

    //----------------------------------------------------------
    // Methods required by TerrainData
    //----------------------------------------------------------

    /**
     * Get the type of terrain data that we are dealing with. One of the
     * constant types are returned. More efficient way of dealing with
     * multiple types than using <code>instanceof</code>
     *
     * @return The type of data that this instance represents
     */
    @Override
    public int getSourceDataType()
    {
        return terrainType;
    }

    /**
     * Check to see if this terrain data has per-vertex colours. Colour may
     * be used with or separately from the texture data. This allows you
     * to use colours to modulate the texture information if desired. Note that
     * if you provide per-vertex colour information here, you need to be
     * careful with the appearance information that gets set if you provide
     * a custom appearance generator implementation that comes with the
     * renderer-specific versions of this class.
     *
     * @return true If per-vertex color is available
     */
    @Override
    public boolean hasColor()
    {
        return colorAvailable;
    }

    /**
     * Check to see if this terrain data has any texturing at all - either
     * tiled or simple.
     *
     * @return true If a texture(s) is available
     */
    @Override
    public boolean hasTexture()
    {
        return textureAvailable;
    }

    /**
     * Get the real world distance between consecutive X values in the grid.
     *
     * @return The distance between each step of the grid
     */
    @Override
    public double getGridXStep()
    {
        return gridStepX;
    }

    /**
     * Get the real world distance between consecutive Y values in the grid.
     *
     * @return The distance between each step of the grid
     */
    @Override
    public double getGridYStep()
    {
        return gridStepY;
    }
}
