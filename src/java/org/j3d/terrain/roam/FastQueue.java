/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)QueueManager.java 1.1 02/01/10 09:27:29
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

import java.util.TreeSet;
import java.util.LinkedList;

/**
 *
 * @author  paulby
 * @version
 */
class FastQueue
{
    /** The number of buckets that approximate the sorted queue */
    private static final int BUCKETS=2048;

    /**
     * The largest number that is 'sorted', all items larger than this
     * number are placed in the maximum bucket
     */
    private static final float TOP = 0.2f;

    /** Flag indicating that we a sorting by diamondVariance */
    private boolean diamondQueue = false;

    private TreeNode[] queueBuckets;
    private int largest = 0;
    private int smallest = BUCKETS;
    private int currentBucket;

    public FastQueue(boolean diamondQueue)
    {
        this.diamondQueue = diamondQueue;
        queueBuckets = new TreeNode[ BUCKETS+1 ];
    }

    public void add(TreeNode node)
    {
        TreeNode existingNode = getList(node);

       // if (diamondQueue)
        //System.out.println("Adding "+diamondQueue+"  "+node.node+"  "+node.leftChild+" "+node.rightChild);
        //TreeNode.printNode(node);

        if (diamondQueue)
        {
            node.nextDiamond = existingNode;
            if(existingNode != null)
                existingNode.previousDiamond = node;
            node.previousDiamond = null;
        }
        else
        {
            node.nextTriangle = existingNode;
            if(existingNode != null)
                existingNode.previousTriangle = node;
            node.previousTriangle = null;
        }

        queueBuckets[currentBucket] = node;

        largest = Math.max(largest, currentBucket);
        smallest = Math.min(smallest, currentBucket);
    }

    public boolean remove(TreeNode node)
    {
        boolean ok = false;

        //if (diamondQueue)
        //System.out.println("Removing "+diamondQueue+"  "+node.node);
        //TreeNode.printNode(node);

        if(diamondQueue)
        {
            if(node.previousDiamond == null)
            {
                TreeNode existingNode = getList(node);

                if(existingNode == null || existingNode != node)
                    return false;

                queueBuckets[currentBucket] = node.nextDiamond;
                if(node.nextDiamond != null)
                    node.nextDiamond.previousDiamond = null;
                node.previousDiamond = null;
                node.nextDiamond = null;
                ok = true;
            }
            else
            {
                node.previousDiamond.nextDiamond = node.nextDiamond;
                if(node.nextDiamond != null)
                    node.nextDiamond.previousDiamond = node.previousDiamond;
                node.previousDiamond = null;
                node.nextDiamond = null;
                ok = true;
            }
        }
        else
        {
            if(node.previousTriangle == null)
            {
                TreeNode existingNode = getList(node);
                if(existingNode == null || existingNode != node)
                    return false;

                queueBuckets[ currentBucket ] = node.nextTriangle;
                if(node.nextTriangle != null)
                    node.nextTriangle.previousTriangle= null;
                node.previousTriangle = null;
                node.nextTriangle = null;
                ok = true;
            }
            else
            {
                node.previousTriangle.nextTriangle = node.nextTriangle;
                if (node.nextTriangle != null)
                    node.nextTriangle.previousTriangle = node.previousTriangle;
                node.previousTriangle = null;
                node.nextTriangle = null;
                ok = true;
            }
        }

        return ok;
    }

    /**
     * Returns the largest element in the queue
     *
     * (Compatible with java.util.SortedSet interface)
     */
    public TreeNode last()
    {
        TreeNode list = queueBuckets[largest];

        while(list == null && largest >= smallest) {
            largest--;
            list = queueBuckets[largest];
        }

        return list;
    }

    /**
     * Returns the smallest element in the queue
     *
     * (Compatible with java.util.SortedSet interface)
     */
    public TreeNode first()
    {
        TreeNode list = queueBuckets[smallest];

        while(list == null && smallest <= largest)
        {
            smallest++;
            list = queueBuckets[smallest];
        }

        return list;
    }

    public void clear()
    {
        for(int i = 0; i < queueBuckets.length; i++)
            queueBuckets[i] = null;
    }

    private TreeNode getList(TreeNode node)
    {
        float value;
        if (diamondQueue)
            value = node.diamondVariance;
        else
            value = node.variance;

        if(value > TOP)
            currentBucket = BUCKETS + 1;
        else
            currentBucket = (int)((value / TOP ) *BUCKETS);

        return queueBuckets[currentBucket];
    }
}

