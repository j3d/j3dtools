/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// Standard imports

// Application Specific imports
import org.j3d.geom.*;


/**
 * Tests for the Triangulation utilities.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TriangulationUtilsDemo
{
    public static void main(String[] argv)
    {
        TriangulationUtils triangulator = new TriangulationUtils();

        float[] coords =
        {
              -2,    -1, 0,
              -2,     1, 0,
            0.5f,     1, 0,
            0.5f, -0.5f, 0,
            1.5f, -0.5f, 0,
            1.5f,     1, 0,
               2,     1, 0,
               2,    -1, 0,
           -0.5f,    -1, 0,
           -0.5f,  0.5f, 0,
           -1.5f,  0.5f, 0,
           -1.5f,    -1, 0
        };

        float[] normal = { 0, 0, 1 };
        int[] output = new int[30];

        for(int i = 0; i < coords.length; )
        {
            System.out.print(i);
            System.out.print(" ");
            System.out.print(coords[i++]);
            System.out.print(" ");
            System.out.print(coords[i++]);
            System.out.print(" ");
            System.out.print(coords[i++]);
            System.out.println();
        }

        int num = triangulator.triangulateConcavePolygon(coords,
                                                         0,
                                                         12,
                                                         output,
                                                         normal);

        System.out.println("number of triangles = " + num);

        for(int i = 0; i < num; i++)
        {
            System.out.print(i);
            System.out.print(": ");
            System.out.print(output[i * 3]);
            System.out.print(" ");
            System.out.print(output[i * 3 + 1]);
            System.out.print(" ");
            System.out.print(output[i * 3 + 2]);
            System.out.print(" c ");
            System.out.print(coords[output[i * 3]]);
            System.out.print(" ");
            System.out.print(coords[output[i * 3] + 1]);
            System.out.print(" ");
            System.out.print(coords[output[i * 3] + 2]);
            System.out.print(", ");
            System.out.print(coords[output[i * 3 + 1]]);
            System.out.print(" ");
            System.out.print(coords[output[i * 3 + 1] + 1]);
            System.out.print(" ");
            System.out.print(coords[output[i * 3 + 1] + 2]);
            System.out.print(", ");
            System.out.print(coords[output[i * 3 + 2]]);
            System.out.print(" ");
            System.out.print(coords[output[i * 3 + 2] + 1]);
            System.out.print(" ");
            System.out.print(coords[output[i * 3 + 2] + 2]);
            System.out.println();
        }

        System.out.println("Trying indexed version");

        int[] indexes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

        num = triangulator.triangulateConcavePolygon(coords,
                                                     0,
                                                     12,
                                                     indexes,
                                                     output,
                                                     normal);

        System.out.println("number of triangles = " + num);

        for(int i = 0; i < num; i++)
        {
            System.out.print(i);
            System.out.print(": ");
            System.out.print(output[i * 3]);
            System.out.print(" ");
            System.out.print(output[i * 3 + 1]);
            System.out.print(" ");
            System.out.print(output[i * 3 + 2]);
            System.out.print(" c ");
            System.out.print(coords[output[i] * 3]);
            System.out.print(" ");
            System.out.print(coords[output[i] * 3 + 1]);
            System.out.print(" ");
            System.out.print(coords[output[i] * 3 + 2]);
            System.out.print(", ");
            System.out.print(coords[output[i + 1] * 3]);
            System.out.print(" ");
            System.out.print(coords[output[i + 1] * 3 + 1]);
            System.out.print(" ");
            System.out.print(coords[output[i + 1] * 3 + 2]);
            System.out.print(", ");
            System.out.print(coords[output[i + 2] * 3]);
            System.out.print(" ");
            System.out.print(coords[output[i + 2] * 3 + 1]);
            System.out.print(" ");
            System.out.print(coords[output[i + 2] * 3 + 2]);
            System.out.println();
        }
    }
}
