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
import java.util.LinkedList;

import javax.vecmath.Tuple3f;

// Application specific imports
import org.j3d.terrain.ViewFrustum;
import org.j3d.terrain.TerrainData;

/**
 * Represents a single node of the triangle mesh of the patch.
 * <p>
 *
 * A triangle is represented by the three sides described as left, right and
 * base. This triangle is connected to neighbour triangles through these
 * references and form part of the ROAM code. A triangle is defined in terms
 * of an apex coordinate and left and right then based on the coordinates
 * supplied. left and right do not necessarily correspond to real world
 * left/right as you look down on the terrain.
 *
 * @author  Paul Byrne, Justin Couch
 * @version
 */
class TreeNode extends QueueItem
{
    /** The visibility status of this node in the tree is not known. */
    public static final int UNDEFINED = -1;

    /** Base to base orientation of the edge split routine */
    public static final int BASE_TO_BASE = 1;

    /** Left to right orientation of the edge split routine */
    public static final int LEFT_TO_RIGHT = 2;

    /** right to left orientation of the edge split routine */
    public static final int RIGHT_TO_LEFT = 3;

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
     *
     * @param leftX X grid coordinate of the left side vertex
     * @param leftY Y grid coordinate of the left side vertex
     * @param rightX X grid coordinate of the right side vertex
     * @param rightY Y grid coordinate of the right side vertex
     * @param apexX X grid coordinate of the apex vertex
     * @param apexY Y grid coordinate of the apex vertex
     * @param node How far down the split heirarchy from the patch
     * @param terrainData The source place for data
     * @param frustum The view frustum to use
     * @param parentVisible Flag to describe the current visibilty state
     *   of the parent triangle
     * @param depth How far down the split heirarchy from the patch
     * @param varianceTree Variance information to feed from
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
     * Reset the tree node so that it is like the first time it has been used.
     *
     * @param frustum The view information
     */
    void reset(ViewFrustum frustum)
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

        visible = checkVisibility(frustum);
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

