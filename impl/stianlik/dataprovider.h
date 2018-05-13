#include <stdio.h>

void dataprovider_read(const char* filename, int d, int n, float *data) {
	int index;
	FILE *file;
	assert( (file = fopen(filename, "r")) != NULL );
	for (int i = 0; i < n; ++i) {
		assert( fscanf(file, "%*d") == 0 );
		for (int j = 0; j < d; ++j) {
			index = i*d + j;
			assert( fscanf(file, "%f", &data[index]) == 1);
		}
	}
	fclose(file);
}

void dataprovider_print(int d, int n, float *data) {
	int index;
	for (int i = 0; i < n; ++i) {
		printf("%d", i+1);
		for (int j = 0; j < d; ++j) {
			index = i*d + j;
			printf(" %.7f", data[index]);
		}
		printf("\n");
	}
}
