/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                              Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.io;

// External imports
import java.io.*;

// Local imports
// None

/**
 * A data input stream which allows reading of arrays of primative types as
 * well as the standard types a DataInputStream allows.
 * <p>
 * This class is considerably faster then the standard DataInputStream provided
 * by the standard JDK. This class is not thread safe. Do not call two methods
 * at the same time.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
 public class BlockDataInputStream extends FilterInputStream
    implements DataInput
{

    /** A scratch buffer for byte reading. Grown as needed */
    private byte[] byteBuff;

    /** A scratch buffer for byte reading. Grown as needed */
    private byte[] readBuffer;

    /** A scratch buffer for string reading. Grown as needed */
    private char[] lineBuffer;

    /**
     * Creates a BlockDataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param  in  the specified input stream
     */
    public BlockDataInputStream(InputStream in)
    {
        super(in);

        byteBuff = new byte[4];
        readBuffer = new byte[8];
    }

    public int read(byte b[])
        throws IOException
    {
        return in.read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len)
        throws IOException
    {
        return in.read(b, off, len);
    }

    public void readFully(byte b[])
        throws IOException
    {
        readFully(b, 0, b.length);
    }

    public void readFully(byte b[], int off, int len)
        throws IOException
    {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
            throw new EOFException();
            n += count;
        }
    }

    public int skipBytes(int n)
        throws IOException
    {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }

        return total;
    }

    public boolean readBoolean()
        throws IOException
    {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    public byte readByte()
        throws IOException
    {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    public int readUnsignedByte()
        throws IOException
    {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    public short readShort()
        throws IOException
    {
        readFully(byteBuff, 0, 2);

        int ch1;
        int ch2;

        ch1 = byteBuff[0];
        ch2 = (byteBuff[1] & 255);

        return (short)((ch1 << 8) + (ch2 << 0));
/*

        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
*/
    }

    public int readUnsignedShort()
        throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + ch2;
    }

    public char readChar()
        throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + ch2);
    }

    public int readInt()
        throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }

    /**
     * Reads n ints into an array.  The array must be preallocated
     * to at least n size.
     *
     * @param data The place to store the data
     * @param len The number of floats to read.
     */
    public void readInts(int[] data, int len)
        throws IOException
    {
        int size = len * 4;
        if (byteBuff.length < size)
            byteBuff = new byte[size];

        readFully(byteBuff, 0, size);

        int ch1;
        int ch2;
        int ch3;
        int ch4;
        int idx=0;

        for(int i=0; i < len; i++)
        {
            ch1 = byteBuff[idx++];
            ch2 = (byteBuff[idx++] & 255);
            ch3 = (byteBuff[idx++] & 255);
            ch4 = (byteBuff[idx++] & 255);

            data[i] = ((ch1 << 24) + (ch2 << 16) +
                  (ch3 << 8) + (ch4 << 0));
        }
    }

    public long readLong()
        throws IOException
    {
        readFully(readBuffer, 0, 8);
        return (((long)readBuffer[0] << 56) +
                ((long)(readBuffer[1] & 255) << 48) +
                ((long)(readBuffer[2] & 255) << 40) +
                ((long)(readBuffer[3] & 255) << 32) +
                ((long)(readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) <<  8) +
                ((readBuffer[7] & 255) <<  0));
    }

    public float readFloat()
        throws IOException
    {
        readFully(byteBuff, 0, 4);

        int ch1;
        int ch2;
        int ch3;
        int ch4;

        ch1 = byteBuff[0];
        ch2 = (byteBuff[1] & 255);
        ch3 = (byteBuff[2] & 255);
        ch4 = (byteBuff[3] & 255);

        return Float.intBitsToFloat((ch1 << 24) + (ch2 << 16) +
                  (ch3 << 8) + (ch4 << 0));

        //return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads n floats into an array.  The array must be preallocated
     * to at least n size.
     *
     * @param data The place to store the data
     * @param len The number of floats to read.
     */
    public void readFloats(float[] data, int len)
        throws IOException
    {
        int size = len * 4;
        if (byteBuff.length < size)
            byteBuff = new byte[size];

        readFully(byteBuff, 0, size);

        int ch1;
        int ch2;
        int ch3;
        int ch4;
        int idx=0;

        for(int i=0; i < len; i++)
        {
            ch1 = byteBuff[idx++];
            ch2 = (byteBuff[idx++] & 255);
            ch3 = (byteBuff[idx++] & 255);
            ch4 = (byteBuff[idx++] & 255);

            data[i] = Float.intBitsToFloat(((ch1 << 24) + (ch2 << 16) +
                  (ch3 << 8) + (ch4 << 0)));
        }
    }

    public double readDouble()
        throws IOException
    {
        return Double.longBitsToDouble(readLong());
    }

    public String readLine()
        throws IOException
    {
        char buf[] = lineBuffer;

        if(buf == null)
            buf = lineBuffer = new char[128];

        int room = buf.length;
        int offset = 0;
        int c;

        loop:
        while (true)
        {
            switch (c = in.read())
            {
                case -1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = in.read();
                    if ((c2 != '\n') && (c2 != -1))
                    {
                        if(!(in instanceof PushbackInputStream))
                            in = new PushbackInputStream(in);

                        ((PushbackInputStream)in).unread(c2);
                    }
                    break loop;

                default:
                    if(--room < 0)
                    {
                        buf = new char[offset + 128];
                        room = buf.length - offset - 1;
                        System.arraycopy(lineBuffer, 0, buf, 0, offset);
                        lineBuffer = buf;
                    }

                    buf[offset++] = (char) c;
                    break;
            }
        }

        if((c == -1) && (offset == 0))
            return null;

        return String.copyValueOf(buf, 0, offset);
    }

    public String readUTF()
        throws IOException
    {
        return readUTF(this);
    }

    public static String readUTF(DataInput in)
        throws IOException
    {
        int utflen = in.readUnsignedShort();

        // TODO: Stop creating new StringBuffers each time
        StringBuffer str = new StringBuffer(utflen);

        // TODO: Stop creating bytearr each time
        byte[] bytearr = new byte[utflen];
        int c, char2, char3;
        int count = 0;

        in.readFully(bytearr, 0, utflen);

        while (count < utflen)
        {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    str.append((char)c);
                    break;

                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if(count > utflen)
                        throw new UTFDataFormatException();
                    char2 = (int) bytearr[count-1];

                    if((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException();

                    str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException();

                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException();
                    str.append((char)(((c     & 0x0F) << 12) |
                                      ((char2 & 0x3F) << 6)  |
                                      ((char3 & 0x3F) << 0)));
                break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();
            }
        }

        // The number of chars produced may be less than utflen
        return new String(str);
    }
}
