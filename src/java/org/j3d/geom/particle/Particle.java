/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.util.Map;


/**
 * An abstract Particle that defines some physical properties
 * and life-cycle properties. This class is subclassed for specific
 * types of particle that have a means of representing themselves.
 * This class contains some fields commonly used to implement physics
 * based particle systems, such as force, energy, surface area as well
 * as a total age and a cyclable age.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public abstract class Particle
{
    // total number of iterations this particles has been in existance
    long totalAge = 0;

    // a resettable cyclable age
    int cycleAge = 0;

    // square meters
    double surfaceArea = 0.004;

    // currently unused and undefined
    double energy = 0;

    // kilograms
    double mass = 0.0000001;

    // currently unused and undefined
    double electrostaticCharge = 0;

    double frictionForce = 0.90;
    double frictionVelocity = 0.95;

    double collisionForce = 0.1;
    double collisionVelocity = 0.6;

    // newtons
    Vector3d resultantForce = new Vector3d();

    // meters per second
    Vector3d velocity = new Vector3d();

    // current position of the particle
    Point3d position = new Point3d();

    // previous position of the particle
    Point3d previousPosition = new Point3d();

    // bounding box for the particle
    private BoundingBox boundingBox = new BoundingBox();

    // color of the particle
    protected Color4f color = new Color4f();

    // width of the particle
    protected double width = 0.2;

    // height of the particle
    protected double height = 0.2;

    // depth of the particle
    protected double depth = 0.2;

    // initialization environment
    protected Map environment;

    public static final String RENDER_FROM_PREVIOUS_POSITION = "RENDER_FROM_PREVIOUS_POSITION";
    protected boolean renderFromPreviousPosition;

    public Particle( Map environment )
    {
        this.environment = environment;

        if ( environment != null )
        {
            renderFromPreviousPosition = ( environment.get( RENDER_FROM_PREVIOUS_POSITION ) != null );
        }
    }

    public boolean isRenderFromPreviousPosition()
    {
        return renderFromPreviousPosition;
    }

    public void setPosition( double x, double y, double z )
    {
        previousPosition.set( position );
        position.set( x, y, z );
    }

    public void setPositionAndPrevious( double x, double y, double z )
    {
        position.set( x, y, z );
        previousPosition.set( position );
    }

    public void getPosition( Tuple3d newPosition )
    {
        newPosition.set( this.position );
    }

    public void getPreviousPosition( Tuple3d position )
    {
        position.set( previousPosition );
    }

    public double getPositionX()
    {
        return position.x;
    }

    public double getPositionY()
    {
        return position.y;
    }

    public double getPositionZ()
    {
        return position.z;
    }

    public void getColor( Color4f newColor )
    {
        newColor.set( this.color );
    }

    public float getColorRed()
    {
        return color.x;
    }

    public float getColorGreen()
    {
        return color.y;
    }

    public float getColorBlue()
    {
        return color.z;
    }

    public float getColorAlpha()
    {
        return color.w;
    }

    public static double getRandomNumber( double basis, double random )
    {
        return basis + ( ( float ) Math.random() * random * 2f ) - ( random );
    }

    public void setColor( float r, float g, float b, float alpha )
    {
        color.set( r, g, b, alpha );
    }

    public void setAlpha( float alpha )
    {
        color.w = alpha;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public double getDepth()
    {
        return depth;
    }

    public Bounds getBounds()
    {
        boundingBox.setLower( position.x - width, position.y - height, position.z - depth );
        boundingBox.setUpper( position.x + width, position.y + height, position.z + depth );

        return boundingBox;
    }

    // increments both the total and cycle ages
    public void incAge()
    {
        totalAge += 1;
        cycleAge += 1;
    }

    // return the total age
    public long getTotalAge()
    {
        return totalAge;
    }

    // return the cycle age
    public int getCycleAge()
    {
        return cycleAge;
    }

    // set the cycle age
    public void setCycleAge( int cycleAge )
    {
        this.cycleAge = cycleAge;
    }

    /**
     * Gets the mass.
     * @return Returns a double
     */
    public double getMass()
    {
        return mass;
    }

    /**
     * Sets the mass.
     * @param mass The mass to set
     */
    public void setMass( double mass )
    {
        this.mass = mass;
    }

    /**
     * Gets the electrostaticCharge.
     * @return Returns a double
     */
    public double getElectrostaticCharge()
    {
        return electrostaticCharge;
    }

    /**
     * Sets the electrostaticCharge.
     * @param electrostaticCharge The electrostaticCharge to set
     */
    public void setElectrostaticCharge( double electrostaticCharge )
    {
        this.electrostaticCharge = electrostaticCharge;
    }

    /**
     * Gets the energy.
     * @return Returns a double
     */
    public double getEnergy()
    {
        return energy;
    }

    /**
     * Sets the energy.
     * @param energy The energy to set
     */
    public void setEnergy( double energy )
    {
        this.energy = energy;
    }

    /**
     * Gets the resultantForce.
     * @return Returns a Vector3d
     */
    public Vector3d getResultantForce()
    {
        return resultantForce;
    }

    /**
     * Sets the resultantForce.
     * @param resultantForce The resultantForce to set
     */
    public void setResultantForce( Vector3d resultantForce )
    {
        this.resultantForce = resultantForce;
    }

    /**
     * Gets the surfaceArea.
     * @return Returns a double
     */
    public double getSurfaceArea()
    {
        return surfaceArea;
    }

    /**
     * Sets the surfaceArea.
     * @param surfaceArea The surfaceArea to set
     */
    public void setSurfaceArea( double surfaceArea )
    {
        this.surfaceArea = surfaceArea;
    }

    /**
     * Gets the velocity.
     * @return Returns a Vector3d
     */
    public Vector3d getVelocity()
    {
        return velocity;
    }

    /**
     * Sets the velocity.
     * @param velocity The velocity to set
     */
    public void setVelocity( Vector3d velocity )
    {
        this.velocity = velocity;
    }

    /**
     * Sets the depth.
     * @param depth The depth to set
     */
    public void setDepth( double depth )
    {
        this.depth = depth;
    }

    /**
     * Sets the height.
     * @param height The height to set
     */
    public void setHeight( double height )
    {
        this.height = height;
    }

    /**
     * Sets the width.
     * @param width The width to set
     */
    public void setWidth( double width )
    {
        this.width = width;
    }

    /**
     * Gets the rictionForce.
     * @return Returns a double
     */
    public double getRictionForce()
    {
        return frictionForce;
    }

    /**
     * Sets the rictionForce.
     * @param rictionForce The rictionForce to set
     */
    public void setFrictionForce( double rictionForce )
    {
        frictionForce = rictionForce;
    }

    /**
     * Gets the rictionVelocity.
     * @return Returns a double
     */
    public double getFrictionVelocity()
    {
        return frictionVelocity;
    }

    /**
     * Sets the rictionVelocity.
     * @param rictionVelocity The rictionVelocity to set
     */
    public void setFrictionVelocity( double rictionVelocity )
    {
        frictionVelocity = rictionVelocity;
    }

    /**
     * Gets the collisionForce.
     * @return Returns a double
     */
    public double getCollisionForce()
    {
        return collisionForce;
    }

    /**
     * Sets the collisionForce.
     * @param collisionForce The collisionForce to set
     */
    public void setCollisionForce( double collisionForce )
    {
        this.collisionForce = collisionForce;
    }

    /**
     * Gets the collisionVelocity.
     * @return Returns a double
     */
    public double getCollisionVelocity()
    {
        return collisionVelocity;
    }

    /**
     * Sets the collisionVelocity.
     * @param collisionVelocity The collisionVelocity to set
     */
    public void setCollisionVelocity( double collisionVelocity )
    {
        this.collisionVelocity = collisionVelocity;
    }

}
