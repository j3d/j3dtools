/*****************************************************************************
 *    J3D.org Copyright (c) 2000
 *     Java Source
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
 * A customisable sphere where you can specify the radius, center and
 * length of the arc.
 * <p>
 *
 * @author Unknown
 * @version $Revision: 1.1 $
 */
public class Sphere
{
    private static final float RADIUS = 1.0f;
    private static final float ARCLENGTH = 0.1f;
    private static final float XPOSITION = 0.0f;
    private static final float YPOSITION = 0.0f;
    private static final float ZPOSITION = 0.0f;

    private Shape3D ssphere;
    private float[] qverts;
    private float x, y, z, theta, t, previousx, previousz,
        calct, rlower, rupper, num, mag, x1, x2, x3, x4, y1, y2, y3, y4,
        z1, z2, z3, z4, xn, yn, zn;
    private int half, numcirc, upcount = 1;
    private int vertCount = 0;
    private int normalcount = 0;
    private Vector3f[] normals;


    /**
    *     Constructs a Sphere of radius 1, arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Sphere()
    {
        this(RADIUS, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Sphere of radius 'r', arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Sphere(float r)
    {
        this(r, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Sphere of radius 1, arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with Appearance sphereAppearance
    **/
    public  Sphere(Appearance sphereAppearance)
    {
        this(RADIUS, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, sphereAppearance);
    }

    /**
    *     Constructs a Sphere of radius 'r', arclength of a quad 'arclength',
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Sphere(float r, float arclength)
    {
        this(r, arclength,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Sphere of radius 1, arclength of a quad 0.1,
    *     at coordinates 'xpos', 'ypos', 'zpos',
    *     with null Appearance
    **/
    public  Sphere(float xpos, float ypos, float zpos)
    {
        this(RADIUS, ARCLENGTH,
            xpos, ypos, zpos, null);
    }

    /**
    *     Constructs a Sphere of radius 'r', arclength of a quad 0.1,
    *     at coordinates 0, 0, 0,
    *     with Appearance sphereAppearance
    **/
    public  Sphere(float r, Appearance sphereAppearance)
    {
        this(r, ARCLENGTH,
            XPOSITION, YPOSITION, ZPOSITION, sphereAppearance);
    }

    /**
    *     Constructs a Sphere of radius 'r', arclength of a quad 'arclength',
    *     at coordinates 0, 0, 0,
    *     with Appearance sphereAppearance
    **/
    public  Sphere(float r, float arclength, Appearance sphereAppearance)
    {
        this(r, arclength,
            XPOSITION, YPOSITION, ZPOSITION, sphereAppearance);
    }

    /**
    *     Constructs a Sphere of radius 1, arclength of a quad 0.1,
    *     at coordinates 'xpos', 'ypos', 'zpos',
    *     with Appearance 'sphereAppearance'
    **/
    public  Sphere(float xpos, float ypos, float zpos,
        Appearance sphereAppearance)
    {
        this(RADIUS, ARCLENGTH,
            xpos, ypos, zpos, sphereAppearance);
    }

