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
 * A definitive collection of all known chunk identifiers in the Discreet 3DS
 * Max file format.
 * <p>
 *
 * The definition this works from is can be found at:
 * <a href="http://www.the-labs.com/Blender/3DS-details.html">
 * http://www.the-labs.com/Blender/3DS-details.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public interface MaxConstants
{
    // 0x0XXXH group

    /** The NULL chunk (0x0000) */
    public static final int NULL_CHUNK = 0x0000;

    /** The chunk is version number (0x0002) */
    public static final int VERSION = 0x0002;

    /** The chunk is version number (0x0005) */
    public static final int ID2_VERSION = 0x0005;

    /** The information is for a float color (0x0010) */
    public static final int COLORF = 0x0010;

    /** The information is for a 24bit colour (0x0011) */
    public static final int COLOR24 = 0x0011;

    /** The information is for a 24bit colour (0x0012) */
    public static final int LIN_COLOR24 = 0x0012;

    /** The information is for a float colour (0x0013) */
    public static final int LIN_COLORF = 0x0013;

    /** The information is for a percentage in int (0x0030) */
    public static final int INT_PERCENT = 0x0030;

    /** The information is for a percentage in flot (0x0031) */
    public static final int FLOAT_PERCENT = 0x0031;

    /** The chunk is master object scale (0x0100) */
    public static final int MASTER_SCALE = 0x0100;

    public static final int CHUNK_TYPE = 0x0995;
    public static final int CHUNK_UNIQUE = 0x0996;
    public static final int CHUNK_NOT = 0x0997;
    public static final int CONTAINER = 0x0998;
    public static final int CHUNK_IS = 0x0999;

    // 1xxx group

    public static final int BITMAP = 0x1100;
    public static final int USE_BITMAP = 0x1101;

    /** The ID is for a solid colour background */
    public static final int SOLBG = 0x1200;

    /** The ID is for a solid colour background */
    public static final int USE_SOLBG = 0x1200;

    /** The ID is for a vertical gradient */
    public static final int V_GRADIENT = 0x1300;

    /** The ID is for a lo res shadow bias (0x1410) */
    public static final int LOW_SHADOW_BIAS = 0x1400;

    /** The ID is for a hi res shadow bias (0x1410) */
    public static final int HI_SHADOW_BIAS = 0x1410;

    /** The ID is for the size of a shadow map (0x1420) */
    public static final int SHADOW_MAP_SIZE = 0x1420;

    /** The ID is for the shadow map samples (0x1430) */
    public static final int SHADOW_MAP_SAMPLES = 0x1430;

    /** The ID is for the shadow map range (0x1440) */
    public static final int SHADOW_MAP_RANGE = 0x1440;

    /** The ID is for the shadow map filter (0x1450) */
    public static final int SHADOW_MAP_FILTER = 0x1450;

    /** The ID is for a ray bias (0x1460) */
    public static final int RAY_BIAS = 0x1460;

    /** The ID is for plane constants (0x1500) */
    public static final int O_CONST = 0x1500;

    // 2xxx group

    /** The ID is for ambient light (0x2100) */
    public static final int AMBIENT_LIGHT = 0x2100;

    /** The ID is for fog (0x2200) */
    public static final int FOG = 0x2200;

    /** The ID is for using another fog (0x2201) */
    public static final int USE_FOG = 0x2201;

    /** The ID is for a fog background (0x2210) */
    public static final int FOG_BACKGROUND = 0x2210;

    /** The ID is for the distant cue for background (0x2300) */
    public static final int DISTANCE_CUE = 0x2300;

    /** The ID is for using another distance cue (0x2301) */
    public static final int USE_DISTANCE_CUE = 0x2301;

    /** The ID is for layered fog (0x2302) */
    public static final int LAYER_FOG = 0x2302;

    /** The ID is for using layered foh (0x2303) */
    public static final int USE_LAYER_FOG = 0x2303;

    /** The ID is for a distance cued background (0x2310) */
    public static final int DCUE_BACKGROUND = 0x2310;

    /** The ID is a small magic number (0x2d2d) */
    public static final int SMAGIC = 0x2d2d;

    /** The ID is a large magic number (0x2d2d) */
    public static final int LMAGIC = 0x2d3d;

    // 3xxx group

    /** The ID is for the default view (0x3000) */
    public static final int DEFAULT_VIEW = 0x3000;

    /** The ID is for the top view (0x3010) */
    public static final int TOP_VIEW = 0x3010;

    /** The ID is for the bottom view (0x3020) */
    public static final int BOTTOM_VIEW = 0x3020;

    /** The ID is for the left view (0x3030) */
    public static final int LEFT_VIEW = 0x3030;

    /** The ID is for the right view (0x3040) */
    public static final int RIGHT_VIEW = 0x3040;

    /** The ID is for the front view (0x3050) */
    public static final int FRONTVIEW = 0x3050;

    /** The ID is for the back view (0x3060) */
    public static final int BACK_VIEW = 0x3060;

    /** The ID is for the user view (0x3070) */
    public static final int USER_VIEW = 0x3070;

    /** The ID is for the camera view (0x3080) */
    public static final int CAMERA_VIEW = 0x3080;

    /** The ID is for the window view (0x3090) */
    public static final int WINDOW_VIEW = 0x3090;


    /** The ID is for the 3D editor header magic number (0x3d3d) */
    public static final int MESH_DATA = 0x3d3d;

    /** The ID is the version of the mesh being read (0x3d3e) */
    public static final int MESH_VERSION = 0x3D3E;

    /** The block ID is for Material Library info (0x3daa) */
    public static final int MATERIAL_LIB = 0x3DAA;

    /** The block ID is for Project (.prj file ) info (0x3dc2) */
    public static final int PROJECT_DATA = 0x3DC2;

    /** The block ID is for Material (.mat) file (0x3dff) */
    public static final int MATERIAL_FILE = 0x3DFF;

    // 4xxx group

    /** The block ID is for a new object mesh (0x4000) */
    public static final int NAMED_OBJECT = 0x4000;

    /** The ID for a hidden object (0x4010) */
    public static final int OBJ_HIDDEN = 0x4010;

    /** The ID for a visual lofter object (0x4011) */
    public static final int VIS_LOFTER = 0x4011;

    /** The ID for an object that doesn't cast a shadow (0x4012) */
    public static final int NO_CAST = 0x4012;

    /** The ID for a matte object (0x4013) */
    public static final int OBJ_MATTE = 0x4013;

    /** The ID for a fast (!) object (0x4014) */
    public static final int OBJ_FAST = 0x4014;

    /** The ID for a procedural object (0x4015) */
    public static final int OBJ_PROCEDURAL = 0x4015;

    /** The ID for a frozen object (0x4016) */
    public static final int OBJ_FROZEN = 0x4016;

    /** The ID for an object that is not shadowed (0x4017) */
    public static final int OBJ_NOT_SHADOWED = 0x4017;

    /** The mesh is a triangle mesh */
    public static final int TRI_MESH = 0x4100;

    /** The ID represents the start of the vertex list */
    public static final int VERTEX_LIST = 0x4110;

    /** The ID represents the start of the vertex flag list */
    public static final int VERTEX_FLAG = 0x4111;

    /** The ID represents the start of the face list */
    public static final int FACE_LIST = 0x4120;

    /** The ID represents the start of the material list */
    public static final int MATERIAL_LIST = 0x4130;

    /** The ID represents an old-style material list */
    public static final int MATERIAL_OLD = 0x4131;

    /** The ID represents the sate of the UV texture map coords list */
    public static final int TEXCOORD_LIST = 0x4140;

    /** The ID represents the start of the smooth shading list */
    public static final int SMOOTH_LIST = 0x4150;

    /** The ID represents the start of a mesh matrix */
    public static final int MESH_MATRIX = 0x4160;

    /** The ID represents the mesh colour (0x4165) */
    public static final int MESH_COLOR = 0x4165;

    /** The ID represents the start mesh texture info (0x4170) */
    public static final int MESH_TEXTURE_INFO = 0x4170;

    /** The ID represents the start of a procedure (0x4181) */
    public static final int PROCEDURE_NAME = 0x4181;

    /** The ID represents the start of a procedure data (0x4182) */
    public static final int PROCEDURE_DATA = 0x4182;

    /** The ID represents the start of a cubic env map (0x4190) */
    public static final int BOX_MAP = 0x4190;

    /** The object ID represents an old normal data (0x4400) */
    public static final int N_D_L_OLD = 0x4400;

    /** The object ID represents an old camera data (0x4500) */
    public static final int CAMERA_OLD = 0x4500;

    /** The ID represents the number of directional lights (0x4600) */
    public static final int N_DIRECTIONAL_LIGHT = 0x4600;

    /** The ID represents a spot light (0x4610) */
    public static final int SPOT_LIGHT = 0x4610;

    /** The ID represents the directional light off or on (0x4620) */
    public static final int DIR_LIGHT_OFF = 0x4620;

    /** The ID represents the directional light attenuation (0x4625) */
    public static final int DIR_LIGHT_ATTENUATION = 0x4625;

    /** The ID represents the directional light ray shaded (0x4627) */
    public static final int DIR_LIGHT_RAYSHAD = 0x4627;

    /** The ID represents the directional light shadowed (0x4630) */
    public static final int DIR_LIGHT_SHADOWED = 0x4630;

    /** The ID represents the directional light local shadow (0x4640) */
    public static final int DIR_LIGHT_LOCAL_SHADOW = 0x4640;

    /** The ID represents the directional light local shadow2 (0x4641) */
    public static final int DIR_LIGHT_LOCAL_SHADOW2 = 0x4641;

    /** The ID represents the directional light visible output (0x4650) */
    public static final int DIR_LIGHT_SEE_CONE = 0x4650;

    /** The ID represents the spot light as a rectangle source (0x4651) */
    public static final int DIR_LIGHT_SPOT_RECT = 0x4651;

    /** The ID represents the spot light overshoot (0x4652) */
    public static final int DIR_LIGHT_SPOT_OVERSHOOT = 0x4652;

    /** The ID represents the spot light projector (0x4653) */
    public static final int DIR_LIGHT_SPOT_PROJECTOR = 0x4653;

    public static final int DIR_LIGHT_EXCLUDE = 0x4654;
    public static final int DIR_LIGHT_RANGE = 0x4655;

    /** The ID represents a spotlight rolloff angle (0x4656) */
    public static final int DIR_LIGHT_SPOT_ROLLOFF = 0x4656;

    /** The ID represents a spotlight aspect ratio (0x4657) */
    public static final int DIR_LIGHT_SPOT_ASPECTRATIO = 0x4657;


    public static final int DIR_LIGHT_RAY_BIAS = 0x4658;

    /** The ID represents the spotlight inner range angle */
    public static final int DIR_LIGHT_INNER_RANGE = 0x4659;

    /** The ID represents the spotlight outer range angle */
    public static final int DIR_LIGHT_OUTER_RANGE = 0x465a;

    /** The ID represents the spotlight multiplier */
    public static final int DIR_LIGHT_MULTIPLIER = 0x465b;

    /** The ID represents number of ambient lights (0x4680) */
    public static final int N_AMBIENT_LIGHT = 0x4680;

    /** The ID represents number of cameras (0x4700) */
    public static final int N_CAMERA = 0x4700;

    /** The ID represents the camera see view cone (0x4710) */
    public static final int CAMERA_SEE_CONE = 0x4710;

    /** The ID represents the camera range (clip params) (0x4720) */
    public static final int CAMERA_RANGE = 0x4720;

    /** The ID is for the header magic number (0x4d4d) */
    public static final int MAIN_CHUNK = 0x4D4D;

    /** The ID represents a heirarchy (0x4F00) */
    public static final int HEIRARCHY = 0x4F00;

    /** The ID represents a parent object pointer (0x4F10) */
    public static final int PARENT_OBJECT = 0x4F10;

    /** The ID represents a pivot object (0x4F20) */
    public static final int PIVOT_OBJECT = 0x4F20;

    /** The ID represents pivot limits (0x4F30) */
    public static final int PIVOT_LIMITS = 0x4F30;

    /** The ID represents pivot order (0x4F40) */
    public static final int PIVOT_ORDER = 0x4F40;

    /** The ID represents range translate (0x4F50) */
    public static final int TRANSLATE_RANGE = 0x4F50;

    // 5xxx group

    /** The block ID represents a 2D polygon (0x5000) */
    public static final int POLY_2D = 0x5000;

    /** The ID represents a valid shape (0x5010) */
    public static final int SHAPE_OK = 0x5010;

    /** The ID represents a invalid shape (0x5011) */
    public static final int SHAPE_BAD = 0x5011;

    /** The ID represents a shape hook (0x5020) */
    public static final int SHAPE_HOOK = 0x5020;

    // 6xxx group

    /** The ID represents a 3D path (0x6000) */
    public static final int PATH_3D = 0x6000;
    public static final int PATH_MATRIX = 0x6005;
    public static final int SHAPE_2D = 0x6010;
    public static final int M_SCALE = 0x6020;
    public static final int M_TWIST = 0x6030;
    public static final int M_TEETER = 0x6040;
    public static final int M_FIT = 0x6050;
    public static final int M_BEVEL = 0x6060;
    public static final int XZ_CURVE = 0x6070;
    public static final int YZ_CURVE = 0x6080;
    public static final int INTERPCT = 0x6090;
    public static final int DEFORM_LIMIT = 0x60a0;
    public static final int USE_CONTOUR = 0x6100;
    public static final int USE_TWEEN = 0x6110;
    public static final int USE_SCALE = 0x6120;
    public static final int USE_TWIST = 0x6130;
    public static final int USE_TEETER = 0x6140;
    public static final int USE_FIT = 0x6150;
    public static final int USE_BEVEL = 0x6160;

    // 7xxx group
    public static final int VIEWPORT_LAYOUT_OLD = 0x7000;
    public static final int VIEWPORT_LAYOUT = 0x7001;
    public static final int VIEWPORT_DATA_OLD = 0x7010;
    public static final int VIEWPORT_DATA = 0x7011;
    public static final int VIEWPORT_DATA_3 = 0x0712;
    public static final int VIEWPORT_SIZE = 0x7020;
    public static final int NETWORK_VIEW = 0x7030;

    // 8xxx group
    public static final int XDATA_SECTION = 0x8000;
    public static final int XDATA_ENTRY = 0x8001;
    public static final int XDATA_APPNAME = 0x8002;
    public static final int XDATA_STRING = 0x8003;
    public static final int XDATA_FLOAT = 0x8004;
    public static final int XDATA_DOUBLE = 0x8005;
    public static final int XDATA_SHORT = 0x8006;
    public static final int XDATA_LONG = 0x8007;
    public static final int XDATA_VOID = 0x8008;
    public static final int XDATA_GROUP = 0x8009;
    public static final int XDATA_RFU6 = 0x800a;
    public static final int XDATA_RFU5 = 0x800b;
    public static final int XDATA_RFU4 = 0x800c;
    public static final int XDATA_RFU3 = 0x800d;
    public static final int XDATA_RFU2 = 0x800e;
    public static final int XDATA_RFU1 = 0x800f;
    public static final int PARENT_NAME = 0x80f0;

    // Axxx Group - Material information

    /** The name of a material (0xA000) */
    public static final int MAT_NAME = 0xA000;

    /** ID represents the ambient color (0xA010) */
    public static final int MAT_AMBIENT = 0xA010;

    /** ID represents the diffuse color (0xA020) */
    public static final int MAT_DIFFUSE = 0xA020;

    /** ID represents the specual color (0xA030) */
    public static final int MAT_SPECULAR = 0xA030;

    /** ID represents the shininess ratio (0xA040) */
    public static final int MAT_SHININESS = 0xA040;

    /** ID represents the shininess strength (0xA041) */
    public static final int MAT_SHIN2PCT = 0xA041;

    /** ID represents the shininess second strength factor (0xA042) */
    public static final int MAT_SHIN3PCT = 0xA042;

    /** ID represents the transparency (0xA050) */
    public static final int MAT_TRANSPARENCY = 0xA050;
    public static final int MAT_XPFALL = 0xA052;
    public static final int MAT_REFBLUR = 0xA053;
    public static final int MAT_SELF_ILLUM = 0xA080;

    /** ID represents two-sided lighting should be used (0xA081) */
    public static final int MAT_TWO_SIDE = 0xA081;

    /** ID represents a decalled texture (0xA082) */
    public static final int MAT_DECAL = 0xA082;

    /** ID represents an additive transparency blend (0xA083) */
    public static final int MAT_ADDITIVE = 0xA083;

    public static final int MAT_SELF_ILPCT = 0xA084;

    /** ID represents the flag for rendering in wireframe */
    public static final int MAT_WIREFRAME = 0xA085;

    public static final int MAT_SUPERSMP = 0xA086;

    /** ID represents the wire thickness (0xA087) */
    public static final int MAT_WIRESIZE = 0xA087;


    public static final int MAT_FACEMAP = 0xA088;
    public static final int MAT_XPFALLIN = 0xA08a;
    public static final int MAT_PHONGSOFT = 0xA08c;
    public static final int MAT_WIREABS = 0xA08e;

    /** ID represents the shading style */
    public static final int MAT_SHADING = 0xA100;

    /** ID represents the base texture map to apply (0xA200) */
    public static final int MAT_TEXMAP = 0xA200;

    /** ID represents the specular texture map to apply (0xA204) */
    public static final int MAT_SPECMAP = 0xA204;

    /** ID represents the opacity texture map to apply (0xA210) */
    public static final int MAT_OPACMAP = 0xA210;

    /** ID represents the reflection texture map to apply (0xA220) */
    public static final int MAT_REFLMAP = 0xA220;

    /** ID represents the bump texture map to apply (0xA230) */
    public static final int MAT_BUMPMAP = 0xA230;


    public static final int MAT_USE_XPFALL = 0xA240;
    public static final int MAT_USE_REFBLUR = 0xA250;

    /** ID represents a percentage scale of the bumpmap to apply */
    public static final int MAT_BUMP_PERCENT = 0xA252;

    /** ID represents the name of the texture file to use (0xA300) */
    public static final int MAT_MAPNAME = 0xA300;

    /** ID represents automatic cube map generation (0xA3100) */
    public static final int MAT_ACUBIC = 0xA310;

    public static final int MAT_SXP_TEXT_DATA = 0xA320;
    public static final int MAT_SXP_TEXT2_DATA = 0xA321;
    public static final int MAT_SXP_OPAC_DATA = 0xA322;
    public static final int MAT_SXP_BUMP_DATA = 0xA324;
    public static final int MAT_SXP_SPEC_DATA = 0xA325;
    public static final int MAT_SXP_SHIN_DATA = 0xA326;
    public static final int MAT_SXP_SELFI_DATA = 0xA328;
    public static final int MAT_SXP_TEXT_MASKDATA = 0xA32a;
    public static final int MAT_SXP_TEXT2_MASKDATA = 0xA32c;
    public static final int MAT_SXP_OPAC_MASKDATA = 0xA32e;
    public static final int MAT_SXP_BUMP_MASKDATA = 0xA330;
    public static final int MAT_SXP_SPEC_MASKDATA = 0xA332;
    public static final int MAT_SXP_SHIN_MASKDATA = 0xA334;
    public static final int MAT_SXP_SELFI_MASKDATA = 0xA336;
    public static final int MAT_SXP_REFL_MASKDATA = 0xA338;

    /** ID represents a second base material texture map (0xA33A) */
    public static final int MAT_TEX2MAP = 0xA33a;

    /** ID represents the shininess map (0xA33C) */
    public static final int MAT_SHINMAP = 0xA33c;

    public static final int MAT_SELFIMAP= 0xA33d;
    public static final int MAT_TEXMASK = 0xA33e;
    public static final int MAT_TEX2MASK = 0xA340;
    public static final int MAT_OPACMASK = 0xA342;
    public static final int MAT_BUMPMASK = 0xA344;
    public static final int MAT_SHINMASK = 0xA346;
    public static final int MAT_SPECMASK = 0xA348;
    public static final int MAT_SELFIMASK = 0xA34a;
    public static final int MAT_REFLMASK = 0xA34c;
    public static final int MAT_MAP_TILINGOLD = 0xA350;

    /** ID represents the texture map tiling param to apply (0xA351) */
    public static final int MAT_MAP_TILING = 0xA351;

    /** ID represents an outdated texture blur param to apply (0xA352) */
    public static final int MAT_MAP_TEXBLUR_OLD = 0xA352;

    /** ID represents an texture blur param to apply (0xA353) */
    public static final int MAT_MAP_TEXBLUR = 0xA353;

    /** ID represents an texture u-coord scale (0xA354) */
    public static final int MAT_MAP_USCALE = 0xA354;

    /** ID represents an texture v-coord scale (0xA356) */
    public static final int MAT_MAP_VSCALE = 0xA356;

    /** ID represents an texture u translation (0xA358) */
    public static final int MAT_MAP_UOFFSET = 0xA358;

    /** ID represents an texture v translation (0xA35A) */
    public static final int MAT_MAP_VOFFSET = 0xA35A;

    /** ID represents an texture rotation angle (0xA35C) */
    public static final int MAT_MAP_ANG = 0xA35C;

    /** ID represents the first blend colour for textures (0xA360) */
    public static final int MAT_MAP_COL1 = 0xA360;

    /** ID represents the second blend colour for textures (0xA360) */
    public static final int MAT_MAP_COL2 = 0xA362;

    /** ID represents the red blend colour for textures (0xA360) */
    public static final int MAT_MAP_RCOL = 0xA364;

    /** ID represents the green blend colour for textures (0xA360) */
    public static final int MAT_MAP_GCOL = 0xA366;

    /** ID represents the blue blend colour for textures (0xA360) */
    public static final int MAT_MAP_BCOL = 0xA368;

    /** The object ID represents a piece of material */
    public static final int MATERIAL_BLOCK = 0xAFFF;

    // Bxxx group   - Animation and keyframe data

    /** The block ID represents a keyframe set */
    public static final int KEYFRAME_CHUNK = 0xB000;

    /** The ID represents the ambient keyframe data (0xB001) */
    public static final int AMBIENT_NODE_TAG = 0xB001;

    /** The ID represents the object keyframe data (0xB002) */
    public static final int OBJECT_NODE_TAG = 0xB002;

    /** The ID represents the camera keyframe data (0xB003) */
    public static final int CAMERA_NODE_TAG = 0xB003;

    /** The ID represents the target keyframe data (0xB004) */
    public static final int TARGET_NODE_TAG = 0xB004;

    /** The ID represents the light keyframe data (0xB005) */
    public static final int LIGHT_NODE_TAG = 0xB005;

    /** The ID represents the light-target keyframe data (0xB006) */
    public static final int LIGHT_TARGET_NODE_TAG = 0xB006;

    /** The ID represents the spotlight keyframe data (0xB007) */
    public static final int SPOTLIGHT_NODE_TAG = 0xB007;

    /** The ID represents the keyframe segment data (0xB008) */
    public static final int KEYFRAME_SEGMENT = 0xB008;

    /** The ID represents the current keyframe time (0xB009) */
    public static final int KEYFRAME_CURRENT_TIME = 0xB009;

    /** The ID represents the keyframe header block (0xB00A) */
    public static final int KEYFRAME_HEADER = 0xB00A;

    /** The ID represents the keyframe node header block (0xB010) */
    public static final int KEYFRAME_NODE_HEADER = 0xB010;

    /** The ID represents the keyframe instance name (0xB011) */
    public static final int KEYFRAME_INSTANCE_NAME = 0xB011;

    /** The ID represents the keyframe prescale (0xB012) */
    public static final int KEYFRAME_PRESCALE = 0xB012;

    /** The ID represents the keyframe pivot coordinate (0xB013) */
    public static final int KEYFRAME_PIVOT = 0xB013;

    /** The ID represents the keyframe bounding box (0xB14) */
    public static final int KEYFRAME_BOUNDS = 0xB014;

    /** The ID represents the keyframe smooth morph value (0xB015) */
    public static final int KEYFRAME_SMOOTH_MORPH = 0xB015;

    /** The ID represents the keyframe track position block (0xB020) */
    public static final int KEYFRAME_POSITION_TRACK_TAG = 0xB020;

    /** The ID represents the keyframe track rotation block (0xB021) */
    public static final int KEYFRAME_ROTATION_TRACK_TAG = 0xB021;

    /** The ID represents the keyframe track scale block (0xB022) */
    public static final int KEYFRAME_SCALE_TRACK_TAG = 0xB022;

    /** The ID represents the keyframe camera field of view block (0xB023) */
    public static final int KEYFRAME_FOV_TRACK_TAG = 0xB023;

    /** The ID represents the keyframe camera roll position block (0xB024) */
    public static final int KEYFRAME_ROLL_TRACK_TAG = 0xB024;

    /** The ID represents the keyframe track color block (0xB025) */
    public static final int KEYFRAME_COLOR_TRACK_TAG = 0xB025;

    /** The ID represents the keyframe track morph block (0xB026) */
    public static final int KEYFRAME_MORPH_TRACK_TAG = 0xB026;

    /** The ID represents the keyframe track light hotspot block (0xB027) */
    public static final int KEYFRAME_HOTSPOT_TRACK_TAG = 0xB027;

    /** The ID represents the keyframe track light falloff block (0xB028) */
    public static final int KEYFRAME_FALLOFF_TRACK_TAG = 0xB028;

    /** The ID represents the keyframe track hidden block (0xB029) */
    public static final int KEYFRAME_HIDE_TRACK_TAG = 0xB029;

    /** The ID represents the keyframe node ID (0xB030) */
    public static final int KEYFRAME_NODE_ID = 0xB030;


    // Cxxx Group
    public static final int C_MDRAWER              = 0xC010;
    public static final int C_TDRAWER              = 0xC020;
    public static final int C_SHPDRAWER            = 0xC030;
    public static final int C_MODDRAWER            = 0xC040;
    public static final int C_RIPDRAWER            = 0xC050;
    public static final int C_TXDRAWER             = 0xC060;
    public static final int C_PDRAWER              = 0xC062;
    public static final int C_MTLDRAWER            = 0xC064;
    public static final int C_FLIDRAWER            = 0xC066;
    public static final int C_CUBDRAWER            = 0xC067;
    public static final int C_MFILE                = 0xC070;
    public static final int C_SHPFILE              = 0xC080;
    public static final int C_MODFILE              = 0xC090;
    public static final int C_RIPFILE              = 0xC0a0;
    public static final int C_TXFILE               = 0xC0b0;
    public static final int C_PFILE                = 0xC0b2;
    public static final int C_MTLFILE              = 0xC0b4;
    public static final int C_FLIFILE              = 0xC0b6;
    public static final int C_PALFILE              = 0xC0b8;
    public static final int C_TX_STRING            = 0xC0c0;
    public static final int C_CONSTS               = 0xC0d0;
    public static final int C_SNAPS                = 0xC0e0;
    public static final int C_GRIDS                = 0xC0f0;
    public static final int C_ASNAPS               = 0xC100;
    public static final int C_GRRANGE           = 0xC110;
    public static final int C_RENDTYPE             = 0xC120;
    public static final int C_PROGMODE             = 0xC130;
    public static final int C_PREVMODE             = 0xC140;
    public static final int C_MODWMODE             = 0xC150;
    public static final int C_MODMODEL             = 0xC160;
    public static final int C_ALL_LINES            = 0xC170;
    public static final int C_BACK_TYPE            = 0xC180;
    public static final int C_MD_CS                = 0xC190;
    public static final int C_MD_CE                = 0xC1a0;
    public static final int C_MD_SML               = 0xC1b0;
    public static final int C_MD_SMW               = 0xC1c0;
    public static final int C_LOFT_WITH_TEXTURE    = 0xC1c3;
    public static final int C_LOFT_L_REPEAT        = 0xC1c4;
    public static final int C_LOFT_W_REPEAT        = 0xC1c5;
    public static final int C_LOFT_UV_NORMALIZE    = 0xC1c6;
    public static final int C_WELD_LOFT            = 0xC1c7;
    public static final int C_MD_PDET              = 0xC1d0;
    public static final int C_MD_SDET              = 0xC1e0;
    public static final int C_RGB_RMODE            = 0xC1f0;
    public static final int C_RGB_HIDE             = 0xC200;
    public static final int C_RGB_MAPSW            = 0xC202;
    public static final int C_RGB_TWOSIDE          = 0xC204;
    public static final int C_RGB_SHADOW           = 0xC208;
    public static final int C_RGB_AA               = 0xC210;
    public static final int C_RGB_OVW              = 0xC220;
    public static final int C_RGB_OVH             = 0xC230;
    public static final int CMAGIC                 = 0xC23d;
    public static final int C_RGB_PICTYPE          = 0xC240;
    public static final int C_RGB_OUTPUT           = 0xC250;
    public static final int C_RGB_TODISK           = 0xC253;
    public static final int C_RGB_COMPRESS         = 0xC254;
    public static final int C_JPEG_COMPRESSION     = 0xC255;
    public static final int C_RGB_DISPDEV          = 0xC256;
    public static final int C_RGB_HARDDEV          = 0xC259;
    public static final int C_RGB_PATH            = 0xC25a;
    public static final int C_BITMAP_DRAWER        = 0xC25b;
    public static final int C_RGB_FILE             = 0xC260;
    public static final int C_RGB_OVASPECT         = 0xC270;
    public static final int C_RGB_ANIMTYPE         = 0xC271;
    public static final int C_RENDER_ALL           = 0xC272;
    public static final int C_REND_FROM            = 0xC273;
    public static final int C_REND_TO              = 0xC274;
    public static final int C_REND_NTH            = 0xC275;
    public static final int C_PAL_TYPE             = 0xC276;
    public static final int C_RND_TURBO            = 0xC277;
    public static final int C_RND_MIP              = 0xC278;
    public static final int C_BGND_METHOD          = 0xC279;
    public static final int C_AUTO_REFLECT         = 0xC27a;
    public static final int C_VP_FROM              = 0xC27b;
    public static final int C_VP_TO                = 0xC27c;
    public static final int C_VP_NTH              = 0xC27d;
    public static final int C_REND_TSTEP           = 0xC27e;
    public static final int C_VP_TSTEP             = 0xC27f;
    public static final int C_SRDIAM               = 0xC280;
    public static final int C_SRDEG                = 0xC290;
    public static final int C_SRSEG                = 0xC2a0;
    public static final int C_SRDIR                = 0xC2b0;
    public static final int C_HETOP                = 0xC2c0;
    public static final int C_HEBOT                = 0xC2d0;
    public static final int C_HEHT                 = 0xC2e0;
    public static final int C_HETURNS              = 0xC2f0;
    public static final int C_HEDEG                = 0xC300;
    public static final int C_HESEG                = 0xC310;
    public static final int C_HEDIR                = 0xC320;
    public static final int C_QUIKSTUFF            = 0xC330;
    public static final int C_SEE_LIGHTS           = 0xC340;
    public static final int C_SEE_CAMERAS          = 0xC350;
    public static final int C_SEE_3D               = 0xC360;
    public static final int C_MESHSEL              = 0xC370;
    public static final int C_MESHUNSEL            = 0xC380;
    public static final int C_POLYSEL              = 0xC390;
    public static final int C_POLYUNSEL            = 0xC3a0;
    public static final int C_SHPLOCAL             = 0xC3a2;
    public static final int C_MSHLOCAL             = 0xC3a4;
    public static final int C_NUM_FORMAT           = 0xC3b0;
    public static final int C_ARCH_DENOM           = 0xC3c0;
    public static final int C_IN_DEVICE            = 0xC3d0;
    public static final int C_MSCALE               = 0xC3e0;
    public static final int C_COMM_PORT            = 0xC3f0;
    public static final int C_TAB_BASES            = 0xC400;
    public static final int C_TAB_DIVS             = 0xC410;
    public static final int C_MASTER_SCALES        = 0xC420;
    public static final int C_SHOW_1STVERT         = 0xC430;
    public static final int C_SHAPER_OK            = 0xC440;
    public static final int C_LOFTER_OK            = 0xC450;
    public static final int C_EDITOR_OK            = 0xC460;
    public static final int C_KEYFRAMER_OK         = 0xC470;
    public static final int C_PICKSIZE             = 0xC480;
    public static final int C_MAPTYPE              = 0xC490;
    public static final int C_MAP_DISPLAY          = 0xC4a0;
    public static final int C_TILE_XY              = 0xC4b0;
    public static final int C_MAP_XYZ              = 0xC4c0;
    public static final int C_MAP_SCALE            = 0xC4d0;
    public static final int C_MAP_MATRIX_OLD       = 0xC4e0;
    public static final int C_MAP_MATRIX           = 0xC4e1;
    public static final int C_MAP_WHT           = 0xC4f0;
    public static final int C_OBNAME               = 0xC500;
    public static final int C_CAMNAME              = 0xC510;
    public static final int C_LTNAME               = 0xC520;
    public static final int C_CUR_MNAME            = 0xC525;
    public static final int C_CURMTL_FROM_MESH    = 0xC526;
    public static final int C_GET_SHAPE_MAKE_FACES = 0xC527;
    public static final int C_DETAIL               = 0xC530;
    public static final int C_VERTMARK             = 0xC540;
    public static final int C_MSHAX                = 0xC550;
    public static final int C_MSHCP                = 0xC560;
    public static final int C_USERAX               = 0xC570;
    public static final int C_SHOOK                = 0xC580;
    public static final int C_RAX                  = 0xC590;
    public static final int C_STAPE                = 0xC5a0;
    public static final int C_LTAPE                = 0xC5b0;
    public static final int C_ETAPE                = 0xC5c0;
    public static final int C_KTAPE                = 0xC5c8;
    public static final int C_SPHSEGS              = 0xC5d0;
    public static final int C_GEOSMOOTH           = 0xC5e0;
    public static final int C_HEMISEGS             = 0xC5f0;
    public static final int C_PRISMSEGS            = 0xC600;
    public static final int C_PRISMSIDES           = 0xC610;
    public static final int C_TUBESEGS             = 0xC620;
    public static final int C_TUBESIDES            = 0xC630;
    public static final int C_TORSEGS              = 0xC640;
    public static final int C_TORSIDES             = 0xC650;
    public static final int C_CONESIDES            = 0xC660;
    public static final int C_CONESEGS             = 0xC661;
    public static final int C_NGPARMS              = 0xC670;
    public static final int C_PTHLEVEL             = 0xC680;
    public static final int C_MSCSYM               = 0xC690;
    public static final int C_MFTSYM               = 0xC6a0;
    public static final int C_MTTSYM               = 0xC6b0;
    public static final int C_SMOOTHING            = 0xC6c0;
    public static final int C_MODICOUNT            = 0xC6d0;
    public static final int C_FONTSEL              = 0xC6e0;
    public static final int C_TESS_TYPE            = 0xC6f0;
    public static final int C_TESS_TENSION         = 0xC6f1;
    public static final int C_SEG_START            = 0xC700;
    public static final int C_SEG_END              = 0xC705;
    public static final int C_CURTIME              = 0xC710;
    public static final int C_ANIMLENGTH          = 0xC715;
    public static final int C_PV_FROM              = 0xC720;
    public static final int C_PV_TO                = 0xC725;
    public static final int C_PV_DOFNUM            = 0xC730;
    public static final int C_PV_RNG               = 0xC735;
    public static final int C_PV_NTH              = 0xC740;
    public static final int C_PV_TYPE              = 0xC745;
    public static final int C_PV_METHOD            = 0xC750;
    public static final int C_PV_FPS               = 0xC755;
    public static final int C_VTR_FRAMES           = 0xC765;
    public static final int C_VTR_HDTL             = 0xC770;
    public static final int C_VTR_HD               = 0xC771;
    public static final int C_VTR_TL               = 0xC772;
    public static final int C_VTR_IN               = 0xC775;
    public static final int C_VTR_PK               = 0xC780;
    public static final int C_VTR_SH              = 0xC785;
    public static final int C_WORK_MTLS            = 0xC790;
    public static final int C_WORK_MTLS_2          = 0xC792;
    public static final int C_WORK_MTLS_3          = 0xC793;
    public static final int C_WORK_MTLS_4          = 0xC794;
    public static final int C_BGTYPE               = 0xC7a1;
    public static final int C_MEDTILE              = 0xC7b0;
    public static final int C_LO_CONTRAST          = 0xC7d0;
    public static final int C_HI_CONTRAST          = 0xC7d1;
    public static final int C_FROZ_DISPLAY         = 0xC7e0;
    public static final int C_BOOLWELD             = 0xC7f0;
    public static final int C_BOOLTYPE             = 0xC7f1;
    public static final int C_ANG_THRESH          = 0xC900;
    public static final int C_SS_THRESH           = 0xC901;
    public static final int C_TEXTURE_BLUR_DEFAULT = 0xC903;
    public static final int C_MAPDRAWER            = 0xCa00;
    public static final int C_MAPDRAWER1           = 0xCa01;
    public static final int C_MAPDRAWER2           = 0xCa02;
    public static final int C_MAPDRAWER3           = 0xCa03;
    public static final int C_MAPDRAWER4           = 0xCa04;
    public static final int C_MAPDRAWER5           = 0xCa05;
    public static final int C_MAPDRAWER6           = 0xCa06;
    public static final int C_MAPDRAWER7           = 0xCa07;
    public static final int C_MAPDRAWER8           = 0xCa08;
    public static final int C_MAPDRAWER9           = 0xCa09;
    public static final int C_MAPDRAWER_ENTRY      = 0xCa10;
    public static final int C_BACKUP_FILE          = 0xCa20;
    public static final int C_DITHER_256           = 0xCa21;
    public static final int C_SAVE_LAST            = 0xCa22;
    public static final int C_USE_ALPHA            = 0xCa23;
    public static final int C_TGA_DEPTH           = 0xCa24;
    public static final int C_REND_FIELDS          = 0xCa25;
    public static final int C_REFLIP               = 0xCa26;
    public static final int C_SEL_ITEMTOG          = 0xCa27;
    public static final int C_SEL_RESET            = 0xCa28;
    public static final int C_STICKY_KEYINF        = 0xCa29;
    public static final int C_WELD_THRESHOLD       = 0xCa2a;
    public static final int C_ZCLIP_POINT          = 0xCa2b;
    public static final int C_ALPHA_SPLIT          = 0xCa2c;
    public static final int C_KF_SHOW_BACKFACE     = 0xCa30;
    public static final int C_OPTIMIZE_LOFT        = 0xCa40;
    public static final int C_TENS_DEFAULT         = 0xCa42;
    public static final int C_CONT_DEFAULT         = 0xCa44;
    public static final int C_BIAS_DEFAULT         = 0xCa46;
    public static final int C_DXFNAME_SRC          = 0xCa50;
    public static final int C_AUTO_WELD            = 0xCa60;
    public static final int C_AUTO_UNIFY           = 0xCa70;
    public static final int C_AUTO_SMOOTH         = 0xCa80;
    public static final int C_DXF_SMOOTH_ANG       = 0xCa90;
    public static final int C_SMOOTH_ANG           = 0xCaa0;
    public static final int C_WORK_MTLS_5          = 0xCb00;
    public static final int C_WORK_MTLS_6          = 0xCb01;
    public static final int C_WORK_MTLS_7          = 0xCb02;
    public static final int C_WORK_MTLS_8          = 0xCb03;
    public static final int C_WORKMTL              = 0xCb04;
    public static final int C_SXP_TEXT_DATA        = 0xCb10;
    public static final int C_SXP_OPAC_DATA        = 0xCb11;
    public static final int C_SXP_BUMP_DATA        = 0xCb12;
    public static final int C_SXP_SHIN_DATA        = 0xCb13;
    public static final int C_SXP_TEXT2_DATA       = 0xCb20;
    public static final int C_SXP_SPEC_DATA        = 0xCb24;
    public static final int C_SXP_SELFI_DATA       = 0xCb28;
    public static final int C_SXP_TEXT_MASKDATA    = 0xCb30;
    public static final int C_SXP_TEXT2_MASKDATA   = 0xCb32;
    public static final int C_SXP_OPAC_MASKDATA    = 0xCb34;
    public static final int C_SXP_BUMP_MASKDATA    = 0xCb36;
    public static final int C_SXP_SPEC_MASKDATA    = 0xCb38;
    public static final int C_SXP_SHIN_MASKDATA    = 0xCb3a;
    public static final int C_SXP_REFL_MASKDATA    = 0xCb3e;
    public static final int C_NET_USE_VPOST        = 0xCc00;
    public static final int C_NET_USE_GAMMA        = 0xCc10;
    public static final int C_NET_FIELD_ORDER      = 0xCc20;
    public static final int C_BLUR_FRAMES          = 0xCd00;
    public static final int C_BLUR_SAMPLES         = 0xCd10;
    public static final int C_BLUR_DUR             = 0xCd20;
    public static final int C_HOT_METHOD           = 0xCd30;
    public static final int C_HOT_CHECK            = 0xCd40;
    public static final int C_PIXEL_SIZE           = 0xCd50;
    public static final int C_DISP_GAMMA           = 0xCd60;
    public static final int C_FBUF_GAMMA           = 0xCd70;
    public static final int C_FILE_OUT_GAMMA       = 0xCd80;
    public static final int C_FILE_IN_GAMMA        = 0xCd82;
    public static final int C_GAMMA_CORRECT        = 0xCd84;
    public static final int C_APPLY_DISP_GAMMA     = 0xCd90;
    public static final int C_APPLY_FBUF_GAMMA     = 0xCda0;
    public static final int C_APPLY_FILE_GAMMA     = 0xCdb0;
    public static final int C_FORCE_WIRE           = 0xCdc0;
    public static final int C_RAY_SHADOWS          = 0xCdd0;
    public static final int C_MASTER_AMBIENT       = 0xCde0;
    public static final int C_SUPER_SAMPLE         = 0xCdf0;
    public static final int C_OBJECT_MBLUR         = 0xCe00;
    public static final int C_MBLUR_DITHER         = 0xCe10;
    public static final int C_DITHER_24            = 0xCe20;
    public static final int C_SUPER_BLACK          = 0xCe30;
    public static final int C_SAFE_FRAME           = 0xCe40;
    public static final int C_VIEW_PRES_RATIO      = 0xCe50;
    public static final int C_BGND_PRES_RATIO      = 0xCe60;
    public static final int C_NTH_SERIAL_NUM       = 0xCe70;

    // Dxxx;Group
    public static final int VPDATA                 = 0xD000;
    public static final int P_QUEUE_ENTRY          = 0xD100;
    public static final int P_QUEUE_IMAGE          = 0xD110;
    public static final int P_QUEUE_USEIGAMMA      = 0xD114;
    public static final int P_QUEUE_PROC           = 0xD120;
    public static final int P_QUEUE_SOLID          = 0xD130;
    public static final int P_QUEUE_GRADIENT       = 0xD140;
    public static final int P_QUEUE_KF             = 0xD150;
    public static final int P_QUEUE_MOTBLUR        = 0xD152;
    public static final int P_QUEUE_MB_REPEAT      = 0xD153;
    public static final int P_QUEUE_NONE           = 0xD160;
    public static final int P_QUEUE_RESIZE         = 0xD180;
    public static final int P_QUEUE_OFFSET         = 0xD185;
    public static final int P_QUEUE_ALIGN          = 0xD190;
    public static final int P_CUSTOM_SIZE          = 0xD1a0;
    public static final int P_ALPH_NONE            = 0xD210;
    public static final int P_ALPH_PSEUDO          = 0xD220;
    public static final int P_ALPH_OP_PSEUDO       = 0xD221;
    public static final int P_ALPH_BLUR            = 0xD222;
    public static final int P_ALPH_PCOL            = 0xD225;
    public static final int P_ALPH_C0              = 0xD230;
    public static final int P_ALPH_OP_KEY          = 0xD231;
    public static final int P_ALPH_KCOL            = 0xD235;
    public static final int P_ALPH_OP_NOCONV       = 0xD238;
    public static final int P_ALPH_IMAGE           = 0xD240;
    public static final int P_ALPH_ALPHA           = 0xD250;
    public static final int P_ALPH_QUES            = 0xD260;
    public static final int P_ALPH_QUEIMG          = 0xD265;
    public static final int P_ALPH_CUTOFF          = 0xD270;
    public static final int P_ALPHANEG             = 0xD280;
    public static final int P_TRAN_NONE            = 0xD300;
    public static final int P_TRAN_IMAGE           = 0xD310;
    public static final int P_TRAN_FRAMES          = 0xD312;
    public static final int P_TRAN_FADEIN          = 0xD320;
    public static final int P_TRAN_FADEOUT         = 0xD330;
    public static final int P_TRANNEG              = 0xD340;
    public static final int P_RANGES               = 0xD400;
    public static final int P_PROc_DATA            = 0xD500;

    // Fxxx;Group
    public static final int POS_TRACK_TAG_KEY      = 0xf020;
    public static final int ROT_TRACK_TAG_KEY      = 0xf021;
    public static final int SCL_TRACK_TAG_KEY      = 0xf022;
    public static final int FOV_TRACK_TAG_KEY      = 0xf023;
    public static final int ROLL_TRACK_TAG_KEY     = 0xf024;
    public static final int COL_TRACK_TAG_KEY      = 0xf025;
    public static final int MORPH_TRACK_TAG_KEY    = 0xf026;
    public static final int HOT_TRACK_TAG_KEY      = 0xf027;
    public static final int FALL_TRACK_TAG_KEY     = 0xf028;
    public static final int POINT_ARRAY_ENTRY      = 0xf110;
    public static final int POINT_FLAG_ARRAY_ENTRY = 0xf111;
    public static final int FACE_ARRAY_ENTRY       = 0xf120;
    public static final int MSH_MAT_GROUP_ENTRY    = 0xf130;
    public static final int TEX_VERTS_ENTRY        = 0xf140;
    public static final int SMOOTH_GROUP_ENTRY     = 0xf150;

    /** A dummy chunk ID (0xFFFF) */
    public static final int DUMMY                  = 0xffff;
}
