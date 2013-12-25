/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
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

// Local Imports
// None

/**
 * Extension of the geometry database base set of functionality that is
 * passed to the geometry importer side of the filtering system.
 * <p/>
 * 
 *
 * @author Justin
 * @version $Revision$
 */
public interface GeometryImportDatabase
    extends GeometryDatabase
{
    /**
     * Get the next URL list to be processed. If there are no more to process,
     * then return null. 
     * 
     * @return The URL list to be processed, in order
     */
    public String[] getNextURI();
   
}
