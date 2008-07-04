/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.loaders.ac3d;

// External imports
import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.UnsupportedFormatException;

/**
 * <p><code>AC3DFileParser</code> handles the work of parsing the AC3D data
 * from a stream. Since the AC3D file format is not only ASCII based, but
 * also formatted using lines, this gets away with just using a
 * <code>BufferedReader</code> and picking off the lines one by one.</p>
 *
 * <p>Although this parser was implemented to facilitate building an AC3D
 * file loader for importing models into Java3D, this parser aims to be
 * independant of the Java3D API. The intention is to be able to leverage this
 * code in other applications, as well. (Perhaps in a command line format
 * conversion tool...) Thus, the separation of Java3D and parsing code.</p>
 *
 * @author  Ryan Wilhm (ryan@entrophica.com)
 * @version $Revision: 1.2 $
 */
public class Ac3dParser
{
    /** Message when the header is not long enough */
    private static final String HEADER_TOO_SHORT_ERR =
        "AC3D data stream header too short.";

    /** Message when the header is invalid */
    private static final String INVALID_PREAMBLE =
        "Data stream did not contain a valid preamble.";

    /** Message when the version is not one supported */
    private static final String UNSUPPORTED_VERSION_ERR =
        "Format version in data stream grater than supported.";

    /** The header preamble. */
    public static final String HEADER_PREAMBLE="AC3D";

    /** The latest version of the file format this parser supports. */
    public static final int SUPPORTED_FORMAT_VERSION=0xb;

    private static final int MATERIAL_TOKEN = 1;
    private static final int OBJECT_TOKEN = 2;
    private static final int KIDS_TOKEN = 3;
    private static final int NUMVERT_TOKEN = 4;
    private static final int NAME_TOKEN = 5;
    private static final int LOCATION_TOKEN = 6;
    private static final int ROTATION_TOKEN = 7;
    private static final int NUMSURF_TOKEN = 8;
    private static final int SURF_TOKEN = 9;
    private static final int REFS_TOKEN = 10;
    private static final int MAT_TOKEN = 11;
    private static final int TEXTURE_TOKEN = 12;

    /** Set of keywords and the constants that they map to for fast parsing */
    private static HashMap<String, Integer> keywordsMap;

    /** Where the data comes from. */
    private BufferedReader reader;

    /** List of current materials found during parsing */
    private ArrayList<Ac3dMaterial> materials;

    /** List of current objects found during parsing */
    private ArrayList<Ac3dObject> objects;

    /** Parser for individual lines */
    private LineTokenizer lineTokenizer;

    /**
     * Static constructor to populate the keywords map
     */
    static
    {
        keywordsMap = new HashMap<String, Integer>();
        keywordsMap.put("MATERIAL", MATERIAL_TOKEN);
        keywordsMap.put("OBJECT", OBJECT_TOKEN);
        keywordsMap.put("kids", KIDS_TOKEN);
        keywordsMap.put("numvert", NUMVERT_TOKEN);
        keywordsMap.put("name", NAME_TOKEN);
        keywordsMap.put("loc", LOCATION_TOKEN);
        keywordsMap.put("rot", ROTATION_TOKEN);
        keywordsMap.put("numsurf", NUMSURF_TOKEN);
        keywordsMap.put("SURF", SURF_TOKEN);
        keywordsMap.put("refs", REFS_TOKEN);
        keywordsMap.put("mat", MAT_TOKEN);
        keywordsMap.put("texture", TEXTURE_TOKEN);
    }

    /**
     * Construct a default parser that does not have an input source.
     * An input source would need to be provided through the use of the
     * reset() methods.
     */
    public Ac3dParser()
    {
        materials = new ArrayList<Ac3dMaterial>();
        objects = new ArrayList<Ac3dObject>();
        lineTokenizer = new LineTokenizer();
    }

    /**
     * Construct a new parser that sources the data from the given
     * reader.
     *
     * @param rdr The reader instance to use
     */
    public Ac3dParser(Reader rdr)
    {
        this();

        if(rdr instanceof BufferedReader)
            reader = (BufferedReader)rdr;
        else
            reader = new BufferedReader(rdr);
    }

    /**
     * Performs the action of parsing the data stream already set.
     *
     * @throws InvalidFormatException The file format does not match the
     *   expected format for AC3D
     * @throws UnsupportedFormatException The format provided is later version
     *   than what we currently support
     * @throws IOException An I/O error occurred while processing the file
     */
    public void parse() throws IOException
    {
        // Deal with header
        checkHeader();

        // Read the token input line by line
        String buffer;

        while((buffer = reader.readLine()) != null)
        {
            String[] tokens = lineTokenizer.enumerateTokens(buffer);

            int token_id = keywordsMap.get(tokens[0]);
            switch(token_id)
            {
                case MATERIAL_TOKEN:
                    break;

                case OBJECT_TOKEN:
                    break;

                case KIDS_TOKEN:
                    break;

                case NUMVERT_TOKEN:
                    break;

                case NAME_TOKEN:
                    break;

                case LOCATION_TOKEN:
                    break;

                case ROTATION_TOKEN:
                    break;

                case NUMSURF_TOKEN:
                    break;

                case SURF_TOKEN:
                    break;

                case REFS_TOKEN:
                    break;

                case MAT_TOKEN:
                    break;

                case TEXTURE_TOKEN:
                    break;

                default:
                    // Issue message here
            }
        }
    }

    /**
     * Reset the parser with a new input reader. After calling this method
     * all currently stored information from the previous parsing pass will
     * be discarded.
     *
     * @param rdr The new reader instance to use
     */
    public void reset(Reader rdr)
    {
        if(rdr instanceof BufferedReader)
            reader = (BufferedReader)rdr;
        else
            reader = new BufferedReader(rdr);

        materials.clear();
        objects.clear();
    }

    /**
     * Fetch the materials from the previously loaded file. If nothing
     * has been loaded yet, this will return a zero length array.
     *
     * @return An array of the materials found
     */
    public Ac3dMaterial[] getMaterials()
    {
        return materials.toArray(new Ac3dMaterial[0]);
    }

    /**
     * Fetch the objects from the previously loaded file. If nothing
     * has been loaded yet, this will return a zero length array.
     *
     * @return An array of the materials found
     */
    public Ac3dObject[] getObjects()
    {
        return objects.toArray(new Ac3dObject[0]);
    }

    /**
     * Reads the header and determines if the parser is capable of handling
     * the data.
     *
     * @exception IOException
     */
    private void checkHeader() throws IOException
    {
        String header = reader.readLine();

        if(header.length() < 5)
            throw new InvalidFormatException(HEADER_TOO_SHORT_ERR);

        String str = header.substring(0,3);
        if(str.equals(HEADER_PREAMBLE))
            throw new InvalidFormatException(INVALID_PREAMBLE);

        str = header.substring(4, header.length());
        int version = Integer.valueOf(str, 16);

        if(version > SUPPORTED_FORMAT_VERSION)
            throw new UnsupportedFormatException(UNSUPPORTED_VERSION_ERR);
    }
}
