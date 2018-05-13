

char* to_string(int n, float* results) {
	int pos = 0;
	char *resultstr = malloc(sizeof(char) * (n * 20 + 5));
	for (int i = 0; i < n; ++i) {
		pos += sprintf(&resultstr[pos], "%g ", results[i]);
	}
	return resultstr;
}

void summary(
	char *experiment_name, 
	int experiment_count, 
	int thread_count, 
	float* experiment_time,
	float* experiment_skyline
) {

	char* timestr = to_string(experiment_count, experiment_time);
	char* skylinestr = to_string(experiment_count, experiment_skyline);

	printf("# name: test\n# type: scalar struct\n# length: 3\n\n");
	printf("# name: name\n# type: string\n# elements: 1\n# length: %d\n%s\n\n", (int) strlen(experiment_name), experiment_name);
	printf("# name: value\n# type: scalar\n%d\n\n", thread_count);
	printf("# name: result\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", experiment_count, timestr);
	printf("# name: skyline_size\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", experiment_count, skylinestr);

	free(timestr);
}
