/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)TreeNode.java 1.1 02/01/10 09:27:31
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

import javax.vecmath.Tuple3f;

// Application specific imports
import org.j3d.terrain.ViewFrustum;
import org.j3d.terrain.TerrainData;

/**
 * Represents a single node of the triangle mesh of the patch.
 *
 * @author  Paul Byrne, Justin Couch
 * @version
 */
class TreeNode
{
    /** The visibility status of this node in the tree is not known. */
    public static final int UNDEFINED = -1;

    /** Child tree node on the left side of the diamond */
    TreeNode leftChild;

    /** Child tree node on the right side of the diamond */
    TreeNode rightChild;

    TreeNode baseNeighbour;
    TreeNode leftNeighbour;
    TreeNode rightNeighbour;

    TreeNode parent;

    private int leftX, leftY;       // Pointers into terrainData
    private int rightX, rightY;
    private int apexX, apexY;

    private int node;

    private int depth;      // For debugging

    int visible = UNDEFINED;

    // The three corners of the triangle
    private float p1X, p1Y, p1Z;
    private float p2X, p2Y, p2Z;
    private float p3X, p3Y, p3Z;

    // Texture coordinates values
    private float p1tS, p1tT;
    private float p2tS, p2tT;
    private float p3tS, p3tT;

    // Color values
    private float p1R, p1G, p1B;
    private float p2R, p2G, p2B;
    private float p3R, p3G, p3B;

    private TerrainData terrainData;
    private VarianceTree varianceTree;

    float variance = 0f;
    float diamondVariance = 0f;

    boolean diamond = false;

    private boolean textured;

    /**
     * A cache of instances of ourselves to help avoid too much object
     * creation and deletion.
     */
    private static LinkedList nodeCache = new LinkedList();


    /**
     * Default constructor for use by TreeNodeCache.
     */
    private TreeNode()
    {
    }


    /**
     * Used to populate a node retrieved from the TreeNodeCache
     * setting the same state as creating a new TreeNode would.
     */
    void newNode(int leftX,
                 int leftY,
                 int rightX,
                 int rightY,
                 int apexX,
                 int apexY,
                 int node,
                 TerrainData terrainData,
                 ViewFrustum frustum,
                 int parentVisible,
                 int depth,
                 VarianceTree varianceTree)
    {
        this.leftX = leftX;
        this.leftY = leftY;
        this.rightX = rightX;
        this.rightY = rightY;
        this.apexX = apexX;
        this.apexY = apexY;
        this.node = node;
        this.terrainData = terrainData;
        this.depth = depth;
        this.varianceTree = varianceTree;

        init(frustum, parentVisible);
    }

    /**
     * Check to see if this treenode is a leaf or a branch. A leaf does not
     * have a left-hand child node.
     *
     * @return true if this is a leaf
     */
    boolean isLeaf()
    {
        return (leftChild == null);
    }

    /**
     * Place this node and all it's children in the TreeNodeCache
     */
    void freeNode()
    {
        if(leftChild != null)
        {
            leftChild.freeNode();
            leftChild = null;
        }

        if(rightChild != null)
        {
            rightChild.freeNode();
            rightChild = null;
        }

        baseNeighbour = null;
        leftNeighbour = null;
        rightNeighbour = null;

        parent = null;
        diamond = false;
        visible = UNDEFINED;

        addTreeNode(this);
    }

    /**
     * Request the recomputation of the variance of this node and place the
     * node on the queue ready for processing.
     *
     * @param position The location to compute the value from
     * @param queueManager The queue to place the node on
     */
    void computeVariance(Tuple3f position, QueueManager queueManager)
    {
        computeVariance(position);

        queueManager.addTriangle(this);
    }

    /**
     * If this triangle was half of a diamond then remove the
     * diamond from the diamondQueue
     *
     * @param queueManager The queue to remove the node from
     */
    void removeDiamond(QueueManager queueManager)
    {
        if(diamond)
        {
            queueManager.removeDiamond(this);
            diamondVariance = 0f;
            diamond = false;
        }
        else if(baseNeighbour != null && baseNeighbour.diamond)
        {
            queueManager.removeDiamond(baseNeighbour);
            baseNeighbour.diamondVariance = 0f;
            baseNeighbour.diamond = false;
        }
    }

