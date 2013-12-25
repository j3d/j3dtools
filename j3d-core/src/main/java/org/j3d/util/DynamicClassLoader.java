/*****************************************************************************
 *                                                J3D.org Copyright (c) 2000
 *                                                             Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
import java.io.InvalidClassException;
import java.text.MessageFormat;

// Local imports
// none

/**
 * A generalised class used to dynamically load other classes according to
 * a preset set of rules.
 * <P>
 *
 * The class loader uses the CLASSPATH setting to locate and load a given
 * class. If the appropriate methods are called, it will attempt to confirm
 * that the class conforms to a specific interface or base class before
 * actually instantiating that class. Various options are provided for this
 * and the loader automatically checks and issues the appropriate errors.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>nullClassNameMsg: Message when the class name requested to be loaded
 *     is null. </li>
 * <li>nullBaseClassNameMsg: Message when the base class name requested to be
 *     checked against is null</li>
 * <li>invalidBaseClassMsg: Message when the loaded class does not implement
 *     the requested base class</li>
 * <li>nullClassNameMsg: Message when the class failed to initialise</li>
 * </ul>
 *
 * @version $Revision: 1.3 $
 * @author Justin Couch
 */
public class DynamicClassLoader
{
    /** Message in the exceptions when the class name is not useful */
    private static final String NULL_NAME_MSG_PROP =
            "org.j3d.util.DynamicClassLoader.nullClassNameMsg";

    /** Message in the exceptions when the base class name is not useful */
    private static final String NULL_BASE_MSG_PROP =
            "org.j3d.util.DynamicClassLoader.nullBaseClassNameMsg";

    /**
     * Message in the exceptions when a class does not implement the
     * correct base class required by the caller.
     */
    private static final String BACKGROUND_MSG_PROP =
        "org.j3d.util.DynamicClassLoader.invalidBaseClassMsg";

    /** Message in the exceptions when a class fails to load correctly */
    private static final String INIT_MSG_PROP =
        "org.j3d.util.DynamicClassLoader.badClassInitMsg";

    /** Message in the exceptions when a base class fails to load correctly */
    private static final String INIT_BASE_MSG_PROP =
        "org.j3d.util.DynamicClassLoader.badBaseClassInitMsg";

    /** Message when we can't locate the requested class that needs to be loaded */
    private static final String MISSING_CLASS_MSG_PROP =
        "org.j3d.util.DynamicClassLoader.missingClassMsg";

    /** Message when we can't locate the requested base class that needs to be loaded */
    private static final String MISSING_BASE_CLASS_MSG_PROP =
        "org.j3d.util.DynamicClassLoader.missingBaseClassMsg";

    /** Message when the classes this depend on have a problem */
    private static final String DEPENDENT_CLASS_MSG_PROP = 
        "org.j3d.util.DynamicClassLoader.classDependencyInitMsg";

    /** Message when the classes this base class depends on have a problem */
    private static final String DEPENDENT_BASE_CLASS_MSG_PROP = 
        "org.j3d.util.DynamicClassLoader.baseClassDependencyInitMsg";

    /**
     * Private constructor to prevent instantiation of this static only class.
     */
    private DynamicClassLoader()
    {
    }

