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
import java.io.File;
import java.io.InvalidClassException;
import java.io.OutputStream;
import java.net.MalformedURLException;
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
        "   [-maxRunTime n] [filter_args] \n" +
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
        "\n";

    /** List of known filter class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_FILTERS;

    /** List of known database class names, mapped from a short name */
    private static final HashMap<String, String> KNOWN_DATABASES;

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
    
    /**
     * Initialisation of global constants
     */
    static
    {
        KNOWN_FILTERS = new HashMap<String, String>();
        
        KNOWN_DATABASES = new HashMap<String, String>();
        KNOWN_DATABASES.put("mem", "j3d.filter.db.InMemoryDatabase");
    }
    
    public Filter() 
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.xj3dResources");

        ParserNameMap content_map = new ParserNameMap();
        content_map.registerType("stl", "model/x-stl");
        content_map.registerType("obj", "model/x-obj");
        content_map.registerType("dae", "application/xml");
        content_map.registerType("ac", "application/x-ac3d");

        //URL.setFileNameMap(content_map);
        URLConnection.setFileNameMap(content_map);
        
        filters = new ArrayList<GeometryFilter>();
    }
    
    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param filters The identifier of the filter type.
     * @param url The URL to open.
     * @param out The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, URL url, String out, String[] fargs) 
    {
        return load(filters, url, null, out, null, null, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param file The file to load.
     * @param out The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, File file, String out, String[] fargs)
    {
        return load(filters, null, file, out, null, null, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param file The file to load.
     * @param out The output stream
     * @param enc The encoding to write
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, File file, OutputStream out, String enc, String[] fargs) 
    {
        return load(filters, null, file, null, out, enc, fargs);
    }

    /**
     * Execute a chain of filters with arguments that have been loaded from the 
     * command line.
     *
     * @param args The list of arguments for this application.
     * @param exit Should we use system exit
     * @param ostream If present output to this stream instead of a file
     * @param enc If ostream is used this specifies the encoding otherwise its ignored.
     * @return The exit code
     */
    public int executeFilters(String[] args, boolean exit, OutputStream ostream, String enc)
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
                System.exit(FilterExitCodes.INVALID_ARGUMENTS);
            else
                return FilterExitCodes.INVALID_ARGUMENTS;
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

        int status;
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
                    status = FilterExitCodes.INVALID_INPUT_FILE;
                } 
                else
                {
                    if(outfile != null)
                        status = filter(filters, fil, outfile, filter_args);
                    else
                        status = filter(filters, fil, ostream, enc, filter_args);
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
                    status = FilterExitCodes.FILE_NOT_FOUND;
                }
            }
        } 
        catch(InvalidFormatException ife) 
        {
            System.out.println("EXTMSG: " + ife.getMessage());

            return FilterExitCodes.INVALID_INPUT_FILE;
        }
        catch (Exception e) 
        {
            System.out.println("Unhandled exception.");
            e.printStackTrace();
            status = FilterExitCodes.ABNORMAL_CRASH;
        }
        catch (OutOfMemoryError oom) 
        {
            System.out.println("Out of memory error.");
            status = FilterExitCodes.OUT_OF_MEMORY;
        }
        catch (Error e) 
        {
            System.out.println("Unhandled error.");
            e.printStackTrace();
            status = FilterExitCodes.EXCEPTIONAL_ERROR;
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
            System.exit(status);

        return status;
    }

    /**
     * Run the actual code now to do the processing. Assumes that prior to
     * calling this method all if the setup has been done.
     * @param filter The identifier of the filter type.
     * @param url The URL to open, or null if the input is specified by the file argument.
     * @param inFile The file to load, or null if the input is specified by the url argument.
     * @param out The output filename.
     * @param filter_args The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    private int load(String[] filterNames,
                     URL url,
                     File inFile,
                     String out,
                     OutputStream outStream,
                     String outEncoding,
                     String[] filterArgs) 
    {
        setupLogging(filterArgs);
        setupDatabase(filterArgs);
        
        return FilterExitCodes.SUCCESS;        
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
    private void setupDatabase(String[] args)
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
                            DynamicClassLoader.loadCheckedClass(db_name, 
                                                                GeometryImportDatabase.class);
        }
        catch(ClassNotFoundException csfe)
        {
            
        }
        catch(InvalidClassException ice)
        {
            
        }
    }
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
    }
}
