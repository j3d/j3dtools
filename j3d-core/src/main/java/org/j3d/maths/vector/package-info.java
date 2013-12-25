/*
 * j3d.org Copyright (c) 2001-2013
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

/**
 * Implementation of Java vector mathematical functions. Originally j3d.org used
 * the javax.vecmath code, but that has effectively been abandoned and provides too
 * generic a capability. This keeps the implementation smaller and uses almost
 * directly compatible class definitions.
 * <p/>
 * The SVD decomposition code comes from the NIST Jama public domain reference
 * implementation and modified for our needs. We don't need generic matrix handling
 * as 3D graphics is all 3x3 or 4x4 matrices. Original source can be found here
 * {@link http://math.nist.gov/javanumerics/jama/}
 */
package org.j3d.maths.vector;
