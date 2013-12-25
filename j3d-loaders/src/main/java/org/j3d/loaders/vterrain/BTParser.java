/*****************************************************************************
 *                            (c) j3d.org 2002-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.vterrain;

// Externla imports
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

// Local parser
import org.j3d.loaders.HeightMapSource;

/**
 * A low-level parser for the VTerrain's Project  BT file format.
 * <p>
 *
 * The output of this parser is the flags and the raw height-field information.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.vterrain.org/Implementation/BT.html">
 *  http://www.vterrain.org/Implementation/BT.html
 * </a>
 *
 * @author  Paul Byrne, Justin Couch
 * @version $Revision: 1.7 $
 */
public class BTParser implements HeightMapSource
{
    /** Buffer while reading bytes from the stream */
    private byte[] buffer;

    /**
     * The stream used to fetch the data from. Must be set before calling
     * any methods in this base class.
     */
    private BufferedInputStream input;

    /** The header information for this file */
    private BTHeader header;

    /** The height array in [row][column] */
    private float[][] heights;

    /** Grid information about the file. */
    private float[] gridStepData;

    /** Flag to say we've already read the stream */
    private boolean dataReady;

    /**
     * Construct a new parser with no stream set.
     */
    public BTParser()
    {
        buffer = new byte[10];
        gridStepData = new float[2];
        dataReady = false;
    }

    /**
     * Construct a new parser using the given stream.
     *
     * @param is The stream to read data from
     */
    public BTParser(InputStream is)
    {
        this();

        if(is instanceof BufferedInputStream)
            input = (BufferedInputStream)is;
        else
            input = new BufferedInputStream(is);
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param is The new stream to use
     */
    public void reset(InputStream is)
    {
        if(is instanceof BufferedInputStream)
            input = (BufferedInputStream)is;
        else
            input = new BufferedInputStream(is);

        dataReady = false;
        header = null;
        heights = null;
    }

    /**
     * Clear the internal data structures used by this parser.
     */
    public void clear()
    {
        dataReady = false;
        input = null;
        header = null;
        heights = null;
    }

    /**
     * Get the header used to describe the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The header for the last read stream or null
     */
    public BTHeader getHeader()
    {
        return header;
    }

    /**
     * Return the height map created for the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The array of heights in [row][column] order or null
     */
    public float[][] getHeights()
    {
        return heights;
    }

    /**
     * Fetch information about the real-world stepping sizes that this
     * grid uses.
     *
     * @return The stepping information for width and depth
     */
    public float[] getGridStep()
    {
        return gridStepData;
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @return A 2D array of the heighs read, if requested
     * @throws IncorrectFormatException The file is not one our loader
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public float[][] parse()
        throws IOException
    {
        if(dataReady)
            throw new IOException("Data has already been read from this stream");

        input.read(buffer, 0, 10);

        header = new BTHeader();
        int version = 0;   // 0 for v1.0, 1 for v1.1, 2 for v1.2 etc

        header.version = new String(buffer, "US-ASCII");

        if(header.version.equals(BTHeader.VERSION_1_0))
            version = 0;
        else if(header.version.equals(BTHeader.VERSION_1_1))
            version = 1;
        else if(header.version.equals(BTHeader.VERSION_1_2))
            version = 2;

        header.columns = readInt();
        header.rows = readInt();

        int rows = header.rows;
        int columns = header.columns;
        boolean floats_used = false;

        int data_size = readShort();

        if(version > 0)
            floats_used = (readShort() == 1);

        header.utmProjection = (readShort() == 1);
        header.utmZone = readShort();

        if(version > 0)
        {
            header.datum = readShort();

            header.leftExtent = readDouble();
            header.rightExtent = readDouble();
            header.bottomExtent = readDouble();
            header.topExtent = readDouble();
        }
        else
        {
            header.leftExtent = readFloat();
            header.rightExtent = readFloat();
            header.bottomExtent = readFloat();
            header.topExtent = readFloat();

            floats_used = (readShort() == 1);
        }

        switch(version)
        {
            case 0:
                input.skip(212);
                break;

            case 1:
                input.skip(196);
                break;

            case 2:
                header.needsExternalProj = (readShort() == 1);
                input.skip(194);
        }

        heights = new float[rows][columns];
        int i = 0;

        if(floats_used)
        {
            for(int c = 0; c < columns; c++)
            {
                for(int r = 0; r < rows; r++)
                    heights[r][c] = readFloat();
            }
        }
        else
        {
            for(int c = 0; c < columns; c++)
            {
                for(int r = 0; r < rows; r++)
                    heights[r][c] = readInt();
            }
        }

        float width = (float)(header.rightExtent - header.leftExtent);
        float depth = (float)(header.topExtent - header.bottomExtent);

        gridStepData[0] = width / header.rows;
        gridStepData[1] = depth / header.columns;

        dataReady = true;

        return heights;
    }

    /**
     * Read a long value from the stream
     *
     * @return A long value
     * @throws IOException An error occurred reading the stream
     */
    private final long readLong() throws IOException
    {
        input.read(buffer, 0, 8);

        long l1 = (buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8) |
                  ((buffer[2] & 0xFF) << 16) | ((buffer[3] & 0xFF) << 24);
        long l2 = ((buffer[4] & 0xFF)) | ((buffer[5] & 0xFF) << 8) |
                  ((buffer[6] & 0xFF) << 16) | ((buffer[7] & 0xFF) << 24);

        return l1 + (l2 << 32);
    }

    /**
     * Read a int value from the stream.
     *
     * @return A int value
     * @throws IOException An error occurred reading the stream
     */
    private final int readInt() throws IOException
    {
        input.read(buffer, 0, 4);

        return (buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8) |
               ((buffer[2] & 0xFF) << 16) | ((buffer[3] & 0xFF) << 24);
    }

    /**
     * Read a short value from the stream.
     *
     * @return A short value
     * @throws IOException An error occurred reading the stream
     */
    private final short readShort() throws IOException
    {
        input.read(buffer, 0, 2);
        return (short)((buffer[0] & 0xFF) + ((buffer[1] & 0xFF) << 8));
    }

    /**
     * Read a float value from the stream
     *
     * @return A float value
     * @throws IOException An error occurred reading the stream
     */
    private final float readFloat() throws IOException
    {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Read a double value from the stream
     *
     * @return A double value
     * @throws IOException An error occurred reading the stream
     */
    private final double readDouble() throws IOException
    {
        return Double.longBitsToDouble(readLong());
    }
}
