/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)VertexData.java 1.1 02/01/10 09:27:38
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    -Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *    -Redistribution in binary form must reproduct the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed,licensed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */
package org.j3d.terrain.roam;

/**
 * Collection of vertex information for a patch of terrain.
 * <p>
 *
 * The data held is coordinate, texture coordinate and vertex colours
 *
 * @author  Paul Byrne
 * @version $Revision: 1.1 $
 */
class VertexData
{
    private float coords[];
    private byte colors[];
    private float textureCoords[];
    private int index=0;
    private int texIndex = 0;

    /**
     * Creates new VertexData that represents a fixed number of vertices.
     * The patchsize is the number of points along one edge and represents
     * a square piece of landscape.
     *
     * @param patchSize The number of points on a side
     */
    public VertexData(int patchSize)
    {
        coords = new float[patchSize * patchSize * 2 * 3 * 3];
        textureCoords = new float[patchSize * patchSize*2 * 3 * 2];
        colors = new byte[coords.length];
    }

    public float[] getCoords()
    {
        return coords;
    }

    public byte[] getColors()
    {
        return colors;
    }

    public float[] getTextureCoords()
    {
        return textureCoords;
    }

    public void addVertex(float x, float y, float z)
    {
        coords[index] = x;
        coords[index+1] = y;
        coords[index+2] = z;

        index += 3;
    }

    public void addVertex(float x, float y, float z,
                          byte clrR, byte clrG, byte clrB)
    {
        coords[index] = x;
        coords[index+1] = y;
        coords[index+2] = z;

        //System.out.println( x+" "+y+" "+z );

        colors[index] = clrR;
        colors[index+1] = clrG;
        colors[index+2] = clrB;

        index += 3;
    }

    public void addVertex(float x, float y, float z,
                          float textureS, float textureT)
    {
        coords[index] = x;
        coords[index+1] = y;
        coords[index+2] = z;

        textureCoords[texIndex++] = textureS;
        textureCoords[texIndex++] = textureT;

        index += 3;
    }

    public int getVertexCount()
    {
        return index / 3;
    }

    public void reset()
    {
        index = 0;
        texIndex = 0;
    }
}
