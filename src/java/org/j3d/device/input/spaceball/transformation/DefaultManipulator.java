/*****************************************************************************
 * DefaultManipulator.java
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
 * Class.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 15. Jun. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class DefaultManipulator implements Manipulator
{
    private final Point3d   itsTempPoint = new Point3d( );
    private final Vector3d  itsTempVector1 = new Vector3d( );
    private final Vector3d  itsTempVector2 = new Vector3d( );

    private final Matrix3d  itsTempMatrix1 = new Matrix3d( );
    private final Matrix3d  itsTempMatrix2 = new Matrix3d( );

    public DefaultManipulator( )
    {
        super( );
    }

    /**
     * @param result new absolute transformation in virtual world coordinate
     *      system.
     * @param currentTransform current absolute transformation
     * @param deltaTransform the <code>currentTransform</code> is concatenated
     *      to this transformation
     * @param localToVWorldTransform Set to the identical transformation if
     *      <code>deltaTransform</code> shall take place in the virtual world
     *      coordinate system.
     */
    public void calculateTransform
    (
        final Transform3D   result,
        final Transform3D   currentTransform,
        final Transform3D   deltaTransform,
        final Transform3D   localToVWorldTransform
    )
    {
        localToVWorldTransform.getRotationScale( itsTempMatrix1 );
        itsTempMatrix2.transpose( itsTempMatrix1 );

        itsTempMatrix2.
            transform( TransformationUtils.itsInitialUpVector, itsTempVector1 );
        itsTempMatrix2.transform
        (
            TransformationUtils.itsInitialViewDirection,
            itsTempPoint
        );

        deltaTransform.getRotationScale( itsTempMatrix2 );
        itsTempMatrix2.transpose( );

        itsTempMatrix2.transform( itsTempPoint );
        itsTempMatrix2.transform( itsTempVector1 );

        itsTempMatrix1.transform( itsTempPoint );
        itsTempMatrix1.transform( itsTempVector1 );

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

        deltaTransform.get( itsTempVector1 );
        itsTempMatrix1.transform( itsTempVector1 );

        currentTransform.get( itsTempVector2 );
        itsTempVector2.add( itsTempVector1 );
        result.setTranslation( itsTempVector2 );
    }
}