/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.loaders.ac3d;


/**
 * Representation of the attributes for a polygon surface in the AC3D file
 * format definition.
 * <p>
 * Primitives are used wherever possible to reduce memory usage and object
 * clutter.</p>
 *
 * <p><strong>TODO:</strong><ul>
 * <li> Perform bounds checking
 * <li> Cleanup, commentary, and optimization.
 * </ul></p>
 *
 * @author  Ryan Wilhm (ryan@entrophica.com)
 * @version $Revision: 1.3 $
 */
public class Ac3dSurface
{

    /** Number of vertex references. */
    private int numrefs;

    /** Specifies type and flags for surface. */
    private int flags;

    /** Specifies the material for the surface by index. */
    private int material;

    /** The vertices index. */
    private int[] vertices;

    /** The texture coordinates. */
    private float[] textureCoordinates;

    /** Surface flag for a polygon */
    public static final int FLAG_POLYGON = 0;

    /** Surface flag for a closed line loop */
    public static final int FLAG_CLOSEDLINE = 1;

    /** Surface flag for an open line */
    public static final int FLAG_LINE = 2;

    /** Surface flag for a shaded polygon */
    public static final int FLAG_SHADED = 16;

    /** Surface flag for a double sided polygon */
    public static final int FLAG_TWOSIDED = 32;

    /**
     * Default constructor, which sets up the initial state.
     */
    public Ac3dSurface()
    {
        numrefs = 0;
        flags = 0;
        material = -1;
    }

    /**
     * <p>Mutator to set the number of vertex references for this surface.
     * This also allocates space for all of the associated data.</p>
     *
     * @param numrefs The number of references this surface is to have.
     */
    public void setNumrefs(int numrefs)
    {
        this.numrefs = numrefs;
        vertices = new int[numrefs];
        textureCoordinates = new float[numrefs * 2];
    }

    /**
     * Retrieve the number of vertex references for this surface.
     *
     * @return The number of surface references.
     */
    public int getNumrefs()
    {
        return numrefs;
    }

    /**
     * <p>Mutator to set the current flag state.</p>
     *
     * @param flags The value ot set the current flag state to.
     */
    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    /**
     * <p>Accessor to get the current flag state.</p>
     *
     * @return The current flag state.
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     * Set the material index reference.
     *
     * @param mat The material index to associate this surface to.
     */
    public void setMaterial(int mat)
    {
        material = mat;
    }

    /**
     * Get the material index reference for this surface.
     *
     * @return The material index associated with this surface.
     */
    public int getMaterial()
    {
        return material;
    }

    /**
     * Append an additional reference to the surface. These
     * identify the coordinates of the vertex, as well as texture
     * coordinates.
     *
     * @param index Indicates the index number of the surface vertes.
     * @param vertex The index of the vertex in the object the surface is a
     *               part of.
     * @param texCoord The texture coordinates for the surface vertex relating
     *                 to the texture map.
     */
    public void addRef(int index, int vertex, float[] texCoord)
    {
        vertices[index] = vertex;
        textureCoordinates[2 * index] = texCoord[0];
        textureCoordinates[2 * index + 1] = texCoord[1];
    }


    /**
     * Get the array of vertices.
     *
     * @return The array of vertices.
     */
    public int[] getVerticesIndex()
    {
        return vertices;
    }


    /**
     * <p>Accessor for the <code>textureCoordinates</code> property.</p>
     *
     * @return The array of texture coordinates.
     */
    public float[] getTextureCoordinates()
    {
        return textureCoordinates;
    }

    /**
     * Helper function that tests to see if the requested flag is set in
     * the local instance state.
     *
     * @param flag The flag to check for.
     * @return Whether or not the flag is set in the local state.
     */
    public boolean checkFlag(int flag)
    {
        return checkFlag(flag, flags);
    }

    /**
     * <p>Creates and returns the stringified version of the internal
     * state.</p>
     *
     * @return The stringified value for the state.
     */
    public String toString()
    {
        StringBuffer ret_val = new StringBuffer();

        ret_val.append("[ flags=");
        ret_val.append(flags);
        ret_val.append(" { ");
        stringifyFlags(flags, ret_val);
        ret_val.append(" }, mat=");
        ret_val.append(material);
        ret_val.append(", numrefs=");
        ret_val.append(numrefs);
        ret_val.append(", refs= { ");
        for (int i = 0; i < vertices.length; i++)
        {
            ret_val.append(vertices[i]);
            ret_val.append("@(");
            ret_val.append(textureCoordinates[2*i]);
            ret_val.append(", ");
            ret_val.append(textureCoordinates[2*i+1]);
            ret_val.append(") ");
        }
        ret_val.append(" } ]");

        return ret_val.toString();
    }


    /**
     * Helper funciton that returns a stringified representation of the
     * flag state.
     *
     * @param flags The flags to stringify.
     * @param buffer The buffer to copy the flags in to
     */
    private void stringifyFlags(int flags, StringBuffer buffer)
    {
        // Deal with type... Should only be one!

        if (checkFlag(FLAG_POLYGON, flags))
            buffer.append("FLAG_POLYGON");

        if (checkFlag(FLAG_CLOSEDLINE, flags))
            buffer.append("FLAG_CLOSEDLINE");

        if (checkFlag(FLAG_LINE, flags))
            buffer.append("FLAG_LINE");

        // Deal with attributes...

        if (checkFlag(FLAG_SHADED, flags))
            buffer.append(" | FLAG_SHADED");

        if (checkFlag(FLAG_TWOSIDED, flags))
            buffer.append(" | FLAG_TWOSIDED");
    }


    /**
     * Returns whether or not the a flag is set. This is defined as
     * <code>private static final</code> to make the function a candidate
     * for inlining by the compiler.
     *
     * @param flag The flag being checked for.
     * @param flags The state flags that are being checked.
     * @return Whether or not <code>flag</code> was set in <code>flags</code>.
     */
    private boolean checkFlag(int flag, int flags)
    {
        return ((flags & flag) == flag);
    }
}
