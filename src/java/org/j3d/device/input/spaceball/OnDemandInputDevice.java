/*****************************************************************************
 * OnDemandInputDevice.java
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

import javax.media.j3d.InputDevice;

/**
 * Base class for on demand input devices with one sensor. The associated
 * Behavior will be activated when the {@link #postStimulus}
 * method is called.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 15. Jun. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public abstract class OnDemandInputDevice extends InputDeviceBase
{
    /**
     * Constructs an OnDemandInputDevice object. Set the default scale ( = 1.0 )
     * for all input values.
     */
    public OnDemandInputDevice(  )
    {
        super( InputDevice.DEMAND_DRIVEN );
    }

    /**
     * Inherited classes shall override this method and call this method, if
     * their initialization was OK.
     * Does not set the sensor !
     */
    public boolean initialize( )
    {
        return super.initialize();
    }

    /**
     * call this method when overriding
     */
    public void close( )
    {
        super.close( );
    }

    /**
     * call this method from inherited classes to activate the associated
     * <code>Behavior</code>, which reads the sensor data and applies it to the
     * scene.
     */
    protected void postStimulus( )
    {
        if( itsBehavior != null )
        {
            itsBehavior.postId( getProcessingMode( ) );
        }
    }
}