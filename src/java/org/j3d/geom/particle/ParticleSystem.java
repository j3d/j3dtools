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

import com.sun.j3d.utils.image.TextureLoader;

// Application specific imports
import org.j3d.texture.TextureCacheFactory;

/**
 * Abstract ParticleSystem. A ParticleSystem managed a List of Particles
 * created by a ParticleFactory. It applies changes to the Particles using
 * a List of MovementFunctions and a ParticleInitializer.
 * <P>
 * A ParticleSystem can be represented in any way appropriate, the only
 * requirement is that is create a Node to be added to the scenegraph.
 * <p>
 * This class is not multiple thread safe.
 * <p>
 * <p>
 * By default Particles used the file particle-factory.properties (loaded from
 * the current directory) to initialize their physical attributes and images.
 * <p>
 * @author Daniel Selman
 * @version $Revision: 1.4 $
 */
public abstract class ParticleSystem implements ParticleFactory
{
    /** Name of the property file that defines the setup information */
    private static final String PROPERTY_FILE = "particle-factory.properties";

    /**
     * Name of the environment property that holds the texture to use
     * on the particle objects. The value may be either a string, which is a
     * filename, relative to the CLASSPATH, or an instance of a J3D
     * {@link javax.media.j3d.Texture} object.
     */
    public static final String PARTICLE_TEXTURE = "texture";

    /**
     * Identifier for this particle system type.
     */
    private String systemName;

    /**
     * A List of Particle objects under the control of this ParticleSystem.
     */
    protected List particles;

    /**
     * Number of Particles in the ParticleSystem.
     */
    protected int particleCount;

    /**
     * List of ParticleFunctions to be applied to each Particle.
     */
    protected List particleFunctions = new ArrayList();

    /**
     * The ParticleInitializer for this ParticleSystem. ParticleInitializers
     * control how long Particles live and there attributes if they are recycled.
     */
    protected ParticleInitializer particleInitializer;

    /**
     * The ParticleFactory for this ParticleSystem, responsible for
     * creating and initializing Particles.
     */
    protected ParticleFactory particleFactory;

    /**
     * Flag indicating that this ParticleSystem is still running
     * and should not be removed from its driving ParticleSystemManager.
     */
    protected boolean running = false;

    /**
     * The environment entries passed to the system for initialisation.
     */
    protected Map environment;

    /**
     * ResourceBundle used to initialize Particle attributes.
     */
    private static Properties resources;

