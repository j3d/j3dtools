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
 * @version $Revision: 1.2 $
 */
public class TriangleArrayByRefParticle extends ByRefParticle
{
    public static final int NUM_VERTICES_PER_PARTICLE = 6;

    protected static final int X_COORD_INDEX = 0;
    protected static final int Y_COORD_INDEX = 1;
    protected static final int Z_COORD_INDEX = 2;

    protected static final int COORD_POINT_1 = 0;
    protected static final int COORD_POINT_2 = 3;
    protected static final int COORD_POINT_3 = 6;
    protected static final int COORD_POINT_4 = 9;
    protected static final int COORD_POINT_5 = 12;
    protected static final int COORD_POINT_6 = 15;

    protected static final int TEX_POINT_1 = 0;
    protected static final int TEX_POINT_2 = 2;
    protected static final int TEX_POINT_3 = 4;
    protected static final int TEX_POINT_4 = 6;
    protected static final int TEX_POINT_5 = 8;
    protected static final int TEX_POINT_6 = 10;

    protected static final int S_COORD_INDEX = 0;
    protected static final int T_COORD_INDEX = 1;

    protected static final int RED_COLOR_INDEX = 0;
    protected static final int GREEN_COLOR_INDEX = 1;
    protected static final int BLUE_COLOR_INDEX = 2;
    protected static final int ALPHA_COLOR_INDEX = 3;

    protected int startIndex = 0;

    private Point3d topLeft = new Point3d();
    private Point3d bottomLeft = new Point3d();
    private Point3d topRight = new Point3d();
    private Point3d bottomRight = new Point3d();

    public TriangleArrayByRefParticle(
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

        startIndex = index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_COORDS;

        setTextureCoordinates();
        setNormals();
    }

    protected void updateColors()
    {
        int colorStartIndex =
                index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_COLORS;

        for ( int vertex = 0; vertex < NUM_VERTICES_PER_PARTICLE; vertex++ )
        {
            colorRefArray[colorStartIndex
                    + ( vertex * ByRefParticle.NUM_COLORS )
                    + RED_COLOR_INDEX] =
                    color.x;
            colorRefArray[colorStartIndex
                    + ( vertex * ByRefParticle.NUM_COLORS )
                    + GREEN_COLOR_INDEX] =
                    color.y;
            colorRefArray[colorStartIndex
                    + ( vertex * ByRefParticle.NUM_COLORS )
                    + BLUE_COLOR_INDEX] =
                    color.z;
            colorRefArray[colorStartIndex
                    + ( vertex * ByRefParticle.NUM_COLORS )
                    + ALPHA_COLOR_INDEX] =
                    color.w;
        }
    }

