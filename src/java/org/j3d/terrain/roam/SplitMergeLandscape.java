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
import javax.media.j3d.*;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

// Application specific imports
import org.j3d.terrain.*;

/**
 * ROAM implmentation of a landscape using the split-merge combination
 * algorithm.
 * <p>
 *
 * First patch is at 0,0 in x, z and then patches are laid out along the
 * +ve x axis and the -ve z axis
 *
 * @author Paul Byrne, Justin Couch
 * @version $Revision: 1.12 $
 */
public class SplitMergeLandscape extends Landscape
{
    /** Message for when the patchSize is <= 0 */
    private static final String NEG_PATCH_SIZE_MSG =
        "The patch size provided is negative or zero.";

    /** Message for when patchSize is not a ^2 */
    private static final String NOT_POW2_MSG =
        "The patchSize is not a power of two";

    /** Message for the static grid size is not a 2^n + 1 */
    private static final String GRID_W_SIZE_MSG =
        "The grid width is not (n * patchSize + 1) in size: ";

    /** Message for the static grid size is not a 2^n + 1 */
    private static final String GRID_D_SIZE_MSG =
        "The grid depth is not (n * patchSize + 1) in size: ";

    /** Patch size in grid points if the user doesn't supply one */
    private static final int DEFAULT_PATCH_SIZE = 64;

    /** The number of tiles to use on an axis */
    private static final int AXIS_TILE_COUNT = 8;

    /** The patch size to use for this landscape */
    private final int patchSize;

    /** 1 over patchSize for calcs */
    private final int invPatchSize;

    /** The terrain data size */
    private final int terrainDataType;

    /** The accuracy for the varianace that we want */
    private final float accuracy;

    /** The collection of all patches in this landscape */
    private ArrayList patches = new ArrayList();

    /** Queue manager for the pathces needing splits or merges each frame */
    private TreeQueueManager queueManager = new TreeQueueManager();

    /** Number of visible triangles */
    private int triCount = 0;

    /** The default generator if none are supplied */
    private AppearanceGenerator defaultAppGenerator;

    // Stuff Only instanced if the terrain type is Tiled.

    /** Maximum bounding point of the view frustum. Used as a working var. */
    private Point3d maxViewBound;

    /** Minimum bounding point of the view frustum. Used as a working var. */
    private Point3d minViewBound;

    /** The set of tile bounds for the last time the position changed. */
    private Rectangle oldTileBounds;

    /** Working var to fetch the required bounds this frame */
    private Rectangle reqdBounds;

    /**
     * The list of patches not currently in use. Per instance because a cleared
     * patch is still part of the scene graph and can't be shared between
     * multiples of them.
     */
    private LinkedList freePatchList;

    /** Working var for the patches just removed */
    private ArrayList removedPatches;

    /** The grid that holds the current patches for the viewable grid. */
    private PatchGrid patchGrid;

    /**
     * Creates new Landscape based on the view information and the terrain
     * data. If the terrain data is based on static data, the terrain is
     * built using the default patch size.
     *
     * @param view The view frustum looking at this landscape
     * @param data The raw data for the terrain
     * @throws IllegalArgumentException The static grid is not n^2 + 1 in size
     */
    public SplitMergeLandscape(ViewFrustum view, TerrainData data)
    {
        super(view, data);

        terrainDataType = data.getSourceDataType();
        accuracy = (float)Math.toRadians(0.1);

        this.patchSize = init(data, DEFAULT_PATCH_SIZE);
        invPatchSize = 1 / patchSize;
    }

