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

import org.j3d.maths.vector.Matrix4d;

/**
 * Base representation of a part from the file that is not the header.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawFileReference extends LDrawColoredPart
{
    /** The transformation matrix in row major form */
    private double[] matrix;

    /** Vecmath representation of the matrix. Only allocated if requested */
    private Matrix4d altMatrix;

    /**
     * The external file reference name. May be absolute or relative - see
     * LDraw spec on how this may be interpreted.
     */
    private String fileReference;

    /**
     * Construct the base part that is rendered in the specific colour. The
     * class keeps a reference to the given matrix rather than a copy.
     *
     * @param col The colour to render in. Most not be null
     * @param ref The file reference to load
     * @param matrix THe transformation matrix to use.
     */
    public LDrawFileReference(LDrawColor col, String ref, double[] matrix)
    {
        super(col);

        assert ref != null && ref.trim().length() != 0 :
               "Need valid external file reference";

        this.matrix = matrix;
        fileReference = ref;
    }

    //------------------------------------------------------------------------
    // Methods defined by Object
    //------------------------------------------------------------------------

    @Override
    public String toString()
    {
        StringBuilder bldr = new StringBuilder("LDraw File Reference ");
        bldr.append("Colour ID ");
        bldr.append(getColor());
        bldr.append(" Inverted? ");
        bldr.append(isInvertedWinding() ? 'Y' : 'N');
        bldr.append(" matrix [ ");

        for(int i = 0; i < matrix.length; i++)
        {
            bldr.append(matrix[i]);
            bldr.append(' ');
        }

        bldr.append("] file: \"");
        bldr.append(fileReference);
        bldr.append("\"");

        return bldr.toString();
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Get the matrix that is used to manipulate the referenced part.
     * Returns the internal reference, not a copy. The matrix is
     * represented in row-major form.
     *
     * @return A reference to the internal matrix value
     */
    public double[] getMatrix()
    {
        return matrix;
    }

    /**
     * Returns the matrix information as a matrix4d. This takes a
     * copy of the internal data, but returns the same referenced object
     * for each call.
     *
     * @return A complete 4x4 matrix definition for the object
     */
    public Matrix4d getMatrixAsMatrix()
    {
        if(altMatrix == null)
        {
            altMatrix = new Matrix4d();
            altMatrix.m00 = matrix[0];
            altMatrix.m01 = matrix[1];
            altMatrix.m02 = matrix[2];
            altMatrix.m03 = matrix[3];

            altMatrix.m10 = matrix[4];
            altMatrix.m11 = matrix[5];
            altMatrix.m12 = matrix[6];
            altMatrix.m13 = matrix[7];

            altMatrix.m20 = matrix[8];
            altMatrix.m21 = matrix[9];
            altMatrix.m22 = matrix[10];
            altMatrix.m23 = matrix[11];

            altMatrix.m30 = matrix[12];
            altMatrix.m31 = matrix[13];
            altMatrix.m32 = matrix[14];
            altMatrix.m33 = matrix[15];
        }

        return altMatrix;
    }

    /**
     * Get the external file reference to render. The rules of interpreting
     * this are defined by the LDraw spec.
     *
     * @return a non-empty file reference string
     */
    public String getReference()
    {
        return fileReference;
    }
}
