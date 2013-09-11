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

import org.j3d.maths.vector.Point3d;
import org.j3d.maths.vector.Vector3d;

/**
 * An abstract Particle that defines some physical properties and life-cycle
 * properties.
 * <p>
 *
 * This class is subclassed for specific types of particle that have a means of
 * representing themselves. This class contains some fields commonly used to
 * implement physics based particle systems, such as force, energy, surface
 * area as well as a total age and a cyclable age.
 * <p>
 *
 * Particles start with all settings as zero, except the dimensions, which default
 * to 0.2.
 *
 *
 * @author Justin Couch, Daniel Selman
 * @version $Revision: 2.0 $
 */
public abstract class Particle
{
    /** a resettable cyclable age */
    protected int cycleAge;

    /** The age of the particle in wall clock time (milliseconds), in this cycle */
    protected long wallClockBirth;

    /** The end time in wall clock time (milliseconds) */
    protected long wallClockLife;

    /** The maximum lifetime of this particle in milliseconds */
    protected int particleLife;

    /** Surface area of the particle in square meters */
    protected float surfaceArea;

    /** currently unused and undefined */
    protected float energy;

    /** Mass of the object in kilograms */
    protected float mass;

    /** Force applied to the particle in newtons */
    protected Vector3d resultantForce;

    /** Current velocity in  meters per second */
    protected Vector3d velocity;

    /** Current position of the particle */
    protected Point3d position;

    /** bounding box for the particle */
    protected float[] boundingBox;

    /** Red component of the color of the particle */
    protected float red;

    /** Green component of the color of the particle */
    protected float green;

    /** Blue component of the color of the particle */
    protected float blue;

    /** Red component of the color of the particle */
    protected float alpha;

    /** width of the particle in meters */
    protected float width;

    /** height of the particle */
    protected float height;

    /** depth of the particle */
    protected float depth;

    /**
     * Construct a new particle instance. The dimensions default to
     * 0.2 but no surface area or mass.
     */
    public Particle()
    {
        surfaceArea = 0;
        energy = 0;
        mass = 0;
        cycleAge = 0;
        resultantForce = new Vector3d();
        velocity = new Vector3d();
        position = new Point3d();

        width = 0.2f;
        height = 0.2f;
        depth = 0.2f;
    }

    /**
     * Retrieve the current position of this particle.
     *
     * @param val An array of length 3 to copy the values to
     */
    public void getPosition(float[] val)
    {
        val[0] = (float)position.x;
        val[1] = (float)position.y;
        val[2] = (float)position.z;
    }

    /**
     * Set the new position of this particle.
     *
     * @param x The x coordinate of the new position
     * @param y The y coordinate of the new position
     * @param z The z coordinate of the new position
     */
    public void setPosition(float x, float y, float z)
    {
        position.set(x, y, z);
    }

    /**
     * Retrieve the current colour that this particle has.
     *
     * @param val An array of length 4 to copy the values to
     */
    public void getColor(float[] val)
    {
        val[0] = red;
        val[1] = green;
        val[2] = blue;
        val[3] = alpha;
    }

    /**
     * Set this particle to a new colour.
     *
     * @param r The red component of the colour
     * @param g The green component of the colour
     * @param b The blue component of the colour
     * @param alpha The alpha component of the colour
     */
    public void setColor(float r, float g, float b, float alpha)
    {
        red = r;
        green = g;
        blue = b;
        this.alpha = alpha;
    }

    /**
     * Retrieve the bounds of this particle in local coordinate space.
     * They are widht, height, depth in order, first for lower bounds and upper
     * bounds
     *
     * @return The array of bounding information
     */
    public float[] getBounds()
    {
        // we don't expect this to be called all that often so don't initialize
        // like all the others in the constructor. Do it on-demand.
        if(boundingBox == null)
            boundingBox = new float[6];

        boundingBox[0] = (float)(position.x - width);
        boundingBox[1] = (float)(position.y - height);
        boundingBox[2] = (float)(position.z - depth);

        boundingBox[0] = (float)(position.x + width);
        boundingBox[1] = (float)(position.y + height);
        boundingBox[2] = (float)(position.z + depth);

        return boundingBox;
    }

    /**
     * Increment the cycle age.
     */
    public void incAge()
    {
        cycleAge += 1;
    }

    /**
     * Get the set maximum cycle age.
     *
     * @return The cycle age in frames
     */
    public final int getCycleAge()
    {
        return cycleAge;
    }

    /**
     * Set the cycle age to a new value.
     *
     * @param age The new age to set
     */
    public void setCycleAge(int age)
    {
        cycleAge = age;
    }

    /**
     * Get the set maximum cycle time.
     *
     * @return The cycle time in frames
     */
    public final int getCycleTime()
    {
        return particleLife;
    }

    /**
     * Get the wall clock time that this particle was born.
     *
     * @return The birth time in milliseconds
     */
    public final long getBirthTime()
    {
        return wallClockBirth;
    }

    /**
     * Set the cycle time to a new value.
     *
     * @param time The new time to set
     */
    public void setCycleTime(int time)
    {
        particleLife = time;
        wallClockLife = wallClockBirth + time;
    }

    /**
     * Gets the mass of this particle.
     *
     * @return Returns a double
     */
    public final float getMass()
    {
        return mass;
    }

    /**
     * Sets the mass of this particle.
     *
     * @param mass The mass to set
     */
    public void setMass(float mass)
    {
        this.mass = mass;
    }

    /**
     * Gets the resultantForce applied to a particle.
     *
     * @return Returns a Vector3d
     */
    public Vector3d getResultantForce()
    {
        return resultantForce;
    }

    /**
     * Sets the resultantForce applied to a particle.
     *
     * @param force The resultant Force to set
     */
    public void setResultantForce(Vector3d force)
    {
        resultantForce.x = force.x;
        resultantForce.y = force.y;
        resultantForce.z = force.z;
    }

    /**
     * Gets the surfaceArea of the particle.
     *
     * @return Returns a double
     */
    public final float getSurfaceArea()
    {
        return surfaceArea;
    }

    /**
     * Sets the surfaceArea.
     *
     * @param surfaceArea The surfaceArea to set
     */
    public void setSurfaceArea(float surfaceArea)
    {
        this.surfaceArea = surfaceArea;
    }

    /**
     * Gets the current velocity  of the particle.
     *
     * @return Returns a Vector3d
     */
    public Vector3d getVelocity()
    {
        return velocity;
    }

    /**
     * Sets the velocity of the particle.
     *
     * @param v The velocity to set
     */
    public void setVelocity(Vector3d v)
    {
        velocity.x = v.x;
        velocity.y = v.y;
        velocity.z = v.z;
    }

    /**
     * Sets the depth of the particle.
     *
     * @param depth The depth to set
     */
    public void setDepth(float depth)
    {
        this.depth = depth;
    }

    /**
     * Sets the height of the particle.
     *
     * @param height The height to set
     */
    public void setHeight(float height)
    {
        this.height = height;
    }

    /**
     * Sets the width of the particle.
     *
     * @param width The width to set
     */
    public void setWidth(float width)
    {
        this.width = width;
    }

    /**
     * Get the current width of the particle.
     *
     * @return The current width value
     */
    public final float getWidth()
    {
        return width;
    }

    /**
     * Get the current height of the particle.
     *
     * @return The current height value
     */
    public final float getHeight()
    {
        return height;
    }

    /**
     * Get the current depth of the particle.
     *
     * @return The current depth value
     */
    public final float getDepth()
    {
        return depth;
    }
}
