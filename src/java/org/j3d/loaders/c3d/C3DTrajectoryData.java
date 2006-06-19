/*****************************************************************************
 *                         (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.c3d;

// External imports
// None

// Local imports
// None

/**
 * The representation of a single trajectory.
 * <p>
 *
 * A trajectory may contain coordinate and/or analog data samples. Coordinates
 * are expressed in 3D space in X,Y,Z orientation. All values are placed as
 * their real floating point representations. If the underlying file had values
 * in integer format, scaling will be performed before storing in this array.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.c3d.org">http://www.c3d.org/</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class C3DTrajectoryData
{
    /**
     * 3D coordinate data for this frame. Values are interleaved as
     * [x1,y1,z1,x2,y2,z2....]. If coordinate data is not read, this will be
     * null.
     */
    public float[] coordinates;

    /** The number of valid frames of coordinate data */
    public int numFrames;

    /**
     * Analog sample data to go with each coordinate. The length of this array
     * will be number of samples times the number of frames. If no analog data
     * was recorded, this will be null.
     */
    public float[] analogSamples;

    /** The number of analog samples per frame of coordinate data */
    public int numAnalogSamples;

    /**
     * Construct a new data object representing a specific trajectory
     *
     * @param numFrames The number of valid frames of data to process
     * @param numAnalogSamples The number of samples per coordinate frame. If
     *    there are no analog samples, set this to zero
     */
    public C3DTrajectoryData(int numFrames, int numAnalogSamples)
    {
        this.numFrames = numFrames;
        this.numAnalogSamples = numAnalogSamples;

        coordinates = new float[numFrames * 3];
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string representation of this header.
     *
     * @return Information about the header
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("C3DTrajectoryData: ");
//        buf.append(name);

        buf.append("\n Number of frames: ");
        buf.append(numFrames);

        buf.append("\n Number of analog samples per frame: ");
        buf.append(numAnalogSamples);

        return buf.toString();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
}

