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
 * Copyright (c) 2001 Dipl. Ing. P. Szawlowski
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
 * {@link MouseInputDevice#pollAndProcessInput}.<p>
 * Calculation of a transformation is triggered by keeping the left mouse button
 * pressed while dragging the mouse
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences,
 * @version 15. Jun. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class MouseInputDevice extends OnDemandInputDevice
implements MouseListener, MouseMotionListener
{
    private final int[ ]    itsCurrentValues = new int[ ]{ 0, 0, 0, 0, 0, 0 };
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

    public void mouseClicked( MouseEvent evt )
    {

    }

    public void mouseEntered( MouseEvent evt )
    {

    }

    public void mouseExited( MouseEvent evt )
    {

    }

    public void mousePressed( MouseEvent evt )
    {
        if( evt.getModifiers( ) == InputEvent.BUTTON1_MASK )
        {
            setMouseStartValues( evt.getX( ), evt.getY( ) );
            setMouseValues( evt.getX( ), evt.getY( ) );
            setButtonValue( 0, 1 );
        }
        if( evt.getModifiers( ) == InputEvent.BUTTON2_MASK )
        {
            setButtonValue( 1, 1 );
        }
        if( evt.getModifiers( ) == InputEvent.BUTTON3_MASK )
        {
            setButtonValue( 2, 1 );
        }
        postStimulus( );
    }

    public void mouseReleased( MouseEvent evt )
    {
        if( evt.getModifiers( ) == InputEvent.BUTTON1_MASK )
        {
            setButtonValue( 0, 0 );
        }
        if( evt.getModifiers( ) == InputEvent.BUTTON2_MASK )
        {
            setButtonValue( 1, 0 );
        }
        if( evt.getModifiers( ) == InputEvent.BUTTON3_MASK )
        {
            setButtonValue( 2, 0 );
        }
        postStimulus( );
    }

    public void mouseDragged( MouseEvent evt )
    {
        if( evt.getModifiers( ) == InputEvent.BUTTON1_MASK )
        {
            setMouseValues( evt.getX( ), evt.getY( ) );
        }
        postStimulus( );
    }

    public void mouseMoved( MouseEvent evt )
    {

    }

    /**
     * Sets the sensor for the input device and initialises the input values.
     */
    public boolean  initialize( )
    {
        setSensor( new Sensor( this, Sensor.DEFAULT_SENSOR_READ_COUNT, 3 ) );
        setMouseStartValues( 0, 0 );
        setMouseValues( 0, 0 );
        return super.initialize();
    }

    public void pollAndProcessInput( )
    {
        getDeltaValues( itsDeltaValues );
        getButtonValues( itsTempButtonValues );
        setDeltaDeviceInput( itsDeltaValues, itsTempButtonValues );
    }

    /**
     * Subtracts the 6 start values from the 6 current values and sets the new
     * start values.
     */
    private synchronized void getDeltaValues( final int[ ] deltaValues )
    {
        deltaValues[ 0 ] = itsCurrentValues[ 0 ] - itsStartValues[ 0 ];
        deltaValues[ 1 ] = itsStartValues[ 1 ] - itsCurrentValues[ 1 ];
        deltaValues[ 2 ] = itsCurrentValues[ 2 ] - itsStartValues[ 2 ];
        deltaValues[ 3 ] = itsCurrentValues[ 3 ] - itsStartValues[ 3 ];
        deltaValues[ 4 ] = itsCurrentValues[ 4 ] - itsStartValues[ 4 ];
        deltaValues[ 5 ] = itsCurrentValues[ 5 ] - itsStartValues[ 5 ];
        System.arraycopy( itsCurrentValues, 0, itsStartValues, 0, 6 );
    }

    /**
     * @param index indices 0 and 1 are used for mouse input
     */
    protected synchronized void setStartValue
    (
        final int   index,
        final int   value
    )
    {
        itsStartValues[ index ] = value;
    }

    private synchronized void setMouseStartValues( final int x, final int y )
    {
        itsStartValues[ 0 ] = x;
        itsStartValues[ 1 ] = y;
    }

    private synchronized void setMouseValues( final int x, final int y )
    {
        itsCurrentValues[ 0 ] = x;
        itsCurrentValues[ 1 ] = y;
    }

    /**
     * @param index indices 0 and 1 are used for mouse input
     */
    protected synchronized void setInputValue
    (
        final int   index,
        final int   value
    )
    {
        itsCurrentValues[ index ] = value;
    }

    protected synchronized void setButtonValue
    (
        final int   buttonIndex,
        final int   value
    )
    {
      itsButtonValues[ buttonIndex ] = value;
    }

    protected synchronized void getButtonValues( final int[ ] result )
    {
        System.arraycopy( itsButtonValues, 0, result, 0, 3 );
    }
}