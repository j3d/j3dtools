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
 * A Spring with customisable inner radius, outer radius, number of loops,
 * spacing, arclength of an average quad, coordinates, and appearance
 * <p>
 *
 * @author Unknown
 * @version $Revision: 1.1 $
 */
public class Spring
{
    private static final float INNER_RADIUS = 0.2f;
    private static final float OUTER_RADIUS = 0.3f;
    private static final int LOOPS = 8;
    private static final float SPACING = 0.25f;
    private static final float ARCLENGTH = 0.02f;
    private static final float X_POSITION = 0.0f;
    private static final float Y_POSITION = 0.0f;
    private static final float Z_POSITION = 0.0f;

    private Shape3D sspring;
    private float[] qverts;
    private float x, y, z, theta, t, previousx, previousz,
        calct, rlower, rupper, num, mag, x1, x2, x3, x4, y1, y2, y3, y4,
        z1, z2, z3, z4, xn, yn, zn, secr, yval, r;
    private int half, numcirc, upcount = 1;
    private int vertCount = 0;
    private int normalcount = 0;
    private Vector3f[] normals;

    /**
    *     Constructs a Spring of inner radius 0.2, outer radius 0.3,
    *     8 loops, 0.25 spacing, arclength of a quad 0.02,
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Spring()
    {
        this(INNER_RADIUS, OUTER_RADIUS, LOOPS, SPACING, ARCLENGTH,
            X_POSITION, Y_POSITION, Z_POSITION, null);
    }

    /**
    *     Constructs a Spring of inner radius 'ir', outer radius 'or',
    *     8 loops, 0.25 spacing, arclength of a quad 0.02,
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Spring(float ir, float or)
    {
        this(ir, or, LOOPS, SPACING, ARCLENGTH,
            X_POSITION, Y_POSITION, Z_POSITION, null);
    }

    /**
    *     Constructs a Spring of inner radius 0.2, outer radius 0.3,
    *     8 loops, 0.25 spacing, arclength of a quad 0.02,
    *     at coordinates 0, 0, 0,
    *     with Appearance springAppearance
    **/
    public  Spring(Appearance springAppearance)
    {
        this(INNER_RADIUS, OUTER_RADIUS, LOOPS, SPACING, ARCLENGTH,
            X_POSITION, Y_POSITION, Z_POSITION, springAppearance);
    }

    /**
    *     Constructs a Spring of inner radius 'ir', outer radius 'or',
    *     8 loops, 0.25 spacing, arclength of a quad 'arclength',
    *     at coordinates 0, 0, 0,
    *     with null Appearance
    **/
    public  Spring(float ir, float or, float arclength)
    {
        this(ir, or, LOOPS, SPACING, arclength,
            X_POSITION, Y_POSITION, Z_POSITION, null);
    }

    /**
    *     Constructs a Spring of inner radius 'ir', outer radius 'or',
    *     8 loops, 0.25 spacing, arclength of a quad 0.02,
    *     at coordinates 0, 0, 0,
    *     with Appearance springAppearance
    **/
    public  Spring(float ir, float or, Appearance springAppearance)
    {
        this(ir, or, LOOPS, SPACING, ARCLENGTH,
            X_POSITION, Y_POSITION, Z_POSITION, springAppearance);
    }

    /**
    *     Constructs a Spring of inner radius 'ir', outer radius 'or',
    *     8 loops, 0.25 spacing, arclength of a quad 'arclength',
    *     at coordinates 0, 0, 0,
    *     with Appearance springAppearance
    **/
    public  Spring(float ir, float or, float arclength,
        Appearance springAppearance)
    {
        this(ir, or, LOOPS, SPACING, arclength,
            X_POSITION, Y_POSITION, Z_POSITION, springAppearance);
    }

