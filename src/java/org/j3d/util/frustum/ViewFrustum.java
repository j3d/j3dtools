/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * Copyright (c) 1996-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
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
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */
package org.j3d.util.frustum;

// Standard imports
import javax.vecmath.Point4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Matrix4d;

/**
 * A utility for tracking the ViewFrustum planes and determining if
 * a triangle or point is visible.
 * <p>
 *
 * Because Java3D can have multiple canvases that view a single scenegraph,
 * the view frustum must be a bit more complex than the traditional case. It
 * must take into account that every canvas has its own viewplatform, and they
 * may not be looking at the same thing. That means, for culling, they will
 * need to assemble the union of all the spaces for each canvas.
 * <p>
 *
 * The frustum is for the previous Java3D frame that has just been rendered.
 *
 * @author Paul Byrne, Justin Couch
 * @version $Revision: 1.4 $
 */
public abstract class ViewFrustum
{
    /** The geometry is in the view frustum, either partially or completely */
    public static final int IN = Canvas3DFrustum.IN;

    /** The geometry is outside the view frustum */
    public static final int OUT = Canvas3DFrustum.OUT;

    /** The geometry has been clipped to the view frustum */
    public static final int CLIPPED = Canvas3DFrustum.CLIPPED;

    /** The 8 bounding points of the frustum volume */
    private Point4d[] frustumPoints;

    /** Nested frustum instances for each canvas */
    private Canvas3DFrustum[] frustums;

    /** The number of different canvases provided */
    protected final int numCanvases;

    // Working vars for projection handling */
    private Matrix4d inverseProjection;

    // Temporary data structures
    private Vector3d tVec1;
    private Vector3d tVec2;
    private Vector3d tVec3;
    private Matrix4d tMatrix;

    private Point3d tmpP1;
    private Point3d tmpP2;
    private Point3d tmpP3;

    /**
     * Create a new instance that on a given number of canvases that contribute
     * to the total view frustum.
     *
     * @param numCanvases The number of canvases contributing to this frustum
     */
    public ViewFrustum(int numCanvases)
    {
        this.numCanvases = numCanvases;

        tMatrix = new Matrix4d();
        tVec1 = new Vector3d();
        tVec2 = new Vector3d();
        tVec3 = new Vector3d();

        tmpP1 = new Point3d();
        tmpP2 = new Point3d();
        tmpP3 = new Point3d();

        inverseProjection = new Matrix4d();

        frustumPoints = new Point4d[8];

        frustumPoints[0] = new Point4d();
        frustumPoints[1] = new Point4d();
        frustumPoints[2] = new Point4d();
        frustumPoints[3] = new Point4d();
        frustumPoints[4] = new Point4d();
        frustumPoints[5] = new Point4d();
        frustumPoints[6] = new Point4d();
        frustumPoints[7] = new Point4d();

        frustums = new Canvas3DFrustum[numCanvases];

        for(int i = 0; i < numCanvases; i++)
            frustums[i] = new Canvas3DFrustum();
    }

    /**
     * This method must be called when the view platform transform group
     * has been updated. Until this method is called the frustum will
     * not be valid
     */
    public void viewingPlatformMoved()
    {
        for(int i = 0; i < numCanvases; i++)
            computeFrustumPlanes(i);
    }

    /**
     * Manually re-orient the view frustum by this given matrix. This is used
     * for doing predictive work about where the user will be in the next
     * rendered frame. This assumes that you have called the
     * {@link #viewingPlatformMoved()} method first for this frame.
     *
     * @param tx The transform used to modify the points with
     */
    public void manualPlatformMove(Matrix4d tx)
    {
        tx.transform(frustumPoints[0]);
        tx.transform(frustumPoints[1]);
        tx.transform(frustumPoints[2]);
        tx.transform(frustumPoints[3]);
        tx.transform(frustumPoints[4]);
        tx.transform(frustumPoints[5]);
        tx.transform(frustumPoints[6]);
        tx.transform(frustumPoints[7]);

        for(int i = 0; i < numCanvases; i++)
            updatePlanes(i);
    }

