#ifndef LINKED_LIST_H
#define LINKED_LIST_H
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <omp.h>

typedef struct node_t {
	struct node_t *next;
	bool deleted;
	omp_lock_t lock;
	float data[];
} node_t;

typedef struct linked_list_t {
	node_t *head;
	node_t *tail;
	int d;
} linked_list_t;

node_t *linked_list_node_create(linked_list_t *list, float data[]);
node_t *linked_list_node_create_empty();
void linked_list_node_free(node_t *node);

linked_list_t *linked_list_create(const int d);
void linked_list_free(linked_list_t *list);
int linked_list_length(linked_list_t *list);
node_t* linked_list_find(linked_list_t *list, int d, float *point);

#endif
