/*
 * $RCSfile: Project.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision: 1.1 $
 * $Date: 2009-09-14 14:49:41 $
 * $State: Exp $
 */

// ----------------------------------------------------------------------
//
// The reference to Fast Industrial Strength Triangulation (FIST) code
// in this release by Sun Microsystems is related to Sun's rewrite of
// an early version of FIST. FIST was originally created by Martin
// Held and Joseph Mitchell at Stony Brook University and is
// incorporated by Sun under an agreement with The Research Foundation
// of SUNY (RFSUNY). The current version of FIST is available for
// commercial use under a license agreement with RFSUNY on behalf of
// the authors and Stony Brook University.  Please contact the Office
// of Technology Licensing at Stony Brook, phone 631-632-9009, for
// licensing information.
//
// ----------------------------------------------------------------------

package org.j3d.geom.triangulation;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;

class Project
{

    /**
     * This function projects the vertices of the polygons referenced by
     * loops[i1,..,i2-1] to an approximating plane.
     */
    static void projectFace(Triangulator triRef, int loopMin, int loopMax)
    {
        Vector3f normal, nr;
        int i, j;
        double d;

        normal = new Vector3f();
        nr = new Vector3f();

        // determine the normal of the plane onto which the points get projected
        determineNormal(triRef, triRef.loops[loopMin], normal);
        j = loopMin + 1;
        if(j < loopMax)
        {
            for(i = j; i < loopMax; ++i)
            {
                determineNormal(triRef, triRef.loops[i], nr);
                if(normal.dot(nr) < 0.0)
                {
                    nr.negate();
                }
                normal.add(normal, nr);
            }
            normal.normalise();
        }

        // project the points onto this plane. the projected points are stored in
        // the array `points[0,..,numPoints]'

        // System.out.println("loopMin " + loopMin + " loopMax " + loopMax);
        projectPoints(triRef, loopMin, loopMax, normal);

    }


    /**
     * This function computes the average of all normals defined by triples of
     * successive vertices of the polygon. we'll see whether this is a good
     * heuristic for finding a suitable plane normal...
     */
    static void determineNormal(Triangulator triRef, int ind, Vector3f normal)
    {
        Vector3f nr, pq, pr;
        int ind0, ind1, ind2;
        int i0, i1, i2;
        double d;

        ind1 = ind;
        i1 = triRef.fetchData(ind1);
        ind0 = triRef.fetchPrevData(ind1);
        i0 = triRef.fetchData(ind0);
        ind2 = triRef.fetchNextData(ind1);
        i2 = triRef.fetchData(ind2);
        pq = new Vector3f();
        pq.sub(triRef.vertices[i0], triRef.vertices[i1]);
        pr = new Vector3f();
        pr.sub(triRef.vertices[i2], triRef.vertices[i1]);
        nr = new Vector3f();
        nr.cross(pq, pr);
        nr.normalise();

        pq.set(pr);
        ind1 = ind2;
        ind2 = triRef.fetchNextData(ind1);
        i2 = triRef.fetchData(ind2);
        while(ind1 != ind)
        {
            pr.sub(triRef.vertices[i2], triRef.vertices[i1]);
            nr.cross(pq, pr);

            nr.normalise();
            if(normal.dot(nr) < 0.0)
            {
                nr.negate();
            }
            normal.add(nr, normal);

            pq.set(pr);
            ind1 = ind2;
            ind2 = triRef.fetchNextData(ind1);
            i2 = triRef.fetchData(ind2);
        }

        normal.normalise();
    }


    /**
     * This function maps the vertices of the polygon referenced by `ind' to the
     * plane  n3.x * x + n3.y * y + n3.z * z = 0. every mapped vertex  (x,y,z)
     * is then expressed in terms of  (x',y',z'),  where  z'=0.  this is
     * achieved by transforming the original vertices into a coordinate system
     * whose z-axis coincides with  n3,  and whose two other coordinate axes  n1
     * and  n2  are orthonormal on  n3. note that n3 is supposed to be of unit
     * length!
     */
    static void projectPoints(Triangulator triRef, int i1, int i2, Vector3f n3)
    {
        Matrix4d matrix = new Matrix4d();
        Point3d vtx = new Point3d();
        Vector3f n1, n2;
        double d;
        int ind, ind1;
        int i, j1;


        n1 = new Vector3f();
        n2 = new Vector3f();

        // choose  n1  and  n2  appropriately
        if((Math.abs(n3.x) > 0.1) || (Math.abs(n3.y) > 0.1))
        {
            n1.x = -n3.y;
            n1.y = n3.x;
            n1.z = 0.0f;
        }
        else
        {
            n1.x = n3.z;
            n1.z = -n3.x;
            n1.y = 0.0f;
        }
        n1.normalise();
        n2.cross(n1, n3);
        n2.normalise();

        // initialize the transformation matrix
        matrix.m00 = n1.x;
        matrix.m01 = n1.y;
        matrix.m02 = n1.z;
        matrix.m03 = 0.0f;       // translation of the coordinate system
        matrix.m10 = n2.x;
        matrix.m11 = n2.y;
        matrix.m12 = n2.z;
        matrix.m13 = 0.0f;       // translation of the coordinate system
        matrix.m20 = n3.x;
        matrix.m21 = n3.y;
        matrix.m22 = n3.z;
        matrix.m23 = 0.0f;       // translation of the coordinate system
        matrix.m30 = 0.0f;
        matrix.m31 = 0.0f;
        matrix.m32 = 0.0f;
        matrix.m33 = 1.0f;

        // transform the vertices and store the transformed vertices in the array
        // `points'
        triRef.initPnts(20);
        for(i = i1; i < i2; ++i)
        {
            ind = triRef.loops[i];
            ind1 = ind;
            j1 = triRef.fetchData(ind1);

            vtx.x = triRef.vertices[j1].x;
            vtx.y = triRef.vertices[j1].y;
            vtx.z = triRef.vertices[j1].z;

            matrix.transform(vtx, vtx);

            j1 = triRef.storePoint((float)vtx.x, (float)vtx.y);
            triRef.updateIndex(ind1, j1);
            ind1 = triRef.fetchNextData(ind1);
            j1 = triRef.fetchData(ind1);
            while(ind1 != ind)
            {
                vtx.x = triRef.vertices[j1].x;
                vtx.y = triRef.vertices[j1].y;
                vtx.z = triRef.vertices[j1].z;

                matrix.transform(vtx, vtx);

                j1 = triRef.storePoint((float)vtx.x, (float)vtx.y);
                triRef.updateIndex(ind1, j1);
                ind1 = triRef.fetchNextData(ind1);
                j1 = triRef.fetchData(ind1);
            }
        }
    }

}
