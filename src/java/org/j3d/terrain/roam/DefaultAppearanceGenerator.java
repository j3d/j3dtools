/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

// Standard imports
import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;

// Application specific imports
import org.j3d.terrain.AppearanceGenerator;

/**
 * Implementation of a simple, default generator
 * <p>
 *
 * The appearance generated is lit and uses a base color of green.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class DefaultAppearanceGenerator implements AppearanceGenerator
{
    /** Global material instance to use */
    private Material material;

    /** Global polygon attributes to use */
    private PolygonAttributes polyAttr;

    /**
     * Create a default instance of this class
     */
    DefaultAppearanceGenerator()
    {

        material = new Material();
        material.setLightingEnable(true);

        polyAttr = new PolygonAttributes();
//        polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//        polyAttr.setCullFace(PolygonAttributes.CULL_NONE);

    }

    /**
     * Create a new appearance instance. The instance must be new each time.
     * The returned object should be configured to whatever capabilities you
     * wish, however, it must not be part of a live scene graph, and the
     * texture object must not yet be set.
     *
     * @return The new appearance instance to use
     */
    public Appearance createAppearance()
    {
        Appearance app = new Appearance();

        app.setMaterial(material);
//        app.setPolygonAttributes(polyAttr);

        return app;
    }
}
