/*****************************************************************************
 *  J3D.org Copyright (c) 2000
 *   Java Source
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
 * A torus where you specify inner radius, outer radius, arclength of an
 * average quad, coordinates, and appearance
 * <p>
 *
 * @author Unknown
 * @version $Revision: 1.1 $
 */
public class Torus
{
    private static final float INNERRADIUS = 1.0f;
    private static final float OUTERRADIUS = 3.0f;
    private static final float ARCLENGTH = 0.1f;
    private static final float XPOSITION = 0.0f;
    private static final float YPOSITION = 0.0f;
    private static final float ZPOSITION = 0.0f;

    private Shape3D storus;
    private float[] qverts;
    private float x, y, z, theta, t, previousx, previousz,
        calct, rlower, rupper, num, mag, x1, x2, x3, x4, y1, y2, y3, y4,
        z1, z2, z3, z4, xn, yn, zn, secr, yval, r;
    private int half, numcirc, upcount = 1;
    private int vertCount = 0;
    private int normalcount = 0;

    private Vector3f[] normals;


    /**
    *     Constructs a Torus of inner radius 1, outer radius 3,
    *     arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Torus()
    {
        this(INNERRADIUS, OUTERRADIUS, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Torus of inner radius 'ir', outer radius 'or',
    *     arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Torus(float ir, float or)
    {
        this(ir, or, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Torus of inner radius 1, outer radius 3,
    *     arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with Appearance torusAppearance
    **/
    public  Torus(Appearance torusAppearance)
    {
        this(INNERRADIUS, OUTERRADIUS, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, torusAppearance);
    }

    /**
    *     Constructs a Torus of inner radius 'ir', outer radius 'or',
    *     arclength of a quad 'arclength',
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Torus(float ir, float or, float arclength)
    {
        this(ir, or, arclength,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Torus of inner radius 'ir', outer radius 'or',
    *     arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with Appearance torusAppearance
    **/
    public  Torus(float ir, float or, Appearance torusAppearance)
    {
        this(ir, or, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, torusAppearance);
    }

    /**
    *     Constructs a Torus of inner radius 'ir', outer radius 'or',
    *     arclength of a quad 'arclength',
    *     at coordinates 0, 0, 0,
    *     with Appearance torusAppearance
    **/
    public  Torus(float ir, float or, float arclength,
        Appearance torusAppearance)
    {
        this(ir, or, arclength,
            XPOSITION, YPOSITION, ZPOSITION, torusAppearance);
    }

