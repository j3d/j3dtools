/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.terrain.roam;

// Standard imports
import javax.media.j3d.*;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3f;

// Application specific imports
import org.j3d.terrain.TerrainData;
import org.j3d.terrain.roam.ROAMPatch;
import org.j3d.terrain.roam.VertexData;
import org.j3d.util.frustum.ViewFrustum;

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
class Patch extends ROAMPatch
    implements GeometryUpdater
{
    /** The J3D geometry for this patch */
    private Shape3D shape3D;

    /** The java3D geometry that gets rendered */
    private TriangleArray geometry;

    /** The bounding box of the shape */
    private BoundingBox bounds;

    /** The appearance used by this patch */
    private Appearance appearance;

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
          ViewFrustum frustum,
          int patchX,
          int patchY)
    {
        super(terrain, patchSize, frustum, patchX, patchY);

        appearance = app;

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
     * Request an update to the geometry. If the geometry is visible then
     * tell J3D that we would like to update the geometry. It does not directly
     * do the update because we are using GeomByRef and so need to wait for the
     * renderer to tell us when it is OK to do the updates.
     */
    public void updateGeometry()
    {
        if(NWTree != null &&
           (NWTree.getVisibility() != ViewFrustum.OUT ||
            SETree.getVisibility() != ViewFrustum.OUT ||
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
    public void clear()
    {
        super.clear();

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
    protected void setOrigin(int xOrig, int yOrig)
    {
        super.setOrigin(xOrig, yOrig);

        int height = xOrig + PATCH_SIZE;
        int width = yOrig + PATCH_SIZE;

        double x_step = terrainData.getGridXStep();
        double y_step = terrainData.getGridYStep();

        bounds.setLower(xOrig * x_step, minY, -height * y_step);
        bounds.setUpper(width * x_step, maxY, -yOrig * y_step);

        shape3D.setBounds(bounds);
    }

    /**
     * Get the shape node that is used to represent this patch.
     *
     * @return The shape node
     */
    Node getSceneGraphObject()
    {
        return shape3D;
    }
}
