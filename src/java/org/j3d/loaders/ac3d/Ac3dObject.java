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

// External imports
import java.util.ArrayList;

/**
 * <p><code>AC3DObject</code> provides a modeling of the properties that
 * constitute an object in the AC3D file format specification.</p>
 *
 * <p><strong>TODO:</strong><ul>
 * <li> Cleanup, commentary, and optimization.
 * </ul></p>
 *
 * @author  Ryan Wilhm (ryan@entrophica.com)
 * @version $Revision: 1.5 $
 */
public class Ac3dObject
{
    /** Identity matrix. */
    private static float[] IDENTITY_MATRIX_ARRAY =
    {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    };

    /** The humantext name for the object. */
    private String name;

    /** The type of object represented. */
    private String type;

    /** The displacement vector for this object. */
    private float[] location;

    /** The rotational matrix for this object. */
    private float[] rotation;

    /** The verticies for the object. */
    private float[] vertices;

    /** The texture for the object. */
    private String texture;

    /** The two texture repeat values */
    private float[] textureRepeat;

    /** The object data identified for the object. */
    private String data;

    /** An optional URL for the document */
    private String url;

    /** The crease angle of the object. In degrees. */
    private float creaseAngle;

    /** References to all of the children for this object. */
    private ArrayList<Ac3dObject> kids;

    /** The surfaces for the object. */
    private ArrayList<Ac3dSurface> surfaces;

    /**
     * Default constructor.
     */
    public Ac3dObject()
    {
        creaseAngle = 45.0f;

        kids = new ArrayList<Ac3dObject>();
        surfaces = new ArrayList<Ac3dSurface>();

        location = new float[3];
        textureRepeat = new float[2];
        textureRepeat[0] = 1.0f;
        textureRepeat[1] = 1.0f;

        rotation = new float[9];

        System.arraycopy(IDENTITY_MATRIX_ARRAY, 0, rotation, 0, 9);
    }


    /**
     * Set the name property. A null value will clear the current name.
     *
     * @param name The value to set the internal name to.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the name associated with this object. If no name is set, it will
     * return null.
     *
     * @return The value of the internal name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the type of object that this instance represents.
     *
     * @param type The type to set this object to.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Get the type of object that this instance represents. Type is
     * determined by the AC3D file format.
     *
     * @return The type of object that this instance represents.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Set the crease angle that the object should use when shading. Crease
     * angle is represented in degrees.
     *
     * @param angle The crease angle to use
     */
    public void setCreaseAngle(float angle)
    {
        creaseAngle = angle;
    }

    /**
     * Get the crease angle that the object uses for rendering surfaces and
     * calculating normals. Angle is in degrees.
     *
     * @return The type of object that this instance represents.
     */
    public float getCreaseAngle()
    {
        return creaseAngle;
    }

    /**
     * Get the number of children objects that this object aggregates.
     *
     * @return The number of children that this object has.
     */
    public int getNumKids()
    {
        return kids.size();
    }

    /**
     * Add a child to this object.
     *
     * @param child The new child object instance to add
     */
    public void addChild(Ac3dObject child)
    {
        kids.add(child);
    }

    /**
     * Get the child at the given index. If the index is out of bounds, null
     * will be returned.
     *
     * @param index The index of the child to get
     */
    public Ac3dObject getChild(int index)
    {
        if(index < 0 || index >= kids.size())
            return null;

        return kids.get(index);
    }

    /**
     * Set the number of verticies for the object.
     *
     * @param The number of verticies to set for the object.
     */
    public void setNumvert(int num)
    {
        if(vertices == null)
            vertices = new float[num * 3];
        else if(vertices.length < num * 3)
        {
            float[] tmp = new float[num * 3];
            System.arraycopy(vertices, 0, tmp, 0, vertices.length);
            vertices = tmp;
        }
    }

    /**
     * Get the number of verticies for the object.
     *
     * @return The number of verticies for the object.
     */
    public int getNumvert()
    {
        return vertices.length / 3;
    }

    /**
     * Change the location displacement vector.
     *
     * @param loc The location displacement vector to set to.
     */
    public void setLocation(float[] loc)
    {
        location[0] = loc[0];
        location[1] = loc[1];
        location[2] = loc[2];
    }

    /**
     * Get the current location displacement vector.
     *
     * @return The location displacement vector
     */
    public float[] getLocation()
    {
        return location;
    }


    /**
     * Mutator to set the rotation matrix for the object.
     *
     * @param rot The rotation matrix to set our internal state to.
     */
    public void setRotation(float[] rot)
    {
        rotation[0] = rot[0];
        rotation[1] = rot[1];
        rotation[2] = rot[2];
        rotation[3] = rot[3];
        rotation[4] = rot[4];
        rotation[5] = rot[5];
        rotation[6] = rot[6];
        rotation[7] = rot[7];
        rotation[8] = rot[8];
    }


    /**
     * Get the rotation matrix for the object.
     *
     * @return The rotationation matrix.
     */
    public float[] getRotation()
    {
        return rotation;
    }

