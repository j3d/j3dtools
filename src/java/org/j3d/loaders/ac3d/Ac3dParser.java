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
 * @version $Revision: 1.3 $
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

    /** The header preamble. */
    public static final String HEADER_PREAMBLE="AC3D";

    /** The latest version of the file format this parser supports. */
    public static final int SUPPORTED_FORMAT_VERSION=0xb;

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


    /** Material token for the base RGB colour */ 
    private static final int RGB_TOKEN = 13;

    /** Material token for the base RGB colour */ 
    private static final int AMBIENT_TOKEN = 14;

    /** Material token for the emissive colour */ 
    private static final int EMISSIVE_TOKEN = 15;

    /** Material token for the specular colour */ 
    private static final int SPECULAR_TOKEN = 16;

    /** Material token for the shininess amount*/ 
    private static final int SHININESS_TOKEN = 17;

    /** Material token for the transparency amount */ 
    private static final int TRANSPARENCY_TOKEN = 18;

    /** Set of keywords and the constants that they map to for fast parsing */
    private static HashMap<String, Integer> keywordsMap;

    /** Where the data comes from. */
    private BufferedReader reader;

    /** List of current materials found during parsing */
    private ArrayList<Ac3dMaterial> materials;

    /** List of current objects found during parsing */
    private ArrayList<Ac3dObject> objects;

    /** Keeps track of parent/child objects as they are declared */
    private Stack<Ac3dObject> objectDefStack;

    /** Parser for individual lines */
    private LineTokenizer lineTokenizer;

    /** Count of the material objects read so far */
    private int materialCount;

    /** Count of the surfaces per object read so far */
    private int surfaceCount;

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
        objectDefStack = new Stack<Ac3dObject>();

        materialCount = 0;
        surfaceCount = -1;
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
                    parseMaterial(tokens);
                    break;

                case OBJECT_TOKEN:
                    parseObject(tokens);
                    break;

                case KIDS_TOKEN:
                    parseKids(tokens);
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
        materialCount = 0;
        surfaceCount = -1;
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

    /**
     * Parse a new material object
     *
     * @param tokens The array of tokens to process
     */
    private void parseMaterial(String[] tokens)
    {
        Ac3dMaterial material = new Ac3dMaterial();
        
        if(tokens.length > 1)
            material.setName(tokens[1]);

        for(int i = 2; i < tokens.length; i++) 
        {
            switch(keywordsMap.get(tokens[i])) 
            {
                case RGB_TOKEN:
                    material.setRGB(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case AMBIENT_TOKEN:
                    material.setAmbient(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case EMISSIVE_TOKEN:
                    material.setEmissive(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case SPECULAR_TOKEN:
                    material.setSpecular(parseFloats(tokens, i + 1, 3));
                    i += 3;
                    break;

                case SHININESS_TOKEN:
                    material.setShininess(parseDecimal(tokens, i + 1));
                    i++;
                    break;

                case TRANSPARENCY_TOKEN:
                    material.setTransparency(parseFloat(tokens, i + 1));
                    i++;
                    break;
            }
        }

        material.setIndex(materialCount);
        materialCount++;

        materials.add(material);
    }

    /**
     * Parse a new model object
     *
     * @param tokens The array of tokens to process
     */
    private void parseObject(String[] tokens)
    {
        Ac3dObject object = new Ac3dObject();

        if(tokens.length == 2) 
        {
            object.setType(tokens[1]);
            objectDefStack.push(object);
            surfaceCount = -1;
        }
        else
            throw new ParsingErrorException(OBJECT_TOKEN_CNT_ERR);
    }

    /**
     * Parse the kids of the current object
     *
     * @param tokens The array of tokens to process
     */
    private void parseKids(String[] tokens)
    {
        Ac3dObject object = qualifyTagByAC3DObject(tokens, 2);
        objects.add(objectDefStack.pop());  
    }

    /**
     * Parse a new surface object
     *
     * @param tokens The array of tokens to process
     */
    private void parseSurface(String[] tokens)
    {
    }

    /**
     * Parse the number of vertices tag, and the following list of vertex values.
     *
     * @param tokens The array of tokens to process
     */
    private void parseVertices(String[] tokens)
    {
        Ac3dObject object = qualifyTagByAC3DObject(tokens, 2);
    }

    /**
     * Parse the object name
     *
     * @param tokens The array of tokens to process
     */
    private void parseName(String[] tokens)
    {
        Ac3dObject object = qualifyTagByAC3DObject(tokens, 2);
        object.setName(tokens[1]);
    }

    /**
     * Helper function that qualifies a tag by whether or not its parent
     * should be an instance of AC3DObject or not, as well as
     * the number of arguements for the command.
     *
     * @param tokens All of the tokens for the command.
     * @param numArgsRequired The number of arguements for the token
     *                        command.
     * @return The Object from the stack.
     */
    private Ac3dObject qualifyTagByAC3DObject(String[] tokens,
                                              int numArgsRequired)
        throws ParsingErrorException
    {
        if(tokens.length != numArgsRequired)
        {
            throw new ParsingErrorException("Wrong number of args for " +
                tokens[0] + "; expecting " + numArgsRequired + ", got " +
                tokens.length);
        }

        if(objectDefStack.size() == 0) 
            throw new ParsingErrorException("Parent not found on stack!");

        Ac3dObject tmpObj = objectDefStack.peek();
        if(!(tmpObj instanceof Ac3dObject))
        {
            throw new ParsingErrorException("Was expecting: \"" +
                "Ac3dObject" + ", instead got: \"" +
                tmpObj.getClass().getName() + "\".");
        }

        return (Ac3dObject)tmpObj;
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
        float[] rVal = new float[num];
    
        for (int i = 0; i < num; i++) 
        {
            rVal[i] = parseFloat(in[offset+i]);
        }

        return rVal;
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
        return (Integer.valueOf(in)).intValue();
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
        if (in.startsWith("0x") || in.startsWith("0X"))
        {
            in=in.substring(2, in.length());
        }

        return (Integer.valueOf(in, 16)).intValue();
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
        
        return Float.valueOf(in).floatValue();
    }
}
