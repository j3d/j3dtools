/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter.importer;

// External imports
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Local Imports
import org.j3d.loaders.ldraw.*;
import j3d.filter.graph.*;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import j3d.filter.FilterExitCode;
import j3d.filter.FilterImporter;
import j3d.filter.GeometryDatabase;
import j3d.filter.SceneGraphObjectType;

/**
 * Implementation of an importer for the LDraw format
 * <p/>
 * The parsing is handled by the code in the package 
 * {@link org.j3d.loaders.ldraw}. When parsing geometry, it tries to handle 
 * all the geometry for a single colour index in to a single geometry instance.
 *
 * @author Justin
 * @version $Revision$
 */
public class LDrawImporter
    implements FilterImporter, LDrawParseObserver
{
    /** The error reporter for this interface */
    private ErrorReporter reporter;
    
    /** The database instance that we add geometry to */
    private GeometryDatabase database;
    
    /** Current cull state. Starts with the LDraw default of false */
    private boolean currentCull;
    
    /** Current CCW state. Starts with the LDraw default of true */
    private boolean currentCCW;

    /** 
     * When run in nested mode, this is the ID of the root scene graph
     * object that we place the root nodes in.
     */
    private int rootObjectId;
    
    /** Mapping from colour ID to the holding mesh ID */
    private Map<Integer, Integer> colourToGeomIDMap;
    
    /**
     * Default constructor needed to implement the importer dynamic class
     * loading.
     */
    public LDrawImporter()
    {
        reporter = DefaultErrorReporter.getDefaultReporter();
        currentCull = false;
        currentCCW = true;
        
        colourToGeomIDMap = new HashMap<>();
    }
    
    //------------------------------------------------------------------------
    // Methods defined by LDrawParseObserver
    //------------------------------------------------------------------------

    @Override
    public boolean header(LDrawHeader hdr)
    {
        // Don't bother doing anything right now
        return true;
    }

    @Override
    public boolean bfcStatement(boolean ccw, boolean cull)
    {
        currentCCW = ccw;
        currentCull = cull;

        return true;
    }

    @Override
    public boolean fileReference(LDrawFileReference ref)
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean renderable(LDrawRenderable rend)
    {
        LDrawColor colour = rend.getColor();
        int col_idx = colour.getIndex();
        
        VertexGeometry base_geom = null;
        
        // For now, don't bother with the actual colour objects, just
        // use the ID for mapping purposes for now. 
        if(colourToGeomIDMap.containsKey(col_idx))
        {
            int geom_idx = colourToGeomIDMap.get(colour.getIndex());
        
            base_geom = (VertexGeometry)database.getObject(geom_idx);
        }
        
        if(rend instanceof LDrawLine)
        {
            if(base_geom == null)
            {
                base_geom = (VertexGeometry)database.createObject(SceneGraphObjectType.LINES);
                colourToGeomIDMap.put(col_idx, base_geom.getID());

                Mesh mesh = (Mesh)database.createObject(SceneGraphObjectType.MESH);
                mesh.addGeometry(base_geom);
                
                database.updateObject(mesh);
            }
            
            LDrawLine l = (LDrawLine)rend;
            double[] pt = l.getStartPoint();
            
            Vertex vtx = new Vertex();
            vtx.xCoord = pt[0];
            vtx.yCoord = pt[1];
            vtx.zCoord = pt[2];
            base_geom.addVertex(vtx);
            
            pt = l.getEndPoint();
            
            vtx = new Vertex();
            vtx.xCoord = pt[0];
            vtx.yCoord = pt[1];
            vtx.zCoord = pt[2];
            base_geom.addVertex(vtx);
            
            database.updateObject(base_geom);
        }
        else if(rend instanceof LDrawTriangle)
        {
            if(base_geom == null)
            {
                base_geom = (VertexGeometry)database.createObject(SceneGraphObjectType.TRIANGLES);
                colourToGeomIDMap.put(col_idx, base_geom.getID());

                Mesh mesh = (Mesh)database.createObject(SceneGraphObjectType.MESH);
                mesh.addGeometry(base_geom);
                
                database.updateObject(mesh);
            }

            LDrawTriangle l = (LDrawTriangle)rend;
            double[] pt = l.getStartPoint();
            
            Vertex vtx = new Vertex();
            vtx.xCoord = pt[0];
            vtx.yCoord = pt[1];
            vtx.zCoord = pt[2];
            base_geom.addVertex(vtx);
            
            pt = l.getMiddlePoint();
            
            Vertex vtx1 = new Vertex();
            vtx1.xCoord = pt[0];
            vtx1.yCoord = pt[1];
            vtx1.zCoord = pt[2];

            pt = l.getEndPoint();
            
            Vertex vtx2 = new Vertex();
            vtx2.xCoord = pt[0];
            vtx2.yCoord = pt[1];
            vtx2.zCoord = pt[2];

            if(currentCCW)
            {
                base_geom.addVertex(vtx1);
                base_geom.addVertex(vtx2);
            }
            else
            {
                base_geom.addVertex(vtx2);
                base_geom.addVertex(vtx1);
            }
            
            database.updateObject(base_geom);
        }
        else if(rend instanceof LDrawQuad)
        {
            if(base_geom == null)
            {
                base_geom = (VertexGeometry)database.createObject(SceneGraphObjectType.QUADS);
                colourToGeomIDMap.put(col_idx, base_geom.getID());

                Mesh mesh = (Mesh)database.createObject(SceneGraphObjectType.MESH);
                mesh.addGeometry(base_geom);
                
                database.updateObject(mesh);
            }

            LDrawQuad l = (LDrawQuad)rend;
            double[] pt = l.getStartPoint();
            
            Vertex vtx = new Vertex();
            vtx.xCoord = pt[0];
            vtx.yCoord = pt[1];
            vtx.zCoord = pt[2];
            base_geom.addVertex(vtx);
            
            pt = l.getMiddlePoint1();
            
            Vertex vtx1 = new Vertex();
            vtx1.xCoord = pt[0];
            vtx1.yCoord = pt[1];
            vtx1.zCoord = pt[2];

            pt = l.getMiddlePoint2();
            
            Vertex vtx2 = new Vertex();
            vtx2.xCoord = pt[0];
            vtx2.yCoord = pt[1];
            vtx2.zCoord = pt[2];

            pt = l.getEndPoint();
            
            Vertex vtx3 = new Vertex();
            vtx3.xCoord = pt[0];
            vtx3.yCoord = pt[1];
            vtx3.zCoord = pt[2];

            if(currentCCW)
            {
                base_geom.addVertex(vtx1);
                base_geom.addVertex(vtx2);
                base_geom.addVertex(vtx3);
            }
            else
            {
                base_geom.addVertex(vtx3);
                base_geom.addVertex(vtx2);
                base_geom.addVertex(vtx1);
            }
            
            database.updateObject(base_geom);
        }
        
        return true;
    }

    //------------------------------------------------------------------------
    // Methods defined by FilterImporter
    //------------------------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter eh)
    {
        reporter = eh != null ? eh : DefaultErrorReporter.getDefaultReporter();
    }

    @Override
    public FilterExitCode initialize(GeometryDatabase db)
    {
        database = db;
        
        return FilterExitCode.SUCCESS;
    }

    @Override
    public FilterExitCode parse(InputStream is)
        throws IOException
    {
        InputStream input = is;
        
        if(!(is instanceof BufferedInputStream))
            input = new BufferedInputStream(is);
        
        LDrawParser parser = new LDrawParser(input);
        parser.setErrorReporter(reporter);
        parser.setParseObserver(this);
        
        parser.parse(false);
        
        return FilterExitCode.SUCCESS;
    }
    
    /**
     * Process the contents of the input stream now. Used when loading nested files.
     * 
     * @param is The input stream to use
     * @param rootId The ID of the scene graph object that is the root for
     *    the new scene chunk to be placed in to
     * @return A success or failure error code from FilterExitCodes
     * @throws IOException some sort of low level IO error happened during parsing
     *    that is outside the normal exit codes.
     */
    public FilterExitCode parse(InputStream is, int rootId)
        throws IOException
    {
        rootObjectId = rootId;
        
        FilterExitCode ret_val = parse(is);
        
        rootObjectId = 0;
        
        return ret_val;
    }
        
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
}
