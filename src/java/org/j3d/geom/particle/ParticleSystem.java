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
import java.util.ArrayList;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

/**
 * Abstract representation of a ParticleSystem.
 * <p>
 *
 * A ParticleSystem manages a List of Particles created by a ParticleFactory.
 * It applies changes to the Particles using a List of ParticleFunctions and a
 * a single emitter.
 * <P>
 * A ParticleSystem can be represented in any way appropriate, the only
 * requirement is that is create a Node to be added to the scenegraph.
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>negParticleCountMsg: Message when a negative particle size is
 *     provided. </li>
 * </ul>
 *
 * @author Justin Couch, based on code by Daniel Selman
 * @version $Revision: 2.3 $
 */
public abstract class ParticleSystem implements ParticleFactory
{
    /** The initial number of functions to assume just for initialisation */
    private static final int NUM_INIT_FUNCTIONS = 5;

    /** Error message when the particle count is negative */
    private static final String NEG_PARTICLE_COUNT_PROP =
        "org.j3d.geom.particle.ParticleSystem.negParticleCountMsg";

    /** Local reporter to put errors in */
    protected ErrorReporter errorReporter;

    /** * Identifier for this particle system type. */
    private String systemName;

    /** Maximum number of particles this system can handle */
    protected int maxParticleCount;

    /** Current number of live particles */
    protected int particleCount;

    /** Residue of particles not created last frame */
    private int creationResidue;

    /** Inter-frame time delta. */
    protected int frameTime;

    /** The time of this frame. Set during the update() method */
    protected long timeNow;

    /** * List of ParticleFunctions to be applied to each Particle. */
    private ArrayList<ParticleFunction> particleFunctions;

    /** The cache of currently dead particles */
    private Particle[] deadParticles;

    /** List of active functions for this frame */
    private ParticleFunction[] activeFunctions;

    /** The number of active functions in the above array */
    private int numActiveFunctions;

    /** The number of dead particles currently */
    private int numDeadParticles;

    /** List of currently active particle instances */
    protected ParticleList particleList;

    /** Interpolator of texture coordinate handling */
    protected TexCoordInterpolator texCoordInterp;

    /** The ParticleInitializer for this ParticleSystem. */
    private ParticleInitializer particleInitializer;

    /** Flag indicating if we should be generating new particles or not */
    private boolean createParticles;

    /**
     * Counter check for balancing the delta time values for dealing with
     * the screwed up Win32 clock. When this gets set, we had a zero-time
     * frame delta the previous frame. To avoid divZero we arbitrarily set
     * dT to 1. We need to compensate for that in the next frame to avoid
     * clock creep, so this flag tells the class when to add and remove a
     * per-frame increment.
     */
    private boolean zeroFrame;

    /**
     * Flag indicating that texture coordinate values should be generated
     * for the particles
     */
    protected boolean genTexCoords;


    /**
     * Create a new ParticleSystem.
     *
     * @param name An arbitrary string name for ID purposes
     * @param maxParticleCount The maximum number of particles allowed to exist
     * @throws IllegalArgumentException The particle count was negative
     */
    public ParticleSystem(String name, int maxParticleCount)
        throws IllegalArgumentException
    {
        if(maxParticleCount < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NEG_PARTICLE_COUNT_PROP);
            throw new IllegalArgumentException(msg);
        }

        systemName = name;
        particleFunctions = new ArrayList<ParticleFunction>(NUM_INIT_FUNCTIONS);
        particleList = new ParticleList();
        deadParticles = new Particle[maxParticleCount];

        this.maxParticleCount = maxParticleCount;
        numDeadParticles = 0;
        frameTime = -1;
        createParticles = true;
        zeroFrame = false;
        genTexCoords = true;

        numActiveFunctions = 0;
        activeFunctions = new ParticleFunction[NUM_INIT_FUNCTIONS];
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the object so that any errors generated
     * by the object can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Run the initial particle setup for the first frame now. This should be
     * called just after completing all the setup for the other parts of the system
     * but before the first update driven by the scene graph.
     *
     * @param time The time to start executing for
     */
    public void initialize(long time)
    {
        timeNow = time;

        if(particleInitializer == null)
            return;

        if(createParticles)
            createNewParticles();

        updateGeometry();
    }

    /**
     * Notification that this particle system has been removed from the scene
     * graph and it cleanup anything needed right now.
     */
    public abstract void onRemove();

    /**
     * Update the arrays for the geometry object.
     */
    protected abstract void updateGeometry();

