/*****************************************************************************
 *      J3D.org Copyright (c) 2000
 *         Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;

import javax.vecmath.Vector3f;

// Application specific imports

/**
 * A cone with 2n triangles - n for the wall and n for the base
 * <p>
 *
 * @author Unknown
 * @version $Revision: 1.1 $
 */
public class Cone
{
    private static final float WIDTH = 1.0f;
    private static final float HEIGHT = 2.0f;
    private static final int TRIANGLES = 50;
    private static final float XPOSITION = 0.0f;
    private static final float YPOSITION = 0.0f;
    private static final float ZPOSITION = 0.0f;

    private Shape3D scone;
    private float[] qverts;
    private float[] qtex;
    private float x, y, z, theta, t, previousx, previousy, previousz, num,
        numcirc, calct, xn, yn, zn, mag;
    private int vertCount = 0;
    private int normalcount = 0;
    private Vector3f[] normals;

    /**
    *     Constructs a Cone with 50 triangles, width of 1.0,
    *     height of 2.0, with base centered at 0,0,0
    *     and null appearance
    **/
    public  Cone()
    {
        this(TRIANGLES, WIDTH, HEIGHT,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Cone with 50 triangles, width of 1.0,
    *     height of 2.0, with base centered at 0,0,0
    *     and appearance 'coneAppearance'
    **/
    public  Cone(Appearance coneAppearance)
    {
        this(TRIANGLES, WIDTH, HEIGHT,
            XPOSITION, YPOSITION, ZPOSITION, coneAppearance);
    }

    /**
    *     Constructs a Cone with 'n' triangles, width of 1.0,
    *     height of 2.0, with base centered at 0,0,0
    *     and null appearance
    **/
    public  Cone(int n)
    {
        this(n, WIDTH, HEIGHT,
            XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Cone with 'n' triangles, width of 1.0,
    *     height of 2.0, with base centered at 0,0,0
    *     and appearance 'coneAppearance'
    **/
    public  Cone(int n, Appearance coneAppearance)
    {
        this(n, WIDTH, HEIGHT,
            XPOSITION, YPOSITION, ZPOSITION, coneAppearance);
    }

    /**
    *     Constructs a Cone with 'n' triangles, 'w' width,
    *     'h' height, with base centered at 0,0,0
    *     and null appearance
    **/
    public  Cone(float w, float h)
    {
        this(TRIANGLES, w, h, XPOSITION, YPOSITION, ZPOSITION, null);
    }

    /**
    *     Constructs a Cone with 'n' triangles, 'w' width,
    *     'h' height, with base centered at 0,0,0
    *     and null appearance
    **/
    public  Cone(float w, float h, Appearance coneAppearance)
    {
        this(TRIANGLES, w, h, XPOSITION, YPOSITION, ZPOSITION, coneAppearance);
    }

    /**
    *     Constructs a Cone with 50 triangles, width of 1.0,
    *     height of 2.0, with base centered at 'xpos', 'ypos', 'zpos',
    *     and null appearance
    **/
    public  Cone(float xpos, float ypos, float zpos)
    {
        this(TRIANGLES, WIDTH, HEIGHT, xpos, ypos, zpos, null);
    }

    /**
    *     Constructs a Cone with 50 triangles, width of 1.0,
    *     height of 2.0, with base centered at 'xpos', 'ypos', 'zpos',
    *     and appearance 'coneAppearance'
    **/
    public  Cone(float xpos, float ypos, float zpos,
        Appearance coneAppearance)
    {
        this(TRIANGLES, WIDTH, HEIGHT, xpos, ypos, zpos, coneAppearance);
    }

    /**
    *     Constructs a Cone with 'n' triangles, width of 1.0,
    *     height of 2.0, with base centered at 'xpos', 'ypos', 'zpos',
    *     and null appearance
    **/
    public  Cone(int n, float xpos, float ypos, float zpos)
    {
        this(n, WIDTH, HEIGHT, xpos, ypos, zpos, null);
    }

    /**
    *     Constructs a Cone with 'n' triangles, width of 1.0,
    *     height of 2.0, with base centered at 'xpos', 'ypos', 'zpos',
    *     and appearance 'coneAppearance'
    **/
    public  Cone(int n, float xpos, float ypos, float zpos,
        Appearance coneAppearance)
    {
        this(n, WIDTH, HEIGHT, xpos, ypos, zpos, coneAppearance);
    }

    /**
    *     Constructs a Cone with 'n' triangles, 'w' width,
    *     'h' height, with base centered at 'xpos', 'ypos', 'zpos',
    *     and null appearance
    **/
    public  Cone(int n, float w, float h, float xpos,
        float ypos, float zpos)
    {
        this(n, w, h, xpos, ypos, zpos, null);
    }

    /**
    *     Constructs a Cone with 'n' triangles, 'w' width,
    *     'h' height, with base centered at 'xpos', 'ypos', 'zpos',
    *     and appearance 'coneAppearance'
    **/
    public  Cone(int n, float w, float h, float xpos,
        float ypos, float zpos, Appearance coneAppearance)
    {
        qverts = new float[2*9*n];
        qtex = new float[2*9*n];

        t = ((float) (2*Math.PI*w)/n);
        numcirc = n;

        // Change the arclength to the closest value that fits.
        calct = ((float) (2*Math.PI*w)/numcirc);
        t = calct;
        theta = t/w;

        // Generate the walls
        for (int i=0; i <= numcirc; i++)
        {
            qverts[vertCount]  = xpos;
            vertCount++;
            qverts[vertCount]  = h + ypos;
            vertCount++;
            qverts[vertCount]  = zpos;
            vertCount++;

            if (i == 0)
            {
                x = (float) (w*Math.cos(t*i));
                previousx = x;
                z = (float) (w*Math.sin(t*i));
                previousz = z;
                previousy = ypos;
                qverts[vertCount]  = x + xpos;
                vertCount++;
                qverts[vertCount]  = ypos;
                vertCount++;
                qverts[vertCount]  = z + zpos;
                vertCount++;
                i++;
            }
            else
            {
                qverts[vertCount]  = previousx;
                vertCount++;
                qverts[vertCount]  = previousy;
                vertCount++;
                qverts[vertCount]  = previousz;
                vertCount++;
            }

            x = (float) (w*Math.cos(t*i));
            previousx = x;
            z = (float) (w*Math.sin(t*i));
            previousz = z;
            previousy = ypos;
            qverts[vertCount]  = x + xpos;
            vertCount++;
            qverts[vertCount]  = ypos;
            vertCount++;
            qverts[vertCount]  = z + zpos;
            vertCount++;
        }

        // Generate the base
        for (int i=0; i <= numcirc; i++)
        {
            qverts[vertCount]  = xpos;
            vertCount++;
            qverts[vertCount]  = ypos;
            vertCount++;
            qverts[vertCount]  = zpos;
            vertCount++;

            if (i == 0)
            {
                x = (float) (w*Math.cos(t*i));
                previousx = x;
                z = (float) (w*Math.sin(t*i));
                previousz = z;
                previousy = ypos;
                qverts[vertCount]  = x + xpos;
                vertCount++;
                qverts[vertCount]  = ypos;
                vertCount++;
                qverts[vertCount]  = z + zpos;
                vertCount++;
                i++;
            }
            else
            {
                qverts[vertCount]  = previousx;
                vertCount++;
                qverts[vertCount]  = previousy;
                vertCount++;
                qverts[vertCount]  = previousz;
                vertCount++;
            }

            x = (float) (w*Math.cos(t*i));
            previousx = x;
            z = (float) (w*Math.sin(t*i));
            previousz = z;
            previousy = ypos;
            qverts[vertCount]  = x + xpos;
            vertCount++;
            qverts[vertCount]  = ypos;
            vertCount++;
            qverts[vertCount]  = z + zpos;
            vertCount++;
        }

        TriangleArray coneGeometry = new TriangleArray( vertCount/3,
            TriangleArray.COORDINATES | TriangleArray.NORMALS);

        coneGeometry.setCapability( TriangleArray.ALLOW_COORDINATE_WRITE );
        coneGeometry.setCoordinates( 0, qverts );

        // Calculate normals of all points.
        normals = new Vector3f[vertCount/3];
        for (int s = 0; s < vertCount; s = s + 3)
        {
            Vector3f norm = new Vector3f(0.0f, 0.0f, 0.0f);
            mag = qverts[s] * qverts[s] + qverts[s+1] *
                qverts[s+1] + qverts[s+2] * qverts[s+2];
            if (mag != 0.0)
            {
                mag = 1.0f / ((float) Math.sqrt(mag));
                xn = qverts[s]*mag;
                yn = qverts[s+1]*mag;
                zn = qverts[s+2]*mag;
                norm = new Vector3f(xn, yn, zn);
            }
            normals[normalcount] = norm;
            coneGeometry.setNormal(normalcount, norm);
            normalcount++;
        }

        scone = new Shape3D(coneGeometry, coneAppearance);
    }

    public Shape3D getChild()
    {
        return scone;
    }
}