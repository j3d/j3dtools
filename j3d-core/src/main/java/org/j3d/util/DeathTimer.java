/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2010 - 2011
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.util;

/**
 * A dark and evil class that is very impatient - if it isn't told to stop
 * in time it will kill the whole system.
 * <p>
 * 
 * Copied and tweaked from the Xj3D version of the same class.
 * 
 * @author Eric Fickenscher
 * @version $Revision: 1.0 $
 */
public class DeathTimer extends Thread 
{
    /** Exit code to use when this triggers */
    private final int exitCode;
    
    /** TRUE if we still want this DeathTimer thread
     * to terminate the application by calling System.exit();
     * Set to FALSE by calling exit() if we no longer want
     * to kill the whole system. */
    private boolean exitIfTimeExceeded;

    /** Allow the application to run this many milliseconds
     * before calling System.exit() */
    private long waitTime;

    /** Amount of time in milliseconds to sleep between timeout checks */
    private long sleepDuration;


    /**
	 * Constructor an instance of the death timer with a wait time and
	 * the predefined exit code.
	 * 
     * @param wait long value - number of milliseconds to wait
     *     before calling System.exit().
     * @param exitCode The code to use when this forces the exit
     */
    public DeathTimer(long wait, int exitCode)
    {
        this.exitCode = exitCode;
        waitTime = wait;
        exitIfTimeExceeded = true;
        // sleepDuration is one second unless total wait time is longer than
        // a minute, in which case we sleep for a minute at a time
        sleepDuration = wait > 60000 ? 60000 : 1000;
    }

    /**
     * Continue to call Thread.sleep while {@link #exitIfTimeExceeded} is
     * TRUE.  Shutdown the application if {@link #waitTime} is
     * exceeded.
     */
    @Override
    public void run()
    {
        waitTime += System.currentTimeMillis();

        while(exitIfTimeExceeded)
        {
            if(System.currentTimeMillis() > waitTime) 
            {
                System.out.println("Time exceeded, killing system");
                System.exit(exitCode);
            }

            try 
            {
                sleep(sleepDuration);
            } 
            catch(Exception e)
            {
                // ignored
            }
        }
    }

    /**
     * Exit this watcher.  Call this method if you no longer
     * want to terminate the application. May be called whenever, even if this
     * class has not yet been started.
     */
    public void exit() 
    {
        exitIfTimeExceeded = false;
    }
}