    /**
     * Split this tree node into two smaller triangle tree nodes.
     *
     * @param position The current view location
     * @param frustum The view information
     * @param queueManager The queue to place newly generated items on
     * @return The number of triangles generated as a result
     */
    int split(Tuple3f position,
              ViewFrustum frustum,
              QueueManager queueManager)
    {
        int triCount = 0;

        if(leftChild != null || rightChild != null)
            throw new RuntimeException(" Triangle already split "+ hashCode());

        if(baseNeighbour != null)
        {
            if(baseNeighbour.baseNeighbour != this)
            {
                triCount += baseNeighbour.split(position,
                                                frustum,
                                                queueManager);
            }

            split2(position, frustum, queueManager);
            triCount++;
            baseNeighbour.split2(position, frustum, queueManager);
            triCount++;

            leftChild.rightNeighbour = baseNeighbour.rightChild;
            rightChild.leftNeighbour = baseNeighbour.leftChild;
            baseNeighbour.leftChild.rightNeighbour = rightChild;
            baseNeighbour.rightChild.leftNeighbour = leftChild;

            diamondVariance = Math.max(variance, baseNeighbour.variance);
            diamond = true;
            queueManager.addDiamond(this);
        }
        else
        {
            split2(position, frustum, queueManager);
            triCount++;

            diamondVariance = variance;
            diamond = true;
            queueManager.addDiamond(this);
        }

        return triCount;
    }

    /**
     * Merge the children nodes of this node into a single triangle.
     *
     * @param queueManager The queue to put the merged node on
     * @return The number of triangles that were reduced as a result
     */
    int merge(QueueManager queueManager)
    {
        int trisRemoved = 0;

        if(baseNeighbour != null && baseNeighbour.baseNeighbour != this)
        {
            throw new RuntimeException("Illegal merge");
        }

        merge(this, queueManager);
        trisRemoved++;
        checkForNewDiamond(parent, queueManager);

        if(baseNeighbour != null)
        {
            merge(baseNeighbour, queueManager);
            trisRemoved++;
            checkForNewDiamond(baseNeighbour.parent, queueManager);
        }

        queueManager.removeDiamond(this);
        diamond = false;
        diamondVariance = 0;

        return trisRemoved;
    }

    /**
     * Add the coordinates for this triangle to the list of vertex information.
     * Updates are to include colour and texture information as needed.
     *
     * @param vertexData The place to put the extra vertex information
     */
    void getTriangles(VertexData vertexData)
    {
        if(leftChild == null)
        {
            if((visible != ViewFrustum.OUT) && (visible != UNDEFINED))
            {
                switch(vertexData.dataType)
                {
                    case VertexData.COORD_ONLY:
                        vertexData.addVertex(p1X, p1Y, p1Z);
                        vertexData.addVertex(p2X, p2Y, p2Z);
                        vertexData.addVertex(p3X, p3Y, p3Z);
                        break;

                    case VertexData.COLOR_ONLY:
                        vertexData.addVertex(p1X, p1Y, p1Z, p1R, p1G, p1B);
                        vertexData.addVertex(p2X, p2Y, p2Z, p2R, p2G, p2B);
                        vertexData.addVertex(p3X, p3Y, p3Z, p3R, p3G, p3B);
                        break;

                    case VertexData.TEXTURE_ONLY:
                        vertexData.addVertex(p1X, p1Y, p1Z, p1tS, p1tT);
                        vertexData.addVertex(p2X, p2Y, p2Z, p2tS, p2tT);
                        vertexData.addVertex(p3X, p3Y, p3Z, p3tS, p3tT);
                        break;

                    case VertexData.TEXTURE_AND_COLOR:
                        vertexData.addVertex(p1X, p1Y, p1Z,
                                             p1R, p1G, p1B,
                                             p1tS, p1tT);
                        vertexData.addVertex(p2X, p2Y, p2Z,
                                             p2R, p2G, p2B,
                                             p2tS, p2tT);
                        vertexData.addVertex(p3X, p3Y, p3Z,
                                             p3R, p3G, p3B,
                                             p3tS, p3tT);
                        break;
                }
            }
        }
        else
        {
            leftChild.getTriangles(vertexData);
            rightChild.getTriangles(vertexData);
        }
    }

