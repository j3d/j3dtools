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
 * The representation of a single channel of analog samples.
 * <p>
 *
 * A trajectory may contain coordinate and/or analog data samples. This
 * represents one channel of sampled analog values. All values are placed as
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
public class C3DAnalogData
{
    /**
     * The channel number for this analog data. Note that channel numbers are
     * 1-based, not 0-based as is traditional for C/Java apps.
     */
    public final int channelNumber;

    /** The name or label associated with this trajectory data */
    public final String label;

    /** The description string associated with this data */
    public final String description;

    /** The number of valid frames of coordinate data */
    public int numFrames;

    /**
     * Analog sample data to go with each coordinate. The length of this array
     * will be number of samples times the number of frames.
     */
    public float[] analogSamples;

    /** The number of analog samples per frame of coordinate data */
    public int numAnalogSamples;

    /**
     * Construct a new data object representing a specific trajectory
     *
     * @param label The name or label of this data
     * @param description A description from the parameter information
     * @param channel The channel number for this data
     * @param numFrames The number of valid frames of data to process
     * @param numAnalogSamples The number of samples per coordinate frame. If
     *    there are no analog samples, set this to zero
     */
    public C3DAnalogData(int channel,
                         String label,
                         String description,
                         int numFrames,
                         int numAnalogSamples)
    {
        channelNumber = channel;
        this.label = label;
        this.description = description;
        this.numFrames = numFrames;
        this.numAnalogSamples = numAnalogSamples;

        analogSamples = new float[numFrames * numAnalogSamples];
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
        StringBuffer buf = new StringBuffer("C3DAnalogData: Channel: ");
        buf.append(channelNumber);

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