    /**
     * Change the state about whether new particles should be created from
     * this point onwards. The particle system will continue to run, but no
     * new particles would be created if this state is set to false.
     *
     * @param state true to enable particle creation, false to stop
     */
    public void enableParticleCreation(boolean state)
    {
        createParticles = state;
    }

    /**
     * Fetch the current particle creation state.
     *
     * @return true if new particles are being created
     */
    public boolean isParticleCreationEnabled()
    {
        return createParticles;
    }

    /**
     * Set the emitter used to initialise particles.
     *
     * @param emitter the ParticleInitializer instance to use
     */
    public void setParticleInitializer(ParticleInitializer emitter)
    {
        particleInitializer = emitter;
    }

    /**
     * Fetch the currently set initializer for particles.
     *
     * @return The initializer or null if not set
     */
    public ParticleInitializer getParticleInitializer()
    {
        return particleInitializer;
    }

    /**
     * Append a new particle function to the list. This is placed on the end
     * of the evaluation list. A null parameter value will be ignored. A
     * function may be added more than once.
     *
     * @param function The function to add
     */
    public void addParticleFunction(ParticleFunction function)
    {
        if(function != null)
            particleFunctions.add(function);

        if(activeFunctions.length < particleFunctions.size())
            activeFunctions = new ParticleFunction[particleFunctions.size()];
    }

    /**
     * Insert a particle function at a specific place in the list. All others
     * are shifted up one place. Inserting at a position above the last item
     * in the list will act as an append. A null parameter value will be ignored.
     *
     * @param index The position in the list to insert it on
     * @param function The function to be added
     */
    public void insertParticleFunction(int index, ParticleFunction function)
    {
        if(function != null)
        {
            int num_functions = particleFunctions.size();

            if(index > num_functions)
                particleFunctions.add(function);
            else
                particleFunctions.add(index, function);

            if(activeFunctions.length < num_functions)
                activeFunctions = new ParticleFunction[num_functions];
        }
    }

    /**
     * Remove the first instance of the function defined. If there are more
     * than one copy inserted, only the first instance found from the front
     * of the list will be deleted. If the function is not in the list, then
     * the request is silently ignored.
     *
     * @param function The function instance to remove
     */
    public void removeParticleFunction(ParticleFunction function)
    {
        particleFunctions.remove(function);
    }

    /**
     * Set a flag to say whether texture coordinates should be generated for
     * this system. If there is no interpolator set, or other reason, such as
     * automatically generated texture coordinates, then this flag can be used
     * to disable texture coordinates. By default they are generated for
     * geometry types that can make use of them (quads, triangles etc).
     *
     * @param state True to enable tex Coord generation
     */
    public void enableTextureCoordinates(boolean state)
    {
        genTexCoords = state;
    }

    /**
     * Get the current value of the texture coordinate generation state.
     *
     * @return True if texture coordinates are currently being generated.
     */
    public boolean isTextureCoordinateEnabled()
    {
        return genTexCoords;
    }

    /**
     * Set the keys and texture coordinates to use for geometry that wishes to
     * change the texture coordinates over the lifetime of the particle. This
     * technique is used by particle systems that put a collection of different
     * images into a single texture and then use the texture coordinates to
     * change the visual appearance over time. Interpolation is based on the
     * particle's lifetime from birth and uses a stepwise set of changes. At
     * the time the particle's life is old enough to move to the next time
     * key, the texture coordinates are completely changed over.
     * <p>
     *
     * It is up to the caller to determine both the ordering and the number
     * of texture coordinate values to use.
     * <p>
     *
     * Makes an internal copy of the values.
     *
     * @param times The list of time keys to use in milliseconds
     * @param numEntries The number of keys/keyValue pairs
     * @param texCoords The raw texture coordinates
     */
    public void setTexCoordFunction(float[] times,
                                    int numEntries,
                                    float[] texCoords)
    {
        if(numEntries == 0)
            texCoordInterp = null;
        else
        {
            if(texCoordInterp == null)
                texCoordInterp =
                    new TexCoordInterpolator(coordinatesPerParticle());

            texCoordInterp.setupInterpolants(times, numEntries, texCoords);
        }
    }

    /**
     * Inform each of the ParticleFunctions so they can do any processing.
     *
     * @param timestamp The time for this frame
     * @return true if the system is currently running
     */
    public boolean update(long timestamp)
    {
        if(particleInitializer == null)
            return true;

        // Work out the delta for frame times. If, due to win32 stupid timer
        // we end up with 0 time between frames, then just bump it a little so
        // that the thing doesn't have a heart attack with zero times.
        frameTime = (int)(timestamp - timeNow);
        if(frameTime == 0)
        {
            frameTime = 1;
            zeroFrame = true;
        }
        else if(zeroFrame)
        {
            zeroFrame = false;
            frameTime--;
        }

        timeNow = timestamp;

        if(createParticles)
            createNewParticles();

        updateParticleFunctions(frameTime);
        runParticleFunctions();
        updateGeometry();

        return true;
    }