    /**
     * Check to see if this tree node is visible. This is part of the
     * optimisation step to prevent recalculating the entire visibility if
     * the system is the same as before. Only update now if the parent was
     * not visible before.
     *
     * @param frustum The view frustum to check against
     * @return The visibility status for this frustum - IN, OUT, CLIPPED
     */
    int checkVisibility(ViewFrustum frustum)
    {
        return frustum.isTriangleInFrustum(p1X, p1Y, p1Z,
                                           p2X, p2Y, p2Z,
                                           p3X, p3Y, p3Z);
    }

    /**
     * Update the tree and variance information for the new view position.
     *
     *
     * @param frustum view information at start time
     * @param position The location to compute the value from
     * @param varianceTree Nested set of variances for each level
     * @param parentVisible Flag about the visibility state of the parent
     *    tree node
     * @param queueManager The queue to put the merged node on
     */
    void updateTree(Tuple3f position,
                    ViewFrustum frustum,
                    VarianceTree varianceTree,
                    int parentVisible,
                    QueueManager queueManager)
    {
        if(parentVisible == UNDEFINED ||
           parentVisible == ViewFrustum.CLIPPED)
        {
            visible = frustum.isTriangleInFrustum(p1X, p1Y, p1Z,
                                                        p2X, p2Y, p2Z,
                                                        p3X, p3Y, p3Z);
        }
        else
            visible = parentVisible;

        if(leftChild == null &&
           rightChild == null &&
           depth < varianceTree.getMaxDepth() &&
           visible != ViewFrustum.OUT)
        {
            computeVariance(position);

            queueManager.addTriangle(this);
        }
        else
        {
            if(leftChild != null)
                leftChild.updateTree(position,
                                     frustum,
                                     varianceTree,
                                     visible,
                                     queueManager);

            if(rightChild != null)
                rightChild.updateTree(position,
                                      frustum,
                                      varianceTree,
                                      visible,
                                      queueManager);

            if(diamond)
            {
// BUG Here, baseNeighbour may not have had it's variance updated
// for the new position
                if(visible != ViewFrustum.OUT)
                {
                    computeVariance(position);

                    if(baseNeighbour != null)
                        diamondVariance = Math.max(variance,
                                                   baseNeighbour.variance);
                    else
                        diamondVariance = variance;
                }
                else
                {
                    diamondVariance = Float.MIN_VALUE;
                }

                queueManager.addDiamond(this);
            }
        }
    }

    /**
     * Either return a node from the cache or if the cache is empty, return
     * a new tree node.
     */
    static TreeNode getTreeNode()
    {
        TreeNode ret_val;

        if(nodeCache.size() > 0)
            ret_val = (TreeNode)nodeCache.removeFirst();
        else
            ret_val = new TreeNode();

        return ret_val;
    }

    //----------------------------------------------------------
    // local convenience methods
    //----------------------------------------------------------

