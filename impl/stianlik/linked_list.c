#include "linked_list.h"

inline node_t *linked_list_node_create(linked_list_t *list, float data[]) {
	node_t *node = malloc(sizeof(node_t) + sizeof(float)*list->d);
	memcpy(node->data, data, sizeof(float)*list->d);
	node->deleted = false;
	omp_init_lock(&node->lock);
	return node;
}

inline node_t *linked_list_node_create_empty() {
	node_t *node = malloc(sizeof(node_t));
	node->deleted = false;
	omp_init_lock(&node->lock);
	return node;
}

linked_list_t *linked_list_create(const int d) {
	linked_list_t *list = malloc(sizeof(linked_list_t));
	list->head = linked_list_node_create_empty();
	list->tail = linked_list_node_create_empty();
	list->d = d;
	list->head->next = list->tail;
	return list;
}

void linked_list_node_free(node_t *node) {
	omp_destroy_lock(&node->lock);
	free(node);
}

void linked_list_free(linked_list_t *list) {
	node_t *cur = list->head;
	node_t *tmp;
	while (cur != list->tail) {
		tmp = cur;
		cur = cur->next;
		linked_list_node_free(tmp);
	}
	free(list->tail);
	free(list);
}

int linked_list_length(linked_list_t *list) {
	node_t *cur = list->head->next;
	int count = 0;
	while (cur != list->tail) {
		cur = cur->next;
		++count;
	}
	return count;
}

node_t* linked_list_find(linked_list_t *list, int d, float *point) {
	node_t *cur = list->head;
	bool match;
	while ( (cur = cur->next) != list->tail) {
		match = true;
		for (int i = 0; i < d; ++i) {
			if (cur->data[i] != point[i]) {
				match = false;
			}
		}
		if (match) {
			return cur;
		}
	}
	return NULL;
}