    /**
     * Query for the number of surfaces that this object contains.
     *
     * @return A non-negative value for the number of surfaces
     */
    public int getNumSurfaces()
    {
        return surfaces.size();
    }

    /**
     * Adds a Ac3dSurface at the given index.
     *
     * @param index The locationation at which to append the surface.
     * @param surface The surface to add.
     */
    public void addSurface(Ac3dSurface surface)
    {
        surfaces.add(surface);
    }

    /**
     * Get the Ac3dSurface> at the given index.
     *
     * @return The surface at the requested index.
     */
    public Ac3dSurface getSurface(int index)
    {
        return surfaces.get(index);
    }

    /**
     * Mutator to add one vertex at the specified index.</p>
     *
     * @param index The index at which to add the vertex.
     * @param vertex Tuple of floats specifying the coordinate.
     */
    public void addVertex(int index, float[] vertex)
    {
        vertices[3 * index] = vertex[0];
        vertices[3 * index + 1] = vertex[1];
        vertices[3 * index + 2] = vertex[2];
    }

    /**
     * Get the entire array of vertices.
     *
     * @return All of the verticies for the object.
     */
    public float[] getVertices()
    {
        return vertices;
    }

    /**
     * Get an individual vertex at a given index.
     *
     * @return The vertex requested.
     * @param vtx An array of length 3 to copy the vertex value to
     */
    public void getVertex(int index, float[] vtx)
    {
        vtx[0] = vertices[3 * index];
        vtx[1] = vertices[3 * index + 1];
        vtx[2] = vertices[3 * index + 2];
    }

    /**
     * Set the name of the texture file to use
     *
     * @param texture The value to set the internal texture name to.
     */
    public void setTexture(String texture)
    {
        this.texture = texture;
    }

    /**
     * Get the texture file name associated with this material. If none is set
     * it will return null.
     *
     * @return The value of the internal texture name.
     */
    public String getTexture()
    {
        return texture;
    }

    /**
     * Change the texture repeat values. The array needs to be at least length
     * two, with two values for the S and T axes. Default values are 1,1.
     *
     * @param rep The repeat factors
     */
    public void setTextureRepeat(float[] rep)
    {
        textureRepeat[0] = rep[0];
        textureRepeat[1] = rep[1];
        textureRepeat[2] = rep[2];
    }

    /**
     * Get the current texture repeat values.
     *
     * @return A length 2 array for S and T repeat amounts
     */
    public float[] getTextureRepeat()
    {
        return textureRepeat;
    }

    /**
     * Set the random character data that was associated with this object.
     *
     * @param str The string to use
     */
    public void setData(String str)
    {
        data = str;
    }

    /**
     * Get the character data associated with this object. If no data is given
     * this will return null.
     *
     * @return The data string or null if none
     */
    public String getData()
    {
        return data;
    }

    /**
     * Set the informational URL that could be associated with this object. If
     * there is none, it will be null. No checking of the sanity is done.
     *
     * @param str The string to use for the URL
     */
    public void setURL(String str)
    {
        url = str;
    }

    /**
     * Get the URL associated with this object. If no URL is given this will
     * return null.
     *
     * @return The URL string or null if none
     */
    public String getURL()
    {
        return url;
    }

    /**
     * Generate a string representation of the internal state.
     *
     * @return A stringified version of the internal state.
     */
    public String toString()
    {
        StringBuffer ret_val = new StringBuffer();

        ret_val.append("[ name=\"");
        if(name == null)
            ret_val.append("(not set)");
        else
            ret_val.append(name);
        ret_val.append("\", type=\"");

        if(type == null)
            ret_val.append("(not set)");
        else
            ret_val.append(type);

        ret_val.append("\"");

        if(texture != null)
        {
            ret_val.append(", texture=\"");
            ret_val.append(texture);
            ret_val.append("\"");
        }

        if(url != null)
        {
            ret_val.append(", url=\"");
            ret_val.append(url);
            ret_val.append("\"");
        }

        if(data != null)
        {
            ret_val.append(", data=\"");
            ret_val.append(data);
            ret_val.append("\"");
        }

        ret_val.append(", numKids=");
        ret_val.append(kids.size());
        ret_val.append(", numvert=");
        if(vertices == null)
            ret_val.append("0");
        else
            ret_val.append(vertices.length / 3);
        ret_val.append(", location={");
        stringifyXf(location, ret_val);
        ret_val.append("}, rotation={");
        stringifyXf(rotation, ret_val);
        ret_val.append("}, numsurf=");
        ret_val.append(surfaces.size());

        ret_val.append("}, creaseAngle=");
        ret_val.append(creaseAngle);

        ret_val.append(" ]");

        return ret_val.toString();
    }


    /**
     * <p>Helper that returns a stringified version of float array values.</p>
     *
     * @param vals Values to stringify.
     * @param buffer The string buffer to append it all to
     */
    private void stringifyXf(float[] vals, StringBuffer buffer)
    {
        if (vals.length>0)
        {
            buffer.append(vals[0]);
            for (int i=1; i<vals.length; i++)
            {
                buffer.append(",");
                buffer.append(vals[i]);
            }
        }
    }
}
