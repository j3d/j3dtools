/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.Shape3D;
import java.util.Map;

/**
 * Particle that uses PointArrays as the basic geometry type.
 * <p>
 *
 * Update methods are defined for a PointArray
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public class PointArrayByRefParticle extends ByRefParticle
{
    public PointArrayByRefParticle(boolean relative)
    {
        super(relative);
    }

    /**
     * Implement this method to update the BYREF positions of
     * the geometry based on the change to the position field.
     *
     */
    public void updateGeometry(float[] coords, int startIndex)
    {
        coords[startIndex] = position.x;
        coords[startIndex + 1] = position.y;
        coords[startIndex + 2] = position.z;
    }

    /**
     * Implement this method to update the BYREF colors for
     * the geometry based on the change to the color field.
     *
     */
    public void updateColors(float[] colors, int startIndex)
    {
        colors[startIndex] = color.x;
        colors[startIndex + 1] = color.y;
        colors[startIndex + 2] = color.z;
        colors[startIndex + 3] = color.w;
    }

    /**
     * Implement this method to update the BYREF positions of
     * the geometry based on the change to the position field.
     *
     */
    public void updateNormals(float[] normals, int startIndex)
    {
        normals[startIndex] = 0;
        normals[startIndex + 1] = 0;
        normals[startIndex + 2] = 1;
    }

    /**
     * Implement this method to update the BYREF colors for
     * the geometry based on the change to the color field.
     *
     */
    public void updateTexCoords(float[] coords, int startIndex)
    {
        // Seems a bit dumb to do this on a line.
        coords[startIndex] = 0;
        coords[startIndex + 1] = 0;
    }
}
