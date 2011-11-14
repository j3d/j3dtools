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
import java.util.*;

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
 * <b>Header Processing</b>
 * </p>
 *
 * All headers are processed, except the following list:
 * <ul>
 * <li>CMDLINE</li>
 * <li>COLOUR</li>
 * <li>HELP</li>
 * </ul>
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
    /** Property holding the error message when the header listener fails */
    private static final String HEADER_LISTENER_PROP =
        "org.j3d.loaders.ldraw.LDrawParser.observerHeaderExceptionMsg";

    /** Property holding the error message when the header listener fails */
    private static final String BFC_LISTENER_PROP =
        "org.j3d.loaders.ldraw.LDrawParser.observerBFCExceptionMsg";

    /** Property holding the error message when the listener for file references fails */
    private static final String FILEREF_LISTENER_PROP =
        "org.j3d.loaders.ldraw.LDrawParser.observerFileRefExceptionMsg";

    /** Property holding the error message when the listener for renderable updates fails */
    private static final String PRIMITIVE_LISTENER_PROP =
        "org.j3d.loaders.ldraw.LDrawParser.observerPrimitiveExceptionMsg";

    /** The step meta command string */
    private static final String STEP_META = "STEP";

    /** The write meta command string */
    private static final String WRITE_META = "WRITE";

    /** The print meta command string */
    private static final String PRINT_META = "PRINT";

    /** The clear drawing meta command string */
    private static final String CLEAR_META = "CLEAR";

    /** The pause (wait for keyboard input) meta command string */
    private static final String PAUSE_META = "PAUSE";

    /** The save meta command string */
    private static final String SAVE_META = "SAVE";

    /** Official header command for author name */
    private static final String AUTHOR_HEADER = "Author:";

    /** Official header command for file name */
    private static final String NAME_HEADER = "Name:";

    /** Official header command for indicating this is an official LDRAW file */
    private static final String LDRAW_HEADER = "LDRAW_ORG";

    /** Official header command for the back face culling extension */
    private static final String BFC_HEADER = "BFC";

    /** Official header command for license details */
    private static final String LICENSE_HEADER = "LICENSE";

    /** Official header command for the category and keywords language extension */
    private static final String CATEGORY_HEADER = "CATEGORY";

    /** Official header command for the category and keywords language extension */
    private static final String KEYWORD_HEADER = "KEYWORDS";

    /** Official header command for history details */
    private static final String HISTORY_HEADER = "HISTORY";

    // BFC extension keywords

    /** BFC keyword indicating compliance with the spec */
    private static final String BFC_CERTIFY = "CERTIFY";

    /** BFC keyword indicating non-compliance with the spec */
    private static final String BFC_NO_CERTIFY = "NOCERTIFY";

    /** BFC keyword indicating clockwise ordering of following polygons */
    private static final String BFC_CW = "CW";

    /** BFC keyword indicating clockwise ordering of following polygons */
    private static final String BFC_INVERT = "INVERTNEXT";


    /** BFC keyword indicating counter clockwise ordering of following polygons */
    private static final String BFC_CCW = "CCW";

    /** BFC keyword indicating back face culling is on */
    private static final String BFC_CLIP = "CLIP";

    /** BFC keyword indicating back face culling is off */
    private static final String BFC_NOCLIP = "NOCLIP";

    /** Set of the old-style meta commands */
    private static final Set<String> OLD_META_COMMANDS;

    /** Set of ! Escaped oficial header keywords */
    private static final Set<String> ESCAPED_OFFICIAL_HEADERS;

    /** Set of the non-escaped oficial header keywords */
    private static final Set<String> STD_OFFICIAL_HEADERS;

    /**
     * The reader used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private BufferedReader inputReader;

    /**
     * Flag indicating that the header has finished reading, so ignore any
     * header meta commands that are only to be processed in the header
     */
    private boolean headerComplete;

    /** Flag to say we've already read the stream */
    private boolean dataReady;

    /**
     * Flag indicating if BFC processing during a file is permitted. According
     * to the spec:
     * "In order for a file to be processed with back face culling, there must
     * be at least one 0 BFC meta-statement before the first operational
     * command-line. If there is no such 0 BFC meta-statement in the file, BFC
     * processing will be disabled for that file."
     */
    private boolean bfcEnabled;

    /**
     * Current BFC winding statement. Kept between lines as sometimes the parsing
     * hits a cull statement only and no change of winding, but the listener
     * needs to report winding anyway
     */
    private boolean bfcCcw;

    /**
     * Current BFC clip statement. Kept between lines as sometimes the parsing
     * hits a ccw statement only and no change of winding, but the listener
     * needs to report winding anyway
     */
    private boolean bfcCull;

    /** Flag indicating next renderable or file will have the coords inverted */
    private boolean bfcInvertNext;

    /** Handler for reading the contents of the stream */
    private StreamTokenizer strtok;

    /** Observer, if set, for the stream of info coming from the file reading */
    private LDrawParseObserver observer;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /** Header that has been read for this file */
    private LDrawHeader header;

    /** The parsed contents of the file, in order read. Does not include the header */
    private List<LDrawFilePart> contents;


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

        ESCAPED_OFFICIAL_HEADERS = new HashSet<String>();
        ESCAPED_OFFICIAL_HEADERS.add(CATEGORY_HEADER);
        ESCAPED_OFFICIAL_HEADERS.add(KEYWORD_HEADER);
        ESCAPED_OFFICIAL_HEADERS.add(HISTORY_HEADER);
        ESCAPED_OFFICIAL_HEADERS.add(LDRAW_HEADER);
        ESCAPED_OFFICIAL_HEADERS.add(LICENSE_HEADER);

        STD_OFFICIAL_HEADERS = new HashSet<String>();
        STD_OFFICIAL_HEADERS.add(AUTHOR_HEADER);
        STD_OFFICIAL_HEADERS.add(NAME_HEADER);
        STD_OFFICIAL_HEADERS.add(BFC_HEADER);
        STD_OFFICIAL_HEADERS.add(LDRAW_HEADER);
    }

    /**
     * Construct a new parser with no stream set.
     */
    public LDrawParser()
    {
        dataReady = false;
        headerComplete = false;
        bfcEnabled = false;
        bfcCcw = true;
        bfcCull = false;
        bfcInvertNext = false;
        contents = new ArrayList<LDrawFilePart>();
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
    public void setParseObserver(LDrawParseObserver obs)
    {
        observer = obs;
    }

    /**
     * Force a clear of the data that has been previous read by this parser.
     */
    public void clear()
    {
        headerComplete = false;
        dataReady = false;
        bfcEnabled = false;
        bfcCcw = true;
        bfcCull = false;
        bfcInvertNext = false;
        inputReader = null;
        contents = new ArrayList<LDrawFilePart>();
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

        clear();

        inputReader = new BufferedReader(new InputStreamReader(is));
        strtok = new StreamTokenizer(inputReader);
        resetSyntax();
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

        clear();

        if(rdr instanceof BufferedReader)
            inputReader = (BufferedReader)rdr;
        else
            inputReader = new BufferedReader(rdr);

        strtok = new StreamTokenizer(inputReader);
        resetSyntax();
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     * @throws IncorrectFormatException The file is not one our parser
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public void parse(boolean retainData)
        throws IOException
    {
        if(dataReady)
            throw new IOException("Data has already been read from this stream");

        resetSyntax();
        header = new LDrawHeader();
        boolean eof = false;

        while(!eof && strtok.nextToken() != StreamTokenizer.TT_EOF)
        {
            // Start of each line is an integer number, so force that here.
            // If it isn't (eg blank line), go to the next line
            if(strtok.ttype != StreamTokenizer.TT_NUMBER)
                continue;

            switch((int)strtok.nval)
            {
                case 0:
                    eof = parseComment(retainData);
                    break;

                case 1:
                    eof = parseReference(retainData);
                    break;

                case 2:
                    eof = parseLine(retainData);
                    break;

                case 3:
                    eof = parseTriangle(retainData);
                    break;

                case 4:
                    eof = parseQuad(retainData);
                    break;

                case 5:
                    eof = parseOptionalLine(retainData);
                    break;

            }
        }

        dataReady = true;
    }

    /**
     * Get the contents of the file, as read. If the parser has been reset,
     * returns an empty list. The list cannot be modified. It does not contain
     * the header. That is fetched separately
     *
     * @return A list containing the file contents in the order read
     */
    public List<LDrawFilePart> getFileContents()
    {
        return Collections.unmodifiableList(contents);
    }

    /**
     * Process a line that starts with '0', which is treated as a comment or
     * metadata command. If it turns out to be a metadata command, then return
     * true so that the following line parsing can be properly handled.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     * @return true if the comment is a metadata command
     */
    private boolean parseComment(boolean retainData)
        throws IOException
    {
        int type = strtok.nextToken();
        boolean has_meta = false;
        boolean needs_clear = true;

        // Check to see if we have any form of meta command. Otherwise, treat
        // it all as a comment.
        if(type == StreamTokenizer.TT_WORD)
        {
            String first_comment = strtok.sval;

            if(first_comment.startsWith("!"))
            {
                first_comment = first_comment.substring(1);

                // Look for the official headers, but if we see them after the
                // header has been completed, then ignore them.
                if(ESCAPED_OFFICIAL_HEADERS.contains(first_comment))
                {
                    if(!headerComplete)
                    {
                        if(CATEGORY_HEADER.equals(first_comment))
                        {
                            StringBuilder bldr = new StringBuilder();

                            while(type != StreamTokenizer.TT_EOL && type != StreamTokenizer.TT_EOF)
                            {
                                if(type == StreamTokenizer.TT_WORD)
                                    bldr.append(strtok.sval);
                                else
                                    bldr.append(strtok.nval);

                                bldr.append(' ');
                            }

                            header.setCategory(bldr.toString());
                            needs_clear = false;
                        }
                        else if(KEYWORD_HEADER.equals(first_comment))
                        {
                            StringTokenizer tok = new StringTokenizer(readToEOL(), ",");

                            while(tok.hasMoreTokens())
                                header.addKeyword(tok.nextToken());

                            needs_clear = false;
                        }
                        else if(LDRAW_HEADER.equals(first_comment))
                        {
                            header.setOfficial(true);
                            type = strtok.nextToken();
                            if(type == StreamTokenizer.TT_WORD)
                                header.setFileType(LDrawFileType.valueOf(strtok.sval.toUpperCase()));
                            else if(type == StreamTokenizer.TT_EOL)
                                needs_clear = false;
                        }
                        else if(LICENSE_HEADER.equals(first_comment))
                        {
                            header.setLicense(readToEOL());
                            needs_clear = false;
                        }
                        else if(HISTORY_HEADER.equals(first_comment))
                        {
                            header.addHistory(readToEOL());
                            needs_clear = false;
                        }
                    }
                }
                else
                {
                    has_meta = true;
                }
            }
            else if(OLD_META_COMMANDS.contains(first_comment))
            {
                has_meta = true;
            }
            else if(STD_OFFICIAL_HEADERS.contains(first_comment))
            {
                if(AUTHOR_HEADER.equals(first_comment))
                {
                    header.setAuthor(readToEOL());
                }
                else if(NAME_HEADER.equals(first_comment))
                {
                    header.setName(readToEOL());
                }
                else if(BFC_HEADER.equals(first_comment))
                {
                    // If we haven't finished the header processing, then this
                    // looks at whether to certify the file for it or not,
                    // otherwise, only process the command if it was previously
                    // enabled in the header
                    if(!headerComplete)
                    {
                        checkNextToken(false);
                        String t2 = strtok.sval;

                        if(BFC_CERTIFY.equals(t2))
                        {
                            header.setBFCCompliant(true);
                            if(hasNextToken())
                            {
                                t2 = strtok.sval;
                                bfcCcw = !BFC_CW.equals(t2);
                                header.setCCW(bfcCcw);
                            }
                            else
                            {
                                bfcCcw = true;
                                header.setCCW(true);
                            }

                            bfcEnabled = true;
                        }
                        else if(BFC_NO_CERTIFY.equals(t2))
                        {
                            header.setBFCCompliant(false);
                            bfcEnabled = false;
                            bfcCcw = true;
                            bfcCull = false;
                        }
                        else if(bfcEnabled)
                        {
                            if(BFC_CCW.equals(t2))
                            {
                                header.setCCW(true);
                            }
                            else if(BFC_CW.equals(t2))
                            {
                                header.setCCW(false);
                            }
                            else if(BFC_CLIP.equals(t2))
                            {
                                if(hasNextToken())
                                {
                                    t2 = strtok.sval;
                                    if(BFC_CCW.equals(t2))
                                    {
                                        header.setCCW(true);
                                    }
                                    else if(BFC_CW.equals(t2))
                                    {
                                        header.setCCW(false);
                                    }
                                }
                                else
                                {
                                    header.setCCW(true);
                                }
                            }
                        }
                    }
                    else if(bfcEnabled)
                    {
                        // Only parse the rest of the line if BFC was enabled
                        // earlier in the header. Otherwise, ignore.
                        checkNextToken(false);
                        String t2 = strtok.sval;

                        needs_clear = true;
                        if(BFC_CCW.equals(t2))
                        {
                            bfcCcw = true;
                            fireBFCState();
                        }
                        else if(BFC_CW.equals(t2))
                        {
                            bfcCcw = false;
                            fireBFCState();
                        }
                        else if(BFC_CLIP.equals(t2))
                        {
                            bfcCull = true;

                            if(hasNextToken())
                            {
                                t2 = strtok.sval;
                                if(BFC_CCW.equals(t2))
                                {
                                    bfcCcw = true;
                                }
                                else if(BFC_CW.equals(t2))
                                {
                                    bfcCcw = false;
                                }
                            }
                            else
                            {
                                bfcCcw = true;
                            }

                            fireBFCState();
                        }
                        else if(BFC_NOCLIP.equals(t2))
                        {
                            bfcCull = false;
                            fireBFCState();
                        }
                        else if(BFC_INVERT.equals(t2))
                        {
                            bfcInvertNext = true;
                        }
                    }
                }
                else if(LDRAW_HEADER.equals(first_comment))
                {
                    header.setOfficial(true);
                    type = strtok.nextToken();
                    if(type == StreamTokenizer.TT_WORD)
                        header.setFileType(LDrawFileType.valueOf(strtok.sval));
                    else if(type == StreamTokenizer.TT_EOL)
                        needs_clear = false;
                }
            }
            else
            {
                String other = readToEOL();
                needs_clear = false;

                if(strtok.lineno() == 2)
                    header.setPreamble(first_comment + " " + other);
            }
        }

        boolean eof = strtok.ttype == StreamTokenizer.TT_EOF;

        if(!eof && needs_clear && strtok.ttype != StreamTokenizer.TT_EOL)
            eof = clearToEOL();

        return eof;
    }

    /**
     * Parse the line that starts with '1', which is treated as a sub-file reference.
     * The line is defined as:
     * <pre>
     * 1 <colour> x y z a b c d e f g h i <file>
     * </pre>
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private boolean parseReference(boolean retainData)
        throws IOException
    {
        finishHeader(retainData);

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

        file_ref = readToEOL();

        LDrawFileReference ref = new LDrawFileReference(colour, file_ref, matrix);

        fireReferenceEvent(ref, retainData);

        return clearToEOL();
    }

    /**
     * Parse the line that starts with '2', which is treated as a line.
     * The format of the line is:
     *
     * 2 <colour> x1 y1 z1 x2 y2 z2
     * </pre>
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private boolean parseLine(boolean retainData)
        throws IOException
    {
        finishHeader(retainData);

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

        boolean eof = clearToEOL();

        LDrawLine line = new LDrawLine(colour, p1, p2);
        firePrimitiveEvent(line, retainData);

        return eof;
    }

    /**
     * Parse the line that starts with '3', which is treated as a triangle.
     * The format of the line is:
     *
     * 3 <colour> x1 y1 z1 x2 y2 z2 x3 y3 z3
     * </pre>
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private boolean parseTriangle(boolean retainData)
        throws IOException
    {
        finishHeader(retainData);

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

        boolean eof = clearToEOL();

        LDrawTriangle tri = new LDrawTriangle(colour, p1, p2, p3);
        firePrimitiveEvent(tri, retainData);

        return eof;
    }

    /**
     * Parse the line that starts with '4', which is treated as a Quad.
     * The format of the line is:
     *
     * 4 <colour> x1 y1 z1 x2 y2 z2 x3 y3 z3 x4 y4 z4
     * </pre>
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private boolean parseQuad(boolean retainData)
        throws IOException
    {
        finishHeader(retainData);

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

        boolean eof = clearToEOL();

        LDrawQuad quad = new LDrawQuad(colour, p1, p2, p3, p4);
        firePrimitiveEvent(quad, retainData);

        return eof;
    }

    /**
     * Parse the line that starts with '5', which is treated as an optional
     * line for the line below.
     * The format of the line is:
     *
     * 5 <colour> x1 y1 z1 x2 y2 z2 x3 y3 z3 x4 y4 z4
     * </pre>
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private boolean parseOptionalLine(boolean retainData)
        throws IOException
    {
        finishHeader(retainData);

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

        boolean eof = clearToEOL();

//        LDrawQuad quad = new LDrawQuad(colour, p1, p2, p3, p4);
//        firePrimitiveEvent(quad, retainData);

        return eof;
    }

    /**
     * See if there is another token of the requested type on the current line.
     * Return value indicates presence of the required type. Unlike
     * checkNextToken() this does not generate an exception and can be used when
     * looking for optional additional keywords.
     *
     * @return true if there is another token available
     * @throws IOException There was some other form of I/O error when reading
     */
    private boolean hasNextToken()
        throws IOException
    {
        int type = strtok.nextToken();

        return type != StreamTokenizer.TT_EOL || type != StreamTokenizer.TT_EOF;
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
                throw new InvalidFormatException("Type is "  + strtok.toString());
            }
       }
    }

    /**
     * Check to see if the header has just completed, and if so, then send
     * out the notification to the observer.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private void finishHeader(boolean retainData)
    {
        if(retainData && !headerComplete && observer != null)
        {
            try
            {
                observer.header(header);
            }
            catch(Exception e)
            {
                if(errorReporter != null)
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(HEADER_LISTENER_PROP);
                    errorReporter.errorReport(msg, e);
                }
            }
        }

        headerComplete = true;
    }

    /**
     * Send to the observer the current primitive that has just bean read
     *
     * @param poly The polygon/line definition that is to be sent
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private void firePrimitiveEvent(LDrawRenderable poly, boolean retainData)
    {
        try
        {
            if(retainData)
                contents.add(poly);

            poly.setInvertedWinding(bfcInvertNext);

            if(observer != null)
                observer.renderable(poly);

            bfcInvertNext = false;
        }
        catch(Exception e)
        {
            if(errorReporter != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();

                String msg = intl_mgr.getString(PRIMITIVE_LISTENER_PROP);
                errorReporter.errorReport(msg, e);
            }
        }
    }

    /**
     * Send to the observer the details of an external file reference.
     *
     * @param ref The details of the file reference read
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally after completing parsing
     */
    private void fireReferenceEvent(LDrawFileReference ref, boolean retainData)
    {
        try
        {
            if(retainData)
                contents.add(ref);

            ref.setInvertedWinding(bfcInvertNext);

            if(observer != null)
                observer.fileReference(ref);

            bfcInvertNext = false;
        }
        catch(Exception e)
        {
            if(errorReporter != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();

                String msg = intl_mgr.getString(FILEREF_LISTENER_PROP);
                errorReporter.errorReport(msg, e);
            }
        }
    }

    /**
     * Resend the current BFC state to the observer, if registered.
     */
    private void fireBFCState()
    {
        try
        {
            if(observer != null)
                observer.bfcStatement(bfcCcw, bfcCull);
        }
        catch(Exception e)
        {
            if(errorReporter != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();

                String msg = intl_mgr.getString(BFC_LISTENER_PROP);
                errorReporter.errorReport(msg, e);
            }
        }
    }

    /**
     * Reset the tokenizer to the default setting after having something else
     * tweaked.
     */
    private void resetSyntax()
    {
        strtok.resetSyntax();
        strtok.eolIsSignificant(true);
        strtok.wordChars(33, 127);
        strtok.whitespaceChars('\t', '\t');
        strtok.whitespaceChars(' ', ' ');
        strtok.whitespaceChars('\r', '\r');
        strtok.parseNumbers();

    }

    /**
     * Read the stream until the end of the line and return it as a single string
     *
     * @return A string for the rest of this line.
     */
    private String readToEOL()
        throws IOException
    {
        int type;

        strtok.resetSyntax();
        strtok.wordChars(1, 127);
        strtok.eolIsSignificant(true);
        strtok.whitespaceChars('\r', '\r');

        String last_word = null;

        do
        {
            last_word = strtok.sval;

            type = strtok.nextToken();
        }
        while(type != StreamTokenizer.TT_EOL && type != StreamTokenizer.TT_EOF);

        resetSyntax();

        return last_word;
    }

    /**
     * Clear the stream until the end of the line.
     *
     * @return true if the EOF is found, false for EOL
     */
    private boolean clearToEOL()
        throws IOException
    {
        int type;

        if(strtok.ttype == StreamTokenizer.TT_EOL)
            return false;
        else if(strtok.ttype == StreamTokenizer.TT_EOF)
            return true;
        do
        {
            type = strtok.nextToken();
        }
        while(type != StreamTokenizer.TT_EOL && type != StreamTokenizer.TT_EOF);

        return strtok.ttype == StreamTokenizer.TT_EOF;
    }
}
