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
 * Particle that uses a TriangleArray for the basic geometry.
 * <p>
 *
 * Update methods are defined for a TriangleArray
 * We draw two triangles for each particle:
 *
 * <pre>
 *  <- width*2 ->
 *  2 --------- 3,5  / \
 *   |         /|     |
 *   |       /  |     |
 *   |     +    |   height*2
 *   |   /      |     |
 *   | /        |     |
 * 1,4 -------- 6    \ /
 *
 * </pre>
 *
 * Triangle 1: 1,2,3<br>
 * Triangle 2: 4,5,6<br>
 * Point 1 == Point 4<br>
 * Point 3 == Point 5<br>
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class LineArrayByRefParticle extends ByRefParticle
{
    public static final int NUM_VERTICES_PER_PARTICLE = 2;

    protected int startIndex = 0;

    public LineArrayByRefParticle(
            Map env,
            Shape3D shape,
            int index,
            double[] positionRefArray,
            float[] colorRefArray,
            float[] textureCoordRefArray,
            float[] normalRefArray )
    {
        super(
                env,
                shape,
                index,
                positionRefArray,
                colorRefArray,
                textureCoordRefArray,
                normalRefArray );

        setTextureCoordinates();
        setNormals();

        startIndex = index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_COORDS;
    }

    protected void updateColors()
    {
        int colorStartIndex =
                index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_COLORS;

        colorRefArray[colorStartIndex] = color.x;
        colorRefArray[colorStartIndex+1] = color.y;
        colorRefArray[colorStartIndex+2] = color.z;
        colorRefArray[colorStartIndex+3] = 1.0f;
        colorRefArray[colorStartIndex+4] = color.x;
        colorRefArray[colorStartIndex+5] = color.y;
        colorRefArray[colorStartIndex+6] = color.z;
        colorRefArray[colorStartIndex+7] = 0.0f;
    }

    protected void updateGeometry()
    {
        // point 1
        positionRefArray[startIndex] = position.x;
        positionRefArray[startIndex+1] = position.y;
        positionRefArray[startIndex+2] = position.z;

        // point 2
        positionRefArray[startIndex+3] = previousPosition.x;
        positionRefArray[startIndex+4] = previousPosition.y;
        positionRefArray[startIndex+5] = previousPosition.z;
    }

    private void setTextureCoordinates()
    {
        int texStartIndex =
                index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_TEXTURE_COORDS;

        // point 1
        textureCoordRefArray[texStartIndex] = 0;
        textureCoordRefArray[texStartIndex + 1] = 0;

        // point 2
        textureCoordRefArray[texStartIndex + 2] = 1;
        textureCoordRefArray[texStartIndex + 3] = 0;
    }

    private void setNormals()
    {
    }
}