    protected void updateGeometry()
    {
        if ( renderFromPreviousPosition != false )
        {
            topLeft.set( position );
            topLeft.x -= width;
            topRight.set( position );
            topRight.x += width;

            bottomLeft.set( previousPosition );
            bottomLeft.x -= width;
            bottomRight.set( previousPosition );
            bottomRight.x += width;
        }
        else
        {
            topLeft.set( position );
            topLeft.x -= width;
            topLeft.y += height;

            topRight.set( position );
            topRight.x += width;
            topRight.y += height;

            bottomLeft.set( position );
            bottomLeft.x -= width;
            bottomLeft.y -= height;

            bottomRight.set( position );
            bottomRight.x += width;
            bottomRight.y -= height;
        }

        // point 1
        positionRefArray[startIndex + COORD_POINT_1 + X_COORD_INDEX] =
                bottomLeft.x;
        positionRefArray[startIndex + COORD_POINT_1 + Y_COORD_INDEX] =
                bottomLeft.y;
        positionRefArray[startIndex + COORD_POINT_1 + Z_COORD_INDEX] = bottomLeft.z;

        // point 2
        positionRefArray[startIndex + COORD_POINT_2 + X_COORD_INDEX] =
                topLeft.x;
        positionRefArray[startIndex + COORD_POINT_2 + Y_COORD_INDEX] =
                topLeft.y;
        positionRefArray[startIndex + COORD_POINT_2 + Z_COORD_INDEX] = topLeft.z;

        // point 3
        positionRefArray[startIndex + COORD_POINT_3 + X_COORD_INDEX] =
                topRight.x;
        positionRefArray[startIndex + COORD_POINT_3 + Y_COORD_INDEX] =
                topRight.y;
        positionRefArray[startIndex + COORD_POINT_3 + Z_COORD_INDEX] = topRight.z;

        // point 4 = point 1
        positionRefArray[startIndex + COORD_POINT_4 + X_COORD_INDEX] =
                bottomLeft.x;
        positionRefArray[startIndex + COORD_POINT_4 + Y_COORD_INDEX] =
                bottomLeft.y;
        positionRefArray[startIndex + COORD_POINT_4 + Z_COORD_INDEX] = bottomLeft.z;

        // point 5 = point 3
        positionRefArray[startIndex + COORD_POINT_5 + X_COORD_INDEX] =
                topRight.x;
        positionRefArray[startIndex + COORD_POINT_5 + Y_COORD_INDEX] =
                topRight.y;
        positionRefArray[startIndex + COORD_POINT_5 + Z_COORD_INDEX] = topRight.z;

        // point 6
        positionRefArray[startIndex + COORD_POINT_6 + X_COORD_INDEX] =
                bottomRight.x;
        positionRefArray[startIndex + COORD_POINT_6 + Y_COORD_INDEX] =
                bottomRight.y;
        positionRefArray[startIndex + COORD_POINT_6 + Z_COORD_INDEX] = bottomRight.z;
    }

    private void setTextureCoordinates()
    {
        int texStartIndex =
                index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_TEXTURE_COORDS;

        // point 1
        textureCoordRefArray[texStartIndex + TEX_POINT_1 + S_COORD_INDEX] = 0;
        textureCoordRefArray[texStartIndex + TEX_POINT_1 + T_COORD_INDEX] = 0;

        // point 2
        textureCoordRefArray[texStartIndex + TEX_POINT_2 + S_COORD_INDEX] = 0;
        textureCoordRefArray[texStartIndex + TEX_POINT_2 + T_COORD_INDEX] = 1;

        // point 3
        textureCoordRefArray[texStartIndex + TEX_POINT_3 + S_COORD_INDEX] = 1;
        textureCoordRefArray[texStartIndex + TEX_POINT_3 + T_COORD_INDEX] = 1;

        // point 4 = point 1
        textureCoordRefArray[texStartIndex + TEX_POINT_4 + S_COORD_INDEX] = 0;
        textureCoordRefArray[texStartIndex + TEX_POINT_4 + T_COORD_INDEX] = 0;

        // point 5 = point 3
        textureCoordRefArray[texStartIndex + TEX_POINT_5 + S_COORD_INDEX] = 1;
        textureCoordRefArray[texStartIndex + TEX_POINT_5 + T_COORD_INDEX] = 1;

        // point 6
        textureCoordRefArray[texStartIndex + TEX_POINT_6 + S_COORD_INDEX] = 1;
        textureCoordRefArray[texStartIndex + TEX_POINT_6 + T_COORD_INDEX] = 0;
    }

    private void setNormals()
    {
        int normalStartIndex =
                index * NUM_VERTICES_PER_PARTICLE * ByRefParticle.NUM_NORMALS;

        for ( int vertex = 0; vertex < NUM_VERTICES_PER_PARTICLE; vertex++ )
        {
            normalRefArray[normalStartIndex
                    + ( vertex * ByRefParticle.NUM_NORMALS )
                    + X_COORD_INDEX] =
                    0;
            normalRefArray[normalStartIndex
                    + ( vertex * ByRefParticle.NUM_NORMALS )
                    + Y_COORD_INDEX] =
                    0;
            normalRefArray[normalStartIndex
                    + ( vertex * ByRefParticle.NUM_NORMALS )
                    + Z_COORD_INDEX] =
                    1;
        }
    }
}