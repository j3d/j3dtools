/*******************************************************************************
 *               J3D.org Copyright (c) 2000 - 2011
 *                             Java Source
 *  
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  
 ******************************************************************************/

package j3d.examples.loaders;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

// Local imports
import org.j3d.loaders.c3d.*;

/**
 * Commandline app that reads a C3D file and prints out information about it.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class C3DReader
{
    /** Manager for the scene graph handling */
    public C3DReader()
    {
    }

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

            C3DParser parser = new C3DParser(new FileInputStream(file));
            parser.parse(true);

            C3DHeader header = parser.getHeader();
            System.out.println("Parse OK. Header\n " + header);

        }
        catch(IOException ioe)
        {
            System.out.println("IO Error reading file" + ioe);
        }
    }


    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        C3DReader demo = new C3DReader();
        demo.load(args[0]);
    }
}
