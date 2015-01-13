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
 * @version $Revision: 1.2 $
 */
public class C3DTrajectoryData
{
    /** The name or label associated with this trajectory data */
    public final String label;

    /** The description string associated with this data */
    public final String description;

    /**
     * 3D coordinate data for this frame. Values are interleaved as
     * [x1,y1,z1,x2,y2,z2....].
     */
    public float[] coordinates;

    /** The number of valid frames of coordinate data */
    public int numFrames;

    /**
     * The bit mask of camera IDs that contributed to each frame. Using a short
     * rather than a byte defined by the C3D spec as there are proposals to
     * support more than 7 cameras proposed by Vicon.
     */
    public short[] cameraMasks;

    /**
     * Accuracy information about the coordinates made each frame.
     */
    public float[] residuals;

    /**
     * Construct a new data object representing a specific trajectory
     *
     * @param label The name or label of this data
     * @param description A description from the parameter information
     * @param numFrames The number of valid frames of data to process
     */
    public C3DTrajectoryData(String label, String description, int numFrames)
    {
        this.label = label;
        this.description = description;
        this.numFrames = numFrames;

        coordinates = new float[numFrames * 3];
        cameraMasks = new short[numFrames];
        residuals = new float[numFrames];
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string representation of this header.
     *
     * @return Information about the header
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer("C3DTrajectoryData: ");
        buf.append(label);
        buf.append("\n Number of frames: ");
        buf.append(numFrames);

        return buf.toString();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
}

