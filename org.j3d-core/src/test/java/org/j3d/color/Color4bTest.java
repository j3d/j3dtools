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
public class Color4bTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Color4b classUnderTest = new Color4b();

        assertEquals(classUnderTest.r, 0, "Non-zero default red component");
        assertEquals(classUnderTest.g, 0, "Non-zero default green component");
        assertEquals(classUnderTest.b, 0, "Non-zero default blue component");
        assertEquals(classUnderTest.a, 0, "Non-zero default alpha component");
    }

    @Test(groups = "unit")
    public void testSet() throws Exception
    {
        final byte TEST_RED = (byte)45;
        final byte TEST_GREEN = (byte)146;
        final byte TEST_BLUE = (byte)134;
        final byte TEST_ALPHA = (byte)250;

        Color4b classUnderTest = new Color4b();
        classUnderTest.set(TEST_RED, TEST_GREEN, TEST_BLUE, TEST_ALPHA);

        assertEquals(classUnderTest.r, TEST_RED, "Red component incorrectly set");
        assertEquals(classUnderTest.g, TEST_GREEN, "Green component incorrectly set");
        assertEquals(classUnderTest.b, TEST_BLUE, "Blue component incorrectly set");
        assertEquals(classUnderTest.a, TEST_ALPHA, "Alpha component incorrectly set");
    }

    @Test(groups = "unit")
    public void testNotEqualsToOther() throws Exception
    {
        Color4b classUnderTest = new Color4b();
        assertFalse(classUnderTest.equals(new Date()), "Should not be equal to a date");
    }

    @Test(groups = "unit", dataProvider = "equals")
    public void testEquals(Byte[] src, Byte[] dest, boolean expectedResult) throws Exception
    {
        Color4b testClass = new Color4b();
        testClass.r = src[0];
        testClass.g = src[1];
        testClass.b = src[2];
        testClass.a = src[3];

        Color4b classUnderTest = new Color4b();
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
        final byte TEST_RED = (byte)86;
        final byte TEST_GREEN = (byte)200;
        final byte TEST_BLUE = (byte)5;
        final byte TEST_ALPHA = (byte)255;

        Color4b testClass = new Color4b();
        testClass.r = TEST_RED;
        testClass.g = TEST_GREEN;
        testClass.b = TEST_BLUE;
        testClass.a = TEST_ALPHA;

        Color4b classUnderTest = new Color4b();
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

        retval[0][0] = new Byte[] {0, 0, 0, (byte)255};
        retval[0][1] = new Byte[] {0, 0, 0, (byte)255};
        retval[0][2] = true;

        retval[1][0] = new Byte[] {0, 0, 0, (byte)255};
        retval[1][1] = new Byte[] {(byte)15, 0, 0, (byte)255};
        retval[1][2] = false;

        retval[2][0] = new Byte[] {(byte)86, 0, 0, (byte)255};
        retval[2][1] = new Byte[] {(byte)86, 0, 0, (byte)255};
        retval[2][2] = true;

        retval[3][0] = new Byte[] {0, (byte)134, 0, (byte)255};
        retval[3][1] = new Byte[] {0, (byte)134, 0, (byte)255};
        retval[3][2] = true;

        retval[4][0] = new Byte[] {0, 0, (byte)255, (byte)255};
        retval[4][1] = new Byte[] {0, 0, (byte)255, (byte)255};
        retval[4][2] = true;

        // NaN need to be equal?

        return retval;
    }
}
