/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders;

// Standard imports
import java.awt.Rectangle;

import javax.media.j3d.Texture;

import javax.vecmath.Point2d;

// Application specific imports
import org.j3d.terrain.TerrainData;

/**
 * Generalised implementation of the {@link TerrainData} for any file format
 * or loader that supports a grid based data structure.
 * <p>
 *
 * The basic implementation here does not support a texture. If an application
 * wishes to use a texture, they should extend this class and override the
 * {@link #getTexture()} method. If you wish to provide a pre-loaded texture,
 * then you can use the {@link #setTexture()} method of this class to place
 * one here.
 * <p>
 *
 * Tiled textures are not supported and requests for this just return the
 * entire texture.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class HeightMapTerrainData implements TerrainData
{
    // Global Implementation Note:
    // There is a lot of mixing of single and double precision floating point
    // data here. Internally, Java3D turns everything into single precision,
    // and as this is concerned with the rendering rather than representation
    // precision, we favour single precision calculations. The advantage here
    // is that we can use single FP parts of the CPU rather than needing to
    // use the more expensive double precision versions of the same
    // calculations. For this reason, you will see in the code that anywhere
    // we clash with single and double precision, we attempt to force the
    // values to single precision as early as possible rather than letting
    // everything bubble up to the greatest precision and then casting the
    // final result back to single precision floats.

    /** The height values */
    private float[][] heightMap;

    /** The real world distance between each grid position */
    private Point2d gridStep;

    /** The texture to supply to the user of this class */
    private Texture texture = null;

    /** Flag indicating if texCoords should assume tiling */
    private boolean hasTiledTextures;

    /** The number of points in the width */
    private final int gridWidth;

    /** The number of points in the depth */
    private final int gridDepth;

    /**
     * Create a new instance that sources the data from the given loader.
     * It assumes that the loader has already loaded the data from the
     * underlying source.
     *
     * @param loader The loader to source the data from
     */
    public HeightMapTerrainData(HeightMapLoader loader)
    {
        heightMap = loader.getHeights();
        gridStep = loader.getGridStep();

        hasTiledTextures = false;

        gridDepth = heightMap.length;
        gridWidth = heightMap[0].length;
    }

    /**
     * Create a new instance that uses the passed height map data to this
     * loader. The data passed can be either referenced or copied, depending
     * on the value of the <code>mustCopy</code> parameter. If it is not
     * copied, then the calling code should make sure that it does not change
     * values in the array after calling this method. If copying, the code
     * assumes a rectangular grid of points where the second dimension size is
     * based on <code>data[0].length</code>.
     *
     * @param data The source data to use in [length][width] order
     * @param mustCopy true to request an internal copy be made of the data
     *    false for it to just reference the data
     * @param stepDetails The distance between each height value in the X and
     *    Z coordinates (Y in terrain parlance)
     */
    public HeightMapTerrainData(float[][] data,
                                boolean mustCopy,
                                Point2d stepDetails)
    {
        if(mustCopy)
        {
            int length = data.length;
            int width = data[0].length;

            heightMap = new float[length][width];

            for(int i = 0; i < length; i++)
                System.arraycopy(data[i], 0, heightMap[i], 0, width);

            gridDepth = length;
            gridWidth = width;
        }
        else
        {
            heightMap = data;

            gridDepth = heightMap.length;
            gridWidth = heightMap[0].length;
        }

        gridStep = new Point2d(stepDetails);

        hasTiledTextures = false;
    }

    //----------------------------------------------------------
    // Methods required by TerrainData
    //----------------------------------------------------------

    /**
     * Get the coordinate of the point in the grid.
     *
     * @param coord the x, y, and z coordinates will be placed in the
     *    first three elements of the array.
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinateFromGrid( float[] coord, int gridX, int gridY )
    {
        coord[0] = gridX * (float)gridStep.x;
        coord[1] = heightMap[gridX][gridY];
        coord[2] = -gridY * (float)gridStep.y;
    }

    /**
     * Get the coordinate of the point and correspond texture coordinate in
     * the grid. Assumes that the grid covers a single large texture rather
     * than multiple smaller textures.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param textureCoord 2D coordinates are placed in the first two elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinateFromGrid(float[] coord,
                                      float[] textureCoord,
                                      int gridX,
                                      int gridY)
    {
        coord[0] = gridX * (float)gridStep.x;
        coord[1] = heightMap[gridX][gridY];
        coord[2] = -gridY * (float)gridStep.y;

        if(!hasTiledTextures)
        {
            textureCoord[0] = ((float)gridX) / heightMap[0].length;
            textureCoord[1] = ((float)gridY) / heightMap.length;
        }
        else
        {
            // do something here.
        }
    }

    /**
     * Notify the terrain data handler that when generating texture coordinates
     * that we are using tiled textures and that the coordinates generated
     * should be based on the tiled versions of the images rather than a single
     * large texture.
     *
     * @param enabled True to set the mode to tiled, false for single
     * @see #getCoordinateFromGrid(float[], float[], int, int)
     */
    public void setTiledTextures(boolean enabled)
    {
        hasTiledTextures = enabled;
    }

    /**
     * Check to see if the texture coordinates are being tiled.
     *
     * @return true if texture coordinates are currently being tiled
     */
    public boolean isTiledTextures()
    {
        return hasTiledTextures;
    }

    /**
     * Fetch the Texture that is used to cover the entire terrain. If no
     * texture is used, then return null. Assumes a single large texture for
     * the entire terrain.
     *
     * @return The texture instance to use or null
     */
    public Texture getTexture()
    {
        return texture;
    }

    /**
     * Fetch the texture or part of a texture that can be applied to the
     * sub-region of the overall object. This is to allow for texture tiling
     * of very large texture images or terrain items. If there is no texture
     * or no texture for that region, then this should return null.
     *
     * @param bounds The bounds of the region based on the grid positions
     * @return The texture object suitable for that bounds or null
     */
    public Texture getTexture(Rectangle bounds)
    {
        return texture;
    }

    /**
     * Get the height at the specified grid position.
     *
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     * @return The height at the given grid position
     */
    public float getHeightFromGrid(int gridX, int gridY)
    {
        return heightMap[gridX][gridY];
    }

    /**
     * Get the width (number of points on the Y axis) of the grid.
     *
     * @return The number of points in the width if the grid
     */
    public int getGridWidth()
    {
        return heightMap[0].length;
    }

    /**
     * Get the depth (number of points on the X axis) of the grid.
     *
     * @return The number of points in the depth of the grid
     */
    public int getGridDepth()
    {
        return gridDepth;
    }

    /**
     * Get the real world distance between consecutive X values in the grid.
     *
     * @return The distance between each step of the grid
     */
    public double getGridXStep()
    {
        return gridStep.x;
    }

    /**
     * Get the real world distance between consecutive Y values in the grid.
     *
     * @return The distance between each step of the grid
     */
    public double getGridYStep()
    {
        return gridStep.y;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the texture to the new instance. Setting a value of null will
     * clear the existing texture.
     *
     * @param tex The new texture to use
     */
    public void setTexture(Texture tex)
    {
        texture = tex;
    }
}
