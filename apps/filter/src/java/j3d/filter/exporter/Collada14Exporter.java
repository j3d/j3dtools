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

package j3d.filter.exporter;

// External imports
import javax.xml.stream.*;

import java.io.OutputStream;

// Local Imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import j3d.filter.*;

/**
 * Exporter for the Collada v1.4 file format.
 * <p/>
 * Exports a simple 1.4 format.
 *
 * @author Justin
 * @version $Revision$
 */
public class Collada14Exporter
    implements FilterExporter
{
    /** The error reporter for this interface */
    private ErrorReporter reporter;
    
    /** The database that we're going to be reading the scene graph from */
    private GeometryDatabase database;
    
    /**
     * Default constructor needed so that reflection works correctly.
     */
    public Collada14Exporter()
    {
        reporter = DefaultErrorReporter.getDefaultReporter();
    }
    
    //------------------------------------------------------------------------
    // Methods defined by FilterExporter
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
    public FilterExitCode export(OutputStream os)
    {
        try
        {
            XMLOutputFactory fac = XMLOutputFactory.newFactory();            
            XMLStreamWriter writer = fac.createXMLStreamWriter(os);
            
            writer.writeStartDocument();
            writer.writeStartElement("COLLADA");
            writer.writeAttribute("xmlns", "http://www.collada.org/2005/11/COLLADASchema");
            writer.writeAttribute("version", "1.4.1");
            
            writeMaterials(writer);
            writeGeometry(writer);
            writeScene(writer);
            
            writer.writeEndElement();
            writer.writeEndDocument();
            
            writer.flush();
            writer.close();
        }
        catch(FactoryConfigurationError fce)
        {
            reporter.fatalErrorReport("My message", fce);
        }
        catch (XMLStreamException xse)
        {
            reporter.errorReport("My message", xse);
        }

        return FilterExitCode.SUCCESS;
    }
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    private void writeMaterials(XMLStreamWriter writer) 
        throws XMLStreamException
    {
        writer.writeStartElement("library_materials");
        writer.writeEndElement();
    }
            
    private void writeGeometry(XMLStreamWriter writer)
        throws XMLStreamException
    {
        writer.writeStartElement("library_materials");
        writer.writeEndElement();
    }

    private void writeScene(XMLStreamWriter writer)
        throws XMLStreamException
    {
        final String scene_id = "j3dscene";
        
        writer.writeStartElement("library_visual_scenes");
        writer.writeStartElement("visual_scene");
        writer.writeAttribute("id", scene_id);
        
        int count = database.getRootObjectCount();
        for(int i = 0; i < count; i++)
        {
            SceneGraphObject obj = database.getRootObject(i);
            writer.writeStartElement("node");
            writer.writeAttribute("id", "node" + i);
            writer.writeAttribute("name", "node" + i);
            
            writer.writeStartElement("translate");
            writer.writeCharacters("0 0 0");
            writer.writeEndElement();

            writer.writeStartElement("rotate");
            writer.writeCharacters("0 0 1 0");
            writer.writeEndElement();

            writer.writeStartElement("rotate");
            writer.writeCharacters("0 1 0 0");
            writer.writeEndElement();

            writer.writeStartElement("rotate");
            writer.writeCharacters("1 0 0 0");
            writer.writeEndElement();

            writer.writeStartElement("scale");
            writer.writeCharacters("1 1 1");
            writer.writeEndElement();

            writer.writeStartElement("instance_geometry");
            writer.writeAttribute("url", "#abc");
            writer.writeStartElement("bind_material");
            writer.writeStartElement("technique_common");
            writer.writeStartElement("instance_material");
            writer.writeAttribute("symbol", "material ABC");
            writer.writeAttribute("target", "#whiteMaterial");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();

            writer.writeEndElement();

            writer.writeEndElement();
        }
        
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("scene");
        writer.writeStartElement("instance_visual_scene");
        writer.writeAttribute("url","#" + scene_id);
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
