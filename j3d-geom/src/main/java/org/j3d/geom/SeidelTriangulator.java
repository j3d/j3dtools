/*****************************************************************************
 *                        j3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.geom;

// External imports
import java.util.Random;

import org.j3d.maths.vector.Point2d;

// Local imports
// None

/**
 * Seidel triangulation implementation main class.
 * <p>
 * The algorithm is defined in the paper at:
 * http://www.cs.unc.edu/~dm/CODE/GEM/chapter.html
 * and this is a direct port of the linked C code.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SeidelTriangulator
{
    /** Log 2 base change in log e */
    private static final double LOG_2_BASE = 1 / Math.log(2);

    /**
     * The floating point epsilon to use for working out if values are
     * equal within this given range.
     */
    private static final float EPSILON = 10e-7f;

    /** A list of segments to use for this triangulation */
    private SeidelSegment[] segments;

    /** The query structure, made of nodes */
    private SeidelNode[] queryList;

    /** The trapezoid structure, made of nodes */
    private SeidelTrapezoid[] trapezoidList;

    /** Index of the last used item in the queryList */
    private int lastQuery;

    /** Index of the last used item in the trapezoidList */
    private int lastTrapezoid;

    /** Where we are in the choice array */
    private int choiceIndex;

    /** Random number generator */
    private Random randomiser;

    /** A list of permutations in the random ordering */
    private int[] permute;

    /**
     * Table to hold all the monotone polygons. Each monotone polygon
     * is a circularly linked list
     */
    private SeidelMonotoneChain[] monotoneChain;

    /**
     * Chain init information. This is used to decide which monotone polygon
     * to split if there are several other polygons touching at the same vertex
     */
    private SeidelVertexChain[] vertexChain;

    /** Where we are in the monoChain array */
    private int lastMonotone;

    /** Where we are in the vertexChain array */
    private int lastVertex;

    /** Position of any vertex in the monotone chain for the polygon */
    private int[] monotonePosition;

    /** Has this trapezoid been visited in this round */
    private boolean[] visited;

    /** Reflex chain used to perform the greedy triangulation */
    private int[] reflexChain;

    /**
     * Construct a new instance of the triangulator
     */
    SeidelTriangulator()
    {
        randomiser = new Random();
    }

    /**
     * Triangulate a single polygon, defined as a set of contours.
     * Input specified as contours. Outer contour must be anti-clockwise.
     * All inner contours must be clockwise.
     * <p>
     *
     * Every contour is specified by giving all its points in order. No
     * point shoud be repeated. i.e. if the outer contour is a square,
     * only the four distinct endpoints should be specified in order.
     *
     * @param ncontours #contours
     * @param contourCounts An array describing the number of points in each
     *   contour. Thus, contourCounts[i] = #points in the i'th contour.
     * @param vertices Input array of vertices. Vertices for each contour
     *     immediately follow those for previous one. Array location
     *     vertices[0] must NOT be used (i.e. i/p starts from
     *     vertices[1] instead. The output triangles are
     *     specified  angle.r.t. the indices of these vertices.
     * @param triangles: Output array to hold triangles. Should be
     *    (n - 2) * 3 in length, where n is the number of input vertices
     */
    void triangulatePolygon(int ncontours,
                            int[] contourCounts,
                            float[] vertices,
                            int[] triangles)
    {
        // total up all the contour counts to find out total number of segments
        // needed.
        // num_segments starts at 1 to account for the extra blank space at
        // the start of the vertex list.
        int i;
        int num_segments = 1;
        for(i = 0; i < ncontours; i++)
            num_segments += contourCounts[i];

        // Reset the various global variables if needed
        if(segments == null || segments.length < num_segments)
        {
            int tr_size = num_segments * 4;
            int qs_size = num_segments * 8;

            SeidelSegment[] seg_tmp = new SeidelSegment[num_segments];
            SeidelTrapezoid[] trap_tmp = new SeidelTrapezoid[tr_size];
            SeidelNode[] node_tmp = new SeidelNode[qs_size];
            SeidelVertexChain[] vert_tmp = new SeidelVertexChain[num_segments];
            SeidelMonotoneChain[] mono_tmp = new SeidelMonotoneChain[tr_size];

            int last_seg = 0;
            int last_trap = 0;
            int last_node = 0;
            int last_vert = 0;
            int last_mono = 0;

            if(segments != null)
            {
                last_seg = segments.length;
                last_trap = trapezoidList.length;
                last_node = queryList.length;
                last_mono = monotoneChain.length;
                last_vert = vertexChain.length;

                System.arraycopy(segments, 0, seg_tmp, 0, last_seg);
                System.arraycopy(trapezoidList, 0, trap_tmp, 0, last_trap);
                System.arraycopy(queryList, 0, node_tmp, 0, last_node);
                System.arraycopy(monotoneChain, 0, mono_tmp, 0, last_mono);
                System.arraycopy(vertexChain, 0, vert_tmp, 0, last_vert);
            }

            for(i = last_seg; i < num_segments; i++)
                seg_tmp[i] = new SeidelSegment();

            for(i = last_trap; i < tr_size; i++)
                trap_tmp[i] = new SeidelTrapezoid();

            for(i = last_node; i < qs_size; i++)
                node_tmp[i] = new SeidelNode();

            for(i = last_vert; i < num_segments; i++)
                vert_tmp[i] = new SeidelVertexChain();

            for(i = last_mono; i < tr_size; i++)
                mono_tmp[i] = new SeidelMonotoneChain();

            for(i = 0; i < last_seg; i++)
                seg_tmp[i].clear();

            for(i = 0; i < last_trap; i++)
                trap_tmp[i].clear();

            for(i = 0; i < last_node; i++)
                node_tmp[i].clear();

            for(i = 0; i < last_mono; i++)
                mono_tmp[i].clear();

            for(i = 0; i < last_vert; i++)
                vert_tmp[i].clear();

            segments = seg_tmp;
            trapezoidList = trap_tmp;
            queryList = node_tmp;
            monotoneChain = mono_tmp;
            vertexChain = vert_tmp;

            // The ones that don't need to reallocate structures:
            permute = new int[num_segments];
            visited = new boolean[tr_size];
            monotonePosition = new int[num_segments];
        }
        else
        {
            for(i = 0; i < segments.length; i++)
                segments[i].clear();

            for(i = 0; i < trapezoidList.length; i++)
                trapezoidList[i].clear();

            for(i = 0; i < queryList.length; i++)
                queryList[i].clear();
        }

        i = 1;

        for(int c_count = 0; c_count < ncontours; c_count++)
        {
            int npoints = contourCounts[c_count];
            int first = i;
            int last = first + npoints - 1;

            for(int j = 0; j < npoints; j++, i++)
            {
                segments[i].v0.x = vertices[i * 2];
                segments[i].v0.y = vertices[i * 2 + 1];

                if(i == last)
                {
                    segments[i].next = first;
                    segments[i].prev = i - 1;
                    segments[i - 1].v1.set(segments[i].v0);
                }
                else if(i == first)
                {
                    segments[i].next = i + 1;
                    segments[i].prev = last;
                    segments[last].v1.set(segments[i].v0);
                }
                else
                {
                    segments[i].prev = i - 1;
                    segments[i].next = i + 1;
                    segments[i - 1].v1.set(segments[i].v0);
                }

                segments[i].isInserted = false;
            }
        }

        initialise(i - 1);

        constructTrapezoids(i - 1);
        int nmonpoly = monotonateTrapezoids(i - 1);

        triangulateMonotonePolygons(i - 1, nmonpoly, triangles);
    }

    /**
     * Initialize the given number of segments
     *
     * @param n The number of segments to initialize for this run
     */
    private void initialise(int n)
    {
        for(int i = 1; i <= n; i++)
            segments[i].isInserted = false;

        int[] st = new int[n + 1];
        int p;

        choiceIndex = 1;

        randomiser.setSeed(System.currentTimeMillis());

        for(int i = 0; i <= n; i++)
              st[i] = i;

        for(int i = 1; i <= n; i++)
        {
            int m = randomiser.nextInt(1 << 15) % (n + 1 - i) + 1;
            permute[i] = st[i - 1 + m];

            if(m != 1)
                st[i - 1 + m] = st[i];
        }
    }

    /*
     * Main routine to perform trapezoidation.
     *
     * @param nseg The number of segments to create trapezoids for
     */
    private void constructTrapezoids(int nseg)
    {
        // Add the first segment and get the query structure and trapezoid
        // list initialised
        int root = initQueryStructure(chooseSegment());

        for(int i = 1; i <= nseg; i++)
            segments[i].root0 = segments[i].root1 = root;

        int ls_n = logStarN(nseg);

        for(int h = 1; h <= ls_n; h++)
        {
            for(int i = nRatio(nseg, h - 1) + 1; i <= nRatio(nseg, h); i++)
                addSegment(chooseSegment());

            /* Find a new root for each of the segment endpoints */
            for(int i = 1; i <= nseg; i++)
                findNewRoots(i);
        }

        for(int i = nRatio(nseg, ls_n) + 1; i <= nseg; i++)
            addSegment(chooseSegment());
    }

    /**
     * Main routine to get monotone polygons from the trapezoidation of
     * the polygon.
     *
     * @return the number of polygons created
     */
    private int monotonateTrapezoids(int n)
    {
        int i;

        for(i = 0; i < vertexChain.length; i++)
            vertexChain[i].clear();

        for(i = 0; i < visited.length; i++)
            visited[i] = false;

        for(i = 0; i < monotoneChain.length; i++)
            monotoneChain[i].clear();

        for(i = 0; i < monotonePosition.length; i++)
            monotonePosition[i] = 0;

        // First locate a trapezoid which lies inside the polygon
        // and which is triangular
        for(i = 0; i < trapezoidList.length; i++)
            if(insidePolygon(trapezoidList[i]))
                break;

        int tr_start = i;

        // Initialise the mon data-structure and start spanning all the
        // trapezoids within the polygon
        for(i = 1; i <= n; i++)
        {
            monotoneChain[i].prev = segments[i].prev;
            monotoneChain[i].next = segments[i].next;
            monotoneChain[i].vnum = i;
            vertexChain[i].point = segments[i].v0;
            vertexChain[i].nextVertex[0] = segments[i].next; // next vertex
            vertexChain[i].vertexPosition[0] = i;            // locn. of next vertex
            vertexChain[i].nextFree = 1;
        }

        lastVertex = n;
        lastMonotone = 0;

        monotonePosition[0] = 1;  // position of any vertex in the first chain

        // traverse the polygon
        if(trapezoidList[tr_start].u0 > 0)
            traversePolygon(0, tr_start, trapezoidList[tr_start].u0, true);
        else if(trapezoidList[tr_start].d0 > 0)
            traversePolygon(0, tr_start, trapezoidList[tr_start].d0, false);

        // return the number of polygons created
        return newMonotone();
    }

    /**
     * Initilialise the query structure (Q) and the trapezoid table (T)
     * when the first segment is added to start the trapezoidation. The
     * query-tree starts out with 4 trapezoids, one S-node and 2 Y-nodes
     *
     * <pre>
     *                4
     *   -----------------------------------
     *                \
     *      1          \        2
     *                  \
     *   -----------------------------------
     *                3
     * </pre>
     *
     * @param segnum The segment number to initialise for
     */
    private int initQueryStructure(int segnum)
    {
        SeidelSegment s = segments[segnum];

        lastQuery = 1;
        lastTrapezoid = 1;

        for(int i = 0; i < trapezoidList.length; i++)
            trapezoidList[i].clear();

        for(int i = 0; i < queryList.length; i++)
            queryList[i].clear();

        int i1 = newNode();
        queryList[i1].nodeType = SeidelNode.TYPE_Y;
        max(queryList[i1].yVal, s.v0, s.v1); // root
        int root = i1;

        int i2 = newNode();
        queryList[i1].rightChild = i2;
        queryList[i2].nodeType = SeidelNode.TYPE_SINK;
        queryList[i2].parent = i1;

        int i3 = newNode();
        queryList[i1].leftChild = i3;
        queryList[i3].nodeType = SeidelNode.TYPE_Y;
        min(queryList[i3].yVal, s.v0, s.v1);   // root
        queryList[i3].parent = i1;

        int i4 = newNode();
        queryList[i3].leftChild = i4;
        queryList[i4].nodeType = SeidelNode.TYPE_SINK;
        queryList[i4].parent = i3;

        int i5 = newNode();
        queryList[i3].rightChild = i5;
        queryList[i5].nodeType = SeidelNode.TYPE_X;
        queryList[i5].segmentIndex = segnum;
        queryList[i5].parent = i3;

        int i6 = newNode();
        queryList[i5].leftChild = i6;
        queryList[i6].nodeType = SeidelNode.TYPE_SINK;
        queryList[i6].parent = i5;

        int i7 = newNode();
        queryList[i5].rightChild = i7;
        queryList[i7].nodeType = SeidelNode.TYPE_SINK;
        queryList[i7].parent = i5;

        int t1 = newTrapezoid(); // middle left
        int t2 = newTrapezoid(); // middle right
        int t3 = newTrapezoid(); // bottom-most
        int t4 = newTrapezoid(); // topmost

        trapezoidList[t1].hi = queryList[i1].yVal;
        trapezoidList[t2].hi = queryList[i1].yVal;
        trapezoidList[t4].lo = queryList[i1].yVal;
        trapezoidList[t1].lo = queryList[i3].yVal;
        trapezoidList[t2].lo = queryList[i3].yVal;
        trapezoidList[t3].hi = queryList[i3].yVal;

        trapezoidList[t4].hi.y = Float.POSITIVE_INFINITY;
        trapezoidList[t4].hi.x = Float.POSITIVE_INFINITY;
        trapezoidList[t3].lo.y = Float.NEGATIVE_INFINITY;
        trapezoidList[t3].lo.x = Float.NEGATIVE_INFINITY;
        trapezoidList[t1].rightSegment = segnum;
        trapezoidList[t2].leftSegment = segnum;
        trapezoidList[t1].u0 = trapezoidList[t2].u0 = t4;
        trapezoidList[t1].d0 = trapezoidList[t2].d0 = t3;
        trapezoidList[t4].d0 = trapezoidList[t3].u0 = t1;
        trapezoidList[t4].d1 = trapezoidList[t3].u1 = t2;

        trapezoidList[t1].sink = i6;
        trapezoidList[t2].sink = i7;
        trapezoidList[t3].sink = i4;
        trapezoidList[t4].sink = i2;

        trapezoidList[t1].valid = true;
        trapezoidList[t2].valid = true;
        trapezoidList[t3].valid = true;
        trapezoidList[t4].valid = true;

        queryList[i2].trapezoidIndex = t4;
        queryList[i4].trapezoidIndex = t3;
        queryList[i6].trapezoidIndex = t1;
        queryList[i7].trapezoidIndex = t2;

        s.isInserted = true;
        return root;
    }

    /* Add in the new segment into the trapezoidation and update Q and T
     * structures. First locate the two endpoints of the segment in the
     * Q-structure. Then start from the topmost trapezoid and go down to
     * the  lower trapezoid dividing all the trapezoids in between .
     */

    private void addSegment(int segnum)
    {
        SeidelSegment s = segments[segnum];
        SeidelSegment so = segments[segnum];

        int tfirst, tlast, tnext;
        int tfirstr = 0;
        int tlastr = 0;
        int tfirstl = 0;
        int tlastl = 0;
        int t, tn;

        boolean tribot = false;
        boolean is_swapped = false;

        if(greaterThan(s.v1, s.v0)) // Get higher vertex in v0
        {
            Point2d tmp = s.v0;
            s.v0 = s.v1;
            s.v1 = tmp;

            int tmp_r = s.root0;
            s.root0 = s.root1;
            s.root1 = tmp_r;

            is_swapped = true;
        }

        if((is_swapped) ? !inserted(segnum, false) :
                          !inserted(segnum, true))
        {
            // insert v0 in the tree
            int tmp_d;

            int tu = locateEndpoint(s.v0, s.v1, s.root0);
            int tl = newTrapezoid();           // tl is the new lower trapezoid
            trapezoidList[tl].valid = true;
            trapezoidList[tl] = trapezoidList[tu];
            trapezoidList[tu].lo.y = trapezoidList[tl].hi.y = s.v0.y;
            trapezoidList[tu].lo.x = trapezoidList[tl].hi.x = s.v0.x;
            trapezoidList[tu].d0 = tl;
            trapezoidList[tu].d1 = 0;
            trapezoidList[tl].u0 = tu;
            trapezoidList[tl].u1 = 0;

            if(((tmp_d = trapezoidList[tl].d0) > 0) && (trapezoidList[tmp_d].u0 == tu))
                trapezoidList[tmp_d].u0 = tl;
            if(((tmp_d = trapezoidList[tl].d0) > 0) && (trapezoidList[tmp_d].u1 == tu))
                trapezoidList[tmp_d].u1 = tl;

            if(((tmp_d = trapezoidList[tl].d1) > 0) && (trapezoidList[tmp_d].u0 == tu))
                trapezoidList[tmp_d].u0 = tl;
            if(((tmp_d = trapezoidList[tl].d1) > 0) && (trapezoidList[tmp_d].u1 == tu))
                trapezoidList[tmp_d].u1 = tl;

            // Now update the query structure and obtain the sinks for the
            // two trapezoids
            int i1 = newNode();           // Upper trapezoid sink
            int i2 = newNode();           // Lower trapezoid sink
            int sk = trapezoidList[tu].sink;

            queryList[sk].nodeType = SeidelNode.TYPE_Y;
            queryList[sk].yVal = s.v0;
            queryList[sk].segmentIndex = segnum;   // not really reqd ... maybe later
            queryList[sk].leftChild = i2;
            queryList[sk].rightChild = i1;

            queryList[i1].nodeType = SeidelNode.TYPE_SINK;
            queryList[i1].trapezoidIndex = tu;
            queryList[i1].parent = sk;

            queryList[i2].nodeType = SeidelNode.TYPE_SINK;
            queryList[i2].trapezoidIndex = tl;
            queryList[i2].parent = sk;

            trapezoidList[tu].sink = i1;
            trapezoidList[tl].sink = i2;
            tfirst = tl;
        }
        else
        {
            // v0 already present
            // Get the topmost intersecting trapezoid
            tfirst = locateEndpoint(s.v0, s.v1, s.root0);
        }


        if((is_swapped) ? !inserted(segnum, true) :
                          !inserted(segnum, false))
        {
            // insert v1 in the tree
            int tmp_d;

            int tu = locateEndpoint(s.v1, s.v0, s.root1);
            int tl = newTrapezoid();           // tl is the new lower trapezoid
            trapezoidList[tl].valid = true;
            trapezoidList[tl] = trapezoidList[tu];
            trapezoidList[tu].lo.y = trapezoidList[tl].hi.y = s.v1.y;
            trapezoidList[tu].lo.x = trapezoidList[tl].hi.x = s.v1.x;
            trapezoidList[tu].d0 = tl;
            trapezoidList[tu].d1 = 0;
            trapezoidList[tl].u0 = tu;
            trapezoidList[tl].u1 = 0;

            if(((tmp_d = trapezoidList[tl].d0) > 0) && (trapezoidList[tmp_d].u0 == tu))
                trapezoidList[tmp_d].u0 = tl;
            if(((tmp_d = trapezoidList[tl].d0) > 0) && (trapezoidList[tmp_d].u1 == tu))
                trapezoidList[tmp_d].u1 = tl;

            if(((tmp_d = trapezoidList[tl].d1) > 0) && (trapezoidList[tmp_d].u0 == tu))
                trapezoidList[tmp_d].u0 = tl;
            if(((tmp_d = trapezoidList[tl].d1) > 0) && (trapezoidList[tmp_d].u1 == tu))
                trapezoidList[tmp_d].u1 = tl;

            // Now update the query structure and obtain the sinks for the
            // two trapezoids

            int i1 = newNode();           // Upper trapezoid sink
            int i2 = newNode();           // Lower trapezoid sink
            int sk = trapezoidList[tu].sink;

            queryList[sk].nodeType = SeidelNode.TYPE_Y;
            queryList[sk].yVal = s.v1;
            queryList[sk].segmentIndex = segnum;   // not really reqd ... maybe later
            queryList[sk].leftChild = i2;
            queryList[sk].rightChild = i1;

            queryList[i1].nodeType = SeidelNode.TYPE_SINK;
            queryList[i1].trapezoidIndex = tu;
            queryList[i1].parent = sk;

            queryList[i2].nodeType = SeidelNode.TYPE_SINK;
            queryList[i2].trapezoidIndex = tl;
            queryList[i2].parent = sk;

            trapezoidList[tu].sink = i1;
            trapezoidList[tl].sink = i2;
            tlast = tu;
        }
        else
        {
            // v1 already present
            // Get the lowermost intersecting trapezoid
            tlast = locateEndpoint(s.v1, s.v0, s.root1);
            tribot = true;
        }

        // Thread the segment into the query tree creating a new X-node
        // First, split all the trapezoids which are intersected by s into two

        t = tfirst;
        while((t > 0) && greaterThanOrEqualTo(trapezoidList[t].lo, trapezoidList[tlast].lo))
        {
            // traverse from top to bot
            int sk = trapezoidList[t].sink;
            int i1 = newNode();           // left trapezoid sink
            int i2 = newNode();           // right trapezoid sink

            queryList[sk].nodeType = SeidelNode.TYPE_X;
            queryList[sk].segmentIndex = segnum;
            queryList[sk].leftChild = i1;
            queryList[sk].rightChild = i2;

            queryList[i1].nodeType = SeidelNode.TYPE_SINK; // left use existing one
            queryList[i1].trapezoidIndex = t;
            queryList[i1].parent = sk;

            tn = newTrapezoid();
            queryList[i2].nodeType = SeidelNode.TYPE_SINK; // right allocate new
            queryList[i2].trapezoidIndex = tn;
            trapezoidList[tn].valid = true;
            queryList[i2].parent = sk;

            if(t == tfirst)
                tfirstr = tn;

            if(equalsEpsilon(trapezoidList[t].lo, trapezoidList[tlast].lo))
                tlastr = tn;

            trapezoidList[tn] = trapezoidList[t];
            trapezoidList[t].sink = i1;
            trapezoidList[tn].sink = i2;
            int t_sav = t;
            int tn_sav = tn;

            // Sanity check. This should never fail
            if((trapezoidList[t].d0 <= 0) && (trapezoidList[t].d1 <= 0))
            {
                System.err.println("add_segment: error\n");
                break;
            }
            else if((trapezoidList[t].d0 > 0) && (trapezoidList[t].d1 <= 0))
            {
                // only one trapezoid below. partition t into two and make the
                // two resulting trapezoids t and tn as the upper neighbours of
                // the sole lower trapezoid

                // Only one trapezoid below
                if((trapezoidList[t].u0 > 0) && (trapezoidList[t].u1 > 0))
                {
                    // continuation of a chain from abv.
                    if(trapezoidList[t].usave > 0)
                    {
                        // three upper neighbours
                        if(trapezoidList[t].mergeSideLeft)
                        {
                          trapezoidList[tn].u0 = trapezoidList[t].u1;
                          trapezoidList[t].u1 = -1;
                          trapezoidList[tn].u1 = trapezoidList[t].usave;

                          trapezoidList[trapezoidList[t].u0].d0 = t;
                          trapezoidList[trapezoidList[tn].u0].d0 = tn;
                          trapezoidList[trapezoidList[tn].u1].d0 = tn;
                        }
                        else // intersects in the right
                        {
                          trapezoidList[tn].u1 = -1;
                          trapezoidList[tn].u0 = trapezoidList[t].u1;
                          trapezoidList[t].u1 = trapezoidList[t].u0;
                          trapezoidList[t].u0 = trapezoidList[t].usave;

                          trapezoidList[trapezoidList[t].u0].d0 = t;
                          trapezoidList[trapezoidList[t].u1].d0 = t;
                          trapezoidList[trapezoidList[tn].u0].d0 = tn;
                        }

                        trapezoidList[t].usave = trapezoidList[tn].usave = 0;
                    }
                    else  // No usave.... simple case
                    {
                        trapezoidList[tn].u0 = trapezoidList[t].u1;
                        trapezoidList[t].u1 = trapezoidList[tn].u1 = -1;
                        trapezoidList[trapezoidList[tn].u0].d0 = tn;
                    }
                }
                else
                {
                    // fresh seg. or upward cusp
                    int tmp_u = trapezoidList[t].u0;
                    int td0, td1;
                    if(((td0 = trapezoidList[tmp_u].d0) > 0) &&
                       ((td1 = trapezoidList[tmp_u].d1) > 0))
                    {
                        // upward cusp
                        if((trapezoidList[td0].rightSegment > 0) && !isLeftOf(trapezoidList[td0].rightSegment, s.v1))
                        {
                            trapezoidList[t].u0 = trapezoidList[t].u1 = trapezoidList[tn].u1 = -1;
                            trapezoidList[trapezoidList[tn].u0].d1 = tn;
                        }
                        else
                        {
                            // cusp going leftwards
                          trapezoidList[tn].u0 = trapezoidList[tn].u1 = trapezoidList[t].u1 = -1;
                          trapezoidList[trapezoidList[t].u0].d0 = t;
                        }
                    }
                    else
                    {
                        // fresh segment
                        trapezoidList[trapezoidList[t].u0].d0 = t;
                        trapezoidList[trapezoidList[t].u0].d1 = tn;
                    }
                }

                if(equalsEpsilon(trapezoidList[t].lo.y, trapezoidList[tlast].lo.y) &&
                   equalsEpsilon(trapezoidList[t].lo.x, trapezoidList[tlast].lo.x) && tribot)
                {
                    // bottom forms a triangle
                    int seg = is_swapped ?
                              segments[segnum].prev :
                              segments[segnum].next;

                    if((seg > 0) && isLeftOf(seg, s.v0))
                    {
                      // L-R downward cusp
                      trapezoidList[trapezoidList[t].d0].u0 = t;
                      trapezoidList[tn].d0 = trapezoidList[tn].d1 = -1;
                    }
                    else
                    {
                      // R-L downward cusp
                      trapezoidList[trapezoidList[tn].d0].u1 = tn;
                      trapezoidList[t].d0 = trapezoidList[t].d1 = -1;
                    }
                }
                else
                {
                    if((trapezoidList[trapezoidList[t].d0].u0 > 0) && (trapezoidList[trapezoidList[t].d0].u1 > 0))
                    {
                        // Does it pass through the LHS
                        if(trapezoidList[trapezoidList[t].d0].u0 == t)
                        {
                          trapezoidList[trapezoidList[t].d0].usave = trapezoidList[trapezoidList[t].d0].u1;
                          trapezoidList[trapezoidList[t].d0].mergeSideLeft = true;
                        }
                        else
                        {
                          trapezoidList[trapezoidList[t].d0].usave = trapezoidList[trapezoidList[t].d0].u0;
                          trapezoidList[trapezoidList[t].d0].mergeSideLeft = false;
                        }
                    }
                    trapezoidList[trapezoidList[t].d0].u0 = t;
                    trapezoidList[trapezoidList[t].d0].u1 = tn;
                }

                t = trapezoidList[t].d0;
            }
            else if((trapezoidList[t].d0 <= 0) && (trapezoidList[t].d1 > 0))
            {
                // Only one trapezoid below
                if((trapezoidList[t].u0 > 0) && (trapezoidList[t].u1 > 0))
                {
                    // continuation of a chain from abv
                    if(trapezoidList[t].usave > 0)
                    {
                        // three upper neighbours
                        if(trapezoidList[t].mergeSideLeft)
                        {
                            trapezoidList[tn].u0 = trapezoidList[t].u1;
                            trapezoidList[t].u1 = -1;
                            trapezoidList[tn].u1 = trapezoidList[t].usave;

                            trapezoidList[trapezoidList[t].u0].d0 = t;
                            trapezoidList[trapezoidList[tn].u0].d0 = tn;
                            trapezoidList[trapezoidList[tn].u1].d0 = tn;
                        }
                        else
                        {
                            // intersects in the right
                            trapezoidList[tn].u1 = -1;
                            trapezoidList[tn].u0 = trapezoidList[t].u1;
                            trapezoidList[t].u1 = trapezoidList[t].u0;
                            trapezoidList[t].u0 = trapezoidList[t].usave;

                            trapezoidList[trapezoidList[t].u0].d0 = t;
                            trapezoidList[trapezoidList[t].u1].d0 = t;
                            trapezoidList[trapezoidList[tn].u0].d0 = tn;
                        }

                        trapezoidList[t].usave = trapezoidList[tn].usave = 0;
                    }
                    else
                    {
                        // No usave.... simple case
                        trapezoidList[tn].u0 = trapezoidList[t].u1;
                        trapezoidList[t].u1 = trapezoidList[tn].u1 = -1;
                        trapezoidList[trapezoidList[tn].u0].d0 = tn;
                    }
                }
                else
                {
                    // fresh seg. or upward cusp
                    int tmp_u = trapezoidList[t].u0;
                    int td0, td1;
                    if(((td0 = trapezoidList[tmp_u].d0) > 0) &&
                       ((td1 = trapezoidList[tmp_u].d1) > 0))
                    {
                        // upward cusp
                        if ((trapezoidList[td0].rightSegment > 0) &&
                          !isLeftOf(trapezoidList[td0].rightSegment, s.v1))
                        {
                            trapezoidList[t].u0 = trapezoidList[t].u1 = trapezoidList[tn].u1 = -1;
                            trapezoidList[trapezoidList[tn].u0].d1 = tn;
                        }
                        else
                        {
                            trapezoidList[tn].u0 = trapezoidList[tn].u1 = trapezoidList[t].u1 = -1;
                            trapezoidList[trapezoidList[t].u0].d0 = t;
                        }
                    }
                    else
                    {
                        // fresh segment
                        trapezoidList[trapezoidList[t].u0].d0 = t;
                        trapezoidList[trapezoidList[t].u0].d1 = tn;
                    }
                }

                if(equalsEpsilon(trapezoidList[t].lo.y, trapezoidList[tlast].lo.y) &&
                   equalsEpsilon(trapezoidList[t].lo.x, trapezoidList[tlast].lo.x) && tribot)
                {
                    // bottom forms a triangle
                    int seg = is_swapped ?
                              segments[segnum].prev :
                              segments[segnum].next;

                    if((seg > 0) && isLeftOf(seg, s.v0))
                    {
                      // L-R downward cusp
                        trapezoidList[trapezoidList[t].d1].u0 = t;
                        trapezoidList[tn].d0 = trapezoidList[tn].d1 = -1;
                    }
                    else
                    {
                      // R-L downward cusp
                      trapezoidList[trapezoidList[tn].d1].u1 = tn;
                      trapezoidList[t].d0 = trapezoidList[t].d1 = -1;
                    }
                }
                else
                {
                    if((trapezoidList[trapezoidList[t].d1].u0 > 0) && (trapezoidList[trapezoidList[t].d1].u1 > 0))
                    {
                        // Does it pass thru the LHS?
                        if(trapezoidList[trapezoidList[t].d1].u0 == t)
                        {
                            trapezoidList[trapezoidList[t].d1].usave = trapezoidList[trapezoidList[t].d1].u1;
                            trapezoidList[trapezoidList[t].d1].mergeSideLeft = true;
                        }
                        else
                        {
                            trapezoidList[trapezoidList[t].d1].usave = trapezoidList[trapezoidList[t].d1].u0;
                            trapezoidList[trapezoidList[t].d1].mergeSideLeft = false;
                        }
                    }

                    trapezoidList[trapezoidList[t].d1].u0 = t;
                    trapezoidList[trapezoidList[t].d1].u1 = tn;
                }

                t = trapezoidList[t].d1;
            }
            else
            {
                 // two trapezoids below. Find out which one is intersected by
                 // this segment and proceed down that one

                int tmpseg = trapezoidList[trapezoidList[t].d0].rightSegment;

                boolean i_d0 = false;
                boolean i_d1 = false;

                if(equalsEpsilon(trapezoidList[t].lo.y, s.v0.y))
                {
                    if (trapezoidList[t].lo.x > s.v0.x)
                        i_d0 = true;
                    else
                        i_d1 = true;
                }
                else
                {
                    // TODO: fix this dynamic allocation
                    Point2d tmppt = new Point2d();
                    tmppt.y = trapezoidList[t].lo.y;
                    double y0 = trapezoidList[t].lo.y;

                    double yt = (y0 - s.v0.y) / (s.v1.y - s.v0.y);
                    tmppt.x = s.v0.x + yt * (s.v1.x - s.v0.x);

                    if(lessThan(tmppt, trapezoidList[t].lo))
                        i_d0 = true;
                    else
                        i_d1 = true;
                }

                // check continuity from the top so that the lower-neighbour
                // values are properly filled for the upper trapezoid

                if((trapezoidList[t].u0 > 0) && (trapezoidList[t].u1 > 0))
                {
                    // continuation of a chain from abv.
                    if(trapezoidList[t].usave > 0)
                    {
                        // three upper neighbours
                        if(trapezoidList[t].mergeSideLeft)
                        {
                            trapezoidList[tn].u0 = trapezoidList[t].u1;
                            trapezoidList[t].u1 = -1;
                            trapezoidList[tn].u1 = trapezoidList[t].usave;

                            trapezoidList[trapezoidList[t].u0].d0 = t;
                            trapezoidList[trapezoidList[tn].u0].d0 = tn;
                            trapezoidList[trapezoidList[tn].u1].d0 = tn;
                        }
                        else
                        {
                            // intersects in the right
                            trapezoidList[tn].u1 = -1;
                            trapezoidList[tn].u0 = trapezoidList[t].u1;
                            trapezoidList[t].u1 = trapezoidList[t].u0;
                            trapezoidList[t].u0 = trapezoidList[t].usave;

                            trapezoidList[trapezoidList[t].u0].d0 = t;
                            trapezoidList[trapezoidList[t].u1].d0 = t;
                            trapezoidList[trapezoidList[tn].u0].d0 = tn;
                        }

                        trapezoidList[t].usave = trapezoidList[tn].usave = 0;
                    }
                    else
                    {
                        // No usave.... simple case
                        trapezoidList[tn].u0 = trapezoidList[t].u1;
                        trapezoidList[tn].u1 = -1;
                        trapezoidList[t].u1 = -1;
                        trapezoidList[trapezoidList[tn].u0].d0 = tn;
                    }
                }
                else
                {
                    // fresh seg. or upward cusp
                    int tmp_u = trapezoidList[t].u0;
                    int td0, td1;
                    if(((td0 = trapezoidList[tmp_u].d0) > 0) &&
                       ((td1 = trapezoidList[tmp_u].d1) > 0))
                    {
                        // upward cusp
                        if((trapezoidList[td0].rightSegment > 0) &&
                           !isLeftOf(trapezoidList[td0].rightSegment, s.v1))
                        {
                            trapezoidList[t].u0 = trapezoidList[t].u1 = trapezoidList[tn].u1 = -1;
                            trapezoidList[trapezoidList[tn].u0].d1 = tn;
                        }
                        else
                        {
                            trapezoidList[tn].u0 = trapezoidList[tn].u1 = trapezoidList[t].u1 = -1;
                            trapezoidList[trapezoidList[t].u0].d0 = t;
                        }
                    }
                    else
                    {
                        // fresh segment
                        trapezoidList[trapezoidList[t].u0].d0 = t;
                        trapezoidList[trapezoidList[t].u0].d1 = tn;
                    }
                }

                if(equalsEpsilon(trapezoidList[t].lo.y, trapezoidList[tlast].lo.y) &&
                   equalsEpsilon(trapezoidList[t].lo.x, trapezoidList[tlast].lo.x) && tribot)
                {
                    // this case arises only at the lowest trapezoid.. i.e.
                    // tlast, if the lower endpoint of the segment is
                    // already inserted in the structure

                    trapezoidList[trapezoidList[t].d0].u0 = t;
                    trapezoidList[trapezoidList[t].d0].u1 = -1;
                    trapezoidList[trapezoidList[t].d1].u0 = tn;
                    trapezoidList[trapezoidList[t].d1].u1 = -1;

                    trapezoidList[tn].d0 = trapezoidList[t].d1;
                    trapezoidList[t].d1 = trapezoidList[tn].d1 = -1;

                    tnext = trapezoidList[t].d1;
                }
                else if (i_d0)
                {
                    // intersecting d0
                    trapezoidList[trapezoidList[t].d0].u0 = t;
                    trapezoidList[trapezoidList[t].d0].u1 = tn;
                    trapezoidList[trapezoidList[t].d1].u0 = tn;
                    trapezoidList[trapezoidList[t].d1].u1 = -1;

                    // new code to determine the bottom neighbours of the
                    // newly partitioned trapezoid
                    trapezoidList[t].d1 = -1;

                    tnext = trapezoidList[t].d0;
                }
                else
                {
                    // intersecting d1
                    trapezoidList[trapezoidList[t].d0].u0 = t;
                    trapezoidList[trapezoidList[t].d0].u1 = -1;
                    trapezoidList[trapezoidList[t].d1].u0 = t;
                    trapezoidList[trapezoidList[t].d1].u1 = tn;

                    // new code to determine the bottom neighbours of the
                    // newly partitioned trapezoid

                    trapezoidList[tn].d0 = trapezoidList[t].d1;
                    trapezoidList[tn].d1 = -1;

                    tnext = trapezoidList[t].d1;
                }

                t = tnext;
            }

            trapezoidList[t_sav].rightSegment = trapezoidList[tn_sav].leftSegment  = segnum;
        }

        // Now combine those trapezoids which share common segments. We can
        // use the pointers to the parent to connect these together. This
        // works only because all these new trapezoids have been formed
        // due to splitting by the segment, and hence have only one parent
        tfirstl = tfirst;
        tlastl = tlast;
        mergeTrapezoids(segnum, tfirstl, tlastl, true);
        mergeTrapezoids(segnum, tfirstr, tlastr, false);

        segments[segnum].isInserted = true;
    }

    /**
     * Thread in the segment into the existing trapezoidation. The
     * limiting trapezoids are given by tfirst and tlast (which are the
     * trapezoids containing the two endpoints of the segment). Merges all
     * possible trapezoids which flank this segment and have been recently
     * divided because of its insertion
     *
     * @param segnum The segment number of the trapezoid to merge
     * @param leftSide true if this is a left size or false for right
     */
    private void mergeTrapezoids(int segnum,
                                 int tfirst,
                                 int tlast,
                                 boolean leftSide)
    {
        int t = tfirst;

        // First merge polys on the LHS
        while((t > 0) && greaterThanOrEqualTo(trapezoidList[t].lo, trapezoidList[tlast].lo))
        {
            boolean cond;
            int tnext;

            if(leftSide)
               cond = ((((tnext = trapezoidList[t].d0) > 0) &&
                        (trapezoidList[tnext].rightSegment == segnum)) ||
                       (((tnext = trapezoidList[t].d1) > 0) &&
                        (trapezoidList[tnext].rightSegment == segnum)));
            else
               cond = ((((tnext = trapezoidList[t].d0) > 0) &&
                        (trapezoidList[tnext].leftSegment == segnum)) ||
                       (((tnext = trapezoidList[t].d1) > 0) &&
                        (trapezoidList[tnext].leftSegment == segnum)));

            if(cond)
            {
                if((trapezoidList[t].leftSegment == trapezoidList[tnext].leftSegment) &&
                   (trapezoidList[t].rightSegment == trapezoidList[tnext].rightSegment))
                {
                    // good neighbours, merge them
                    // Use the upper node as the new node i.e. t
                    int ptnext = queryList[trapezoidList[tnext].sink].parent;

                    if(queryList[ptnext].leftChild == trapezoidList[tnext].sink)
                        queryList[ptnext].leftChild = trapezoidList[t].sink;
                    else
                        queryList[ptnext].rightChild = trapezoidList[t].sink;  // redirect parent

                    // Change the upper neighbours of the lower trapezoids
                    if((trapezoidList[t].d0 = trapezoidList[tnext].d0) > 0)
                        if(trapezoidList[trapezoidList[t].d0].u0 == tnext)
                            trapezoidList[trapezoidList[t].d0].u0 = t;
                      else if(trapezoidList[trapezoidList[t].d0].u1 == tnext)
                          trapezoidList[trapezoidList[t].d0].u1 = t;

                    if((trapezoidList[t].d1 = trapezoidList[tnext].d1) > 0)
                        if(trapezoidList[trapezoidList[t].d1].u0 == tnext)
                            trapezoidList[trapezoidList[t].d1].u0 = t;
                        else if(trapezoidList[trapezoidList[t].d1].u1 == tnext)
                            trapezoidList[trapezoidList[t].d1].u1 = t;

                    trapezoidList[t].lo = trapezoidList[tnext].lo;
                    trapezoidList[tnext].valid = false; // invalidate the lower trapezium
                }
                else
                {
                    // not good neighbours
                    t = tnext;
                }
            }
            else
                t = tnext;
        }
    }

    /*
     * Update the roots stored for each of the endpoints of the segment.
     * This is done to speed up the location-query for the endpoint when
     * the segment is inserted into the trapezoidation subsequently
     *
     * @param segnum The index of the segment to find roots for
     */
    private void findNewRoots(int segnum)
    {
        SeidelSegment s = segments[segnum];

        if(s.isInserted)
            return;

        s.root0 = locateEndpoint(s.v0, s.v1, s.root0);
        s.root0 = trapezoidList[s.root0].sink;

        s.root1 = locateEndpoint(s.v1, s.v0, s.root1);
        s.root1 = trapezoidList[s.root1].sink;
    }

    /**
     * A query routine that determines which trapezoid the point v lies in.
     * The return value is the trapezoid number.
     */
    private int locateEndpoint(Point2d v, Point2d vo, int r)
    {
        SeidelNode rptr = queryList[r];

        int ret_val = 0;

        switch(rptr.nodeType)
        {
            case SeidelNode.TYPE_SINK:
                ret_val = rptr.trapezoidIndex;
                break;

            case SeidelNode.TYPE_Y:
                if(greaterThan(v, rptr.yVal)) // above
                    ret_val = locateEndpoint(v, vo, rptr.rightChild);
                else if(equalsEpsilon(v, rptr.yVal))
                {
                    // the point is already inserted.
                    if(greaterThan(vo, rptr.yVal)) // above
                        ret_val = locateEndpoint(v, vo, rptr.rightChild);
                    else
                        ret_val = locateEndpoint(v, vo, rptr.leftChild); // below
                }
                else
                    ret_val = locateEndpoint(v, vo, rptr.leftChild); // below
                break;

            case SeidelNode.TYPE_X:
                if(equalsEpsilon(v, segments[rptr.segmentIndex].v0) ||
                    equalsEpsilon(v, segments[rptr.segmentIndex].v1))
                {
                    if(equalsEpsilon(v.y, vo.y)) // horizontal segment
                    {
                        if(vo.x < v.x)
                            ret_val = locateEndpoint(v, vo, rptr.leftChild);
                        else
                            ret_val = locateEndpoint(v, vo, rptr.rightChild);
                    }
                    else if(isLeftOf(rptr.segmentIndex, vo))
                        ret_val = locateEndpoint(v, vo, rptr.leftChild);
                    else
                        ret_val = locateEndpoint(v, vo, rptr.rightChild);
                }
                else if(isLeftOf(rptr.segmentIndex, v))
                    ret_val = locateEndpoint(v, vo, rptr.leftChild);
                else
                    ret_val = locateEndpoint(v, vo, rptr.rightChild);
                break;
        }

        return ret_val;
    }

    // Stuff for monotonation

    /**
     * Recursively visit all the trapezoids
     */
    private void traversePolygon(int mcur, int trapezoidIndex, int from, boolean up)
    {
        SeidelTrapezoid t = trapezoidList[trapezoidIndex];
        int v0, v1, v0next, v1next;

        if((trapezoidIndex <= 0) || visited[trapezoidIndex])
            return;

        visited[trapezoidIndex] = true;

        // We have much more information available here.
        // rseg: goes upwards
        // lseg: goes downwards
        // Initially assume that up = false (from the left)
        // Switch v0 and v1 if necessary afterwards


        // special cases for triangles with cusps at the opposite ends.
        // take care of this first
        if((t.u0 <= 0) && (t.u1 <= 0))
        {
            if((t.d0 > 0) && (t.d1 > 0)) // downward opening triangle
            {
                v0 = trapezoidList[t.d1].leftSegment;
                v1 = t.leftSegment;
                if (from == t.d1)
                {
                    int mnew = makeNewMonotonePoly(mcur, v1, v0);
                    traversePolygon(mcur, t.d1, trapezoidIndex, true);
                    traversePolygon(mnew, t.d0, trapezoidIndex, true);
                }
                else
                {
                    int mnew = makeNewMonotonePoly(mcur, v0, v1);
                    traversePolygon(mcur, t.d0, trapezoidIndex, true);
                    traversePolygon(mnew, t.d1, trapezoidIndex, true);
                }
            }
            else
            {
                traversePolygon(mcur, t.u0, trapezoidIndex, false);
                traversePolygon(mcur, t.u1, trapezoidIndex, false);
                traversePolygon(mcur, t.d0, trapezoidIndex, true);
                traversePolygon(mcur, t.d1, trapezoidIndex, true);
            }
        }
        else if ((t.d0 <= 0) && (t.d1 <= 0))
        {
            if ((t.u0 > 0) && (t.u1 > 0)) // upward opening triangle
            {
                v0 = t.rightSegment;
                v1 = trapezoidList[t.u0].rightSegment;
                if (from == t.u1)
                {
                    int mnew = makeNewMonotonePoly(mcur, v1, v0);
                    traversePolygon(mcur, t.u1, trapezoidIndex, false);
                    traversePolygon(mnew, t.u0, trapezoidIndex, false);
                }
                else
                {
                    int mnew = makeNewMonotonePoly(mcur, v0, v1);
                    traversePolygon(mcur, t.u0, trapezoidIndex, false);
                    traversePolygon(mnew, t.u1, trapezoidIndex, false);
                }
            }
            else
            {
                traversePolygon(mcur, t.u0, trapezoidIndex, false);
                traversePolygon(mcur, t.u1, trapezoidIndex, false);
                traversePolygon(mcur, t.d0, trapezoidIndex, true);
                traversePolygon(mcur, t.d1, trapezoidIndex, true);
            }
        }
        else if((t.u0 > 0) && (t.u1 > 0))
        {
            if((t.d0 > 0) && (t.d1 > 0)) // downward + upward cusps
            {
                v0 = trapezoidList[t.d1].leftSegment;
                v1 = trapezoidList[t.u0].rightSegment;

                if((!up && (t.d1 == from)) || (up && (t.u1 == from)))
                {
                    int mnew = makeNewMonotonePoly(mcur, v1, v0);
                    traversePolygon(mcur, t.u1, trapezoidIndex, false);
                    traversePolygon(mcur, t.d1, trapezoidIndex, true);
                    traversePolygon(mnew, t.u0, trapezoidIndex, false);
                    traversePolygon(mnew, t.d0, trapezoidIndex, true);
                }
                else
                {
                    int mnew = makeNewMonotonePoly(mcur, v0, v1);
                    traversePolygon(mcur, t.u0, trapezoidIndex, false);
                    traversePolygon(mcur, t.d0, trapezoidIndex, true);
                    traversePolygon(mnew, t.u1, trapezoidIndex, false);
                    traversePolygon(mnew, t.d1, trapezoidIndex, true);
                }
            }
            else  // only downward cusp
            {
                if(equalsEpsilon(t.lo, segments[t.leftSegment].v1))
                {
                    v0 = trapezoidList[t.u0].rightSegment;
                    v1 = segments[t.leftSegment].next;

                    if(up && (t.u0 == from))
                    {
                        int mnew = makeNewMonotonePoly(mcur, v1, v0);
                        traversePolygon(mcur, t.u0, trapezoidIndex, false);
                        traversePolygon(mnew, t.d0, trapezoidIndex, true);
                        traversePolygon(mnew, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.d1, trapezoidIndex, true);
                    }
                    else
                    {
                        int mnew = makeNewMonotonePoly(mcur, v0, v1);
                        traversePolygon(mcur, t.u1, trapezoidIndex, false);
                        traversePolygon(mcur, t.d0, trapezoidIndex, true);
                        traversePolygon(mcur, t.d1, trapezoidIndex, true);
                        traversePolygon(mnew, t.u0, trapezoidIndex, false);
                    }
                }
                else
                {
                    v0 = t.rightSegment;
                    v1 = trapezoidList[t.u0].rightSegment;

                    if(up && (t.u1 == from))
                    {
                        int mnew = makeNewMonotonePoly(mcur, v1, v0);
                        traversePolygon(mcur, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.d1, trapezoidIndex, true);
                        traversePolygon(mnew, t.d0, trapezoidIndex, true);
                        traversePolygon(mnew, t.u0, trapezoidIndex, false);
                    }
                    else
                    {
                        int mnew = makeNewMonotonePoly(mcur, v0, v1);
                        traversePolygon(mcur, t.u0, trapezoidIndex, false);
                        traversePolygon(mcur, t.d0, trapezoidIndex, true);
                        traversePolygon(mcur, t.d1, trapezoidIndex, true);
                        traversePolygon(mnew, t.u1, trapezoidIndex, false);
                    }
                }
            }
        }
        else if((t.u0 > 0) || (t.u1 > 0)) // no downward cusp
        {
            if((t.d0 > 0) && (t.d1 > 0)) // only upward cusp
            {
                if(equalsEpsilon(t.hi, segments[t.leftSegment].v0))
                {
                    v0 = trapezoidList[t.d1].leftSegment;
                    v1 = t.leftSegment;

                    if(!(!up && (t.d0 == from)))
                    {
                        int mnew = makeNewMonotonePoly(mcur, v1, v0);
                        traversePolygon(mcur, t.u1, trapezoidIndex, false);
                        traversePolygon(mcur, t.d1, trapezoidIndex, true);
                        traversePolygon(mcur, t.u0, trapezoidIndex, false);
                        traversePolygon(mnew, t.d0, trapezoidIndex, true);
                    }
                    else
                    {
                        int mnew = makeNewMonotonePoly(mcur, v0, v1);
                        traversePolygon(mcur, t.d0, trapezoidIndex, true);
                        traversePolygon(mnew, t.u0, trapezoidIndex, false);
                        traversePolygon(mnew, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.d1, trapezoidIndex, true);
                    }
                }
                else
                {
                    v0 = trapezoidList[t.d1].leftSegment;
                    v1 = segments[t.rightSegment].next;

                    if(!up && (t.d1 == from))
                    {
                        int mnew = makeNewMonotonePoly(mcur, v1, v0);
                        traversePolygon(mcur, t.d1, trapezoidIndex, true);
                        traversePolygon(mnew, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.u0, trapezoidIndex, false);
                        traversePolygon(mnew, t.d0, trapezoidIndex, true);
                    }
                    else
                    {
                        int mnew = makeNewMonotonePoly(mcur, v0, v1);
                        traversePolygon(mcur, t.u0, trapezoidIndex, false);
                        traversePolygon(mcur, t.d0, trapezoidIndex, true);
                        traversePolygon(mcur, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.d1, trapezoidIndex, true);
                    }
                }
            }
            else  // no cusp
            {
                if(equalsEpsilon(t.hi, segments[t.leftSegment].v0) &&
                    equalsEpsilon(t.lo, segments[t.rightSegment].v0))
                {
                    v0 = t.rightSegment;
                    v1 = t.leftSegment;

                    if(up)
                    {
                        int mnew = makeNewMonotonePoly(mcur, v1, v0);
                        traversePolygon(mcur, t.u0, trapezoidIndex, false);
                        traversePolygon(mcur, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.d1, trapezoidIndex, true);
                        traversePolygon(mnew, t.d0, trapezoidIndex, true);
                    }
                    else
                    {
                        int mnew = makeNewMonotonePoly(mcur, v0, v1);
                        traversePolygon(mcur, t.d1, trapezoidIndex, true);
                        traversePolygon(mcur, t.d0, trapezoidIndex, true);
                        traversePolygon(mnew, t.u0, trapezoidIndex, false);
                        traversePolygon(mnew, t.u1, trapezoidIndex, false);
                    }
                }
                else if(equalsEpsilon(t.hi,segments[t.rightSegment].v1) &&
                        equalsEpsilon(t.lo, segments[t.leftSegment].v1))
                {
                    v0 = segments[t.rightSegment].next;
                    v1 = segments[t.leftSegment].next;

                    if(up)
                    {
                        int mnew = makeNewMonotonePoly(mcur, v1, v0);
                        traversePolygon(mcur, t.u0, trapezoidIndex, false);
                        traversePolygon(mcur, t.u1, trapezoidIndex, false);
                        traversePolygon(mnew, t.d1, trapezoidIndex, true);
                        traversePolygon(mnew, t.d0, trapezoidIndex, true);
                    }
                    else
                    {
                        int mnew = makeNewMonotonePoly(mcur, v0, v1);
                        traversePolygon(mcur, t.d1, trapezoidIndex, true);
                        traversePolygon(mcur, t.d0, trapezoidIndex, true);
                        traversePolygon(mnew, t.u0, trapezoidIndex, false);
                        traversePolygon(mnew, t.u1, trapezoidIndex, false);
                    }
                }
                else // no split possible
                {
                    traversePolygon(mcur, t.u0, trapezoidIndex, false);
                    traversePolygon(mcur, t.d0, trapezoidIndex, true);
                    traversePolygon(mcur, t.u1, trapezoidIndex, false);
                    traversePolygon(mcur, t.d1, trapezoidIndex, true);
                }
            }
        }
    }

    /**
     * For each monotone polygon, find the ymax and ymin (to determine the
     * two y-monotone chains) and pass on this monotone polygon for greedy
     * triangulation.
     * Take care not to triangulate duplicate monotone polygons
     */
    private void triangulateMonotonePolygons(int nvert,
                                             int nmonpoly,
                                             int[] outputTriangles)
    {
        int op_idx = 0;

        for(int i = 0; i < nmonpoly; i++)
        {
            boolean processed = false;

            int vcount = 1;
            int vfirst = monotoneChain[monotonePosition[i]].vnum;

            Point2d ymax = vertexChain[vfirst].point;
            Point2d ymin = vertexChain[vfirst].point;

            int posmax = monotonePosition[i];
            int posmin = monotonePosition[i];
            monotoneChain[monotonePosition[i]].marked = true;
            int p = monotoneChain[monotonePosition[i]].next;
            int v;

            while((v = monotoneChain[p].vnum) != vfirst)
            {
                if(monotoneChain[p].marked)
                {
                    processed = true;
                    break;             // break from inner while
                }
                else
                    monotoneChain[p].marked = true;

                if(greaterThan(vertexChain[v].point, ymax))
                {
                    ymax = vertexChain[v].point;
                    posmax = p;
                }

                if(lessThan(vertexChain[v].point, ymin))
                {
                    ymin = vertexChain[v].point;
                    posmin = p;
                }

                p = monotoneChain[p].next;
                vcount++;
            }

            if(processed)
                continue;

            // already a triangle?
            if(vcount == 3)
            {
                outputTriangles[op_idx] = monotoneChain[p].vnum;
                outputTriangles[op_idx + 1] = monotoneChain[monotoneChain[p].next].vnum;
                outputTriangles[op_idx + 2] = monotoneChain[monotoneChain[p].prev].vnum;
                op_idx += 3;
            }
            else
            {
                // triangulate the polygon
                v = monotoneChain[monotoneChain[posmax].next].vnum;
                if(equalsEpsilon(vertexChain[v].point, ymin))
                {
                    // LHS is a single line
                    op_idx = triangulateSinglePolygon(nvert,
                                                      posmax,
                                                      false,
                                                      outputTriangles,
                                                      op_idx);
                }
                else
                    op_idx = triangulateSinglePolygon(nvert,
                                                      posmax,
                                                      true,
                                                      outputTriangles,
                                                      op_idx);
            }
        }
    }

    /**
     * A greedy corner-cutting algorithm to triangulate a y-monotone
     * polygon in O(n) time. Joseph O-Rourke, Computational Geometry in C.
     */
    private int triangulateSinglePolygon(int nvert,
                                         int posmax,
                                         boolean rightSide,
                                         int[] outputTriangles,
                                         int outputIndex)
    {
        int output_index = outputIndex;

        int v;
        int ri = 0;      // reflex chain
        int endv, vpos;

        if(rightSide)          // RHS segment is a single segment
        {
            reflexChain[0] = monotoneChain[posmax].vnum;
            reflexChain[1] = monotoneChain[monotoneChain[posmax].next].vnum;
            ri = 1;

            vpos = monotoneChain[monotoneChain[posmax].next].next;
            v = monotoneChain[vpos].vnum;

            if((endv = monotoneChain[monotoneChain[posmax].prev].vnum) == 0)
                endv = nvert;
        }
        else                          // LHS is a single segment
        {
          int tmp = monotoneChain[posmax].next;
          reflexChain[0] = monotoneChain[tmp].vnum;
          tmp = monotoneChain[tmp].next;
          reflexChain[1] = monotoneChain[tmp].vnum;
          ri = 1;

          vpos = monotoneChain[tmp].next;
          v = monotoneChain[vpos].vnum;

          endv = monotoneChain[posmax].vnum;
        }

        while((v != endv) || (ri > 1))
        {
            if(ri > 0)
            {
                // reflex chain is non-empty
                if(tripleCross(vertexChain[v].point,
                               vertexChain[reflexChain[ri - 1]].point,
                               vertexChain[reflexChain[ri]].point) > 0)
                {
                    // convex corner: cut if off
                    outputTriangles[output_index] = reflexChain[ri - 1];
                    outputTriangles[output_index + 1] = reflexChain[ri];
                    outputTriangles[output_index + 2] = v;
                    output_index += 3;
                    ri--;
                }
                else          /*  */
                {
                    // non-convex, add v to the chain
                    ri++;
                    reflexChain[ri] = v;
                    vpos = monotoneChain[vpos].next;
                    v = monotoneChain[vpos].vnum;
                }
            }
            else
            {
                // reflex-chain empty: add v to the reflex chain and advance it
                reflexChain[++ri] = v;
                vpos = monotoneChain[vpos].next;
                v = monotoneChain[vpos].vnum;
            }
        }

        // reached the bottom vertex. Add in the triangle formed
        outputTriangles[output_index] = reflexChain[ri - 1];
        outputTriangles[output_index + 1] = reflexChain[ri];
        outputTriangles[output_index + 2] = v;
        output_index += 3;
        ri--;

        return output_index;
    }

    /**
     * v0 and v1 are specified in anti-clockwise order with respect to
     * the current monotone polygon mcur. Split the current polygon into
     * two polygons using the diagonal (v0, v1)
     */
    private int makeNewMonotonePoly(int mcur, int v0, int v1)
    {
        int mnew = newMonotone();

        SeidelVertexChain vp0 = vertexChain[v0];
        SeidelVertexChain vp1 = vertexChain[v1];

        int ip = getVertexPosition(v0, v1);
        int iq = getVertexPosition(v1, v0);

        int p = vp0.vertexPosition[ip];
        int q = vp1.vertexPosition[iq];

        // At this stage, we have got the positions of v0 and v1 in the
        // desired chain. Now modify the linked lists

        int i = newChainElement(); // for the new list
        int j = newChainElement();

        monotoneChain[i].vnum = v0;
        monotoneChain[j].vnum = v1;

        monotoneChain[i].next = monotoneChain[p].next;
        monotoneChain[monotoneChain[p].next].prev = i;
        monotoneChain[i].prev = j;
        monotoneChain[j].next = i;
        monotoneChain[j].prev = monotoneChain[q].prev;
        monotoneChain[monotoneChain[q].prev].next = j;

        monotoneChain[p].next = q;
        monotoneChain[q].prev = p;

        int nf0 = vp0.nextFree;
        int nf1 = vp1.nextFree;

        vp0.nextVertex[ip] = v1;

        vp0.vertexPosition[nf0] = i;
        vp0.nextVertex[nf0] = monotoneChain[monotoneChain[i].next].vnum;
        vp1.vertexPosition[nf1] = j;
        vp1.nextVertex[nf1] = v0;

        vp0.nextFree++;
        vp1.nextFree++;

        monotonePosition[mcur] = p;
        monotonePosition[mnew] = i;

        return mnew;
    }

    /**
     * (v0, v1) is the new diagonal to be added to the polygon. Find which
     * chain to use and return the positions of v0 and v1 in p and q
     */
    private int getVertexPosition(int v0, int v1)
    {
        SeidelVertexChain vp0 = vertexChain[v0];
        SeidelVertexChain vp1 = vertexChain[v1];

        // p is identified as follows. Scan from (v0, v1) rightwards till
        // you hit the first segment starting from v0. That chain is the
        // chain of our interest

        double angle = -4.0;
        int ret_val = 0;

        for(int i = 0; i < 4; i++)
        {
            if(vp0.nextVertex[i] <= 0)
                continue;

            double temp = getAngle(vp0.point,
                                   vertexChain[vp0.nextVertex[i]].point,
                                   vp1.point);

            if(temp > angle)
            {
                angle = temp;
                ret_val = i;
            }
        }

        return ret_val;
    }

    // Generic stuff

    /**
     * Return a new node to be added into the query tree.
     *
     * @return The index of the node to use
     */
    private int newNode()
    {
        if(lastQuery < queryList.length)
            return lastQuery++;
        else
        {
            System.err.println("newnode: Query-table overflow\n");
            return -1;
        }
    }

    /**
     * Fetch and initialize the next free trapezoid.
     *
     * @return The index of the trapezoid to use
     */
    private int newTrapezoid()
    {
        if(lastTrapezoid < trapezoidList.length)
        {
            trapezoidList[lastTrapezoid].leftSegment = -1;
            trapezoidList[lastTrapezoid].rightSegment = -1;
            trapezoidList[lastTrapezoid].valid = true;
            return lastTrapezoid++;
        }
        else
        {
            System.err.println("newtrap: Trapezoid-table overflow\n");
            return -1;
        }
    }

    /**
     * Return a new mon structure from the table
     */
    private int newMonotone()
    {
      return ++lastMonotone;
    }

    /**
     * Return a new chain element from the table
     */
    private int newChainElement()
    {
      return ++lastVertex;
    }

    /**
     * Return the next segment in the generated random ordering of all the
     * segments in S
     *
     * @return the next segment index
     */
    private int chooseSegment()
    {
        return permute[choiceIndex++];
    }

    /**
     * Check to see if the trapezoid lies inside the polygon.
     *
     * @param t The trapezoid to test against the polygon
     * @return true if the trapezoid lies completely inside the polygon
     */
    private boolean insidePolygon(SeidelTrapezoid t)
    {
        if((!t.valid) || ((t.leftSegment <= 0) || (t.rightSegment <= 0)))
            return false;

        if(((t.u0 <= 0) && (t.u1 <= 0)) ||
           ((t.d0 <= 0) && (t.d1 <= 0)))
        {
            int rseg = t.rightSegment;
            return greaterThan(segments[rseg].v1, segments[rseg].v0);
        }

        return false;
    }

    /**
     * Calculate the angle between two vectors that share a common vertex
     *
     * @param vp0 The vertex of the first line
     * @param vpnext The shared vertex for the two lines
     * @param vp1 The vertex of the second line
     * @return The angle between them
     */
    private double getAngle(Point2d vp0, Point2d  vpnext, Point2d vp1)
    {
        double v0_x = vpnext.x - vp0.x;
        double v0_y = vpnext.y - vp0.y;

        double v1_x = vp1.x - vp0.x;
        double v1_y = vp1.y - vp0.y;

        double l0 = Math.sqrt(v0_x * v0_x + v0_y * v0_y);
        double l1 = Math.sqrt(v1_x * v1_x + v1_y * v1_y);

        if((v0_x * v1_y - v1_x * v0_y) >= 0)  // sine is positive
            return (v0_x * v1_x + v1_y * v0_y) / (l0 * l1);
        else
            return (-1 * (v0_x * v1_x + v1_y * v0_y) / l0 / l1 - 2);
    }

    /**
     * Return the maximum of the two points into the yval structure
     */
    private int max(Point2d yval, Point2d v0, Point2d v1)
    {
        if(v0.y > v1.y + EPSILON)
            yval.set(v0);
        else if(equalsEpsilon(v0.y, v1.y))
        {
            if(v0.x > v1.x + EPSILON)
                yval.set(v0);
            else
                yval.set(v1);
        }
        else
            yval.set(v1);

        return 0;
    }


    /* Return the minimum of the two points into the yval structure */
    private int min(Point2d yval, Point2d v0, Point2d v1)
    {
        if(v0.y < v1.y - EPSILON)
            yval.set(v0);
        else if(equalsEpsilon(v0.y, v1.y))
        {
            if(v0.x < v1.x)
                yval.set(v0);
            else
                yval.set(v1);
        }
        else
            yval.set(v1);

        return 0;
    }

    /**
     * Check whether the one point is greater than the other, based on their
     * coordinate locations per axis.
     *
     * @param v0 The first point to check
     * @param v1 The destination point to check
     * @return true if v0 is gt v1
     */
    private boolean greaterThan(Point2d v0, Point2d v1)
    {
        if(v0.y > v1.y + EPSILON)
            return true;
        else if(v0.y < v1.y - EPSILON)
            return false;
        else
            return v0.x > v1.x;
    }

    /**
     * Check whether the one point is less than the other, based on their
     * coordinate locations per axis.
     *
     * @param v0 The first point to check
     * @param v1 The destination point to check
     * @return true if v0 is gt v1
     */
    private boolean lessThan(Point2d v0, Point2d v1)
    {
        if(v0.y < v1.y - EPSILON)
            return true;
        else if(v0.y > v1.y + EPSILON)
            return false;
        else
            return v0.x < v1.x;
    }

    /**
     * Check whether the one point is greater than or equal to the other, based
     * on their coordinate locations per axis.
     *
     * @param v0 The first point to check
     * @param v1 The destination point to check
     * @return true if v0 is gteq  v1
     */
    private boolean greaterThanOrEqualTo(Point2d v0, Point2d v1)
    {
        if(v0.y > v1.y + EPSILON)
            return true;
        else if(v0.y < v1.y - EPSILON)
            return false;
        else
            return v0.x >= v1.x;
    }

    /**
     * Retun TRUE if the vertex v is to the left of line segment no number
     * segnum. Takes care of the degenerate cases when both the vertices
     * have the same y--cood, etc.
     */
    private boolean isLeftOf(int segnum, Point2d v)
    {
        SeidelSegment s = segments[segnum];
        double area;

        if(greaterThan(s.v1, s.v0)) /* seg. going upwards */
        {
            if(equalsEpsilon(s.v1.y, v.y))
            {
                if(v.x < s.v1.x)
                    area = 1;
                else
                    area = -1;
            }
            else if(equalsEpsilon(s.v0.y, v.y))
            {
                if (v.x < s.v0.x)
                    area = 1;
                else
                    area = -1;
            }
            else
                area = tripleCross(s.v0, s.v1, v);
        }
        else
        {
            // v0 > v1
            if(equalsEpsilon(s.v1.y, v.y))
            {
                if (v.x < s.v1.x)
                    area = 1;
                else
                    area = -1;
            }
            else if(equalsEpsilon(s.v0.y, v.y))
            {
                if (v.x < s.v0.x)
                    area = 1;
                else
                    area = -1;
            }
            else
                area = tripleCross(s.v1, s.v0, v);
        }

        return area > 0;
    }

    /**
     * Two-dimensional triple cross product resulting in a scalar output.
     *
     * @param v0 The first point to use
     * @param v1 The second point to use
     * @param v2 The third point to use
     * @return The result of the cross product
     */
    private double tripleCross(Point2d v0, Point2d v1, Point2d v2)
    {
        return (v1.x - v0.x) * (v2.y - v0.y) - (v1.y - v0.y) * (v2.x - v0.x);
    }

    /**
     * Returns true if the corresponding endpoint of the given segment is
     * already inserted into the segment tree. Use the simple test of
     * whether the segment which shares this endpoint is already inserted
     *
     * @param segnum The index of the segment to check
     * @param first Should it check for first (true) or last (false)
     * @return true if the given segment has been inserted
     */
    private boolean inserted(int segnum, boolean first)
    {
        if(first)
            return segments[segments[segnum].prev].isInserted;
        else
            return segments[segments[segnum].next].isInserted;
    }

   /**
     * Calculate  log*n for given n
     *
     */
    private int logStarN(int n)
    {
        int i;
        double v = n;

        for(i = 0; v >= 1; i++)
          v = Math.log(v) * LOG_2_BASE;  // v = log2(v) equivalent;

        return (int)(i - 1);
    }

    /**
     * No idea what this is really calculating.
     */
    private int nRatio(int n, int h)
    {
      double v = n;

      for(int i = 0; i < h; i++)
        v = Math.log(v) * LOG_2_BASE;   // v = log2(v) equivalent

      return (int) Math.ceil(n / v);
    }

    /**
     * Floating point comparison for almost equal values within the epsilon
     * delta value.
     */
    private boolean equalsEpsilon(double a, double b)
    {
        return Math.abs(a - b) <= EPSILON;
    }

    /**
     * Floating point comparison for almost equal values within the epsilon
     * delta value.
     */
    private boolean equalsEpsilon(Point2d a, Point2d b)
    {
        double diff = a.x - b.x;

        if(Double.isNaN(diff))
            return false;

        if((diff < 0 ? -diff : diff) > EPSILON)
            return false;

        diff = a.y - b.y;

        if(Double.isNaN(diff))
            return false;

        return !((diff < 0 ? -diff : diff) > EPSILON);
    }
}