    /**
     * Do the class initialisation to load the global resources.
     */
    static
    {
        InputStream is = (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return ClassLoader.getSystemResourceAsStream(PROPERTY_FILE);
                }
            }
        );

        resources = new Properties();

        try
        {
            resources.load(is);
            is.close();
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading particle.properties");
        }
    }

    /**
     * Creates a ParticleSystem of the given type, using
     * the supplied initialization environment.
     * @param systemType the numeric identified for this particle system
     * @param environment the initialization environment
     */
    public ParticleSystem( String systemName, Map environment )
    {
        this.systemName = systemName;
        this.environment = new HashMap( environment );
        particleFactory = this;
    }

    /**
     * Creates and initializes the Particles contained within this
     * ParticleSystem. The ParticleFactory and ParticleInitializer
     * are used to create and inititalize the Particles respectively.
     * @param particleInitializer the ParticleInitializer instance to
     * use.
     * @param particleCount the number of Particles to create using the
     * ParticleFactory.
     */
    protected void createParticles( ParticleInitializer particleInitializer,
                                    int particleCount )
    {
        this.particleCount = particleCount;
        this.particleInitializer = particleInitializer;

        particles = new ArrayList();

        for ( int n = 0; n < particleCount; n++ )
        {
            Particle particle = particleFactory.createParticle( environment, systemName, n );
            initParticle( particle );
            particles.add( n, particle );
        }
    }

    /**
     * Replaces the ParticleInitialzer supplied at construction time.
     *
     * @param particleInitializer the ParticleInitializer instance to
     * use.
     */
    public void setParticleInitializer( ParticleInitializer particleInitializer )
    {
        this.particleInitializer = particleInitializer;
    }

    /**
     * Updates a single Particle by applying all ParticleFunctions
     * to the Particle.
     *
     * @param index the Particle index
     * @param particle the Particle to be updated
     * @return true if this Particle requires further updates from the ParticleSystem
     */
    public boolean updateParticle( int index, Particle particle )
    {
        particle.incAge();

        if ( particleInitializer.isAlive( particle ) != false )
        {
            ParticleFunction function = null;

            for ( int n = particleFunctions.size() - 1; n >= 0; n-- )
            {
                function = ( ParticleFunction ) particleFunctions.get( n );
                running |= function.apply( particle );
            }
        }
        else
        {
            particle.setCycleAge( 0 );
            running |= particleInitializer.initialize( particle );
        }

        return running;
    }

    /**
     * Initializes a single Particle using the ParticleInitializer.
     *
     * @param index the Particle index
     * @param particle the Particle to be updated
     * @return true if this Particle requires further updates from the ParticleSystem
     */
    public void initParticle( Particle particle )
    {
        particleInitializer.initialize( particle );
    }

    public void addParticleFunction( ParticleFunction function )
    {
        particleFunctions.add( function );
    }

    /**
     * Inform each of the ParticleFunctions so they can
     * do any processing.
     * @return true if the system is currently running
     */
    public boolean update( )
    {
        ParticleFunction function;

        for ( int n = particleFunctions.size() - 1; n >= 0; n-- )
        {
            function = ( ParticleFunction ) particleFunctions.get( n );
            function.onUpdate( this );
        }

       return true;
    }

    public abstract Node getNode();

    public abstract void onRemove();

    protected void assignAttributes( String name, Particle particle )
    {
        particle.setSurfaceArea( loadDouble( name + ".surface.area" ) );
        particle.setEnergy( loadDouble( name + ".energy" ) );
        particle.setMass( loadDouble( name + ".mass" ) );
        particle.setElectrostaticCharge( loadDouble( name + ".electrostatic.charge" ) );
        particle.setWidth( loadDouble( name + ".width" ) );
        particle.setHeight( loadDouble( name + ".height" ) );
        particle.setDepth( loadDouble( name + ".depth" ) );
        particle.setCollisionForce( loadDouble( name + ".collision.force" ) );
        particle.setCollisionVelocity( loadDouble( name + ".collision.velocity" ) );
        particle.setFrictionForce( loadDouble( name + ".friction.force" ) );
        particle.setFrictionVelocity( loadDouble( name + ".friction.velocity" ) );
    }

    private double loadDouble( String name )
    {
        String value = resources.getProperty( name + ".average" );
        double basis = Double.parseDouble( value );

        value = resources.getProperty( name + ".random" );
        double random = Double.parseDouble( value );

        return Particle.getRandomNumber( basis, random );
    }

    /**
     * Retrieves and optionally loads a texture from the
     * initialization environment. The TextureCache is used
     * to load the texture.
     */
    protected Texture getTexture()
    {
        // load the texture image and assign to the appearance
        Object prop = environment.get( PARTICLE_TEXTURE );
        Texture tex = null;

        if ( prop instanceof String )
        {
            try
            {
                // load the texture from the texture cache
                tex = TextureCacheFactory.getCache().fetchTexture( ( String ) prop );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        else if ( prop instanceof Texture )
        {
            tex = ( Texture ) prop;
        }
        else
        {

            TextureLoader texLoader =
                new TextureLoader( resources.getProperty( systemName + ".texture" ),
                                   Texture.RGBA,
                                   null );
            tex = texLoader.getTexture();
        }

        return tex;
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

        app.setPolygonAttributes(
                new PolygonAttributes(
                        PolygonAttributes.POLYGON_FILL,
                        PolygonAttributes.CULL_NONE,
                        0 ) );

        app.setTransparencyAttributes(
                new TransparencyAttributes( TransparencyAttributes.FASTEST, 0.0f ) );

        app.setTextureAttributes(
                new TextureAttributes(
                        TextureAttributes.MODULATE,
                        new Transform3D(),
                        new Color4f(),
                        TextureAttributes.FASTEST ) );

        app.setTexture( getTexture() );
        return app;
    }

    /**
     * Gets the systemName.
     * @return Returns a String
     */
    public String getSystemName()
    {
        return systemName;
    }

    /**
     * Sets the systemName.
     * @param systemName The systemName to set
     */
    public void setSystemName( String systemName )
    {
        this.systemName = systemName;
    }

}