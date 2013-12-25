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
 * Representation of the official standard core colour charts.
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
public class LDrawStdColor extends LDrawColor
{
    public static final LDrawStdColor CORE_0 = new LDrawStdColor(0, ColorType.CORE, 0x05131D, 0x595959, 0);
    public static final LDrawStdColor CORE_1 = new LDrawStdColor(1, ColorType.CORE, 0x0055BF, 0x333333, 0);
    public static final LDrawStdColor CORE_2 = new LDrawStdColor(2, ColorType.CORE, 0x237841, 0x333333, 0);
    public static final LDrawStdColor CORE_3 = new LDrawStdColor(3, ColorType.CORE, 0x008F9B, 0x333333, 0);
    public static final LDrawStdColor CORE_4 = new LDrawStdColor(4, ColorType.CORE, 0xC91A09, 0x333333, 0);
    public static final LDrawStdColor CORE_5 = new LDrawStdColor(5, ColorType.CORE, 0xC870A0, 0x333333, 0);
    public static final LDrawStdColor CORE_6 = new LDrawStdColor(6, ColorType.CORE, 0x583927, 0x1E1E1E, 0);
    public static final LDrawStdColor CORE_7 = new LDrawStdColor(7, ColorType.CORE, 0x9BA19D, 0x333333, 0);
    public static final LDrawStdColor CORE_8 = new LDrawStdColor(8, ColorType.CORE, 0x6D6E5C, 0x333333, 0);
    public static final LDrawStdColor CORE_9 = new LDrawStdColor(9, ColorType.CORE, 0xB4D2E3, 0x333333, 0);
    public static final LDrawStdColor CORE_10 = new LDrawStdColor(10, ColorType.CORE, 0x4B9F4A, 0x333333, 0);
    public static final LDrawStdColor CORE_11 = new LDrawStdColor(11, ColorType.CORE, 0x55A5AF, 0x333333, 0);
    public static final LDrawStdColor CORE_12 = new LDrawStdColor(12, ColorType.CORE, 0xF2705E, 0x333333, 0);
    public static final LDrawStdColor CORE_13 = new LDrawStdColor(13, ColorType.CORE, 0xFC97AC, 0x333333, 0);
    public static final LDrawStdColor CORE_14 = new LDrawStdColor(14, ColorType.CORE, 0xF2CD37, 0x333333, 0);
    public static final LDrawStdColor CORE_15 = new LDrawStdColor(15, ColorType.CORE, 0xFFFFFF, 0x333333, 0);
    public static final LDrawStdColor CORE_17 = new LDrawStdColor(17, ColorType.CORE, 0xC2DAB8, 0x333333, 0);
    public static final LDrawStdColor CORE_18 = new LDrawStdColor(18, ColorType.CORE, 0xFBE696, 0x333333, 0);
    public static final LDrawStdColor CORE_19 = new LDrawStdColor(19, ColorType.CORE, 0xE4CD9E, 0x333333, 0);
    public static final LDrawStdColor CORE_20 = new LDrawStdColor(20, ColorType.CORE, 0xC9CAE2, 0x333333, 0);
    public static final LDrawStdColor CORE_22 = new LDrawStdColor(22, ColorType.CORE, 0x81007B, 0x333333, 0);
    public static final LDrawStdColor CORE_23 = new LDrawStdColor(23, ColorType.CORE, 0x2032B0, 0x1E1E1E, 0);
    public static final LDrawStdColor CORE_25 = new LDrawStdColor(25, ColorType.CORE, 0xFE8A18, 0x333333, 0);
    public static final LDrawStdColor CORE_26 = new LDrawStdColor(26, ColorType.CORE, 0x923978, 0x333333, 0);
    public static final LDrawStdColor CORE_27 = new LDrawStdColor(27, ColorType.CORE, 0xBBE90B, 0x333333, 0);
    public static final LDrawStdColor CORE_28 = new LDrawStdColor(28, ColorType.CORE, 0x958A73, 0x333333, 0);
    public static final LDrawStdColor CORE_29 = new LDrawStdColor(29, ColorType.CORE, 0xE4ADC8, 0x333333, 0);
    public static final LDrawStdColor CORE_68 = new LDrawStdColor(68, ColorType.CORE, 0xF3CF9B, 0x333333, 0);
    public static final LDrawStdColor CORE_69 = new LDrawStdColor(69, ColorType.CORE, 0xCD6298, 0x333333, 0);
    public static final LDrawStdColor CORE_70 = new LDrawStdColor(70, ColorType.CORE, 0x582A12, 0x333333, 0);
    public static final LDrawStdColor CORE_71 = new LDrawStdColor(71, ColorType.CORE, 0xA0A5A9, 0x333333, 0);
    public static final LDrawStdColor CORE_72 = new LDrawStdColor(72, ColorType.CORE, 0x6C6E68, 0x333333, 0);
    public static final LDrawStdColor CORE_73 = new LDrawStdColor(73, ColorType.CORE, 0x5A93DB, 0x333333, 0);
    public static final LDrawStdColor CORE_74 = new LDrawStdColor(74, ColorType.CORE, 0x73DCA1, 0x333333, 0);
    public static final LDrawStdColor CORE_77 = new LDrawStdColor(77, ColorType.CORE, 0xFECCCF, 0x333333, 0);
    public static final LDrawStdColor CORE_78 = new LDrawStdColor(78, ColorType.CORE, 0xF6D7B3, 0x333333, 0);
    public static final LDrawStdColor CORE_84 = new LDrawStdColor(84, ColorType.CORE, 0xCC702A, 0x333333, 0);   // *****
    public static final LDrawStdColor CORE_85 = new LDrawStdColor(85, ColorType.CORE, 0x3F3691, 0x1E1E1E, 0);
    public static final LDrawStdColor CORE_86 = new LDrawStdColor(86, ColorType.CORE, 0x7C503A, 0x333333, 0);
    public static final LDrawStdColor CORE_89 = new LDrawStdColor(89, ColorType.CORE, 0x4C61DB, 0x333333, 0);
    public static final LDrawStdColor CORE_92 = new LDrawStdColor(92, ColorType.CORE, 0xD09168, 0x333333, 0);
    public static final LDrawStdColor CORE_100 = new LDrawStdColor(100, ColorType.CORE, 0xFEBABD, 0x333333, 0);
    public static final LDrawStdColor CORE_110 = new LDrawStdColor(110, ColorType.CORE, 0x4354A3, 0x333333, 0);
    public static final LDrawStdColor CORE_112 = new LDrawStdColor(112, ColorType.CORE, 0x6874CA, 0x333333, 0);
    public static final LDrawStdColor CORE_115 = new LDrawStdColor(115, ColorType.CORE, 0xC7D23C, 0x333333, 0);
    public static final LDrawStdColor CORE_118 = new LDrawStdColor(118, ColorType.CORE, 0xB3D7D1, 0x333333, 0);
    public static final LDrawStdColor CORE_120 = new LDrawStdColor(120, ColorType.CORE, 0xD9E4A7, 0x333333, 0);
    public static final LDrawStdColor CORE_125 = new LDrawStdColor(125, ColorType.CORE, 0xF9BA61, 0x333333, 0);
    public static final LDrawStdColor CORE_152 = new LDrawStdColor(152, ColorType.CORE, 0xE6E3E0, 0x333333, 0);
    public static final LDrawStdColor CORE_191 = new LDrawStdColor(191, ColorType.CORE, 0xF8BB3D, 0x333333, 0);
    public static final LDrawStdColor CORE_212 = new LDrawStdColor(212, ColorType.CORE, 0x9FC3E9, 0x333333, 0);
    public static final LDrawStdColor CORE_216 = new LDrawStdColor(216, ColorType.CORE, 0xB31004, 0x333333, 0);
    public static final LDrawStdColor CORE_226 = new LDrawStdColor(226, ColorType.CORE, 0xFFF03A, 0x333333, 0);
    public static final LDrawStdColor CORE_232 = new LDrawStdColor(232, ColorType.CORE, 0x7DBFDD, 0x333333, 0);
    public static final LDrawStdColor CORE_272 = new LDrawStdColor(272, ColorType.CORE, 0x0A3463, 0x1E1E1E, 0);
    public static final LDrawStdColor CORE_288 = new LDrawStdColor(288, ColorType.CORE, 0x184632, 0x333333, 0);
    public static final LDrawStdColor CORE_308 = new LDrawStdColor(308, ColorType.CORE, 0x352100, 0x000000, 0);
    public static final LDrawStdColor CORE_313 = new LDrawStdColor(313, ColorType.CORE, 0x3592C3, 0x333333, 0);
    public static final LDrawStdColor CORE_320 = new LDrawStdColor(320, ColorType.CORE, 0x720E0F, 0x333333, 0);
    public static final LDrawStdColor CORE_321 = new LDrawStdColor(321, ColorType.CORE, 0x078BC9, 0x088DCD, 0);   // *****
    public static final LDrawStdColor CORE_323 = new LDrawStdColor(323, ColorType.CORE, 0xADC3C0, 0xAFC9C2, 0);   // *****
    public static final LDrawStdColor CORE_335 = new LDrawStdColor(335, ColorType.CORE, 0xD67572, 0x333333, 0);
    public static final LDrawStdColor CORE_351 = new LDrawStdColor(351, ColorType.CORE, 0xF785B1, 0x333333, 0);   // *****
    public static final LDrawStdColor CORE_366 = new LDrawStdColor(366, ColorType.CORE, 0xFA9C1C, 0x333333, 0);
    public static final LDrawStdColor CORE_373 = new LDrawStdColor(373, ColorType.CORE, 0x845E84, 0x333333, 0);
    public static final LDrawStdColor CORE_378 = new LDrawStdColor(378, ColorType.CORE, 0xA0BCAC, 0x333333, 0);
    public static final LDrawStdColor CORE_379 = new LDrawStdColor(379, ColorType.CORE, 0x6074A1, 0x333333, 0);
    public static final LDrawStdColor CORE_450 = new LDrawStdColor(450, ColorType.CORE, 0xB67B50, 0x333333, 0);   // *****
    public static final LDrawStdColor CORE_462 = new LDrawStdColor(462, ColorType.CORE, 0xFFA70B, 0x333333, 0);
    public static final LDrawStdColor CORE_484 = new LDrawStdColor(484, ColorType.CORE, 0xA95500, 0x333333, 0);
    public static final LDrawStdColor CORE_503 = new LDrawStdColor(503, ColorType.CORE, 0xE6E3DA, 0x333333, 0);