    /**
     * Creates new Landscape based on the view information and static terrain
     * data, with a controlable patch size. The patch size must be a power of
     * two. If the terrain data object provides tiled terrain, the patchSize
     * request is ignored. The patchSize must be a power of two, otherwise an
     * exception will be thrown.
     *
     * @param view The view frustum looking at this landscape
     * @param data The raw data for the terrain
     * @param patchSize The number of grid points per patch side, power 2.
     * @throws IllegalArgumentException The patchSize was < 0 or not a power
     *    of two.
     */
    public SplitMergeLandscape(ViewFrustum view,
                               TerrainData data,
                               int patchSize)
    {
        super(view, data);

        if(patchSize <= 0)
            throw new IllegalArgumentException(NEG_PATCH_SIZE_MSG);

        if(!power2Check(patchSize))
            throw new IllegalArgumentException(NOT_POW2_MSG);

        terrainDataType = data.getSourceDataType();
        accuracy = (float)Math.toRadians(0.1);

        this.patchSize = init(data, patchSize);
        invPatchSize = 1 / patchSize;
    }

    /**
     * Provide a landscape with a specific appearance generator set. If the
     * generator argument is null, then the default is used.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @param gen The generator instance to use
     * @throws IllegalArgumentException either parameter is null
     * @throws IllegalArgumentException The patchSize was < 0 or not a power
     *    of two.
     */
    public SplitMergeLandscape(ViewFrustum view,
                               TerrainData data,
                               AppearanceGenerator gen)
    {
        super(view, data, gen);

        terrainDataType = data.getSourceDataType();
        accuracy = (float)Math.toRadians(0.1);

        this.patchSize = init(data, DEFAULT_PATCH_SIZE);
        invPatchSize = 1 / patchSize;
    }

    /**
     * Provide a landscape with a specific appearance generator set. If the
     * generator argument is null, then the default is used.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @param gen The generator instance to use
     * @param patchSize The number of grid points per patch side, power 2.
     * @throws IllegalArgumentException either parameter is null
     * @throws IllegalArgumentException The patchSize was < 0 or not a power
     *    of two.
     */
    public SplitMergeLandscape(ViewFrustum view,
                               TerrainData data,
                               int patchSize,
                               AppearanceGenerator gen)
    {
        super(view, data, gen);

        if(patchSize <= 0)
            throw new IllegalArgumentException(NEG_PATCH_SIZE_MSG);

        if(!power2Check(patchSize))
            throw new IllegalArgumentException(NOT_POW2_MSG);

        terrainDataType = data.getSourceDataType();
        accuracy = (float)Math.toRadians(0.1);

        this.patchSize = init(data, patchSize);
        invPatchSize = 1 / patchSize;
    }

    /**
     * Initialise the landscape ready for viewing. This should be called
     * before you add the parent branchgroup to the live scenegraph as the
     * implementation code will need to construct the renderable scene graph.
     * It also sets the initial position so that if the terrain is using
     * tilable datasets it can determine where to start building from.
     *
     * @param position The position the user is in the virtual world
     * @param direction The orientation of the user's gaze
     */
    public void initialize(Tuple3f position, Vector3f direction)
    {
        // Just to make sure
        landscapeView.viewingPlatformMoved();

        switch(terrainDataType)
        {
            case TerrainData.STATIC_DATA:
                createStaticPatches();
                break;

            case TerrainData.TILED_DATA:
                minViewBound = new Point3d();
                maxViewBound = new Point3d();
                reqdBounds = new Rectangle();
                oldTileBounds = new Rectangle();
                freePatchList = new LinkedList();
                removedPatches = new ArrayList();

                createTiledPatches(position, direction);
                break;

            case TerrainData.FREEFORM_DATA:
                createFreeFormPatches();
                break;
        }

        setView(position, direction);
    }

