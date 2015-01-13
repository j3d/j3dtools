/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

// Local imports
import org.j3d.io.BlockDataInputStream;

/**
 * A low-level parser for the Discreet 3DS Max file format.
 * <p>
 *
 * The definition this works from is can be found at:
 * http://www.the-labs.com/Blender/3DS-details.html
 * http://www.spacesimulator.net/tut4_3dsloader.html
 *
 * @author  Justin Couch
 * @version $Revision: 1.9 $
 */
public class MaxParser
{
    /**
     * The stream used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private BlockDataInputStream inputStream;

    /** Flag to say we've already read the stream */
    private boolean dataReady;

    /** The mesh that was last parsed */
    private ObjectMesh decodedMesh;

    /** The version of the mesh structures we're reading */
    private int releaseVersion;

    /**
     * Construct a new parser with no stream set.
     */
    public MaxParser()
    {
        dataReady = false;
    }

    /**
     * Construct a new parser using the given stream to source the data from.
     *
     * @param is The stream to read data from
     */
    public MaxParser(InputStream is)
    {
        this();

        BufferedInputStream stream;

        if(is instanceof BufferedInputStream)
            stream = (BufferedInputStream)is;
        else
            stream = new BufferedInputStream(is);

        inputStream = new BlockDataInputStream(stream);
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param is The new stream to use
     */
    public void reset(InputStream is)
    {
        BufferedInputStream stream;

        if(is instanceof BufferedInputStream)
            stream = (BufferedInputStream)is;
        else
            stream = new BufferedInputStream(is);

        inputStream = new BlockDataInputStream(stream);

        decodedMesh = null;
        dataReady = false;
    }

    /**
     * Get the last decoded mesh. If the stream has been reset or hasn't been
     * parsed for the first time, the method returns null.
     *
     * @return The last decoded mesh instance or null
     */
    public ObjectMesh getObjectMesh()
    {
        return decodedMesh;
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @return A completed object mesh representative of the file
     */
    public ObjectMesh parse()
        throws IOException
    {
        if(dataReady)
            throw new IOException("Data has already been read from this stream");

        parseMain();

        dataReady = true;

        return decodedMesh;
    }

    /**
     * Parse the main chunk of the file now.
     */
    private void parseMain()
        throws IOException
    {
        int type = readUnsignedShort();
        int size = readInt();

        if(type != MaxConstants.MAIN_CHUNK)
            throw new IOException("Wrong main chunk in file");

        ObjectMesh mesh = new ObjectMesh();
        readMain(size - 6, mesh);

        decodedMesh = mesh;
    }

    /**
     * Process all the object meshes in the file.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
     */
    private void readMain(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.VERSION:
                    data.meshVersion = readInt();
                    releaseVersion = data.meshVersion;
                    break;

                case MaxConstants.MESH_DATA:
                    readMeshChunk(size - 6, data);
                    break;

                case MaxConstants.KEYFRAME_CHUNK:
                    readKeyframeChunk(size - 6, data);
                    break;

                default:
                    System.out.println("Unknown top-level block chunk ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for main mesh. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Process all the object meshes in the file.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
     */
    private void readMeshChunk(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.NAMED_OBJECT:
                    readObjectBlock(size - 6, data);
                    break;

                case MaxConstants.MATERIAL_BLOCK:
                    readMaterialBlock(size - 6, data);
                    break;

                case MaxConstants.MASTER_SCALE:
                    data.masterScale = readFloat();
                    break;

                case MaxConstants.MESH_VERSION:
                    data.meshVersion = readInt();
                    break;

                case MaxConstants.AMBIENT_LIGHT:
                    data.ambientLight = new float[3];
                    int read = readColor(data.ambientLight);
                    if(size - read - 6 != 0)
                        skipBytes(size - read - 6);
                    break;

                // Deliberately ignored
                case MaxConstants.LOW_SHADOW_BIAS:
                case MaxConstants.HI_SHADOW_BIAS:
                case MaxConstants.SHADOW_MAP_SIZE:
                case MaxConstants.SHADOW_MAP_SAMPLES:
                case MaxConstants.SHADOW_MAP_RANGE:
                case MaxConstants.SHADOW_MAP_FILTER:
                case MaxConstants.RAY_BIAS:
                case MaxConstants.O_CONST:
                case MaxConstants.VIEWPORT_LAYOUT_OLD:
                case MaxConstants.VIEWPORT_LAYOUT:
                case MaxConstants.NETWORK_VIEW:
                    skipBytes(size - 6);
                    break;

                case MaxConstants.BITMAP:
                    data.backgroundBitmap = readString();
                    break;

                case MaxConstants.SOLID_BG:

                    data.solidBackgroundColor = new float[3];
                    read = readColor(data.solidBackgroundColor);

                    // If an R3 file, may double declare the colour value
                    // so replace the COLOR_F with LIN_COLOR value.
                    if(size - read - 6 != 0)
                        readColor(data.solidBackgroundColor);

                    break;

                case MaxConstants.V_GRADIENT:
                    data.backgroundMidpoint = readFloat();  // midpoint of gradient

                    data.gradientBackgroundColors = new float[3][3];

                    read = readColor(data.gradientBackgroundColors[0]);
                    read += readColor(data.gradientBackgroundColors[1]);
                    read += readColor(data.gradientBackgroundColors[2]);

                    // Did we get R3 data with the LIN_COLOR values interlaced?
                    // If so, we just read BG color 0 twice and bg color 1 once
                    // as a COLOR_F. Reshuffle and read extras again. Only keep
                    // the LIN_COLOR values
                    if(size - read - 6 - 4 != 0)
                    {
                        // set color[0] to be the LIN_COLOR value from [1].
                        data.gradientBackgroundColors[0] =
                            data.gradientBackgroundColors[1];

                        // [2] now contains the COLOR_F value from the middle
                        // value, which should be [1]. Read [1] again to get
                        // the LIN_COLOR version.
                        readColor(data.gradientBackgroundColors[1]);

                        // read and discard COLOR_F value for [2], replacing
                        // it with the LIN_COLOR version.
                        readColor(data.gradientBackgroundColors[2]);
                        readColor(data.gradientBackgroundColors[2]);
                    }

                    break;

                case MaxConstants.USE_V_GRADIENT:
                    data.selectedBackground = ObjectMesh.USE_GRADIENT;
                    break;

                case MaxConstants.USE_SOLID_BG:
                    data.selectedBackground = ObjectMesh.USE_SOLID_BG;
                    break;

                case MaxConstants.USE_BITMAP:
                    data.selectedBackground = ObjectMesh.USE_BITMAP;
                    break;

                case MaxConstants.FOG:
                    data.linearFogDetails = new float[4];
                    data.fogColor = new float[3];

                    data.linearFogDetails[0] = readFloat();
                    data.linearFogDetails[1] = readFloat();
                    data.linearFogDetails[2] = readFloat();
                    data.linearFogDetails[3] = readFloat();

                    read = readColor(data.fogColor);

                    // have the fog background flag?
                    if(size - read - 16 - 6 != 0)
                    {
                        readUnsignedShort();
                        readInt();
                        data.fogBackground = true;
                    }
                    break;

                case MaxConstants.LAYER_FOG:
                    data.layerFogDetails = new float[4];
                    data.fogColor = new float[3];

                    data.layerFogDetails[0] = readFloat();
                    data.layerFogDetails[1] = readFloat();
                    data.layerFogDetails[2] = readFloat();
                    data.layerFogFlags = readInt();

                    readColor(data.fogColor);
                    break;

                case MaxConstants.DISTANCE_CUE:
                    data.distanceFogDetails = new float[4];

                    data.distanceFogDetails[0] = readFloat();
                    data.distanceFogDetails[1] = readFloat();
                    data.distanceFogDetails[2] = readFloat();
                    data.distanceFogDetails[3] = readFloat();

                    if(size - 16 - 6 != 0)
                    {
                        readUnsignedShort();
                        readInt();
                        data.fogBackground = true;
                    }
                    break;

                case MaxConstants.USE_FOG:
                    data.selectedFog = ObjectMesh.USE_LINEAR_FOG;
                    break;

                case MaxConstants.USE_LAYER_FOG:
                    data.selectedFog = ObjectMesh.USE_LAYER_FOG;
                    break;

                case MaxConstants.USE_DISTANCE_CUE:
                    data.selectedFog = ObjectMesh.USE_DISTANCE_FOG;
                    break;

                case MaxConstants.DEFAULT_VIEW:
                    // Ignore it. Only used for commandline rendering.
                    skipBytes(size - 6);
                    break;

                default:
                    System.out.println("Unknown mesh chunk ID 0x" +
                                       Integer.toHexString(type) + " size " + size);
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for mesh chunk. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read all the object blocks for this mesh
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The object block to put everything into
     */
    private void readObjectBlock(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        if(data.blocks.length == data.numBlocks)
        {
            ObjectBlock[] tmp = new ObjectBlock[data.numBlocks + 8];
            System.arraycopy(data.blocks, 0, tmp, 0, data.numBlocks);
            data.blocks = tmp;
        }

        ObjectBlock block = new ObjectBlock();
        data.blocks[data.numBlocks] = block;
        data.numBlocks++;

        // Read the object block's name.
        block.name = readString();
        int bytes_read = block.name.length() + 1;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.TRI_MESH:
                    readTriMesh(size - 6, block);
                    break;

                case MaxConstants.N_DIRECTIONAL_LIGHT:
                    readLightBlock(size - 6, block);
                    break;

                case MaxConstants.N_CAMERA:
                    readCameraBlock(size - 6, block);
                    break;

                // Ignore, zero size.
                case MaxConstants.VIS_LOFTER:
                case MaxConstants.NO_CAST:
                case MaxConstants.OBJ_MATTE:
                case MaxConstants.OBJ_FAST:
                case MaxConstants.OBJ_PROCEDURAL:
                case MaxConstants.OBJ_FROZEN:
                case MaxConstants.OBJ_NOT_SHADOWED:
                    break;

                default:
                    System.out.println("Unknown object block chunk ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for object block. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read all a triangle mesh block.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
     */
    private void readTriMesh(int bytesToRead, ObjectBlock data)
        throws IOException
    {
        if(data.meshes.length == data.numMeshes)
        {
            TriangleMesh[] tmp = new TriangleMesh[data.numMeshes + 8];
            System.arraycopy(data.meshes, 0, tmp, 0, data.numMeshes);
            data.meshes = tmp;
        }

        TriangleMesh mesh = new TriangleMesh();
        data.meshes[data.numMeshes] = mesh;
        data.numMeshes++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.VERTEX_LIST:
                    mesh.numVertices = readUnsignedShort();
                    mesh.vertices = new float[mesh.numVertices * 3];

                    for(int i = 0; i < mesh.numVertices; i++)
                        readPoint(mesh.vertices, i * 3);
                    break;

                case MaxConstants.TEXCOORD_LIST:
                    mesh.numTexCoords = readUnsignedShort();
                    mesh.texCoords = new float[mesh.numTexCoords * 2];

                    for(int i = 0; i < mesh.numTexCoords; i++)
                    {
                        mesh.texCoords[i * 2] = readFloat();
                        mesh.texCoords[i * 2 + 1] = readFloat();
                    }
                    break;

                case MaxConstants.MESH_MATRIX:
                    mesh.localCoords = new float[12];
                    for(int i = 0; i < 12; i++)
                        mesh.localCoords[i] = readFloat();
                    break;

                case MaxConstants.FACE_LIST:
                    readFaceList(size - 6, mesh);
                    break;

                case MaxConstants.BOX_MAP:
                    mesh.boxMapMaterials = new String[6];
                    mesh.boxMapMaterials[0] = readString();
                    mesh.boxMapMaterials[1] = readString();
                    mesh.boxMapMaterials[2] = readString();
                    mesh.boxMapMaterials[3] = readString();
                    mesh.boxMapMaterials[4] = readString();
                    mesh.boxMapMaterials[5] = readString();
                    break;

                // These are deliberately ignored as they are useless.
                case MaxConstants.VERTEX_FLAG:
                case MaxConstants.MESH_COLOR:
                case MaxConstants.MESH_TEXTURE_INFO:
                    skipBytes(size - 6);
                    break;

                default:
                    System.out.println("Unknown trimesh chunk ID 0x" +
                                       Integer.toHexString(type));

                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for triangle mesh. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read all the face list and any subchunks for this mesh.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
     */
    private void readFaceList(int bytesToRead, TriangleMesh data)
        throws IOException
    {
        int cnt = 0;
        data.numFaces = readUnsignedShort();
        data.faces = new int[data.numFaces * 3];

        for(int i = 0; i < data.numFaces; i++)
        {
            data.faces[cnt++] = readUnsignedShort();
            data.faces[cnt++] = readUnsignedShort();
            data.faces[cnt++] = readUnsignedShort();
            readUnsignedShort();
        }

        // num faces * sizeof(unsigned short) * 4 shorts per face
        // plus 2 bytes for numFaces int and 6 bytes for the
        // chunk ID + size indicator
        int bytes_read = data.numFaces * 2 * 4 + 2;

        if(bytes_read < bytesToRead)
        {
            while(bytes_read < bytesToRead)
            {
                int type = readUnsignedShort();
                int size = readInt();

                switch(type)
                {
                    case MaxConstants.SMOOTH_LIST:
                        data.smoothgroups = new int[data.numFaces];

                        for(int i = 0; i < data.numFaces; i++)
                            data.smoothgroups[i] = readInt();
                        break;

                    case MaxConstants.MATERIAL_LIST:
                        readMaterialList(size - 6, data);
                        break;

                    default:
                        System.out.println("Unknown subface list ID 0x" +
                                           Integer.toHexString(type));
                        skipBytes(size - 6);
                }

                bytes_read += size;
            }

            if(bytes_read != bytesToRead)
                 System.out.println("Incorrect bytes read from file for face " +
                                    "chunk. Read: " + bytes_read + " required " +
                                    bytesToRead);
        }
    }

    /**
     * Process a tri-mesh material list value.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
     */
    private void readMaterialList(int bytesToRead, TriangleMesh data)
        throws IOException
    {
        if((data.materials == null) || (data.materials.length == data.numMaterials))
        {
            MaterialData[] tmp = new MaterialData[data.numMaterials + 4];

            if(data.numMaterials != 0)
                System.arraycopy(data.materials, 0, tmp, 0, data.numMaterials);

            data.materials = tmp;
        }

        MaterialData mat = new MaterialData();
        data.materials[data.numMaterials] = mat;
        data.numMaterials++;

        mat.materialName = readString();
        mat.numFaces = readUnsignedShort();

        mat.faceList = new int[mat.numFaces];

        for(int i = 0; i < mat.numFaces; i++)
            mat.faceList[i] = readUnsignedShort();

        int bytes_read = mat.materialName.length() + 1 + 2 + mat.numFaces * 2;
        if(bytesToRead != bytes_read)
             System.out.println("Incorrect bytes read from file for material " +
                                "list. Read: " + bytes_read + " required " +
                                bytesToRead);


    }


    /**
     * Read all a light block.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
     */
    private void readLightBlock(int bytesToRead, ObjectBlock data)
        throws IOException
    {
        if((data.lights == null) || (data.lights.length == data.numLights))
        {
            LightBlock[] tmp = new LightBlock[data.numLights + 8];

            if(data.numLights != 0)
                System.arraycopy(data.lights, 0, tmp, 0, data.numLights);
            data.lights = tmp;
        }

        LightBlock light = new LightBlock();
        data.lights[data.numLights] = light;
        data.numLights++;

        readPoint(light.direction, 0);

        int bytes_read = readColor(light.color);

        bytes_read += 12;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.SPOT_LIGHT:
                    readSpotlightBlock(size - 6, light);
                    break;

                case MaxConstants.DIR_LIGHT_OFF:
                    light.enabled = false;
                    break;

                case MaxConstants.DIR_LIGHT_ATTENUATION:
                    light.attenuation = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_INNER_RANGE:
                    light.innerRange = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_OUTER_RANGE:
                    light.outerRange = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_MULTIPLIER:
                    light.multiple = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_EXCLUDE:
                    // Light exclusion range string. Not sure what this does
                    // so ignore it.
                    readString();
                    break;

                default:
                    System.out.println("Unknown light block ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for lights chunk. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read an extended spotlight block.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param light The light object to put everything into
     */
    private void readSpotlightBlock(int bytesToRead, LightBlock light)
        throws IOException
    {
        light.type = LightBlock.SPOT_LIGHT;
        light.target = new float[3];
        readPoint(light.target, 0);
        light.hotspotAngle = readFloat();
        light.falloffAngle = readFloat();

        int bytes_read = 12 + 4 + 4;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.DIR_LIGHT_SPOT_ROLLOFF:
                    light.rollAngle = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_SPOT_ASPECTRATIO:
                    light.aspectRatio = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_SEE_CONE:
                    light.seeCone = true;
                    break;

                case MaxConstants.DIR_LIGHT_SHADOWED:
                    light.castsShadows = true;
                    break;

                case MaxConstants.DIR_LIGHT_LOCAL_SHADOW2:
                    light.shadowParams = new float[2];
                    light.shadowParams[0] = readFloat();
                    light.shadowParams[1] = readFloat();
                    light.shadowMapSize = readUnsignedShort();
                    break;

                case MaxConstants.DIR_LIGHT_RAY_BIAS:
                    // ignored because it's for raytracing
                    skipBytes(size - 6);
                    break;

                case MaxConstants.DIR_LIGHT_RAYSHAD:
                    // ignored because it's for raytracing
                    break;

                default:
                    System.out.println("Unknown spotlight block ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for spotlight block. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read all a Camera block.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The object mesh object to put everything into
     */
    private void readCameraBlock(int bytesToRead, ObjectBlock data)
        throws IOException
    {
        if((data.cameras == null) || (data.cameras.length == data.numCameras))
        {
            CameraBlock[] tmp = new CameraBlock[data.numCameras + 8];

            if(data.numCameras != 0)
                System.arraycopy(data.cameras, 0, tmp, 0, data.numCameras);
            data.cameras = tmp;
        }

        CameraBlock camera = new CameraBlock();
        data.cameras[data.numCameras] = camera;
        data.numCameras++;

        readPoint(camera.location, 0);
        readPoint(camera.target, 0);

        camera.bankAngle = readFloat();
        camera.focus = readFloat();

        int bytes_read = 32;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.CAMERA_SEE_CONE:
                     camera.seeOutline = true;
                     break;

                case MaxConstants.CAMERA_RANGE:
                    camera.ranges = new float[2];
                    camera.ranges[0] = readFloat();
                    camera.ranges[1] = readFloat();
                    break;

                default:
                    System.out.println("Unknown camera block ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for camera block. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    // Material block handling

    /**
     * Read all the material blocks for this mesh
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The object block to put everything into
     */
    private void readMaterialBlock(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        if((data.materials == null) || (data.materials.length == data.numMaterials))
        {
            MaterialBlock[] tmp = new MaterialBlock[data.numMaterials + 8];
            if(data.numMaterials != 0)
                System.arraycopy(data.materials, 0, tmp, 0, data.numMaterials);
            data.materials = tmp;
        }

        MaterialBlock mat = new MaterialBlock();
        data.materials[data.numMaterials] = mat;
        data.numMaterials++;

        int bytes_read = 0;
        int read;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.MAT_NAME:
                    mat.name = readString();
                    break;

                case MaxConstants.MAT_AMBIENT:
                    mat.ambientColor = new float[3];
                    read = readColor(mat.ambientColor);

                    // May have a gamma corrected value too. Skip that
                    if(size - read - 6 != 0)
                        skipBytes(size - read - 6);
                    break;

                case MaxConstants.MAT_DIFFUSE:
                    mat.diffuseColor = new float[3];
                    read = readColor(mat.diffuseColor);

                    // May have a gamma corrected value too. Skip that
                    if(size - read - 6 != 0)
                        skipBytes(size - read - 6);
                    break;

                case MaxConstants.MAT_SPECULAR:
                    mat.specularColor = new float[3];
                    read = readColor(mat.specularColor);
                    // May have a gamma corrected value too. Skip that
                    if(size - read - 6 != 0)
                        skipBytes(size - read - 6);
                    break;

                case MaxConstants.MAT_SHININESS:
                    mat.shininessRatio = readPercentage();
                    break;

                case MaxConstants.MAT_SHIN2PCT:
                    mat.shininessStrength = readPercentage();
                    break;

                case MaxConstants.MAT_WIREFRAME:
                    mat.wireframe = true;
                    break;

                case MaxConstants.MAT_WIRESIZE:
                    mat.wireSize = readFloat();
                    break;

                case MaxConstants.MAT_SHADING:
                    mat.shadingType = readUnsignedShort();
                    break;

                case MaxConstants.MAT_ADDITIVE:
                    mat.additiveBlend = true;
                    break;

                case MaxConstants.MAT_TRANSPARENCY:
                    mat.transparency = readPercentage();
                    break;

                case MaxConstants.MAT_TWO_SIDE:
                    mat.twoSidedLighting = true;
                    break;

                case MaxConstants.MAT_TEXMAP:
                case MaxConstants.MAT_TEX2MAP:
                case MaxConstants.MAT_SHINMAP:
                case MaxConstants.MAT_SPECMAP:
                case MaxConstants.MAT_OPACMAP:
                case MaxConstants.MAT_REFLMAP:
                case MaxConstants.MAT_BUMPMAP:
                case MaxConstants.MAT_TEXMASK:
                case MaxConstants.MAT_TEX2MASK:
                case MaxConstants.MAT_SHINMASK:
                case MaxConstants.MAT_SPECMASK:
                case MaxConstants.MAT_OPACMASK:
                case MaxConstants.MAT_REFLMASK:
                case MaxConstants.MAT_BUMPMASK:
                    readTextureBlock(size - 6, mat, type);
                    break;

                // These are deliberately ignored
                case MaxConstants.MAT_XPFALL:
                case MaxConstants.MAT_SELF_ILPCT:
                case MaxConstants.MAT_SELF_ILLUM:
                case MaxConstants.MAT_XPFALLIN:
                case MaxConstants.MAT_PHONGSOFT:
                case MaxConstants.MAT_REFBLUR:
                case MaxConstants.MAT_USE_REFBLUR:
                case MaxConstants.MAT_SXP_TEXT_DATA:
                case MaxConstants.MAT_SXP_TEXT2_DATA:
                case MaxConstants.MAT_SXP_OPAC_DATA:
                case MaxConstants.MAT_SXP_BUMP_DATA:
                case MaxConstants.MAT_SXP_SPEC_DATA:
                case MaxConstants.MAT_SXP_SHIN_DATA:
                case MaxConstants.MAT_SXP_SELFI_DATA:
                case MaxConstants.MAT_SXP_TEXT_MASKDATA:
                case MaxConstants.MAT_SXP_TEXT2_MASKDATA:
                case MaxConstants.MAT_SXP_OPAC_MASKDATA:
                case MaxConstants.MAT_SXP_BUMP_MASKDATA:
                case MaxConstants.MAT_SXP_SPEC_MASKDATA:
                case MaxConstants.MAT_SXP_SHIN_MASKDATA:
                case MaxConstants.MAT_SXP_SELFI_MASKDATA:
                case MaxConstants.MAT_SXP_REFL_MASKDATA:
                    skipBytes(size - 6);
                    break;

                default:
                    System.out.println("Unknown material block chunk ID 0x" +
                                       Integer.toHexString(type));
                    if(type != 0x0)
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for material block. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Common method to read all texture block types for this material.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The object block to put everything into
     * @param textureType the ID of the texture to read and assign
     */
    private void readTextureBlock(int bytesToRead,
                                  MaterialBlock data,
                                  int textureType)
        throws IOException
    {
        TextureBlock tex = new TextureBlock();

        int bytes_read = 0;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.INT_PERCENT:
                    int i_perc = readUnsignedShort();
                    tex.strength = i_perc * 0.01f;
                    break;

                case MaxConstants.MAT_MAPNAME:
                    tex.filename = readString();
                    break;

                case MaxConstants.MAT_MAP_TILING:
                    tex.tiling = readUnsignedShort();
                    break;

                case MaxConstants.MAT_MAP_TEXBLUR:
                    tex.blurring = readFloat();
                    break;

                case MaxConstants.MAT_MAP_USCALE:
                    tex.uScale = readFloat();
                    break;

                case MaxConstants.MAT_MAP_VSCALE:
                    tex.vScale = readFloat();
                    break;

                case MaxConstants.MAT_MAP_UOFFSET:
                    tex.uOffset = readFloat();
                    break;

                case MaxConstants.MAT_MAP_VOFFSET:
                    tex.vOffset = readFloat();
                    break;

                case MaxConstants.MAT_MAP_ANG:
                    tex.angle = readFloat();
                    break;

                case MaxConstants.MAT_MAP_COL1:
                    tex.blendColor1 = new float[3];
                    int c = inputStream.readUnsignedByte();
                    tex.blendColor1[0] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.blendColor1[1] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.blendColor1[2] = c * 0.0039215f;
                    break;

                case MaxConstants.MAT_MAP_COL2:
                    tex.blendColor2 = new float[3];
                    c = inputStream.readUnsignedByte();
                    tex.blendColor2[0] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.blendColor2[1] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.blendColor2[2] = c * 0.0039215f;
                    break;

                case MaxConstants.MAT_MAP_RCOL:
                    tex.redBlends = new float[3];
                    c = inputStream.readUnsignedByte();
                    tex.redBlends[0] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.redBlends[1] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.redBlends[2] = c * 0.0039215f;
                    break;

                case MaxConstants.MAT_MAP_GCOL:
                    tex.greenBlends = new float[3];
                    c = inputStream.readUnsignedByte();
                    tex.greenBlends[0] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.greenBlends[1] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.greenBlends[2] = c * 0.0039215f;
                    break;

                case MaxConstants.MAT_MAP_BCOL:
                    tex.blueBlends = new float[3];
                    c = inputStream.readUnsignedByte();
                    tex.blueBlends[0] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.blueBlends[1] = c * 0.0039215f;

                    c = inputStream.readUnsignedByte();
                    tex.blueBlends[2] = c * 0.0039215f;
                    break;

                case MaxConstants.MAT_BUMP_PERCENT:
                    tex.bumpPercentage = readUnsignedShort();
                    break;

                default:
                    System.out.println("Unknown texture block chunk ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
              System.out.println("Incorrect bytes read from file for texture block. " +
                                 "Read: " + bytes_read + " required " + bytesToRead);

        switch(textureType)
        {
            case MaxConstants.MAT_TEXMAP:
                data.textureMap1 = tex;
                break;

            case MaxConstants.MAT_TEX2MAP:
                data.textureMap2 = tex;
                break;

            case MaxConstants.MAT_SHINMAP:
                data.shininessMap = tex;
                break;

            case MaxConstants.MAT_SPECMAP:
                data.specularMap = tex;
                break;

            case MaxConstants.MAT_OPACMAP:
                data.opacityMap = tex;
                break;

            case MaxConstants.MAT_REFLMAP:
                data.reflectionMap = tex;
                break;

            case MaxConstants.MAT_BUMPMAP:
                data.bumpMap = tex;
                break;

            case MaxConstants.MAT_TEXMASK:
                data.textureMask1 = tex;
                break;

            case MaxConstants.MAT_TEX2MASK:
                data.textureMask2 = tex;
                break;

            case MaxConstants.MAT_SHINMASK:
                data.shininessMask = tex;
                break;

            case MaxConstants.MAT_SPECMASK:
                data.specularMask = tex;
                break;

            case MaxConstants.MAT_OPACMASK:
                data.opacityMask = tex;
                break;

            case MaxConstants.MAT_REFLMASK:
                data.reflectionMask = tex;
                break;

            case MaxConstants.MAT_BUMPMASK:
                data.bumpMask = tex;
                break;

        }
    }


    // Keyframe block handling


    /**
     * Read all the keyframe blocks for this mesh
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The object block to put everything into
     */
    private void readKeyframeChunk(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        if((data.keyframes == null) ||
            data.keyframes.length == data.numKeyframes)
        {
            KeyframeBlock[] tmp = new KeyframeBlock[data.numKeyframes + 8];

            if(data.numKeyframes != 0)
                System.arraycopy(data.keyframes, 0, tmp, 0, data.numKeyframes);
            data.keyframes = tmp;
        }

        KeyframeBlock keyframe = new KeyframeBlock();
        data.keyframes[data.numKeyframes] = keyframe;
        data.numKeyframes++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_HEADER:
                    keyframe.revision = readUnsignedShort();
                    keyframe.filename = readString();
                    keyframe.animationLength = readInt();
                    break;

                case MaxConstants.KEYFRAME_SEGMENT:
                    keyframe.startFrame = readInt();
                    keyframe.endFrame = readInt();
                    break;

                case MaxConstants.KEYFRAME_CURRENT_TIME:
                    keyframe.currentFrame = readInt();
                    break;

                case MaxConstants.OBJECT_NODE_TAG:
                    readObjectNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.CAMERA_NODE_TAG:
                    readCameraNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.TARGET_NODE_TAG:
                    readCameraTargetNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.LIGHT_NODE_TAG:
                    readLightNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.SPOTLIGHT_NODE_TAG:
                    readSpotlightNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.LIGHT_TARGET_NODE_TAG:
                    readSpotlightTargetNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.AMBIENT_NODE_TAG:
                    readAmbientNodeTag(size - 6, keyframe);
                    break;

                case MaxConstants.VIEWPORT_LAYOUT:
                    // ignored.
                    skipBytes(size - 6);
                    break;

                default:
                    System.out.println("Unknown keyframe chunk ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe chunk. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe object node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readObjectNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.frames == null) ||
            data.frames.length == data.numFrames)
        {
            KeyframeFrameBlock[] tmp = new KeyframeFrameBlock[data.numFrames + 8];

            if(data.numFrames != 0)
                System.arraycopy(data.frames, 0, tmp, 0, data.numFrames);
            data.frames = tmp;
        }

        KeyframeFrameBlock frame = new KeyframeFrameBlock();
        data.frames[data.numFrames] = frame;
        data.numFrames++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    frame.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_POSITION_TRACK_TAG:
                    frame.positions = new KeyframePositionBlock();
                    readPositionTag(size - 6, frame.positions);
                    break;

                case MaxConstants.KEYFRAME_ROTATION_TRACK_TAG:
                    frame.rotations = new KeyframeRotationBlock();
                    readRotationTag(size - 6, frame.rotations);
                    break;

                case MaxConstants.KEYFRAME_SCALE_TRACK_TAG:
                    frame.scales = new KeyframeScaleBlock();
                    readScaleTag(size - 6, frame.scales);
                    break;

                case MaxConstants.KEYFRAME_MORPH_TRACK_TAG:
                    frame.morphs = new KeyframeMorphBlock();
                    readMorphTag(size - 6, frame.morphs);
                    break;

                case MaxConstants.KEYFRAME_INSTANCE_NAME:
                    frame.instanceName = readString();
                    break;

                case MaxConstants.KEYFRAME_BOUNDS:
                    readPoint(frame.minBounds, 0);
                    readPoint(frame.maxBounds, 0);
                    break;

                case MaxConstants.KEYFRAME_PIVOT:
                    readPoint(frame.pivotPoint, 0);
                    break;

                case MaxConstants.KEYFRAME_SMOOTH_MORPH:
                    frame.morphSmoothingAngle = readFloat();
                    break;

                case MaxConstants.XDATA_SECTION:
                    // Ignore it
                    skipBytes(size - 6);
                    break;

                // hide track not handed yet
                default:
                    System.out.println("Unknown keyframe frame ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe frame. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe camera node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readCameraNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.cameraInfo == null) ||
            data.cameraInfo.length == data.numCameras)
        {
            KeyframeCameraBlock[] tmp = new KeyframeCameraBlock[data.numCameras + 8];

            if(data.numCameras != 0)
                System.arraycopy(data.cameraInfo, 0, tmp, 0, data.numCameras);
            data.cameraInfo = tmp;
        }

        KeyframeCameraBlock camera = new KeyframeCameraBlock();
        data.cameraInfo[data.numCameras] = camera;
        data.numCameras++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    camera.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, camera);
                    break;

                case MaxConstants.KEYFRAME_POSITION_TRACK_TAG:
                    camera.positions = new KeyframePositionBlock();
                    readPositionTag(size - 6, camera.positions);
                    break;

                case MaxConstants.KEYFRAME_FOV_TRACK_TAG:
                    camera.fovs = new KeyframeFOVBlock();
                    readFieldOfViewTag(size - 6, camera.fovs);
                    break;

                case MaxConstants.KEYFRAME_ROLL_TRACK_TAG:
                    camera.rolls = new KeyframeRollBlock();
                    readRolloffTag(size - 6, camera.rolls);
                    break;

                default:
                    System.out.println("Unknown keyframe camera ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe camera. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe camera node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readCameraTargetNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.cameraTargetInfo == null) ||
            data.cameraTargetInfo.length == data.numCameraTargets)
        {
            KeyframeCameraTargetBlock[] tmp =
                new KeyframeCameraTargetBlock[data.numCameraTargets + 8];

            if(data.numCameraTargets != 0)
                System.arraycopy(data.cameraTargetInfo, 0, tmp, 0, data.numCameraTargets);
            data.cameraTargetInfo = tmp;
        }

        KeyframeCameraTargetBlock target = new KeyframeCameraTargetBlock();
        data.cameraTargetInfo[data.numCameraTargets] = target;
        data.numCameraTargets++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    target.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, target);
                    break;

                case MaxConstants.KEYFRAME_POSITION_TRACK_TAG:
                    target.positions = new KeyframePositionBlock();
                    readPositionTag(size - 6, target.positions);
                    break;

                default:
                    System.out.println("Unknown keyframe camera target ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe camera target. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe camera node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readAmbientNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.ambientInfo == null) ||
            data.ambientInfo.length == data.numAmbients)
        {
            KeyframeAmbientBlock[] tmp = new KeyframeAmbientBlock[data.numAmbients + 8];

            if(data.numAmbients != 0)
                System.arraycopy(data.ambientInfo, 0, tmp, 0, data.numAmbients);
            data.ambientInfo = tmp;
        }

        KeyframeAmbientBlock light = new KeyframeAmbientBlock();
        data.ambientInfo[data.numAmbients] = light;
        data.numAmbients++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    light.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, light);
                    break;

                case MaxConstants.KEYFRAME_COLOR_TRACK_TAG:
                    light.colors = new KeyframeColorBlock();
                    readColorTag(size - 6, light.colors);
                    break;

                default:
                    System.out.println("Unknown keyframe ambient ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe ambient. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe camera node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readLightNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.lightInfo == null) ||
            data.lightInfo.length == data.numLights)
        {
            KeyframeLightBlock[] tmp = new KeyframeLightBlock[data.numLights + 8];

            if(data.numLights != 0)
                System.arraycopy(data.lightInfo, 0, tmp, 0, data.numLights);
            data.lightInfo = tmp;
        }

        KeyframeLightBlock light = new KeyframeLightBlock();
        data.lightInfo[data.numLights] = light;
        data.numLights++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    light.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, light);
                    break;

                case MaxConstants.KEYFRAME_POSITION_TRACK_TAG:
                    light.positions = new KeyframePositionBlock();
                    readPositionTag(size - 6, light.positions);
                    break;

                case MaxConstants.KEYFRAME_COLOR_TRACK_TAG:
                    light.colors = new KeyframeColorBlock();
                    readColorTag(size - 6, light.colors);
                    break;

                default:
                    System.out.println("Unknown keyframe light ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe light. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe camera node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readSpotlightNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.spotlightInfo == null) ||
            data.spotlightInfo.length == data.numSpotlights)
        {
            KeyframeSpotlightBlock[] tmp =
                new KeyframeSpotlightBlock[data.numSpotlights + 8];

            if(data.numSpotlights != 0)
                System.arraycopy(data.spotlightInfo, 0, tmp, 0, data.numSpotlights);
            data.spotlightInfo = tmp;
        }

        KeyframeSpotlightBlock light = new KeyframeSpotlightBlock();
        data.spotlightInfo[data.numSpotlights] = light;
        data.numSpotlights++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    light.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, light);
                    break;

                case MaxConstants.KEYFRAME_POSITION_TRACK_TAG:
                    light.positions = new KeyframePositionBlock();
                    readPositionTag(size - 6, light.positions);
                    break;

                case MaxConstants.KEYFRAME_COLOR_TRACK_TAG:
                    light.colors = new KeyframeColorBlock();
                    readColorTag(size - 6, light.colors);
                    break;

                case MaxConstants.KEYFRAME_HOTSPOT_TRACK_TAG:
                    light.hotspots = new KeyframeHotspotBlock();
                    readHotspotTag(size - 6, light.hotspots);
                    break;

                case MaxConstants.KEYFRAME_FALLOFF_TRACK_TAG:
                    light.falloffs = new KeyframeFalloffBlock();
                    readFalloffTag(size - 6, light.falloffs);
                    break;

                case MaxConstants.KEYFRAME_ROLL_TRACK_TAG:
                    light.rolloffs = new KeyframeRollBlock();
                    readRolloffTag(size - 6, light.rolloffs);
                    break;

                default:
                    System.out.println("Unknown keyframe spotlight ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe spotlight. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node tag for the given keyframe camera node tag.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The keyframe block to put everything into
     */
    private void readSpotlightTargetNodeTag(int bytesToRead, KeyframeBlock data)
        throws IOException
    {
        if((data.spotlightTargetInfo == null) ||
            data.spotlightTargetInfo.length == data.numSpotlightTargets)
        {
            KeyframeSpotlightTargetBlock[] tmp =
                new KeyframeSpotlightTargetBlock[data.numSpotlightTargets + 8];

            if(data.numSpotlightTargets != 0)
                System.arraycopy(data.spotlightTargetInfo, 0, tmp, 0, data.numSpotlightTargets);
            data.spotlightTargetInfo = tmp;
        }

        KeyframeSpotlightTargetBlock target = new KeyframeSpotlightTargetBlock();
        data.spotlightTargetInfo[data.numSpotlightTargets] = target;
        data.numSpotlightTargets++;

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.KEYFRAME_NODE_ID:
                    target.nodeId = readUnsignedShort();
                    break;

                case MaxConstants.KEYFRAME_NODE_HEADER:
                    readNodeHeader(size - 6, target);
                    break;

                case MaxConstants.KEYFRAME_POSITION_TRACK_TAG:
                    target.positions = new KeyframePositionBlock();
                    readPositionTag(size - 6, target.positions);
                    break;

                default:
                    System.out.println("Unknown keyframe spotlight target ID 0x" +
                                       Integer.toHexString(type));
                    skipBytes(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe spotlight target. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read the a node header for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The block to put everything into
     */
    private void readNodeHeader(int bytesToRead, KeyframeTag frame)
        throws IOException
    {
        frame.nodeHeader = new NodeHeaderData();
        frame.nodeHeader.name = readString();
        frame.nodeHeader.flags1 = readUnsignedShort();
        frame.nodeHeader.flags2 = readUnsignedShort();
        frame.nodeHeader.heirarchyPosition = readUnsignedShort();

        int bytes_read = frame.nodeHeader.name.length() + 1 + 2 + 2 + 2;

        if(bytes_read != bytesToRead)
             System.out.println("Incorrect bytes read from file for keyframe node header. " +
                                "Read: " + bytes_read + " required " + bytesToRead);
    }

    /**
     * Read a track position tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param pos The block to put everything into
     */
    private void readPositionTag(int bytesToRead, KeyframePositionBlock pos)
        throws IOException
    {
        pos.flags = readUnsignedShort();
        readInt();
        readInt();
        pos.numKeys = readInt();
        pos.positions = new PositionData[pos.numKeys];

        for(int i = 0; i < pos.numKeys; i++)
        {
            PositionData pd = new PositionData();
            pos.positions[i] = pd;

            readTrackData(pd);

            // swap coord order
            pd.x = readFloat();
            pd.z = readFloat();
            pd.y = readFloat();
        }
    }

    /**
     * Read a track position tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param rot The block to put everything into
     */
    private void readRotationTag(int bytesToRead, KeyframeRotationBlock rot)
        throws IOException
    {
        rot.flags = readUnsignedShort();
        readInt();
        readInt();
        rot.numKeys = readInt();
        rot.rotations = new RotationData[rot.numKeys];

        for(int i = 0; i < rot.numKeys; i++)
        {
            RotationData rd = new RotationData();
            rot.rotations[i] = rd;

            readTrackData(rd);

            // swap coord order
            rd.rotation = readFloat();
            rd.xAxis = readFloat();
            rd.zAxis = readFloat();
            rd.yAxis = readFloat();
        }
    }

    /**
     * Read a track scale tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param scale The block to put everything into
     */
    private void readScaleTag(int bytesToRead, KeyframeScaleBlock scale)
        throws IOException
    {
        scale.flags = readUnsignedShort();
        readInt();
        readInt();
        scale.numKeys = readInt();
        scale.scales = new ScaleData[scale.numKeys];

        for(int i = 0; i < scale.numKeys; i++)
        {
            ScaleData sd = new ScaleData();
            scale.scales[i] = sd;

            readTrackData(sd);

            // swap coord order
            sd.xScale = readFloat();
            sd.zScale = readFloat();
            sd.yScale = readFloat();
        }
    }

    /**
     * Read a track fov tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param fov The block to put everything into
     */
    private void readFieldOfViewTag(int bytesToRead, KeyframeFOVBlock fov)
        throws IOException
    {
        fov.flags = readUnsignedShort();
        readInt();
        readInt();
        fov.numKeys = readInt();
        fov.fovs = new FieldOfViewData[fov.numKeys];

        for(int i = 0; i < fov.numKeys; i++)
        {
            FieldOfViewData fd = new FieldOfViewData();
            fov.fovs[i] = fd;

            readTrackData(fd);

            fd.fov = readFloat();
        }
    }

    /**
     * Read a track rolloff tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param rolloff The block to put everything into
     */
    private void readRolloffTag(int bytesToRead, KeyframeRollBlock rolloff)
        throws IOException
    {
        rolloff.flags = readUnsignedShort();
        readInt();
        readInt();
        rolloff.numKeys = readInt();
        rolloff.rolls = new RollData[rolloff.numKeys];

        for(int i = 0; i < rolloff.numKeys; i++)
        {
            RollData rd = new RollData();
            rolloff.rolls[i] = rd;

            readTrackData(rd);

            rd.roll = readFloat();
        }
    }

    /**
     * Read a track color tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param color The block to put everything into
     */
    private void readColorTag(int bytesToRead, KeyframeColorBlock color)
        throws IOException
    {
        color.flags = readUnsignedShort();
        readInt();
        readInt();
        color.numKeys = readInt();
        color.colors = new ColorData[color.numKeys];

        for(int i = 0; i < color.numKeys; i++)
        {
            ColorData cd = new ColorData();
            color.colors[i] = cd;

            readTrackData(cd);

            cd.red = readFloat();
            cd.green = readFloat();
            cd.blue = readFloat();
        }
    }

    /**
     * Read a track morph tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param morph The block to put everything into
     */
    private void readMorphTag(int bytesToRead, KeyframeMorphBlock morph)
        throws IOException
    {
        morph.flags = readUnsignedShort();
        readInt();
        readInt();
        morph.numKeys = readInt();
        morph.morphs = new MorphData[morph.numKeys];

        int bytes_read = 2 + 4 + 4 + 4;

        for(int i = 0; i < morph.numKeys; i++)
        {
            MorphData md = new MorphData();
            morph.morphs[i] = md;

            readTrackData(md);

            md.objectName = readString();

            bytes_read += md.objectName.length() + 1;
        }
    }

    /**
     * Read a track hotspot tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param hotspot The block to put everything into
     */
    private void readHotspotTag(int bytesToRead, KeyframeHotspotBlock hotspot)
        throws IOException
    {
        hotspot.flags = readUnsignedShort();
        readInt();
        readInt();
        hotspot.numKeys = readInt();
        hotspot.hotspots = new HotspotData[hotspot.numKeys];

        for(int i = 0; i < hotspot.numKeys; i++)
        {
            HotspotData hsd = new HotspotData();
            hotspot.hotspots[i] = hsd;

            readTrackData(hsd);

            hsd.angle = readFloat();
        }
    }

    /**
     * Read a track falloffoff tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param falloff The block to put everything into
     */
    private void readFalloffTag(int bytesToRead, KeyframeFalloffBlock falloff)
        throws IOException
    {
        falloff.flags = readUnsignedShort();
        readInt();
        readInt();
        falloff.numKeys = readInt();
        falloff.falloffs = new FalloffData[falloff.numKeys];

        for(int i = 0; i < falloff.numKeys; i++)
        {
            FalloffData fd = new FalloffData();
            falloff.falloffs[i] = fd;

            readTrackData(fd);

            fd.angle = readFloat();
        }
    }

    /**
     * Convenience method to read the track and key header data.
     *
     * @param data The TrackData instance to read stuff into
     * @return The number of bytes read
     */
    private int readTrackData(TrackData data)
        throws IOException
    {
        data.frameNumber = readInt();
        data.splineFlags = readUnsignedShort();

        int bytes_read = 6;

        if(data.splineFlags != 0)
        {
            data.splineData = new float[5];

            // Bit 0 set
            if((data.splineFlags & 0x01) != 0)
            {
                bytes_read += 4;
                data.splineData[0] = readFloat();
            }

            // Bit 1 set
            if((data.splineFlags & 0x02) != 0)
            {
                bytes_read += 4;
                data.splineData[1] = readFloat();
            }

            // Bit 2 set
            if((data.splineFlags & 0x04) != 0)
            {
                bytes_read += 4;
                data.splineData[2] = readFloat();
            }

            // Bit 3 set
            if((data.splineFlags & 0x08) != 0)
            {
                bytes_read += 4;
                data.splineData[3] = readFloat();
            }

            // Bit 4 set
            if((data.splineFlags & 0x10) != 0)
            {
                bytes_read += 4;
                data.splineData[4] = readFloat();
            }
        }

        return bytes_read;
    }

    // Generic internal methods.

    /**
     * Read a percentage chunk from the file and return it as a normalised
     * value from [0,1].
     *
     * @return The converted percentage value
     */
    private float readPercentage()
        throws IOException
    {
        int type = readUnsignedShort();
        int size = readInt();
        float ret_val = 0;

        switch(type)
        {
            case MaxConstants.INT_PERCENT:
                int i_perc = readUnsignedShort();
                ret_val = i_perc * 0.01f;
                break;

            case MaxConstants.FLOAT_PERCENT:
                ret_val = readFloat();
                break;

            default:
                System.out.println("Unknown percentage chunk ID 0x" +
                                   Integer.toHexString(type));
                skipBytes(size - 6);
        }

        return ret_val;
    }

    /**
     * Read a position value. Converts from 3DS form with Z up, Y into the
     * screen to Java3D with Y up.
     *
     * @param vec The array to copy the values into
     * @param offset The offset into the array to start at
     */
    private void readPoint(float[] vec, int offset)
        throws IOException
    {
        vec[offset] = readFloat();
        vec[offset + 2] = readFloat();
        vec[offset + 1] = readFloat();
    }

    /**
     * Read a colour chunk from the file and place it in the given colour
     * array. Values are normalised to [0,1] if byte forms are read.
     *
     * @param target An array to copy the read values into
     * @return The number of bytes read
     */
    private int readColor(float[] target)
        throws IOException
    {
        int type = readUnsignedShort();
        int size = readInt();

        switch(type)
        {
            // Not sure on the differences between the L and normal type
            case MaxConstants.LIN_COLORF:
            case MaxConstants.COLORF:
                target[0] = readFloat();
                target[1] = readFloat();
                target[2] = readFloat();
                break;

            case MaxConstants.LIN_COLOR24:
            case MaxConstants.COLOR24:
                int val = inputStream.readUnsignedByte();
                target[0] = val * 0.0039215f;   // 1/255 for range conv

                val = inputStream.readUnsignedByte();
                target[1] = val * 0.0039215f;

                val = inputStream.readUnsignedByte();
                target[2] = val * 0.0039215f;
                break;

            default:
                System.out.println("Unknown colour chunk ID 0x" +
                                   Integer.toHexString(type));
                skipBytes(size - 6);
        }

        return size;
    }

    /**
     * Special case reader to grab a single char at a time until we hit a \0
     * at the end of the string.
     *
     * @return The next collection of bytes read as a string
     */
    private String readString()
        throws IOException
    {
        StringBuffer buf = new StringBuffer();

        int i = 0;
        char ch = (char)inputStream.readByte();
        while((ch != '\0') && (ch >= 0))
        {
            buf.append(ch);
            ch = (char)inputStream.readByte();
        }

        return buf.toString();
    }

    /**
     * Reader for ints to swap the byte order from that normally ready be
     * Java.
     *
     * @return The next 4 bytes read as an int
     */
    private int readInt()
        throws IOException
    {
        int ch1 = inputStream.readUnsignedByte();
        int ch2 = inputStream.readUnsignedByte();
        int ch3 = inputStream.readUnsignedByte();
        int ch4 = inputStream.readUnsignedByte();

        if((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();

        return (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    /**
     * Reader for shorts to swap the byte order from that normally ready be
     * Java.
     *
     * @return The next 2 bytes read as a short
     */
    private int readUnsignedShort()
        throws IOException
    {
        int ch1 = inputStream.readUnsignedByte();
        int ch2 = inputStream.readUnsignedByte();

        if((ch1 | ch2) < 0)
            throw new EOFException();

        return ch1 + (ch2 << 8);
    }

    /**
     * Reader for floats to swap the byte order from that normally ready be
     * Java.
     *
     * @return The next 4 bytes read as a float
     */
    private float readFloat()
        throws IOException
    {
        int ch1 = inputStream.readUnsignedByte();
        int ch2 = inputStream.readUnsignedByte();
        int ch3 = inputStream.readUnsignedByte();
        int ch4 = inputStream.readUnsignedByte();

        if((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();

        return Float.intBitsToFloat((ch1 << 0) + (ch2 << 8) +
                  (ch3 << 16) + (ch4 << 24));
    }

    /**
     * Guarantee to skip the given length of bytes. InputStream.skip() is not
     * guaranteed to skip all the bytes requested (eg lack of bytes left in the
     * buffer. This will loop to make sure that all the bytes have been skipped
     * as requested.
     *
     * @param numBytes The number of bytes to skip
     */
    private void skipBytes(int numBytes)
        throws IOException
    {
        if(numBytes == 0)
            return;

        int skipped = (int)inputStream.skip(numBytes);

        if(skipped != numBytes)
        {
            int total = skipped;
            while(total != numBytes)
            {
                total += inputStream.skip(numBytes - total);
            }
        }
    }
}
