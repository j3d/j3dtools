/*
 * j3d.org Copyright (c) 2001-2015
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package j3d.filter.graph;


// External imports
// None

// Local Imports
// None

/**
 * Abstract representation of the leaf node in a scene graph that holds 
 * vertex mesh information. 
 * <p/>
 * 
 * A single mesh may be composed of zero or more geometry definitions, 
 * while containing a single appearance.
 * 
 * @author Justin
 * @version $Revision$
 */
public interface Mesh
    extends Leaf
{
    /**
     * Add a new geometry element to this mesh. Duplicates and nulls are ignored.
     * 
     * @param geom The geometry instance to add
     */
    public void addGeometry(VertexGeometry geom);
    
    /**
     * Get the geometry that has been added to this mesh, in declaration order.
     * If no geometry has been added this will return null.
     * 
     * @return An array of geometry objects, or null if none
     */
    public VertexGeometry[] getGeometry();
    
    /**
     * Set the visual properties of this mesh. These are used for rendering 
     * purposes only. Setting this will replace the existing value. Setting
     * a value of null will clear the currently set value.
     * 
     * @param props The new properties to set
     */
    public void setVisualProperties(VisualProperties props);

    /**
     * Get the currently set visual properties. If none have been set, this
     * will return null.
     * 
     * @return The properties that have been currently set
     */
    public VisualProperties getVisualProperties();
    
    /**
     * Set the physical properties of this mesh. These are used for manufacturing 
     * purposes but may also be used to influence the rendering. Setting this 
     * will replace the existing value. Setting a value of null will clear the
     * currently set value.
     * 
     * @param props The new properties to set
     */
    public void setPhysicalProperties(PhysicalProperties props);

    /**
     * Get the currently set visual properties. If none have been set, this
     * will return null.
     * 
     * @return The properties that have been currently set
     */
    public PhysicalProperties getPhysicalProperties();
    
}
