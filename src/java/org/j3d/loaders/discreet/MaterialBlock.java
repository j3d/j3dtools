/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
// None

// Local imports
// None

/**
 * Representation of a collection of material definitions that form a single
 * block of data in the file.
 * <p>
 *
 * For each piece of data, if it was not defined in the loaded file, then the
 * array will be null.
 * <p>
 *
 * The material chunk has the following format:
 * <pre>
 * MATERIAL BLOCK 0xAFFF
 *     MATERIAL NAME 0xA000
 *     AMBIENT COLOR 0xA010
 *     DIFFUSE COLOR 0xA020
 *     SPECULAR COLOR 0xA030
 *     SHININESS PERC 0xA040
 *     TRANSPARENCY PERC 0xA050
 *     TWO_SIDED_LIGHTING 0xA081
 *     TEXTURE MAP 1 0xA200
 *     SPECULAR MAP 0xA204
 *     OPACITY MAP 0xA210
 *     REFLECTION MAP 0xA220
 *     BUMP MAP 0xA230
 *     [SUB CHUNKS FOR EACH MAP]
 *         MAPPING FILENAME 0xA300
 *         MAPPING PARAMETERS 0xA351
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class MaterialBlock
{
    /** The name of this material */
    public String name;

    /** The ambient colour of this block - transcribed to 3 value RGB [0,1] */
    public float[] ambientColor;

    /** The diffuse colour of this block - transcribed to 3 value RGB [0,1] */
    public float[] diffuseColor;

    /** The specular colour of this block - transcribed to 3 value RGB [0,1] */
    public float[] specularColor;

    /** The shininess ratio to a [0,1] range */
    public float shininessRatio;

    /**
     * The shininess converted to a [0,1] range. This is the typical shininess
     * that one expects of a material property.
     */
    public float shininessStrength;

    /** The transparency converted to a [0,1] range */
    public float transparency;

    /** Flag indicating if the object should be rendered as wireframe */
    public boolean wireframe;

    /** The wire size to use if wireframe */
    public float wireSize;

    /** The shading type. What each value means is not known right now */
    public int shadingType;

    /** Flag indicating the transparency blend is additive */
    public boolean additiveBlend;

    /** Flag indicating 2-sided lighting is set or not */
    public boolean twoSidedLighting;

    /** The first texture map to be associated with this material */
    public TextureBlock textureMap1;

    /** The second texture map to be associated with this material */
    public TextureBlock textureMap2;

    /** The specular light map to be associated with this material */
    public TextureBlock specularMap;

    /** The opacity map to be associated with this material */
    public TextureBlock opacityMap;

    /** The bump map to be associated with this material */
    public TextureBlock bumpMap;

    /** The reflection map (environment map) associated with this material */
    public TextureBlock reflectionMap;

    /** The shininess map associated with this material */
    public TextureBlock shininessMap;

    /**
     * Create a new instance of this material block with the defaults set.
     */
    public MaterialBlock()
    {
        twoSidedLighting = false;
        wireframe = false;
        additiveBlend = false;
    }
}
