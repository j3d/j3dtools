/*****************************************************************************
 * OBJFileReader.java
 * Java Source
 *
 * This source is licensed under the GNU LGPL v2.1.
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information.
 *
 * Copyright (c) 2001, 2002 Dipl. Ing. P. Szawlowski
 * University of Vienna, Dept. of Medical Computer Sciences
 ****************************************************************************/

package org.j3d.loaders.obj;

// External Imports
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.awt.Component;
import java.util.List;

// Local imports
import org.j3d.loaders.InvalidFormatException;
import org.j3d.geom.GeometryData;

/**
 * Class to read OBJ files.
 *
 * Usage: First create a <code>OBJFileReader</code> object. To obtain the number
 * of objects, name of objects and number of facets for each object use the
 * appropriate methods. Then use the {@link #getNextFacet} method repetitively
 * to obtain the geometric data for each facet. Call {@link #close} to free the
 * resources.<p>
 * In case that the file uses the binary OBJ format, no check can be done to
 * assure that the file is in OBJ format. A wrong format will only be
 * recognized if an invalid amount of data is contained in the file.<p>
 *
 * @author  Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OBJFileReader
{
    private OBJParser itsParser;

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format.
     * @param file <code>File</code> object of OBJ file to read.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(File file)
        throws InvalidFormatException, IOException
    {
        this(file.toURL());
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format.
     * @param fileName Name of OBJ file to read.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(String fileName)
        throws InvalidFormatException, IOException
    {
        this(new URL(fileName));
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format.
     * @param fileName Name of OBJ file to read.
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(String fileName, boolean strict)
        throws InvalidFormatException, IOException
    {
        this(new URL(fileName), strict);
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from an
     * URL. The data may be in ASCII or binary format.
     * @param url URL of OBJ file to read.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(URL url)
        throws InvalidFormatException, IOException
    {
        final OBJASCIIParser asciiParser = new OBJASCIIParser();

        if(asciiParser.parse(url))
        {
            itsParser = asciiParser;
        }
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from an
     * URL. The data may be in ASCII or binary format.
     * @param url URL of OBJ file to read.
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(URL url, boolean strict)
        throws InvalidFormatException, IOException
    {

        final OBJParser asciiParser = new OBJASCIIParser(strict);

        if(asciiParser.parse(url))
        {
            itsParser = asciiParser;
        }
    }


    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from an
     * URL. The data may be in ASCII or binary format. A progress monitor will
     * show the progress during reading.
     * @param url URL of OBJ file to read.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(URL url, Component parentComponent)
        throws InvalidFormatException, IOException
    {
        final OBJASCIIParser asciiParser = new OBJASCIIParser();
        if(asciiParser.parse(url, parentComponent))
        {
            itsParser = asciiParser;
        }
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from an
     * URL. The data may be in ASCII or binary format. A progress monitor will
     * show the progress during reading.
     * @param url URL of OBJ file to read.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(URL url, Component parentComponent, boolean strict)
        throws InvalidFormatException, IOException
    {
        final OBJASCIIParser asciiParser = new OBJASCIIParser(strict);
        if(asciiParser.parse(url, parentComponent))
        {
            itsParser = asciiParser;
        }
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format. A progress monitor will
     * show the progress during reading.
     * @param file <code>File</code> object of OBJ file to read.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(File file, Component parentComponent)
        throws InvalidFormatException, IOException
    {
        this(file.toURL(), parentComponent);
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format. A progress monitor will
     * show the progress during reading.
     * @param file <code>File</code> object of OBJ file to read.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader(File file, Component parentComponent, boolean strict)
        throws InvalidFormatException, IOException
    {
        this(file.toURL(), parentComponent, strict);
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format. A progress monitor will
     * show the progress during reading.
     * @param fileName Name of OBJ file to read.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader (String fileName, Component parentComponent)
        throws InvalidFormatException, IOException
    {
        this(new URL(fileName), parentComponent);
    }

    /**
     * Creates a <code>OBJFileReader</code> object to read a OBJ file from a
     * file. The data may be in ASCII or binary format. A progress monitor will
     * show the progress during reading.
     * @param fileName Name of OBJ file to read.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public OBJFileReader (String fileName, Component parentComponent, boolean strict)
        throws InvalidFormatException, IOException
    {
        this(new URL(fileName), parentComponent, strict);
    }


    /**
     * Get the next object in the stream.
     *
     * @return The object or null if EOF reached.
     */
    public GeometryData getNextObject() throws IOException {
        return itsParser.getNextObject();
    }

    /**
     * Get detailed messages on what was wrong when parsing.  Only can happen
     * when strictParsing is false.  Means things like getNumOfFacets might
     * be larger then reality.
     */
    public List<String> getParsingMessages()
    {
        return itsParser.getParsingMessages();
    }

    /**
     * Releases used resources. Must be called after finishing reading.
     */
    public void close() throws IOException
    {
        if(itsParser != null)
        {
            itsParser.close();
        }
    }
}
