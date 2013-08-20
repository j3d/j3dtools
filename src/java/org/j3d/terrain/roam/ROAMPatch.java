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
import java.awt.Point;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3f;

// Application specific imports
import org.j3d.terrain.TerrainData;
import org.j3d.util.frustum.ViewFrustum;

/**
 * A patch represents a single piece of terrain geometry that can be
 * rendered as a standalone block within ROAM.
 * <p>
 *
 * A patch represents a single block of geometry within the overall scheme
 * of the terrain data. Apart from a fixed size nothing else is fixed in this
 * patch. The patch consists of a single TriangleArray that uses a geometry
 * updater (geometry by reference is used) to update the geometry each frame
 * as necessary. It will, when instructed, dynamically recalculate what
 * vertices need to be shown and set those into the geometry array.
 * <p>
 *
 * This is an abstract representation that renderer-specific items should be
 * extending with their own geometry handling.
 *
 * @author  Justin Couch, Paul Byrne
 * @version $Revision: 1.3 $
 */
public abstract class ROAMPatch
{
    /** The final size in number of grid points for this patch */
    protected final int PATCH_SIZE;

    /** The values of the nodes in the NW triangle of this patch */
    protected TreeNode NWTree;

    /** The values of the nodes in the NW triangle of this patch */
    protected TreeNode SETree;

    /** Variance holders for the two sub-trees of this patch */
    private VarianceTree NWVariance;
    private VarianceTree SEVariance;

    /** The origin grid coordinates. */
    private int xOrigin;
    private int yOrigin;

    /** The patch grid coordinates */
    private int patchX;
    private int patchY;

    /** The origin of the patch in tile coordinates */
    protected Point tileOrigin;

    /** Source terrain data */
    protected TerrainData terrainData;

    /** The view frustum used for this terrain */
    protected ViewFrustum viewFrustum;

    /** Neighbour references for calculations */
    private ROAMPatch northNeighbour;
    private ROAMPatch southNeighbour;
    private ROAMPatch eastNeighbour;
    private ROAMPatch westNeighbour;

    /** Raw vertex collection information */
    protected VertexData vertexData;

    /** The maximum Y for this patch */
    protected float maxY;

    /** The minimumY for this patch */
    protected float minY;

    /** Flag to indicate if a clear has been requested */
    protected boolean resetRequested;

    /** Flag to see if this is the first time used or not */
    private boolean firstUse;

    /**
     * Create a new patch based on the terrain and appearance information.
     *
     * @param terrain The raw height map info to use for this terrain
     * @param patchSize The number of grid points to use in the patch on a side
     * @param frustum The view frustum container used
     */
    protected ROAMPatch(TerrainData terrain,
                        int patchSize,
                        ViewFrustum frustum,
                        int patchX,
                        int patchY)
    {
        PATCH_SIZE = patchSize;

        terrainData = terrain;
        viewFrustum = frustum;
        this.patchX = patchX;
        this.patchY = patchY;

        firstUse = false;

        tileOrigin = new Point();
    }

    //----------------------------------------------------------
    // local public methods
    //----------------------------------------------------------

    /**
     * Change the view to the new position and orientation. In this
     * implementation the direction information is ignored because we have
     * the view frustum to use.
     *
     * @param position The location of the user in space
     * @param frustum The viewing frustum information for clipping
     * @param queueManager Manager for ordering terrain chunks
     */
    void setView(Tuple3f position,
                 ViewFrustum frustum,
                 QueueManager queueManager)
    {
        // Will be null if the patch is not in use currently, so ignore the
        // request.
        if(NWTree == null)
            return;

        viewFrustum = frustum;

        try
        {
            NWTree.updateTree(position,
                              frustum,
                              NWVariance,
                              TreeNode.UNDEFINED,
                              queueManager);
        }
        catch(RuntimeException re)
        {
            System.out.println("NW tree " + hashCode());
        }

        try
        {
            SETree.updateTree(position,
                              frustum,
                              SEVariance,
                              TreeNode.UNDEFINED,
                              queueManager);
        }
        catch(RuntimeException re)
        {
            System.out.println("NW tree " + hashCode());
        }
    }

    /**
     * Request an update to the geometry. If the geometry is visible then
     * tell J3D that we would like to update the geometry. It does not directly
     * do the update because we are using GeomByRef and so need to wait for the
     * renderer to tell us when it is OK to do the updates.
     */
    public abstract void updateGeometry();

