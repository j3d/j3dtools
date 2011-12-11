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

package j3d.filter;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

// Local Imports
import org.j3d.io.ParserNameMap;
import org.j3d.loaders.InvalidFormatException;
import org.j3d.util.DeathTimer;
import org.j3d.util.DynamicClassLoader;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

/**
 * Primary class for the filter that is run to turn one file format in to another
 * <p/>
 * Long definition
 *
 * @author Justin
 * @version $Revision$
 */
public class Filter
{
    /** App name to register preferences under */
    private static final String APP_NAME = "org.j3d.filter.Filter";

    /** The default database type name */
    private static final String DEFAULT_DB_NAME = "mem";
    
    /** The usage error message */
    private static final String USAGE_MESSAGE =
        "CDFFilter - usage:  filter [filters] input output [-loglevel type]\n" +
        "   [-maxRunTime n] [-databaseType t] [-exporter t] [-importer t] [filter_args] \n" +
        "\n" +
        "  -loglevel type [ALL|WARNINGS|ERRORS|FATAL|NONE]\n" +
        "                 The minimum level that logs should be written at\n" +
        "\n" +
        "  -maxRunTime n\n" +
        "                 Positive floating point value representing the maximum number of\n" +
        "                 minutes the application is allowed to run before termination.\n" +
        "                 If maxRunTime is not specified, no upper bound limits runtime." +
        "\n" +
        "  -databaseType type\n" +
        "                 Use the selected database type, or class. If the type name can't\n" +
        "                 be found, an attempt will be made to load as a fully qualified\n" +
        "                 class name from the classpath. If it is not provided, defaults\n" +
        "                 to " + DEFAULT_DB_NAME + "\n" +
        "  -exporter type\n" +
        "                 Defines the exporter to use, regardless of the file name extension.\n" +
        "                 The type may be one of the built-in types, or a fully qualified\n" +
        "                 class name to load" +
        "  -importer type\n" +
        "                 Defines the importer to use, regardless of the file name extension.\n" +
        "                 The type may be one of the built-in types, or a fully qualified\n" +
        "                 class name to load\n" +
        "\n";

    /** List of known filter class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_FILTERS;

    /** List of known database class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_DATABASES;

    /** List of known importer class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_IMPORTERS_FROM_MIME;

    /** List of known importer class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_EXPORTERS_FROM_MIME;

    /** List of known importer class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_IMPORTERS_FROM_TYPE;

    /** List of known importer class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_EXPORTERS_FROM_TYPE;

    /** Set of valid filters to run */
    private List<GeometryFilter> filters;

    
    /** 
     * Application timer.  If -maxRunTime is set to a positive parseable number,
     * deathTimer will terminate application once that many minutes have passed.
     */
    private DeathTimer deathTimer;

    /** Output for sending messages to the outside world */
    private ErrorReporter errorReporter;

    /** DB implementation, chosen by the command line interface */
    private GeometryImportDatabase geometryDatabase;
    
    /** The exporter that has been determined for this run */
    private FilterExporter exporter;
    
    /** The importer that has been determined for this run */
    private FilterImporter importer;
    
    /**
     * Initialisation of global constants
     */
    static
    {
        KNOWN_FILTERS = new HashMap<String, String>();
        
        KNOWN_DATABASES = new HashMap<String, String>();
        KNOWN_DATABASES.put("mem", "j3d.filter.db.InMemoryDatabase");
        
        KNOWN_IMPORTERS_FROM_MIME = new HashMap<String, String>();
        KNOWN_IMPORTERS_FROM_MIME.put("model/x-ldraw", "j3d.filter.importer.LDrawImporter");
        
        KNOWN_EXPORTERS_FROM_MIME = new HashMap<String, String>();
        KNOWN_EXPORTERS_FROM_MIME.put("model/x-collada", "j3d.filter.exporter.Collada14Exporter");
        KNOWN_EXPORTERS_FROM_MIME.put("application/xml", "j3d.filter.exporter.Collada14Exporter");
        
        KNOWN_IMPORTERS_FROM_TYPE = new HashMap<String, String>();
        KNOWN_IMPORTERS_FROM_TYPE.put("LDraw", "j3d.filter.importer.LDrawImporter");

        KNOWN_EXPORTERS_FROM_TYPE = new HashMap<String, String>();
        KNOWN_EXPORTERS_FROM_TYPE.put("COLLADA_14", "j3d.filter.exporter.Collada14Exporter");
    }
    
