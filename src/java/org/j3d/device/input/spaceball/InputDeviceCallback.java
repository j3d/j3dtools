/*****************************************************************************
 * InputDeviceCallback.java
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

package org.j3d.device.input.spaceball;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

/**
 * Interface.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 25. Oct. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public interface InputDeviceCallback
{
    /**
     * Called if a new transformation was calculated from the input of an
     * input device. The implementation of the method is responsible to set
     * the new transformation to the <code>TransformGroup</code>.
     * @param tg <code>TransformGroup</code> to be updated, still containing
     *      the last transformation.
     * @param newTransform Updated transformation to be applied to
     *      <code>tg</code>.
     * @param deltaTransform Delta transformation between the new transformation
     *      and the old transformation in <code>tg</code>.
     * @param buttonValues Last button values read from the input device's
     *      sensor.Size of array is the number of available buttons of the
     *      input device.
     */
    public void update
    (
        InputDeviceBehavior behavior,
        TransformGroup      tg,
        Transform3D         newTransform,
        Transform3D         deltaTransform,
        int[ ]              buttonValues
    );
}