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
import java.util.LinkedList;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Shape3D;
import javax.media.j3d.GeometryUpdater;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3f;

// Application specific imports
import org.j3d.terrain.ViewFrustum;
import org.j3d.terrain.TerrainData;

/**
 * A patch represents a single piece of terrain geometry that can be
 * rendered as a standalone block.
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
 * If the patch is used with one of the tiled terrain formats, then it is
 * expected that the user instance will manage the change in texture
 * references of the passed in Appearance.
 *
 * @author  Justin Couch, Paul Byrne
 * @version
 */
class Patch implements GeometryUpdater
{
    /** The final size in number of grid points for this patch */
    private final int PATCH_SIZE;

    /** The values of the nodes in the NW triangle of this patch */
    TreeNode NWTree;

    /** The values of the nodes in the NW triangle of this patch */
    TreeNode SETree;

    /** Variance holders for the two sub-trees of this patch */
    private VarianceTree NWVariance;
    private VarianceTree SEVariance;

    /** The J3D geometry for this patch */
    private Shape3D shape3D;

    /** The origin grid coordinates. */
    private int xOrigin;
    private int yOrigin;

    /** The origin of the patch in tile coordinates */
    private Point tileOrigin;

    /** Source terrain data */
    private TerrainData terrainData;

    /** The view frustum used for this terrain */
    private ViewFrustum viewFrustum;

    /** Neighbour references for calculations */
    private Patch westPatchNeighbour;
    private Patch southPatchNeighbour;

    /** Raw vertex collection information */
    private VertexData vertexData;

    /** The java3D geometry that gets rendered */
    private TriangleArray geometry;

    /** The bounding box of the shape */
    private BoundingBox bounds;

    /** The appearance used by this patch */
    private Appearance appearance;

    /** The maximum Y for this patch */
    private float maxY;

    /** The minimumY for this patch */
    private float minY;

    /** Flag to indicate if a clear has been requested */
    private boolean resetRequested;

    /** Flag to see if this is the first time used or not */
    private boolean firstUse;

    /**
     * Create a new patch based on the terrain and appearance information.
     *
     * @param terrain The raw height map info to use for this terrain
     * @param patchSize The number of grid points to use in the patch on a side
     * @param app The global appearance object to use for this patch
     * @param frustum The view frustum container used
     */
    Patch(TerrainData terrain,
          int patchSize,
          Appearance app,
          ViewFrustum frustum)
    {
        PATCH_SIZE = patchSize;

        terrainData = terrain;
        viewFrustum = frustum;
        appearance = app;

        firstUse = false;

        tileOrigin = new Point();
        bounds = new BoundingBox();

        boolean has_texture = terrainData.hasTexture();
        boolean has_color = terrainData.hasColor();
        vertexData = new VertexData(PATCH_SIZE,
                                    has_texture,
                                    has_color);

        int format = TriangleArray.COORDINATES |
                     TriangleArray.BY_REFERENCE;

        if(has_texture)
            format |= TriangleArray.TEXTURE_COORDINATE_2;

        if(has_color)
            format |= TriangleArray.COLOR_3;

        geometry = new TriangleArray(PATCH_SIZE * PATCH_SIZE * 2 * 3, format);

        geometry.setCapability(TriangleArray.ALLOW_REF_DATA_WRITE);
        geometry.setCapability(TriangleArray.ALLOW_COUNT_WRITE);
        geometry.setCoordRefFloat(vertexData.getCoords());

        if(has_texture)
            geometry.setTexCoordRefFloat(0, vertexData.getTextureCoords());

        if(has_color)
            geometry.setColorRefByte(vertexData.getColors());

        shape3D = new Shape3D(geometry, app);
        shape3D.setCapability(Shape3D.ALLOW_BOUNDS_WRITE);
        shape3D.setBoundsAutoCompute(false);

        // Just as a failsafe, always set the terrain data in the user
        // data section of the node so that terrain code will find it
        // again, even if the top user is stupid.
        shape3D.setUserData(terrainData);
    }

