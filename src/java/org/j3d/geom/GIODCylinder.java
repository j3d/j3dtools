/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
//import java.lang.Math.*;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;
import javax.media.j3d.QuadArray;

import javax.vecmath.Vector3f;

// Application specific imports

/**
 * A simple cylinder without end caps.
 * <p>
 *
 * @author Robin Smith robinsm@sunyit.edu
 * @version $Revision: 1.1 $
 */
public class GIODCylinder
{
    private Shape3D scylinder;
    private float[] qverts;
    private float[] qtex;
    private float x, y, z, theta, t, num,
        numcirc, calct, xn, yn, zn, mag;
    private int vertCount = 0;
    private int normalcount = 0;
    private Vector3f[] normals;

    /**
    * Constructs a Cylinder with 'n' quads (faces), 'w' width (diameter),
    * 'h' height, centered at 'xpos', 'ypos', 'zpos',
    * and appearance 'cylinderAppearance'
    **/
    public  GIODCylinder(int n, float w, float h, float xpos,
        float ypos, float zpos, Appearance cylinderAppearance)
    {
        qverts = new float[12*(n+1)];
        qtex = new float[12*(n+1)];

        t = ((float) (2*Math.PI*(w/2))/n);
        theta = t/(w/2);

        numcirc = n;

        // Generate the walls only.. this is a cylinder without "ends"
        for (int i=0; i <= numcirc; i++)
        {
            //bottom points
            x = (float) ((w/2)*Math.cos(theta*i));
            z = (float) ((w/2)*Math.sin(theta*i));
            qverts[vertCount]  = x;
            vertCount++;
            qverts[vertCount]  = 0-(h/2);
            vertCount++;
            qverts[vertCount]  = z;
            vertCount++;

            i++;

            x = (float) ((w/2)*Math.cos(theta*i));
            z = (float) ((w/2)*Math.sin(theta*i));
            qverts[vertCount]  = x;
            vertCount++;
            qverts[vertCount]  = 0-(h/2);
            vertCount++;
            qverts[vertCount]  = z;
            vertCount++;

            //top points
            x = (float) ((w/2)*Math.cos(theta*i));
            z = (float) ((w/2)*Math.sin(theta*i));
            qverts[vertCount]  = x;
            vertCount++;
            qverts[vertCount]  = 0+(h/2);
            vertCount++;
            qverts[vertCount]  = z;
            vertCount++;

            i--;

            x = (float) ((w/2)*Math.cos(theta*i));
            z = (float) ((w/2)*Math.sin(theta*i));
            qverts[vertCount]  = x;
            vertCount++;
            qverts[vertCount]  = 0+(h/2);
            vertCount++;
            qverts[vertCount]  = z;
            vertCount++;
        }

        //position the cylinder
        for (int j=0; j < vertCount; j = j+3)
        {
            qverts[j] = qverts[j] + xpos;
            qverts[j+1] = qverts[j+1] + ypos;
            qverts[j+2] = qverts[j+2] + zpos;
        }

        QuadArray cylinderGeometry = new QuadArray( vertCount/3,
                        QuadArray.COORDINATES | QuadArray.NORMALS);

        cylinderGeometry.setCapability( QuadArray.ALLOW_COORDINATE_WRITE );
        cylinderGeometry.setCoordinates( 0, qverts );

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
            cylinderGeometry.setNormal(normalcount, norm);
            normalcount++;
        }

        scylinder = new Shape3D(cylinderGeometry, cylinderAppearance);
    }

    public Shape3D getChild()
    {
        return scylinder;
    }
}
