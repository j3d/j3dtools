/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.uti;

// Standard imports
import java.io.File;
import javax.swing.filechooser.FileFilter;

// Application specific imports
import org.j3d.util.device.FileLoaderDescriptor;

/**
 * A file filter implementation so that you can grab files of the types
 * that correspond to the loaders available on the system.
 * <P>
 *
 * The class takes information from the filter and builds filtering
 * information from that. It does not maintain a reference to the
 * descriptor.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class LoaderFileFilter extends FileFilter
{
  /** The description of the file filter */
  private String description;

  /** The extension matching this loader type */
  private String extension;

  /**
   * Construct an instance of the filter based on the given device
   * description.
   *
   * @param fld The file loader description to base the filter on
   */
  public LoaderFileFilter(FileLoaderDescriptor fld)
  {
    description = fld.getDescription();
    extension = fld.getExtension();
  }

  /**
   * Decide whether to accept this file based on the filter type.
   *
   * @param f The file to test for suitability
   * @return true if the file passes the filter
   */
  public boolean accept(File f)
  {
    String name = f.getName();
    int index = name.lastIndexOf('.');

    String ext = name.substring(index);

    return extension.equals(ext);
  }

  /**
   * Return a description string of the this filter (The file type)
   * supported by this filter.
   *
   * @return a String describing this filter
   */
  public String getDescription()
  {
    return description;
  }
}

