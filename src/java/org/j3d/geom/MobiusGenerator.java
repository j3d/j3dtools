/*****************************************************************************
 *                          J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
// None

// Application specific imports
// None

/**
 * A mobius strip geometry generator with specified number of divisions per
 * strip, number of strips, position, and appearance.
 * <p>
 * The generator is used to create Mobius strip shapes as geometry. The output
 * are coordinates as quads suitable for use in an unindexed-quad array. The
 * number of faces (polygons) generated is derived from the number of divisions
 * times.
 * <p>
 * When normals are calculated, they are only generated for one side of the
 * strip. When you create the Java3D geometry from this you should make sure
 * to set the rendering attributes to be double sided.
 * <p>
 * The algorithm was adapted from Tore Nordstrand's Math Image Gallery:
 * <A HREF="http://www.uib.no/people/nfytn/mathgal.htm">
 * http://www.uib.no/people/nfytn/mathgal.htm</A> (This algorithm is not
 * perfect yet: The strips are slightly out of alignment. This is easy to see
 * with a small number of strips
 * <p>
 * The current algorithm does not have control over the diameter of the strip
 * created. I'd like to fix it but don't know where to start yet.
 *
 * @author Justin Couch (heavily revised version of code from unknown source)
 * @version $Revision: 1.2 $
 */
public class MobiusGenerator extends GeometryGenerator
{
    /** The default number of divisions to create */
    private static final int DEFAULT_DIVISIONS = 28;

    /** The default number of strips along the length to create */
    private static final int DEFAULT_STRIPS = 14;

    /** The number of strips to use */
    private int strips;

    /** The number of divisions to use */
    private int divisions;

    /**
     * Constructs a mobius strip with 28 divisions per strip,
     * 14 strips.
     */
    public MobiusGenerator()
    {
        this(DEFAULT_DIVISIONS, DEFAULT_STRIPS);
    }

    /**
     * Constructs a mobius strip with number of divisions per
     * strip and number of strips.
     *
     * @param divisions The number of divisions to use
     * @param strips The number of strips to use
     */
    public MobiusGenerator(int divisions, int strips)
    {
        this.divisions = divisions;
        this.strips = strips;
        coordsDimensionsChanged = true;
        normalsDimensionsChanged = true;
        coordsCountChanged = true;
        normalsCountChanged = true;

        geometryType = QUADS;

        // make sure there is always an even number for the vertex count
        if(divisions % 2 == 1)
            this.divisions++;

        vertexCount = (strips + 1) * this.divisions * 4;
    }

    /**
     * Change the dimensions of the cone to be generated. Calling this will
     * make the points be re-calculated next time you as for geometry or
     * normals.
     *
     * @param divisions The number of divisions to use
     * @param strips The number of strips to use
     */
    public void setDimensions(int divisions, int strips)
    {

        if((this.divisions == divisions) && (this.strips == strips))
            return;

        this.divisions = divisions;
        this.strips = strips;
        coordsDimensionsChanged = true;
        normalsDimensionsChanged = true;

        // make sure there is always an even number for the vertex count
        if(divisions % 2 == 1)
            this.divisions++;

        vertexCount = (strips + 1) * this.divisions * 4;
    }

    /**
     * Generates new set of points. If the dimensions have not changed since
     * the last call, the identical array will be returned. Note that you
     * should make a copy of this if you intend to call this method more than
     * once as it will replace the old values with the new ones and not
     * reallocate the array.
     *
     * @return An array of points representing the geometry vertices
     */
    public float[] generateUnindexedCoordinates()
    {
        if(!coordsDimensionsChanged)
            return coordinates;
        else
        {
            coordsDimensionsChanged = false;
            if((coordinates == null) ||
               (coordinates.length != vertexCount * 3))
                coordinates = new float[vertexCount * 3];
        }

        geometryType = QUADS;

        float vmin = -0.3f;
        float vmax = 0.3f;
        float umin = 0.0f;
        float umax = (float)(2 * Math.PI);
        float u_inc = (umax - umin) / divisions;
        float v_inc = (vmax - vmin) / strips;

        // Begin Algorithm
        float uu = umin;
        double cos_uu;
        double cos_uu_2;
        double sin_uu;
        double sin_uu_2;
        float x, y, z;
        int count = 0;

        while (uu <= umax)                //outer loop
        {
            float vv = vmin;
            while (vv <= vmax)            //inner loop
            {
                cos_uu = Math.cos(uu);
                cos_uu_2 = Math.cos(uu / 2);
                sin_uu = Math.sin(uu);
                sin_uu_2 = Math.sin(uu/2);

                x = (float)(cos_uu + vv * cos_uu_2 * cos_uu);
                y = (float)(sin_uu + vv * cos_uu_2 * sin_uu);
                z = (float)(vv * sin_uu_2);
                coordinates[count++] = x;
                coordinates[count++] = y;
                coordinates[count++] = z;

                uu = uu + u_inc;

                cos_uu = Math.cos(uu);
                cos_uu_2 = Math.cos(uu / 2);
                sin_uu = Math.sin(uu);
                sin_uu_2 = Math.sin(uu/2);

                x = (float)(cos_uu + vv * cos_uu_2 * cos_uu);
                y = (float)(sin_uu + vv * cos_uu_2 * sin_uu);
                z = (float)(vv * sin_uu_2);
                coordinates[count++] = x;
                coordinates[count++] = y;
                coordinates[count++] = z;

                vv = vv + v_inc;

                x = (float)(cos_uu + vv * cos_uu_2 * cos_uu);
                y = (float)(sin_uu + vv * cos_uu_2 * sin_uu);
                z = (float)(vv * sin_uu_2);
                coordinates[count++] = x;
                coordinates[count++] = y;
                coordinates[count++] = z;

                uu = uu - u_inc;
                cos_uu = Math.cos(uu);
                cos_uu_2 = Math.cos(uu / 2);
                sin_uu = Math.sin(uu);
                sin_uu_2 = Math.sin(uu/2);

                x = (float)(cos_uu + vv * cos_uu_2 * cos_uu);
                y = (float)(sin_uu + vv * cos_uu_2 * sin_uu);
                z = (float)(vv * sin_uu_2);
                coordinates[count++] = x;
                coordinates[count++] = y;
                coordinates[count++] = z;
            }

            uu = uu + u_inc;
        }
        // End Algorithm

        return coordinates;
    }
}