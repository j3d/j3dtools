/*****************************************************************************
 * Keyboard6DInputDevice.java
 * Java Source
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
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.media.j3d.Sensor;

/**
 * Implementation of the InputDevice interface to provide 6D input via keyboard
 * keys. Add the object to a Component with the {@link #attachToComponent}
 * method in order to receive input from the keyboard.<p>
 * Default key settings: 'd'/'s' are used for +/- translations along the x axes,
 * 'e'/'x' are used for +/- translations along the y axes, 'c'/'f' are used for
 * +/- translations along the z axes. 'm'/'i' are used for +/-
 * clockwise/counterclockwise rotations about the x axes, 'k'/'j' are used for
 * +/- clockwise/counterclockwise rotations about the y axes, 'u'/'o' are used
 * for  +/- clockwise/counterclockwise rotations about the z axes.<p>
 * Default button key settings: The numerical keys ('0',....'9') will trigger
 * buttons events. The button index equals the numerical value of the keys.
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 20. Dec. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class Keyboard6DInputDevice extends OnDemandInputDevice
implements KeyListener, ComponentInputDevice
{
    /**
     * first index: index in <code>itsValues</code> <p>
     * second index: increment (1/-1)
     */
    private static final int[ ][ ]  itsIncrements = new int[ ][ ]
            {
                { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { 2, 1 }, { 2, -1 },
                { 3, 1 }, { 3, -1 }, { 4, 1 }, { 4, -1 }, { 5, 1 }, { 5, -1 }
            };

    private final char[ ]   itsTransformationKeys;

    private final char[ ]   itsButtonKeys;

    private final int[ ]    itsValues = new int[ ]{ 0, 0, 0, 0, 0, 0 };
    private final int[ ]    itsDeltaValues = new int[ ]{ 0, 0, 0, 0, 0, 0 };
    private final int[ ]    itsButtonValues;
    private final int[ ]    itsTempButtonValues;

    /**
     * Constructs a Keyboard6DInputDevice object with default key settings. Sets
     * the default scale (0.01/0.01/0.01/0.01/0.01/0.01) for all input values.
     */
    public Keyboard6DInputDevice( )
    {
        this
        (
            new char[ ]
            {
                'd', 's', 'e', 'x', 'c', 'f',
                'i', 'm', 'k', 'j', 'u', 'o',
            },
            new char[ ]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }
        );
    }

    /**
     * Constructs a Keyboard6DInputDevice object with user defines key settings.
     * Sets the default scale (0.01/0.01/0.01/0.01/0.01/0.01) for all input
     * values.
     * @param transformationKeys Size of Array <= 12.
     *      <UL>
     *          <LI>index 0: key for + translations along the x axes
     *          <LI>index 1: key for - translations along the x axes
     *          <LI>index 2: key for + translations along the y axes
     *          <LI>index 3: key for - translations along the y axes
     *          <LI>index 4: key for + translations along the z axes
     *          <LI>index 5: key for - translations along the z axes
     *          <LI>index 6: key for clockwise rotations about the x axes
     *          <LI>index 7: key for counterclockwise rotations about the x axes
     *          <LI>index 8: key for clockwise rotations about the y axes
     *          <LI>index 9: key for counterclockwise rotations about the y axes
     *          <LI>index 10: key for clockwise rotations about the z axes
     *          <LI>index 11: key for counterclockwise rotations about the z
     *              axes
     *      </UL>
     * @param buttonKeys keys used as buttons
     */
    public Keyboard6DInputDevice
    (
        final char[ ]   transformationKeys,
        final char[ ]   buttonKeys
    )
    {
        super( );
        itsTransformationKeys = transformationKeys;
        itsButtonKeys = buttonKeys;
        itsButtonValues = new int[ buttonKeys.length ];
        itsTempButtonValues = new int[ buttonKeys.length ];
        setScale( new double[ ]{ 0.01, 0.01, 0.01, 0.01, 0.01, 0.01 } );
    }

    /**
     * Check if key typed is a transformation or button key. If yes then trigger
     * the app action.
     */
    public void keyTyped( KeyEvent evt )
    {
        final char key = evt.getKeyChar( );
        int index = -1;
        final int numOfKeys = itsTransformationKeys.length;
        for( int i = 0; i < numOfKeys; i ++ )
        {
            if( key == itsTransformationKeys[ i ] )
            {
                index = i;
            }
        }
        if( index >= 0 )
        {
            synchronized( this )
            {
                itsValues[ itsIncrements[ index ][ 0 ] ] +=
                    itsIncrements[ index ][ 1 ];
            }
            postStimulus( );
        }
        else
        {
            index = -1;
            final int length = itsButtonKeys.length;
            for( int i = 0; i < length; i ++ )
            {
                if( key == itsButtonKeys[ i ] )
                {
                    index = i;
                }
            }
            if( index >= 0 )
            {
                synchronized( this )
                {
                    itsButtonValues[ index ] = 1;
                }
                postStimulus( );
            }
        }
    }

    /**
     * implementation does nothing.
     */
    public void keyPressed( KeyEvent evt )
    {

    }

    /**
     * implementation does nothing.
     */
    public void keyReleased( KeyEvent evt )
    {

    }

    public boolean initialize( )
    {
        final int numOfButtons = itsButtonKeys.length;
        setSensor
        (
            new Sensor( this, Sensor.DEFAULT_SENSOR_READ_COUNT, numOfButtons )
        );
        synchronized( this )
        {
            for( int i = 0; i < 6; i ++ )
            {
                itsValues[ i ] = 0;
            }
            for( int i = 0; i < numOfButtons; i ++ )
            {
                itsButtonValues[ i ] = 0;
            }
        }
        return super.initialize( );
    }

    public void pollAndProcessInput( )
    {
        final int numOfButtons = itsButtonValues.length;
        synchronized( this )
        {
            for( int i = 0; i < 6; i ++ )
            {
                itsDeltaValues[ i ] = itsValues[ i ];
                itsValues[ i ] = 0;
            }
            for( int i = 0; i < numOfButtons; i ++ )
            {
                itsTempButtonValues[ i ] = itsButtonValues[ i ];
                itsButtonValues[ i ] = 0;
            }
        }
        setDeltaDeviceInput( itsDeltaValues, itsTempButtonValues );
    }

    public void attachToComponent( final Component component )
    {
        component.addKeyListener( this );
    }

    public void removeFromComponent( final Component component )
    {
        component.removeKeyListener( this );
    }
}