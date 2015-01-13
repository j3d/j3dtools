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
import org.j3d.util.interpolator.PositionInterpolator;

/**
 * An emitter that generates particles along a single polyline length.
 * <p>
 *
 * Time limits on the maximum age based on wall-clock existance time.
 * Generates particles with no velocity but placed along the line.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class PolylineEmitter extends BaseEmitter
{
    /**
     * Number of frames of zero consecutive zero time deltas before we should
     * force the issue of another particle.
     */
    private static final int DELTA_LIMIT = 5;

    /** Number of particles to generate per millisecond */
    private float particlesPerMs;

    /** The origin to generate the particles at */
    private float[] origin;

    /** The direction to shoot the particles in */
    private float[] direction;

    /**
     * The calculated initial velocity of the particles using the direction and
     * velocity variables.
     */
    private float[] initialVelocity;

    /** Did we have any valid coordinates set? If not treat as point */
    private boolean usePointSource;

    /** The interpolator to work out where we are on the polyline */
    private PositionInterpolator interpolator;

    /** Temporary array for working with the intermediate lengths */
    private float[] lengthTmp;

    /**
     * The zero time delta counter - used to make sure particles are still
     * issued on really high-speed machines.
     */
    private int zeroDeltaCounter;

    /**
     * Used to handle the case when there is a low particle count relative to
     * the lifetime. This is the case when there is less than one particle per
     * millisecond to be generated. Saves up the previous elapsed time since
     * the last particle was generated until we get to the point where one has
     * to be generated.
     */
    private int elapsedZeroParticleTime;

    /**
     * Construct a new default emitter. All values are set to zero, and the
     * direction is straight up along the Y axis. The initial line is a
     * a point at the origin.
     */
    public PolylineEmitter()
    {
        initialVelocity = new float[3];
        direction = new float[3];
        direction[1] = 1;

        interpolator = new PositionInterpolator(2);
        lengthTmp = new float[2];

        usePointSource = true;
        elapsedZeroParticleTime = 0;
    }

    /**
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param lineCoords The line to emit from
     * @param color The initial color of particles (4 component)
     * @param speed The speed of the particles to start with
     * @param direction The direction the particles are sent in
     * @param variation The amount of variance for the initial values
     */
    public PolylineEmitter(int maxTime,
                           int maxParticleCount,
                           float[] lineCoords,
                           float[] color,
                           float[] direction,
                           float speed,
                           float variation)
    {
        this(maxTime,
             maxParticleCount,
             lineCoords,
             lineCoords.length / 3,
             color,
             direction,
             speed,
             variation);
    }

    /**
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param lineCoords The line to emit from
     * @param numCoords The number of coordinates to read from lineCoords
     * @param color The initial color of particles (4 component)
     * @param speed The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    public PolylineEmitter(int maxTime,
                           int maxParticleCount,
                           float[] lineCoords,
                           int numCoords,
                           float[] color,
                           float[] direction,
                           float speed,
                           float variation)
    {
        super(maxTime, maxParticleCount, color, speed, variation);

        // Particle per millisecond is just averaged over the base lifetime
        // Uses the integer division for rounding purposes as we really don't
        // care for high-accuracy in this number - it's just a guide.
        particlesPerMs = (lifetime == 0) ? 1 : (float)particleCount / lifetime;
        elapsedZeroParticleTime = 0;

        initialVelocity = new float[3];
        initialVelocity[0] = direction[0] * speed;
        initialVelocity[1] = direction[1] * speed;
        initialVelocity[2] = direction[2] * speed;

        this.direction = new float[3];

        float d = direction[0] * direction[0] + direction[1] * direction[1] +
                  direction[2] * direction[2];

        if(d != 0)
        {
            d = 1 / d;
            this.direction[0] = d * direction[0];
            this.direction[1] = d * direction[1];
            this.direction[2] = d * direction[2];
        }

        zeroDeltaCounter = 0;

        interpolator = new PositionInterpolator(numCoords);
        lengthTmp = new float[numCoords];
        updateEmitterLine(lineCoords, numCoords);
    }

    /**
     * Adjust the maximum number of particles that this initializer is going to
     * work with. This should not normally be called by the end user. The
     * particle system that this initializer is registered with will call this
     * method when it's corresponding method is called.
     *
     * @param maxCount The new maximum particle count to use
     */
    @Override
    public void setMaxParticleCount(int maxCount)
    {
        super.setMaxParticleCount(maxCount);

        particlesPerMs = (lifetime == 0) ? 1 : (float)particleCount / lifetime;
    }

    /**
     * Change the maximum lifetime of the particles. The lifetime of particles
     * is defined in milliseconds, and must be positive.
     *
     * @param time The new lifetime, in milliseconds
     * @throws IllegalArgumentException The lifetime is zero or negative
     */
    @Override
    public void setParticleLifetime(int time)
        throws IllegalArgumentException
    {
        super.setParticleLifetime(time);

        particlesPerMs = (lifetime == 0) ? 1 : (float)particleCount / lifetime;
    }

    /**
     * The number of particles that should be created and initialised this
     * frame. This is called once per frame by the particle system manager.
     *
     * @param timeDelta The delta between the last frame and this one in
     *    milliseconds
     * @return The number of particles to create
     */
    @Override
    public int numParticlesToCreate(int timeDelta)
    {
        int ret_val = 0;

        switch(timeDelta)
        {
            case -1:
                // do nothing
                break;

            case 0:
                if(zeroDeltaCounter == DELTA_LIMIT)
                {
                    ret_val = 1;
                    zeroDeltaCounter = 0;
                }
                else
                    zeroDeltaCounter++;
                break;

            default:
                zeroDeltaCounter = 0;
                ret_val = (int)(particlesPerMs * timeDelta);
                if(ret_val == 0)
                {
                    elapsedZeroParticleTime += timeDelta;

                    ret_val = (int)(particlesPerMs * elapsedZeroParticleTime);
                    if(ret_val != 0)
                        elapsedZeroParticleTime = 0;
                }
        }

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
    @Override
    public boolean initialize(Particle particle)
    {
        particle.resultantForce.set(0, 0, 0);
        particle.setColor(color[0], color[1], color[2], color[3]);

        float rnd = 1 - randomiser.nextFloat() * lifetimeVariation;
        particle.setCycleTime((int)(rnd * lifetime));

        if(usePointSource)
        {
            particle.setPosition(0, 0, 0);
        }
        else
        {
            float position_fraction = randomiser.nextFloat();
            float[] pos = interpolator.floatValue(position_fraction);

            particle.setPosition(pos[0], pos[1], pos[2]);
        }

        particle.setMass(initialMass);
        particle.setSurfaceArea(surfaceArea);

        // Set up the initial velocity using a bit of randomness. Uses the same
        // scale factor in each component. If more randomness is desired, this
        // could be used on each component of the velocity.
        rnd = 1 - randomiser.nextFloat() * variation;

        float v_x = initialVelocity[0] * rnd;
        float v_y = initialVelocity[1] * rnd;
        float v_z = initialVelocity[2] * rnd;

        particle.velocity.set(v_x, v_y, v_z);

        return true;
    }

    /**
     * Change the initial speed that the particles are endowed with.
     *
     * @param speed The magnitude of the speed to use
     */
    @Override
    public void setSpeed(float speed)
    {
        super.setSpeed(speed);

        initialVelocity[0] = direction[0] * speed;
        initialVelocity[1] = direction[1] * speed;
        initialVelocity[2] = direction[2] * speed;
    }

    /**
     * Change the initial velocity that the particles are endowed with by
     * modifying the direction. Speed still stays the same. The direction
     * value will be normalised to a unit length vector before being applied
     * to the velocity calculation.
     *
     * @param x The x component of the velocity direction
     * @param y The y component of the velocity direction
     * @param z The z component of the velocity direction
     */
    public void setDirection(float x, float y, float z)
    {
        float d = x * x + y * y + z * z;
        if(d != 0)
        {
            d = 1 / d;
            x *= d;
            y *= d;
            z *= d;
        }

        direction[0] = x;
        direction[1] = y;
        direction[2] = z;

        initialVelocity[0] = x * speed;
        initialVelocity[1] = y * speed;
        initialVelocity[2] = z * speed;
    }

    /**
     * Change the line coordinates used as the emitter position.
     *
     * @param lineCoords The line to emit from
     */
    public void setEmitterLine(float[] lineCoords)
    {
        updateEmitterLine(lineCoords, lineCoords.length / 3);
    }

    /**
     * Change the line coordinates used as the emitter position.
     *
     * @param lineCoords The line to emit from
     * @param numCoords The number of coordinates to read from lineCoords
     */
    public void setEmitterLine(float[] lineCoords, int numCoords)
    {
        updateEmitterLine(lineCoords, numCoords);
    }

    /**
     * Reset the interpolator with the new coordinate values.
     *
     * @param coords The list of coords in a flat array
     * @param len The number of coords to take from the array
     */
    private void updateEmitterLine(float[] coords, int len)
    {
        if(len == 0)
        {
            usePointSource = true;
            return;
        }

        usePointSource = false;

        interpolator.clear();

        // First calculate the total length of the line and the fractions
        // of the length of the line
        if(lengthTmp.length < len)
            lengthTmp = new float[len];

        int idx = 0;
        float total_dist = 0;

        for(int i = 0; i < len - 1; i++)
        {
            float x = coords[idx] - coords[idx + 3];
            float y = coords[idx + 1] - coords[idx + 4];
            float z = coords[idx + 2] - coords[idx + 5];

            lengthTmp[i] = x * x + y * y + z * z;
            total_dist += lengthTmp[i];

            idx += 3;
        }

        // Insert the values in the array
        interpolator.addKeyFrame(0, coords[0], coords[1], coords[2]);
        idx = 3;
        float acc_dist = 0;

        for(int i = 0; i < len - 1; i++)
        {
            acc_dist += lengthTmp[i];
            float fraction =  acc_dist / total_dist;
            interpolator.addKeyFrame(fraction,
                                     coords[idx++],
                                     coords[idx++],
                                     coords[idx++]);
        }
    }
}
