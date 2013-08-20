/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.loaders;

// Standard imports
import java.io.*;

import java.net.URL;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;

// Application specific imports
import org.j3d.loaders.ac3d.parser.*;

/**
 * Java 3D Loader implementation for AC3D files.
 *
 * @author  Ryan Wilhm (ryan@entrophica.com)
 * @version $Revision: 1.1 $
 */
public class Ac3dLoader implements Loader
{
    /** The token handler, which populates the <code>Scene</code>. */
    private LoaderTokenHandler tokenHandler;

    /** The flages that specify constraints for the traversal. */
    private int flags;

    /** The path to the files in use for relative file references. */
    private String basePath;

    /** The base url to the files when using relative file references. */
    private URL baseUrl;

    /**
     * <p>Default constructor, which initializes the token handler.</p>
     */

    public Ac3dLoader()
    {
        tokenHandler = new LoaderTokenHandler();
        flags = 0;
        basePath = null;
        baseUrl = null;
    }

    /**
     *
     * @param fileName
     * @return The scene instance representing the contents of the file
     * @exception FileNotFoundException
     * @exception IncorrectFormatException
     * @exception ParsingException
     */
    public Scene load(String fileName) throws FileNotFoundException,
        IncorrectFormatException, ParsingErrorException
    {

        Reader reader;

        if(basePath==null)
            tokenHandler.setBasePath(new File(fileName).getParent());
        else
            tokenHandler.setBasePath(basePath);

        reader = new FileReader(fileName);
        return load(reader);
    }


    /**
     *
     * @param reader
     * @return The scene instance representing the contents of the file
     * @exception FileNotFoundException
     * @exception IncorrectFormatException
     * @exception ParsingException
     */
    public Scene load(Reader reader) throws FileNotFoundException,
        IncorrectFormatException, ParsingErrorException {

        Scene rVal=null;
        BufferedReader br=new BufferedReader(reader);
        Ac3dParser parser=new Ac3dParser();


        try
        {
            parser.setBufferedReader(br);
            tokenHandler.setBufferedReader(br);
            parser.setTokenHandler(tokenHandler);
            parser.parse();
            rVal=tokenHandler.getScene();
        }
        catch (Exception e)
        {
            // Deal with exception stuff
            System.err.println("Exception during parse: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        return rVal;
    }


    /**
     *
     * @param url
     * @return The scene instance representing the contents of the file
     * @exception FileNotFoundException
     * @exception IncorrectFormatException
     * @exception ParsingException
     */
    public Scene load(URL url) throws FileNotFoundException,
        IncorrectFormatException, ParsingErrorException {

        Reader reader;

        if (baseUrl==null)
            tokenHandler.setBaseUrl(url);
        else
            tokenHandler.setBaseUrl(baseUrl);

        try
        {
            reader = new InputStreamReader(url.openStream());
        }
        catch (IOException e)
        {
            throw new FileNotFoundException(e.getMessage());
        }

        return load(reader);
    }


    /**
     */
    public void setFlags(int flags)
    {
        this.flags=flags;
    }

    /**
     * <p>Accessor for the <code>flags</code> property.</p>
     *
     * @return The current state of the <code>flags</code> property.
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     *
     */
    public void setBaseUrl(URL baseUrl)
    {
        this.baseUrl=baseUrl;
    }

    /**
     *
     */
    public URL getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * Mutator that sets the <code>basePath</code> property.
     *
     * @param basePath The value to set the <code>basePath</code> property
     *                 to.
     */
    public void setBasePath(String basePath)
    {
        this.basePath=basePath;
    }

    /**
     * Accessor for the <code>basePath</code> property.
     *
     * @return The current state of the <code>basePath</code> property.
     */
    public String getBasePath()
    {
        return basePath;
    }
}
