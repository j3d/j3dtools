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
// none

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
 * @version $Revision: 1.1 $
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
    protected float[][] patchCoordinates;

    /** The number of patch coordinates in depth */
    protected int numPatchValues;

    /** Flag indicating base values have changed */
    protected boolean patchChanged;

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

        if(facetCount != facets)
            patchChanged = true;

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
    protected regenerateNormals()
    {
    }
}
