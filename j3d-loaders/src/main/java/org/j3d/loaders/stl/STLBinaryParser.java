/*****************************************************************************
 * STLBinaryParser.java
 * Java Source
 *
 * This source is licensed under the GNU LGPL v2.1.
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information.
 *
 * Copyright (c) 2002 Dipl. Ing. P. Szawlowski
 * University of Vienna, Dept. of Medical Computer Sciences
 ****************************************************************************/

package org.j3d.loaders.stl;

// External imports
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.awt.Component;
import javax.swing.ProgressMonitorInputStream;
import java.util.ArrayList;

// Local imports
import org.j3d.io.EndianConverter;
import org.j3d.loaders.InvalidFormatException;

/**
 * Class to parse STL (stereolithography) files in binary format.<p>
 * @see STLFileReader
 * @see STLLoader
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version $Revision: 1.3 $
 */
class STLBinaryParser extends STLParser
{
    /** size of binary header */
    private static final int HEADER_SIZE = 84;

    /** size of one facet record in binary format */
    private static final int RECORD_SIZE = 50;

    /** size of comments in header */
    private static final int COMMENT_SIZE = 80;

    /** The stream that is being read from */
    private BufferedInputStream itsStream;

    /** Common buffer for reading */
    private byte[] itsReadBuffer;

    /** Common buffer for reading the converted data from bytes */
    private int[] itsDataBuffer;

    STLBinaryParser()
    {
        itsReadBuffer = new byte[48];
        itsDataBuffer = new int[12];
    }

    /**
     * Constructor.
     *
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     */
    STLBinaryParser(boolean strict)
    {
        super(strict);

        itsReadBuffer = new byte[48];
        itsDataBuffer = new int[12];
    }

    @Override
    public void close() throws IOException
    {
        if(itsStream != null)
        {
            itsStream.close();
        }
    }

    @Override
    public boolean parse(URL url)
        throws InvalidFormatException, IOException
    {
        URLConnection connection = url.openConnection();
        InputStream stream = connection.getInputStream();
        int length = connection.getContentLength();

        itsStream = new BufferedInputStream(stream);
        return parse(length);
    }

    @Override
    public boolean parse(URL url,  Component parentComponent)
        throws InvalidFormatException, IOException
    {
        URLConnection connection = url.openConnection();
        InputStream stream = connection.getInputStream();
        int length = connection.getContentLength();

        stream = new ProgressMonitorInputStream(
            parentComponent,
            "parsing " + url.toString(),
            stream);

        itsStream = new BufferedInputStream(stream);
        return parse(length);
    }

    /**
     * Internal convenience method that does the stream parsing regardless of
     * the input source the stream came from. Assumes itsStream is already
     * initialised before it called here.
     *
     * @param length The length of data from the incoming stream, if not. Use
     *   -1 not known.
     * @return true if the method does not work out
     */
    private boolean parse(int length)
        throws InvalidFormatException, IOException
    {
        try
        {
            // skip header until number of facets info
            for(int i = 0; i < COMMENT_SIZE; i ++)
            {
                itsStream.read();
            }

            // binary file contains only on object
            itsNumOfObjects = 1;
            itsNumOfFacets = new int[]{ EndianConverter.read4ByteBlock(itsStream) };
            itsNames = new String[1];
            // if length of file is known, check if it matches with the content
            // binary file contains only on object
            if(strictParsing && length != -1 &&
               length != itsNumOfFacets[0] * RECORD_SIZE + HEADER_SIZE)
            {
                String msg = "File size does not match the expected size for" +
                    " the given number of facets. Given " +
                    itsNumOfFacets[0] + " facets for a total size of " +
                    (itsNumOfFacets[0] * RECORD_SIZE + HEADER_SIZE) +
                    " but the file size is " + length;
                close();

                throw new InvalidFormatException(msg);
            }
            else if (!strictParsing && length != -1 &&
                     length != itsNumOfFacets[0] * RECORD_SIZE + HEADER_SIZE)
            {

               String msg = "File size does not match the expected size for" +
                    " the given number of facets. Given " +
                    itsNumOfFacets[0] + " facets for a total size of " +
                    (itsNumOfFacets[0] * RECORD_SIZE + HEADER_SIZE) +
                    " but the file size is " + length;

                if (parsingMessages == null)
                {
                    parsingMessages = new ArrayList<>();
                }
                parsingMessages.add(msg);
            }
        }
        catch(IOException e)
        {
            close();
            throw e;
        }
        return false;
    }

    /**
     * Read the next face from the underlying stream
     *
     * @return true if the read completed successfully
     */
    @Override
    public boolean getNextFacet(double[] normal, double[][] vertices)
        throws IOException
    {
        EndianConverter.read(itsReadBuffer,
                             itsDataBuffer,
                             0,
                             12,
                             itsStream);

        boolean nan_found = false;;

        for(int i = 0; i < 3; i ++)
        {
            normal[i] = Float.intBitsToFloat(itsDataBuffer[i]);
            if (Double.isNaN(normal[i]) || Double.isInfinite(normal[i]))
            {
                nan_found = true;
            }
        }

        if (nan_found)
        {
            // STL spec says use 0 0 0 for autocalc
            normal[0] = 0;
            normal[1] = 0;
            normal[2] = 0;
        }

        for(int i = 0; i < 3; i ++)
        {
            for(int j = 0; j < 3; j ++)
            {
                vertices[i][j] = Float.intBitsToFloat(itsDataBuffer[i * 3 + j + 3]);
            }
        }

        // skip last 2 padding bytes
        itsStream.read();
        itsStream.read();
        return true;
    }
}
