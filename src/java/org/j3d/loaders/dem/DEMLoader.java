/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.dem;

// Standard imports
import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleStripArray;

import javax.vecmath.Point2d;

import com.sun.j3d.loaders.LoaderBase;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;

// Application specific imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;
import org.j3d.loaders.HeightMapLoader;

/**
 * Loader for the VTerrain Project's BT file format.
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
 * The definition of the file format can be found at:
 * <a href="http://www.vterrain.org/Implementation/BT.html">
 *  http://www.vterrain.org/Implementation/BT.html
 * </a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class DEMLoader extends HeightMapLoader
{
    /** Current parser */
    private DEMParser parser;

    /** Generator of the grid structure for the geometry */
    private ElevationGridGenerator generator;

    /**
     * Construct a new default loader with no flags set
     */
    public DEMLoader()
    {
    }

    /**
     * Construct a new loader with the given flags set.
     *
     * @param flags The list of flags to be set
     */
    public DEMLoader(int flags)
    {
        super(flags);
    }

    /**
     * Load the scene from the given reader. Always throws an exception as the
     * file format is binary only and readers don't handle this.
     *
     * @param reader The source of input characters
     * @return A description of the scene
     * @throws IncorrectFormatException The file is binary
     */
    public Scene load(java.io.Reader reader)
        throws IncorrectFormatException
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


        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS |
                     GeometryArray.TEXTURE_COORDINATE_2;

        TriangleStripArray geom =
            new TriangleStripArray(data.vertexCount,
                                   format,
                                   data.stripCounts);

        geom.setCoordinates(0, data.coordinates);
        geom.setNormals(0, data.normals);
        geom.setTextureCoordinates(0, data.textureCoordinates);

        Appearance app = new Appearance();

        Shape3D shape = new Shape3D(geom, app);

        root_group.addChild(shape);
        scene.setSceneGroup(root_group);

        return scene;
    }

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
    public Point2d getGridStep()
    {
        return parser.getGridStep();
    }
}