    /**
     * Internal common initialization for the startup of the class.
     *
     * @param frustum view information at start time
     * @param parentVisible Flag about the visibility state of the parent
     *    tree node
     */
    private void init(ViewFrustum frustum, int parentVisible)
    {
        float[] tmp = new float[3];
        float[] tex = new float[2];
        float[] col = new float[3];

        int type = terrainData.hasTexture() ? 1 : 0;

        type += terrainData.hasColor()? 2 : 0;

        switch(type)
        {
            case 0:   // No color or texture
                terrainData.getCoordinate(tmp, leftX, leftY);
                break;

            case 1:   // Texture only
                terrainData.getCoordinateWithTexture(tmp, tex, leftX, leftY);
                p1tS = tex[0];
                p1tT = tex[1];
                break;

            case 2:     // Color only
                terrainData.getCoordinateWithColor(tmp, col, leftX, leftY);
                p1R = col[0];
                p1G = col[1];
                p1B = col[2];

                break;

            case 3:     // Both texture and color.
                terrainData.getCoordinate(tmp, tex, col, leftX, leftY);
                p1tS = tex[0];
                p1tT = tex[1];

                p1R = col[0];
                p1G = col[1];
                p1B = col[2];
                break;
        }

        p1X = tmp[0];
        p1Y = tmp[1];
        p1Z = tmp[2];


        switch(type)
        {
            case 0:   // No color or texture
                terrainData.getCoordinate(tmp, rightX, rightY);
                break;

            case 1:   // Texture only
                terrainData.getCoordinateWithTexture(tmp, tex, rightX, rightY);
                p2tS = tex[0];
                p2tT = tex[1];
                break;

            case 2:     // Color only
                terrainData.getCoordinateWithColor(tmp, col, rightX, rightY);
                p2R = col[0];
                p2G = col[1];
                p2B = col[2];

                break;

            case 3:     // Both texture and color.
                terrainData.getCoordinate(tmp, tex, col, rightX, rightY);
                p2tS = tex[0];
                p2tT = tex[1];

                p2R = col[0];
                p2G = col[1];
                p2B = col[2];
                break;
        }

        p2X = tmp[0];
        p2Y = tmp[1];
        p2Z = tmp[2];


        switch(type)
        {
            case 0:   // No color or texture
                terrainData.getCoordinate(tmp, apexX, apexY);
                break;

            case 1:   // Texture only
                terrainData.getCoordinateWithTexture(tmp, tex, apexX, apexY);
                p3tS = tex[0];
                p3tT = tex[1];
                break;

            case 2:     // Color only
                terrainData.getCoordinateWithColor(tmp, col, apexX, apexY);
                p3R = col[0];
                p3G = col[1];
                p3B = col[2];

                break;

            case 3:     // Both texture and color.
                terrainData.getCoordinate(tmp, tex, col, apexX, apexY);
                p3tS = tex[0];
                p3tT = tex[1];

                p3R = col[0];
                p3G = col[1];
                p3B = col[2];
                break;
        }

        p3X = tmp[0];
        p3Y = tmp[1];
        p3Z = tmp[2];


        // Check the visibility of this triangle
        if(parentVisible == UNDEFINED ||
           parentVisible == ViewFrustum.CLIPPED)
        {
            visible = frustum.isTriangleInFrustum(p1X, p1Y, p1Z,
                                                        p2X, p2Y, p2Z,
                                                        p3X, p3Y, p3Z);
        }
        else
            visible = parentVisible;

        variance = 0;
    }

    /**
     * Compute the variance variable value.
     *
     * @param position The position for the computation
     */
    private void computeVariance(Tuple3f position)
    {
        float center_x = (p1X + p2X) * 0.5f;
        float center_z = -(p1Y + p2Y) * 0.5f;
        float pos_x = (position.x - center_x) * (position.x - center_x);
        float pos_z = (position.z - center_z) * (position.z - center_z);
        float distance = (float)Math.sqrt(pos_x + pos_z);

        float angle = varianceTree.getVariance(node) / distance;

        variance = (float)Math.abs(Math.atan(angle));
    }

    /**
     * Forceful split of this triangle and turns it into two triangles.
     */
    private void splitTriangle(Tuple3f position,
                               ViewFrustum frustum,
                               QueueManager queueManager)
    {
        int splitX = (leftX + rightX)/2;
        int splitY = (leftY + rightY)/2;

        if(parent != null)
            parent.removeDiamond(queueManager);

        leftChild = getTreeNode();
        rightChild = getTreeNode();

        leftChild.newNode(apexX, apexY,
                          leftX, leftY,
                          splitX, splitY,
                          node << 1,
                          terrainData,
                          frustum,
                          visible,
                          depth + 1,
                          varianceTree);

        rightChild.newNode(rightX, rightY,
                           apexX, apexY,
                           splitX, splitY,
                           1 + (node << 1),
                           terrainData,
                           frustum,
                           visible,
                           depth + 1,
                           varianceTree);

        leftChild.parent = this;
        rightChild.parent = this;

        if(depth + 1 < varianceTree.getMaxDepth() &&
           visible != ViewFrustum.OUT)
        {
            rightChild.computeVariance(position, queueManager);
            leftChild.computeVariance(position, queueManager);
        }
    }

