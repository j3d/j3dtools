/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import java.util.Map;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;

import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;


/**
 * An abstract Particle that defines some physical properties and life-cycle
 * properties.
 * <p>
 * This class is subclassed for specific types of particle that have a means of
 * representing themselves. This class contains some fields commonly used to
 * implement physics based particle systems, such as force, energy, surface
 * area as well as a total age and a cyclable age.
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public abstract class Particle
{
    public static final String RENDER_FROM_PREVIOUS_POSITION = "RENDER_FROM_PREVIOUS_POSITION";

    /** a resettable cyclable age */
    protected int cycleAge = 0;

    /** The age of the particle in wall clock time (milliseconds), in this cycle */
    protected long wallClockBirth;

    /** The maximum lifetime of this partical in milliseconds */
    protected int wallClockLife;

    /** Surface area of the particle in square meters */
    protected double surfaceArea;

    /** currently unused and undefined */
    protected double energy;

    /** Mass of the object in kilograms */
    protected double mass;

    /** Force applied to the particle in newtons */
    protected Vector3d resultantForce;

    /** Current velocity in  meters per second */
    protected Vector3d velocity;

    /** Current position of the particle */
    protected Point3f position;

    /** Previous position of the particle */
    protected Point3f previousPosition;

    /** bounding box for the particle */
    protected BoundingBox boundingBox;

    /** color of the particle */
    protected Color4f color;

    /** width of the particle in meters */
    protected float width;

    /** height of the particle */
    protected float height;

    /** depth of the particle */
    protected float depth;

    protected boolean renderFromPreviousPosition;

    /**
     * Construct a new particle instance.
     *
     * @param relative true if the position is relative
     */
    public Particle(boolean relative)
    {
        renderFromPreviousPosition = relative;

        surfaceArea = 0.0004;
        energy = 0;
        mass = 0.0000001;
        resultantForce = new Vector3d();
        velocity = new Vector3d();
        position = new Point3f();
        previousPosition = new Point3f();
        color = new Color4f();

        width = 0.2f;
        height = 0.2f;
        depth = 0.2f;
    }

    public boolean isRenderFromPreviousPosition()
    {
        return renderFromPreviousPosition;
    }

    public void setPosition(float x, float y, float z)
    {
        previousPosition.set(position);
        position.set(x, y, z);
    }

    public void setPositionAndPrevious(float x, float y, float z)
    {
        position.set(x, y, z);
        previousPosition.set(position);
    }

    public void getPosition(Tuple3d newPosition)
    {
        newPosition.set(this.position);
    }

    public void getPreviousPosition(Tuple3d position)
    {
        position.set(previousPosition);
    }

    public float getPositionX()
    {
        return position.x;
    }

    public float getPositionY()
    {
        return position.y;
    }

    public float getPositionZ()
    {
        return position.z;
    }

    public void getColor(Color4f newColor)
    {
        newColor.set(this.color);
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

    public void setColor(float r, float g, float b, float alpha)
    {
        color.set(r, g, b, alpha);
    }

    public void setAlpha(float alpha)
    {
        color.w = alpha;
    }


    public Bounds getBounds()
    {
        // we don't expect this to be called all that often so don't initialize
        // like all the others in the constructor. Do it on-demand.
        if(boundingBox == null)
            boundingBox = new BoundingBox();

        boundingBox.setLower(position.x - width,
                             position.y - height,
                             position.z - depth);

        boundingBox.setUpper(position.x + width,
                             position.y + height,
                             position.z + depth);

        return boundingBox;
    }

    /**
     * Increment the cycle age
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
    public int getCycleAge()
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
    public int getCycleTime()
    {
        return wallClockLife;
    }

    /**
     * Set the cycle time to a new value.
     *
     * @param time The new time to set
     */
    public void setCycleTime(int time)
    {
        wallClockLife = time;
    }

    /**
     * Gets the mass of this particle.
     *
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
    public void setMass(double mass)
    {
        this.mass = mass;
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
    public void setResultantForce(Vector3d resultantForce)
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
    public void setSurfaceArea(double surfaceArea)
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
    public void setVelocity(Vector3d velocity)
    {
        this.velocity = velocity;
    }

    /**
     * Sets the depth.
     * @param depth The depth to set
     */
    public void setDepth(float depth)
    {
        this.depth = depth;
    }

    /**
     * Sets the height.
     * @param height The height to set
     */
    public void setHeight(float height)
    {
        this.height = height;
    }

    /**
     * Sets the width.
     * @param width The width to set
     */
    public void setWidth(float width)
    {
        this.width = width;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    public float getDepth()
    {
        return depth;
    }
}
