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

package org.j3d.util;

// External imports
// None

// Local imports
import org.j3d.maths.vector.*;

/**
 * A utility class that performs various matrix operations on the
 * {@link org.j3d.maths.vector} package.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class MatrixUtils
{
    /** Work variable for the fallback lookat calculations. */
    private AxisAngle4d orientd;

    /** A temp 4x4 matrix used during the invert() routines */
    private double[] resMat4;

	/** A temp 3x3 double matrix used during the invert() routines 
	 *  when double precision is required */
	private double[] tempMat3d;
	
	/** A temp 4x4 double matrix used during the invert() routines 
	 *  when double precision is required */
	private double[] tempMat4d;

    /** SVD decomposer when getting scales */
    private SingularValueDecomposition svdUtil;

    /**
     * Construct a default instance of this class.
     */
    public MatrixUtils()
    {
        resMat4 = new double[16];
		
		tempMat3d = new double[9];
		tempMat4d = new double[16];
    }

    /**
     * Perform a LookAt camera calculation and place it in the given matrix.
     * If using this for a viewing transformation, you should invert() the
     * matrix after the call.
     *
     * @param eye The eye location
     * @param center The place to look at
     * @param up The up vector
     * @param res The result to put the calculation into
     */
    public void lookAt(Point3d eye, Point3d center, Vector3d up, Matrix4d res)
    {
        double d = eye.x - center.x;
        double d1 = eye.y - center.y;
        double d2 = eye.z - center.z;

        double det = d * d + d1 * d1 + d2 * d2;
        if(det != 1)
        {
            if(det == 0)
            {
                res.setIdentity();
                res.m03 = eye.x;
                res.m13 = eye.y;
                res.m23 = eye.z;
                return;
            }

            det = 1 / Math.sqrt(det);
            d *= det;
            d1 *= det;
            d2 *= det;
        }

        double d4 = up.x;
        double d5 = up.y;
        double d6 = up.z;

        det = (up.x * up.x + up.y * up.y + up.z * up.z);
        if(det != 1)
        {
            if(det == 0)
                throw new IllegalArgumentException("Up vector is all zeroes");

            det = 1 / Math.sqrt(det);
            d4 *= det;
            d5 *= det;
            d6 *= det;
        }

        double d7 = d5 * d2 - d1 * d6;
        double d8 = d6 * d - d4 * d2;
        double d9 = d4 * d1 - d5 * d;

        det = d7 * d7 + d8 * d8 + d9 * d9;

        if(det != 1)
        {
            // If this value is zero then we have a case where the up vector is
            // parallel to the eye-center vector. In this case, set the
            // translation to the normal location and recalculate everything
            // again using the slow way using a reference of the normal view
            // orientation pointing along the -Z axis.
            if(det == 0)
            {
                lookAtFallback(eye, d, d1, d2, res);
                return;
            }

            det = 1 / Math.sqrt(det);
            d7 *= det;
            d8 *= det;
            d9 *= det;
        }

        d4 = d1 * d9 - d8 * d2;
        d5 = d2 * d7 - d * d9;
        d6 = d * d8 - d1 * d7;

        res.m00 = d7;
        res.m01 = d8;
        res.m02 = d9;
        res.m03 = (-eye.x * res.m00 - eye.y * res.m01 - eye.z * res.m02);

        res.m10 = d4;
        res.m11 = d5;
        res.m12 = d6;
        res.m13 = (-eye.x * res.m10 - eye.y * res.m11 - eye.z * res.m12);

        res.m20 = d;
        res.m21 = d1;
        res.m22 = d2;
        res.m23 = (-eye.x * res.m20 - eye.y * res.m21 - eye.z * res.m22);

        res.m30 = 0;
        res.m31 = 0;
        res.m32 = 0;
        res.m33 = 1;
    }

    /**
     * Generate a projection matrix using the given configuration, using OpenGL-style
     * calculation. The calculation leaves it in the result matrix in the expected
     * row-major form of Matrix4d, so if you are going to use this in an OGL call,
     * make sure you transpose it first. Assumes the usual location of on the origin
     * with right-handed coordinate system, looking down the -Z axis.
     *
     * @param left The value of the left edge of the view plane (-X axis)
     * @param right The value of the right edge of the view plane (+X axis)
     * @param top The value of the top edge of the view plane (Y axis)
     * @param bottom The value of the bottom edge of the view plane (-Y axis)
     * @param near The value of the near distance of the view plane (-Z axis)
     * @param far The value of the far distance of the view plane (-Z axis)
     * @param projectionMatrix
     */
    public void generateProjectionMatrix(double left, double right, double top, double bottom, double near, double far,
                                         Matrix4d projectionMatrix)
    {
        double x, y;
        double a, b, c, d;

        x = (2.0 * near) / (right - left);
        y = (2.0 * near) / (top - bottom);
        a = (right + left) / (right - left);
        b = (top + bottom) / (top - bottom);
        c = -(far + near) / ( far - near);
        d = -(2.0 * far * near) / (far - near);

        //////////////////////////////////////////////
        // rem: clamping to the limit as farval approaches infinity
        // see: http://www.terathon.com/gdc07_lengyel.ppt
        if (d < -Float.MAX_VALUE)
        {
            d = -2;
        }
        //////////////////////////////////////////////

        // This is in row-form like our Matrix4d expects. Typical OGL documentation
        // has this in column-form, so don't forget to transpose if you're dropping
        // this into an openGL glMatrix() call.
        projectionMatrix.m00 = x;
        projectionMatrix.m10 = 0;
        projectionMatrix.m20 = a;
        projectionMatrix.m30 = 0;

        projectionMatrix.m01 = 0;
        projectionMatrix.m11 = y;
        projectionMatrix.m21 = b;
        projectionMatrix.m31 = 0;

        projectionMatrix.m02 = 0;
        projectionMatrix.m12 = 0;
        projectionMatrix.m22 = c;
        projectionMatrix.m32 = d;

        projectionMatrix.m03 = 0;
        projectionMatrix.m13 = 0;
        projectionMatrix.m23 = -1;
        projectionMatrix.m33 = 0;
    }

    /**
     * Get the uniform scale component of the given matrix. Performs a decomposition and
     * returns the maximum of the scale values across the 3 directional components.
     *
     * @param mat The matrix to source the uniform scale from
     */
    public double getUniformScale(Matrix4d mat)
    {
        // Put the upper 3x3 into the temp matrix before decomposing
        tempMat3d[0] = mat.m00;
        tempMat3d[1] = mat.m01;
        tempMat3d[2] = mat.m02;

        tempMat3d[3] = mat.m10;
        tempMat3d[4] = mat.m11;
        tempMat3d[5] = mat.m12;

        tempMat3d[6] = mat.m20;
        tempMat3d[7] = mat.m21;
        tempMat3d[8] = mat.m22;

        if(svdUtil == null)
        {
            svdUtil = new SingularValueDecomposition();
        }

        svdUtil.decompose(tempMat3d);
        svdUtil.getSingularValues(tempMat4d);

        // since the S values are ordered in decreasing size, just take the first one
        // and we have the max scale factor
        return tempMat4d[0];
    }

    /**
     * Perform a Singular Value Decomposition (SVD) of the given input matrix for the upper
     * left 3x3 matrix.
     *
     * @param mat The matrix to source the uniform scale from
     * @param uOutput The optional output for the left singular vectors. If null not generated
     * @param vOutput The optional output for the right singular vectors. If null not generated
     * @param sOutput The singular (diagonal) vector
     */
    public void decomposeSVD(Matrix4d mat, double[] uOutput, double[] vOutput, double[] sOutput)
    {
        // Put the upper 3x3 into the temp matrix before decomposing
        tempMat3d[0] = mat.m00;
        tempMat3d[1] = mat.m01;
        tempMat3d[2] = mat.m02;

        tempMat3d[3] = mat.m10;
        tempMat3d[4] = mat.m11;
        tempMat3d[5] = mat.m12;

        tempMat3d[6] = mat.m20;
        tempMat3d[7] = mat.m21;
        tempMat3d[8] = mat.m22;

        if(svdUtil == null)
        {
            svdUtil = new SingularValueDecomposition();
        }

        double[][] m = new double[3][3];
        m[0][0] = mat.m00;
        m[0][1] = mat.m01;
        m[0][2] = mat.m02;

        m[1][0] = mat.m10;
        m[1][1] = mat.m11;
        m[1][2] = mat.m12;

        m[2][0] = mat.m20;
        m[2][1] = mat.m21;
        m[2][2] = mat.m22;

        svdUtil.generateLeftVectors(uOutput != null);
        svdUtil.generateRightVectors(vOutput != null);
        svdUtil.decompose(m);

        svdUtil.getSingularValues(sOutput);
    }

    /**
     * Set the upper 3x3 matrix based on the given the euler angles.
     *
     * @param angles The set of angles to use, one per axis
     * @param mat The matrix to set the values in
     */
    public void setEuler(Vector3d angles, Matrix4d mat)
    {
        setEuler(angles.x, angles.y, angles.z, mat);
    }

    /**
     * Set the upper 3x3 matrix based on the given the euler angles.
     *
     * @param x The amount to rotate around the X axis
     * @param y The amount to rotate around the Y axis
     * @param z The amount to rotate around the Z axis
     * @param mat The matrix to set the values in
     */
    public void setEuler(double x, double y, double z, Matrix4d mat)
    {
        double a = Math.cos(x);
        double b = Math.sin(x);
        double c = Math.cos(y);
        double d = Math.sin(y);
        double e = Math.cos(z);
        double f = Math.sin(z);
        double a_d = a * d;
        double b_d = b * d;

        mat.m00 = c * e;
        mat.m01 = -c * f;
        mat.m02 = d;
        mat.m03 = 0;

        mat.m10 = b_d * e + a * f;
        mat.m11 = -b_d * f + a * e;
        mat.m12 = -b * c;
        mat.m13 = 0;

        mat.m20 = -a_d * e + b * f;
        mat.m21 = a_d * f + b * e;
        mat.m22 = a * c;
        mat.m23 = 0;

        mat.m30 = 0;
        mat.m31 = 0;
        mat.m33 = 1;
        mat.m32 = 0;
    }

    /**
     * Set the matrix to the rotation about the X axis by the given angle.
     *
     * @param angle The angle to rotate in radians
     * @param mat The matrix to set the values in
     */
    public void rotateX(double angle, Matrix4d mat)
    {
        double a = Math.cos(angle);
        double b = Math.sin(angle);

        mat.m00 = 1;
        mat.m01 = 0;
        mat.m02 = 0;
        mat.m03 = 0;

        mat.m10 = 0;
        mat.m11 = a;
        mat.m12 = -b;
        mat.m13 = 0;

        mat.m20 = 0;
        mat.m21 = b;
        mat.m22 = a;
        mat.m23 = 0;

        mat.m30 = 0;
        mat.m31 = 0;
        mat.m33 = 1;
        mat.m32 = 0;
    }

    /**
     * Set the matrix to the rotation about the Y axis by the given angle.
     *
     * @param angle The angle to rotate in radians
     * @param mat The matrix to set the values in
     */
    public void rotateY(double angle, Matrix4d mat)
    {
        double a = Math.cos(angle);
        double b = Math.sin(angle);

        mat.m00 = a;
        mat.m01 = 0;
        mat.m02 = b;
        mat.m03 = 0;

        mat.m10 = 0;
        mat.m11 = 1;
        mat.m12 = 0;
        mat.m13 = 0;

        mat.m20 = -b;
        mat.m21 = 0;
        mat.m22 = a;
        mat.m23 = 0;

        mat.m30 = 0;
        mat.m31 = 0;
        mat.m33 = 1;
        mat.m32 = 0;
    }

    /**
     * Set the matrix to the rotation about the Y axis by the given angle.
     *
     * @param angle The angle to rotate in radians
     * @param mat The matrix to set the values in
     */
    public void rotateZ(double angle, Matrix4d mat)
    {
        double a = Math.sin(angle);
        double b = Math.cos(angle);

        mat.m00 = b;
        mat.m01 = -a;
        mat.m02 = 0.0;
        mat.m03 = 0.0;

        mat.m10 = a;
        mat.m11 = b;
        mat.m12 = 0.0;
        mat.m13 = 0.0;

        mat.m20 = 0.0;
        mat.m21 = 0.0;
        mat.m22 = 1.0;
        mat.m23 = 0.0;

        mat.m30 = 0.0;
        mat.m31 = 0.0;
        mat.m32 = 0.0;
        mat.m33 = 1.0;    
    }

    /**
     * Calculate the inverse of a 4x4 matrix and place it in the output. The
     * implementation uses the algorithm from
     * http://www.j3d.org/matrix_faq/matrfaq_latest.html#Q24
     *
     * @param src The source matrix to read the values from
     * @param dest The place to put the inverted matrix
     * @return true if the inversion was successful
     */
    public boolean inverse(Matrix4d src, Matrix4d dest)
	{
		double mdet = src.determinant();
		
		if(Math.abs(mdet) < 0.0000005f)
		{
			dest.setIdentity();
			return false;
		}

		mdet = 1 / mdet;
		
		// copy the matrix into an array for faster calcs
		tempMat4d[0] = src.m00;
		tempMat4d[1] = src.m01;
		tempMat4d[2] = src.m02;
		tempMat4d[3] = src.m03;
		
		tempMat4d[4] = src.m10;
		tempMat4d[5] = src.m11;
		tempMat4d[6] = src.m12;
		tempMat4d[7] = src.m13;
		
		tempMat4d[8] = src.m20;
		tempMat4d[9] = src.m21;
		tempMat4d[10] = src.m22;
		tempMat4d[11] = src.m23;
		
		tempMat4d[12] = src.m30;
		tempMat4d[13] = src.m31;
		tempMat4d[14] = src.m32;
		tempMat4d[15] = src.m33;

        // calculate the cofactor matrix

		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				int sign = 1 - ((i + j) % 2) * 2;
				submatrixd(i, j);
				resMat4[i + j * 4] = determinant3x3() * sign * mdet;
			}
		}

        // Now copy it back to the destination
        dest.m00 = resMat4[0];
        dest.m01 = resMat4[1];
        dest.m02 = resMat4[2];
        dest.m03 = resMat4[3];

        dest.m10 = resMat4[4];
        dest.m11 = resMat4[5];
        dest.m12 = resMat4[6];
        dest.m13 = resMat4[7];

        dest.m20 = resMat4[8];
        dest.m21 = resMat4[9];
        dest.m22 = resMat4[10];
        dest.m23 = resMat4[11];

        dest.m30 = resMat4[12];
        dest.m31 = resMat4[13];
        dest.m32 = resMat4[14];
        dest.m33 = resMat4[15];
        
        // finally transpose it
        dest.transpose(dest);

        return true;
	}

    /**
	 * Find the 3x3 submatrix for the 4x4 matrix given the intial start and
	 * end positions. This uses the class-level double temp matrices for input.
	 */
	private void submatrixd(int i, int j)
	{
		// loop through 3x3 submatrix
		for(int di = 0; di < 3; di++)
		{
			for(int dj = 0; dj < 3; dj++)
			{
				// map 3x3 element (destination) to 4x4 element (source)
				int si = di + ((di >= i) ? 1 : 0);
				int sj = dj + ((dj >= j) ? 1 : 0);
				
				// copy element
				tempMat3d[di * 3 + dj] = tempMat4d[si * 4 + sj];
			}
		}
	}
	
	/**
	 * Calculate the determinant of the 3x3 double temp matrix.
	 *
	 * @return the determinant value
	 */
	private double determinant3x3()
	{
		return tempMat3d[0] * (tempMat3d[4] * tempMat3d[8] - tempMat3d[7] * tempMat3d[5]) -
			   tempMat3d[1] * (tempMat3d[3] * tempMat3d[8] - tempMat3d[6] * tempMat3d[5]) +
			   tempMat3d[2] * (tempMat3d[3] * tempMat3d[7] - tempMat3d[6] * tempMat3d[4]);
	}
	
    /**
     * Perform a LookAt camera calculation and place it in the given matrix.
     * If using this for a viewing transformation, you should invert() the
     * matrix after the call.
     *
     * @param eye The eye location
     * @param evX The eye vector X component
     * @param evY The eye vector Y component
     * @param evZ The eye vector Z component
     * @param res The result to put the calculation into
     */
    private void lookAtFallback(Point3d eye,
                                double evX,
                                double evY,
                                double evZ,
                                Matrix4d res)
    {
         // cross product of the -Z axis and the direction vector (ev? params).
         // This gives the rotation axis to put into the matrix. Since we know
         // the original -Z axis is (0, 0, -1) then we can skip a lot of the
         // calculations to be done.
        double rot_x = evY;   //  0 * evZ - -1 * evY;
        double rot_y = -evX;  // -1 * evX - 0 * evZ;
        double rot_z = 0;     //  0 * evY - 0 * evX;

        // Angle is  cos(theta) = (A / |A|) . (B / |B|)
        // A is the -Z vector. B is ev? values that need to be normalised first
        double n = evX * evX + evY * evY + evZ * evZ;
        if(n != 0 && n != 1)
        {
            n = 1 / Math.sqrt(n);
            evX *= n;
            evY *= n;
            evZ *= n;
        }

        double dot = -evZ;   // 0 * evX + 0 * evY + -1 * evZ;
        double angle = Math.acos(dot);

        if(orientd == null)
            orientd = new AxisAngle4d();

        orientd.x = rot_x;
        orientd.y = rot_y;
        orientd.z = rot_z;
        orientd.angle = angle;

        res.set(orientd);
        res.m03 = eye.x;
        res.m13 = eye.y;
        res.m23 = eye.z;
    }
}
