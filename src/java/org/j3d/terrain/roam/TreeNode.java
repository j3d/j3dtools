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
import javax.vecmath.Point3f;
import javax.vecmath.Color3f;

// Application specific imports
import org.j3d.terrain.ViewFrustum;
import org.j3d.terrain.TerrainData;

/**
 *
 * @author  Paul Byrne, Justin Couch
 * @version
 */
class TreeNode
{
    TreeNode leftChild;
    TreeNode rightChild;

    TreeNode baseNeighbour;
    TreeNode leftNeighbour;
    TreeNode rightNeighbour;

    TreeNode parent;

    int leftX, leftY;       // Pointers into terrainData
    int rightX, rightY;
    int apexX, apexY;

    int node;

    int depth;      // For debugging

    public static final int UNDEFINED = -1;

    int visible = UNDEFINED;

    private float p1X, p1Y, p1Z;    // The three corners of the triangle
    private float p2X, p2Y, p2Z;
    private float p3X, p3Y, p3Z;

    private float p1tS, p1tT;       // Texture coordinates
    private float p2tS, p2tT;       // Texture coordinates
    private float p3tS, p3tT;       // Texture coordinates

    private TerrainData terrainData;
    private VarianceTree varianceTree;

    float variance = 0f;
    float diamondVariance = 0f;

    private boolean error = false;

    boolean diamond = false;

    /**
     * A cache of instances of ourselves to help avoid too much object
     * creation and deletion.
     */
    private static LinkedList nodeCache = new LinkedList();


    //boolean splitThisFrame = false;
    //boolean mergedThisFrame = false;

    /**
     * Default constructor for use by TreeNodeCache.
     */
    TreeNode()
    {
    }

    /**
     * Creates new TreeNode customised with all the data set.
     */
    public TreeNode(int leftX,
                    int leftY,
                    int rightX,
                    int rightY,
                    int apexX,
                    int apexY,
                    int node,
                    TerrainData terrainData,
                    ViewFrustum landscapeView,
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

        init(landscapeView, parentVisible);
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
                 ViewFrustum landscapeView,
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

        init(landscapeView, parentVisible);
    }

    private void init(ViewFrustum landscapeView, int parentVisible)
    {
        float tmp[] = new float[3];
        float texTmp[] = new float[2];

        terrainData.getCoordinateFromGrid(tmp, texTmp, leftX, leftY);
        p1X = tmp[0];
        p1Y = tmp[1];
        p1Z = tmp[2];

        p1tS = texTmp[0];
        p1tT = texTmp[1];

        terrainData.getCoordinateFromGrid(tmp, texTmp, rightX, rightY);
        p2X = tmp[0];
        p2Y = tmp[1];
        p2Z = tmp[2];

        p2tS = texTmp[0];
        p2tT = texTmp[1];

        terrainData.getCoordinateFromGrid(tmp, texTmp, apexX, apexY);
        p3X = tmp[0];
        p3Y = tmp[1];
        p3Z = tmp[2];

        p3tS = texTmp[0];
        p3tT = texTmp[1];

        /*
        p1X = (float)leftX*scale;
        p1Y = heightMap[leftX][leftY];
        p1Z = (float)-leftY*scale;
        p2X = (float)rightX*scale;
        p2Y = heightMap[rightX][rightY];
        p2Z = (float)-rightY*scale;
        p3X = (float)apexX*scale;
        p3Y = heightMap[apexX][apexY];
        p3Z = (float)-apexY*scale;
         */

        // Check the visibility of this triangle
        if(parentVisible == UNDEFINED ||
           parentVisible == ViewFrustum.CLIPPED)
        {
            visible = landscapeView.isTriangleInFrustum(p1X, p1Y, p1Z,
                                                        p2X, p2Y, p2Z,
                                                        p3X, p3Y, p3Z);
        }
        else
            visible = parentVisible;

        variance = 0;
    }

    /**
     * Reset this node by
     *     removing all it's children,
     *     set visible depending on visibiling in view
     */
    void reset(ViewFrustum landscapeView)
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

        baseNeighbour =null;
        leftNeighbour =null;
        rightNeighbour = null;

