/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spline;

// Standard imports
// none

// Application specific imports
// None

/**
 * Representation of data that represents a single B-Spline curve.
 * <p>
 *
 * The curve data may represent either rational or non-rational forms.
 * If the numWeights value is positive then it will assume a rational
 * curve form, otherwise a non-rational calculation will be used.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BSplineCurveData
{
    /**
     * The control points of the curve described as a flat array of values. The
     * array must be at least 3 * numControlPoints in length.
     */
    public float[] controlPoints;

    /** The number of valid points in the controlPoints array. */
    public int numControlPoints;

    /** The knot values for the curve */
    public float[] knots;

    /** The number of valid knot values in the knots array */
    public int numKnots;

    /** The degree of  the curve */
    public int degree;

    /** The weight values for the curve if rational */
    public float[] weights;

    /** The number of valid weight values in the weights array */
    public int numWeights;

}