    //----------------------------------------------------------
    // Methods required by GeometryUpdater
    //----------------------------------------------------------

    /**
     * Update the J3D geometry array for data now.
     *
     * @param geom The geometry object to update
     */
    public void updateData(Geometry geom)
    {
        if(!resetRequested)
            createGeometry();

        TriangleArray tri = (TriangleArray)geom;
        tri.setValidVertexCount(vertexData.getVertexCount());
    }

    //----------------------------------------------------------
    // local public methods
    //----------------------------------------------------------

    /**
     * Get the appearance in use with this patch. Used so the code can
     * change the textures as the patch moves.
     */
    Appearance getAppearance()
    {
        return appearance;
    }

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
    void updateGeometry()
    {
        if(NWTree != null &&
           (NWTree.visible != ViewFrustum.OUT ||
            SETree.visible != ViewFrustum.OUT ||
            vertexData.getVertexCount() != 0))
        {
            geometry.updateData(this);
        }
    }

    /**
     * Instruct this patch that it is about to be removed from active duty.
     * That means it should set all the geometry to be non-renderable, and
     * clear all references to neightbours. It may be re-activated at some
     * later stage.
     */
    void clear()
    {
        resetRequested = true;

        vertexData.reset();

        NWTree.freeNode();
        SETree.freeNode();

        NWTree = null;
        SETree = null;

        setWestNeighbour(null);
        setSouthNeighbour(null);

        // No need to clear out the variance tree as the only thing it really
        // cares about is the patchSize so that it can recompute the levels.
        // As the patches are fixed size, we can just ignore them.

        geometry.updateData(this);
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
    void setOrigin(int xOrig, int yOrig)
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
                       NWVariance);

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
                       SEVariance);

        maxY = Math.max(NWVariance.getMaxY(), SEVariance.getMaxY());
        minY = Math.min(NWVariance.getMinY(), SEVariance.getMinY());

        NWTree.baseNeighbour = SETree;
        SETree.baseNeighbour = NWTree;

        double x_step = terrainData.getGridXStep();
        double y_step = terrainData.getGridYStep();

        bounds.setLower(xOrig * x_step, minY, -height * y_step);
        bounds.setUpper(width * x_step, maxY, -yOrig * y_step);

        shape3D.setBounds(bounds);
    }

    /**
     * Set the west neighbour of this patch to the new value. May be called
     * on an active or inactive patch.
     *
     * @param p The patch to set as the new neighbour
     */
    void setWestNeighbour(Patch p)
    {
        westPatchNeighbour = p;

        if(westPatchNeighbour != null)
        {
            NWTree.leftNeighbour = westPatchNeighbour.SETree;
            westPatchNeighbour.SETree.leftNeighbour = NWTree;
        }
    }

    /**
     * Set the south neighbour of this patch to the new value. May be called
     * on an active or inactive patch.
     *
     * @param p The patch to set as the new neighbour
     */
    void setSouthNeighbour(Patch p)
    {
        southPatchNeighbour = p;

        if(southPatchNeighbour != null)
        {
            SETree.rightNeighbour = southPatchNeighbour.NWTree;
            southPatchNeighbour.NWTree.rightNeighbour = SETree;
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

    /**
     * Get the shape node that is used to represent this patch.
     *
     * @return The shape node
     */
    Shape3D getShape3D()
    {
        return shape3D;
    }

    //----------------------------------------------------------
    // local convenience methods
    //----------------------------------------------------------

    /**
     * Create the geometry needed for this patch. Just sets how many vertices
     * are to be used based on the triangles of the two halves of the tree.
     */
    private void createGeometry()
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

        if(westPatchNeighbour != null)
            buf.append(westPatchNeighbour.hashCode());
        else
            buf.append("-1");

        buf.append(',');

        if(southPatchNeighbour != null)
            buf.append(southPatchNeighbour.hashCode());
        else
            buf.append("-1");

        return buf.toString();
    }
}
