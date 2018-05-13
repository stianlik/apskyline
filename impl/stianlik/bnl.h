#ifndef BNL_H
#define BNL_H
#include "linked_list.h"
#include "linked_list_iter.h"

#define DOMINATES 1
#define DOMINATED 2
#define INCOMPARABLE 3
#define EQUAL 4

typedef struct bnl_skyline_t {
	float *data;
	int d, n, thread_count;
} bnl_skyline_t;

typedef short int point_relationship_t;

point_relationship_t compare(int, const float * const, const float * const);
void bnl_skyline(linked_list_t*,bnl_skyline_t);

#endif
