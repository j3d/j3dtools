/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                              Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
// none

// Application specific imports
// none

/**
 * Data representation of geometry information that is created through the
 * various generator classes in this package.
 * <p>
 *
 * This data representation is used to hold information needed to generate
 * geometry from one of the generator classes in this package. In general,
 * data does not get filled in for items that are not requested.
 * <p>
 *
 * The type of data to be produced can be changed with each call. While it is
 * possible to ask for both 2D and 3D texture coordinates, the code will only
 * generate 2D values if asked.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class GeometryData implements Cloneable
{
    /** Generate the geometry as individual unindexed triangles */
    public static final int TRIANGLES = 1;

    /** Generate the geometry as individual unindexed quads */
    public static final int QUADS = 2;

    /** Generate the geometry as a triangle strip array(s) */
    public static final int TRIANGLE_STRIPS = 3;

    /** Generate the geometry as a triangle fan array(s) */
    public static final int TRIANGLE_FANS = 4;

    /** Generate the geometry as indexed quads */
    public static final int INDEXED_QUADS = 5;

    /** Generate the geometry as an indexed triangle array */
    public static final int INDEXED_TRIANGLES = 6;

    /** Generate the geometry as an indexed triangle strip array */
    public static final int INDEXED_TRIANGLE_STRIPS = 7;

    /** Generate the geometry as an indexed triangle fan array */
    public static final int INDEXED_TRIANGLE_FANS = 8;

    /** Generate the geometry as a line array */
    public static final int LINES = 9;

    /** Generate the geometry as an line strip array */
    public static final int LINE_STRIPS = 10;

    /** Generate the geometry as an indexed line array */
    public static final int INDEXED_LINES = 11;

    /** Generate the geometry as an indexed line strip array */
    public static final int INDEXED_LINE_STRIPS = 12;


    /** Request for lighting normal data to be produced */
    public static final int NORMAL_DATA = 0x02;

    /** Request for 2D Texture coordinate data to be produced */
    public static final int TEXTURE_2D_DATA = 0x04;

    /** Request for 3D Texture coordinate data to be produced */
    public static final int TEXTURE_3D_DATA = 0x08;

    /** This is the type of geometry that you want to have made */
    public int geometryType = 0;

    /**
     * A generator specific field that describes the type of output
     * algorithm you would like to use for the geometry. May be ignored.
     */
    public int geometrySubType;

    /**
     * The attributes of the geometry you want created. This is an OR'd
     * list of the above variables. It is not possible to generate anything
     * without the raw geometry being computed.
     */
    public int geometryComponents;

    /** The number of vertices stored in the coordinates array */
    public int vertexCount;

    /**
     * Storage for coordinate information. These are stored in flat
     * [x1, y1, z1, x2, y2, z2, ...] configuration
     */
    public float[] coordinates;

    /**
     * Storage for lighting normal information. This should be at least the
     * length of the coordinates array. Data is stored in the same fashion.
     * If normals are requested, the count is the same as vertexCount.
     */
    public float[] normals;

    /** The number of items stored in the indexes array */
    public int indexesCount;

    /** Storage for coordinate index information if the shape type requires it. */
    public int[] indexes;

    /** The number of items stored in the strip counts */
    public int numStrips;

    /** Storage for strip counts if the shape type uses it */
    public int[] stripCounts;

    /**
     * Texture coordinate information if requested. May be 2D or 3D depending
     * on the requested type. If 2D the values are stored [s1, t1, s2, t2...]
     * For 3D coordinates it is stores as [r1, s1, t1, r2, s2, t2,...]
     */
    public float[] textureCoordinates;

    /**
     * Colour values if using per-vertex coloring. This array will be identical
     * in length to the coordinate array and index values match etc.
     */
    public float[] colors;

    /**
     * Storage for normal index information if the shape type requires it. Not
     * used by the geometry generation classes, but may be by 3rd party software.
     */
    public int[] normalIndexes;

    /**
     * Storage for texture coordinate index information if the shape type
     * requires it. Not used by the geometry generation classes, but may be by
     * 3rd party software.
     */
    public int[] texCoordIndexes;

    /**
     * Storage for color index information if the shape type requires it. Not
     * used by the geometry generation classes, but may be by 3rd party software.
     */
    public int[] colorIndexes;

    public void prettyPrint() {
        if (coordinates != null) {
            System.out.print(" Coords: " );
            for(int i=0; i < coordinates.length; i++) {
                System.out.print(coordinates[i] + " ");
                if ((i+1) % 3 == 0)
                   System.out.print(", ");
            }
            System.out.println();
        }

        if (textureCoordinates != null) {
            System.out.print("TCoords: " );
            for(int i=0; i < textureCoordinates.length; i++) {
                System.out.print(textureCoordinates[i] + " ");
                if ((i+1) % 2 == 0)
                   System.out.print(", ");

            }
            System.out.println();
        }

        if (normals != null) {
            System.out.print("NCoords: " );
            for(int i=0; i < normals.length; i++) {
                System.out.print(normals[i] + " ");
                if ((i+1) % 3 == 0)
                   System.out.print(", ");
            }
            System.out.println();
        }

        if (colors != null) {
            System.out.print("Ccoords: " );
            for(int i=0; i < colors.length; i++) {
                System.out.print(colors[i] + " ");
                if ((i+1) % 3 == 0)
                   System.out.print(", ");
            }
            System.out.println();
        }
    }

    public Object clone() {
        GeometryData gd = new GeometryData();
        gd.geometryType = geometryType;
        gd.geometrySubType = geometrySubType;
        gd.geometryComponents = geometryComponents;
        gd.vertexCount = vertexCount;
        if (coordinates != null) {
            gd.coordinates = new float[coordinates.length];
            System.arraycopy(coordinates, 0, gd.coordinates, 0, coordinates.length);
        }

        if (normals != null) {
            gd.normals = new float[normals.length];
            System.arraycopy(normals, 0, gd.normals, 0, normals.length);
        }

        gd.indexesCount = indexesCount;

        if (indexes != null) {
            gd.indexes = new int[indexes.length];
            System.arraycopy(indexes, 0, gd.indexes, 0, indexes.length);
        }

        gd.numStrips = numStrips;

        if (stripCounts != null) {
            gd.stripCounts = new int[stripCounts.length];
            System.arraycopy(stripCounts, 0, gd.stripCounts, 0, stripCounts.length);
        }

        if (textureCoordinates != null) {
            gd.textureCoordinates = new float[textureCoordinates.length];
            System.arraycopy(textureCoordinates, 0, gd.textureCoordinates, 0, textureCoordinates.length);
        }

        if (colors != null) {
            gd.colors = new float[colors.length];
            System.arraycopy(colors, 0, gd.colors, 0, colors.length);
        }

        if (normalIndexes != null) {
            gd.normalIndexes = new int[normalIndexes.length];
            System.arraycopy(normalIndexes, 0, gd.normalIndexes, 0, normalIndexes.length);
        }

        if (texCoordIndexes != null) {
            gd.texCoordIndexes = new int[texCoordIndexes.length];
            System.arraycopy(texCoordIndexes, 0, gd.texCoordIndexes, 0, texCoordIndexes.length);
        }

        if (colorIndexes != null) {
            gd.colorIndexes = new int[colorIndexes.length];
            System.arraycopy(colorIndexes, 0, gd.colorIndexes, 0, colorIndexes.length);
        }

        return gd;
    }
}