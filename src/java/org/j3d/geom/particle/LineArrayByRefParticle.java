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
import javax.vecmath.Point3d;
import java.util.Map;

/**
 * Particle that uses a LineArray for the basic geometry.
 * <p>
 *
 * Update methods are defined for a TriangleArray
 * We draw two triangles for each particle:
 *
 * <pre>
 *  <- width*2 ->
 *              2    / \
 *             /      |
 *           /        |
 *         +        height*2
 *       /            |
 *     /              |
 * 1,4               \ /
 *
 * </pre>
 *
 * Triangle 1: 1,2,3<br>
 * Triangle 2: 4,5,6<br>
 * Point 1 == Point 4<br>
 * Point 3 == Point 5<br>
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class LineArrayByRefParticle extends ByRefParticle
{
    public LineArrayByRefParticle(boolean relative)
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
        // point 1
        coords[startIndex] = position.x;
        coords[startIndex + 1] = position.y;
        coords[startIndex + 2] = position.z;

        // point 2
        coords[startIndex + 3] = previousPosition.x;
        coords[startIndex + 4] = previousPosition.y;
        coords[startIndex + 5] = previousPosition.z;
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

        colors[startIndex + 4] = color.x;
        colors[startIndex + 5] = color.y;
        colors[startIndex + 6] = color.z;
        colors[startIndex + 7] = color.w;
    }

    /**
     * Implement this method to update the BYREF positions of
     * the geometry based on the change to the position field.
     *
     */
    public void updateNormals(float[] normals, int startIndex)
    {
        // Lines can't have normals
    }

    /**
     * Implement this method to update the BYREF colors for
     * the geometry based on the change to the color field.
     *
     */
    public void updateTexCoords(float[] coords, int startIndex)
    {
        coords[startIndex] = 0;
        coords[startIndex + 1] = 0;

        // point 2
        coords[startIndex + 2] = 1;
        coords[startIndex + 3] = 0;
    }
}