/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)SplitMergeLandscape.java 1.1 02/01/10 09:27:31
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    -Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *    -Redistribution in binary form must reproduct the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed,licensed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */
package org.j3d.terrain.roam;

// Standard imports
import javax.media.j3d.*;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

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
 * @version $Revision: 1.7 $
 */
public class SplitMergeLandscape extends Landscape
{
    /** Message for when the patchSize is <= 0 */
    private static final String NEG_PATCH_SIZE_MSG =
        "The patch size provided is negative or zero.";

    /** Message for when patchSize is not a ^2 */
    private static final String NOT_POW2_MSG =
        "The patchSize is not a power of two";

    /** Patch size in grid points if the user doesn't supply one */
    private static final int DEFAULT_PATCH_SIZE = 64;

    /**
    /** The patch size to use for this landscape */
    private final int patchSize;

    /** The terrain data size */
    private final int terrainDataType;

    /** The collection of all patches in this landscape */
    private ArrayList patches = new ArrayList();

    /** Queue manager for the pathces needing splits or merges each frame */
    private TreeQueueManager queueManager = new TreeQueueManager();

    /** Number of visible triangles */
    private int triCount = 0;

    /** The default generator if none are supplied */
    private AppearanceGenerator defaultAppGenerator;

    /** Maximum bounding point of the view frustum. Used as a working var. */
    private Point3d maxViewBound;

    /** Minimum bounding point of the view frustum. Used as a working var. */
    private Point3d minViewBound;

