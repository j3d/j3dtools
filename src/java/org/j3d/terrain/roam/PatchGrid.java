/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

// Standard imports
import java.awt.Rectangle;
import java.util.ArrayList;

// Application specific imports
// none

/**
 * A data holder class that manages the patch information as a grid.
 * <p>
 *
 * The implementation uses array lists rather than linked lists for
 * storing the bounding boxes.
 *
 * @author  Justin Couch
 * @version
 */
class PatchGrid
{
    private int numNorth;
    private int numWest;

    private int northOffset;
    private int westOffset;

    private Patch[][] grid;

    /**
     * Create a new grid that has its structures initialiesd to the given
     * bounding box size. The size is expressed in tile coordinates, not
     * grid coordinates.
     *
     * @param bounds The bounds to create the grid for
     */
    PatchGrid(Rectangle bounds)
    {
        grid = new Patch[bounds.width][bounds.height];
        northOffset = bounds.y;
        westOffset = bounds.x;
        numNorth = bounds.height;
        numWest = bounds.width;
    }

    /**
     * Change the grid to occupy the bounds given the current setup.
     */
    void prepareNewBounds(Rectangle bounds)
    {
        // shift the whole lot around. Do we first need to reallocate if the
        // passed in bounds is bigger than the actual allocated grid?

        // first just deal with the width of the grid
        int i, j;

        if(bounds.width > grid.length)
        {
            Patch[][] tmp = new Patch[bounds.width][0];

            for(i = 0; i < grid.length; i++)
                tmp[i] = grid[i];

            int cur_depth = grid[0].length;

            for( ; i < bounds.width; i++)
                tmp[i] = new Patch[cur_depth];
        }
        else if(bounds.width < numWest)
        {
            // dereference the bits that are left over
            for(i = bounds.width; i < numWest; i++)
            {
                for(j = 0; j < numNorth; j++)
                {
                    grid[i][j] = null;
                }
            }
        }

        // new lets just deal with the depth.
        if(bounds.height > grid[0].length)
        {
            Patch[] tmp;

            // Copy only the ones that have valid data. Replace the rest
            for(i = 0; i < numWest; i++)
            {
                tmp = new Patch[bounds.height];
                System.arraycopy(grid[i], 0, tmp, 0, numNorth);

                grid[i] = tmp;
            }

            for( ; i < grid.length; i++)
                grid[i] = new Patch[bounds.height];
        }
        else if(bounds.height < numNorth)
        {
            // dereference the bits that are left over
            for(i = 0; i < numWest; i++)
            {
                for(j = bounds.height; j < numNorth; j++)
                {
                    grid[i][j] = null;
                }
            }
        }


        // Right, now, where were we again? Oh, that's right, we need to now
        // shift everything around within the array so that we leave blank
        // spots in the right place. Start first with depth issues.
        if(bounds.y < northOffset)
        {
            int diff = northOffset - bounds.y;

            for(i = 0; i < bounds.width; i++)
                System.arraycopy(grid[i], 0, grid[i], diff, bounds.height);

            for(i = 0; i < bounds.width; i++)
                for(j = 0; j < diff; j++)
                    grid[i][j] = null;
        }
        else if(bounds.y > northOffset)
        {
            int diff = bounds.y - northOffset;

            for(i = 0; i < bounds.width; i++)
                System.arraycopy(grid[i], diff, grid[i], 0, bounds.height);

            for(i = 0; i < bounds.width; i++)
                for(j = diff; j < numNorth; j++)
                    grid[i][j] = null;
        }

        if(bounds.x < westOffset)
        {
            int diff = westOffset - bounds.x;

            for(i = bounds.width - 1; i > diff; i--)
                System.arraycopy(grid[i - 1], 0, grid[i], 0, bounds.height);

            for( ; i > 0; i--)
                for(j = 0; j < bounds.height; j++)
                    grid[i][j] = null;
        }
        else if(bounds.x > westOffset)
        {
            int diff = bounds.x - westOffset;

            for(i = 0; i < bounds.width; i++)
                System.arraycopy(grid[i + diff], 0, grid[i], 0, bounds.height);

            for( ; i < numWest; i++)
                for(j = 0; j < bounds.height; j++)
                    grid[i][j] = null;
        }

        // Now that we are all done, set the vars with the correct values
        numWest = bounds.width;
        numNorth = bounds.height;

        westOffset = bounds.x;
        northOffset = bounds.y;
    }

    /**
     * Add the patch to the grid. Sets the south and west neighbour
     * patches of both this class and the nearest north and east
     * neighbours.
     *
     * @param p The patch to add
     * @param tileX The position in the tile that this should go
     * @param tileY The position in the tile that this should go
     */
    void addPatch(Patch p, int tileX, int tileY)
    {
        int local_x = tileX - westOffset;
        int local_y = tileY - northOffset;

        grid[local_x][local_y] = p;

        if(local_x != 0)
            grid[local_x - 1][local_y].setWestNeighbour(p);

        if(local_y != 0)
            grid[local_x][local_y - 1].setSouthNeighbour(p);

        if(local_x < grid.length - 1)
            p.setWestNeighbour(grid[local_x + 1][local_y]);

        if(local_y < grid[0].length - 1)
            p.setSouthNeighbour(grid[local_x][local_y + 1]);
    }
}