    /**
    *     Constructs a Spring centered at 'xpos', 'ypos', 'zpos',
    *     with inner radius 'ir', outer radius 'or',
    *     #loops 'loops', spacing 'spacing', arclength of a quad 'arclength',
    *     and Appearance 'springAppearance'
    **/
    public  Spring(float ir, float or, int loops, float spacing,
        float arclength, float xpos, float ypos, float zpos,
        Appearance springAppearance)
    {
        if (spacing <= 0.0)
        {
            spacing = 0.01f;
            System.out.println("Spacing was set too low.. defaulting to 0.01");
        }
        if (arclength <= 0.0)
        {
            arclength = 0.5f;
            System.out.println("Arclength was set too low.. defaulting to 0.5");
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

        int nspirals = loops;
        if (nspirals == 0)
        {
            nspirals = 1;
            System.out.println("InnerInnerRadius is too small.. generating just one section");
        }

        // In case theres too many quads...
        if ( 2*(half*(6*(2*numcirc))) > 600000)
        {
            System.out.println("Too detailed! Choose a bigger" +
                " arclength or smaller radiuses.");
            System.exit(0);
        }
        qverts = new float[nspirals*2*(half*(6*(2*(numcirc))))];

        float tempy = -spacing;
        float tempinc = spacing/numcirc;

        float xadder = 0;
        float zadder = 0;
        int spiralVertCount = 0;

        for (int sp=0; sp < nspirals; sp++)
        {
            if (sp == 0)
            {
                rlower = or;// radius of first loop
                rupper = or - (r - (r*((float) Math.cos(theta))));// radius of second loop
                for (int k=0; k < 2*half; k++)
                {
                    tempy = -spacing;
                    for (int i=0 ; i < numcirc; i++)
                    {
                        x1 =  rlower*((float) Math.cos(theta*i));
                        qverts[vertCount] = x1 + xpos;
                        vertCount++;

                        z1 =  rlower*((float) Math.sin(theta*i));
                        qverts[vertCount] = -z1 + zpos;
                        vertCount++;

                        y1 = r*((float) Math.sin(theta*(k))) + tempy;// + zadder;
                        qverts[vertCount] = y1 + ypos;
                        vertCount++;

                        tempy = tempy + tempinc;

                        x2 =  rlower*((float) Math.cos(theta*(i+1)));
                        qverts[vertCount] = x2 + xpos;
                        vertCount++;

                        z2 =  rlower*((float) Math.sin(theta*(i+1)));
                        qverts[vertCount] = -z2 + zpos;
                        vertCount++;

                        y2 = r*((float) Math.sin(theta*(k))) + tempy;// + zadder;
                        qverts[vertCount] = y2 + ypos;
                        vertCount++;

                        x3 =  rupper*((float) Math.cos(theta*(i+1)));
                        qverts[vertCount] = x3 + xpos;
                        vertCount++;

                        z3 =  rupper*((float) Math.sin(theta*(i+1)));
                        qverts[vertCount] = -z3 + zpos;
                        vertCount++;

                        y3 =  r*((float) Math.sin(theta*(k+1))) + tempy;// + zadder;
                        qverts[vertCount] = y3 + ypos;
                        vertCount++;

                        tempy = tempy - tempinc;

                        x4 =  rupper*((float) Math.cos(theta*i));
                        qverts[vertCount] = x4 + xpos;
                        vertCount++;

                        z4 =  rupper*((float) Math.sin(theta*i));
                        qverts[vertCount] = -z4 + zpos;
                        vertCount++;

                        y4 =  r*((float) Math.sin(theta*(k+1))) + tempy;// + zadder;
                        qverts[vertCount] = y4 + ypos;
                        vertCount++;

                        tempy = tempy + tempinc;
                    }
                    rlower = rupper;
                    upcount++;
                    rupper = or - (r - (r*((float) Math.cos(theta*upcount))));
                }
                spiralVertCount = vertCount;
            }
            else
            {
                zadder = zadder + spacing;
                for (int k=0; k < spiralVertCount; k = k+3)
                {
                    qverts[vertCount] = qverts[k];
                    vertCount++;
                    qverts[vertCount] = qverts[k+1];
                    vertCount++;
                    qverts[vertCount] = qverts[k+2] + zadder;
                    vertCount++;
                }
            }
        }

        QuadArray springGeometry = new QuadArray( vertCount/3,
            QuadArray.COORDINATES | QuadArray.NORMALS);

        springGeometry.setCapability( QuadArray.ALLOW_COLOR_WRITE );
        springGeometry.setCapability( QuadArray.ALLOW_COORDINATE_WRITE );
        springGeometry.setCoordinates( 0, qverts );

        // Calculate normals of all points.
        normals = new Vector3f[vertCount/3];
        for (int w = 0; w < vertCount; w = w + 3)
        {
            Vector3f norm = new Vector3f(0.0f, 0.0f, 0.0f);
            mag = qverts[w] * qverts[w] + qverts[w+1] *
                qverts[w+1] + qverts[w+2] * qverts[w+2];
            if (mag != 0.0)
            {
                mag = 1.0f / ((float) Math.sqrt(mag));
                xn = qverts[w]*mag;
                yn = qverts[w+1]*mag;
                zn = qverts[w+2]*mag;
                norm = new Vector3f(xn, yn, zn);
            }
            normals[normalcount] = norm;
            springGeometry.setNormal(normalcount, norm);
            normalcount++;
        }

        sspring = new Shape3D(springGeometry, springAppearance);
    }

    // Scaling works because QuadArray.ALLOW_COORDINATE_WRITE was set in
    // constructor.
    public void Scale(float xs, float ys, float zs)
    {
        QuadArray qa = (QuadArray) sspring.getGeometry();
        for (int i=0; i < qa.getVertexCount(); i++)
        {
            float[] q = new float[3];
            qa.getCoordinate(i, q);
            q[0] = xs * q[0];
            q[1] = ys * q[1];
            q[2] = ys * q[2];

            qa.setCoordinate(i, q);
        }
        sspring.setGeometry(qa);
    }

    public Shape3D getChild()
    {
        return sspring;
    }
}