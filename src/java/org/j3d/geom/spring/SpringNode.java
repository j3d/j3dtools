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
 * Representation of a node in a collection that many springs are connected to.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SpringNode
{
    /** A reference to the global array containing position information */
    public float[] position;

    /** A reference to the global array containing normal information */
    public float[] normal;
    public float[] dir;

    /** The offset into the array to work with the normal and positions */
    public int offset;

    /** Connections to other nodes */
    public SpringNode[] connections;

    /** Natural length of each connection */
    public float[] naturalLengths;

    public int numConnections;

    int nNormal;

    /** Is this node currently locked in position? */
    public boolean locked;

    /**
     * Create a new node at the given position and normal. The array values
     * are kept as references, not copied.
     *
     * @param pos The position array that contains the location
     * @param norm The normal array that contains our normal
     * @param offset The offset into the arrays that this node is located at
     */
    SpringNode(float[] pos, float[] norm, int offset)
    {
        position = pos;
        normal = norm;

        this.offset = offset;

        dir = new float[3];
        locked = false;
        nNormal = 0;

        // Start with a basic rectangular grid assumption for size;
        connections = new SpringNode[4];
        naturalLengths = new float[4];
        numConnections = 0;
    }

    /**
     * Add a spring between the given node and this node.
     *
     * @param node The new node instance to connect to this one
     */
    public void addSpring(SpringNode node)
    {
        if(numConnections == connections.length)
        {
            int new_size = numConnections + 4;

            SpringNode[] tmp1 = new SpringNode[new_size];
            float[] tmp2 = new float[new_size];

            System.arraycopy(connections, 0, tmp1, 0, numConnections);
            System.arraycopy(naturalLengths, 0, tmp2, 0, numConnections);

            connections = tmp1;
            naturalLengths = tmp2;
        }

        float x = position[offset] - node.position[node.offset];
        float y = position[offset + 1] - node.position[node.offset + 1];
        float z = position[offset + 2] - node.position[node.offset + 2];
        float d = (float)Math.sqrt(x * x + y * y + z * z);

        connections[numConnections] = node;
        naturalLengths[numConnections] = d;
//        nNormal++;
        numConnections++;
    }

    /**
     * Regenerate the natural lengths based on the current separation between
     * this node and it's connections. This will also reset the current
     * direction vector back to zero again.
     */
    public void resetNaturalLengths()
    {
        for(int i = 0; i < numConnections; i++)
        {
            SpringNode conn = connections[i];

            float x = position[offset] - conn.position[conn.offset];
            float y = position[offset + 1] - conn.position[conn.offset + 1];
            float z = position[offset + 2] - conn.position[conn.offset + 2];
            float d = (float)Math.sqrt(x * x + y * y + z * z);

            naturalLengths[i] = d;
        }

        dir[0] = 0;
        dir[1] = 0;
        dir[2] = 0;
    }
};
