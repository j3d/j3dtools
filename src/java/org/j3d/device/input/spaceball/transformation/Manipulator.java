/*****************************************************************************
 * Manipulator.java
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

/**
 * Interface for combining a relative transformation change with and an absoulte
 * absolute transformation in the local coordinate system.<p>
 * coordinate system.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 15. Jun. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public interface Manipulator
{
    /**
     * Combines an absolute transformation with a relative transformation change
     * in the local coordinate system.
     * @param result new absolute transformation in virtual world coordinate
     *      system.
     * @param currentTransform absolute transformation.
     * @param deltaTransform relative transformation change
     * @param localToVWorldTransform Transformation from the coordinate system
     *      in which the transformation shall be done to the virtual coordinate
     *      system. Set to identity transformation if the transformation shall
     *      be done in the virtual world coordinate system.
     */
    public void calculateTransform
    (
        final Transform3D result,
        final Transform3D currentTransform,
        final Transform3D deltaTransform,
        final Transform3D localToVWorldTransform
    );
}