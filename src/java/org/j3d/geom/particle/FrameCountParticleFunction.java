/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;


/**
 * Simple ParticleFunction that causes a ParticleSystem
 * to run for a fixed number of frames.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class FrameCountParticleFunction implements ParticleFunction
{
    private int maxAge;
    private int frameCount = 0;

    public FrameCountParticleFunction( int maxAge )
    {
        this.maxAge = maxAge;
    }

    public boolean onUpdate( ParticleSystem ps )
    {
       frameCount++;
       return true;
    }

    public boolean apply( Particle particle )
    {
        return( frameCount < maxAge );
    }
}