    /**
     * Change the view of the landscape. The virtual camera is now located in
     * this position and orientation, so adjust the visible terrain to
     * accommodate the changes.
     *
     * @param position The position of the camera
     * @param direction The direction the camera is looking
     */
    public void setView(Tuple3f position, Vector3f direction)
    {
        queueManager.clear();
        landscapeView.viewingPlatformMoved();
        TreeNode splitCandidate;
        TreeNode mergeCandidate;
        boolean done;

        // Do terrain type spectific processing first
        switch(terrainDataType)
        {
            case TerrainData.TILED_DATA:
                if(!calculateViewTileBounds(position, direction))
                {

//System.out.println("New tile " + reqdBounds);
//System.out.println("old      " + oldTileBounds);
//System.out.println(patchGrid.toString());
//System.out.println();

                    patchGrid.prepareNewBounds(reqdBounds);
                    clearOldTiledPatches();
                    loadNewTiles();

                    oldTileBounds.setBounds(reqdBounds);
                }
                break;

            case TerrainData.FREEFORM_DATA:

            case TerrainData.STATIC_DATA:
                // do nothing
            default:
        }

        int size = patches.size();

        // Firstly set up the queue of triangles that need merging or splitting
        for(int i = 0; i < size; i++)
        {
            Patch p = (Patch)patches.get(i);
            p.setView(position, landscapeView, queueManager);
        }

        // ROAM away!
        done = false;

        while(!done)
        {
            splitCandidate = queueManager.getSplitCandidate();
            mergeCandidate = queueManager.getMergeCandidate();

            if(mergeCandidate == null && splitCandidate != null)
            {
                if (splitCandidate.variance > accuracy)
                {
                    triCount += splitCandidate.split(position, landscapeView, queueManager);
                }
                else
                    done = true;
            }
            else if(mergeCandidate != null && splitCandidate == null)
            {
                if(mergeCandidate.diamondVariance < accuracy)
                {
                    triCount -= mergeCandidate.merge(queueManager);
                    //System.out.println("No split merge "+mergeCandidate+"  "+mergeCandidate.diamondVariance);
                }
                else
                    done = true;
            }
            else if(mergeCandidate != null && splitCandidate != null &&
                    (splitCandidate.variance > accuracy ||
                     splitCandidate.variance > mergeCandidate.diamondVariance))
            {
                if (splitCandidate.variance > accuracy)
                {
                    triCount += splitCandidate.split(position, landscapeView, queueManager);
                }
                else if (mergeCandidate.diamondVariance < accuracy)
                {
                    triCount -= mergeCandidate.merge(queueManager);
                }
            }
            else
            {
                done = true;
            }
        }

        // Finally, tell the geometry to be updated at the Java3D level.
        for(int i = 0; i < size; i++)
        {
            Patch p = (Patch)patches.get(i);
            p.updateGeometry();
        }

        queueManager.clear();
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Common initialisation code the constructors of this class. Expects that
     * the class var <code>terrainDataType</code> has alredy been called.
     *
     * @param data The source terrain to work with
     * @param ps The patch size if nothing else provided
     * @return The patch size to actually use
     * @throws IllegalArgumentException The patch size doesn't fit the width
     *    of the static data.
     */
    private int init(TerrainData data, int ps)
    {
        int ret_val = ps;

        switch(terrainDataType)
        {
            case TerrainData.STATIC_DATA:
                StaticTerrainData s_data = (StaticTerrainData)data;

                int w = s_data.getGridWidth();
                int h = s_data.getGridDepth();

                if(!checkPatchSide(w, ps))
                    throw new IllegalArgumentException(GRID_W_SIZE_MSG + w);

                if(!checkPatchSide(h, ps))
                    throw new IllegalArgumentException(GRID_D_SIZE_MSG + h);

                break;

            case TerrainData.FREEFORM_DATA:
                break;

            case TerrainData.TILED_DATA:
                ret_val = ((TiledTerrainData)data).getTileSize();
                break;

            default:
                ret_val = 0;
                System.out.println("Unknown terrain type");
        }

        return ret_val;
    }

    /**
     * Create patches for a tiled terrain. Assumes all the working vars have
     * been instantiated before calling.
     *
     * @param position The position of the camera
     * @param direction The direction the camera is looking
     */
    private void createTiledPatches(Tuple3f position, Vector3f direction)
    {
        TiledTerrainData t_data = (TiledTerrainData)terrainData;

        calculateViewTileBounds(position, direction);

        // request the bounds be set to the minimum required
        t_data.setActiveBounds(reqdBounds);
        oldTileBounds.setBounds(reqdBounds);
        patchGrid = new PatchGrid(reqdBounds);

        AppearanceGenerator app_gen = getAppGenerator();
        Appearance app;
        Patch p = null;

        int east = reqdBounds.x * patchSize;
        int x_tile = reqdBounds.x;

        int e_grid = east;
        int n_grid;

        for(int i = 0 ; i < reqdBounds.width; i++)
        {
            int north = reqdBounds.y * patchSize;
            int y_tile = reqdBounds.y;

            n_grid = north;

            for(int j = 0; j < reqdBounds.height; j++)
            {
                app = app_gen.createAppearance();
                app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
                app.setTexture(t_data.getTexture(x_tile, y_tile));

                p = new Patch(terrainData,
                              patchSize,
                              app,
                              landscapeView);

                p.setOrigin(e_grid, n_grid);

                patchGrid.addPatch(p, x_tile, y_tile);
                p.makeActive();

                patches.add(p);

                triCount += 2;
                addChild(p.getShape3D());

                y_tile++;
                north++;
                n_grid += patchSize;
            }

            x_tile++;
            east++;
            e_grid += patchSize;
        }
    }

    /**
     * Create patches for freeform terrain.
     */
    private void createFreeFormPatches()
    {
        FreeFormTerrainData t_data = (FreeFormTerrainData)terrainData;
System.out.println("Free-form terrain not implemented yet");
    }

    /**
     * Create a new set of patches based on static data set
     */
    private void createStaticPatches()
    {
        StaticTerrainData t_data = (StaticTerrainData)terrainData;

        int depth = t_data.getGridDepth() - patchSize;
        int width = t_data.getGridWidth() - patchSize;

        if((depth < 0) || (width < 0))
            throw new IllegalArgumentException("Patch size is greater than " +
                                               "the grid cell size");


        Patch[] westPatchNeighbour = new Patch[width];
        Patch southPatchNeighbour = null;
        Patch p = null;

        AppearanceGenerator app_gen = getAppGenerator();
        Appearance app;

        app = app_gen.createAppearance();
        app.setTexture(t_data.getTexture());

        // We meed to special case the handling for a 1 cell wide grid.
        // Here there is no west patch neighbour and so we avoid the
        // array assignments.
        if(width == 0)
        {
            for(int north = 0; north <= depth; north += patchSize)
            {
                p = new Patch(terrainData,
                              patchSize,
                              app,
                              landscapeView);

                p.setOrigin(0, north);
                p.setSouthNeighbour(southPatchNeighbour);
                p.makeActive();

                patches.add(p);

                triCount += 2;
                addChild(p.getShape3D());

                southPatchNeighbour = p;
            }

            southPatchNeighbour = null;
        }
        else
        {
            for(int east = 0 ; east <= width; east += patchSize)
            {
                for(int north = 0; north <= depth; north += patchSize)
                {
                    int w_pos = (int)(north * invPatchSize);

                    p = new Patch(terrainData,
                                  patchSize,
                                  app,
                                  landscapeView);

                    p.setOrigin(east, north);

                    p.setWestNeighbour(westPatchNeighbour[w_pos]);
                    p.setSouthNeighbour(southPatchNeighbour);
                    p.makeActive();

                    patches.add(p);

                    triCount += 2;
                    addChild(p.getShape3D());

                    southPatchNeighbour = p;
                    westPatchNeighbour[w_pos] = p;
                }

                southPatchNeighbour = null;
            }
        }
    }

    /**
     * Convenience method to check whether the value is a power of two.
     *
     * @param val The value to check
     * @return true if this is a power of two, false if not
     */
    private boolean power2Check(int val)
    {
        int shift_val = val;

        // rolling shift until the bottom bit is non-zero
        for( ; (shift_val & 0x01) == 0; shift_val >>= 1) ;

        // Now, is the remaining value non-zero. If it is, then it wasn't a
        // power of two.
        return ((shift_val | 0x01) == 0x01);
    }

    /**
     * Convenience method to check that the side of a set of patches is a
     * multiple + 1 number of points of the patch size
     *
     * @param size The number of raw points
     * @param ps The patch size
     * @return true The size is correct for the patch size
     */
    private boolean checkPatchSide(int size, int ps)
    {
        int val = size - 1;

        return ((val % ps) == 0);
    }

    /**
     * Calculate the current required bounds for the viewpoint. Uses the
     * information directly from the view frustum, so no need to pass in the
     * position and orientation info. Assumes that the update of the view
     * platform is called before this method, for this frame.
     *
     * @param direction The direction the viewer is looking
     * @return true if these bounds are the same as the old ones
     */
    private boolean calculateViewTileBounds(Tuple3f position,
                                            Vector3f direction)
    {
        TiledTerrainData t_data = (TiledTerrainData)terrainData;

        float x_spacing = (float)t_data.getGridXStep();
        float y_spacing = (float)t_data.getGridYStep();
        int tile_size = t_data.getTileSize();

        float[] origin = new float[3];
        t_data.getCoordinate(origin, 0, 0);

        // turn the position into a tile coord
        float cur_x = position.x - origin[0];
        float cur_z = origin[2] - position.z;

        int tile_x = (int)Math.floor(cur_x / (x_spacing * tile_size));
        int tile_y = (int)Math.floor(cur_z / (y_spacing * tile_size));

        // first check - are we looking straight up/down? If so, then get
        // the tile we are over and just build a square grid around that
        // point.
        if(direction.x == 0 && direction.z == 0)
        {
            tile_x -= AXIS_TILE_COUNT >> 1;
            tile_y -= AXIS_TILE_COUNT >> 1;
        }
        else
        {
            // The int rounding helps us because if the angle is such that
            // it takes more than half a tile then the numbers round up and
            // we get something that looks right.
            float aspect = Math.abs(direction.x / direction.z);

            if(direction.x >= 0)
            {
                if(direction.z <= 0)
                {
                    if(aspect < 0.5f)
                        tile_x--;
                    else if(aspect > 2)
                        tile_y--;
                }
                else
                {
                    tile_y--;

                    if(aspect < 0.5f)
                        tile_x--;
                }
            }
            else
            {
                if(direction.z <= 0)
                {
                    tile_x--;

                    if(aspect > 2)
                        tile_y--;
                }
                else
                {
                    tile_x--;
                    tile_y--;
                }
            }

            reqdBounds.x = tile_x;
            reqdBounds.y = tile_y;
            reqdBounds.width = AXIS_TILE_COUNT;
            reqdBounds.height = AXIS_TILE_COUNT;
        }

        // If the two are the same, don't bother going any further.
        return oldTileBounds.equals(reqdBounds);
    }

    /**
     * Clean out the patches that are not needed any more due to a move in the
     * viewpoint and tiling patch list. Should be called after the tiled
     * view bounds have been recalculated.
     */
    private void clearOldTiledPatches()
    {
        int size = patches.size();
        boolean patch_removed = false;

        for(int i = 0; i < size; i++)
        {
            Patch p = (Patch)patches.get(i);

            if(!reqdBounds.contains(p.getTileOrigin()))
            {
                p.clear();
                freePatchList.add(p);
                removedPatches.add(p);
                patch_removed = true;
            }
        }

        if(patch_removed)
        {
            patches.removeAll(removedPatches);
            removedPatches.clear();
        }
    }

    /**
     * Load new tiles into the system for the areas that are not accounted
     * for.
     */
    private void loadNewTiles()
    {
        // Since we run axis aligned boundaies, let's just look at the
        // difference between new and old.

        Appearance app;
        Patch p = null;

        AppearanceGenerator app_gen = getAppGenerator();
        int north, east;

        // For each row difference
        // add a tile
        float invPatchSize = 1 / (float)patchSize;

        int start_east = 0;
        int start_north = 0;

        // Do we need to add stuff to the start in the depth direction?
        int diff = reqdBounds.x - oldTileBounds.x;

        if(diff <= 0)
            start_east = reqdBounds.x;
        else
            start_east = reqdBounds.x + reqdBounds.width - diff;

        int num_east = Math.abs(diff);

        diff = reqdBounds.y - oldTileBounds.y;

        if(diff <= 0)
            start_north = reqdBounds.y;
        else
            start_north = reqdBounds.y + reqdBounds.height - diff;

        int num_north = Math.abs(diff);
        int i = 0;
        int j = 0;

//System.out.print("** add e: " + start_east + " " + num_east);
//System.out.println(" n: " + start_north + " " + num_north);

        // Do we need to tack on a row in the north-south direction?
        // Only works from the old tile bounds beginning, up to the end
        // of the new section.
        if(num_north != 0)
        {
            TiledTerrainData td = (TiledTerrainData)terrainData;
            east = oldTileBounds.x - 1;
            int e_grid = east * patchSize;
            int n_grid;

            for(i = 0; i < reqdBounds.width; i++)
            {
                e_grid += patchSize;
                east++;

                north = start_north;
                n_grid = (north - 1) * patchSize;

                for(j = 0; j < num_north; j++)
                {
                    n_grid += patchSize;

                    if(freePatchList.size() == 0)
                    {
                        app = app_gen.createAppearance();
                        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
                        app.setTexture(td.getTexture(east, north));

                        p = new Patch(terrainData,
                                      patchSize,
                                      app,
                                      landscapeView);

                        addChild(p.getShape3D());
                    }
                    else
                    {
                        p = (Patch)freePatchList.remove(0);
                        app = p.getAppearance();
                        app.setTexture(td.getTexture(east, north));
                    }

                    p.setOrigin(e_grid, n_grid);
                    patchGrid.addPatch(p, east, north);
                    p.makeActive();

                    p.reset();

                    patches.add(p);

                    triCount += 2;
                    north++;
                }
            }
        }

        // Now clean up anything that needs to be added to the east/west
        // sides. Add this the full length, including any new north/south
        // parts.
        if(num_east != 0)
        {
            east = start_east - 1;
            num_north = reqdBounds.height;

            TiledTerrainData td = (TiledTerrainData)terrainData;
            int e_grid = east * patchSize;
            int n_grid;

            for(i = 0; i < num_east; i++)
            {
                e_grid += patchSize;
                north = start_north;
                n_grid = (north - 1) * patchSize;
                east++;

                for(j = 0; j < num_north; j++)
                {
                    int w_pos = (int)(north * invPatchSize);
                    n_grid += patchSize;

                    if(freePatchList.size() == 0)
                    {
                        app = app_gen.createAppearance();
                        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
                        app.setTexture(td.getTexture(east, north));

                        p = new Patch(terrainData,
                                      patchSize,
                                      app,
                                      landscapeView);

                        addChild(p.getShape3D());
                    }
                    else
                    {
                        p = (Patch)freePatchList.remove(0);
                        app = p.getAppearance();
                        app.setTexture(td.getTexture(east, north));
                    }

                    p.setOrigin(e_grid, n_grid);
                    patchGrid.addPatch(p, east, north);
                    p.makeActive();

                    patches.add(p);

                    triCount += 2;
                    north++;
                }
            }
        }
    }

    /**
     * Convenience method to fetch an appearance generator instance. Does all
     * the setup and checking so that a valid instance is always returned.
     *
     * @return A valie generator instance
     */
    private AppearanceGenerator getAppGenerator()
    {
        AppearanceGenerator app_gen = null;

        if(appearanceGenerator == null)
        {
            if(defaultAppGenerator == null)
                defaultAppGenerator = new DefaultAppearanceGenerator();

            app_gen = defaultAppGenerator;
        }
        else
        {
            app_gen = appearanceGenerator;
        }

        return app_gen;
    }
}