    /**
     * Instruct this patch that it is about to be removed from active duty.
     * That means it should set all the geometry to be non-renderable, and
     * clear all references to neightbours. It may be re-activated at some
     * later stage.
     */
    public void clear()
    {
        resetRequested = true;

        vertexData.reset();

        NWTree.freeNode();
        SETree.freeNode();

        NWTree = null;
        SETree = null;

        setNorthNeighbour(null);
        setSouthNeighbour(null);
        setEastNeighbour(null);
        setWestNeighbour(null);

        // No need to clear out the variance tree as the only thing it really
        // cares about is the patchSize so that it can recompute the levels.
        // As the patches are fixed size, we can just ignore them.
    }

    /**
     * Setup this patch with new data from the terrain. The patch now exists
     * in a different part of the world. After construction, this can be called
     * at any time to set up the node for runtime use.
     *
     * @param xOrig The tileOrigin of the X grid coord for this patch in the
     *    global set of grid coordinates
     * @param yOrig The tileOrigin of the Y grid coord for this patch in the
     *    global set of grid coordinates
     */
    protected void setOrigin(int xOrig, int yOrig)
    {
        tileOrigin.x = xOrig / PATCH_SIZE;
        tileOrigin.y = yOrig / PATCH_SIZE;

        xOrigin = xOrig;
        yOrigin = yOrig;

        int height = yOrigin + PATCH_SIZE;
        int width = xOrigin + PATCH_SIZE;

        NWVariance = new VarianceTree(terrainData,
                                       PATCH_SIZE,
                                       xOrigin,
                                       yOrigin,
                                       width,
                                       height,
                                       xOrigin,
                                       height);

        SEVariance = new VarianceTree(terrainData,
                                      PATCH_SIZE,
                                      width,
                                      height,   // Left X, Y
                                      xOrigin,
                                      yOrigin,    // Right X, Y
                                      width,
                                      yOrigin);   // Apex X, Y

        NWTree = TreeNode.getTreeNode();
        SETree = TreeNode.getTreeNode();

        NWTree.newNode(xOrigin,
                       yOrigin,      // Left X, Y
                       width,
                       height,       // Right X, Y
                       xOrigin,
                       height,       // Apex X, Y
                       1,
                       terrainData,
                       viewFrustum,
                       TreeNode.UNDEFINED,
                       1,
                       NWVariance,
                       patchX,patchY);

        SETree.newNode(width,
                       height,       // Left X, Y
                       xOrigin,
                       yOrigin,      // Right X, Y
                       width,
                       yOrigin,      // Apex X, Y
                       1,
                       terrainData,
                       viewFrustum,
                       TreeNode.UNDEFINED,
                       1,
                       SEVariance,
                       patchX,patchY);

        maxY = Math.max(NWVariance.getMaxY(), SEVariance.getMaxY());
        minY = Math.min(NWVariance.getMinY(), SEVariance.getMinY());

        NWTree.baseNeighbour = SETree;
        SETree.baseNeighbour = NWTree;
    }

    /**
     * Reset this patch back to a simple patch like new.
     *
     * @param frustum The view information
     */
    void reset()
    {
        NWTree.reset(viewFrustum);
        SETree.reset(viewFrustum);

        NWTree.baseNeighbour = SETree;
        SETree.baseNeighbour = NWTree;
    }

    /**
     * Set the north neighbour of this patch to the new value. May be called
     * on an active or inactive patch.
     *
     * @param p The patch to set as the new neighbour
     */
    void setNorthNeighbour(ROAMPatch p)
    {
        northNeighbour = p;
    }

    /**
     * Set the south neighbour of this patch to the new value. May be called
     * on an active or inactive patch.
     *
     * @param p The patch to set as the new neighbour
     */
    void setSouthNeighbour(ROAMPatch p)
    {
        southNeighbour = p;

        if(southNeighbour != null)
        {
            SETree.rightNeighbour = southNeighbour.NWTree;
            southNeighbour.NWTree.rightNeighbour = SETree;
        }
    }

    /**
     * Set the east neighbour of this patch to the new value. May be called
     * on an active or inactive patch.
     *
     * @param p The patch to set as the new neighbour
     */
    void setEastNeighbour(ROAMPatch p)
    {
        eastNeighbour = p;
    }

