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

package org.j3d.geom.particle;

// Standard imports
import org.j3d.util.ObjectArray;

// Application specific imports
// None

/**
 * A special-case linked-list (buffer) implementation for particle systems.
 * <p>
 *
 * The implementation uses a double linked list.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ParticleList
{
    /** a shared cache of Entry instances */
    private static ObjectArray entryCache = new ObjectArray();

    /** The start of the list */
    private Entry start;

    /** The last item on the list */
    private Entry end;

    /** The current item that we are pointing to */
    private Entry current;

    /** The total number of entries in the list. */
    private int count;

    /**
     * Innerclass that acts as a datastructure to create a new entry in the
     * list.
     */
    private static class Entry
    {
        /** The value stored in the list */
        Particle value;

        /** The next item in the list */
        Entry next;

        /** The previous item in the list */
        Entry prev;
    }

    /**
     * Constructs a new, empty list.
     */
    ParticleList()
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
     * Reset the list pointer to just before the first item in the list.
     */
    public void reset()
    {
        current = null;
    }

    /**
     * Fetch the next item in the list from this one and advance the current
     * pointer to it. If the list is empty, returns null.
     *
     * @return The next item in the list.
     */
    public Particle next()
    {
        if((count == 0) || (current == end))
            return null;

        if(current == null)
            current = start;
        else
            current = current.next;

        return current.value;
    }

    /**
     * Get the current item that is being pointed to in the list.  If the list
     * is empty, returns null.
     *
     * @return The current item
     */
    public Particle current()
    {
        if(count == 0)
            return null;

        if(current == null)
            current = start;

        return current.value;
    }

    /**
     * Adds the specified element to this list. Duplicate entries are allowed,
     * but null values are not. The Add operation places it at the end of the
     * list, just behind the current pointer.
     *
     * @param o element to be added to this set
     * @throws NullPointerException The passed object was null
     */
    public void add(Particle o)
    {
        if(o == null)
            throw new NullPointerException("Attempting to add null object");

        Entry e = newEntry();
        e.value = o;
        if(start == null)
        {
            e.prev = null;
            e.next = null;

            start = e;
        }
        else
        {
            e.prev = end;
            e.next = null;
            end.next = e;
        }

        end = e;
        count++;
    }

    /**
     * Removes the current element from this set if it is present.
     *
     * @return The object that was removed
     */
    public Particle remove()
    {
        if(count == 0)
            return null;

        Particle ret_val = current.value;
        Entry current_tmp = current.prev;

        if(current.next != null)
            current.next.prev = current.prev;

        if(current.prev != null)
            current.prev.next = current.next;

        if(current == end)
            end = current.prev;
        else if(current == start)
            start = current.next;

        current.prev = null;
        current.next = null;

        freeEntry(current);

        current = current_tmp;
        count--;

        return ret_val;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear()
    {
        if(count == 0)
            return;

        Entry next = current;

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
        start = null;
        end = null;

        count = 0;
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
