/*****************************************************************************
 * MouseInputDevice.java
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
 * Copyright (c) 2001, 2002 Dipl. Ing. P. Szawlowski
 * University of Vienna, Dept. of Medical Computer Sciences
 ****************************************************************************/

package org.j3d.device.input.spaceball;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.media.j3d.Sensor;

import org.j3d.device.input.spaceball.transformation.Manipulator;

/**
 * Implementation of the InputDevice interface for a mouse device with up to
 * 3 buttons. It supports up to 4 additional input values.  The mouse input
 * values use the indexes 0 and 1 of the input values array. These values are
 * difference values since the last call of
 * {@link MOuseInputDevice#pollAndProcessInput}.<p>
 * Add the object to a Component with the {@link #attachToComponent}
 * method in order to receive input from the mouse.<p>
 * Calculation of a transformation is triggered by keeping the left mouse button
 * pressed while dragging the mouse.
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences,
 * @version 5. May. 2002
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class MouseInputDevice extends OnDemandInputDevice
implements MouseListener, MouseMotionListener, ComponentInputDevice
{
    /**
     * current values of the device, index 0 and 1 are reserved for mouse input
     */
    protected final int[ ]  itsCurrentValues = new int[ ]{ 0, 0, 0, 0, 0, 0 };
    private final int[ ]    itsStartValues = new int[ ]{ 0, 0, 0, 0, 0, 0 };

    private final int[ ]    itsDeltaValues = new int[ ]{ 0, 0, 0, 0, 0, 0 };

    private final int[ ]    itsButtonValues = new int[ ]{ 0, 0, 0 };
    private final int[ ]    itsTempButtonValues = new int[ ]{ 0, 0, 0 };

    /**
     * Constructs an MouseInputDevice object with default settings. Sets the
     * default scale ( 0.001/0.001/1.0/1.0/1.0/1.0 ) for all input values.
     */
    public MouseInputDevice( )
    {
        super( );
        setScale( 0, 0.005 );
        setScale( 1, 0.005 );
    }

    public void attachToComponent( final Component component )
    {
        component.addMouseListener( this );
        component.addMouseMotionListener( this );
    }

    public void removeFromComponent( final Component component )
    {
        component.removeMouseListener( this );
        component.removeMouseMotionListener( this );
    }

    /**
     * implementation does nothing.
     */
    public void mouseClicked( MouseEvent evt )
    {

    }

    /**
     * implementation does nothing.
     */
    public void mouseEntered( MouseEvent evt )
    {

    }

    /**
     * implementation does nothing.
     */
    public void mouseExited( MouseEvent evt )
    {

    }

    /**
     * Checks which mouse button is pressed and activates the associated
     * <code>Behavior</code>.
     */
    public void mousePressed( MouseEvent evt )
    {
        synchronized( this )
        {
            if( evt.getModifiers( ) == InputEvent.BUTTON1_MASK )
            {
                itsStartValues[ 0 ] = evt.getX( );
                itsStartValues[ 1 ] = evt.getY( );
                itsCurrentValues[ 0 ] = evt.getX( );
                itsCurrentValues[ 1 ] = evt.getY( );
                itsButtonValues[ 0 ] = 1;
            }
            if( evt.getModifiers( ) == InputEvent.BUTTON2_MASK )
            {
                itsButtonValues[ 1 ] = 1;
            }
            if( evt.getModifiers( ) == InputEvent.BUTTON3_MASK )
            {
                itsButtonValues[ 2 ] = 1;
            }
        }
        postStimulus( );
    }

    /**
     * Checks which mouse button is released and activates the associated
     * <code>Behavior</code>.
     */
    public void mouseReleased( MouseEvent evt )
    {
        synchronized( this )
        {
            if( evt.getModifiers( ) == InputEvent.BUTTON1_MASK )
            {
                itsButtonValues[ 0 ] = 0;
            }
            if( evt.getModifiers( ) == InputEvent.BUTTON2_MASK )
            {
                itsButtonValues[ 1 ] = 0;
            }
            if( evt.getModifiers( ) == InputEvent.BUTTON3_MASK )
            {
                itsButtonValues[ 2 ] = 0;
            }
        }
        postStimulus( );
    }

    /**
     * Checks if left mouse button is pressed, calculates the new values for
     * translation along the x and y axes and activates the associated
     * <code>Behavior</code>.
     */
    public void mouseDragged( MouseEvent evt )
    {
        if( evt.getModifiers( ) == InputEvent.BUTTON1_MASK )
        {
            synchronized( this )
            {
                itsCurrentValues[ 0 ] = evt.getX( );
                itsCurrentValues[ 1 ] = evt.getY( );
            }
            postStimulus( );
        }
    }

    /**
     * Implementation does nothing.
     */
    public void mouseMoved( MouseEvent evt )
    {

    }

    /**
     * Sets the sensor for the input device and initializes the start values.
     */
    public boolean  initialize( )
    {
        setSensor( new Sensor( this, Sensor.DEFAULT_SENSOR_READ_COUNT, 3 ) );
        synchronized( this )
        {
            for( int i = 0; i < 6; i ++ )
            {
                itsStartValues[ i ] = 0;
                itsCurrentValues[ i ] = 0;
            }
            itsTempButtonValues[ 0 ] = 0;
            itsTempButtonValues[ 1 ] = 0;
            itsTempButtonValues[ 2 ] = 0;
        }
        return super.initialize();
    }

    public void pollAndProcessInput( )
    {
        synchronized( this )
        {
            for( int i = 0; i < 6; i ++ )
            {
                itsDeltaValues[ i ] = itsCurrentValues[ i ]
                    - itsStartValues[ i ];
                itsStartValues[ i ] = itsCurrentValues[ i ];
            }
            itsTempButtonValues[ 0 ] = itsButtonValues[ 0 ];
            itsTempButtonValues[ 1 ] = itsButtonValues[ 1 ];
            itsTempButtonValues[ 2 ] = itsButtonValues[ 2 ];
        }
        itsDeltaValues[ 1 ] = - itsDeltaValues[ 1 ];
        setDeltaDeviceInput( itsDeltaValues, itsTempButtonValues );
    }
}