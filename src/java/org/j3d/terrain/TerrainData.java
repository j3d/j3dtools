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

// Application specific imports
import org.j3d.ui.navigation.HeightDataSource;

/**
 * This class provides a generic interface to the terrain dataset.
 * <p>
 *
 * The dataset is represented as a regular grid of heights which use the
 * carto-based notion of the ground being the X-Y plane, rather than the
 * 3D graphics convention of X-Z. In all cases, the access to this data
 * is assumed to be in a grid pattern. If the source data comes from
 * non-uniform sources, such as spot heights, then it will need to be
 * accessible/interpolated to fit the grid nature of the access routines.
 * <p>
 *
 * <b>Note:</b> You should never directly implement this interface. It is
 * expected by the rendering algorithms that one of the derived types will
 * always be used.
 *
 * @author  Justin Couch based on original ideas of Paul Byrne
 * @version $Revision: 1.8 $
 */
public interface TerrainData extends HeightDataSource
{
    /** The source data type is a simple, static terrain */
    public int STATIC_DATA = 1;

    /**
     * The source data comes in the form of tiles that may be dynamically
     * loaded. The most efficient way to use this data is to align the
     * accesses according to the tile boundaries.
     */
    public int TILED_DATA = 2;

    /**
     * The source data comes in free-form data that may be accessed any way
     * that the terrain rendering algorithm likes.
     */
    public int FREEFORM_DATA = 3;

    /**
     * Get the type of terrain data that we are dealing with. One of the
     * constant types are returned. More efficient way of dealing with
     * multiple types than using <code>instanceof</code>
     *
     * @return The type of data that this instance represents
     */
    public int getSourceDataType();

    /**
     * Get the coordinate of the point in the grid. This should translate
     * between the grid position (x,y) and the 3D axis system (x,y,z), which
     * don't use the same coordinate system.
     *
     * @param coord the x, y, and z coordinates will be placed in the
     *    first three elements of the array.
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinate(float[] coord, int gridX, int gridY);

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
                              int gridY);

    /**
     * Get the coordinate of the point and corresponding texture coordinate in
     * the grid. Assumes that the grid covers a single large texture rather
     * than multiple smaller textures. This should translate between the grid
     * position (x,y) and the 3D axis system (x,y,z), which don't use the same
     * coordinate system.
     *
     * @param coord he x, y, and z coordinates will be placed in the first
     *   three elements of the array.
     * @param tex 2D coordinates are placed in the first two elements
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     */
    public void getCoordinateWithTexture(float[] coord,
                                         float[] tex,
                                         int gridX,
                                         int gridY,
                                         int patchX,
                                         int patchY);

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
                                       int gridY);


    /**
     * Check to see if this terrain data has per-vertex colours. Colour may
     * be used with or separately from the texture data. This allows you
     * to use colours to modulate the texture information if desired. Note that
     * if you provide per-vertex colour information here, you need to be
     * careful with the Appearance information that gets set if you provide
     * a custom {@link AppearanceGenerator} implementation.
     *
     * @return true If per-vertex color is available
     */
    public boolean hasColor();

    /**
     * Check to see if this terrain data has any texturing at all - either
     * tiled or simple.
     *
     * @return true If a texture(s) is available
     */
    public boolean hasTexture();

    /**
     * Get the height at the specified grid position.
     *
     * @param gridX The X coordinate of the position in the grid
     * @param gridY The Y coordinate of the position in the grid
     * @return The height at the given grid position
     */
    public float getHeightFromGrid(int gridX, int gridY);

    /**
     * Get the real world distance between consecutive X values in the grid.
     *
     * @return The distance between each step of the grid
     */
    public double getGridXStep();

    /**
     * Get the real world distance between consecutive Y values in the grid.
     *
     * @return The distance between each step of the grid
     */
    public double getGridYStep();
}
