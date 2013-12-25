/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// External imports
// None

// Local imports
// None

/**
 * Generates particles that explode from a point in space, in any direction.
 * <p>
 * All particles are generated in the initial valuation and then no further
 * particles are generated.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class ExplosionPointEmitter extends BaseEmitter
{
    /** The origin to generate the particles at */
    private float[] origin;

    /**
     * Construct a new default emitter. All values are set to zero.
     */
    public ExplosionPointEmitter()
    {
        origin = new float[3];
    }

    /**
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param position The emitting position in the local space
     * @param color The initial color of particles (4 component)
     * @param speed The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    public ExplosionPointEmitter(int maxTime,
                                 int maxParticleCount,
                                 float[] position,
                                 float[] color,
                                 float speed,
                                 float variation)
    {
        super(maxTime, maxParticleCount, color, speed, variation);

        this.origin = new float[3];
        this.origin[0] = position[0];
        this.origin[1] = position[1];
        this.origin[2] = position[2];
    }

    /**
     * The number of particles that should be created and initialised this
     * frame. This is called once per frame by the particle system manager.
     * Sends all particles initialially and nothing after that.
     *
     * @param timeDelta The delta between the last frame and this one in
     *    milliseconds
     * @return The number of particles to create
     */
    public int numParticlesToCreate(int timeDelta)
    {
        int ret_val = particleCount;
        particleCount = 0;

        return ret_val;
    }

    /**
     * Initialize a particle based on the rules defined by this initializer.
     * The particle system may choose to re-initialise previously dead
     * particles. The implementation should not care whether the particle was
     * previously in existance or not.
     *
     * @param particle The particle instance to initialize
     * @return true if the ParticleSytem should keep running
     */
    public boolean initialize(Particle particle)
    {
        // Vary the alpha channel of the color a bit too
        float rnd = 1 - randomiser.nextFloat() * variation;
        particle.setColor(color[0], color[1], color[2], color[3] * rnd);

        rnd = 1 - randomiser.nextFloat() * lifetimeVariation;
        particle.setCycleTime((int)(lifetime * rnd));

        particle.setPosition(origin[0], origin[1], origin[2]);
        particle.resultantForce.set(0, 0, 0);
        particle.setMass(initialMass);
        particle.setSurfaceArea(surfaceArea);

        float x_sign = randomiser.nextBoolean() ? 1 : -1;
        float y_sign = randomiser.nextBoolean() ? 1 : -1;
        float z_sign = randomiser.nextBoolean() ? 1 : -1;

        float v_x = randomiser.nextFloat() * variation * speed * x_sign;
        float v_y = randomiser.nextFloat() * variation * speed * y_sign;
        float v_z = randomiser.nextFloat() * variation * speed * z_sign;

        particle.velocity.set(v_x, v_y, v_z);
        particle.velocity.normalise();
        particle.velocity.scale(speed * rnd);

        return true;
    }

    /**
     * Change the basic position that the particles are being generated from.
     *
     * @param x The x component of the location
     * @param y The y component of the location
     * @param z The z component of the location
     */
    public void setPosition(float x, float y, float z)
    {
        origin[0] = x;
        origin[1] = y;
        origin[2] = z;
    }
}
