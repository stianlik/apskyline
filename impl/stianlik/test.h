#include <stdbool.h>
#include "bnl.h"

void test_comparator(point_relationship_t (*compare)(int, const float * const, const float * const)) {
	float a[] = { 1.0, 1.0, 1.0 };
	float b[] = { 1.0, 2.0, 1.0 }; // dominates a
	float c[] = { 2.0, 0.5, 1.0 }; // incomparable to a
	float d[] = { 1.0, 1.0, 1.0 }; // equal to a
	assert( compare(3,a,b) == DOMINATED && "a is dominated by b");
	assert( compare(3,b,a) == DOMINATES && "b dominates a");
	assert( compare(3,a,c) == INCOMPARABLE && "a incomparable to c" );
	assert( compare(3,c,a) == INCOMPARABLE && "c incomparable to a" );
	assert( compare(3,a,d) == EQUAL && "a equal to d" );
	assert( compare(3,d,a) == EQUAL && "d equal to a" );
}

void test_algorithm(void (*algorithm)(linked_list_t*,bnl_skyline_t), int thread_count) {
	// Input and expected output
	float input[] = {
		50.0, 3.0, 2.0, // dominated by s1
		51.0, 5.0, 1.0, // dominated by s1
		51.0, 5.0, 2.0, // s1
		52.0, 4.0, 2.0, // dominated by s2
		53.0, 4.0, 2.0, // s2
		50.0, 1.0, 3.0, // s3
		51.0, 3.0, 2.0  // dominated by s1
	};
	int expected_len = 3;
	int expected[] = { 2, 4, 5 };

	// Perform skyline operation
	bnl_skyline_t skyline;
	skyline.data = input;
	skyline.d = 3;
	skyline.n = 7;
	skyline.thread_count = thread_count;
	linked_list_t *window = linked_list_create(3);
	algorithm(window, skyline);

	// Verify result
	float *needle;
	assert(linked_list_length(window) == 3);
	for (int i = 0; i < expected_len; ++i) {
		needle = &input[expected[i]*skyline.d];
		assert( linked_list_find(window, skyline.d, needle) );
	}
	linked_list_free(window);
}
