/*****************************************************************************
 * TranslationManipulator.java
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

import javax.media.j3d.Transform3D;
import javax.vecmath.*;

/**
 * Class which applies a translation to an existing transformation in a local
 * coordinate system.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 13. Nov. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class TranslationManipulator implements Manipulator
{
    private final Vector3d  itsTempVector1 = new Vector3d( );
    private final Vector3d  itsTempVector2 = new Vector3d( );

    private final Matrix3d  itsTempMatrix1 = new Matrix3d( );

    public TranslationManipulator( )
    {
    }

    /**
     * Combines the current transformation with the translational
     * part of <code>deltaTransform</code> under consideration of a local
     * coordinate system.
     */
    public void calculateTransform
    (
        Transform3D result,
        Transform3D currentTransform,
        Transform3D deltaTransform,
        Transform3D localToVWorldTransform
    )
    {
        localToVWorldTransform.getRotationScale( itsTempMatrix1 );

        deltaTransform.get( itsTempVector1 );
        itsTempMatrix1.transform( itsTempVector1 );

        currentTransform.get( itsTempVector2 );
        itsTempVector2.add( itsTempVector1 );
        result.setTranslation( itsTempVector2 );
    }
} 