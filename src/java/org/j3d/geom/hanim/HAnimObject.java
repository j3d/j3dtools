/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.hanim;

// External imports
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Matrix4f;

// Local imports
// None

/**
 * Common object functionality for all objects that implement the H-Anim spec.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class HAnimObject
{
    /** High-Side epsilon float = 0 */
    private static final float ZEROEPS = 0.0001f;

    /** The name of the object */
    protected String name;

    // Classes for doing the matrix multiplication

    private static Vector3f tempVec;
    private static AxisAngle4f tempAxis;
    private static Matrix4f tempMtx1;
    private static Matrix4f tempMtx2;
    private static Matrix4f tempMtx3;

    // Static constructor to set up the common nodes
    static
    {
        tempVec = new Vector3f();
        tempAxis = new AxisAngle4f();
        tempMtx1 = new Matrix4f();
        tempMtx2 = new Matrix4f();
        tempMtx3 = new Matrix4f();
    }

    /**
     * Set the new name to associate with this object. A null value will remove
     * the currently set name.
     *
     * @param n The new name value to set
     */
    public void setName(String n)
    {
        name = n;
    }

    /**
     * Get the currently set name associated with this object. If none is set,
     * return null.
     *
     * @return The current name string, or null
     */
    public String getName()
    {
        return name;
    }

    /**
     * Compares to floats to determine if they are equal or very close
     *
     * @param val1 The first value to compare
     * @param val2 The second value to compare
     * @return True if they are equal within the given epsilon
     */
    private static boolean floatEq(float val1, float val2)
    {
        float diff = val1 - val2;

        if(diff < 0)
            diff *= -1;

        return (diff < ZEROEPS);
    }

    /**
     * Calculate transforms needed to handle VRML semantics and place the
     * results in the matrix variable of this class.
     *  formula: T x C x R x SR x S x -SR x -C
     */
    protected static void updateMatrix(float[] center,
                                       float[] rotation,
                                       float[] scale,
                                       float[] scaleOrientation,
                                       float[] translation,
                                       Matrix4f output)
    {

        //System.out.println(this);
        tempVec.x = -center[0];
        tempVec.y = -center[1];
        tempVec.z = -center[2];

        tempMtx3.setIdentity();
        tempMtx3.setTranslation(tempVec);

        float scaleVal = 1.0f;

        if(floatEq(scale[0], scale[1]) && floatEq(scale[0], scale[2]))
        {

            scaleVal = scale[0];
            tempMtx1.set(scaleVal);
        }
        else
        {
            // non-uniform scale
            tempAxis.x = scaleOrientation[0];
            tempAxis.y = scaleOrientation[1];
            tempAxis.z = scaleOrientation[2];
            tempAxis.angle = -scaleOrientation[3];

            double tempAxisNormalizer =
                1 / Math.sqrt(tempAxis.x * tempAxis.x +
                              tempAxis.y * tempAxis.y +
                              tempAxis.z * tempAxis.z);

            tempAxis.x *= tempAxisNormalizer;
            tempAxis.y *= tempAxisNormalizer;
            tempAxis.z *= tempAxisNormalizer;

            tempMtx1.set(tempAxis);
            tempMtx2.mul(tempMtx1, tempMtx3);

            // Set the scale by individually setting each element
            tempMtx1.setIdentity();
            tempMtx1.m00 = scale[0];
            tempMtx1.m11 = scale[1];
            tempMtx1.m22 = scale[2];

            tempMtx3.mul(tempMtx1, tempMtx2);

            tempAxis.x = scaleOrientation[0];
            tempAxis.y = scaleOrientation[1];
            tempAxis.z = scaleOrientation[2];
            tempAxis.angle = scaleOrientation[3];
            tempMtx1.set(tempAxis);
        }

        tempMtx2.mul(tempMtx1, tempMtx3);

        //System.out.println("Sx-C" + tempMtx2);
        float magSq = rotation[0] * rotation[0] +
                      rotation[1] * rotation[1] +
                      rotation[2] * rotation[2];

        if(magSq < ZEROEPS)
        {
            tempAxis.x = 0;
            tempAxis.y = 0;
            tempAxis.z = 1;
            tempAxis.angle = 0;
        }
        else
        {
            if((magSq > 1.01) || (magSq < 0.99))
            {

                float mag = (float)(1 / Math.sqrt(magSq));
                tempAxis.x = rotation[0] * mag;
                tempAxis.y = rotation[1] * mag;
                tempAxis.z = rotation[2] * mag;
            }
            else
            {
                tempAxis.x = rotation[0];
                tempAxis.y = rotation[1];
                tempAxis.z = rotation[2];
            }

            tempAxis.angle = rotation[3];
        }

        tempMtx1.set(tempAxis);
        tempMtx3.mul(tempMtx1, tempMtx2);

        tempVec.x = center[0];
        tempVec.y = center[1];
        tempVec.z = center[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);

        tempMtx2.mul(tempMtx1, tempMtx3);

        tempVec.x = translation[0];
        tempVec.y = translation[1];
        tempVec.z = translation[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);

        output.mul(tempMtx1, tempMtx2);
    }
}