    /**
     * Load the named class with no checking of the background. The limitation
     * to the loading and instantiation process is that the class must have
     * a public default constructor. As this method does not take any
     * arguments, constructors that do require parameters cannot be called.
     *
     * @param name The fully qualified name of the class to be loaded
     * @return An instance of the named class if it could be found.
     * @throws NullPointerException The class name supplied is null or zero
     *     length
     * @throws ClassNotFoundException We couldn't locate the class anywhere
     * @throws InvalidClassException The class could not be instantiated either
     *     due to internal errors or no default constructor
     */
    public static Object loadBasicClass(String name)
        throws ClassNotFoundException, InvalidClassException
    {
        if((name == null) || (name.trim().length() == 0))
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NULL_NAME_MSG_PROP);
            throw new NullPointerException(msg);
        }

        Object ret_val = null;

        try
        {
            Class new_class = Class.forName(name);
            ret_val = new_class.newInstance();
        }
        catch(ClassNotFoundException cnfe)
        {
            // Just rethrow this particular error. Done to save more mess
            // later on in the catch list.
            throw cnfe;
        }
        catch(Exception e)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INIT_MSG_PROP);

            Object[] msg_args = { name };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);

            InvalidClassException ex = new InvalidClassException(msg);
            ex.initCause(e);
            throw ex;
        }
        catch(LinkageError le)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(DEPENDENT_CLASS_MSG_PROP);

            Object[] msg_args = { name };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);

            throw new InvalidClassException(msg);
        }

        return ret_val;
    }

    /**
     * Load the class that has the given class as a super class. This will
     * check for both the interface and derived class being of the given type.
     *
     * @param name The fully qualified name of the class to be loaded
     * @param base The fully qualified name of the base class to be checked
     *     against
     * @return An instance of the named class if it could be found.
     * @throws NullPointerException The class name or base class name supplied
     *     is null or zero length
     * @throws ClassNotFoundException We couldn't locate the class anywhere
     * @throws InvalidClassException The class could not be instantiated either
     *     due to internal errors or no default constructor
     */
    public static Object loadCheckedClass(String name, String base)
        throws ClassNotFoundException, InvalidClassException
    {
        if((name == null) || (name.trim().length() == 0))
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NULL_NAME_MSG_PROP);
            throw new NullPointerException(msg);
        }

        if((base == null) || (base.trim().length() == 0))
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NULL_BASE_MSG_PROP);
            throw new NullPointerException(msg);
        }

        Object ret_val = null;

        try
        {
            Class base_class = Class.forName(base);
            ret_val = loadCheckedClass(name, base_class);
        }
        catch(ClassNotFoundException cnfe)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(MISSING_BASE_CLASS_MSG_PROP);

            Object[] msg_args = { base };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);
            
            throw new InvalidClassException(msg);
        }
        catch(ExceptionInInitializerError eiie)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg_pattern = intl_mgr.getString(INIT_BASE_MSG_PROP);

            Object[] msg_args = { base };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);

            InvalidClassException ex = new InvalidClassException(msg);
            ex.initCause(eiie);
            throw ex;
        }
        catch(LinkageError le)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg_pattern = intl_mgr.getString(DEPENDENT_BASE_CLASS_MSG_PROP);

            Object[] msg_args = { base };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);
            throw new InvalidClassException(msg);
        }

        return ret_val;
    }

    /**
     * Load the class that has the given class as a super class. This will
     * check for both the interface and derived class being of the given type.
     * @param <T>
     *
     * @param name The fully qualified name of the class to be loaded
     * @param base The fully qualified name of the base class to be checked
     *     against
     * @return An instance of the named class if it could be found.
     * @throws NullPointerException The class name or base class name supplied
     *     is null or zero length
     * @throws ClassNotFoundException We couldn't locate the class anywhere
     * @throws InvalidClassException The class could not be instantiated either
     *     due to internal errors or no default constructor
     */
    public static <T> T loadCheckedClass(String name, Class<T> base)
        throws ClassNotFoundException, InvalidClassException
    {
        if((name == null) || (name.trim().length() == 0))
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NULL_NAME_MSG_PROP);
            throw new NullPointerException(msg);
        }

        if(base == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NULL_BASE_MSG_PROP);
            throw new NullPointerException(msg);
        }

        T ret_val = null;
        boolean check_ok = true;

        try
        {
            Class<T> new_class = (Class<T>)Class.forName(name);

            if(check_ok = backgroundChecks(new_class, base))
                ret_val = new_class.newInstance();
        }
        catch(ClassNotFoundException cnfe)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(MISSING_CLASS_MSG_PROP);

            Object[] msg_args = { name };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);
            
            throw new InvalidClassException(msg);
        }
        catch(Exception e)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg_pattern = intl_mgr.getString(INIT_MSG_PROP);

            Object[] msg_args = { name };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);

            InvalidClassException ex = new InvalidClassException(msg);
            ex.initCause(e);
            throw ex;
        }

        if(!check_ok)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg_pattern = intl_mgr.getString(BACKGROUND_MSG_PROP);

            Object[] msg_args = { name, base.getName() };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern,
                                                      intl_mgr.getFoundLocale());
            String msg = msg_fmt.format(msg_args);

            throw new InvalidClassException(msg);
        }

        return ret_val;
    }

    /**
     * Check the current class to see if it conforms to the required base
     * class type. This method may be recursive if the class is derived from
     * more than one class. It will also recursively check the derived
     * interfaces of the interfaces that this class implements
     *
     * @param current The class to be checked for conformity
     * @param source The class to be checked against
     * @return true if the the class is or implements the base class.
     */
    private static boolean backgroundChecks(Class current, Class source)
    {
        boolean ret_val = false;

        ret_val = source.isAssignableFrom(current);

        // if this is not an instance of the source class then let's check
        // the base class (if it has one) for a match.
        if(!ret_val)
        {
            Class base = current.getSuperclass();
            if(base != null)
             ret_val = backgroundChecks(base, source);
        }

        return ret_val;
    }
}
