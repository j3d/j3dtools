/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// Standard imports
// None

// Application Specific imports
// None

/**
 * Listener for the facet count changing.
 *
 * <p>
 *
 * There are three modes - add a point, remove a point and draw.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface FacetCountListener
{
    /**
     * Notification that the user has selected a different number of facets
     * to work with.
     *
     * @para number The new number to use
     */
    public void changeFacetCount(int number);
}
