/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * PlanarCollisionParticleFunction TODO - not yet implemented.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class PlanarCollisionParticleFunction implements ParticleFunction
{
    // plane defintion
    Point3d point;
    Vector3d vector;

    public PlanarCollisionParticleFunction( Point3d point, Vector3d vector )
    {
        throw new UnsupportedOperationException();
    }

    public boolean onUpdate( ParticleSystem ps )
    {
       return true;
    }

    public boolean apply( Particle particle )
    {
        // if the particle is going to pass through the plane
        // we are going to add an impulse force to stop it
        // from doing so
        // we assume that the particle is
        return false;
    }
}
