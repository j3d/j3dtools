/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.j3d.device.output.elumens;

public class SPI {
    public static final int SPI_1C_FRONT=   1;  // chan[0]

    public static final int SPI_2C_LEFT =   2;  // chan[1]
    public static final int SPI_2C_RIGHT=   4;  // chan[2]

    public static final int SPI_3C_LEFT =   8;  // chan[3]
    public static final int SPI_3C_RIGHT=   16; // chan[4]
    public static final int SPI_3C_TOP  =   32; // chan[5]

    public static final int SPI_4C_LEFT =   8;  // chan[3] - aliased to SPI_3C_LEFT
    public static final int SPI_4C_RIGHT=   16; // chan[4] - aliased to SPI_3C_RIGHT
    public static final int SPI_4C_TOP  =   32; // chan[5] - aliased to SPI_3C_TOP
    public static final int SPI_4C_BOTTOM=  64; // chan[6]

    public static final int SPI_OC_FRONT=   128;    // chan[8]
    public static final int SPI_2C_INSERT=  128;    // chan[8]
    public static final int SPI_2C_BORDER=  256;    // chan[7]

    public static final int SPI_ALL_CHAN = SPI_1C_FRONT | SPI_2C_LEFT |
        SPI_2C_RIGHT | SPI_4C_LEFT | SPI_4C_RIGHT | SPI_4C_TOP | SPI_4C_BOTTOM |
        SPI_OC_FRONT | SPI_2C_INSERT | SPI_2C_BORDER;
    public static final int SPI_ALL_2_CHAN = SPI_2C_LEFT | SPI_2C_RIGHT;
    public static final int SPI_ALL_3_CHAN = SPI_3C_LEFT | SPI_3C_RIGHT |
        SPI_3C_TOP;
    public static final int SPI_ALL_4_CHAN = SPI_4C_LEFT | SPI_4C_RIGHT |
        SPI_4C_TOP | SPI_4C_BOTTOM;

    public static final int SPI_PF_NONE=            0;
    public static final int SPI_PF_BACKBUFFER=      1;
    public static final int SPI_PF_NORMAL=          1;
    public static final int SPI_PF_PBUFFER=         2;
    public static final int SPI_PF_TEXTURE=         4;
    public static final int SPI_PF_AUTO=            6;
    public static final int SPI_PF_STEREO=          8;

    public static final int SPI_PF_1_CHAN=          512;
    public static final int SPI_PF_2_CHAN=          1024;
    public static final int SPI_PF_3_CHAN=          2048;
    public static final int SPI_PF_4_CHAN=          4096;
    public static final int SPI_PF_O_CHAN=          8192;
    public static final int SPI_PF_2_CTR_CHAN=      8192*2;

    public native boolean initialize(int pformat, int numChannels);
    public native void begin();
    public native void end();
    public native void preRender(int loc);
    public native void postRender(int loc);
    public native void flush(int loc);
    public native void setChanSize(int loc, int fovH, int fovV);

    public native void oglClear();
    public native void marker(float num);

    public native void warpCoords(int x, int y, int z, int width, int height, double[] newCoords);
    public native void setNearFar(float near, float far);
    public native void setChanLensPosition(int wall, float x, float y, float z);
    public native void setChanEyePosition(int wall, float x, float y, float z);
    public native void setScreenOrientation(double r, double p, double v);
    public native void marker(float a, int b);
    public native void getModelView(double[] mv);
}
