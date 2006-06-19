/*****************************************************************************
 *                            (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.c3d;

// External imports
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;

// Local parser
import org.j3d.io.BlockDataInputStream;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.IntHashMap;

/**
 * A low-level parser for the C3D file format.
 * <p>
 *
 * The output of this parser is the parameter block and streams of data
 * expressed as Java float arrays.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.c3d.org">http://www.c3d.org/</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class C3DParser
{
    /** Message when the 2nd byte is not 0x50. */
    private static final String INVALID_FILE_FORMAT =
        "The stream does not represent a C3D file. The binary ID is incorrect";

    /**
     * Message when the user code generates an exception in observer callback
     * for headers.
     */
    private static final String USER_HEADER_ERR =
        "User code implementing C3DParseObserver has generated an exception " +
        "during the headerComplete() callback.";

    /**
     * Message when the user code generates an exception in observer callback
     * for parameter groups.
     */
    private static final String USER_PARAM_ERR =
        "User code implementing C3DParseObserver has generated an exception " +
        "during the parametersComplete() callback.";

    /** Buffer while reading bytes from the stream */
    private byte[] buffer;

    /**
     * The stream used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private InputStream inputStream;

    /** The number of the current block that we're reading. */
    private int currentBlock;

    /** The byte-swapping reader for processing the file */
    private BufferReader reader;

    /** The processed file header */
    private C3DHeader header;

    /** The processed set of parameter groups */
    private C3DParameterGroup[] parameters;

    /** The track data. One for each tracked object */
    private C3DTrajectoryData[] trajectories;

    /** The observer of updates, if registered */
    private C3DParseObserver observer;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /**
     * Construct a new parser with no stream set.
     */
    public C3DParser()
    {
        // Equivalent to the block size of C3D.
        buffer = new byte[512];
        currentBlock = 0;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Construct a new parser using the given stream to source the data from.
     *
     * @param is The stream to read data from
     */
    public C3DParser(InputStream is)
    {
        this();

        InputStream stream;

        if(is instanceof BufferedInputStream)
            stream = (BufferedInputStream)is;
        else
            stream = new BufferedInputStream(is);

        inputStream = new BlockDataInputStream(stream);
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
     * Get the last parsed file header information. If this parser has been
     * cleared or the user requested that data not be retained, this will
     * return null.
     *
     * @return The header of the last-read stream or null
     */
    public C3DHeader getHeader()
    {
        return header;
    }

    /**
     * Get the last parsed set of parameter groups. If this parser has been
     * cleared or the user requested that data not be retained, this will
     * return null. Otherwise, the array will contain the exact number of
     * parameter groups read.
     *
     * @return The set of groups from the last-read stream, or null
     */
    public C3DParameterGroup[] getParameters()
    {
        return parameters;
    }

    /**
     * Get the last parsed set of trajectories. If this parser has been
     * cleared or the user requested that data not be retained, this will
     * return null. Otherwise, the array will contain the exact number of
     * trajectories read.
     *
     * @return The set of groups from the last-read stream, or null
     */
    public C3DTrajectoryData[] getTrajectories()
    {
        return trajectories;
    }

    /**
     * Force a clear of the data that has been previous read by this parser.
     */
    public void clear()
    {
        header = null;
        parameters = null;
        trajectories = null;
        currentBlock = 0;
        inputStream = null;
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param is The new stream to use
     */
    public void reset(InputStream is)
    {
        InputStream stream;

        if(is instanceof BufferedInputStream)
            stream = (BufferedInputStream)is;
        else
            stream = new BufferedInputStream(is);

        inputStream = new BlockDataInputStream(stream);

        header = null;
        currentBlock = 0;
    }

    /**
     * Parse the stream now and start generating the output. This will
     * automatically have the first parameter block queued in the reader
     * because we need it to know which file format type is to be processed.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @throws IOException some problem reading the basic file.
     */
    public void parse(boolean retainData) throws IOException
    {
        // The header data is always kept until parsing is complete because it
        // tells us how to find chunks of the rest of the file.
        if(parseHeader() && parseParams(retainData))
                parseTrajectories(retainData);

        if(!retainData)
            header = null;
    }

    /**
     * Parse the header block from the file.
     *
     * @return true if the parsing should continue to the next section
     * @throws IOException some problem reading the basic file.
     */
    private boolean parseHeader() throws IOException
    {
        // Read the first blocks of 512 bytes

        // First two bytes are a pointer to the start of the parameter block,
        // followed, by 0x50. Look for the magic number first.
        readBlocks(1);

        if(buffer[1] != 0x50)
            throw new IOException(INVALID_FILE_FORMAT);

        // We need to read the first block of the parameter section to find
        // out what format the numbers are in.
        int param_block = buffer[0] & 0xFF;

        byte[] param_buffer = buffer;
        buffer = new byte[512];

        for(int i = 1; i < param_block; i++)
            readBlocks(1);

        // 83 decimal + processor type.
        // Processor type 1 = Intel
        // Processor type 2 = DEC (VAX, PDP-11)
        // Processor type 3 = MIPS processor (SGI/MIPS)
        int proc_type = buffer[3] - 83;

        switch(proc_type)
        {
            case 1:
                reader = new IntelBufferReader();
                break;

            case 2:
                reader = new DECBufferReader();
                break;

            case 3:
                reader = new MIPSBufferReader();
                break;
        }

        byte[] tmp = param_buffer;
        param_buffer = buffer;
        buffer = tmp;

        reader.setBuffer(buffer);

        header = new C3DHeader();
        header.processorType = proc_type;
        header.startParamBlock = param_block;

        header.numTrajectories = reader.readShort(2);
        header.numAnalogChannels = reader.readShort(4);

        header.start3DFrame = reader.readShort(6) - 1;
        header.end3DFrame = reader.readShort(8) - 1;

        header.maxInterpolationGap = reader.readShort(10);
        header.scaleFactor = reader.readFloat(12);
        header.startDataBlock = reader.readShort(16);
        header.numAnalogSamplesPer3DFrame = reader.readShort(18);
        header.trajectorySampleRate = reader.readFloat(20);

        // A key value (12345 decimal) is written here if Label and Range data is
        // present, otherwise write 0x00.
        header.hasRangeData = (reader.readShort(294) == 12345);
        header.rangeDataStart = reader.readShort(296);

        // A key value (12345 decimal) present if this file supports 4 char
        // event labels. An older format supported only 2 character labels.
        boolean old_labels = (reader.readShort(298) != 12345);

        header.numTimeEvents = reader.readShort(300);

        // Probably want to bitch if this is greater than 18, but for now,
        // we'll just just truncate.
        if(header.numTimeEvents > 18)
            header.numTimeEvents = 18;

        header.eventTimes = new float[header.numTimeEvents];
        header.eventLabels = new String[header.numTimeEvents];
        header.eventDisplayFlag = new boolean[header.numTimeEvents];

        for(int i = 0; i < header.numTimeEvents; i++)
            header.eventTimes[i] = reader.readFloat(304 + (i << 2));

        // Read display flags. Single byte each. 0x00 is on, 0x01 is off.
        // ie backwards to normal C conventions.
        for(int i = 0; i < header.numTimeEvents; i++)
            header.eventDisplayFlag[i] = buffer[376 + i] == 0;

        for(int i = 0; i < header.numTimeEvents; i++)
            header.eventLabels[i] = reader.readString(396 + (i << 2), 4);

        buffer = param_buffer;
        reader.setBuffer(buffer);

        // Readjust the read values to be more useful
        header.numAnalogChannels /= header.numAnalogSamplesPer3DFrame;
        header.analogSampleRate = header.trajectorySampleRate *
                                  header.numAnalogSamplesPer3DFrame;

        boolean ret_val = true;

        if(observer != null)
        {
            try
            {
                ret_val = observer.headerComplete(header);
            }
            catch(Exception e)
            {
                errorReporter.errorReport(USER_HEADER_ERR, e);
            }
        }

        return ret_val;
    }

    /**
     * Parse the parameter block(s).
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @return true if the parsing should continue to the next section
     * @throws IOException some problem reading the basic file.
     */
    private boolean parseParams(boolean retainData) throws IOException
    {
        // The group name as the key, linking to the C3DParameterGroup object
        IntHashMap param_groups = new IntHashMap();

        // Read all the param blocks into one big array and process from there.
        int num_param_blocks = buffer[2] & 0xFF;

        byte[] tmp = buffer;
        buffer = new byte[512 * num_param_blocks];

        readBlocks(num_param_blocks - 1);

        System.arraycopy(buffer, 0, buffer, 512, 512 * (num_param_blocks - 1));
        System.arraycopy(tmp, 0, buffer, 0, 512);

        reader.setBuffer(buffer);

        // Start reading the param block. First param or group starts as byte 4
        boolean have_params = true;
        int offset = 4;

        while(have_params)
        {
            // if num_name_chars is negative, this indicates a locked parameter
            // record.
            int num_name_chars = buffer[offset];
            boolean locked = false;
            int id = buffer[offset + 1];

            if(num_name_chars < 0)
            {
                locked = true;
                num_name_chars = -num_name_chars;
            }

            String name = reader.readString(offset + 2, num_name_chars);
            name = name.toUpperCase();

            int next_record = reader.readShort(offset + 2 + num_name_chars);
            if(next_record == 0)
                have_params = false;

            // process a group or a parameter
            if(id < 0)
            {
                int desc_size = buffer[offset + 4 + num_name_chars];
                String desc = reader.readString(offset + 5 + num_name_chars,
                                                desc_size);

                // It is possible that one or more params were declared for
                // this group before we got here in the file. If that is the
                // case then we will have a proxy already stored in the map.
                // If there is a proxy, just fill in the extra details.
                C3DParameterGroup grp = (C3DParameterGroup)param_groups.get(id);

                if(grp != null)
                {
                    grp.setName(name);
                    grp.setDescription(desc);
                    grp.setLocked(locked);
                }
                else
                {
                    grp = new C3DParameterGroup(name, locked, id, desc);
                    param_groups.put(id, grp);
                }

                offset += 5 + num_name_chars + desc_size;
            }
            else
            {
                int data_size = buffer[offset + 4 + num_name_chars];
                int num_dimensions = buffer[offset + 5 + num_name_chars];

                int[] dimensions = new int[num_dimensions];

                for(int i = 0; i < num_dimensions; i++)
                    dimensions[i] = buffer[offset + 6 + num_name_chars + i];

                offset += 6 + num_name_chars + num_dimensions;

                // Group ID is negative value of parameter ID
                C3DParameterGroup grp =
                    (C3DParameterGroup)param_groups.get(-id);

                // It is possible that we've run across the group before it was
                // defined in the file. This is allowed by C3D. If this is the
                // case, the grp variable will still be null. So, let's create
                // a proxy group object to hold this and any other values for
                // now.
                if(grp == null)
                {
                    grp = new C3DParameterGroup(null, false, -id, null);
                    param_groups.put(-id, grp);
                }

                switch(data_size)
                {
                    case -1:
                        C3DStringParameter sp =
                            new C3DStringParameter(name, false, id);
                        offset = readStringParams(sp, dimensions, offset);
                        sp.setLocked(locked);
                        grp.addParameterUnlocked(sp);
                        break;

                    case 1:
                        C3DByteParameter bp =
                            new C3DByteParameter(name, false, id);
                        offset = readByteParams(bp, dimensions, offset);
                        bp.setLocked(locked);
                        grp.addParameterUnlocked(bp);
                        break;

                    case 2:
                        C3DIntParameter ip =
                            new C3DIntParameter(name, false, id);
                        offset = readIntParams(ip, dimensions, offset);
                        ip.setLocked(locked);
                        grp.addParameterUnlocked(ip);
                        break;

                    case 4:
                        C3DFloatParameter fp =
                            new C3DFloatParameter(name, false, id);
                        offset = readFloatParams(fp, dimensions, offset);
                        fp.setLocked(locked);
                        grp.addParameterUnlocked(fp);
                        break;
                }


                int desc_size = buffer[offset];
                String desc = reader.readString(offset + 1, desc_size);

                offset += 1 + desc_size;
            }
        }

        boolean ret_val = true;

        if(retainData || (observer != null))
        {
            int[] ids = param_groups.keySet();
            parameters = new C3DParameterGroup[ids.length];

            for(int i = 0; i < ids.length; i++)
                parameters[i] = (C3DParameterGroup)param_groups.get(ids[i]);
        }

        if(observer != null)
        {
            try
            {
                ret_val = observer.parametersComplete(parameters);

            }
            catch(Exception e)
            {
                errorReporter.errorReport(USER_PARAM_ERR, e);
            }

            if(!retainData)
                parameters = null;
        }

        return ret_val;
    }

    /**
     * Parse the trajectories.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @throws IOException some problem reading the basic file.
     */
    private void parseTrajectories(boolean retainData) throws IOException
    {
    }

    /**
     * Fill the internal buffer with data from the input source passed to us
     * last time the class was reset.
     *
     * @param blocks The number of 512 byte blocks to read at once
     */
    private void readBlocks(int blocks) throws IOException
    {
        if(inputStream != null)
        {
            inputStream.read(buffer, 0, 512 * blocks);
            currentBlock += blocks;
        }
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     */
    private int readStringParams(C3DStringParameter param, int[] d, int offset)
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                String sd0 = reader.readString(offset, 1);
                offset++;
                param.setValue(sd0);
                break;

            case 1:
                String sd1 = reader.readString(offset, d[0]);
                offset += d[0];
                param.setValue(sd1);
                break;

            case 2:
                String[] sd2 = new String[d[1]];
                for(int i = 0; i < d[1]; i++)
                    sd2[i] = reader.readString(offset + d[0] * i, d[0]);

                offset += d[0] * d[1];
                param.setValue(sd2);
                break;

            case 3:
                String[][] sd3 = new String[d[2]][d[1]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        sd3[i][j] = reader.readString(offset + d[0] * i * j, d[0]);

                offset += d[0] * d[1] * d[2];
                param.setValue(sd3);
                break;

            case 4:
                String[][][] sd4 = new String[d[3]][d[2]][d[1]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            sd4[i][j][k] = reader.readString(offset + d[0] * i * j * k, d[0]);

                offset += d[0] * d[1] * d[2] * d[3];
                param.setValue(sd4);
                break;

            case 5:
                String[][][][] sd5 = new String[d[4]][d[3]][d[2]][d[1]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                sd5[i][j][k][l] = reader.readString(offset + d[0] * i * j * k * l, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4];
                param.setValue(sd5);
                break;

            case 6:
                String[][][][][] sd6 = new String[d[5]][d[4]][d[3]][d[2]][d[1]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    sd6[i][j][k][l][m] = reader.readString(offset + d[0] * i * j * k * l * m, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5];
                param.setValue(sd6);
                break;

            case 7:
                String[][][][][][] sd7 = new String[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        sd7[i][j][k][l][m][n] = reader.readString(offset + d[0] * i * j * k * l * m * n, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6];
                param.setValue(sd7);
                break;
        }

        return offset;
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     */
    private int readByteParams(C3DByteParameter param, int[] d, int offset)
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                byte sd0 = buffer[offset];
                offset++;
                param.setValue(sd0);
                break;

            case 1:
                byte[] sd1 = new byte[d[0]];
                System.arraycopy(buffer, offset, sd1, 0, d[0]);
                offset += d[0];
                param.setValue(sd1);
                break;

            case 2:
                byte[][] sd2 = new byte[d[1]][d[0]];
                for(int i = 0; i < d[1]; i++)
                    System.arraycopy(buffer, offset + d[0] * i, sd2[i], 0, d[0]);

                offset += d[0] * d[1];
                param.setValue(sd2);
                break;

            case 3:
                byte[][][] sd3 = new byte[d[2]][d[1]][d[0]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        System.arraycopy(buffer, offset + d[0] * i * j, sd3[i][j], 0, d[0]);

                offset += d[0] * d[1] * d[2];
                param.setValue(sd3);
                break;

            case 4:
                byte[][][][] sd4 = new byte[d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            System.arraycopy(buffer, offset + d[0] * i * j * k, sd4[i][j][k], 0, d[0]);

                offset += d[0] * d[1] * d[2] * d[3];
                param.setValue(sd4);
                break;

            case 5:
                byte[][][][][] sd5 = new byte[d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                System.arraycopy(buffer, offset + d[0] * i * j * k * l, sd5[i][j][k][l], 0, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4];
                param.setValue(sd5);
                break;

            case 6:
                byte[][][][][][] sd6 = new byte[d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    System.arraycopy(buffer, offset + d[0] * i * j * k * l * m, sd6[i][j][k][l][m], 0, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5];
                param.setValue(sd6);
                break;

            case 7:
                byte[][][][][][][] sd7 = new byte[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        System.arraycopy(buffer, offset + d[0] * i * j * k * l * m * n, sd7[i][j][k][l][m][n], 0, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6];
                param.setValue(sd7);
                break;
        }

        return offset;
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     */
    private int readIntParams(C3DIntParameter param, int[] d, int offset)
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                int sd0 = reader.readShort(offset);
                offset += 2;
                param.setValue(sd0);
                break;

            case 1:
                int[] sd1 = new int[d[0]];
                for(int i = 0; i < d[0]; i++)
                    sd1[i] = reader.readShort(offset + 2 * i);

                offset += d[0] * 2;
                param.setValue(sd1);
                break;

            case 2:
                int[][] sd2 = new int[d[1]][d[0]];
                for(int i = 0; i < d[1]; i++)
                    for(int j = 0; j < d[0]; j++)
                        sd2[i][j] = reader.readShort(offset + 2 * i * j);

                offset += d[0] * d[1] * 2;
                param.setValue(sd2);
                break;

            case 3:
                int[][][] sd3 = new int[d[2]][d[1]][d[0]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        for(int k = 0; k < d[0]; k++)
                            sd3[i][j][k] = reader.readShort(offset + 2 * i * j * k);

                offset += d[0] * d[1] * d[2] * 2;
                param.setValue(sd3);
                break;

            case 4:
                int[][][][] sd4 = new int[d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            for(int l = 0; l < d[0]; l++)
                                sd4[i][j][k][l] = reader.readShort(offset + 2 * i * j * k * l);

                offset += d[0] * d[1] * d[2] * d[3] * 2;
                param.setValue(sd4);
                break;

            case 5:
                int[][][][][] sd5 = new int[d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                for(int m = 0; m < d[0]; m++)
                                    sd5[i][j][k][l][m] = reader.readShort(offset + 2 * i * j * k * l * m);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * 2;
                param.setValue(sd5);
                break;

            case 6:
                int[][][][][][] sd6 = new int[d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    for(int n = 0; n < d[0]; n++)
                                        sd6[i][j][k][l][m][n] = reader.readShort(offset + 2 * i * j * k * l * m * n);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * 2;
                param.setValue(sd6);
                break;

            case 7:
                int[][][][][][][] sd7 = new int[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        for(int p = 0; p < d[0]; p++)
                                            sd7[i][j][k][l][m][n][p] = reader.readShort(offset + 2 * i * j * k * l * m * n * p);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6] * 2;
                param.setValue(sd7);
                break;
        }

        return offset;
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     */
    private int readFloatParams(C3DFloatParameter param, int[] d, int offset)
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                float sd0 = reader.readFloat(offset);
                offset += 4;
                param.setValue(sd0);
                break;

            case 1:
                float[] sd1 = new float[d[0]];
                for(int i = 0; i < d[0]; i++)
                    sd1[i] = reader.readFloat(offset + 4 * i);

                offset += d[0] * 4;
                param.setValue(sd1);
                break;

            case 2:
                float[][] sd2 = new float[d[1]][d[0]];
                for(int i = 0; i < d[1]; i++)
                    for(int j = 0; j < d[0]; j++)
                        sd2[i][j] = reader.readFloat(offset + 4 * i * j);

                offset += d[0] * d[1] * 4;
                param.setValue(sd2);
                break;

            case 3:
                float[][][] sd3 = new float[d[2]][d[1]][d[0]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        for(int k = 0; k < d[0]; k++)
                            sd3[i][j][k] = reader.readFloat(offset + 4 * i * j * k);

                offset += d[0] * d[1] * d[2] * 4;
                param.setValue(sd3);
                break;

            case 4:
                float[][][][] sd4 = new float[d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            for(int l = 0; l < d[0]; l++)
                                sd4[i][j][k][l] = reader.readFloat(offset + 4 * i * j * k * l);

                offset += d[0] * d[1] * d[2] * d[3] * 4;
                param.setValue(sd4);
                break;

            case 5:
                float[][][][][] sd5 = new float[d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                for(int m = 0; m < d[0]; m++)
                                    sd5[i][j][k][l][m] = reader.readFloat(offset + 4 * i * j * k * l * m);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * 4;
                param.setValue(sd5);
                break;

            case 6:
                float[][][][][][] sd6 = new float[d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    for(int n = 0; n < d[0]; n++)
                                        sd6[i][j][k][l][m][n] = reader.readFloat(offset + 4 * i * j * k * l * m * n);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * 4;
                param.setValue(sd6);
                break;

            case 7:
                float[][][][][][][] sd7 = new float[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        for(int p = 0; p < d[0]; p++)
                                            sd7[i][j][k][l][m][n][p] = reader.readFloat(offset + 4 * i * j * k * l * m * n * p);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6] * 4;
                param.setValue(sd7);
                break;
        }

        return offset;
    }
}
