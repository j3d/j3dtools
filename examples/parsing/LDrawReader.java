/*******************************************************************************
 *               J3D.org Copyright (c) 2000 - 2011
 *                             Java Source
 *  
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  
 ******************************************************************************/

// External imports
import java.io.File;
import java.io.IOException;
import java.io.FileReader;

// Local imports
import org.j3d.loaders.ldraw.*;

/**
 * Commandline app that reads a LDraw file and prints out information about it.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class LDrawReader implements LDrawParseObserver
{
    /** Manager for the scene graph handling */
    public LDrawReader()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by LDrawParseObserver
    //---------------------------------------------------------------

    /**
     * A material block has been read from the file.
     *
     * @param mat The material definition
     * @return true if to keep reading
     * @return true if to keep reading
     */
    public boolean header(LDrawHeader hdr)
    {
        System.out.println("Got header: " + hdr);
        return true;
    }

    /**
     * A BFC culling statement has been received and the following is the new state
     *
     * @param ccw true if the following polygons are wound counter clockwise
     * @param cull true if back face culling is to be performed
     * @return true if to keep reading
     */
    public boolean bfcStatement(boolean ccw, boolean cull)
    {
        System.out.println("BFC Statement: ccw " + ccw + " clip " + cull);
        return true;
    }

    /**
     * An external file reference has been detected, and these are the details.
     *
     * @param ref The details of the file reference read
     * @return true if to keep reading
     */
    public boolean fileReference(LDrawFileReference ref)
    {
        System.out.println("File reference: " + ref);
        return true;
    }

    /**
     * A surface definition has been read.
     *
     * @param boolean The polygon/line definition that is to be sent
     * @return true if to keep reading
     */
    public boolean renderable(LDrawRenderable rend)
    {
        System.out.println("Renderable: " + rend);
        return true;
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load the requested file and add it to the scene graph.
     *
     * @param filename Name of the file to load
     */
    private void load(String filename)
    {
        try
        {
            File file = new File(filename);

            if(!file.exists())
            {
                System.out.println("File " + filename + " does not exist");
                return;
            }

            LDrawParser parser = new LDrawParser(new FileReader(file));
            parser.setParseObserver(this);
            parser.parse(true);


        }
        catch(IOException ioe)
        {
            System.out.println("IO Error reading file" + ioe);
        }
    }

    public static void main(String[] args)
    {
        LDrawReader demo = new LDrawReader();
        demo.load(args[0]);
    }
}
