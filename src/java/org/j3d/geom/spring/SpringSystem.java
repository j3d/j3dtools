/*****************************************************************************
 *                        J3D.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spring;

// External imports
// None

// Local imports
// None

/**
 * An implementation of a spring system, particularly useful for modelling cloth
 * dynamics.
 * <p>
 *
 * The spring system assumes that it is operating over the top of some
 * underlying geometry. As such, it tries to minimise the performance impacts
 * by directly operating on the same array that is used by the geometry itself
 * during rendering. The arrays that are passed to the system are not copied,
 * just directly referenced and each node in the system acts as a .
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SpringSystem
{
    /** Constants used to set up the springs */
    private static final int[] DX = { 1, 1, 0, -1, -1, -1,  0,  1 };
    private static final int[] DY = { 0, 1, 1,  1,  0, -1, -1, -1 };

    /** 3D vector holding the current gravity direction */
    private float[] gravity;

    /** Set of all the spring nodes currently held */
    private SpringNode[] nodes;

    /** The number of nodes that are current in the array */
    private int numNodes;

    /** A constant value defining how springy the connections are */
    private float springConstant;

    /**
     * Create a new default spring system. Gravity is pointed downwards with a
     * value of -9.8 and spring constant of 200.
     */
    public SpringSystem()
    {
        gravity = new float[3];
        gravity[1] = -9.8f;

        numNodes = 0;
        springConstant = 200;
    }

    /**
     * Add a retangular field of spring node location points to this field.
     * Each node in the field is connected to the 4 surrounding nodes.
     *
     * @param width The number of points in the width of the field
     * @param height The number of points in the height of the field
     * @param pos An array of positions, one per location to work on
     * @param norm An array of normals, one per location to work on
     */
    public void addRectField(int width, int height, float[] pos, float[] norm)
    {
        float[] p = pos;
        float[] n = norm;
        int idx = 0;

        // Make sure we have enough room
        incNodeListSize(width * height);

        for(int j = 0; j < height; j++)
        {
            for(int i = 0; i < width; i++)
            {
                nodes[numNodes++] = new SpringNode(pos, norm, idx);
                idx += 3;
            }
        }

        for(int j = 0; j < height; j++)
        {
            for(int i = 0; i < width; i++)
            {
                SpringNode node = nodes[j * width + i];

                int start;
                int num;
                if (i == 0){
                    if (j == 0){
                        start = 0;
                        num = 3;
                    } else if (j == height - 1){
                        start = 6;
                        num = 3;
                    } else {
                        start = 6;
                        num = 5;
                    }
                } else if (i == width - 1){
                    if (j == 0){
                        start = 2;
                        num = 3;
                    } else if (j == height - 1){
                        start = 4;
                        num = 3;
                    } else {
                        start = 2;
                        num = 5;
                    }
                } else if (j == 0){
                    start = 0;
                    num = 5;
                } else if (j == height - 1){
                    start = 4;
                    num = 5;
                } else {
                    start = 0;
                    num = 8;
                }

                node.nNormal = num;

                num += start;
                for(int k = start; k < num; k++)
                    node.addSpring(nodes[(j + DY[k & 7]) * width + i + DX[k & 7]]);

                if(i < 2)
                {
                    if (j < 2)
                    {
                        start = 0;
                        num = 3;
                    }
                    else if (j > height - 3)
                    {
                        start = 6;
                        num = 3;
                    }
                    else
                    {
                        start = 6;
                        num = 5;
                    }
                }
                else if (i > width - 3)
                {
                    if (j < 2)
                    {
                        start = 2;
                        num = 3;
                    }
                    else if (j > height - 3)
                    {
                        start = 4;
                        num = 3;
                    }
                    else
                    {
                        start = 2;
                        num = 5;
                    }
                }
                else if (j < 2)
                {
                    start = 0;
                    num = 5;
                }
                else if (j > height - 3)
                {
                    start = 4;
                    num = 5;
                }
                else
                {
                    start = 0;
                    num = 8;
                }

                num += start;
                for(int k = start; k < num; k++)
                    node.addSpring(nodes[(j + 2 * DY[k & 7]) * width + i + 2 * DX[k & 7]]);
            }
        }
    }

    /**
     * Regenerate the natural lengths between the various nodes based on the
     * current separation between each node's current position and it's
     * connections. The direction information on each spring is reset to zero
     * too.
     */
    public void resetNaturalLengths()
    {
        for(int i = 0; i < numNodes; i++)
            nodes[i].resetNaturalLengths();
    }

    /**
     * Time to update all the interactions between the springs and nodes.
     *
     * @param dTime The delta in time between the last update and this
     */
    public void update(float dTime)
    {
        update(dTime, null, null);
    }

    /**
     * Time to update all the interactions between the springs and nodes.
     *
     * @param dTime The delta in time between the last update and this
     * @param callback A per-node extra processing if needed callback
     */
    public void update(float dTime, SpringEvaluatorCallback callback)
    {
        update(dTime, callback, null);
    }

    /**
     * Time to update all the interactions between the springs and nodes.
     *
     * @param dTime The time delta from last evaluation
     * @param callback A per-node extra processing if needed callback
     * @param attribs Any per-node attribute info that is useful for the callback
     */
    public void update(float dTime, SpringEvaluatorCallback callback, float[] attribs)
    {
        float time = (float)Math.pow(0.5f, dTime);

        for(int i = 0; i < numNodes; i++)
        {
            if(nodes[i].locked)
                continue;

            SpringNode node = nodes[i];

            node.dir[0] *= time;
            node.dir[1] *= time;
            node.dir[2] *= time;

            float p_x = node.position[node.offset];
            float p_y = node.position[node.offset + 1];
            float p_z = node.position[node.offset + 2];

            for(int j = 0; j < node.numConnections; j++)
            {
                // Calculate the distance between the two nodes right now
                int n_off = node.connections[j].offset;
                float d_x = node.connections[j].position[n_off] - p_x;
                float d_y = node.connections[j].position[n_off + 1] - p_y;
                float d_z = node.connections[j].position[n_off + 2] - p_z;

                float len = (float)Math.sqrt(d_x * d_x + d_y * d_y + d_z * d_z);
                float t = (len - node.naturalLengths[j]) / len;

                float f_x = 3 * d_x * t;
                float f_y = 3 * d_y * t;
                float f_z = 3 * d_z * t;

                node.dir[0] += springConstant * dTime * f_x;
                node.dir[1] += springConstant * dTime * f_y;
                node.dir[2] += springConstant * dTime * f_z;
            }

            node.dir[0] += dTime * gravity[0];
            node.dir[1] += dTime * gravity[1];
            node.dir[2] += dTime * gravity[2];

            if(callback != null)
                callback.processSpringNode(node, attribs);
        }

        for(int i = 0; i < numNodes; i++)
        {
            if(nodes[i].locked)
                continue;

            int n_off = nodes[i].offset;
            nodes[i].position[n_off] += dTime * nodes[i].dir[0];
            nodes[i].position[n_off + 1] += dTime * nodes[i].dir[1];
            nodes[i].position[n_off + 2] += dTime * nodes[i].dir[2];
        }
    }

    /**
     * Update the normals now following in any post-processing after the
     * update.
     */
    public void evaluateNormals()
    {
        for(int i = 0; i < numNodes; i++)
        {
            SpringNode node = nodes[i];

            float n_x = 0;
            float n_y = 0;
            float n_z = 0;

            float p_x = node.position[node.offset];
            float p_y = node.position[node.offset + 1];
            float p_z = node.position[node.offset + 2];

            int n_off = node.connections[0].offset;

            float v1_x = node.connections[0].position[n_off] - p_x;
            float v1_y = node.connections[0].position[n_off + 1] - p_y;
            float v1_z = node.connections[0].position[n_off + 2] - p_z;

            for(int j = 1; j < node.nNormal; j++)
            {
                float v0_x = v1_x;
                float v0_y = v1_y;
                float v0_z = v1_z;

                n_off = node.connections[j].offset;
                v1_x = node.connections[j].position[n_off] - p_x;
                v1_y = node.connections[j].position[n_off + 1] - p_y;
                v1_z = node.connections[j].position[n_off + 2] - p_z;

                float dot_v0 = v0_x * v0_x + v0_y * v0_y + v0_z * v0_z;
                float dot_v1 = v1_x * v1_x + v1_y * v1_y + v1_z * v1_z;

                float cross_x = v0_y * v1_z - v0_z * v1_y;
                float cross_y = v0_z * v1_x - v0_x * v1_z;
                float cross_z = v0_x * v1_y - v0_y * v1_x;

                float r_sqrt = rsqrt(dot_v0 * dot_v1);

                n_x +=  cross_x * r_sqrt;
                n_y +=  cross_y * r_sqrt;
                n_z +=  cross_z * r_sqrt;
            }

            float d = rsqrt(n_x * n_x + n_y * n_y + n_z * n_z);

            node.normal[node.offset] = n_x * d;
            node.normal[node.offset + 1] = n_y * d;
            node.normal[node.offset + 2] = n_z * d;
        }
    }

    /**
     * Get the node at the given index.
     *
     * @param index The index of the spring node to fetch
     * @return The node at that index or null if index > getNodeCount()
     */
    public SpringNode getNode(int index)
    {
        return nodes[index];
    }

    /**
     * Get the number of nodes that this system is currently maintaining.
     *
     * @return a Value >= 0
     */
    public int getNodeCount()
    {
        return numNodes;
    }

    /**
     * Set a new gravity strength and direction vector.
     *
     * @param grav The new values to use for gravity
     */
    public void setGravity(float[] grav)
    {
        gravity[0] = grav[0];
        gravity[1] = grav[1];
        gravity[2] = grav[2];
    }

    /**
     * Get the currently set gravity strength and direction vector.
     *
     * @param grav an array to copy the values into
     */
    public void getGravity(float[] grav)
    {
        grav[0] = gravity[0];
        grav[1] = gravity[1];
        grav[2] = gravity[2];
    }

    /**
     * Set a new value for how stretchy the springs are. Any value is OK but
     * negative values are likely to cause interesting visual effects. The
     * greater the number, the more still the springs are.
     *
     * @param val The new values to use for the constant
     */
    public void setSpringConstant(float val)
    {
        springConstant = val;
    }

    /**
     * Get the currently set spring constant setting.
     *
     * @return The current setting value
     */
    public float getSpringConstant()
    {
        return springConstant;
    }

    /**
     * Increment the node list size by this amount
     *
     * @param size The increment to add
     */
    private void incNodeListSize(int size)
    {
        int new_size = numNodes + size;

        SpringNode[] tmp = new SpringNode[new_size];

        if(numNodes != 0)
            System.arraycopy(nodes, 0, tmp, 0, numNodes);

        nodes = tmp;
    }

    /**
     * Fast version of reciprocal sqrt function approximation.
     *
     * @param v The number to calc
     * @return 1/sqrt(v)
     */
    private float rsqrt(float v)
    {
        float v_half = v * 0.5f;
        int i = Float.floatToRawIntBits(v);
        i = 0x5f3759df - (i >> 1);
        v = Float.intBitsToFloat(i);
        return v * (1.5f - v_half * v * v);
    }

}
