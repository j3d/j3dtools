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

import java.io.*;
import java.util.*;

import java.net.URL;
import java.awt.Component;
import javax.swing.ProgressMonitorInputStream;

// Internal imports
import org.j3d.loaders.InvalidFormatException;
import org.j3d.util.I18nManager;
import org.j3d.geom.GeometryData;

/**
 * Class to parse OBJ (stereolithography) files in ASCII format.<p>
 * <p/>
 * <p/>
 * <b>Internationalisation Resource Names</b>
 * <p/>
 * <ul>
 * <li>invalidKeywordMsg: Unknown keyword encountered. </li>
 * <li>emptyFileMsg: File contained the header but no content. </li>
 * <li>invalidDataMsg: Some strange data was encountered. </li>
 * <li>unexpectedEofMsg: We hit an EOF before we were expecting to.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 2.0 $
 * @see OBJFileReader
 */
class OBJASCIIParser
{
    /** Max number of unsupported messages before we go silent */
    private static final int UNSUPPORTED_MAX_MSGS = 10;

    /** Coordinate data */
    private ArrayList<double[]> coords;

    /** Normal data */
    private ArrayList<double[]> normals;

    /** Texture Coordinate data */
    private ArrayList<double[]> texCoords;

    /** Tex coords are missing */
    private boolean texCoordMissing;

    /** Normals are missing */
    private boolean normalCoordMissing;

    /** The count of unsupported errors.  Stop at UNSUPPORTED_MAX_MSGS */
    private int unsupportedCount;

    /** Error message of a keyword that we don't recognise */
    private static final String UNKNOWN_KEYWORD_MSG_PROP =
        "org.j3d.loaders.stl.OBJASCIIParser.invalidKeywordMsg";

    /**
     * Error message when the solid header is found, but there is no
     * geometry after it. Basically an empty file.
     */
    private static final String EMPTY_FILE_MSG_PROP =
        "org.j3d.loaders.stl.OBJASCIIParser.emptyFileMsg";

    /** Unexpected data is encountered during parsing */
    private static final String INVALID_NORMAL_DATA_MSG_PROP =
        "org.j3d.loaders.stl.OBJASCIIParser.invalidNormalDataMsg";

    /** Unexpected data is encountered during parsing */
    private static final String INVALID_VERTEX_DATA_MSG_PROP =
        "org.j3d.loaders.stl.OBJASCIIParser.invalidVertexDataMsg";

    /** Unexpected data is encountered during parsing */
    private static final String INVALID_FACE_DATA_MSG_PROP =
        "org.j3d.loaders.stl.OBJASCIIParser.invalidFaceDataMsg";

    /** Unexpected EOF is encountered during parsing */
    private static final String EOF_WTF_MSG_PROP =
        "org.j3d.loaders.stl.OBJASCIIParser.unexpectedEofMsg";

    /** Reader for the main stream */
    private BufferedReader itsReader;

    /** The line number that we're at in the file */
    private int lineCount;

    /**
     * Create a new default parser instance.
     */
    OBJASCIIParser()
    {
        this(false);
    }


    /**
     * Create a new default parser instance.
     *
     * @param strict Attempt to deal with crappy data or short downloads.
     * Will try to return any useable geometry.
     */
    OBJASCIIParser(boolean strict)
    {
        strictParsing = strict;

        coords = new ArrayList<>();
        normals = new ArrayList<>();
        texCoords = new ArrayList<double[]>();
    }

    /** Do we strictly parse or try harder */
    protected boolean strictParsing;

    /** Detailed parsing messages or null if none */
    protected List<String> parsingMessages;

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
     * Finish the parsing off now.
     */
    public void close() throws IOException
    {
        if(itsReader != null)
            itsReader.close();
    }

