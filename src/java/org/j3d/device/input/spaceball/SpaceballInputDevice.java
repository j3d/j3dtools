/*****************************************************************************
 * SpaceballInputDevice.java
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

import javax.media.j3d.*;

import org.j3d.device.input.spaceball.driver.SpaceballDriver;
import org.j3d.device.input.spaceball.driver.SpaceballTypeDetector;

/**
 * Class which implements Java3D's InputDevice interface for Labtec's
 * Spaceball device. <p>
 * Spaceball, Spaceball 2003, Spaceball 3003 and Spaceball 4000 are Trademarks
 *    of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 19. Oct. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 */
public class SpaceballInputDevice extends InputDeviceBase
{
    private SpaceballDriver itsDriver = null;

    private final int[ ]    itsCurrentPositionValues =
            new int[ ]{ 0, 0, 0, 0, 0, 0 };
    private int[ ]          itsCurrentButtonValues = null;
    private String          itsPortDescriptor = null;

    /**
     * Creates a Spaceball4000InputDevice object. Set the default scale
     * ( = 0.001 ) for all input values.
     * @param portDescriptor Must be a descriptor of a serial port.
     *      For Windows 95 and Windows 98 the Java communications API will
     *      always enumerate the serial ports COMM 1 through COMM 4 and the
     *      parallel ports LPT1 and LPT2. For Windows NT the Java communications
     *      API will enumerate the serial ports entered in the Registry and the
     *      parallel port LPT1 and LPT2.
     *      For Solaris the Java communications API enumerates both the actual
     *      port names and the aliases to the ports.
     */
    public SpaceballInputDevice( final String portDescriptor )
    {
        super( InputDevice.NON_BLOCKING );
        setScale
        (
            new double[ ]{ 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001 }
        );
        itsPortDescriptor = portDescriptor;
    }

    /**
     * initialises the device and sets the sensor.
     */
    public boolean initialize( )
    {
        try
        {
            itsDriver = SpaceballTypeDetector.
                createSpaceballDriver( itsPortDescriptor, 4000 );
            final int numOfButtons = itsDriver.getNumOfButtons( );
            setSensor
            (
                new Sensor
                (
                    this,
                    Sensor.DEFAULT_SENSOR_READ_COUNT,
                    numOfButtons
                )
            );
            itsDriver.enableDevice( 100 );
            itsCurrentButtonValues = new int[ numOfButtons ];
            for( int i = 0; i < numOfButtons; i ++ )
            {
                itsCurrentButtonValues[ i ] = 0;
            }
            if( super.initialize( ) )
            {
                return true;
            }
            else
            {
                close( );
                return false;
            }
        }
        catch( Exception e )
        {
            e.printStackTrace( );
            close( );
            return false;
        }
    }

    /**
     * Overriding methods must call this method.
     */
    public void close( )
    {
        try
        {
            if( itsDriver != null )
            {
                itsDriver.emitPatternedBeep( new char[ ]{ 'F','f','F','f' } );
                itsDriver.disableDevice( 100 );
                try
                {
                    Thread.sleep( 1000 );
                }
                catch( InterruptedException e )
                {

                }
                itsDriver.close( );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace( );
        }
        super.close( );
    }

    public void pollAndProcessInput( )
    {
        try
        {
            itsDriver.read();
            itsDriver.getLastButtonValues( itsCurrentButtonValues );
            itsDriver.getLastPositionValues( itsCurrentPositionValues );
            setDeltaDeviceInput
            (
                itsCurrentPositionValues,
                itsCurrentButtonValues
            );
        }
        catch( Exception e )
        {
            e.printStackTrace( );
        }
    }
}