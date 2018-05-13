package ifis.skysim2.simulator;

import java.util.Formatter;

public class AlgorithmSummaryStatistics {

    protected SimulatorConfiguration config;
    protected String algorithmName;
    protected int sizeOfData;
    protected double sizeTotal;
    protected double timeGenerateTotal;
    protected double timeSortTotal;
    protected double timeComputeTotal;
    protected double timePreprocessTotal;
    protected double timeReorgTotal;
    protected long cpuCostTotal;
    protected double cpuPerTuple;
    protected double timePerTuple;
    protected double avgTimePerCompTotalNS;
    protected double ioCostTotal;

    @Override
    public String toString() {
        return String.format("%19s | %13s | %4d | %12.0f | %9.2f ms | %9.2f ms | %7.2f ms | %9.2f ms | %9.2f ms | %11d | %10.1f | %12.2f ns | %13.0f | %12s |",
                getAlgorithmName(),
                formatN(getSizeOfData()),
                config.getD(),
                getSizeTotal(),
                getTimeGenerateTotal(),
                getTimeSortTotal(),
                getTimePreprocessTotal(),
                getTimeComputeTotal(),
                getTimeReorgTotal(),
                getCpuCostTotal(),
                getCpuPerTuple(),
                getAvgTimePerCompTotalNS(),
                getIoCostTotal(),
                config.getDataGenerator().getShortName());
    }

    /**
     * Formats the size String (e.g. 10K, 100K, etc...)
     * @param n
     * @return
     */
    public String formatN(int n) {
        if (n % 1000000000 == 0) {
            // millions
            return (n / 1000000000) + "G";
        }
        if (n % 1000000 == 0) {
            // millions
            return (n / 1000000) + "M";
        }
        if (n % 1000 == 0) {
            // millions
            return (n / 1000) + "K";
        }
        return String.valueOf(n);
    }

    public static String getHeader() {
        return "              trial | relation size | dim. | skyline size |   time (gen) |  time (sort) | time (pre) |  time (comp) | time (reorg) | comparisons | comp/tuple | time/comparison |       io cost |      datagen |";
    }

    public static String getDivider() {
        return "--------------------+---------------+------+--------------+--------------+--------------+------------+--------------+--------------+-------------+------------+-----------------+---------------+--------------+";
    }

    public String toStringPercentage(double referenceTime) {
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb);

        // Explicit argument indices may be used to re-order output.

        formatter.format("%19s | %9.2f %% | %13d | %12.0f | %9.2f ms | %9.2f ms | %9.2f ms | %9.2f ms | %11d | %10.1f | %12.2f ns | %13.0f |",
                getAlgorithmName(),
                (100 * getTimeComputeTotal() / referenceTime),
                getSizeOfData(),
                getSizeTotal(),
                getTimeGenerateTotal(),
                getTimeSortTotal(),
                getTimeComputeTotal(),
                getTimeReorgTotal(),
                getCpuCostTotal(),
                getCpuPerTuple(),
                getAvgTimePerCompTotalNS(),
                getIoCostTotal());
        return sb.toString();
    }

    public AlgorithmSummaryStatistics setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
        return this;
    }

    public AlgorithmSummaryStatistics setSizeOfData(int sizeOfData) {
        this.sizeOfData = sizeOfData;
        return this;
    }

    public AlgorithmSummaryStatistics setSizeTotal(double sizeTotal) {
        this.sizeTotal = sizeTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setTimeGenerateTotal(double timeGenerateTotal) {
        this.timeGenerateTotal = timeGenerateTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setTimeSortTotal(double timeSortTotal) {
        this.timeSortTotal = timeSortTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setTimeComputeTotal(double timeComputeTotal) {
        this.timeComputeTotal = timeComputeTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setTimeReorgTotal(double timeReorgTotal) {
        this.timeReorgTotal = timeReorgTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setTimePreprocessTotal(double timePreprocessTotal) {
        this.timePreprocessTotal = timePreprocessTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setCpuPerTuple(double cpuPerTuple) {
        this.cpuPerTuple = cpuPerTuple;
        return this;
    }

    public AlgorithmSummaryStatistics setTimePerTuple(double timePerTuple) {
        this.timePerTuple = timePerTuple;
        return this;
    }

    public AlgorithmSummaryStatistics setAvgTimePerCompTotalNS(double avgTimePerCompTotalNS) {
        this.avgTimePerCompTotalNS = avgTimePerCompTotalNS;
        return this;
    }

    public AlgorithmSummaryStatistics setIoCostTotal(double ioCostTotal) {
        this.ioCostTotal = ioCostTotal;
        return this;
    }

    public AlgorithmSummaryStatistics setCpuCostTotal(long cpuCostTotal) {
        this.cpuCostTotal = cpuCostTotal;
        return this;
    }

    public SimulatorConfiguration getBaseConfig() {
        return getConfig();
    }

    public AlgorithmSummaryStatistics setBaseConfig(SimulatorConfiguration config) {
        this.config = config;
        return this;
    }

    public SimulatorConfiguration getConfig() {
        return config;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getSizeOfData() {
        return sizeOfData;
    }

    public double getSizeTotal() {
        return sizeTotal;
    }

    public double getTimeGenerateTotal() {
        return timeGenerateTotal;
    }

    public double getTimePreprocessTotal() {
        return timePreprocessTotal;
    }

    public double getTimeSortTotal() {
        return timeSortTotal;
    }

    public double getTimeComputeTotal() {
        return timeComputeTotal;
    }

    public double getTimeReorgTotal() {
        return timeReorgTotal;
    }

    public long getCpuCostTotal() {
        return cpuCostTotal;
    }

    public double getCpuPerTuple() {
        return cpuPerTuple;
    }

    public double getTimePerTuple() {
        return timePerTuple;
    }

    public double getAvgTimePerCompTotalNS() {
        return avgTimePerCompTotalNS;
    }

    public double getIoCostTotal() {
        return ioCostTotal;
    }
}