    /**
     * Get the next object in the stream.
     *
     * @return The object or null if EOF reached.
     */
    public GeometryData getNextObject() throws IOException, InvalidFormatException
    {
        GeometryData ret_val = null;
        ArrayList<int[]> coord_indexes = new ArrayList<>();
        ArrayList<int[]> normal_indexes = new ArrayList<>();
        ArrayList<int[]> texCoord_indexes = new ArrayList<>();

        String input_line = itsReader.readLine();

        texCoordMissing = false;
        normalCoordMissing = false;
        unsupportedCount = 0;
        boolean obj_started = false;

        loop:
        while(input_line != null)
        {
            if(input_line.startsWith("#"))
            {
                input_line = itsReader.readLine();

                continue;
            }
            else if(input_line.contains("\\"))
            {
                input_line = input_line.replace("\\", "");
                // Line break.  "Spec" doesn't give rules
                input_line += itsReader.readLine();
                continue;
            }
            else
            {
                StringTokenizer strtok = new StringTokenizer(input_line, " ");

                if(!strtok.hasMoreElements())
                {
                    input_line = itsReader.readLine();
                    continue;
                }

                String token = strtok.nextToken();

                switch(token)
                {
                    case "o":
                        if(obj_started)
                        {
                            break loop;
                        }
                        // new object
                        break;

                    case "v":
                        double[] coord = readCoordinate(strtok);
                        coords.add(coord);
                        if(ret_val == null)
                        {
                            ret_val = new GeometryData();
                        }
                        // vertex
                        break;

                    case "vn":

                        // normal
                        coord = readNormal(strtok);
                        normals.add(coord);
                        break;

                    case "vt":

                        // texture coordinate
                        coord = readTextureCoordinate(strtok);
                        texCoords.add(coord);
                        break;

                    case "f":
                        int[][] face = readFace(strtok);
                        coord_indexes.add(face[0]);

                        if(face.length > 1)
                        {
                            texCoord_indexes.add(face[1]);
                        }

                        if(face.length > 2)
                        {
                            normal_indexes.add(face[2]);
                        }

                        obj_started = true;
                        break;

                    case "cstype":
                        // Unsupported Geometry

                        if(unsupportedCount < UNSUPPORTED_MAX_MSGS)
                        {
                            String msg = "Unsupported geometry: " + token;
                            if(parsingMessages == null)
                            {
                                parsingMessages = new ArrayList<String>();
                            }
                            parsingMessages.add(msg);
                        }

                        unsupportedCount++;

                        break;

                    case "g":
                        // Ignore Grouping command
                        break;
                    case "s":
                        // Ignore State
                        break;

                    case "surf":
                    case "ctech":
                    case "curv":
                    case "curv2":
                    case "trim":
                    case "scurv":
                    case "end":
                    case "deg":
                    case "parm":
                        // Ignore, caught by cstype
                        break;

                    default:
                        // Unsupported
                        if(unsupportedCount < UNSUPPORTED_MAX_MSGS)
                        {

                            System.out.println("Unsupported: " + input_line);
                        }

                        unsupportedCount++;
                        break;
                }
            }

            input_line = itsReader.readLine();

        }

        if(ret_val == null)
        {
            return null;
        }

        int len = coords.size();
        ret_val.vertexCount = len;
        ret_val.coordinates = new float[len * 3];
        int idx = 0;

        for(double[] val: coords)
        {
            ret_val.coordinates[idx++] = (float) val[0];
            ret_val.coordinates[idx++] = (float) val[1];
            ret_val.coordinates[idx++] = (float) val[2];
        }

        if(!normalCoordMissing && normals.size() > 0)
        {
            len = normals.size();
            ret_val.normals = new float[len * 3];
            idx = 0;

            for(double[] val: normals)
            {
                ret_val.normals[idx++] = (float) val[0];
                ret_val.normals[idx++] = (float) val[1];
                ret_val.normals[idx++] = (float) val[2];
            }
        }

        if(!texCoordMissing && texCoords.size() > 0)
        {
            len = texCoords.size();
            ret_val.textureCoordinates = new float[len * 2];
            idx = 0;

            for(double[] val: texCoords)
            {
                ret_val.textureCoordinates[idx++] = (float) val[0];
                ret_val.textureCoordinates[idx++] = (float) val[1];
            }
        }

        len = coord_indexes.size();
        int count = 0;

        // TODO: do this count during creation?
        for(int i = 0; i < len; i++)
        {
            int[] face = coord_indexes.get(i);
            count += face.length;
        }

        ret_val.indexesCount = count;
        ret_val.indexes = new int[count + len];  // for extra -1
        idx = 0;
        int max_face_size = 0;

        for(int i = 0; i < len; i++)
        {
            int[] face = coord_indexes.get(i);

            max_face_size = max_face_size < face.length ? face.length: max_face_size;

            for(int aFace : face)
            {
                if(aFace < 0 || aFace >= coords.size())
                {
                    throw new InvalidFormatException("Coordinate index out of bounds");
                }
                ret_val.indexes[idx++] = aFace;
            }

            ret_val.indexes[idx++] = -1;
        }

        switch(max_face_size)
        {
            case 0:
            case 1:
                throw new InvalidFormatException("No valid lines or triangles found");

            case 2:
                ret_val.geometryType = GeometryData.INDEXED_LINES;
                break;

            case 3:
                ret_val.geometryType = GeometryData.INDEXED_TRIANGLES;
                break;

            case 4:
                ret_val.geometryType = GeometryData.INDEXED_QUADS;
                break;

            default:
                ret_val.geometryType = GeometryData.INDEXED_POLYGONS;
                break;
        }

        len = texCoord_indexes.size();
        count = 0;

        // TODO: do this count during creation?
        for(int i = 0; i < len; i++)
        {
            int[] face = texCoord_indexes.get(i);

            if(face != null)
            {
                count += face.length;
            }
        }

        if(count > 0)
        {
            boolean error_found = false;

            ret_val.texCoordIndexes = new int[count + len];  // for extra -1
            idx = 0;
            loop:
            for(int i = 0; i < len; i++)
            {
                int[] face = texCoord_indexes.get(i);
                for(int aFace : face)
                {
                    if(aFace < 0 || aFace >= texCoords.size())
                    {
                        if(strictParsing)
                        {
                            throw new InvalidFormatException("TextureCoordinate index out of bounds");
                        }
                        else
                        {
                            error_found = true;
                            break loop;
                        }
                    }

                    ret_val.texCoordIndexes[idx++] = aFace;
                }

                ret_val.texCoordIndexes[idx++] = -1;
            }

            if(error_found)
            {
                ret_val.texCoordIndexes = null;
            }
            else
            {
                ret_val.geometryComponents |= GeometryData.TEXTURE_2D_DATA;
            }
        }

        len = normal_indexes.size();
        count = 0;

        // TODO: do this count during creation?
        for(int i = 0; i < len; i++)
        {
            int[] face = normal_indexes.get(i);

            if(face != null)
            {
                count += face.length;
            }
        }

        if(!normalCoordMissing && count > 0)
        {
            boolean error_found = false;

            ret_val.normalIndexes = new int[count + len];  // for extra -1
            ret_val.geometryComponents |= GeometryData.NORMAL_DATA;

            idx = 0;
            loop:
            for(int i = 0; i < len; i++)
            {
                int[] face = normal_indexes.get(i);
                for(int aFace : face)
                {
                    if(aFace < 0 || aFace >= normals.size())
                    {
                        if(strictParsing)
                        {
                            throw new InvalidFormatException("Normal index out of bounds");
                        }
                        else
                        {
                            error_found = true;
                            break loop;
                        }
                    }

                    ret_val.normalIndexes[idx++] = aFace;
                }

                ret_val.normalIndexes[idx++] = -1;
            }

            if(error_found)
            {
                ret_val.normalIndexes = null;
            }
        }

        return ret_val;
    }

