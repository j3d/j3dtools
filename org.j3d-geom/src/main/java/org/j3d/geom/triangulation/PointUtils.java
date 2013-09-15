/*
 * $RCSfile: Basic.java,v $
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

/** Cleans up the basic operations on points like they were vectors. */
class PointUtils
{

    static double det2D(Point2f u, Point2f v, Point2f w)
    {
        return (((u).x - (v).x) * ((v).y - (w).y) + ((v).y - (u).y) * ((v).x - (w).x));
    }

    static double dotProduct2D(Point2f u, Point2f v)
    {
        return (((u).x * (v).x) + ((u).y * (v).y));
    }

    static void vectorProduct(Point3f p, Point3f q, Point3f r)
    {
        (r).x = (p).y * (q).z - (q).y * (p).z;
        (r).y = (q).x * (p).z - (p).x * (q).z;
        (r).z = (p).x * (q).y - (q).x * (p).y;
    }

    static void vectorSub(Point3f p, Point3f q, Point3f r)
    {
        (r).x = (p).x - (q).x;
        (r).y = (p).y - (q).y;
        (r).z = (p).z - (q).z;
    }

    static void vectorSub2D(Point2f p, Point2f q, Point2f r)
    {
        (r).x = (p).x - (q).x;
        (r).y = (p).y - (q).y;
    }

    static int signEps(double x, double eps)
    {
        return ((x <= eps) ? ((x < -eps) ? -1 : 0) : 1);
    }
}
