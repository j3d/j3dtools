/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.util;

// Standard imports
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

// Application specific imports
// None

/**
 * A circular list (buffer) implementation.
 * <p>
 *
 * The implementation uses a double linked list. When the toArray method is
 * called, the values are written to the list based on the current position.
 * The code is implemented for speed, not safety. Accessing a single instance
 * from multiple threads is probably going to cause problems. However, it
 * does cache internal entry values amongst instances to save on garbage
 * generation and allocation costs. If the code needs to reduce allocated
 * memory, a convenience method is provided to reduce the internal cache size.
 *
 * @author Rob Nielsen
 * @version $Revision: 1.1 $
 */
public class CircularList<T>
{
    /** a shared cache of Entry instances */
    private static ObjectArray entryCache = new ObjectArray();

    /** The current item that we are pointing to */
    private Entry<T> current;

    /** The total number of entries in the list. */
    private int count;

    /**
     * Innerclass that acts as a datastructure to create a new entry in the
     * list.
     */
    private static class Entry<T>
    {
        /** The value stored in the list */
        T value;

        /** The next item in the list */
        Entry<T> next;

        /** The previous item in the list */
        Entry<T> prev;
    }

    /**
     * Constructs a new, empty list.
     */
    public CircularList()
    {
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set
     */
    public int size()
    {
        return count;
    }

    /**
     * Check to see if this set contains elements.
     *
     * @return true if this set contains no elements.
     */
    public boolean isEmpty()
    {
        return count == 0;
    }

    /**
     * Fetch the next item in the list from this one and advance the current
     * pointer to it. If the list is empty, returns null.
     *
     * @return The next item in the list.
     */
    public T next()
    {
        if(count == 0)
            return null;

        current = current.next;
        return current.value;
    }

    /**
     * Fetch the previous item in the list from this one and retire the current
     * pointer to it. If the list is empty, returns null.
     *
     * @return The previous item in the list.
     */
    public T previous()
    {
        if(count == 0)
            return null;

        current = current.prev;
        return current.value;
    }

    /**
     * Get the current item that is being pointed to in the list.  If the list
     * is empty, returns null.
     *
     * @return The current item
     */
    public T current()
    {
        if(count == 0)
            return null;

        return current.value;
    }

    /**
     * Returns true if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return true if this set contains the specified element.
     */
    public boolean contains(T o)
    {
        boolean ret_val = false;

        if((o != null) && (count != 0))
        {

            Entry<T> checker = current.next;
            while((checker != current) && !ret_val)
            {
                ret_val = (checker == o) || checker.equals(o);
            }
        }

        return ret_val;
    }

    /**
     * Adds the specified element to this list. Duplicate entries are allowed,
     * but null values are not. The Add operation places it at the end of the
     * list, just behind the current pointer.
     *
     * @param o element to be added to this set
     * @throws NullPointerException The passed object was null
     */
    public void add(T o)
    {
        if(o == null)
            throw new NullPointerException("Attempting to add null object");

        Entry<T> e = newEntry();
        e.value = o;

        if(current == null)
        {
            e.prev = e;
            e.next = e;
        }
        else
        {
            e.prev = current.prev;
            e.next = current;
            current.prev = e;
            e.prev.next = e;
        }

        count++;

    }

    /**
     * Removes the specified element from this set if it is present. Comparison
     * is made using both referential equality or .equals(). If the removed
     * object is the current object, the list pointer is moved to the next
     * object in the list.
     *
     * @param o object to be removed from this set, if present.
     * @return true if the set contained the specified element.
     */
    public boolean remove(T o)
    {
        if((o == null) || (count == 0))
            return false;

        // find the object
        boolean ret_val = false;
        Entry<T> checker = current.next;
        while((checker != current) && !ret_val)
            ret_val = (checker == o) || checker.equals(o);

        if(ret_val)
        {
            Entry<T> tmp = checker.next;
            checker.prev.next = checker.next;
            tmp.next.prev = checker;
            checker.next = null;
            checker.prev = null;
            checker.value = null;

            if(current == checker)
                current = (count == 1) ? null : tmp;

            count--;

            freeEntry(checker);
        }

        return ret_val;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear()
    {
        if(count == 0)
            return;

        Entry<T> next = current;

        do
        {
            current = next;
            next = current.next;

            current.prev = null;
            current.next = null;
            current.value = null;

            freeEntry(current);
        }

        while((next != null) && (next != current));

        current = null;
        count = 0;
    }

    /**
     * Adds all of the elements in the specified collection to this set.
     * The behavior of this operation is undefined if the specified collection
     * is modified while the operation is in progress.
     * <p>
     * This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.
     *
     * @param c collection whose elements are to be added to this collection.
     * @return true if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if this collection does not
     *         support the <tt>addAll</tt> method.
     * @throws NullPointerException if the specified collection is null.
     */
    public boolean addAll(Collection<T> c)
    {
        boolean modified = false;
        Iterator<T> e = c.iterator();

        while (e.hasNext())
        {
            T obj = e.next();

            if(!contains(obj))
            {
                add(obj);
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Removes from this set all of its elements that are contained in
     * the specified collection.
     * <p>
     * This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's so contained, it's removed from
     * this collection with the iterator's <tt>remove</tt> method.<p>
     *
     * @param c elements to be removed from this set.
     * @return true if this collection changed as a result of the call.
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     *         is not supported by this collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<T> c)
    {
        if((c.size() == 0) || (count == 0))
            return false;

        boolean modified = false;
        Iterator<T> e = c.iterator();

        while(e.hasNext())
        {
            T obj = e.next();
            if(remove(obj))
                modified = true;
        }

        return modified;
    }

    /**
     * Returns an array containing all of the elements in this collection.  If
     * the collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the
     * same order.  The returned array will be "safe" in that no references to
     * it are maintained by the collection.  (In other words, this method must
     * allocate a new array even if the collection is backed by an Array).
     * The caller is thus free to modify the returned array.<p>
     *
     * This implementation allocates the array to be returned, and iterates
     * over the elements in the collection, storing each object reference in
     * the next consecutive element of the array, starting with element 0.
     *
     * @return an array containing all of the elements in this collection.
     */
    public Object[] toArray()
    {
        Object[] ret_val = new Object[count];
        Entry<T> e = current;

        for(int i = 0; i < count; i++)
        {
            ret_val[i] = e;
            e = e.next;
        }

        return ret_val;
    }

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.<p>
     *
     * If the collection fits in the specified array with room to spare (i.e.,
     * the array has more elements than the collection), the element in the
     * array immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the
     * collection <i>only</i> if the caller knows that the collection does
     * not contain any <tt>null</tt> elements.)<p>
     *
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order. <p>
     *
     * This implementation checks if the array is large enough to contain the
     * collection; if not, it allocates a new array of the correct size and
     * type (using reflection).  Then, it iterates over the collection,
     * storing each object reference in the next consecutive element of the
     * array, starting with element 0.  If the array is larger than the
     * collection, a <tt>null</tt> is stored in the first location after the
     * end of the collection.
     *
     * @param array the array into which the elements of the set are to
     *     be stored, if it is big enough; otherwise, a new array of the
     *     same runtime type is allocated for this purpose.
     * @return an array containing the elements of the collection.
     * @throws NullPointerException if the specified array is <tt>null</tt>.
     * @throws ArrayStoreException if the runtime type of the specified array
     *     is not a supertype of the runtime type of every element in this
     *     collection.
     */
    public T[] toArray(T[] array)
    {
        int size = count;

        if(array.length < size)
        {
            Class cls = array.getClass();
            array = (T[])Array.newInstance(cls.getComponentType(), size);
        }

        Entry<T> e = current;

        for(int i = 0; i < count; i++)
        {
            array[i] = e.value;
            e = e.next;
        }

        return array;
    }

    /**
     * Compares the specified object with this set for equality.  Returns
     * true if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set and in the exact same order.
     *
     * This implementation first checks if the specified object is this
     * set; if so it returns true.  Then, it checks if the
     * specified object is a set whose size is identical to the size of
     * this set; if not, it it returns false.  If so, it returns
     * <tt>containsAll((Collection) o)</tt>.
     *
     * @param o Object to be compared for equality with this set.
     * @return true if the specified object is equal to this set.
     */
    public boolean equals(Object o)
    {
        if(o == this)
            return true;

        if(!(o instanceof CircularList))
            return false;

        CircularList<T> list = (CircularList<T>)o;

        // check that the list contains the start pointer
        if((list.count != count) && !list.contains(current.value))
            return false;

        // So let's go find the start pointer in the other list
        Entry<T> remote = list.current;
        while(remote != current)
            remote = remote.next;

        boolean ret_val = true;

        Entry<T> local = current;

        for(int i = 0; i < count && ret_val;i++)
            ret_val = (local == remote) && local.equals(remote);

        return ret_val;
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list is
     * defined to be the sum of the hash codes of the elements in the list.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * Object.hashCode.<p>
     *
     * This implementation enumerates over the set, calling the
     * <tt>hashCode</tt> method on each element in the collection, and
     * adding up the results.
     *
     * @return the hash code value for this set.
     */
    public int hashCode()
    {
        int h = 0;

        Entry<T> e = current;
        for(int i = 0; i < count; i++)
        {
            h += e.value.hashCode();
            e = e.next;
        }

        return h;
    }

    /**
     * Returns a string representation of this set.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by <tt>String.valueOf(Object)</tt>.<p>
     *
     * This implementation creates an empty string buffer, appends a left
     * square bracket, and iterates over the collection appending the string
     * representation of each element in turn.  After appending each element
     * except the last, the string <tt>", "</tt> is appended.  Finally a right
     * bracket is appended.  A string is obtained from the string buffer, and
     * returned.
     *
     * @return a string representation of this collection.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");

        Entry<T> e = current;
        for(int i = 0; i < count; i++)
        {
            buf.append(e.value);

            if(i < count - 1)
                buf.append(", ");

            e = e.next;
        }

        buf.append("]");
        return buf.toString();
    }

    /**
     * Clean up the internal cache and reduce it to zero.
     */
    public void clearCachedObjects()
    {
        entryCache.clear();
    }

    /**
     * Fetch a new entry object from the cache. If there are none, create a
     * new one.
     *
     * @return an available entry object
     */
    private synchronized static Entry newEntry()
    {
        return (entryCache.size() == 0) ?
               new Entry() :
               (Entry)entryCache.remove(0);
    }

    /**
     * Release this entry back to the cache. Assumes that the entry has been
     * freed of all the links and value before the call.
     *
     * @param e The entry to put back in the list
     */
    private static void freeEntry(Entry e)
    {
        entryCache.add(e);
    }
}
