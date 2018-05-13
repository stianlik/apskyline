#define GC_ACTIVE
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "bnl.h"
#include "dataprovider.h"
#include "test.h"
#include "wtime.c"
#include "linked_list_iter.h"
#include "comparator.c"
#include "gc.h"
#include "summary.c"

inline void bnl_process_point(linked_list_iter_t*,int,float*);
void bnl_skyline_worker(bnl_skyline_t input, int thread_id, int thread_count, linked_list_t *window);
inline int bnl_increment(int);
inline int bnl_decrement(int);

int main(int argc, char* argv[]) {

	setbuf(stdout, NULL);
	
	if (argc < 2) {
		printf("%s THREAD_COUNT\n", argv[0]);
		exit(0);
	}

	// Unit testing
	test_comparator(compare);
	test_algorithm(bnl_skyline, 2);

	// Configure and allocate algorithm
	bnl_skyline_t config;
	config.thread_count = atoi(argv[1]);
	config.d = 10;
	config.n = 100000;
	config.data = malloc(sizeof(float)*config.d*config.n);
	dataprovider_read("../data/10d_100k_uniform/data.txt", config.d, config.n, config.data);

	// Prepare experiment
	const int experiment_n = 10;
	float experiment_time[experiment_n];
	float experiment_skyline_size[experiment_n];

	// Run benchmark
	for (int i = -1; i < experiment_n; ++i) {

		// Perform algorithm
		linked_list_t *output = linked_list_create(10);
		double starttime = wtime();
		bnl_skyline(output, config);
		double endtime = wtime();

		// Log results
		if (i >= 0) {
			experiment_time[i] = endtime - starttime;
			experiment_skyline_size[i] = linked_list_length(output);
		}

		linked_list_free(output);
	}
	summary("C", experiment_n, config.thread_count, experiment_time, experiment_skyline_size);

	free(config.data);

	return 0;
}

void bnl_skyline(linked_list_t *output, bnl_skyline_t input) {
	omp_set_num_threads(input.thread_count);
	gc_init(input.thread_count);
	#pragma omp parallel for default(none) shared(input, output)
	for (int thread_id = 0; thread_id < input.thread_count; thread_id++) {
		bnl_skyline_worker(input, thread_id, input.thread_count, output);
	}
	gc_free();
}

void bnl_skyline_worker(bnl_skyline_t input, int thread_id, int thread_count, linked_list_t *output) {
	linked_list_iter_t *iter = linked_list_iter_create(thread_id, output);
	float *point = malloc(sizeof(float) * input.d);
	for (int i = thread_id; i < input.n; i += thread_count) {
		memcpy(point, &input.data[i*input.d], sizeof(float) * input.d);
		bnl_process_point(iter, input.d, point);
	}
	linked_list_iter_free(iter);
	free(point);
}

inline void bnl_process_point(linked_list_iter_t *iter, int d, float *point) {
	point_relationship_t rel;
	node_t *node;
	restart:
	linked_list_iter_reset(iter);
	while (linked_list_iter_not_tail(iter)) {
		rel = compare(d, point, iter->cur->data);
		if (rel == DOMINATED) {
			return;
		}
		else if (rel == DOMINATES) {
			if (!linked_list_iter_remove(iter)) {
				goto restart;
			}
			continue;
		}
		else {
			linked_list_iter_next(iter);
		}
	}
	node = linked_list_node_create(iter->list, point);
	if (!linked_list_iter_add(iter, node)) {
		free(node);
		goto restart;
	}
}

inline int bnl_increment(int i) {
	return ++i;
}

inline int bnl_decrement(int i) {
	return --i;
}
