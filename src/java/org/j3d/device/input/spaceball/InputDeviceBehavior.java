/*****************************************************************************
 * InputDeviceBehavior.java
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
import java.util.Enumeration;

import org.j3d.device.input.spaceball.transformation.*;

/**
 * Class.<p>
 * A listener can be added to get notified if the transformation is updated
 * and to process button events. In this case it is the listeners
 * responsiblity to set the updated transformation to the
 * <code>TransformGroup</code>.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept of Medical Computer Sciences
 * @version 25. Oct. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class InputDeviceBehavior extends Behavior
{
    private final Sensor            itsSensor;
    private TransformGroup          itsTransformGroup = null;
    private Node                    itsLocalCoordinateSystemNode = null;
    private Manipulator             itsManipulator = null;

    private final WakeupCondition   itsWakeup;

    private final int[ ]            itsButtonValues;

    private final Transform3D       itsDeltaTransform = new Transform3D( );
    private final Transform3D       itsLocalToVWorldTransform =
            new Transform3D( );
    private final Transform3D       itsCurrentTransform = new Transform3D( );
    private final Transform3D       itsNewTransform = new Transform3D( );
    private final Transform3D       itsInitialTransform = new Transform3D( );

    private InputDeviceCallback     itsListener = null;

    /**
     * Constructs an InputDeviceBehavior object. Uses per default a
     * {@link DefaultManipulator}.<p>
     * @param sensor The sensor of an input device which shall be used for
     *      calculating the absolute transformation.
     */
    public InputDeviceBehavior( final Sensor sensor )
    {
        this( sensor, new DefaultManipulator( ) );
    }

    /**
     * Constructs an InputDeviceBehavior object. Uses a
     * {@link DefaultManipulator} per default.
     * @param sensor The sensor of an input device which shall be used for
     *      calculating the absolute transformation.
     * @param manipulator manipulator for calculating a transformation out of
     *      the input values
     */
    public InputDeviceBehavior
    (
        final Sensor        sensor,
        final Manipulator   manipulator
    )
    {
        super( );
        setSchedulingBounds( new BoundingSphere( ) );

        itsSensor = sensor;
        final int processingMode = sensor.getDevice( ) . getProcessingMode( );
        if( processingMode == InputDevice.DEMAND_DRIVEN )
        {
            itsWakeup = new WakeupOnBehaviorPost( this, processingMode );
        }
        else
        {
            itsWakeup = new WakeupOnElapsedFrames( 0 );
        }
        setManipulator( manipulator );
        itsButtonValues = new int[ sensor.getSensorButtonCount( ) ];
    }

    public void initialize( )
    {
        wakeupOn( itsWakeup );
    }

    /**
     * @parameter manipulator must not be null
     */
    public synchronized void setManipulator( final Manipulator manipulator )
    {
        if( manipulator != null )
        {
            itsManipulator = manipulator;
        }
    }

    /**
     * @param transformGroup <code>TransformGroup</code> to be used. Set to null
     *      to remove the current <code>TransformGroup</code>. The
     *      <code>TransformGroup.ALLOW_TRANSFORM_READ</code> and
     *      <code>TransformGroup.ALLOW_TRANSFORM_WRITE</code> capabilities must
     *      be set.
     */
    public synchronized void
    setTransformGroup( final TransformGroup transformGroup )
    {
        final int readCapability = TransformGroup.ALLOW_TRANSFORM_READ;
        final int writeCapability = TransformGroup.ALLOW_TRANSFORM_WRITE;
        if( transformGroup.getCapability( readCapability )
            && transformGroup.getCapability( writeCapability ) )
        {
            itsTransformGroup = transformGroup;
            transformGroup.getTransform( itsInitialTransform );
        }
        else
        {
            itsTransformGroup = null;
        }
    }

    /**
     * @param node Set to null to remove the current <code>Node</code>.
     */
    public synchronized void setLocalCoordinateSystemNode( final Node node )
    {
        itsLocalCoordinateSystemNode = node;
        if( node == null )
        {
            itsLocalToVWorldTransform.setIdentity( );
        }
    }

    /**
     * Sets a new object to be called if the transformation is updated.<p>
     * NOTe: The <code>listener</code> is reponsible to apply the new
     * transformation to the <code>TransformGroup</code>.
     * @param listener Set to <code>null</code> to remove the current
     *      <code>listener</code>.
     */
    public synchronized void
    setCallback( final InputDeviceCallback listener )
    {
        itsListener = listener;
    }

    public void processStimulus( Enumeration criteria )
    {
        final TransformGroup tg;
        final Node localCoordSystem;
        final Manipulator manipulator;
        final InputDeviceCallback listener;
        synchronized( this )
        {
            tg = itsTransformGroup;
            localCoordSystem = itsLocalCoordinateSystemNode;
            manipulator = itsManipulator;
            listener = itsListener;
        }
        itsSensor.getRead( itsDeltaTransform );
        itsSensor.lastButtons( itsButtonValues );
        if( tg != null )
        {
            if( localCoordSystem != null )
            {
                localCoordSystem.getLocalToVworld( itsLocalToVWorldTransform );
            }
            tg.getTransform( itsCurrentTransform );
            manipulator.calculateTransform
            (
                itsNewTransform,
                itsCurrentTransform,
                itsDeltaTransform,
                itsLocalToVWorldTransform
            );
        }

        if( listener != null )
        {
            listener.update
            (
                this,
                tg,
                itsNewTransform,
                itsDeltaTransform,
                itsButtonValues
            );
        }
        else if( tg != null )
        {
            tg.setTransform( itsNewTransform );
        }
        wakeupOn( itsWakeup );
    }

    public void reset( )
    {

    }
}