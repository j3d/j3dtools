/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

// Standard imports
import java.util.TreeSet;

// Application specific imports
// none

/**
 *
 * @author  Justin Couch
 * @version
 */
public interface QueueManager
{
    public void addTriangle(Object node);

    public void removeTriangle(Object node);

    public void addDiamond(Object node);

    public void removeDiamond(Object node);

    public void clear();
}
