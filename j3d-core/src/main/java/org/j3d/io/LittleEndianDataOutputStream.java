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

import java.io.*;

/**
 * A DataOutputStream that writes data to the underlying stream in little-endian
 * order rather than Java's default of big-endian.
 *
 *
 * @author justin
 */
public class LittleEndianDataOutputStream extends FilterOutputStream
    implements DataOutput
{
    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     * this instance is to be created without an underlying stream.
     */
    public LittleEndianDataOutputStream(OutputStream in)
    {
        super(in);
    }

    // ----- Methods defined by DataOutput -----------------------------------

    @Override
    public void writeBoolean(boolean value) throws IOException
    {
        out.write(value ? (byte) 1 : (byte) 0);
    }

    @Override
    public void writeByte(int value) throws IOException
    {
        out.write(value);
    }

    @Override
    public void writeShort(int value) throws IOException
    {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    @Override
    public void writeChar(int value) throws IOException
    {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    @Override
    public void writeInt(int value) throws IOException
    {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }

    @Override
    public void writeLong(long value) throws IOException
    {
        out.write((int)(value & 0xFF));
        out.write((int)((value >> 8) & 0xFF));
        out.write((int)((value >> 16) & 0xFF));
        out.write((int)((value >> 24) & 0xFF));
        out.write((int)((value >> 32) & 0xFF));
        out.write((int)((value >> 40) & 0xFF));
        out.write((int)((value >> 48) & 0xFF));
        out.write((int)((value >> 56) & 0xFF));
    }

    @Override
    public void writeFloat(float value) throws IOException
    {
        writeInt(Float.floatToIntBits(value));
    }

    @Override
    public void writeDouble(double value) throws IOException
    {
        writeLong(Double.doubleToLongBits(value));
    }

    @Override
    public void writeBytes(String s) throws IOException
    {
        int len = s.length();
        for (int i = 0 ; i < len ; i++)
        {
            out.write((byte)s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOException
    {
        int len = s.length();
        for (int i = 0 ; i < len ; i++)
        {
            writeChar(s.charAt(i));
        }

    }

    @Override
    public void writeUTF(String s) throws IOException
    {
        int strlen = s.length();
        int utflen = 0;
        int c;

        // Find out max character size in the underlying string to count
        // the total bytes provided in the string.
        for (int i = 0; i < strlen; i++)
        {
            c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                utflen++;
            }
            else if (c > 0x07FF)
            {
                utflen += 3;
            }
            else
            {
                utflen += 2;
            }
        }

        if (utflen > 65535)
        {
            throw new UTFDataFormatException("Encoded string exceeded max length of 2^16 bytes: " + utflen + " bytes");
        }

        // Dump the length of the string, then put in characters
        writeShort(utflen);

        int i = 0;
        for (i = 0; i < strlen; i++)
        {
            c = s.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
            {
                break;
            }

            out.write((byte)c);
        }

        for ( ;i < strlen; i++)
        {
            c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                out.write((byte) c);
            }
            else if (c > 0x07FF)
            {
                out.write((byte) (0xE0 | ((c >> 12) & 0x0F)));
                out.write((byte) (0x80 | ((c >>  6) & 0x3F)));
                out.write((byte) (0x80 | ((c >>  0) & 0x3F)));
            }
            else
            {
                out.write((byte) (0xC0 | ((c >>  6) & 0x1F)));
                out.write((byte) (0x80 | ((c >>  0) & 0x3F)));
            }
        }
    }
}
