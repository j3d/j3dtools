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
import javax.media.j3d.Transform3D;
import java.util.Map;

/**
 * Particle that stores position, color and texture
 * information in shared BYREF arrays.
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public abstract class ByRefParticle extends Particle
{
    /**
     * Construct a new particle using by-reference geometry.
     *
     * @param relative true if the position is relative
     */
    public ByRefParticle(boolean relative)
    {
        super(relative);
    }

    /**
     * Implement this method to update the BYREF positions of
     * the geometry based on the change to the position field.
     *
     */
    public abstract void updateGeometry(float[] coords, int startIndex);

    /**
     * Implement this method to update the BYREF colors for
     * the geometry based on the change to the color field.
     *
     */
    public abstract void updateColors(float[] colors, int startIndex);

    /**
     * Implement this method to update the BYREF positions of
     * the geometry based on the change to the position field.
     *
     */
    public abstract void updateNormals(float[] normals, int startIndex);

    /**
     * Implement this method to update the BYREF colors for
     * the geometry based on the change to the color field.
     *
     */
    public abstract void updateTexCoords(float[] coords, int startIndex);
}
