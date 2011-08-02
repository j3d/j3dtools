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
import java.net.URLConnection;
import java.awt.Component;
import javax.swing.ProgressMonitorInputStream;

// Internal imports
import org.j3d.loaders.InvalidFormatException;
import org.j3d.util.I18nManager;
import org.j3d.geom.GeometryData;

/**
 * Class to parse OBJ (stereolithography) files in ASCII format.<p>
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>invalidKeywordMsg: Unknown keyword encountered. </li>
 * <li>emptyFileMsg: File contained the header but no content. </li>
 * <li>invalidDataMsg: Some strange data was encountered. </li>
 * <li>unexpectedEofMsg: We hit an EOF before we were expecting to.</li>
 * </ul>
 *
 * @see OBJFileReader
 * @see OBJLoader
 * @author  Alan Hudson
 * @version $Revision: 2.0 $
 */
class OBJASCIIParser extends OBJParser
{
    /** Coordinate data */
    private ArrayList<double[]> coords;

    /** Normal data */
    private ArrayList<double[]> normals;

    /** Texture Coordinate data */
    private ArrayList<double[]> texCoords;

    private boolean texCoordMissing = false;
    private boolean normalCoordMissing = false;

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
    private BufferedReader  itsReader;

    /** The line number that we're at in the file */
    private int lineCount;

    /**
     * Create a new default parser instance.
     */
    public OBJASCIIParser()
    {
        coords = new ArrayList<double[]>();
        normals = new ArrayList<double[]>();
        texCoords = new ArrayList<double[]>();
    }


