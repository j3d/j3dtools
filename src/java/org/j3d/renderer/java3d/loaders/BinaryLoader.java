/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.loaders;

// Standard imports
import java.io.InputStream;
import java.io.IOException;

import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;

// Application specific imports
// none

/**
 * Extension of the Sun Loader definition interface that provides extra
 * methods for loading {@link java.io.InputStream}.
 * <p>
 *
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public interface BinaryLoader extends Loader
{
    /**
     * This method loads the InputStream and returns the Scene containing the
     * scene. Any data files referenced by the Reader should be located in
     * the user's current working directory or the directory specified by the
     * base path.
     *
     * @param is The stream to read the file data from
     * @return The scene that represents the file content
     * @throws IOException An I/O error while reading the data
     * @param IncorrectFormatException The file format is not of this type
     * @param ParsingErrorException Syntax and/or semantic parsing problems
     */
    public Scene load(InputStream is)
               throws IOException,
                      IncorrectFormatException,
                      ParsingErrorException;
}
