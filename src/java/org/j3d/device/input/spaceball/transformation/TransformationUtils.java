/*****************************************************************************
 * TransformationUtils.java
 * Java Source
 *
 * This source is licensed under the GNU LGPL v2.1.
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 *
 * Copyright (c) 2001 Dipl. Ing. P. Szawlowski
 * University of Vienna, Dept. of Medical Computer Sciences
 ****************************************************************************/

package org.j3d.device.input.spaceball.transformation;

import javax.vecmath.*;
import javax.media.j3d.Transform3D;

 /**
  * Transformation utility class.<p>
  * @author  Dipl. Ing. Paul Szawlowski -
  *          University of Vienna, Dept of Medical Computer Sciences
  * @version 23. Oct. 2001
  * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
  */
public final class TransformationUtils
{
    public final static Point3d   itsOrigin = new Point3d( );
    public final static Vector3d  itsInitialUpVector =
            new Vector3d( 0.0, 1.0, 0.0 );
    public final static Vector3d  itsInitialViewDirection =
            new Vector3d( 0.0, 0.0, -1.0 );

    public TransformationUtils( )
    {

    }

    /**
     * @param result the calculated normalized cartesian coordinates will be
     *      copied into this object
     * @param phi [rad] polar angle in xy-plane counted from positive x axis to
     *      positive y axis; -pi < phi <= pi
     * @param theta [rad] second polar angle counted from positive z axis;
     *      0 <= theta <= pi
     */
    public static void polarToCartesian
    (
        final Tuple3d   result,
        final double    phi,
        final double    theta
    )
    {
        final double sinTheta = Math.sin( theta );
        result.set
        (
            sinTheta * Math.cos( phi ),
            sinTheta * Math.sin( phi ),
            Math.cos( theta )
        );
    }

    /**
     * @param result the calculated cartesian coordinates will be copied into
     *      this object
     * @param radius length of vector
     * @param phi [rad] polar angle in xy-plane counted from positive x axis to
     *      positive y axis; -pi < phi <= pi
     * @param theta [rad] second polar angle counted from positive z axis;
     *      0 <= theta <= pi
     */
    public static void polarToCartesian
    (
        final Tuple3d   result,
        final double    radius,
        final double    phi,
        final double    theta
    )
    {
        final double sinTheta = radius * Math.sin( theta );
        result . set
        (
            sinTheta * Math.cos( phi ),
            sinTheta * Math.sin( phi ),
            radius * Math.cos( theta )
        );
   }

    /**
     * @param result the calculated polar coordinates will be copied into this
     *      object:
     *      index 0: radius,
     *      index 1: sin phi; phi = polar angle in xy-plane counted from
     *          positive x axis to positive y axis; -pi < phi <= pi,
     *      index 2: cos phi,
     *      index 3: sin theta; theta = second polar angle counted from positive
     *          z axis; 0 <= theta <= pi,
     *      index 4: cos theta;
     */
    public static void cartesianToPolar
    (
        final double[ ] result,
        final Tuple3d   coordinates
    )
    {
        final double x = coordinates.x;
        final double y = coordinates.y;
        final double z = coordinates.z;
        final double x_y_sqr = x * x + y * y;
        final double x_y = Math.sqrt( x_y_sqr );
        final double x_y_z = Math.sqrt( x_y_sqr + z * z );
        result[ 0 ] = x_y_z;
        result[ 3 ] = x_y / x_y_z;
        result[ 4 ] = z / x_y_z;
        result[ 1 ] = y / x_y;
        result[ 2 ] = x / x_y;
    }

    /**
     * Calculates the <code>Transform3D</code> for a combined rotation and
     * translation.
     * @param input difference input values. Size of array = 6. Uses the first 3
     *      input values to translate along the x-axis, y-axis and
     *      z-axis respectively and uses the second 3 input values to rotate
     *      about the y-axis, x-axis and z-axis respectively ( clockwise ).
     *      The 4th and 5th input values (rotation about y- and x-axis) are
     *      used as polar angles to define a direction vector for the rotations.
     *      The reference vector for the direction is (0,0,1).<p>
     *      polar angles: first angle between vector and xz plane, second angle
     *          angle in xz plane between vector and positive z-axis<p>
     *      z-axis: angle around local positive z axis (measured from the
     *          rotated positive y-axis counterclockwise )
     * @param deltaTransformation The difference transformation will be stored
     *      in this object.
     * @param position The difference translation vector will be stored in this
     *      object.
     * @param direction The difference rotation vector will be stored in this
     *      object. This vector defines the rotation around the x- and y-axis.
     *      The rotation angles are the angles between this vector and (0,0,-1).
     * @param upVector The difference up vector will be stored in this object.
     *      This vector defines the rotation around the z axis. The rotation
     *      angle is the angle between this vector and the normal of the
     *      direction vector.
     * @param tempMatrix only used for temporary data storage
     */
    public static void getRotationTranslationTransform3D
    (
        final double[ ]     input,
        final Transform3D   deltaTransformation,
        final Vector3d      position,
        final Point3d       direction,
        final Vector3d      upVector
    )
    {
        final double sinXrot = Math.sin( input[ 3 ] );
        final double sinYrot = Math.sin( input[ 4 ] );
        final double sinZrot = Math.sin( input[ 5 ] );
        final double cosXrot = Math.cos( input[ 3 ] );
        final double cosYrot = Math.cos( input[ 4 ] );
        final double cosZrot = Math.cos( input[ 5 ] );

        position.set( input[ 0 ], input[ 1 ], input[ 2 ] );
        direction.set( sinYrot * cosXrot, sinXrot, - cosYrot * cosXrot );
        upVector.set
        (
            - sinYrot * sinXrot  * cosZrot + cosXrot * sinZrot,
            - sinYrot * sinXrot * sinZrot + cosXrot * cosZrot,
            cosYrot * sinXrot
        );

        deltaTransformation.lookAt( itsOrigin, direction, upVector );
        deltaTransformation.setTranslation( position );
    }