    /**
     * Parses the file to obtain the number of objects, object names and number
     * of facets per object. A progress monitor will show the progress during
     * parsing.
     * @param url URL to read from.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     * @return <code>true</code> if file is in ASCII format, <code>false</code>
     *      otherwise. Use the appropriate subclass for reading.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public boolean parse(URL url, Component parentComponent)
        throws InterruptedIOException, IOException
    {
        InputStream stream = url.openStream();

        stream = new ProgressMonitorInputStream(
            parentComponent, "analyzing " + url.toString(), stream);

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream));

        boolean isAscii = false;

        try
        {
            isAscii = parse(reader);
        }
        finally
        {
            reader.close();
        }

        if(!isAscii)
        {
            return false;
        }

        try
        {
            stream = url.openStream();
        }
        catch(IOException e)
        {
            stream.close();
            throw e;
        }

        stream = new ProgressMonitorInputStream(
            parentComponent,
            "parsing " + url.toString(),
            stream);

        reader = new BufferedReader(new InputStreamReader(stream));
        itsReader = reader;

        return true;
    }

    /**
     * Parses the file to obtain the number of objects, object names and number
     * of facets per object.
     * @param url URL to read from.
     * @return <code>true</code> if file is in ASCII format, <code>false</code>
     *      otherwise. Use the appropriate subclass for reading.
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public boolean parse(URL url)
        throws IOException
    {
        InputStream stream = url.openStream();

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream));
        boolean isAscii = false;

        try
        {
            isAscii = parse(reader);
        }
        catch(InterruptedIOException e)
        {
            // should never happen
            e.printStackTrace();
        }
        finally
        {
            reader.close();
        }

        if(!isAscii)
            return false;

        try
        {
            stream = url.openStream();
        }
        catch(IOException e)
        {
            stream.close();
            throw e;
        }

        reader = new BufferedReader(new InputStreamReader(stream));
        itsReader = reader;

        return true;
    }

    /**
     * Parse the stream now from the given reader.
     *
     * @param reader The reader to source the file from
     * @return true if this is a ASCII format file, false if not
     * @throws InvalidFormatException The file was structurally incorrect
     * @throws IOException Something happened during the reading
     */
    private boolean parse(BufferedReader reader)
        throws IOException, InvalidFormatException
    {
        // There is not header information
        return true;
    }

