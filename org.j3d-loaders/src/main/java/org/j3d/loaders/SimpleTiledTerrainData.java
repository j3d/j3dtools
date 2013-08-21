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
import java.awt.image.BufferedImage;

import javax.vecmath.Point2d;

// Application specific imports
import org.j3d.terrain.AbstractTiledTerrainData;
import org.j3d.util.interpolator.ColorInterpolator;

/**
 * Simplified implementation of the {@link org.j3d.terrain.TiledTerrainData}
 * for any file format or loader that supports a static grid based data
 * structure.
 * <p>
 *
 * This implementation is pretty dumb in that it does no paging of the internal
 * data and just keeps it available in memory. That should make it useful for
 * grids of up to 2049x2049 heights.
 * <p>
 *
 * If a color interpolator is not provided, then color is not supported in this
 * terrain (unless set by some implementing class).
 *
 * @author  Justin Couch, Alan Hudson
 * @version $Revision: 1.3 $
 */
public class SimpleTiledTerrainData extends AbstractTiledTerrainData
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

    /** The width of the terrain in grid points. (Y coordinate) */
    private int gridWidth;

    /** The depth of the terrain in grid points. (X coordinate) */
    private int gridDepth;

    /** The maximum tile number in the width */
    private int maxWidthTile;

    /** The maximum tile number in the depth */
    private int maxDepthTile;

    /** The height values */
    private float[][] heightMap;

    /** The colour interpolator used by this class */
    private ColorInterpolator colorInterp;

    /**
     * Create a new instance that sources the data from the given parser.
     * Assumes that the parser has already fetched its information and has
     * the height-grid available for use.
     *
     * @param source The place to source the data from
     */
    public SimpleTiledTerrainData(HeightMapSource source)
    {
        heightMap = source.getHeights();
        float[] steps = source.getGridStep();

        gridStepX = steps[0];
        gridStepY = steps[1];

        gridDepth = heightMap.length;
        gridWidth = heightMap[0].length;

        calcTileSize();
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
    public SimpleTiledTerrainData(float[][] data,
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

        gridStepX = stepDetails.x;
        gridStepY = stepDetails.y;

        calcTileSize();
    }

    //----------------------------------------------------------
    // Methods required by TiledTerrainData
    //----------------------------------------------------------

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
    public void getTilesAvailableBounds(Rectangle bounds)
    {
        bounds.x = 0;
        bounds.y = 0;
        bounds.width = maxWidthTile;
        bounds.height = maxDepthTile;
    }

    /**
     * Set the bounding information, in number of tiles, of the area that the
     * terrain rendering code will be limiting its access to. Ignored for the
     * moment.
     *
     * @param bounds The bounds of the area, in tile coordinates, that will be
     *    accessed
     */
    public void setActiveBounds(Rectangle bounds)
    {
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
    public float getHeight(float x, float z)
    {
        // work out where we are in the grid first. Rememeber that we have
        // to convert between coordinate systems
        float rel_x_pos = x / (float)gridStepX;
        float rel_y_pos = z / (float)gridStepY;

        // fetch the coords of the four heights surrounding this point
        int x_coord = (int)Math.floor(rel_x_pos);
        int y_coord = (int)Math.floor(rel_y_pos);

        // This algorithm sucks. It should be much nicer, but I'm lazy and
        // want to do some other things ATM......

        if((x_coord < 0) || (y_coord < 0) ||
           (x_coord + 1 >= gridWidth) || (y_coord + 1 >= gridDepth))
        {
           return Float.NaN;
        }

        float h1 = heightMap[x_coord][y_coord];
        float h2 = heightMap[x_coord][y_coord + 1];
        float h3 = heightMap[x_coord + 1][y_coord];
        float h4 = heightMap[x_coord + 1][y_coord + 1];

        // return the average height
        return (h1 + h2 + h3 + h4) * 0.25f;
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
    public void getCoordinate(float[] coord, int gridX, int gridY)
    {

        coord[0] = gridX * (float)gridStepX;
        coord[2] = -gridY * (float)gridStepY;

        int g_x = 0;
        int g_y = 0;

        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        coord[1] = heightMap[g_x][g_y];
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
    public void getCoordinate(float[] coord,
                              float[] tex,
                              float[] color,
                              int gridX,
                              int gridY)
    {
        int g_x = 0;
        int g_y = 0;

        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        coord[1] = heightMap[g_x][g_y];

        coord[0] = gridX * (float)gridStepX;
        coord[2] = -gridY * (float)gridStepY;

        tex[0] = ((float)gridX) / (gridWidth - 1);
        tex[1] = ((float)gridY) / (gridDepth - 1);

        if(gridX >= 0 && gridY >= 0 && gridX < gridWidth && gridY < gridDepth)
        {
            float[] rgb = colorInterp.floatRGBValue(coord[1]);
            color[0] = rgb[0];
            color[1] = rgb[1];
            color[2] = rgb[2];
        }
        else
        {
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
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
    public void getCoordinateWithTexture(float[] coord,
                                         float[] textureCoord,
                                         int gridX,
                                         int gridY,
                                         int patchX,
                                         int patchY)
    {
        int g_x = 0;
        int g_y = 0;

        if (tileGenerator == null)
        {
            if(gridX >= gridWidth)
                g_x = gridWidth - 1;
            else if(gridX > 0)
                g_x = gridX;

            if(gridY >= gridDepth)
                g_y = gridDepth - 1;
            else if(gridY > 0)
                g_y = gridY;

            coord[1] = heightMap[g_x][g_y];

            coord[0] = gridX * (float)gridStepX;
            coord[2] = -gridY * (float)gridStepY;

            textureCoord[0] = ((float)gridX) / (gridWidth - 1);
            textureCoord[1] = ((float)gridY) / (gridDepth - 1);
        }
        else
        {
            if(gridX >= gridWidth)
                g_x = gridWidth - 1;
            else if(gridX > 0)
                g_x = gridX;

            if(gridY >= gridDepth)
                g_y = gridDepth - 1;
            else if(gridY > 0)
                g_y = gridY;

            coord[1] = heightMap[g_x][g_y];

            coord[0] = gridX * (float)gridStepX;
            coord[2] = -gridY * (float)gridStepY;

            if (patchY % 2 == 0)
            {
                if (gridY < 0)
                {
                    if (gridY % 128 == 0)
                        textureCoord[1] = 0;
                    else
                        textureCoord[1] = 1 + (gridY % 64) / 64.0f;
                }
                else
                {
                    if (gridY % 64 == 0 && gridY % 128 != 0)
                        textureCoord[1] = 1;
                    else
                        textureCoord[1] = (gridY % 64) / 64.0f;
                }
            }
            else
            {
                if (gridY <= 0)
                {
                    if (gridY % 64 == 0 && gridY % 128 != 0)
                        textureCoord[1] = 0;
                    else
                       textureCoord[1] = 1 + (gridY % 64) / 64.0f;
                }
                else
                {
                    if (gridY % 128 == 0)
                        textureCoord[1] = 1;
                    else
                        textureCoord[1] = (gridY % 64) / 64.0f;
                }
            }

            if (patchX % 2 == 0)
            {
                if (gridX < 0)
                {
                    if (gridX % 64 == 0 && gridX % 128 != 0)
                        textureCoord[0] = 0;
                    else
                        textureCoord[0] = 1 + (gridX % 64) / 64.0f;
                }
                else
                {
                    if (gridX % 128 == 0)
                        textureCoord[0] = 1;
                    else
                        textureCoord[0] = (gridX % 64) / 64.0f;
                }
            }
            else
            {
                if (gridX <= 0)
                {
                    if (gridX % 128 == 0)
                        textureCoord[0] = 0;
                    else
                       textureCoord[0] = 1 + (gridX % 64) / 64.0f;
                }
                else
                {
                    if (gridX % 64 == 0 && gridX % 128 != 0)
                        textureCoord[0] = 1;
                    else
                        textureCoord[0] = (gridX % 64) / 64.0f;
                }
            }
        }
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
    public void getCoordinateWithColor(float[] coord,
                                       float[] color,
                                       int gridX,
                                       int gridY)
    {
        int g_x = 0;
        int g_y = 0;

        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        float height = heightMap[g_x][g_y];

        coord[0] = gridX * (float)gridStepX;
        coord[1] = height;
        coord[2] = -gridY * (float)gridStepY;

        if(gridX >= 0 && gridY >= 0 && gridX < gridWidth && gridY < gridDepth)
        {
            float[] rgb = colorInterp.floatRGBValue(coord[1]);
            color[0] = rgb[0];
            color[1] = rgb[1];
            color[2] = rgb[2];
        }
        else
        {
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
        }
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
        int g_x = 0;
        int g_y = 0;

        if(gridX >= gridWidth)
            g_x = gridWidth - 1;
        else if(gridX > 0)
            g_x = gridX;

        if(gridY >= gridDepth)
            g_y = gridDepth - 1;
        else if(gridY > 0)
            g_y = gridY;

        return heightMap[g_x][g_y];
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Calculate the best tile size for the given grid size. Only ever called
     * once at startup of the class.
     */
    private void calcTileSize()
    {
        // We like to start at 64 and work our way down until we find something
        // that is exactly divisible. Should always have a grid of 2^n + 1
        // points.
        int t_size = 64;
        int depth = gridDepth - 1;

        while((depth % t_size) != 0)
            t_size >>= 1;

        gridPointsPerTile = t_size;

        maxDepthTile = (gridDepth - 1) / t_size;
        maxWidthTile = (gridWidth - 1) / t_size;
    }
}
