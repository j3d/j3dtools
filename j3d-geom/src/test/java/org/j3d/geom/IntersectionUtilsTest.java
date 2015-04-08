/*
 * j3d.org Copyright (c) 2001-2015
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.geom;

import org.j3d.maths.vector.Point3d;
import org.j3d.maths.vector.Vector3d;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class IntersectionUtilsTest
{
    @Test(groups = "unit", dataProvider = "sphere intersection")
    public void testRaySphereArray(float[] rayOrigin,
                                   float[] rayDirection,
                                   float[] sphereCenter,
                                   float sphereRadius,
                                   float[] expectedIntersection) throws Exception {

        float[] result = new float[3];

        IntersectionUtils classUnderTest = new IntersectionUtils();

        // if expectedIntersection is null, it means we expect there is no intersection between
        // the ray and the sphere, and thus the method should return false.
        if(expectedIntersection != null)
        {
            assertTrue(classUnderTest.raySphere(rayOrigin, rayDirection, sphereCenter, sphereRadius, result),
                       "Did not find any intersection");

            assertEquals(result[0], expectedIntersection[0], 0.001f, "X coordinate of intersection wrong");
            assertEquals(result[1], expectedIntersection[1], 0.001f, "Y coordinate of intersection wrong");
            assertEquals(result[2], expectedIntersection[2], 0.001f, "Z coordinate of intersection wrong");
        }
        else
        {
            assertFalse(classUnderTest.raySphere(rayOrigin, rayDirection, sphereCenter, sphereRadius, result),
                        "Found an unexpected intersection at " + result[0] + "," + result[1] + "," + result[2]);
        }
    }

    @Test(groups = "unit", dataProvider = "sphere intersection")
    public void testRaySphereVector(float[] rayOrigin,
                                    float[] rayDirection,
                                    float[] sphereCenter,
                                    float sphereRadius,
                                    float[] expectedIntersection) throws Exception {

        Point3d testOrigin = new Point3d();
        testOrigin.set(rayOrigin[0], rayOrigin[1], rayOrigin[2]);

        Vector3d testDirection = new Vector3d();
        testDirection.set(rayDirection[0], rayDirection[1], rayDirection[2]);


        Point3d result = new Point3d();

        IntersectionUtils classUnderTest = new IntersectionUtils();

        // if expectedIntersection is null, it means we expect there is no intersection between
        // the ray and the sphere, and thus the method should return false.
        if(expectedIntersection != null)
        {
            assertTrue(classUnderTest.raySphere(testOrigin, testDirection, sphereCenter, sphereRadius, result),
                       "Did not find any intersection");

            assertEquals(result.x, expectedIntersection[0], 0.001f, "X coordinate of intersection wrong");
            assertEquals(result.y, expectedIntersection[1], 0.001f, "Y coordinate of intersection wrong");
            assertEquals(result.z, expectedIntersection[2], 0.001f, "Z coordinate of intersection wrong");
        }
        else
        {
            assertFalse(classUnderTest.raySphere(testOrigin, testDirection, sphereCenter, sphereRadius, result),
                        "Found an unexpected intersection at " + result.x + "," + result.y + "," + result.z);
        }
    }

    @Test(groups = "unit", dataProvider = "plane intersection")
    public void testRayPlaneArray(float[] rayOrigin,
                                  float[] rayDirection,
                                  float[] planeEqCoeff,
                                  float[] expectedIntersection) throws Exception {

        float[] result = new float[3];

        IntersectionUtils classUnderTest = new IntersectionUtils();

        // if expectedIntersection is null, it means we expect there is no intersection between
        // the ray and the sphere, and thus the method should return false.
        if(expectedIntersection != null)
        {
            assertTrue(classUnderTest.rayPlane(rayOrigin, rayDirection, planeEqCoeff, result),
                       "Did not find any intersection");

            assertEquals(result[0], expectedIntersection[0], 0.001f, "X coordinate of intersection wrong");
            assertEquals(result[1], expectedIntersection[1], 0.001f, "Y coordinate of intersection wrong");
            assertEquals(result[2], expectedIntersection[2], 0.001f, "Z coordinate of intersection wrong");
        }
        else
        {
            assertFalse(classUnderTest.rayPlane(rayOrigin, rayDirection, planeEqCoeff, result),
                        "Found an unexpected intersection at " + result[0] + "," + result[1] + "," + result[2]);
        }
    }

    @DataProvider(name = "sphere intersection")
    public Object[][] generateSphereIntersectionData() {
        Object[][] retval = new Object[3][5];

        // Basic intersection right through the center
        retval[0][0] = new float[] { 2.0f, 0, 0 };
        retval[0][1] = new float[] { -1.0f, 0, 0 };
        retval[0][2] = new float[] { 0, 0, 0 };
        retval[0][3] = 1.0f;
        retval[0][4] = new float[] { 1.0f, 0, 0 };

        // Ray points away from the sphere, no intersection
        retval[1][0] = new float[] { 2.0f, 0, 0 };
        retval[1][1] = new float[] { 1.0f, 0, 0 };
        retval[1][2] = new float[] { 0, 0, 0 };
        retval[1][3] = 1.0f;
        retval[1][4] = null;

        // Intersection grazing the radius
        retval[2][0] = new float[] { 2.0f, 1.0f, 0 };
        retval[2][1] = new float[] { -1.0f, 0, 0 };
        retval[2][2] = new float[] { 0, 0, 0 };
        retval[2][3] = 1.0f;
        retval[2][4] = new float[] { 0, 1.0f, 0 };

        return retval;
    }

    @DataProvider(name = "plane intersection")
    public Object[][] generatePlaneIntersectionData() {
        Object[][] retval = new Object[3][4];

        // Basic intersection plane perpendicular to ray, with the plane at the origin
        retval[0][0] = new float[] { 2, 0, 0 };
        retval[0][1] = new float[] { -1, 0, 0 };
        retval[0][2] = new float[] { 1, 0, 0, 0 };
        retval[0][3] = new float[] { 0, 0, 0};

        // Ray parallel to the plane, no intersection
        retval[1][0] = new float[] { 2, 0, 0 };
        retval[1][1] = new float[] { 1, 0, 0 };
        retval[1][2] = new float[] { 0, 1, 0, 0  };
        retval[1][3] = null;

        // Ray points away from the plane, no intersection
        retval[2][0] = new float[] { 2, 0, 0 };
        retval[2][1] = new float[] { 1, 0, 0 };
        retval[2][2] = new float[] { 1, 0, 0, 0 };
        retval[2][3] = null;

        return retval;
    }
}
