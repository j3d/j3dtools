/*****************************************************************************
 * OrbitManipulator.java
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

import javax.media.j3d.Transform3D;
import javax.vecmath.*;

/**
 * Calculates a transformation which mimics orbiting around a fixed point.
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences,
 * @version 25. Oct. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class OrbitManipulator implements Manipulator
{
    private Vector3d        itsOrbitCenter;

    private final Point3d   itsTempPoint = new Point3d( );
    private final Vector3d  itsTempVector1 = new Vector3d( );
    private final Vector3d  itsTempVector2 = new Vector3d( );

    private final Matrix3d  itsTempMatrix1 = new Matrix3d( );
    private final Matrix3d  itsTempMatrix2 = new Matrix3d( );

    /**
     * Creates an OrbitManipulator object. The default orbit center is at
     * (0,0,0).
     */
    public OrbitManipulator( )
    {
        this( new Vector3d( ) );
    }

    /**
     * Creates an OrbitManipulator object.
     */
    public OrbitManipulator( final Tuple3d orbitCenter )
    {
        super( );
        setOrbitCenter( orbitCenter );
    }

    /**
     * Calculates a transformation which mimics orbiting around a fixed point.
     * @param result new absolute transformation in virtual world coordinate
     *      system.
     * @param currentTransform current absolute transformation
     * @param deltaTransform uses the rotational part to calculate the new
     *      position and rotation. Uses the z coordinate of the translation
     *      vector to calculate the new radius.
     * @param localToVWorldTransform Set to the identical transformation if
     *      <code>deltaTransform</code> shall be done in the virtual world
     *      coordinate system.
     */
    public void calculateTransform
    (
        final Transform3D result,
        final Transform3D currentTransform,
        final Transform3D deltaTransform,
        final Transform3D localToVWorldTransform
    )
    {
        final Vector3d orbitCenter;
        synchronized( this )
        {
            orbitCenter = itsOrbitCenter;
        }
        currentTransform.get( itsTempVector2 );
        deltaTransform.get( itsTempVector1 );
        itsTempVector2.sub( orbitCenter );
        final double radius = itsTempVector2.length( );
        if( radius > 0.000001 )
        {
            itsTempVector2.
                scaleAdd( itsTempVector1.z / radius , itsTempVector2 );
        }
        else
        {
            itsTempVector2.x = itsTempVector1.z;
        }

        localToVWorldTransform.getRotationScale( itsTempMatrix1 );
        itsTempMatrix2.transpose( itsTempMatrix1 );

        itsTempMatrix2.
            transform( TransformationUtils.itsInitialUpVector, itsTempVector1 );
        itsTempMatrix2.transform
        (
            TransformationUtils.itsInitialViewDirection,
            itsTempPoint
        );
        itsTempMatrix2.transform( itsTempVector2 );

        deltaTransform.getRotationScale( itsTempMatrix2 );
        itsTempMatrix2.transform( itsTempVector2 );
        itsTempMatrix2.transpose( );

        itsTempMatrix2.transform( itsTempPoint );
        itsTempMatrix2.transform( itsTempVector1 );

        itsTempMatrix1.transform( itsTempPoint );
        itsTempMatrix1.transform( itsTempVector1 );
        itsTempMatrix1.transform( itsTempVector2 );

        currentTransform.getRotationScale( itsTempMatrix2 );
        itsTempMatrix2.transpose( );

        itsTempMatrix2.transform( itsTempPoint );
        itsTempMatrix2.transform( itsTempVector1 );

        result.lookAt
        (
            TransformationUtils.itsOrigin,
            itsTempPoint,
            itsTempVector1
        );

        itsTempVector2.add( orbitCenter );
        result.setTranslation( itsTempVector2 );
   }

    public synchronized void setOrbitCenter( final Tuple3d point )
    {
        itsOrbitCenter = new Vector3d( point );
    }
}