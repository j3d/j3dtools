/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spline;

// Standard imports
import javax.vecmath.Vector3f;

// Application specific imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;

/**
 * Geometry generator for generating rectangular Bezier patches.
 * <P>
 *
 * Bezier patches of all orders are permitted. Order information is derived
 * from the provided controlPoint coordinates. When generating a patch, the values
 * for the coordinates are nominally provided in the X and Z plane although no
 * explicit checking is performed to ensure that controlPoint coordinates do not
 * self-intersect or do anything nasty. Normals are always generated as the
 * average between the adjacent edges.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class PatchGenerator extends GeometryGenerator
{
    /** ControlPoint values used to generate patches */
    protected float[][] controlPointCoordinates;

    /** The number of control points in the width */
    protected int numWidthControlPoints;

    /** The number of control points in the depth */
    protected int numDepthControlPoints;

    /** The points on the patch. */
    protected float[][] patchCoordinates;

    /** The smoothed normal for each point on the patch. */
    protected float[][] patchNormals;

    /** The texture coordinate for each point on the patch. */
    protected float[] patchTexcoords;

    /** The number of patch coordinates in depth */
    protected int numPatchValues;

    /** The number of patch coordinates in depth */
    protected int numNormalValues;

    /** The number of patch coordinates in depth */
    protected int numTexcoordValues;

    /** Flag indicating base values have changed */
    protected boolean patchChanged;

    /** Flag indicating base values have changed */
    protected boolean normalsChanged;

    /** Flag indicating base values have changed */
    protected boolean texCoordsChanged;

    /**
     * The number of sections used around the patch. Assumes square
     * tesselation resolution.
     */
    protected int facetCount;

    /**
     * Construct a new generator with no control points set.
     */
    protected PatchGenerator()
    {
        numWidthControlPoints = 0;
        numDepthControlPoints = 0;
        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Change the number of facets used to create this cone. This will cause
     * the geometry to be regenerated next time they are asked for.
     * The minimum number of facets is 3.
     *
     * @param facets The number of facets on the side of the cone
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public void setFacetCount(int facets)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        if(facetCount != facets) {
            patchChanged = true;
            normalsChanged = true;
            texCoordsChanged = true;
        }

        facetCount = facets;
    }

    /**
     * Set the bezier patch control points. The array is presented as
     * [width][depth] with the coordinates flattened as [Xn, Yn, Zn] in the
     * depth array. The order of the patch is determined by the passed array.
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setPatchControlPoints(float[][] controlPoints)
    {
        int min_length = controlPoints[0].length;

        if((controlPoints.length < 3) || (controlPoints[0].length < 3))
            throw new IllegalArgumentException("Depth patch size < 3");

        // second check for consistent lengths of the width patches
        int i;

        for(i = 1; i < controlPoints.length; i++)
        {
            if(controlPoints[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        if((controlPointCoordinates == null) ||
           ((controlPoints.length != controlPointCoordinates.length) &&
            (min_length != controlPointCoordinates[0].length)))
        {
            if((controlPointCoordinates == null) ||
               (controlPoints.length != controlPointCoordinates.length))
            {
                controlPointCoordinates = new float[controlPoints.length][min_length];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[min_length];
            }
        }

        for(i = 0; i < controlPoints.length; i++)
        {
            System.arraycopy(controlPoints[i],
                             0,
                             controlPointCoordinates[i],
                             0,
                             min_length);
        }

        numWidthControlPoints = controlPoints.length;
        numDepthControlPoints = min_length / 3;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the bezier patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     */
    public void setPatchControlPoints(float[] controlPoints,
                                      int numWidth,
                                      int numDepth)
    {
        if(controlPoints.length < 3)
            throw new IllegalArgumentException("Depth patch size < 3");

        if(controlPoints.length < numWidth * numDepth * 3)
            throw new IllegalArgumentException("Array not big enough ");

        int i;

        if((controlPointCoordinates == null) ||
           (controlPointCoordinates.length < numWidth) ||
           (controlPointCoordinates[0].length < numDepth * 3))
        {
            if((controlPointCoordinates == null) ||
               (controlPointCoordinates.length < numDepth))
            {
                controlPointCoordinates = new float[numWidth][numDepth * 3];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[numDepth * 3];
            }
        }

        int offset = 0;

        for(i = 0; i < numWidth; i++)
        {
            System.arraycopy(controlPoints,
                             offset,
                             controlPointCoordinates[i],
                             0,
                             numDepth * 3);
            offset += numDepth * 3;
        }

        numWidthControlPoints = numWidth;
        numDepthControlPoints = numDepth;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Get the number of vertices that this generator will create for the
     * shape given in the definition.
     *
     * @param data The data to patch the calculations on
     * @return The vertex count for the object
     * @throws UnsupportedTypeException The generator cannot handle the type
     *   of geometry you have requested.
     */
    public int getVertexCount(GeometryData data)
        throws UnsupportedTypeException
    {
        int ret_val = 0;

        switch(data.geometryType)
        {
            case GeometryData.TRIANGLES:
                ret_val = facetCount * facetCount * 6 ;
                break;
            case GeometryData.QUADS:
                ret_val = facetCount * facetCount * 4;
                break;

            // These all have the same vertex count
            case GeometryData.TRIANGLE_STRIPS:
            case GeometryData.TRIANGLE_FANS:
            case GeometryData.INDEXED_TRIANGLES:
            case GeometryData.INDEXED_QUADS:
            case GeometryData.INDEXED_TRIANGLE_STRIPS:
            case GeometryData.INDEXED_TRIANGLE_FANS:
                ret_val = facetCount * facetCount;
                break;

            default:
                throw new UnsupportedTypeException("Unknown geometry type: " +
                                                   data.geometryType);
        }

        return ret_val;
    }

    /**
     * Convenience method to regenerate the smoothed normals for the
     * patch. It assumes that the patch has been regenerated just before
     * this call.
     */
    protected void regenerateNormals()
    {
        if(!normalsChanged)
            return;

        normalsChanged = false;
        numNormalValues = numPatchValues;

        if((patchNormals == null) ||
           (patchNormals.length <= facetCount) ||
           (patchNormals[0].length < numNormalValues))
        {
            patchNormals = new float[facetCount + 1][numNormalValues];
        }

        Vector3f norm;
        int count = 0;
        int i, j;

        // The first edge
        // corner point - normal based on only that face
        norm = createFaceNormal(patchCoordinates, 1, 0, 0, 0, 0, 3);

        patchNormals[0][count++] = norm.x;
        patchNormals[0][count++] = norm.y;
        patchNormals[0][count++] = norm.z;

        for(i = 1; i < facetCount; i++)
        {
            norm = calcSideAverageNormal(0, count,
                                         0, count + 3,
                                         1, count,
                                         0, count - 3);

            patchNormals[0][count++] = norm.x;
            patchNormals[0][count++] = norm.y;
            patchNormals[0][count++] = norm.z;
        }

        // Last corner point of the first row
        norm = createFaceNormal(patchCoordinates,
                                0, count,
                                0, count,
                                1, count - 3);

        patchNormals[0][count++] = norm.x;
        patchNormals[0][count++] = norm.y;
        patchNormals[0][count++] = norm.z;

        // Now, process all of the internal points
        for(i = 1; i < facetCount; i++)
        {
            count = 0;
            norm = calcSideAverageNormal(i, count,
                                         i - 1, count,
                                         i, count + 3,
                                         i + 1, count);

            patchNormals[i][count++] = norm.x;
            patchNormals[i][count++] = norm.y;
            patchNormals[i][count++] = norm.z;

            for(j = 1; j < facetCount; j++)
            {

                norm = calcQuadAverageNormal(i, count,
                                             i, count + 3,
                                             i + 1, count,
                                             i, count - 3,
                                             i - 1, count);

                patchNormals[i][count++] = norm.x;
                patchNormals[i][count++] = norm.y;
                patchNormals[i][count++] = norm.z;
            }

            // Last point of the row
            norm = calcSideAverageNormal(i, count,
                                         i + 1, count,
                                         i, count - 3,
                                         i - 1, count);

            patchNormals[i][count++] = norm.x;
            patchNormals[i][count++] = norm.y;
            patchNormals[i][count++] = norm.z;
        }

        // The last edge
        // corner point - normal based on only that face
        count = 0;
        norm = createFaceNormal(patchCoordinates,
                                facetCount - 1, count,
                                facetCount - 1, count,
                                facetCount, count + 3);

        patchNormals[facetCount][count++] = norm.x;
        patchNormals[facetCount][count++] = norm.y;
        patchNormals[facetCount][count++] = norm.z;

        for(i = 1; i < facetCount; i++)
        {
            norm = calcSideAverageNormal(i, count,
                                         i, count - 3,
                                         i - 1, count,
                                         i, count + 3);

            patchNormals[facetCount][count++] = norm.x;
            patchNormals[facetCount][count++] = norm.y;
            patchNormals[facetCount][count++] = norm.z;
        }

        // Last corner point of the first row
        norm = createFaceNormal(patchCoordinates,
                                facetCount, count,
                                facetCount, count - 3,
                                facetCount - 1, count);

        patchNormals[facetCount][count++] = norm.x;
        patchNormals[facetCount][count++] = norm.y;
        patchNormals[facetCount][count++] = norm.z;
    }

    /**
     * Convenience method to calculate the average normal value between
     * two quads - ie along the side of an object
     *
     * @param coords The coordinates to generate from
     * @param p The centre point
     * @param p1 The first point of the first side
     * @param p2 The middle, shared side point
     * @param p3 The last point of the second side
     * @return The averaged vector
     */
    private Vector3f calcSideAverageNormal(int w, int p,
                                           int w1, int p1,
                                           int w2, int p2,
                                           int w3, int p3)
    {
        Vector3f norm;
        float x, y, z;

        // Normal first for the previous quad
        norm = createFaceNormal(patchCoordinates, w, p, w1, p1, w2, p2);
        x = norm.x;
        y = norm.y;
        z = norm.z;

        // Normal for the next quad
        norm = createFaceNormal(patchCoordinates, w, p, w2, p2, w3, p3);

        // create the average of each compoenent for the final normal
        norm.x = (norm.x + x) / 2;
        norm.y = (norm.y + y) / 2;
        norm.z = (norm.z + z) / 2;

        norm.normalize();

        return norm;
    }

    /**
     * Convenience method to create quad average normal amongst four
     * quads based around a common centre point (the one having the normal
     * calculated).
     *
     * @param coords The coordinates to generate from
     * @param p The centre point
     * @param p1 shared point between first and last quad
     * @param p2 shared point between first and second quad
     * @param p3 shared point between second and third quad
     * @param p4 shared point between third and fourth quad
     * @return The averaged vector
     */
    private Vector3f calcQuadAverageNormal(int w, int p,
                                           int w1, int p1,
                                           int w2, int p2,
                                           int w3, int p3,
                                           int w4, int p4)
    {
        Vector3f norm;
        float x, y, z;

        // Normal first for quads 1 & 2
        norm = createFaceNormal(patchCoordinates, w, p, w2, p2, w1, p1);
        x = norm.x;
        y = norm.y;
        z = norm.z;

        // Normal for the quads 2 & 3
        norm = createFaceNormal(patchCoordinates, w, p, w2, p2, w3, p3);

        x += norm.x;
        y += norm.y;
        z += norm.z;

        // Normal for quads 3 & 4
        norm = createFaceNormal(patchCoordinates, w, p, w3, p3, w4, p4);

        x += norm.x;
        y += norm.y;
        z += norm.z;

        // Normal for quads 1 & 4
        norm = createFaceNormal(patchCoordinates, w, p, w4, p4, w1, p1);

        // create the average of each compoenent for the final normal
        norm.x = (norm.x + x) / 4;
        norm.y = (norm.y + y) / 4;
        norm.z = (norm.z + z) / 4;

        norm.normalize();

        return norm;
    }

    /**
     * Regenerate the texture coordinate points.
     * Assumes regenerateBase has been called before this
     */
    private final void regenerateTexcoords()
    {
        if(!texCoordsChanged)
            return;

        texCoordsChanged = false;

        numTexcoordValues = facetCount * facetCount * 2;

        if((patchTexcoords == null) ||
           (numTexcoordValues > patchTexcoords.length))
        {
            patchTexcoords = new float[numTexcoordValues];
        }

        float d = 0;
        float w = 0;
        float width_inc = 1.0f / (facetCount - 1);
        float depth_inc = 1.0f / (facetCount - 1);

        int count = 0;

        for(int i = 0; i < facetCount; i++)
        {
            for(int j = 0;  j < facetCount; j++)
            {
                patchTexcoords[count++] = w;
                patchTexcoords[count++] = d;

                w += width_inc;
            }

            d += depth_inc;
            w = 0;
        }
    }
}