    /**
     * Read three numbers from the tokeniser and place them in the double value
     * returned.
     */
    private double[] readCoordinate(StringTokenizer strtok)
        throws IOException
    {
        double[] vector = new double[3];

        for(int i = 0; i < 3; i++)
        {
            String num_str = strtok.nextToken();

            if(num_str.isEmpty())
            {
                // ignore extra spaces
                i = i - 1;
                continue;
            }

            try
            {
                vector[i] = Double.parseDouble(num_str);
            }
            catch(NumberFormatException e)
            {
                if(strictParsing)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                        ": Cannot parse vertex: " + num_str;
                    throw new InvalidFormatException(msg);
                }
                else
                {
                    // Common error is to use commas instead of . in Europe
                    String new_str = num_str.replace(",", ".");

                    try
                    {
                        vector[i] = Double.parseDouble(new_str);
                    }
                    catch(NumberFormatException e2)
                    {

                        I18nManager intl_mgr = I18nManager.getManager();

                        String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                            ": Cannot parse vertex: " + num_str;
                        throw new InvalidFormatException(msg);
                    }
                }
            }
        }

        return vector;
    }

    /**
     * Read three numbers from the tokeniser and place them in the double value
     * returned.
     */
    private double[] readNormal(StringTokenizer strtok)
        throws IOException
    {
        double[] vector = new double[3];

        for(int i = 0; i < 3; i++)
        {
            String num_str = strtok.nextToken();

            boolean error_found = false;

            try
            {
                vector[i] = Double.parseDouble(num_str);
            }
            catch(NumberFormatException e)
            {
                if(strictParsing)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                        ": Cannot parse normal: " + num_str;
                    throw new InvalidFormatException(msg);
                }
                else
                {
                    // Common error is to use commas instead of . in Europe
                    String new_str = num_str.replace(",", ".");

                    try
                    {
                        vector[i] = Double.parseDouble(new_str);
                    }
                    catch(NumberFormatException e2)
                    {

                        error_found = true;
                    }
                }

                if(error_found)
                {
                    vector[0] = 0;
                    vector[1] = 0;
                    vector[2] = 0;
                }
            }
        }

        return vector;
    }

    /**
     * Read three numbers from the tokeniser and place them in the double value
     * returned.
     */
    private double[] readTextureCoordinate(StringTokenizer strtok)
        throws IOException
    {
        double[] vector = new double[2];

        for(int i = 0; i < 2; i++)
        {
            String num_str = strtok.nextToken();

            boolean error_found = false;

            try
            {
                vector[i] = Double.parseDouble(num_str);
            }
            catch(NumberFormatException e)
            {
                if(strictParsing)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                        ": Cannot parse texture coordinate: " + num_str;
                    throw new InvalidFormatException(msg);
                }
                else
                {
                    // Common error is to use commas instead of . in Europe
                    String new_str = num_str.replace(",", ".");

                    try
                    {
                        vector[i] = Double.parseDouble(new_str);
                    }
                    catch(NumberFormatException e2)
                    {

                        error_found = true;
                    }
                }

                if(error_found)
                {
                    vector[0] = 0;
                    vector[1] = 0;
                }
            }
        }

        return vector;
    }

    /**
     * Read a face
     * returned.
     */
    private int[][] readFace(StringTokenizer strtok)
        throws IOException
    {
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Integer> indices_tc = new ArrayList<>();
        ArrayList<Integer> indices_normals = new ArrayList<>();
        int num_comps = 0;

        while(strtok.hasMoreElements())
        {
            String num_str = strtok.nextToken();

            if(!num_str.contains("/"))
            {
                num_comps = 1;
                try
                {
                    int index = Integer.parseInt(num_str);

                    if(index < 0)
                    {
//System.out.println("Input index: " + index + " coord size: " + coords.size() + " ans: " + (coords.size() - index));

                        // Need to resolve relative index
                        index = coords.size() + index;
                        indices.add(index);
                    }
                    else
                    {
                        indices.add(index - 1); //  Account for weird 1 numbering
                    }
                }
                catch(NumberFormatException e)
                {
                    if(strictParsing)
                    {
                        I18nManager intl_mgr = I18nManager.getManager();

                        String msg = intl_mgr.getString(INVALID_FACE_DATA_MSG_PROP) +
                            ": Cannot parse face: " + num_str;
                        throw new InvalidFormatException(msg);
                    }
                    else
                    {
                        // Common error is to use commas instead of . in Europe
                        String new_str = num_str.replace(",", ".");

                        try
                        {
                            int index = Integer.parseInt(new_str);

                            if(index < 0)
                            {
                                // Need to resolve relative index
                                index = coords.size() + index;
                            }
                            else
                            {
                                indices.add(index - 1); //  Account for weird 1 numbering
                            }
                        }
                        catch(NumberFormatException e2)
                        {

                            I18nManager intl_mgr = I18nManager.getManager();

                            String msg = intl_mgr.getString(INVALID_FACE_DATA_MSG_PROP) +
                                ": Cannot parse face: " + num_str;
                            throw new InvalidFormatException(msg);
                        }
                    }
                }
            }
            else
            {
                // We have multiple indexes
                num_str = num_str.replace("//", "/U/");  // denote unspecified
                StringTokenizer toker = new StringTokenizer(num_str, "/");

                num_comps = toker.countTokens();

                num_str = toker.nextToken();

                int index = Integer.parseInt(num_str);

                if(index < 0)
                {
                    // Need to resolve relative index
                    index = coords.size() + index;
                }
                else
                {
                    indices.add(index - 1); //  Account for weird 1 numbering
                }

                if(num_comps >= 2)
                {
                    num_str = toker.nextToken();

                    if(num_str != null)
                    {
                        if(num_str.equals("U"))
                        {
                            texCoordMissing = true;
                        }
                        else
                        {
                            index = Integer.parseInt(num_str);

                            if(index < 0)
                            {
                                // Need to resolve relative index
                                index = texCoords.size() + index;
                                indices_tc.add(index);
                            }
                            else
                            {
                                indices_tc.add(index - 1); //  Account for weird 1 numbering
                            }
                        }
                    }
                }

                if(num_comps >= 3)
                {
                    num_str = toker.nextToken();

                    if(num_str.equals("U"))
                    {
                        normalCoordMissing = true;
                    }
                    else
                    {
                        index = Integer.parseInt(num_str);

                        if(index < 0)
                        {
                            // Need to resolve relative index
                            index = normals.size() + index;
                            indices_normals.add(index);
                        }
                        else
                        {
                            indices_normals.add(index - 1); //  Account for weird 1 numbering
                        }
                    }
                }

            }
        }

        int len = indices.size();
        int[][] ret_val = new int[num_comps][];
        ret_val[0] = new int[len];

        for(int i = 0; i < len; i++)
        {
            ret_val[0][i] = indices.get(i);
        }

        if(num_comps > 1 && !texCoordMissing)
        {
            len = indices_tc.size();
            ret_val[1] = new int[len];
            for(int i = 0; i < len; i++)
            {
                ret_val[1][i] = indices_tc.get(i);
            }
        }

        if(num_comps > 2 && !normalCoordMissing)
        {
            len = indices_normals.size();
            ret_val[2] = new int[len];

            for(int i = 0; i < len; i++)
            {
                ret_val[2][i] = indices_normals.get(i);
            }
        }

        return ret_val;
    }
}