    /**
     * Determines if the triangle defined by the 3 points is visible
     * in the view frustum.
     * <p>
     * IN is returned for any triangle which is partially or completely in
     * the view frustum. OUT is returned if the triangle is outside the
     * frustum.
     * <p>
     * In some cases triangles which are not visible will be reported as IN,
     * however the converse is not true. All triangles which have any part
     * in the frustum will be reported as IN.
     *
     * @param p1X The x component of the first point of the triangle
     * @param p1Y The y component of the first point of the triangle
     * @param p1Z The z component of the first point of the triangle
     * @param p2X The x component of the second point of the triangle
     * @param p2Y The y component of the second point of the triangle
     * @param p2Z The z component of the second point of the triangle
     * @param p3X The x component of the third point of the triangle
     * @param p3Y The y component of the third point of the triangle
     * @param p3Z The z component of the third point of the triangle
     * @return IN, OUT indicating triangle is visible in the View Frustum
     */
    public int isTriangleInFrustum(float p1X, float p1Y, float p1Z,
                                   float p2X, float p2Y, float p2Z,
                                   float p3X, float p3Y, float p3Z)
    {
        tmpP1.set(p1X, p1Y, p1Z);
        tmpP2.set(p2X, p2Y, p2Z);
        tmpP3.set(p3X, p3Y, p3Z);

        return isTriangleInFrustum(tmpP1, tmpP2, tmpP3);
    }

    /**
     * Determines if the triangle defined by the 3 points is visible
     * in the view frustum.
     * <p>
     * IN is returned for any triangle which is partially or completely in
     * the view frustum. OUT is returned if the triangle is outside the
     * frustum.
     * <p>
     * In some cases triangles which are not visible will be reported as IN,
     * however the converse is not true. All triangles which have any part
     * in the frustum will be reported as IN.
     *
     * @param p1 The first point of the triangle
     * @param p2 The first point of the triangle
     * @param p3 The first point of the triangle
     * @return IN, OUT indicating triangle is visible in the View Frustum
     */
    public int isTriangleInFrustum(Point3d p1, Point3d p2, Point3d p3)
    {
        for(int i = 0; i < frustums.length; i++)
        {
            if(frustums[i].isTriangleInFrustum(p1, p2, p3) != OUT)
                return IN;
        }

        return OUT;
    }

    /**
      * Checks if the point is inside the view frustum.
      *
      * @return IN, OUT indicating point is inside or outside the View Frustum
      */
    public int isPointInFrustum(Point3d p1)
    {
        for(int i = 0; i < frustums.length; i++)
        {
            if(frustums[i].isPointInFrustum(p1))
                return IN;
        }

        return OUT;
    }

