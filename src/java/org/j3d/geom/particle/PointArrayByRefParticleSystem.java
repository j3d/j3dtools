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
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;

/**
 * A particle system that uses points to represent the individual particles.
 * <p>
 *
 * By default, the points are 3 pixels in size and are not anti-aliased.
 * Convenience methods are provided to change these settings if needed.
 *
 * @author Justin Couch based on code by Daniel Selman
 * @version $Revision: 1.3 $
 */
public class PointArrayByRefParticleSystem extends ByRefParticleSystem
{
    /** The point size to generate the particles for */
    private float pointSize;

    /** Flag to say if the points should be antialiased */
    private boolean antiAliased;

    /**
     * Create a new particle system that uses points for the particles.
     *
     * @param name An arbitrary string name for ID purposes
     * @param maxParticleCount The maximum number of particles allowed to exist
     */
    public PointArrayByRefParticleSystem(String systemName, int maxParticleCount)
    {
        super(systemName, maxParticleCount);

        pointSize = 3;
        antiAliased = false;
    }

    /**
     * Create a new particle as demanded by the system.
     *
     * @return The newly created particle
     */
    public Particle createParticle()
    {
        return new PointArrayByRefParticle(false);
    }

    /**
     * Request the number of coordinates each particle will use. Used so that
     * the manager can allocate the correct length array.
     *
     * @return The number of coordinates this particle uses
     */
    public int coordinatesPerParticle()
    {
        return 1;
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

    public GeometryArray createGeometryArray()
    {
        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS |
                     GeometryArray.TEXTURE_COORDINATE_2 |
                     GeometryArray.BY_REFERENCE |
                     GeometryArray.COLOR_4;

        GeometryArray geomArray = new PointArray(maxParticleCount, format);

        return geomArray;
    }

    /**
     * Override the normal appearance creation to generate values that are more
     * appropriate for points rather than polygons.
     *
     * @return The new appearance instance to use
     */
    protected Appearance createAppearance()
    {
        PointAttributes attr = new PointAttributes(pointSize, antiAliased);

        Appearance app = new Appearance();
        app.setPointAttributes(attr);

        return app;
    }

    /**
     * Set the point size to the new value. This will only take effect if
     * called before the initialise method. After that is ignored. The point
     * size must be > 0.
     *
     * @param size The new size to use
     * @throws IllegalArgumentException The value was <= 0
     */
    public void setPointSize(float size) throws IllegalArgumentException
    {
        if(size <= 0)
            throw new IllegalArgumentException("Point size <= 0");

        pointSize = size;
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