    /**
     * Create a new default parser instance.
     */
    public OBJASCIIParser(boolean strict)
    {
        super(strict);

        coords = new ArrayList<double[]>();
        normals = new ArrayList<double[]>();
        texCoords = new ArrayList<double[]>();
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
    public GeometryData getNextObject() throws IOException
    {
        GeometryData ret_val = null;
        ArrayList<int[]> coord_indexes = new ArrayList<int[]>();
        ArrayList<int[]> normal_indexes = new ArrayList<int[]>();
        ArrayList<int[]> texCoord_indexes = new ArrayList<int[]>();

        String input_line = itsReader.readLine();

        texCoordMissing = false;
        normalCoordMissing = false;
        boolean obj_started = false;

        loop: while(input_line != null)
        {
            if (input_line.startsWith("#")) {
                input_line = itsReader.readLine();

                continue;
            } else if (input_line.indexOf("\\") > -1) {
                input_line = input_line.replace("\\","");
                // Line break.  "Spec" doesn't give rules
                input_line += itsReader.readLine();
                continue loop;
            } else {

                StringTokenizer strtok = new StringTokenizer(input_line," ");

                if (!strtok.hasMoreElements()) {
                    input_line = itsReader.readLine();
                    continue;
                }

                String token = strtok.nextToken();

                if (token.equals("o"))
                {
                    if (obj_started) {
                        break;
                    }
                    // new object
                } else if (token.equals("v"))
                {
                    double[] coord = readCoordinate(strtok);
                    coords.add(coord);
//System.out.println("Found vertex: " + java.util.Arrays.toString(coord));
                    if (ret_val == null) {
                        ret_val = new GeometryData();
                    }
                    // vertex
                } else if (token.equals("vn"))
                {
                    // normal
                    double[] coord = readNormal(strtok);
                    normals.add(coord);
                } else if (token.equals("vt"))
                {
                    // texture coordinate
                    double[] coord = readTextureCoordinate(strtok);
                    texCoords.add(coord);
                } else if (token.equals("f"))
                {
                    int[][] face = readFace(strtok);
//System.out.println("Found face: " + java.util.Arrays.toString(face));
                    coord_indexes.add(face[0]);

                    if (face.length > 1)
                    {
                        texCoord_indexes.add(face[1]);
                    }

                    if (face.length > 2)
                    {
                        normal_indexes.add(face[2]);
                    }

                    obj_started = true;
                } else if (token.equals("g")) {
                    // Ignore Grouping command
                } else if (token.equals("s")) {
                    // Ignore State
                } else if (token.equals("cstype")) {
                    // Unsupported Geometry

                    String msg = "Unsupported geometry: " + token;
                    if (parsingMessages == null) {
                        parsingMessages = new ArrayList<String>();
                    }
                    parsingMessages.add(msg);

                } else if (token.equals("surf")) {
                    // Ignore, caught by cstype
                } else if (token.equals("ctech")) {
                    // Ignore, caught by cstype
                } else if (token.equals("curv")) {
                    // Ignore, caught by cstype
                } else if (token.equals("curv2")) {
                    // Ignore, caught by cstype
                } else if (token.equals("trim")) {
                    // Ignore, caught by cstype
                } else if (token.equals("scurv")) {
                    // Ignore, caught by cstype
                } else if (token.equals("end")) {
                    // Ignore, caught by cstype
                } else if (token.equals("deg")) {
                    // Ignore, caught by cstype
                } else if (token.equals("parm")) {
                    // Ignore, caught by cstype
                } else
                {
                    // Unsupported
                    System.out.println("Unsupported: " + input_line);
                }
            }

            input_line = itsReader.readLine();

        }

        if (ret_val == null)
            return null;

        int len = coords.size();
        ret_val.vertexCount = len;
        ret_val.coordinates = new float[len * 3];
        int idx = 0;

        for(int i=0; i < len; i++) {
            double[] val = coords.get(i);
            ret_val.coordinates[idx++] = (float) val[0];
            ret_val.coordinates[idx++] = (float) val[1];
            ret_val.coordinates[idx++] = (float) val[2];
        }

        if (!normalCoordMissing && normals.size() > 0) {
            len = normals.size();
            ret_val.normals = new float[len * 3];
            idx = 0;

            for(int i=0; i < len; i++) {
                double[] val = normals.get(i);
                ret_val.normals[idx++] = (float) val[0];
                ret_val.normals[idx++] = (float) val[1];
                ret_val.normals[idx++] = (float) val[2];
            }
        }

        if (!texCoordMissing && texCoords.size() > 0) {
            len = texCoords.size();
            ret_val.textureCoordinates = new float[len * 2];
            idx = 0;

            for(int i=0; i < len; i++) {
                double[] val = texCoords.get(i);
                ret_val.textureCoordinates[idx++] = (float) val[0];
                ret_val.textureCoordinates[idx++] = (float) val[1];
            }
        }

        len = coord_indexes.size();
        int count = 0;

        // TODO: do this count during creation?
        for(int i=0; i < len; i++) {
            int[] face = coord_indexes.get(i);
            count += face.length;
        }

        ret_val.indexesCount = count;
        ret_val.indexes = new int[count + len];  // for extra -1
        idx = 0;
        for(int i=0; i < len; i++) {
            int[] face = coord_indexes.get(i);
            for(int j=0; j < face.length; j++) {
                ret_val.indexes[idx++] = face[j];
            }

            ret_val.indexes[idx++] = -1;
        }

        len = texCoord_indexes.size();
        count = 0;

        // TODO: do this count during creation?
        for(int i=0; i < len; i++) {
            int[] face = texCoord_indexes.get(i);
            count += face.length;
        }

        if (count > 0)
        {
            ret_val.texCoordIndexes = new int[count + len];  // for extra -1
            idx = 0;
            for(int i=0; i < len; i++)
            {
                int[] face = texCoord_indexes.get(i);
                for(int j=0; j < face.length; j++)
                {
                    ret_val.texCoordIndexes[idx++] = face[j];
                }

                ret_val.texCoordIndexes[idx++] = -1;
            }
        }

        len = normal_indexes.size();
        count = 0;

        // TODO: do this count during creation?
        for(int i=0; i < len; i++)
        {
            int[] face = normal_indexes.get(i);
            count += face.length;
        }

        if (!normalCoordMissing && count > 0)
        {
            ret_val.normalIndexes = new int[count + len];  // for extra -1
            idx = 0;
            for(int i=0; i < len; i++) {
                int[] face = normal_indexes.get(i);
                for(int j=0; j < face.length; j++) {
                    ret_val.normalIndexes[idx++] = face[j];
                }

                ret_val.normalIndexes[idx++] = -1;
            }
        }

        return ret_val;
    }

    /**
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public boolean parse(URL url, Component parentComponent)
        throws InterruptedIOException, IOException
    {
        InputStream stream = null;
        try
        {
            stream = url.openStream();
        }
        catch(IOException e)
        {
            if(stream != null)
                stream.close();

            throw e;
        }

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

        stream = new ProgressMonitorInputStream (
            parentComponent,
            "parsing " + url.toString(),
            stream);

        reader = new BufferedReader(new InputStreamReader(stream));
        itsReader = reader;

        return true;
    }

    /**
     * @throws InvalidFormatException The file was structurally incorrect
     */
    public boolean parse(URL url)
        throws IOException
    {
        InputStream stream = null;
        try
        {
            stream = url.openStream();
        }
        catch(IOException e)
        {
            if(stream != null)
                stream.close();

            throw e;
        }

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

        for(int i = 0; i < 3; i ++)
        {
            String num_str = strtok.nextToken();

            boolean error_found = false;

            if (num_str == "") {
                // ignore extra spaces
                i = i -1;
                continue;
            }

            try
            {
                vector[i] = Double.parseDouble(num_str);
            }
            catch(NumberFormatException e)
            {
                if (strictParsing)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                       ": Cannot parse vertex: " + num_str;
                    throw new InvalidFormatException(msg);
                } else {
                    // Common error is to use commas instead of . in Europe
                    String new_str = num_str.replace(",",".");

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

        for(int i = 0; i < 3; i ++)
        {
            String num_str = strtok.nextToken();

            boolean error_found = false;

            try
            {
                vector[i] = Double.parseDouble(num_str);
            }
            catch(NumberFormatException e)
            {
                if (strictParsing)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                       ": Cannot parse normal: " + num_str;
                    throw new InvalidFormatException(msg);
                } else {
                    // Common error is to use commas instead of . in Europe
                    String new_str = num_str.replace(",",".");

                    try
                    {
                        vector[i] = Double.parseDouble(new_str);
                    }
                    catch(NumberFormatException e2)
                    {

                        error_found = true;
                    }
                }

                if (error_found == true) {
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

        for(int i = 0; i < 2; i ++)
        {
            String num_str = strtok.nextToken();

            boolean error_found = false;

            try
            {
                vector[i] = Double.parseDouble(num_str);
            }
            catch(NumberFormatException e)
            {
                if (strictParsing)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(INVALID_VERTEX_DATA_MSG_PROP) +
                       ": Cannot parse texture coordinate: " + num_str;
                    throw new InvalidFormatException(msg);
                } else {
                    // Common error is to use commas instead of . in Europe
                    String new_str = num_str.replace(",",".");

                    try
                    {
                        vector[i] = Double.parseDouble(new_str);
                    }
                    catch(NumberFormatException e2)
                    {

                        error_found = true;
                    }
                }

                if (error_found == true) {
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
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<Integer> indices_tc = new ArrayList<Integer>();
        ArrayList<Integer> indices_normals = new ArrayList<Integer>();
        int num_comps = 0;

        while(strtok.hasMoreElements()) {
            String num_str = strtok.nextToken();

            boolean error_found = false;

            if (num_str.indexOf("/") < 0) {
                num_comps = 1;
                try
                {
                    int index = Integer.parseInt(num_str);

                    if (index < 0) {
//System.out.println("Input index: " + index + " coord size: " + coords.size() + " ans: " + (coords.size() - index));

                        // Need to resolve relative index
                        index = coords.size() + index;
                        indices.add(index);
                    } else {
                        indices.add(index - 1); //  Account for weird 1 numbering
                    }
                }
                catch(NumberFormatException e)
                {
                    if (strictParsing)
                    {
                        I18nManager intl_mgr = I18nManager.getManager();

                        String msg = intl_mgr.getString(INVALID_FACE_DATA_MSG_PROP) +
                           ": Cannot parse face: " + num_str;
                        throw new InvalidFormatException(msg);
                    } else {
                        // Common error is to use commas instead of . in Europe
                        String new_str = num_str.replace(",",".");

                        try {
                            int index = Integer.parseInt(new_str);

                            if (index < 0) {
//System.out.println("Input index: " + index + " coord size: " + coords.size() + " ans: " + (coords.size() - index));
                                // Need to resolve relative index
                                index = coords.size() + index;
                            } else {
                                indices.add(index - 1); //  Account for weird 1 numbering
                            }
                        } catch(NumberFormatException e2)
                        {

                            I18nManager intl_mgr = I18nManager.getManager();

                            String msg = intl_mgr.getString(INVALID_FACE_DATA_MSG_PROP) +
                               ": Cannot parse face: " + num_str;
                            throw new InvalidFormatException(msg);
                        }
                    }
                }
            } else {
                // We have multiple indexes
                num_str = num_str.replace("//","/U/");  // denote unspecified
                StringTokenizer toker = new StringTokenizer(num_str, "/");

                num_comps = toker.countTokens();

                num_str = toker.nextToken();

                int index = Integer.parseInt(num_str);

                if (index < 0) {
                    // Need to resolve relative index
                    index = coords.size() - index;
                } else {
                    indices.add(index - 1); //  Account for weird 1 numbering
                }

                if (num_comps >= 2) {
                    num_str = toker.nextToken();

                    if (num_str != null) {
                        if (num_str.equals("U")) {
                            texCoordMissing = true;
                        } else {
                            index = Integer.parseInt(num_str);

                            if (index < 0) {
                                // Need to resolve relative index
                                index = texCoords.size() - index;
                                indices_tc.add(index);
                            } else {
                                indices_tc.add(index - 1); //  Account for weird 1 numbering
                            }
                        }
                    }
                }

                if (num_comps >= 3) {
                    num_str = toker.nextToken();

                    if (num_str.equals("U")) {
                        normalCoordMissing = true;
                    } else {

                        index = Integer.parseInt(num_str);

                        if (num_str != null) {
                            if (index < 0) {
                                // Need to resolve relative index
                                index = normals.size() - index;
                                indices_normals.add(index);
                            } else {
                                indices_normals.add(index - 1); //  Account for weird 1 numbering
                            }
                        }
                    }
                }

            }
        }

        int len = indices.size();
        int[][] ret_val = new int[num_comps][len];
        for(int i=0; i < len; i++) {
            ret_val[0][i] = indices.get(i);
        }

        if (num_comps > 1 && !texCoordMissing) {
            len = indices_tc.size();
            for(int i=0; i < len; i++) {
                ret_val[1][i] = indices_tc.get(i);
            }
        }

        if (num_comps > 2 && !normalCoordMissing) {
            len = indices_normals.size();
            for(int i=0; i < len; i++) {
                ret_val[2][i] = indices_normals.get(i);
            }
        }

        return ret_val;
    }
}
