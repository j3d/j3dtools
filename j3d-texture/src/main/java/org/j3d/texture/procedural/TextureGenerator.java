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

package org.j3d.texture.procedural;

// External imports
import java.awt.image.BufferedImage;
import java.util.Random;

// Local imports
// None

/**
 * Various utility methods for creating textures from procedural means.
 * <p>
 *
 * <h3>Using Synthesis Textures</h3>
 *
 * Synthesis textures require the user to make multiple passes to generate the
 * desired output. Each passes adds to the previous pass at, hopefully, higher
 * and higher frequency. This can then be turned into a texture by finding the
 * height range and multiplying through the values to generate an appropriate
 * set of bytes.
 * <p>
 *
 * An example usage of this code is:
 * <pre>
 *  int width = 256;
 *  int depth = 256;
 *  int passes = 5;
 *  float scale = 8;
 *  float freq = 4;
 *  float y_scale = 0.4f;
 *  float freq_diff = 6;
 *
 *  float[] raw_img = new float[width * depth];
 *
 *  imageGen.generateSynthesisTexture(raw_img,
 *                                    freq,
 *                                    scale,
 *                                    width,
 *                                    depth);
 *
 *  for(int i = 1; i < passes; i++)
 *  {
 *      freq += freq_diff;
 *      scale *= y_scale;
 *      imageGen.generateSynthesisTexture(raw_img,
 *                                        freq,
 *                                        scale,
 *                                        width,
 *                                        depth);
 *  }
 *
 *  // find min and max values of the floats
 *  float min_y = raw_img[0];
 *  float max_y = raw_img[0];
 *
 *  for(int i = 0; i < width * depth; i++)
 *  {
 *      if(raw_img[i] > max_y)
 *          max_y = raw_img[i];
 *      else if(raw_img[i] < min_y)
 *          min_y = raw_img[i];
 *  }
 *
 *  byte[] pixels = new byte[width * depth];
 *  float diff = 1 / (max_y - min_y);
 *
 *  for(int i = 0; i < width * depth; i++)
 *      pixels[i] = (byte)(((raw_img[i] - min_y) * diff) * 255);
 *
 *  WritableRaster raster =
 *      Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
 *                                width,
 *                                depth,
 *                                1,
 *                                8,
 *                                null);
 *
 *  raster.setDataElements(0, 0, width, depth, pixels);
 *  BufferedImage img =
 *      new BufferedImage(width, depth, BufferedImage.TYPE_BYTE_GRAY);
 *
 *  img.setData(raster);
 * </pre>
 *
 * <h3>Sources</h3>
 *
 * The synthesis texture generation comes from A GameDev.net article by
 * <a href="http://www.gamedev.net/reference/programming/features/noiseterrain/">Druid</a>
 * <br>
 * The texture mixing code comes from a FlipCode tutorial by
 * <a href="http://www.flipcode.com/tutorials/tut_proctext.shtml">Tobias Franke</a>.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TextureGenerator
{
    // basis matrix for spline interpolation
    private static final float CR00 =  -0.5f;
    private static final float CR01 =  1.5f;
    private static final float CR02 =  -1.5f;
    private static final float CR03 =  0.5f;
    private static final float CR10 =  1;
    private static final float CR11 =  -2.5f;
    private static final float CR12 =  2;
    private static final float CR13 =  -0.5f;
    private static final float CR20 =  -0.5f;
    private static final float CR21 =  0;
    private static final float CR22 =  0.5f;
    private static final float CR23 =  0;
    private static final float CR30 =  0;
    private static final float CR31 =  1;
    private static final float CR32 =  0;
    private static final float CR33 =  0;

    /** Random number generator instance shared amongst runs of this code */
    private Random rgen;

    /**
     * Create a new instance of this generator with default values set.
     */
    public TextureGenerator()
    {
        rgen = new Random();
    }

    /**
     * Reset the random number generator using the new seed value.
     *
     * @param seed The new seed value to use
     */
    public void setRandomSeed(long seed)
    {
        rgen = new Random(seed);
    }

    /**
     * Generate a texture using spectral synthesis techniques into a height
     * field of floats. This takes various sine waves and combines harmonics
     * of them for a single pass. Each time this is called with the same
     * outputImage provided, the values are added to the previous values, so
     * that mutli-pass techniques give a nicer terrain.
     *
     * @param outputImage An optional array to write the output values to
     * @param freq The number of passes to add to the image
     * @param zScale A scaling factor for the heights (0, 1]
     * @param width The width in pixels for the output image
     * @param height The height in pixels for the output image
     * @return A float of greyscale heights for the output image
     */
    public float[] generateSynthesisTexture(float[] outputImage,
                                            float freq,
                                            float zScale,
                                            int width,
                                            int height)
    {
        int reqd_size = height * width;
        float[] ret_val;

        if((outputImage == null) || (outputImage.length < reqd_size))
            ret_val = new float[reqd_size];
        else
            ret_val = outputImage;

        int max = (int)freq + 2;

        float[] buf = generateSynthesisTexture(freq, zScale, width, height);

        // Write everything to the output array now
        for(int z = 0; z < height; z++)
        {
            for(int x = 0; x < width; x++)
                ret_val[z * width + x] += buf[z * width + x];
        }

        return ret_val;
    }

    /**
     * From the given height map and input textures, generate a single mixed
     * RGB texture that can be drapped over the terrain. The input for this
     * method is expected to be something similar to the output from the
     * {@link org.j3d.geom.terrain.FractalTerrainGenerator}.
     * <p>
     * There must be at least one more <code>textureHeight</code> than there is
     * <code>numColorTextures</code>. Each index in the array represents where
     * the texture for that index is blended at 0%. The last value indicates
     * where the highest texture is blended at 100%.
     * <p>
     * Height values must be monotonically increasing.
     * <p>
     * The height colour textures are not required to be the same size as the
     * height map. If they are not, then the pixels are retrieved using a
     * wrapping function.
     *
     * @param heightMap The list of heights in [height][width] order
     * @param colorTextures Images to read color values from for mixing
     * @param textureHeight List of heights that each texture applies to
     * @param numColorTextures The number of textures to use in heights
     * @return A flat array in [r,g,b,...] order suitable for dumping to a
     *    texture or Image object.
     */
    public int[] generateMixedTerrainTexture(int[] outputImage,
                                             float[] heightMap,
                                             int width,
                                             int height,
                                             BufferedImage[] colorTextures,
                                             float[] textureHeight,
                                             int numColorTextures)
    {
        int reqd_size = width * height;
        int[] ret_val;

        if((outputImage == null) || (outputImage.length < reqd_size))
            ret_val = new int[reqd_size];
        else
            ret_val = outputImage;

        float[] texture_factor = new float[numColorTextures];
        float[] height_diff = new float[numColorTextures];
        int[] point_colors = new int[numColorTextures];
        int[][] texture_sizes = new int[numColorTextures][2];

        for(int i = 0; i < numColorTextures; i++)
        {
            height_diff[i] = textureHeight[i + 1] - textureHeight[i];
            texture_sizes[i][0] = colorTextures[i].getWidth();
            texture_sizes[i][1] = colorTextures[i].getHeight();
        }

        int color_idx = 0;

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                float h = heightMap[y * width + x];

                for(int i = 0; i < numColorTextures; i++)
                {
                    texture_factor[i] = texMixFactor(textureHeight[i + 1],
                                                     h,
                                                     height_diff[i]);

                    int x_pos = x % texture_sizes[i][0];
                    int y_pos = y % texture_sizes[i][1];
                    point_colors[i] =
                        colorTextures[i].getRGB(x_pos, y_pos);
                }

                int r = 0;
                int g = 0;
                int b = 0;

                for(int i = 0; i < numColorTextures; i++)
                {
                    if(texture_factor[i] != 0)
                    {
                        int o_r = (point_colors[i] >> 16) & 0xFF;
                        int o_g = (point_colors[i] >> 8) & 0xFF;
                        int o_b = (point_colors[i]) & 0xFF;

                        r += o_r * texture_factor[i];
                        g += o_g * texture_factor[i];
                        b += o_b * texture_factor[i];
                    }
                }

                ret_val[color_idx++] = ((r & 0xFF) << 16) +
                                       ((r & 0xFF) << 8) +
                                       (b & 0xFF);
            }
        }

        return ret_val;
    }

    /**
     * From the given height map and input textures, generate a single mixed
     * RGB texture that can be drapped over the terrain. The input for this
     * method is expected to be something similar to the output from the
     * {@link org.j3d.geom.terrain.FractalTerrainGenerator}.
     * <p>
     * There must be at least one more <code>textureHeight</code> than there is
     * <code>numColorTextures</code>. Each index in the array represents where
     * the texture for that index is blended at 0%. The last value indicates
     * where the highest texture is blended at 100%.
     * <p>
     * Height values must be monotonically increasing.
     * <p>
     * The height colour textures are not required to be the same size as the
     * height map. If they are not, then the pixels are retrieved using a
     * wrapping function.
     *
     * @param heightMap The list of heights in [height][width] order
     * @param colorTextures Images to read color values from for mixing
     * @param textureHeight List of heights that each texture applies to
     * @param numColorTextures The number of textures to use in heights
     * @return A flat array in [r,g,b,...] order suitable for dumping to a
     *    texture or Image object.
     */
    public byte[] generateMixedTerrainTexture(byte[] outputImage,
                                              float[] heightMap,
                                              int width,
                                              int height,
                                              BufferedImage[] colorTextures,
                                              float[] textureHeight,
                                              int numColorTextures)
    {
        int reqd_size = width * height * 3;
        byte[] ret_val;

        if((outputImage == null) || (outputImage.length < reqd_size))
            ret_val = new byte[reqd_size];
        else
            ret_val = outputImage;

        float[] texture_factor = new float[numColorTextures];
        float[] height_diff = new float[numColorTextures];
        int[] point_colors = new int[numColorTextures];
        int[][] texture_sizes = new int[numColorTextures][2];

        for(int i = 0; i < numColorTextures; i++)
        {
            height_diff[i] = textureHeight[i + 1] - textureHeight[i];
            texture_sizes[i][0] = colorTextures[i].getWidth();
            texture_sizes[i][1] = colorTextures[i].getHeight();
        }

        int color_idx = 0;

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                float h = heightMap[y * width + x];

                for(int i = 0; i < numColorTextures; i++)
                {
                    texture_factor[i] = texMixFactor(textureHeight[i + 1],
                                                     h,
                                                     height_diff[i]);

                    int x_pos = x % texture_sizes[i][0];
                    int y_pos = y % texture_sizes[i][1];
                    point_colors[i] =
                        colorTextures[i].getRGB(x_pos, y_pos);
                }

                int r = 0;
                int g = 0;
                int b = 0;

                for(int i = 0; i < numColorTextures; i++)
                {
                    if(texture_factor[i] != 0)
                    {
                        int o_r = (point_colors[i] >> 16) & 0xFF;
                        int o_g = (point_colors[i] >> 8) & 0xFF;
                        int o_b = (point_colors[i]) & 0xFF;

                        r += o_r * texture_factor[i];
                        g += o_g * texture_factor[i];
                        b += o_b * texture_factor[i];
                    }
                }

                ret_val[color_idx] = (byte)r;
                ret_val[color_idx + 1] = (byte)g;
                ret_val[color_idx + 2] = (byte)b;

                color_idx += 3;
            }
        }

        return ret_val;
    }

    /**
     * From the given height map and input textures, generate a single mixed
     * RGB texture that can be drapped over the terrain. The input for this
     * method is expected to be something similar to the output from the
     * {@link org.j3d.geom.terrain.FractalTerrainGenerator}.
     * <p>
     * There must be at least one more <code>textureHeight</code> than there is
     * <code>numColorTextures</code>. Each index in the array represents where
     * the texture for that index is blended at 0%. The last value indicates
     * where the highest texture is blended at 100%.
     * <p>
     * Height values must be monotonically increasing
     *
     * @param heightMap The list of heights in [height][width] order
     * @param colorTextures Images to read color values from for mixing
     * @param textureHeight List of heights that each texture applies to
     * @param numColorTextures The number of textures to use in heights
     * @return A flat array in [r,g,b,...] order suitable for dumping to a
     *    texture or Image object.
     */
    public int[] generateMixedTerrainTexture(int[] outputImage,
                                             float[][] heightMap,
                                             BufferedImage[] colorTextures,
                                             float[] textureHeight,
                                             int numColorTextures)
    {
        int reqd_size = heightMap.length * heightMap[0].length;
        int[] ret_val;

        if((outputImage == null) || (outputImage.length < reqd_size))
            ret_val = new int[reqd_size];
        else
            ret_val = outputImage;

        float[] texture_factor = new float[numColorTextures];
        float[] height_diff = new float[numColorTextures];
        int[] point_colors = new int[numColorTextures];
        int[][] texture_sizes = new int[numColorTextures][2];

        for(int i = 0; i < numColorTextures; i++)
        {
            height_diff[i] = textureHeight[i + 1] - textureHeight[i];
            texture_sizes[i][0] = colorTextures[i].getWidth();
            texture_sizes[i][1] = colorTextures[i].getHeight();
        }

        int hm_height = heightMap.length;
        int hm_width = heightMap[0].length;
        int color_idx = 0;

        for(int y = 0; y < hm_height; y++)
        {
            for(int x = 0; x < hm_width; x++)
            {
                float height = heightMap[y][x];

                for(int i = 0; i < numColorTextures; i++)
                {
                    texture_factor[i] = texMixFactor(textureHeight[i + 1],
                                                     height,
                                                     height_diff[i]);

                    int x_pos = x % texture_sizes[i][0];
                    int y_pos = y % texture_sizes[i][1];
                    point_colors[i] =
                        colorTextures[i].getRGB(x_pos, y_pos);
                }

                int r = 0;
                int g = 0;
                int b = 0;

                for(int i = 0; i < numColorTextures; i++)
                {
                    if(texture_factor[i] != 0)
                    {
                        int o_r = (point_colors[i] >> 16) & 0xFF;
                        int o_g = (point_colors[i] >> 8) & 0xFF;
                        int o_b = (point_colors[i]) & 0xFF;

                        r += o_r * texture_factor[i];
                        g += o_g * texture_factor[i];
                        b += o_b * texture_factor[i];
                    }
                }

                ret_val[color_idx++] = ((r & 0xFF) << 16) +
                                       ((r & 0xFF) << 8) +
                                       (b & 0xFF);
            }
        }

        return ret_val;
    }

    /**
     * From the given height map and input textures, generate a single mixed
     * RGB texture that can be drapped over the terrain. The input for this
     * method is expected to be something similar to the output from the
     * {@link org.j3d.geom.terrain.FractalTerrainGenerator}.
     * <p>
     * There must be at least one more <code>textureHeight</code> than there is
     * <code>numColorTextures</code>. Each index in the array represents where
     * the texture for that index is blended at 0%. The last value indicates
     * where the highest texture is blended at 100%.
     * <p>
     * Height values must be monotonically increasing
     *
     * @param heightMap The list of heights in [height][width] order
     * @param colorTextures Images to read color values from for mixing
     * @param textureHeight List of heights that each texture applies to
     * @param numColorTextures The number of textures to use in heights
     * @return A flat array in [r,g,b,...] order suitable for dumping to a
     *    texture or Image object.
     */
    public byte[] generateMixedTerrainTexture(byte[] outputImage,
                                              float[][] heightMap,
                                              BufferedImage[] colorTextures,
                                              float[] textureHeight,
                                              int numColorTextures)
    {
        int reqd_size = heightMap.length * heightMap[0].length * 3;
        byte[] ret_val;

        if((outputImage == null) || (outputImage.length < reqd_size))
            ret_val = new byte[reqd_size];
        else
            ret_val = outputImage;

        float[] texture_factor = new float[numColorTextures];
        float[] height_diff = new float[numColorTextures];
        int[] point_colors = new int[numColorTextures];
        int[][] texture_sizes = new int[numColorTextures][2];

        for(int i = 0; i < numColorTextures; i++)
        {
            height_diff[i] = textureHeight[i + 1] - textureHeight[i];
            texture_sizes[i][0] = colorTextures[i].getWidth();
            texture_sizes[i][1] = colorTextures[i].getHeight();
        }

        int hm_height = heightMap.length;
        int hm_width = heightMap[0].length;
        int color_idx = 0;

        for(int y = 0; y < hm_height; y++)
        {
            for(int x = 0; x < hm_width; x++)
            {
                float height = heightMap[y][x];

                for(int i = 0; i < numColorTextures; i++)
                {
                    texture_factor[i] = texMixFactor(textureHeight[i + 1],
                                                     height,
                                                     height_diff[i]);

                    int x_pos = x % texture_sizes[i][0];
                    int y_pos = y % texture_sizes[i][1];
                    point_colors[i] =
                        colorTextures[i].getRGB(x_pos, y_pos);
                }

                int r = 0;
                int g = 0;
                int b = 0;

                for(int i = 0; i < numColorTextures; i++)
                {
                    if(texture_factor[i] != 0)
                    {
                        int o_r = (point_colors[i] >> 16) & 0xFF;
                        int o_g = (point_colors[i] >> 8) & 0xFF;
                        int o_b = (point_colors[i]) & 0xFF;

                        r += o_r * texture_factor[i];
                        g += o_g * texture_factor[i];
                        b += o_b * texture_factor[i];
                    }
                }

                ret_val[color_idx] = (byte)r;
                ret_val[color_idx + 1] = (byte)g;
                ret_val[color_idx + 2] = (byte)b;

                color_idx += 3;
            }
        }

        return ret_val;
    }

    /**
     * Generate a percentage mix factor for the two given heights.
     *
     * @param h1 The lower height to use
     * @param h2 The higher height to use
     * @param heightDiff a differential between the two base heights mixed
     * @return A percentage mixing factor between 0 and 1
     */
    private float texMixFactor(float h1, float h2, float heightDiff)
    {
        float percent = (heightDiff - Math.abs(h1 - h2)) / heightDiff;

        if(percent < 0)
            percent = 0;
        else if(percent > 1)
            percent = 1;

        return percent;
    }

    /**
     * Generate a texture using spectral synthesis techniques into a height
     * feild of floats. Can then be used to generate various different forms,
     * such as bytes, shorts and floats.
     *
     * @param freq The number of passes to add to the image
     * @param zScale A scaling factor for the heights (0, 1]
     * @param width The width in pixels for the output image
     * @param height The height in pixels for the output image
     * @return A float array of greyscale heights for the output image
     */
    private float[] generateSynthesisTexture(float freq,
                                             float zScale,
                                             int width,
                                             int height)
    {
        int max = (int)freq + 2;

        // delta x and z - pixels per spline segment
        float d_fx = width / (freq - 1);
        float d_fz = height / (freq - 1);
        float i_d_fx = 1 / d_fx;
        float i_d_fz = 1 / d_fz;

        // the generated values - to be equally spread across buf
        float[] val = new float[max * max];
        float[] h_list = new float[max * width];

        // intermediate calculated spline knots (for 2d)
        float[] zKnots =  new float[max];
        float[] buf =  new float[width * height];

        // start at -dfx, -dfz - generate random knots
        for(int z = 0; z < max; z++)
        {
            for(int x = 0; x < max; x++)
                val[z * max + x] = rgen.nextFloat() * 2 - 1;
        }

        // interpolate horizontal lines through knots
        for(int i = 0; i < max; i++)
        {
            int k_offset = i * max;
            float xk = 0;
            for(int x = 0; x < width; x++ )
            {
                h_list[i * width + x] = spline(xk * i_d_fx, 4, val, k_offset);
                xk += 1;

                if(xk >= d_fx)
                {
                    xk -= d_fx;
                    k_offset++;
                }
            }
        }

        // interpolate all vertical lines
        for(int x = 0; x < width; x++)
        {
            float zk = 0;
            int k_offset = 0;

            // build knot list
            for(int i = 0; i < max; i++)
                zKnots[i] = h_list[i * width + x];

            for(int z = 0; z < height; z++)
            {
                buf[z * width + x] = spline(zk * i_d_fz, 4, zKnots, k_offset) * zScale;
                zk += 1;
                if(zk >= d_fz)
                {
                    zk -= d_fz;
                    k_offset++;
                }
            }
        }

        return buf;
    }

    /**
     * Interpolate a 1d spline. Taken from _Texturing & Modeling: A Procedural
     * Approach_  chapter 2 - by Darwyn Peachey.
     *
     * @param float x A starting point on the spline
     * @param nKnots number of knots on the spline
     * @param knot An array containing the knot values to interpolate
     * @param kOffset Starting offset into the knot array to work from
     */
    private float spline(float x, int nknots, float[] knot, int kOffset)
    {
        int nspans = nknots - 3;

        // illegal
        if(nspans < 1)
            return 0;

        // find the appropriate 4-point span of the spline
        x = (x < 0 ? 0 : (x > 1 ? 1 : x)) * nspans;
        int span = (int)x;
        if(span >= nknots - 3)
            span = nknots - 3;
        x -= span;
        kOffset += span;

        // evaluate the span cubic at x using horner's rule
        float c3 = CR00 * knot[kOffset + 0] + CR01 * knot[kOffset + 1] +
                   CR02 * knot[kOffset + 2] + CR03 * knot[kOffset + 3];
        float c2 = CR10 * knot[kOffset + 0] + CR11 * knot[kOffset + 1] +
                   CR12 * knot[kOffset + 2] + CR13 * knot[kOffset + 3];
        float c1 = CR20 * knot[kOffset + 0] + CR21 * knot[kOffset + 1] +
                   CR22 * knot[kOffset + 2] + CR23 * knot[kOffset + 3];
        float c0 = CR30 * knot[kOffset + 0] + CR31 * knot[kOffset + 1] +
                   CR32 * knot[kOffset + 2] + CR33 * knot[kOffset + 3];

        return ((c3 * x + c2) * x + c1) * x + c0;
    }
}
