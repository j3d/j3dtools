/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.vecmath.Point3f;

/**
 * Particle that uses a TriangleArray for the basic geometry.
 * <p>
 *
 * Update methods are defined for a TriangleArray
 * We draw two triangles for each particle:
 *
 * <pre>
 *  <- width*2 ->
 *  3 --------- 2,6  / \
 *   |         /|     |
 *   |       /  |     |
 *   |     +    |   height*2
 *   |   /      |     |
 *   | /        |     |
 * 1,4 -------- 5    \ /
 *
 * </pre>
 *
 * Triangle 1: 1,2,3<br>
 * Triangle 2: 4,5,6<br>
 * Point 1 == Point 4<br>
 * Point 3 == Point 5<br>
 *
 * @author Daniel Selman
 * @version $Revision: 1.4 $
 */
public class TriangleArrayByRefParticle extends ByRefParticle
{
    private Point3f topLeft = new Point3f();
    private Point3f bottomLeft = new Point3f();
    private Point3f topRight = new Point3f();
    private Point3f bottomRight = new Point3f();

    public TriangleArrayByRefParticle(boolean relative)
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
        if(renderFromPreviousPosition)
        {
            topLeft.set(position);
            topLeft.x -= width;
            topRight.set(position);
            topRight.x += width;

            bottomLeft.set(previousPosition);
            bottomLeft.x -= width;
            bottomRight.set(previousPosition);
            bottomRight.x += width;
        }
        else
        {
            topLeft.set(position);
            topLeft.x -= width;
            topLeft.y += height;

            topRight.set(position);
            topRight.x += width;
            topRight.y += height;

            bottomLeft.set(position);
            bottomLeft.x -= width;
            bottomLeft.y -= height;

            bottomRight.set(position);
            bottomRight.x += width;
            bottomRight.y -= height;
        }

        // point 1
        coords[startIndex] = bottomLeft.x;
        coords[startIndex + 1] = bottomLeft.y;
        coords[startIndex + 2] = bottomLeft.z;

        // point 2
        coords[startIndex + 3] = topRight.x;
        coords[startIndex + 4] = topRight.y;
        coords[startIndex + 5] = topRight.z;

        // point 3
        coords[startIndex + 6] = topLeft.x;
        coords[startIndex + 7] = topLeft.y;
        coords[startIndex + 8] = topLeft.z;

        // point 4 = point 1
        coords[startIndex + 9] = bottomLeft.x;
        coords[startIndex + 10] = bottomLeft.y;
        coords[startIndex + 11] = bottomLeft.z;

        // point 5 = point 3
        coords[startIndex + 12] = bottomRight.x;
        coords[startIndex + 13] = bottomRight.y;
        coords[startIndex + 14] = bottomRight.z;

        // point 6
        coords[startIndex + 15] = topRight.x;
        coords[startIndex + 16] = topRight.y;
        coords[startIndex + 17] = topRight.z;
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

        colors[startIndex + 16] = color.x;
        colors[startIndex + 17] = color.y;
        colors[startIndex + 18] = color.z;
        colors[startIndex + 19] = color.w;

        colors[startIndex + 20] = color.x;
        colors[startIndex + 21] = color.y;
        colors[startIndex + 22] = color.z;
        colors[startIndex + 23] = color.w;
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

        // point 5 = point 3
        normals[startIndex + 12] = 0;
        normals[startIndex + 13] = 0;
        normals[startIndex + 14] = 1;

        // point 6
        normals[startIndex + 15] = 0;
        normals[startIndex + 16] = 0;
        normals[startIndex + 17] = 1;
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
        coords[startIndex + 3] = 1;

        // point 3
        coords[startIndex + 4] = 0;
        coords[startIndex + 5] = 1;

        // point 4 = point 1
        coords[startIndex + 6] = 0;
        coords[startIndex + 7] = 0;

        // point 5 = point 3
        coords[startIndex + 8] = 1;
        coords[startIndex + 9] = 0;

        // point 6
        coords[startIndex + 10] = 1;
        coords[startIndex + 11] = 1;
    }
}