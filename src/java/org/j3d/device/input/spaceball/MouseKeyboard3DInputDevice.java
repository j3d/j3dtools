/*****************************************************************************
 * MouseKeyboard3DInputDevice.java
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

/**
 * Implementation of the InputDevice interface for a mouse device with up to
 * 3 buttons. Additionally the +, - keys are used for changing a third input
 * value. It supports up to 3 additional input values. The mouse input
 * values use the indexes 0 and 1 of the input values array and the additional
 * key input values uses the index 2.<p>
 * Calculation of a transformation is triggered by keeping the left mouse button
 * pressed while dragging the mouse or by pressing the +, - buttons while the
 * attached Component object has focus.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 15. Jun. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class MouseKeyboard3DInputDevice extends MouseInputDevice
implements MouseListener, MouseMotionListener, KeyListener
{
    private int itsCurrentValue = 0;

    /**
     * Constructs an MouseInputDevice object with default settings. Sets the
     * default scale (0.001/0.001/0.01/1.0/1.0/1.0) for all input values.
     */
    public MouseKeyboard3DInputDevice( )
    {
        super( );
        setScale( 2, 0.01 );
    }

    public void attachToComponent( final Component component )
    {
        component.addKeyListener( this );
        super.attachToComponent( component );
    }

    public void removeFromComponent( final Component component )
    {
        component.removeKeyListener( this );
        super.removeFromComponent( component );
    }

    public void keyTyped( KeyEvent evt )
    {
        if( evt.getKeyChar() == '+' )
        {
            itsCurrentValue ++;
            setInputValue( 2, itsCurrentValue );
            postStimulus( );
        }
        else if( evt.getKeyChar() == '-' )
        {
            itsCurrentValue --;
            setInputValue( 2, itsCurrentValue );
            postStimulus( );
        }
    }

    public void keyPressed( KeyEvent evt )
    {

    }

    public void keyReleased( KeyEvent evt )
    {

    }

    /**
     * Sets the sensor for the input device and initialises the input values.
     */
    public boolean initialize( )
    {
        itsCurrentValue = 0;
        setStartValue( 2, 0 );
        return super.initialize();
    }
}

