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
import java.util.*;

import javax.media.j3d.*;

import java.io.InputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.vecmath.Color4f;

// Application specific imports
import org.j3d.texture.TextureCacheFactory;

/**
 * Abstract representation of a ParticleSystem.
 *
 * A ParticleSystem manages a List of Particles created by a ParticleFactory.
 * It applies changes to the Particles using a List of ParticleFunctions and a
 * a single emitter.
 * <P>
 * A ParticleSystem can be represented in any way appropriate, the only
 * requirement is that is create a Node to be added to the scenegraph.
 * <p>
 *
 * @author Justin Couch, based on code by Daniel Selman
 * @version $Revision: 1.8 $
 */
public abstract class ParticleSystem implements ParticleFactory
{
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
    private ArrayList particleFunctions;

    /** The cache of currently dead particles */
    private ArrayList deadParticles;

    /** The number of dead particles currently */
    private int numDeadParticles;

    /** List of currently active particle instances */
    protected ParticleList particleList;

    /** The ParticleInitializer for this ParticleSystem. */
    private ParticleInitializer particleInitializer;

    /**
     * Create a new ParticleSystem.
     *
     * @param name An arbitrary string name for ID purposes
     * @param maxParticleCount The maximum number of particles allowed to exist
     */
    public ParticleSystem(String name, int maxParticleCount)
    {
        systemName = name;
        particleFunctions = new ArrayList();
        particleList = new ParticleList();
        deadParticles = new ArrayList(maxParticleCount);

        this.maxParticleCount = maxParticleCount;
        numDeadParticles = 0;
        frameTime = -1;
    }

    /**
     * Run the initial particle setup for the first frame now. This should be
     * called just after completing all the setup for the other parts of the system
     * but before the first update driven by the scene graph.
     */
    public void initialize()
    {

        timeNow = System.currentTimeMillis();

        createParticles();

        updateGeometry();
    }

    /**
     * Get the node instance that represents the particle system in the scene
     * graph.
     *
     * @return The Java3D node instance
     */
    public abstract Node getNode();

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
     * Set the emitter used to initialise particles.
     *
     * @param emitter the ParticleInitializer instance to use
     */
    public void setParticleInitializer(ParticleInitializer emitter)
    {
        this.particleInitializer = emitter;
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
            if(index > particleFunctions.size())
                particleFunctions.add(function);
            else
                particleFunctions.add(index, function);
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
     * Inform each of the ParticleFunctions so they can do any processing.
     *
     * @return true if the system is currently running
     */
    public boolean update()
    {
        if(particleInitializer == null)
            return true;

        // Work out the delta for frame times.
        long cur_time = System.currentTimeMillis();

        frameTime = (int)(cur_time - timeNow);
        timeNow = cur_time;

        updateParticleFunctions();
        runParticleFunctions();
        createParticles();
        updateGeometry();

        return true;
    }

    /**
     * Set the texture instance that this particle should be using. If the
     * particle doesn't need or want a texture, don't call this. Setting a
     * value of null will clear the current texture.
     *
     * @param tex The texture instance to use
     */
    public void setTexture(Texture tex)
    {
    }

    /**
     * Create the appearance used to render the objects with. This appearance
     * should have all appropriate information set - including textures.
     *
     * @return The appearance object to use with this system
     */
    protected Appearance createAppearance()
    {
        Appearance app = new Appearance();

/*
        app.setPolygonAttributes(
                new PolygonAttributes(
                        PolygonAttributes.POLYGON_FILL,
                        PolygonAttributes.CULL_NONE,
                        0));
*/

        TransparencyAttributes trans =
            new TransparencyAttributes(TransparencyAttributes.BLENDED, 0);
        trans.setSrcBlendFunction(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);

        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.MODULATE);
        tex_attr.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);

        app.setTransparencyAttributes(trans);
        app.setTextureAttributes(tex_attr);

//        app.setTexture(getTexture());
        return app;
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
     * Inform all the particle functions that a new frame has started and that they
     * should do any common initialization work now.
     */
    private void updateParticleFunctions()
    {
        ParticleFunction function;

        for (int n = particleFunctions.size() - 1; n >= 0; n--)
        {
            function = (ParticleFunction)particleFunctions.get(n);
            if(function.isEnabled())
                function.newFrame();
        }
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

        for(int i = 0; i < num_functions; i++)
        {
            function = (ParticleFunction)particleFunctions.get(i);

            if(function.isEnabled())
            {
                int num_particles = particleList.size();
                for(int j = 0; j < num_particles; j++)
                {
                    Particle p = particleList.next();
                    if(!function.apply(p))
                    {
                        particleList.remove();
                        releaseParticle(p);
                        particleCount--;
                    }
                }

                particleList.reset();
            }
        }
    }

    /**
     * Create particles for this frame.
     */
    private void createParticles()
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

            particleInitializer.initialize(particle);
            particle.wallClockBirth = timeNow;

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
             ret_val  = (Particle)deadParticles.remove(numDeadParticles - 1);
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
        deadParticles.add(p);
        numDeadParticles++;
    }
}