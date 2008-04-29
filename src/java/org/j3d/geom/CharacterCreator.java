/*****************************************************************************
 *                        j3d.org Copyright (c) 2008
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
import java.util.Arrays;
import java.util.ArrayList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

// Local imports
import org.j3d.util.CharHashMap;
import org.j3d.util.IntHashMap;

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
 * @version $Revision: 1.11 $
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
    public synchronized void createCharacterTriangles(char[] characters,
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
     * Convenience method that creates the glyph information
	 * for the specified character.
     *
     * @param character The character to be created.
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

		ArrayList<int[]> polyIndexList = new ArrayList<int[]>();
//System.out.println("processing " + character + " flatness " + (flatness / scale));

        while(!glyph_path.isDone())
        {
            switch(glyph_path.currentSegment(newCoords))
            {
                case PathIterator.SEG_MOVETO:
					polyIndexList.add(new int[]{total_coords, -1});
                    resizeCharCoords(total_coords + 3);

                    charCoords[total_coords++] = newCoords[0] * scale;
                    charCoords[total_coords++] = newCoords[1] * scale;
                    charCoords[total_coords++] = 0;
                    vtx_count++;

                    break;

                case PathIterator.SEG_LINETO:
                    resizeCharCoords(total_coords + 3);

                    charCoords[total_coords++] = newCoords[0] * scale;
                    charCoords[total_coords++] = newCoords[1] * scale;
                    charCoords[total_coords++] = 0;
                    vtx_count++;
					
                    break;

                case PathIterator.SEG_CLOSE:
					int[] poly_index = polyIndexList.get(polyIndexList.size()-1);
					poly_index[1] = total_coords;
                    break;

                // Javadoc guarantees that no other types are used on fonts
            }

            glyph_path.next();
        }
		
		// determine how the polygon segments are 'contained',
		// keys are the ids of exterior polys, the value is an
		// int[] of the ids of interior ploys
		IntHashMap polyIDMap = group(charCoords, polyIndexList);
		
		// setup the ordering of the vertices and generate the triangles
		total_coords = 0;
		int[] parent_poly_ids = polyIDMap.keySet();
		int num_parent_polys = parent_poly_ids.length;
		float[][] poly_coords = new float[num_parent_polys][];
		for (int i = 0; i < num_parent_polys; i++) {
			int parent_id = parent_poly_ids[i];
			int[] parent_poly_indices = polyIndexList.get(parent_id);
			int parent_start_idx = parent_poly_indices[0];
			int parent_end_idx = parent_poly_indices[1];
			int parent_num_vertices = (parent_end_idx - parent_start_idx) / 3;
			
			// ensure that the exterior poly(s) are ordered clockwise
			if (!isClockwise(charCoords, parent_start_idx, parent_num_vertices)) {
				invertOrder(charCoords, parent_start_idx, parent_num_vertices);
			}
			
			// order the interior poly vertices such that the starting vertex
			// is the one closest to the starting vertex of the exterior poly
			int[] children_poly_ids = (int[])polyIDMap.get(parent_id);
			int num_children = children_poly_ids.length;
			if (num_children > 0)
			{
				float parent_start_x = charCoords[parent_start_idx];
				float parent_start_y = charCoords[parent_start_idx+1];
				for (int j = 0; j < num_children; j++) 
				{
					int child_id = children_poly_ids[j];
					int[] child_poly_indices = polyIndexList.get(child_id);
					int child_start_idx = child_poly_indices[0];
					int child_end_idx = child_poly_indices[1];
					int child_array_length = child_end_idx - child_start_idx;
					int child_num_vertices = child_array_length / 3;
					
					// ensure that the interior poly(s) are ordered counter-clockwise
					if (isClockwise(charCoords, child_start_idx, child_num_vertices)) {
						invertOrder(charCoords, child_start_idx, child_num_vertices);
					}
					/*
					// rem: this fixes some problems and causes others.......
					closest_point = findClosestPoint(
						parent_start_x,
						parent_start_y,
						charCoords,
						child_start_idx,
						child_num_vertices);
					
					if (closest_point != child_start_idx)
					{
						reorder(charCoords, child_start_idx, child_num_vertices, closest_point);
					}
					*/
				}
			}
			poly_coords[i] = combine(charCoords, polyIndexList, polyIDMap, parent_id);
			total_coords += poly_coords[i].length;
		}
		
		// aggregate the ordered vertices back into the charCoords array
		resizeCharCoords(total_coords);
		int idx = 0;
		for (int i = 0; i < num_parent_polys; i++) 
		{
			int length = poly_coords[i].length;
			System.arraycopy(poly_coords[i], 0, charCoords, idx, length);
			idx += length;
		}
		
        // tesselate the curves
		start_vtx = 0;
		for (int i = 0; i < num_parent_polys; i++) 
		{
			int length = poly_coords[i].length;
			vtx_count = length / 3;
			
			if (triOutputIndex.length < vtx_count * 3)
			{
				triOutputIndex = new int[vtx_count * 3];
			}
			
			num_triangles = triangulator.triangulateConcavePolygon(
				charCoords,
				start_vtx,
				vtx_count,
				triOutputIndex,
				FACE_NORMAL);
			
			if(num_triangles > 0)
			{
				num_triangles *= 3;
				for (int j = 0; j < num_triangles; j++)
				{
					triOutputIndex[j] /= 3;
				}
				
				if (charIndex.length < total_index + num_triangles)
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
				System.out.println("can't tesselate \'"+ character +"\'");
			}
			start_vtx += length;
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
     * Convenience method that goes through a list of characters and creates
     * the needed glyph information.
     *
     * @param characters List of characters that need to be created.
     */
/*
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

//
//System.out.println("char \'" + character + "\'");
//System.out.println("Logical bounds " + l_bounds);
//System.out.println("visual bounds " + v_bounds);
//System.out.println(" offset " +
//                   metrics.getBaselineOffsets()[0] + ", " +
//                   metrics.getBaselineOffsets()[1] + " desc " +
//                   metrics.getHeight() + " a " +
//                   metrics.getDescent() + " h " +
//                   metrics.getAscent());
//

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
                    resizeCharCoords(total_coords + 3);

                    if(total_coords == 0)
                    {
                        charCoords[total_coords++] = newCoords[0] * scale;
                        charCoords[total_coords++] = newCoords[1] * scale;
                        charCoords[total_coords++] = 0;
                        vtx_count = 1;
//System.out.println("start " + newCoords[0] + " " + newCoords[1] );
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
                            resizeCharCoords(Math.max((closest_point+lower), (total_coords+lower)));
                            System.arraycopy(charCoords,
                                             closest_point,
                                             charCoords,
                                             total_coords,
                                             lower);

                            // Shift the whole lot down to the insert position
                            resizeCharCoords(Math.max((total_coords+lower+3), 
                                (closest_point+3+total_coords-second_curve_start+lower+3)));
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
                                System.out.println("can't tesselate 0 \'" +
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
//System.out.println("line " + total_coords + " : " + newCoords[0] + " " + newCoords[1]);
                    resizeCharCoords(total_coords + 3);

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
            resizeCharCoords(Math.max((closest_point+lower), (total_coords+lower)));
            System.arraycopy(charCoords,
                             closest_point,
                             charCoords,
                             total_coords,
                             lower);

            // Shift the whole lot down to the insert position
            resizeCharCoords(Math.max((total_coords+lower+3), 
                (closest_point+3+total_coords-second_curve_start+lower+3)));
            System.arraycopy(charCoords,
                             second_curve_start,
                             charCoords,
                             closest_point + 3,
                             total_coords -
                              second_curve_start + lower + 3);
//
//System.out.println("adjusting curve: closest " + closest_point +
//   " total " + total_coords + " amt " + lower);
//System.out.println("2nd copy " + second_curve_start +
//   " to " + (closest_point + 3) +
//   " amt " + (total_coords - second_curve_start + lower + 3));
//
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
            System.out.println("can't tesselate 1 \'" +
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
*/
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
    
    /**
     * Ensure that the charCoords array is sufficiently sized
     *
     * @param size The minimum size required
     */
    private void resizeCharCoords(int size) 
    {
        int len = charCoords.length;
        if(len < size)
        {
            while(len < size)
            {
                len += 256;
            }
            float[] tmp = new float[len];
            System.arraycopy(charCoords, 0, tmp, 0, charCoords.length);
            charCoords = tmp;
        }
    }
	
    /**
     * Ensure that the argument array is sufficiently sized
     *
	 * @param array The array to size
     * @param size The minimum size required
     */
    private float[] resize(float[] array, int size) 
    {
		float[] ret_array = null;
		if (array == null)
		{
			ret_array = new float[size];
		}
		else {
			int len = array.length;
			if(len < size)
			{
				while(len < size)
				{
					len += 256;
				}
				ret_array = new float[len];
				System.arraycopy(array, 0, ret_array, 0, array.length);
			}
		}
		return(ret_array);
    }
	
	/**
     * Invert the ordering of the vertices of a polygon
	 *
     * @param coord The set of points of the polygon
     * @param start The index of the start vertex in the coord array
     * @param num The number of vertices in the polygon
     */
	private void invertOrder(float[] coord, int start, int num) {
		
		// swap the vertices end-to-end
		int end = start + (num - 1) * 3;
		int num_swap = num/2;
		for (int i = 0; i < num_swap; i++) 
		{
			float x = coord[end];
			float y = coord[end+1];
			float z = coord[end+2];
			coord[end] = coord[start];
			coord[end+1] = coord[start+1];
			coord[end+2] = coord[start+2];
			coord[start] = x;
			coord[start+1] = y;
			coord[start+2] = z;
			start += 3;
			end -= 3;
		}
	}

	/**
     * Reorder the vertices of a polygon, with the specified index as
	 * the new starting index
	 *
     * @param coord The set of points of the polygon
     * @param start The index of the current start vertex in the coord array
     * @param num The number of vertices in the polygon
	 * @param newStart The index in the coord array to make the new starting index
     */
	private void reorder(float[] coord, int start, int num, int newStart) {

		if (newStart == start)
		{
			return;
		}
		int end = start + (num * 3);
		if ((newStart > start) && (newStart < end))
		{
			// reorder the original into a scratch array
			int size = end - start;
			float[] tmp = new float[size];
			int num_copy0 = end - newStart;
			System.arraycopy(coord, newStart, tmp, 0, num_copy0);
			int num_copy1 = newStart - start;
			System.arraycopy(coord, start, tmp, num_copy0, num_copy1);
			
			// replace the original coords with the reordered copy
			System.arraycopy(tmp, 0, coord, start, size);
		}
		else
		{
			// the new start index is not with the range of the poly
		}
	}

	/**
     * Determine whether the ordering of the vertices of a polygon
	 * is clockwise.
	 * <p>
	 * See: <a href="http://local.wasp.uwa.edu.au/~pbourke/geometry/clockwise/">
	 * Determining whether ...</a>
	 *
     * @param coord The set of points of the polygon
     * @param start The index of the start vertex in the coord array
     * @param num The number of vertices in the polygon
	 * @return true if the ordering is clockwise, false if the ordering
	 * is counter-clockwise.
     */
	private boolean isClockwise(float[] coord, int start, int num)
	{
		float sum = 0;
		int offset = start;
		
		float x0;
		float y0;
		float x1;
		float y1;
		
		for (int i = 0; i < num - 1; i++) 
		{
			x0 = coord[offset];
			y0 = coord[offset + 1];
			offset += 3;
			x1 = coord[offset];
			y1 = coord[offset + 1];
			
			sum += (x0 * y1 - x1 * y0);
		}
		return((sum < 0));
	}
	
    /**
     * Return the bounds of the geometry. An array containing
     * [x_min, x_max, y_min, y_max].
     *
     * @param coord The set of points of the polygon
     * @param start The index of the start vertex in the coord array
     * @param end The index of the last vertex (+1) in the coord array
     * @return The bounds of the geometry
     */
    private float[] getBounds(float[] coord, int start, int end) 
	{
        float cx, cy, cz;
        float[] bounds = new float[] {
            Float.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE, Float.MIN_VALUE};
        
        for (int i = start; i < end;) 
		{
            cx = coord[i++];
            cy = coord[i++];
            i++;
            
            // gets max and min bounds
            if (cx < bounds[0]) 
			{
                bounds[0] = cx;
            }
            if (cx > bounds[1]) 
			{
                bounds[1] = cx;
            }
            if (cy < bounds[2]) 
			{
                bounds[2] = cy;
            }
            if (cy > bounds[3]) 
			{
                bounds[3] = cy;
            }
        }
        return(bounds);
    }
	
	/**
	 * Return whether a set of bounds is contained by another
	 *
	 * @param external The external bounds
	 * @param internal The candidate internal bounds
	 * @return true if internal is contained within external, false
	 * otherwise.
	 */
	private boolean contains(float[] external, float[] internal) 
	{
		return(
			(internal[0] > external[0]) && (internal[1] < external[1]) &&
			(internal[2] > external[2]) && (internal[3] < external[3]));
	}
	
	/**
	 * Group the polygons together based on their bounds. Polygons that
	 * are contained within the bounds of another polygon are included
	 * into that polygon's group.
	 *
     * @param coord The set of points of the polygon
	 * @param polyIndexList The list containing the beginning and ending indices
	 * of each polygon.
	 * @return A map containing arrays of indices within the polyIndexList that
	 * are to be grouped together. The map is keyed by indices to polygons from
	 * the polyIndexList.
	 */
	private IntHashMap group(float[] coord, ArrayList<int[]> polyIndexList) 
	{
		int num_poly = polyIndexList.size();
		IntHashMap map = new IntHashMap(num_poly);
		
		// get the bounds of each polygon for comparison
		float[][] bounds = new float[num_poly][];
		for (int i = 0; i < num_poly; i++) 
		{
			int[] poly_idx = polyIndexList.get(i);
			bounds[i] = getBounds(coord, poly_idx[0], poly_idx[1]);
		}
		// for each polygon, determine which other polygons are
		// contained within it's bounds
		int[] set = new int[num_poly-1];
		for (int i = 0; i < num_poly; i++) 
		{
			float[] bound_i = bounds[i];
			int idx = 0;
			for (int j = 0; j < num_poly; j++) 
			{
				if (j != i) 
				{
					if (contains(bound_i, bounds[j])) 
					{
						set[idx++] = j;
					}
				}
			}
			// for each poly, create the index list of the
			// polys that are contained
			int[] children = new int[idx];
			for (int n = 0; n < idx; n++)
			{
				children[n] = set[n];
			}
			map.put(i, children);
		}
		// remove the polys that are 'grouped'
		for (int i = 0; i < num_poly; i++) 
		{
			int[] children = (int[])map.get(i);
			if (children != null)
			{
				for (int n = 0; n < children.length; n++) {
					map.remove(children[n]);
				}
			}
		}
		return(map);
	}
	
	/**
	 * Combine together an exterior polygon with it's set of interior polygons.
	 *
     * @param coord The set of points of the polygon
	 * @param polyIndexList The list containing the beginning and ending indices
	 * of each polygon.
	 * @param polyIDMap A map containing arrays of indices within the polyIndexList that
	 * are to be grouped together. The map is keyed by indices to polygons from
	 * the polyIndexList.
	 * @param id The polygon id (map key) that identifies the exterior polygon to
	 * combine with it's interior polygons.
	 * @return The combined vertices of the polygons
	 */
	private float[] combine(float[] coord, ArrayList<int[]> polyIndexList, 
		IntHashMap polyIDMap, int id)
	{
		float[] combined_coord = null;
		
		int[] parent_poly_indices = polyIndexList.get(id);
		int parent_start_idx = parent_poly_indices[0];
		int parent_end_idx = parent_poly_indices[1];
		int parent_array_length = parent_end_idx - parent_start_idx;
		int parent_num_vertices = parent_array_length / 3;
		
		int[] children_poly_ids = (int[])polyIDMap.get(id);
		int num_children = children_poly_ids.length;
		if (num_children > 0)
		{
			// determine the insertion index for each interior poly
			// within the exterior poly's vertices
			int total_array_length = parent_array_length;
			int[] insertion_point = new int[num_children];
			int child_poly_id = -1;
			int[] child_poly_indices = null;
			int child_start_idx = -1;
			int child_end_idx = -1;
			int child_array_length = -1;
			for (int i = 0; i < num_children; i++)
			{
				child_poly_id = children_poly_ids[i];
				child_poly_indices = polyIndexList.get(child_poly_id);
				child_start_idx = child_poly_indices[0];
				float child_start_x = coord[child_start_idx];
				float child_start_y = coord[child_start_idx+1];
				child_end_idx = child_poly_indices[1];
				child_array_length = child_end_idx - child_start_idx;
				//int child_num_vertices = child_array_length / 3;
				insertion_point[i] = findClosestPoint(
					child_start_x,
					child_start_y,
					coord,
					parent_start_idx,
					parent_num_vertices);
				total_array_length += (child_array_length + 3);
			}
			// get the order of the polys to insert
			int[] insertion_order = new int[num_children];
			boolean[] used = new boolean[num_children];
			Arrays.fill(used, false);
			for (int i = 0; i < num_children; i++)
			{
				int idx = 0;
				int min_pass = Integer.MAX_VALUE;
				for (int j = 0; j < num_children; j++)
				{
					if (!used[j]) 
					{
						int point = insertion_point[j];
						if (point < min_pass)
						{
							idx = j;
							min_pass = point;
						}
					}
				}
				insertion_order[i] = children_poly_ids[idx];
				used[idx] = true;
			}
			
			combined_coord = new float[total_array_length];
			//
			// child_poly_ids[] -> indices into the polyIndexList to retrieve the begin-end indices
			// insertion_point[] -> insertion indices for each child into the parent vertex array
			// insertion_order[] -> indices into child_poly_ids[] & insertion_point[]
			//
			// for each child, in insertion_order, insert the vertices, combined with 
			// the parent array into the combined_coord[]
			//////////////////////////////////////////////////////////////////////////
			// copy the start of the exterior vertices
			child_poly_id = insertion_order[0];
			int insert_idx_0 = -1;
			for (int i = 0; i < num_children; i++) 
			{
				if (child_poly_id == children_poly_ids[i])
				{
					insert_idx_0 = insertion_point[i];
				}
			}
			int length = insert_idx_0 - parent_start_idx + 3;
			int combined_idx = 0;
			System.arraycopy(coord, parent_start_idx, combined_coord, combined_idx, length);
			combined_idx += length;
			//////////////////////////////////////////////////////////////////////////
			// copy the interior vertices, and fill in the external 
			for (int i = 0; i < num_children; i++) 
			{
				child_poly_id = insertion_order[i];
				child_poly_indices = polyIndexList.get(child_poly_id);
				child_start_idx = child_poly_indices[0];
				child_end_idx = child_poly_indices[1];
				length = child_end_idx - child_start_idx;
				
				System.arraycopy(coord, child_start_idx, combined_coord, combined_idx, length);
				combined_idx += length;
				if ((i + 1) < num_children)
				{
					child_poly_id = insertion_order[i+1];
					int insert_idx_1 = -1;
					for (int j = 0; j < num_children; j++) 
					{
						if (child_poly_id == children_poly_ids[j])
						{
							insert_idx_1 = insertion_point[j];
						}
					}
					length = insert_idx_1 - insert_idx_0 + 3;
					
					System.arraycopy(coord, insert_idx_0, combined_coord, combined_idx, length);
					combined_idx += length;
					insert_idx_0 = insert_idx_1;
				}
			}
			//////////////////////////////////////////////////////////////////////////
			// copy the end of the exterior vertices
			length = parent_end_idx - insert_idx_0;
			System.arraycopy(coord, insert_idx_0, combined_coord, combined_idx, length);
		}
		else 
		{
			combined_coord = new float[parent_array_length];
			System.arraycopy(coord, parent_start_idx, combined_coord, 0, parent_array_length);
		}
		return(combined_coord);
	}
}
