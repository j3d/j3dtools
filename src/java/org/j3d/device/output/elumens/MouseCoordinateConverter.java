/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.device.output.elumens;

// Standard imports
// Application specific imports

/**
 * An interface for servicing coordinate conversion.  The Spiclops
 * library provides a method to unwarp mouse coordinates.  This
 * interface handles that data sleping.  After registering interest
 * the class can call warpMouseCoordinate when its update method is called.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface MouseCoordinateConverter {
    /**
     * Register interest in having a coordinate converted
     *
     * @param msc The source of the event.
     */
    public void registerInterest(MouseCoordinateSource msc);

    /**
     * Convert a set of coordinates.  Only call when the MouseCoordinateSource.update
     * method is called.
     *
     * @param coords The coordinates to convert.
     */
    public void warpMouseCoordinate(double[] coords);
}