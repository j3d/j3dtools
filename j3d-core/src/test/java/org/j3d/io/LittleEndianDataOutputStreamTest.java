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

package org.j3d.io;

import java.io.ByteArrayOutputStream;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the output stream.
 *
 * @author justin
 */
public class LittleEndianDataOutputStreamTest
{
    @Test(groups = "unit")
    public void testSimpleByteArrayWrite() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        classUnderTest.write(test_data);
        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(written_result[i], test_data[i], "Written array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testByteWrite() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        for(int i = 0; i < test_data.length; i++)
        {
            classUnderTest.write(test_data[i]);
        }

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, test_data.length, "Wrong amount of data written");

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(written_result[i], test_data[i], "Written array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testWriteBoolean() throws Exception
    {
        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        classUnderTest.writeBoolean(true);
        classUnderTest.writeBoolean(false);

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, 2, "Incorrect number of bytes written out");
        assertEquals(written_result[0], 1, "True value incorrectly written");
        assertEquals(written_result[1], 0, "False value incorrectly written");
    }

    @Test(groups = "unit")
    public void testWriteShort() throws Exception
    {
        final short[] test_data = { 0, 1, 2, 8, 10849, -34};

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        for(int i = 0; i < test_data.length; i++)
        {
            classUnderTest.writeShort(test_data[i]);
        }

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, test_data.length * 2, "Incorrect number of bytes written out");

        for(int i = 0; i < test_data.length; i++)
        {
            short output = (short)(((written_result[i * 2 + 1] & 0xFF) << 8) + (written_result[i * 2] & 0xFF));
            assertEquals(output, test_data[i], "Written array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testWriteInt() throws Exception
    {
        final int[] test_data = { 0, 1, 2, 8, 10835649, -34};

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        for(int i = 0; i < test_data.length; i++)
        {
            classUnderTest.writeInt(test_data[i]);
        }

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, test_data.length * 4, "Incorrect number of bytes written out");

        for(int i = 0; i < test_data.length; i++)
        {
            int output =
                ((written_result[i * 4 + 3] & 0xFF) << 24) +
                    ((written_result[i * 4 + 2] & 0xFF) << 16) +
                    ((written_result[i * 4 + 1] & 0xFF) << 8) +
                (written_result[i * 4] & 0xFF);
            assertEquals(output, test_data[i], "Written array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testWriteLong() throws Exception
    {
        final long[] test_data = { 0, 1, 2, 8, 108359432449l, -34};

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        for(int i = 0; i < test_data.length; i++)
        {
            classUnderTest.writeLong(test_data[i]);
        }

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, test_data.length * 8, "Incorrect number of bytes written out");

        for(int i = 0; i < test_data.length; i++)
        {
            long output =
                    ((long)(written_result[i * 8 + 7] & 0xFF) << 56) +
                    ((long)(written_result[i * 8 + 6] & 0xFF) << 48) +
                    ((long)(written_result[i * 8 + 5] & 0xFF) << 40) +
                    ((long)(written_result[i * 8 + 4] & 0xFF) << 32) +
                    ((long)(written_result[i * 8 + 3] & 0xFF) << 24) +
                    ((written_result[i * 8 + 2] & 0xFF) << 16) +
                    ((written_result[i * 8 + 1] & 0xFF) << 8) +
                    (written_result[i * 8] & 0xFF);
            assertEquals(output, test_data[i], "Written array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testWriteFloat() throws Exception
    {
        final float[] test_data = { 0, 1, 0.456f, 10835649, -34};

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        for(int i = 0; i < test_data.length; i++)
        {
            classUnderTest.writeFloat(test_data[i]);
        }

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, test_data.length * 4, "Incorrect number of bytes written out");

        for(int i = 0; i < test_data.length; i++)
        {
            int output =
                ((written_result[i * 4 + 3] & 0xFF) << 24) +
                    ((written_result[i * 4 + 2] & 0xFF) << 16) +
                    ((written_result[i * 4 + 1] & 0xFF) << 8) +
                    (written_result[i * 4] & 0xFF);


            assertEquals(Float.intBitsToFloat(output), test_data[i], "Written array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testWriteDouble() throws Exception
    {
        final double[] test_data = { 0, 1, 2, 0.0000345, 108359432449.0, -34};

        ByteArrayOutputStream test_output_stream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream classUnderTest = new LittleEndianDataOutputStream(test_output_stream);

        for(int i = 0; i < test_data.length; i++)
        {
            classUnderTest.writeDouble(test_data[i]);
        }

        classUnderTest.flush();
        classUnderTest.close();

        byte[] written_result = test_output_stream.toByteArray();

        assertEquals(written_result.length, test_data.length * 8, "Incorrect number of bytes written out");

        for(int i = 0; i < test_data.length; i++)
        {
            long output =
                ((long)(written_result[i * 8 + 7] & 0xFF) << 56) +
                    ((long)(written_result[i * 8 + 6] & 0xFF) << 48) +
                    ((long)(written_result[i * 8 + 5] & 0xFF) << 40) +
                    ((long)(written_result[i * 8 + 4] & 0xFF) << 32) +
                    ((long)(written_result[i * 8 + 3] & 0xFF) << 24) +
                    ((written_result[i * 8 + 2] & 0xFF) << 16) +
                    ((written_result[i * 8 + 1] & 0xFF) << 8) +
                    (written_result[i * 8] & 0xFF);
            assertEquals(Double.longBitsToDouble(output), test_data[i], "Written array contents incorrect at index " + i);
        }
    }

}
