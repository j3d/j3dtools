/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// Standard imports
import javax.vecmath.Color4f;

import javax.media.j3d.*;

// Application specific imports
// None

/**
 * Abstract ParticleSystem for handling ByRef GeometryArrays.
 * <p>
 *
 * The entire geometry for the ParticleSystem represented
 * by a single Shape3D. When an update request is received, the particles
 * update the position and color information in the arrays provided by this
 * class and are updated in the underlying geometry.
 * <p>
 * TODO: add support for interleaved arrays and test performance.
 *
 * @author Daniel Selman
 * @version $Revision: 1.6 $
 */
public abstract class ByRefParticleSystem extends ParticleSystem
        implements GeometryUpdater
{
    /** The geometry created for this system */
    private GeometryArray geometryArray;

    /** Array containing the current position coordinates */
    private float[] positionRefArray;

    /** Array containing the current texture coordinates */
    private float[] textureCoordRefArray;

    /** Array containing the current color values */
    private float[] colorRefArray;

    /** Array containing the current normals */
    private float[] normalRefArray;

    /** The shape containing the geometry */
    protected Shape3D shape;

    /** Array increment value for the coordinate array when writing values */
    protected int coordInc;

    /** Array increment value for the color array when writing values */
    protected int colorInc;

    /** Array increment value for the texture coords array when writing values */
    protected int textureInc;

    /** The number of valid particles last time around */
    protected int numValidVertices;

    /** Flag to say this is the first update of this system */
    private boolean firstUpdate;

    /**
     * Create a new particle system that represents the given type.
     *
     * @param name An arbitrary string name for ID purposes
     * @param maxParticleCount The maximum number of particles allowed to exist
     */
    public ByRefParticleSystem(String systemName, int maxParticleCount)
    {
        super(systemName, maxParticleCount);

        shape = new Shape3D(); // new OrientedShape3D();

        geometryArray = createGeometryArray();
        geometryArray.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
        geometryArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);

        shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

        shape.setGeometry(geometryArray);
        shape.setAppearance(createAppearance());

        shape.setCollidable(false);
        shape.setPickable(false);

        firstUpdate = true;
    }

    /**
     * Override the initialise method of the base class to allow setting up of
     * the correct array sizes.
     */
    public void initialize()
    {
        int num_coords = coordinatesPerParticle();

        coordInc = num_coords * 3;
        colorInc = num_coords * numColorComponents();
        textureInc = num_coords * numTexCoordComponents();

        int pos = maxParticleCount * coordInc;
        int col = maxParticleCount * colorInc;
        int norm = maxParticleCount * coordInc;
        int tex = maxParticleCount * textureInc;

        positionRefArray = new float[pos];
        colorRefArray = new float[col];
        normalRefArray = new float[norm];
        textureCoordRefArray = new float[tex];

        super.initialize();
    }

    /**
     * Fetch the scene graph node that represents the particle system.
     *
     * @return The shape containing the particles
     */
    public Node getNode()
    {
        return shape;
    }

    public void onRemove()
    {
        Appearance app = new Appearance();
        RenderingAttributes renderingAttributes = new RenderingAttributes();
        renderingAttributes.setVisible(false);

        app.setRenderingAttributes(renderingAttributes);
        shape.setAppearance(app);
    }

    /**
     * Request to create the geometry needed by this system.
     *
     * @return The object representing the geometry
     */
    public abstract GeometryArray createGeometryArray();

    /**
     * Update the arrays for the geometry object.
     */
    protected void updateGeometry()
    {
        int num_particles = particleList.size();
        int color_index = 0;
        int coord_index = 0;

        ByRefParticle p;

        for(int i = 0; i < num_particles; i++)
        {
            p = (ByRefParticle)particleList.next();

            p.updateGeometry(positionRefArray, coord_index);
            p.updateColors(colorRefArray, color_index);

            coord_index += coordInc;
            color_index += colorInc;
        }

        particleList.reset();

        numValidVertices = num_particles * coordinatesPerParticle();

        // First time through, set up the normal and texture coordinate
        // arrays. Only needs to be done once as it never changes.
        // Reuse the color_index and coord_index fields
        if(firstUpdate)
        {
            color_index = 0;
            coord_index = 0;
            p = (ByRefParticle)particleList.next();

            for(int i = 0; i < num_particles; i++)
            {
                p.updateNormals(normalRefArray, coord_index);
                p.updateTexCoords(textureCoordRefArray, color_index);

                coord_index += coordInc;
                color_index += textureInc;
            }

            particleList.reset();
        }
    }

    /**
     * Request to force an update of the geometry now.
     *
     * @return true if the system is currently running
     */
    public boolean update()
    {
        boolean val = super.update();

        geometryArray.updateData(this);

        return val;
    }

    /**
     * Update request on the geometry data that is accessed by reference.
     *
     * @param geometry The geometry object being updated
     */
    public void updateData(Geometry geometry)
    {
        GeometryArray geometryArray = (GeometryArray)geometry;

        geometryArray.setValidVertexCount(numValidVertices);

        geometryArray.setCoordRefFloat(positionRefArray);
        geometryArray.setColorRefFloat(colorRefArray);

        if(firstUpdate)
        {
            geometryArray.setNormalRefFloat(normalRefArray);
            geometryArray.setTexCoordRefFloat(0, textureCoordRefArray);
            firstUpdate = false;
        }
    }
}
