/*****************************************************************************
 * InputDeviceBase.java
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
import javax.vecmath.*;

import org.j3d.device.input.spaceball.transformation.*;

/**
 * Base class for input devices with one sensor. Inherited classes shall use
 * {@link #setDeltaDeviceInput} for transformation calculation. Input values
 * will be scaled. A difference transformation will be put into the sensor
 * object.<p>
 * Before using this class an appropriate <code>Behavior</code> must be added
 * to the scene graph.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 15. Jun. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 * @see InputDeviceBase#getBehavior
 *
 */
public abstract class InputDeviceBase implements InputDevice
{
    protected InputDeviceBehavior   itsBehavior = null;

    private int     itsProcessingMode = InputDevice . DEMAND_DRIVEN;
    private Sensor  itsSensor = null;

    private final Transform3D   itsTempTransform = new Transform3D( );
    private final Vector3d      itsTempPosition = new Vector3d( );
    private final Point3d       itsTempDirection = new Point3d( );
    private final Vector3d      itsTempUpVector = new Vector3d( );

    private final double[ ] itsScale =
            new double[ ]{ 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
    private final double[ ] itsScaledInput =
            new double[ ]{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    private final int[ ]    itsInputOrder = new int[ ]{ 0, 1, 2, 3, 4, 5 };

    /**
     * Constructs an InputDeviceBase object. Sets the default scale ( = 1.0 )
     * for all input values.
     * @param processingMode use constants
     *      <UL type=disk>
     *          <LI><code>InputDevice.BLOCKING</code>
     *          <LI><code>InputDevice.NON_BLOCKING</code>
     *          <LI><code>InputDevice.DEMAND_DRIVEN</code>
     *      <UL>
     */
    public InputDeviceBase( final int processingMode )
    {
        this( processingMode, null );
    }

    /**
     * Constructs an InputDeviceBase object. Sets the default scale ( = 1.0 )
     * for all input values.
     * @param processingMode use constants
     *      <UL type=disk>
     *          <LI><code>InputDevice.BLOCKING</code>
     *          <LI><code>InputDevice.NON_BLOCKING</code>
     *          <LI><code>InputDevice.DEMAND_DRIVEN</code>
     *      <UL>
     * @param sensor sensor to be used
     */
    public InputDeviceBase( final int processingMode, final Sensor sensor )
    {
        itsProcessingMode = processingMode;
        itsSensor = sensor;
    }


    /**
     * Inherited classes shall override this method and call this method, if
     * their initialization was OK.
     * @return false if sensor object was not set from inherited classes
     * @see #setSensor
     */
    public boolean initialize( )
    {
        return itsSensor != null ? true : false;
    }

    public void setNominalPositionAndOrientation( )
    {

    }

    public void processStreamInput( )
    {

    }

    /**
     * call this method when overriding
     */
    public void close( )
    {

    }

    public int getProcessingMode( )
    {
        return itsProcessingMode;
    }

    /**
     * not implemented
     */
    public void setProcessingMode( int mode )
    {
//      itsProcessingMode = mode;
    }

    public int getSensorCount( )
    {
        return 1;
    }

    /**
     * use after initialisation
     */
    public Sensor getSensor( int sensorIndex )
    {
        return itsSensor;
    }

    /**
     * @param scale value to be multiplied with the input values of the device
     *    driver. Size of array = 6.
     */
    public void setScale( final double[ ] scale )
    {
        System.arraycopy( scale, 0, itsScale, 0, scale.length );
    }

    /**
     * Set individual scale.
     * @param index 0 <= index < 6
     * @param scale value to be multiplied with the received corrsponding value
     *    of the device driver.
     */
    public void setScale( final int index, final double scale )
    {
        itsScale[ index ] = scale;
    }

    /**
     * Utility method to get the assigned <code>Behavior</code> for the input
     * device. The <code>Behavior</code> will read the transformations of the
     * inputs device's sensor and apply them to the specified
     * <code>TransformGroup</code>.<p>
     * The Behavior is not added to the scene graph. This must be done by the
     * user.
     * @param manipulator set to null if a default manipulator shall be used
     */
    public InputDeviceBehavior getBehavior( )
    {
        if( itsBehavior == null )
        {
            itsBehavior = new InputDeviceBehavior( itsSensor );
        }
        return itsBehavior;
    }

    /**
     * Define the processing order of the input values. E. g. the array
     * { 0, 2, 1, 3, 4, 5 } will reorder the input values in this way:
     *    {  inputValue[ 0 ], inputValue[ 2 ], inputValue[ 1 ],
     *       inputValue[ 3 ], inputValue[ 4 ], inputValue[ 5 ]  }
     * @param inputOrder The size of the array and the indizes must match with
     * the number of input values.
     *   @see #setDeltaDeviceInput
     */
    public void setInputValueProcessingOrder( final int[ ] inputOrder )
    {
        System.arraycopy( inputOrder, 0, itsInputOrder, 0, inputOrder.length );
    }

    /**
     * Calculates the transformation puts the result into the sensor. Scales the
     * input values. Uses the first 3 input values to translate along the
     * x-axis, y-axis and z-axis respectively and uses the second 3 input
     * values to rotate about the y-axis, x-axis and z-axis respectively
     * ( clockwise ). The 4th and 5th input values (rotation about y- and
     *  x-axis) are used as polar angles to define a direction vector for the
     *  rotations. The reference vector for the direction is (0,0,1).
     * @param input new difference input values for transformation calculation.
     *      Size of array = 6.
     * @param buttonValues set to <code>null</code> if input device has no
     *      buttons
     */
    protected void setDeltaDeviceInput( final int[ ] input, final int[ ] buttonValues )
    {
        final int length = input.length;
        for( int i = 0; i < length; i ++ )
        {
            itsScaledInput[ i ] =
                input[ itsInputOrder[ i ] ] * itsScale[ itsInputOrder[ i ] ];
        }
        TransformationUtils.getRotationTranslationTransform3D
        (
            itsScaledInput,
            itsTempTransform,
            itsTempPosition,
            itsTempDirection,
            itsTempUpVector
        );
        itsSensor.setNextSensorRead
        (
            System.currentTimeMillis( ),
            itsTempTransform,
            buttonValues
        );
    }

    /**
     * call this method during initialisation
     * @param sensor sensor to be used
     */
    protected void setSensor( final Sensor sensor )
    {
        itsSensor = sensor;
    }
}