    /**
    *     Constructs a Torus centered at 'xpos', 'ypos', 'zpos',
    *     with inner radius 'ir', outer radius 'or',
    *     arclength of a quad 'arclength',
    *     and Appearance 'torusAppearance'
    **/
    public  Torus(float ir, float or, float arclength,
        float xpos, float ypos, float zpos, Appearance torusAppearance)
    {
        if (arclength <= 0.0)
        {
            arclength = 0.5f;
        }
        t = arclength;
        r = (or - ir) / 2;
        num = ((float) (2*Math.PI*r)/t);
        numcirc = (int) num;

        // Change the arclength to the closest value that fits.
        calct = ((float) (2*Math.PI*r)/numcirc);
        t = calct;
        theta = t/r;
        half = (int) ((r*Math.PI)/t)+1;

        // In case theres too many quads...
        if ( 2*(half*(6*(2*numcirc))) > 600000)
        {
            System.out.println("Too detailed! Choose a bigger" +
                " arclength or smaller radiuses.");
            System.exit(0);
        }

        qverts = new float[2*(half*(6*(2*(numcirc+1))))];

        rlower = or;// radius of first loop
        rupper = or - (r - (r*((float) Math.cos(theta))));// radius of second loop

        // upper half
        for (int k=0; k < half; k++)
        {
            for (int i=0 ; i < numcirc+1; i++)
            {
                x1 =  rlower*((float) Math.cos(theta*i));
                coordinates[vertCount] = x1;

                y1 = r*((float) Math.sin(theta*(k)));
                coordinates[vertCount] = y1;

                z1 =  rlower*((float) Math.sin(theta*i));
                coordinates[vertCount] = -z1;

                x2 =  rlower*((float) Math.cos(theta*(i+1)));
                coordinates[vertCount] = x2;

                y2 = r*((float) Math.sin(theta*(k)));
                coordinates[vertCount] = y2;

                z2 =  rlower*((float) Math.sin(theta*(i+1)));
                coordinates[vertCount] = -z2;

                x3 =  rupper*((float) Math.cos(theta*(i+1)));
                coordinates[vertCount] = x3;

                y3 =  r*((float) Math.sin(theta*(k+1)));
                coordinates[vertCount] = y3;

                z3 =  rupper*((float) Math.sin(theta*(i+1)));
                coordinates[vertCount] = -z3;

                x4 =  rupper*((float) Math.cos(theta*i));
                coordinates[vertCount] = x4;

                y4 =  r*((float) Math.sin(theta*(k+1)));
                coordinates[vertCount] = y4;

                z4 =  rupper*((float) Math.sin(theta*i));
                coordinates[vertCount] = -z4;
            }

            rlower = rupper;
            upcount++;
            rupper = or - (r - (r*((float) Math.cos(theta*upcount))));
        }

        rlower = or;// radius of first loop
        rupper = or - (r - (r*((float) Math.cos(theta))));// radius of second loop
        upcount = 0;

        // lower half just mirrors the upper half on y axis but we have to
        // change the winding so that they all remain with the same clockwise
        // ordering of vertices
        int tempVertCount = vertCount;
        for ( int k =0; k < tempVertCount; k = k+12)
        {
            coordinates[vertCount+0+3*0] =   coordinates[k+0+3*0];
            coordinates[vertCount+1+3*0] = -(coordinates[k+1+3*0]);
            coordinates[vertCount+2+3*0] =   coordinates[k+2+3*0];
            coordinates[vertCount+0+3*1] =   coordinates[k+0+3*3];
            coordinates[vertCount+1+3*1] = -(coordinates[k+1+3*3]);
            coordinates[vertCount+2+3*1] =   coordinates[k+2+3*3];
            coordinates[vertCount+0+3*2] =   coordinates[k+0+3*2];
            coordinates[vertCount+1+3*2] = -(coordinates[k+1+3*2]);
            coordinates[vertCount+2+3*2] =   coordinates[k+2+3*2];
            coordinates[vertCount+0+3*3] =   coordinates[k+0+3*1];
            coordinates[vertCount+1+3*3] = -(coordinates[k+1+3*1]);
            coordinates[vertCount+2+3*3] =   coordinates[k+2+3*1];
            vertCount += 12;
        }
    }

    /**
     * Generate a new set of normals. If the dimensions have not changed since
     * the last call, the identical array will be returned. Note that you
     * should make a copy of this if you intend to call this method more than
     * once as it will replace the old values with the new ones and not
     * reallocate the array.
     *
     * @return An array of points representing the geometry normals
     */
    public float[] generateUnindexedNormals()
    {
        if(!normalsDimensionsChanged && !normalsCountChanged)
            return normals;
        else
            normalsDimensionsChanged = false;

        if((normals == null) || normalsCountChanged)
        {
            // Normals depend on the facets for the calculations. If the
            // normals have changed then we really want to recalculate the
            // geometry first.
            generateUnindexedCoordinates();
            normalsCountChanged = false;

            if((normals == null) || (normals.length != vertexCount * 3))
                normals = new float[vertexCount * 3];
        }

        float x_coord;
        float y_coord;
        float z_coord;
        float mag;
        int count = 0;

        // Calculate normals of all points.
        for(int w = 0; w < coordinates.length;)
        {
            x_coord = coordinates[w++];
            y_coord = coordinates[w++];
            z_coord = coordinates[w++];

            mag = x_coord * x_coord +
                  y_coord * y_coord +
                  z_coord * z_coord;

            if(mag != 0.0)
            {
                mag = 1 / ((float)Math.sqrt(mag));
                normals[count++] = x_coord * mag;
                normals[count++] = y_coord * mag;
                normals[count++] = z_coord * mag;
            }
            else
            {
                normals[count++] = 0;
                normals[count++] = 0;
                normals[count++] = 0;
            }
        }

        return normals;
    }
}