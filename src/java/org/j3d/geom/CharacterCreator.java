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
 * @version $Revision: 1.3 $
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
//            GlyphVector glyph_vec =
//                font.layoutGlyphVector(fontContext, new_chars);

        sourceChar[0] = character;
        GlyphVector glyph_vec =
            font.createGlyphVector(fontContext, sourceChar);

        // Font Y-axis is downwards, so create an affine transform to flip it.
        Rectangle2D v_bounds = glyph_vec.getVisualBounds();
        double tx = v_bounds.getX() + 0.5 * v_bounds.getWidth();
        double ty = v_bounds.getY() + 0.5 * v_bounds.getHeight();

        AffineTransform neg_trans = new AffineTransform();
        neg_trans.setToTranslation(-tx, -ty);
        neg_trans.scale(1.0, -1.0);
        neg_trans.translate(tx, -ty);

        int vtx_count = 0;
        int start_vtx = 0;
        int total_coords = 0;
        int total_index = 0;
        boolean just_closed = false;

        CharacterData ch_data = new CharacterData();
        Rectangle2D l_bounds = glyph_vec.getLogicalBounds();

        Shape glyph_shape = glyph_vec.getOutline();
        PathIterator glyph_path =
            glyph_shape.getPathIterator(neg_trans, flatness);
        int num_triangles;
        float scale = 1 / (float)l_bounds.getHeight();

        ch_data.bounds =
            new Rectangle2D.Float((float)l_bounds.getX() * scale,
                                  (float)l_bounds.getY() * scale,
                                  (float)l_bounds.getWidth() * scale,
                                  1);
        total_coords = 0;
        total_index = 0;
        float last_x = 0;
        float last_y = 0;
        float init_x = 0;
        float init_y = 0;

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

                    init_x = newCoords[0] * scale;
                    init_y = newCoords[1] * scale;

                    charCoords[total_coords++] = newCoords[0] * scale;
                    charCoords[total_coords++] = newCoords[1] * scale;
                    charCoords[total_coords++] = 0;

//System.out.println("moveto " + newCoords[0] + " " + newCoords[1] + " " + just_closed + " i " + (total_coords - 3));

                    if(just_closed || total_coords == 3)
                        break;

                    if(triOutputIndex.length < vtx_count * 3)
                        triOutputIndex = new int[vtx_count * 3];

                    num_triangles =
                        triangulator.triangulateConcavePolygon(charCoords,
                                                               start_vtx,
                                                               vtx_count,
                                                               triOutputIndex,
                                                               FACE_NORMAL);

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

                    start_vtx = (vtx_count + 1) * 3;
                    vtx_count = 1;
                    just_closed = false;

                    break;

                case PathIterator.SEG_LINETO:
                    if(last_x == newCoords[0] && last_y == newCoords[1])
                        break;

                    charCoords[total_coords++] = newCoords[0] * scale;
                    charCoords[total_coords++] = newCoords[1] * scale;
                    charCoords[total_coords++] = 0;
                    vtx_count++;

                    last_x = newCoords[0];
                    last_y = newCoords[1];
//System.out.println("line " + (total_coords - 3) + " : " + newCoords[0] + " " + newCoords[1]);
                    break;

                case PathIterator.SEG_CLOSE:
                    just_closed = true;

                    if(triOutputIndex.length < vtx_count * 3)
                        triOutputIndex = new int[vtx_count * 3];

                    if(charCoords[total_coords - 3] == init_x &&
                       charCoords[total_coords - 2] == init_y)
                    {
                        vtx_count--;
                        total_coords -= 3;
                    }

//System.out.println("close at " + total_coords + " : " + start_vtx + " " + vtx_count);
                    num_triangles =
                        triangulator.triangulateConcavePolygon(charCoords,
                                                               start_vtx,
                                                               vtx_count,
                                                               triOutputIndex,
                                                               FACE_NORMAL);

//System.out.println("num index post triangle = " + num_triangles);

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

                    start_vtx = (vtx_count + 1) * 3;
                    vtx_count = 0;
                    break;

                // Javadoc guarantees that no other types are used on fonts
            }

            glyph_path.next();
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
}