    /**
     * Calculates a rotation matrix. The calculated rotation transforms the
     * vector (0,0,1) ( polar coordinates: radius = 1,
     * <code>xRotAngle</code> = 0, <code>yRotAngle</code> = 0 ) into the vector
     * defined with the polar angles <code>xRotAngle</code> and
     * <code>yRotAngle</code>. The 3rd rotation angle defines the rotation
     * around the defined vector (= local z-axis).
     * @param result the resulting transformation is stored into this object
     * @param rotationVector the vector defined with <code>xRotAngle</code> and
     *      <code>yRotAngle</code> is stored into this object
     * @param upVector local z axis is stored into this object
     * @param xRotAngle polar angle: angle between vector and xz plane
     * @param yRotAngle polar angle: angle in xz plane between vector and
     *      positive z-axis
     * @param zRotAngle rotation angle around local positive z axis (measured
     *      from the rotated positive y-axis counterclockwise )
     */
    public static void getRotationTransform3D
    (
        final Transform3D result,
        final Point3d     rotationVector,
        final Vector3d    upVector,
        final double      xRotAngle,
        final double      yRotAngle,
        final double      zRotAngle
    )
    {
        final double sinXrot = Math.sin( xRotAngle );
        final double sinYrot = Math.sin( yRotAngle );
        final double sinZrot = Math.sin( zRotAngle );
        final double cosXrot = Math.cos( xRotAngle );
        final double cosYrot = Math.cos( yRotAngle );
        final double cosZrot = Math.cos( zRotAngle );

        rotationVector.set( sinYrot * cosXrot, sinXrot, - cosYrot * cosXrot );
        upVector.set
        (
            - sinYrot * sinXrot  * cosZrot + cosXrot * sinZrot,
            - sinYrot * sinXrot * sinZrot + cosXrot * cosZrot,
            cosYrot * sinXrot
        );

        result.lookAt( itsOrigin, rotationVector, upVector );
    }

    /**
     * @param result it is assumed it contains no rotation, existing translation
     *      will be preserved
     * @param tempMatrix: it is assumed that this is an identity matrix or all
     *      elements are 0 except the diagonal elements which can have any value
     */
    public static void getScaleTransform3D
    (
        final Transform3D   result,
        final Matrix3d      tempMatrix,
        final double        xScale,
        final double        yScale,
        final double        zScale
    )
    {
        tempMatrix.m00 = xScale;
        tempMatrix.m11 = yScale;
        tempMatrix.m22 = zScale;
        result.setRotationScale( tempMatrix );
    }

    /**
     * @param result it is assumed that it contains no scaling, existing
     *    translations will be preserved
     * @param tempMatrix it is assumed that this is an identity matrix or a
     *    matrix which diagonal elements are 1 and all other elements
     *    are 0 except m11, m12, m21, m22 which can have any value
     * @param sinAngle sine of counter clockwiese rotation angle around x-axis
     * @param cosAngle cosine of counter clockwiese rotation angle around x-axis
    */
    public static void getXRotationTransform3D
    (
        final Transform3D   result,
        final Matrix3d      tempMatrix,
        final double        sinAngle,
        final double        cosAngle
    )
    {
        tempMatrix.m11 = cosAngle;
        tempMatrix.m21 = - sinAngle;
        tempMatrix.m12 = sinAngle;
        tempMatrix.m22 = cosAngle;
        result.setRotationScale( tempMatrix );
    }

    /**
     * @param result it is assumed that it contains no scaling, existing
     *      translations will be preserved
     * @param tempMatrix it is assumed that this is an identity matrix or a
     *      matrix which diagonal elements are 1 and all other elements
     *      are 0 except m00, m20, m02, m22 which can have any value
     * @param sinAngle sine of counter clockwiese rotation angle around y-axis
     * @param cosAngle cosine of counter clockwiese rotation angle around y-axis
     */
    public static void getYRotationTransform3D
    (
        final Transform3D   result,
        final Matrix3d      tempMatrix,
        final double        sinAngle,
        final double        cosAngle
    )
    {
        tempMatrix.m00 = cosAngle;
        tempMatrix.m20 = - sinAngle;
        tempMatrix.m02 = sinAngle;
        tempMatrix.m22 = cosAngle;
        result.setRotationScale( tempMatrix );
    }

    /**
     * @param result it is assumed that it contains no scaling, existing
     *    translations will be preserved
     * @param tempMatrix it is assumed that this is an identity matrix or a
     *    matrix which diagonal elements are 1 and all other elements
     *    are 0 except m00, m01, m10, m11 which can have any value
     * @param sinAngle sine of counter clockwiese rotation angle around z-axis
     * @param cosAngle cosine of counter clockwiese rotation angle around z-axis
     */
    public static void getZRotationTransform3D
    (
        final Transform3D   result,
        final Matrix3d      tempMatrix,
        final double        sinAngle,
        final double        cosAngle
    )
    {
        tempMatrix.m00 = cosAngle;
        tempMatrix.m10 = - sinAngle;
        tempMatrix.m01 = sinAngle;
        tempMatrix.m11 = cosAngle;
        result.setRotationScale( tempMatrix );
    }
}