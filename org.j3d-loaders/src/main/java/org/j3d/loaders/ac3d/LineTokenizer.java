/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.loaders.ac3d;

// External imports
import java.util.ArrayList;

// Local imports
// None

/**
 * A simple tokenizer that breaks down a single line of text into individual
 * word tokens, with the exception of text surrounded by quotes.
 * <p>
 *
 * This is actually something that could (and
 * maybe should) be implemented using a <code>StreamTokenizer</code>.
 * This is actually a suboptimal process, which should be revisited
 * when everything is functionally complete.</p>
 *
 * <p><strong>TODO:</strong><ul>
 * <li> Fix transient inclusion of quotes
 * <li> Cleanup, commentary, and optimization.
 * </ul></p>
 *
 * @author  Ryan Wilhm (ryan@entrophica.com)
 * @version $Revision: 1.1 $
 */
class LineTokenizer
{
    /** Used for generating the right type from ArrayList.toArray() */
    private static final String[] STRING_ARRAY_TYPE = new String[0];

    /** Current collection of strings from the line */
    private ArrayList<String> tokens;

    /**
     * Create a new default instance of the tokenizer
     */
    LineTokenizer()
    {
        tokens = new ArrayList<String>();
    }

    /**
     * <p>Returns an array of <code>String</code> objects containing the
     * individual tokens from the parameter. These tokens were delimited
     * by whitespace, except for when enclosed in quotes.</p>
     *
     * @param line The input <code>String</code> to decompose.
     * @return The output array of tokens.
     */

    String[] enumerateTokens(String line)
    {
        int startPos = 0;
        int currentPos = 0;
        boolean ignoreSpace = false;
        boolean usedQuotes = false;

        line = line.trim();

        // Rough resizing to avoid over/re allocating unless necessary
        int line_len = line.length();
        tokens.ensureCapacity(line_len / 2);

        for(currentPos = 0; currentPos < line_len; currentPos++)
        {
            int ch = line.charAt(currentPos);

            if(ch == '\"')
            {
                ignoreSpace = !ignoreSpace;
                usedQuotes = true;
            }

            if(!ignoreSpace && (ch ==' '))
            {
                if (usedQuotes)
                {
                    tokens.add(line.substring(startPos + 1, currentPos - 1));
                    usedQuotes = false;
                }
                else if((currentPos - startPos) > 0)
                {
                    // Ignore extra spaces
                    tokens.add(line.substring(startPos, currentPos));
                }

                startPos = currentPos + 1;
            }
        }

        // Make sure the last token is included
        if(currentPos > startPos)
        {
            if(usedQuotes)
            {
                tokens.add(line.substring(startPos + 1, currentPos - 1));
                usedQuotes = false;
            }
            else if((currentPos - startPos)>0)
            {
                // Ignore extra spaces
                tokens.add(line.substring(startPos, currentPos));
            }
        }

        String[] ret_val = tokens.toArray(STRING_ARRAY_TYPE);
        tokens.clear();

        return ret_val;
    }
}
