/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter.graph;

// External imports
// None

// Local Imports
// None

/**
 * Represents a group node that can move it's children around spatially. 
 * <p>
 * The matrix is presented as a flat array in row-major form:
 * </p>
 * <p>
 * Given the matrix definition like this:
 * </p>
 * <pre>
 * |m00 m01 m02 m03|
 * |m10 m11 m12 m13|
 * |m20 m21 m22 m23|
 * |m30 m31 m32 m33|
 * </pre>
 * <p>
 * Where (m03, m13, m23) represents the translation components of the matrix,
 * This is placed in the array in the order:
 * </p>
 * <pre>
 * [m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33]
 * </pre>
 * <p>
 * The default matrix, if not set, is the identity matrix
 * </p>
 * 
 * @author Justin Couch
 * @version $Revision$
 */
public interface TransformGroup
    extends Group
{

    /** 
     * Get the 4x4 transformation matrix. See class docs for the matrix
     * definition.
     * 
     * @param mat The matrix to copy the matrix data in to. Must be at 
     *    least length 16
     */
    public void getTransformationMatrix(double[] mat);
    
    /**
     * Set the transformation matrix as a 4x4 matrix. Assumes that the 
     * input array is at least length 16 and in the format described
     * in the class documentation. 
     * 
     * @param mat The new matrix value to set.
     */
    public void setTransformationMatrix(double[] mat);
}
