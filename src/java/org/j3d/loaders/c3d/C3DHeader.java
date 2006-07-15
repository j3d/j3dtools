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
 * The representation of most of the header of a C3D file.
 * <p>
 *
 * The header contains the following information:
 *
 * <ul>
 * <li>The number of trajectories stored within the file</li>
 * <li>The number of analog channels recorded in the file</li>
 * <li>The number of trajectory samples stored within the file</li>
 * <li>The number of analog samples stored within the file</li>
 * <li>The trajectory and analog sample rates</li>
 * </ul>
 *
 * A standard C3D header also includes the following information that is not
 * kept by this class as it is only useful for parsing.
 *
 * <ul>
 * <li>The location of the start of the interleaved 3D and analog data records
 *     within the file</li>
 * <li>The location of the start of the parameter records within the file</li>
 * </ul>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.c3d.org">http://www.c3d.org/</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class C3DHeader
{
    /**
     * The number of trajectories stored within the file.A trajectory is also
     * sometimes known as a marker, refering to the shiny reflective markers
     * used in motion capture systems.
     */
    public int numTrajectories;

    /** The number of trajectory samples stored within the file */
    public int numTrajectorySamples;

    /** The trajectory sample rate */
    public float trajectorySampleRate;

    /** The number of analog channels recorded in the file */
    public int numAnalogChannels;

    /** The number of analog samples per frame of 3D data */
    public int numAnalogSamplesPer3DFrame;

    /** The trajectory and analog sample rates */
    public float analogSampleRate;

    /**
     * The maximum interpolation gap between frames. The spec says the following:
     * <blockquote>
     * Header word six contains a value that records the maximum interpolation
     * gap length for 3D point data.  The use of this item is not specified in
     * the C3D file description although the maximum interpolation gap length
     * value is usually set to indicate the maximum length of invalid 3D point
     * data samples (in frames) over which interpolation was performed in the
     * creation of the C3D file. This may be used by various applications to
     * specify the length of gaps that can be interpolated or gap filled when
     * reading or creating a C3D file. Since the value of the maximum
     * interpolation gap is recorded in 3D frames, it represents time. Note
     * that since this value is not well defined in the C3D file specification,
     * its use does not indicate that any 3D data points are actually
     * interpolated the precise interpretation of this value is left up to the
     * application that created the data. Any application reading the C3D file
     * may, if necessary or requested, override this value and interpolate gaps
     * of any length.
     * </blockquote>
     */
    public int maxInterpolationGap;

    /**
     * The 3D scale factor used to convert ints to floats. If this is -1 the
     * the raw data is already in floating point format.
     */
    public float scaleFactor;

    /** True if range and label data is available */
    public boolean hasRangeData;

    /** The number of defined time events. Maximum of 18. */
    public int numTimeEvents;

    /** The time the events occur in seconds. Array is length numTimeEvents */
    public float[] eventTimes;

    /**
     * Flags about each event derived from the file. True is the same as on, as
     * stored in the file, false is off. Array is length numTimeEvents
     */
    public boolean[] eventDisplayFlag;

    /**
     * 4 character label associated with each event time. In old versions of
     * the file format, these may be only 2 characters.
     */
    public String[] eventLabels;

    // Variables that are not exposed to the outside world

    // Traditionally 1-based frame numbers are used in the file format.
    // We use zero-based here so all numbers are -1 compared to what is in
    // the file.

    /** Frame number that the first 3D data block starts at. */
    int start3DFrame;

    /** Frame number that the first 3D data block ends at. */
    int end3DFrame;

    /** The number of the first block of the 3D+Analog section (DATA_START) */
    int startDataBlock;

    /** First block of the range and label data */
    int rangeDataStart;

    /** The original byte encoding format */
    int processorType;

    /** Block number that parameters start at */
    int startParamBlock;

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
        return toString(false);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Alternate form of the toString() method that has the option of showing
     * private data details from the internals of the way the file is stored.
     *
     * @param showFileDetails true if the private data should be shown
     * @return Information about the header
     */
    public String toString(boolean showFileDetails)
    {
        StringBuffer buf = new StringBuffer("C3DHeader: ");
        buf.append(" encoding ");

        switch(processorType)
        {
            case 1:
                buf.append("Intel");
                break;

            case 2:
                buf.append("DEC/VAX");
                break;

            case 3:
                buf.append("SGI/MIPS");
                break;
        }

        buf.append("\n Trajectories: ");
        buf.append(numTrajectories);
        buf.append(" samples: ");
        buf.append(numTrajectorySamples);
        buf.append(" rate ");
        buf.append(trajectorySampleRate);
        buf.append("\n Analog Channels: ");
        buf.append(numAnalogChannels);
        buf.append(" samples per frame: ");
        buf.append(numAnalogSamplesPer3DFrame);
        buf.append(" rate: ");
        buf.append(analogSampleRate);
        buf.append("\n Interpolation Gap: ");
        buf.append(maxInterpolationGap);
        buf.append(" scale factor: ");
        buf.append(scaleFactor);
        buf.append("\n Range data available?: ");
        buf.append(hasRangeData ? "Yes" : "No");
        buf.append("\n Time Events");

        for(int i = 0; i < numTimeEvents; i++)
        {
            buf.append("\n  \'");
            buf.append(eventLabels[i]);
            buf.append("\' at time ");
            buf.append(eventTimes[i]);
            buf.append(" display? ");
            buf.append(eventDisplayFlag[i] ? "Yes" : "No");
        }

        if(showFileDetails)
        {
            buf.append("\n Param block starts at ");
            buf.append(startParamBlock);
            buf.append("\n 3D frame range: ");
            buf.append(start3DFrame);
            buf.append(" to ");
            buf.append(end3DFrame);

            buf.append("\n Data block starts at: ");
            buf.append(startDataBlock);
            buf.append("\n Range data starts at: ");
            buf.append(rangeDataStart);
        }

        return buf.toString();
    }
}

