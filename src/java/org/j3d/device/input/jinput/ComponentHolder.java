/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.device.input.jinput;

// External imports
import net.java.games.input.Component;

// Local imports
// None

/**
 * Associates a component with a browser function.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class ComponentHolder
{
    public Component component;
    public int function;

    public ComponentHolder(Component component, int function)
    {
        this.component = component;
        this.function = function;
    }
}
