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
 * @version $Revision: 1.3 $
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
     * @param returnHeights true if this should return the array of height values
     * @return An array of the heights if requested or null if not
     * @throws IncorrectFormatException The file is not one our parser
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public ObjectMesh parse()
        throws IOException
    {
        if(dataReady)
            throw new IOException("Data has already been read from this stream");

        parseMain();

        // once parsed, generate all the normals etc
        for(int i = 0; i < decodedMesh.numBlocks; i++)
        {
            ObjectBlock block = decodedMesh.blocks[i];
            for(int j = 0; j < block.numMeshes; j++)
                calcNormals(block.meshes[j]);
        }

        dataReady = true;

        return decodedMesh;
    }

    /**
     * Parse the type A reccord that belongs to this file.
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
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for main mesh");
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

                default:
                    System.out.println("Unknown mesh chunk ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for object mesh");
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
//System.out.println("tri mesh");
                    readTriMesh(size - 6, block);
                    break;

                case MaxConstants.DIRECTIONAL_LIGHT:
//System.out.println("light");
                    readLightBlock(size - 6, block);
                    break;

                case MaxConstants.N_CAMERA:
//System.out.println("camera");
                    readCameraBlock(size - 6, block);
                    break;

                default:
                    System.out.println("Unknown object block chunk ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for object block");
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

            int cnt = 0;

            switch(type)
            {
                case MaxConstants.VERTEX_LIST:
                    mesh.numVertex = readUnsignedShort();
                    mesh.vertex = new float[mesh.numVertex * 3];

                    for(int i = 0; i < mesh.numVertex; i++)
                    {
                        mesh.vertex[cnt++] = readFloat();
                        mesh.vertex[cnt++] = readFloat();
                        mesh.vertex[cnt++] = readFloat();
                    }
                    break;

                case MaxConstants.TEXCOORD_LIST:
                    mesh.numTexCoords = readUnsignedShort();
                    mesh.texCoords = new float[mesh.numTexCoords * 2];

                    for(int i = 0; i < mesh.numTexCoords; i++)
                    {
                        mesh.texCoords[cnt++] = readFloat();
                        mesh.texCoords[cnt++] = readFloat();
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

                default:
                    System.out.println("Unknown trimesh chunk ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for triangle mesh");
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
        data.numFace = readUnsignedShort();
        data.face = new int[data.numFace * 3];

        for(int i = 0; i < data.numFace; i++)
        {
            data.face[cnt++] = readUnsignedShort();
            data.face[cnt++] = readUnsignedShort();
            data.face[cnt++] = readUnsignedShort();
            readUnsignedShort();
        }

        // num faces * sizeof(unsigned short) * 4 shorts per face
        // plus 2 bytes for numFaces int and 6 bytes for the
        // chunk ID + size indicator
        int bytes_read = data.numFace * 2 * 4 + 2;

        if(bytes_read < bytesToRead)
        {
            while(bytes_read < bytesToRead)
            {
                int type = readUnsignedShort();
                int size = readInt();

                switch(type)
                {
                    case MaxConstants.SMOOTH_LIST:
                        data.smoothgroup = new int[data.numFace];

                        for(int i = 0; i < data.numFace; i++)
                            data.smoothgroup[i] = readInt();
                        break;

                    case MaxConstants.MATERIAL_LIST:
                        readMaterialList(size - 6, data);
                        break;

                    default:
                        System.out.println("Unknown subface list ID 0x" +
                                           Integer.toHexString(type));
                        inputStream.skip(size - 6);
                }

                bytes_read += size;
            }

            if(bytes_read != bytesToRead)
                 System.out.println("Not enough bytes in file for face list");
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

        // TODO: should confirm number of bytes read here.
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

        int bytes_read = readColor(light.color);
        light.direction[0] = readFloat();
        light.direction[1] = readFloat();
        light.direction[2] = readFloat();

        bytes_read += 12;

        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
                case MaxConstants.SPOT_LIGHT:
                    light.type = LightBlock.SPOT_LIGHT;
                    light.target = new float[3];
                    light.target[0] = readFloat();
                    light.target[1] = readFloat();
                    light.target[2] = readFloat();
                    light.hotspotAngle = readFloat();
                    light.falloffAngle = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_OFF:
                    int enable = inputStream.readUnsignedByte();
                    light.enabled = (enable != 0);
                    break;

                case MaxConstants.DIR_LIGHT_ATTENUATION:
                    light.attenuation = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_SPOT_ROLLOFF:
                    light.rollAngle = readFloat();
                    break;

                case MaxConstants.DIR_LIGHT_SPOT_ASPECTRATIO:
                    light.aspectRatio = readFloat();
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

                default:
                    System.out.println("Unknown light block ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }
    }

    /**
     * Read all a Camera block.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The mesh object to put everything into
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

        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            switch(type)
            {
//                case VERTEX_LIST:
                default:
                    System.out.println("Unknown camera block ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }
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
            ObjectBlock[] tmp = new ObjectBlock[data.numMaterials + 8];
            if(data.numMaterials != 0)
                System.arraycopy(data.materials, 0, tmp, 0, data.numMaterials);
            data.blocks = tmp;
        }

        MaterialBlock mat = new MaterialBlock();
        data.materials[data.numMaterials] = mat;
        data.numMaterials++;

        int bytes_read = 0;

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
                    readColor(mat.ambientColor);
                    break;

                case MaxConstants.MAT_DIFFUSE:
                    mat.diffuseColor = new float[3];
                    readColor(mat.diffuseColor);
                    break;

                case MaxConstants.MAT_SPECULAR:
                    mat.specularColor = new float[3];
                    readColor(mat.specularColor);
                    break;

                case MaxConstants.MAT_SHININESS:
                    mat.shininess = readPercentage();
                    break;

                case MaxConstants.MAT_TRANSPARENCY:
                    mat.shininess = readPercentage();
                    break;

                case MaxConstants.MAT_TWO_SIDE:
                    int enable = inputStream.readUnsignedByte();
                    mat.twoSidedLighting = (enable != 0);
                    break;

                default:
                    System.out.println("Unknown material block chunk ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for material block");
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
                    readNodeTag(size - 6, keyframe);
                    break;

                default:
                    System.out.println("Unknown keyframe chunk ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for keyframe chunk");
    }

    /**
     * Read the a node tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param data The object block to put everything into
     */
    private void readNodeTag(int bytesToRead, KeyframeBlock data)
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
                    readPositionTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_ROTATION_TRACK_TAG:
                    readRotationTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_SCALE_TRACK_TAG:
                    readScaleTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_FOV_TRACK_TAG:
                    readFieldOfViewTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_ROLL_TRACK_TAG:
                    readRolloffTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_COLOR_TRACK_TAG:
                    readColorTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_MORPH_TRACK_TAG:
                    readMorphTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_HOTSPOT_TRACK_TAG:
                    readHotspotTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_FALLOFF_TRACK_TAG:
                    readFalloffTag(size - 6, frame);
                    break;

                case MaxConstants.KEYFRAME_PIVOT:
                    frame.pivotPoint = new float[3];
                    frame.pivotPoint[0] = readFloat();
                    frame.pivotPoint[1] = readFloat();
                    frame.pivotPoint[2] = readFloat();
                    break;

                default:
                    System.out.println("Unknown keyframe frame ID 0x" +
                                       Integer.toHexString(type));
                    inputStream.skip(size - 6);
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for keyframe frame");
    }

    /**
     * Read the a node header for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The frame to put everything into
     */
    private void readNodeHeader(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        frame.nodeHeader = new NodeHeaderData();
        frame.nodeHeader.name = readString();
        frame.nodeHeader.flags1 = readUnsignedShort();
        frame.nodeHeader.flags2 = readUnsignedShort();
        frame.nodeHeader.heirarchyPosition = readUnsignedShort();
    }

    /**
     * Read a track position tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The frame to put everything into
     */
    private void readPositionTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.positions == null) ||
            frame.positions.length == frame.numPositions)
        {
            KeyframePositionBlock[] tmp = new KeyframePositionBlock[frame.numPositions + 8];

            if(frame.numPositions != 0)
                System.arraycopy(frame.positions, 0, tmp, 0, frame.numPositions);
            frame.positions = tmp;
        }

        KeyframePositionBlock pos = new KeyframePositionBlock();
        frame.positions[frame.numPositions] = pos;
        frame.numPositions++;

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

            pd.x = readFloat();
            pd.y = readFloat();
            pd.z = readFloat();
        }
    }

    /**
     * Read a track position tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The frame to put everything into
     */
    private void readRotationTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.rotations == null) ||
            frame.rotations.length == frame.numRotations)
        {
            KeyframeRotationBlock[] tmp = new KeyframeRotationBlock[frame.numRotations + 8];

            if(frame.numRotations != 0)
                System.arraycopy(frame.rotations, 0, tmp, 0, frame.numRotations);
            frame.rotations = tmp;
        }

        KeyframeRotationBlock rot = new KeyframeRotationBlock();
        frame.rotations[frame.numRotations] = rot;
        frame.numRotations++;

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

            rd.rotation = readFloat();
            rd.xAxis = readFloat();
            rd.yAxis = readFloat();
            rd.zAxis = readFloat();
        }
    }

    /**
     * Read a track scale tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The frame to put everything into
     */
    private void readScaleTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.scales == null) ||
            frame.scales.length == frame.numScales)
        {
            KeyframeScaleBlock[] tmp = new KeyframeScaleBlock[frame.numScales + 8];

            if(frame.numScales != 0)
                System.arraycopy(frame.scales, 0, tmp, 0, frame.numScales);
            frame.scales = tmp;
        }

        KeyframeScaleBlock scale = new KeyframeScaleBlock();
        frame.scales[frame.numScales] = scale;
        frame.numScales++;

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

            sd.xScale = readFloat();
            sd.yScale = readFloat();
            sd.zScale = readFloat();
        }
    }

    /**
     * Read a track fov tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The frame to put everything into
     */
    private void readFieldOfViewTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.fovs == null) ||
            frame.fovs.length == frame.numFOVs)
        {
            KeyframeFOVBlock[] tmp = new KeyframeFOVBlock[frame.numFOVs + 8];

            if(frame.numFOVs != 0)
                System.arraycopy(frame.fovs, 0, tmp, 0, frame.numFOVs);
            frame.fovs = tmp;
        }

        KeyframeFOVBlock fov = new KeyframeFOVBlock();
        frame.fovs[frame.numFOVs] = fov;
        frame.numFOVs++;

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
     * @param frame The frame to put everything into
     */
    private void readRolloffTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.rolls == null) || frame.rolls.length == frame.numRolls)
        {
            KeyframeRollBlock[] tmp = new KeyframeRollBlock[frame.numRolls + 8];

            if(frame.numRolls != 0)
                System.arraycopy(frame.rolls, 0, tmp, 0, frame.numRolls);
            frame.rolls = tmp;
        }

        KeyframeRollBlock rolloff = new KeyframeRollBlock();
        frame.rolls[frame.numRolls] = rolloff;
        frame.numRolls++;

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
     * @param frame The frame to put everything into
     */
    private void readColorTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.colors == null) ||
            frame.colors.length == frame.numColors)
        {
            KeyframeColorBlock[] tmp = new KeyframeColorBlock[frame.numColors + 8];

            if(frame.numColors != 0)
                System.arraycopy(frame.colors, 0, tmp, 0, frame.numColors);
            frame.colors = tmp;
        }

        KeyframeColorBlock color = new KeyframeColorBlock();
        frame.colors[frame.numColors] = color;
        frame.numColors++;

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
     * @param frame The frame to put everything into
     */
    private void readMorphTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.morphs == null) ||
            frame.morphs.length == frame.numMorphs)
        {
            KeyframeMorphBlock[] tmp = new KeyframeMorphBlock[frame.numMorphs + 8];

            if(frame.numMorphs != 0)
                System.arraycopy(frame.morphs, 0, tmp, 0, frame.numMorphs);
            frame.morphs = tmp;
        }

        KeyframeMorphBlock morph = new KeyframeMorphBlock();
        frame.morphs[frame.numMorphs] = morph;
        frame.numMorphs++;

        morph.flags = readUnsignedShort();
        readInt();
        readInt();
        morph.numKeys = readInt();
        morph.morphs = new MorphData[morph.numKeys];

        for(int i = 0; i < morph.numKeys; i++)
        {
            MorphData md = new MorphData();
            morph.morphs[i] = md;

            readTrackData(md);

            md.objectName = readString();
        }
    }

    /**
     * Read a track hotspot tag for the given keyframe.
     *
     * @param bytesToRead number of bytes requiring processing
     * @param frame The frame to put everything into
     */
    private void readHotspotTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.hotspots == null) ||
            frame.hotspots.length == frame.numHotspots)
        {
            KeyframeHotspotBlock[] tmp = new KeyframeHotspotBlock[frame.numHotspots + 8];

            if(frame.numHotspots != 0)
                System.arraycopy(frame.hotspots, 0, tmp, 0, frame.numHotspots);
            frame.hotspots = tmp;
        }

        KeyframeHotspotBlock hotspot = new KeyframeHotspotBlock();
        frame.hotspots[frame.numHotspots] = hotspot;
        frame.numHotspots++;

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
     * @param frame The frame to put everything into
     */
    private void readFalloffTag(int bytesToRead, KeyframeFrameBlock frame)
        throws IOException
    {
        if((frame.falloffs == null) ||
            frame.falloffs.length == frame.numFalloffs)
        {
            KeyframeFalloffBlock[] tmp = new KeyframeFalloffBlock[frame.numFalloffs + 8];

            if(frame.numFalloffs != 0)
                System.arraycopy(frame.falloffs, 0, tmp, 0, frame.numFalloffs);
            frame.falloffs = tmp;
        }

        KeyframeFalloffBlock falloff = new KeyframeFalloffBlock();
        frame.falloffs[frame.numFalloffs] = falloff;
        frame.numFalloffs++;

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
     */
    private void readTrackData(TrackData data)
        throws IOException
    {
        data.frameNumber = readInt();
        data.splineFlags = readUnsignedShort();

        if(data.splineFlags != 0)
        {
            data.splineData = new float[5];

            // Bit 0 set
            if((data.splineFlags & 0x01) != 0)
                data.splineData[0] = readFloat();

            // Bit 1 set
            if((data.splineFlags & 0x02) != 0)
                data.splineData[1] = readFloat();

            // Bit 2 set
            if((data.splineFlags & 0x04) != 0)
                data.splineData[2] = readFloat();

            // Bit 3 set
            if((data.splineFlags & 0x08) != 0)
                data.splineData[3] = readFloat();

            // Bit 4 set
            if((data.splineFlags & 0x10) != 0)
                data.splineData[4] = readFloat();
        }
    }

    // Generic internal methods.

    private void calcNormals(TriangleMesh mesh)
    {
        float[] normal_face = new float[mesh.numFace * 3];
        float[] tangent_face = new float[mesh.numFace * 3];
        float[] binormal_face = new float[mesh.numFace * 3];

        int[] face = mesh.face;
        int[] vertex_count = new int[mesh.numVertex];
        int[][] vertex_face = new int[mesh.numVertex][];

        float[] vertex = mesh.vertex;
        float[] tex_coords = mesh.texCoords;
        int[] smoothgroup = mesh.smoothgroup;

        mesh.normal = new float[mesh.numFace * 9];
        mesh.tangent = new float[mesh.numFace * 9];
        mesh.binormal = new float[mesh.numFace * 9];
        float[] normal = mesh.normal;
        float[] tangent = mesh.tangent;
        float[] binormal = mesh.binormal;

        if(tex_coords == null)
        {
            mesh.texCoords = new float[mesh.numVertex * 2];
            tex_coords = mesh.texCoords;
        }

        for(int i = 0; i < mesh.numFace; i++)
        {
            int j = i * 3;
            int v0 = face[j + 0];
            int v1 = face[j + 1];
            int v2 = face[j + 2];
            vertex_count[v0]++;
            vertex_count[v1]++;
            vertex_count[v2]++;

            float e0_x = vertex[v1 * 3] - vertex[v0 * 3];
            float e0_y = vertex[v1 * 3 + 1] - vertex[v0 * 3 + 1];
            float e0_z = vertex[v1 * 3 + 2] - vertex[v0 * 3 + 2];

            float e1_x = vertex[v2 * 3] - vertex[v0 * 3];
            float e1_y = vertex[v2 * 3 + 1] - vertex[v0 * 3 + 1];
            float e1_z = vertex[v2 * 3 + 2] - vertex[v0 * 3 + 2];

            float cp_x = e0_y * e1_z - e0_z * e1_y;
            float cp_y = e0_z * e1_x - e0_x * e1_z;
            float cp_z = e0_x * e1_y - e0_y * e1_x;

            float d = cp_x * cp_x + cp_y * cp_y + cp_z * cp_z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                normal_face[i * 3] = cp_x * d;
                normal_face[i * 3 + 1] = cp_y * d;
                normal_face[i * 3 + 2] = cp_z * d;
            }

            e0_y = tex_coords[v1 * 2] - tex_coords[v0 * 2];
            e0_z = tex_coords[v1 * 2 + 1] - tex_coords[v0 * 2 + 1];

            e1_y = tex_coords[v2 * 2] - tex_coords[v0 * 2];
            e1_z = tex_coords[v2 * 2 + 1] - tex_coords[v0 * 2 + 1];

            for(int k = 0; k < 3; k++)
            {
                e0_x = vertex[v1 * 3 + k] - vertex[v0 * 3 + k];
                e1_x = vertex[v2 * 3 + k] - vertex[v0 * 3 + k];

                cp_x = e0_y * e1_z - e0_z * e1_y;
                cp_y = e0_z * e1_x - e0_x * e1_z;
                cp_z = e0_x * e1_y - e0_y * e1_x;

                tangent_face[i * 3 + k] = -cp_y / cp_x;
                binormal_face[i * 3 + k] = -cp_z / cp_x;
            }

            float x = tangent_face[i * 3];
            float y = tangent_face[i * 3 + 1];
            float z = tangent_face[i * 3 + 2];
            d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                tangent_face[i * 3] *= d;
                tangent_face[i * 3 + 1] *= d;
                tangent_face[i * 3 + 2] *= d;
            }

            x = binormal_face[i * 3];
            y = binormal_face[i * 3 + 1];
            z = binormal_face[i * 3 + 2];
            d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                binormal_face[i * 3] *= d;
                binormal_face[i * 3 + 1] *= d;
                binormal_face[i * 3 + 2] *= d;
            }

            float n_x = tangent_face[i * 3 + 1] * binormal_face[i * 3 + 2] -
                        tangent_face[i * 3 + 2] * binormal_face[i * 3 + 1];
            float n_y = tangent_face[i * 3 + 2] * binormal_face[i * 3] -
                        tangent_face[i * 3] * binormal_face[i * 3 + 2];
            float n_z = tangent_face[i * 3] * binormal_face[i * 3 + 1] -
                        tangent_face[i * 3 + 1] * binormal_face[i * 3];

            d = n_x * n_x + n_y * n_y + n_z * n_z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                n_x *= d;
                n_y *= d;
                n_z *= d;
            }

            binormal_face[i * 3] = n_y * tangent_face[i * 3 + 2] -
                                   n_z * tangent_face[i * 3 + 1];

            binormal_face[i * 3 + 1] = n_z * tangent_face[i * 3] -
                                       n_x * tangent_face[i * 3 + 2];
            binormal_face[i * 3 + 2] = n_x * tangent_face[i * 3 + 1] -
                                       n_y * tangent_face[i * 3];
        }

        for(int i = 0; i < mesh.numVertex; i++)
        {
            vertex_face[i] = new int[vertex_count[i] + 1];
            vertex_face[i][0] = vertex_count[i];
        }

        for(int i = 0; i < mesh.numFace; i++)
        {
            int j = i * 3;
            int v0 = face[j + 0];
            int v1 = face[j + 1];
            int v2 = face[j + 2];
            vertex_face[v0][vertex_count[v0]--] = i;
            vertex_face[v1][vertex_count[v1]--] = i;
            vertex_face[v2][vertex_count[v2]--] = i;
        }

        boolean do_smooth = (smoothgroup != null);

        for(int i = 0; i < mesh.numFace; i++)
        {
            int j = i * 3;
            int v0 = face[j + 0];
            int v1 = face[j + 1];
            int v2 = face[j + 2];

            for(int k = 1; k <= vertex_face[v0][0]; k++)
            {
                int l = vertex_face[v0][k];
                if(l == i || (do_smooth && ((smoothgroup[i] & smoothgroup[l]) != 0)))
                {
                    int p1 = j * 3;
                    int l1 = l * 3;

                    normal[p1] += normal_face[l1];
                    normal[p1 + 1] += normal_face[l1 + 1];
                    normal[p1 + 2] += normal_face[l1 + 2];

                    tangent[p1] += tangent_face[l1];
                    tangent[p1 + 1] += tangent_face[l1 + 1];
                    tangent[p1 + 2] += tangent_face[l1 + 2];

                    binormal[p1] += binormal_face[l1];
                    binormal[p1 + 1] += binormal_face[l1 + 1];
                    binormal[p1 + 2] += binormal_face[l1 + 2];
                }
            }

            for(int k = 1; k <= vertex_face[v1][0]; k++)
            {
                int l = vertex_face[v1][k];
                if(l == i || (do_smooth && ((smoothgroup[i] & smoothgroup[l]) != 0)))
                {
                    int p1 = (j + 1) * 3;
                    int l1 = l * 3;

                    normal[p1] += normal_face[l1];
                    normal[p1 + 1] += normal_face[l1 + 1];
                    normal[p1 + 2] += normal_face[l1 + 2];

                    tangent[p1] += tangent_face[l1];
                    tangent[p1 + 1] += tangent_face[l1 + 1];
                    tangent[p1 + 2] += tangent_face[l1 + 2];

                    binormal[p1] += binormal_face[l1];
                    binormal[p1 + 1] += binormal_face[l1 + 1];
                    binormal[p1 + 2] += binormal_face[l1 + 2];
                }
            }

            for(int k = 1; k <= vertex_face[v2][0]; k++)
            {
                int l = vertex_face[v2][k];
                if(l == i || (do_smooth && ((smoothgroup[i] & smoothgroup[l]) != 0)))
                {
                    int p1 = (j + 2) * 3;
                    int l1 = l * 3;

                    normal[p1] += normal_face[l1];
                    normal[p1 + 1] += normal_face[l1 + 1];
                    normal[p1 + 2] += normal_face[l1 + 2];

                    tangent[p1] += tangent_face[l1];
                    tangent[p1 + 1] += tangent_face[l1 + 1];
                    tangent[p1 + 2] += tangent_face[l1 + 2];

                    binormal[p1] += binormal_face[l1];
                    binormal[p1 + 1] += binormal_face[l1 + 1];
                    binormal[p1 + 2] += binormal_face[l1 + 2];
                }
            }
        }

        int num_calc = mesh.numFace * 3;
        for(int i = 0; i < num_calc; i++)
        {
            float x = normal[i * 3];
            float y = normal[i * 3 + 1];
            float z = normal[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                normal[i * 3] *= d;
                normal[i * 3 + 1] *= d;
                normal[i * 3 + 2] *= d;
            }
        }

        for(int i = 0; i < num_calc; i++)
        {
            float x = tangent[i * 3];
            float y = tangent[i * 3 + 1];
            float z = tangent[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                tangent[i * 3] *= d;
                tangent[i * 3 + 1] *= d;
                tangent[i * 3 + 2] *= d;
            }
        }

        for(int i = 0; i < num_calc; i++)
        {
            float x = binormal[i * 3];
            float y = binormal[i * 3 + 1];
            float z = binormal[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                binormal[i * 3] *= d;
                binormal[i * 3 + 1] *= d;
                binormal[i * 3 + 2] *= d;
            }
        }
    }

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

            default:
                System.out.println("Unknown percentage chunk ID 0x" +
                                   Integer.toHexString(type));
                inputStream.skip(size - 6);
        }

        return ret_val;
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
                target[0] = val *= 0.0039215f;   // 1/255 for range conv

                val = inputStream.readUnsignedByte();
                target[1] = val *= 0.0039215f;

                val = inputStream.readUnsignedByte();
                target[2] = val *= 0.0039215f;
                break;

            default:
                System.out.println("Unknown colour chunk ID 0x" +
                                   Integer.toHexString(type));
                inputStream.skip(size - 6);
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
}