    /**
     * Convenience method to fetch the axis-aligned bounding box that the
     * view frustum encloses. The two structures passed in are filled with the
     * appropriate information.
     *
     * @param min The min corner vertex (+X, +Y and +Z values)
     * @param max The max corner vertex (+X, +Y and +Z values)
     */
    public void getBounds(Point3d min, Point3d max)
    {
        min.x = frustumPoints[0].x;
        min.y = frustumPoints[0].y;
        min.z = frustumPoints[0].z;

        max.x = frustumPoints[0].x;
        max.y = frustumPoints[0].y;
        max.z = frustumPoints[0].z;

        double x, y, z;

        for(int i = 1; i < 7; i++)
        {
            x = frustumPoints[i].x;
            y = frustumPoints[i].y;
            z = frustumPoints[i].z;

            min.x = (min.x < x) ? min.x : x;
            min.y = (min.y < y) ? min.y : y;
            min.z = (min.z < z) ? min.z : z;

            max.x = (max.x > x) ? max.x : x;
            max.y = (max.y > y) ? max.y : y;
            max.z = (max.z > z) ? max.z : z;
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Request from the renderer-specific canvas the inverse projection
     * matrix for the given canvasId.
     *
     * @param id The ID of the canvas
     * @param matrix The matrix to copy the data into
     */
    protected abstract void getInverseWorldProjection(int id,
                                                      Matrix4d matrix);

    /**
     * Compute the frustum planes for the specified canvas.
     *
     * @param canvasId The array index of the canvas to work on
     */
    private void computeFrustumPlanes(int canvasId)
    {

        frustumPoints[0].set(-1.0, -1.0,  1.0, 1.0);  // lower-left-front
        frustumPoints[1].set(-1.0,  1.0,  1.0, 1.0);  // upper-left-front
        frustumPoints[2].set( 1.0,  1.0,  1.0, 1.0);  // upper-right-front
        frustumPoints[3].set( 1.0, -1.0,  1.0, 1.0);  // lower-right-front
        frustumPoints[4].set(-1.0, -1.0, -1.0, 1.0);  // lower-left-back
        frustumPoints[5].set(-1.0,  1.0, -1.0, 1.0);  // upper-left-back
        frustumPoints[6].set( 1.0,  1.0, -1.0, 1.0);  // upper-right-back
        frustumPoints[7].set( 1.0, -1.0, -1.0, 1.0);  // lower-right-back

        getInverseWorldProjection(canvasId, inverseProjection);

        for (int i = 0; i < frustumPoints.length; i++)
        {
            inverseProjection.transform(frustumPoints[i]);
            double w_inv = 1.0 / frustumPoints[i].w;
            frustumPoints[i].x *= w_inv;
            frustumPoints[i].y *= w_inv;
            frustumPoints[i].z *= w_inv;
        }

        updatePlanes(canvasId);
    }

    /**
     * Update the plane equations for the current points.
     */
    private void updatePlanes(int canvasId)
    {
        // Now compute the 6 plane equations
        // left
        computePlaneEq(frustumPoints[0],
                       frustumPoints[4],
                       frustumPoints[5],
                       frustumPoints[1],
                       frustums[canvasId].frustumPlanes[0]);

        // right
        computePlaneEq(frustumPoints[3],
                       frustumPoints[2],
                       frustumPoints[6],
                       frustumPoints[7],
                       frustums[canvasId].frustumPlanes[1]);

        // top
        computePlaneEq(frustumPoints[1],
                       frustumPoints[5],
                       frustumPoints[6],
                       frustumPoints[2],
                       frustums[canvasId].frustumPlanes[2]);

        // bottom
        computePlaneEq(frustumPoints[0],
                       frustumPoints[3],
                       frustumPoints[7],
                       frustumPoints[4],
                       frustums[canvasId].frustumPlanes[3]);

        // front
        computePlaneEq(frustumPoints[0],
                       frustumPoints[1],
                       frustumPoints[2],
                       frustumPoints[3],
                       frustums[canvasId].frustumPlanes[4]);

        // back
        computePlaneEq(frustumPoints[4],
                       frustumPoints[7],
                       frustumPoints[6],
                       frustumPoints[5],
                       frustums[canvasId].frustumPlanes[5]);
    }

    /**
     * Given the four bounding points, compute the plane equation that defines
     * the plane passing through them. The point should be in order and not
     * present bow-tie shapes.
     *
     * @param p1 The first point of the plane
     * @param p2 The second point of the plane
     * @param p3 The third point of the plane
     * @param p4 The fourth point of the plane
     * @param planeEq The vector to put the plane equation values into
     */
    private void computePlaneEq(Point4d p1,
                                Point4d p2,
                                Point4d p3,
                                Point4d p4,
                                Vector4d planeEq)
    {
        tVec1.x = p3.x - p1.x;
        tVec1.y = p3.y - p1.y;
        tVec1.z = p3.z - p1.z;

        tVec2.x = p2.x - p1.x;
        tVec2.y = p2.y - p1.y;
        tVec2.z = p2.z - p1.z;

        tVec3.cross(tVec2, tVec1);
        tVec3.normalize();
        planeEq.x = tVec3.x;
        planeEq.y = tVec3.y;
        planeEq.z = tVec3.z;
        planeEq.w = -(planeEq.x * p1.x + planeEq.y * p1.y + planeEq.z * p1.z);
    }
}
