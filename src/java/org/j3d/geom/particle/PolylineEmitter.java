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
import javax.vecmath.Vector3f;

// Application specific imports
import org.j3d.util.interpolator.PositionInterpolator;

/**
 * An emitter that generates particles along a polyline length.
 * <p>
 *
 * Time limits on the maximum age based on wall-clock existance time.
 * Generates particles with no velocity but placed along the line.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class PolylineEmitter implements ParticleInitializer
{
    /**
     * Number of frames of zero consecutive zero time deltas before we should
     * force the issue of another particle.
     */
    private static final int DELTA_LIMIT = 5;

    /** Base lifetime in milliseconds */
    private int lifetime;

    /** Number of particles to generate per millisecond */
    private float particlesPerMs;

    /** The origin to generate the particles at */
    private float[] origin;

    /**
     * The calculated initial velocity of the particles using the direction and
     * velocity variables.
     */
    private float[] initialVelocity;

    /** Initial colour to make all particles */
    private float[] color;

    /** Amount of variation on the randomness */
    private float variation;

    /** Initial mass that is imparted to all particles */
    private double initialMass;

    /** Initial surface area given to all particles */
    private double surfaceArea;

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
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param lineCoords The line to emit from
     * @param color The initial color of particles (4 component)
     * @param velocity The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    public PolylineEmitter(int lifetime,
                           int maxParticleCount,
                           float[] lineCoords,
                           float[] color,
                           float[] velocity,
                           float variation)
    {
        this(lifetime,
             maxParticleCount,
             lineCoords,
             lineCoords.length / 3,
             color,
             velocity,
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
     * @param velocity The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    public PolylineEmitter(int lifetime,
                           int maxParticleCount,
                           float[] lineCoords,
                           int numCoords,
                           float[] color,
                           float[] velocity,
                           float variation)
    {
        this.lifetime = lifetime;

        // Particle per millisecond is just averaged over the base lifetime
        // Uses the integer division for rounding purposes as we really don't
        // care for high-accuracy in this number - it's just a guide.
        particlesPerMs = (float)maxParticleCount / lifetime;

        initialVelocity = new float[3];
        initialVelocity[0] = velocity[0];
        initialVelocity[1] = velocity[1];
        initialVelocity[2] = velocity[2];

        this.color = new float[4];
        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2];
        this.color[3] = color[3];

        this.variation = variation;

        initialMass = 0.0000001;
        surfaceArea = 0.0004;
        zeroDeltaCounter = 0;

        interpolator = new PositionInterpolator(numCoords);
        lengthTmp = new float[numCoords];

        updateEmitterLine(lineCoords, numCoords);
    }

    /**
     * The number of particles that should be created and initialised this
     * frame. This is called once per frame by the particle system manager.
     *
     * @param timeDelta The delta between the last frame and this one in
     *    milliseconds
     * @return The number of particles to create
     */
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
    public boolean initialize(Particle particle)
    {
        particle.resultantForce.set(0, 0, 0);
        particle.setColor(color[0], color[1], color[2], color[3]);

        float rnd = 1 - (float)Math.random() * variation;
        particle.setCycleTime((int)(rnd * lifetime));

        float position_fraction = (float)Math.random();
        float[] pos = interpolator.floatValue(position_fraction);

        particle.setPositionAndPrevious(pos[0], pos[1], pos[2]);

        // Set up the initial velocity using a bit of randomness. Uses the same
        // scale factor in each component. If more randomness is desired, this
        // could be used on each component of the velocity.
        rnd = 1 - (float)Math.random() * variation;

        float v_x = initialVelocity[0] * rnd;
        float v_y = initialVelocity[1] * rnd;
        float v_z = initialVelocity[2] * rnd;

        particle.velocity.set(v_x, v_y, v_z);

        return true;
    }

    /**
     * Set the initial color that that the particle is given. If the emitter does
     * not support the alpha channel, ignore the parameter.
     *
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     * @param alpha The alpha component of the color
     */
    public void setColor(float r, float g, float b, float alpha)
    {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = alpha;
    }

    /**
     * Change the apparent surface area. Surface area is measured in square
     * metres.
     *
     * @param area surface area
     */
    public void setSurfaceArea(double area)
    {
        surfaceArea = area;
    }

    /**
     * Change the mass of the particle. Mass is measured in kilograms.
     *
     * The mass of an individual particle
     */
    public void setMass(double mass)
    {
        initialMass = mass;
    }

    /**
     * Change the basic position that the particles are being generated from.
     * Ignored by this implementation as the line defines it's own position.
     *
     * @param x The x component of the location
     * @param y The y component of the location
     * @param z The z component of the location
     */
    public void setPosition(float x, float y, float z)
    {
    }

    /**
     * Change the initial velocity that the particles are endowed with.
     *
     * @param x The x component of the velocity
     * @param y The y component of the velocity
     * @param z The z component of the velocity
     */
    public void setVelocity(float x, float y, float z)
    {
        initialVelocity[0] = x;
        initialVelocity[1] = y;
        initialVelocity[2] = z;
    }

    /**
     * Change the line coordinates used as the emitter position.
     *
     * @param lineCoords The line to emit from
     */
    public void setEmitterLine(float[] line)
    {
        updateEmitterLine(line, line.length / 3);
    }

    /**
     * Change the line coordinates used as the emitter position.
     *
     * @param lineCoords The line to emit from
     * @param numCoords The number of coordinates to read from lineCoords
     */
    public void setEmitterLine(float[] line, int numCoords)
    {
        updateEmitterLine(line, numCoords);
    }

    /**
     * Reset the interpolator with the new coordinate values.
     *
     * @param coords The list of coords in a flat array
     * @param len The number of coords to take from the array
     */
    private void updateEmitterLine(float[] coords, int len)
    {
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
