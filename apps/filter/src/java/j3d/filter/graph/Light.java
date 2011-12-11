/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter.graph;

// External imports
// None

// Local Imports
// None

/**
 * Represents any form of light in the scene graph. 
 * <p/>
 * 
 * Derived types from this will indicate the type of light that this is
 * 
 * @author Justin
 * @version $Revision$
 */
public interface Light
    extends Leaf
{

    public enum Type
    {
        POINT, SPOT, DIRECTIONAL, LINE
    }
    
    /**
     * Get the location of this light. This represents the centre point of the light,
     * regardless of which type of light it is. 
     * 
     * @returns an array of coordinate values in the order x, y, z
     */
    public double[] getPosition();
    
    /**
     * Update the position of the light.
     */
    public void setPosition(double x, double y, double z);
    
    /**
     * Get the primary colour of the light. The values are RGB in the range [0,1]
     * 
     * @return A array of 3 component colours - r,g,b
     */
    public float[] getColor();
    
    /**
     * Set the colour of the light. The values are RGB in the range [0,1]
     * 
     * @param r The red component of the light colour
     * @param g The green component of the light colour
     * @param b The blue component of the light colour
     */
    public void setColor(float r, float g, float b);
}