    /**
    *     Constructs a Sphere centered at 'xpos', 'ypos', 'zpos',
    *     with radius 'r', arclength of a quad 'arclength',
    *     and Appearance 'sphereAppearance'
    **/
    public  Sphere(float r, float arclength,
        float xpos, float ypos, float zpos, Appearance sphereAppearance)
    {
        t = arclength;
        num = ((float) (2*Math.PI*r)/t);
        numcirc = (int) num;

        // Change the arclength to the closest value that fits.
        calct = ((float) (2*Math.PI*r)/numcirc);
        t = calct;
        theta = t/r;
        half = (int) ((r*Math.PI/2)/t)+1;

        // In case theres too many quads...
        if ( 2*(half*(6*(2*numcirc))) > 600000)
        {
            System.out.println("Too detailed! Choose a bigger" +
                " arclength or smaller radius.");
            System.exit(0);
        }
        qverts = new float[2*(half*(6*(2*numcirc)))];

        rlower = r;// radius of first loop
        rupper = r*((float) Math.cos(theta));// radius of second loop

        // upper hemisphere
        for (int k=0; k < half; k++)
        {
            for (int i=0 ; i < numcirc; i = i)
            {
                x1 =  rlower*((float) Math.cos(theta*i));
                qverts[vertCount] = x1;
                vertCount++;

                y1 =  r*((float) Math.sin(theta*k));
                if (y1 < 0)
                {
                    y1 = -y1;
                }
                qverts[vertCount] = y1;
                vertCount++;

                z1 =  rlower*((float) Math.sin(theta*i));
                qverts[vertCount] = -z1;
                vertCount++;

                x2 =  rupper*((float) Math.cos(theta*i));
                qverts[vertCount] = x2;
                vertCount++;

                y2 =  r*((float) Math.sin(theta*(k+1)));
                if (y2 < 0)
                {
                    y2 = -y2;
                }
                qverts[vertCount] = y2;
                vertCount++;

                z2 =  rupper*((float) Math.sin(theta*i));
                qverts[vertCount] = -z2;
                vertCount++;

                i++;// increment for variable

                x3 =  rupper*((float) Math.cos(theta*i));
                qverts[vertCount] = x3;
                vertCount++;

                y3 =  r*((float) Math.sin(theta*(k+1)));
                if (y3 < 0)
                {
                    y3 = -y3;
                }
                qverts[vertCount] = y3;
                vertCount++;

                z3 =  rupper*((float) Math.sin(theta*i));
                qverts[vertCount] = -z3;
                vertCount++;

                x4 =  rlower*((float) Math.cos(theta*i));
                qverts[vertCount] = x4;
                vertCount++;

                y4 =  r*((float) Math.sin(theta*k));
                if (y4 < 0)
                {
                    y4 = -y4;
                }
                qverts[vertCount] = y4;
                vertCount++;

                z4 =  rlower*((float) Math.sin(theta*i));
                qverts[vertCount] = -z4;
                vertCount++;
            }
            rlower = rupper;
            upcount++;
            rupper = r *((float) Math.cos(theta*upcount));
        }
        rlower = r;
        rupper = r  *((float) Math.cos(theta));
        upcount = 1;

        // lower hemisphere
        // (just mirror the upper half on y axis)
        int tempVertCount = vertCount;
        for (int k=0; k < tempVertCount; k = k+3)
        {
            qverts[vertCount] = qverts[k];
            vertCount++;
            qverts[vertCount] = -(qverts[k+1]);
            vertCount++;
            qverts[vertCount] = qverts[k+2];
            vertCount++;
        }

        //position the sphere corectly
        for (int j=0; j < vertCount; j = j+3)
        {
            qverts[j] = qverts[j] + xpos;
            qverts[j+1] = qverts[j+1] + ypos;
            qverts[j+2] = qverts[j+2] + zpos;
        }

        QuadArray sphereGeometry = new QuadArray( vertCount/3,
            QuadArray.COORDINATES | QuadArray.NORMALS);

        sphereGeometry.setCapability( QuadArray.ALLOW_COLOR_WRITE );
        sphereGeometry.setCapability( QuadArray.ALLOW_COORDINATE_WRITE );
        sphereGeometry.setCoordinates( 0, qverts );

        // Calculate normals of all points.
        normals = new Vector3f[vertCount/3];
        for (int w = 0; w < vertCount; w = w + 3)
        {
            Vector3f norm = new Vector3f(0.0f, 0.0f, 0.0f);
            mag = qverts[w] * qverts[w] + qverts[w+1] * qverts[w+1]
                + qverts[w+2] * qverts[w+2];
            if (mag != 0.0)
            {
                mag = 1.0f / ((float) Math.sqrt(mag));
                xn = qverts[w]*mag;
                yn = qverts[w+1]*mag;
                zn = qverts[w+2]*mag;
                norm = new Vector3f(xn, yn, zn);
            }
            normals[normalcount] = norm;
            sphereGeometry.setNormal(normalcount, norm);
            normalcount++;
        }

        ssphere = new Shape3D(sphereGeometry, sphereAppearance);
    }

    public Shape3D getChild()
    {
        return ssphere;
    }
}