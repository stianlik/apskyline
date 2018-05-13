#ifndef LINKED_LIST_ITER_H
#define LINKED_LIST_ITER_H
#include "linked_list.h"
#include "gc.h"

typedef struct linked_list_iter_t {
	int thread_id;
	linked_list_t *list;
	node_t *prev;
	node_t *cur;
} linked_list_iter_t;

linked_list_iter_t *linked_list_iter_create(int thread_id, linked_list_t *list) {
	linked_list_iter_t *iter = malloc(sizeof(linked_list_iter_t));
	iter->thread_id = thread_id;
	iter->list = list;
	iter->prev = list->head;
	iter->cur = list->tail;
	return iter;
}

void linked_list_iter_free(linked_list_iter_t *iter) {
	free(iter);
}

inline bool linked_list_iter_is_modified(linked_list_iter_t *iter) {
	return iter->prev->deleted || iter->prev->next != iter->cur;
}

inline void linked_list_iter_reset(linked_list_iter_t *iter) {
	iter->prev = iter->list->head;
	iter->cur = iter->prev->next;
#ifdef GC_ACTIVE
	gc_step(iter->thread_id);
	gc_clean(iter->thread_id);
#endif
}

inline void linked_list_iter_next(linked_list_iter_t *iter) {
	iter->prev = iter->cur;
	iter->cur = iter->prev->next;
}

inline void linked_list_iter_unlock(linked_list_iter_t *iter) {
	omp_unset_lock(&iter->prev->lock);
	omp_unset_lock(&iter->cur->lock);
}

inline bool linked_list_iter_lock(linked_list_iter_t *iter) {
	omp_set_lock(&iter->prev->lock);
	omp_set_lock(&iter->cur->lock);
	if (linked_list_iter_is_modified(iter)) {
		linked_list_iter_unlock(iter);
		return false;
	}
	return true;
}

inline bool linked_list_iter_remove(linked_list_iter_t *iter) {
	if (linked_list_iter_lock(iter)) {
		node_t *node = iter->cur;
		node->deleted = true;

		// Sequential remove operation
		iter->prev->next = iter->cur->next;
		iter->cur = iter->cur->next;

		// Unlock
		omp_unset_lock(&iter->prev->lock);
		omp_unset_lock(&node->lock);

#ifndef GC_LEAK
		gc_delete(iter->thread_id, node);
#endif

		return true;
	}
	else {
		return false;
	}
}

inline bool linked_list_iter_add(linked_list_iter_t *iter, node_t *node) {
	if (linked_list_iter_lock(iter)) {
		node->next = iter->cur;
		iter->prev->next = node;
		linked_list_iter_unlock(iter);
		return true;
	}
	else {
		return false;
	}
}

inline bool linked_list_iter_not_tail(linked_list_iter_t *iter) {
	return iter->cur != iter->list->tail;
}
#endif
