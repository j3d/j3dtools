/*
 * j3d.org Copyright (c) 2001-2014
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.io;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the endian convertor
 *
 * @author justin
 */
public class EndianConverterTest
{
    @Test(groups = "unit")
    public void testShortRoundTrip() throws Exception
    {
        final short TEST_VALUE = 13565;

        short intermediate = EndianConverter.convert(TEST_VALUE);

        assertNotEquals(intermediate, TEST_VALUE, "Didn't convert the intermediate form properly");

        short result = EndianConverter.convert(intermediate);

        assertEquals(result, TEST_VALUE, "Round trip value failed");
    }

    @Test(groups = "unit")
    public void testIntRoundTrip() throws Exception
    {
        final int TEST_VALUE = 135625;

        int intermediate = EndianConverter.convert(TEST_VALUE);

        assertNotEquals(intermediate, TEST_VALUE, "Didn't convert the intermediate form properly");

        int result = EndianConverter.convert(intermediate);

        assertEquals(result, TEST_VALUE, "Round trip value failed");
    }

    @Test(groups = "unit")
    public void testLongRoundTrip() throws Exception
    {
        final long TEST_VALUE = 456783257846l;

        long intermediate = EndianConverter.convert(TEST_VALUE);

        assertNotEquals(intermediate, TEST_VALUE, "Didn't convert the intermediate form properly");

        long result = EndianConverter.convert(intermediate);

        assertEquals(result, TEST_VALUE, "Round trip value failed");
    }

    @Test(groups = "unit")
    public void testShortArrayRoundTrip() throws Exception
    {
        final short[] TEST_VALUES = { 4, -2345, 10 };
        byte[] intermediate_values = new byte[TEST_VALUES.length * 2];
        short[] result_values = new short[TEST_VALUES.length];

        int count = EndianConverter.convertToLittleEndian(TEST_VALUES, intermediate_values, 0, TEST_VALUES.length);

        assertEquals(count, TEST_VALUES.length, "Wrong converted byte count");

        int processed =
            EndianConverter.convert(intermediate_values, result_values, count * 2, 0, TEST_VALUES.length);

        assertEquals(processed, TEST_VALUES.length * 2, "Wrong number of bytes converted to floats");

        for(int i = 0; i < TEST_VALUES.length; i++)
        {
            assertEquals(result_values[i], TEST_VALUES[i], "Value at index " + i + " is wrong");
        }
    }

    @Test(groups = "unit")
    public void testIntArrayRoundTrip() throws Exception
    {
        final int[] TEST_VALUES = { 4, -2345, 10 };
        byte[] intermediate_values = new byte[TEST_VALUES.length * 4];
        int[] result_values = new int[TEST_VALUES.length];

        int count = EndianConverter.convertToLittleEndian(TEST_VALUES, intermediate_values, 0, TEST_VALUES.length);

        assertEquals(count, TEST_VALUES.length, "Wrong converted byte count");

        int processed =
            EndianConverter.convert(intermediate_values, result_values, count * 4, 0, TEST_VALUES.length);

        assertEquals(processed, TEST_VALUES.length * 4, "Wrong number of bytes converted to floats");

        for(int i = 0; i < TEST_VALUES.length; i++)
        {
            assertEquals(result_values[i], TEST_VALUES[i], "Value at index " + i + " is wrong");
        }
    }

    @Test(groups = "unit")
    public void testLongArrayRoundTrip() throws Exception
    {
        final long[] TEST_VALUES = { 4, -2345, 10 };
        byte[] intermediate_values = new byte[TEST_VALUES.length * 8];
        long[] result_values = new long[TEST_VALUES.length];

        int count = EndianConverter.convertToLittleEndian(TEST_VALUES, intermediate_values, 0, TEST_VALUES.length);

        assertEquals(count, TEST_VALUES.length, "Wrong converted byte count");

        int processed =
            EndianConverter.convert(intermediate_values, result_values, count * 8, 0, TEST_VALUES.length);

        assertEquals(processed, TEST_VALUES.length * 8, "Wrong number of bytes converted to floats");

        for(int i = 0; i < TEST_VALUES.length; i++)
        {
            assertEquals(result_values[i], TEST_VALUES[i], "Value at index " + i + " is wrong");
        }
    }

    @Test(groups = "unit")
    public void testFloatArrayRoundTrip() throws Exception
    {
        final float[] TEST_VALUES = { 4.0f, -2345, 0.45f };
        byte[] intermediate_values = new byte[TEST_VALUES.length * 4];
        float[] result_values = new float[TEST_VALUES.length];

        int count = EndianConverter.convertToLittleEndian(TEST_VALUES, intermediate_values, 0, TEST_VALUES.length);

        assertEquals(count, TEST_VALUES.length, "Wrong converted byte count");

        int processed =
            EndianConverter.convertLittleEndianToFloat(intermediate_values,
                                                       result_values,
                                                       count * 4,
                                                       0,
                                                       TEST_VALUES.length);

        assertEquals(processed, TEST_VALUES.length * 4, "Wrong number of bytes converted to floats");

        for(int i = 0; i < TEST_VALUES.length; i++)
        {
            assertEquals(result_values[i], TEST_VALUES[i], 0.0001, "Value at index " + i + " is wrong");
        }
    }

    @Test(groups = "unit")
    public void testDoubleArrayRoundTrip() throws Exception
    {
        final double[] TEST_VALUES = { 4.0, -2345, 0.45 };
        byte[] intermediate_values = new byte[TEST_VALUES.length * 8];
        double[] result_values = new double[TEST_VALUES.length];

        int count = EndianConverter.convertToLittleEndian(TEST_VALUES, intermediate_values, 0, TEST_VALUES.length);

        assertEquals(count, TEST_VALUES.length, "Wrong converted byte count");

        int processed =
            EndianConverter.convertLittleEndianToDouble(intermediate_values,
                                                        result_values,
                                                        count * 8,
                                                        0,
                                                        TEST_VALUES.length);

        assertEquals(processed, TEST_VALUES.length * 8, "Wrong number of bytes converted to floats");

        for(int i = 0; i < TEST_VALUES.length; i++)
        {
            assertEquals(result_values[i], TEST_VALUES[i], 0.0001, "Value at index " + i + " is wrong");
        }
    }
}
