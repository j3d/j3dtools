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
import java.io.DataInput;
import java.io.InputStream;
import java.io.IOException;

// Local parser
import org.j3d.exporters.vterrain.BTVersion;
import org.j3d.io.LittleEndianDataInputStream;
import org.j3d.loaders.HeightMapSource;
import org.j3d.loaders.HeightMapSourceOrigin;
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.UnsupportedFormatException;

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
    /** Header string constant representing V1.0 */
    private static final String VERSION_1_0 = "binterr1.0";

    /** Header string constant representing V1.1 */
    private static final String VERSION_1_1 = "binterr1.1";

    /** Header string constant representing V1.2 */
    private static final String VERSION_1_2 = "binterr1.2";

    /** Header string constant representing V1.3 */
    private static final String VERSION_1_3 = "binterr1.3";

    /** Buffer while reading bytes from the stream */
    private byte[] buffer;

    /**
     * The stream used to fetch the data from. Must be set before calling
     * any methods in this base class.
     */
    private DataInput input;

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

        BufferedInputStream bis;

        if(is instanceof BufferedInputStream)
        {
            bis = (BufferedInputStream) is;
        }
        else
        {
            bis = new BufferedInputStream(is);
        }

        input = new LittleEndianDataInputStream(bis);
    }

    // ----- Methods defined by HeightMapSource ------------------------------

    @Override
    public float[][] getHeights()
    {
        return heights;
    }

    @Override
    public float[] getGridStep()
    {
        return gridStepData;
    }

    @Override
    public HeightMapSourceOrigin getOriginLocation()
    {
        return HeightMapSourceOrigin.BOTTOM_LEFT;
    }

    // ----- Local Methods ---------------------------------------------------

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param is The new stream to use
     */
    public void reset(InputStream is)
    {
        BufferedInputStream bis;

        if(is instanceof BufferedInputStream)
        {
            bis = (BufferedInputStream) is;
        }
        else
        {
            bis = new BufferedInputStream(is);
        }

        input = new LittleEndianDataInputStream(bis);

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
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @return A 2D array of the heights read, if requested in [column][row] order
     */
    public float[][] parse()
        throws IOException
    {
        if(dataReady)
        {
            throw new IOException("Data has already been read from this stream");
        }

        input.readFully(buffer, 0, 10);

        header = new BTHeader();
        int version = 0;   // 0 for v1.0, 1 for v1.1, 2 for v1.2 etc

        String versionStr = new String(buffer, "US-ASCII");

        switch(versionStr)
        {
            case VERSION_1_0:
                version = 0;
                header.version = BTVersion.VERSION_1_0;
                break;

            case VERSION_1_1:
                version = 1;
                header.version = BTVersion.VERSION_1_1;
                break;

            case VERSION_1_2:
                version = 2;
                header.version = BTVersion.VERSION_1_2;
                break;

            case VERSION_1_3:
                version = 3;
                header.version = BTVersion.VERSION_1_3;
                break;

            default:
                throw new UnsupportedFormatException("Can't handle version " + header.version + " files");
        }

        header.columns = input.readInt();
        header.rows = input.readInt();

        int rows = header.rows;
        int columns = header.columns;
        boolean floats_used = false;
        boolean two_byte_heights = false;

        if(version > 0)
        {
            two_byte_heights = parseByteSize(input.readShort());
            floats_used = (input.readShort() == 1);
        }
        else
        {
            two_byte_heights = parseByteSize(input.readInt());
        }


        switch(version)
        {
            case 0:
                floats_used = readHeader1_0();
                break;

            case 1:
                readHeader1_1();
                break;

            case 2:
                readHeader1_2();
                break;

            case 3:
                readHeader1_3();

        }

        switch(version)
        {
            case 0:
                input.skipBytes(212);
                break;

            case 1:
                input.skipBytes(196);
                break;

            case 2:
                input.skipBytes(194);
                break;

            case 3:
                input.skipBytes(190);
                break;

        }

        heights = new float[rows][columns];
        int i = 0;

        if(floats_used)
        {
            if(two_byte_heights)
            {
                for(int c = 0; c < columns; c++)
                {
                    for(int r = 0; r < rows; r++)
                    {
                        heights[c][r] = input.readShort();
                    }
                }
            }
            else
            {
                for(int c = 0; c < columns; c++)
                {
                    for(int r = 0; r < rows; r++)
                    {
                        heights[c][r] = input.readFloat();
                    }
                }
            }
        }
        else
        {
            if(two_byte_heights)
            {
                for(int c = 0; c < columns; c++)
                {
                    for(int r = 0; r < rows; r++)
                    {
                        heights[c][r] = input.readShort();
                    }
                }
            }
            else
            {
                for(int c = 0; c < columns; c++)
                {
                    for(int r = 0; r < rows; r++)
                    {
                        heights[c][r] = input.readInt();
                    }
                }
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
     * Convenience method to take the read byte size amount from
     * the stream and check for a value that makes sense. If not
     * then throw an exception.
     *
     * @param read The value read from the stream
     * @return true if to use two bytes for heights, false to use 4 bytes
     * @throws InvalidFormatException The read byte size was not 2 or 4
     */
    private boolean parseByteSize(int read)
    {
        switch(read)
        {
            case 2:
                return true;

            case 4:
                return false;

            default:
                throw new InvalidFormatException("Byte size of " +
                                                 read +
                                                 " is invalid. Expected either 2 or 4");

        }
    }

    private boolean readHeader1_0() throws IOException
    {
        header.utmProjection = (input.readShort() == 1);
        header.utmZone = input.readShort();

        header.datum = -2; // default for NO_DATUM value
        header.leftExtent = input.readFloat();
        header.rightExtent = input.readFloat();
        header.bottomExtent = input.readFloat();
        header.topExtent = input.readFloat();

        return (input.readShort() == 1);
    }

    private void readHeader1_1() throws IOException
    {
        header.utmProjection = (input.readShort() == 1);
        header.utmZone = input.readShort();

        header.datum = input.readShort();

        header.leftExtent = input.readDouble();
        header.rightExtent = input.readDouble();
        header.bottomExtent = input.readDouble();
        header.topExtent = input.readDouble();
    }

    private void readHeader1_2() throws IOException
    {
        header.utmProjection = (input.readShort() == 1);
        header.utmZone = input.readShort();

        header.datum = input.readShort();

        header.leftExtent = input.readDouble();
        header.rightExtent = input.readDouble();
        header.bottomExtent = input.readDouble();
        header.topExtent = input.readDouble();

        header.needsExternalProj = (input.readShort() == 1);
    }

    private void readHeader1_3() throws IOException
    {
        int horizontal_units = input.readShort();

        int utm_zone = input.readShort();

        header.utmProjection = (utm_zone == 1);
        header.utmZone = utm_zone;
        header.datum = input.readShort();

        header.leftExtent = input.readDouble();
        header.rightExtent = input.readDouble();
        header.bottomExtent = input.readDouble();
        header.topExtent = input.readDouble();

        header.needsExternalProj = (input.readShort() == 1);

        float vertical_scale = input.readFloat();
    }
}
