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
import java.util.HashMap;
import java.util.LinkedList;

// Local imports
import org.j3d.io.BlockDataInputStream;

/**
 * A low-level parser for the Discreet 3DS Max file format.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class MaxParser
{
    private static final int CHUNK_MAIN = 0x4d4d;
    private static final int CHUNK_OBJECT_MESH = 0x3d3d;
    private static final int CHUNK_OBJECT_BLOCK = 0x4000;
    private static final int CHUNK_TRIMESH = 0x4100;
    private static final int CHUNK_VERTEX_LIST = 0x4110;
    private static final int CHUNK_FACE_LIST = 0x4120;
    private static final int CHUNK_MAP_LIST = 0x4140;
    private static final int CHUNK_SMOOTH_LIST = 0x4150;

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

        if(type != CHUNK_MAIN)
            throw new IOException("Wrong main chunk in file");

        ObjectMesh mesh = new ObjectMesh();
        readMain(size - 6, mesh);

        decodedMesh = mesh;
    }

    /**
     * Process all the object meshes in the file.
     */
    private void readMain(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            if(type != CHUNK_OBJECT_MESH)
                inputStream.skip(size - 6);
            else
                readMeshes(size - 6, data);

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for main mesh");
    }

    /**
     * Process all the object meshes in the file.
     */
    private void readMeshes(int bytesToRead, ObjectMesh data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            if(type != CHUNK_OBJECT_BLOCK)
                inputStream.skip(size - 6);
            else
                processObjectMesh(size - 6, data);

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for object mesh");
    }

    private void processObjectMesh(int size, ObjectMesh data)
        throws IOException
    {
        String name = readString();
        int mesh_size = size - name.length() - 1;

        if(data.blocks.length == data.numBlocks)
        {
            ObjectBlock[] tmp = new ObjectBlock[data.numBlocks + 8];
            System.arraycopy(data.blocks, 0, tmp, 0, data.numBlocks);
            data.blocks = tmp;
        }

        data.blocks[data.numBlocks] = new ObjectBlock();
        data.numBlocks++;

        ObjectBlock block = data.blocks[data.numBlocks - 1];
        block.name = name;

        readObjectBlock(mesh_size, block);
    }

    /**
     * Read all the object blocks for this mesh
     */
    private void readObjectBlock(int bytesToRead, ObjectBlock data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            if(type != CHUNK_TRIMESH)
                inputStream.skip(size - 6);
            else
                processObjectBlock(size - 6, data);

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for object block");
    }

    private void processObjectBlock(int size, ObjectBlock data)
        throws IOException
    {
        if(data.meshes.length == data.numMeshes)
        {
            TriangleMesh[] tmp = new TriangleMesh[data.numMeshes + 8];
            System.arraycopy(data.meshes, 0, tmp, 0, data.numMeshes);
            data.meshes = tmp;
        }

        data.meshes[data.numMeshes] = new TriangleMesh();
        data.numMeshes++;

        readTriMesh(size, data.meshes[data.numMeshes - 1]);
    }

    /**
     * Read all the object blocks for this mesh
     */
    private void readTriMesh(int bytesToRead, TriangleMesh data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            if(!processTriMesh(type, size - 6, data))
                inputStream.skip(size - 6);

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for triangle mesh");
    }

    private boolean processTriMesh(int type, int bytesToRead, TriangleMesh data)
        throws IOException
    {
        boolean ret_val = false;
        int cnt = 0;

        switch(type)
        {
            case CHUNK_VERTEX_LIST:
                data.numVertex = readUnsignedShort();
                data.vertex = new float[data.numVertex * 3];

                for(int i = 0; i < data.numVertex; i++)
                {
                    data.vertex[cnt++] = readFloat();
                    data.vertex[cnt++] = readFloat();
                    data.vertex[cnt++] = readFloat();
                }
                ret_val = true;
                break;

            case CHUNK_MAP_LIST:
                data.num_st = readUnsignedShort();
                data.st = new float[data.num_st * 2];

                for(int i = 0; i < data.num_st; i++)
                {
                    data.st[cnt++] = readFloat();
                    data.st[cnt++] = 1 - readFloat();
                }
                ret_val = true;
                break;

            case CHUNK_FACE_LIST:
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
                int bytes_left = bytesToRead - data.numFace * 2 * 4 - 2;

                if(bytes_left > 0)
                    readSmoothList(bytes_left, data);

                ret_val = true;
        }

        return ret_val;
    }

    /**
     * Read all the object blocks for this mesh
     */
    private void readSmoothList(int bytesToRead, TriangleMesh data)
        throws IOException
    {
        int bytes_read = 0;
        while(bytes_read < bytesToRead)
        {
            int type = readUnsignedShort();
            int size = readInt();

            if(type != CHUNK_SMOOTH_LIST)
                inputStream.skip(size - 6);
            else
            {
                data.smoothgroup = new int[data.numFace];

                for(int i = 0; i < data.numFace; i++)
                    data.smoothgroup[i] = readInt();
            }

            bytes_read += size;
        }

        if(bytes_read != bytesToRead)
             System.out.println("Not enough bytes in file for triangle mesh");
    }

    private void calcNormals(TriangleMesh mesh)
    {
        float[] normal_face = new float[mesh.numFace * 3];
        float[] tangent_face = new float[mesh.numFace * 3];
        float[] binormal_face = new float[mesh.numFace * 3];

        int[] face = mesh.face;
        int[] vertex_count = new int[mesh.numVertex];
        int[][] vertex_face = new int[mesh.numVertex][];

        float[] vertex = mesh.vertex;
        float[] st = mesh.st;
        int[] smoothgroup = mesh.smoothgroup;

        mesh.normal = new float[mesh.numFace * 9];
        mesh.tangent = new float[mesh.numFace * 9];
        mesh.binormal = new float[mesh.numFace * 9];
        float[] normal = mesh.normal;
        float[] tangent = mesh.tangent;
        float[] binormal = mesh.binormal;

        if(st == null)
        {
            mesh.st = new float[mesh.numVertex * 2];
            mesh.num_st = mesh.numVertex;
            st = mesh.st;
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

            e0_y = st[v1 * 2] - st[v0 * 2];
            e0_z = st[v1 * 2 + 1] - st[v0 * 2 + 1];

            e1_y = st[v2 * 2] - st[v0 * 2];
            e1_z = st[v2 * 2 + 1] - st[v0 * 2 + 1];

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
