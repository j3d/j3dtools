/*
 * j3d.org Copyright (c) 2001-2015
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Utilities for finding and manipulating files
 *
 * @author justin
 */
public class DataUtils
{
    private static ErrorReporter errorReporter;

    static
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public static void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter != null ? reporter : DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Search for a file in the classpath and user/application directories. Since classpath
     * may be a complex setup, we use several different strategies to find it amongst the
     * various classloaders.
     * <p/>
     * The order of searching is:
     * <ol>
     *     <li>User's home directory <code>$user.home</code></li>
     *     <li>Application home directory <code>$user.dir</code></li>
     *     <li>Classpath from the classloader of the defaultClass</li>
     *     <li>Classpath of the System classloader</li>
     * </ol>
     *
     * @param fileToSearch The relative path and file name to go looking for
     * @param defaultClass The class that we use as the initial classloader scope to search in
     * @param defaultFullPathFile A fully qualified path to use, may be null
     * @return The located file, or null if we cannot find it the default fill path file
     */
    public static File lookForFile(String fileToSearch, Class defaultClass, final String defaultFullPathFile)
    {

        File retPointsFile = defaultFullPathFile != null ? new File(defaultFullPathFile) : null;
        try
        {

            String path = System.getProperty("user.home");
            File f = new File(path, fileToSearch);
            if (f.exists() && f.canRead())
            {
                errorReporter.messageReport("Loading file from user home " + path);
                retPointsFile = f;
            }
            else
            {
                path = System.getProperty("user.dir");
                f = new File(path, fileToSearch);
                if (f.exists() && f.canRead())
                {
                    errorReporter.messageReport("Loading file from application home " + path);
                    retPointsFile = f;
                }
                else
                {
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    URL resourceURL = cl.getResource(fileToSearch);
                    if (resourceURL != null)
                    {
                        URI localURI = resourceURL.toURI();
                        errorReporter.messageReport("Loading from system classpath " + resourceURL.toExternalForm());
                        retPointsFile = new File(localURI);
                    }
                    else
                    {

                        cl = defaultClass.getClassLoader();
                        resourceURL = cl.getResource(fileToSearch);
                        if (resourceURL != null)
                        {
                            URI localURI = resourceURL.toURI();
                            errorReporter.messageReport("Loading file from local classloader classpath " + resourceURL.toExternalForm());
                            retPointsFile = new File(localURI);
                        }
                        else
                        {
                            errorReporter.messageReport("Unable to locate file anywhere. Using defaults");
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            errorReporter.warningReport(e.getMessage(), e);
        }

        return retPointsFile;
    }

    /**
     * Search for a resource in the classpath. Since classpath may be a complex setup,
     * we use several different strategies to find it amongst the various classloaders.
     * <p/>
     * The order of searching is:
     * <ol>
     *     <li>Classpath from the classloader of the defaultClass</li>
     *     <li>Classpath of the System classloader</li>
     * </ol>
     *
     * @param fileToSearch The relative path and file name to go looking for
     * @param defaultClass The class that we use as the initial classloader scope to search in
     * @return The located resource's URL, or null if it can't be found
     */
    public static URL lookForResource(String fileToSearch, Class defaultClass)
    {
        URL retval = null;

        try
        {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            URL resourceURL = cl.getResource(fileToSearch);
            if (resourceURL != null)
            {
                errorReporter.messageReport("Loading from system classpath " + resourceURL.toExternalForm());
                retval = resourceURL;
            }
            else
            {

                cl = defaultClass.getClassLoader();
                resourceURL = cl.getResource(fileToSearch);
                if (resourceURL != null)
                {
                    errorReporter.messageReport("Loading file from local classloader classpath " + resourceURL.toExternalForm());
                    retval = resourceURL;
                }
                else
                {
                    errorReporter.messageReport("Unable to locate file anywhere. Using defaults");
                }
            }
        }
        catch(Exception e)
        {
            errorReporter.warningReport(e.getMessage(), e);
        }

        return retval;
    }
}
