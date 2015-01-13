/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// External imports
// None

// Local imports
import org.j3d.util.IntHashMap;
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;

/**
 * Base geometry generator defintion for all forms of non-adaptive
 * subdivision-based patches.
 * <P>
 *
 * The subdivision information at a given level is described by the uniform
 * subdivision handling paper.
 * <p>
 *
 * Because of the structure of this code, it is not suited for generating
 * adaptive subdivision surfaces. They should use a separate class.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CatmullClarkGenerator extends PolygonSubdivisionGenerator
{
    /** Mapping of vertex IDs to the sector object */
    private IntHashMap vertexToSectorMap;

    /** Listing of the rules for the concave quad generation */
    private QuadRule[] concaveRuleTable;

    /** Listing of the rules for the convex quad generation */
    private QuadRule[] convexRuleTable;

    /** Listing of the rules for the interior quad generation */
    private QuadRule[] interiorRuleTable;

    /** Listing of the rules for the crease quad generation */
    private QuadRule[] creaseRuleTable;

    /** The ring around the currently being processed vertex */
    private FaceRing ring;

    /** temporary variables for working with stuff */
    private float[] wkVec0;
    private float[] wkVec1;
    private float[] wkVec2;
    private float[] wkVec3;
    private float[] wkVec4;
    private float[] wkVec5;
    private float[] wkVec6;
    private float[] wkVec7;
    private float[] wkVec8;
    private float[] wkVec9;

    /**
     * Construct a new generator with no control mesh set. The levelMult field
     * describes the multiplier of how many vertices are created at each level
     * from the previous one. For triangle-based schemes, this value is normally
     * 3.
     *
     *
     */
    public CatmullClarkGenerator()
    {
        super(4, false);

        wkVec0 = new float[3];
        wkVec1 = new float[3];
        wkVec2 = new float[3];
        wkVec3 = new float[3];
        wkVec4 = new float[3];
        wkVec5 = new float[3];
        wkVec6 = new float[3];
        wkVec7 = new float[3];
        wkVec8 = new float[3];
        wkVec9 = new float[3];
    }

    /**
     * Regenerate the patch coordinate points in accordance with the derived
     * classes algorithm type.
     */
    @Override
    protected void regenerateSubdivision()
    {
        if(levelChanged)
        {
            concaveRuleTable = new QuadRule[totalSubdivisions];
            convexRuleTable = new QuadRule[totalSubdivisions];
            interiorRuleTable = new QuadRule[totalSubdivisions];
            creaseRuleTable = new QuadRule[totalSubdivisions];
        }

// Must fix this with something that determines the maximum number of
// edges to a face dynamically.
        ring = new FaceRing(6);

        // Iterate through all of the top level faces and subdivide them
        int num_faces = faces.size();

        for(int i = 0; i < totalSubdivisions; i++)
        {
            for(int j = 0; j < num_faces; j++)
            {
                Face f = (Face)faces.get(i);
                subdivide(f, i);
            }
        }
    }

    /**
     * Subdivide the face at the given level.
     *
     * @param f The face to be subdivided
     * @param d The depth of the current subdivision
     */
    private void subdivide(Face f, int d)
    {
        computeCenterPoint(f, d);

        int i;
        int num_vtx = f.numVertex + 1;

        for(i = 1; i < num_vtx; i++)
            computeVertexPoint(f, i, d);

        for(i = 1; i < num_vtx; i++)
            computeEdgePoint(f, i, d);

        //for(e = 1; e < noVtx()+1; ++e)
        //    computeEdgeNormal(e, d);

        //  computeFaceNormal(d);

        // clear children mid points points
        if(f.children != null)
        {
            for(i = 0; i < f.children.length; i++)
                f.children[i].clearFace(d + 1);
        }
    }

    /**
     * Compute the center point of the given object. Assumes that the face has
     * at least 3 vertices.
     */
    private void computeCenterPoint(Face f, int depth)
    {
        if(f.centerVertex() != null)
            return;

        float x = 0;
        float y = 0;
        float z = 0;

        // some loop unrolling here for performance. After the first pass
        // through, this should always hit the 4 vertex case.
        switch(f.numVertex)
        {
            case 3:
                float[] v = f.vertices[0].getPos(depth);
                x = v[0];
                y = v[1];
                z = v[2];

                v = f.vertices[1].getPos(depth);
                x += v[0];
                y += v[1];
                z += v[2];

                v = f.vertices[2].getPos(depth);
                x += v[0];
                y += v[1];
                z += v[2];
                break;

            case 4:
                v = f.vertices[0].getPos(depth);
                x = v[0];
                y = v[1];
                z = v[2];

                v = f.vertices[1].getPos(depth);
                x += v[0];
                y += v[1];
                z += v[2];

                v = f.vertices[2].getPos(depth);
                x += v[0];
                y += v[1];
                z += v[2];

                v = f.vertices[3].getPos(depth);
                x += v[0];
                y += v[1];
                z += v[2];
                break;

            default:
                for(int i = 0; i < f.numVertex; i++)
                {
                    v = f.vertices[i].getPos(depth);
                    x += v[0];
                    y += v[1];
                    z += v[2];
                }
        }

        float inv_num = 1 / f.numVertex;

        x *= inv_num;
        y *= inv_num;
        z *= inv_num;

        // Modify the vertex value based on the requirements for the
        // edge conditions. If any edge of this quad has a non-standard
        // flatness or normal value then use that to contribute to the
        // final position of the vertex.
        int num_relevant = 0;

        wkVec8[0] = 0;
        wkVec8[1] = 0;
        wkVec8[2] = 0;

        for(int i = 0; i < f.numVertex; i++)
        {
            Vertex vtx = f.headVertex(i);

            if(vtx.isSpecial)
                continue;

            Sector s = f.sectorInfo(f.headVertexIndex(i));
            if(s.normal == null && s.flatness == -1)
                continue;

            QuadRule rule = getRule(f, depth, s.theta);

            float x1 = rule.x1.face[ring.startEdge];
            float x2 = rule.x2.face[ring.startEdge];

            wkVec5[0] = x;
            wkVec5[1] = y;
            wkVec5[2] = z;

            if(s.flatness != -1)
            {
                modifyFlatness(rule, wkVec5, x1, x2, depth, wkVec7);

                wkVec5[0] = wkVec7[0];
                wkVec5[1] = wkVec7[1];
                wkVec5[2] = wkVec7[2];
            }

            if(s.normal != null)
            {
                if(ring.isClosed)
                    modifyClosedNormal(rule,
                                       wkVec5,
                                       rule.x1.center,
                                       rule.x2.center,
                                       depth,
                                       wkVec6);
                else
                    modifyOpenNormal(rule,
                                     wkVec5,
                                     rule.x1.center,
                                     rule.x2.center,
                                     depth,
                                     wkVec6);
            }

            num_relevant++;
            wkVec8[0] += wkVec5[0];
            wkVec8[1] += wkVec5[1];
            wkVec8[2] += wkVec5[2];
        }

        if(num_relevant != 0)
        {
            float i_nr = 1 / (float)num_relevant;
            x = i_nr * wkVec8[0];
            y = i_nr * wkVec8[1];
            z = i_nr * wkVec8[2];
        }

        // set center pos
        f.setCenterPosition(depth, x, y, z);
    }

    private void computeVertexPoint(Face f, int e, int d)
    {
        if(f.children == null)
            f.makeChildren(d);

        Vertex vtx;
        float[] pos;

        Vertex h_vtx = f.headVertex(e);

        if(h_vtx.currentDepth <= d + 1)
        {
            pos = h_vtx.getPos(d + 1);
            wkVec6[0] = pos[0];
            wkVec6[1] = pos[1];
            wkVec6[2] = pos[2];
        }
        else
        {
            if(h_vtx.isSpecial)
            {
                int vtx_id = f.headVertexIndex(e);
                ring.rebuild(f, vtx_id, d);
                Sector s = f.sectorInfo(vtx_id);
                QuadRule rule = getRule(f, vtx_id, s.theta);

                computeRingVertexPoint(rule, d, wkVec6);
                h_vtx.setPosition(d, wkVec6);
            }
            else
            {
                ring.rebuildFromRing(f, e);

                wkVec7[0] = 0;
                wkVec7[1] = 0;
                wkVec7[2] = 0;

                wkVec8[0] = 0;
                wkVec8[1] = 0;
                wkVec8[2] = 0;

                int num_vtx = ring.numVertex();
                for(int i = 0;  i < num_vtx; i++)
                {
                    int e1 = ring.getFaceIndex(i);
                    Face f1 = ring.getFace(i);

                    vtx = f.headVertex(e1);
                    pos =  vtx.getPos(d);

                    wkVec7[0] += pos[0];
                    wkVec7[1] += pos[1];
                    wkVec7[2] += pos[2];

                    vtx = f.headVertex(f.nextEdgeIndex(e1));
                    pos =  vtx.getPos(d);

                    wkVec8[0] += pos[0];
                    wkVec8[1] += pos[1];
                    wkVec8[2] += pos[2];
                }

                float k = ring.numVertex();

                float beta1 = 1.5f / k;
                float beta2 = 0.25f / k;
                float b1_b2 = 1 - beta1 - beta2;

                pos = h_vtx.getPos(d);

                wkVec6[0] = (b1_b2) * pos[0] + beta1 / k * wkVec7[0] +
                            beta2 / k * wkVec8[0];
                wkVec6[1] = (b1_b2) * pos[1] + beta1 / k * wkVec7[1] +
                            beta2 / k * wkVec8[1];
                wkVec6[2] = (b1_b2) * pos[2] + beta1 / k * wkVec7[2] +
                            beta2 / k * wkVec8[2];
            }

            h_vtx.setPosition(d + 1, wkVec6);
        }
    }

    private void computeRingVertexPoint(QuadRule rule, int d, float[] res)
    {
        int e = ring.getCenterFaceIndex();
        Face t = ring.getCenterFace();

        applyCoef(rule.sub, d, res);
        Sector s = t.sectorInfo(t.headVertexIndex(e));

        if((s != null) && (s.flatness != 0))
        {
            modifyFlatness(rule, res, rule.x1.center, rule.x2.center, d, wkVec5);
            res[0] = wkVec5[0];
            res[1] = wkVec5[1];
            res[2] = wkVec5[2];
        }

        if((s != null) && (s.normal != null))
        {
            if(ring.isClosed)
                modifyClosedNormal(rule,
                                   res,
                                   rule.x1.center,
                                   rule.x2.center,
                                   d,
                                   wkVec5);
            else
                modifyOpenNormal(rule,
                                 res,
                                 rule.x1.center,
                                 rule.x2.center,
                                 d,
                                 wkVec5);

            res[0] = wkVec5[0];
            res[1] = wkVec5[1];
            res[2] = wkVec5[2];
        }
    }

    private void computeEdgePoint(Face f, int e, int d)
    {
        if(f.children == null)
            f.makeChildren(d);

        if(f.hasMidPosition(e, d))
            return;

        wkVec9[0] = 0;
        wkVec9[1] = 0;
        wkVec9[2] = 0;

        int vtx_id = f.headVertexIndex(e);
        Sector s = f.sectorInfo(vtx_id);

        if(f.headVertex(e).isSpecial || f.tailVertex(e).isSpecial)
        {

            if(!isRelevantToMidpoint(f, -e))
            {
                ring.rebuild(f, e, d);
                computeRingEdgePoint(d, wkVec8);
            }
            else if(!isRelevantToMidpoint(f, e))
            {
                ring.rebuild(f, -e, d);
                computeRingEdgePoint(d, wkVec8);
            }
            else
            {
                ring.rebuild(f, e, d);
                computeRingEdgePoint(d, wkVec8);

                ring.rebuild(f, -e, d);
                computeRingEdgePoint(d, wkVec9);
                wkVec8[0] = 0.5f * wkVec8[0] + 0.5f * wkVec9[0];
                wkVec8[1] = 0.5f * wkVec8[1] + 0.5f * wkVec9[0];
                wkVec8[2] = 0.5f * wkVec8[2] + 0.5f * wkVec9[0];
            }
        }
        else
        {
            int ne = f.neighborIndex(e);
            Face nf = f.neighbor(e);

            Vertex vtx = f.headVertex(e);
            float[] head = vtx.getPos(d);

            vtx = f.tailVertex(e);
            float[] tail = vtx.getPos(d);

            computeFaceCenter(f, d, wkVec8);
            computeFaceCenter(nf, d, wkVec9);

            wkVec8[0] = 0.25f * (head[0] + tail[0] + wkVec8[0] + wkVec9[0]);
            wkVec8[0] = 0.25f * (head[0] + tail[0] + wkVec8[0] + wkVec9[0]);
            wkVec8[0] = 0.25f * (head[0] + tail[0] + wkVec8[0] + wkVec9[0]);
        }

        f.setMidPosition(e, d, wkVec8);
    }

    /**
     * Compute the center point of the given face.
     */
    private void computeFaceCenter(Face f, int d, float[] res)
    {
        if(f.children == null)
            f.makeChildren(d);

        Vertex vtx = f.centerVertex();

        if(vtx.currentDepth <= d + 1)
        {
            float[] p = vtx.getPos(d);
            res[0] = p[0];
            res[1] = p[1];
            res[2] = p[2];
        }
        else
        {
            // compute average...
            float x = 0;
            float y = 0;
            float z = 0;

            for(int v = 0; v < f.numVertex; v++)
            {
                float[] p = f.vertices[0].getPos(d);
                x += p[0];
                y += p[1];
                z += p[2];
            }

            float av = 1.0f / f.numVertex;

            x *= av;
            y *= av;
            z *= av;

            // modify average...
            int num_relevant = 0;
            wkVec7[0] = 0;
            wkVec7[1] = 0;
            wkVec7[2] = 0;

            for(int e = 1; e < f.numVertex + 1; e++)
            {
                if(f.headVertex(e).isSpecial)
                {
                    int h_vtx = f.headVertexIndex(e);
                    Sector s = f.sectorInfo(h_vtx);

                    if((s != null) && (s.normal != null || s.flatness != 0))
                    {
                        ring.rebuild(f, e, d);
                        QuadRule rule = getRule(f, h_vtx, s.theta);

                        float x1 = rule.x1.face[ring.startEdge];
                        float x2 = rule.x2.face[ring.startEdge];

                        wkVec5[0] = x;
                        wkVec5[1] = y;
                        wkVec5[2] = z;

                        if(s.flatness != 0)
                        {
                            modifyFlatness(rule, wkVec5, x1, x2, d, wkVec6);
                            wkVec5[0] = wkVec6[0];
                            wkVec5[1] = wkVec6[1];
                            wkVec5[2] = wkVec6[2];
                        }

                        if(s.normal != null)
                        {
                            if(ring.isClosed)
                            {
                                modifyClosedNormal(rule, wkVec5, x1, x2, d, wkVec6);
                                wkVec5[0] = wkVec6[0];
                                wkVec5[1] = wkVec6[1];
                                wkVec5[2] = wkVec6[2];
                            }
                            else
                            {
                                modifyOpenNormal(rule, wkVec5, x1, x2, d, wkVec6);
                                wkVec5[0] = wkVec6[0];
                                wkVec5[1] = wkVec6[1];
                                wkVec5[2] = wkVec6[2];
                            }
                        }

                        num_relevant++;
                        wkVec7[0] += wkVec5[0];
                        wkVec7[1] += wkVec5[1];
                        wkVec7[2] += wkVec5[2];
                    }
                }
            }

            if(num_relevant != 0)
            {
                float i_nr = 1 / (float)num_relevant;
                x = i_nr * wkVec7[0];
                y = i_nr * wkVec7[1];
                z = i_nr * wkVec7[2];
            }

            // set center pos
            f.setCenterPosition(d, x, y, z);
            res[0] = x;
            res[1] = y;
            res[2] = z;
        }
    }

    private void computeRingEdgePoint(int d, float[] res)
    {
        int e = ring.getCenterFaceIndex();
        Face t = ring.getCenterFace();

        int hv = t.headVertexIndex(e);
        Sector s = t.sectorInfo(hv);

        QuadRule rule = getRule(t, hv, s.theta);

        if(t.edgeTag(e) == SubdivisionTypes.CREASE_EDGE)
            applyEdgeCoef(rule.creaseSub, d, res);
        else
            applyEdgeCoef(rule.edgeSub, d, res);

        float x1 = rule.x1.edge[ring.startEdge];
        float x2 = rule.x2.edge[ring.startEdge];

        Sector si = t.sectorInfo(t.headVertexIndex(e));
        if((si != null) && (si.flatness != 0))
        {
            modifyFlatness(rule, res, x1, x2, d, wkVec5);
            res[0] = wkVec5[0];
            res[1] = wkVec5[1];
            res[2] = wkVec5[2];
        }

        si = t.sectorInfo(t.headVertexIndex(e));
        if((si != null) && (si.normal != null))
        {
            if(ring.isClosed)
                modifyClosedNormal(rule, res, x1, x2, d, wkVec5);
            else
                modifyOpenNormal(rule, res, x1, x2, d, wkVec5);

            res[0] = wkVec5[0];
            res[1] = wkVec5[1];
            res[2] = wkVec5[2];
        }
    }

    /**
     * Check to see if the edge e is relevant to the face for updates.
     *
     * @param f The face to check against
     * @param e The index of the vertex in the list
     */
    private boolean isRelevantToMidpoint(Face f, int e)
    {
        int vn = f.headVertexIndex(e);

        if(!f.vertices[vn].isSpecial)
            return false;

        Sector s = f.sectorInfo(vn);

        if((s != null) && (s.normal != null) || (s.flatness != 0))
            return true;

        int vt = f.vertexTag(vn);
        int et = f.edgeTag(e);
        if((et == SubdivisionTypes.UNTAGGED_EDGE) &&
           (vt == SubdivisionTypes.SMOOTH_VERTEX))
            return false;

        if((et == SubdivisionTypes.CREASE_EDGE) &&
           ((vt == SubdivisionTypes.CREASE_VERTEX) ||
            (vt == SubdivisionTypes.SMOOTH_VERTEX)))
            return false;

        return true;
    }

    /**
     * Get the quad rule from the table for the given k. If one has not been
     * allocated, create it, including the theta value.
     *
     * @param k The level of subdivision in use
     * @param theta The theta used in the coefficient
     */
    private QuadRule getRule(Face f, int vertex, float theta)
    {
        QuadRule ret_val = null;

        int k = ring.numFace;

        switch(f.vertexTag(vertex))
        {
            case SubdivisionTypes.SMOOTH_VERTEX:
                if(interiorRuleTable[k] == null)
                    interiorRuleTable[k] = new InteriorQuadRule(k, theta);

                ret_val = interiorRuleTable[k];
                break;

            case SubdivisionTypes.CREASE_VERTEX:
                if(creaseRuleTable[k] == null)
                    creaseRuleTable[k] = new CreaseQuadRule(k, theta);

                ret_val = creaseRuleTable[k];
                break;

            case SubdivisionTypes.CORNER_VERTEX:
                switch(f.sectorTag(vertex))
                {
                    case Sector.CONVEX_SECTOR:
                        if((convexRuleTable[k] == null) ||
                           (convexRuleTable[k].theta != theta))
                            convexRuleTable[k] = new ConvexQuadRule(k, theta);

                        ret_val = convexRuleTable[k];
                        break;

                    case Sector.CONCAVE_SECTOR:
                        if((concaveRuleTable[k] == null) ||
                           (concaveRuleTable[k].theta != theta))
                            concaveRuleTable[k] = new ConcaveQuadRule(k, theta);

                        ret_val = concaveRuleTable[k];
                        break;

                    default:
                        System.out.println("CC.getRule.invalid");

                }
        }

        return ret_val;
    }

    /**
     * Modify the flatness of a point and return it in the provided point
     *
     * @param d The subdivision depth we're working with.
     */
    private void modifyFlatness(QuadRule rule,
                                float[] p,
                                float x1,
                                float x2,
                                int d,
                                float[] res)
    {
        int e = ring.getCenterFaceIndex();
        Face cf = ring.getCenterFace();
        int c_vtx = cf.headVertexIndex(e);

        res[0] = p[0];
        res[1] = p[1];
        res[2] = p[2];

        float[] a0 = wkVec1;
        float[] a1 = wkVec2;
        float[] a2 = wkVec3;

        applyCoef(rule.l0, d, a0);
        applyCoef(rule.l1, d, a1);
        applyCoef(rule.l2, d, a2);

        Sector si = cf.sectorInfo(c_vtx);
        float s = si.flatness;

        res[0] = (1 - s) * p[0] +
                  s * (a0[0] +
                       a1[0] * x1 * rule.lambda1 +
                       a2[0] * x2 * rule.lambda2);

        res[1] = (1 - s) * p[1] +
                  s * (a0[1] +
                       a1[1] * x1 * rule.lambda1 +
                       a2[1] * x2 * rule.lambda2);

        res[2] = (1 - s) * p[2] +
                  s * (a0[2] +
                       a1[2] * x1 * rule.lambda1 +
                       a2[2] * x2 * rule.lambda2);
    }

    private void modifyClosedNormal(QuadRule rule,
                                    float[] p,
                                    float x1,
                                    float x2,
                                    int depth,
                                    float[] res)
    {
        int fe = ring.getCenterFaceIndex();
        Face cf = ring.getCenterFace();
        int c_vtx = cf.headVertexIndex(fe);

        Sector si = cf.sectorInfo(c_vtx);

        float[] pn = si.normal;

        applyCoef(rule.l1, depth, wkVec1);
        applyCoef(rule.l2, depth, wkVec2);

        float a1_dot_pn =
            wkVec1[0] * pn[0] + wkVec1[1] * pn[1] + wkVec1[2] * pn[2];
        float a2_dot_pn =
            wkVec2[0] * pn[0] + wkVec2[1] * pn[1] + wkVec2[2] * pn[2];

        float fac = si.normalT * (rule.lambda1 * a1_dot_pn * x1 +
                                  rule.lambda2 * a2_dot_pn * x2);

        res[0] = p[0] - pn[0] * fac;
        res[1] = p[1] - pn[1] * fac;
        res[2] = p[2] - pn[2] * fac;
    }


    private void modifyOpenNormal(QuadRule rule,
                                  float[] p,
                                  float x1,
                                  float x2,
                                  int depth,
                                  float[] res)
    {
        // real tangents
        applyCoef(rule.l1, depth, wkVec2);
        applyCoef(rule.l2, depth, wkVec3);

        // center
        int e1 = ring.getCenterFaceIndex();
        Face t1 = ring.getCenterFace();
        int c_vtx = t1.headVertexIndex(e1);


        Sector si = t1.sectorInfo(c_vtx);
        float[] n1 = si.normal;

        // wkVec1 == a1, wkVec2 == a2
        float[] a1 = wkVec1;
        float[] a2 = wkVec2;

        applyCoef(rule.l1, depth, a1);
        applyCoef(rule.l2, depth, a2);

        // modified tangents
        float a1_dot_n1 = a1[0] * n1[0] + a1[1] * n1[1] + a1[2] * n1[2];
        float a2_dot_n1 = a2[0] * n1[0] + a2[1] * n1[1] + a2[2] * n1[2];

        // tan1
        wkVec3[0] = (a1[0] - a1_dot_n1 * n1[0]);
        wkVec3[1] = (a1[1] - a1_dot_n1 * n1[1]);
        wkVec3[2] = (a1[2] - a1_dot_n1 * n1[2]);

        // Normalise the vector.
        float d = wkVec3[0] * wkVec3[0] +
                  wkVec3[1] * wkVec3[1] +
                  wkVec3[2] * wkVec3[2];

        if(d != 0)
        {
            d = 1 / (float)Math.sqrt(d);
            wkVec3[0] *= d;
            wkVec3[1] *= d;
            wkVec3[2] *= d;
        }

        // tan2
        wkVec4[0] = (a2[0] - a2_dot_n1 * n1[0]);
        wkVec4[1] = (a2[1] - a2_dot_n1 * n1[1]);
        wkVec4[2] = (a2[2] - a2_dot_n1 * n1[2]);

        // Normalise the vector.
        d = wkVec4[0] * wkVec4[0] +
            wkVec4[1] * wkVec4[1] +
            wkVec4[2] * wkVec4[2];

        if(d != 0)
        {
            d = 1 / (float)Math.sqrt(d);
            wkVec4[0] *= d;
            wkVec4[1] *= d;
            wkVec4[2] *= d;
        }

        int k = ring.numFace;

        int e_first = ring.getFaceIndex(0);
        int e_last = ring.getFaceIndex(k - 1);
        Face t_first = ring.getFace(0);
        Face t_last = ring.getFace(k - 1);

        int e0 = t_first.neighborIndex(e_first);
        Face t0 = t_first.neighbor(e_first);

        int p_e2 = t_last.prevEdgeIndex(e_last);
        int e2 = t_last.neighborIndex(p_e2);
        Face t2 = t_last.neighbor(p_e2);

        boolean two_sector_corner =
           ((t1.vertexTag(t1.headVertexIndex(e1)) !=
             SubdivisionTypes.SMOOTH_VERTEX) &&
           (t0 != null) && (t2 != null) &&
           (t0.sectorInfo(t0.tailVertexIndex(e0)) ==
            t2.sectorInfo(t2.headVertexIndex(e2))));

        if(!two_sector_corner)
        {
            // startV
            if(t0 != null)
            {
                si = t0.sectorInfo(t0.tailVertexIndex(e0));
                float[] n0 = si.normal;

                // n0 x n1
                float x = n0[1] * n1[2] - n0[2] * n1[1];
                float y = n0[2] * n1[0] - n0[0] * n1[2];
                float z = n0[0] * n1[1] - n0[1] * n1[0];

                // error calc
                double l1 = Math.abs(x) + Math.abs(y) + Math.abs(z);

                if(l1 > 0.001)
                {
                    l1 = Math.sqrt(x * x + y * y + z * z);
                    if(l1 == 0)
                        l1 = 1;

                    switch(t1.vertexTag(t1.headVertexIndex(e1)))
                    {
                        case SubdivisionTypes.SMOOTH_VERTEX:
                            // Should never get to this one.
                            break;

                        case SubdivisionTypes.CREASE_VERTEX:
                            // this case only happens if the mesh is tagged
                            // incorrectly. Normalise the cross product to

                            wkVec3[0] /= l1;
                            wkVec3[1] /= l1;
                            wkVec3[2] /= l1;
                            break;

                        case SubdivisionTypes.CORNER_VERTEX:
                            Sector s1 = t1.sectorInfo(t1.headVertexIndex(e1));

                            if(s1.tag == Sector.CONCAVE_SECTOR)
                            {
                                wkVec3[0] /= l1;
                                wkVec3[1] /= l1;
                                wkVec3[2] /= l1;
                            }
                            else
                            {
                                wkVec4[0] /= l1;
                                wkVec4[1] /= l1;
                                wkVec4[2] /= l1;
                            }

                            break;
                    }
                }
            }

            if(t2 != null)
            {
                si = t2.sectorInfo(t2.headVertexIndex(e2));
                float[] n2 = si.normal;

                // n1 x n2
                float x = n1[1] * n2[2] - n1[2] * n2[1];
                float y = n1[2] * n2[0] - n1[0] * n2[2];
                float z = n1[0] * n2[1] - n1[1] * n2[0];

                double l1 = Math.abs(x) + Math.abs(y) + Math.abs(z);

                if(l1 > 0.001)
                {
                    l1 = Math.sqrt(x * x + y * y + z * z);
                    if(l1 == 0)
                        l1 = 1;

                    switch(t1.sectorTag(t1.headVertexIndex(e1)))
                    {
                        case Sector.UNTAGGED_SECTOR:
                            // should never get this
                            break;

                        case Sector.CONCAVE_SECTOR:
                            wkVec4[0] /= l1;
                            wkVec4[1] /= l1;
                            wkVec4[2] /= l1;
                            break;

                        case Sector.CONVEX_SECTOR:
                            wkVec3[0] /= l1;
                            wkVec3[1] /= l1;
                            wkVec3[2] /= l1;
                            break;

                    }
                }
            }
        }

        si = t1.sectorInfo(t1.headVertexIndex(e1));
        float nt = si.normalT;

        float a1_dot_tan1 =
            a1[0] * wkVec3[0] + a1[1] * wkVec3[1] + a1[2] * wkVec3[2];
        float a2_dot_tan2 =
            a2[0] * wkVec4[0] + a2[1] * wkVec4[1] + a2[2] * wkVec4[2];

        float l1 = x1 * a1_dot_tan1 * rule.lambda1;
        float l2 = x2 * a2_dot_tan2 * rule.lambda2;

        res[0] = p[0] + nt * (l1 * (wkVec3[0] - wkVec2[0]) +
                              l2 * (wkVec4[0] - wkVec3[0]));
        res[1] = p[1] + nt * (l1 * (wkVec3[1] - wkVec2[1]) +
                              l2 * (wkVec4[1] - wkVec3[1]));
        res[2] = p[2] + nt * (l1 * (wkVec3[2] - wkVec2[2]) +
                              l2 * (wkVec4[2] - wkVec3[2]));
    }

    /**
     * Apply the vertex cooefficients to a face.
     */
    private void applyCoef(QuadCoefficients coef, int d, float[] res)
    {
        Vertex vtx = ring.getCenterVertex();
        float[] p = vtx.getPos(d);

        res[0] = coef.center * p[0];
        res[1] = coef.center * p[1];
        res[2] = coef.center * p[2];

        for(int i = 0; i < ring.numVertex(); i++)
        {
            p = ring.vertex(i).getPos(d);

            res[0] += coef.edge[i] * p[0];
            res[1] += coef.edge[i] * p[1];
            res[2] += coef.edge[i] * p[2];
        }

        for(int i = 0; i < ring.numFace; i++)
        {
            Face f = ring.getFace(i);
            int edge = ring.getFaceEdge(i);

            vtx = f.headVertex(f.nextEdgeIndex(edge));
            float[] t = vtx.getPos(d);

            if(d == 0)
            {
                wkVec0[0] = 0;
                wkVec0[1] = 0;
                wkVec0[2] = 0;
                float i_num = 1 / (float)f.numVertex;
                float[] pos;

                for(int v = 0; v < f.numVertex; v++)
                {
                    pos = f.vertices[i].getPos(d);
                    wkVec0[0] += i_num * pos[0];
                    wkVec0[1] += i_num * pos[1];
                    wkVec0[2] += i_num * pos[2];
                }

                vtx = f.headVertex(edge);
                float[] f_hp = vtx.getPos(d);

                vtx = f.headVertex(f.nextEdgeIndex(edge));
                float[] f_hpe = vtx.getPos(d);

                vtx = f.tailVertex(edge);
                float[] f_tp = vtx.getPos(d);

                t[0] = 4 * wkVec0[0] - f_hp[0] - f_hpe[0] - f_tp[0];
                t[1] = 4 * wkVec0[1] - f_hp[1] - f_hpe[1] - f_tp[1];
                t[2] = 4 * wkVec0[2] - f_hp[2] - f_hpe[2] - f_tp[2];
            }

            res[0] += coef.face[i] * wkVec0[0];
            res[1] += coef.face[i] * wkVec0[1];
            res[2] += coef.face[i] * wkVec0[2];
        }
    }

    /**
     * Apply an edge coefficient to the value
     */
    private void applyEdgeCoef(float[] coef, int d, float[] res)
    {
        int ce = ring.edgeId;
        Face cf = ring.centerFace;

        int ne = cf.neighborIndex(ce);
        Face nf = cf.neighbor(ce);
        Vertex vtx;

        if(d == 0)
        {
            int i;
            float[][] pos = new float[6][];
            int num_vtx = cf.numVertex;
            float i_nv = 1 / (float)num_vtx;

            float[] a0 = wkVec0;
            float[] a1 = wkVec1;

            a0[0] = 0;
            a0[1] = 0;
            a0[2] = 0;

            a1[0] = 0;
            a1[1] = 0;
            a1[2] = 0;

            for(i = 0; i < num_vtx; i++)
            {
                float[] p = cf.vertices[i].getPos(d);
                a0[0] += i_nv * p[0];
                a0[1] += i_nv * p[1];
                a0[2] += i_nv * p[2];
            }

            vtx = cf.headVertex(ce);
            pos[0] = vtx.getPos(d);

            vtx = cf.headVertex(cf.nextEdgeIndex(ce));
            pos[1] = vtx.getPos(d);

            vtx = cf.tailVertex(ce);
            pos[3] = vtx.getPos(d);

            pos[2] = wkVec2;
            pos[2][0] = 4 * a0[0] - pos[0][0] - pos[1][0] - pos[3][0];
            pos[2][1] = 4 * a0[1] - pos[0][1] - pos[1][1] - pos[3][1];
            pos[2][2] = 4 * a0[2] - pos[0][2] - pos[1][2] - pos[3][2];

            if(nf != null)
            {
                num_vtx = nf.numVertex;
                i_nv = 1 / (float)num_vtx;

                for(i = 0; i < num_vtx; i++)
                {
                    float[] p = nf.vertices[i].getPos(d);
                    a1[0] += i_nv * p[0];
                    a1[1] += i_nv * p[1];
                    a1[2] += i_nv * p[2];
                }

                vtx = nf.headVertex(nf.nextEdgeIndex(ne));

                pos[5] = vtx.getPos(d);

                pos[4] = wkVec3;
                pos[4][0] = 4 * a1[0] - pos[0][0] - pos[3][0] - pos[5][0];
                pos[4][1] = 4 * a1[1] - pos[0][1] - pos[3][1] - pos[5][1];
                pos[4][2] = 4 * a1[2] - pos[0][2] - pos[3][2] - pos[5][2];
            }

            res[0] = coef[0] * pos[0][0] + coef[1] * pos[1][0] +
                     coef[2] * pos[2][0] + coef[3] * pos[3][0] +
                     coef[4] * pos[4][0] + coef[5] * pos[5][0];

            res[1] = coef[0] * pos[0][1] + coef[1] * pos[1][1] +
                     coef[2] * pos[2][1] + coef[3] * pos[3][1] +
                     coef[4] * pos[4][1] + coef[5] * pos[5][1];

            res[2] = coef[0] * pos[0][2] + coef[1] * pos[1][2] +
                     coef[2] * pos[2][2] + coef[3] * pos[3][2] +
                     coef[4] * pos[4][2] + coef[5] * pos[5][2];
        }
        else
        {
            if(nf != null)
            {
                vtx = nf.headVertex(nf.nextEdgeIndex(-ne));
                float[] p1 = vtx.getPos(d);

                vtx = nf.headVertex(nf.nextEdgeIndex(nf.nextEdgeIndex(-ne)));
                float[] p2 = vtx.getPos(d);

                res[0] = coef[4] * p1[0] + coef[5] * p2[0];
                res[1] = coef[4] * p1[1] + coef[5] * p2[1];
                res[2] = coef[4] * p1[2] + coef[5] * p2[2];
            }

            float[] p1 = cf.headVertex(ce).getPos(d);
            float[] p2 = cf.headVertex(cf.nextEdgeIndex(ce)).getPos(d);
            float[] p3 = cf.headVertex(cf.nextEdgeIndex(cf.nextEdgeIndex(ce))).getPos(d);
            float[] p4 = cf.tailVertex(ce).getPos(d);

            res[0] += coef[0] * p1[0] + coef[1] * p2[0] +
                      coef[2] * p3[0] + coef[3] * p4[0];

            res[1] += coef[0] * p1[1] + coef[1] * p2[1] +
                      coef[2] * p3[1] + coef[3] * p4[1];

            res[2] += coef[0] * p1[2] + coef[1] * p2[2] +
                      coef[2] * p3[2] + coef[3] * p4[2];
        }
    }
}
