/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// Standard imports
// None

// Application specific imports
// None

/**
 * Particle that uses TriangleFanArrays as the basic geometry.
 * <p>
 *
 * Update methods are defined for a TriangleFanArray:
 *
 * <pre>
 *  <- width*2 ->
 *  4 --------- 3    / \
 *   |        / |     |
 *   |      /   |     |
 *   |    /     |   height*2
 *   |  /       |     |
 *   |/         |     |
 *  1 --------- 2    \ /
 *
 * </pre>
 * Fan : 1,2,3   4
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TriangleFanByRefParticle extends ByRefParticle
{
    public TriangleFanByRefParticle(boolean relative)
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
        // Fan centre
        coords[startIndex] = position.x - width;
        coords[startIndex + 1] = position.y - height;
        coords[startIndex + 2] = position.z;

        // point 2
        coords[startIndex + 3] = position.x + width;
        coords[startIndex + 4] = position.y - height;
        coords[startIndex + 5] = position.z;

        // point 3
        coords[startIndex + 6] = position.x + width;
        coords[startIndex + 7] = position.y + height;
        coords[startIndex + 8] = position.z;

        // point 4
        coords[startIndex + 9] = position.x - width;
        coords[startIndex + 10] = position.y + height;
        coords[startIndex + 11] = position.z;
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

        colors[startIndex + 8] = color.x;
        colors[startIndex + 9] = color.y;
        colors[startIndex + 10] = color.z;
        colors[startIndex + 11] = color.w;

        colors[startIndex + 12] = color.x;
        colors[startIndex + 13] = color.y;
        colors[startIndex + 14] = color.z;
        colors[startIndex + 15] = color.w;
    }

    /**
     * Implement this method to update the BYREF positions of
     * the geometry based on the change to the position field.
     *
     */
    public void updateNormals(float[] normals, int startIndex)
    {
        // point 1
        normals[startIndex] = 0;
        normals[startIndex + 1] = 0;
        normals[startIndex + 2] = 1;

        // point 2
        normals[startIndex + 3] = 0;
        normals[startIndex + 4] = 0;
        normals[startIndex + 5] = 1;

        // point 3
        normals[startIndex + 6] = 0;
        normals[startIndex + 7] = 0;
        normals[startIndex + 8] = 1;

        // point 4 = point 1
        normals[startIndex + 9] = 0;
        normals[startIndex + 10] = 0;
        normals[startIndex + 11] = 1;

    }

    /**
     * Implement this method to update the BYREF colors for
     * the geometry based on the change to the color field.
     *
     */
    public void updateTexCoords(float[] coords, int startIndex)
    {
        // point 1
        coords[startIndex] = 0;
        coords[startIndex + 1] = 0;

        // point 2
        coords[startIndex + 2] = 1;
        coords[startIndex + 3] = 0;

        // point 3
        coords[startIndex + 4] = 1;
        coords[startIndex + 5] = 1;

        // point 4 = point 1
        coords[startIndex + 6] = 0;
        coords[startIndex + 7] = 1;
    }
}
