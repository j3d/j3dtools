/*****************************************************************************
 *                        j3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.geom;

// External imports
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

// Local imports
import org.j3d.util.CharHashMap;

/**
 * Representation of font information needed to create polygonal text.
 * <p>
 *
 * This class serves as a common representation of font information and as a
 * cache for creating individual character polygonal information for a specific
 * font. The role of this class is to create a set of polygonal representations
 * of individual characters, which are then expressed as instances of
 * {@link CharacterData}. It does not perform any placement of those
 * characters in any spatial position. That is a requirement of the caller to
 * perform the correct relative placement of characters.
 * <p>
 *
 * Characters are generated in the AWT coordinate system. That means that text
 * is invariably drawn upside down in the Y axis. Before using the text, you
 * may need to perform a negative scale about the Y axis.
 * <p>
 *
 * <b>Note:</b> Because this class works at the individual character level,
 * it doesn't render very well fonts that need to be connected per-character
 * such as arabic etc.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class CharacterCreator
{
    /** The fixed face normal for all characters */
    private static final float[] FACE_NORMAL = { 0, 0, -1 };

    /** The font that this object will be using */
    private final Font font;

    /** The flatness of the tesselation. */
    private final double flatness;

    /** Triangulator used to create triangles from the font outline */
    private TriangulationUtils triangulator;

    /** Internal render context to generate new glyphs with */
    private FontRenderContext fontContext;

    /** Stored collection of characters we already have */
    private CharHashMap charDataMap;

    /** Convenience array used to fetch coordinates from the PathIterator */
    private float[] newCoords;

    /** Array to collect all the coords for a character */
    private float[] charCoords;

    /** Array to collect all the coord index lists for a character */
    private int[] charIndex;

    /** Place to put the triangluated index value output */
    private int[] triOutputIndex;

    /** Array used to pass a character to the glyph creation routines */
    private char[] sourceChar;

    /**
     * Create a new fontstyle object representing the given font.
     *
     * @param font The font object to use
     * @param flatness How closely the points should resemble the defined
     *   outlines for the font
     */
    public CharacterCreator(Font font, double flatness)
    {
        this.font = font;
        this.flatness = flatness;

        // Set it up using default transform, antialiased and metrics
        fontContext = new FontRenderContext(null, true, true);
        charDataMap = new CharHashMap();
        newCoords = new float[6];
        charCoords = new float[1024];
        charIndex = new int[512];
        triOutputIndex = new int[512]; // arbitrary large number
        triangulator = new TriangulationUtils();
        sourceChar = new char[1];
    }

    /**
     * Get the Java 2D Font used to create this FontStyle object.
     *
     * @return The Font object used to render this text
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * Get the Java 2D FontRenderContext used to create this FontStyle object.
     *
     * @return The Font object used to render this text
     */
    public FontRenderContext getFontRenderContext()
    {
        return fontContext;
    }

    /**
     * Get the flatness value used to create the internal glyphs.
     *
     * @return A value greater than zero
     */
    public double getFlatness()
    {
        return flatness;
    }

    /**
     * From the provided font, generate the output triangles now.
     *
     * @param characters The string of characters to append to the list
     * @param numChars The number of valid characters in the array
     * @param output A place to put each of the character arrays
     */
    public void createCharacterTriangles(char[] characters,
                                         int numChars,
                                         ArrayList output)
    {
        // first find out what we don't have characters for:
        int needed_char_cnt = 0;
        for(int i = 0; i < numChars; i++)
            if(!charDataMap.containsKey(characters[i]))
                needed_char_cnt++;

        // Do we need to create any more?
        if(needed_char_cnt != 0)
        {
            needed_char_cnt = 0;

            for(int i = 0; i < numChars; i++)
                if(!charDataMap.containsKey(characters[i]))
                    createNewGlyph(characters[i]);
        }


        for(int i = 0; i < numChars; i++)
            output.add(charDataMap.get(characters[i]));
    }

    /**
     * Convenience method that goes through a list of characters and creates
     * the needed glyph information.
     *
     * @param characters List of characters that need to be created.
     */
    private void createNewGlyph(char character)
    {
        sourceChar[0] = character;
        GlyphVector glyph_vec =
            font.createGlyphVector(fontContext, sourceChar);

//java.awt.font.LineMetrics metrics = font.getLineMetrics(sourceChar, 0, 1, fontContext);

        // Font Y-axis is downwards, so create an affine transform to flip it.
        Rectangle2D v_bounds = glyph_vec.getVisualBounds();
        double tx = v_bounds.getX() + 0.5 * v_bounds.getWidth();
        double ty = v_bounds.getY() + 0.5 * v_bounds.getHeight();

        AffineTransform neg_trans = new AffineTransform();
        neg_trans.setToTranslation(-tx, -ty);
        neg_trans.scale(1.0, -1.0);
        neg_trans.translate(tx, -ty);

        CharacterData ch_data = new CharacterData();
        Rectangle2D l_bounds = glyph_vec.getLogicalBounds();

/*
System.out.println("char \'" + character + "\'");
System.out.println("Logical bounds " + l_bounds);
System.out.println("visual bounds " + v_bounds);
System.out.println(" offset " +
                   metrics.getBaselineOffsets()[0] + ", " +
                   metrics.getBaselineOffsets()[1] + " desc " +
                   metrics.getHeight() + " a " +
                   metrics.getDescent() + " h " +
                   metrics.getAscent());
*/

//        float scale = 1 / (float)(metrics.getAscent() + metrics.getDescent());

        float scale = 1 / (float)l_bounds.getHeight();
//System.out.println("scale " + scale);

        ch_data.bounds =
            new Rectangle2D.Float((float)l_bounds.getX() * scale,
                                  (float)l_bounds.getY() * scale,
                                  (float)l_bounds.getWidth() * scale,
                                  1);

        ch_data.scale = scale;

        if(Character.isWhitespace(character))
        {
            ch_data.numIndex = 0;
            charDataMap.put(character, ch_data);
            return;
        }

        Shape glyph_shape = glyph_vec.getOutline();
        PathIterator glyph_path =
            glyph_shape.getPathIterator(neg_trans, flatness / scale);
        int num_triangles;

//System.out.println("final ascent " + ch_data.ascent);

        int vtx_count = 0;
        int start_vtx = 0;
        int total_coords = 0;
        int total_index = 0;
        boolean new_curve = true;
        int second_curve_start = 0;
        int closest_point = 0;

//System.out.println("processing " + character + " flatness " + (flatness / scale));

        while(!glyph_path.isDone())
        {
            switch(glyph_path.currentSegment(newCoords))
            {
                case PathIterator.SEG_MOVETO:
                    // end of one outline, move to the next. So,
                    // let's close the polygon and triangulate it
                    // before moving onto the next.

                    // Java can sometimes issue a CLOSE before doing a
                    // MOVETO. We want to check for that and make sure
                    // we don't unnecessarily do a double
                    // triangulation.
                    if(charCoords.length < total_coords + 2)
                    {
                        float[] tmp = new float[total_coords + 256];
                        System.arraycopy(charCoords, 0, tmp, 0, total_coords);
                        charCoords = tmp;
                    }

                    if(total_coords == 0)
                    {
                        charCoords[total_coords++] = newCoords[0] * scale;
                        charCoords[total_coords++] = newCoords[1] * scale;
                        charCoords[total_coords++] = 0;
                        vtx_count = 1;
// System.out.println("start " + newCoords[0] + " " + newCoords[1] );
                    }
                    else
                    {
                        // If we are inside the last polygon then we want to
                        // do some special treatment to it. Because the
                        // tesselator cannot handle polygons with holes, we
                        // end up modifying the curve by finding the closest
                        // point to the new point from the existing current
                        // curve and inserting the entire new curve there,
                        // then finish off the curve by going back to the
                        // original curve.
                        // If this point is not inside the current curve,
                        // then tesselate the curve and start a new one.
                        //
                        // Note, does not currently handle the case of
                        // testing all the previous polygons to see if this
                        // point is inside it. I suspect we'll never see that
                        // case, but just making note of it here just in case.

                        // First, do we have an existing internal curve that
                        // needs to be inserted?
                        if(second_curve_start > start_vtx)
                        {
                            int lower = second_curve_start - closest_point;

                            // Shift the bit between the insert position and
                            // the start of the second curve to the end past
                            // the second curve. Since we also want to
                            // duplicate closest point on the first curve for
                            // the insertion process, make sure we add an extra
                            // value.
                            System.arraycopy(charCoords,
                                             closest_point,
                                             charCoords,
                                             total_coords,
                                             lower);

                            // Shift the whole lot down to the insert position
                            System.arraycopy(charCoords,
                                             second_curve_start,
                                             charCoords,
                                             closest_point + 3,
                                             total_coords -
                                              second_curve_start + lower + 3);

//System.out.println("adjusting curve: closest " + closest_point +
//                   " total " + total_coords + " amt " + lower);
//System.out.println("2nd copy " + second_curve_start +
//                   " to " + (closest_point + 3) +
//                   " amt " + (total_coords - second_curve_start + lower + 3));
                            // Adjust for the extra replicated coordinate
                            total_coords += 3;
                            vtx_count++;

                            // Set second curve loc back to start position so
                            // that next time around we don't trigger this
                            // condition unless we have to.
                            second_curve_start = start_vtx;
                        }

                        if(isInsideEvenOdd(newCoords[0] * scale,
                                           newCoords[1] * scale,
                                           charCoords,
                                           start_vtx,
                                           vtx_count))
                        {
                            // do we have an old internal curve that needs clean
                            second_curve_start = total_coords;

                            // go find the closest point that we want to
                            // insert this new curve relative to.
                            closest_point =
                                findClosestPoint(newCoords[0] * scale,
                                                 newCoords[1] * scale,
                                                 charCoords,
                                                 start_vtx,
                                                 vtx_count);

//System.out.println("inside curve at " + vtx_count + " closest " + closest_point);
//System.out.println("pt   " + total_coords + " : " + newCoords[0] + " " + newCoords[1]);

                            charCoords[total_coords++] = newCoords[0] * scale;
                            charCoords[total_coords++] = newCoords[1] * scale;
                            charCoords[total_coords++] = 0;
                            vtx_count++;
                        }
                        else
                        {
//System.out.println("new curve at " + vtx_count);
//System.out.println("pt   " + total_coords + " : " + newCoords[0] + " " + newCoords[1]);

                            if(triOutputIndex.length < vtx_count * 3)
                                triOutputIndex = new int[vtx_count * 3];

                            num_triangles =
                                triangulator.triangulateConcavePolygon(charCoords,
                                                                       start_vtx,
                                                                       vtx_count,
                                                                       triOutputIndex,
                                                                       FACE_NORMAL);

//System.out.println("old num tri " + num_triangles + " " + vtx_count + " " + start_vtx);
                            if(num_triangles > 0)
                            {
                                num_triangles *= 3;
                                for(int i = 0; i < num_triangles; i++)
                                    triOutputIndex[i] /= 3;

                                if(charIndex.length < total_index + num_triangles)
                                {
                                    int[] tmp =
                                        new int[total_index + num_triangles];
                                    System.arraycopy(charIndex,
                                                     0,
                                                     tmp,
                                                     0,
                                                     total_index);
                                    charIndex = tmp;
                                }

                                System.arraycopy(triOutputIndex,
                                                 0,
                                                 charIndex,
                                                 total_index,
                                                 num_triangles);
                                total_index += num_triangles;
                            }
                            else if(num_triangles < 0)
                            {
                                System.out.println("can't tesselate \'" +
                                                   character + "\'");
                            }

                            charCoords[total_coords++] = newCoords[0] * scale;
                            charCoords[total_coords++] = newCoords[1] * scale;
                            charCoords[total_coords++] = 0;

                            start_vtx = total_coords - 3;
                            vtx_count = 1;
                        }
                    }

                    break;

                case PathIterator.SEG_LINETO:
// System.out.println("line " + total_coords + " : " + newCoords[0] + " " + newCoords[1]);
                    if(charCoords.length < total_coords + 2)
                    {
                        float[] tmp = new float[total_coords + 256];
                        System.arraycopy(charCoords, 0, tmp, 0, total_coords);
                        charCoords = tmp;
                    }

                    charCoords[total_coords++] = newCoords[0] * scale;
                    charCoords[total_coords++] = newCoords[1] * scale;
                    charCoords[total_coords++] = 0;
                    vtx_count++;
                    break;

                case PathIterator.SEG_CLOSE:
                    break;

                // Javadoc guarantees that no other types are used on fonts
            }

            glyph_path.next();
        }

//System.out.println("final curve: size " + charCoords.length + " vtx " + vtx_count + " total " + total_coords);
        if(second_curve_start > start_vtx)
        {
            int lower = second_curve_start - closest_point;

            // Shift the bit between the insert position and
            // the start of the second curve to the end past
            // the second curve. Since we also want to
            // duplicate closest point on the first curve for
            // the insertion process, make sure we add an extra
            // value.

//System.out.println("Character " + character +
//                   " char len " + charCoords.length +
//                   " cp " + closest_point +
//                   " tc " + total_coords +
//                   " len " + lower);

            System.arraycopy(charCoords,
                             closest_point,
                             charCoords,
                             total_coords,
                             lower);

            // Shift the whole lot down to the insert position
            System.arraycopy(charCoords,
                             second_curve_start,
                             charCoords,
                             closest_point + 3,
                             total_coords -
                              second_curve_start + lower + 3);
/*
System.out.println("adjusting curve: closest " + closest_point +
   " total " + total_coords + " amt " + lower);
System.out.println("2nd copy " + second_curve_start +
   " to " + (closest_point + 3) +
   " amt " + (total_coords - second_curve_start + lower + 3));
*/
            // Adjust for the extra replicated coordinate
            total_coords += 3;
            vtx_count++;
        }

        // Tesselate the final curve that has not be tesselated yet due
        // to not having the moveto command called.
        if(triOutputIndex.length < vtx_count * 3)
            triOutputIndex = new int[vtx_count * 3];

//System.out.println("close at " + total_coords + " : " + start_vtx + " " + vtx_count);
//for(int i = 0; i < vtx_count; i++)
//  System.out.println(i + " " + charCoords[i * 3] + " " + charCoords[i * 3 + 1]);


        num_triangles =
            triangulator.triangulateConcavePolygon(charCoords,
                                                   start_vtx,
                                                   vtx_count,
                                                   triOutputIndex,
                                                   FACE_NORMAL);

//System.out.println("num index post triangle = " + num_triangles + " " + vtx_count + " " + start_vtx);

        if(num_triangles > 0)
        {
            num_triangles *= 3;
            for(int i = 0; i < num_triangles; i++)
                triOutputIndex[i] /= 3;

//for(int i = 0; i < num_triangles / 3; i++)
//  System.out.println(i + " " + triOutputIndex[i * 3] +
//                     " " + triOutputIndex[i * 3 + 1] +
//                     " " + triOutputIndex[i * 3 + 2]);

            if(charIndex.length < total_index + num_triangles)
            {
                int[] tmp = new int[total_index + num_triangles];
                System.arraycopy(charIndex,
                                 0,
                                 tmp,
                                 0,
                                 total_index);
                charIndex = tmp;
            }

            System.arraycopy(triOutputIndex,
                             0,
                             charIndex,
                             total_index,
                             num_triangles);

            total_index += num_triangles;
        }
        else if(num_triangles < 0)
        {
            System.out.println("can't tesselate \'" +
                               character + "\'");
        }

        // Change the Y coordinate to reflect the normal Y orientation of up
        // in 3D land, where Y is down in 2D land. Also, turn the triangles
        // around now too as they'll be changed to clockwise when ccw is needed.
        for(int i = 0; i < total_index / 3; i++)
        {
            int tmp = charIndex[i * 3];
            charIndex[i * 3] = charIndex[i * 3 + 2];
            charIndex[i * 3 + 2] = tmp;
        }

        ch_data.coordinates = createFloatBuffer(total_coords);
        ch_data.coordinates.put(charCoords, 0, total_coords);

        ch_data.coordIndex = createIntBuffer(total_index);
        ch_data.coordIndex.put(charIndex, 0, total_index);
        ch_data.numIndex = total_index;

        charDataMap.put(character, ch_data);
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    private FloatBuffer createFloatBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        FloatBuffer ret_val = buf.asFloatBuffer();

        return ret_val;
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of ints to have in the array
     */
    private IntBuffer createIntBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        IntBuffer ret_val = buf.asIntBuffer();

        return ret_val;
    }

    /**
     * Point is inside check using the odd-even winding rule.
     * Think of standing inside a field with a fence representing the polygon.
     * Then walk north. If you have to jump the fence you know you are now
     * outside the poly. If you have to cross again you know you are now
     * inside again; i.e., if you were inside the field to start with, the
     * total number of fence jumps you would make will be odd, whereas if you
     * were outside the jumps will be even.
     *
     * @param x The x coordinate of the point to test against
     * @param y The y coordinate of the point to test against
     * @param polygon The set of points of the polygon
     * @param start The index of the start vertex of the current curve
     * @param numPoints the number of vertices in this curve
     * @return true when the point is inside the curve
     */
    private boolean isInsideEvenOdd(float x,
                                    float y,
                                    float[] polygon,
                                    int start,
                                    int numPoints)
    {
        boolean inside = false;

        int j = numPoints - 1;
        for(int i = 0; i < numPoints; j = i++)
        {
            if((((polygon[start + i * 3 + 1] <= y) &&
                 (y < polygon[start + j * 3 + 1])) ||
                ((polygon[start + j * 3 + 1] <= y) &&
                 (y < polygon[start + i * 3 + 1]))) &&
               (x < (polygon[start + j * 3] - polygon[start + i * 3]) *
                    (y - polygon[start + i * 3 + 1]) / (polygon[start + j * 3 + 1] -
                     polygon[start + i * 3 + 1]) + polygon[start + i * 3]))

            inside = !inside;
        }

        return inside;
    }

    /**
     * Find the closest point on the previous curve that is closest to the
     * given point. Uses a simple radial distance calc.
     *
     * @param x The x coordinate of the point to test against
     * @param y The y coordinate of the point to test against
     * @param polygon The set of points of the polygon
     * @param start The index of the start vertex of the current curve
     * @param numPoints the number of vertices in this curve
     * @return The index of the closest point
     */
    private int findClosestPoint(float x,
                                 float y,
                                 float[] polygon,
                                 int start,
                                 int numPoints)
    {
        float dx = x - polygon[start];
        float dy = y - polygon[start + 1];
        float min_d = dx * dx + dy * dy;
        int ret_val = start;

        for(int i = 1; i < numPoints; i++)
        {
            dx = x - polygon[start + i * 3];
            dy = y - polygon[start + i * 3 + 1];

            float d = dx * dx + dy * dy;
            if(d < min_d)
            {
                min_d = d;
                ret_val = start + i * 3;
            }
        }

        return ret_val;
    }
}
