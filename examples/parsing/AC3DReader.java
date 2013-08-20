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
import org.j3d.loaders.ac3d.*;

/**
 * Commandline app that reads a C3D file and prints out information about it.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class AC3DReader implements Ac3dParseObserver
{
    /** Manager for the scene graph handling */
    public AC3DReader()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by Ac3dParseObserver
    //---------------------------------------------------------------

    /**
     * A material block has been read from the file.
     *
     * @param mat The material definition
     * @return true if to keep reading
     */
    public boolean materialComplete(Ac3dMaterial mat)
    {
        System.out.println("Got Material");
        System.out.println(mat);
        return true;
    }

    /**
     * An object has been completed.Object calls are top donw -
     * the parent is read and sent, but at the time it is sent does not
     * yet contain the children.
     *
     * @param parent The parent object that contains this surface
     * @param object The object that was just read
     * @return true if to keep reading
     */
    public boolean objectComplete(Ac3dObject parent, Ac3dObject obj)
    {
        System.out.print("Got Object. Parent:");
        if(parent == null)
            System.out.println(" none");
        else
            System.out.println(parent.getName());

        System.out.println(obj);

        return true;
    }

    /**
     * A surface definition from the previously declared object
     * has been read.
     *
     * @param obj The parent object that contains this surface
     * @param surf The surface object that has been read
     * @return true if to keep reading
     */
    public boolean surfaceComplete(Ac3dObject obj, Ac3dSurface surf)
    {
        System.out.print("Got Surface. Parent:");
        if(obj == null)
            System.out.println(" none");
        else
            System.out.println(obj.getName());

        System.out.println(surf);

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

            Ac3dParser parser = new Ac3dParser(new FileReader(file));
            parser.setParseObserver(this);
            parser.parse(true);

            Ac3dMaterial[] materials = parser.getMaterials();
            System.out.println("Parse OK. Material Count\n " + materials.length);

        }
        catch(IOException ioe)
        {
            System.out.println("IO Error reading file" + ioe);
        }
    }

    public static void main(String[] args)
    {
        AC3DReader demo = new AC3DReader();
        demo.load(args[0]);
    }
}
