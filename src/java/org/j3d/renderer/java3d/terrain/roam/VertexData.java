/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

/**
 * Collection of vertex information for a patch of terrain.
 * <p>
 *
 * The data held is coordinate, texture coordinate and vertex colours. Setting
 * up the class with the right flags helps to reduce the amount of data that is
 * kept around.
 *
 * @author  Justin Couch, based on original ideas by Paul Byrne
 * @version $Revision: 1.1 $
 */
public class VertexData
{
    /** This data contains coordinate information only */
    static final byte COORD_ONLY = 1;

    /** the data contains coordinate and color information only */
    static final byte COLOR_ONLY = 2;

    /** the data contains coordinate and texture information only */
    static final byte TEXTURE_ONLY = 3;

    /** the data contains coordinates, color and texture information. */
    static final byte TEXTURE_AND_COLOR = 4;

    /** This is the type of data held by this instance */
    final byte dataType;

    private float[] coords;
    private byte[] colors;
    private float[] textureCoords;
    private int index;
    private int texIndex;

    /**
     * Creates new VertexData that represents a fixed number of vertices.
     * The patchsize is the number of points along one edge and represents
     * a square piece of landscape.
     *
     * @param patchSize The number of points on a side
     * @param hasTexture true if we have to represent texture coordinates
     * @param hasColor true if we have to represent color values
     */
    public VertexData(int patchSize, boolean hasTexture, boolean hasColor)
    {
        coords = new float[patchSize * patchSize * 2 * 3 * 3];

        if(hasTexture)
            textureCoords = new float[patchSize * patchSize*2 * 3 * 2];

        if(hasColor)
            colors = new byte[coords.length];

        byte type = COORD_ONLY;

        type += hasColor ? 1 : 0;
        type += hasTexture ? 2 : 0;

        dataType = type;
    }

    /**
     * Return the complete set of coordinates held by this node.
     *
     * @return The flat array of coordinates
     */
    public float[] getCoords()
    {
        return coords;
    }

    /**
     * Return the complete set of color values held by this node.
     *
     * @return The flat array of color components
     */
    public byte[] getColors()
    {
        return colors;
    }

    /**
     * Return the complete set of texture coordinates of this node
     *
     * @return The flat array of texture coordinates
     */
    public float[] getTextureCoords()
    {
        return textureCoords;
    }

    /**
     * Add a vertext, but don't include any color or texture coordinate
     * information.
     *
     * @param x The x component of the vertex
     * @param y The y component of the vertex
     * @param z The z component of the vertex
     */
    void addVertex(float x, float y, float z)
    {
        coords[index] = x;
        coords[index + 1] = y;
        coords[index + 2] = z;

        index += 3;
    }

    /**
     * Add a vertex with color information as bytes.
     *
     * @param x The x component of the vertex
     * @param y The y component of the vertex
     * @param z The z component of the vertex
     * @param clrR The red component of the color
     * @param clrG The green component of the color
     * @param clrB The blue component of the color
     */
    void addVertex(float x, float y, float z,
                   byte clrR, byte clrG, byte clrB)
    {
        coords[index] = x;
        coords[index + 1] = y;
        coords[index + 2] = z;

        if(dataType != COLOR_ONLY && dataType != TEXTURE_AND_COLOR)
            System.out.println("Setting color on a uncolored object");
        else
        {
            colors[index] = clrR;
            colors[index + 1] = clrG;
            colors[index + 2] = clrB;
        }

        index += 3;
    }

    /**
     * Add a vertex with color information as floats.
     *
     * @param x The x component of the vertex
     * @param y The y component of the vertex
     * @param z The z component of the vertex
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     */
    void addVertex(float x, float y, float z,
                   float r, float g, float b)
    {
        byte r_tmp = (byte)(r * 255);
        byte g_tmp = (byte)(g * 255);
        byte b_tmp = (byte)(b * 255);

        addVertex(x, y, z, r_tmp, g_tmp, b_tmp);
    }

    /**
     * Add a vertex with texture coordinate information only.
     *
     * @param x The x component of the vertex
     * @param y The y component of the vertex
     * @param z The z component of the vertex
     * @param texS The S component of the texture coordinate
     * @param texT The T component of the texture coordinate
     */
    void addVertex(float x, float y, float z,
                   float texS, float texT)
    {
        coords[index] = x;
        coords[index + 1] = y;
        coords[index + 2] = z;

        if(dataType != TEXTURE_ONLY && dataType != TEXTURE_AND_COLOR)
            System.out.println("Setting texture coords in untextured object");
        else
        {
            textureCoords[texIndex++] = texS;
            textureCoords[texIndex++] = texT;
        }

        index += 3;
    }

    /**
     * Add a vertex with both color (as bytes) and texture information.
     *
     * @param x The x component of the vertex
     * @param y The y component of the vertex
     * @param z The z component of the vertex
     * @param texS The S component of the texture coordinate
     * @param texT The T component of the texture coordinate
     * @param clrR The red component of the color
     * @param clrG The green component of the color
     * @param clrB The blue component of the color
     */
    void addVertex(float x, float y, float z,
                   byte clrR, byte clrG, byte clrB,
                   float texS, float texT)
    {
        coords[index] = x;
        coords[index + 1] = y;
        coords[index + 2] = z;

        if(dataType != COLOR_ONLY && dataType != TEXTURE_AND_COLOR)
            System.out.println("Setting color on a uncolored object");
        else
        {
            colors[index] = clrR;
            colors[index + 1] = clrG;
            colors[index + 2] = clrB;
        }

        index += 3;

        if(dataType != TEXTURE_ONLY && dataType != TEXTURE_AND_COLOR)
            System.out.println("Setting texture coords in untextured object");
        else
        {
            textureCoords[texIndex++] = texS;
            textureCoords[texIndex++] = texT;
        }

    }


    /**
     * Add a vertex with both color and texture information.
     *
     * @param x The x component of the vertex
     * @param y The y component of the vertex
     * @param z The z component of the vertex
     * @param texS The S component of the texture coordinate
     * @param texT The T component of the texture coordinate
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     */
    void addVertex(float x, float y, float z,
                   float r, float g, float b,
                   float texS, float texT)
    {
        byte r_tmp = (byte)(r * 255);
        byte g_tmp = (byte)(g * 255);
        byte b_tmp = (byte)(b * 255);

        addVertex(x, y, z, r_tmp, g_tmp, b_tmp, texS, texT);
    }

    /**
     * Get the number of vertices registered here.
     *
     * @return The total number of registered vertices
     */
    public int getVertexCount()
    {
        return index / 3;
    }

    /**
     * Clear the current arrays of values.
     */
    void reset()
    {
        index = 0;
        texIndex = 0;
    }
}
