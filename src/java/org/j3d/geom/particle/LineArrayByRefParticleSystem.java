/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;

/**
 * LineArrayByRefParticleSystem creates a BYREF LineArray
 * to represent the ParticleSystem. This can be used for rain type
 * effects by also setting Particle.RENDER_FROM_PREVIOUS_POSITION in
 * the environment Map.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class LineArrayByRefParticleSystem extends ByRefParticleSystem
{
    /**
     * Flag for the particle creation using the draw from previous position
     * flag of the particle.
     */
    private boolean usePreviousPosition;

    /** The point size to generate the particles for */
    private float lineSize;

    /** Flag to say if the points should be antialiased */
    private boolean antiAliased;

    /** The pattern to use on the line */
    private int linePattern;

    /**
     * Create a new particle system that uses lines for the particles.
     *
     * @param name An arbitrary string name for ID purposes
     * @param maxParticleCount The maximum number of particles allowed to exist
     * @param drawFromPrevious true if this should draw the particle relative to
     *    the position of last frame
     */
    public LineArrayByRefParticleSystem(String systemName,
                                        int maxParticleCount,
                                        boolean drawFromPrevious)
    {
        super(systemName, maxParticleCount);

        usePreviousPosition = drawFromPrevious;
        lineSize = 3;
        antiAliased = false;
        linePattern = LineAttributes.PATTERN_SOLID;
    }

    /**
     * Request to create the geometry needed by this system.
     *
     * @return The object representing the geometry
     */
    public GeometryArray createGeometryArray()
    {
        GeometryArray geomArray =
                new LineArray(maxParticleCount * 2,
                              GeometryArray.COORDINATES |
                              GeometryArray.TEXTURE_COORDINATE_2 |
                              GeometryArray.BY_REFERENCE |
                              GeometryArray.COLOR_4);
        return geomArray;
    }

    /**
     * Request the number of coordinates each particle will use. Used so that
     * the manager can allocate the correct length array.
     *
     * @return The number of coordinates this particle uses
     */
    public int coordinatesPerParticle()
    {
        return 2;
    }

    /**
     * Request the number of color components this particle uses. Should be a
     * value of 4 or 3 to indicate use or not of alpha channel.
     *
     * @return The number of color components in use
     */
    public int numColorComponents()
    {
        return 4;
    }

    /**
     * Request the number of texture coordinate components this particle uses.
     * Should be a value of 2 or 3 to indicate use or not of 2D or 3D textures.
     *
     * @return The number of color components in use
     */
    public int numTexCoordComponents()
    {
        return 2;
    }

    /**
     * Create a new particle with the given ID.
     *
     * @param index The id of the particle
     * @return A particle corresponding to the given index
     */
    public Particle createParticle()
    {
        Particle particle = new LineArrayByRefParticle(usePreviousPosition);
        return particle;
    }

    /**
     * Override the normal appearance creation to generate values that are more
     * appropriate for lines rather than polygons.
     *
     * @return The new appearance instance to use
     */
    public Appearance createAppearance()
    {
        LineAttributes attr = new LineAttributes(lineSize,
                                                 linePattern,
                                                 antiAliased);

        Appearance app = super.createAppearance();

        app.setLineAttributes(attr );

        return app;
    }

    /**
     * Set the line size to the new value. This will only take effect if
     * called before the initialise method. After that is ignored. The line
     * size must be > 0.
     *
     * @param size The new size to use
     * @throws IllegalArgumentException The value was <= 0
     */
    public void setLineSize(float size) throws IllegalArgumentException
    {
        if(size <= 0)
            throw new IllegalArgumentException("Line width <= 0");

        lineSize = size;
    }

    /**
     * Set the line style to the new value. This will only take effect if
     * called before the initialise method. After that is ignored. The line
     * style is one of the values defined in the LineAttributes class of
     * Java3D
     *
     * @param type The new style to use
     * @see javax.media.j3d.LineAttributes
     */
    public void setLineStyle(int type)
    {
        linePattern = type;
    }

    /**
     * Set whether the points should be antialiased or not. This will only take
     * effect if called before the initialise method. After that is ignored.
     *
     * @param state true if it should be anti-aliased, false if not
     */
    public void setAntiAliased(boolean state)
    {
        antiAliased = state;
    }
}