    /**
     * Set the west neighbour of this patch to the new value. May be called
     * on an active or inactive patch.
     *
     * @param p The patch to set as the new neighbour
     */
    void setWestNeighbour(ROAMPatch p)
    {
        westNeighbour = p;

        if(westNeighbour != null)
        {
            NWTree.leftNeighbour = westNeighbour.SETree;
            westNeighbour.SETree.leftNeighbour = NWTree;
        }
    }

    /**
     * Update this patch to merge in with its new neighbours. Should only be
     * called after a patch has be brought back to activity and after
     * makeActive() is called.
     *
     * @param position The current view location
     * @param queueManager The queue to place newly generated items on
     */
    void updateEdges(Tuple3f position, QueueManager queueManager)
    {
        int old = -1;
        int neu = -1;

        if(southNeighbour != null)
        {
            while(old != 0 && neu != 0)
            {
                neu = SETree.edgeSplit(southNeighbour.NWTree,
                                       TreeNode.RIGHT_TO_LEFT,
                                       position,
                                       viewFrustum,
                                       queueManager);

                old = southNeighbour.NWTree.edgeSplit(SETree,
                                                      TreeNode.LEFT_TO_RIGHT,
                                                      position,
                                                      viewFrustum,
                                                      queueManager);
            }
        }

        if(northNeighbour != null)
        {
            old = -1;
            neu = -1;

            while(old != 0 && neu != 0)
            {
                neu = NWTree.edgeSplit(northNeighbour.SETree,
                                       TreeNode.LEFT_TO_RIGHT,
                                       position,
                                       viewFrustum,
                                       queueManager);

                old = northNeighbour.SETree.edgeSplit(NWTree,
                                                      TreeNode.RIGHT_TO_LEFT,
                                                      position,
                                                      viewFrustum,
                                                      queueManager);

            }
        }

        if(eastNeighbour != null)
        {
            old = -1;
            neu = -1;

            while(old != 0 && neu != 0)
            {
                neu = SETree.edgeSplit(eastNeighbour.NWTree,
                                       TreeNode.LEFT_TO_RIGHT,
                                       position,
                                       viewFrustum,
                                       queueManager);

                old = eastNeighbour.NWTree.edgeSplit(SETree,
                                                     TreeNode.RIGHT_TO_LEFT,
                                                     position,
                                                     viewFrustum,
                                                     queueManager);
            }
        }

        if(westNeighbour != null)
        {
            old = -1;
            neu = -1;

            while(old != 0 && neu != 0)
            {
                neu = NWTree.edgeSplit(westNeighbour.SETree,
                                       TreeNode.RIGHT_TO_LEFT,
                                       position,
                                       viewFrustum,
                                       queueManager);

                old = westNeighbour.SETree.edgeSplit(NWTree,
                                                     TreeNode.LEFT_TO_RIGHT,
                                                     position,
                                                     viewFrustum,
                                                     queueManager);
            }
        }
    }

    /**
     * Everything has been reset correctly - set active again.
     */
    void makeActive()
    {
        resetRequested = false;
    }

    /**
     * Fetch the tileOrigin of this patch in tile coordinates. This is effectively
     * the Tile ID, describing it in terms of X and Y positions.
     *
     * @return A point describing the tileOrigin of the patch
     */
    Point getTileOrigin()
    {
        return tileOrigin;
    }

    /**
     * Fetch the number of triangles that are currently visible in this patch.
     *
     * @return The number of visible triangles
     */
    int getTriangleCount()
    {
        return vertexData.getVertexCount() / 3;
    }

    //----------------------------------------------------------
    // local convenience methods
    //----------------------------------------------------------

    /**
     * Create the geometry needed for this patch. Just sets how many vertices
     * are to be used based on the triangles of the two halves of the tree.
     */
    protected void createGeometry()
    {
        vertexData.reset();

        if(NWTree.visible != ViewFrustum.OUT)
            NWTree.getTriangles(vertexData);

        if(SETree.visible != ViewFrustum.OUT)
            SETree.getTriangles(vertexData);
    }

    /**
     * Create a string representation of this patch.
     *
     * @return A string representation of the patch
     */
    public String toString2()
    {
        StringBuffer buf = new StringBuffer();

        if(westNeighbour != null)
            buf.append(westNeighbour.hashCode());
        else
            buf.append("-1");

        buf.append(',');

        if(southNeighbour != null)
            buf.append(southNeighbour.hashCode());
        else
            buf.append("-1");

        return buf.toString();
    }
}
