#ifndef GC_H
#define GC_H
#include "linked_list.h"

/**
 * Domain-specific memory deallocator for multithreaded lazy singly linked list
 * iteration. Used to avoid freeing nodes that other threads are working on.
 * - Main thread MUST call gc_init() before any iterators are used 
 * - Iterator MUST use gc_delete() to free nodes (as opposed to directly calling free())
 * - Main thread MUST call gc_free() when all iterators are done
 * Note that gc_init and gc_free can only be called once as global state information
 * is used.
 */

void gc_init(int thread_count);
void gc_free();

/**
 * Add node to queue of unused nodes, memory deallocation deferred until the
 * next gc_clean, or gc_free call.
 */
void gc_delete(int thread_id, node_t * const node);

/**
 * Called to notify the garbage collector that any nodes removed from the list
 * (before this call) will never be accessed by this thread again. That is, the
 * iterator has no direct reference to any nodes may have been removed by other
 * threads.
 * @returns iteration number for the current thread
 */
int gc_step(int thread_id);

/**
 * Try to free memory for nodes deleted with gc_delete(). Nodes are only freed
 * if all threads have called gc_step() at least once after the
 * gc_delete()-call. Only nodes for the specified thread are processed.
 */
void gc_clean(int thread_id);


#endif
