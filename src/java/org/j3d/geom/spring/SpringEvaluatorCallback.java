/*****************************************************************************
 *                        J3D.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spring;

// External imports
// None

// Local imports
// None

/**
 * Callback interface for optional processing of a node of springs if needed.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SpringEvaluatorCallback
{

    /**
     * Process this node now.
     *
     * @param node The node instance to work with
     * @param attribs List of attributes to associate with the node
     */
    public void processSpringNode(SpringNode node, float[] attribs);
}