        nodeCache.add(this);
    }

    /**
     * Request the recomputation of the variance of this node.
     *
     * @param position The position for the computation
     */
    void computeVariance(Tuple3f position)
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
     * Split the edge of this triangle to match the neighbour. This
     * is used for the edges when blending a new tile with a pre-existing
     * tile.
     *
     * @param newNeighbour The neighbour node to match up with
     * @param orientation One of the directions to look at the split
     * @param position The current view location
     * @param queue The queue to place newly generated items on
     * @return The number of triangles generated as a result
     */
    int edgeSplit(TreeNode newNeighbour,
                  int orientation,
                  Tuple3f position,
                  ViewFrustum frustum,
                  QueueManager queue)
    {
        // If the new neighbour triangle is not split, don't go any further
        if(newNeighbour.leftChild == null)
            return 0;

        int tri_count = 0;

        // If we are not split, force split ourselves and then work out which
        // way to start splitting the new set of code.
        if(leftChild == null)
        {
            forceSplit(position, frustum, queue);
            tri_count = 2;
        }

        switch(orientation)
        {
            case LEFT_TO_RIGHT:
                tri_count += rightChild.edgeSplit(newNeighbour.leftChild,
                                                  BASE_TO_BASE,
                                                  position,
                                                  frustum,
                                                  queue);
                break;

            case RIGHT_TO_LEFT:
                tri_count += leftChild.edgeSplit(newNeighbour.rightChild,
                                                 BASE_TO_BASE,
                                                 position,
                                                 frustum,
                                                 queue);
                break;

            case BASE_TO_BASE:
                tri_count += rightChild.edgeSplit(newNeighbour.leftChild,
                                                  LEFT_TO_RIGHT,
                                                  position,
                                                  frustum,
                                                  queue);
                tri_count += leftChild.edgeSplit(newNeighbour.rightChild,
                                                 RIGHT_TO_LEFT,
                                                 position,
                                                 frustum,
                                                 queue);
        }

        return tri_count;
    }

    /**
     * Force split this tree node into two smaller triangle tree nodes.
     *
     * @param position The current view location
     * @param frustum The view information
     * @param queue The queue to place newly generated items on
     * @return The number of triangles generated as a result
     */
    void forceSplit(Tuple3f position, ViewFrustum frustum, QueueManager queue)
    {
        if(baseNeighbour != null)
        {
            // If the base neighbour is not us then it is not at the same
            // level as this node so split it before this gets split otherwise
            // the level constraint is broken
            if(baseNeighbour.baseNeighbour != this)
            {
                baseNeighbour.forceSplit(position, frustum, queue);
            }

            queue.removeTriangle(this);
            queue.removeTriangle(baseNeighbour);

            if(parent != null && isDiamond(parent))
                queue.removeDiamond(parent);

            if(baseNeighbour.parent != null && isDiamond(baseNeighbour))
                queue.removeDiamond(baseNeighbour.parent);

            split(position, frustum, queue);
            baseNeighbour.split(position, frustum, queue);

            leftChild.rightNeighbour = baseNeighbour.rightChild;
            rightChild.leftNeighbour = baseNeighbour.leftChild;
            baseNeighbour.leftChild.rightNeighbour = rightChild;
            baseNeighbour.rightChild.leftNeighbour = leftChild;

            queue.addTriangle(leftChild);
            queue.addTriangle(rightChild);
            queue.addTriangle(baseNeighbour.leftChild);
            queue.addTriangle(baseNeighbour.rightChild);

            queue.addDiamond(this);
        }
        else
        {
            if(parent != null && isDiamond(parent))
                queue.removeDiamond(parent);

            queue.removeTriangle(this);

            split(position, frustum, queue);

            leftChild.rightNeighbour = null;
            rightChild.leftNeighbour = null;

            queue.addTriangle(leftChild);
            queue.addTriangle(rightChild);
        }
    }

    /**
     * Do a normal split of this triangle and turns it into two triangles.
     *
     * @param position The current view location
     * @param frustum The view information
     * @param queue The queue manager to place newly generated items on
     */
    void split(Tuple3f position,
               ViewFrustum frustum,
               QueueManager queue)
    {
        initChildren(frustum);

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

        if(depth + 1 < varianceTree.getMaxDepth() &&
           visible != ViewFrustum.OUT)
        {
            rightChild.computeVariance(position);
            leftChild.computeVariance(position);
        }
    }

    /**
     * Perform a merge operation on this tree node.
     */
    int merge(QueueManager queue)
    {
        int num_tris;

        queue.removeDiamond(this);

        num_tris = internalMerge(queue);
        num_tris += baseNeighbour.internalMerge(queue);

        if(parent != null && isDiamond(parent))
            queue.addDiamond(parent);

        if(baseNeighbour.parent != null &&
           isDiamond(baseNeighbour.parent))
            queue.addDiamond(baseNeighbour.parent);

        queue.addTriangle(this);
        queue.addTriangle(this.baseNeighbour);

        return num_tris;
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
     * @param queue The queue to put the merged node on
     */
    void updateTree(Tuple3f position,
                    ViewFrustum frustum,
                    VarianceTree varianceTree,
                    int parentVisible,
                    QueueManager queue)
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

            queue.addTriangle(this);
        }
        else if(leftChild != null)
        {
            // If we have children, continue to refine
            leftChild.updateTree(position,
                                 frustum,
                                 varianceTree,
                                 visible,
                                 queue);

            rightChild.updateTree(position,
                                  frustum,
                                  varianceTree,
                                  visible,
                                  queue);
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
     * Merge the children nodes of this node into a single triangle.
     *
     * @param queue The queue to put the merged node on
     * @return The number of triangles that were reduced as a result
     */
    private int internalMerge(QueueManager queue)
    {
        TreeNode new_left = leftChild.baseNeighbour;
        TreeNode new_right = rightChild.baseNeighbour;

        leftNeighbour = new_left;
        rightNeighbour = new_right;

        if(new_left != null)
        {
            if(new_left.baseNeighbour == leftChild)
               new_left.baseNeighbour = this;
            else
            {
                if(new_left.leftNeighbour == leftChild)
                    new_left.leftNeighbour = this;
                else
                    new_left.rightNeighbour = this;
            }
        }

        if(new_right != null)
        {
            if(new_right.baseNeighbour == rightChild)
                new_right.baseNeighbour = this;
            else
            {
                if(new_right.rightNeighbour == rightChild)
                    new_right.rightNeighbour = this;
                else
                    new_right.leftNeighbour = this;
            }
        }

        queue.removeTriangle(leftChild);
        queue.removeTriangle(rightChild);

        leftChild.freeNode();
        rightChild.freeNode();

        leftChild = null;
        rightChild = null;
        diamond = true;

        return 2;
    }

    /**
     * Convenience method to see if the given triangle forms one half of a
     * diamond.
     */
    private boolean isDiamond(TreeNode node)
    {
        return node.isDiamond() && node.baseNeighbour.isDiamond();
    }

    /**
     * Check if the tree node forms a diamond.
     *
     * @return true if this is a part of a diamond
     */
    private boolean isDiamond()
    {
        if(baseNeighbour == null)
            diamond = false;
        else
            diamond = baseNeighbour.baseNeighbour == this &&
                      leftChild != null &&
                      baseNeighbour.leftChild != null &&
                      leftChild.leftChild == null &&
                      rightChild.leftChild == null &&
                      baseNeighbour.leftChild.leftChild == null &&
                      baseNeighbour.rightChild.leftChild == null;

        return diamond;
    }

    /**
     * Convenience method to create and initialise a new pair of tree node
     * children for this tree node.
     */
    private void initChildren(ViewFrustum frustum)
    {
        // Calc split coordinate - half way between the two.
        int splitX = (leftX + rightX) >> 1;
        int splitY = (leftY + rightY) >> 1;

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
    }
}
