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

import java.io.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Unit test for the data input stream handler
 *
 * @author justin
 */
public class BlockDataInputStreamTest
{
    @Test(groups = "unit")
    public void testSimpleByteArrayReading() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        int readCount = classUnderTest.read(read_result);

        assertEquals(readCount, test_data.length, "Incorrect number of items read");

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(read_result[i], test_data[i], "Read array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testExtendedByteArrayReading() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };
        final int TEST_READ_OFFSET = 3;

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        int readCount = classUnderTest.read(read_result, TEST_READ_OFFSET, test_data.length);

        assertEquals(readCount, test_data.length, "Incorrect number of items read");

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(read_result[TEST_READ_OFFSET + i], test_data[i], "Read array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit", expectedExceptions = IndexOutOfBoundsException.class)
    public void testExtendedByteArrayReadingInvalidLength() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        classUnderTest.read(read_result, 0, -2);
    }

    @Test(groups = "unit", expectedExceptions = IndexOutOfBoundsException.class)
    public void testExtendedByteArrayReadingInvalidOffset() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        classUnderTest.read(read_result, -2, test_data.length);
    }

    @Test(groups = "unit")
    public void testSimpleByteArrayFullReading() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the exact size to ensure we get all the data.
        byte[] read_result = new byte[test_data.length];

        classUnderTest.readFully(read_result);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(read_result[i], test_data[i], "Read array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testByteArrayMultiReading() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the exact size to ensure we get all the data.
        byte[] read_result1 = new byte[test_data.length - 2];
        byte[] read_result2 = new byte[2];

        classUnderTest.readFully(read_result1);

        for(int i = 0; i < test_data.length - 2; i++)
        {
            assertEquals(read_result1[i], test_data[i], "First read contents incorrect at index " + i);
        }

        classUnderTest.readFully(read_result2);

        for(int i = 0; i < 2; i++)
        {
            assertEquals(read_result2[i], test_data[test_data.length - 2 + i], "Second read contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testExtendedByteArrayFullReading() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };
        final int TEST_READ_OFFSET = 3;

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        classUnderTest.readFully(read_result, TEST_READ_OFFSET, test_data.length);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(read_result[TEST_READ_OFFSET + i], test_data[i], "Read array contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class)
    public void testSimpleFullReadingNotEnoughData() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // generate the EOFException.
        byte[] read_result = new byte[test_data.length + 1];

        classUnderTest.readFully(read_result);
    }

    @Test(groups = "unit", expectedExceptions = IndexOutOfBoundsException.class)
    public void testExtendedFullReadingInvalidLength() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        classUnderTest.readFully(read_result, 0, -2);
    }

    @Test(groups = "unit", expectedExceptions = IndexOutOfBoundsException.class)
    public void testExtendedFullReadingInvalidOffset() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the read array bigger than the source data to make sure we
        // read the correct values.
        byte[] read_result = new byte[test_data.length * 2];

        classUnderTest.readFully(read_result, -2, test_data.length);
    }

    @Test(groups = "unit", dependsOnMethods = "testByteArrayMultiReading")
    public void testSkipBytes() throws Exception
    {
        final byte[] test_data = { 0, 0, 1, 2, 4, 8 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        // Make the exact size to ensure we get all the data.
        byte[] read_result1 = new byte[test_data.length - 3];
        byte[] read_result2 = new byte[2];

        classUnderTest.readFully(read_result1);

        for(int i = 0; i < test_data.length - 3; i++)
        {
            assertEquals(read_result1[i], test_data[i], "First read contents incorrect at index " + i);
        }

        classUnderTest.skipBytes(1);

        classUnderTest.readFully(read_result2);

        for(int i = 0; i < 2; i++)
        {
            assertEquals(read_result2[i], test_data[test_data.length - 2 + i], "Second read contents incorrect at index " + i);
        }
    }

    @Test(groups = "unit")
    public void testReadBoolean() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { 0, 1, 3, -1 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        assertFalse(classUnderTest.readBoolean(), test_data[0] + " was not false");
        assertTrue(classUnderTest.readBoolean(), test_data[1] + " was not true");
        assertTrue(classUnderTest.readBoolean(), test_data[2] + " was not true");
        assertTrue(classUnderTest.readBoolean(), test_data[3] + " was not true");
    }

    @Test(groups = "unit", expectedExceptions = IOException.class)
    public void testReadBooleanEmptyStream() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readBoolean();
    }

    @Test(groups = "unit")
    public void testReadByte() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { 0, 1, 3, -1 };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readByte(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class)
    public void testReadByteEmptyStream() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readByte();
    }

    @Test(groups = "unit")
    public void testReadUnsignedByte() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { 0, 1, 3, -1 };
        final int[] expected_result = new int[test_data.length];

        for(int i = 0; i < test_data.length; i++)
        {
            expected_result[i] = test_data[i] < 0 ? (2 << (Byte.SIZE - 1)) + test_data[i] : test_data[i];
        }

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readUnsignedByte(), expected_result[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class)
    public void testReadUnsignedByteEmptyStream() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readUnsignedByte();
    }

    @Test(groups = "unit")
    public void testReadShort() throws Exception
    {
        final short[] test_data = { 0, 10, -400, 300, -4 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeShort(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readShort(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class, dataProvider = "2 byte count")
    public void testReadShortEmptyStream(int numBytes) throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = new byte[numBytes];

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readShort();
    }

    @Test(groups = "unit")
    public void testReadUnsignedShort() throws Exception
    {
        final short[] test_data = { 0, 10, -400, 300, -4 };
        final int[] expected_result = new int[test_data.length];

        for(int i = 0; i < test_data.length; i++)
        {
            expected_result[i] = test_data[i] < 0 ? (2 << (Short.SIZE - 1)) + test_data[i] : test_data[i];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeShort(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readUnsignedShort(), expected_result[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class, dataProvider = "2 byte count")
    public void testReadUnsignedShortEmptyStream(int numBytes) throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = new byte[numBytes];

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readUnsignedShort();
    }

    @Test(groups = "unit")
    public void testReadChar() throws Exception
    {
        final char[] test_data = { 'a', '$', '\u0400', '1', 'W' };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeChar(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readChar(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class)
    public void testReadCharEmptyStream() throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = { };

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readChar();
    }

    @Test(groups = "unit")
    public void testReadInt() throws Exception
    {
        final int[] test_data = { 0, 10, -400, 300, -4 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeInt(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readInt(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class, dataProvider = "4 byte count")
    public void testReadIntEmptyStream(int numBytes) throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = new byte[numBytes];

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readInt();
    }

    @Test(groups = "unit")
    public void testReadBulkInts() throws Exception
    {
        final int[] test_data = { 0, 10, -400, 300, -4 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeInt(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        int[] test_result = new int[test_data.length];
        classUnderTest.readInts(test_result, test_data.length);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(test_result[i], test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit")
    public void testReadLong() throws Exception
    {
        final long[] test_data = { 0, 10, -400, 300, -4 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeLong(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readLong(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class, dataProvider = "8 byte count")
    public void testReadLongEmptyStream(int numBytes) throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = new byte[numBytes];

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readLong();
    }

    @Test(groups = "unit")
    public void testReadFloat() throws Exception
    {
        final float[] test_data = { 0, 10, 1.46E-5f, -1.0f, -4.653f };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeFloat(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readFloat(), test_data[i], 0.001, "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class, dataProvider = "4 byte count")
    public void testReadFloatEmptyStream(int numBytes) throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = new byte[numBytes];

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readFloat();
    }

    @Test(groups = "unit")
    public void testReadBulkFloats() throws Exception
    {
        final float[] test_data = { 0, 10, 1.46E-5f, -1.0f, -4.653f };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeFloat(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        float[] test_result = new float[test_data.length];
        classUnderTest.readFloats(test_result, test_data.length);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(test_result[i], test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit")
    public void testReadDouble() throws Exception
    {
        final double[] test_data = { 0, 10, 1.46E-5, -1.0, -4.653 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeDouble(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readDouble(), test_data[i], 0.001, "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit", expectedExceptions = IOException.class, dataProvider = "8 byte count")
    public void testReadDoubleEmptyStream(int numBytes) throws Exception
    {
        // should produce, in order, false, true, true, true
        final byte[] test_data = new byte[numBytes];

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        classUnderTest.readDouble();
    }

    @Test(groups = "unit")
    public void testReadUTF8() throws Exception
    {
        final String[] test_data = { "abcdef", "12345", "My 1nter#\u0400tes?" };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for(int i = 0; i < test_data.length; i++)
            dos.writeUTF(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readUTF(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @Test(groups = "unit")
    public void testReadLine() throws Exception
    {
        // ReadLine cannot deal with multibyte character implementations. Assumes basic
        // 7-bit ASCII.
        final String[] test_data = { "abcdef", "12345", "My 1nter#tes?" };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter dos = new PrintWriter(new OutputStreamWriter(bos));

        for(int i = 0; i < test_data.length; i++)
            dos.println(test_data[i]);

        dos.flush();
        dos.close();

        byte[] test_byte_data = bos.toByteArray();

        ByteArrayInputStream test_input = new ByteArrayInputStream(test_byte_data);
        BlockDataInputStream classUnderTest = new BlockDataInputStream(test_input);

        for(int i = 0; i < test_data.length; i++)
        {
            assertEquals(classUnderTest.readLine(), test_data[i], "Test data at index " + i + " was not correct");
        }
    }

    @DataProvider(name = "2 byte count")
    public Object[][] generateTest2ByteCounts()
    {
        return new Object[][]
            {
                { 0 },
                { 1 },
            };
    }

    @DataProvider(name = "4 byte count")
    public Object[][] generateTest4ByteCounts()
    {
        return new Object[][]
            {
                { 0 },
                { 1 },
                { 2 },
                { 3 },
            };
    }

    @DataProvider(name = "8 byte count")
    public Object[][] generateTest8ByteCounts()
    {
        return new Object[][]
        {
            { 0 },
            { 1 },
            { 2 },
            { 3 },
            { 4 },
            { 5 },
            { 6 },
            { 7 },
        };
    }
}
