/*****************************************************************************
 * ComponentInputDevice.java
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

import java.awt.Component;

/**
 * Interface to add/remove an <code>InputDevice</code>, which receives events
 * from a <code>Component</code>, to/from a <code>Component</code>.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 18. Dec. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public interface ComponentInputDevice
{
    /**
     * Add the <code>InputDevice</code> InputDevice to the
     * <code>Component</code>.
     */
    public void attachToComponent( Component component );

    /**
     * Remove the <code>InputDevice</code> InputDevice from the
     * <code>Component</code>.
     */
    public void removeFromComponent( Component component );
}