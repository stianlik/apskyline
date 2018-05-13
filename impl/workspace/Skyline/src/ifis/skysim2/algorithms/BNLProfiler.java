package ifis.skysim2.algorithms;

public class BNLProfiler {
    private long insertions = 0;
    private long deletions = 0;
    private long moves = 0;
    private long drops = 0;
    private long cpuCost = 0;

    public void update(long insertions, long deletions, long moves, long drops, long cpuCost) {
	this.insertions += insertions;
	this.deletions += deletions;
	this.moves += moves;
	this.drops += drops;
	this.cpuCost += cpuCost;
    }

    public static void updateProfiler(BNLProfiler profiler, long insertions, long deletions, long moves, long drops, long cpuCost) {
	if (profiler != null) {
	    profiler.update(insertions, deletions, moves, drops, cpuCost);
	}
    }

    public long getInsertions() {
	return insertions;
    }

    public long getDeletions() {
	return deletions;
    }

    public long getMoves() {
	return moves;
    }

    public long getDrops() {
	return drops;
    }

    public long getCpuCost() {
	return cpuCost;
    }

    @Override
    public String toString() {
	return String.format("Insertions: %10d%n" +
		             "Deletions:  %10d%n" +
		             "Moves:      %10d%n" +
			     "Drops:      %10d%n" +
			     "CPU cost:   %10d", insertions, deletions, moves, drops, cpuCost);
    }
}
