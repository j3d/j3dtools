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
import java.util.Stack;

// Local imports
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.ParsingErrorException;
import org.j3d.loaders.UnsupportedFormatException;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

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
 * @version $Revision: 1.11 $
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

    /** Message for a badly formatted OBJECT definition */
    private static final String OBJECT_TOKEN_CNT_ERR =
        "The OBJECT token contains either 1 or more than 2 tokens";

    /** Message for a badly formatted kid definition */
    private static final String INVALID_OBJECT_KID_ERR =
        "When parsing a child OBJECT of another OBJECT an invalid " +
        "definition was found: ";

    /** Message for a badly formatted SURF definition */
    private static final String INVALID_SURFACE_TOKEN_ERR =
        "When parsing a child SURFACE of an OBJECT an invalid " +
        "definition was found";


    /**
     * Message when the user code generates an exception in observer callback
     * for material definitions.
     */
    private static final String USER_MATERIAL_ERR =
        "User code implementing Ac3dParseObserver has generated an exception " +
        "during the materialComplete() callback.";

    /**
     * Message when the user code generates an exception in observer callback
     * for object definitions.
     */
    private static final String USER_OBJECT_ERR =
        "User code implementing Ac3dParseObserver has generated an exception " +
        "during the objectComplete() callback.";

    /**
     * Message when the user code generates an exception in observer callback
     * for surface definitions.
     */
    private static final String USER_SURFACE_ERR =
        "User code implementing Ac3dParseObserver has generated an exception " +
        "during the surfaceComplete() callback.";



    /** The latest version of the file format this parser supports. */
    public static final int SUPPORTED_FORMAT_VERSION=0xb;

    /** The header preamble. */
    private static final String HEADER_PREAMBLE="AC3D";

    /** File format token for a material definition */
    private static final int MATERIAL_TOKEN = 1;

    /** File format token for a object definition */
    private static final int OBJECT_TOKEN = 2;

    /** File format token for the object's children */
    private static final int KIDS_TOKEN = 3;

    /** File format token for the number of vertices */
    private static final int NUMVERT_TOKEN = 4;

    /** File format token for a object  name */
    private static final int NAME_TOKEN = 5;

    /** File format token for a location declaration  */
    private static final int LOCATION_TOKEN = 6;

    /** File format token for a object rotation  declaration */
    private static final int ROTATION_TOKEN = 7;

    /** File format token for number of surfaces */
    private static final int NUMSURF_TOKEN = 8;

    /** File format token for a surface definition */
    private static final int SURF_TOKEN = 9;

    /** File format token for a object reference */
    private static final int REFS_TOKEN = 10;

    /** File format token for a material use declaration */
    private static final int MAT_TOKEN = 11;

    /** File format token for a texture name definition */
    private static final int TEXTURE_TOKEN = 12;

    /** File format token for a texture name definition */
    private static final int TEXTURE_REPEAT_TOKEN = 13;

    /** File format token for object data */
    private static final int DATA_TOKEN = 14;

    /** File format token for the object's URL */
    private static final int URL_TOKEN = 15;

    /** File format token for the object's crease angle */
    private static final int CREASE_TOKEN = 16;




    /** Material token for the base RGB colour */
    private static final int RGB_TOKEN = 17;

    /** Material token for the base RGB colour */
    private static final int AMBIENT_TOKEN = 18;

    /** Material token for the emissive colour */
    private static final int EMISSIVE_TOKEN = 19;

    /** Material token for the specular colour */
    private static final int SPECULAR_TOKEN = 20;

    /** Material token for the shininess amount*/
    private static final int SHININESS_TOKEN = 21;

    /** Material token for the transparency amount */
    private static final int TRANSPARENCY_TOKEN = 22;

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

    /** Count of the material objects read so far */
    private int materialCount;

    /** The observer of updates, if registered */
    private Ac3dParseObserver observer;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

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
        keywordsMap.put("data", DATA_TOKEN);
        keywordsMap.put("texrep", TEXTURE_REPEAT_TOKEN);
        keywordsMap.put("url", URL_TOKEN);
        keywordsMap.put("crease", CREASE_TOKEN);

        keywordsMap.put("rgb", RGB_TOKEN);
        keywordsMap.put("amb", AMBIENT_TOKEN);
        keywordsMap.put("emis", EMISSIVE_TOKEN);
        keywordsMap.put("spec", SPECULAR_TOKEN);
        keywordsMap.put("shi", SHININESS_TOKEN);
        keywordsMap.put("trans", TRANSPARENCY_TOKEN);
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

        materialCount = 0;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
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
     * Register an error reporter with the engine so that any errors generated
     * by the parsing internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Set the observer for parsing events that can be used with this class.
     * Only a single instance may be set at any time, so calling this will
     * replace the currently registered instance. If called with a null value,
     * it removes the currently set instance.
     *
     * @param obs The observer instance to use
     */
    public void setParseObserver(Ac3dParseObserver obs)
    {
        observer = obs;
    }

    /**
     * Performs the action of parsing the data stream already set.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     * @throws InvalidFormatException The file format does not match the
     *   expected format for AC3D
     * @throws UnsupportedFormatException The format provided is later version
     *   than what we currently support
     * @throws IOException An I/O error occurred while processing the file
     */
    public void parse(boolean retainData) throws IOException
    {
        // Deal with header
        checkHeader();

        // Read the token input line by line
        String buffer;
        boolean forced_end = false;

        while((buffer = reader.readLine()) != null && !forced_end)
        {
            String[] tokens = lineTokenizer.enumerateTokens(buffer);

            int token_id = keywordsMap.get(tokens[0]);
            switch(token_id)
            {
                case MATERIAL_TOKEN:
                    forced_end = parseMaterial(tokens, retainData);
                    break;

                case OBJECT_TOKEN:
                    forced_end = parseObject(null, tokens, retainData);
                    break;
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
        materialCount = 0;
    }

    /**
     * Fetch the materials from the previously loaded file. If nothing
     * has been loaded yet, this will return a zero length array. If the
     * retainData flag was set to false on the parse() method, this will
     * return a zero length array.
     *
     * @return An array of the materials found
     */
    public Ac3dMaterial[] getMaterials()
    {
        return materials.toArray(new Ac3dMaterial[0]);
    }

    /**
     * Fetch the objects from the previously loaded file. If nothing
     * has been loaded yet, this will return a zero length array. If the
     * retainData flag was set to false on the parse() method, this will
     * return a zero length array.
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

    /**
     * Parse a new material object
     *
     * @param tokens The array of tokens to process
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     * @return true if the parsing has been requested to end now
     */
    private boolean parseMaterial(String[] tokens, boolean retainData)
    {
        Ac3dMaterial material = new Ac3dMaterial();

        if(tokens.length > 1)
            material.setName(tokens[1]);

        for(int i = 2; i < tokens.length; i++)
        {
            switch(keywordsMap.get(tokens[i]))
            {
                case RGB_TOKEN:
                    material.setRGBColor(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case AMBIENT_TOKEN:
                    material.setAmbientColor(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case EMISSIVE_TOKEN:
                    material.setEmissiveColor(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case SPECULAR_TOKEN:
                    material.setSpecularColor(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case SHININESS_TOKEN:
                    material.setShininess(parseFloat(tokens[i + 1]));
                    i++;
                    break;

                case TRANSPARENCY_TOKEN:
                    material.setTransparency(parseFloat(tokens[i + 1]));
                    i++;
                    break;
            }
        }

        material.setIndex(materialCount);
        materialCount++;

        if(retainData)
            materials.add(material);

        boolean ret_val = false;

        if(observer != null)
        {
            try
            {
                ret_val = !observer.materialComplete(material);
            }
            catch(Exception e)
            {
                errorReporter.errorReport(USER_MATERIAL_ERR, e);
            }
        }

        return ret_val;
    }

    /**
     * Parse a new model object
     *
     * @param parent The parent object to this one, or null if at the
     *   root level
     * @param tokens The array of tokens to process
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     * @return true if the parsing has been requested to end now
     */
    private boolean parseObject(Ac3dObject parent,
                                String[] tokens,
                                boolean retainData)
        throws IOException
    {
        if(tokens.length != 2)
            throw new ParsingErrorException(OBJECT_TOKEN_CNT_ERR);

        Ac3dObject object = new Ac3dObject();

        object.setType(tokens[1]);

        if(parent != null)
            parent.addChild(object);
        else if(retainData)
            objects.add(object);

        String buffer;
        boolean forced_end = false;
        boolean object_complete = false;

        while(!forced_end && !object_complete &&
              (buffer = reader.readLine()) != null)
        {
            String[] kid_tokens = lineTokenizer.enumerateTokens(buffer);

            Integer tk_val = keywordsMap.get(kid_tokens[0]);

            // check for a keyword we know about. If not, ignore this line
            // and move on
            if(tk_val == null)
            {
                String msg = "Unknown object token encountered in file: " +
                             kid_tokens[0];

                errorReporter.warningReport(msg, null);
                continue;
            }

            switch(tk_val.intValue())
            {
                case KIDS_TOKEN:
                    if(observer != null)
                    {
                        try
                        {
                            forced_end =
                                !observer.objectComplete(parent, object);
                        }
                        catch(Exception e)
                        {
                            errorReporter.errorReport(USER_SURFACE_ERR, e);
                        }
                    }

                    if(!forced_end)
                        forced_end = parseKids(object, kid_tokens, retainData);
                    object_complete = true;

                    break;

                case NUMVERT_TOKEN:
                    parseVertices(object, kid_tokens);
                    break;

                case NAME_TOKEN:
                    parseName(object, kid_tokens);
                    break;

                case LOCATION_TOKEN:
                    parseLocation(object, kid_tokens);
                    break;

                case ROTATION_TOKEN:
                    parseRotation(object, kid_tokens);
                    break;

                case NUMSURF_TOKEN:
                    forced_end = parseNumSurfaces(object, kid_tokens);
                    break;

                case TEXTURE_TOKEN:
                    parseTexture(object, kid_tokens);
                    break;

                case TEXTURE_REPEAT_TOKEN:
                    parseTextureRepeat(object, kid_tokens);
                    break;

                case DATA_TOKEN:
                    parseData(object, kid_tokens);
                    break;

                case URL_TOKEN:
                    parseURL(object, kid_tokens);
                    break;

                case CREASE_TOKEN:
                    parseCreaseAngle(object, kid_tokens);
                    break;

                default:
                    // Issue message here
            }
        }

        return forced_end;
    }

    /**
     * Parse the kids of the current object
     *
     * @param tokens The array of tokens to process
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     * @return true if the parsing has been requested to end now
     */
    private boolean parseKids(Ac3dObject object,
                              String[] tokens,
                              boolean retainData)
        throws IOException
    {
        qualifyTagByAC3DObject(tokens, 2);

        int num_kids = parseDecimal(tokens[1]);
        boolean forced_end = false;

        for(int i = 0; i < num_kids && !forced_end; i++)
        {
            String buffer = reader.readLine();
            String[] kid_tokens = lineTokenizer.enumerateTokens(buffer);

            int token_id = keywordsMap.get(kid_tokens[0]);

            if(token_id != OBJECT_TOKEN)
            {
                String msg = INVALID_OBJECT_KID_ERR + kid_tokens[0];
                throw new ParsingErrorException(msg);
            }

            forced_end = parseObject(object, kid_tokens, retainData);
        }

        return forced_end;
    }

    /**
     * Parse the kids of the current object
     *
     * @param tokens The array of tokens to process
     */
    private void parseTextureRepeat(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 2);

        object.setTextureRepeat(parseFloats(tokens, 0, 2));
    }

    /**
     * Parse the kids of the current object
     *
     * @param tokens The array of tokens to process
     */
    private void parseData(Ac3dObject object, String[] tokens)
        throws IOException
    {
        qualifyTagByAC3DObject(tokens, 2);
        int num_chars = parseDecimal(tokens[1]);

        char[] data_chars = new char[num_chars];

        reader.read(data_chars, 0, num_chars);
        // read an extra char for end of line. Use readLine() rather than
        // read() so that it picks up all forms of <CR><LF> combos.
        reader.readLine();
        String line = new String(data_chars);

        object.setData(line);
    }

    /**
     * Parse the kids of the current object
     *
     * @param tokens The array of tokens to process
     */
    private void parseURL(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 1);
        object.setURL(tokens[1]);
    }

    /**
     * Parse the number of surfaces token definition.
     *
     * @param tokens The array of tokens to process
     * @return true if the parsing has been requested to end now
     */
    private void parseCreaseAngle(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 2);
        object.setCreaseAngle(parseFloat(tokens[1]));
    }

    /**
     * Parse the number of surfaces token definition.
     *
     * @param tokens The array of tokens to process
     * @return true if the parsing has been requested to end now
     */
    private boolean parseNumSurfaces(Ac3dObject object, String[] tokens)
        throws IOException
    {
        qualifyTagByAC3DObject(tokens, 2);

        int num_surfaces = parseDecimal(tokens[1]);

        boolean forced_end = false;

        for(int i = 0; i < num_surfaces && !forced_end; i++)
            forced_end = parseSurface(object);

        return forced_end;
    }

    /**
     * Parse a new surface object
     *
     * @param tokens The array of tokens to process
     * @return true if the parsing has been requested to end now
     */
    private boolean parseSurface(Ac3dObject object)
        throws IOException
    {
        String buffer = reader.readLine();
        String[] tokens = lineTokenizer.enumerateTokens(buffer);

        int token_id = keywordsMap.get(tokens[0]);

        if(token_id != SURF_TOKEN)
            throw new ParsingErrorException(INVALID_SURFACE_TOKEN_ERR);

        Ac3dSurface surface = new Ac3dSurface();
        surface.setFlags(parseHexidecimal(tokens[1]));

        buffer = reader.readLine();
        tokens = lineTokenizer.enumerateTokens(buffer);

        Integer tk_val = keywordsMap.get(tokens[0]);

        // check for a keyword we know about. If not, ignore this line
        // and move on
        if(tk_val == null)
        {
            String msg = "Unknown surface token encountered in file: " +
                         tokens[0];

            errorReporter.warningReport(msg, null);
            return true;
        }

        parseMaterialRef(surface, tokens);

        // Now red the second line, which will be index refrences
        buffer = reader.readLine();
        tokens = lineTokenizer.enumerateTokens(buffer);

        tk_val = keywordsMap.get(tokens[0]);

        // check for a keyword we know about. If not, ignore this line
        // and move on
        if(tk_val == null)
        {
            String msg = "Unknown surface token encountered in file: " +
                         tokens[0];

            errorReporter.warningReport(msg, null);
            return true;
        }

        parseSurfaceRefs(surface, tokens);

        object.addSurface(surface);

        boolean ret_val = false;

        if(observer != null)
        {
            try
            {
                ret_val = !observer.surfaceComplete(object, surface);
            }
            catch(Exception e)
            {
                errorReporter.errorReport(USER_SURFACE_ERR, e);
            }
        }

        return ret_val;
    }

    /**
     * Parse the number of vertices tag, and the following list of vertex values.
     *
     * @param tokens The array of tokens to process
     */
    private void parseVertices(Ac3dObject object, String[] tokens)
        throws IOException
    {
        qualifyTagByAC3DObject(tokens, 2);

        int num_vertices = parseDecimal(tokens[1]);
        object.setNumvert(num_vertices);

        for(int i = 0; i < num_vertices; i++)
        {
            String line = reader.readLine();
            String[] ref = lineTokenizer.enumerateTokens(line);
            object.addVertex(i, parseFloats(ref, 0, 3));
        }
    }

    /**
     * Parse the object name
     *
     * @param tokens The array of tokens to process
     */
    private void parseName(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 2);
        object.setName(tokens[1]);
    }

    /**
     * Parse a new texture object
     *
     * @param tokens The array of tokens to process
     */
    private void parseTexture(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 2);
        object.setTexture(tokens[1]);
    }

    /**
     * Parse the rotation definition
     *
     * @param tokens The array of tokens to process
     */
    private void parseRotation(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 10);
        object.setRotation(parseFloats(tokens, 1, 9));
    }

    /**
     * Parse the location definition
     *
     * @param tokens The array of tokens to process
     */
    private void parseLocation(Ac3dObject object, String[] tokens)
    {
        qualifyTagByAC3DObject(tokens, 4);
        object.setLocation(parseFloats(tokens, 1, 3));
    }

    /**
     * Parse a reference to a material object
     *
     * @param surface The surface to put the indices in
     * @param tokens The array of tokens to process
     */
    private void parseMaterialRef(Ac3dSurface surface, String[] tokens)
    {
        surface.setMaterial(parseDecimal(tokens[1]));
    }

    /**
     * Parse the references to surface vertex indices for an object
     *
     * @param surface The surface to put the indices in
     * @param tokens The array of tokens to process
     */
    private void parseSurfaceRefs(Ac3dSurface surface, String[] tokens)
        throws IOException
    {
        int num_refs = parseDecimal(tokens[1]);
        surface.setNumrefs(num_refs);

        for (int i = 0; i < num_refs; i++)
        {
            String line = reader.readLine();
            String[] ref = lineTokenizer.enumerateTokens(line);
            surface.addRef(i, parseDecimal(ref[0]), parseFloats(ref, 1, 2));
        }
    }

    /**
     * Helper function that qualifies a tag by whether or not its parent
     * should be an instance of AC3DObject or not, as well as
     * the number of arguements for the command.
     *
     * @param tokens All of the tokens for the command.
     * @param numArgsRequired The number of arguements for the token
     *                        command.
     */
    private void qualifyTagByAC3DObject(String[] tokens,
                                        int numArgsRequired)
        throws ParsingErrorException
    {
        if(tokens.length != numArgsRequired)
        {
            throw new ParsingErrorException("Wrong number of args for " +
                tokens[0] + "; expecting " + numArgsRequired + ", got " +
                tokens.length);
        }
    }

    /**
     * <p>Helper function to parse a number of <code>float</code> strings
     * into an array.</p>
     *
     * @param in The list of strings to parse.
     * @param offset The starting position of the floats to extract.
     * @param num The number of floats to extract from the starting position.
     * @return The array of parsed floats.
     */
    private float[] parseFloats(String[] in, int offset, int num)
    {
        float[] ret_val = new float[num];

        for(int i = 0; i < num; i++)
        {
            ret_val[i] = parseFloat(in[offset+i]);
        }

        return ret_val;
    }

    /**
     * <p>Helper function to parse a decimal value into an <code>int</code>.
     * The method definition should present this method as a candidate for
     * inlining by an optimizing compiler, since it is statically
     * resolvable.</p>
     *
     * @param in The <code>String</code> to convert into an <code>int</code>.
     * @return The converted <code>int</code>.
     */
    private int parseDecimal(String in)
    {
        return Integer.valueOf(in);
    }

    /**
     * <p>Helper function to convert a decimal presented in hex to an int.
     * The method definition should present this method as a candidate for
     * inlining by an optimizing compiler, since it is statically
     * resolvable.</p>
     *
     * @param in The <code>String</code> to convert.
     * @return The converted <code>int</code>.
     */
    private int parseHexidecimal(String in)
    {
        if(in.startsWith("0x") || in.startsWith("0X"))
        {
            in = in.substring(2, in.length());
        }

        return Integer.valueOf(in, 16);
    }

    /**
     * <p>Helper function to parse a decimal value into a <code>float</code>.
     * The method definition should present this method as a candidate for
     * inlining by an optimizing compiler, since it is statically
     * resolvable.</p>
     *
     * @param in The <code>String</code> to convert into a <code>float</code>.
     * @return The converted <code>float</code>.
     */
    private static final float parseFloat(String in)
    {
        if(in.indexOf(".") < 0)
        {
            in+=".0";
        }

        return Float.valueOf(in);
    }
}