        visible = landscapeView.isTriangleInFrustum(p1X, p1Y, p1Z,
                                                        p2X, p2Y, p2Z,
                                                        p3X, p3Y, p3Z);
    }

    public boolean isLeaf()
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

        addTreeNode(this);
    }

    void computeVariance(Point3f position, QueueManager queueManager)
    {
        float centerX = (p1X + p2X) * 0.5f;
        float centerZ = -(p1Y + p2Y) * 0.5f;

        float distance = (float)Math.sqrt(Math.pow(position.x - centerX, 2) +
                                          Math.pow(position.z - centerZ, 2));

        double angle = Math.atan(varianceTree.getVariance(node) / distance);
        variance = (float)Math.abs(angle);

        queueManager.addTriangle(this);
    }

    /**
     * If this triangle was half of a diamond then remove the
     * diamond from the diamondQueue
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

    int split(Point3f position,
              ViewFrustum landscapeView,
              QueueManager queueManager)
    {
        int triCount = 0;

        //System.out.println("-----------> Splitting "+node);

        //if(mergedThisFrame)
        //    System.out.println("SPLITTING Tri that has been merged");
        //splitThisFrame = true;

        if(leftChild != null || rightChild != null)
        {
            System.out.println("ERROR tri is already split");
            printNode(this);
            throw new RuntimeException(" Triangle is already split "+node);
            //error = true;
            //queueManager.removeTriangle(this);
            //return 0;
        }

        if(baseNeighbour != null)
        {
            if(baseNeighbour.baseNeighbour != this)
                triCount += baseNeighbour.split(position,
                                                landscapeView,
                                                queueManager);

            split2(position, landscapeView, queueManager);
            triCount++;
            baseNeighbour.split2(position, landscapeView, queueManager);
            //if(baseNeighbour.visible!=ViewFrustum.OUT)
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
            split2(position, landscapeView, queueManager);
            triCount++;

            diamondVariance = variance;
            diamond = true;
            queueManager.addDiamond(this);
        }

        return triCount;
    }

    int merge(QueueManager queueManager)
    {
        int trisRemoved = 0;

        //System.out.print("Merging ");
        //printNode(this);

        //if(splitThisFrame)
        //    System.out.println("Merging Tri that was split this frame");
        //mergedThisFrame = true;

        if(baseNeighbour != null && baseNeighbour.baseNeighbour != this)
        {
            System.out.println("++++++++++++ Illegal merge *********************************");
            printNode(this);
            error = true;
            queueManager.removeDiamond(this);
            diamond = false;
            diamondVariance = 0f;
            return 0;
            //throw new RuntimeException("Illegal merge");
        }

        merge(this, queueManager);
        trisRemoved++;
        checkForNewDiamond(this.parent, queueManager);
        if(baseNeighbour!=null)
        {
            merge(baseNeighbour, queueManager);
            trisRemoved++;
            checkForNewDiamond(baseNeighbour.parent, queueManager);
        }

        queueManager.removeDiamond(this);
        diamond = false;
        diamondVariance = 0f;

        return trisRemoved;
    }

    /**
     * Add the coordinates for this triangle to the list
     */
    void getTriangles(VertexData vertexData)
    {
        // Not sure this needs to be here
        if(error)
        {
            byte clrR;
            byte clrG;
            byte clrB;
            clrR = clrG = 0;
            clrB = (byte)255;
            vertexData.addVertex(p1X, p1Y+2f, p1Z,
                                  clrR, clrG, clrB);
            vertexData.addVertex(p2X, p2Y+2f, p2Z,
                                  clrR, clrG, clrB);
            vertexData.addVertex(p3X, p3Y+2f, p3Z,
                                  clrR, clrG, clrB);
        }

        if(leftChild == null)
        {
            byte clrR;
            byte clrG;
            byte clrB;

            if(visible == ViewFrustum.OUT)
            {
                clrR = clrG = clrB = (byte)(255*0.3f);
            }
            else
            {
                clrR = clrB = 0;
                clrG = (byte)255;
            }

            /*
            if(error || parent!=null && (parent.diamond || (parent.baseNeighbour!=null && parent.baseNeighbour.diamond))) {
                clrR = (byte)255;
                clrG = clrB = (byte)0;
                if(error) {
                    clrR = clrG = 0;
                    clrB = (byte)255;
                }
                vertexData.addVertex(p1X, p1Y+2f, p1Z,
                                      clrR, clrG, clrB);
                vertexData.addVertex(p2X, p2Y+2f, p2Z,
                                      clrR, clrG, clrB);
                vertexData.addVertex(p3X, p3Y+2f, p3Z,
                                      clrR, clrG, clrB);
            }
             */

            if((visible == ViewFrustum.OUT) || (visible == UNDEFINED))
            {
                /*
                vertexData.addVertex(p1X, p1Y-20f, p1Z,
                                      clrR, clrG, clrB);
                vertexData.addVertex(p2X, p2Y-20f, p2Z,
                                      clrR, clrG, clrB);
                vertexData.addVertex(p3X, p3Y-20f, p3Z,
                                      clrR, clrG, clrB);
                 */
            }
            else
            {
// how is this set?
                boolean textured = false;

                if(textured)
                {
                    vertexData.addVertex(p1X, p1Y, p1Z,
                                          p1tS, p1tT);
                    vertexData.addVertex(p2X, p2Y, p2Z,
                                          p2tS, p2tT);
                    vertexData.addVertex(p3X, p3Y, p3Z,
                                          p3tS, p3tT);
                }
                else
                {
                    vertexData.addVertex(p1X, p1Y, p1Z,
                                          clrR, clrG, clrB);
                    vertexData.addVertex(p2X, p2Y, p2Z,
                                          clrR, clrG, clrB);
                    vertexData.addVertex(p3X, p3Y, p3Z,
                                          clrR, clrG, clrB);
                }
            }


            //System.out.println(leftX+"  "+leftY);
            //System.out.println(rightX+"  "+rightY);
            //System.out.println(apexX+"  "+apexY);
        }
        else
        {
            leftChild.getTriangles(vertexData);
            rightChild.getTriangles(vertexData);
        }
    }

    /**
     * Update the tree depending on the view position and variance
     */
    void updateTree(Point3f position,
                    ViewFrustum landscapeView,
                    VarianceTree varianceTree,
                    int parentVisible,
                    QueueManager queueManager)
    {

        //splitThisFrame = false;
        //mergedThisFrame = false;

        if(parentVisible == UNDEFINED ||
           parentVisible == ViewFrustum.CLIPPED)
        {
            visible = landscapeView.isTriangleInFrustum(p1X, p1Y, p1Z,
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
            float centerX = (p1X + p2X) * 0.5f;
            float centerZ = -(p1Y + p2Y) * 0.5f;

            float pos_x = (position.x - centerX) * (position.x - centerX);
            float pos_z = (position.z - centerZ) * (position.z - centerZ);
            float distance = (float)Math.sqrt(pos_x + pos_z);

            float angle = varianceTree.getVariance(node) / distance;
            variance = (float)Math.abs(Math.atan(angle));

            queueManager.addTriangle(this);
        }
        else
        {
            if(leftChild != null)
                leftChild.updateTree(position, landscapeView, varianceTree, visible, queueManager);

            if(rightChild != null)
                rightChild.updateTree(position, landscapeView, varianceTree, visible, queueManager);

            //System.out.println(diamond+"  "+diamondVariance);
            if(diamond)
            {
// BUG Here, baseNeighbour may not have had it's variance updated
// for the new position
                if(visible != ViewFrustum.OUT)
                {
                    float centerX = (p1X + p2X) * 0.5f;
                    float centerZ = -(p1Y + p2Y) * 0.5f;
                    float pos_x = (position.x - centerX) * (position.x - centerX);
                    float pos_z = (position.z - centerZ) * (position.z - centerZ);
                    float distance = (float)Math.sqrt(pos_x + pos_z);

                    float angle = varianceTree.getVariance(node) / distance;

                    variance = (float)Math.abs(Math.atan(angle));

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

    public String toString()
    {
        //return Integer.toString(hashCode());
        return Integer.toString(node);
    }

    public static void printNode(TreeNode tn) {
        System.out.println("Node "+tn+"  "+tn.hashCode());
        System.out.print("  Children "+tn.leftChild+" "+ tn.rightChild);
        System.out.print("  Nhbrs "+tn.baseNeighbour+" "+tn.leftNeighbour+" "+tn.rightNeighbour);

        System.out.print("  Parent "+ tn.parent);
        System.out.print("  PBase ");
        if(tn.parent!=null) {
            System.out.print(tn.parent.baseNeighbour);
            System.out.print("  PChldrn "+tn.parent.leftChild+" "+tn.parent.rightChild);
        }

        System.out.println();
    }

    //----------------------------------------------------------
    // local convenience methods
    //----------------------------------------------------------

    /**
     * Forceful split of this triangle and turns it into two triangles.
     */
    private void splitTriangle(Point3f position,
                               ViewFrustum landscapeView,
                               QueueManager queueManager)
    {
        int splitX = (leftX+rightX)/2;
        int splitY = (leftY+rightY)/2;

        if(parent != null)
            parent.removeDiamond(queueManager);

        leftChild = getTreeNode();
        rightChild = getTreeNode();

        leftChild.newNode(apexX, apexY,
                                  leftX, leftY,
                                  splitX, splitY,
                                  node << 1,
                                  terrainData,
                                  landscapeView,
                                  visible,
                                  depth + 1,
                                  varianceTree);

        rightChild.newNode(rightX, rightY,
                                   apexX, apexY,
                                   splitX, splitY,
                                   1 + (node << 1),
                                   terrainData,
                                   landscapeView,
                                   visible,
                                   depth + 1,
                                   varianceTree);

        leftChild.parent = this;
        rightChild.parent = this;

        if(depth+1 < varianceTree.getMaxDepth() && visible!=ViewFrustum.OUT)
        {
            rightChild.computeVariance(position, queueManager);
            leftChild.computeVariance(position, queueManager);
        }
    }

    private void split2(Point3f position,
                        ViewFrustum landscapeView,
                        QueueManager queueManager)
    {
        splitTriangle(position, landscapeView, queueManager);

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
            System.out.print("ILLEGAL merge of ");
            printNode(mergeNode);
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
     * Check if tn forms a diamond
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
     * Either return a node from the cache or if the cache is empty, return
     * a new tree node.
     */
    private static TreeNode getTreeNode()
    {
        TreeNode ret_val;

        if(nodeCache.size() > 0)
            ret_val = (TreeNode)nodeCache.removeFirst();
        else
            ret_val = new TreeNode();

        return ret_val;
    }

    /**
     * Add the node to the free cache.
     */
    private static void addTreeNode(TreeNode node)
    {
        nodeCache.add(node);
    }
}