/*****************************************************************************
 *                  Web3d Consortium Copyright (c) 2001 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.j3d.io;

// External imports
import java.net.FileNameMap;
import java.util.HashMap;

// Local imports
// None

/**
 * FileNameMap for the URL resolution system so that content types can be
 * accurately determined, even when the local platform does not have a
 * mapping.
 * <p>
 *
 * Will automatically test all extensions against lower case forms. Will
 * automatically lower case anything that is added to this map.
 * <p>
 * 
 * Copied from my original code in Xj3D filter code.
 * 
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ParserNameMap implements FileNameMap
{

    /** Mapping of extension to mime type string */
    private HashMap<String, String> contentTypeMap;

    /** Mapping of extension to mime type string */
    private HashMap<String, String> reverseMap;

    /**
     * Create a new empty map instance.
     */
    public ParserNameMap() 
    {
        contentTypeMap = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    //---------------------------------------------------------------
    // Methods defined by org.ietf.uri.FileNameMap
    //---------------------------------------------------------------

    /**
     * Gets the file extension for the specified mime type. If none is known,
     * return null. Always will return the lower-case form of what was
     * originally registered, regardless of what case was provided.
     *
     * @param mimetype The mimetype to extract an extension for
     * @return The matching extension type or null if not known
     */
    public String getFileExtension(String mimetype) 
    {
        return reverseMap.get(mimetype);
    }

    //---------------------------------------------------------------
    // Methods defined by java.net.FileNameMap
    //---------------------------------------------------------------

    /**
     * Gets the MIME type for the specified file name. If none is known,
     * return null.
     *
     * @param filename The filename to extract an extension from
     * @return The matching MIME type or null if not known
     */
    @Override
    public String getContentTypeFor(String filename)
    {
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        ext = ext.toLowerCase();
        return contentTypeMap.get(ext);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Register a file type and corresponding extension. Will automatically
     * convert the extension to lower case.
     *
     * @param extension The file extension - everything after the last dot
     * @param mimetype The mimetype string to use
     */
    public void registerType(String extension, String mimetype) 
    {
        String ext = extension.toLowerCase();
        contentTypeMap.put(ext, mimetype);
        reverseMap.put(mimetype, ext);
    }
 }
