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
 * @version $Revision: 1.4 $
 */
public class Ac3dMaterial
{
    /** Default name string to use for each material */
    private static final String DEFAULT_NAME = "";

    /** Name associated with material. */
    private String name;

    /** The base colour of the object */
    private float[] rgb;

    /** The ambientient colour to apply to the object */
    private float[] ambient;

    /** The emissive colour to apply to the object */
    private float[] emissive;

    /** The specularular colour to apply to the object */
    private float[] specular;

    /** The shininess factor to apply to the object */
    private float shininess;

    /** Transparency of the material. 0 is completely opaque. */
    private float transparency;

    /**
     * Identifies the index of the material within the AC3D file that this
     * object represents.
     */
    private int index;

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
        shininess = 0.0f;
        transparency = 0.0f;
        index = -1;
    }

    /**
     * Set the name of this material.
     *
     * @param name The new name to associate with this material
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the currently set name. If the name is not set, will return
     * an empty string.
     *
     * @return The current name of this material
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the base colour of this material. Colour is a 3-component value.
     *
     * @param rgb The components of the colour to set in R, G, B order
     */
    public void setRGBColor(float[] rgb)
    {
        this.rgb[0] = rgb[0];
        this.rgb[1] = rgb[1];
        this.rgb[2] = rgb[2];
    }

    /**
     * Get the current base colour set.
     *
     * @param rgb An array of length 3 to cop the values in to.
     */
    public void getRGBColor(float[] rgb)
    {
        rgb[0] = this.rgb[0];
        rgb[1] = this.rgb[1];
        rgb[2] = this.rgb[2];
    }

    /**
     * Set the ambient colour of this material. Colour is a 3-component value.
     *
     * @param rgb The components of the colour to set in R, G, B order
     */
    public void setAmbientColor(float[] rgb)
    {
        ambient[0] = rgb[0];
        ambient[1] = rgb[1];
        ambient[2] = rgb[2];
    }

    /**
     * Get the current ambient colour set.
     *
     * @param rgb An array of length 3 to cop the values in to.
     */
    public void getAmbientColor(float[] rgb)
    {
        rgb[0] = ambient[0];
        rgb[1] = ambient[1];
        rgb[2] = ambient[2];
    }

    /**
     * Set the emissive colour of this material. Colour is a 3-component value.
     *
     * @param rgb The components of the colour to set in R, G, B order
     */
    public void setEmissiveColor(float[] rgb)
    {
        emissive[0] = rgb[0];
        emissive[1] = rgb[1];
        emissive[2] = rgb[2];
    }

    /**
     * Get the current emissive colour set.
     *
     * @param rgb An array of length 3 to cop the values in to.
     */
    public void getEmissiveColor(float[] rgb)
    {
        rgb[0] = emissive[0];
        rgb[1] = emissive[1];
        rgb[2] = emissive[2];
    }

    /**
     * Set the specular colour of this material. Colour is a 3-component value.
     *
     * @param rgb The components of the colour to set in R, G, B order
     */
    public void setSpecularColor(float[] rgb)
    {
        specular[0] = rgb[0];
        specular[1] = rgb[1];
        specular[2] = rgb[2];
    }

    /**
     * Get the current specular colour set.
     *
     * @param rgb An array of length 3 to cop the values in to.
     */
    public void getSpecularColor(float[] rgb)
    {
        rgb[0] = specular[0];
        rgb[1] = specular[1];
        rgb[2] = specular[2];
    }

    /**
     * Set the shininess factor of the object. This should be a value that
     * is greater than zero. The max value is unbounded.
     *
     * @param factor The shininess factor value
     */
    public void setShininess(float factor)
    {
        shininess = factor;
    }

    /**
	 * Get the shininess factor of the object.
	 *
	 * @return The shininess factor value
     */
    public float getShininess()
    {
		return shininess;
	}

    /**
     * Set the transparency amount. 0 is completely opaque, 1 is completely
     * see through.
     *
     * @param trans The value of transparency between 0 and 1
     */
    public void setTransparency(float trans)
    {
        transparency = trans;
    }

    /**
	 * Get the transparency amount. 0 is completely opaque, 1 is completely
	 * see through.
	 *
	 * @return The value of transparency between 0 and 1
     */
    public float getTransparency()
    {
		return transparency;
	}

    /**
     * Set the index of this material in the file.
     *
     * @param index A zero or positive number
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
	 * Get the index of this material in the file.
	 *
	 * @return The index of this material in the file
     */
    public int getIndex()
    {
		return index;
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
