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
import java.awt.image.BufferedImage;

// Local imports
import org.j3d.terrain.AbstractStaticTerrainData;
import org.j3d.util.interpolator.ColorInterpolator;

/**
 * Generalised implementation of the {@link org.j3d.terrain.TerrainData} for
 * any file format or loader that supports a static grid based data structure.
 * <p>
 *
 * Supporting the height data source methods requires a bit of assumption about
 * the data. Because we have data in quads, and we don't know how the
 * underlying terrain rendering code is triangulating the data, we have to
 * punt and take a guess. To interpolate a height value for a point that is
 * not directly on a grid position, the code will take the average height of
 * the grid square. This is a really horrible algorithm, but is the fastest to
 * implement currently. We need to re-visit this to get something a little more
 * accurate and is proportional to the position in the cell.
 *
 * <p>
 * The basic implementation here does not support a texture. If an application
 * wishes to use a texture, they should extend this class and override the
 * {@link #getTexture()} method. If you wish to provide a pre-loaded texture,
 * then you can use the {@link #setTexture(BufferedImage)} method of this class to
 * place one here.
 * <p>
 *
 * If a color interpolator is not provided, then color is not supported in this
 * terrain (unless set by some implementing class).
 *
 * @author  Justin Couch
 * @version $Revision: 1.10 $
 */
public class HeightMapTerrainData extends AbstractStaticTerrainData
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

    /** The texture to supply to the user of this class */
    private BufferedImage texture = null;

    /** The colour interpolator used by this class */
    private ColorInterpolator colorInterp;

    /**
     * Create a new instance that sources the data from the given parser.
     * Assumes that the parser has already fetched its information and has
     * the height-grid available for use.
     *
     * @param source The place to source the data from
     */
    public HeightMapTerrainData(HeightMapSource source)
    {
        heightMap = source.getHeights();
        float[] steps = source.getGridStep();

        gridStepX = steps[0];
        gridStepY = steps[1];

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
                                double[] stepDetails)
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

        gridStepX = stepDetails[0];
        gridStepY = stepDetails[1];
    }

    //----------------------------------------------------------
    // Methods required by HeightDataSource
    //----------------------------------------------------------

    /**
     * Get the height at the given X,Z coordinate in the local coordinate
     * system. The
     *
     * @param x The x coordinate for the height sampling
     * @param z The z coordinate for the height sampling
     * @return The height at the current point or NaN
     */
    @Override
    public float getHeight(float x, float z)
    {
        // work out where we are in the grid first. Rememeber that we have
        // to convert between coordinate systems
        float rel_x_pos = x / (float)gridStepX;
        float rel_y_pos = z / (float)gridStepY;

        // fetch the coords of the four heights surrounding this point
        int x_coord = (int)Math.floor(rel_x_pos);
        int y_coord = (int)Math.floor(rel_y_pos);

        if((x_coord < 0) || (y_coord < 0) ||
           (x_coord + 1 > gridWidth) || (y_coord + 1 > gridDepth))
        {
           return Float.NaN;
        }

        // if we are on the far edge, set the two grid indicies the same.
        // results in the right answer after interpolation without
        // excessive if statements.
        int x2 = x_coord + 1 == gridWidth ? x_coord : x_coord + 1;
        int y2 = y_coord + 1 == gridDepth ? y_coord : y_coord + 1;

        float h1 = heightMap[x_coord][y_coord];
        float h2 = heightMap[x_coord][y2];
        float h3 = heightMap[x2][y_coord];
        float h4 = heightMap[x2][y2];

        float t_x = (x % (float)gridStepX) / (float)gridStepX;
        float t_y = (z % (float)gridStepY) / (float)gridStepY;

        float avg_x_1 = h1 + (h2 - h1) * t_x;
        float avg_x_2 = h3 + (h4 - h3) * t_x;

        // return the average height
        return avg_x_1 + (avg_x_2 - avg_x_1) * t_y;
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
    @Override
    public void getCoordinate(float[] coord, int gridX, int gridY)
    {
        coord[0] = gridX * (float)gridStepX;
        coord[1] = heightMap[gridX][gridY];
        coord[2] = -gridY * (float)gridStepY;
    }

    /**
     * Get the coordinate with all the information - texture and colors.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param tex 2D coordinates are placed in the first two elements
     * @param color 3 component colors are placed in the first 3 elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    @Override
    public void getCoordinate(float[] coord,
                              float[] tex,
                              float[] color,
                              int gridX,
                              int gridY)
    {
        float height = heightMap[gridX][gridY];

        coord[0] = gridX * (float)gridStepX;
        coord[1] = height;
        coord[2] = -gridY * (float)gridStepY;

        tex[0] = ((float)gridX) / (gridWidth - 1);
        tex[1] = ((float)gridY) / (gridDepth - 1);

        if(colorInterp != null)
        {
            float[] rgb = colorInterp.floatRGBValue(height);
            color[0] = rgb[0];
            color[1] = rgb[1];
            color[2] = rgb[2];
        }
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
    @Override
    public void getCoordinateWithTexture(float[] coord,
                                         float[] textureCoord,
                                         int gridX,
                                         int gridY,
                                         int patchX,
                                         int patchY)
    {
        // Note: Does not use patchX, patchY so likely needs to be fixed
        coord[0] = gridX * (float)gridStepX;
        coord[1] = heightMap[gridX][gridY];
        coord[2] = -gridY * (float)gridStepY;

        textureCoord[0] = ((float)gridX) / (gridWidth - 1);
        textureCoord[1] = ((float)gridY) / (gridDepth - 1);
    }

    /**
     * Get the coordinate of the point and the corresponding color value in
     * the grid. Color values are used when there is no texture supplied, so
     * this should always provide something useful.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param color 3 component colors are placed in the first 3 elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    @Override
    public void getCoordinateWithColor(float[] coord,
                                       float[] color,
                                       int gridX,
                                       int gridY)
    {
        float height = heightMap[gridX][gridY];

        coord[0] = gridX * (float)gridStepX;
        coord[1] = height;
        coord[2] = -gridY * (float)gridStepY;

        if(colorInterp != null)
        {
            float[] rgb = colorInterp.floatRGBValue(height);
            color[0] = rgb[0];
            color[1] = rgb[1];
            color[2] = rgb[2];
        }
    }

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
     * Get the height at the specified grid position.
     *
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     * @return The height at the given grid position
     */
    @Override
    public float getHeightFromGrid(int gridX, int gridY)
    {
        return heightMap[gridX][gridY];
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
    @Override
    public void setTexture(BufferedImage tex)
    {
        texture = tex;

        textureAvailable = (texture != null);
    }

    /**
     * Set up a height color ramp to provide colour information. This should
     * be set before passing the terrain data to a rendering algorithm as it
     * sets the hasColor() flag to true. Heights should be based on sea-level
     * as value zero. A value of null clears the current reference.
     *
     * @param interp The interpolator instance to use
     */
    public void setColorInterpolator(ColorInterpolator interp)
    {
        colorInterp = interp;

        colorAvailable = (colorInterp != null);
    }
}