    /**
     * Construct a default filter implementation. If called directly from code,
     * you will need to make sure you call one of the appropriate filter() 
     * methods
     */
    public Filter() 
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.j3dResources");

        ParserNameMap content_map = new ParserNameMap();
        content_map.registerType("stl", "model/x-stl");
        content_map.registerType("obj", "model/x-obj");
        content_map.registerType("dae", "application/xml");
        content_map.registerType("ac", "application/x-ac3d");
        content_map.registerType("ldr", "model/x-ldraw");

        //URL.setFileNameMap(content_map);
        URLConnection.setFileNameMap(content_map);
        
        filters = new ArrayList<GeometryFilter>();
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param filters The identifier of the filter type.
     * @param inUrl The URL to open.
     * @param outFileName The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public FilterExitCode filter(String[] filters, URL inUrl, String outFileName, String[] fargs) 
    {
        InputStream in_stream = null;
        OutputStream out_stream = null;
        String in_encoding = null;
        String out_encoding = null;
        
        try
        {
            URLConnection conn = inUrl.openConnection();            
            in_encoding = conn.getContentType();
            in_stream = conn.getInputStream();
        }
        catch(IOException ioe)
        {
            return FilterExitCode.FILE_NOT_FOUND;
        }
        
        File f = new File(outFileName);
        if(f.exists())
        {
            if(!f.canWrite())
                return FilterExitCode.CANNOT_WRITE_OUTPUT_FILE;            
        }
        else
        {
            if(!f.mkdirs())
                return FilterExitCode.CANNOT_WRITE_OUTPUT_FILE;
        }

        try
        {
            // try to guess the output encoding from the file name map
            out_encoding = guessContentType(outFileName);
            out_stream = new FileOutputStream(f);
        }
        catch(IOException ioe)
        {
            return FilterExitCode.FILE_NOT_FOUND;
        }

        return load(filters, in_stream, out_stream, in_encoding, out_encoding, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param inFile The file to load.
     * @param outFileName The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public FilterExitCode filter(String[] filters, File inFile, String outFileName, String[] fargs)
    {
        InputStream in_stream = null;
        OutputStream out_stream = null;
        String in_encoding = null;
        String out_encoding = null;
        
        File f = new File(outFileName);
        if(f.exists())
        {
            if(!f.canWrite())
                return FilterExitCode.CANNOT_WRITE_OUTPUT_FILE;            
        }
        else
        {
            if(!f.mkdirs())
                return FilterExitCode.CANNOT_WRITE_OUTPUT_FILE;
        }

        try
        {
            // try to guess the output encoding from the file name map
            out_encoding = guessContentType(outFileName);
            out_stream = new FileOutputStream(f);
        }
        catch(IOException ioe)
        {
            return FilterExitCode.FILE_NOT_FOUND;
        }

        return load(filters, in_stream, out_stream, in_encoding, out_encoding, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param file The file to load.
     * @param outStream The output stream
     * @param outEncoding The encoding to write
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public FilterExitCode filter(String[] filters, 
                                 File file, 
                                 OutputStream outStream, 
                                 String outEncoding, 
                                 String[] fargs) 
    {
        InputStream in_stream = null;
        String in_encoding = null;
        
        if(file.exists())
        {
            if(!file.canWrite())
                return FilterExitCode.FILE_NOT_FOUND;            
        }
        else
        {
            if(!file.mkdirs())
                return FilterExitCode.FILE_NOT_FOUND;
        }

        try
        {
            // try to guess the output encoding from the file name map
            in_encoding = guessContentType(file);
            in_stream = new FileInputStream(file);
        }
        catch(IOException ioe)
        {
            return FilterExitCode.FILE_NOT_FOUND;
        }

        return load(filters, in_stream, outStream, in_encoding, outEncoding, fargs);
    }

    /**
     * Execute a chain of filters with arguments that have been loaded from the 
     * command line.
     *
     * @param args The list of arguments for this application.
     * @param exit Should we use system exit
     * @param ostream Send the output to this stream
     * @param enc Specifies the encoding to use for the output stream
     * @return The exit code
     */
    public FilterExitCode executeFilters(String[] args, boolean exit)
    {
        String filename = null;
        String outfile = null;
        String[] filters = null;

        String[] filter_args = null;
        deathTimer = null;

        int num_args = args.length;
        if (num_args < 3)
        {
            printUsage();

            if (exit)
                System.exit(FilterExitCode.INVALID_ARGUMENTS.getCodeValue());
            else
                return FilterExitCode.INVALID_ARGUMENTS;
        }
        else
        {
            if (num_args > 3)
            {
                // Work through the list of arguments looking for the first one
                // that starts with a - The first two items before that are
                int filter_count = 0;
                for(int i = 0; i < args.length; i++)
                {
                    if(args[i].charAt(0) == '-')
                        break;

                    filter_count++;
                }

                filters = new String[filter_count - 2];

                filename = args[filter_count - 2];
                outfile = args[filter_count - 1];

                if (outfile.equals("NULL"))
                    outfile = null;

                System.arraycopy(args, 0, filters, 0, filter_count - 2);
                int num_filter_args = num_args - filter_count;

                filter_args = new String[num_filter_args];
                System.arraycopy(args,
                                 filter_count,
                                 filter_args,
                                 0,
                                 num_filter_args);
            }
            else
            {
                filters = new String[1];
                filters[0] = args[0];
                filename = args[1];
                outfile = args[2];
                filter_args = new String[0];

                if (outfile.equals("NULL"))
                    outfile = null;
            }
        }

        FilterExitCode status;
        File fil = new File(filename);

        //
        // Begin the filter process.
        //
        try 
        {
            if(fil.exists()) 
            {
                if(fil.length() == 0) 
                {
                    System.out.println("Empty File: " + filename);
                    status = FilterExitCode.INVALID_INPUT_FILE;
                } 
                else
                {
                    if(outfile != null)
                        status = filter(filters, fil, outfile, filter_args);
                    else
                        status = FilterExitCode.FILE_NOT_FOUND;
                }
            } 
            else 
            {
                try 
                {
                    URL url = new URL(filename);
                    status = filter(filters, url, outfile, filter_args);
                }
                catch(MalformedURLException mfe) 
                {
                    System.out.println("Malformed URL: " + filename);
                    status = FilterExitCode.FILE_NOT_FOUND;
                }
            }
        } 
        catch(InvalidFormatException ife) 
        {
            System.out.println("EXTMSG: " + ife.getMessage());

            return FilterExitCode.INVALID_INPUT_FILE;
        }
        catch (Exception e) 
        {
            System.out.println("Unhandled exception.");
            e.printStackTrace();
            status = FilterExitCode.ABNORMAL_CRASH;
        }
        catch (OutOfMemoryError oom) 
        {
            System.out.println("Out of memory error.");
            status = FilterExitCode.OUT_OF_MEMORY;
        }
        catch (Error e) 
        {
            System.out.println("Unhandled error.");
            e.printStackTrace();
            status = FilterExitCode.EXCEPTIONAL_ERROR;
            // Check for thread death to avoid doing the cleanup
            if (e instanceof ThreadDeath)
                throw e;
        }

        //
        // Application has finished, so shut down deathTimer to avoid an
        // out-of-time termination.  Note that calls to deathTimer.exit()
        // are fine to make whether or not deathTimer.start() has been called
        //
        if(deathTimer != null)
        {
            deathTimer.exit();
        }

        if (exit)
            System.exit(status.getCodeValue());

        return status;
    }

    /**
     * Run the actual code now to do the processing. Assumes that prior to
     * calling this method all if the setup has been done.
     * @param filterNames The identifiers of the filters to use
     * @param inStream The stream to read the data from
     * @param outStream The stream to write the converted content to
     * @param inEncoding The MIME type of the incoming stream
     * @param outEncoding The MIME type of the outgoing stream
     * @param filterArgs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    private FilterExitCode load(String[] filterNames,
                                InputStream inStream,
                                OutputStream outStream,
                                String outEncoding,
                                String inEncoding,
                                String[] filterArgs) 
    {
        setupLogging(filterArgs);

        FilterExitCode status = setupDatabase(filterArgs);
        
        if(status != FilterExitCode.SUCCESS)
            return status;
        
        if((status = setupImporter(filterArgs, inEncoding)) != FilterExitCode.SUCCESS)
            return status;
        
        if((status = setupExporter(filterArgs, outEncoding)) != FilterExitCode.SUCCESS)
            return status;
        
        if((status = setupFilters(filterNames, filterArgs)) != FilterExitCode.SUCCESS)
            return status;
        
        // Now everything is initialised, run the processing
        
        try
        {
            if((status = importer.parse(inStream)) != FilterExitCode.SUCCESS)
                return status;
            
            for(GeometryFilter f: filters)
            {
                if((status = f.process()) != FilterExitCode.SUCCESS)
                    return status;        
            }
            
            status = exporter.export(outStream);
        }
        catch(IOException ioe)
        {
ioe.printStackTrace();            
System.out.println("IO Exception " + ioe);            
        }
        
        return status;        
    }
    
    /**
     * Print out the filters available.
     */
    public void printFilters()
    {
        System.out.println("Available filters:");
        for(GeometryFilter f: filters)
            System.out.println("   " + f);
    }

    /** 
     * Print the usage message for this application
     */
    public void printUsage()
    {
        System.out.println(USAGE_MESSAGE);
        printFilters();
    }
    
    /**
     * Set up the log handling.
     * 
     * @param args The command line arguments to extract the logging info from
     */
    private void setupLogging(String[] args)
    {
        int log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;

        for(int i = 0; i < args.length; i++)
        {
            String currentArg = args[i];

            if(currentArg.equals("-loglevel"))
            {
                String lvl = args[++i];

                if(lvl.equals("ALL"))
                    log_level = FilterErrorReporter.PRINT_ALL;
                else if(lvl.equals("WARNINGS"))
                    log_level = FilterErrorReporter.PRINT_WARNINGS;
                else if(lvl.equals("ERRORS"))
                    log_level = FilterErrorReporter.PRINT_ERRORS;
                else if(lvl.equals("FATAL"))
                    log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;
                else if(lvl.equals("NONE"))
                    log_level = FilterErrorReporter.PRINT_NONE;

                break;
            }
        }

        errorReporter = new FilterErrorReporter(log_level);
        
    }
    
    /**
     * Convenience method to set up the geometry database that will be used by
     * this instance of the filter.
     *  
     * @param args The command line arguments to extract the db info from
     */
    private FilterExitCode setupDatabase(String[] args)
    {
        String db_name = DEFAULT_DB_NAME;
        
        for(int i = 0; i < args.length; i++)
        {
            String currentArg = args[i];

            if(currentArg.equals("-databaseType"))
            {
                String type = args[++i];
                
                db_name = KNOWN_DATABASES.get(type);
                
                if(db_name == null)
                    db_name = type;
                
                break;
            }
        }

        try
        {
            geometryDatabase = 
                DynamicClassLoader.loadCheckedClass(db_name, GeometryImportDatabase.class);
        }
        catch(ClassNotFoundException csfe)
        {
            return FilterExitCode.INVALID_DATABASE_SPECIFIED;
        }
        catch(InvalidClassException ice)
        {
            return FilterExitCode.DATABASE_STARTUP_ERROR;
        }
        
        return FilterExitCode.SUCCESS;
    }
    
    /**
     * Convenience method to set up the importer that will be used by
     * this instance of the filter.
     *  
     * @param args The command line arguments to extract the db info from
     * @param encoding Default encoding if we can't work it out from the args
     */
    private FilterExitCode setupImporter(String[] args, String encoding)
    {
        String importer_name = null;
        
        for(int i = 0; i < args.length; i++)
        {
            String currentArg = args[i];

            if(currentArg.equals("-importer"))
            {
                String type = args[++i];
                
                importer_name = KNOWN_IMPORTERS_FROM_TYPE.get(type);
                
                if(importer_name == null)
                    importer_name = type;
                
                break;
            }
        }
        
        if(importer_name == null)
        {
            // Try to determine it from the encoding type
        }

        try
        {
            importer = 
                DynamicClassLoader.loadCheckedClass(importer_name, FilterImporter.class);
        }
        catch(ClassNotFoundException csfe)
        {
            return FilterExitCode.INVALID_IMPORTER_SPECIFIED;
        }
        catch(InvalidClassException ice)
        {
            return FilterExitCode.IMPORTER_STARTUP_ERROR;
        }
        
        return FilterExitCode.SUCCESS;
    }

    /**
     * Convenience method to set up the importer that will be used by
     * this instance of the filter.
     *  
     * @param args The command line arguments to extract the db info from
     * @param encoding Default encoding if we can't work it out from the args
     */
    private FilterExitCode setupExporter(String[] args, String encoding)
    {
        String exporter_name = null;
        
        for(int i = 0; i < args.length; i++)
        {
            String currentArg = args[i];

            if(currentArg.equals("-importer"))
            {
                String type = args[++i];
                
                exporter_name = KNOWN_EXPORTERS_FROM_TYPE.get(type);
                
                if(exporter_name == null)
                    exporter_name = type;
                
                break;
            }
        }
        
        if(exporter_name == null)
        {
            // Try to determine it from the content type
        }

        try
        {
            exporter = 
                DynamicClassLoader.loadCheckedClass(exporter_name, FilterExporter.class);
        }
        catch(ClassNotFoundException csfe)
        {
            return FilterExitCode.INVALID_EXPORTER_SPECIFIED;
        }
        catch(InvalidClassException ice)
        {
            return FilterExitCode.EXPORTER_STARTUP_ERROR;
        }
        
        return FilterExitCode.SUCCESS;
    }

    /**
     * Convenience method to set up the all of the filters that will be used by
     * this instance of the filter.
     *  
     * @param filterNames The names of each filter to apply, in order
     * @param args The command line arguments to extract the db info from
     * @return SUCCESS if they all loaded, otherwise the error code for the 
     *     first one that failed.
     */
    private FilterExitCode setupFilters(String[] filterNames, String[] args)
    {
        for(int i = 0; i < filterNames.length; i++)
        {
            String filter_name = filterNames[i];

            String filter_class_name = KNOWN_FILTERS.get(filter_name);
                
            if(filter_class_name == null)
            {
                filter_class_name = filter_name;
            }                

            try
            {
                GeometryFilter filter = 
                    DynamicClassLoader.loadCheckedClass(filter_class_name, GeometryFilter.class);
                
                FilterExitCode status = filter.initialise(args, geometryDatabase);
                
                if(status != FilterExitCode.SUCCESS)
                    return status;
                
                filters.add(filter);
            }
            catch(ClassNotFoundException csfe)
            {
                return FilterExitCode.INVALID_IMPORTER_SPECIFIED;
            }
            catch(InvalidClassException ice)
            {
                return FilterExitCode.IMPORTER_STARTUP_ERROR;
            }
        }
                
        return FilterExitCode.SUCCESS;
    }

    /** 
     * From the given file name string, guess the content type.
     * 
     * @param name The file name string to parse
     * @return The guessed content type or null if it can't be
     */
    private String guessContentType(String name)
    {
        File f = new File(name);
        URI uri = f.toURI();
        
        return URLConnection.guessContentTypeFromName(uri.toString());
    }
    
    /** 
     * From the given file object, guess the content type.
     * 
     * @param file The file to parse
     * @return The guessed content type or null if it can't be
     */
    private String guessContentType(File file)
    {
        URI uri = file.toURI();
        
        return URLConnection.guessContentTypeFromName(uri.toString());
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
    }
}
