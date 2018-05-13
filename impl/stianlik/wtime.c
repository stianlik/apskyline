#include <sys/time.h>

/**
 * Returns seconds since the epoch
 */
double wtime() {
	struct timeval time;
	assert( gettimeofday(&time, NULL) == 0 );
	double seconds = ((double) time.tv_sec) + ((double) time.tv_usec)*1e-6;
	return seconds;
}