    public static final LDrawStdColor TRANSPARENT_33 = new LDrawStdColor(33, ColorType.TRANSPARENT, 0x0020A0, 0x000064, 128);
    public static final LDrawStdColor TRANSPARENT_34 = new LDrawStdColor(34, ColorType.TRANSPARENT, 0x84B68D, 0x002800, 128);
    public static final LDrawStdColor TRANSPARENT_35 = new LDrawStdColor(35, ColorType.TRANSPARENT, 0xD9E4A7, 0x9DA86B, 128);
    public static final LDrawStdColor TRANSPARENT_36 = new LDrawStdColor(36, ColorType.TRANSPARENT, 0xC91A09, 0x880000, 128);
    public static final LDrawStdColor TRANSPARENT_37 = new LDrawStdColor(37, ColorType.TRANSPARENT, 0xDF6695, 0xA32A59, 128);
    public static final LDrawStdColor TRANSPARENT_38 = new LDrawStdColor(38, ColorType.TRANSPARENT, 0xFF800D, 0xBD2400, 128);    // *****
    public static final LDrawStdColor TRANSPARENT_39 = new LDrawStdColor(39, ColorType.TRANSPARENT, 0xC1DFF0, 0x85A3B4, 128);    // *****
    public static final LDrawStdColor TRANSPARENT_40 = new LDrawStdColor(40, ColorType.TRANSPARENT, 0x635F52, 0x171316, 128);
    public static final LDrawStdColor TRANSPARENT_41 = new LDrawStdColor(41, ColorType.TRANSPARENT, 0x559AB7, 0x196973, 128);
    public static final LDrawStdColor TRANSPARENT_42 = new LDrawStdColor(42, ColorType.TRANSPARENT, 0xC0FF00, 0x84C300, 128);
    public static final LDrawStdColor TRANSPARENT_43 = new LDrawStdColor(43, ColorType.TRANSPARENT, 0xAEE9EF, 0x72B3B0, 128);
    public static final LDrawStdColor TRANSPARENT_44 = new LDrawStdColor(44, ColorType.TRANSPARENT, 0x96709F, 0x5A3463, 128);    // *****
    public static final LDrawStdColor TRANSPARENT_45 = new LDrawStdColor(45, ColorType.TRANSPARENT, 0xFC97AC, 0xA8718C, 128);
    public static final LDrawStdColor TRANSPARENT_46 = new LDrawStdColor(46, ColorType.TRANSPARENT, 0xF5CD2F, 0x8E7400, 128);
    public static final LDrawStdColor TRANSPARENT_47 = new LDrawStdColor(47, ColorType.TRANSPARENT, 0xFCFCFC, 0xC3C3C3, 128);
    public static final LDrawStdColor TRANSPARENT_52 = new LDrawStdColor(52, ColorType.TRANSPARENT, 0xA5A5CB, 0x280025, 128);    // *****
    public static final LDrawStdColor TRANSPARENT_54 = new LDrawStdColor(54, ColorType.TRANSPARENT, 0xDAB000, 0xC3BA3F, 128);    // *****
    public static final LDrawStdColor TRANSPARENT_57 = new LDrawStdColor(57, ColorType.TRANSPARENT, 0xF08F1C, 0xA45C28, 128);

    public static final LDrawStdColor INTERNAL_16 = new LDrawStdColor(16, ColorType.INTERNAL, 0x7F7F7F, 0x333333, 0);
    public static final LDrawStdColor INTERNAL_24 = new LDrawStdColor(24, ColorType.INTERNAL, 0x7F7F7F, 0x333333, 0);
    public static final LDrawStdColor INTERNAL_32 = new LDrawStdColor(32, ColorType.INTERNAL, 0x000000, 0x05131D, 220);
    public static final LDrawStdColor INTERNAL_494 = new LDrawStdColor(494, ColorType.INTERNAL, 0xD0D0D0, 0x6E6E6E, 0);
    public static final LDrawStdColor INTERNAL_495 = new LDrawStdColor(495, ColorType.INTERNAL, 0xAE7A59, 0x723E1D, 0);

    // Not defined Transparent: 143, 157, 182, 227, 228, 230, 231, 234, 236, 284

    private LDrawStdColor(int idx,
                          ColorType ct,
                          int hexColour,
                          int hexComplement,
                          int a)
    {
        super(idx, ct, hexColour, hexComplement, a);
    }
}
