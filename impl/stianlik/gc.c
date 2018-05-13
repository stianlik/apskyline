#include "gc.h"
#include <stdio.h>

typedef struct gc_node_t {
	int *timestamp;
	node_t *data;
	struct gc_node_t *next;
} gc_node_t;

typedef struct gc_list_t {
	gc_node_t *head;
	gc_node_t *cur;
	gc_node_t *tail;
} gc_list_t;

int g_thread_count = 0;
int *g_iter;
gc_list_t **g_thrash;

void gc_init(int thread_count) {
	g_thread_count = thread_count;
	g_iter = calloc(thread_count,sizeof(int));
	g_thrash = malloc(sizeof(node_t*) * thread_count);
	for (int i = 0; i < thread_count; ++i) {
		g_thrash[i] = malloc(sizeof(gc_list_t));
		g_thrash[i]->head = malloc(sizeof(gc_node_t));
		g_thrash[i]->head->timestamp = calloc(thread_count, sizeof(int));
		g_thrash[i]->cur = g_thrash[i]->head;
		g_thrash[i]->tail = malloc(sizeof(gc_node_t));
		g_thrash[i]->tail->timestamp = calloc(thread_count, sizeof(int));
		g_thrash[i]->head->next = g_thrash[i]->tail;
	}
}

inline int gc_step(int thread_id) {
	__sync_fetch_and_add(&g_iter[thread_id], 1);
	return g_iter[thread_id];
}

inline bool gc_has_passed(int const * const timestamp) {
	for (int i = 0; i < g_thread_count; ++i) {
		if (timestamp[i] >= g_iter[i]) {
			return false;
		}
	}
	return true;
}

inline void gc_clean(int thread_id) {
	gc_list_t *thrash = g_thrash[thread_id];
	if (thrash->cur == thrash->head) {
		return;
	}

	// Free nodes
	gc_node_t *cur = thrash->head->next, *tmp;
	while (cur != thrash->tail && gc_has_passed(cur->timestamp)) {
		tmp = cur;
		cur = cur->next;
		linked_list_node_free(tmp->data);
		free(tmp->timestamp);
		free(tmp);
	}

	// Update list
	thrash->head->next = cur;
	if (cur == thrash->tail) {
		thrash->cur = thrash->head;
	}
}

inline void gc_delete(int thread_id, node_t * const node) {
	gc_node_t *wrapper = malloc(sizeof(gc_node_t));
	wrapper->timestamp = malloc(sizeof(int)*g_thread_count);
	memcpy(wrapper->timestamp, g_iter, sizeof(int)*g_thread_count);
	wrapper->data = node;
	wrapper->next = g_thrash[thread_id]->tail;
	g_thrash[thread_id]->cur->next = wrapper;
	g_thrash[thread_id]->cur = wrapper;
}

void gc_free() {
	gc_node_t *cur, *tmp;
	gc_list_t *thrash;
	for (int i = 0; i < g_thread_count; ++i) {
		thrash = g_thrash[i];
		cur = thrash->head->next;
		while (cur != thrash->tail) {
			tmp = cur;
			cur = cur->next;
			linked_list_node_free(tmp->data);
			free(tmp->timestamp);
			free(tmp);
		}
		free(thrash->head->timestamp);
		free(thrash->head);
		free(thrash->tail->timestamp);
		free(thrash->tail);
		free(g_thrash[i]);
	}
	free(g_thrash);
	free(g_iter);
}
