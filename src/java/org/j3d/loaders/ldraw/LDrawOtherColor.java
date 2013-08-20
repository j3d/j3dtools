/*****************************************************************************
 *                            (c) j3d.org 2002-2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
// None

// Local parser
// None

/**
 * Enumerated list of the official colour charts.
 * <p>
 * Colours are named by number in the Lego view of the world, however we can't
 * name stuff as a number here so it is prefixed by the colour type (core,
 * transparent etc)
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawOtherColor extends LDrawColor
{
    public static final LDrawOtherColor CHROME_60 = new LDrawOtherColor(60, ColorType.CHROME, 0x645A4C, 0x281E10, 0);
    public static final LDrawOtherColor CHROME_61 = new LDrawOtherColor(61, ColorType.CHROME, 0x6C96BF, 0x202A68, 0);
    public static final LDrawOtherColor CHROME_62 = new LDrawOtherColor(62, ColorType.CHROME, 0x3CB371, 0x007735, 0);
    public static final LDrawOtherColor CHROME_63 = new LDrawOtherColor(63, ColorType.CHROME, 0xAA4D8E, 0x6E1152, 0);
    public static final LDrawOtherColor CHROME_64 = new LDrawOtherColor(64, ColorType.CHROME, 0x1B2A34, 0x000000, 0);
    public static final LDrawOtherColor CHROME_334 = new LDrawOtherColor(334, ColorType.CHROME, 0xBBA53D, 0xA4C374, 0);
    public static final LDrawOtherColor CHROME_383 = new LDrawOtherColor(383, ColorType.CHROME, 0xE0E0E0, 0xA4A4A4, 0);

    // 494, 495 defined as internal colours
    public static final LDrawOtherColor PEARL_134 = new LDrawOtherColor(134, ColorType.PEARL, 0xAB6038, 0x333333, 0);
    public static final LDrawOtherColor PEARL_135 = new LDrawOtherColor(135, ColorType.PEARL, 0x9CA3A8, 0x333333, 0);
    public static final LDrawOtherColor PEARL_137 = new LDrawOtherColor(137, ColorType.PEARL, 0x5677BA, 0x333333, 0);
    public static final LDrawOtherColor PEARL_142 = new LDrawOtherColor(142, ColorType.PEARL, 0xDCBE61, 0x333333, 0);
    public static final LDrawOtherColor PEARL_148 = new LDrawOtherColor(148, ColorType.PEARL, 0x575857, 0x333333, 0);
    public static final LDrawOtherColor PEARL_150 = new LDrawOtherColor(150, ColorType.PEARL, 0xBBBDBC, 0x333333, 0);
    public static final LDrawOtherColor PEARL_178 = new LDrawOtherColor(178, ColorType.PEARL, 0xB4883E, 0x333333, 0);
    public static final LDrawOtherColor PEARL_179 = new LDrawOtherColor(179, ColorType.PEARL, 0x898788, 0x333333, 0);
    public static final LDrawOtherColor PEARL_183 = new LDrawOtherColor(183, ColorType.PEARL, 0xF2F3F2, 0x333333, 0);
    public static final LDrawOtherColor PEARL_297 = new LDrawOtherColor(297, ColorType.PEARL, 0xCC9C2B, 0x333333, 0);


    public static final LDrawOtherColor METALLIC_80 = new LDrawOtherColor(80, ColorType.METALLIC, 0xA5A9B4, 0x333333, 250);
    public static final LDrawOtherColor METALLIC_81 = new LDrawOtherColor(81, ColorType.METALLIC, 0x899B5F, 0x333333, 250);
    public static final LDrawOtherColor METALLIC_82 = new LDrawOtherColor(82, ColorType.METALLIC, 0xDBAC34, 0x333333, 250);
    public static final LDrawOtherColor METALLIC_83 = new LDrawOtherColor(83, ColorType.METALLIC, 0x1A2831, 0x000000, 250);
    public static final LDrawOtherColor METALLIC_87 = new LDrawOtherColor(87, ColorType.METALLIC, 0x6D6E5C, 0x5D5B53, 250);

    public static final LDrawOtherColor RUBBER_65 = new LDrawOtherColor(65, ColorType.RUBBER, 0xF5CD2F, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_66 = new LDrawOtherColor(66, ColorType.RUBBER, 0xCAB000, 0x8E7400, 128);
    public static final LDrawOtherColor RUBBER_67 = new LDrawOtherColor(67, ColorType.RUBBER, 0xFFFFFF, 0xC3C3C3, 128);
    public static final LDrawOtherColor RUBBER_256 = new LDrawOtherColor(256, ColorType.RUBBER, 0x212121, 0x595959, 0);
    public static final LDrawOtherColor RUBBER_273 = new LDrawOtherColor(273, ColorType.RUBBER, 0x0033B2, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_324 = new LDrawOtherColor(324, ColorType.RUBBER, 0xC40026, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_375 = new LDrawOtherColor(375, ColorType.RUBBER, 0xC1C2C1, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_406 = new LDrawOtherColor(406, ColorType.RUBBER, 0x001D68, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_449 = new LDrawOtherColor(449, ColorType.RUBBER, 0x81007B, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_490 = new LDrawOtherColor(490, ColorType.RUBBER, 0xD7F000, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_496 = new LDrawOtherColor(496, ColorType.RUBBER, 0xA3A2A4, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_504 = new LDrawOtherColor(504, ColorType.RUBBER, 0x898788, 0x333333, 0);
    public static final LDrawOtherColor RUBBER_511 = new LDrawOtherColor(511, ColorType.RUBBER, 0xFAFAFA, 0x333333, 0);


    private LDrawOtherColor(int idx,
                            ColorType ct,
                            int hexColour,
                            int hexComplement,
                            int a)
    {
        super(idx, ct, hexColour, hexComplement, a);
    }
}
