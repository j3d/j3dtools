/*
 * j3d.org Copyright (c) 2001-2013
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.color;

import java.util.Date;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Unit tests for the floating point colour representation
 *
 * @author justin
 */
public class Color4fTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Color4f classUnderTest = new Color4f();

        assertEquals(classUnderTest.r, 0.0f, "Non-zero default red component");
        assertEquals(classUnderTest.g, 0.0f, "Non-zero default green component");
        assertEquals(classUnderTest.b, 0.0f, "Non-zero default blue component");
        assertEquals(classUnderTest.a, 0.0f, "Non-zero default alpha component");
    }

    @Test(groups = "unit")
    public void testSet() throws Exception
    {
        final float TEST_RED = 0.4f;
        final float TEST_GREEN = 1.0f;
        final float TEST_BLUE = 0.4f;
        final float TEST_ALPHA = 0.6f;

        Color4f classUnderTest = new Color4f();
        classUnderTest.set(TEST_RED, TEST_GREEN, TEST_BLUE, TEST_ALPHA);

        assertEquals(classUnderTest.r, TEST_RED, 0.001, "Red component incorrectly set");
        assertEquals(classUnderTest.g, TEST_GREEN, 0.001, "Green component incorrectly set");
        assertEquals(classUnderTest.b, TEST_BLUE, 0.001, "Blue component incorrectly set");
        assertEquals(classUnderTest.a, TEST_ALPHA, 0.001, "Alpha component incorrectly set");
    }

    @Test(groups = "unit")
    public void testNotEqualsToOther() throws Exception
    {
        Color4f classUnderTest = new Color4f();
        assertFalse(classUnderTest.equals(new Date()), "Should not be equal to a date");
    }

    @Test(groups = "unit", dataProvider = "equals")
    public void testEquals(Float[] src, Float[] dest, boolean expectedResult) throws Exception
    {
        Color4f testClass = new Color4f();
        testClass.r = src[0];
        testClass.g = src[1];
        testClass.b = src[2];
        testClass.a = src[3];

        Color4f classUnderTest = new Color4f();
        classUnderTest.r = dest[0];
        classUnderTest.g = dest[1];
        classUnderTest.b = dest[2];
        classUnderTest.a = dest[3];

        assertEquals(classUnderTest.equals(testClass), expectedResult, "Incorrect equals comparison");
        assertEquals(testClass.equals(classUnderTest), expectedResult, "Incorrect reverse equals comparison");
    }

    @Test(groups = "unit")
    public void testHashCode() throws Exception
    {
        final float TEST_RED = 0.4f;
        final float TEST_GREEN = 0.6f;
        final float TEST_BLUE = 0.25f;
        final float TEST_ALPHA = 1.0f;

        Color4f testClass = new Color4f();
        testClass.r = TEST_RED;
        testClass.g = TEST_GREEN;
        testClass.b = TEST_BLUE;
        testClass.a = TEST_ALPHA;

        Color4f classUnderTest = new Color4f();
        classUnderTest.r = TEST_RED;
        classUnderTest.g = TEST_GREEN;
        classUnderTest.b = TEST_BLUE;
        classUnderTest.a = TEST_ALPHA;

        assertEquals(classUnderTest.hashCode(), testClass.hashCode(), "Same variables didn't generate same hash");
    }

    @DataProvider(name = "equals")
    public Object[][] generateEqualsData()
    {
        Object[][] retval = new Object[5][3];

        retval[0][0] = new Float[] {0.0f, 0.0f, 0.0f, 1.0f};
        retval[0][1] = new Float[] {0.0f, 0.0f, 0.0f, 1.0f};
        retval[0][2] = true;

        retval[1][0] = new Float[] {0.0f, 0.0f, 0.0f, 1.0f};
        retval[1][1] = new Float[] {0.1f, 0.0f, 0.0f, 1.0f};
        retval[1][2] = false;

        retval[2][0] = new Float[] {0.3f, 0.0f, 0.0f, 1.0f};
        retval[2][1] = new Float[] {0.3f, 0.0f, 0.0f, 1.0f};
        retval[2][2] = true;

        retval[3][0] = new Float[] {0.0f, 0.5f, 0.0f, 1.0f};
        retval[3][1] = new Float[] {0.0f, 0.5f, 0.0f, 1.0f};
        retval[3][2] = true;

        retval[4][0] = new Float[] {0.0f, 0.0f, 1.0f, 1.0f};
        retval[4][1] = new Float[] {0.0f, 0.0f, 1.0f, 1.0f};
        retval[4][2] = true;

        // NaN need to be equal?

        return retval;
    }
}
