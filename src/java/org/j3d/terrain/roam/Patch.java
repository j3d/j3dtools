/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)Patch.java 1.1 02/01/10 09:27:28
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
import java.util.LinkedList;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Shape3D;
import javax.media.j3d.GeometryUpdater;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Color3f;
import javax.vecmath.Point2d;

// Application specific imports
import org.j3d.terrain.ViewFrustum;
import org.j3d.terrain.TerrainData;

/**
 *
 * @author  paulby
 * @version
 */
class Patch implements GeometryUpdater
{
    private final int PATCH_SIZE;

    TreeNode NWTree;
    TreeNode SETree;
    private VarianceTree NWVariance;
    private VarianceTree SEVariance;
    private Shape3D shape3D;

    private int xOrig;
    private int yOrig;

    private TerrainData terrainData;
    private Patch westPatchNeighbour;
    private Patch southPatchNeighbour;
    private VertexData vertexData;

    private TriangleArray geom;

    /** The maximum Y for this patch */
    private float maxY;

    /** The minimumY for this patch */
    private float minY;

    /**
     * Creates new Patch
     *
     * @param westPatchNeighbour the Patch to the west of this patch
     * @param southPatchNeighbour the Patch to the south of this patch
     */
    Patch(TerrainData terrainData,
          int patchSize,
          int xOrig,
          int yOrig,
          Appearance app,
          ViewFrustum landscapeView,
          Patch westPatchNeighbour,
          Patch southPatchNeighbour)
    {
        int height = yOrig + patchSize;
        int width = xOrig + patchSize;

        this.xOrig = xOrig;
        this.yOrig = yOrig;
        this.PATCH_SIZE = patchSize;
        this.terrainData = terrainData;
        this.westPatchNeighbour = westPatchNeighbour;
        this.southPatchNeighbour = southPatchNeighbour;

        vertexData = new VertexData(PATCH_SIZE);

        int format = TriangleArray.COORDINATES |
                     TriangleArray.COLOR_3 |
                     TriangleArray.BY_REFERENCE |
                     TriangleArray.TEXTURE_COORDINATE_2;

        geom = new TriangleArray(PATCH_SIZE * PATCH_SIZE * 2 * 3, format);

        geom.setCapability(TriangleArray.ALLOW_REF_DATA_WRITE);
        geom.setCapability(TriangleArray.ALLOW_COUNT_WRITE);
        geom.setCoordRefFloat(vertexData.getCoords());
        geom.setTexCoordRefFloat(0, vertexData.getTextureCoords());
        geom.setColorRefByte(vertexData.getColors());

        NWVariance = new VarianceTree(terrainData,
                                       PATCH_SIZE,
                                       xOrig, yOrig,
                                       width, height,
                                       xOrig, height);

        NWTree = new TreeNode(xOrig, yOrig,        // Left X, Y
                               width, height,       // Right X, Y
                               xOrig, height,       // Apex X, Y
                               1,
                               terrainData,
                               landscapeView,
                               TreeNode.UNDEFINED,
                               1,
                               NWVariance);

        SEVariance = new VarianceTree(terrainData,
                                       PATCH_SIZE,
                                       width, height,       // Left X, Y
                                       xOrig, yOrig,        // Right X, Y
                                       width, yOrig);       // Apex X, Y


        SETree = new TreeNode(width, height,       // Left X, Y
                               xOrig, yOrig,        // Right X, Y
                               width, yOrig,        // Apex X, Y
                               1,
                               terrainData,
                               landscapeView,
                               TreeNode.UNDEFINED,
                               1,
                               SEVariance);

        maxY = Math.max(NWVariance.getMaxY(), SEVariance.getMaxY());
        minY = Math.min(NWVariance.getMinY(), SEVariance.getMinY());

        NWTree.baseNeighbour = SETree;
        SETree.baseNeighbour = NWTree;

        if(westPatchNeighbour!=null)
        {
            NWTree.leftNeighbour = westPatchNeighbour.SETree;
            westPatchNeighbour.SETree.leftNeighbour = NWTree;
        }

        if(southPatchNeighbour!=null)
        {
            SETree.rightNeighbour = southPatchNeighbour.NWTree;
            southPatchNeighbour.NWTree.rightNeighbour = SETree;
        }

        Point3d min_bounds =
            new Point3d(xOrig * terrainData.getGridXStep(),
                        minY,
                        -(yOrig + height) * terrainData.getGridYStep());

        Point3d max_bounds =
            new Point3d((xOrig + width) * terrainData.getGridXStep(),
                        maxY,
                        -yOrig * terrainData.getGridYStep());

        shape3D = new Shape3D(geom, app);
        shape3D.setBoundsAutoCompute(false);
        shape3D.setBounds(new BoundingBox(min_bounds, max_bounds));
    }

    //----------------------------------------------------------
    // Methods required by GeometryUpdater
    //----------------------------------------------------------

    public void updateData(Geometry geom)
    {
        createGeometry((TriangleArray)geom);
    }

    //----------------------------------------------------------
    // local convenience methods
    //----------------------------------------------------------

    void reset(ViewFrustum landscapeView)
    {
        NWTree.reset(landscapeView);
        SETree.reset(landscapeView);

        NWTree.baseNeighbour = SETree;
        SETree.baseNeighbour = NWTree;

        if(westPatchNeighbour != null)
        {
            NWTree.leftNeighbour = westPatchNeighbour.SETree;
            westPatchNeighbour.SETree.leftNeighbour = NWTree;
        }

        if(southPatchNeighbour != null)
        {
            SETree.rightNeighbour = southPatchNeighbour.NWTree;
            southPatchNeighbour.NWTree.rightNeighbour = SETree;
        }
    }

    /**
     *
     */
    void setView(Point3f position,
                 Vector3f direction,
                 ViewFrustum landscapeView,
                 QueueManager queueManager)
    {
        NWTree.updateTree(position,
                          landscapeView,
                          NWVariance,
                          TreeNode.UNDEFINED,
                          queueManager);

        SETree.updateTree(position,
                          landscapeView,
                          SEVariance,
                          TreeNode.UNDEFINED,
                          queueManager);

        /*
        NWTree.split(landscapeView);
        NWTree.rightChild.split(landscapeView);
        NWTree.rightChild.rightChild.split(landscapeView);
        NWTree.rightChild.rightChild.leftChild.split(landscapeView);
        NWTree.rightChild.rightChild.leftChild.rightChild.split(landscapeView);
        NWTree.rightChild.rightChild.leftChild.rightChild.leftChild.split(landscapeView);
         */
    }

    void updateGeometry()
    {
        if(NWTree.visible != ViewFrustum.OUT ||
           SETree.visible != ViewFrustum.OUT ||
            vertexData.getVertexCount() != 0)
        {
            geom.updateData(this);
        }
    }

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

    private void createGeometry(TriangleArray geom)
    {
        vertexData.reset();

        if(NWTree.visible!=ViewFrustum.OUT)
            NWTree.getTriangles(vertexData);

        if(SETree.visible != ViewFrustum.OUT)
            SETree.getTriangles(vertexData);

        /*
        System.out.println(vertexData.getVertexCount() + "  "+this);
        float[] v = vertexData.getCoords();
        for(int i=0; i<9; i++)
            System.out.print(v[i]+" ");
        System.out.println();
         */

        geom.setValidVertexCount(vertexData.getVertexCount());
    }
}
