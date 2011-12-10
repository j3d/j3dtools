/*****************************************************************************
 *                    j3d.org Copyright (c) 2004  - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter;

// External imports
// None

// Local imports
// None

/**
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface GeometryFilter
{
    /**
     * Initialise the filter for the given database now. Processing has not yet
     * started for the filter at this time.
     * 
     * @param db The database instance to use as the source for geometry
     */
    public void initialise(GeometryDatabase db);

    public void startStructure(int id, SceneGraphObjectType type);

    public void endStructure(int id, SceneGraphObjectType type);

    /**
     * Filter processing has completed, so close down anything that this filter
     * may still have open.
     */
    public void shutdown();
}
