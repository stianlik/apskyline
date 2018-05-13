
inline point_relationship_t compare(const int d, const float * const a, const float * const b) {

	point_relationship_t rel = EQUAL;
	for (int i = 0; i < d; ++i) {
		if (a[i] > b[i]) {
			if (rel == DOMINATED) {
				return INCOMPARABLE;
			}
			else {
				rel = DOMINATES;
			}
		}
		else if (a[i] < b[i]) {
			if (rel == DOMINATES) {
				return INCOMPARABLE;
			}
			else {
				rel = DOMINATED;
			}
		}
	}

	return rel;
}
