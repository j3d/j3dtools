/*****************************************************************************
 * OBJParser.java
 * Java Source
 *
 * This source is licensed under the GNU LGPL v2.1.
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information.
 *
 * Copyright (c) 2011 Shapeways, Inc
 ****************************************************************************/

package org.j3d.loaders.obj;

// External imports
import java.net.URL;
import java.awt.Component;
import java.io.IOException;
import java.util.List;

// Local imports
import org.j3d.loaders.InvalidFormatException;
import org.j3d.geom.GeometryData;

/**
 * Abstract base class for parsing OBJ (stereolithography) files. Subclasses
 * of this class implement parsing the two formats of OBJ files: binary and
 * ASCII.<p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
abstract class OBJParser
{
    /** Do we strictly parse or try harder */
    protected boolean strictParsing;

    /** Detailed parsing messages or null if none */
    protected List<String> parsingMessages;

    public OBJParser()
    {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     */
    public OBJParser(boolean strict)
    {
        strictParsing = strict;
    }

    /**
     * Get detailed messages on what was wrong when parsing.  Only can happen
     * when strictParsing is false.  Means things like getNumOfFacets might
     * be larger then reality.
     */
    public List<String> getParsingMessages()
    {
        return parsingMessages;
    }

    /**
     * Releases used resources. Must be called after finishing reading.
     */
    abstract void close() throws IOException;

    /**
     * Parses the file to obtain the number of objects, object names and number
     * of facets per object.
     * @param url URL to read from.
     * @return <code>true</code> if file is in ASCII format, <code>false</code>
     *      otherwise. Use the appropriate subclass for reading.
     */
    abstract boolean parse(URL url)
        throws IOException;

    /**
     * Parses the file to obtain the number of objects, object names and number
     * of facets per object. A progress monitor will show the progress during
     * parsing.
     * @param url URL to read from.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @return <code>true</code> if file is in ASCII format, <code>false</code>
     *      otherwise. Use the appropriate subclass for reading.
     */
    abstract boolean parse(URL url, Component parentComponent)
        throws InvalidFormatException, IOException;

    /**
     * Get the next object in the stream.
     *
     * @return The object or null if EOF reached.
     */
    abstract public GeometryData getNextObject() throws IOException;
}
