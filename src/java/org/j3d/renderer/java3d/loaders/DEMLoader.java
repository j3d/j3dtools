/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.loaders;

// Standard imports
import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Collections;
import java.util.Map;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.TriangleStripArray;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;

// Application specific imports
import org.j3d.loaders.dem.*;

import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;

/**
 * Loader for the USGS DEM file format.
 * <p>
 *
 * The mesh produced is, by default, triangle strip arrays. The X axis
 * represents East-West and the Z-axis represents North-South. +X is east,
 * -Z is North. Texture coordinates are generated for the extents based on
 * a single 0-1 scale for the width of the object.
 * <p>
 *
 * The loader produces a single mesh that represents the file's contents. No
 * further processing is performed in the current implementation to break the
 * points into smaller tiles or use multi-resolution terrain structures.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class DEMLoader extends HeightMapLoader
    implements BinaryLoader, ManagedLoader
{
    //
    // Impl note. Because this loader just builds static arrays of data for
    // rendering, it never sets its own capability bits. Therefore, required
    // and override bits are treated identically.
    //

    /** Current parser */
    private DEMParser parser;

    /** Generator of the grid structure for the geometry */
    private ElevationGridGenerator generator;

    /** The map of the override capability bit settings */
    private Map overrideCapBitsMap;

    /** The map of the required capability bit settings */
    private Map requiredCapBitsMap;

    /** The map of the override capability bit settings */
    private Map overrideFreqBitsMap;

    /** The map of the required capability bit settings */
    private Map requiredFreqBitsMap;

    /** Flag for the API being new enough to have frquency bit setting */
    private final boolean haveFreqBitsAPI;

    /**
     * Construct a new default loader with no flags set
     */
    public DEMLoader()
    {
        this(0);
    }

    /**
     * Construct a new loader with the given flags set.
     *
     * @param flags The list of flags to be set
     */
    public DEMLoader(int flags)
    {
        super(flags);

        Boolean bool = (Boolean)AccessController.doPrivileged
        (
            new PrivilegedAction()
            {
                public Object run()
                {
                    try
                    {
                        Class cls =
                            Class.forName("javax.media.j3d.SceneGraphObject");
                        Package pkg = cls.getPackage();

                        return new Boolean(pkg.isCompatibleWith("1.3"));
                    }
                    catch(ClassNotFoundException cnfe)
                    {
                        return Boolean.FALSE;
                    }
                }
            }
        );

        haveFreqBitsAPI = bool.booleanValue();
    }

    //----------------------------------------------------------
    // Methods defined in ManagedLoader
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits)
    {
        overrideCapBitsMap = capBits;
        overrideFreqBitsMap = freqBits;
    }


    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits)
    {
        requiredCapBitsMap = capBits;
        requiredFreqBitsMap = freqBits;
    }


    //----------------------------------------------------------
    // Methods defined in BinaryLoader
    //----------------------------------------------------------

    /**
     * Load the scene from the given reader. Always throws an exception as the
     * file format is binary only and readers don't handle this.
     *
     * @param is The source of input characters
     * @return A description of the scene
     * @throws IncorrectFormatException The file is binary
     */
    public Scene load(InputStream is)
        throws IncorrectFormatException,
               ParsingErrorException
    {
        return loadInternal(is);
    }

    //----------------------------------------------------------
    // Methods defined in Loader
    //----------------------------------------------------------

    /**
     * Load the scene from the given reader. Always throws an exception as the
     * file format is binary only and readers don't handle this.
     *
     * @param reader The source of input characters
     * @return A description of the scene
     * @throws IncorrectFormatException The file is binary
     */
    public Scene load(java.io.Reader reader)
        throws IncorrectFormatException,
               ParsingErrorException
    {
        return loadInternal(reader);
    }


    /**
     * Load a scene from the given filename. The scene instance returned by
     * this loader will have textures already loaded.
     *
     * @param filename The name of the file to load
     * @return A description of the scene
     * @throws FileNotFoundException The reader can't find the file
     * @throws IncorrectFormatException The file is not one our loader
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public Scene load(String filename)
        throws FileNotFoundException,
               IncorrectFormatException,
               ParsingErrorException
    {
        File file = new File(filename);
        InputStream input = null;

        if(!file.exists())
            throw new FileNotFoundException("File does not exist");

        if(file.isDirectory())
            throw new FileNotFoundException("File is a directory");

        FileInputStream fis = new FileInputStream(file);
        input = new BufferedInputStream(fis);

        return loadInternal(input);
    }

    /**
     * Load a scene from the named URL. The scene instance returned by
     * this loader will have textures already loaded.
     *
     * @param url The URL instance to load data from
     * @return A description of the scene
     * @throws FileNotFoundException The reader can't find the file
     * @throws IncorrectFormatException The file is not one our loader
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public Scene load(URL url)
        throws FileNotFoundException,
               IncorrectFormatException,
               ParsingErrorException
    {

        InputStream input = null;

        try
        {
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();

            if(is instanceof BufferedInputStream)
                input = (BufferedInputStream)is;
            else
                input = new BufferedInputStream(is);
        }
        catch(IOException ioe)
        {
            throw new FileNotFoundException(ioe.getMessage());
        }

        return loadInternal(input);
    }

    //----------------------------------------------------------
    // Methods defined in HeightMapLoader
    //----------------------------------------------------------

    /**
     * Return the height map created for the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The array of heights in [row][column] order or null
     */
    public float[][] getHeights()
    {
        return parser.getHeights();
    }

    /**
     * Fetch information about the real-world stepping sizes that this
     * grid uses.
     *
     * @return The stepping information for width and depth
     */
    public float[] getGridStep()
    {
        return parser.getGridStep();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the header used to describe the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The header for the last read stream or null
     */
    public DEMTypeARecord getTypeARecord()
    {
        return parser.getTypeARecord();
    }

    /**
     * Fetch all of the type B records that were registered in this file.
     * Will probably contain more than one record and is always non-null.
     * The records will be in the order they were read from the file.
     *
     * @return The list of all the Type B records parsed
     */
    public DEMTypeBRecord[] getTypeBRecords()
    {
        return parser.getTypeBRecords();
    }

    /**
     * Get the type C record from the file. If none was provided, then this
     * will return null.
     *
     * @return The type C record info or null
     */
    public DEMTypeCRecord getTypeCRecord()
    {
        return parser.getTypeCRecord();
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Do all the parsing work for an inputstream. Convenience method
     * for all to call internally
     *
     * @param is The inputsource for this reader
     * @return The scene description
     * @throws IncorrectFormatException The file is not one our loader
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    private Scene loadInternal(InputStream str)
        throws IncorrectFormatException,
               ParsingErrorException
    {
        if(parser == null)
            parser = new DEMParser(str);
        else
            parser.reset(str);

        return load();
    }

    /**
     * Do all the parsing work for a reader. Convenience method
     * for all to call internally
     *
     * @param is The inputsource for this reader
     * @return The scene description
     * @throws IncorrectFormatException The file is not one our loader
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    private Scene loadInternal(Reader rdr)
        throws IncorrectFormatException,
               ParsingErrorException
    {
        if(parser == null)
            parser = new DEMParser(rdr);
        else
            parser.reset(rdr);

        return load();
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return The scene description
     * @throws IncorrectFormatException The file is not one our loader
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    private Scene load()
        throws IncorrectFormatException,
               ParsingErrorException
    {
        float[][] heights = null;

        try
        {
            heights = parser.parse(true);
        }
        catch(IOException ioe)
        {
            throw new ParsingErrorException("Error parsing stream: " + ioe);
        }

        DEMTypeARecord header = parser.getTypeARecord();

        float width =
            (float)(heights[0].length * header.spatialResolution[DEMRecord.X]);

        float depth =
            (float)(heights.length * header.spatialResolution[DEMRecord.Y]);

        if(generator == null)
        {
            generator = new ElevationGridGenerator(width,
                                                   depth,
                                                   heights[0].length,
                                                   heights.length,
                                                   heights,
                                                   0);
        }
        else
        {
            generator.setDimensions(width, depth, heights[0].length, heights.length);
            generator.setTerrainDetail(heights, 0);
        }

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        // So that passed, well let's look at the building the scene now. All
        // we need to do is create a single big tri-strip array based on the
        // points.
        //
        // In a later variant, we may want to look at dividing this up into
        // collections of points dependent on the culling algorithm we are
        // going to use or to generate multi-resolution terrains.
        //
        // At some stage, we should use the HeightMapGenerator so that we
        // can throw the GeometryData into the ITSA for the collision and
        // terrain following code.
        SceneBase scene = new SceneBase();
        BranchGroup root_group = new BranchGroup();

        setCapBits(root_group);
        setFreqBits(root_group);

        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS |
                     GeometryArray.TEXTURE_COORDINATE_2;

        TriangleStripArray geom =
            new TriangleStripArray(data.vertexCount,
                                   format,
                                   data.stripCounts);

        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);
        geom.setTextureCoordinates(0, 0, data.textureCoordinates);

        Appearance app = new Appearance();

        setCapBits(app);
        setFreqBits(app);

        Shape3D shape = new Shape3D(geom, app);

        setCapBits(shape);
        setFreqBits(shape);

        root_group.addChild(shape);
        scene.setSceneGroup(root_group);

        return scene;
    }

    /**
     * Set the frequency bits on this scene graph object according to the
     * pre-set settings.
     *
     * @param sgo The j3d node to set the capabilities on
     */
    private void setCapBits(SceneGraphObject sgo)
    {
        Class cls = sgo.getClass();
        Map bits_map = Collections.EMPTY_MAP;

        if(overrideCapBitsMap != null)
            bits_map = overrideCapBitsMap;
        else if(requiredCapBitsMap != null)
            bits_map = requiredCapBitsMap;

        int[] bits = (int[])bits_map.get(cls);

        int size = (bits == null) ? 0 : bits.length;

        for(int i = 0; i < size; i++)
            sgo.setCapability(bits[i]);
    }

    /**
     * Set the frequency bits on this scene graph object according to the
     * pre-set settings. If the API version is < 1.3 then this method returns
     * immediately.
     *
     * @param sgo The j3d node to set the capabilities on
     */
    private void setFreqBits(SceneGraphObject sgo)
    {
        if(!haveFreqBitsAPI)
            return;

        Class cls = sgo.getClass();
        Map bits_map = Collections.EMPTY_MAP;

        if(overrideFreqBitsMap != null)
            bits_map = overrideFreqBitsMap;
        else if(requiredFreqBitsMap != null)
            bits_map = requiredFreqBitsMap;

        int[] bits = (int[])bits_map.get(cls);

        int size = (bits == null) ? 0 : bits.length;

        for(int i = 0; i < size; i++)
            sgo.setCapabilityIsFrequent(bits[i]);
    }
}