    private void split2(Tuple3f position,
                        ViewFrustum frustum,
                        QueueManager queueManager)
    {
        splitTriangle(position, frustum, queueManager);

        queueManager.removeTriangle(this);

        leftChild.leftNeighbour = rightChild;
        rightChild.rightNeighbour = leftChild;
        leftChild.baseNeighbour = leftNeighbour;

        if(leftNeighbour != null)
        {
            if(leftNeighbour.baseNeighbour == this)
                leftNeighbour.baseNeighbour = leftChild;
            else
            {
                if(leftNeighbour.leftNeighbour == this)
                    leftNeighbour.leftNeighbour = leftChild;
                else
                    leftNeighbour.rightNeighbour = leftChild;
            }
        }

        rightChild.baseNeighbour = rightNeighbour;

        if(rightNeighbour != null)
        {
            if(rightNeighbour.baseNeighbour == this)
                rightNeighbour.baseNeighbour = rightChild;
            else
            {
                if(rightNeighbour.rightNeighbour == this)
                    rightNeighbour.rightNeighbour = rightChild;
                else
                    rightNeighbour.leftNeighbour = rightChild;
            }
        }
    }

    private void merge(TreeNode mergeNode, QueueManager queueManager)
    {
        if(mergeNode.leftChild == null ||
           mergeNode.rightChild == null ||
           !mergeNode.leftChild.isLeaf() ||
           !mergeNode.rightChild.isLeaf())
        {
            throw new RuntimeException("Illegal merge");
        }

        if(mergeNode.leftNeighbour != null)
        {
            if(mergeNode.leftNeighbour.baseNeighbour == mergeNode.leftChild)
               mergeNode.leftNeighbour.baseNeighbour = mergeNode;
            else
            {
                if(mergeNode.leftNeighbour.leftNeighbour == mergeNode.leftChild)
                    mergeNode.leftNeighbour.leftNeighbour = mergeNode;
                else
                    mergeNode.leftNeighbour.rightNeighbour = mergeNode;
            }
        }

        if(mergeNode.rightNeighbour != null)
        {
            if(mergeNode.rightNeighbour.baseNeighbour == mergeNode.rightChild)
                mergeNode.rightNeighbour.baseNeighbour = mergeNode;
            else
            {
                if(mergeNode.rightNeighbour.rightNeighbour == mergeNode.rightChild)
                    mergeNode.rightNeighbour.rightNeighbour = mergeNode;
                else
                    mergeNode.rightNeighbour.leftNeighbour = mergeNode;
            }
        }

        if(mergeNode.leftChild.baseNeighbour != null &&
           mergeNode.leftChild.baseNeighbour.baseNeighbour == mergeNode.leftChild)
        {
            mergeNode.leftChild.baseNeighbour.baseNeighbour = mergeNode;
        }

        if(mergeNode.rightChild.baseNeighbour != null &&
           mergeNode.rightChild.baseNeighbour.baseNeighbour == mergeNode.rightChild)
        {
           mergeNode.rightChild.baseNeighbour.baseNeighbour = mergeNode;
        }

        mergeNode.leftNeighbour = mergeNode.leftChild.baseNeighbour;
        mergeNode.rightNeighbour = mergeNode.rightChild.baseNeighbour;

        if(mergeNode.visible != ViewFrustum.OUT)
            queueManager.addTriangle(mergeNode);

        queueManager.removeTriangle(mergeNode.leftChild);
        queueManager.removeTriangle(mergeNode.rightChild);

        mergeNode.leftChild.freeNode();
        mergeNode.leftChild = null;
        mergeNode.rightChild.freeNode();
        mergeNode.rightChild = null;
    }

    /**
     * Check if the tree node forms a diamond.
     *
     * @param tn The tree node to check
     * @param queueManager The queue for nodes
     */
    private void checkForNewDiamond(TreeNode tn, QueueManager queueManager)
    {
        if(tn == null)
            return;

        if(tn.leftChild.isLeaf() && tn.rightChild.isLeaf() &&
           (tn.baseNeighbour == null ||
            tn.baseNeighbour.leftChild == null ||
            (tn.baseNeighbour.leftChild.isLeaf() &&
             tn.baseNeighbour.rightChild.isLeaf())))
        {
            tn.diamond = true;

            if(tn.visible != ViewFrustum.OUT)
            {
                if(tn.baseNeighbour != null)
                    tn.diamondVariance = Math.max(tn.variance,
                                                  tn.baseNeighbour.variance);
                else
                    tn.diamondVariance = tn.variance;
            }
            else
                tn.diamondVariance = Float.MIN_VALUE;

            queueManager.addDiamond(tn);
        }
    }

    /**
     * Add the node to the free cache.
     */
    private static void addTreeNode(TreeNode node)
    {
        nodeCache.add(node);
    }
}
