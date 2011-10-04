/*****************************************************************************
 *                            (c) j3d.org 2002-2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
import java.io.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

// Local parser
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.ParsingErrorException;
import org.j3d.loaders.UnsupportedFormatException;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

/**
 * A low-level parser for the LDraw file format.
 * <p>
 *
 * The output of this parser is the records as per the file and the option
 * of a raw height array.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article218.html">
 *  http://www.ldraw.org/Article218.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawParser
{
    /** Set of the old-style meta commands */
    private static final Set<String> OLD_META_COMMANDS;

    /** The step meta command string */
    private static final String STEP_META = "STEP";

    /** The step meta command string */
    private static final String WRITE_META = "WRITE";

    /** The step meta command string */
    private static final String PRINT_META = "PRINT";

    /** The step meta command string */
    private static final String CLEAR_META = "CLEAR";

    /** The step meta command string */
    private static final String PAUSE_META = "PAUSE";

    /** The step meta command string */
    private static final String SAVE_META = "SAVE";

    /**
     * The reader used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private BufferedReader inputReader;

    /** Flag to say we've already read the stream */
    private boolean dataReady;

    /** Handler for reading the contents of the stream */
    private StreamTokenizer strtok;

    /** Observer, if set, for the stream of info coming from the file reading */
    private LDrawParseObserver observer;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /**
     * Global static init for setting up the meta commands.
     */
    static
    {
        OLD_META_COMMANDS = new HashSet<String>();

        OLD_META_COMMANDS.add(STEP_META);
        OLD_META_COMMANDS.add(WRITE_META);
        OLD_META_COMMANDS.add(PRINT_META);
        OLD_META_COMMANDS.add(CLEAR_META);
        OLD_META_COMMANDS.add(PAUSE_META);
        OLD_META_COMMANDS.add(SAVE_META);
    }

    /**
     * Construct a new parser with no stream set.
     */
    public LDrawParser()
    {
        dataReady = false;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Construct a new parser using the given stream to source the data from.
     *
     * @param is The stream to read data from
     */
    public LDrawParser(InputStream is)
    {
        this();

        assert is != null : "Null input stream provided";

        inputReader = new BufferedReader(new InputStreamReader(is));
        strtok = new StreamTokenizer(inputReader);
    }

    /**
     * Construct a new parser using the given reader to source the data from.
     *
     * @param rdr The stream to read data from
     */
    public LDrawParser(Reader rdr)
    {
        this();

        assert rdr != null : "Null reader provided";

        if(rdr instanceof BufferedReader)
            inputReader = (BufferedReader)rdr;
        else
            inputReader = new BufferedReader(rdr);

        strtok = new StreamTokenizer(inputReader);
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
     * Force a clear of the data that has been previous read by this parser.
     */
    public void clear()
    {
        dataReady = false;
        inputReader = null;
        strtok = null;
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param is The new stream to use
     */
    public void reset(InputStream is)
    {
        assert is != null : "Null input stream provided";

        inputReader = new BufferedReader(new InputStreamReader(is));
        strtok = new StreamTokenizer(inputReader);

        dataReady = false;
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param rdr The stream to read data from
     */
    public void reset(Reader rdr)
    {
        assert rdr != null : "Null reader provided";

        if(rdr instanceof BufferedReader)
            inputReader = (BufferedReader)rdr;
        else
            inputReader = new BufferedReader(rdr);

        strtok = new StreamTokenizer(inputReader);
        dataReady = false;
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @throws IncorrectFormatException The file is not one our parser
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public void parse()
        throws IOException
    {
        if(dataReady)
            throw new IOException("Data has already been read from this stream");


        while(strtok.nextToken() != StreamTokenizer.TT_EOF)
        {
            // Start of each line is an integer number, so force that here.
            switch((int)strtok.nval)
            {
                case 0:
                    parseComment();
                    break;

                case 1:
                    parseReference();
                    break;

                case 2:
                    parseLine();
                    break;

                case 3:
                    parseTriangle();
                    break;

                case 4:
                    parseQuad();
                    break;

                case 5:
                    parseOptionalLine();
                    break;

            }
        }

        dataReady = true;
    }

    /**
     * Process a line that starts with '0', which is treated as a comment or
     * metadata command. If it turns out to be a metadata command, then return
     * true so that the following line parsing can be properly handled.
     *
     * @return true if the comment is a metadata command
     */
    private boolean parseComment()
        throws IOException
    {
        int type = strtok.nextToken();
        boolean has_meta = false;

        // Check to see if we have any form of meta command. Otherwise, treat
        // it all as a comment.
        if(type == StreamTokenizer.TT_WORD)
        {
            String first_comment = strtok.sval;

            if(first_comment.startsWith("!"))
            {
                has_meta = true;
            }
            else if(OLD_META_COMMANDS.contains(first_comment))
            {
                has_meta = true;
            }
        }


        clearToEOL();

        return false;
    }

    /**
     * Parse the line that starts with '1', which is treated as a sub-file reference.
     * The line is defined as:
     *
     * 1 <colour> x y z a b c d e f g h i <file>
     */
    private void parseReference()
        throws IOException
    {
        int color_id = -1;
        double[] matrix = new double[16];
        String file_ref = null;

        checkNextToken(true);

        color_id = (int)strtok.nval;

        LDrawColor colour = LDrawColor.getColorForIndex(color_id);

        // x, y, z
        checkNextToken(true);
        matrix[3] = strtok.nval;

        checkNextToken(true);
        matrix[7] = strtok.nval;

        checkNextToken(true);
        matrix[11] = strtok.nval;

        // a, b, c
        checkNextToken(true);
        matrix[0] = strtok.nval;

        checkNextToken(true);
        matrix[1] = strtok.nval;

        checkNextToken(true);
        matrix[2] = strtok.nval;

        // d, e, f
        checkNextToken(true);
        matrix[4] = strtok.nval;

        checkNextToken(true);
        matrix[5] = strtok.nval;

        checkNextToken(true);
        matrix[6] = strtok.nval;

        // g, h, i
        checkNextToken(true);
        matrix[8] = strtok.nval;

        checkNextToken(true);
        matrix[9] = strtok.nval;

        checkNextToken(true);
        matrix[10] = strtok.nval;

        // Last row is 0, 0, 0, 1
        matrix[15] = 1.0;

        checkNextToken(false);

        file_ref = strtok.sval;

        clearToEOL();
    }

    /**
     * Parse the line that starts with '2', which is treated as a line.
     * The format of the line is:
     *
     * 2 <colour> x1 y1 z1 x2 y2 z2
     *
     */
    private void parseLine()
        throws IOException
    {
        double[] p1 = new double[3];
        double[] p2 = new double[3];

        int color_id = -1;

        checkNextToken(true);

        color_id = (int)strtok.nval;

        LDrawColor colour = LDrawColor.getColorForIndex(color_id);

        // p1
        checkNextToken(true);
        p1[0] = strtok.nval;

        checkNextToken(true);
        p1[1] = strtok.nval;

        checkNextToken(true);
        p1[2] = strtok.nval;

        // p2
        checkNextToken(true);
        p2[0] = strtok.nval;

        checkNextToken(true);
        p2[1] = strtok.nval;

        checkNextToken(true);
        p2[2] = strtok.nval;

        clearToEOL();
    }

    /**
     * Parse the line that starts with '3', which is treated as a triangle.
     * The format of the line is:
     *
     * 3 <colour> x1 y1 z1 x2 y2 z2 x3 y3 z3
     */
    private void parseTriangle()
        throws IOException
    {
        double[] p1 = new double[3];
        double[] p2 = new double[3];
        double[] p3 = new double[3];

        int color_id = -1;

        checkNextToken(true);

        color_id = (int)strtok.nval;

        LDrawColor colour = LDrawColor.getColorForIndex(color_id);

        // p1
        checkNextToken(true);
        p1[0] = strtok.nval;

        checkNextToken(true);
        p1[1] = strtok.nval;

        checkNextToken(true);
        p1[2] = strtok.nval;

        // p2
        checkNextToken(true);
        p2[0] = strtok.nval;

        checkNextToken(true);
        p2[1] = strtok.nval;

        checkNextToken(true);
        p2[2] = strtok.nval;

        // p3
        checkNextToken(true);
        p3[0] = strtok.nval;

        checkNextToken(true);
        p3[1] = strtok.nval;

        checkNextToken(true);
        p3[2] = strtok.nval;

        clearToEOL();
    }

    /**
     * Parse the line that starts with '4', which is treated as a Quad.
     * The format of the line is:
     *
     * 4 <colour> x1 y1 z1 x2 y2 z2 x3 y3 z3 x4 y4 z4
     */
    private void parseQuad()
        throws IOException
    {
        double[] p1 = new double[3];
        double[] p2 = new double[3];
        double[] p3 = new double[3];
        double[] p4 = new double[3];

        int color_id = -1;

        checkNextToken(true);

        color_id = (int)strtok.nval;

        LDrawColor colour = LDrawColor.getColorForIndex(color_id);

        // p1
        checkNextToken(true);
        p1[0] = strtok.nval;

        checkNextToken(true);
        p1[1] = strtok.nval;

        checkNextToken(true);
        p1[2] = strtok.nval;

        // p2
        checkNextToken(true);
        p2[0] = strtok.nval;

        checkNextToken(true);
        p2[1] = strtok.nval;

        checkNextToken(true);
        p2[2] = strtok.nval;

        // p3
        checkNextToken(true);
        p3[0] = strtok.nval;

        checkNextToken(true);
        p3[1] = strtok.nval;

        checkNextToken(true);
        p3[2] = strtok.nval;

        // p4
        checkNextToken(true);
        p4[0] = strtok.nval;

        checkNextToken(true);
        p4[1] = strtok.nval;

        checkNextToken(true);
        p4[2] = strtok.nval;

        clearToEOL();
    }

    /**
     * Parse the line that starts with '5', which is treated as an optional
     * line for the line below.
     * The format of the line is:
     *
     * 5 <colour> x1 y1 z1 x2 y2 z2 x3 y3 z3 x4 y4 z4
     */
    private void parseOptionalLine()
        throws IOException
    {
        double[] p1 = new double[3];
        double[] p2 = new double[3];
        double[] c1 = new double[3];
        double[] c2 = new double[3];

        int color_id = -1;

        checkNextToken(true);

        color_id = (int)strtok.nval;

        LDrawColor colour = LDrawColor.getColorForIndex(color_id);

        // p1
        checkNextToken(true);
        p1[0] = strtok.nval;

        checkNextToken(true);
        p1[1] = strtok.nval;

        checkNextToken(true);
        p1[2] = strtok.nval;

        // p2
        checkNextToken(true);
        p2[0] = strtok.nval;

        checkNextToken(true);
        p2[1] = strtok.nval;

        checkNextToken(true);
        p2[2] = strtok.nval;

        // c1
        checkNextToken(true);
        c1[0] = strtok.nval;

        checkNextToken(true);
        c1[1] = strtok.nval;

        checkNextToken(true);
        c1[2] = strtok.nval;

        // c2
        checkNextToken(true);
        c2[0] = strtok.nval;

        checkNextToken(true);
        c2[1] = strtok.nval;

        checkNextToken(true);
        c2[2] = strtok.nval;

        clearToEOL();
    }

    /**
     * Read the next token and confirm that it is of the requested type. When
     * true, you want a number, otherwise it will be a string
     *
     * @param needNumber true if you want the next type to be a number
     * @throws IOException There was some other form of I/O error when reading
     * @throws InvalidFormatException The read data type is not the required
     *    number or string
     */
    private void checkNextToken(boolean needNumber)
        throws IOException, InvalidFormatException
    {
        int type = strtok.nextToken();

        if(needNumber)
        {
            if(type != StreamTokenizer.TT_NUMBER)
            {
                throw new InvalidFormatException();
            }
       }
       else
       {
            if(type != StreamTokenizer.TT_WORD)
            {
                throw new InvalidFormatException();
            }
       }
    }

    /**
     * Clear the stream until the end of the line.
     */
    private void clearToEOL()
        throws IOException
    {
        int type;

        do
        {
            type = strtok.nextToken();
        }
        while(type != StreamTokenizer.TT_EOL || type != StreamTokenizer.TT_EOF);
    }
}
