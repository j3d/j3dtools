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
import java.util.Map;

import com.sun.j3d.loaders.Loader;

// Application specific imports
// none

/**
 * Extension of the Sun Loader definition interface that provides extra
 * ability to control the scene graph setup.
 * <p>
 *
 * The methods provided here set the capability bit mappings to be used
 * during the next run of the loader instance. This allows the parser to
 * set the user-required capability bits while the loader is parsing, rather
 * than requiring the user to do their own scene-graph traversal.
 * <p>
 *
 * The maps here have very specific requirements to their content. Each map
 * has the same requirement. The key for an entry in the map is the
 * {@link java.lang.Class} instance for the java3D class that you want to
 * control. For example, if you want to set capability bits on an
 * {@link javax.media.j3d.Appearance} instance, then the key must be
 * <code>Appearance.class</code>. The value for that key must be an array
 * of ints - ie <code>int[]</code>. This always applies, even if you only
 * want one specific capability set. If you do not, then internally the
 * code will throw a class cast exception and the parsing process will
 * stop.
 * <p>
 *
 * Setting any value through this interface means that the loader is
 * obligated to follow these rules. If the key is not defined here, then the
 * loader is free to set exactly what they want. Any other behaviours are
 * defined in the individual methods.
 * <p>
 *
 * <b>Note</b> If both required and capability maps are set then override
 * settings will be use in preference to required settings for that mapping.
 * So, given the following method calls, override capability bits will be set
 * but required frequency bits will be used for all subsequent calls to
 * <code>load()</code>.
 * <pre>
 *   myLoader.setCapabilityOverrideMap(overrideBits, null);
 *   myLoader.setCapabilityRequiredMap(requiredBits, requiredFreqs);
 * </pre>
 * <p>
 *
 * Calling any method with null will clear that requirement. So, if the user
 * then called
 * <pre>
 *   myLoader.setCapabilityOverrideMap(null, null);
 * </pre>
 * <p>
 *
 * after the two previous calls, the override mappings are gone, but the
 * required mappings still exist and are valid.
 * <p>
 *
 * <b.Extra Note!</b> Capability bits are only available in Java3D 1.3. If
 * the loader is built using java3d 1.2, the frquency bit mapping information
 * is ignored. C
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ManagedLoader extends Loader
{
    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits);


    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits);
}
