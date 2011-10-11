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
// None

// Local parser
// None

/**
 * Representation of the header portion of the file.
 * <p>
 *
 * For all header elements, if they are not defined, the return value
 * for the corresponding element is null.
 *
 * <p><b>Documentation</b></p>
 *
 * <p>
 * The official meta commands (including header keywords):
 * <a href="http://www.ldraw.org/Article401.html">
 *  http://www.ldraw.org/Article401.html</a>
 * </p>
 * The official header spec:
 * <a href="http://www.ldraw.org/Article398.html">
 *  http://www.ldraw.org/Article398.html</a>
 * </p>
 * <p>
 * The definition of the file format:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 * </p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawHeader
{
    /** The type of file definition this represents */
    private LDrawFileType fileType;

    /** Who authored the file, if known */
    private String author;

    /** The version of this file, if known */
    private String version;

    /** License declaration about this file's contents */
    private String license;

    /** The declared name of the file. If not declared, this will be null */
    private String fileName;

    /**
     * Whether back face culling extension is declared and the winding
     * direction. Defaults to true, as per the spec.
     */
    private boolean ccw;

    /** Set true when this is an official LDraw file */
    private boolean official;

    /**
     * Create a default header representation. BFC is not enabled, but defaults
     * to CCW winding.
     */
    public LDrawHeader()
    {
        ccw = true;
        official = false;
    }

    /**
     * Get the type of file that this header represents
     *
     * @return one of the enumerated file types
     */
    public LDrawFileType getFileType()
    {
        return fileType;
    }

    /**
     * Set the type of the file that has been read. Usually determined after
     * reading some part of the header statement.
     *
     * @param type The new type enum to set it to
     */
    void setFileType(LDrawFileType type)
    {
        fileType = type;
    }

    /**
     * Get the current license string that is set. There's no standard format
     * to the license string in use.
     *
     * @return A possibly null license string
     */
    public String getLicense()
    {
        return license;
    }

    /**
     * Set the license string that was declared for this file
     *
     * @param str The source string for the license declaration
     */
    void setLicense(String str)
    {
        license = trim(str);
    }

    /**
     * Get the version number of the model contained in this parts
     * specification. All files in the official repository should have a
     * version number, but it is not required.
     *
     * @return A possibly null version string
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Set the version string that was declared for this file
     *
     * @param str The source string for the version declaration
     */
    void setVersion(String str)
    {
        version = trim(str);
    }

    /**
     * Get the name of the file that was parsed for this model. This may
     * be determinable from the incoming source, but also may not provide
     * anything, depending on how the stream was originally loaded.
     *
     * @return A possibly null file name string
     */
    public String getName()
    {
        return fileName;
    }

    /**
     * Set the file name that was declared for this file
     *
     * @param str The source string for the file declaration
     */
    void setName(String str)
    {
        fileName = trim(str);
    }

    /**
     * Get the optional author declaration that may be provided with the file.
     *
     * @return A possibly null author declaration
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Set the author that was declared for this file
     *
     * @param str The source string for the author's name
     */
    void setAuthor(String str)
    {
        author = trim(str);
    }

    /**
     * Check to see what the winding of the polygons are. This takes in to
     * account anything that may be declared with the BFC extension. By default
     * everything is clockwise.
     *
     * @return true if all triangles are to be declared in counter-clockwise order
     */
    public boolean isCCW()
    {
        return ccw;
    }

    /**
     * Set the CCW state from the BFC header statements . By default, this
     * value is true.
     *
     * @param ccw true for ccw winding
     */
    void setCCW(boolean ccw)
    {
        this.ccw = ccw;
    }

    /**
     * Check to see if this is a model from the official LDraw repository.
     *
     * @return true if this is a model from the official repository
     */
    public boolean isOfficial()
    {
        return official;
    }

    /**
     * Set the official LDraw format state from the header declaration. By
     * default this value is false.
     *
     * @param state true if this is an official file
     */
    void setOfficial(boolean state)
    {
        official = state;
    }

    /**
     * Internal convenience method to trim a string of extra whitespace and
     * check to see if it is just empty, which we reinterpret to be null
     *
     * @param str The source string to process
     * @return The trimmed string or null if only whitespace
     */
    private String trim(String str)
    {
        String ret_val = str != null ? str.trim() : null;

        if(ret_val != null && ret_val.length() == 0)
            ret_val = null;

        return ret_val;
    }
}
