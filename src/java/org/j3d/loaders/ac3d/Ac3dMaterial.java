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
 * Defines a model to represent the properties of a surface material in the
 * AC3D file format.</p>
 *
 * @author  Ryan Wilhm (ryan@entrophica.com)
 * @version $Revision: 1.1 $
 */
public class Ac3dMaterial
{
    /** Default name string to use for each material */
    private static final String DEFAULT_NAME = "";

    /** Name associated with material. */
    public String name;

    /** The base colour of the object */
    public float[] rgb;

    /** The ambientient colour to apply to the object */
    public float[] ambient;

    /** The emissive colour to apply to the object */
    public float[] emissive;

    /** The specularular colour to apply to the object */
    public float[] specular;

    /** The shininess factor to apply to the object */
    public int shininess;

    /** Transparency of the material. 0 is completely opaque. */
    public float transparency;

    /**
     * Identifies the index of the material within the AC3D file that this
     * object represents.
     */
    public int index;


    /**
     * Default constructor. Sets the internal state to initial values.
     */
    public Ac3dMaterial()
    {
        name = DEFAULT_NAME;
        rgb = new float[3];
        ambient = new float[3];
        emissive = new float[3];
        specular = new float[3];
        shininess = 0;
        transparency = 0.0f;
        index = -1;
    }

    /**
     * Provides a humantext string version of the current object state.
     *
     * @return The humantext stringification of the current object state.
     */
    public String toString()
    {
        StringBuffer ret_val=new StringBuffer();

        ret_val.append("[ name=\"");
        ret_val.append(name);
        ret_val.append("\", index=");
        ret_val.append(index);
        ret_val.append(", rgb={");
        stringifyXf(rgb, ret_val);
        ret_val.append("}, ambient={");
        stringifyXf(ambient, ret_val);
        ret_val.append("}, emissive={");
        stringifyXf(emissive, ret_val);
        ret_val.append("}, specular={");
        stringifyXf(specular, ret_val);
        ret_val.append("}, shininess=");
        ret_val.append(shininess);
        ret_val.append(", trans=");
        ret_val.append(transparency);
        ret_val.append(" ]");

        return ret_val.toString();
    }


    /**
     * <p>Simple utility method to generate a stringified representation of
     * multiple <code>float</code> values.</p>
     *
     * @param vals The array of <code>float</code> values to convert into
     *             humantext.
     * @return The humantext string representing the values.
     */

    private void stringifyXf(float[] vals, StringBuffer buffer)
    {
        if(vals.length>0)
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