    /**
     * Creates new Landscape based on the view information and the terrain
     * data. If the terrain data is based on static data, the terrain is
     * built using the default patch size.
     *
     * @param view The view frustum looking at this landscape
     * @param data The raw data for the terrain
     */
    public SplitMergeLandscape(ViewFrustum view, TerrainData data)
    {
        super(view, data);

        terrainDataType = data.getSourceDataType();

        switch(terrainDataType)
        {
            case TerrainData.STATIC_DATA:
            case TerrainData.FREEFORM_DATA:
                patchSize = DEFAULT_PATCH_SIZE;
                break;

            case TerrainData.TILED_DATA:
                patchSize = ((TiledTerrainData)data).getTileSize();
                break;

            default:
                patchSize = 0;
                System.out.println("Unknown terrain type");
        }
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

        if(patchSize >= 0)
            throw new IllegalArgumentException(NEG_PATCH_SIZE_MSG);

        if(!power2Check(patchSize))
            throw new IllegalArgumentException(NOT_POW2_MSG);

        // check for a power of two.
        terrainDataType = data.getSourceDataType();

        if(terrainDataType == TerrainData.TILED_DATA)
            this.patchSize = patchSize;
        else
            this.patchSize = ((TiledTerrainData)data).getTileSize();
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

        switch(terrainDataType)
        {
            case TerrainData.STATIC_DATA:
            case TerrainData.FREEFORM_DATA:
                patchSize = DEFAULT_PATCH_SIZE;
                break;

            case TerrainData.TILED_DATA:
                patchSize = ((TiledTerrainData)data).getTileSize();
                break;

            default:
                patchSize = 0;
                System.out.println("Unknown terrain type");
        }
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

        if(patchSize >= 0)
            throw new IllegalArgumentException(NEG_PATCH_SIZE_MSG);

        if(!power2Check(patchSize))
            throw new IllegalArgumentException(NOT_POW2_MSG);

        // check for a power of two.
        terrainDataType = data.getSourceDataType();

        if(terrainDataType == TerrainData.TILED_DATA)
            this.patchSize = patchSize;
        else
            this.patchSize = ((TiledTerrainData)data).getTileSize();
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
        minViewBound = new Point3d();
        maxViewBound = new Point3d();

        switch(terrainDataType)
        {
            case TerrainData.STATIC_DATA:
                createStaticPatches();
                break;

            case TerrainData.TILED_DATA:
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
        float accuracy = (float)Math.toRadians(0.1);
        TreeNode splitCandidate;
        TreeNode mergeCandidate;
        boolean done;
        int size = patches.size();

// Does not handle the various terrain types yet....

        for(int i = 0; i < size; i++)
        {
            Patch p = (Patch)patches.get(i);

            p.setView(position, landscapeView, queueManager);
        }

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
            else if(mergeCandidate!=null && splitCandidate == null)
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


        for(int i = 0; i < size; i++)
        {
            Patch p = (Patch)patches.get(i);

            p.updateGeometry();
        }

        queueManager.clear();
    }

    /**
     * Create patches for a tiled terrain.
     *
     * @param position The position of the camera
     * @param direction The direction the camera is looking
     */
    private void createTiledPatches(Tuple3f position, Vector3f direction)
    {
        TiledTerrainData t_data = (TiledTerrainData)terrainData;
        Rectangle reqd_bounds = new Rectangle();

        double x_spacing = t_data.getGridXStep();
        double y_spacing = t_data.getGridYStep();
        int tile_size = t_data.getTileSize();

        landscapeView.getBounds(minViewBound, maxViewBound);

        // calc the tile that the current viewpoint is in
        double min_tile_x = minViewBound.x / (x_spacing * tile_size);
        double min_tile_y = minViewBound.z / (y_spacing * tile_size);

        reqd_bounds.x = (int)Math.floor(min_tile_x);
        reqd_bounds.y = (int)Math.floor(min_tile_y);

        double max_tile_x = maxViewBound.x / (x_spacing * tile_size);
        double max_tile_y = maxViewBound.z / (y_spacing * tile_size);

        reqd_bounds.width = reqd_bounds.x + (int)Math.ceil(max_tile_x);
        reqd_bounds.height = reqd_bounds.y + (int)Math.ceil(max_tile_y);

        // request the bounds be set to the minimum required
        t_data.setActiveBounds(reqd_bounds);

        Patch[] westPatchNeighbour = new Patch[reqd_bounds.width];
        Patch southPatchNeighbour = null;
        Patch p = null;

        AppearanceGenerator app_gen = getAppGenerator();
        Appearance app;

        float patch_1 = 1 / (float)patchSize;
        int east = (int)Math.floor(min_tile_x);
        int y_tile = reqd_bounds.y;

        for( ; east <= reqd_bounds.width; east += patchSize)
        {
            int north = (int)Math.floor(min_tile_y);
            int x_tile = reqd_bounds.x;

            for(; north <= reqd_bounds.height; north += patchSize)
            {
                app = app_gen.createAppearance();
                app.setTexture(t_data.getTexture(x_tile, y_tile));

                int w_pos = (int)(north * patch_1);

                p = new Patch(terrainData,
                              patchSize,
                              east,
                              north,
                              app,
                              landscapeView,
                              westPatchNeighbour[w_pos],
                              southPatchNeighbour);

                patches.add(p);

                triCount += 2;
                addChild(p.getShape3D());

                southPatchNeighbour = p;
                westPatchNeighbour[w_pos] = p;
                x_tile++;
            }

            southPatchNeighbour = null;
            y_tile++;
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

        Patch[] westPatchNeighbour = new Patch[width];
        Patch southPatchNeighbour = null;
        Patch p = null;

        AppearanceGenerator app_gen = getAppGenerator();
        Appearance app;

        app = app_gen.createAppearance();
        app.setTexture(t_data.getTexture());

        float patch_1 = 1 / (float)patchSize;

        for(int east = 0 ; east <= width; east += patchSize)
        {
            for(int north = 0; north <= depth; north += patchSize)
            {
                int w_pos = (int)(north * patch_1);

                p = new Patch(terrainData,
                              patchSize,
                              east,
                              north,
                              app,
                              landscapeView,
                              westPatchNeighbour[w_pos],
                              southPatchNeighbour);

                patches.add(p);

                triCount += 2;
                addChild(p.getShape3D());

                southPatchNeighbour = p;
                westPatchNeighbour[w_pos] = p;
            }

            southPatchNeighbour = null;
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
        return (shift_val == 0);
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