    /**
     * Gets the currently set systemName.
     *
     * @return String representing the current system name
     */
    public String getSystemName()
    {
        return systemName;
    }

    /**
     * Sets the system name to a new value. Should not be null.
     *
     * @param systemName The new name to set
     */
    public void setSystemName(String systemName)
    {
        this.systemName = systemName;
    }

    /**
     * Change the maximum number of particles that can be generated. If the
     * number is greater than the currently set value, it will permit more to
     * be made according to the normal creation speed. If the number is less
     * than the current amount, then no new particles will be created until the
     * current total has died down below the new maximum value.
     *
     * @param maxCount The new maximum particle count to use
     * @throws IllegalArgumentException The particle count was negative
     */
    public void setMaxParticleCount(int maxCount)
        throws IllegalArgumentException
    {
        if(maxCount < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NEG_PARTICLE_COUNT_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(maxCount > deadParticles.length)
        {
            Particle[] tmp = new Particle[maxCount];
            System.arraycopy(deadParticles, 0, tmp, 0, numDeadParticles);
            deadParticles = tmp;
        }

        maxParticleCount = maxCount;

        if(particleInitializer != null)
            particleInitializer.setMaxParticleCount(maxCount);
    }

    /**
     * Get the current maximum number of particles that should be created. Value
     * will always be non-negative.
     *
     * @return The maximum number of particles currently permitted
     */
    public int getMaxParticleCount()
    {
        return maxParticleCount;
    }

    /**
     * Inform all the particle functions that a new frame has started and that they
     * should do any common initialization work now.
     *
     * @param deltaT The elapsed time in milliseconds since the last frame
     * @return true if this should force another update after this one
     */
    private void updateParticleFunctions(int deltaT)
    {
        ParticleFunction function;
        int func_idx = 0;

        int size = particleFunctions.size();
        for(int n = 0; n < size; n++)
        {
            function = (ParticleFunction)particleFunctions.get(n);
            if(function.isEnabled())
            {
                activeFunctions[func_idx++] = function;
                function.newFrame(deltaT);
            }
        }

        numActiveFunctions = func_idx;
    }

    /**
     * Evaluate the particle functions now.
     */
    private void runParticleFunctions()
    {
        // Quick exit if there's nothing to be done
        if(particleFunctions.size() == 0 || particleList.size() == 0)
            return;

        int num_functions = particleFunctions.size();
        ParticleFunction function;
        int num_particles = particleList.size();

        for(int j = 0; j < num_particles; j++)
        {
            Particle p = particleList.next();
            p.resultantForce.set(0, 0, 0);

            for(int i = 0; i < numActiveFunctions; i++)
            {
                if(!activeFunctions[i].apply(p))
                {
                    particleList.remove();
                    releaseParticle(p);
                    particleCount--;
                    break;
                }
            }
        }

        particleList.reset();
    }

    /**
     * Create particles for this frame.
     */
    private void createNewParticles()
    {
        int max_avail = maxParticleCount - particleCount;
        int requested = particleInitializer.numParticlesToCreate(frameTime) +
                        creationResidue;

        int needed = 0;

        if(max_avail < requested)
        {
            needed = max_avail;
            creationResidue = requested - max_avail;
        }
        else
        {
            needed = requested;
            creationResidue = 0;
        }

        for(int n = 0; n < needed; n++)
        {
            Particle particle = fetchParticle();

            particle.wallClockBirth = timeNow;
            particleInitializer.initialize(particle);

            particleList.add(particle);
        }

        particleCount += needed;
    }

    /**
     * Fetch a new particle instance. The instance will be fetched from the cache if
     * available, otherwise a new one will be created.
     *
     * @return A new particle instance
     */
    private Particle fetchParticle()
    {
        Particle ret_val;

        if(numDeadParticles != 0)
        {
             ret_val  = deadParticles[numDeadParticles - 1];
             deadParticles[numDeadParticles - 1] = null;
             numDeadParticles--;
        }
        else
             ret_val = createParticle();

        return ret_val;
    }

    /**
     * Hand a particle instance back to the cache because it is now dead.
     *
     * @param p The particle instance to put back into the cache
     */
    private void releaseParticle(Particle p)
    {
        deadParticles[numDeadParticles] = p;
        numDeadParticles++;
    }
}
