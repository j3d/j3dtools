/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;
import javax.media.j3d.QuadArray;

import javax.vecmath.Vector3f;

// Application specific imports

/**
 * A generator of Spring geometry with customisable inner radius, outer
 * radius, number of loops, spacing and facet count.
 * <p>
 *
 * The outer radius is the radius of the center of the tube that forms the
 * spring. The spring has the outer radius in the X-Z plane and it increments
 * along the positive Y axis. The first loop starts at the origin on the
 * positive X axis and rotates counter-clockwise when looking down the
 * -Y axis towards the X-Z plane.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SpringGenerator extends GeometryGenerator
{
    /** The default inner radius of the torus */
    private static final float DEFAULT_INNER_RADIUS = 0.25f;

    /** The default outer radius of the torus */
    private static final float DEFAULT_OUTER_RADIUS = 1.0f;

    /** Default number of faces around the inner radius */
    private static final int DEFAULT_INNER_FACETS = 16;

    /** Default number of faces around the outer radius of one loop */
    private static final int DEFAULT_OUTER_FACETS = 16;

    /** Default number of loops to generate */
    private static final int DEFAULT_LOOP_COUNT = 2;

    /** Default spacing between loops */
    private static final float DEFAULT_LOOP_SPACING = 1.0f;

    /** The inner radius of the torus to generate */
    private float innerRadius;

    /** The outer radius of the torus to generate */
    private float outerRadius;

    /** The spacing between loops */
    private float loopSpacing;

    /** The number of loops to generate */
    private int loopCount;

    /** The number of sections used around the inner radius */
    private int innerFacetCount;

    /** The number of sections used around the outer radius */
    private int outerFacetCount;

    /**
     * Construct a default spring that has:<br>
     * inner radius: 0.25<br>
     * outer radius: 1.0<br>
     * inner facet count: 16<br>
     * outer facet count: 16<br>
     * loop count: 4<br>
     * loop spacing: 1.0<br>
     */
    public SpringGenerator()
    {
        this(DEFAULT_INNER_RADIUS,
             DEFAULT_OUTER_RADIUS,
             DEFAULT_LOOP_SPACING,
             DEFAULT_LOOP_COUNT,
             DEFAULT_INNER_FACETS,
             DEFAULT_OUTER_FACETS);
    }

    /**
     * Construct a spring that has the given radius values with all other
     * values fixed at the defaults
     *
     * @param ir The inner radius to use
     * @param or The outer radius to use
     */
    public SpringGenerator(float ir, float or)
    {
        this(ir,
             or,
             DEFAULT_LOOP_SPACING,
             DEFAULT_LOOP_COUNT,
             DEFAULT_INNER_FACETS,
             DEFAULT_OUTER_FACETS);
    }

    /**
     * Construct a spring that has the given number of loops with all other
     * values fixed at the defaults. The loop count must be one or more.
     *
     * @param lc The loop count
     * @throws IllegalArgumentException The loop count was invalid
     */
    public SpringGenerator(int lc)
    {
        this(DEFAULT_INNER_RADIUS,
             DEFAULT_OUTER_RADIUS,
             DEFAULT_LOOP_SPACING,
             lc,
             DEFAULT_INNER_FACETS,
             DEFAULT_OUTER_FACETS);
    }

    /**
     * Construct a spring with the given loop spacing and all other values
     * fixed at the defaults.
     *
     * @param spacing The spacing between loops
     */
    public SpringGenerator(float spacing)
    {
        this(DEFAULT_INNER_RADIUS,
             DEFAULT_OUTER_RADIUS,
             spacing,
             DEFAULT_LOOP_COUNT,
             DEFAULT_INNER_FACETS,
             DEFAULT_OUTER_FACETS);
    }

    /**
     * Construct a spring that has the selected number of facets but with all
     * other values fixed at the defaults. The minimum number of facets is 3.
     *
     * @param ifc The number of facets to use around the inner radius
     * @param ofc The number of facets to use around the outer radius
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public SpringGenerator(int ifc, int ofc)
    {
        this(DEFAULT_INNER_RADIUS,
             DEFAULT_OUTER_RADIUS,
             DEFAULT_LOOP_SPACING,
             DEFAULT_LOOP_COUNT,
             ifc,
             ofc);
    }

    /**
     * Construct a spring with the given radius, spacing and loop count
     * information. All other values are defaults. The loop count must be
     * greater than or equal to 1.
     *
     * @param ir The inner radius to use
     * @param or The outer radius to use
     * @param spacing The spacing between loops
     * @param lc The loop count
     * @throws IllegalArgumentException The loop count was invalid
     */
    public SpringGenerator(float ir, float or, float spacing, int lc)
    {
        this(ir, or, spacing, lc, DEFAULT_INNER_FACETS, DEFAULT_OUTER_FACETS);
    }

    /**
     * Construct a spring with the given radius, spacing and loop count
     * information, and facet count. The loop count must be greater than or
     * equal to 1 and the facet counts must be 3 or more.
     *
     * @param ir The inner radius to use
     * @param or The outer radius to use
     * @param spacing The spacing between loops
     * @param lc The loop count
     * @param ifc The number of facets to use around the inner radius
     * @param ofc The number of facets to use around the outer radius
     * @throws IllegalArgumentException The loop count was invalid or facet
     *   counts were less than 4
     */
    public SpringGenerator(float ir,
                           float or,
                           float spacing,
                           int lc,
                           int ifc,
                           int ofc)
    {
        if((ifc < 4) || (ofc < 4))
            throw new IllegalArgumentException("Number of facets is < 4");

        if(ifc % 4 != 0)
            throw new IllegalArgumentException("Inner facets not / 4");

        if(lc < 1)
            throw new IllegalArgumentException("Loop count < 1");

        innerRadius = ir;
        outerRadius = or;

        innerFacetCount = ifc;
        outerFacetCount = ofc;

        loopCount = lc;
        loopSpacing = spacing;

        coordsDimensionsChanged = true;
        normalsDimensionsChanged = true;
        coordsCountChanged = true;
        normalsCountChanged = true;

        geometryType = QUADS;
        ccw = true;

        vertexCount = innerFacetCount * outerFacetCount * loopCount * 4;
    }

    /**
     * Get the dimensions of the spring. These are returned as 2 values of
     * inner and outer radius respectively and then spacing and loop count
     * (converted to a float for this case) for the array. A new array is
     * created each time so you can do what you like with it.
     *
     * @return The current size of the spring
     */
    public float[] getDimensions()
    {
        return new float[] {innerRadius, outerRadius, loopSpacing, loopCount};
    }

    /**
     * Change the dimensions of the torus to be generated. Calling this will
     * make the points be re-calculated next time you ask for geometry or
     * normals.
     *
     * @param ir The ir of the cone to generate
     * @param or The or of the bottom of the cone
     * @param ends True if to generate faces for the ends
     */
    public void setDimensions(float ir, float or)
    {
        if((innerRadius != ir) || (outerRadius != or))
        {
            coordsDimensionsChanged = true;
            normalsDimensionsChanged = true;

            innerRadius = ir;
            outerRadius = or;
        }
    }

    /**
     * Change the loop information. Calling this will make the points be
     * re-calculated next time you ask for geometry or normals.
     *
     * @param spacing The spacing between loops
     * @param lc The loop count
     * @throws IllegalArgumentException The loop count was invalid
     */
    public void setLoopDimensions(float spacing, int lc)
    {
        if((loopSpacing != spacing) || (loopCount != lc))
        {
            coordsDimensionsChanged = true;
            normalsDimensionsChanged = true;

            loopSpacing = spacing;

            // Adjust the vertex count too if the loop count has changed
            if(loopCount != lc)
            {
                coordsCountChanged = true;
                normalsCountChanged = true;
                loopCount = lc;

                vertexCount = innerFacetCount * outerFacetCount * loopCount * 4;
            }
        }
    }

    /**
     * Change the number of facets used to create this spring. This will cause
     * the geometry to be regenerated next time they are asked for.
     * The minimum number of facets is 3.
     *
     * @param ifc The number of facets to use around the inner radius
     * @param ofc The number of facets to use around the outer radius
     * @throws IllegalArgumentException The number of facets is less than 4
     */
    public void setFacetCount(int ifc, int ofc)
    {
        if((ifc < 4) || (ofc < 4))
            throw new IllegalArgumentException("Number of facets is < 4");

        if(ifc % 4 != 0)
            throw new IllegalArgumentException("Inner facets not / 4");

        innerFacetCount = ifc;
        outerFacetCount = ofc;

        coordsCountChanged = true;
        normalsCountChanged = true;

        vertexCount = innerFacetCount * outerFacetCount * loopCount * 4;
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
        if(!coordsDimensionsChanged && !coordsCountChanged)
            return coordinates;
        else
            coordsDimensionsChanged = false;

        if((coordinates == null) || coordsCountChanged)
        {
            coordinates = new float[vertexCount * 3];
            coordsCountChanged = false;
        }

        geometryType = QUADS;

        // Increment angles for the inner and outer radius facets.
        double arc_length = (2 * Math.PI * outerRadius) / outerFacetCount;
        float outer_theta = (float)(arc_length / outerRadius);

        arc_length = (2 * Math.PI * innerRadius) / innerFacetCount;
        float inner_theta = (float)(arc_length / innerRadius);

        int count = 0;
        int i, k;
        float rlower;            // radius of first, lower loop
        float rupper;            // radius of second, upper loop
        float cos_theta_i;
        float sin_theta_i;
        float sin_theta_k;
        float sin_theta_k1;
        float x_lower, x_upper;
        float y_lower, y_upper;
        float z_lower, z_upper;
        float y_offset = 0;

        rlower = outerRadius + innerRadius;// radius of first loop
        rupper = rlower;

        float y_space = -loopSpacing;
        float y_inc = loopSpacing / innerFacetCount;

        // Generate one loop. The loop goes around the outer radius
        // adding a quad at a time. When this is finished it moves up one facet
        // around the inner radius. It does this until it reaches the opposite
        // "inner" side of the torus.
        for(k = 0; k < innerFacetCount; k++)
        {
            y_space = -loopSpacing;
            rlower = rupper;
            rupper = outerRadius + (innerRadius * ((float) Math.cos(inner_theta * (k + 1))));

            // Calculate each face in the round. Calculates lower then upper on
            // one side and then moves to the next spot on the loop to calculate
            // the upper then lower values.
            i = 0;
            cos_theta_i = (float)Math.cos(outer_theta * i);
            sin_theta_i = (float)Math.sin(outer_theta * i);

            sin_theta_k = (float)Math.sin(inner_theta * k);
            sin_theta_k1 = (float)Math.sin(inner_theta * (k + 1));

            // Calculate the stuff outside the loop before we start. This saves
            // one lot of calcs through the loop as after the first iteration
            // we save the x and z points from the previous calc.
            x_lower =  rlower * cos_theta_i;
            z_lower = -rlower * sin_theta_i;

            x_upper =  rupper * cos_theta_i;
            z_upper = -rupper * sin_theta_i;

            // y values don't get recalculed for each quad on this strip
            y_lower = innerRadius * sin_theta_k;
            y_upper = innerRadius * sin_theta_k1;

            for( ; i < outerFacetCount; )
            {
                coordinates[count++] = x_upper;
                coordinates[count++] = y_upper + y_space;
                coordinates[count++] = z_upper;

                coordinates[count++] = x_lower;
                coordinates[count++] = y_lower + y_space;
                coordinates[count++] = z_lower;

                // increment for variable to get to the other side of
                // this quad.
                i++;
                y_space += y_inc;
                cos_theta_i = (float)Math.cos(outer_theta * i);
                sin_theta_i = (float)Math.sin(outer_theta * i);

                x_lower =  rlower * cos_theta_i;
                z_lower = -rlower * sin_theta_i;

                x_upper =  rupper * cos_theta_i;
                z_upper = -rupper * sin_theta_i;

                coordinates[count++] = x_lower;
                coordinates[count++] = y_lower + y_space;
                coordinates[count++] = z_lower;

                coordinates[count++] = x_upper;
                coordinates[count++] = y_upper + y_space;
                coordinates[count++] = z_upper;
            }
        }

        // For each loop after the first, just copy the points across
        // but shifted by the appropriate amount.
        float tempVertexCount = count;
        for(i = 1; i < loopCount; i++)
        {
            y_offset += loopSpacing;
            for(k = 0; k < tempVertexCount; )
            {
                coordinates[count++] = coordinates[k++];
                coordinates[count++] = coordinates[k++] + y_offset;
                coordinates[count++] = coordinates[k++];
            }
        }

        return coordinates;
    }
}