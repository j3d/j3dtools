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
import java.util.ArrayList;
import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCondition;

// Application specific imports
// None

/**
 * The ParticleSystemManager is a Behavior and can be
 * added directly to the scenegraph. It has a List of
 * registered ParticleSystems and calls the update method
 * on each whenever it is triggered.
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public class ParticleSystemManager extends Behavior
{
    private ArrayList particleSystems = new ArrayList();
    private WakeupCondition wakeupCondition;

    public ParticleSystemManager( WakeupCondition wakeupCondition )
    {
        this.wakeupCondition = wakeupCondition;
    }

    public void initialize()
    {
        wakeupOn( wakeupCondition );
    }

    public void processStimulus( java.util.Enumeration criteria )
    {
        update();
        wakeupOn( wakeupCondition );
    }

    public void update()
    {
        for ( int n = particleSystems.size() - 1; n >= 0; n-- )
        {
            ParticleSystem particleSystem = ( ParticleSystem ) particleSystems.get( n );

            if ( particleSystem != null && particleSystem.update() == false )
            {
                // the system is dead, so we can remove it...
                System.out.println( "Removing ParticleSystem: " + particleSystem.getSystemName() );
                particleSystem.onRemove();
                particleSystems.remove( n );
            }
        }
    }

    public void addParticleSystem( ParticleSystem particleSystem )
    {
        particleSystems.add( particleSystem );
    }

    public void removeParticleSystem( ParticleSystem particleSystem )
    {
        particleSystems.remove( particleSystem );
    }
}
